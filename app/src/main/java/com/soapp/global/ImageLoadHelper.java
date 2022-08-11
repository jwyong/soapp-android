package com.soapp.global;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.View;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.soapp.global.GlideAPI.GlideApp;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

/**
 * Created by rlwt on 6/8/18.
 */

public class ImageLoadHelper {
    private Bitmap imgBitmap;

    public void setImgByte(ImageView imageView, byte[] imgByte, int defaultDrawable) {
        //use glide to load img
//        if (imgByte != null && imgByte.length > 10) {
//            GlideApp.with(imageView)
//                    .asBitmap()
//                    .load(imgByte)
//                    .placeholder(defaultDrawable)
//                    .skipMemoryCache(false)
//                    .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
//                    .thumbnail(0.5f)
//                    .encodeQuality(50)
//                    .transition(BitmapTransitionOptions.withCrossFade())
//                    .apply(RequestOptions.circleCropTransform())
//                    .override(ChatTab.profileSize, ChatTab.profileSize)
//                    .into(imageView);
////                    .into(new SimpleTarget<Drawable>() {
////                        @Override
////                        public void onResourceReady(@NonNull Drawable resource, @Nullable Transition<? super Drawable> transition) {
////                            imageView.setImageDrawable(resource);
////                        }
////                    });
//        }

        //testing RX set img resource
        if (imgByte != null && imgByte.length > 10) {
            Observable.just(imgByte)
                    .observeOn(Schedulers.io())
                    .subscribeOn(Schedulers.io())
                    .doOnNext(bytes -> {
                        imgBitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
//                        profileImgBitmapRound = RoundedBitmapDrawableFactory.create(context.getResources(), imgBitmap);
//                        profileImgBitmapRound.setCircular(true);
                    })
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribeOn(AndroidSchedulers.mainThread())
                    .doOnComplete(() -> {
                        imageView.setImageBitmap(imgBitmap);
//                            chat_list_profileImage.startAnimation(fadeInAnimation);
                    })
                    .subscribe();
        } else {
            imageView.setImageResource(defaultDrawable);
        }
    }

    public void setImgPath(View itemView, ImageView imageView, String imgPath, int defaultDrawable) {
//        if (imgPath != null) {
//            Observable.just(imgPath)
//                    .observeOn(Schedulers.io())
//                    .subscribeOn(Schedulers.io())
//                    .doOnNext(bytes -> {
//                        imgBitmap = BitmapFactory.decodeFile(imgPath);
//
//                        if (imgBitmap.getWidth() >= imgBitmap.getHeight()) {
//
//                            imgBitmap = Bitmap.createBitmap(
//                                    imgBitmap,
//                                    imgBitmap.getWidth() / 2 - imgBitmap.getHeight() / 2,
//                                    0,
//                                    imgBitmap.getHeight(),
//                                    imgBitmap.getHeight()
//                            );
//
//                        } else {
//
//                            imgBitmap = Bitmap.createBitmap(
//                                    imgBitmap,
//                                    0,
//                                    imgBitmap.getHeight() / 2 - imgBitmap.getWidth() / 2,
//                                    imgBitmap.getWidth(),
//                                    imgBitmap.getWidth()
//                            );
//                        }
//                    })
//                    .observeOn(AndroidSchedulers.mainThread())
//                    .subscribeOn(AndroidSchedulers.mainThread())
//                    .doOnComplete(() -> {
//                        imageView.setImageBitmap(imgBitmap);
//                    })
//                    .subscribe();
//        } else {
//            imageView.setImageResource(defaultDrawable);
//        }

        if (imgPath != null) {
//            final ImageView myImageView;
//            if (recycled == null) {
//                myImageView = (ImageView) inflater.inflate(R.layout.my_image_view, container, false);
//            } else {
//                myImageView = (ImageView) recycled;
//            }
//
//            String url = myUrls.get(position);

//            Glide.with(itemView)
//                    .load(imgPath)
//                    .centerCrop()
//                    .placeholder(defaultDrawable)
//                    .into(imageView);


            Glide.with(imageView)
                    .asBitmap()
                    .load(imgPath)
                    .placeholder(defaultDrawable)
                    .skipMemoryCache(false)
                    .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                    .thumbnail(0.5f)
                    .encodeQuality(50)
                    .into(imageView);
//                    .into(new SimpleTarget<Drawable>() {
//                @Override
//                public void onResourceReady(@NonNull Drawable resource, @Nullable Transition<? super Drawable> transition) {
//                    imageView.setImageDrawable(resource);
//                }
//            });
        }
    }
}
