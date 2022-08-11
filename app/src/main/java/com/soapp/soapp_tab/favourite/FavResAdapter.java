package com.soapp.soapp_tab.favourite;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import com.soapp.R;
import com.soapp.sql.room.entity.Restaurant;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;

/* Created by Ryan Soapp on 12/4/2017. */
public class FavResAdapter extends ListAdapter<Restaurant, FavResHolder> {
    private static final DiffUtil.ItemCallback<Restaurant> DIFF_CALLBACK =
            new DiffUtil.ItemCallback<Restaurant>() {
                @Override

                public boolean areItemsTheSame(
                        @NonNull Restaurant oldUser, @NonNull Restaurant newUser) {
                    // User properties may have changed if reloaded from the DB, but ID is fixed
                    return oldUser.getResID().equals(newUser.getResID());
                }

                @Override
                public boolean areContentsTheSame(
                        @NonNull Restaurant oldUser, @NonNull Restaurant newUser) {
                    // NOTE: if you use equals, your object must properly override Object#equals()
                    // Incorrectly returning false here will result in too many animations.
                    return oldUser.equals(newUser);
                }
            };


    FavResAdapter() {
        super(DIFF_CALLBACK);
        setHasStableIds(true);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

//    @Override
//    public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, Cursor cursor) {
//        FavResHolder holder = (FavResHolder) viewHolder;
//        cursor.moveToPosition(cursor.getPosition());
//        holder.setData(cursor);
//    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }

    @Override
    public int getItemCount() {
        return super.getItemCount();
    }

    @Override
    public FavResHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new FavResHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.soapp_fav_list_item, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull FavResHolder holder, int position) {
        holder.setData(getItem(position));
    }

}
