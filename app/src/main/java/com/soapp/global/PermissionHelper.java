package com.soapp.global;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.TextView;
import android.widget.Toast;

import com.soapp.R;
import com.soapp.setup.Soapp;

public class PermissionHelper {
    private Preferences preferences = Preferences.getInstance();
    public String strPermissionList = "";
    private UIHelper uiHelper = new UIHelper();

    // check device name
    public String getDeviceame() {
        String manufacturer = Build.MANUFACTURER;
        String model = Build.MODEL;
        if (model.startsWith(manufacturer)) {
            return capitalize(model);
        }
        return capitalize(manufacturer) + " " + model;
    }

    private String capitalize(String str) {
        if (TextUtils.isEmpty(str)) {
            return str;
        }
        char[] arr = str.toCharArray();
        boolean capitalizeNext = true;

        StringBuilder phrase = new StringBuilder();
        for (char c : arr) {
            if (capitalizeNext && Character.isLetter(c)) {
                phrase.append(Character.toUpperCase(c));
                capitalizeNext = false;
                continue;
            } else if (Character.isWhitespace(c)) {
                capitalizeNext = true;
            }
            phrase.append(c);
        }

        return phrase.toString();
    }

    //  check permissions >= 6.0
    public void CheckPermissions(Context context, int requestCode, int Nametitle) {
        Activity activity = (Activity) context;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

            // pager for permissions
            String value = preferences.getValue(context, "askAgain");

            switch (requestCode) {
                case 1000: // registration

                    if (!(PackageManager.PERMISSION_GRANTED == activity.checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE))) {
                        strPermissionList += Manifest.permission.WRITE_EXTERNAL_STORAGE + ",";
                    }

                    if (!(PackageManager.PERMISSION_GRANTED == activity.checkSelfPermission(Manifest.permission.WRITE_CONTACTS))) {
                        strPermissionList += Manifest.permission.WRITE_CONTACTS + ",";
                    }

                    if (!(PackageManager.PERMISSION_GRANTED == activity.checkSelfPermission(Manifest.permission.READ_CONTACTS))) {
                        strPermissionList += Manifest.permission.READ_CONTACTS + ",";
                    }
                    break;

                case 1001: // camera

                    if (!(PackageManager.PERMISSION_GRANTED == activity.checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE))) {
                        strPermissionList += Manifest.permission.WRITE_EXTERNAL_STORAGE + ",";
                    }

                    if (!(PackageManager.PERMISSION_GRANTED == activity.checkSelfPermission(Manifest.permission.CAMERA))) {
                        strPermissionList += Manifest.permission.CAMERA + ",";
                    }

                    if (!(PackageManager.PERMISSION_GRANTED == activity.checkCallingPermission(Manifest.permission.RECORD_AUDIO))) {
                        strPermissionList += Manifest.permission.RECORD_AUDIO + ",";
                    }

                    break;

                case 1002: // SMS service

                    if (!(PackageManager.PERMISSION_GRANTED == activity.checkSelfPermission(Manifest.permission.READ_SMS))) {
                        strPermissionList += Manifest.permission.READ_SMS + ",";
                    }

                    break;

                case 1003: // GPS Location

