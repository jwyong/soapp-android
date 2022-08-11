package com.soapp.settings_tab;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.soapp.R;
import com.soapp.base.BaseActivity;
import com.soapp.global.Preferences;
import com.soapp.global.UIHelper;
import com.soapp.setup.Soapp;

import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.work.WorkManager;

/* Created by Soapp on 29/06/2017. */

public class Notifications extends BaseActivity implements View.OnClickListener, CompoundButton.OnCheckedChangeListener {

    private static final int CHAT_RINGTONE = 1;
    private static final int GROUP_CHAT_RINGTONE = 2;
    private UIHelper uiHelper = new UIHelper();
    Switch chat_tone, notification_switch, grp_chat_tone, grp_notification_switch, appt_enable;
    //    RelativeLayout notification_tone, grp_notification_tone, appt_tone;
    CardView notification_tone, grp_notification_tone, appt_tone;
    //    View notification_line, grp_notification_line;
    private Preferences preferences = Preferences.getInstance();

    //dev options
    private int devSwitchSound = 0;
    private int devScheClear = 0;
    private int devWMDrop = 0;
    private int devSwitchCam = 0;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settingstab_notification);
        setupToolbar();
        new UIHelper().setStatusBarColor(this, false, R.color.black3a);

        //dev options for in-app sound
        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setOnClickListener(v -> {
            if (devSwitchSound == 9) {
                if (preferences.getValue(Notifications.this, "devSwitchSound").equals("nil")) { //ori sound
                    preferences.save(Notifications.this, "devSwitchSound", "1");
                    Toast.makeText(Notifications.this, "switched to bubble 1", Toast.LENGTH_SHORT).show();

                } else { //new sound
                    preferences.save(Notifications.this, "devSwitchSound", "nil");
                    Toast.makeText(Notifications.this, "switched back to ori", Toast.LENGTH_SHORT).show();

                }
                devSwitchSound = 0;

            } else {
                devSwitchSound++;
            }
        });

        //dev options for schetab clear btn
        TextView settingstab_chat = findViewById(R.id.settingstab_chat);
        settingstab_chat.setOnClickListener(v -> {
            if (devScheClear == 9) {
                if (preferences.getValue(Notifications.this, "devScheClear").equals("nil")) { //ori sound
                    preferences.save(Notifications.this, "devScheClear", "1");
                    Toast.makeText(Notifications.this, "ScheTab clear btn visible", Toast.LENGTH_SHORT).show();

                } else { //new sound
                    preferences.save(Notifications.this, "devScheClear", "nil");
                    Toast.makeText(Notifications.this, "ScheTab clear btn gone", Toast.LENGTH_SHORT).show();

                }
                devScheClear = 0;

            } else {
                devScheClear++;
            }
        });

        //dev options for drop workManager db
        TextView settingstab_group_Chat = findViewById(R.id.settingstab_group_Chat);
        settingstab_group_Chat.setOnClickListener(v -> {
            if (devWMDrop == 9) {
                devWMDrop = 0;
                Soapp.getInstance().deleteDatabase("androidx.work.workdb");
                Toast.makeText(Notifications.this, "dropped WorkManagerDB, please restart Soapp", Toast.LENGTH_SHORT).show();

            } else {
                devWMDrop++;
            }
        });

        //dev options for camera 1/2
        TextView settingstab_chat3 = findViewById(R.id.settingstab_chat3);
        settingstab_chat3.setOnClickListener(v -> {
            if (devSwitchCam == 9) {
                if (preferences.getValue(Notifications.this, "devSwitchCam").equals("nil") ||
                        preferences.getValue(Notifications.this, "devSwitchCam").equals("2")) { //ori sound
                    preferences.save(Notifications.this, "devSwitchCam", "1");
                    Toast.makeText(Notifications.this, "switched to camera 1", Toast.LENGTH_SHORT).show();

                } else { //new sound
                    preferences.save(Notifications.this, "devSwitchCam", "2");
                    Toast.makeText(Notifications.this, "switched to camera 2", Toast.LENGTH_SHORT).show();

                }
                devSwitchCam = 0;

            } else {
                devSwitchCam++;
            }
        });

        notification_tone = findViewById(R.id.notification_tone);
//        notification_line = findViewById(R.id.notification_line);

        grp_notification_tone = findViewById(R.id.grp_notification_tone);
