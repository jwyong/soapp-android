package com.soapp.chat_class.group_chat.details.events;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import com.soapp.R;
import com.soapp.sql.room.entity.Appointment;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;

/* Created by jayyong on 19/05/2018. */

public class GroupChatEventsAdapter extends ListAdapter<Appointment, GroupChatEventsHolder> {
    private static final DiffUtil.ItemCallback<Appointment> DIFF_CALLBACK = new DiffUtil.ItemCallback<Appointment>() {
        @Override
        public boolean areItemsTheSame(
                @NonNull Appointment oldUser, @NonNull Appointment newUser) {
            // User properties may have changed if reloaded from the DB, but ID is fixed
            return oldUser.getApptRow() != null && oldUser.getApptRow().equals(newUser.getApptRow());
        }

        @Override
        public boolean areContentsTheSame(
                @NonNull Appointment oldUser, @NonNull Appointment newUser) {
            // NOTE: if you use equals, your object must properly override Object#equals()
            // Incorrectly returning false here will result in too many animations.
            return oldUser.equals(newUser);
        }
    };

    public GroupChatEventsAdapter() {
        super(DIFF_CALLBACK);
    }

    @Override
    public void submitList(List<Appointment> list) {
        super.submitList(list);
    }

    @NonNull
    @Override
    public GroupChatEventsHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        switch (viewType) {
            case 0: //not last view
                return new GroupChatEventsHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.chat_det_events_item,
                        parent, false));

            case 1: //last view
                return new GroupChatEventsHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.add_more_round_item,
                        parent, false));

            default:
                return new GroupChatEventsHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.chat_det_events_item,
                        parent, false));
        }
    }

    @Override
    public int getItemViewType(int position) {
        if (position == getItemCount() - 1) { //last item
            return 1;
        } else {
            return 0;
        }
    }

    @Override
    public int getItemCount() {
        return super.getItemCount() + 1;
    }

    @Override
    public void onBindViewHolder(@NonNull GroupChatEventsHolder holder, int position) {
        try {
            holder.setData(getItem(position));
        } catch (IndexOutOfBoundsException e) {
            holder.setDataNoAppt();
        }
    }
}
