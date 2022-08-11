package com.soapp.settings_tab;

/* Created by jonathanhew on 21/09/2017. */

import android.Manifest;
import android.app.ProgressDialog;
import android.content.pm.PackageManager;
import android.graphics.PointF;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.dlazaro66.qrcodereaderview.QRCodeReaderView;
import com.dlazaro66.qrcodereaderview.QRCodeReaderView.OnQRCodeReadListener;
import com.soapp.R;
import com.soapp.SoappApi.ApiModel.GetUserRepo;
import com.soapp.SoappApi.Interface.IndiAPIInterface;
import com.soapp.SoappApi.RetrofitAPIClient;
import com.soapp.base.BaseActivity;
import com.soapp.global.AddContactHelper;
import com.soapp.global.DirectoryHelper;
import com.soapp.global.GlobalVariables;
import com.soapp.global.MiscHelper;
import com.soapp.global.PermissionHelper;
import com.soapp.global.Preferences;
import com.soapp.global.UIHelper;
import com.soapp.sql.DatabaseHelper;
import com.soapp.xmpp.PubsubHelper.PubsubNodeCall;
import com.soapp.xmpp.SmackHelper;

import androidx.core.app.ActivityCompat;

public class QrReader extends BaseActivity implements ActivityCompat.OnRequestPermissionsResultCallback, OnQRCodeReadListener {
    //basics
    private UIHelper uiHelper = new UIHelper();

