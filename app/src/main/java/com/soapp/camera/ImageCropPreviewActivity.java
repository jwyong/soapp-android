package com.soapp.camera;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.isseiaoki.simplecropview.CropImageView;
import com.isseiaoki.simplecropview.callback.CropCallback;
import com.isseiaoki.simplecropview.callback.SaveCallback;
import com.soapp.R;
import com.soapp.SoappApi.APIGlobalVariables;
import com.soapp.SoappApi.ApiModel.GroupModel;
import com.soapp.SoappApi.ApiModel.RestaurantReviewImageUploadModel;
import com.soapp.SoappApi.ApiModel.UserModel;
import com.soapp.SoappApi.Interface.RestaurantReviewImageUpload;
import com.soapp.SoappApi.Interface.UpdateGroupProfileNameImage;
import com.soapp.SoappApi.Interface.UpdateUserProfileNameImage;
import com.soapp.SoappApi.RetrofitAPIClient;
import com.soapp.base.BaseActivity;
import com.soapp.chat_class.group_chat.GroupChatLog;
import com.soapp.chat_class.group_chat.details.GroupChatDetail;
import com.soapp.chat_class.group_chat.details.GroupProfilePic;
import com.soapp.chat_class.single_chat.IndiChatLog;
import com.soapp.global.CameraHelper;
import com.soapp.global.DecryptionHelper;
import com.soapp.global.DirectoryHelper;
import com.soapp.global.EncryptionHelper;
import com.soapp.global.GlideAPI.GlideApp;
import com.soapp.global.GlobalVariables;
import com.soapp.global.ImageHelper;
import com.soapp.global.MediaGallery.GalleryMainActivity;
import com.soapp.global.MediaGallery.OpenGallery;
import com.soapp.global.MiscHelper;
import com.soapp.global.Preferences;
import com.soapp.global.UploadImageRequestBody;
import com.soapp.global.UploadVideoRequestBody;
import com.soapp.new_chat_schedule.group.create_grp.NewCreateGroupController;
import com.soapp.registration.UserProfile;
import com.soapp.schedule_class.new_appt.NewGrpAppt.NewGrpApptActivity;
import com.soapp.settings_tab.ContactUs;
import com.soapp.settings_tab.Profile;
import com.soapp.settings_tab.ProfilePic;
import com.soapp.setup.Soapp;
import com.soapp.sql.DatabaseHelper;
import com.soapp.xmpp.GroupChatHelper.GroupChatStanza;
import com.soapp.xmpp.PubsubHelper.PubsubNodeCall;
import com.soapp.xmpp.SingleChatHelper.SingleChatStanza;
import com.soapp.xmpp.SmackHelper;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Response;

//import com.erikagtierrez.multiple_media_picker.Fragments.OneFragment;
//import com.erikagtierrez.multiple_media_picker.Fragments.TwoFragment;
//import com.erikagtierrez.multiple_media_picker.Gallery;

//compress

/* Created by Kirill on 10/25/2017. */

public class ImageCropPreviewActivity extends BaseActivity implements UploadVideoRequestBody.UploadCallBack, UploadImageRequestBody.UploadCallBack {
    //basics
    private CameraHelper cameraHelper = new CameraHelper();
    private DatabaseHelper databaseHelper = DatabaseHelper.getInstance();

    private static final int ORIENTATION_0 = 0;
    private static final int ORIENTATION_90 = 90;
    private static final int ORIENTATION_180 = 180;
    private static final int ORIENTATION_270 = 270;
    public static Bitmap image;
    public static boolean isCrop = false;
    public static String orient = "vertical";
    public File newFile, realImageWOChanges;
    int rotation = 0;
    boolean isPicked = false;
    Preferences preferences = Preferences.getInstance();
    SingleChatStanza singleChatStanza = new SingleChatStanza();
    GroupChatStanza groupChatStanza = new GroupChatStanza();
    String rotationToSend = "0";
    Uri selectedImage;
    boolean fromCamera = false;
    int resID = -1;
    boolean is4K = false;
    //
    File profilePathFile;
    PubsubNodeCall pubsubNodeCall = new PubsubNodeCall();
    private CropImageView mImageView;
    private ExifInterface mExif;
    private String imageAddress, from;
    private boolean isUploaded = false;
    private ProgressDialog progressDialog, updateProfileImageProgressDialog;
    // encryption & decryption
    private EncryptionHelper encryptionHelper = new EncryptionHelper();
    private DecryptionHelper decryptionHelper = new DecryptionHelper();

    public static Bitmap rotate(Bitmap image, int angle) {
        if (angle == ORIENTATION_0)
            return image;

        Matrix matrix = new Matrix();
        matrix.postRotate(angle);
        return Bitmap.createBitmap(image, 0, 0, image.getWidth(), image.getHeight(),
                matrix, true);

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.img_preview_crop);
        setupToolbar();

        mImageView = findViewById(R.id.cropImageView);
        Intent intent = getIntent();

