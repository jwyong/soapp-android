//package com.soapp.setup;
//
//import android.Manifest;
//import android.content.Intent;
//import android.content.pm.PackageManager;
//import android.net.Uri;
//import android.os.Build;
//import android.os.Bundle;
//import android.provider.Settings;
//
//import com.soapp.R;
//import com.soapp.global.UIHelper;
//import com.soapp.registration.Splash;
//
////first activity which checks for basic required permissions (contacts and storage) before
//// launching Soapp (Splash.class)
//public class BasicPermissions extends AppCompatActivity {
//    boolean notLaunch = false;
//    //positive action for alert dialog
//    private Runnable actionPositive = new Runnable() {
//        public void run() {
//            Intent intent = new Intent();
//            intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
//            Uri uri = Uri.fromParts("package", getPackageName(), null);
//            intent.setData(uri);
//            startActivity(intent);
//
//            int pid = android.os.Process.myPid();
//            android.os.Process.killProcess(pid);
//        }
//    };
//    //negative action for alert dialog
//    private Runnable actionNegative = new Runnable() {
//        public void run() {
//            int pid = android.os.Process.myPid();
//            android.os.Process.killProcess(pid);
//        }
//    };
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//
//        //get intent to check if coming from not launching the app
//        if (getIntent().hasExtra("notLaunch")) {
//            notLaunch = true;
//        }
//
//        if (Build.VERSION.SDK_INT >= 23) {
//            CheckPermissions();
//        } else {
//            if (notLaunch) {
//                setResult(1);
//
//                finish();
//            } else {
//                StartSoApp();
//            }
//        }
//    }
//
//    private void CheckPermissions() {
//        if (Build.VERSION.SDK_INT >= 23) {
//            String strPermissionList = "";
//            if (!(PackageManager.PERMISSION_GRANTED == checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE))) {
//                strPermissionList += Manifest.permission.WRITE_EXTERNAL_STORAGE + ",";
//            }
//
//            if (!(PackageManager.PERMISSION_GRANTED == checkSelfPermission(Manifest.permission.WRITE_CONTACTS))) {
//                strPermissionList += Manifest.permission.WRITE_CONTACTS + ",";
//            }
//
//            if (!strPermissionList.equals("")) { //some required permissions not granted
//                strPermissionList = strPermissionList.substring(0, strPermissionList.length());
//                requestPermissions(strPermissionList.split(","), 1000);
//
//            } else { //all permissions granted, start Soapp
//                if (notLaunch) {
//                    setResult(1);
//
//                    finish();
//                } else {
//                    StartSoApp();
//                }
//            }
//        }
//    }
//
//    @Override
//    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
//        switch (requestCode) {
//            case 1000:
//                if (grantResults.length > 0) {
//                    boolean allow = true;
//
//                    for (int i = 0; i < grantResults.length; i++) {
//                        //granted = 0, denied = -1
//                        if (grantResults[i] == PackageManager.PERMISSION_DENIED) { //if any denied
//                            allow = false;
//                        }
//                    }
//                    if (!allow) { //if any denied
//                        new UIHelper().Dialog2BtnsNew(this, getString(R.string.need_basic_title), getString(R.string
//                                        .need_basic), R.string.open_settings, R.string.cancel, R.color.black,
//                                R.color.black, R.color.primaryDark3, R.color.white, actionPositive, actionNegative, false);
//
//                    } else { //if none denied
//                        if (notLaunch) {
//                            setResult(1);
//
//                            finish();
//                        } else {
//                            StartSoApp();
//                        }
//                    }
//                } else {
//                    setResult(0);
//
//                    finish();
//                }
//        }
//    }
//
//    private void StartSoApp() {
//        Intent intent = new Intent(this, Splash.class);
//        startActivity(intent);
//
//        finish();
//    }
//}
