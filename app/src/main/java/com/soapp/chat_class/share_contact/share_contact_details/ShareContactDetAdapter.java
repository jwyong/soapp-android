package com.soapp.chat_class.share_contact.share_contact_details;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import com.soapp.R;

import androidx.recyclerview.widget.RecyclerView;

/* Created by chang on 10/09/2017. */

public class ShareContactDetAdapter extends RecyclerView.Adapter<ShareContactDetHolder> {
    private Cursor dataCursor;

    public ShareContactDetAdapter(Context context, Cursor dataCursor) {
        this.dataCursor = dataCursor;
    }

    @Override
    public ShareContactDetHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ShareContactDetHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout
                .chat_share_contact_detail_item, parent, false));
    }

    @Override
    public void onBindViewHolder(ShareContactDetHolder holder, int position) {
        dataCursor.moveToPosition(position);

        String contactNumber = dataCursor.getString(0);
        holder.number.setText(contactNumber);

        //add first row of phone number to var (only for now, future need add whole thing)
        if (position == 0) {
            ShareContactDet.contactNumber = contactNumber;
        }
    }

    @Override
    public int getItemCount() {
        return (dataCursor == null) ? 0 : dataCursor.getCount();
    }

    public Cursor swapCursor(Cursor cursor) {
        if (dataCursor == cursor) {
            return null;
        }
        Cursor oldCursor = dataCursor;
        this.dataCursor = cursor;
        if (cursor != null) {
            this.notifyDataSetChanged();
        }
        return oldCursor;
    }
}
