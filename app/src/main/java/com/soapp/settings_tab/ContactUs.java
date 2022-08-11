package com.soapp.settings_tab;

import android.app.Activity;
import android.app.ProgressDialog;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.material.snackbar.Snackbar;
import com.soapp.R;
import com.soapp.SoappApi.APIGlobalVariables;
import com.soapp.SoappApi.ApiModel.UploadEmail;
import com.soapp.SoappApi.Interface.UploadEmailWithImage;
import com.soapp.SoappApi.Interface.UploadEmailWithoutImage;
import com.soapp.SoappApi.RetrofitAPIClient;
import com.soapp.base.BaseActivity;
import com.soapp.global.GlobalVariables;
import com.soapp.global.MiscHelper;
import com.soapp.global.Preferences;
import com.soapp.global.UIHelper;
import com.soapp.setup.Soapp;
import com.soapp.xmpp.SmackHelper;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/* Created by Soapp on 29/06/2017. */

public class ContactUs extends BaseActivity implements View.OnClickListener {

    public static Uri selectedImage;
    public static Activity context;
    final int ACCESS_GALLERY_PROFILE = 2;
    //new
    EditText email, description;
    Button done_btn, select_image;
    String message, Jid, phoneNumber, countryCode, userName, senderEmail;
    ProgressDialog progressDialog;
    ProgressDialog dialog;
    private Preferences preferences = Preferences.getInstance();

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settingstab_contact);
        setupToolbar();
//        setTitle(R.string.settings_contact_us);
        new UIHelper().setStatusBarColor(this, false, R.color.black3a);

        //new
        email = findViewById(R.id.email);
        description = findViewById(R.id.description);
        done_btn = findViewById(R.id.done_btn);
        done_btn.setOnClickListener(ContactUs.this);
