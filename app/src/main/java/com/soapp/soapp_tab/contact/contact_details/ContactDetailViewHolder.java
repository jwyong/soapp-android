package com.soapp.soapp_tab.contact.contact_details;

import android.content.Intent;
import android.net.Uri;
import android.view.View;
import android.widget.TextView;

import com.soapp.R;
import com.soapp.global.UIHelper;

import androidx.recyclerview.widget.RecyclerView;

/* Created by chang on 10/09/2017. */

public class ContactDetailViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

    public TextView number;

    ContactDetailViewHolder(View itemView) {
        super(itemView);

        number = itemView.findViewById(R.id.contact_number);
        itemView.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        Runnable callIntent = () -> {
            Intent intent1 = new Intent(Intent.ACTION_DIAL, Uri.fromParts("tel", number.getText().toString(), null));
            itemView.getContext().startActivity(intent1);
        };

        new UIHelper().dialog2Btns(itemView.getContext(), null,
                itemView.getContext().getString(R.string.contact_call_msg), R.string.ok_label, R.string.cancel, R.color.white, R.color.black,
                callIntent, null, true);
    }
}
