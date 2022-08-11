package com.soapp.schedule_class.single_appt;

import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.load.resource.bitmap.BitmapTransitionOptions;
import com.bumptech.glide.request.RequestOptions;
import com.soapp.R;
import com.soapp.SoappModel.ApptCalendarModel;
import com.soapp.global.GlideAPI.GlideApp;
import com.soapp.global.GlobalVariables;
import com.soapp.global.ImagePreviewByte;
import com.soapp.global.MapGps;
import com.soapp.global.MiscHelper;
import com.soapp.global.Preferences;
import com.soapp.global.UIHelper;
import com.soapp.sql.DatabaseHelper;
import com.soapp.sql.room.entity.Appointment;
import com.soapp.xmpp.SingleChatHelper.SingleChatStanza;
import com.soapp.xmpp.SmackHelper;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;
import io.github.rockerhieu.emojicon.EmojiconTextView;

/* Created by Jayyong on 29/04/2018. */

public class IndiScheHolder extends RecyclerView.ViewHolder {
    //basics
    private DatabaseHelper databaseHelper = DatabaseHelper.getInstance();
    private Context context;
    private UIHelper uiHelper = new UIHelper();
    private MiscHelper miscHelper = new MiscHelper();
    private Preferences preferences = Preferences.getInstance();

    //elements
//    private View schelog_noti_title, schelog_noti_date, schelog_noti_loc;
    private EmojiconTextView schelog_appt_title;
    private TextView schelog_month, schelog_date_short, schelog_host_name, schelog_date_long,
            schelog_time, schelog_loc_title, schelog_frnd_status, schelog_going, schelog_undec,
            schelog_not_going, schelog_hosted_by, host_status_btn, schelog_noti_title, schelog_noti_date, schelog_noti_loc;
    private LinearLayout self_status_btns;
    private ImageView schelog_map, schelog_self_profile, schelog_frnd_profile, schelog_edit_title, schelog_delete_btn;
    private ImageButton waze_btn, google_map_btn;
    private ConstraintLayout indi_appt_date_time, indi_appt_loc;
    private RelativeLayout schelog_loc_rl;

    //stanza
    private SingleChatStanza singleChatStanza = new SingleChatStanza();

    IndiScheHolder(View itemView) {
        super(itemView);

        //set context
        context = itemView.getContext();

        //set elements
        schelog_month = itemView.findViewById(R.id.schelog_month);
        schelog_date_short = itemView.findViewById(R.id.schelog_date_short);
        schelog_appt_title = itemView.findViewById(R.id.schelog_appt_title);
        schelog_host_name = itemView.findViewById(R.id.schelog_host_name);
        schelog_date_long = itemView.findViewById(R.id.schelog_date_long);
        schelog_time = itemView.findViewById(R.id.schelog_time);
        schelog_loc_title = itemView.findViewById(R.id.schelog_loc_title);
        schelog_map = itemView.findViewById(R.id.schelog_map);
        schelog_going = itemView.findViewById(R.id.schelog_going);
        schelog_undec = itemView.findViewById(R.id.schelog_undec);
        schelog_not_going = itemView.findViewById(R.id.schelog_not_going);
        schelog_self_profile = itemView.findViewById(R.id.schelog_self_profile);
        schelog_frnd_profile = itemView.findViewById(R.id.schelog_frnd_profile);
        schelog_frnd_status = itemView.findViewById(R.id.schelog_frnd_status);
        self_status_btns = itemView.findViewById(R.id.self_status_btns);
        host_status_btn = itemView.findViewById(R.id.host_status_btn);
        waze_btn = itemView.findViewById(R.id.waze_btn);
        google_map_btn = itemView.findViewById(R.id.google_map_btn);
        indi_appt_date_time = itemView.findViewById(R.id.indi_appt_date_time);
        indi_appt_loc = itemView.findViewById(R.id.indi_appt_loc);
        schelog_hosted_by = itemView.findViewById(R.id.schelog_hosted_by);
        schelog_loc_rl = itemView.findViewById(R.id.schelog_loc_rl);
        schelog_edit_title = itemView.findViewById(R.id.schelog_edit_title);
        schelog_delete_btn = itemView.findViewById(R.id.schelog_delete_appt);

        //noti badges
        schelog_noti_title = itemView.findViewById(R.id.schelog_noti_title);
        schelog_noti_date = itemView.findViewById(R.id.schelog_noti_date);
        schelog_noti_loc = itemView.findViewById(R.id.schelog_noti_loc);
    }

