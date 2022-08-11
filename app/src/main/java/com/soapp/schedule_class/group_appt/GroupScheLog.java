package com.soapp.schedule_class.group_appt;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;
import android.widget.TextView;

import com.soapp.R;
import com.soapp.base.BaseActivity;
import com.soapp.chat_class.group_chat.GroupChatLog;
import com.soapp.chat_class.group_chat.details.GroupChatDetail;
import com.soapp.global.Preferences;
import com.soapp.global.UIHelper;
import com.soapp.home.Home;
import com.soapp.schedule_class.group_appt.multi_appt_slider.CircleIndicatorAdapter;
import com.soapp.schedule_class.new_appt.NewIndiExistApptActivity;
import com.soapp.sql.DatabaseHelper;
import com.soapp.sql.room.entity.Appointment;

import java.util.List;

import androidx.annotation.RequiresApi;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.PagerSnapHelper;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SnapHelper;

/* Created by Jayyong on 01/05/2018. */

public class GroupScheLog extends BaseActivity implements View.OnClickListener {
    //room name to be accessed from holder
    public static String roomName, jid;

    public static LifecycleOwner lifecycleOwner;

    int currentPosition = 0;
    int limitPosition = 0;
    private DatabaseHelper databaseHelper = DatabaseHelper.getInstance();

    private String apptID;
    private ImageView schelog_chat, schelog_new_appt, previousDot, currentDot;
    private GroupScheAdapter mAdapter = new GroupScheAdapter();
    private Preferences preferences = Preferences.getInstance();
    private TextView schelog_room_name;
    private UIHelper uiHelper = new UIHelper();

    //boolean for first time scroll to apptID
    private Boolean alreadyScrolled = false;

    List<Appointment> appointmentListOut;

