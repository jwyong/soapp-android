package com.soapp.push_notifications;

import android.os.Build;
import android.util.Log;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.soapp.SoappApi.ApiModel.NotificationRegisterTokenModel;
import com.soapp.SoappApi.Interface.RegisterInterface;
import com.soapp.SoappApi.RetrofitAPIClient;
import com.soapp.global.GlobalVariables;
import com.soapp.global.MiscHelper;
import com.soapp.global.Preferences;
import com.soapp.global.StateCheck;
import com.soapp.setup.Soapp;
import com.soapp.sql.DatabaseHelper;
import com.soapp.xmpp.CheckSmackHelper;

import me.leolin.shortcutbadger.ShortcutBadgeException;
import me.leolin.shortcutbadger.ShortcutBadger;

/* Created by chang on 05/09/2017. */

public class FireBaseMessageNotification extends FirebaseMessagingService {
    static DatabaseHelper databaseHelper = DatabaseHelper.getInstance();

    @Override
    public void onMessageReceived(final RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);

        if (!StateCheck.foreground) { //if not in foreground only receive notifications
            notificationAction(remoteMessage);
            CheckSmackHelper.getInstance().check();
        }

        //TO BE MOVED TO LIVEDAT INTEGER OBSERVATION SOON
        //add total badge count to home screen
        try {
            ShortcutBadger.applyCountOrThrow(Soapp.getInstance().getApplicationContext(),
                    DatabaseHelper.getInstance().getTotalBadgeCountChatSche());
        } catch (ShortcutBadgeException ignore) {
        }
    }

    @Override
    public void onNewToken(String s) {
        super.onNewToken(s);

        String access_token = Preferences.getInstance().getValue(this, GlobalVariables.STRPREF_ACCESS_TOKEN);
        Preferences.getInstance().save(this, GlobalVariables.FCM_TOKEN, s);

        if (access_token.equals("nil")) {
            return;
        }

        NotificationRegisterTokenModel notificationRegisterTokenModel =
                new NotificationRegisterTokenModel(Preferences.getInstance().getValue(Soapp.getInstance().getApplicationContext(),
                        GlobalVariables.STRPREF_USER_ID), "fcm", s);

        RegisterInterface client = RetrofitAPIClient.getClient().create(RegisterInterface.class);
        retrofit2.Call<NotificationRegisterTokenModel> call =
                client.notificationRegisterToken(notificationRegisterTokenModel, "Bearer " + access_token);

        call.enqueue(new retrofit2.Callback<NotificationRegisterTokenModel>() {
            @Override
            public void onResponse(retrofit2.Call<NotificationRegisterTokenModel> call,
                                   retrofit2.Response<NotificationRegisterTokenModel> response) {
                if (!response.isSuccessful()) {

                    new MiscHelper().retroLogUnsuc(response, "onNewToken", "JAY");

                    return;
                }

                Preferences.getInstance().save(FireBaseMessageNotification.this, GlobalVariables.FCM_TOKEN, s);
            }

            @Override
            public void onFailure(retrofit2.Call<NotificationRegisterTokenModel> call, Throwable t) {
                new MiscHelper().retroLogFailure(t, "onTokenRefreshfail  ", "JAY");
            }
        });
    }

    private void notificationAction(RemoteMessage remoteMessage) {
        //get required info from push data payload
        String[] thread = remoteMessage.getData().get("type").split(":");
        String type = thread[0];

        if (type != null) { //safety unwrap
            String title = remoteMessage.getData().get("title");
            String body = remoteMessage.getData().get("body");
            String sender_jid = remoteMessage.getData().get("sender_jid");
            String group_id = remoteMessage.getData().get("group_id");


            //check whether group or indi first
            if (group_id == null) { //indi and res booking from biz owners (since no grp_id
                //only make push notification if user did NOT set to off

                switch (type) {
                    case "chat":
                        if (!Preferences.getInstance().getValue(this, "IndiPushNoti").equals("off")) {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                                databaseHelper.saveNotiDataToDB(remoteMessage.getData(), 0, "");
                                databaseHelper.pushMethodCaller(sender_jid, "");
                            } else {
                                databaseHelper.push_go_indi_chat(sender_jid, title, body);
                            }
                        }
                        break;

                    case "appointment":
                        String apptID;

                        if (thread.length == 2) {
                            apptID = thread[1];
                        } else {
                            apptID = null;
                        }
                        if (!Preferences.getInstance().getValue(this, "IndiPushNoti").equals("off")) {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                                databaseHelper.saveNotiDataToDB(remoteMessage.getData(), 0, apptID);
                                databaseHelper.pushMethodCaller(sender_jid, apptID);
                            } else {
                                databaseHelper.push_go_indi_sche(sender_jid, title, body, apptID);
                            }
                        }


                        break;

                    case "restaurant": //booking accepted/rejected from biz owner
                        //no settings for booking push for now
                        databaseHelper.push_go_res_booking(sender_jid, title, body);
                        break;

                    case "call":
                        Log.d("wtf", "notificationAction: ");
                        break;

                    default:
                        break;
                }
            } else { //grp
                //only make push notification if user did NOT set to off
                if (!Preferences.getInstance().getValue(this, "GrpPushNoti").equals("off")) {
                    if (type.equals("chat")) { //push for indi chat
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                            databaseHelper.saveNotiDataToDB(remoteMessage.getData(), 1, "");
                            databaseHelper.pushMethodCaller(group_id, "");
                        } else {
                            databaseHelper.push_go_grp_chat(sender_jid, title, body, group_id);
                        }
                    } else { //push for grp appointment
                        String apptID;

                        if (thread.length == 2) {
                            apptID = thread[1];
                        } else {
                            apptID = null;
                        }

                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                            databaseHelper.saveNotiDataToDB(remoteMessage.getData(), 1, apptID);
                            databaseHelper.pushMethodCaller(group_id, apptID);
                        } else {
                            databaseHelper.push_go_grp_sche(sender_jid, title, body, group_id, apptID);
                        }
                    }
                }
            }
        }
    }


}
