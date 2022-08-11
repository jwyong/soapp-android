package com.soapp.settings_tab;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.request.RequestOptions;
import com.soapp.R;
import com.soapp.SoappApi.ApiModel.UserModel;
import com.soapp.SoappApi.Interface.UpdateUserProfileNameOnly;
import com.soapp.SoappApi.RetrofitAPIClient;
import com.soapp.base.BaseActivity;
import com.soapp.camera.ImageCropPreviewActivity;
import com.soapp.global.GlideAPI.GlideApp;
import com.soapp.global.GlobalVariables;
import com.soapp.global.MiscHelper;
import com.soapp.global.Preferences;
import com.soapp.global.UIHelper;
import com.soapp.setup.Soapp;
import com.soapp.sql.DatabaseHelper;
import com.soapp.xmpp.PubsubHelper.PubsubNodeCall;

import net.glxn.qrgen.android.QRCode;

import java.io.File;
import java.util.UUID;

import androidx.constraintlayout.widget.ConstraintLayout;
import io.fabric.sdk.android.services.concurrency.AsyncTask;
import io.github.rockerhieu.emojicon.EmojiconTextView;
import okhttp3.MediaType;
import okhttp3.RequestBody;

/* Created by Soapp on 29/06/2017. */

public class Profile extends BaseActivity implements View.OnClickListener {
    //basics
    private UIHelper uiHelper = new UIHelper();
    private Preferences preferences = Preferences.getInstance();
    private DatabaseHelper databaseHelper = DatabaseHelper.getInstance();

    public static ImageView image_view_profile;
    private TextView tv_edit_profile;

    ImageView qr_code_display;
    public String userJid;
    public static String displayName;
    PubsubNodeCall pubsubNodeCall = new PubsubNodeCall();
    ProgressDialog dialog;
    private ConstraintLayout qr_code_wrapper;
    //syah
    private EmojiconTextView text_view_profile_name;
    public static ProgressBar pb_profiel_image;

