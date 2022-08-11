package com.soapp.schedule_class.group_appt;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
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
import com.soapp.chat_class.single_chat.details.IndiChatDetail;
import com.soapp.global.ExpandableGridView;
import com.soapp.global.GlideAPI.GlideApp;
import com.soapp.global.GlobalVariables;
import com.soapp.global.MapGps;
import com.soapp.global.MiscHelper;
import com.soapp.global.Preferences;
import com.soapp.global.UIHelper;
import com.soapp.schedule_class.group_appt.gms_collapsed.MemberListAdapter;
import com.soapp.schedule_class.group_appt.gms_expanded.GrpMemStatusAdapter;
import com.soapp.setup.Soapp;
import com.soapp.sql.DatabaseHelper;
import com.soapp.sql.room.entity.Appointment;
import com.soapp.sql.room.entity.ContactRoster;
import com.soapp.xmpp.GroupChatHelper.GroupChatStanza;
import com.soapp.xmpp.SmackHelper;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.LiveData;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import io.github.rockerhieu.emojicon.EmojiconTextView;

/* Created by Jayyong on 01/05/2018. */

public class GroupScheHolder extends RecyclerView.ViewHolder {
    //basics
    private DatabaseHelper databaseHelper = DatabaseHelper.getInstance();
    private Context context;
    private UIHelper uiHelper = new UIHelper();
    private MiscHelper miscHelper = new MiscHelper();

    //elements
//    private View schelog_noti_title;
    private TextView schelog_month, schelog_date_short, schelog_host_name, schelog_date_long,
            schelog_time, schelog_loc_title, schelog_going, schelog_undec, schelog_not_going,
            schelog_hosted_by, host_status_btn, schelog_noti_loc, schelog_noti_date, schelog_noti_title;
    private LinearLayout self_status_btns, grp_appt_date_time;
    private ImageView schelog_map, schelog_self_profile, schelog_edit_title, schelog_delete_btn;
    private EmojiconTextView schelog_appt_title;
    private ImageButton waze_btn, google_map_btn;
    private ConstraintLayout grp_appt_loc, grpsche_status_outer, grpsche_status_inner;
    private RelativeLayout schelog_loc_rl;

    //stanza
    private GroupChatStanza groupChatStanza = new GroupChatStanza();
    private Preferences preferences = Preferences.getInstance();

    //for grp member status
    private TextView grpsche_outer_status_label;
    private MemberListAdapter memberListAdapter;
    private RecyclerView grpsche_members_rv;

    //for inner grp member status
    private TextView grpsche_see_less, tv_going, tv_undecided, tv_not_going;
    private ExpandableGridView gv_going, gv_undecided, gv_not_going;

    private GrpMemStatusAdapter goingApptMemAdapter;
    private GrpMemStatusAdapter grpMemStatusAdapter;
    private GrpMemStatusAdapter notGoingApptMemAdapter;

    //todo ibrahim
    private EditText edtRM;
    private Button btnRM;
    private String a;
    private TextView rmtv;

    GroupScheHolder(View itemView) {
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
        self_status_btns = itemView.findViewById(R.id.self_status_btns);
        host_status_btn = itemView.findViewById(R.id.host_status_btn);
        waze_btn = itemView.findViewById(R.id.waze_btn);
        google_map_btn = itemView.findViewById(R.id.google_map_btn);
        schelog_edit_title = itemView.findViewById(R.id.schelog_edit_title);
        schelog_delete_btn = itemView.findViewById(R.id.schelog_delete_appt);
        grp_appt_date_time = itemView.findViewById(R.id.grp_appt_date_time);
        grp_appt_loc = itemView.findViewById(R.id.grp_appt_loc);
        schelog_loc_rl = itemView.findViewById(R.id.schelog_loc_rl);
        schelog_hosted_by = itemView.findViewById(R.id.schelog_hosted_by);

        //noti badges
        schelog_noti_title = itemView.findViewById(R.id.schelog_noti_title);
        schelog_noti_date = itemView.findViewById(R.id.schelog_noti_date);
        schelog_noti_loc = itemView.findViewById(R.id.schelog_noti_loc);

        //grp members images (mains only)
        grpsche_status_outer = itemView.findViewById(R.id.grpsche_status_outer);
        grpsche_outer_status_label = itemView.findViewById(R.id.grpsche_outer_status_label);
        grpsche_members_rv = itemView.findViewById(R.id.grpsche_members_rv);
        grpsche_status_inner = itemView.findViewById(R.id.grpsche_status_inner);

        //inner status elements
        grpsche_see_less = itemView.findViewById(R.id.grpsche_see_less);
        tv_going = itemView.findViewById(R.id.tv_going);
        tv_undecided = itemView.findViewById(R.id.tv_undecided);
        tv_not_going = itemView.findViewById(R.id.tv_not_going);
        gv_going = itemView.findViewById(R.id.gv_going);
        gv_undecided = itemView.findViewById(R.id.gv_undecided);
        gv_not_going = itemView.findViewById(R.id.gv_not_going);

        edtRM = itemView.findViewById(R.id.remindertime);
        btnRM = itemView.findViewById(R.id.rmbutton);
        rmtv = itemView.findViewById(R.id.rmtv);

    }

