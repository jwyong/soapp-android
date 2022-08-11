package com.soapp.chat_class.Chatlist;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import com.soapp.R;
import com.soapp.chat_class.group_chat.GroupChatHolder;
import com.soapp.databinding.ChatlistItemBinding;
import com.soapp.sql.room.joiners.ChatTabList;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;

/* Created by chang on 06/08/2017. */

public class ChatAdapter extends ListAdapter<ChatTabList, ChatHolder> {
    private static final DiffUtil.ItemCallback<ChatTabList> DIFF_CALLBACK = new DiffUtil.ItemCallback<ChatTabList>() {
        private static final String TAG = "JAY";

        @Override
        public boolean areItemsTheSame(
                @NonNull ChatTabList oldUser, @NonNull ChatTabList newUser) {
            // User properties may have changed if reloaded from the DB, but ID is fixed

            return oldUser.getChatList().getChatJid().equals(newUser.getChatList().getChatJid());
        }

        @Override
        public boolean areContentsTheSame(
                @NonNull ChatTabList oldUser, @NonNull ChatTabList newUser) {
            // NOTE: if you use equals, your object must properly override Object#equals()
            // Incorrectly returning false here will result in too many animations.

            return oldUser.getChatList().equals(newUser.getChatList());
        }
    };

    ChatAdapter() {
        super(DIFF_CALLBACK);
    }

    @NonNull
    @Override
    public ChatHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == -1) {
            return new ChatHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout
                    .empty_view, parent, false));
        }

        return new ChatHolder(
                ChatlistItemBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
    }

    @Override
    public int getItemCount() {
        return super.getItemCount() + 1;
    }

    @Override
    public int getItemViewType(int position) {
        if (position == getItemCount() -1) { //if last position return empty view
            return -1;
        }
        return super.getItemViewType(position);
    }

    @Override
    public long getItemId(int position) {
        if (position == getItemCount() - 1) { //if last position, do nothing
            return position;
        }

        return getItem(position).getChatList().getChatRow();
    }

    @Override
    public void submitList(List<ChatTabList> list) {
        super.submitList(list);
    }

    @Override
    public void onBindViewHolder(@NonNull ChatHolder holder, int position) {
        try {
            holder.bind(getItem(position));
        } catch (IndexOutOfBoundsException ignore) {
        }
    }
}