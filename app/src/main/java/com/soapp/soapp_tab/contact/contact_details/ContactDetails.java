package com.soapp.soapp_tab.contact.contact_details;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.view.View;
import android.widget.TextView;

import com.soapp.R;
import com.soapp.base.BaseActivity;
import com.soapp.global.SharingHelper;
import com.soapp.global.UIHelper;

import androidx.loader.app.LoaderManager;
import androidx.loader.content.CursorLoader;
import androidx.loader.content.Loader;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

//import com.soapp.setup.BasicPermissions;

public class ContactDetails extends BaseActivity implements LoaderManager
        .LoaderCallbacks<Cursor>, View.OnClickListener {

    static final String[] CONTACTS_SUMMARY_PROJECTION = new String[]{
            ContactsContract.CommonDataKinds.Phone.NUMBER,
    };
    RecyclerView mRecycleView;
    ContactsDetailsAdapter mAdapter;
    String strContactName = "", strPhoneNumber = "", strJid = "";
    long lngContactId;
    String Selection = ContactsContract.CommonDataKinds.Phone.CONTACT_ID + "=?";
    Cursor data;
    private TextView txt_contactName, invite_to_soapp;
    private RecyclerView.LayoutManager mLayoutManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //status bar color
        new UIHelper().setStatusBarColor(this, false , R.color.grey8);

        setContentView(R.layout.contact_details);
        setupToolbar();
//        setTitle(R.string.contact_details);


        txt_contactName = findViewById(R.id.txt_contactName);
        invite_to_soapp = findViewById(R.id.invite_to_soapp);
        invite_to_soapp.setOnClickListener(this);
        mRecycleView = findViewById(R.id.contactdetail_recycle);
        mLayoutManager = new LinearLayoutManager(this);
        mRecycleView.setLayoutManager(mLayoutManager);
        mAdapter = new ContactsDetailsAdapter(data);
        mRecycleView.setAdapter(mAdapter);

        Intent myLocalIntent = getIntent();
        if (myLocalIntent != null) {
            if (myLocalIntent.hasExtra("displayname")) {
                strContactName = myLocalIntent.getExtras().getString("displayname");
            }
            if (myLocalIntent.hasExtra("number")) {
                strPhoneNumber = myLocalIntent.getExtras().getString("number");
            }
            if (myLocalIntent.hasExtra("jid")) {
                strJid = myLocalIntent.getExtras().getString("jid");
            }
            if (myLocalIntent.hasExtra("contactid")) {
                if (myLocalIntent.getExtras().getString("contactid").equalsIgnoreCase("1")) {
                    lngContactId = 1;
                } else {
                    lngContactId = Long.parseLong(myLocalIntent.getExtras().getString("contactid"));
                }
            }
        }

        txt_contactName.setText(strContactName);
        getSupportLoaderManager().initLoader(0, null, this);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        Uri baseUri = ContactsContract.CommonDataKinds.Phone.CONTENT_URI;

        return new CursorLoader(this, baseUri, CONTACTS_SUMMARY_PROJECTION, Selection, new String[]{strPhoneNumber}, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        mAdapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case (R.id.invite_to_soapp):
                new SharingHelper().shareSoappToFriends(this, findViewById(R.id.progress_bar_cl), view.findViewById(R.id.invite_to_soapp));
                break;

            default:
                break;
        }
    }
}
