package com.soapp.new_chat_schedule.group.create_grp;

import android.content.Context;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.soapp.R;
import com.soapp.global.ImageLoadHelper;
import com.soapp.sql.room.entity.ContactRoster;

import androidx.recyclerview.widget.RecyclerView;

/* Created by chang on 11/08/2017. */

public class NewCreateGroupHolder extends RecyclerView.ViewHolder {
    //basics
    private Context context;

    //elements
    private TextView membername;
    private ImageView memberphoto;

    private ImageLoadHelper imageLoadHelper = new ImageLoadHelper();

    NewCreateGroupHolder(View itemView) {
        super(itemView);

        //set elements
        membername = itemView.findViewById(R.id.newchatname);
        memberphoto = itemView.findViewById(R.id.newchatprofileimage);
    }

    public void setData(final ContactRoster data) {
        //set member profile img
        imageLoadHelper.setImgByte(memberphoto, data.getProfilephoto(), R.drawable.in_propic_circle_150px);

//        if (data.getProfilephoto() != null) {
//            GlideApp.with(itemView)
//                    .asBitmap()
//                    .load(data.getProfilephoto())
//                    .placeholder(R.drawable.in_propic_circle_150px)
//                    .apply(RequestOptions.circleCropTransform())
//                    .transition(BitmapTransitionOptions.withCrossFade())
//                    .thumbnail(0.5f)
//                    .encodeQuality(50)
//                    .override(180, 180)
//                    .into(memberphoto);
//        }

        //set member displayname
        membername.setText(data.getPhonename());
    }
}