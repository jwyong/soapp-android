package com.soapp.registration;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Base64;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.google.firebase.iid.FirebaseInstanceId;
import com.soapp.R;
import com.soapp.SoappApi.APIGlobalVariables;
import com.soapp.SoappApi.ApiModel.NotificationRegisterTokenModel;
import com.soapp.SoappApi.ApiModel.Resource1v3Model;
import com.soapp.SoappApi.ApiModel.UserModel;
import com.soapp.SoappApi.Interface.DownloadFromUrlInterface;
import com.soapp.SoappApi.Interface.RegisterInterface;
import com.soapp.SoappApi.Interface.Resource1v3Interface;
import com.soapp.SoappApi.Interface.UpdateUserProfileNameImage;
import com.soapp.SoappApi.RetrofitAPIClient;
import com.soapp.global.CameraHelper;
import com.soapp.global.DirectoryHelper;
import com.soapp.global.GlideAPI.GlideApp;
import com.soapp.global.GlobalVariables;
import com.soapp.global.ImageHelper;
import com.soapp.global.MediaGallery.GalleryMainActivity;
import com.soapp.global.MiscHelper;
import com.soapp.global.PermissionHelper;
import com.soapp.global.Preferences;
import com.soapp.global.UIHelper;
import com.soapp.setup.Soapp;
import com.soapp.sql.DatabaseHelper;

import java.io.IOException;

import androidx.core.content.ContextCompat;
import io.github.rockerhieu.emojicon.EmojiconEditText;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions.withCrossFade;

public class UserProfile extends Activity implements View.OnClickListener {

    //debug
    String tag = "diao";
    String className = getClass().getSimpleName();
    String jid;
    byte[] imageByteThumb;
    String imageUrl = null;

    public static ImageView img_icon;
    public static Uri selectedImage;
    int MY_PERMISSIONS_REQUEST_CAMERA = 100;
    String ALLOW_KEY = "ALLOWED";
    String CAMERA_PREF = "camera_pref";
    ImageView clear_title;
    public String askAgain;
    private ProgressDialog pDialog;
    private TextView txt_img = null, text_counter = null;
    private EmojiconEditText edtxt_name = null;
    private Button btn_next = null;

    private Preferences preferences = Preferences.getInstance();

    private ProgressDialog dialog;
    private MiscHelper miscHelper = new MiscHelper();
    int contacts, j = 0, k = 0, m = 0, oldGrps;
    public static DatabaseHelper databaseHelper = DatabaseHelper.getInstance();


    private final TextWatcher NameWatcher = new TextWatcher() {
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        public void onTextChanged(CharSequence s, int start, int before, int count) {
        }

        public void afterTextChanged(Editable s) {
            if (s.length() == 0) {
                btn_next.setEnabled(false);
                btn_next.setBackgroundResource(R.drawable.xml_round_corner_20dp);

            } else {

                btn_next.setEnabled(true);
                btn_next.setBackgroundResource(R.drawable.xml_roundcorner_primarylogo);
            }

            int count = 25 - s.length();
            text_counter.setText(String.valueOf(count));

        }
    };

    private ViewGroup verifyreg;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.reg_userprofile);

