package com.soapp.new_chat_schedule.group;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import com.soapp.R;
import com.soapp.sql.room.joiners.ChatTabList;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;

/* Created by Soapp on 02/01/2018. */

class ExistingGrpAdapter extends ListAdapter<ChatTabList, ExistingGrpHolder> {
    private static final DiffUtil.ItemCallback<ChatTabList> DIFF_CALLBACK = new DiffUtil.ItemCallback<ChatTabList>() {
        @Override
        public boolean areItemsTheSame(
                @NonNull ChatTabList oldUser, @NonNull ChatTabList newUser) {
            // User properties may have changed if reloaded from the DB, but ID is fixed
            return oldUser.getChatList().getChatRow() != null && oldUser.getChatList().getChatRow().equals(newUser.getChatList().getChatRow());
        }

        @Override
        public boolean areContentsTheSame(
                @NonNull ChatTabList oldUser, @NonNull ChatTabList newUser) {
            // NOTE: if you use equals, your object must properly override Object#equals()
            // Incorrectly returning false here will result in too many animations.
            return oldUser.equals(newUser);
        }
    };

    ExistingGrpAdapter() {
        super(DIFF_CALLBACK);
    }

    @Override
    public void submitList(List<ChatTabList> list) {
        super.submitList(list);
    }

    @NonNull
    @Override
    public ExistingGrpHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ExistingGrpHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.share_item, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ExistingGrpHolder holder, int position) {
        holder.setData(getItem(position));
    }

    @Override
    public long getItemId(int position) {
        return getItem(position).getChatList().getChatRow();
    }
}
