package com.soapp.soapp_tab.contact;

import android.content.Intent;
import android.view.View;
import android.widget.TextView;

import com.soapp.R;
import com.soapp.soapp_tab.contact.contact_details.ContactDetails;

import androidx.recyclerview.widget.RecyclerView;

/* Created by chang on 29/07/2017. */

public class ContactViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

    public TextView name, number;
    private Intent intent;

    public ContactViewHolder(View itemView) {
        super(itemView);
        name = itemView.findViewById(R.id.contact_name);
        number = itemView.findViewById(R.id.contact_number);
        itemView.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        intent = new Intent(itemView.getContext(), ContactDetails.class);
        intent.putExtra("number", number.getText().toString());
        intent.putExtra("displayname", name.getText().toString());
        itemView.getContext().startActivity(intent);
    }
}