//        syah testing
//        Window window = this.getWindow();
//        window.setBackgroundDrawable(getResources().getDrawable(R.drawable.ly_signin_testasty));

        jid = preferences.getValue(this, GlobalVariables.STRPREF_USER_ID);

        //reg_userprofile
        askAgain = preferences.getValue(this, "askAgain");

        new PermissionHelper().CheckPermissions(this, 1000, R.string.permission_txt);

        verifyreg = findViewById(R.id.verifyreg);
        img_icon = findViewById(R.id.img_icon);
        txt_img = findViewById(R.id.new_g_create_gallery);
        edtxt_name = findViewById(R.id.edtxt_name);
        btn_next = findViewById(R.id.btn_next);
        verifyreg.setOnClickListener(this);
        img_icon.setOnClickListener(UserProfile.this);
        txt_img.setOnClickListener(UserProfile.this);
        btn_next.setOnClickListener(UserProfile.this);
        edtxt_name.addTextChangedListener(NameWatcher);
        text_counter = findViewById(R.id.text_counter);
        preferences.save(UserProfile.this, GlobalVariables.STRPREF_LOGIN_STATUS, "halfsuccessful");
        text_counter.setText(String.valueOf(25 - edtxt_name.length()));
        clear_title = findViewById(R.id.clear_title);
        clear_title.setOnClickListener(this);

        btn_next.setEnabled(false);

        //new setting visibility for clear title icon
        edtxt_name.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    clear_title.setVisibility(View.VISIBLE);
                } else {
                    clear_title.setVisibility(View.INVISIBLE);
                }
            }
        });

        edtxt_name.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            public boolean onEditorAction(TextView view, int actionId, KeyEvent event) {
                if ((event != null && (event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) || (actionId == EditorInfo.IME_ACTION_DONE)) {
                    //do what you want on the press of 'done'
                    if (btn_next.isEnabled()) {
                        btn_next.performClick();
                    } else {
                        return true;
                    }
                }
                return false;
            }
        });

        resourceRetro();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (selectedImage != null) {
            GlideApp.with(this)
                    .load(selectedImage)
                    .placeholder(R.drawable.in_propic_circle_520px)
                    .diskCacheStrategy(DiskCacheStrategy.NONE)
                    .skipMemoryCache(true)
                    .apply(RequestOptions.circleCropTransform())
                    .transition(withCrossFade())
                    .into(img_icon);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.img_icon:
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) !=
                        PackageManager.PERMISSION_GRANTED) {
                    //no permission for camera
                    new PermissionHelper().CheckPermissions(this, 1001, R.string.permission_txt);
                } else {
                    openCamera();
                }
                break;

            case R.id.new_g_create_gallery:
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                    // no permission for storages
                    new PermissionHelper().CheckPermissions(this, 1006, R.string.permission_txt);

                } else {
                    Intent intent = new Intent(this, GalleryMainActivity.class);
                    intent.putExtra("from", "UserProfileMedia");
                    intent.putExtra("title", "Select media");
                    intent.putExtra("mode", 2);
                    intent.putExtra("maxSelection", 1);
                    startActivity(intent);

                }
                break;

            case R.id.btn_next:
                String userDisplayName = edtxt_name.getText().toString().trim();
                new asyncUserProfileNext().execute(userDisplayName);

                break;

            case (R.id.clear_title): //new function for clearing appointment title
                edtxt_name.setText("");


            case R.id.verifyreg:
                new UIHelper().closeKeyBoard(this);

                break;
            default:
                break;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case 1000: //
                for (int i = 0; i < permissions.length; i++) {
                    String permission = permissions[i];

                    if (grantResults[i] == PackageManager.PERMISSION_DENIED) {
                        boolean showRationale = false;
                        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                            showRationale = shouldShowRequestPermissionRationale(permission);

                        }
                        if (!showRationale) {
                            preferences.save(UserProfile.this, "askAgain", "false");
                            break;
                        }

                    } else {
                        if (permission.equals(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                            new DirectoryHelper().checkAndCreateDir(this, true);
                            break;
                        }
                    }
                }

                break;

            case 1006:
                for (int i = 0; i < permissions.length; i++) {
                    String permission = permissions[i];

                    if (grantResults[i] == PackageManager.PERMISSION_DENIED) {
                        boolean showRationale = false;
                        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                            showRationale = shouldShowRequestPermissionRationale(permission);

                        }
                        if (!showRationale) {
                            preferences.save(UserProfile.this, "askAgain", "false");
                            break;
                        }

                    } else {

                        if (permission.equals(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                            new DirectoryHelper().checkAndCreateDir(this, true);

                            Intent intent = new Intent(this, GalleryMainActivity.class);
                            intent.putExtra("from", "UserProfileMedia");
                            intent.putExtra("title", "Select media");
                            intent.putExtra("mode", 2);
                            intent.putExtra("maxSelection", 1);
                            startActivity(intent);
                        }
                    }
                }

                break;

            case 1001:
                for (int i = 0, len = permissions.length; i < len; i++) {
                    String permission = permissions[i];

                    if (grantResults[i] == PackageManager.PERMISSION_DENIED) {
                        boolean showRationale = false;
                        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                            showRationale = shouldShowRequestPermissionRationale(permission);

                        }

                        if (!showRationale) {

                            preferences.save(this, "askAgain", "false");
                            Toast.makeText(this, R.string.need_camera, Toast.LENGTH_SHORT)
                                    .show();
                            break;
                        }

                    } else {
                        if (permission.equals(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                            new DirectoryHelper().checkAndCreateDir(this, true);
                        }
                        openCamera();
                        break;
                    }
                }
                break;

            default:
                break;
        }
    }

    private void openCamera() {
        Intent cameraintent = new CameraHelper().startCameraIntent(this);

        cameraintent.putExtra("from", "UserProfile");

        startActivity(cameraintent);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }

    private void registerFcmTokenWithServer(final String refresh_token) {
        String access_token = preferences.getValue(Soapp.getInstance().getApplicationContext(),
                GlobalVariables.STRPREF_ACCESS_TOKEN);
        if (access_token == null) {
            return;
        }

        NotificationRegisterTokenModel notificationRegisterTokenModel =
                new NotificationRegisterTokenModel(preferences.getValue(Soapp.getInstance().getApplicationContext(),
                        GlobalVariables.STRPREF_USER_ID), "fcm", refresh_token);
        RegisterInterface client = RetrofitAPIClient.getClient().create(RegisterInterface.class);
        retrofit2.Call<NotificationRegisterTokenModel> call =
                client.notificationRegisterToken(notificationRegisterTokenModel, "Bearer " + access_token);
        call.enqueue(new retrofit2.Callback<NotificationRegisterTokenModel>() {
            @Override
            public void onResponse(retrofit2.Call<NotificationRegisterTokenModel> call,
                                   retrofit2.Response<NotificationRegisterTokenModel> response) {
                if (!response.isSuccessful()) {

                    new MiscHelper().retroLogUnsuc(response, "regFCM", "JAY");

                    return;
                }
//                FcmTokenData.setFcmToken(getApplicationContext(), refresh_token);
                Preferences.getInstance().save(UserProfile.this, GlobalVariables.FCM_TOKEN, refresh_token);
            }

            @Override
            public void onFailure(retrofit2.Call<NotificationRegisterTokenModel> call, Throwable t) {

                new MiscHelper().retroLogFailure(t, "regFCM", "JAY");

            }
        });
    }

    //progress dialog for btn next
    private class asyncUserProfileNext extends android.os.AsyncTask<String, Integer, Void> {
        byte[] imageByteFull = null;
        byte[] imageByteThumb = null;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog = ProgressDialog.show(UserProfile.this, null, "Setting Profile");
        }

        @Override
        protected Void doInBackground(String... params) {
            FirebaseInstanceId.getInstance().getInstanceId()
                    .addOnSuccessListener(UserProfile.this,
                            instanceIdResult -> {
                                String refreshedPrefToken = preferences.getValue(UserProfile.this,
                                        GlobalVariables.FCM_TOKEN);
                                if (!instanceIdResult.getToken().equals(refreshedPrefToken)) {
                                    registerFcmTokenWithServer(instanceIdResult.getToken());
                                }

                            });

            preferences = Preferences.getInstance();
            String access_token = preferences.getValue(UserProfile.this, GlobalVariables.STRPREF_ACCESS_TOKEN);
            final String user_id = preferences.getValue(UserProfile.this, GlobalVariables.STRPREF_USER_ID);

            //if has select image
            Bitmap bitmap;
            RequestBody image = null;
            ImageHelper imageHelper = new ImageHelper(UserProfile.this);
            MultipartBody.Part filePart = null;
            if (selectedImage != null) {
                bitmap = imageHelper.getBitmapForProfile(selectedImage);
                imageByteFull = DirectoryHelper.getBytesFromBitmap100(bitmap);
                imageByteThumb = DirectoryHelper.getBytesFromBitmap33(bitmap);
                image = RequestBody.create(APIGlobalVariables.JPEG, imageByteFull);
                filePart = MultipartBody.Part.createFormData("image",
                        "name.jpeg", image);
            }

            RequestBody name = RequestBody.create(MediaType.parse("text/plain"), params[0]);

            //build retrofit
            UpdateUserProfileNameImage client = RetrofitAPIClient.getClient().create(UpdateUserProfileNameImage.class);
            retrofit2.Call<UserModel> call = client.editUser("Bearer " + access_token, filePart, name);
            call.enqueue(new retrofit2.Callback<UserModel>() {
                @Override
                public void onResponse(retrofit2.Call<UserModel> call, final retrofit2.Response<UserModel> response) {
                    if (!response.isSuccessful()) {
                        new Handler(Looper.getMainLooper()).post(() -> {
                            new MiscHelper().retroLogUnsuc(response, "upProfile", "JAY");

                            Toast.makeText(UserProfile.this, R.string.onresponse_unsuccessful, Toast.LENGTH_SHORT)
                                    .show();
                            if (pDialog != null && pDialog.isShowing()) {
                                pDialog.dismiss();
                            }
                        });
                        return;
                    }

                    preferences.save(UserProfile.this, GlobalVariables.STRPREF_USERNAME,
                            edtxt_name.getText().toString().trim());

                    if (selectedImage == null && imageUrl != null) {
                        downloadFromUrlRetro(imageUrl);
                    } else {
                        //save self profile img byte to contact roster
                        DatabaseHelper.getInstance().updateIMGContactRoster(user_id,
                                imageByteThumb, imageByteFull);
                    }

                    Intent IntentMain = new Intent(UserProfile.this, SyncContact.class);
                    startActivity(IntentMain);

                    new Handler(Looper.getMainLooper()).post(() -> {
                        if (pDialog != null && pDialog.isShowing()) {
                            pDialog.dismiss();
                        }
                    });
                    UserProfile.this.finish();
                }

                @Override
                public void onFailure(retrofit2.Call<UserModel> call, final Throwable t) {
                    new Handler(Looper.getMainLooper())
                            .post(new Runnable() {
                                @Override
                                public void run() {

                                    new MiscHelper().retroLogFailure(t, "upProfile", "JAY");

                                    Toast.makeText(UserProfile.this, R.string.onfailure, Toast.LENGTH_SHORT)
                                            .show();
                                    if (pDialog != null && pDialog.isShowing()) {
                                        pDialog.dismiss();
                                    }
                                }
                            });
                }
            });
            return null;
        }
    }

    // get resource url if user got in db
    public void resourceRetro() {
        String access_token = preferences.getValue(this, GlobalVariables.STRPREF_ACCESS_TOKEN);
        Resource1v3Interface resource1v3Interface = RetrofitAPIClient.getClient().create(Resource1v3Interface.class);
        retrofit2.Call<Resource1v3Model> call = resource1v3Interface.getResource(jid, "Bearer " + access_token);
        call.enqueue(new retrofit2.Callback<Resource1v3Model>() {
            @Override
            public void onResponse(retrofit2.Call<Resource1v3Model> call,
                                   retrofit2.Response<Resource1v3Model> response) {
                if (!response.isSuccessful()) {
                    return;
                }
                String resourceURL = response.body().getResource_url();
                String thumb = response.body().getThumbnail();

                if (resourceURL != null) {
                    imageUrl = resourceURL;
                    imageByteThumb = Base64.decode(thumb, Base64.DEFAULT);
                    GlideApp.with(UserProfile.this)
                            .load(resourceURL)
                            .placeholder(R.drawable.in_propic_circle_520px)
                            .diskCacheStrategy(DiskCacheStrategy.NONE)
                            .skipMemoryCache(true)
                            .apply(RequestOptions.circleCropTransform())
                            .transition(withCrossFade())
                            .into(img_icon);
                }
            }

            @Override
            public void onFailure(retrofit2.Call<Resource1v3Model> call, Throwable t) {
            }
        });
    }

    public void downloadFromUrlRetro(String resourceURL) {
        DownloadFromUrlInterface client = RetrofitAPIClient.getClient().create(DownloadFromUrlInterface.class);
        retrofit2.Call<ResponseBody> call1 = client.downloadFileWithDynamicUrlAsync(resourceURL);
        call1.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (!response.isSuccessful()) {
                    return;
                }
                try {
                    byte[] imageByte100 = response.body().bytes();
                    databaseHelper.updateIMGContactRoster(jid, imageByteThumb, imageByte100);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {

            }
        });
    }
}
