package com.soapp.chat_class.single_chat.details.grps;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import com.soapp.R;
import com.soapp.sql.room.entity.ContactRoster;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;

/* Created by chang on 13/08/2017. */

public class IndiChatGrpsAdapter extends ListAdapter<ContactRoster, IndiChatGrpsHolder> {
    private static final DiffUtil.ItemCallback<ContactRoster> DIFF_CALLBACK = new DiffUtil.ItemCallback<ContactRoster>() {
        @Override
        public boolean areItemsTheSame(
                @NonNull ContactRoster oldUser, @NonNull ContactRoster newUser) {
            // User properties may have changed if reloaded from the DB, but ID is fixed
            return oldUser.getContactRow() != null
                    && oldUser.getContactRow().equals(newUser.getContactRow());
        }

        @Override
        public boolean areContentsTheSame(
                @NonNull ContactRoster oldUser, @NonNull ContactRoster newUser) {
            // NOTE: if you use equals, your object must properly override Object#equals()
            // Incorrectly returning false here will result in too many animations.
            return oldUser.equals(newUser);
        }
    };
    private Context context;
    //for expanding grp mem
    private boolean showAllCommonGrps = false;

    public IndiChatGrpsAdapter() {
        super(DIFF_CALLBACK);
    }

    @Override
    public void submitList(List<ContactRoster> list) {
        super.submitList(list);
    }

    @NonNull
    @Override
    public IndiChatGrpsHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        this.context = parent.getContext();

        return new IndiChatGrpsHolder(LayoutInflater.from(context).inflate(R.layout.chat_profile_common_grps_item, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull IndiChatGrpsHolder holder, int position) {
        holder.setData(getItem(position));
    }

    @Override
    public int getItemCount() {
        int count = super.getItemCount();
        if (showAllCommonGrps) {
            return count;
        } else {
            if (count > 4) {
                return 4;
            } else {
                return count;
            }
        }
    }

    @Override
    public long getItemId(int position) {
        if (getItem(position).getContactRow() != null) {
            return getItem(position).getContactRow();
        } else {
            return super.getItemId(position);
        }
    }

    public boolean getShowAllCommonGrps() {
        return showAllCommonGrps;
    }

    public void setShowAllCommonGrps(boolean showAllCommonGrps) {
        this.showAllCommonGrps = showAllCommonGrps;
    }
}
