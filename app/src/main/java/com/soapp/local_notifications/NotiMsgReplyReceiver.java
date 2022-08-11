package com.soapp.local_notifications;

import android.annotation.SuppressLint;
import android.app.NotificationManager;
import android.app.RemoteInput;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.service.notification.StatusBarNotification;
import android.widget.Toast;

import com.soapp.R;
import com.soapp.global.GlobalVariables;
import com.soapp.global.Preferences;
import com.soapp.sql.DatabaseHelper;
import com.soapp.xmpp.GroupChatHelper.GroupChatStanza;
import com.soapp.xmpp.SingleChatHelper.SingleChatStanza;
import com.soapp.xmpp.SmackHelper;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import io.reactivex.Observable;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.observers.DefaultObserver;

/* Created by Soapp on 08/11/2017. */

public class NotiMsgReplyReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        NotificationManager notificationManager =
                (NotificationManager) context.getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);

        Bundle replyResult = RemoteInput.getResultsFromIntent(intent);

        String apptid = intent.getStringExtra("apptID");
        String type = intent.getStringExtra("type");
        String lat, lon, uri, self_username, jid, userJid;
        switch (type) {
            case "appointment":
                jid = intent.getStringExtra("jid");
                int isGroup = intent.getIntExtra("isGroup", 0);
                self_username = Preferences.getInstance().getValue(context, GlobalVariables.STRPREF_USERNAME);
                String pushMsg;
                int status = intent.getIntExtra("status", 2);

                switch (status) {
                    case 1:
                        //only do action if not the same as selected status
                        if (SmackHelper.getXMPPConnection() != null && SmackHelper.getXMPPConnection().isAuthenticated()) {
                            //show confirmation dialog box
                            pushMsg = self_username + " " + context.getString(R.string.appt_is_going);
                            String uniqueID = UUID.randomUUID().toString();
                            if (isGroup == 0) {

                                new SingleChatStanza().SoappAppointmentStanza(jid, pushMsg, "",
                                        "", "", "", "", "", "", "",
                                        "1", "", uniqueID, "", "", apptid,
                                        self_username, "appointment", "0", "4");
                            } else {
                                userJid = Preferences.getInstance().getValue(context, GlobalVariables.STRPREF_USER_ID);
                                new GroupChatStanza().GroupAppointment(jid, userJid, uniqueID,
                                        "", "", "", "", "",
                                        "", "", "1", "", "",
                                        apptid, intent.getStringExtra("groupname"), pushMsg, "appointment",
                                        "", "4");
                            }
                            DatabaseHelper.getInstance().outgoingApptStatus(jid, apptid, 1, uniqueID);

                            //set reminder alarm
                            DatabaseHelper.getInstance().scheduleLocalNotification(apptid);
                        } else {
                            Toast.makeText(context, R.string.xmpp_waiting_connection, Toast.LENGTH_SHORT).show();
                        }

                        break;

                    case 3:
                        if (SmackHelper.getXMPPConnection() != null && SmackHelper.getXMPPConnection().isAuthenticated()) {
                            pushMsg = self_username + " " + context.getString(R.string.appt_is_not_going);
                            String uniqueID = UUID.randomUUID().toString();

                            if (isGroup == 0) {
                                new SingleChatStanza().SoappAppointmentStanza(jid, pushMsg, "",
                                        "", "", "", "", "", "", "",
                                        "3", "", uniqueID, "", "", apptid,
                                        self_username, "appointment", "0", "4");
                            } else {
                                userJid = Preferences.getInstance().getValue(context, GlobalVariables.STRPREF_USER_ID);
                                new GroupChatStanza().GroupAppointment(jid, userJid, uniqueID,
                                        "", "", "", "", "",
                                        "", "", "3", "",
                                        "", apptid, intent.getStringExtra("groupname"), pushMsg,
                                        "appointment", "", "4");
                            }
                            DatabaseHelper.getInstance().outgoingApptStatus(jid, apptid, 3, uniqueID);

                            //cancel reminder alarm
                            DatabaseHelper.getInstance().cancelPendingAlarm(apptid);
                        } else {
                            Toast.makeText(context, R.string.xmpp_waiting_connection, Toast.LENGTH_SHORT).show();
                        }
                        break;

                    default:
                        pushMsg = self_username + " " + context.getString(R.string.appt_is_undecided);
                        break;
                }
                DatabaseHelper.getInstance().zeroBadgeApptRoom(apptid, isGroup);
                break;

            case "gmaps":
                //clear noti on click
                notificationManager.cancel(String.format("reminder-%s", apptid).hashCode());

                try {
                    lat = intent.getStringExtra("lat");
                    lon = intent.getStringExtra("lon");

                    // Create a Uri from an intent string. Use the result to create an Intent.
                    uri = "http://maps.google.com/maps?daddr=" + lat + "," + lon;

                    // Create an Intent from gmmIntentUri. Set the action to ACTION_VIEW
                    Intent mapIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
                    // Make the Intent explicit by setting the Google Maps package
                    mapIntent.setPackage("com.google.android.apps.maps");

                    // Attempt to start an activity that can handle the Intent
                    context.startActivity(mapIntent);
                } catch (ActivityNotFoundException ex) {
                    Intent gmapintent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=com.google.android.apps.maps"));
                    context.startActivity(gmapintent);
                }
                break;

            case "waze":
                //clear noti on click
                notificationManager.cancel(String.format("reminder-%s", apptid).hashCode());

                try {
                    lat = intent.getStringExtra("lat");
                    lon = intent.getStringExtra("lon");
                    uri = "waze://?ll=" + lat + "," + lon + "&navigate=yes";
                    context.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(uri)));

                } catch (ActivityNotFoundException ex) {
                    Intent wazeIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=com.waze"));
                    context.startActivity(wazeIntent);
                }
                break;

            case "reply":
                jid = intent.getStringExtra("jid");
                int isgroup = intent.getIntExtra("isGroup", 0);

                if (replyResult != null) {

                    long currentDate = System.currentTimeMillis();
                    String uniqueId = UUID.randomUUID().toString();

                    SingleChatStanza singleChatStanza = new SingleChatStanza();
                    String textBody = replyResult.getString("Reply");

                    self_username = Preferences.getInstance().getValue(context, GlobalVariables.STRPREF_USERNAME);
                    if (isgroup == 0) {
                        if (SmackHelper.getXMPPConnection() != null && SmackHelper.getXMPPConnection().isAuthenticated()) {

                            DatabaseHelper.getInstance().MessageOutputDatabase(jid, textBody, uniqueId, currentDate);

                            //send original text to encryption helper to get encrypted text
//                    String encryptedText = encryptionHelper.encryptText(oriText);
//                        singleChatStanza.SoappChatStanza(encryptedText.replace("\n", ""), jid, self_username, uniqueId);
                            singleChatStanza.SoappChatStanza(textBody, jid, self_username, uniqueId);
                            //remove handler to send out pause stanza if msg successfully sent
//                    pausedHandler.removeCallbacksAndMessages(null);

                        } else {
                            DatabaseHelper.getInstance().saveMessageAndSendWhenOnline("message", jid,
                                    null, textBody, uniqueId, currentDate, "", "",
                                    "", "", "", "");
                        }

                    } else {
                        userJid = Preferences.getInstance().getValue(context, GlobalVariables.STRPREF_USER_ID);
                        String displayName = intent.getStringExtra("groupname");

                        if (SmackHelper.getXMPPConnection() != null && SmackHelper.getXMPPConnection().isAuthenticated()) {
//                    String GencryptTxt = encryptionHelper.encryptText(text2);

                            DatabaseHelper.getInstance().GroupMessageOutputDatabase(jid, textBody, uniqueId, currentDate, userJid);

                            // stanza send to server and base64 clear \n
                            new GroupChatStanza().GroupMessage(jid, userJid, self_username, textBody, uniqueId, displayName);

                        } else {
                            DatabaseHelper.getInstance().saveMessageAndSendWhenOnline("message", jid, userJid, textBody, uniqueId, currentDate, null, null, null, null, null, null);
                        }
                    }
                    DatabaseHelper.getInstance().zeroBadgeChatRoom(jid, isgroup);
                }
                break;
        }

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) { //Android 7 and above
            StatusBarNotification[] statusBarNotification = notificationManager.getActiveNotifications();
            Observable.create((ObservableOnSubscribe<StatusBarNotification[]>) emitter -> emitter.onNext(statusBarNotification))
                    .subscribe(new DefaultObserver<StatusBarNotification[]>() {
                        @SuppressLint("NewApi")
                        @Override
                        public void onNext(StatusBarNotification[] statusBarNotifications) {
                            List<StatusBarNotification> list = new ArrayList<>(Arrays.asList(statusBarNotification));

                            list.removeIf(statusBarNotification1 -> statusBarNotification1.getId() == 3 || statusBarNotification1.getId() == 666);
                            if (list.size() == 1) {
                                //clear notifications on going into foreground (appt)
                                notificationManager.cancel(list.get(0).getId() == 1 ? 1 : 2);
                            } else if (list.size() == 2) {
                                boolean apptclear = true;
                                boolean chatclear = true;
                                for (StatusBarNotification bar: list) {
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

    private void getLog(String apptid, String jid, int isGroup, String pushMsg, String status) {
    }
}