    @RequiresApi(api = Build.VERSION_CODES.M)
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.scheduletab_appt_main);

        Window window = this.getWindow();
        window.setBackgroundDrawable(getResources().getDrawable(R.drawable.ly_schelog_background));

        //set status bar colour
        uiHelper.setStatusBarColor(this, false, R.color.grey10);

        //setup toolbar
        setupToolbar();
        setTitle("");

        //get intent extras (room jid and appt id)
        jid = getIntent().getStringExtra("jid");
        apptID = getIntent().getStringExtra("apptID");

        //set room name
        schelog_room_name = findViewById(R.id.schelog_room_name);
        roomName = databaseHelper.getNameFromContactRoster(jid);
        schelog_room_name.setText(roomName);
        schelog_room_name.setOnClickListener(this);

        //set function for go to chat room
        schelog_chat = findViewById(R.id.schelog_chat);
        schelog_chat.setOnClickListener(this);

        //set function for new grp appt
        schelog_new_appt = findViewById(R.id.schelog_new_appt);
        schelog_new_appt.setOnClickListener(this);

        //setup recyclerview
        RecyclerView schelog_rv = findViewById(R.id.schelog_rv);
        SnapHelper snapHelper = new PagerSnapHelper();
        snapHelper.attachToRecyclerView(schelog_rv);

        RecyclerView rv_circle_indicator_schelog = findViewById(R.id.rv_circle_indicator_schelog);

        LinearLayoutManager llm = new LinearLayoutManager(this);
        llm.setOrientation(LinearLayoutManager.HORIZONTAL);
        schelog_rv.setLayoutManager(llm);

        mAdapter.setHasStableIds(true);

        GroupScheLogVM viewModel = ViewModelProviders.of(this).get(GroupScheLogVM.class);
        viewModel.setJid(jid);
        viewModel.getData();

        viewModel.getmObservableGrpScheLog().observe(this, (List<Appointment> appointmentList) -> {
            mAdapter.submitList(appointmentList);

            limitPosition = appointmentList.size();
            appointmentListOut = appointmentList;
            CircleIndicatorAdapter cAdapter = new CircleIndicatorAdapter(limitPosition);
            LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);

            rv_circle_indicator_schelog.setLayoutManager(layoutManager);
            rv_circle_indicator_schelog.setAdapter(cAdapter);
            rv_circle_indicator_schelog.addOnChildAttachStateChangeListener(new RecyclerView.OnChildAttachStateChangeListener() {
                @Override
                public void onChildViewAttachedToWindow(View view) {
                    if (rv_circle_indicator_schelog.getChildAt(currentPosition) != null) {
                        currentDot = rv_circle_indicator_schelog.getChildAt(currentPosition).findViewById(R.id.circle_indicator_item);
                        currentDot.setImageResource(R.drawable.xml_oval_pd3);
                    }
                }

                @Override
                public void onChildViewDetachedFromWindow(View view) {

                }
            });

            //scroll to page with xx appt ID only if got apptID AND hvn't scrolled yet
            if (apptID != null && !alreadyScrolled) {
                int index = 0;
                for (Appointment list: appointmentList) {
                    if (list.getApptID().equals(apptID)) {
                        currentPosition = index;
                        schelog_rv.scrollToPosition(index);

                        //come in first time, set badge and seeing current appt
                        preferences.save(GroupScheLog.this, "seeing_current_appt", apptID);

                        //set first time scrolling boolean
                        alreadyScrolled = true;
                    }
                    index++;
                }
            }
        });

        schelog_rv.setAdapter(mAdapter);
        schelog_rv.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                switch (newState) {
                    case RecyclerView.SCROLL_STATE_DRAGGING:
                        break;

                    //after settle at xx page
                    case RecyclerView.SCROLL_STATE_IDLE:
                        //set previous position and clear indicator
                        previousDot = rv_circle_indicator_schelog.getChildAt(currentPosition).findViewById(R.id.circle_indicator_item);
                        previousDot.setImageResource(R.drawable.ic_grey_dot_42px);

                        //clear previous apptID noti badge
                        apptID = appointmentListOut.get(currentPosition).getApptID();



                        //clear current apptID noti badge
                        databaseHelper.zeroBadgeApptRoom(apptID, 1);
                        Home.scheBadgeListener();

                        //set current position and clear indicator
                        currentPosition = llm.findFirstVisibleItemPosition();
                        currentDot = rv_circle_indicator_schelog.getChildAt(currentPosition).findViewById(R.id.circle_indicator_item);
                        currentDot.setImageResource(R.drawable.xml_oval_pd3);

                        //set current apptID
                        apptID = appointmentListOut.get(currentPosition).getApptID();
                        break;

                    //letting go of finger
                    case RecyclerView.SCROLL_STATE_SETTLING:
                        break;
                }
            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
            }
        });

        //hide sche linking button if came from sche log chat linking button
        if (getIntent().hasExtra("link")) {
            schelog_chat.setVisibility(View.INVISIBLE);
        } else {
            schelog_chat.setVisibility(View.VISIBLE);
        }

        lifecycleOwner = this;
    }

    @Override
    public void onClick(View view) {
        Intent intent;
        switch (view.getId()) {
            //go to chat details
            case R.id.schelog_room_name:
                intent = new Intent(this, GroupChatDetail.class);
                intent.putExtra("jid", jid);

                startActivity(intent);
                break;

            //go to chat room
            case (R.id.schelog_chat):
                intent = new Intent(this, GroupChatLog.class);

                intent.putExtra("jid", jid);
                intent.putExtra("link", "1");

                startActivity(intent);
                break;

            case (R.id.schelog_new_appt):
                intent = new Intent(this, NewIndiExistApptActivity.class);
                intent.putExtra("jid", jid);

                startActivity(intent);
                break;
        }
    }

    @Override
    protected void onPause() {
        super.onPause();

        preferences.save(this, "seeing_current_appt", "");

        //clear current apptID noti badge
        if (apptID != null) {
            databaseHelper.zeroBadgeApptRoom(apptID, 1);
        }
        Home.scheBadgeListener();
    }

    @Override
    protected void onResume() {
        super.onResume();

        //disable UI if user blocked friend
//        if (databaseHelper.getDisabledStatus(jid) == 0) { //user haven't blocked friend
//            indi_appt_stat_spinner.setEnabled(true);
//            indi_appt_stat_spinner_down.setEnabled(true);
//            appt_title.setEnabled(true);
//            appt_date_btn.setEnabled(true);
//            appt_time_btn.setEnabled(true);
//            appt_location.setEnabled(true);
//
//            apptTitleName.setText(displayName);

//        } else { //user blocked
//            indi_appt_stat_spinner.setEnabled(false);
//            indi_appt_stat_spinner_down.setEnabled(false);
//            appt_title.setEnabled(false);
//            appt_date_btn.setEnabled(false);
//            appt_time_btn.setEnabled(false);
//            appt_location.setEnabled(false);
//
//            String blockedDisplayname = getString(R.string.blocked_user_title) + " " + displayName;
//            apptTitleName.setText(blockedDisplayname);
//        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();

//        if (getIntent().hasExtra("remoteSche")) {
//            Intent intentHome = new Intent(this, Home.class);
//            intentHome.putExtra("remoteSche", "1");
//            intentHome.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
//            startActivity(intentHome);
//        }
    }

    @Override
    protected void onDestroy() {
        preferences.save(this, "seeing_current_appt", "");

        //clear current apptID noti badge
        databaseHelper.zeroBadgeApptRoom(apptID, 1);
        Home.scheBadgeListener();

        super.onDestroy();
    }
}