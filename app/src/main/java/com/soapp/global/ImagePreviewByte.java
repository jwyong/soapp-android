package com.soapp.global;

import android.graphics.Bitmap;
import android.graphics.Point;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;
import com.soapp.R;
import com.soapp.base.BaseActivity;
import com.soapp.global.GlideAPI.GlideApp;
import com.soapp.setup.Soapp;
import com.soapp.sql.DatabaseHelper;
import com.soapp.sql.room.entity.ContactRoster;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;

/* Created by Soapp on 16/09/2017. */

public class ImagePreviewByte extends BaseActivity implements View.OnTouchListener {

    public static ImageView fullscreen_img;
    public static ProgressBar img_loading;
    public SimpleTarget targetUri;
    Button sharebutton;
    Point p;
    DatabaseHelper databaseHelper = DatabaseHelper.getInstance();
    private DisplayMetrics displayMetrics = new DisplayMetrics();

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.image_single);

        displayMetrics = new DisplayMetrics();
        displayMetrics = this.getResources().getDisplayMetrics();

        fullscreen_img = findViewById(R.id.fullscreen_img);
//        fullscreen_img.setOnTouchListener(ImagePreviewByte.this);
        targetUri = new SimpleTarget<Bitmap>() {
            @Override
            public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                fullscreen_img.setImageBitmap(resource);
                fullscreen_img.setVisibility(View.VISIBLE);
            }
        };

        setupToolbar();

        //share button for future sharing profile image to chat
//        sharebutton = findViewById(R.id.sharebutton);

        mToolbar = findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);

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
        String jid = getIntent().getStringExtra("jid");
        String imgURL = getIntent().getStringExtra("imgURL");

        if (getIntent().getStringExtra("jid").equals(jid)) {
            //Receive JID and from
            String jid1 = getIntent().getStringExtra("jid");
            //try download full profile img if got no full img bytes in sqlite
//            downloadProfileImg(jid1);
            liveDataforProfileImageAndGroupName(jid1);
        } else if (getIntent().getStringExtra("imgURL").equals(imgURL)) {
            String imgURL1 = getIntent().getStringExtra("imgURL");

            justImg(imgURL1);
        }
    }

    private void justImg(String imgURL) {


        GlideApp.with(this)
                .load(imgURL)
                .dontTransform()
                .into(targetUri);

    }

    @Override
    public boolean onTouch(View view, MotionEvent motionEvent) {
        switch (view.getId()) {

            case R.id.profile_pic:
                boolean showing = getSupportActionBar().isShowing();
                if (showing) {
                    getSupportActionBar().hide();

                } else {
                    getSupportActionBar().show();

                }

                break;

            default:
                break;
        }
        return false;
    }

    public void liveDataforProfileImageAndGroupName(String jid){
        LiveData<ContactRoster> live_grpProfileList = Soapp.getDatabase().selectQuery().live_get_cr_profile(jid);
        live_grpProfileList.observe(this, profile->{
            byte[] new_profile_full = profile.getProfilefull();
            byte[] new_profile_thumb = profile.getProfilephoto();
            String photo_url = profile.getPhotourl();

            if(new_profile_full == null){
                if(photo_url != null) {
                    new ChatHelper().downloadFromUrl(photo_url, jid, null);
                }else{
                    new ChatHelper().resourceRetro(ImagePreviewByte.this, jid);
                }
            }

            GlideApp.with(this)
                    .asBitmap()
                    .load(new_profile_full)
                    .thumbnail(GlideApp.with(ImagePreviewByte.this)
                            .asBitmap()
                            .load(new_profile_thumb))
                    .into(new SimpleTarget<Bitmap>() {
                        @Override
                        public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                            fullscreen_img.setImageBitmap(resource);
                        }
                    });
        });
    }
}