        //if does not have intent from, this activity consider useless
        if (intent.hasExtra("from")) {
            from = intent.getExtras().getString("from");

            //come from camera
            if (intent.hasExtra("camera")) {
                fromCamera = true;
            }

            //if has picture
            setTitle(R.string.img_preview);

            //for from foodtabnearby (Public image upload)
            if (intent.hasExtra("resID")) {
                resID = getIntent().getIntExtra("resID", -1);
            }

            imageAddress = intent.getExtras().getString("image_address");

            mImageView.setFrameColor(Color.BLACK);
//            mImageView.setOverlayColor(Color.WHITE);
            mImageView.setHandleShowMode(CropImageView.ShowMode.SHOW_ALWAYS);
            mImageView.setGuideShowMode(CropImageView.ShowMode.SHOW_ALWAYS);
            mImageView.setInitialFrameScale(0.75f);

            mImageView.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View view, MotionEvent motionEvent) {
                    switch (motionEvent.getAction()) {
                        case MotionEvent.ACTION_UP:
                            break;
                    }
                    return false;
                }
            });
            File imageAddressFile;

            switch (from) {
                case "chat":
                    //the image is in the cache, duplicate same image and named to copy.jpg in cache
                    //so that we can save the image W/O cropping and rotating later
                    imageAddressFile = new File(imageAddress);
                    realImageWOChanges = new File(getCacheDir(), "copy.jpg");
                    try {
                        realImageWOChanges = Util.ryanCopyFile(imageAddressFile, realImageWOChanges);
                        imageAddress = realImageWOChanges.getPath();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    mImageView.setCropMode(CropImageView.CropMode.FREE);
                    break;

                case "external_image":
                case "chatmedia":
                    //do not need to copy into cache if come from chat gallery
                    mImageView.setCropMode(CropImageView.CropMode.FREE);
                    break;

                default:
                    //the image is in the cache, duplicate same image and named to copy.jpg in cache
                    //so that we can save the image W/O cropping and rotating later
                    imageAddressFile = new File(imageAddress);
                    realImageWOChanges = new File(getCacheDir(), "copy.jpg");

                    try {
                        realImageWOChanges = Util.ryanCopyFile(imageAddressFile, realImageWOChanges);
                        imageAddress = realImageWOChanges.getPath();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    mImageView.setCropMode(CropImageView.CropMode.SQUARE);
                    break;
            }

            //decode into bitmap
            image = BitmapFactory.decodeFile(imageAddress);
            int android_version = new MiscHelper().getDeviceAndroidVersion();
            try {
                mExif = new ExifInterface(imageAddress);
                if (image != null) {
                    //temporary only, still need to test on samsung, htc ,xiaomi and etc seems ok for honor
                    if (mExif != null) {
                        rotation = mExif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
                        checkOrientCamera();
                    }
                } else {
                    finish();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }


            // Checks the orientation of the screen
            int orientationScreen = this.getResources().getConfiguration().orientation;
            if (orientationScreen == Configuration.ORIENTATION_LANDSCAPE) {
                if (image != null) {
                    if (image.getHeight() < image.getWidth()) {
                        mImageView.setScaleType(ImageView.ScaleType.FIT_XY);
                    }
                }
            }

            ImageHelper imageHelper = new ImageHelper(this);

            if (image.getHeight() >= 3000 || image.getWidth() >= 3000) {
                is4K = true;
            }
            image = imageHelper.scaleDown3KImage(image);

            GlideApp.with(this)
                    .asBitmap()
                    .placeholder(R.drawable.default_img_full)
                    .load(imageAddress)
                    .skipMemoryCache(true)
                    .diskCacheStrategy(DiskCacheStrategy.NONE)
                    .thumbnail(0.5f)
                    .encodeQuality(50)
                    .into(mImageView);

//                mImageView.setImageBitmap(image);
        } else {
            Toast.makeText(this, "Something went wrong: missing 'from'", Toast.LENGTH_SHORT).show();
            finish();
        }

    }

    @Override
    public void onBackPressed() {
        finish();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.image_preview, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {

        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (image != null) {
            if (id == R.id.action_rotate_anti_clockwise) {
                mImageView.rotateImage(CropImageView.RotateDegrees.ROTATE_M90D);
                try {
                    rotation = mExif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);

                    switch (rotation) {
                        case 6:
                            mExif.setAttribute(ExifInterface.TAG_ORIENTATION,
                                    String.valueOf(1));
                            mExif.saveAttributes();
                            rotation = mExif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
                            image = rotate(image, ORIENTATION_270);
                            rotationToSend = "0";
                            changeOrientHoriBasedOnManufacturer();
                            break;

                        case 1:
                            mExif.setAttribute(ExifInterface.TAG_ORIENTATION,
                                    String.valueOf(8));
                            mExif.saveAttributes();
                            rotation = mExif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
                            image = rotate(image, ORIENTATION_270);
                            rotationToSend = "270";
                            changeOrientVerBasedOnManufacturer();
                            break;

                        case 8:
                            mExif.setAttribute(ExifInterface.TAG_ORIENTATION,
                                    String.valueOf(3));
                            mExif.saveAttributes();
                            rotation = mExif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
                            image = rotate(image, ORIENTATION_270);
                            rotationToSend = "180";
                            changeOrientHoriBasedOnManufacturer();
                            break;

                        case 3:
                            mExif.setAttribute(ExifInterface.TAG_ORIENTATION,
                                    String.valueOf(6));
                            mExif.saveAttributes();
                            rotation = mExif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
                            image = rotate(image, ORIENTATION_270);
                            rotationToSend = "90";
                            changeOrientVerBasedOnManufacturer();
                            break;

                        case 0:
                            mExif.setAttribute(ExifInterface.TAG_ORIENTATION,
                                    String.valueOf(8));
                            mExif.saveAttributes();
                            rotation = mExif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
                            image = rotate(image, ORIENTATION_270);
                            rotationToSend = "270";
                            changeOrientVerBasedOnManufacturer();
                            break;

                        default:
                            mExif.setAttribute(ExifInterface.TAG_ORIENTATION,
                                    String.valueOf(rotation));
                            mExif.saveAttributes();
                            rotation = mExif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
                            break;
                    }

                } catch (IOException e) {
                }

            } else if (id == R.id.action_rotate_clockwise) {
                mImageView.rotateImage(CropImageView.RotateDegrees.ROTATE_90D);
                try {
                    rotation = mExif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
                    switch (rotation) {
                        case 6:
                            mExif.setAttribute(ExifInterface.TAG_ORIENTATION,
                                    String.valueOf(3));
                            mExif.saveAttributes();
                            rotation = mExif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
                            image = rotate(image, ORIENTATION_90);
                            rotationToSend = "180";
                            changeOrientHoriBasedOnManufacturer();
                            break;

                        case 3:
                            mExif.setAttribute(ExifInterface.TAG_ORIENTATION,
                                    String.valueOf(8));
                            mExif.saveAttributes();
                            rotation = mExif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
                            image = rotate(image, ORIENTATION_90);
                            rotationToSend = "270";
                            changeOrientVerBasedOnManufacturer();
                            break;

                        case 8:
                            mExif.setAttribute(ExifInterface.TAG_ORIENTATION,
                                    String.valueOf(1));
                            mExif.saveAttributes();
                            rotation = mExif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
                            image = rotate(image, ORIENTATION_90);
                            rotationToSend = "0";
                            changeOrientHoriBasedOnManufacturer();
                            break;

                        case 1:
                            mExif.setAttribute(ExifInterface.TAG_ORIENTATION,
                                    String.valueOf(6));
                            mExif.saveAttributes();
                            rotation = mExif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
                            image = rotate(image, ORIENTATION_90);
                            rotationToSend = "90";
                            changeOrientVerBasedOnManufacturer();
                            break;

                        case 0:
                            mExif.setAttribute(ExifInterface.TAG_ORIENTATION,
                                    String.valueOf(6));
                            mExif.saveAttributes();
                            rotation = mExif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
                            image = rotate(image, ORIENTATION_90);
                            rotationToSend = "90";
                            changeOrientVerBasedOnManufacturer();
                            break;

                        default:
                            mExif.setAttribute(ExifInterface.TAG_ORIENTATION,
                                    String.valueOf(rotation));
                            mExif.saveAttributes();
                            rotation = mExif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
                            break;
                    }
                } catch (IOException e) {
                }

            } else if (id == R.id.cropaction_save) {

                if (mImageView.getCroppedBitmap().getHeight() >= 720 || mImageView.getCroppedBitmap().getWidth() >= 720) {
                    if (is4K) {
                        ImageHelper imageHelper = new ImageHelper(this);
                        imageAddress = imageHelper.saveBitmapIntoFile(getCacheDir().getPath() + "4k.jpg", image);
                    }
                }
                new SaveFile().execute(imageAddress);
            } else if (id == android.R.id.home) {
                onBackPressed();
            }
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onStop() {
        super.onStop();

    }

    public void uploadPropicImageFile(String filePath, final int resID) {
        String access_token = preferences.getValue(ImageCropPreviewActivity.this, GlobalVariables.STRPREF_ACCESS_TOKEN);
        File file = new File(filePath);

        RequestBody imageRB = RequestBody.create(APIGlobalVariables.JPEG, file);
        MultipartBody.Part image = MultipartBody.Part.createFormData("image", "name.jpeg", imageRB);
        MultipartBody.Part resId = MultipartBody.Part.createFormData("res_ID", String.valueOf(resID));

        //build retrofit
        RestaurantReviewImageUpload client = RetrofitAPIClient.getClient().create(RestaurantReviewImageUpload.class);
        retrofit2.Call<RestaurantReviewImageUploadModel> call = client.uploadImage("Bearer " + access_token, resId, image);
        call.enqueue(new retrofit2.Callback<RestaurantReviewImageUploadModel>() {
            @Override
            public void onResponse(Call<RestaurantReviewImageUploadModel> call, Response<RestaurantReviewImageUploadModel> response) {
                if (!response.isSuccessful()) {

                    new MiscHelper().retroLogUnsuc(response, "uploadPropicImageFile ", "JAY");
                    Toast.makeText(ImageCropPreviewActivity.this, R.string.onresponse_unsuccessful, Toast
                            .LENGTH_SHORT).show();
                } else {
                    Toast.makeText(ImageCropPreviewActivity.this, String.valueOf(response.body().getResponse()), Toast
                            .LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<RestaurantReviewImageUploadModel> call, Throwable t) {
                new MiscHelper().retroLogFailure(t, "uploadPropicImageFile ", "JAY");

                Toast.makeText(ImageCropPreviewActivity.this, R.string.onfailure, Toast
                        .LENGTH_SHORT).show();
            }
        });

    }

    public void reverseOrient() {
        if (orient.equals("vertical")) {
            orient = "horizontal";
        } else {
            orient = "vertical";
        }
    }

    public void changeOrientVerBasedOnManufacturer() {
        if (android.os.Build.MANUFACTURER.toLowerCase().equals("huawei") || android.os.Build.MANUFACTURER.toLowerCase().equals("xiaomi") || android.os.Build.MANUFACTURER.toLowerCase().equals("htc")) {
            reverseOrient();
        } else {
            orient = "vertical";
        }
    }

    public void changeOrientHoriBasedOnManufacturer() {
        if (android.os.Build.MANUFACTURER.toLowerCase().equals("huawei") || android.os.Build.MANUFACTURER.toLowerCase().equals("xiaomi") || android.os.Build.MANUFACTURER.toLowerCase().equals("htc")) {
            reverseOrient();
        } else {
            orient = "vertical";
        }
    }

    public void checkOrientCamera() {
        if (rotation == 6) {
            //hori
            image = rotate(image, ORIENTATION_90);
        } else if (rotation == 8) {
            //ver
            image = rotate(image, ORIENTATION_270);
        } else if (rotation == 3) {
            //hori
            image = rotate(image, ORIENTATION_180);
        }

        if (image.getHeight() > image.getWidth() || image.getHeight() > image.getWidth()) {
            orient = "vertical";
        } else if (image.getWidth() > image.getHeight() || image.getWidth() > image.getHeight()) { //left
            orient = "horizontal";
        } else {
            orient = "vertical";
        }
    }

    @Override
    public void onError() {

    }

    @Override
    public void onFinish(String uniqueId) {

    }

    @Override
    public void onProgressUpdate(int percentage, String uniqueId) {

    }

    public void updateUserProfileImage(Uri profileImage) {
        String access_token = preferences.getValue(ImageCropPreviewActivity.this, GlobalVariables.STRPREF_ACCESS_TOKEN);
        String userJid = preferences.getValue(ImageCropPreviewActivity.this, GlobalVariables.STRPREF_USER_ID);
        String displayName = preferences.getValue(ImageCropPreviewActivity.this, GlobalVariables.STRPREF_USERNAME);

        ImageHelper imageHelper = new ImageHelper(this);
        Bitmap bitmap = imageHelper.getBitmapForProfile(profileImage);

        final byte[] imageByteFull = DirectoryHelper.getBytesFromBitmap100(bitmap);
        final byte[] imageByteThumb = DirectoryHelper.getBytesFromBitmap33(bitmap);

        RequestBody imageFull = RequestBody.create(APIGlobalVariables.JPEG, imageByteFull);
        RequestBody name = RequestBody.create(MediaType.parse("text/plain"),
                displayName);

        MultipartBody.Part filePart = MultipartBody.Part.createFormData("image", "name.jpeg", imageFull);

        UpdateUserProfileNameImage client = RetrofitAPIClient.getClient().create(UpdateUserProfileNameImage.class);
        retrofit2.Call<UserModel> call = client.editUser("Bearer " + access_token, filePart, name);

        call.enqueue(new retrofit2.Callback<UserModel>() {
            @Override
            public void onResponse(Call<UserModel> call, Response<UserModel> response) {
                if (!response.isSuccessful()) {
                    new Handler(Looper.getMainLooper()).post(() -> {
                        new MiscHelper().retroLogUnsuc(response, "UpdateUserProfileNameImage ", "JAY");
                        Toast.makeText(ImageCropPreviewActivity.this, R.string.onresponse_unsuccessful, Toast.LENGTH_SHORT).show();
                    });
                    return;
                }
                new Handler(Looper.getMainLooper())
                        .post(new Runnable() {
                            @Override
                            public void run() {
                                Profile.pb_profiel_image.setVisibility(View.GONE);
                                String uniqueId = UUID.randomUUID().toString();
                                pubsubNodeCall.PubsubSingle(userJid, userJid, uniqueId);

                                databaseHelper.updateIMGContactRoster(userJid, imageByteThumb, imageByteFull);
                                Profile.profilePicUpdateListener();
                                Toast.makeText(ImageCropPreviewActivity.this, "Profile Image Updated", Toast.LENGTH_SHORT).show();
                            }
                        });
            }

            @Override
            public void onFailure(Call<UserModel> call, Throwable t) {
                new Handler(Looper.getMainLooper())
                        .post(new Runnable() {
                            @Override
                            public void run() {
                                new MiscHelper().retroLogFailure(t, "UpdateUserProfileNameImage ", "JAY");
                                Toast.makeText(ImageCropPreviewActivity.this, R.string.onfailure, Toast.LENGTH_SHORT).show();
                            }
                        });
            }
        });
    }

    public void updateGroupProfileImage(Uri profileImage, String groupName, String roomJid) {
        String access_token = preferences.getValue(ImageCropPreviewActivity.this, GlobalVariables.STRPREF_ACCESS_TOKEN);
        final String Username = preferences.getValue(ImageCropPreviewActivity.this, GlobalVariables.STRPREF_USERNAME);
        final String updateMsg = Username + " " + getString(R.string.updated_grp_profile);
        String userJid = preferences.getValue(ImageCropPreviewActivity.this, GlobalVariables.STRPREF_USER_ID);

        RequestBody name = RequestBody.create(MediaType.parse("text/plain"), groupName);
        RequestBody roomid = RequestBody.create(MediaType.parse("text/plain"), roomJid);

        ImageHelper imageHelper = new ImageHelper(this);

        Bitmap bitmap = imageHelper.getBitmapForProfile(profileImage);
        final byte[] imageByte = DirectoryHelper.getBytesFromBitmap100(bitmap);
        final byte[] imageByte33 = DirectoryHelper.getBytesFromBitmap33(bitmap);

        RequestBody image = RequestBody.create(APIGlobalVariables.JPEG, imageByte);
        MultipartBody.Part filePart = MultipartBody.Part.createFormData("image", "name.jpeg", image);

        UpdateGroupProfileNameImage client = RetrofitAPIClient.getClient().create(UpdateGroupProfileNameImage.class);
        retrofit2.Call<GroupModel> call = client.editUser("Bearer " + access_token, filePart, name, roomid);
        call.enqueue(new retrofit2.Callback<GroupModel>() {
            @Override
            public void onResponse(Call<GroupModel> call, Response<GroupModel> response) {
                if (!response.isSuccessful()) {
                    new Handler(Looper.getMainLooper())
                            .post(new Runnable() {
                                @Override
                                public void run() {
                                    new MiscHelper().retroLogUnsuc(response, "updateGroupProfileImage ", "JAY");

                                    Toast.makeText(ImageCropPreviewActivity.this, R.string
                                            .onresponse_unsuccessful, Toast.LENGTH_SHORT).show();
                                }
                            });
                    return;
                }
                databaseHelper.selfUpdateGrpProfile(imageByte33, imageByte, groupName, roomJid);

                pubsubNodeCall.PubsubGroup(roomJid, userJid, UUID.randomUUID().toString(), roomJid, "");

                GroupChatDetail.groupProfileChangeListener(roomJid);
//                GroupChatDetail.groupName = null;
//                GroupChatDetail.jid = null;
                Toast.makeText(ImageCropPreviewActivity.this, "Profile Image Updated", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFailure(Call<GroupModel> call, Throwable t) {
                new Handler(Looper.getMainLooper())
                        .post(new Runnable() {
                            @Override
                            public void run() {
                                new MiscHelper().retroLogFailure(t, "updateGroupProfileImage ", "JAY");
                                Toast.makeText(ImageCropPreviewActivity.this, R.string
                                        .onfailure, Toast.LENGTH_SHORT).show();
                            }
                        });
            }
        });
    }

    @Override
    protected void onDestroy() {
        image = null;

        super.onDestroy();
    }

    public class SaveFile extends AsyncTask<String, Void, File> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog = ProgressDialog.show(ImageCropPreviewActivity.this, null,
                    getString(R.string.crop_image));
        }

        @Override
        protected File doInBackground(String... params) {
            return saveFile(params[0]);
        }

        private File saveFile(String address) {
            final String jid = getIntent().getStringExtra("jid");
            final String uniqueId = UUID.randomUUID().toString();
            final long currentDate = System.currentTimeMillis();
            String desiredName;

            if (Util.isExternalStorageWritable()) {
                // save to SD card
                // Get the directory for the user's public pictures directory.

                if (from.equals("chatmedia") || from.equals("chat") || from.equals("foodNearbyTabItem") || from.equals("external_image")) {
                    newFile = new File(GlobalVariables.IMAGES_PATH);
                } else {
                    //save the cropped or rotate image to PROFILE_CROPPED_PATH
                    newFile = new File(GlobalVariables.PROFILE_CROPPED_PATH);
                }

                try {
                    if (!from.equals("chatmedia") && !from.equals("chat")) {
                        //from cache
                        File oldFile = new File(address);
                        newFile = Util.copyFile(oldFile, newFile);
                        //just uri of newFile
                        selectedImage = Uri.fromFile(newFile);
                        if (from.equals("ProfilePic") || from.equals("UserProfile") || from.equals("CreateGroup") || from.equals("GroupDetail") || from.equals("NewGrpAppt")) {
                            desiredName = ImageHelper.getFileNameForSavingMedia(GlobalVariables.PROFILE_PATH, "PR");
                            profilePathFile = new File(GlobalVariables.PROFILE_PATH + desiredName);
                            try {
                                profilePathFile = Util.ryanCopyFile(new File(imageAddress), profilePathFile);
                                sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(profilePathFile)));
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }

                    } else {
                        if (from.equals("chat")) {
                            desiredName = ImageHelper.getFileNameForSavingMedia(GlobalVariables.IMAGES_PATH, "IMG");
                            profilePathFile = new File(GlobalVariables.IMAGES_PATH + desiredName);
                            try {
                                profilePathFile = Util.ryanCopyFile(new File(imageAddress), profilePathFile);
                                Soapp.getInstance().getApplicationContext().sendBroadcast(new Intent(Intent
                                        .ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(profilePathFile)));
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                        File oldFile = new File(address);
                        selectedImage = Uri.fromFile(oldFile);
                    }

                    switch (from) {
                        case "Contact":
                            ContactUs.selectedImage = selectedImage;
                            finish();
                            break;

                        default:
                            mImageView.startCrop(selectedImage, new CropCallback() {
                                @Override
                                public void onSuccess(Bitmap cropped) {
                                }

                                @Override
                                public void onError() {
                                }
                            }, new SaveCallback() {
                                @Override
                                public void onSuccess(Uri outputUri) {
                                    String compressedFilePath;
                                    File croppedFileWithWrongName;
                                    String nameForCroppedImg = ImageHelper.getFileNameForSavingMedia(GlobalVariables.PROFILE_CROPPED_PATH, "PR");
                                    File croppedFileWithCorrectName = new File(GlobalVariables.PROFILE_CROPPED_PATH, nameForCroppedImg);
                                    Intent cropImg = new Intent(ImageCropPreviewActivity.this, ImageNormalPreviewActivity.class);
                                    int cropResiltcode = 3000;

                                    switch (from) {
                                        case "chatmedia":

//                                            cropImg.putExtra("cropImg", outputUri.getPath());
//                                            setResult(cropResiltcode, cropImg);

                                            //trying to do all the things in chat holder, address is the choosen image path
                                            if (jid.length() == 12) {
                                                databaseHelper.ImageOutputDatabase(jid, uniqueId, currentDate, outputUri.getPath(), orient, "image", false);
                                            } else {
                                                databaseHelper.GroupImageOutputDatabase(jid, uniqueId, currentDate, outputUri.getPath(), orient, "image", false);
                                            }
                                            GalleryMainActivity.activity.finish();
                                            OpenGallery.activity.finish();
                                            ImageNormalPreviewActivity.activity.finish();
                                            if (progressDialog != null && progressDialog.isShowing()) {
                                                progressDialog.dismiss();
                                            }
                                            finish();
                                            break;

                                        case "chat":
//                                            cropImg.putExtra("cropImg", outputUri.getPath());
//                                            setResult(cropResiltcode, cropImg);

                                            if (jid.length() == 12) {
                                                databaseHelper.ImageOutputDatabase(jid, uniqueId, currentDate, outputUri.getPath(), orient, "image", true);
                                            } else {
                                                databaseHelper.GroupImageOutputDatabase(jid, uniqueId,
                                                        currentDate, outputUri.getPath(), orient, "image", true);
                                            }

                                            //trying to do all the things in chat holder, address is the choosen image path
                                            ImageNormalPreviewActivity.activity.finish();
                                            cameraHelper.finishCameraIntent();

                                            if (progressDialog != null && progressDialog.isShowing()) {
                                                progressDialog.dismiss();
                                            }
                                            finish();
                                            break;

                                        case "external_image":
                                            if (progressDialog != null && progressDialog.isShowing()) {
                                                progressDialog.dismiss();
                                            }
//
                                            if (jid.length() == 12) {
                                                databaseHelper.ImageOutputDatabase(jid, uniqueId, currentDate, outputUri.getPath(), orient, "image", false);
                                                Intent in = new Intent(ImageCropPreviewActivity.this, IndiChatLog.class);
                                                in.putExtra("jid", jid);
                                                startActivity(in);
                                            } else {
                                                databaseHelper.GroupImageOutputDatabase(jid, uniqueId, currentDate, outputUri.getPath(), orient, "image", false);
                                                Intent in = new Intent(ImageCropPreviewActivity.this, GroupChatLog.class);
                                                in.putExtra("jid", jid);
                                                startActivity(in);
                                            }
                                            ImageNormalPreviewActivity.activity.finish();
                                            finish();
                                            break;

                                        case "foodNearbyTabItem":
                                            if (resID != -1) {
                                                //upload image to server
                                                uploadPropicImageFile(selectedImage.getPath(), resID);
                                            }
                                            if (progressDialog != null && progressDialog.isShowing()) {
                                                if (newFile != null) {
                                                    newFile.delete();
                                                }
                                                progressDialog.dismiss();
                                            }
                                            cameraHelper.finishCameraIntent();
                                            finish();
                                            break;

                                        case "UserProfileMedia":
                                            croppedFileWithWrongName = new File(outputUri.getPath());
                                            compressedFilePath = ImageHelper.ryanCompressAndSaveImage(croppedFileWithCorrectName.getPath(), Uri.fromFile(croppedFileWithWrongName));
                                            UserProfile.selectedImage = Uri.fromFile(croppedFileWithCorrectName);
                                            GalleryMainActivity.activity.finish();
                                            OpenGallery.activity.finish();
                                            if (progressDialog != null && progressDialog.isShowing()) {
                                                if (newFile != null) {
                                                    newFile.delete();
                                                }
                                                progressDialog.dismiss();
                                            }
                                            finish();
                                            break;

                                        case "UserProfile":
                                            croppedFileWithWrongName = new File(outputUri.getPath());
                                            croppedFileWithWrongName.renameTo(croppedFileWithCorrectName);
                                            UserProfile.selectedImage = Uri.fromFile(croppedFileWithCorrectName);
                                            cameraHelper.finishCameraIntent();

                                            if (progressDialog != null && progressDialog.isShowing()) {
                                                progressDialog.dismiss();
                                            }
                                            finish();
                                            break;

                                        case "ProfilePicMedia":
                                            croppedFileWithWrongName = new File(outputUri.getPath());
                                            compressedFilePath = ImageHelper.ryanCompressAndSaveImage(croppedFileWithCorrectName.getPath(), Uri.fromFile(croppedFileWithWrongName));
                                            GalleryMainActivity.activity.finish();
                                            OpenGallery.activity.finish();
                                            if (progressDialog != null && progressDialog.isShowing()) {
                                                if (newFile != null) {
                                                    newFile.delete();
                                                }
                                                progressDialog.dismiss();
                                                if (SmackHelper.getXMPPConnection() != null && SmackHelper.getXMPPConnection().isAuthenticated()) {
                                                    new asyncUpdateProfile().execute(Uri.fromFile(croppedFileWithCorrectName));
                                                } else {
                                                    Toast.makeText(ImageCropPreviewActivity.this, R.string.xmpp_waiting_connection, Toast.LENGTH_SHORT).show();
                                                }
                                            }
                                            ProfilePic.context.finish();
                                            finish();
                                            break;

                                        case "ProfilePic":
                                            croppedFileWithWrongName = new File(outputUri.getPath());
                                            croppedFileWithWrongName.renameTo(croppedFileWithCorrectName);
                                            cameraHelper.finishCameraIntent();

                                            if (progressDialog != null && progressDialog.isShowing()) {
                                                progressDialog.dismiss();
                                                if (SmackHelper.getXMPPConnection() != null && SmackHelper.getXMPPConnection().isAuthenticated()) {
                                                    new asyncUpdateProfile().execute(Uri.fromFile(croppedFileWithCorrectName));
                                                } else {
                                                    Toast.makeText(ImageCropPreviewActivity.this, R.string.xmpp_waiting_connection, Toast.LENGTH_SHORT).show();
                                                }
                                            }
                                            ProfilePic.context.finish();
                                            finish();
                                            break;

                                        case "CreateGroup":
                                            croppedFileWithWrongName = new File(outputUri.getPath());
                                            croppedFileWithWrongName.renameTo(croppedFileWithCorrectName);
                                            NewCreateGroupController.bigImagePath = profilePathFile.getPath();
                                            NewCreateGroupController.selectedImage = Uri.fromFile(croppedFileWithCorrectName);
                                            cameraHelper.finishCameraIntent();

                                            if (progressDialog != null && progressDialog.isShowing()) {
                                                progressDialog.dismiss();
                                            }
                                            finish();
                                            break;

                                        case "createGroupMedia":
                                            croppedFileWithWrongName = new File(outputUri.getPath());
                                            compressedFilePath = ImageHelper.ryanCompressAndSaveImage(croppedFileWithCorrectName.getPath(), Uri.fromFile(croppedFileWithWrongName));
                                            NewCreateGroupController.selectedImage = Uri.fromFile(croppedFileWithCorrectName);
                                            ;
                                            GalleryMainActivity.activity.finish();
                                            OpenGallery.activity.finish();
                                            if (progressDialog != null && progressDialog.isShowing()) {
                                                if (newFile != null) {
                                                    newFile.delete();
                                                }
                                                progressDialog.dismiss();
                                            }
                                            finish();
                                            break;

                                        case "GroupDetailMedia":
                                            croppedFileWithWrongName = new File(outputUri.getPath());
                                            compressedFilePath = ImageHelper.ryanCompressAndSaveImage(croppedFileWithCorrectName.getPath(), Uri.fromFile(croppedFileWithWrongName));
                                            GalleryMainActivity.activity.finish();
                                            OpenGallery.activity.finish();
                                            if (progressDialog != null && progressDialog.isShowing()) {
                                                if (newFile != null) {
                                                    newFile.delete();
                                                }
                                                progressDialog.dismiss();
                                                if (SmackHelper.getXMPPConnection() != null && SmackHelper.getXMPPConnection().isAuthenticated()) {
                                                    new asyncUpdateGroupProfile().execute(Uri.fromFile(croppedFileWithCorrectName));
                                                } else {
                                                    Toast.makeText(ImageCropPreviewActivity.this, R.string.xmpp_waiting_connection, Toast.LENGTH_SHORT).show();
                                                }
                                            }
                                            GroupProfilePic.context.finish();
                                            finish();
                                            break;

                                        case "GroupDetail":
                                            croppedFileWithWrongName = new File(outputUri.getPath());
                                            croppedFileWithWrongName.renameTo(croppedFileWithCorrectName);
                                            cameraHelper.finishCameraIntent();

                                            if (progressDialog != null && progressDialog.isShowing()) {
                                                progressDialog.dismiss();
                                                if (SmackHelper.getXMPPConnection() != null && SmackHelper.getXMPPConnection().isAuthenticated()) {
                                                    new asyncUpdateGroupProfile().execute(Uri.fromFile(croppedFileWithCorrectName));
                                                } else {
                                                    Toast.makeText(ImageCropPreviewActivity.this, R.string.xmpp_waiting_connection, Toast.LENGTH_SHORT).show();
                                                }
                                            }
                                            GroupProfilePic.context.finish();
                                            finish();
                                            break;

                                        case "NewGrpAppt":
                                            croppedFileWithWrongName = new File(outputUri.getPath());
                                            croppedFileWithWrongName.renameTo(croppedFileWithCorrectName);
                                            NewGrpApptActivity.selectedImage = Uri.fromFile(croppedFileWithCorrectName);
                                            cameraHelper.finishCameraIntent();

                                            if (progressDialog != null && progressDialog.isShowing()) {
                                                progressDialog.dismiss();
                                            }
                                            finish();
                                            break;

                                        case "NewGrpApptMedia":
                                            croppedFileWithWrongName = new File(outputUri.getPath());
                                            compressedFilePath = ImageHelper.ryanCompressAndSaveImage(croppedFileWithCorrectName.getPath(), Uri.fromFile(croppedFileWithWrongName));
                                            NewGrpApptActivity.selectedImage = Uri.fromFile(croppedFileWithCorrectName);
                                            ;
                                            GalleryMainActivity.activity.finish();
                                            OpenGallery.activity.finish();
                                            if (progressDialog != null && progressDialog.isShowing()) {
                                                if (newFile != null) {
                                                    newFile.delete();
                                                }
                                                progressDialog.dismiss();
                                            }
                                            finish();
                                            break;

                                        default:
                                            finish();
                                            break;
                                    }

                                }

                                @Override
                                public void onError() {
                                }
                            });
                            break;
                    }

                } catch (NullPointerException | IOException ignore) {
                }
                return newFile;
            } else {
                return null;
            }
        }

        @Override
        protected void onPostExecute(File file) {

            super.onPostExecute(file);
        }
    }

    private class asyncUpdateProfile extends io.fabric.sdk.android.services.concurrency.AsyncTask<Uri, Integer, Void> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            //Display progressDialog before download starts
            Profile.pb_profiel_image.setVisibility(View.VISIBLE);
            updateProfileImageProgressDialog = ProgressDialog.show(ImageCropPreviewActivity.this, null, getString(R.string.updating_profile));
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
        }

        @Override
        protected Void doInBackground(Uri... params) {
            updateUserProfileImage(params[0]);

            return null;
        }
    }

    private class asyncUpdateGroupProfile extends io.fabric.sdk.android.services.concurrency.AsyncTask<Uri, Integer, Void> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            //Display progressDialog before download starts
//            updateProfileImageProgressDialog = ProgressDialog.show(ImageCropPreviewActivity.this, null, getString(R.string.updating_profile));
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
        }

        @Override
        protected Void doInBackground(Uri... params) {
            updateGroupProfileImage(params[0], GroupChatDetail.groupName, GroupChatDetail.jid);

            return null;
        }
    }

}
