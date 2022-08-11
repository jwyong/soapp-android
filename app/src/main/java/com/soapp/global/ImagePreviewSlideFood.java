package com.soapp.global;

import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.View;

import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;
import com.soapp.R;
import com.soapp.base.BaseActivity;
import com.soapp.global.GlideAPI.GlideApp;
import com.soapp.sql.DatabaseHelper;
import com.soapp.sql.room.entity.Message;

import java.io.File;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;


/* Created by jonathanhew on 27/09/2017. */


//ImagePreviewSlide is for previewing images under ChatImageActivity

public class ImagePreviewSlideFood extends BaseActivity {

    public static SimpleTarget targetUri;
    public static List<Message> list;
    public static String[] urlList;
    public static int uriPosition, urlPosition;
    float x1, x2, y2, y1;
    String initUrl;
    private TouchImageViewFood fullscreen_img;
    private DatabaseHelper databaseHelper = DatabaseHelper.getInstance();
    private DisplayMetrics displayMetrics = new DisplayMetrics();

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.img_previewfood);

        displayMetrics = new DisplayMetrics();
        displayMetrics = this.getResources().getDisplayMetrics();

        //getting indiScheList of image from databaseHelper

        fullscreen_img = findViewById(R.id.fullscreen_img);

        targetUri = new SimpleTarget<BitmapDrawable>() {
            @Override
            public void onResourceReady(@NonNull BitmapDrawable resource, @Nullable Transition<? super BitmapDrawable> transition) {
                fullscreen_img.setImageDrawable(resource);
            }
        };

        setupToolbar();

        mToolbar = findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);

        //recieve URI
        if (getIntent().hasExtra("imageUri")) {
            if (savedInstanceState == null) {
                if (uriPosition > 0) {
                    urlList = null;
                }
                Uri imageUri = Uri.fromFile(new File(getIntent().getStringExtra("imageUri")));
                uriPosition = getIntent().getIntExtra("position", 0);

                GlideApp.with(this)
                        .load(imageUri)
                        .placeholder(R.drawable.default_img_full)
                        .into(targetUri);
            } else { //when rotate
                if (list == null) {
                    Uri imageUri = Uri.fromFile(new File(getIntent().getStringExtra("imageUri")));
                    uriPosition = getIntent().getIntExtra("position", 0);
                    GlideApp.with(this)
                            .load(imageUri)
                            .placeholder(R.drawable.default_img_full)
                            .into(targetUri);
                } else {
                    String path = null;
                    if (list.get(uriPosition).getIsSender() == 21) {
                        path = GlobalVariables.IMAGES_PATH + list.get(uriPosition).getMsgInfoUrl();
                    } else {
                        path = GlobalVariables.IMAGES_SENT_PATH + "/" + list.get(uriPosition).getMsgInfoUrl();
                    }
                    GlideApp.with(this)
                            .load(path)
                            .placeholder(R.drawable.default_img_full)
                            .into(targetUri);
                }
            }
        }

        if (getIntent().hasExtra("url")) {
            if (savedInstanceState == null) {
                if (urlPosition > 0) {
                    list = null;
                }
                urlPosition = getIntent().getIntExtra("position", 0);
                String url = getIntent().getStringExtra("url");

                if (url == null) {
                    initUrl = "";
                } else {
                    initUrl = url.substring(0, url.indexOf("Thumb")) + url.substring(url
                            .indexOf("Thumb") + 5);
                }

            } else { //when rotate
                if (urlList == null) {
                    urlPosition = getIntent().getIntExtra("position", 0);
                    String url = getIntent().getStringExtra("url");
                    initUrl = url.substring(0, url.indexOf("Thumb")) + url.substring(url.indexOf
                            ("Thumb") + 5);
                } else {
                    initUrl = urlList[urlPosition].substring(0, urlList[urlPosition].indexOf("Thumb")
                    ) + urlList[urlPosition].substring(urlList[urlPosition].indexOf("Thumb") + 5);
                }
            }

            GlideApp.with(this)
                    .load(initUrl)
                    .placeholder(R.drawable.default_img_full)
                    .into(targetUri);
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

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        list = null;
        urlList = null;
        fullscreen_img = null;
        targetUri = null;
        databaseHelper = null;
    }
}
