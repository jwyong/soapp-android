package com.soapp.chat_class.share_contact.share_contact_details;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.soapp.R;
import com.soapp.base.BaseActivity;
import com.soapp.chat_class.share_contact.ShareContactActivity;
import com.soapp.global.GlobalVariables;
import com.soapp.global.Preferences;
import com.soapp.setup.Soapp;
import com.soapp.sql.DatabaseHelper;
import com.soapp.xmpp.GlobalMessageHelper.GlobalHeaderHelper;
import com.soapp.xmpp.GroupChatHelper.GroupChatStanza;
import com.soapp.xmpp.SingleChatHelper.SingleChatStanza;
import com.soapp.xmpp.SmackHelper;

import java.util.UUID;

import androidx.loader.app.LoaderManager;
import androidx.loader.content.CursorLoader;
import androidx.loader.content.Loader;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

//import com.soapp.setup.BasicPermissions;

public class ShareContactDet extends BaseActivity implements LoaderManager.LoaderCallbacks<Cursor>, View.OnClickListener {

    static final String[] CONTACTS_SUMMARY_PROJECTION = new String[]{
            ContactsContract.CommonDataKinds.Phone.NUMBER,
    };
    public static ShareContactDetAdapter mAdapter;
    public static String contactNumber;
    RecyclerView mRecycleView;
    String contactName = "", contactNumberID = "", jid = "";
    long lngContactId;
    String Selection = ContactsContract.CommonDataKinds.Phone.CONTACT_ID + "=?";
    Cursor data;
    private TextView txt_contactName;
    private Button share_btn;
    private RecyclerView.LayoutManager mLayoutManager;
    //for when click on "share" button
    private SingleChatStanza singleChatStanza = new SingleChatStanza();
    private GroupChatStanza groupChatStanza = new GroupChatStanza();
    private DatabaseHelper databaseHelper = DatabaseHelper.getInstance();
    private Preferences preferences = Preferences.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.chat_sharecontact_details);
        setupToolbar();
        setTitle(R.string.contact_details);

        txt_contactName = findViewById(R.id.txt_contactName);
        share_btn = findViewById(R.id.share_btn);
        share_btn.setOnClickListener(this);
        mRecycleView = findViewById(R.id.contactdetail_recycle);
        mLayoutManager = new LinearLayoutManager(this);
        mRecycleView.setLayoutManager(mLayoutManager);
        mAdapter = new ShareContactDetAdapter(this, data);
        mRecycleView.setAdapter(mAdapter);

        Intent myLocalIntent = getIntent();
        if (myLocalIntent != null) {
            if (myLocalIntent.hasExtra("displayname")) {
                contactName = myLocalIntent.getExtras().getString("displayname");
            }
            if (myLocalIntent.hasExtra("number")) {
                contactNumberID = myLocalIntent.getExtras().getString("number");
            }
            if (myLocalIntent.hasExtra("jid")) {
                jid = myLocalIntent.getExtras().getString("jid");
            }
            if (myLocalIntent.hasExtra("contactid")) {
                if (myLocalIntent.getExtras().getString("contactid").equalsIgnoreCase("1")) {
                    lngContactId = 1;
                } else {
                    lngContactId = Long.parseLong(myLocalIntent.getExtras().getString("contactid"));
                }
            }
        }

        txt_contactName.setText(contactName);
        getSupportLoaderManager().initLoader(0, null, this);


        //check for basic permissions
//        Intent intent = new Intent(this, BasicPermissions.class);
//        intent.putExtra("notLaunch", "1");

//        startActivityForResult(intent, 100);
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data2) {
        super.onActivityResult(requestCode, resultCode, data2);
        switch (requestCode) {
            case 100:
                if (resultCode == 1) {
                    setContentView(R.layout.chat_sharecontact_details);
                    setupToolbar();
                    setTitle(R.string.contact_details);

                    txt_contactName = findViewById(R.id.txt_contactName);
                    share_btn = findViewById(R.id.share_btn);
                    share_btn.setOnClickListener(this);
                    mRecycleView = findViewById(R.id.contactdetail_recycle);
                    mLayoutManager = new LinearLayoutManager(this);
                    mRecycleView.setLayoutManager(mLayoutManager);
                    mAdapter = new ShareContactDetAdapter(this, data);
                    mRecycleView.setAdapter(mAdapter);

                    Intent myLocalIntent = getIntent();
                    if (myLocalIntent != null) {
                        if (myLocalIntent.hasExtra("displayname")) {
                            contactName = myLocalIntent.getExtras().getString("displayname");
                        }
                        if (myLocalIntent.hasExtra("number")) {
                            contactNumberID = myLocalIntent.getExtras().getString("number");
                        }
                        if (myLocalIntent.hasExtra("jid")) {
                            jid = myLocalIntent.getExtras().getString("jid");
                        }
                        if (myLocalIntent.hasExtra("contactid")) {
                            if (myLocalIntent.getExtras().getString("contactid").equalsIgnoreCase("1")) {
                                lngContactId = 1;
                            } else {
                                lngContactId = Long.parseLong(myLocalIntent.getExtras().getString("contactid"));
                            }
                        }
                    }

                    txt_contactName.setText(contactName);
                    getSupportLoaderManager().initLoader(0, null, this);
                }
                break;

            default:
                break;
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        Uri baseUri = ContactsContract.CommonDataKinds.Phone.CONTENT_URI;

        return new CursorLoader(this, baseUri, CONTACTS_SUMMARY_PROJECTION, Selection, new String[]{contactNumberID}, null);
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
            case (R.id.share_btn):
                String userJid = preferences.getValue(Soapp.getInstance().getApplicationContext(), GlobalVariables.STRPREF_USER_ID);
                String userName = preferences.getValue(Soapp.getInstance().getApplicationContext(), GlobalVariables.STRPREF_USERNAME);
                String uniqueId = UUID.randomUUID().toString();


                String roomName = databaseHelper.getNameFromContactRoster(jid);
                if (SmackHelper.getXMPPConnection() != null && SmackHelper.getXMPPConnection().isAuthenticated()) {
                    String self_username = preferences.getValue(this, GlobalVariables.STRPREF_USERNAME);

                    if (jid.length() == 12) { //indi
                        singleChatStanza.SoappContactStanza(jid, contactName, contactNumber, uniqueId, self_username);

                        databaseHelper.ContactOutputDatabase(jid, uniqueId, System.currentTimeMillis(), contactName, contactNumber);
                    } else { //group

                        databaseHelper.GroupContactOutputDatabase(jid, uniqueId, System.currentTimeMillis(), userJid, contactName, contactNumber);

                        groupChatStanza.GroupContact(jid, userJid, userName, contactName, contactNumber, uniqueId, roomName);
                    }
                    new GlobalHeaderHelper().GlobalHeaderTime(jid);

                } else {
                    databaseHelper.saveMessageAndSendWhenOnline("contact", jid, userJid, getString(R.string.contact_sent), uniqueId, System.currentTimeMillis(), contactName, contactNumber, null, null, null, null);
                }
                finish();
                ShareContactActivity.activity.finish();
                break;

            default:
                break;
        }
    }
}
