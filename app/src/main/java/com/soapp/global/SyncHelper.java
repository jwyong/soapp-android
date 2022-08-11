package com.soapp.global;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;
import android.util.Base64;
import android.view.View;
import android.widget.Toast;

import com.soapp.R;
import com.soapp.SoappApi.ApiModel.AppointmentMemStatus;
import com.soapp.SoappApi.ApiModel.GetGrpApptModel;
import com.soapp.SoappApi.ApiModel.GetOldGrpMemberModel;
import com.soapp.SoappApi.ApiModel.GetOldGrpModel;
import com.soapp.SoappApi.ApiModel.PostPhoneGetJidModel;
import com.soapp.SoappApi.ApiModel.SPgetAllResModel;
import com.soapp.SoappApi.ApiModel.SyncContactsModel;
import com.soapp.SoappApi.Interface.GetOldGrp;
import com.soapp.SoappApi.Interface.GetProfileThumb;
import com.soapp.SoappApi.Interface.IndiAPIInterface;
import com.soapp.SoappApi.RetrofitAPIClient;
import com.soapp.registration.SyncContact;
import com.soapp.sql.DatabaseHelper;
import com.soapp.xmpp.PubsubHelper.PubsubNodeCall;
import com.soapp.xmpp.SingleChatHelper.SingleChatStanza;

import java.util.ArrayList;
import java.util.List;

import androidx.lifecycle.MutableLiveData;
import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;
import ir.mirrajabi.rxcontacts.Contact;
import ir.mirrajabi.rxcontacts.RxContacts;
import okhttp3.MediaType;
import okhttp3.RequestBody;

public class SyncHelper {

    private static final String TAG = "wtf";
    public Context context;
    public Activity activity;
    public DatabaseHelper databaseHelper = DatabaseHelper.getInstance();
    public String numbers = "1,";
    public Integer contacts, j = 0, k = 0, r = 0, oldGrps;
    public Preferences preferences = Preferences.getInstance();
    public ArrayList<String> phoneNumberList = new ArrayList<>();
    public MiscHelper miscHelper = new MiscHelper();
    public SingleChatStanza singleChatStanza = new SingleChatStanza();
    public PubsubNodeCall pubsubNodeCall = new PubsubNodeCall();
    boolean noPhoneNumber;

    private ProgressDialog dialog;
    public String syncItem;
    public Boolean failMsg = true;

    //live data for UI updating of progress
    private MutableLiveData<Integer> phoneBookSyncLD;
    private int currentContact = 0;
    private int totalContacts = 0;
    private int currentgrpNum = 0;
    public List<String> phoneNumberList2 = new ArrayList<>();
    float totalGrpNum;
    float countGrpNum;

    public SyncHelper(Context context, String syncItem) {
        this.context = context;
        this.activity = (Activity) context;
        this.syncItem = syncItem;
    }