                    if (!(PackageManager.PERMISSION_GRANTED == activity.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION))) {
                        strPermissionList += Manifest.permission.ACCESS_FINE_LOCATION + ",";
                    }

                    if (!(PackageManager.PERMISSION_GRANTED == activity.checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION))) {
                        strPermissionList += Manifest.permission.ACCESS_COARSE_LOCATION + ",";
                    }

                    break;

                case 1004: // Phone call

                    if (!(PackageManager.PERMISSION_GRANTED == activity.checkSelfPermission(Manifest.permission.CALL_PHONE))) {
                        strPermissionList += Manifest.permission.CALL_PHONE + ",";
                    }
                    break;

                case 1005: // audio Record

                    if (!(PackageManager.PERMISSION_GRANTED == activity.checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE))) {
                        strPermissionList += Manifest.permission.WRITE_EXTERNAL_STORAGE + ",";
                    }

                    if (!(PackageManager.PERMISSION_GRANTED == activity.checkCallingPermission(Manifest.permission.RECORD_AUDIO))) {
                        strPermissionList += Manifest.permission.RECORD_AUDIO + ",";
                    }

                    break;

                case 1006: // storage read write

                    if (!(PackageManager.PERMISSION_GRANTED == activity.checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE))) {
                        strPermissionList += Manifest.permission.WRITE_EXTERNAL_STORAGE + ",";
                    }
                    if (!(PackageManager.PERMISSION_GRANTED == activity.checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE))) {
                        strPermissionList += Manifest.permission.READ_EXTERNAL_STORAGE + ",";
                    }
                    break;


                case 1007: // contact read write

                    if (!(PackageManager.PERMISSION_GRANTED == activity.checkSelfPermission(Manifest.permission.WRITE_CONTACTS))) {
                        strPermissionList += Manifest.permission.WRITE_CONTACTS + ",";
                    }

                    if (!(PackageManager.PERMISSION_GRANTED == activity.checkSelfPermission(Manifest.permission.READ_CONTACTS))) {
                        strPermissionList += Manifest.permission.READ_CONTACTS + ",";
                    }

                    break;

                case 1008: // Qr code

                    if (!(PackageManager.PERMISSION_GRANTED == activity.checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE))) {
                        strPermissionList += Manifest.permission.WRITE_EXTERNAL_STORAGE + ",";
                    }

                    if (!(PackageManager.PERMISSION_GRANTED == activity.checkSelfPermission(Manifest.permission.CAMERA))) {
                        strPermissionList += Manifest.permission.CAMERA + ",";
                    }

                    if (!(PackageManager.PERMISSION_GRANTED == activity.checkSelfPermission(Manifest.permission.WRITE_CONTACTS))) {
                        strPermissionList += Manifest.permission.WRITE_CONTACTS + ",";
                    }

                    if (!(PackageManager.PERMISSION_GRANTED == activity.checkSelfPermission(Manifest.permission.READ_CONTACTS))) {
                        strPermissionList += Manifest.permission.READ_CONTACTS + ",";
                    }

                    break;

            }

            //  required permissions
            if (!strPermissionList.equals("")) { //some required permissions not granted
                try {
                    switch (value) {
                        case "nil":
                            strPermissionList = strPermissionList.substring(0, strPermissionList.length());
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                activity.requestPermissions(strPermissionList.split(","), requestCode);
                            }

                            break;

                        case "true":
                            strPermissionList = strPermissionList.substring(0, strPermissionList.length());
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                activity.requestPermissions(strPermissionList.split(","), requestCode);
                            }
                            break;

                        case "false":
                            PermissionDialog(context, activity, Nametitle, null, requestCode);

                            break;
                    }

                } catch (Exception e) {
                }
            }

        }
    }

    public boolean test() {
        return true;
    }

    // permissiondialog
    public void PermissionDialog(Context context, Activity activity, int permissionText, Runnable runnable, int requestCode) {
        Dialog permissionDialog = new Dialog(context);
        LayoutInflater permissionlayout = LayoutInflater.from(context);
        View permissionView = permissionlayout.inflate(R.layout.dialog_permission, null);

        permissionDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        permissionDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        permissionDialog.setContentView(permissionView);
        permissionDialog.setCancelable(false);
        permissionDialog.create();

        TextView Permission_Title = permissionDialog.findViewById(R.id.Permission_Title);
        TextView Permission_Message = permissionDialog.findViewById(R.id.Permission_Message);
        TextView Permission_negative = permissionDialog.findViewById(R.id.Permission_negative);
        TextView Permission_positive = permissionDialog.findViewById(R.id.Permission_positive);

        if (permissionText != -1) {
            Permission_Message.setText(permissionText);
        }

        Permission_positive.setText("SETTING");

        //set OnclickListener
        Permission_negative.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String qrCamera = preferences.getValue(context, "Qrcamera");
                String foodMap = preferences.getValue(context, "foodMap");
                if (qrCamera == "false" || foodMap == "false") {
                    activity.onBackPressed();
                }
                permissionDialog.dismiss();
                activity.isFinishing();
            }
        });

        Permission_positive.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                preferences.save(context, "askAgain", "true");
                Intent intent = new Intent();

                intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                Uri uri = Uri.fromParts("package", activity.getPackageName(), null);
                intent.setData(uri);
                activity.startActivityForResult(intent, 1000);

                permissionDialog.dismiss();
                activity.isFinishing();
            }
        });

        permissionDialog.show();
    }

    //check lower 6.0 android permission
    public void checkpermissionlower(Context context) {
        Activity activity = (Activity) context;

    }