    public void setData(final Appointment data) {
        //set self username
        String selfUsername = preferences.getValue(context, GlobalVariables.STRPREF_USERNAME);

        //set final variables
        final String apptTitle = data.getApptTitle();
        final String apptID = data.getApptID();
        final String apptLat = data.getApptLatitude();
        final String apptLon = data.getApptLongitude();
        final String apptResID = data.getApptResID();
        final String apptResImgURL = data.getApptResImgURL();

        //set appt title and action
        schelog_appt_title.setText(apptTitle);

        //syah font of emojitextview
        Typeface typeface = Typeface.createFromAsset(context.getAssets(), "fonts/Quicksand_Bold.otf");
        schelog_appt_title.setTypeface(typeface);

        schelog_appt_title.setOnClickListener(v -> {
            editApptTitle(apptTitle, apptID);
        });

        //set edit appt title action
        schelog_edit_title.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editApptTitle(apptTitle, apptID);
            }
        });

        //set noti for title
        if (data.getApptNotiBadgeTitle() > 0) { //got noti
            animateFadeNotiBadge(schelog_noti_title, true);

            //syah margin top for when have title noti badge
            ViewGroup.MarginLayoutParams marginTop = (ViewGroup.MarginLayoutParams) schelog_appt_title.getLayoutParams();
            marginTop.topMargin = 30;
            schelog_appt_title.setLayoutParams(marginTop);

        } else {
            schelog_noti_title.setVisibility(View.GONE);
        }

        //get appt date and time in long
        long apptDate = data.getApptDate();
        long apptTime = data.getApptTime();

        //set month
        DateFormat monthFormat = new SimpleDateFormat("MMM", Locale.ENGLISH);
        schelog_month.setText(monthFormat.format(apptDate));

        //set date (day)
        DateFormat dateDayFormat = new SimpleDateFormat("dd", Locale.ENGLISH);
        schelog_date_short.setText(dateDayFormat.format(apptDate));

        //set host
        String hostName;

        //set self status
        switch (data.getSelf_Status()) {
            case 0: //host
                hostName = " " + context.getString(R.string.you);

                self_status_btns.setVisibility(View.INVISIBLE);
                host_status_btn.setVisibility(View.VISIBLE);
                break;

            case 1: //going
                hostName = " " + IndiScheLog.roomName;

                self_status_btns.setVisibility(View.VISIBLE);
                host_status_btn.setVisibility(View.GONE);

                setStatusBtns(data, selfUsername, apptID);

                schelog_going.setBackgroundResource(R.drawable.xml_left_round_right_round_primarydark2);
                schelog_going.setTextColor(context.getResources().getColor(R.color.grey1));

                schelog_undec.setBackgroundResource(0);
                schelog_undec.setTextColor(context.getResources().getColor(R.color.primaryDark1));

                schelog_not_going.setBackgroundResource(0);
                schelog_not_going.setTextColor(context.getResources().getColor(R.color.primaryDark1));
                break;

            case 2: //undecided
                hostName = " " + IndiScheLog.roomName;

                self_status_btns.setVisibility(View.VISIBLE);
                host_status_btn.setVisibility(View.GONE);

                setStatusBtns(data, selfUsername, apptID);

                schelog_not_going.setBackgroundResource(0);
                schelog_not_going.setTextColor(context.getResources().getColor(R.color.primaryDark1));

                schelog_undec.setBackgroundResource(R.drawable.xml_left_round_right_round_grey10);
                schelog_undec.setTextColor(context.getResources().getColor(R.color.grey1));

                schelog_going.setBackgroundResource(0);
                schelog_going.setTextColor(context.getResources().getColor(R.color.primaryDark1));
                break;

            case 3: //not going
                hostName = " " + IndiScheLog.roomName;

                self_status_btns.setVisibility(View.VISIBLE);
                host_status_btn.setVisibility(View.GONE);

                setStatusBtns(data, selfUsername, apptID);

                schelog_not_going.setBackgroundResource(R.drawable.xml_left_round_right_square_red);
                schelog_not_going.setTextColor(context.getResources().getColor(R.color.grey1));

                schelog_undec.setBackgroundResource(0);
                schelog_undec.setTextColor(context.getResources().getColor(R.color.primaryDark1));

                schelog_going.setBackgroundResource(0);
                schelog_going.setTextColor(context.getResources().getColor(R.color.primaryDark1));
                break;

            default: //invited
                hostName = " " + IndiScheLog.roomName;

                self_status_btns.setVisibility(View.VISIBLE);
                host_status_btn.setVisibility(View.GONE);

                setStatusBtns(data, selfUsername, apptID);

                schelog_not_going.setBackgroundResource(0);
                schelog_not_going.setTextColor(context.getResources().getColor(R.color.primaryDark1));

                schelog_undec.setBackgroundResource(R.drawable.xml_left_round_right_round_grey10);
                schelog_undec.setTextColor(context.getResources().getColor(R.color.grey1));

                schelog_going.setBackgroundResource(0);
                schelog_going.setTextColor(context.getResources().getColor(R.color.primaryDark1));
                break;
        }

        //set hostname
        if (hostName.equals(" null")) { //not host
            schelog_host_name.setVisibility(View.INVISIBLE);
            schelog_hosted_by.setVisibility(View.INVISIBLE);
            schelog_delete_btn.setVisibility(View.GONE);
        } else {
            if (hostName.equals(" " + context.getString(R.string.you))) {
                // hosting the appt
                schelog_delete_btn.setVisibility(View.VISIBLE);

                schelog_delete_btn.setOnClickListener(v ->
                        uiHelper.deleteAppt(context, IndiScheLog.jid, apptID, IndiScheLog.roomName,
                                false));
            } else {
                //not hosting appt
                schelog_delete_btn.setVisibility(View.GONE);
            }
            schelog_host_name.setText(hostName);
        }

        //set noti for date
        if (data.getApptNotiBadgeDate() > 0) { //got noti
            animateFadeNotiBadge(schelog_noti_date, true);
        } else {
            schelog_noti_date.setVisibility(View.GONE);
        }

        //set full date
        DateFormat dateFullFormat = new SimpleDateFormat("EEE, dd MMM yyyy", Locale.ENGLISH);
        schelog_date_long.setText(dateFullFormat.format(apptDate));

        //set time
        DateFormat timeFormat = new SimpleDateFormat("h:mm a", Locale.ENGLISH);
        schelog_time.setText(timeFormat.format(apptTime));

        indi_appt_date_time.setOnClickListener(v -> {
            if (SmackHelper.getXMPPConnection() != null && SmackHelper.getXMPPConnection().isAuthenticated()) {

                List<ApptCalendarModel> apptCalList = new ArrayList<>();
                apptCalList.add(0, new ApptCalendarModel(databaseHelper.getHostingGoingCDList(),
                        databaseHelper.getApptCDList(2), databaseHelper.getApptCDList(3)));

                uiHelper.dateTimePicker(context, "IndiScheLog", IndiScheLog.jid, apptID, null, apptCalList,
                        String.valueOf(apptDate), String.valueOf(apptTime));
            } else {
                Toast.makeText(context, R.string.xmpp_waiting_connection, Toast.LENGTH_SHORT).show();
            }
        });

        //set location title
        schelog_loc_title.setText(data.getApptLocation());

        //set location map
        GlideApp.with(context)
                .asBitmap()
                .load(data.getApptMapURL())
                .placeholder(R.drawable.ic_def_location_loading_400px)
                .transition(BitmapTransitionOptions.withCrossFade())
                .into(schelog_map);

        //edit loc title (edittext)
        schelog_loc_rl.setOnClickListener(view -> {
            Runnable editLocTitleAction = () -> {
                String uniqueId = UUID.randomUUID().toString();
                String staticurl = miscHelper.getGmapsStaticURL(data.getApptLatitude(), data.getApptLongitude());
                String pushMsg = context.getString(R.string.appt_location_changed);

                singleChatStanza.SoappAppointmentStanza(IndiScheLog.jid, pushMsg, "", staticurl,
                        GlobalVariables.string1, "", data.getApptLatitude(), data.getApptLongitude(), "", "",
                        "", "", uniqueId, "", "",
                        apptID, selfUsername, "appointment", "0", "3");

                databaseHelper.outgoingApptLocation(IndiScheLog.jid, apptID, staticurl, data.getApptLatitude(), data.getApptLongitude(),
                        GlobalVariables.string1, "", "", uniqueId);

                GlobalVariables.string1 = null;
            };

            uiHelper.editTextAlertDialog(context, context.getResources().getString(R.string.appt_loc_edit),
                    schelog_loc_title.getText().toString(), "scheLocTitle", IndiScheLog.jid, apptID,
                    true, editLocTitleAction);
        });

        indi_appt_loc.setOnClickListener(v -> {
            Intent intent = new Intent(context, MapGps.class);

            //specify from
            intent.putExtra("from", "scheHolder");

            //add user profile (friend's
            intent.putExtra("jid", IndiScheLog.jid);
            intent.putExtra("apptID", apptID);
            intent.putExtra("displayname", IndiScheLog.roomName);

            if (apptLat != null && !apptLat.equals("")) {
                intent.putExtra("placeName", schelog_loc_title.getText().toString());
                intent.putExtra("latitude", apptLat);
                intent.putExtra("longitude", apptLon);
            }

            //add restaurant info if got
            if (apptResID != null && !apptResID.equals("")) {
                intent.putExtra("resID", apptResID);
                intent.putExtra("resImgUrl", apptResImgURL);
            }

            context.startActivity(intent);
        });

        //show or hide navigation popup button based on whether got lat long
        if (apptLat != null && !apptLat.equals("")) {
            waze_btn.setVisibility(View.VISIBLE);
            waze_btn.setOnClickListener(v -> {
                miscHelper.openWazeApp(context, apptLat, apptLon);
            });

            google_map_btn.setVisibility(View.VISIBLE);
            google_map_btn.setOnClickListener(v -> {
                miscHelper.openGmapsApp(context, apptLat, apptLon);
            });
        }

        //set noti for location
        if (data.getApptNotiBadgeLoc() > 0) { //got noti
            animateFadeNotiBadge(schelog_noti_loc, true);
        } else {
            schelog_noti_loc.setVisibility(View.GONE);
        }

        //need to set location desc soon

        //set self's profile img
        String selfJid = preferences.getValue(context, GlobalVariables.STRPREF_USER_ID);
        byte[] selfProfileImgByte = databaseHelper.getImageBytesThumbFromContactRoster(selfJid);

        GlideApp.with(context)
                .load(selfProfileImgByte)
                .placeholder(R.drawable.in_propic_circle_150px)
                .apply(RequestOptions.circleCropTransform())
                .into(schelog_self_profile);

        schelog_self_profile.setOnClickListener(v -> {
            Intent myimageintent = new Intent(context, ImagePreviewByte.class);

            myimageintent.putExtra("jid", selfJid);

            context.startActivity(myimageintent);
        });

        //set friend's profile img
        byte[] frndProfileImgByte = databaseHelper.getImageBytesThumbFromContactRoster(IndiScheLog.jid);

        GlideApp.with(context)
                .load(frndProfileImgByte)
                .placeholder(R.drawable.in_propic_circle_150px)
                .apply(RequestOptions.circleCropTransform())
                .into(schelog_frnd_profile);

        schelog_frnd_profile.setOnClickListener(v -> {
            Intent friendimageintent = new Intent(context, ImagePreviewByte.class);

            friendimageintent.putExtra("jid", IndiScheLog.jid);

            context.startActivity(friendimageintent);
        });

        //set friend's name + status
        int frndStatus = data.getFriend_Status();

        String frndStatusStr;
        switch (frndStatus) {
            case 0: //host
                frndStatusStr = IndiScheLog.roomName + " " + context.getString(R.string.appt_is_hosting);
                break;

            case 1: //going
                frndStatusStr = IndiScheLog.roomName + " " + context.getString(R.string.appt_is_going);
                break;

            case 2: //undecided
                frndStatusStr = IndiScheLog.roomName + " " + context.getString(R.string.appt_is_undecided);
                break;

            case 3: //not going
                frndStatusStr = IndiScheLog.roomName + " " + context.getString(R.string.appt_is_not_going);
                break;

            default: //invited
                frndStatusStr = IndiScheLog.roomName + " " + context.getString(R.string.appt_is_undecided);
                break;

        }
        schelog_frnd_status.setText(frndStatusStr);
    }

    //set onclick for status btns if self is not hosting
    private void setStatusBtns(Appointment data, String selfUsername, String apptID) {
        //set onclick actions for each status button
        schelog_going.setOnClickListener(v -> {
            //only do action if not the same as selected status
            if (data.getSelf_Status() != 1) {
                if (SmackHelper.getXMPPConnection() != null && SmackHelper.getXMPPConnection().isAuthenticated()) {
                    //show confirmation dialog box
                    Runnable updateStatusAction = new Runnable() {
                        @Override
                        public void run() {
                            String pushMsg = selfUsername + " " + context.getString(R.string.appt_is_going);
                            String uniqueID = UUID.randomUUID().toString();

                            singleChatStanza.SoappAppointmentStanza(IndiScheLog.jid, pushMsg, "",
                                    "", "", "", "", "", "", "",
                                    "1", "", uniqueID, "", "", apptID,
                                    selfUsername, "appointment", "0", "4");

                            databaseHelper.outgoingApptStatus(IndiScheLog.jid, apptID, 1, uniqueID);

                            //set reminder alarm
                            databaseHelper.scheduleLocalNotification(apptID);

                        }
                    };

                    apptStatusConfirmation(updateStatusAction);

                } else {
                    Toast.makeText(context, R.string.xmpp_waiting_connection, Toast.LENGTH_SHORT).show();
                }
            }
        });

        schelog_undec.setOnClickListener(v -> {
            //only do action if not the same as selected status
            if (data.getSelf_Status() != 2) {
                if (SmackHelper.getXMPPConnection() != null && SmackHelper.getXMPPConnection().isAuthenticated()) {
                    Runnable updateStatusAction = new Runnable() {
                        @Override
                        public void run() {
                            String pushMsg = selfUsername + " " + context.getString(R.string.appt_is_undecided);
                            String uniqueID = UUID.randomUUID().toString();

                            singleChatStanza.SoappAppointmentStanza(IndiScheLog.jid, pushMsg, "",
                                    "", "", "", "", "", "", "",
                                    "2", "", uniqueID, "", "", apptID,
                                    selfUsername, "appointment", "0", "4");

                            databaseHelper.outgoingApptStatus(IndiScheLog.jid, apptID, 2, uniqueID);

                            //cancel reminder alarm
                            databaseHelper.cancelPendingAlarm(apptID);
                        }
                    };

                    apptStatusConfirmation(updateStatusAction);

                } else {
                    Toast.makeText(context, R.string.xmpp_waiting_connection, Toast.LENGTH_SHORT).show();
                }
            }
        });

        schelog_not_going.setOnClickListener(v -> {
            //only do action if not the same as selected status
            if (data.getSelf_Status() != 3) {
                if (SmackHelper.getXMPPConnection() != null && SmackHelper.getXMPPConnection().isAuthenticated()) {
                    Runnable updateStatusAction = new Runnable() {
                        @Override
                        public void run() {
                            String pushMsg = selfUsername + " " + context.getString(R.string.appt_is_not_going);
                            String uniqueID = UUID.randomUUID().toString();

                            singleChatStanza.SoappAppointmentStanza(IndiScheLog.jid, pushMsg, "",
                                    "", "", "", "", "", "", "",
                                    "3", "", uniqueID, "", "", apptID,
                                    selfUsername, "appointment", "0", "4");

                            databaseHelper.outgoingApptStatus(IndiScheLog.jid, apptID, 3, uniqueID);

                            //cancel reminder alarm
                            databaseHelper.cancelPendingAlarm(apptID);
                        }
                    };

                    apptStatusConfirmation(updateStatusAction);

                } else {
                    Toast.makeText(context, R.string.xmpp_waiting_connection, Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    //animate fade-away for appt noti badges
    private void animateFadeNotiBadge(View notiBadgeTextView, boolean needFade) {
        notiBadgeTextView.setVisibility(View.VISIBLE);

//        if (needFade) {
//            notiBadgeTextView.animate().alpha(0f).setDuration(2000);
//        }
    }

    //function for editing appt title
    private void editApptTitle(String apptTitle, String apptID) {
        uiHelper.editTextAlertDialog(context, context.getResources().getString(R.string.appt_title_edit), apptTitle, "IndiScheHolder",
                IndiScheLog.jid, apptID, true, null);
    }

    //function for showing appt status confirmation alertdialog
    private void apptStatusConfirmation(Runnable apptStatusAction) {
        uiHelper.dialog2Btns(context, null,
                context.getString(R.string.schelog_appt_status_msg), R.string.ok_label, R.string
                        .cancel, R.color.white, R.color.black,
                apptStatusAction, null, true);
    }


}