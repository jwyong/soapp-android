package com.soapp.schedule_class.Schedulelist;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.soapp.R;
import com.soapp.sql.room.joiners.Applist;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;

/* Created by Jayyong on 20/04/2018. */

public class ScheduleAdapter extends ListAdapter<Applist, ScheduleHolder> {
    private static final DiffUtil.ItemCallback<Applist> DIFF_CALLBACK = new DiffUtil.ItemCallback<Applist>() {
        @Override
        public boolean areItemsTheSame(
                @NonNull Applist oldUser, @NonNull Applist newUser) {
            // User properties may have changed if reloaded from the DB, but ID is fixed
            return oldUser.getAppointment().getApptRow().equals(newUser.getAppointment().getApptRow());
        }

        @Override
        public boolean areContentsTheSame(
                @NonNull Applist oldUser, @NonNull Applist newUser) {
            // NOTE: if you use equals, your object must properly override Object#equals()
            // Incorrectly returning false here will result in too many animations.
            return oldUser.equals(newUser);
        }
    };

    ScheduleAdapter() {
        super(DIFF_CALLBACK);
    }

    @Override
    public void submitList(List<Applist> list) {
        super.submitList(list);
    }

    @NonNull
    @Override
    public ScheduleHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view;
        TextView tv_month;
        switch (viewType) {
            case 0: //[DONE] hosting with date (need new design, use GOING for now)
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.schedulelist_item_going_date_cl, parent, false);
                break;

            case 1: //[DONE] going with date
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.schedulelist_item_going_date_cl, parent, false);
                break;

            case 2: //[DONE] undecided with date
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.schedulelist_item_invited_date_cl, parent, false);
                break;

            case 3: //[DONE] not going with date
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.schedulelist_item_not_going_title_date, parent, false);
                break;

            case 100: //[DONE] hosting NO date
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.schedulelist_item_going_no_date, parent, false);
                break;

            case 101: //[DONE] going NO date
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.schedulelist_item_going_no_date, parent, false);
                break;

            case 102: //[DONE] undecided NO date
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.schedulelist_item_invited_no_date_cl, parent, false);
                break;

            case 103: //[DONE] not going NO date
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.schedulelist_item_not_going_title_no_date, parent, false);
                break;

            case 200: //[DONE] going with month
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.schedulelist_item_going_date_cl, parent, false);
                tv_month = view.findViewById(R.id.tv_month);
                tv_month.setVisibility(View.VISIBLE);
                break;

            case 201: //[DONE] going with month
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.schedulelist_item_going_date_cl, parent, false);
                tv_month = view.findViewById(R.id.tv_month);
                tv_month.setVisibility(View.VISIBLE);
                break;

            case 202: //[DONE] undecided
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.schedulelist_item_invited_date_cl, parent, false);
                tv_month = view.findViewById(R.id.tv_month);
                tv_month.setVisibility(View.VISIBLE);
                break;

            case 203: //[DONE] not going
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.schedulelist_item_not_going_title_date, parent, false);
                tv_month = view.findViewById(R.id.tv_month);
                tv_month.setVisibility(View.VISIBLE);
                break;

            case 666: //empty view for not going duplications
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.empty_view, parent, false);
                break;

            case 999: //last view
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.last_view, parent, false);
                break;

            default: //default = hosting with date
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.schedulelist_item_going_date_cl, parent, false);
                break;
        }
        return new ScheduleHolder(view);
    }

    @Override
    public int getItemViewType(int position) {
        if (position == getItemCount() - 1) { //if last position, add empty view with instructions
            return 999;
        }

        int selfStatus = getItem(position).getAppointment().getSelf_Status();
        int lastSelfStatus = -1;

        long currentApptDate = getItem(position).getAppointment().getApptDate();
        long lastApptDate = -100;

        DateFormat monthFormat = new SimpleDateFormat("MMM", Locale.ENGLISH);
        String currentMonth = monthFormat.format(currentApptDate);
        String lastMonth = monthFormat.format(lastApptDate);

        if (position - 1 >= 0) { //get last position's variables if current position is not first
            lastApptDate = getItem(position - 1).getAppointment().getApptDate();
            lastSelfStatus = getItem(position - 1).getAppointment().getSelf_Status();
            lastMonth = monthFormat.format(lastApptDate);
        }

        if (lastApptDate != currentApptDate) { //NOT same date with previous appt, show date
            if (!currentMonth.equals(lastMonth)) {
                return selfStatus + 200;
            }
            return selfStatus;

        } else { //same date with previous appt, DON'T show date
            if (selfStatus == 3 && selfStatus == lastSelfStatus) { //not going and repeated
                return 666;
            }
            return selfStatus + 100;
        }
    }

    @Override
    public int getItemCount() {
        return super.getItemCount() + 1;
    }

    @Override
    public long getItemId(int position) {
        if (position == getItemCount() - 1) { //if last position, do nothing
            return position;
        }

        return getItem(position).getAppointment().getApptRow();
    }

    @Override
    public void onBindViewHolder(@NonNull ScheduleHolder holder, int position) {
        try {
            holder.setData(getItem(position));
        } catch (IndexOutOfBoundsException e) {
            holder.setDataLastItem();
        }
    }
}