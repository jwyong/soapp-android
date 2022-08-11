package com.soapp.chat_class.share_contact.share_contact_details;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.view.View;
import android.widget.TextView;

import com.soapp.R;
import com.soapp.global.UIHelper;
import com.soapp.setup.Soapp;

import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.RecyclerView;

/* Created by chang on 10/09/2017. */

public class ShareContactDetHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
    public TextView number;
    private Runnable actionPositive = new Runnable() {
        public void run() {
            Intent callIntent = new Intent(Intent.ACTION_CALL);
            callIntent.setData(Uri.parse("tel:" + number.getText().toString()));

            if (ActivityCompat.checkSelfPermission(Soapp.getInstance().getApplicationContext(),
                    Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            itemView.getContext().startActivity(callIntent);
        }
    };

    public ShareContactDetHolder(View itemView) {
        super(itemView);

        number = itemView.findViewById(R.id.contact_number);
        itemView.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        new UIHelper().dialog2Btns(itemView.getContext(), itemView.getContext().getString(R.string.contact_call),
                itemView.getContext().getString(R.string.contact_call_msg), R.string.ok_label, R.string
                .cancel, R.color.white, R.color.black,
                actionPositive, null, true);
    }
}