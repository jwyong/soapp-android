package com.soapp.chat_class.share_contact;

import android.content.Intent;
import android.view.View;
import android.widget.TextView;

import com.soapp.R;
import com.soapp.chat_class.share_contact.share_contact_details.ShareContactDet;

import androidx.recyclerview.widget.RecyclerView;

/* Created by chang on 29/07/2017. */

public class ShareContactViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

    public TextView name, number;

    ShareContactViewHolder(View itemView) {
        super(itemView);
        name = itemView.findViewById(R.id.contact_name);
        number = itemView.findViewById(R.id.contact_number);
        itemView.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        Intent intent = new Intent(itemView.getContext(), ShareContactDet.class);

        intent.putExtra("number", number.getText().toString());
        intent.putExtra("displayname", name.getText().toString());
        intent.putExtra("jid", ShareContactActivity.jid);

        itemView.getContext().startActivity(intent);
    }


}
