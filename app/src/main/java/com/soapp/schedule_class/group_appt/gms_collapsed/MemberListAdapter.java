package com.soapp.schedule_class.group_appt.gms_collapsed;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import com.soapp.R;
import com.soapp.sql.room.entity.ContactRoster;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

/* Created by Jayyong on 20/04/2018. */

public class MemberListAdapter extends ListAdapter<ContactRoster, MemberListHolder> {
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
    private Context context;

    public MemberListAdapter() {
        super(DIFF_CALLBACK);
    }

    @Override
    public void submitList(List<ContactRoster> list) {
        super.submitList(list);
    }

    @NonNull
    @Override
    public MemberListHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        this.context = parent.getContext();

        return new MemberListHolder(LayoutInflater.from(context).inflate(R.layout.profile_img_item, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull MemberListHolder holder, int position) {
        holder.setData(getItem(position));

        //get screen density
        float d = context.getResources().getDisplayMetrics().density;

        int width = (int) (40 * d); // width in pixels

        if (position > 0) {
            RecyclerView.LayoutParams params = new RecyclerView.LayoutParams(
                    width,
                    width
            );

            int margin = (int) (-10 * d); // margin in pixels

            params.setMargins(margin, 0, 0, 0);
            holder.profile_img.setLayoutParams(params);
        }
    }

    @Override
    public int getItemCount() {
        int count = super.getItemCount();

        if (count > 7) {
            return 5;
        } else {
            return count;
        }
    }
}