//        grp_notification_line = findViewById(R.id.grp_notification_line);

        appt_tone = findViewById(R.id.appt_tone);

        notification_tone.setOnClickListener(this);
        grp_notification_tone.setOnClickListener(this);
        appt_tone.setOnClickListener(this);

        //in-app msg tone switch (indi)
        chat_tone = findViewById(R.id.chat_tone);
        chat_tone.setOnCheckedChangeListener(this);
        if (preferences.getValue(Notifications.this, "IndiInMsg").equals("off")) {
            chat_tone.setChecked(false);
        } else {
            chat_tone.setChecked(true);
        }

        //push notification switch (indi)
        notification_switch = findViewById(R.id.notification_switch);
        notification_switch.setOnCheckedChangeListener(this);
        if (preferences.getValue(Notifications.this, "IndiPushNoti").equals("off")) {
            notification_switch.setChecked(false);
        } else {
            notification_switch.setChecked(true);
        }

        //in-app msg tone switch (grp)
        grp_chat_tone = findViewById(R.id.grp_chat_tone);
        grp_chat_tone.setOnCheckedChangeListener(this);
        if (preferences.getValue(Notifications.this, "GrpInMsg").equals("off")) {
            grp_chat_tone.setChecked(false);
        } else {
            grp_chat_tone.setChecked(true);
        }

        //push notification switch (grp)
        grp_notification_switch = findViewById(R.id.grp_notification_switch);
        grp_notification_switch.setOnCheckedChangeListener(this);
        if (preferences.getValue(Notifications.this, "GrpPushNoti").equals("off")) {
            grp_notification_switch.setChecked(false);
        } else {
            grp_notification_switch.setChecked(true);
        }

        //appt reminder switch
        appt_enable = findViewById(R.id.appt_enable);
        appt_enable.setOnCheckedChangeListener(this);
        if (preferences.getValue(Notifications.this, "ApptReminder").equals("off")) {
            appt_enable.setChecked(false);
        } else {
            appt_enable.setChecked(true);
        }
    }

    public void onClick(View view) {
        switch (view.getId()) {
            //SINGLE CHAT NOTIFICATION TONE
            case R.id.notification_tone:
                // >= API 23
                if (!(PackageManager.PERMISSION_GRANTED == ActivityCompat.checkSelfPermission(Notifications.this, Manifest.permission.WRITE_SETTINGS))) {
                    if (Build.VERSION.SDK_INT >= 23) {
                        if (!android.provider.Settings.System.canWrite(this)) {
                            //cannot write to system
                            //action for confirmed delete room
                            Runnable deleteRoomConfirm = () -> {
//                                if (jid.length() == 12) { //indi
//                                    databaseHelper.selfDeleteIndiChat(jid);
//
//                                } else { //grp
//                                    if (SmackHelper.getXMPPConnection() != null && SmackHelper.getXMPPConnection().isAuthenticated()) {
//                                        databaseHelper.selfLeaveDeleteRoom(jid, true);
//                                    } else {
//                                        Toast.makeText(context, R.string.xmpp_waiting_connection, Toast.LENGTH_SHORT).show();
//                                    }
//                                }
                            };

                            // new dialog box
                            Runnable pushNotificationPositive = () -> {
                                Intent intentWriteSettings = new Intent(android.provider.Settings.ACTION_MANAGE_WRITE_SETTINGS);
                                startActivity(intentWriteSettings);
                            };

                            uiHelper.dialog1Btn(this, getString(R.string.need_basic_title), getString(R.string.need_noti_permission_txt),
                                    R.string.ok_label, R.color.black, pushNotificationPositive, true, false);


                            //old dialog box
                            //PUT IN POPUP TO ASK FOR PERMISSION (ONLY ONE OPTION "OK")
                            //DIALOG BOX CODES
//                            AlertDialog alertDialog = new AlertDialog.Builder(Notifications.this).create();
//                            alertDialog.setTitle("Permission Requested");
//                            alertDialog.setMessage("Please enable Soapp to change system settings in order to access Notification Notifications");
//                            alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK", new DialogInterface.OnClickListener() {
//                                public void onClick(DialogInterface dialog, int which) {
//                                    dialog.dismiss();
//                                    Intent intentWriteSettings = new Intent(android.provider.Settings.ACTION_MANAGE_WRITE_SETTINGS);
//                                    startActivity(intentWriteSettings);
//                                }
//                            });
//
//                            alertDialog.show();

                        } else {
                            //can write to system
                            Intent intent = new Intent(RingtoneManager.ACTION_RINGTONE_PICKER);
                            intent.putExtra(RingtoneManager.EXTRA_RINGTONE_TITLE, "Select ringtone for notifications:");
                            intent.putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE, RingtoneManager.TYPE_NOTIFICATION);
                            intent.putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_SILENT, true);
                            intent.putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_DEFAULT, true);
                            intent.putExtra(RingtoneManager.EXTRA_RINGTONE_DEFAULT_URI,
                                    RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION));

                            String ringtone = preferences.getValue(Notifications.this, "ChatRingtone");
                            switch (ringtone) {
                                case "nil":
                                    intent.putExtra(RingtoneManager.EXTRA_RINGTONE_EXISTING_URI,
                                            RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION));
                                    break;

                                case "null": //user chose silent
                                    intent.putExtra(RingtoneManager.EXTRA_RINGTONE_EXISTING_URI,
                                            2);
                                    break;

                                default: //caters for no default preferences as well
                                    intent.putExtra(RingtoneManager.EXTRA_RINGTONE_EXISTING_URI, Uri
                                            .parse(ringtone));
                                    break;
                            }
                            startActivityForResult(intent, CHAT_RINGTONE);
                        }
                    }


                } else {
                    // < API23
                    Intent intent = new Intent(RingtoneManager.ACTION_RINGTONE_PICKER);
                    intent.putExtra(RingtoneManager.EXTRA_RINGTONE_TITLE, "Select ringtone for notifications:");
                    intent.putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE, RingtoneManager.TYPE_NOTIFICATION);
                    intent.putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_SILENT, true);
                    intent.putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_DEFAULT, true);
                    intent.putExtra(RingtoneManager.EXTRA_RINGTONE_DEFAULT_URI,
                            RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION));

                    String ringtone = preferences.getValue(Notifications.this, "ChatRingtone");
                    switch (ringtone) {
                        case "nil":
                            intent.putExtra(RingtoneManager.EXTRA_RINGTONE_EXISTING_URI,
                                    RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION));
                            break;

                        case "null": //user chose silent
                            intent.putExtra(RingtoneManager.EXTRA_RINGTONE_EXISTING_URI,
                                    2);
                            break;

                        default: //caters for no default preferences as well
                            intent.putExtra(RingtoneManager.EXTRA_RINGTONE_EXISTING_URI, Uri
                                    .parse(ringtone));
                            break;
                    }
                    startActivityForResult(intent, CHAT_RINGTONE);
                }
                break;

            //GROUP NOTIFICATION TONE
            case R.id.grp_notification_tone:
                // >= API 23
                if (!(PackageManager.PERMISSION_GRANTED == ActivityCompat.checkSelfPermission(Notifications.this, Manifest.permission.WRITE_SETTINGS))) {
                    if (Build.VERSION.SDK_INT >= 23) {
                        if (!android.provider.Settings.System.canWrite(this)) {
                            //cannot write to system

                            //new dialog box
                            Runnable pushNotificationPositive = () -> {
                                Intent intentWriteSettings = new Intent(android.provider.Settings.ACTION_MANAGE_WRITE_SETTINGS);
                                startActivity(intentWriteSettings);
                            };

                            uiHelper.dialog1Btn(this, getString(R.string.need_basic_title), getString(R.string.need_noti_permission_txt),
                                    R.string.ok_label, R.color.black, pushNotificationPositive, true, false);

                            //old dialog box
                            // POPUP TO ASK FOR PERMISSION (ONLY ONE OPTION "OK")
                            //DIALOG BOX CODES
//                            AlertDialog alertDialog = new AlertDialog.Builder(Notifications.this).create();
//                            alertDialog.setTitle("Permission Requested");
//                            alertDialog.setMessage("Please enable Soapp to change system settings in order to access Notification Notifications");
//                            alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK", new DialogInterface.OnClickListener() {
//                                public void onClick(DialogInterface dialog, int which) {
//                                    dialog.dismiss();
//                                    Intent intentWriteSettings = new Intent(android.provider.Settings.ACTION_MANAGE_WRITE_SETTINGS);
//                                    startActivity(intentWriteSettings);
//                                }
//                            });
//                            alertDialog.show();

                        } else {
                            //can write to system
                            Intent intent = new Intent(RingtoneManager.ACTION_RINGTONE_PICKER);
                            intent.putExtra(RingtoneManager.EXTRA_RINGTONE_TITLE, "Select ringtone for notifications:");
                            intent.putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_SILENT, true);
                            intent.putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_DEFAULT, true);
                            intent.putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE, RingtoneManager.TYPE_NOTIFICATION);
                            intent.putExtra(RingtoneManager.EXTRA_RINGTONE_DEFAULT_URI,
                                    RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION));

                            String ringtone = preferences.getValue(Notifications.this, "GrpChatRingtone");
                            switch (ringtone) {
                                case "nil":
                                    intent.putExtra(RingtoneManager.EXTRA_RINGTONE_EXISTING_URI,
                                            RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION));
                                    break;

                                case "null": //user chose silent
                                    intent.putExtra(RingtoneManager.EXTRA_RINGTONE_EXISTING_URI,
                                            2);
                                    break;

                                default: //caters for no default preferences as well
                                    intent.putExtra(RingtoneManager.EXTRA_RINGTONE_EXISTING_URI, Uri
                                            .parse(ringtone));
                                    break;
                            }
                            startActivityForResult(intent, GROUP_CHAT_RINGTONE);
                        }
                    }

                } else {
                    // < API23

                    Intent intent = new Intent(RingtoneManager.ACTION_RINGTONE_PICKER);
                    intent.putExtra(RingtoneManager.EXTRA_RINGTONE_TITLE, "Select ringtone for notifications:");
                    intent.putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_SILENT, true);
                    intent.putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_DEFAULT, true);
                    intent.putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE, RingtoneManager.TYPE_NOTIFICATION);
                    intent.putExtra(RingtoneManager.EXTRA_RINGTONE_DEFAULT_URI,
                            RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION));

                    String ringtone = preferences.getValue(Notifications.this, "GrpChatRingtone");
                    switch (ringtone) {
                        case "nil":
                            intent.putExtra(RingtoneManager.EXTRA_RINGTONE_EXISTING_URI,
                                    RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION));
                            break;

                        case "null": //user chose silent
                            intent.putExtra(RingtoneManager.EXTRA_RINGTONE_EXISTING_URI,
                                    2);
                            break;

                        default: //caters for no default preferences as well
                            intent.putExtra(RingtoneManager.EXTRA_RINGTONE_EXISTING_URI, Uri
                                    .parse(ringtone));
                            break;
                    }
                    startActivityForResult(intent, GROUP_CHAT_RINGTONE);
                }
                break;


            //APPOINTMENT REMINDER times
            case R.id.appt_tone:
                Intent IntentMain3 = new Intent(Notifications.this, ScheduleNotification.class);
                startActivity(IntentMain3);

            default:
                break;
        }
    }

    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        switch (buttonView.getId()) {
            case (R.id.chat_tone):
                if (isChecked) {
                    //do stuff when Switch is ON
                    preferences.save(Notifications.this, "IndiInMsg", "on");

                } else {
                    //do stuff when Switch if OFF
                    preferences.save(Notifications.this, "IndiInMsg", "off");

                }
                break;

            //indi push notifications switch
            case (R.id.notification_switch):
                if (isChecked) {
                    //do stuff when Switch is ON
                    preferences.save(Notifications.this, "IndiPushNoti", "on");

                    notification_tone.setVisibility(View.VISIBLE);
//                    notification_line.setVisibility(View.VISIBLE);

                } else {
                    //do stuff when Switch if OFF
                    preferences.save(Notifications.this, "IndiPushNoti", "off");

                    notification_tone.setVisibility(View.GONE);
//                    notification_line.setVisibility(View.GONE);
                }
                break;

            case (R.id.grp_chat_tone):
                if (isChecked) {
                    //do stuff when Switch is ON
                    preferences.save(Notifications.this, "GrpInMsg", "on");

                } else {
                    //do stuff when Switch if OFF
                    preferences.save(Notifications.this, "GrpInMsg", "off");

                }
                break;

            //grp push notification switch
            case (R.id.grp_notification_switch):
                if (isChecked) {
                    //do stuff when Switch is ON
                    preferences.save(Notifications.this, "GrpPushNoti", "on");

                    grp_notification_tone.setVisibility(View.VISIBLE);
//                    grp_notification_line.setVisibility(View.VISIBLE);

                } else {
                    //do stuff when Switch if OFF
                    preferences.save(Notifications.this, "GrpPushNoti", "off");

                    grp_notification_tone.setVisibility(View.GONE);
//                    grp_notification_line.setVisibility(View.GONE);

                }
                break;

            case (R.id.appt_enable): //appt reminder
                if (isChecked) {
                    //do stuff when Switch is ON
                    preferences.save(Notifications.this, "ApptReminder", "on");

                    Soapp.getInstance().setReminderCheckingPeriodically(true);

                } else {
                    //do stuff when Switch if OFF
                    preferences.save(Notifications.this, "ApptReminder", "off");
                    WorkManager.getInstance().cancelAllWorkByTag("ReminderAppt");
                    WorkManager.getInstance().cancelAllWorkByTag("ExactReminderAppt"); //appt happening now
                    WorkManager.getInstance().cancelUniqueWork("periodicSetReminder");
                }
                break;

            default:
                break;
        }
    }


    //ON ACTIVITY RESULT
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case CHAT_RINGTONE:

                if (resultCode == RESULT_OK) {

                    final Uri uriRingtone = data.getParcelableExtra(RingtoneManager.EXTRA_RINGTONE_PICKED_URI);
                    preferences.save(this, "ChatRingtone", "" + uriRingtone);

                }
                break;

            case GROUP_CHAT_RINGTONE:

                if (resultCode == RESULT_OK) {

                    final Uri uriRingtone = data.getParcelableExtra(RingtoneManager.EXTRA_RINGTONE_PICKED_URI);
                    preferences.save(Notifications.this, "GrpChatRingtone", "" + uriRingtone);

                }
                break;

            default:
                break;
        }
    }

    @Override
    protected void onDestroy() {
        devSwitchSound = 0;

        super.onDestroy();
    }
}


