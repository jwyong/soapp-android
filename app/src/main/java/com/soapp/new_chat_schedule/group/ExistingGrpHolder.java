package com.soapp.new_chat_schedule.group;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.widget.ImageView;

import com.soapp.R;
import com.soapp.global.ImageLoadHelper;
import com.soapp.schedule_class.new_appt.NewIndiExistApptActivity;
import com.soapp.sql.room.entity.ContactRoster;
import com.soapp.sql.room.joiners.ChatTabList;

import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import io.github.rockerhieu.emojicon.EmojiconTextView;

/* Created by chang on 28/09/2017. */

class ExistingGrpHolder extends RecyclerView.ViewHolder {
    //basics
    private Context context;
    private ImageLoadHelper imageLoadHelper = new ImageLoadHelper();

    //elements
    private ImageView list_profileImage;
    private EmojiconTextView list_name;

    ExistingGrpHolder(View itemView) {
        super(itemView);
        context = itemView.getContext();

        //set elements
        list_name = itemView.findViewById(R.id.list_name);
        list_profileImage = itemView.findViewById(R.id.list_profileImage);
    }

    public void setData(final ChatTabList data) {
        //get row from cr list
        ContactRoster contactRoster = data.getCR();

        //set final jid
        final String jid = contactRoster.getContactJid();

        //set grp profile img
        imageLoadHelper.setImgByte(list_profileImage, contactRoster.getProfilephoto(), R.drawable.grp_propic_circle_150px);

//        if (contactRoster.getProfilephoto() != null) {
//            GlideApp.with(itemView)
//                    .asBitmap()
//                    .load(contactRoster.getProfilephoto())
//                    .placeholder(R.drawable.grp_propic_circle_150px)
//                    .apply(RequestOptions.circleCropTransform())
////                    .transition(BitmapTransitionOptions.withCrossFade())
//                    .thumbnail(0.5f)
//                    .encodeQuality(50)
//                    .override(180, 180)
////                    .into(newchatprofileimage);
//                    .into(new SimpleTarget<Bitmap>() {
//                        @Override
//                        public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
//                            list_profileImage.setImageBitmap(resource);
//                        }
//                    });
//        }

        //set grp displayname
        list_name.setText(contactRoster.getDisplayname());

        //set UI and disable functions for disabled rooms
        if (contactRoster.getDisabledStatus() == 1) { //disabled
            itemView.setClickable(false);

            list_profileImage.setColorFilter(ContextCompat.getColor(itemView.getContext(), R.color.grey5));
            list_name.setTextColor(ContextCompat.getColor(itemView.getContext(), R.color.grey5));
        }

        //set onclick listener to whole itemview
        itemView.setOnClickListener(v -> {
            Activity activity = (Activity) context;
            long apptDate = activity.getIntent().getLongExtra("Date", 0);

            Intent intent = new Intent(itemView.getContext(), NewIndiExistApptActivity.class);

            intent.putExtra("jid", jid);
            intent.putExtra("exist", 1);
            intent.putExtra("Date", apptDate);

            //start new activity
            context.startActivity(intent);

            //finish current activity
            activity.finish();
        });
    }


//    private TextView jid_txt;
//    private ImageView list_profileImage;
//    private EmojiconTextView list_name;
//
//    ExistingGrpHolder(View itemView) {
//        super(itemView);
//
//        list_name = (EmojiconTextView) itemView.findViewById(R.id.list_name);
//        jid_txt = (TextView) itemView.findViewById(R.id.jid_txt);
//        list_profileImage = (ImageView) itemView.findViewById(R.id.list_profileImage);
//
//        itemView.setOnClickListener(this);
//    }
//
//    public void setData(Cursor cursor) {
//        list_name.setText(cursor.getString(2));
//        jid_txt.setText(cursor.getString(3));
//
//        if (cursor.getBlob(4) != null) {
//            GlideApp.with(itemView)
//                    .asBitmap()
//                    .load(cursor.getBlob(4))
//                    .placeholder(R.drawable.default_propic_small_round)
//                    .apply(RequestOptions.circleCropTransform())
////                    .skipMemoryCache(false)
////                    .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
//                    .thumbnail(0.5f)
//                    .encodeQuality(50)
//                    .override(180, 180)
////                    .transition(withCrossFade())
//                    .into(new SimpleTarget<Bitmap>() {
//                        @Override
//                        public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
//                            list_profileImage.setImageBitmap(resource);
//                        }
//                    });
////                    .into(profileimage);
//        } else {
////            Glide.with(itemView).clear(list_profileImage);
//
//            GlideApp.with(itemView)
//                    .load(R.drawable.default_propic_small_round)
//                    .placeholder(R.drawable.default_propic_small_round)
//                    .into(list_profileImage);
//        }
//
//        if (cursor.getInt(5) == 1) { //user has left room
//            itemView.setClickable(false);
//
//            list_profileImage.setColorFilter(ContextCompat.getColor(itemView.getContext(), R.color
//                    .grey5));
//            list_name.setTextColor(ContextCompat.getColor(itemView.getContext(), R.color
//                    .grey5));
//        }
//    }
//
//    @Override
//    public void onClick(View v) {

//    }
}
