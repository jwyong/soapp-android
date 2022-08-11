package com.soapp.new_chat_schedule.group;

import android.content.Context;
import android.view.View;
import android.widget.CheckBox;
import android.widget.ImageView;

import com.soapp.R;
import com.soapp.global.ImageLoadHelper;
import com.soapp.sql.DatabaseHelper;
import com.soapp.sql.room.entity.ContactRoster;

import androidx.recyclerview.widget.RecyclerView;
import io.github.rockerhieu.emojicon.EmojiconTextView;

/* Created by chang on 11/08/2017. */

class NewGrpChatHolder extends RecyclerView.ViewHolder {
    //basics
    private Context context;
    private DatabaseHelper databaseHelper = DatabaseHelper.getInstance();
    private ImageLoadHelper imageLoadHelper = new ImageLoadHelper();

    //elements
    private ImageView newchatprofileimage;
    private EmojiconTextView newchatname;
    private CheckBox g_checkbox;

    NewGrpChatHolder(View itemView) {
        super(itemView);
        context = itemView.getContext();

        //set elements
        newchatprofileimage = itemView.findViewById(R.id.newchatprofileimage);
        newchatname = itemView.findViewById(R.id.newchatname);
        g_checkbox = itemView.findViewById(R.id.g_checkbox);
    }

    public void setData(final ContactRoster data) {
        //set final jid
        final String jid = data.getContactJid();

        //set member profile img
        imageLoadHelper.setImgByte(newchatprofileimage, data.getProfilephoto(), R.drawable.in_propic_circle_150px);

//        if (data.getProfilephoto() != null) {
//            GlideApp.with(itemView)
//                    .asBitmap()
//                    .load(data.getProfilephoto())
//                    .placeholder(R.drawable.in_propic_circle_150px)
//                    .apply(RequestOptions.circleCropTransform())
////                    .transition(BitmapTransitionOptions.withCrossFade())
//                    .thumbnail(0.5f)
//                    .encodeQuality(50)
//                    .override(180, 180)
////                    .into(newchatprofileimage);
//                    .into(new SimpleTarget<Bitmap>() {
//                        @Override
//                        public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
//                            newchatprofileimage.setImageBitmap(resource);
//                        }
//                    });
//        }

        //set member displayname
        newchatname.setText(data.getPhonename());

        //set checkbox based on "selected"
        if (data.getSelected() != null && data.getSelected() == 1) { //selected, check
            g_checkbox.setChecked(true);
        } else { //not selected, uncheck
            g_checkbox.setChecked(false);
        }

        //set onclick listener to whole itemview for checking
        itemView.setOnClickListener(v -> {
            int selected;
            if (g_checkbox.isChecked()) { //checked, set 0 to sqlite
                selected = 0;
            } else { //unchecked, set 1 to sqlite
                selected = 1;
            }
            databaseHelper.updateCRSelected(jid, selected);
        });
    }
}
