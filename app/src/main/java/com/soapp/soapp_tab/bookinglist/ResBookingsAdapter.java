package com.soapp.soapp_tab.bookinglist;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import com.soapp.R;
import com.soapp.sql.room.joiners.BookingList;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;

/* Created by chang on 04/08/2017. */

class ResBookingsAdapter extends ListAdapter<BookingList, ResBookingsHolder> {
    private static final DiffUtil.ItemCallback<BookingList> DIFF_CALLBACK = new DiffUtil.ItemCallback<BookingList>() {
        @Override
        public boolean areItemsTheSame(
                @NonNull BookingList oldUser, @NonNull BookingList newUser) {
            // User properties may have changed if reloaded from the DB, but ID is fixed
            return oldUser.getBooking().getBookingId().equals(newUser.getBooking().getBookingId());
        }

        @Override
        public boolean areContentsTheSame(
                @NonNull BookingList oldUser, @NonNull BookingList newUser) {
            // NOTE: if you use equals, your object must properly override Object#equals()
            // Incorrectly returning false here will result in too many animations.
            return oldUser.equals(newUser);
        }
    };


    ResBookingsAdapter() {
        super(DIFF_CALLBACK);
    }

    @Override
    public long getItemId(int position) {
        return getItem(position).getBooking().getBookingId() != null ? getItem(position).getBooking().getBookingId().hashCode() : super.getItemId(position);
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }

    @Override
    public int getItemViewType(int position) {
        return 0;
    }

    @Override
    public int getItemCount() {
        return super.getItemCount();
    }

    @NonNull
    @Override
    public ResBookingsHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ResBookingsHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.soapp_mybooking_list_item,
                parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ResBookingsHolder holder, int position) {
        holder.setData(getItem(position));

    }
}