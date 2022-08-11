package com.soapp.chat_class.group_chat.details.images;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import com.soapp.R;
import com.soapp.sql.room.entity.Message;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;

/* Created by jonathanhew on 15/09/2017. */
public class GroupChatImgAdapter extends ListAdapter<Message, GroupChatImgHolder> {
    private static final DiffUtil.ItemCallback<Message> DIFF_CALLBACK = new DiffUtil.ItemCallback<Message>() {
        @Override
        public boolean areItemsTheSame(
                @NonNull Message oldUser, @NonNull Message newUser) {
            // User properties may have changed if reloaded from the DB, but ID is fixed
            return oldUser.getMsgRow() != null && oldUser.getMsgRow().equals(newUser.getMsgRow());
        }

        @Override
        public boolean areContentsTheSame(
                @NonNull Message oldUser, @NonNull Message newUser) {
            // NOTE: if you use equals, your object must properly override Object#equals()
            // Incorrectly returning false here will result in too many animations.
            return oldUser.equals(newUser);
        }
    };
    public static List<Message> grpChatImgList;

    public GroupChatImgAdapter() {
        super(DIFF_CALLBACK);
    }

    @Override
    public void submitList(List<Message> list) {
        super.submitList(list);

        grpChatImgList = list;
    }

    @NonNull
    @Override
    public GroupChatImgHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new GroupChatImgHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.chat_det_img_item,
                parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull GroupChatImgHolder holder, int position) {
        holder.setData(getItem(position));
    }
}
