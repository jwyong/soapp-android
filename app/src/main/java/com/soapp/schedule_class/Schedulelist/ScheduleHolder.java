package com.soapp.schedule_class.Schedulelist;

import android.animation.ValueAnimator;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.soapp.R;
import com.soapp.chat_class.group_chat.details.GroupChatDetail;
import com.soapp.chat_class.single_chat.details.IndiChatDetail;
import com.soapp.global.GlobalVariables;
import com.soapp.global.Preferences;
import com.soapp.global.UIHelper;
import com.soapp.home.Home;
import com.soapp.schedule_class.Schedulelist.not_going_list.NotGoingListAdapter;
import com.soapp.schedule_class.group_appt.GroupScheLog;
import com.soapp.schedule_class.single_appt.IndiScheLog;
import com.soapp.setup.Soapp;
import com.soapp.sql.DatabaseHelper;
import com.soapp.sql.room.joiners.Applist;
import com.soapp.xmpp.GroupChatHelper.GroupChatStanza;
import com.soapp.xmpp.SingleChatHelper.SingleChatStanza;
import com.soapp.xmpp.SmackHelper;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import io.github.rockerhieu.emojicon.EmojiconTextView;

/* Created by Jayyong on 20/04/2018. */

public class ScheduleHolder extends RecyclerView.ViewHolder {
    int oldheight, txtScheHeight, oldheight1, oldheight2, notGoingCount;

    //basics
    private DatabaseHelper databaseHelper = DatabaseHelper.getInstance();
    private Context context;
    private Preferences preferences = Preferences.getInstance();
    private UIHelper uiHelper = new UIHelper();
    private TextView schelist_appt_date, schelist_appt_day, schelist_appt_time, schelist_appt_loc,
             tv_month,schelist_noti_badge;
    private EmojiconTextView schelist_displayname, schelist_appt_title;
    private ImageView schelist_going_tick, schelist_not_going_cross;

    private View emptyView;
    private TextView schelist_not_going_number;
    private ConstraintLayout not_going_main_cl, notGoing_main_Ly, schelist_not_going_init;
    private LinearLayout not_going_ll;

    //not going
    private RecyclerView schelist_not_going_rv;
    private Boolean isExpanded = false;

    ScheduleHolder(View itemView) {
        super(itemView);
        //if empty_view, return
        emptyView = itemView.findViewById(R.id.empty_view);
        if (emptyView != null) {
            return;
        }

        //set context
        context = itemView.getContext();

        //set ids
        schelist_displayname = itemView.findViewById(R.id.schelist_displayname);
        schelist_appt_title = itemView.findViewById(R.id.schelist_appt_title);
        schelist_appt_date = itemView.findViewById(R.id.schelist_appt_date);
        schelist_appt_day = itemView.findViewById(R.id.schelist_appt_day);
        schelist_appt_time = itemView.findViewById(R.id.schelist_appt_time);
        schelist_appt_loc = itemView.findViewById(R.id.schelist_appt_loc);
        schelist_noti_badge = itemView.findViewById(R.id.schelist_noti_badge);
        tv_month = itemView.findViewById(R.id.tv_month);

        //for invited/undecided
        schelist_going_tick = itemView.findViewById(R.id.schelist_going_tick);
        schelist_not_going_cross = itemView.findViewById(R.id.schelist_not_going_cross);

        //for not going
        schelist_not_going_number = itemView.findViewById(R.id.schelist_not_going_number);
        not_going_main_cl = itemView.findViewById(R.id.not_going_main_cl);
        not_going_ll = itemView.findViewById(R.id.not_going_ll);
        schelist_not_going_init = itemView.findViewById(R.id.schelist_not_going_init);
    }

    //for last item
    public void setDataLastItem() {
    }

