package com.soapp.chat_class.group_chat.details.grp_mem;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import com.soapp.R;
import com.soapp.sql.room.joiners.GrpMemList;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;

/* Created by chang on 13/08/2017. */

public class GroupChatMemAdapter extends ListAdapter<GrpMemList, GroupChatMemHolder> {
    private static final DiffUtil.ItemCallback<GrpMemList> DIFF_CALLBACK = new DiffUtil.ItemCallback<GrpMemList>() {
        @Override
        public boolean areItemsTheSame(
                @NonNull GrpMemList oldUser, @NonNull GrpMemList newUser) {
            // User properties may have changed if reloaded from the DB, but ID is fixed
            return oldUser.getContactRoster().getContactRow() != null
                    && oldUser.getContactRoster().getContactRow().equals(newUser.getContactRoster().getContactRow());
        }

        @Override
        public boolean areContentsTheSame(
                @NonNull GrpMemList oldUser, @NonNull GrpMemList newUser) {
            // NOTE: if you use equals, your object must properly override Object#equals()
            // Incorrectly returning false here will result in too many animations.
            return oldUser.equals(newUser);
        }
    };
    private Context context;
    //for expanding grp mem
    private boolean showAllGrpMem = false;

    public GroupChatMemAdapter() {
        super(DIFF_CALLBACK);
    }

    @Override
    public void submitList(List<GrpMemList> list) {
        super.submitList(list);
    }

    @NonNull
    @Override
    public GroupChatMemHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        this.context = parent.getContext();

        return new GroupChatMemHolder(LayoutInflater.from(context).inflate(R.layout.chat_profile_grp_mem_item, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull GroupChatMemHolder holder, int position) {
        holder.setData(getItem(position));
    }

    @Override
    public int getItemCount() {
        int count = super.getItemCount();

        if (showAllGrpMem) {
            return count;
        } else {
            if (count > 3) {
                return 3;
            } else {
                return count;
            }
        }
    }

    @Override
    public long getItemId(int position) {
        if (getItem(position).getContactRoster().getContactRow() != null) {
            return getItem(position).getContactRoster().getContactRow();
        } else {
            return super.getItemId(position);
        }
    }

    public boolean getShowAllGrpMem() {
        return showAllGrpMem;
    }

    public void setShowAllGrpMem(boolean showAllGrpMem) {
        this.showAllGrpMem = showAllGrpMem;
    }
}
