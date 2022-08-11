package com.soapp.soapp_tab.contact.contact_details;

import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.soapp.R;

import androidx.recyclerview.widget.RecyclerView;

/* Created by chang on 10/09/2017. */

public class ContactsDetailsAdapter extends RecyclerView.Adapter<ContactDetailViewHolder> {
    private Cursor dataCursor;

    public ContactsDetailsAdapter(Cursor dataCursor) {
        this.dataCursor = dataCursor;
    }

    @Override
    public ContactDetailViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.contact_detail_item, parent, false);
        return new ContactDetailViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ContactDetailViewHolder holder, int position) {
        dataCursor.moveToPosition(position);
        String str0 = dataCursor.getString(0);
        holder.number.setText(str0);

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