//        select_image = (Button) findViewById(R.id.select_image);
//        select_image.setOnClickListener(ContactUs.this);
        Jid = preferences.getValue(ContactUs.this, GlobalVariables.STRPREF_USER_ID);
        phoneNumber = preferences.getValue(this, GlobalVariables.STRPREF_PHONE_NUMBER);
        countryCode = preferences.getValue(this, GlobalVariables.STRPREF_COUNTRY_CODE);
        userName = preferences.getValue(this, GlobalVariables.STRPREF_USERNAME);
    }

    public void onClick(View view) {
        switch (view.getId()) {
//            case R.id.select_image:
//
//                int permission3 = ActivityCompat.checkSelfPermission(ContactUs.this, Manifest
//                        .permission
//                        .READ_EXTERNAL_STORAGE);
//                if (permission3 == PackageManager.PERMISSION_DENIED) {
//                    // We don't have permission so prompt the user
//                    String[] PERMISSIONS_GALLERY = {
//                            Manifest.permission.READ_EXTERNAL_STORAGE,
//                    };
//                    ActivityCompat.requestPermissions(this, PERMISSIONS_GALLERY,
//                            ACCESS_GALLERY_PROFILE);
//
//                } else {
//                    Intent intent = new Intent(this, ImageCropPreviewActivity.class);
//                    intent.putExtra("from", "Contact");
//                    startActivity(intent);
//                }
//

//                break;

            case R.id.done_btn:
                if (SmackHelper.getXMPPConnection() != null && SmackHelper.getXMPPConnection().isAuthenticated()) {
                    if (selectedImage == null) {

                        message = description.getText().toString();
                        senderEmail = email.getText().toString();
                        String[] strings = {Jid, userName, countryCode, phoneNumber, senderEmail, message};

                        new sendEmailContactUs().execute(strings);
                    } else {

                        message = description.getText().toString();
                        senderEmail = email.getText().toString();

                        new sendEmailContactUsImage(Jid, userName, countryCode, phoneNumber, senderEmail, message, selectedImage).execute();
                    }
                } else {
                    Toast.makeText(ContactUs.context, R.string.xmpp_waiting_connection,
                            Toast.LENGTH_SHORT).show();

                }
                break;


            default:
                break;
        }


    }

    public void snackbarMaker() {
        View parentLayout = findViewById(R.id.main_layout);

        Snackbar.make(parentLayout, "Your feedback has been uploaded. Thanks!", Snackbar.LENGTH_LONG)
                .setAction("CLOSE", new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        onBackPressed();

                    }
                })
                .show();
    }

    private class sendEmailContactUs extends AsyncTask<String, Integer, Void> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            String sending = "Your Feedback is being Sent";
            //Display progressDialog before download starts
            progressDialog = ProgressDialog.show(ContactUs.this, null, sending);
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);

        }

        @Override
        protected Void doInBackground(String... params) {
            String Jid = params[0];
            String userName = params[1];
            String countryCode = params[2];
            String phoneNumber = params[3];
            String senderEmail = params[4];
            String message = params[5];
            String access_token = preferences.getValue(Soapp.getInstance().getApplicationContext(), GlobalVariables.STRPREF_ACCESS_TOKEN);
            UploadEmail upload = new UploadEmail(Jid, userName, countryCode, phoneNumber, senderEmail, message);

            //build retrofit
            UploadEmailWithoutImage client = RetrofitAPIClient.getClient().create(UploadEmailWithoutImage.class);
            retrofit2.Call<UploadEmail> call = client.upload("Bearer " + access_token, upload);

            call.enqueue(new Callback<UploadEmail>() {
                @Override
                public void onResponse(Call<UploadEmail> call, Response<UploadEmail> response) {
                    if (!response.isSuccessful()) {
                        new Handler(Looper.getMainLooper())
                                .post(new Runnable() {
                                    @Override
                                    public void run() {
                                        new MiscHelper().retroLogUnsuc(response, "UploadEmailWithoutImage ", "JAY");
                                        Toast.makeText(ContactUs.this, R.string.onresponse_unsuccessful, Toast.LENGTH_SHORT).show();
                                        if (progressDialog != null && progressDialog.isShowing()) {
                                            progressDialog.dismiss();
                                        }
                                    }
                                });
                        return;
                    }

                    if (progressDialog != null && progressDialog.isShowing()) {
                        progressDialog.dismiss();

                    }

                    Runnable doneContactUs = () -> finish();

                    new UIHelper().dialog1Btn(ContactUs.this, getString(R.string.contact_us_sent_title),
                            getString(R.string.contact_us_sent_msg), R.string.ok_label, R.color.black,
                            doneContactUs, false, true);

//                    snackbarMaker();
//
//                    new Handler(Looper.getMainLooper())
//                            .post(new Runnable() {
//                                @Override
//                                public void run() {
//
//
//
//                                }
//                            });
                }

                @Override
                public void onFailure(Call<UploadEmail> call, Throwable t) {
                    new Handler(Looper.getMainLooper())
                            .post(new Runnable() {
                                @Override
                                public void run() {
                                    new MiscHelper().retroLogFailure(t, "UploadEmailWithoutImage ", "JAY");
                                    Toast.makeText(ContactUs.this, R.string.onfailure, Toast
                                            .LENGTH_SHORT).show();
                                    if (progressDialog != null && progressDialog.isShowing()) {
                                        progressDialog.dismiss();
                                    }
                                }
                            });

                }
            });

            return null;
        }

    }

    public class sendEmailContactUsImage extends AsyncTask<Void, Void, Void> {

        String userJid, userName, countryCode, phoneNumber, senderEmail, message;
        Uri selectedImage;

        public sendEmailContactUsImage(String userJid, String userName, String countryCode, String phoneNumber, String senderEmail,
                                       String message, Uri selectedImage) {
            this.userJid = userJid;
            this.userName = userName;
            this.countryCode = countryCode;
            this.phoneNumber = phoneNumber;
            this.senderEmail = senderEmail;
            this.message = message;
            this.selectedImage = selectedImage;


        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            String sending = "Your Feedback is being Sent";
            //Display progressDialog before download starts
            progressDialog = ProgressDialog.show(ContactUs.this,
                    null, sending);
        }


        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);


        }


        @Override
        protected Void doInBackground(Void... params) {

//            File file = new File(selectedImage.getPath()); //new
            File outputFile = new File(GlobalVariables.TEMP_IMG_FILE);
            final int chunkSize = 1024;
            byte[] imageData = new byte[chunkSize];

            try {
                InputStream inputStream = Soapp.getInstance().getApplicationContext().getContentResolver().openInputStream(selectedImage);
                OutputStream outputStream = new FileOutputStream(outputFile);

                int bytesRead;
                while ((bytesRead = inputStream.read(imageData)) > 0) {
                    outputStream.write(Arrays.copyOfRange(imageData, 0, Math.max(0, bytesRead)));
                }
            } catch (Exception e) {
            }

            String access_token = preferences.getValue(ContactUs.this, GlobalVariables.STRPREF_ACCESS_TOKEN);
            RequestBody Jid = RequestBody.create(MediaType.parse("text/plain"), userJid);
            RequestBody Name = RequestBody.create(MediaType.parse("text/plain"), userName);
            RequestBody Countrycode = RequestBody.create(MediaType.parse("text/plain"), countryCode);
            RequestBody Phone = RequestBody.create(MediaType.parse("text/plain"), phoneNumber);
            RequestBody Email = RequestBody.create(MediaType.parse("text/plain"), senderEmail);
            RequestBody Message = RequestBody.create(MediaType.parse("text/plain"), message);

            //image file
            RequestBody image = RequestBody.create(APIGlobalVariables.JPEG, outputFile);//new
            MultipartBody.Part filePart = MultipartBody.Part.createFormData("image", "name.jpeg", image);

            //build retrofit
            UploadEmailWithImage client = RetrofitAPIClient.getClient().create(UploadEmailWithImage.class);
            retrofit2.Call<UploadEmail> call = client.uploade("Bearer " + access_token, filePart, Jid, Name, Countrycode, Phone, Email, Message);
            call.enqueue(new retrofit2.Callback<UploadEmail>() {
                @Override
                public void onResponse(retrofit2.Call<UploadEmail> call, retrofit2.Response<UploadEmail> response) {
                    if (!response.isSuccessful()) {
                        new Handler(Looper.getMainLooper())
                                .post(new Runnable() {
                                    @Override
                                    public void run() {
                                        new MiscHelper().retroLogUnsuc(response, "sendEmailContactUsImage ", "JAY");
                                        Toast.makeText(ContactUs.this, R.string.onresponse_unsuccessful, Toast
                                                .LENGTH_SHORT).show();
                                        if (progressDialog != null && progressDialog.isShowing()) {
                                            progressDialog.dismiss();
                                        }
                                    }
                                });

                    } else {
                        new Handler(Looper.getMainLooper())
                                .post(new Runnable() {
                                    @Override
                                    public void run() {
                                        if (progressDialog != null && progressDialog.isShowing()) {
                                            progressDialog.dismiss();
                                        }
                                        snackbarMaker();
                                    }
                                });
                    }
                }

                @Override
                public void onFailure(retrofit2.Call<UploadEmail> call, Throwable t) {
                    new Handler(Looper.getMainLooper())
                            .post(new Runnable() {
                                @Override
                                public void run() {
                                    new MiscHelper().retroLogFailure(t, "sendEmailContactUsImage ", "JAY");
                                    Toast.makeText(ContactUs.this, R.string.onfailure, Toast
                                            .LENGTH_SHORT).show();
                                    if (progressDialog != null && progressDialog.isShowing()) {
                                        progressDialog.dismiss();
                                    }
                                }
                            });


                }
            });


            return null;
        }

    }
}
