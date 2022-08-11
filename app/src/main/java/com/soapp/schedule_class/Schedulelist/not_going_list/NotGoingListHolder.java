package com.soapp.schedule_class.Schedulelist.not_going_list;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.widget.TextView;

import com.soapp.R;
import com.soapp.chat_class.group_chat.details.GroupChatDetail;
import com.soapp.chat_class.single_chat.details.IndiChatDetail;
import com.soapp.global.Preferences;
import com.soapp.global.UIHelper;
import com.soapp.schedule_class.group_appt.GroupScheLog;
import com.soapp.schedule_class.single_appt.IndiScheLog;
import com.soapp.sql.DatabaseHelper;
import com.soapp.sql.room.joiners.Applist;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Locale;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;
import io.github.rockerhieu.emojicon.EmojiconTextView;

/* Created by Jayyong on 20/04/2018. */

public class NotGoingListHolder extends RecyclerView.ViewHolder {
    //basics
    private DatabaseHelper databaseHelper = DatabaseHelper.getInstance();
    private Context context;
    private Preferences preferences = Preferences.getInstance();
    private UIHelper uiHelper = new UIHelper();
    private ConstraintLayout notGoing_main_Ly;

    private TextView schelist_appt_date, schelist_appt_day, schelist_appt_time, schelist_appt_loc;
    private EmojiconTextView schelist_displayname, schelist_appt_title;

    NotGoingListHolder(View itemView) {
        super(itemView);
        //set context
        context = itemView.getContext();

        //set ids
        notGoing_main_Ly = itemView.findViewById(R.id.notGoing_main_Ly);
        schelist_displayname = itemView.findViewById(R.id.schelist_displayname);
        schelist_appt_title = itemView.findViewById(R.id.schelist_appt_title);
        schelist_appt_date = itemView.findViewById(R.id.schelist_appt_date);
        schelist_appt_day = itemView.findViewById(R.id.schelist_appt_day);
        schelist_appt_time = itemView.findViewById(R.id.schelist_appt_time);
        schelist_appt_loc = itemView.findViewById(R.id.schelist_appt_loc);
    }

    public void setData(final Applist data) {
        //appt date (only need set date if not null, means first)
        if (schelist_appt_date != null) {
            long apptDate = data.getAppointment().getApptDate();
            DateFormat dateFormat = new SimpleDateFormat("dd", Locale.ENGLISH);
            schelist_appt_date.setText(dateFormat.format(apptDate));

            //appt day (Mon, Tue...)
            DateFormat dayFormat = new SimpleDateFormat("E", Locale.ENGLISH);
            schelist_appt_day.setText(dayFormat.format(apptDate));
        }


        //set final jid and apptID
        final String jid = data.getAppointment().getApptJid();
        final String apptID = data.getAppointment().getApptID();

        if (jid.length() == 12) { //indi
            String phoneName = data.getContactRoster().getPhonename();
            if (phoneName != null && !phoneName.equals("")) {
                schelist_displayname.setText(phoneName);
            } else {
                String phoneNameNumber = data.getContactRoster().getDisplayname() + " " + data.getContactRoster().getPhonenumber();
                schelist_displayname.setText(phoneNameNumber);
            }
        } else { //grp
            schelist_displayname.setText(data.getContactRoster().getDisplayname());
        }

        //appt title
        String apptTitle = data.getAppointment().getApptTitle();
        if (apptTitle != null && !apptTitle.equals("")) {
            schelist_appt_title.setText(apptTitle);
        } else {
            schelist_appt_title.setText(R.string.appt_title);
        }

        //appt time
        long apptTime = data.getAppointment().getApptTime();

        if (apptTime == 0) {
            schelist_appt_time.setText("--:--");
        } else {
            DateFormat timeFormat = DateFormat.getTimeInstance(DateFormat.SHORT);
            schelist_appt_time.setText(timeFormat.format(apptTime));
        }

        //appt location
        String apptLoc = data.getAppointment().getApptLocation();
        if (apptLoc != null) {
            schelist_appt_loc.setText(apptLoc);
        }

        //set onclick - go to schelog
        itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent;
                if (jid.length() == 12) { //indi
                    intent = new Intent(context, IndiScheLog.class);
                } else { //group
                    intent = new Intent(context, GroupScheLog.class);
                }
                intent.putExtra("jid", jid);
                intent.putExtra("apptID", apptID);

                context.startActivity(intent);
            }
        });

        //set longclick - delete room and view profile
        itemView.setOnLongClickListener(v -> {

            //action for deleting room - bring up alert dialog
            Runnable deleteApptAction = () -> {
                String roomName = schelist_displayname.getText().toString();

                uiHelper.deleteAppt(context, jid, apptID, roomName, true);
            };

            Activity activity = (Activity) context;

            //action for viewing room profile - go to chat detail
            Runnable viewProfileAction = () -> {
                Intent intent;
                if (jid.length() == 12) { //indi
                    intent = new Intent(context, IndiChatDetail.class);
                } else { //group
                    intent = new Intent(context, GroupChatDetail.class);
                }
                intent.putExtra("jid", jid);

                context.startActivity(intent);
            };

            uiHelper.bottomDialog5Btns(context, context.getString(R.string.delete_appt), deleteApptAction,
                    context.getString(R.string.view_profile), viewProfileAction,
                    null,null,null,null,null,null);

            return true;
        });
    }
}