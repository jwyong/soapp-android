package com.soapp.global;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;

import com.bumptech.glide.RequestBuilder;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.target.BitmapImageViewTarget;
import com.soapp.R;
import com.soapp.global.GlideAPI.GlideApp;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public class ImageFullView extends AppCompatActivity {

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i("diao", getClass().getSimpleName());
        setContentView(R.layout.image_single);

        TouchImageView img_single = findViewById(R.id.fullscreen_img);

        String uriStringImg = getIntent().getStringExtra("imageUri");

        if (getIntent().getByteArrayExtra("byteArray") != null) {
            byte[] byteImg = getIntent().getByteArrayExtra("byteArray");
            byte[] byteImgThumb = getIntent().getByteArrayExtra("byteArrayThumb");

            //new
            RequestBuilder<Bitmap> thumbnailRequest =
                    GlideApp.with(ImageFullView.this)
                            .asBitmap()
                            .load(byteImgThumb);

            GlideApp.with(ImageFullView.this)
                    .asBitmap()
                    .load(byteImg)
                    .thumbnail(thumbnailRequest)
                    .into(new BitmapImageViewTarget(img_single) {
                        @Override
                        public void onLoadStarted(@Nullable Drawable placeholder) {
                            super.onLoadStarted(placeholder);
                            setThumb(byteImgThumb, img_single);
                        }
                    });

        } else {
            GlideApp.with(this)
                    .load(uriStringImg)
                    .skipMemoryCache(true)
                    .diskCacheStrategy(DiskCacheStrategy.NONE)
                    .placeholder(R.drawable.default_img_full)
                    .into(img_single);
        }
    }

    public void setThumb(byte[] byteImgThumb, TouchImageView img_single) {
        if (byteImgThumb != null) {
            Bitmap bitmap = BitmapFactory.decodeByteArray(byteImgThumb, 0, byteImgThumb.length);
            img_single.setImageBitmap(bitmap);
        }
    }

}
