package com.soapp.schedule_class.Schedulelist;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SearchView;
import android.widget.TextView;

import com.soapp.R;
import com.soapp.SoappModel.ApptCalendarModel;
import com.soapp.global.DateTime.DateTimePickerHelper.DateHelper.CalendarDay;
import com.soapp.global.DateTime.DateTimePickerHelper.DateHelper.CalendarMode;
import com.soapp.global.DateTime.DateTimePickerHelper.DateHelper.MaterialCalendarView;
import com.soapp.global.DateTime.DateTimePickerHelper.decorators.EventDecorator;
import com.soapp.global.DateTime.DateTimePickerHelper.decorators.EventDecorator2;
import com.soapp.global.DateTime.DateTimePickerHelper.decorators.EventDecorator3;
import com.soapp.global.DateTime.DateTimePickerHelper.decorators.EventDecoratorCurrentDate;
import com.soapp.global.HomeTabHelper;
import com.soapp.global.Preferences;
import com.soapp.global.UIHelper;
import com.soapp.home.Home;
import com.soapp.sql.DatabaseHelper;
import com.soapp.sql.room.joiners.Applist;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashSet;
import java.util.List;

import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.RecyclerView;

/* Created by Jayyong on 20/04/2018. */

public class ScheduleTab extends Fragment implements SearchView.OnQueryTextListener, SearchView.OnCloseListener,
        View.OnClickListener, Home.ScheTabComm {
    //basics
    private View view;
    private DatabaseHelper databaseHelper = DatabaseHelper.getInstance();
    private Preferences preferences = Preferences.getInstance();

    // material calendar
    public static MaterialCalendarView ScheduleCalendar;
    public static ImageView swapUp_Down;
    public UIHelper uiHelper = new UIHelper();
    ScheduleVM scheTabVM;

    private LinearLayout start_new_sche;
    private TextView start_new_sche_msg, clear_sche_noti_btn;

    //recyclerview
    private RecyclerView scheduleRecycleView;
    private ScheduleAdapter mAdapter;
    private CenterLayoutManager llm;

    private Activity activity;
    private CardView new_sche_btn;
    private SearchView searchView;

    //for first time scroll to top
    private boolean isFirstTime = true;
    private CalendarMode calendarMode;

    //for search bar
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setHasOptionsMenu(true);

        activity = getActivity();
        mAdapter = new ScheduleAdapter();

        scheTabVM = ViewModelProviders.of(this).get(ScheduleVM.class);
        scheTabVM.init();
        scheTabVM.addSource();
        scheTabVM.getmObservableAppointment().observe(this, appointmentList -> {
            mAdapter.submitList(appointmentList);

            if (appointmentList != null) {
                //show xml if no items
                if (appointmentList.size() == 0) { //no items
                    if (scheTabVM.getIsSearching().getValue()) { //searching
                        start_new_sche.setEnabled(false);
                        start_new_sche_msg.setText(R.string.no_search_results);
                    } else { //not searching
                        start_new_sche.setEnabled(true);
                        start_new_sche.setOnClickListener(ScheduleTab.this);
                        start_new_sche_msg.setText(R.string.start_new_appt);
                    }
                    start_new_sche.setVisibility(View.VISIBLE);

                } else { //got items
                    start_new_sche.setVisibility(View.GONE);
                }

                //populate schedule calendar async
                List<ApptCalendarModel> apptCalList = new ArrayList<>();
                apptCalList.add(0, new ApptCalendarModel(databaseHelper.getHostingGoingCDList(),
                        databaseHelper.getApptCDList(2), databaseHelper.getApptCDList(3)));

                if (ScheduleCalendar == null) {
                    ScheduleCalendar = view.findViewById(R.id.ScheduleCalendar);
                }

                ScheduleCalendar.removeDecorators();
                HashSet<CalendarDay> hashSetTodayCal = new HashSet<>();
                hashSetTodayCal.add(CalendarDay.today());
                ScheduleCalendar.setShowOtherDates(MaterialCalendarView.SHOW_OUT_OF_RANGE);

                //today background
                ScheduleCalendar.addDecorator(new EventDecoratorCurrentDate(hashSetTodayCal,
                        getResources().getDrawable(R.drawable.xml_oval_border_pd3)));

                //set background for selection
                ScheduleCalendar.setSelectionColor(getResources().getColor(R.color.primaryDark3));

                ScheduleCalendar.addDecorator(new EventDecorator(apptCalList.get(0).getGoingArray(),
                        getResources().getColor(R.color.primaryDark4)));
                ScheduleCalendar.addDecorator(new EventDecorator2(apptCalList.get(0).getUndecArray(),
                        getResources().getColor(R.color.orange)));
                ScheduleCalendar.addDecorator(new EventDecorator3(apptCalList.get(0).getNotGoingArray(),
                        getResources().getColor(R.color.red)));

                ScheduleCalendar.setCurrentMonthByDate(CalendarDay.today());
                ScheduleCalendar.setCurrentDate(CalendarDay.today(), true);

                ScheduleCalendar
                        .state()
                        .edit()
                        .setFirstDayOfWeek(Calendar.SUNDAY)
                        .setMinimumDate(CalendarDay.today())
                        .commit();

                ScheduleCalendar.setOnDateChangedListener((widget, date, selected) -> {
                    boolean ok = databaseHelper.checkIfDateHasAppt(date.getDate().getTime());
                    ScheduleCalendar.setCurrentMonthByDate(date);
                    ScheduleCalendar.updateUi();
                    if (ok) {
                        int index = 0;
                        for (Applist list : appointmentList) {
                            if (list.getAppointment().getApptDate() == date.getDate().getTime()) {
                                scheduleRecycleView.smoothScrollToPosition(index);
                                break;
                            }
                            index++;
                        }
                    } else {
                        new HomeTabHelper().startNewSche(getContext(), date.getDate().getTime());
                    }
                });

                //first time scroll to top
                if (isFirstTime) {
                    scheduleRecycleView.scrollToPosition(0);
                    isFirstTime = false;
                } else {
                    if (llm.findFirstVisibleItemPosition() == 0) { //only scroll to top if user is scrolled to top
                        if (searchView.isIconified()) { //only smooth scroll when NOT searching
                            scheduleRecycleView.smoothScrollToPosition(-10);
                        }
                    }
                }
            }
        });
        scheTabVM.getIsSearching().observe(this, aBoolean -> scheTabVM.init());
        scheTabVM.getSearchString().observe(this, s -> scheTabVM.init());
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        ((Home) context).scheTabComm = this;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle
            savedInstanceState) {
        if (view == null) {
            view = inflater.inflate(R.layout.schedulelist, container, false);

            // setting the statusbar
            ImageView statusBar = view.findViewById(R.id.statusBar);
            ConstraintLayout.LayoutParams statusParams =
                    new ConstraintLayout.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            new UIHelper().getStatusBarHeight(getActivity()));
            statusBar.setLayoutParams(statusParams);
            statusBar.setBackgroundColor(getResources().getColor(R.color.grey8));
            //** end setting statusbar **//

            //reset seeing current appt
            preferences.save(getContext(), "seeing_current_appt", "");

            scheduleRecycleView = view.findViewById(R.id.schedule_list);
            ScheduleCalendar = view.findViewById(R.id.ScheduleCalendar);
//            swapUp_Down = view.findViewById(R.id.swapUp_Down);

            start_new_sche = view.findViewById(R.id.start_new_sche);
            start_new_sche_msg = view.findViewById(R.id.start_new_sche_msg);
            new_sche_btn = view.findViewById(R.id.new_sche_btn);
            searchView = view.findViewById(R.id.search_sche_btn);
            new_sche_btn.setOnClickListener(this);
            searchView.setOnSearchClickListener(this);

            llm = new CenterLayoutManager(this.getContext());
            llm.setOrientation(RecyclerView.VERTICAL);

            mAdapter.setHasStableIds(true);

            scheduleRecycleView.setLayoutManager(llm);
            scheduleRecycleView.setItemAnimator(null);
            scheduleRecycleView.setAdapter(mAdapter);
            scheduleRecycleView.setItemViewCacheSize(200);

            //for dev show/hide schetab clear btn
            if (!preferences.getValue(getContext(), "devScheClear").equals("nil")) {
                clear_sche_noti_btn = view.findViewById(R.id.clear_sche_noti_btn);
                clear_sche_noti_btn.setVisibility(View.VISIBLE);
                clear_sche_noti_btn.setOnClickListener(this);
            }
        }
        return view;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            //plus btn (new appt)
            case (R.id.start_new_sche):
            case (R.id.new_sche_btn):
                new HomeTabHelper().startNewSche(getContext(), 0);
                break;

            //search btn
            case (R.id.search_sche_btn):
                searchView.setOnQueryTextListener(this);
                searchView.setOnCloseListener(this);
                scheTabVM.removeSource();
                break;

            //clear appt noti badge btn
            case R.id.clear_sche_noti_btn:
                Runnable actionClearApptNoti = () -> {
                    databaseHelper.zeroBadgeApptALL();
                };

                uiHelper.dialog2Btns(getContext(), getString(R.string.clear_appt_noti_title),
                        getString(R.string.clear_appt_noti_msg), R.string.ok_label, R.string
                                .cancel,
                        R.color.white, R.color.black, actionClearApptNoti, null, true);
                break;
        }
    }

    //for search bar
    @Override
    public boolean onQueryTextSubmit(String query) {
        return false;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        if (newText.trim().length() == 0) {
            scheTabVM.setIsSearching(false);
            //populate back
        } else {
            scheTabVM.setIsSearching(true);
            scheTabVM.setSearchString(newText.toLowerCase());
        }

        return true;
    }

    @Override
    public boolean onClose() {
        scheTabVM.setIsSearching(false);
        scheTabVM.addSource();

        llm.scrollToPosition(0);
        return false;
    }

    @Override
    public boolean isScheSearchIconified() {
        if (searchView != null) {
            return searchView.isIconified();
        } else { //return true (iconified = not searching) since no searchView
            return true;
        }
    }

    @Override
    public void closeScheTabSearch() {
        searchView.setIconified(true);
        searchView.clearFocus();

        llm.scrollToPosition(0);
    }

    @Override
    public void onDestroy() {
        ScheduleCalendar = null;

        super.onDestroy();
    }
}