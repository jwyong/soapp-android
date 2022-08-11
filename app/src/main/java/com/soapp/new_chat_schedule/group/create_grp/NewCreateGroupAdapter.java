package com.soapp.new_chat_schedule.group.create_grp;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import com.soapp.R;
import com.soapp.sql.room.entity.ContactRoster;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;

/* Created by chang on 11/08/2017. */

public class NewCreateGroupAdapter extends ListAdapter<ContactRoster, NewCreateGroupHolder> {
    private static final DiffUtil.ItemCallback<ContactRoster> DIFF_CALLBACK = new DiffUtil.ItemCallback<ContactRoster>() {
        @Override
        public boolean areItemsTheSame(
                @NonNull ContactRoster oldUser, @NonNull ContactRoster newUser) {
            // User properties may have changed if reloaded from the DB, but ID is fixed
            return oldUser.getContactRow() != null && oldUser.getContactRow().equals(newUser.getContactRow());
        }

        @Override
        public boolean areContentsTheSame(
                @NonNull ContactRoster oldUser, @NonNull ContactRoster newUser) {
            // NOTE: if you use equals, your object must properly override Object#equals()
            // Incorrectly returning false here will result in too many animations.
            return oldUser.equals(newUser);
        }
    };

    NewCreateGroupAdapter() {
        super(DIFF_CALLBACK);
    }

    @Override
    public void submitList(List<ContactRoster> list) {
        super.submitList(list);
    }

    @NonNull
    @Override
    public NewCreateGroupHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new NewCreateGroupHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.new_chatlist_item, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull NewCreateGroupHolder holder, int position) {
        holder.setData(getItem(position));
    }

    @Override
    public long getItemId(int position) {
        if (getItem(position).getContactRow() != null) {
            return getItem(position).getContactRow();
        } else {
            return super.getItemId(position);
        }
    }
}