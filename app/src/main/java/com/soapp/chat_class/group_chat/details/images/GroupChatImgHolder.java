package com.soapp.chat_class.group_chat.details.images;

import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.widget.ImageView;

import com.bumptech.glide.load.resource.bitmap.BitmapTransitionOptions;
import com.bumptech.glide.request.RequestOptions;
import com.soapp.R;
import com.soapp.global.GlideAPI.GlideApp;
import com.soapp.global.GlobalVariables;
import com.soapp.global.ImagePreviewSlide;
import com.soapp.sql.room.entity.Message;

import androidx.recyclerview.widget.RecyclerView;

/* Created by chang on 13/08/2017. */

public class GroupChatImgHolder extends RecyclerView.ViewHolder {
    //basics
    Context context;

    //elements
    private ImageView profile_img;

    GroupChatImgHolder(View itemView) {
        super(itemView);
        context = itemView.getContext();
        //set elements
        profile_img = itemView.findViewById(R.id.profile_img);
    }

    public void setData(final Message data) {
        String mediaPath = null;
        boolean isVideo = false;
        switch (data.getIsSender()) {
            case 20: //outgoing image
                mediaPath = GlobalVariables.IMAGES_SENT_PATH + "/" + data.getMsgInfoUrl();
                break;

            case 21: //incoming image
                mediaPath = GlobalVariables.IMAGES_PATH + data.getMsgInfoUrl();
                break;

            case 24:
                mediaPath = GlobalVariables.VIDEO_SENT_THUMBNAIL_PATH + "/" + data.getMsgInfoId() + ".jpg";
                isVideo = true;
                break;
            case 25:
                mediaPath = GlobalVariables.VIDEO_THUMBNAIL_PATH + "/" + data.getMsgInfoUrl() + ".jpg";
                isVideo = true;
                break;
        }


        if (mediaPath != null) {
            GlideApp.with(itemView)
                    .asBitmap()
                    .load(mediaPath)
                    .placeholder(R.drawable.default_propic_small_round)
                    .apply(RequestOptions.circleCropTransform())
                    .transition(BitmapTransitionOptions.withCrossFade())
                    .into(profile_img);
        }

//        if (mediaPath != null) {
//            GlideApp.with(itemView)
//                    .asBitmap()
//                    .load(mediaPath)
//                    .placeholder(R.drawable.default_propic_small_round)
//                    .apply(RequestOptions.circleCropTransform())
//                    .transition(BitmapTransitionOptions.withCrossFade())
//                    .into(profile_img);
//        }

        //final media path for slide previewing
        profile_img.setOnClickListener(view -> {

            Intent imageIntent = new Intent(context, ImagePreviewSlide.class);
            imageIntent.putExtra("position", getLayoutPosition());
            ImagePreviewSlide.list = GroupChatImgAdapter.grpChatImgList;
            context.startActivity(imageIntent);

        });

    }
}
