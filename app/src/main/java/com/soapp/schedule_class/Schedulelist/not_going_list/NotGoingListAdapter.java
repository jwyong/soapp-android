package com.soapp.schedule_class.Schedulelist.not_going_list;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.soapp.R;
import com.soapp.sql.room.joiners.Applist;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;

/* Created by Jayyong on 20/04/2018. */

public class NotGoingListAdapter extends ListAdapter<Applist, NotGoingListHolder> {
    private static final DiffUtil.ItemCallback<Applist> DIFF_CALLBACK = new DiffUtil.ItemCallback<Applist>() {
        @Override
        public boolean areItemsTheSame(
                @NonNull Applist oldUser, @NonNull Applist newUser) {
            // User properties may have changed if reloaded from the DB, but ID is fixed
            return oldUser.getAppointment().getApptJid().equals(newUser.getAppointment().getApptJid());
        }

        @Override
        public boolean areContentsTheSame(
                @NonNull Applist oldUser, @NonNull Applist newUser) {
            // NOTE: if you use equals, your object must properly override Object#equals()
            // Incorrectly returning false here will result in too many animations.
            return oldUser.equals(newUser);
        }
    };

    public NotGoingListAdapter() {
        super(DIFF_CALLBACK);
    }

    @Override
    public void submitList(List<Applist> list) {
        super.submitList(list);
    }

    @NonNull
    @Override
    public NotGoingListHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.schedulelist_item_not_going_no_date, parent, false);

        return new NotGoingListHolder(view);
    }

    @Override
    public long getItemId(int position) {
        return getItem(position).getAppointment().getApptRow();
    }

    @Override
    public void onBindViewHolder(@NonNull NotGoingListHolder holder, int position) {
        holder.setData(getItem(position));
    }

    @Override
    public int getItemCount() {
        return super.getItemCount();
    }
}