    public void setData(final Appointment data) {
        //set self username
        String selfUsername = preferences.getValue(context, GlobalVariables.STRPREF_USERNAME);
        String selfJid = preferences.getValue(context, GlobalVariables.STRPREF_USER_ID);

        //set final variables
        final int selfStatus = data.getSelf_Status();
        final String apptID = data.getApptID();
        final String apptTitle = data.getApptTitle();
        final String apptLat = data.getApptLatitude();
        final String apptLon = data.getApptLongitude();
        final String apptResID = data.getApptResID();
        final String apptResImgURL = data.getApptResImgURL();

        //set appt title and action (need to change to popup soon)
        if (apptTitle != null) {
            schelog_appt_title.setText(apptTitle);

            //syah font of emojitextview
            Typeface typeface = Typeface.createFromAsset(context.getAssets(), "fonts/Quicksand_Bold.otf");
            schelog_appt_title.setTypeface(typeface);
        }

        //set edit appt title action to hosted by
        schelog_hosted_by.setOnClickListener(v -> editApptTitle(apptTitle, apptID));
        schelog_host_name.setOnClickListener(v -> editApptTitle(apptTitle, apptID));

        //set edit appt title action to appt title
        schelog_appt_title.setOnClickListener(v -> editApptTitle(apptTitle, apptID));

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

        //set edit appt title action to edit appt title btn
        schelog_edit_title.setOnClickListener(v -> editApptTitle(apptTitle, apptID));

        //get appt date and time in long
        long apptDate = data.getApptDate();
        long apptTime = data.getApptTime();

        //set month
        DateFormat monthFormat = new SimpleDateFormat("MMM", Locale.ENGLISH);
        schelog_month.setText(monthFormat.format(apptDate));

        //set date (day)
        DateFormat dateDayFormat = new SimpleDateFormat("dd", Locale.ENGLISH);
        schelog_date_short.setText(dateDayFormat.format(apptDate));

        //set host, going and undec count
        String hostName, goingMemCount, undecMemCount;

        //set self status and number of members going/undec (need to add self status in)
        switch (data.getSelf_Status()) {
            case 0: //host
                hostName = " " + context.getString(R.string.you);
                goingMemCount = databaseHelper.getApptGoingMemCount(apptID) + " ";
                undecMemCount = databaseHelper.getApptUndecMemCount(apptID) + " ";

                self_status_btns.setVisibility(View.INVISIBLE);
                host_status_btn.setVisibility(View.VISIBLE);
                break;

            case 1: //going
                hostName = " " + databaseHelper.getApptHostNameGrp(apptID);
                goingMemCount = (databaseHelper.getApptGoingMemCount(apptID) + 1) + " ";
                undecMemCount = databaseHelper.getApptUndecMemCount(apptID) + " ";

                self_status_btns.setVisibility(View.VISIBLE);
                host_status_btn.setVisibility(View.GONE);

                setStatusBtns(data, selfUsername, selfJid, apptID);

                schelog_going.setBackgroundResource(R.drawable.xml_left_round_right_round_primarydark2);
                schelog_going.setTextColor(context.getResources().getColor(R.color.grey1));

                schelog_undec.setBackgroundResource(0);
                schelog_undec.setTextColor(context.getResources().getColor(R.color.primaryDark1));

                schelog_not_going.setBackgroundResource(0);
                schelog_not_going.setTextColor(context.getResources().getColor(R.color.primaryDark1));
                break;

            case 2: //undecided
                hostName = " " + databaseHelper.getApptHostNameGrp(apptID);
                goingMemCount = databaseHelper.getApptGoingMemCount(apptID) + " ";
                undecMemCount = (databaseHelper.getApptUndecMemCount(apptID) + 1) + " ";

                self_status_btns.setVisibility(View.VISIBLE);
                host_status_btn.setVisibility(View.GONE);

                setStatusBtns(data, selfUsername, selfJid, apptID);

                schelog_undec.setBackgroundResource(R.drawable.xml_left_round_right_round_grey10);
                schelog_undec.setTextColor(context.getResources().getColor(R.color.grey1));

                schelog_going.setBackgroundResource(0);
                schelog_going.setTextColor(context.getResources().getColor(R.color.primaryDark1));

                schelog_not_going.setBackgroundResource(0);
                schelog_not_going.setTextColor(context.getResources().getColor(R.color.primaryDark1));
                break;

            case 3: //not going
                hostName = " " + databaseHelper.getApptHostNameGrp(apptID);
                goingMemCount = databaseHelper.getApptGoingMemCount(apptID) + " ";
                undecMemCount = databaseHelper.getApptUndecMemCount(apptID) + " ";

                self_status_btns.setVisibility(View.VISIBLE);
                host_status_btn.setVisibility(View.GONE);

                setStatusBtns(data, selfUsername, selfJid, apptID);

                schelog_not_going.setBackgroundResource(R.drawable.xml_left_round_right_square_red);
                schelog_not_going.setTextColor(context.getResources().getColor(R.color.grey1));

                schelog_undec.setBackgroundResource(0);
                schelog_undec.setTextColor(context.getResources().getColor(R.color.primaryDark1));

                schelog_going.setBackgroundResource(0);
                schelog_going.setTextColor(context.getResources().getColor(R.color.primaryDark1));
                break;

            default: //invited
                hostName = " " + databaseHelper.getApptHostNameGrp(apptID);
                goingMemCount = databaseHelper.getApptGoingMemCount(apptID) + " ";
                undecMemCount = (databaseHelper.getApptUndecMemCount(apptID) + 1) + " ";

                self_status_btns.setVisibility(View.VISIBLE);
                host_status_btn.setVisibility(View.GONE);

                setStatusBtns(data, selfUsername, selfJid, apptID);

                schelog_undec.setBackgroundResource(R.drawable.xml_left_round_right_round_grey10);
                schelog_undec.setTextColor(context.getResources().getColor(R.color.grey1));

                schelog_going.setBackgroundResource(0);
                schelog_going.setTextColor(context.getResources().getColor(R.color.primaryDark1));

                schelog_not_going.setBackgroundResource(0);
                schelog_not_going.setTextColor(context.getResources().getColor(R.color.primaryDark1));
                break;
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

        grp_appt_date_time.setOnClickListener(v -> {
            if (SmackHelper.getXMPPConnection() != null && SmackHelper.getXMPPConnection().isAuthenticated()) {

                List<ApptCalendarModel> apptCalList = new ArrayList<>();
                apptCalList.add(0, new ApptCalendarModel(databaseHelper.getHostingGoingCDList(),
                        databaseHelper.getApptCDList(2), databaseHelper.getApptCDList(3)));

                uiHelper.dateTimePicker(context, "GrpScheLog", GroupScheLog.jid, apptID, GroupScheLog.roomName, apptCalList,
                        String.valueOf(apptDate), String.valueOf(apptTime));
            } else {
                Toast.makeText(context, R.string.xmpp_waiting_connection, Toast.LENGTH_SHORT).show();
            }
        });

        //set location title
        schelog_loc_title.setText(data.getApptLocation());

        //set noti for location
        if (data.getApptNotiBadgeLoc() > 0) { //got noti
            animateFadeNotiBadge(schelog_noti_loc, true);
        } else {
            schelog_noti_loc.setVisibility(View.GONE);
        }

        //set location map
        GlideApp.with(itemView)
                .asBitmap()
                .load(miscHelper.getGmapsStaticURL(data.getApptLatitude(), data.getApptLongitude()))
                .placeholder(R.drawable.ic_def_location_loading_400px)
                .transition(BitmapTransitionOptions.withCrossFade())
                .into(schelog_map);

        //go to map when click on map
        grp_appt_loc.setOnClickListener(v -> {
            Intent intent = new Intent(context, MapGps.class);

            //specify from
            intent.putExtra("from", "scheHolder");

            //add user profile (friend's
            intent.putExtra("jid", GroupScheLog.jid);
            intent.putExtra("apptID", apptID);
            intent.putExtra("displayname", GroupScheLog.roomName);

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

        //edit loc title (edittext)
        schelog_loc_rl.setOnClickListener(view -> {
            Runnable editLocTitleAction = () -> {
                String uniqueId = UUID.randomUUID().toString();
                String staticurl = miscHelper.getGmapsStaticURL(data.getApptLatitude(), data.getApptLongitude());
                String pushMsg = context.getString(R.string.appt_location_changed);

                groupChatStanza.GroupAppointment(GroupScheLog.jid, selfJid, uniqueId, "",
                        staticurl, GlobalVariables.string1, data.getApptLatitude(), data.getApptLongitude(), "", "",
                        "", "", "", apptID, GroupScheLog.roomName,
                        pushMsg, "appointment", "", "3");

                databaseHelper.outgoingApptLocation(GroupScheLog.jid, apptID, staticurl, data.getApptLatitude(), data.getApptLongitude(),
                        GlobalVariables.string1, "", "", uniqueId);

                GlobalVariables.string1 = null;
            };

            uiHelper.editTextAlertDialog(context, context.getResources().getString(R.string.appt_loc_edit),
                    schelog_loc_title.getText().toString(), "scheLocTitle", GroupScheLog.jid, apptID,
                    true, editLocTitleAction);
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

        //need to set location desc soon

        //set self's profile img
        byte[] selfProfileImgByte = databaseHelper.getImageBytesThumbFromContactRoster(selfJid);

        GlideApp.with(itemView)
                .load(selfProfileImgByte)
                .placeholder(R.drawable.in_propic_circle_150px)
                .apply(RequestOptions.circleCropTransform())
                .into(schelog_self_profile);

        //set hostname and going/undec numbers
        if (hostName.equals(" null")) {
            //hosting the appt
            schelog_host_name.setVisibility(View.INVISIBLE);
            schelog_hosted_by.setVisibility(View.INVISIBLE);
            schelog_delete_btn.setVisibility(View.GONE);
        } else {
            if (hostName.equals(" " + context.getString(R.string.you))) {
                // hosting the appt
                schelog_delete_btn.setVisibility(View.VISIBLE);

                schelog_delete_btn.setOnClickListener(v ->
                        uiHelper.deleteAppt(context, GroupScheLog.jid, apptID, GroupScheLog.roomName,
                                false));

            } else {
                //not hosting appt
                schelog_delete_btn.setVisibility(View.GONE);
            }
            schelog_host_name.setText(hostName);

        }

        //set outer label (xx going . yy undecided)
        String outerLabel = goingMemCount + " Going . " + undecMemCount + " Undecided";
        grpsche_outer_status_label.setText(outerLabel);

        //populate recyclerview for grpMem
        memberListAdapter = new MemberListAdapter();

        LinearLayoutManager llm = new LinearLayoutManager(context);
        llm.setOrientation(LinearLayoutManager.HORIZONTAL);

        grpsche_members_rv.setLayoutManager(llm);
        grpsche_members_rv.setItemAnimator(null);

        LiveData<List<ContactRoster>> list = Soapp.getDatabase().selectQuery().load_grpMemScheGoingFirst(GroupScheLog.jid, apptID);
        list.observe((LifecycleOwner) context, memberList -> {
            memberListAdapter.submitList(memberList);
            memberListAdapter.notifyDataSetChanged();
        });
        grpsche_members_rv.setAdapter(memberListAdapter);

        //function for show all grp mem (click on outer cl)
        grpsche_status_outer.setOnClickListener(v -> {
            //set live data for all statuses
            if (goingApptMemAdapter == null) {
                LiveData<List<ContactRoster>> going_list = Soapp.getDatabase().selectQuery().load_grpMemSche_with_status(apptID, 1);
                going_list.observe((LifecycleOwner) context, memGoingList -> {
                    if (selfStatus == 1) { //add self's profile to top of list
                        memGoingList.add(0, new ContactRoster(selfUsername, selfProfileImgByte));
                    }

                    //set going number label
                    String grpMemGoingTx = memGoingList.size() + " " + context.getString(R.string.appt_going);
                    tv_going.setText(grpMemGoingTx);

                    //setup gridview
                    if (memGoingList.size() != 0) {
                        goingApptMemAdapter = new GrpMemStatusAdapter(context, memGoingList);
                        gv_going.setAdapter(goingApptMemAdapter);
                        gv_going.setOnItemClickListener((parent, view, position, id) -> goToChatProfile(memGoingList.get(position).getContactJid()));
                    }
                });

                LiveData<List<ContactRoster>> undecided_list = Soapp.getDatabase().selectQuery().load_grpMemSche_with_status(apptID, 2);
                undecided_list.observe((LifecycleOwner) context, memUndecList -> {
                    if (selfStatus == 2) { //add self's profile to top of list
                        memUndecList.add(0, new ContactRoster(selfUsername, selfProfileImgByte));
                    }

                    String grpMemUndecTx = memUndecList.size() + " " + context.getString(R.string.appt_undecided);
                    tv_undecided.setText(grpMemUndecTx);

                    if (memUndecList.size() != 0) {
                        grpMemStatusAdapter = new GrpMemStatusAdapter(context, memUndecList);
                        gv_undecided.setAdapter(grpMemStatusAdapter);
                        gv_undecided.setOnItemClickListener((parent, view, position, id) -> goToChatProfile(memUndecList.get(position).getContactJid()));

                    }
                });

                LiveData<List<ContactRoster>> not_going_list = Soapp.getDatabase().selectQuery().load_grpMemSche_with_status(apptID, 3);
                not_going_list.observe((LifecycleOwner) context, memNGList -> {
                    if (selfStatus == 3) { //add self's profile to top of list
                        memNGList.add(0, new ContactRoster(selfUsername, selfProfileImgByte));
                    }

                    String grpMemNGTx = memNGList.size() + " " + context.getString(R.string.appt_not_going);
                    tv_not_going.setText(grpMemNGTx);

                    if (memNGList.size() != 0) {
                        notGoingApptMemAdapter = new GrpMemStatusAdapter(context, memNGList);
                        gv_not_going.setAdapter(notGoingApptMemAdapter);
                        gv_not_going.setOnItemClickListener((parent, view, position, id) -> goToChatProfile(memNGList.get(position).getContactJid()));

                    }
                });

                //set onclick to show less
                grpsche_see_less.setOnClickListener(v1 -> {
                    grpsche_status_outer.setVisibility(View.VISIBLE);
                    grpsche_status_inner.setVisibility(View.GONE);
                });
            }

            //hide/show cl after setup
            grpsche_status_outer.setVisibility(View.GONE);
            grpsche_status_inner.setVisibility(View.VISIBLE);
        });

        final long test = (data.getApptTime() - System.currentTimeMillis() - data.getApptReminderTime() < 0 ? TimeUnit.MINUTES.toMillis(60) : data.getApptReminderTime());
        final Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis() + test);

        rmtv.setText(String.format("Reminder at %s", calendar.getTime().toString()));

//todo ibrahim
        btnRM.setOnClickListener(v -> {
            ContentValues cv = new ContentValues();
            Long r = Long.valueOf(a);
            cv.put("ApptReminderTime", TimeUnit.MINUTES.toMillis(r));
            databaseHelper.updateRDB1Col(DatabaseHelper.A_TABLE_NAME, cv, DatabaseHelper.A_COLUMN_ID, apptID);

//            OneTimeWorkRequest oneTimeWorkRequest = new OneTimeWorkRequest.Builder(ReminderWorker.class)
//                    .setInitialDelay(r , TimeUnit.MINUTES)
//                    .setConstraints(Constraints.NONE)
//                    .build();
//
//            WorkManager.getInstance().enqueue(oneTimeWorkRequest);
            databaseHelper.scheduleLocalNotification(apptID);
        });

        edtRM.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                a = s.toString();
            }
        });
    }

    //set onclick for status btns if self is not hosting
    private void setStatusBtns(Appointment data, String selfUsername, String selfJid, String apptID) {
        //set onclick actions for each status button
        schelog_going.setOnClickListener(v -> {
            //only do action if not the same as selected status
            if (data.getSelf_Status() != 1) {
                if (SmackHelper.getXMPPConnection() != null && SmackHelper.getXMPPConnection().isAuthenticated()) {
                    Runnable updateStatusAction = () -> {
                        String pushMsg = selfUsername + " " + context.getString(R.string.appt_is_going);
                        String uniqueID = UUID.randomUUID().toString();

                        groupChatStanza.GroupAppointment(GroupScheLog.jid, selfJid, uniqueID,
                                "", "", "", "", "",
                                "", "", "1", "", "",
                                apptID, GroupScheLog.roomName, pushMsg, "appointment",
                                "", "4");

                        databaseHelper.outgoingApptStatus(GroupScheLog.jid, apptID, 1, uniqueID);

                        //set reminder alarm
                        databaseHelper.scheduleLocalNotification(apptID);
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

                            groupChatStanza.GroupAppointment(GroupScheLog.jid, selfJid, uniqueID,
                                    "", "", "", "", "",
                                    "", "", "2", "",
                                    "", apptID, GroupScheLog.roomName, pushMsg,
                                    "appointment", "", "4");

                            databaseHelper.outgoingApptStatus(GroupScheLog.jid, apptID, 2, uniqueID);

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

                            groupChatStanza.GroupAppointment(GroupScheLog.jid, selfJid, uniqueID,
                                    "", "", "", "", "",
                                    "", "", "3", "",
                                    "", apptID, GroupScheLog.roomName, pushMsg,
                                    "appointment", "", "4");

                            databaseHelper.outgoingApptStatus(GroupScheLog.jid, apptID, 3, uniqueID);

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

    //go to member's profile on click
    private void goToChatProfile(String jid) {
        Intent indiChatDetail = new Intent(context, IndiChatDetail.class);
        indiChatDetail.putExtra("jid", jid);

        context.startActivity(indiChatDetail);
    }

    //animate fade-away for appt noti badges
    private void animateFadeNotiBadge(View notiBadgeTextView, boolean needFade) {
        notiBadgeTextView.setVisibility(View.VISIBLE);

//        if (needFade) {
//            notiBadgeTextView.animate().alpha(0f).setDuration(2000);
//        }
    }

    private void editApptTitle(String apptTitle, String apptID) {
        uiHelper.editTextAlertDialog(context, context.getResources().getString(R.string.appt_title_edit), apptTitle, "GrpScheHolder",
                GroupScheLog.jid, apptID, true, null);
    }

    private void setDynamicHeight(GridView gridView) {
//        ListAdapter gridViewAdapter = gridView.getAdapter();
//        if (gridViewAdapter == null) {
//            // pre-condition
//            return;
//        }
//
//        View listItem = gridViewAdapter.getView(0, null, gridView);
//        listItem.measure(0, 0);
//        int totalHeight = listItem.getMeasuredHeight();
//
//        int items = gridViewAdapter.getCount();
//        int rows;
//        float x;
//        if (items > 3) {
//            x = items / 3;
//            if (items % 3 == 0) {
//                rows = (int) (x);
//            } else {
//                rows = (int) (x + 1);
//            }
//            totalHeight *= rows;
//        }
//
//        ViewGroup.LayoutParams params = gridView.getLayoutParams();
//        params.height = totalHeight;
//        gridView.setLayoutParams(params);
    }

    //function for showing appt status confirmation alertdialog
    private void apptStatusConfirmation(Runnable apptStatusAction) {
        uiHelper.dialog2Btns(context, null,
                context.getString(R.string.schelog_appt_status_msg), R.string.ok_label, R.string
                        .cancel, R.color.white, R.color.black, apptStatusAction, null, true);
    }
}