    public static void profilePicUpdateListener() {
        Preferences preferences = Preferences.getInstance();
        DatabaseHelper databaseHelper = DatabaseHelper.getInstance();
        String userJid = preferences.getValue(Soapp.getInstance().getApplicationContext(), GlobalVariables.STRPREF_USER_ID);
        byte[] profileImgByte = databaseHelper.getImageBytesThumbFromContactRoster(userJid);

        GlideApp.with(Soapp.getInstance().getApplicationContext())
                .load(profileImgByte)
                .placeholder(R.drawable.in_propic_circle_150px)
                .apply(RequestOptions.circleCropTransform())
                .thumbnail(0.5f)
                .encodeQuality(50)
                .override(180, 180)
                .into(image_view_profile);
    }

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settingstab_profile);
        setupToolbar();

        new UIHelper().setStatusBarColor(this, false , R.color.black3a);

        pb_profiel_image = findViewById(R.id.pb_profiel_image);
        pb_profiel_image.bringToFront();

        qr_code_wrapper = findViewById(R.id.qr_code_wrapper);
        qr_code_wrapper.setOnClickListener(this);

        image_view_profile = findViewById(R.id.image_view_profile);
        image_view_profile.setOnClickListener(Profile.this);
        tv_edit_profile = findViewById(R.id.tv_edit_profile);
        tv_edit_profile.setOnClickListener(this);

        text_view_profile_name = findViewById(R.id.text_view_profile_name);
        text_view_profile_name.setOnClickListener(this);

        //displayname
        displayName = preferences.getValue(this, GlobalVariables.STRPREF_USERNAME);
        text_view_profile_name.setText(displayName);

        //set font type
        Typeface typeface = Typeface.createFromAsset(getAssets(),"fonts/Quicksand_Bold.otf");
        text_view_profile_name.setTypeface(typeface);


        //Display the QR
        qr_code_display = findViewById(R.id.qr_code_display);
        userJid = preferences.getValue(Profile.this, GlobalVariables.STRPREF_USER_ID);
        String qrdet = "Soapp" + userJid;

        Bitmap myBitmap = QRCode.from(qrdet).withSize(200, 200).bitmap();

        qr_code_display.setImageBitmap(myBitmap);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            //edit display name
            case R.id.text_view_profile_name:
                Runnable runnable = () -> {
                    new asyncUpdateProfile().execute();
                };

                //jason's edittext
                new UIHelper().editTextAlertDialog(this, getResources().getString(R.string.profile_name_edit),
                        displayName, "Profile", null, null, true, runnable);
                break;

            case R.id.image_view_profile:
            case R.id.tv_edit_profile:
                startActivity(new Intent(this, ProfilePic.class));
                break;


            case R.id.qr_code_wrapper:
                startActivity(new Intent(this, QrReader.class));
                break;

            default:
                break;

        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        String userJid = preferences.getValue(Profile.this, GlobalVariables.STRPREF_USER_ID);
        byte[] profileImgByte = databaseHelper.getImageBytesThumbFromContactRoster(userJid);

        GlideApp.with(this)
                .load(profileImgByte)
                .placeholder(R.drawable.in_propic_circle_150px)
                .apply(RequestOptions.circleCropTransform())
                .thumbnail(0.5f)
                .encodeQuality(50)
                .override(180, 180)
                .into(image_view_profile);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    @Override
    protected void onDestroy() {
        ImageCropPreviewActivity.isCrop = false;

        //delete temp.jpg after saving profile image
        File tempFile = new File(GlobalVariables.TEMP_IMG_FILE);
        if (tempFile.exists()) {
            tempFile.delete();
        }

        super.onDestroy();
    }

    private class asyncUpdateProfile extends AsyncTask<String, Integer, Void> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            //Display progressDialog before download starts
            dialog = ProgressDialog.show(Profile.this, null, getString(R.string.updating_profile));
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
        }

        @Override
        protected Void doInBackground(String... params) {
            //define universal variables first
            String access_token = preferences.getValue(Profile.this, GlobalVariables.STRPREF_ACCESS_TOKEN);

            RequestBody name = RequestBody.create(MediaType.parse("text/plain"), displayName);

            //if new image selected, need to add image to retro and use uploadImage retro
            //no image chosen, just update profile name
            //build retrofit client for uploading profile img
            UpdateUserProfileNameOnly client = RetrofitAPIClient.getClient().create(UpdateUserProfileNameOnly.class);
            RequestBody image = RequestBody.create(MediaType.parse("text/plain"), "null");

            retrofit2.Call<UserModel> call = client.editUser("Bearer " + access_token, name, image);
            call.enqueue(new retrofit2.Callback<UserModel>() {
                @Override
                public void onResponse(retrofit2.Call<UserModel> call, final retrofit2.Response<UserModel> response) {
                    if (!response.isSuccessful()) {
                        new Handler(Looper.getMainLooper())
                                .post(new Runnable() {
                                    @Override
                                    public void run() {

                                        new MiscHelper().retroLogUnsuc(response, "uploadProfile ", "JAY");
                                        Toast.makeText(Profile.this, R.string.onresponse_unsuccessful, Toast.LENGTH_SHORT).show();
                                        if (dialog != null && dialog.isShowing()) {
                                            dialog.dismiss();
                                        }

                                    }
                                });
                        return;
                    }

                    String uniqueId = UUID.randomUUID().toString();
                    preferences.save(Profile.this, GlobalVariables.STRPREF_USERNAME, displayName);
                    pubsubNodeCall.PubsubSingle(userJid, userJid, uniqueId);

                    text_view_profile_name.setText(displayName);
                    SettingsTab.text_view_profile_name.setText(displayName);

                    if (dialog != null && dialog.isShowing()) {
                        dialog.dismiss();
                    }
                }

                @Override
                public void onFailure(retrofit2.Call<UserModel> call, Throwable t) {
                    new MiscHelper().retroLogFailure(t, "uploadProfile ", "JAY");
                    Toast.makeText(Profile.this, R.string.onfailure, Toast.LENGTH_SHORT).show();
                    if (dialog != null && dialog.isShowing()) {
                        dialog.dismiss();
                    }
                }
            });

            return null;
        }
    }
}

