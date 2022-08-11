package com.soapp.new_chat_schedule.group;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import com.soapp.R;
import com.soapp.sql.room.entity.ContactRoster;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;

/* Created by chang on 30/07/2017. */

class NewGrpChatAdapter extends ListAdapter<ContactRoster, NewGrpChatHolder> {
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

    NewGrpChatAdapter() {
        super(DIFF_CALLBACK);
    }

    @Override
    public void submitList(List<ContactRoster> list) {
        super.submitList(list);
    }

    @NonNull
    @Override
    public NewGrpChatHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new NewGrpChatHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.new_g_chat_item, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull NewGrpChatHolder holder, int position) {
        holder.setData(getItem(position));
    }

    @Override
    public long getItemId(int position) {
        return getItem(position).getContactRow();
    }
}