    private static ProgressDialog dialog;
    PubsubNodeCall pubsubNodeCall = new PubsubNodeCall();
    boolean qrScanned = false;
    private ViewGroup mainLayout;
    private QRCodeReaderView qrCodeReaderView;
    private PointsOverlayView pointsOverlayView;
    private DatabaseHelper databaseHelper = DatabaseHelper.getInstance();
    private Preferences preferences = Preferences.getInstance();
    private Handler qrHandler = new Handler();
    private MiscHelper miscHelper = new MiscHelper();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.qr_activity_decoder);
        setupToolbar();
        new UIHelper().setStatusBarColor(this, false, R.color.primaryDark4);
        mainLayout = findViewById(R.id.main_layout);

        //status bar color
        new UIHelper().setStatusBarColor(this, false, R.color.grey8);

        if (PackageManager.PERMISSION_DENIED == this.checkCallingOrSelfPermission(Manifest.permission.CAMERA) ||
                PackageManager.PERMISSION_DENIED == this.checkCallingOrSelfPermission(Manifest.permission.WRITE_CONTACTS)) {

            new PermissionHelper().CheckPermissions(this, 1008, R.string.permission_txt);
        } else {

            initQRCodeReaderView();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (qrCodeReaderView != null) {
            qrCodeReaderView.startCamera();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();

        if (qrCodeReaderView != null) {
            qrCodeReaderView.stopCamera();
        }
    }

    // Called when a QR is decoded
    // "text" : the text encoded in QR
    // "points" : points where QR control points are placed
    @Override
    public void onQRCodeRead(String text, PointF[] points) {
        if (!qrScanned) {
            //set qrScanned to true so that won't get fired twice
            qrScanned = true;

            //set qrScanned to false after 500ms so that can fire another time when scan another
            // qr
            qrHandler.postDelayed(() -> qrScanned = false, 500);

            String text1;
            String text2;
            String errorMessage = "Jon: Hohoho, it does seem like that\'s not a valid Soapp user QrCode!";

            pointsOverlayView.setPoints(points);

            try {
                text1 = text.substring(0, 5);
                text2 = text.substring(5);

                if (text1.equals("Soapp") && text.length() == 17) {
                    alertdialog(text2);

                } else {
                    Toast toast = Toast.makeText(this, errorMessage, Toast.LENGTH_LONG);
                    toast.setGravity(Gravity.CENTER_VERTICAL, 0, 0);
                    toast.setText(errorMessage);
                    toast.show();
                }

            } catch (IndexOutOfBoundsException e) {
                Toast toast = Toast.makeText(this, errorMessage, Toast.LENGTH_LONG);
                toast.setGravity(Gravity.CENTER_VERTICAL, 0, 0);
                toast.setText(errorMessage);
                toast.show();
            }


        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {

        switch (requestCode) {
            case 1008:
                for (int i = 0, len = permissions.length; i < len; i++) {
                    String permission = permissions[i];
                    if (grantResults[i] == PackageManager.PERMISSION_DENIED) {

                        boolean showRationale = false;
                        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                            showRationale = shouldShowRequestPermissionRationale(permission);

                        }
                        if (!showRationale) {

                            preferences.save(QrReader.this, "dunAskagain", String.valueOf(showRationale));
                            preferences.save(QrReader.this, "Qrcamera", "false");
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                onBackPressed();
                            }
                            break;
                        } else {
                            preferences.save(QrReader.this, "dunAskagain", String.valueOf(showRationale));

                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                onBackPressed();
                            }
                            Toast.makeText(QrReader.this, R.string.need_camera, Toast.LENGTH_SHORT)
                                    .show();
                        }

                    } else {
                        if (permission.equals(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                            new DirectoryHelper().checkAndCreateDir(this, true);
                        }
                        initQRCodeReaderView();
                        break;
                    }
                }
                break;
        }
    }

//    private void requestCameraPermission() {
//        if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.CAMERA)) {
//            Snackbar.make(mainLayout, "Camera access is required to display the camera preview.",
//                    Snackbar.LENGTH_INDEFINITE).setAction("OK", new View.OnClickListener() {
//                @Override
//                public void onClick(View view) {
//                    ActivityCompat.requestPermissions(QrReader.this, new String[]{
//                            Manifest.permission.CAMERA
//                    }, MY_PERMISSION_REQUEST_CAMERA);
//                }
//            }).show();
//        } else {
//            Snackbar.make(mainLayout, "Permission is not available. Requesting camera permission.",
//                    Snackbar.LENGTH_SHORT).show();
//            ActivityCompat.requestPermissions(this, new String[]{
//                    Manifest.permission.CAMERA
//            }, MY_PERMISSION_REQUEST_CAMERA);
//        }
//    }

    private void initQRCodeReaderView() {
        View content = getLayoutInflater().inflate(R.layout.qr_content_decoder, mainLayout, true);

        qrCodeReaderView = content.findViewById(R.id.qrdecoderview);
        pointsOverlayView = content.findViewById(R.id.points_overlay_view);

        qrCodeReaderView.setAutofocusInterval(2000L);
        qrCodeReaderView.setOnQRCodeReadListener(this);
        qrCodeReaderView.setBackCamera();

        qrCodeReaderView.startCamera();
    }

    private void alertdialog(final String jid) {
        if (qrCodeReaderView != null) {
            qrCodeReaderView.stopCamera();
        }

        Preferences preferences = Preferences.getInstance();
        final String access_token = preferences.getValue(QrReader.this, GlobalVariables.STRPREF_ACCESS_TOKEN);

        if (SmackHelper.getXMPPConnection() != null && SmackHelper.getXMPPConnection().isAuthenticated()) {
            if (databaseHelper.getContactRosterExist(jid) == 0) {
                //jid doesn't exist in contact roster, straight add qr
                new AsyncQr().execute(jid, access_token);

            } else { //jid exists in CR, check phone name
                if (databaseHelper.getPhoneNameExist(jid) == 0) {
                    //no phone name, straight add qr
                    new AsyncQr().execute(jid, access_token);

                } else { //got phone name, already exists
                    Toast.makeText(QrReader.this, R.string.contact_exists, Toast
                            .LENGTH_LONG).show();

                    //function to refresh qr scanning for next qr code
                    recreate();
                }
            }
        } else {
            Toast.makeText(QrReader.this, R.string.xmpp_waiting_connection, Toast.LENGTH_SHORT)
                    .show();
        }

        qrCodeReaderView.startCamera();

//        AlertDialog.Builder builder = new AlertDialog.Builder(this);
//
//        builder.setTitle(R.string.add_contacts)
//                .setMessage(R.string.add_unknown_contact_msg)
//                .setPositiveButton(R.string.ok_label, new DialogInterface.OnClickListener() {
//                    public void onClick(DialogInterface dialog, int which) {
//
//                        if (SmackHelper.getXMPPConnection() != null && SmackHelper.getXMPPConnection().isAuthenticated()) {
//                            if (databaseHelper.getContactRosterExist(jid) == 0) {
//                                //jid doesn't exist in contact roster, straight add qr
//                                new AsyncQr().execute(jid, access_token);
//
//                            } else { //jid exists in CR, check phone name
//                                if (databaseHelper.getPhoneNameExist(jid) == 0) {
//                                    //no phone name, straight add qr
//                                    new AsyncQr().execute(jid, access_token);
//
//                                } else { //got phone name, already exists
//                                    Toast.makeText(QrReader.this, R.string.contact_exists, Toast
//                                            .LENGTH_LONG).show();
//
//                                    //function to refresh qr scanning for next qr code
//                                    recreate();
//                                }
//                            }
//                        } else {
//                            Toast.makeText(QrReader.this, R.string.xmpp_waiting_connection, Toast.LENGTH_SHORT)
//                                    .show();
//                        }
//
//                        dialog.cancel();
//                        qrCodeReaderView.startCamera();
//
//                    }
//                })
//                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
//                    public void onClick(DialogInterface dialog, int which) {
//                        //function to refresh qr scanning for next qr code
//                        recreate();
//
//                        dialog.cancel();
//                    }
//
//
//                })
//                .show();
    }

    private void getUserProfile(final String jid, final String access_token) {
        //build retrofit
        String size = miscHelper.getDeviceDensity(QrReader.this);
        IndiAPIInterface indiAPIInterface = RetrofitAPIClient.getClient().create(IndiAPIInterface.class);
        retrofit2.Call<GetUserRepo> call = indiAPIInterface.getIndiProfile(jid, size, "Bearer " + access_token);

        call.enqueue(new retrofit2.Callback<GetUserRepo>() {
            @Override
            public void onResponse(retrofit2.Call<GetUserRepo> call, final retrofit2.Response<GetUserRepo> response) {
                if (!response.isSuccessful()) {
                    runOnUiThread(() -> {
                        if (dialog != null && dialog.isShowing()) {
                            dialog.dismiss();
                        }
                        new MiscHelper().retroLogUnsuc(response, "QrReader  ", "JAY");
                        Toast.makeText(QrReader.this, R.string.onresponse_unsuccessful, Toast.LENGTH_SHORT).show();

                        //function to refresh qr scanning for next qr code
                        recreate();
                    });
                    return;
                }
                String phoneNumber = String.valueOf(response.body().getPhone());
                String displayName = String.valueOf(response.body().getName());
                String imageURL = String.valueOf(response.body().getResource_url());
                String image_data = String.valueOf(response.body().getImage_data());

                //qr alert dialog
                //action for allow notification
                Runnable pushNotificationPositive = () -> {

                    //add contact to phone book
                    new AddContactHelper(QrReader.this).createContactPhoneBook(GlobalVariables.string1, phoneNumber);

                    //get the image data
                    databaseHelper.updateNewContactToContactRoster(jid, phoneNumber, GlobalVariables.string1, imageURL, image_data);

                    runOnUiThread(() -> {
                        if (dialog != null && dialog.isShowing()) {
                            dialog.dismiss();
                        }
                        Toast.makeText(QrReader.this, "Created a new contact with name: " + GlobalVariables.string1 +
                                " and Phone No: " + phoneNumber, Toast.LENGTH_SHORT).show();
                        GlobalVariables.string1 = null;
                        finish();
                    });

                };

                //action for cancel notification
                Runnable pushNotificationNegative = () -> {
                    //function to refresh qr scanning for next qr code
                    recreate();
                };

                if (dialog != null && dialog.isShowing()) {
                    dialog.dismiss();
                    if (qrCodeReaderView != null) {
                        qrCodeReaderView.stopCamera();
                    }
                }

                uiHelper.dialog2Btns2Eview(QrReader.this,
                        getString(R.string.add_contacts),
                        displayName,
                        phoneNumber,
                        pushNotificationPositive,
                        pushNotificationNegative,
                        false);

            }

            @Override
            public void onFailure(retrofit2.Call<GetUserRepo> call, Throwable t) {
                runOnUiThread(() -> {
                    if (dialog != null && dialog.isShowing()) {
                        dialog.dismiss();
                    }
                    new MiscHelper().retroLogFailure(t, "QrReader  ", "JAY");
                    Toast.makeText(QrReader.this, R.string.onfailure, Toast.LENGTH_SHORT).show();

                    //function to refresh qr scanning for next qr code
                    recreate();
                });
            }
        });
    }


    private class AsyncQr extends AsyncTask<String, Void, Void> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            //Display progressDialog before download starts
            dialog = ProgressDialog.show(QrReader.this, null, getString(R.string.saving_contact));
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);

        }

        @Override
        protected Void doInBackground(String... params) {
            String String2 = params[0];
            String access_token = params[1];


            getUserProfile(String2, access_token);

            return null;
        }
    }
}