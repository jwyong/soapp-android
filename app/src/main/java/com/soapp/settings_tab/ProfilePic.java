package com.soapp.settings_tab;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;

import com.soapp.R;
import com.soapp.base.BaseActivity;
import com.soapp.camera.ImageCropPreviewActivity;
import com.soapp.global.CameraHelper;
import com.soapp.global.GlideAPI.GlideApp;
import com.soapp.global.GlobalVariables;
import com.soapp.global.Preferences;
import com.soapp.global.TouchImageView;
import com.soapp.global.UIHelper;
import com.soapp.sql.DatabaseHelper;

/* Created by jonathanhew on 03/08/2017. */

public class ProfilePic extends BaseActivity implements View.OnClickListener, View.OnTouchListener {
    private static final int RESULT_CAMERA_STORAGE = 1;
    private static final int ACCESS_GALLERY_PROFILE = 2;
    public static TouchImageView profile_pic;
    public static Activity context;
    private ImageButton editButton;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settingstab_profile_image);
        context = this;

        profile_pic = findViewById(R.id.profile_pic);
        profile_pic.setOnTouchListener(ProfilePic.this);

        setupToolbar();
        editButton = findViewById(R.id.editbutton);
        editButton.setOnClickListener(ProfilePic.this);

        mToolbar = findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);

        //glide profile image from db
        String userJid = Preferences.getInstance().getValue(ProfilePic.this, GlobalVariables.STRPREF_USER_ID);
        byte[] profileImgByte = DatabaseHelper.getInstance().getImageBytesFullFromContactRoster(userJid);

        if (profileImgByte != null) {
            GlideApp.with(this)
                    .load(profileImgByte)
                    .placeholder(R.drawable.default_img_full)
                    .into(profile_pic);
        }

        //Show or hide action bar depending on whether its fullscreen or not.
        View decorView = getWindow().getDecorView();
        decorView.setOnSystemUiVisibilityChangeListener(new View.OnSystemUiVisibilityChangeListener() {
            @Override
            public void onSystemUiVisibilityChange(int visibility) {
                // Note that system bars will only be "visible" if none of the
                // LOW_PROFILE, HIDE_NAVIGATION, or FULLSCREEN flags are set.
                if ((visibility & View.SYSTEM_UI_FLAG_FULLSCREEN) == 0) {
                    // Not full Screen
                    getSupportActionBar().show();

                } else {
                    getSupportActionBar().hide();
                }
            }
        });
    }

    //button on click, if click on image then fullscreen, if click edit run popup
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.editbutton:
                new UIHelper().showCameraGalleryPopup(this, "ProfilePic", "ProfilePicMedia");
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

                    cameraintent.putExtra("from", "ProfilePic");

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
                    Intent cameraintent = new Intent(ProfilePic.this, ImageCropPreviewActivity.class);
                    cameraintent.putExtra("from", "ProfilePicMedia");
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
}