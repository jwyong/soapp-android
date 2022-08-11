package com.soapp.global.sharing;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import com.soapp.R;
import com.soapp.sql.room.joiners.SharingList;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;

/* Created by Soapp on 28/11/2017. */

public class SharingExistAdapter extends ListAdapter<SharingList, SharingExistHolder> {
    private static final DiffUtil.ItemCallback<SharingList> DIFF_CALLBACK = new DiffUtil.ItemCallback<SharingList>() {
        @Override
        public boolean areItemsTheSame(
                @NonNull SharingList oldUser, @NonNull SharingList newUser) {
            // User properties may have changed if reloaded from the DB, but ID is fixed
            return oldUser.getContactRoster().getContactRow().equals(newUser.getContactRoster().getContactRow());
        }

        @Override
        public boolean areContentsTheSame(
                @NonNull SharingList oldUser, @NonNull SharingList newUser) {
            // NOTE: if you use equals, your object must properly override Object#equals()
            // Incorrectly returning false here will result in too many animations.
            return oldUser.equals(newUser);
        }
    };

    SharingExistAdapter() {
        super(DIFF_CALLBACK);
    }

    @Override
    public SharingExistHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new SharingExistHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.share_item, parent,
                false));
    }

    @Override
    public void onBindViewHolder(@NonNull SharingExistHolder holder, int position) {
        holder.setData(getItem(position));
    }

    @Override
    public long getItemId(int position) {
        return getItem(position).getContactRoster().getContactRow();
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }

    @Override
    public int getItemCount() {
        return super.getItemCount();
    }
}
