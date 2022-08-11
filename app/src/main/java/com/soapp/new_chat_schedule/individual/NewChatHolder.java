package com.soapp.new_chat_schedule.individual;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.soapp.R;
import com.soapp.chat_class.single_chat.IndiChatLog;
import com.soapp.global.ImageLoadHelper;
import com.soapp.schedule_class.new_appt.NewIndiExistApptActivity;
import com.soapp.sql.room.entity.ContactRoster;

import androidx.recyclerview.widget.RecyclerView;

//cross fade animation from glide

/* Created by chang on 04/08/2017. */
public class NewChatHolder extends RecyclerView.ViewHolder {
    //basics
    private Context context = itemView.getContext();

    //elements
    private TextView newchatname;
    private ImageView newchatprofileimage;

    private ImageLoadHelper imageLoadHelper = new ImageLoadHelper();

    NewChatHolder(View itemView) {
        super(itemView);

        //set elements
        newchatprofileimage = itemView.findViewById(R.id.newchatprofileimage);
        newchatname = itemView.findViewById(R.id.newchatname);
    }

    public void setData(final ContactRoster data) {
        //set final jid
        final String jid = data.getContactJid();

        //set member profile img
        imageLoadHelper.setImgByte(newchatprofileimage, data.getProfilephoto(), R.drawable.in_propic_circle_150px);

        //set member displayname
        newchatname.setText(data.getPhonename());

        //set onclick listener to whole itemview for checking
        itemView.setOnClickListener(v -> {
            Intent intent;
            if (NewChatController.from.equals("chat")) { //create new chat
                intent = new Intent(context, IndiChatLog.class);

            } else { //create new schedule
                intent = new Intent(context, NewIndiExistApptActivity.class);
                intent.putExtra("Date", NewChatController.date);
            }
            intent.putExtra("jid", jid);

            //start new activity
            context.startActivity(intent);

            //finish current activity
            Activity activity = (Activity) context;
            activity.finish();
        });
    }
}