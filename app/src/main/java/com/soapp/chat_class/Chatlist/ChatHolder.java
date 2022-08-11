package com.soapp.chat_class.Chatlist;

import android.view.View;

import com.soapp.databinding.ChatlistItemBinding;
import com.soapp.sql.room.joiners.ChatTabList;

import androidx.recyclerview.widget.RecyclerView;

/* Created by chang on 06/08/2017. */

public class ChatHolder extends RecyclerView.ViewHolder {
    //for data binding
    private ChatlistItemBinding grpItemBinding;

    ChatHolder(ChatlistItemBinding itemRecyclerBinding) {
        super(itemRecyclerBinding.getRoot());
        grpItemBinding = itemRecyclerBinding;
    }

    ChatHolder(View itemView) {
        super(itemView);
    }

    public void bind(ChatTabList data) {
        grpItemBinding.setChatTabListBD(data);
        grpItemBinding.executePendingBindings();
    }
}