    public void setData(final Applist data) {
        //if empty_view, return
        emptyView = itemView.findViewById(R.id.empty_view);
        if (emptyView != null) {
            return;
        }

        //set final jid and apptID
        final String jid = data.getAppointment().getApptJid();
        final String apptID = data.getAppointment().getApptID();

        //appt date (only need set date if not null, means first)
        if (schelist_appt_date != null) {

            long apptDate = data.getAppointment().getApptDate();
            DateFormat dateFormat = new SimpleDateFormat("dd", Locale.ENGLISH);

            schelist_appt_date.setText(dateFormat.format(apptDate));

            if (tv_month != null) {
                DateFormat monthFormat = new SimpleDateFormat("MMM", Locale.ENGLISH);
                tv_month.setText(monthFormat.format(apptDate).toUpperCase());
            }

            //appt day (Mon, Tue...)
            DateFormat dayFormat = new SimpleDateFormat("E", Locale.ENGLISH);
            schelist_appt_day.setText(dayFormat.format(apptDate).toUpperCase());
        }

        //if not going, no need set data
        int selfStatus = data.getAppointment().getSelf_Status();
        if (selfStatus == 3) {
            //set number and events (singular or plural)
            long apptDate = data.getAppointment().getApptDate();
            int apptNGNumber = databaseHelper.getApptNGSameDateCount(apptDate);

            String ngEventNumber;
            if (apptNGNumber > 1) { //plural
                ngEventNumber = " " + apptNGNumber + " " + context.getString(R.string.sche_notgoing_events);
            } else {
                ngEventNumber = " " + apptNGNumber + " " + context.getString(R.string.sche_notgoing_event);
            }
            schelist_not_going_number.setText(ngEventNumber);

            //set rv to visible if already expanded before
            if (!isExpanded) { //collapsed
                //populate recyclerview for not going details
                final NotGoingListAdapter notGoingListAdapter = new NotGoingListAdapter();
                notGoingListAdapter.setHasStableIds(true);

                final LinearLayoutManager llm = new LinearLayoutManager(context);
                llm.setOrientation(LinearLayoutManager.VERTICAL);

                schelist_not_going_rv = itemView.findViewById(R.id.schelist_not_going_rv);
                schelist_not_going_rv.setLayoutManager(llm);
                schelist_not_going_rv.setItemAnimator(null);

                final LiveData<List<Applist>> list = Soapp.getDatabase().selectQuery().getScheTabNotGoing(data.getAppointment().getApptDate());
                list.observe((LifecycleOwner) context, new Observer<List<Applist>>() {
                    @Override
                    public void onChanged(@Nullable List<Applist> applists) {
                        notGoingListAdapter.submitList(applists);

                        //no animation


                        //for animation
//                    //if first time, expand
//                    if (schelist_ng_height.isEnabled()) {
//                        //set enabled to false as boolean (for first time)
//                        schelist_ng_height.setEnabled(false);
//
//                        //get final height for whole list
//                        schelist_not_going_rv.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
//                    }
//
//                    //set value to textview
//                    int totalHeight = schelist_not_going_rv.getMeasuredHeight();
//                    schelist_ng_height.setText(String.valueOf(totalHeight));
//
//                    //if collapsed, set height to 1 ("selectable" = expanded)
//                    ViewGroup.LayoutParams params = schelist_not_going_rv.getLayoutParams();
//                    if (!schelist_ng_height.isTextSelectable()) {
//                        params.height = 1;
//
//                    } else { //expanded, set height to new height
//                        params.height = totalHeight;
//
//                    }
//                    schelist_not_going_rv.setLayoutParams(params);
                    }
                });
                schelist_not_going_rv.setAdapter(notGoingListAdapter);
            }

            //set on click listener for toggling details
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    //no animation (for now)
                    if (schelist_not_going_rv.getVisibility() == View.GONE) {
                        schelist_not_going_rv.setVisibility(View.VISIBLE);

                        //set "expanded" boolean to true
                        isExpanded = true;
                    } else {
                        schelist_not_going_rv.setVisibility(View.GONE);

                        //set "expanded" boolean to false
                        isExpanded = false;
                    }


                    //for animation
//                    int totalHeight = Integer.parseInt(schelist_ng_height.getText().toString());
//
//                    //collapsed, "selectable" = expanded
//                    if (!schelist_ng_height.isTextSelectable()) {
//                        //set "expanded" boolean to true
//                        schelist_ng_height.setTextIsSelectable(true);
//
//                        //animate expand
//                        expandRVAnimated(schelist_not_going_rv, 1, totalHeight).start();
//
//                    } else { //expanded
//                        //set "expanded" boolean to false
//                        schelist_ng_height.setTextIsSelectable(false);
//
//                        //animate collapse
//                        expandRVAnimated(schelist_not_going_rv, totalHeight, 1).start();
//
////                        toggleDescription(totalHeight, initialHeight);
//
//
//                    }
                }
            });
            return;

        } else if (selfStatus == 2) { //invited
            //action for "going"
            schelist_going_tick.setOnClickListener(v -> {
                //only do action if not the same as selected status
                if (SmackHelper.getXMPPConnection() != null && SmackHelper.getXMPPConnection().isAuthenticated()) {
                    Runnable updateStatusAction = new Runnable() {
                        @Override
                        public void run() {
                            String selfUsername = preferences.getValue(context, GlobalVariables.STRPREF_USERNAME);
                            String pushMsg = selfUsername + " " + context.getString(R.string.appt_is_going);
                            String uniqueID = UUID.randomUUID().toString();

                            if (jid.length() == 12) { //indi
                                new SingleChatStanza().SoappAppointmentStanza(jid, pushMsg, "",
                                        "", "", "", "", "", "", "",
                                        String.valueOf(1), "", uniqueID, "", "", apptID,
                                        selfUsername, "appointment", "0", "4");
                            } else {
                                String selfJid = preferences.getValue(context, GlobalVariables.STRPREF_USER_ID);
                                String roomName = schelist_displayname.getText().toString().trim();

                                new GroupChatStanza().GroupAppointment(jid, selfJid, uniqueID, "", "",
                                        "", "", "", "", "", "1",
                                        "", "", apptID, roomName, pushMsg,
                                        "appointment", "", "4");
                            }
                            databaseHelper.outgoingApptStatus(jid, apptID, 1, uniqueID);

                            //set reminder alarm
                            databaseHelper.scheduleLocalNotification(apptID);
                        }
                    };

                    apptStatusConfirmation(updateStatusAction);

                } else {
                    Toast.makeText(context, R.string.xmpp_waiting_connection, Toast.LENGTH_SHORT).show();
                }
            });

            schelist_not_going_cross.setOnClickListener(v -> {
                if (SmackHelper.getXMPPConnection() != null && SmackHelper.getXMPPConnection().isAuthenticated()) {
                    Runnable updateStatusAction = new Runnable() {
                        @Override
                        public void run() {
                            String selfUsername = preferences.getValue(context, GlobalVariables.STRPREF_USERNAME);
                            String pushMsg = selfUsername + " " + context.getString(R.string.appt_is_not_going);
                            String uniqueID = UUID.randomUUID().toString();

                            if (jid.length() == 12) { //indi
                                new SingleChatStanza().SoappAppointmentStanza(jid, pushMsg, "",
                                        "", "", "", "", "", "", "",
                                        String.valueOf(3), "", uniqueID, "", "", apptID,
                                        selfUsername, "appointment", "0", "4");
                            } else {
                                String selfJid = preferences.getValue(context, GlobalVariables.STRPREF_USER_ID);
                                String roomName = schelist_displayname.getText().toString().trim();

                                new GroupChatStanza().GroupAppointment(jid, selfJid, uniqueID, "", "",
                                        "", "", "", "", "", "3",
                                        "", "", apptID, roomName, pushMsg,
                                        "appointment", "", "4");
                            }
                            databaseHelper.outgoingApptStatus(jid, apptID, 3, uniqueID);

                            //cancel reminder alarm
                            databaseHelper.cancelPendingAlarm(apptID);
                        }
                    };

                    apptStatusConfirmation(updateStatusAction);
                } else {
                    Toast.makeText(context, R.string.xmpp_waiting_connection, Toast.LENGTH_SHORT).show();
                }
            });
        }

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
        } else {
            schelist_appt_loc.setText(R.string.appt_title);
        }

        //notification badge (currently only showing 1)
        int badgeInt = data.getAppointment().getApptNotiBadge();
        if (badgeInt > 0) {
            schelist_noti_badge.setVisibility(View.VISIBLE);
            Home.scheBadgeListener();
        } else {
            schelist_noti_badge.setVisibility(View.GONE);
        }

        //set on click listener for going to sche logs
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

        //only set longclick listener if NOT not going
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
                    null, null, null, null, null, null);

            return true;
        });
    }

    //function for showing appt status confirmation alertdialog
    private void apptStatusConfirmation(Runnable apptStatusAction) {
        uiHelper.dialog2Btns(context, context.getString(R.string.schelog_appt_status_title),
                context.getString(R.string.schelog_appt_status_msg), R.string.ok_label, R.string
                        .cancel, R.color.white, R.color.black,
                apptStatusAction, null, true);
    }

    private ValueAnimator expandRVAnimated(RecyclerView view, int startHeight, int endHeight) {
        // check recycle height
        ValueAnimator animator = ValueAnimator.ofInt(startHeight, endHeight);

        // animation start & end
        animator.addUpdateListener(animation -> {
            int listUp = (Integer) animation.getAnimatedValue();

            ViewGroup.LayoutParams params = view.getLayoutParams();
            params.height = listUp;
            view.setLayoutParams(params);
        });

        animator.setDuration(500);
        return animator;
    }
}