    //get contacts from user's phone book (requires permission)
    public void getContactsFromPhonebook() {
        noPhoneNumber = false;

        Observable<List<Contact>> observable = RxContacts.fetch(context)
                .toSortedList(Contact::compareTo)
                .observeOn(Schedulers.io())
                .toObservable();

        observable.observeOn(Schedulers.io())
                .subscribeOn(Schedulers.io())
                .subscribe(new Observer<List<Contact>>() {
                    @Override
                    public void onSubscribe(Disposable d) {
                    }

                    @Override
                    public void onNext(List<Contact> contacts) {
                        for (Contact a : contacts) {
                            phoneNumberList2.addAll(a.getPhoneNumbers());
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        //not retro - won't hv errors
                    }

                    @Override
                    public void onComplete() {
                        if (phoneNumberList2.size() > 0) {
                            getJidFromServer();
                        } else {
                            activity.runOnUiThread(() -> {
                                if (dialog != null && dialog.isShowing()) {
                                    dialog.dismiss();
                                }
                            });
                        }
                    }
                });
    }

    //post phone numbers to server to get back user profile
    private void getJidFromServer() {
        // no phone number no need to post to Server
        if (phoneNumberList2.size() > 0) {
            String access_token = preferences.getValue(context, GlobalVariables.STRPREF_ACCESS_TOKEN);
            String size = miscHelper.getDeviceDensity(context);
            PostPhoneGetJidModel model = new PostPhoneGetJidModel(phoneNumberList2, size);

            //build retrofit
            IndiAPIInterface indiAPIInterface = RetrofitAPIClient.getClient().create(IndiAPIInterface.class);
            Observable<List<SyncContactsModel>> call = indiAPIInterface.syncContacts2(model, "Bearer " + access_token);

            call.observeOn(Schedulers.io())
                    .subscribeOn(Schedulers.io())
                    .subscribe(new Observer<List<SyncContactsModel>>() {
                        @Override
                        public void onSubscribe(Disposable d) {
                        }

                        @Override
                        public void onNext(List<SyncContactsModel> syncContactsModels) {
                            int l = syncContactsModels.size();

                            //create variables before start looping
                            String phoneName, phoneNumber, imageURL, jid, image_data;

//                            commented out on august 2 2018 by ryan
//                            Bitmap icon = BitmapFactory.decodeResource(Soapp.getInstance().getApplicationContext().getResources(),
//                                    R.drawable.default_img_240);

                            for (k = 0; k < l; k++) {
                                phoneNumber = syncContactsModels.get(k).getPhone_number();
                                imageURL = syncContactsModels.get(k).getResource_url();
                                jid = syncContactsModels.get(k).getUser_jid();
                                image_data = String.valueOf(syncContactsModels.get(k).getImage_data());

                                //get phone name from phone book if got
                                if (getContactNameRegister(context, phoneNumber) != null) {
                                    phoneName = getContactNameRegister(context, phoneNumber);
                                } else {
                                    phoneName = syncContactsModels.get(k).getName();
                                }

                                //save default image to all DBs and try download
                                databaseHelper.updateNewContactToContactRoster(jid, phoneNumber, phoneName, imageURL, image_data);

                            }
                        }

                        @Override
                        public void onError(Throwable e) {
                            new MiscHelper().retroLogFailure(e, "getJidFromServer", "JAY");

                            if (syncItem.equals("SyncContact")) { //from registration
                                //2 = seen tute but failed syncing
                                SyncContact.syncStatus = 2;
                                activity.runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        if (SyncContact.btnNext != null) {
                                            SyncContact.btnNext.setVisibility(View.VISIBLE);
                                        }
                                    }
                                });
                            }

                            activity.runOnUiThread(() -> {
                                if (dialog != null && dialog.isShowing()) {
                                    dialog.dismiss();
                                }
                                Toast.makeText(context, R.string.onfailure, Toast.LENGTH_SHORT).show();
                            });
                        }

                        @Override
                        public void onComplete() {
                            if (!syncItem.equals("SyncContact")) {
                                activity.runOnUiThread(() -> {
                                    Toast.makeText(context, R.string.sync_contact_done, Toast.LENGTH_SHORT).show();

                                    if (dialog != null && dialog.isShowing()) {
                                        dialog.dismiss();
                                    }
                                });
                            }
                        }
                    });
        }
    }

    // syncOldGroup
    public void getOldGroupsFunc() {
        //create default variables before loop (member and room profile img)
        final String access_token = preferences.getValue(context, GlobalVariables.STRPREF_ACCESS_TOKEN);
        final long currentDate = System.currentTimeMillis();
        String size = miscHelper.getDeviceDensity(context);

        //build retrofit
        GetOldGrp clientOldGrp = RetrofitAPIClient.getClient().create(GetOldGrp.class);
        Observable<List<GetOldGrpModel>> callOldGrp = clientOldGrp.getOldGrps2(size, "Bearer " + access_token);

        GetProfileThumb client1 = RetrofitAPIClient.getClient().create(GetProfileThumb.class);

        callOldGrp.observeOn(Schedulers.io())
                .subscribeOn(Schedulers.io())
                .flatMap((Function<List<GetOldGrpModel>, ObservableSource<List<SPgetAllResModel>>>) roomList -> {

                    oldGrps = roomList.size();
                    //declare variables first before looping
                    String room_id, room_name, room_resource_url, user_jid, admin_type, member_jid, color, room_image_data;
                    List<GetOldGrpMemberModel> member_list;

                    //build retrofit
                    StringBuilder listString = new StringBuilder();
                    ArrayList<String> uniqueMemberList = new ArrayList<>();

                    //start looping groups
                    String grpUpdateMsg;
                    for (currentgrpNum = 0; currentgrpNum < oldGrps; currentgrpNum++) {
//                        update progress msg for group looping
                        activity.runOnUiThread(() -> {
                            if (syncItem.equals("SyncContact")) { //from registration
                                countGrpNum = (currentgrpNum + 1);
                                totalGrpNum = countGrpNum / oldGrps;
                                float finalTotalNum = totalGrpNum * 100;
                                SyncContact.logo_setting_sync.setProgressValue((int) finalTotalNum);

                            } else { //not from registration
                                if (dialog != null) {
                                    dialog.setMessage("Retrieving groups " + currentgrpNum + " of " + oldGrps);
                                }
                            }
                        });

                        room_id = roomList.get(currentgrpNum).getRoom_id();
                        room_name = roomList.get(currentgrpNum).getRoom_name();
                        room_resource_url = roomList.get(currentgrpNum).getResource_url();
                        room_image_data = roomList.get(currentgrpNum).getImage_data();

                        byte[] roomImgByte = null;
                        if (room_image_data != null && !room_image_data.equals("Empty")) {
                            //create group in roster list with default image if not exist yet
                            roomImgByte = Base64.decode(roomList.get(currentgrpNum).getImage_data(), Base64.DEFAULT);
                        }
                        databaseHelper.createNewGrpChat(room_id, currentDate, room_name, roomImgByte, room_resource_url, false, null, true);


                        //loop member info from list
                        member_list = roomList.get(currentgrpNum).getUsers();

                        //get userjid before looping
                        user_jid = preferences.getValue(context, GlobalVariables.STRPREF_USER_ID);
                        for (int i2 = 0; i2 < member_list.size(); i2++) { //loop grp members
                            //admin_type member or admin
                            admin_type = member_list.get(i2).getType();
                            member_jid = member_list.get(i2).getUser_jid();
                            if (member_jid.equals(user_jid)) { //self, check for admin status
                                databaseHelper.updateSelfAdminCL(room_id, admin_type);
                            } else { //add members other than self
                                color = MiscHelper.getColorForGrpDisplayName(i2);

                                //add jid to grpmem first
                                databaseHelper.checkAndAddJidToGrpMem(member_jid, room_id, admin_type, color);

                                if (databaseHelper.getContactRosterExist(member_jid) == 0) { //user
                                    if (!uniqueMemberList.contains(member_jid)) {
                                        uniqueMemberList.add(member_jid);
                                        listString.append(member_jid).append(",");
                                    }
                                }
                            }
                        }

                        //get appt details
                        if (roomList.get(currentgrpNum).getRoom_appointments() != null) {
                            for (GetGrpApptModel appt : roomList.get(currentgrpNum).getRoom_appointments()) {
                                databaseHelper.incomingApptDetails(room_id, null, appt.getAppointment_title(),
                                        appt.getLocation_map_url(), appt.getLocation_name(), appt.getLocation_lon(),
                                        appt.getLocation_lat(), appt.getAppointment_date(), appt.getAppointment_time(),
                                        "", "", "", false,
                                        null, appt.getAppointment_id(), "99");

                                for (AppointmentMemStatus status : appt.getAppointment_users()) {
                                    databaseHelper.checkAndAddStatusToGrpMemStatus(status.getUser_jid(), room_id, appt.getAppointment_id(), status.getAppointment_status());
                                }
                            }
                        }
                    }

                    if (!listString.toString().equals("")) {
                        RequestBody sizeRB = RequestBody.create(MediaType.parse("text/plain"), size);
                        RequestBody jidListString = RequestBody.create(MediaType.parse("text/plain"), listString.toString());

                        return client1.spGetAllRes2("Bearer " + access_token, jidListString, sizeRB);
                    } else {
                        return new Observable<List<SPgetAllResModel>>() {
                            @Override
                            protected void subscribeActual(Observer<? super List<SPgetAllResModel>> observer) {
                                observer.onComplete();
                            }
                        };
                    }
                })
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
                .subscribe(new Observer<List<SPgetAllResModel>>() {
                    @Override
                    public void onSubscribe(Disposable d) {
                    }

                    @Override
                    public void onNext(List<SPgetAllResModel> spList) {
                        for (int i = 0; i < spList.size(); i++) {
                            String displayName = spList.get(i).getName();
                            String phoneNumber = spList.get(i).getPhone_number();
                            String image_data = spList.get(i).getImage_data();
                            String member_resource_url = spList.get(i).getResource_url();
                            String finalMember_jid = spList.get(i).getId();

                            databaseHelper.checkAndAddUserProfileToContactRoster
                                    (phoneNumber, null, displayName, finalMember_jid, member_resource_url, image_data, null);
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        e.printStackTrace();
                        new MiscHelper().retroLogFailure(e, "getOldGroupsFunc", "JAY");

                        if (syncItem.equals("SyncContact")) { //from registration
                            //2 = seen tute but failed syncing
                            SyncContact.syncStatus = 2;

                            activity.runOnUiThread(() -> {
                                if (SyncContact.btnNext != null) {
                                    SyncContact.btnNext.setVisibility(View.VISIBLE);
                                }
                            });
                        }

                        activity.runOnUiThread(() -> {
                            if (dialog != null && dialog.isShowing()) {
                                dialog.dismiss();
                            }
                            Toast.makeText(context, R.string.onfailure, Toast.LENGTH_SHORT).show();
                        });

                    }

                    @Override
                    public void onComplete() {
                        if (syncItem.equals("SyncContact")) { //coming from registration
                            //set syncStatus to 1 then set button visibility
                            SyncContact.syncStatus = 1;

                            activity.runOnUiThread(() -> {
                                if (SyncContact.btnNext != null) {
                                    SyncContact.btnNext.setVisibility(View.VISIBLE);
                                }
                            });

                        } else {
                            activity.runOnUiThread(() -> {
                                if (dialog != null && dialog.isShowing()) {
                                    dialog.dismiss();
                                }
                                //only show toast for non-registration function
                                Toast.makeText(context, R.string.settings_joined_grp_success, Toast.LENGTH_SHORT).show();
                            });
                        }
                    }
                });
    }

    // function get contactnameregister done no need change
    public String getContactNameRegister(Context context, String number) {
        String name = null;
        String[] projection = new String[]{
                ContactsContract.PhoneLookup.DISPLAY_NAME,
                ContactsContract.PhoneLookup._ID};
        Uri contactUri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(number));
        Cursor cursor = context.getContentResolver().query(contactUri, projection, null, null, null);
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                name = cursor.getString(cursor.getColumnIndex(ContactsContract.PhoneLookup.DISPLAY_NAME));
            }
            cursor.close();
        }
        return name;
    }

    /*** Async Function ***/

// allow permission syncContact register
    public class AsyncRegSyncBtn extends android.os.AsyncTask<Void, Void, Void> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            j = 0;
            r = 0;
            k = 0;

        }

        @Override
        protected Void doInBackground(Void... arg0) {

            getContactsFromPhonebook();
            getOldGroupsFunc();

            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
        }
    }

    // not allow Permission syncContact register & restore old group setting
    public class AsyncOldGourpBtn extends android.os.AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            j = 0;
            r = 0;
            k = 0;

            dialog = ProgressDialog.show(context, null, "Checking for existing groups");
        }

        @Override
        protected Void doInBackground(Void... arg0) {

            getOldGroupsFunc();

            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
        }
    }

    // normal syncContact & share contact
    public class AsyncContactbtn extends android.os.AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            j = 0;
            r = 0;
            k = 0;

            dialog = ProgressDialog.show(context, null, "Checking for friends who use Soapp");
        }

        @Override
        protected Void doInBackground(Void... arg0) {

            getContactsFromPhonebook();

            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
        }
    }
}