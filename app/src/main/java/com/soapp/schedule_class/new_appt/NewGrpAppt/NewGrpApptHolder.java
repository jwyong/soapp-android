package com.soapp.schedule_class.new_appt.NewGrpAppt;

import android.view.View;
import android.widget.ImageView;

import com.bumptech.glide.load.resource.bitmap.BitmapTransitionOptions;
import com.bumptech.glide.request.RequestOptions;
import com.soapp.R;
import com.soapp.global.GlideAPI.GlideApp;
import com.soapp.sql.room.entity.ContactRoster;

import androidx.recyclerview.widget.RecyclerView;

/* Created by Jayyong on 30/04/2018. */

public class NewGrpApptHolder extends RecyclerView.ViewHolder {
    //elements
    public ImageView profile_img;

    NewGrpApptHolder(View itemView) {
        super(itemView);
        //set elements
        profile_img = itemView.findViewById(R.id.profile_img);
    }

    public void setData(final ContactRoster data) {
        GlideApp.with(itemView)
                .asBitmap()
                .load(data.getProfilephoto())
                .placeholder(R.drawable.in_propic_circle_150px)
                .apply(RequestOptions.circleCropTransform())
                .transition(BitmapTransitionOptions.withCrossFade())
                .into(profile_img);
    }
}