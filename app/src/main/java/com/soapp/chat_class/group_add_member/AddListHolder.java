package com.soapp.chat_class.group_add_member;

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

/* Created by chang on 27/08/2017. */

public class AddListHolder extends RecyclerView.ViewHolder {
    //basics
    private Context context;
    private DatabaseHelper databaseHelper = DatabaseHelper.getInstance();
    private ImageLoadHelper imageLoadHelper = new ImageLoadHelper();

    //elements
    private ImageView newchatprofileimage;
    private EmojiconTextView newchatname;
    private CheckBox g_checkbox;

    AddListHolder(View itemView) {
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
//                    .override(AddListController.profileSize, AddListController.profileSize)
////                    .into(newchatprofileimage);
//                    .into(new SimpleTarget<Bitmap>() {
//                        @Override
//                        public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
//                            newchatprofileimage.setImageBitmap(resource);
//                        }
//                    });
//        }

        //set member displayname
        String displayName = data.getPhonename();

        if (displayName == null) {
            displayName = "";
        }
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
                g_checkbox.setChecked(false);
                selected = 0;
            } else { //unchecked, set 1 to sqlite
                g_checkbox.setChecked(true);
                selected = 1;
            }
            databaseHelper.updateCRSelected(jid, selected);
        });
    }

//    private TextView name, number, jidtxt;
//    private ImageView profileimage;
//    private CheckBox checkBox;
//    private DatabaseHelper databaseHelper = DatabaseHelper.getInstance(Soapp.getInstance().getApplicationContext());
//
//    AddListHolder(View itemView) {
//        super(itemView);
//
//        name = (TextView) itemView.findViewById(R.id.newchatname);
//        profileimage = itemView.findViewById(R.id.newchatprofileimage);
//        number = (TextView) itemView.findViewById(R.id.newchatnumber);
//        jidtxt = (TextView) itemView.findViewById(R.id.newchatjid);
//        checkBox = (CheckBox) itemView.findViewById(R.id.g_checkbox);
//
//        itemView.setOnClickListener(this);
//    }
//
//    public void setData(Cursor c) {
//        jidtxt.setText(c.getString(1));
//        String phoneName = c.getString(2);
//
//        if (phoneName == null || phoneName.equals("")) {
//            phoneName = c.getString(4) + " " + c.getString(3);
//        }
//        name.setText(phoneName);
//        number.setText(c.getString(3));
//
//        if (c.getBlob(5) != null) {
//            GlideApp.with(itemView)
//                    .asBitmap()
//                    .load(c.getBlob(5))
//                    .placeholder(R.drawable.default_propic_small_round)
//                    .apply(RequestOptions.circleCropTransform())
//                    .thumbnail(0.5f)
//                    .encodeQuality(50)
//                    .override(180, 180)
//                    .into(new SimpleTarget<Bitmap>() {
//                        @Override
//                        public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
//                            profileimage.setImageBitmap(resource);
//                        }
//                    });
//        } else {
//
//            GlideApp.with(itemView)
//                    .load(R.drawable.default_propic_small_round)
//                    .placeholder(R.drawable.default_propic_small_round)
//                    .into(profileimage);
//        }
//
//        if (c.getInt(7) != 1) {
//            checkBox.setChecked(false);
//        } else {
//            checkBox.setChecked(true);
//        }
//    }
//
//    @Override
//    public void onClick(View v) {
//        if (v != null) {
//            ContentValues cv = new ContentValues();
//
//            if (checkBox.isChecked()) {
//                cv.put("Selected", 0);
//            } else {
//                cv.put("Selected", 1);
//            }
//
//            if (AddListController.isSearching) {
//                new CursorLoaderProvider().update(CursorLoaderProvider.UPDATE_NEWGRP_SEARCH_CR_URI, cv, "ContactJid = ?",
//                        new String[]{jidtxt.getText().toString()});
//            } else {
//                new CursorLoaderProvider().update(CursorLoaderProvider.UPDATE_NEWGRP_CR_URI, cv, "ContactJid = ?",
//                        new String[]{jidtxt.getText().toString()});
//            }
//
//            //get number of checked rows in new grp chat
//            if (databaseHelper.getCRSelectedCount() == 0) { //no rows, don't allow create btn
//                AddListController.next_btn.setEnabled(false);
//            } else {
//                AddListController.next_btn.setEnabled(true);
//            }
//        }
//    }

}