//            Intent intent = new Intent();
//            if (!preferences.getValue(context, "whitelisted").equals("true")) {
//
//                Runnable lowerPermission = new Runnable() {
//                    @Override
//                    public void run() {
//
//                        switch (Build.MANUFACTURER.toLowerCase()) {
//                            case "oppo":
//
//                                intent.setComponent(new ComponentName("com.color.safecenter", "com.color.safecenter.permission.PermissionManagerActivity"));
//                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT);
//
//                                break;
//
//                            case "xiaomi":
//
//                                intent.setComponent(new ComponentName("com.miui.securitycenter", "com.miui.appmanager.ApplicationsDetailsActivity"));
//                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT);
//
//                                break;
//                        }
//                    }
//                };

//                activity.startActivityForResult(intent, 101);
//                PermissionDialog(context, activity, R.string.permission_txt, lowerPermission, -1);


    //check if phone is whitelisted p20 pro popup noti disable dialog no need call function to get notification
    public void whitelisted(Context context) {
        String component, activity;
        switch (android.os.Build.MANUFACTURER.toLowerCase()) {
            case "asus":
                component = "com.asus.mobilemanager";
                activity = "com.asus.mobilemanager.MainActivity";
                if (packageIsInstalled(context, component)) { //Mobile Manager installed
                    showWhitelistAlert(context, context.getString(R.string.whitelist_asus_msg),
                            component, activity);
                }
                break;

            case "huawei":
                component = "com.huawei.systemmanager";
                activity = "com.huawei.systemmanager.optimize.process.ProtectActivity";
                if (packageIsInstalled(context, component)) { //Mobile Manager installed
                    showWhitelistAlert(context, context.getString(R.string.whitelist_huawei_msg),
                            component, activity);
                }

                //nootification on status bar etc
                component = "com.huawei.systemmanager";
                activity = "com.huawei.notificationmanager.ui.NotificationManagmentActivity";
                //Mobile Manager installed
                if (packageIsInstalled(context, component)) {
                    showWhitelistAlert(context, ("Please allow Soapp to receive push notifications under Notification Manager"),
                            component, activity);
                }

                break;

            case "samsung":
                component = "com.samsung.android.sm";
                String component2 = "com.samsung.android.lool";
                activity = "com.samsung.android.sm.ui.battery.BatteryActivity";
                if (packageIsInstalled(context, component)) { //Mobile Manager installed
                    showWhitelistAlert(context, context.getString(R.string.whitelist_samsung_msg),
                            component, activity);
                }

                if (packageIsInstalled(context, component2)) { //Mobile Manager installed
                    showWhitelistAlert(context, context.getString(R.string.whitelist_samsung_msg),
                            component, activity);
                }

                break;

            case "xiaomi":
                component = "com.miui.securitycenter";
                activity = "com.miui.permcenter.autostart.AutoStartManagementActivity";
                if (packageIsInstalled(context, component)) { //Mobile Manager installed
                    showWhitelistAlert(context, context.getString(R.string.whitelist_asus_msg),
                            component, activity);
                }
                break;

            case "vivo":
                if (!preferences.getValue(context, "pushWhitelistCheck").equals("true")) {

                    //action for cancel notification
                    Runnable pushNotificationNegative = () -> {
                        Toast.makeText(context, R.string.whitelist_denied, Toast
                                .LENGTH_LONG).show();
                    };

                    //action for allow notification
                    Runnable pushNotificationPositive = () -> {
                        autoLaunchVivo(Soapp.getInstance().getApplicationContext());
                    };

                    uiHelper.dialog2BtnsCheckobxView(context,
                            context.getString(R.string.whitelist_title),
                            context.getString(R.string.whitelist_msg),
                            pushNotificationPositive,
                            pushNotificationNegative,
                            true);
                }

                break;

            case "oppo":
                component = "com.oppo.safe";
                activity = "com.oppo.safe.permission.startup.StartupAppListActivity";
                if (packageIsInstalled(context, component)) { //Mobile Manager installed
                    showWhitelistAlertOppo(context, context.getString(R.string.whitelist_asus_msg),
                            component, activity);
                }

                component = "com.color.safecenter";
                activity = "com.color.safecenter.permission.startup.StartupAppListActivity";
                if (packageIsInstalled(context, component)) { //Mobile Manager installed
                    showWhitelistAlertOppo(context, context.getString(R.string.whitelist_asus_msg),
                            component, activity);
                }

                component = "com.color.∫ƒguardelf";
                activity = "com.color.powermanager.fuelgaue.PowerConsumptionActivity";
                if (packageIsInstalled(context, component)) { //Mobile Manager installed
                    showWhitelistAlertOppo(context, context.getString(R.string.whitelist_asus_msg),
                            component, activity);
                }

                component = "com.coloros.safecenter";
                activity = "com.coloros.safecenter.permission.startup.StartupAppListActivity";
                if (packageIsInstalled(context, component)) { //Mobile Manager installed
                    showWhitelistAlertOppo(context, context.getString(R.string.whitelist_asus_msg),
                            component, activity);
                }
                break;

            default: //need to think of default action
                break;
        }
    }

    // link mathod
    public boolean packageIsInstalled(Context context, String packagename) {
        PackageManager packageManager = context.getPackageManager();

        try {
            packageManager.getPackageInfo(packagename, 0);

            return true;
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }

    private void showWhitelistAlertOppo(Context context, String msg, final String component, final String activity) {
        if (!preferences.getValue(context, "pushWhitelistCheck").equals("true")) {

            //action for allow notification
            Runnable pushNotificationPositive = () -> {
                Intent intent = new Intent();
                intent.setComponent(new ComponentName(component, activity));
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT);
                try {
                    context.startActivity(intent);
                } catch (ActivityNotFoundException e) {
                    Toast.makeText(context, R.string.whitelist_allowed, Toast
                            .LENGTH_SHORT).show();
                }
                uiHelper.dialog1Btn(context, context.getString(R.string.oppo_last_msg), context.getString(R.string.oppo_whitelist_app_manual),
                        R.string.ok_label, R.color.black, null, true, false);
            };

            //action for cancel notification
            Runnable pushNotificationNegative = () -> {
                Toast.makeText(context, R.string.whitelist_denied, Toast
                        .LENGTH_LONG).show();
            };

            uiHelper.dialog2BtnsCheckobxView(context,
                    context.getString(R.string.whitelist_title),
                    msg,
                    pushNotificationPositive,
                    pushNotificationNegative,
                    true);
        }
    }

    private void showWhitelistAlert(Context context, String msg, final String component, final String activity) {
        if (!preferences.getValue(context, "pushWhitelistCheck").equals("true")) {
            //action for allow notification
            Runnable pushNotificationPositive = () -> {
                Intent intent = new Intent();
                intent.setComponent(new ComponentName(component, activity));
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT);
                try {
                    context.startActivity(intent);
                } catch (ActivityNotFoundException e) {
                    Toast.makeText(context, R.string.whitelist_allowed, Toast
                            .LENGTH_SHORT).show();
                }
            };

            //action for cancel notification
            Runnable pushNotificationNegative = () -> {
                Toast.makeText(context, R.string.whitelist_denied, Toast
                        .LENGTH_LONG).show();
            };

            uiHelper.dialog2BtnsCheckobxView(context,
                    context.getString(R.string.whitelist_title),
                    msg,
                    pushNotificationPositive,
                    pushNotificationNegative,
                    true);
        }
    }

    private void autoLaunchVivo(Context context) {
        try {
            Intent intent = new Intent();
            intent.setComponent(new ComponentName("com.iqoo.secure",
                    "com.iqoo.secure.ui.phoneoptimize.AddWhiteListActivity"));
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
        } catch (Exception e) {
        }
        try {
            Intent intent = new Intent();
            intent.setComponent(new ComponentName("com.vivo.permissionmanager",
                    "com.vivo.permissionmanager.activity.BgStartUpManagerActivity"));
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
        } catch (Exception ex) {
        }
        try {

            Intent intent = new Intent();
            //intent.setClassName("com.iqoo.secure",
            //       "com.iqoo.secure.ui.phoneoptimize.BgStartUpManager");
            intent.setComponent(new ComponentName("com.iqoo.secure",
                    "com.iqoo.secure.ui.phoneoptimize.BgStartUpManager"));
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);

        } catch (Exception exx) {
            exx.printStackTrace();
        }
    }


}
