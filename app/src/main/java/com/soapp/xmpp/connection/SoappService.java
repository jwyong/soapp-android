package com.soapp.xmpp.connection;

import android.annotation.SuppressLint;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;
import android.os.IBinder;
import android.provider.ContactsContract;
import android.service.notification.StatusBarNotification;

import com.soapp.SoappApi.ApiModel.PostPhoneGetJidModel;
import com.soapp.SoappApi.ApiModel.SyncContactsModel;
import com.soapp.SoappApi.Interface.IndiAPIInterface;
import com.soapp.SoappApi.RetrofitAPIClient;
import com.soapp.global.GlobalVariables;
import com.soapp.global.MiscHelper;
import com.soapp.global.Preferences;
import com.soapp.global.StateCheck;
import com.soapp.setup.Soapp;
import com.soapp.sql.DatabaseHelper;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import io.reactivex.Observable;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.observers.DefaultObserver;

public class SoappService extends Service {

    public static DatabaseHelper databaseHelper = DatabaseHelper.getInstance();
    StateCheck.Listener myListener = new StateCheck.Listener() {
        public void onBecameForeground() {
            NotificationManager notificationManager =
                    (NotificationManager) Soapp.getInstance().getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);

            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) { //Android
                // 7 and above
                StatusBarNotification[] statusBarNotification =
                        notificationManager.getActiveNotifications();
                Observable.create((ObservableOnSubscribe<StatusBarNotification[]>) emitter -> emitter.onNext(statusBarNotification))
                        .subscribe(new DefaultObserver<StatusBarNotification[]>() {
                            @SuppressLint("NewApi")
                            @Override
                            public void onNext(StatusBarNotification[] statusBarNotifications) {
                                List<StatusBarNotification> list =
                                        new ArrayList<>(Arrays.asList(statusBarNotification));

                                list.removeIf(statusBarNotification1 -> statusBarNotification1.getId() == 3 || statusBarNotification1.getId() == 666);
                                if (list.size() == 1) {
                                    //clear notifications on going into foreground (appt)
                                    notificationManager.cancel(list.get(0).getId() == 1 ? 1 : 2);
                                } else if (list.size() == 2) {
                                    boolean apptclear = true;
                                    boolean chatclear = true;
                                    for (StatusBarNotification bar : list) {
                                        if (bar.getId() != 1 && bar.getId() != 2) {
                                            if (bar.getTag().equals("chat")) {
                                                chatclear = false;
                                            } else if (bar.getTag().equals("appt")) {
                                                apptclear = false;
                                            }
                                        }
                                    }
                                    if (apptclear) {
                                        notificationManager.cancel(2);
                                    }

                                    if (chatclear) {
                                        notificationManager.cancel(1);
                                    }
                                }

                            }

                            @Override
                            public void onError(Throwable e) {

                            }

                            @Override
                            public void onComplete() {
                            }
                        });
            } else {
                //clear push noti for 6 below (appt)
                if (notificationManager != null) {
                    notificationManager.cancelAll();
                }
            }
        }

        public void onBecameBackground() {
        }
    };

    private MiscHelper miscHelper = new MiscHelper();
    private Preferences preferences = Preferences.getInstance();
    private String fire;
    private Timer timer = new Timer();

    //contact change observer
    private ContentObserver mObserver = new ContentObserver(new Handler()) {
        @Override
        public boolean deliverSelfNotifications() {
            return true;
        }

        @Override
        //working for both "adding new contact" and "editting existing contact"
        public void onChange(boolean selfChange) {
            super.onChange(selfChange);
            if (fire.equals("NF")) {
                fire = "F";
                //get the last updated contact
                Cursor cursor = getContentResolver().query(ContactsContract.CommonDataKinds.Phone
                                .CONTENT_URI, null, null, null,
                        ContactsContract.CommonDataKinds.Phone.CONTACT_LAST_UPDATED_TIMESTAMP +
                                " Desc");

                //check added/editted phone number with server
                if (cursor.moveToNext()) {
                    String updatedContact =
                            cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));

                    //don't do anything if null
                    if (updatedContact == null) {
                        return;
                    }

                    updatedContact = updatedContact.replace(" ", "").replace("-", "");
                    final String phonename =
                            cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));

                    final String access_token = preferences.getValue(SoappService.this,
                            GlobalVariables.STRPREF_ACCESS_TOKEN);

                    //add phonenumber to list
                    ArrayList<String> phoneNumberList = new ArrayList<>();
                    phoneNumberList.add(updatedContact);

                    //build retrofit
                    IndiAPIInterface indiAPIInterface =
                            RetrofitAPIClient.getClient().create(IndiAPIInterface.class);
                    String size =
                            miscHelper.getDeviceDensity(Soapp.getInstance().getApplicationContext());
                    PostPhoneGetJidModel model = new PostPhoneGetJidModel(phoneNumberList, size);

                    retrofit2.Call<List<SyncContactsModel>> call =
                            indiAPIInterface.syncContacts(model
                            , "Bearer " + access_token);
                    call.enqueue(new retrofit2.Callback<List<SyncContactsModel>>() {
                        @Override
                        public void onFailure(retrofit2.Call<List<SyncContactsModel>> call,
                                              Throwable t) {
                            new MiscHelper().retroLogFailure(t, "deliverSelfNotifications ", "JAY");
                        }

                        @Override
                        public void onResponse(retrofit2.Call<List<SyncContactsModel>> call,
                                               retrofit2.Response<List<SyncContactsModel>> response) {
                            if (!response.isSuccessful()) {

                                new MiscHelper().retroLogUnsuc(response,
                                        "deliverSelfNotifications ", "JAY");
                                return;
                            }

                            List<SyncContactsModel> userList = response.body();
                            final int listSize = userList.size();

                            if (listSize > 0) {
                                String phone = userList.get(0).getPhone_number();
                                String imageURL = userList.get(0).getResource_url();
                                String jid = userList.get(0).getUser_jid();
                                String image_data = userList.get(0).getImage_data();

                                databaseHelper.updateNewContactToContactRoster(jid, phone,
                                        phonename, imageURL,
                                        image_data);
                            }
                        }
                    });
                }
                cursor.close();

                timer.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        fire = "NF";
                    }
                }, 1000);
            }
        }

        @Override
        public void onChange(boolean selfChange, Uri uri) {
            super.onChange(selfChange, uri);
        }
    };

    @Override
    public void onCreate() {
        super.onCreate();

//        startForeground(998, new Notification.Builder(this).build());
        StateCheck.init(Soapp.getInstance());
        StateCheck.get(this).addListener(myListener);
        fire = "NF";
        getContentResolver().registerContentObserver(ContactsContract.Contacts.CONTENT_URI, true,
                mObserver);
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        return new LocalBinder<SoappService>(this);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return Service.START_STICKY;
    }

    @Override
    public void onDestroy() {
        //set firsttime boolean to false

        //shut down connection in order to reset booleans

        //set connection to null after reset booleans for next init

        //get rid of observer for next starting of service
        getContentResolver().unregisterContentObserver(mObserver);

        super.onDestroy();

        //restart service
        new Thread(() -> {
            Intent xmppService = new Intent(Soapp.getInstance(), SoappService.class);
            Soapp.getInstance().startService(xmppService);
        });
//        Intent xmppService = new Intent(Soapp.getInstance(), SoappService.class);
//        Soapp.getInstance().startService(xmppService);
    }

}