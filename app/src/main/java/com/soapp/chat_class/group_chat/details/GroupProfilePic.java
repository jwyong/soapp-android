package com.soapp.chat_class.group_chat.details;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.net.Uri;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;
import com.soapp.R;
import com.soapp.base.BaseActivity;
import com.soapp.camera.ImageCropPreviewActivity;
import com.soapp.global.CameraHelper;
import com.soapp.global.ChatHelper;
import com.soapp.global.GlideAPI.GlideApp;
import com.soapp.global.TouchImageView;
import com.soapp.global.UIHelper;
import com.soapp.setup.Soapp;
import com.soapp.sql.DatabaseHelper;
import com.soapp.sql.room.entity.ContactRoster;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;

/* Created by jonathanhew on 03/08/2017. */

public class GroupProfilePic extends BaseActivity implements View.OnClickListener, View.OnTouchListener {

    private static final int RESULT_CAMERA_STORAGE = 1;
    private static final int ACCESS_GALLERY_PROFILE = 2;
    public static TouchImageView profile_pic;

    public static ProgressBar img_loading;
    public static Activity context;
    public static Uri selectedImage;
    private String jid, group_name;
    DatabaseHelper databaseHelper = DatabaseHelper.getInstance();
    private boolean isUploaded = false;
    private ImageButton editButton;
    private Point p;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.chattab_profile_grp);
        context = this;

        jid = getIntent().getStringExtra("jid");

        profile_pic = findViewById(R.id.profile_pic);
        profile_pic.setOnTouchListener(this);


        setupToolbar();
        editButton = findViewById(R.id.editbutton);
        editButton.setOnClickListener(this);

        mToolbar = findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);

        if (selectedImage != null) { //no selectedImage, show full image from byte
            //got selectedimage, glide in
            GlideApp.with(this)
                    .asBitmap()
                    .load(selectedImage)
                    .diskCacheStrategy(DiskCacheStrategy.NONE)
                    .skipMemoryCache(true)
                    .into(new SimpleTarget<Bitmap>() {
                        @Override
                        public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                            profile_pic.setImageBitmap(resource);
                        }
                    });
        }

        //Show or hide action bar depending on whether its fullscreen or not.
        View decorView = getWindow().getDecorView();
        decorView.setOnSystemUiVisibilityChangeListener(visibility -> {
            // Note that system bars will only be "visible" if none of the
            // LOW_PROFILE, HIDE_NAVIGATION, or FULLSCREEN flags are set.
            if ((visibility & View.SYSTEM_UI_FLAG_FULLSCREEN) == 0) {
                // Not full Screen
                getSupportActionBar().show();

            } else {
                getSupportActionBar().hide();
            }
        });

        liveDataforProfileImageAndGroupName();
    }

    //button on click, if click on image then fullscreen, if click edit run popup
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.editbutton:
                new UIHelper().showCameraGalleryPopup(this, "GroupDetail", "GroupDetailMedia");
                break;

            default:
                break;
        }
    }

    //permissions request in case that permissions not granted. Permissions needed for storage
    //and also for camera itself.
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case RESULT_CAMERA_STORAGE:
                boolean allow1 = true;
                for (int i = 0, len = permissions.length; i < len; i++) {
                    if (grantResults[i] == PackageManager.PERMISSION_DENIED) {
                        allow1 = false;
                    }
                }

                if (allow1) {
                    Intent cameraintent = new CameraHelper().startCameraIntent(this);

                    cameraintent.putExtra("from", "GroupDetail");

                    startActivity(cameraintent);
                } else {
                    Toast.makeText(this, R.string.need_camera, Toast.LENGTH_SHORT).show();
                }
                break;

            case ACCESS_GALLERY_PROFILE:
                boolean allow2 = true;
                for (int i = 0, len = permissions.length; i < len; i++) {
                    if (grantResults[i] == PackageManager.PERMISSION_DENIED) {
                        allow2 = false;
                    }
                }
                if (allow2) {
                    Intent cameraintent = new Intent(GroupProfilePic.this, ImageCropPreviewActivity.class);
                    cameraintent.putExtra("from", "GroupDetailMedia");
                    startActivity(cameraintent);
                } else {
                    Toast.makeText(this, R.string.need_gallery, Toast.LENGTH_SHORT).show();
                }
                break;

            default:
                break;
        }
    }

    @Override
    public boolean onTouch(View view, MotionEvent motionEvent) {
        int eventaction = motionEvent.getAction();
        switch (view.getId()) {

            case R.id.profile_pic:

                if (eventaction == MotionEvent.ACTION_UP) {
                    boolean showing = getSupportActionBar().isShowing();
                    if (showing) {
                        getSupportActionBar().hide();

                    } else {
                        getSupportActionBar().show();

                    }
                }
                break;

            default:
                break;
        }
        return false;
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    public void liveDataforProfileImageAndGroupName(){
        LiveData<ContactRoster> live_grpProfileList = Soapp.getDatabase().selectQuery().live_get_cr_profile(jid);
        live_grpProfileList.observe(this, profile->{
            byte[] new_profile_full = profile.getProfilefull();
            byte[] new_profile_thumb = profile.getProfilephoto();
            String photo_url = profile.getPhotourl();

            if(new_profile_full == null){
                if(photo_url != null) {
                    new ChatHelper().downloadFromUrl(photo_url, jid, null);
                }else{
                    new ChatHelper().resourceRetro(GroupProfilePic.this, jid);
                }
            }

            GlideApp.with(this)
                    .asBitmap()
                    .load(new_profile_full)
                    .thumbnail(GlideApp
                            .with(GroupProfilePic.this)
                            .asBitmap()
                            .load(new_profile_thumb))
                    .into(new SimpleTarget<Bitmap>() {
                        @Override
                        public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                            profile_pic.setImageBitmap(resource);
                        }
                    });
        });
    }
}

