package com.soapp.chat_class.group_chat.details.events;

import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.soapp.R;
import com.soapp.chat_class.group_chat.details.GroupChatDetail;
import com.soapp.schedule_class.group_appt.GroupScheLog;
import com.soapp.schedule_class.new_appt.NewIndiExistApptActivity;
import com.soapp.sql.room.entity.Appointment;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Locale;

import androidx.recyclerview.widget.RecyclerView;

/* Created by jayyong on 19/05/2018. */

public class GroupChatEventsHolder extends RecyclerView.ViewHolder {
    //basics
    private Context context;

    //elements
    private LinearLayout chat_det_event_ll;
    private TextView chat_det_event_day, chat_det_event_month;

    //background array
    private int[] backgrounds = {R.drawable.xml_round_corner_30dp_pd1, R.drawable.xml_round_corner_30dp_pd2, R.drawable.xml_round_corner_30dp_pd3,
            R.drawable.xml_round_corner_30dp_pd4, R.drawable.xml_round_corner_30dp_pl4, R.drawable.xml_round_corner_30dp_pl3, R.drawable.xml_round_corner_30dp_pl2,
            R.drawable.xml_round_corner_30dp_pl1};

    GroupChatEventsHolder(View itemView) {
        super(itemView);
        context = itemView.getContext();

        //set elements
        chat_det_event_ll = itemView.findViewById(R.id.chat_det_event_ll);
        chat_det_event_day = itemView.findViewById(R.id.chat_det_event_day);
        chat_det_event_month = itemView.findViewById(R.id.chat_det_event_month);
    }

    public void setData(final Appointment data) {
        //if last item, add more button then return
        if (chat_det_event_ll == null) {
            itemView.setOnClickListener(v -> {
                Intent grpScheIntent = new Intent(context, NewIndiExistApptActivity.class);
                grpScheIntent.putExtra("jid", GroupChatDetail.jid);

                context.startActivity(grpScheIntent);
            });
            return;
        }
        long apptDate = data.getApptDate();

        //set date (day)
        DateFormat dateDayFormat = new SimpleDateFormat("dd", Locale.ENGLISH);
        chat_det_event_day.setText(dateDayFormat.format(apptDate));

        //set month
        DateFormat monthFormat = new SimpleDateFormat("MMM", Locale.ENGLISH);
        chat_det_event_month.setText(monthFormat.format(apptDate));

        //set color to background
        int position = getLayoutPosition() + 1;
        int roundedPos = Math.round(getLayoutPosition() / 8);

        if (position > 8) {
            position = position - roundedPos * 8 - 1;
        } else {
            position = position - 1;
        }

        chat_det_event_ll.setBackground(context.getDrawable(backgrounds[position]));

        //set final variables for on click
        final String apptID = data.getApptID();
        itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, GroupScheLog.class);

            intent.putExtra("jid", GroupChatDetail.jid);
            intent.putExtra("apptID", apptID);

            context.startActivity(intent);
        });
    }

    public void setDataNoAppt() {
        itemView.setOnClickListener(v -> {
            Intent grpScheIntent = new Intent(context, NewIndiExistApptActivity.class);
            grpScheIntent.putExtra("jid", GroupChatDetail.jid);

            context.startActivity(grpScheIntent);
        });
    }
}
