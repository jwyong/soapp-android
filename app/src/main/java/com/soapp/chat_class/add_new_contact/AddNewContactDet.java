package com.soapp.chat_class.add_new_contact;

import android.app.ProgressDialog;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.soapp.R;
import com.soapp.SoappApi.ApiModel.PostPhoneGetJidModel;
import com.soapp.SoappApi.ApiModel.SyncContactsModel;
import com.soapp.SoappApi.Interface.IndiAPIInterface;
import com.soapp.SoappApi.RetrofitAPIClient;
import com.soapp.base.BaseActivity;
import com.soapp.global.AddContactHelper;
import com.soapp.global.GlobalVariables;
import com.soapp.global.MiscHelper;
import com.soapp.global.PermissionHelper;
import com.soapp.global.Preferences;
import com.soapp.global.UIHelper;
import com.soapp.setup.Soapp;
import com.soapp.sql.DatabaseHelper;
import com.soapp.xmpp.SmackHelper;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/* Created by chang on 09/09/2017. */

//activity for add new contact (issender = 5 and 7)
public class AddNewContactDet extends BaseActivity implements View.OnClickListener {

    private String contactName, contactNumber;
    private TextView txt_phonename, txt_phonenumber;
    private Button save_btn;
    private DatabaseHelper databaseHelper = DatabaseHelper.getInstance();
    private ProgressDialog dialog;
    private MiscHelper miscHelper = new MiscHelper();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.chat_contact_detail);
        setupToolbar();
        setTitle("");
        txt_phonename = findViewById(R.id.txt_phonename);
        txt_phonenumber = findViewById(R.id.txt_phonenumber);

        contactName = getIntent().getStringExtra("nametxt");
        contactNumber = getIntent().getStringExtra("numbertxt");

        txt_phonename.setText(contactName);
        txt_phonenumber.setText(contactNumber);

        //remove special characters in contact number for function purposes
        contactNumber = contactNumber.replaceAll("[\\s\\-()]", "");

        save_btn = findViewById(R.id.save_btn);
        save_btn.setOnClickListener(this);

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case (R.id.save_btn):
                Runnable saveContact = () -> {
                    if (!getIntent().hasExtra("outgoing")) { //incoming
                        //if phone name exists, hide "save" button
                        if (databaseHelper.getPhoneNameExistNumber(contactNumber) != 0) {
                            Toast.makeText(AddNewContactDet.this, R.string.contact_exists, Toast.LENGTH_SHORT).show();
                        } else { //phone name not exist yet, save to phone book and post to server
                            //only save contact if phone number does NOT exist in contact roster
                            if (SmackHelper.getXMPPConnection() != null && SmackHelper.getXMPPConnection().isAuthenticated()) {
                                if (PackageManager.PERMISSION_DENIED == AddNewContactDet.this.checkCallingOrSelfPermission(android.Manifest.permission.WRITE_CONTACTS)) {

                                    new PermissionHelper().CheckPermissions(AddNewContactDet.this, 1007, R.string.permission_txt);

                                } else {
                                    new asyncAddContact().execute();

                                }

                            } else {
                                Toast.makeText(AddNewContactDet.this, R.string.xmpp_waiting_connection, Toast.LENGTH_SHORT).show();
                            }
                        }
                    } else { //outgoing
                        Toast.makeText(AddNewContactDet.this, R.string.contact_exists, Toast.LENGTH_SHORT).show();
                    }
                };

                new UIHelper().dialog2Btns2Eview(this, getString(R.string.add_contacts),
                        contactName, contactNumber, saveContact, null, true);
                break;
        }
    }

    private class asyncAddContact extends AsyncTask<String, Void, Void> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            //Display progressDialog before download starts
            dialog = ProgressDialog.show(AddNewContactDet.this, null, getString(R.string.saving_contact));
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);

        }

        @Override
        protected Void doInBackground(String... params) {
            getUserProfile();
            return null;
        }

        private void getUserProfile() {
            //add phonenumber to list
            ArrayList<String> phoneNumberList = new ArrayList<>();
            phoneNumberList.add(contactNumber);
            String access_token = Preferences.getInstance().getValue(AddNewContactDet.this, GlobalVariables.STRPREF_ACCESS_TOKEN);

            //build retrofit
            String size = miscHelper.getDeviceDensity(AddNewContactDet.this);
            IndiAPIInterface indiAPIInterface = RetrofitAPIClient.getClient().create(IndiAPIInterface.class);
            PostPhoneGetJidModel model = new PostPhoneGetJidModel(phoneNumberList, size);

            Call<List<SyncContactsModel>> call = indiAPIInterface.syncContacts(model, "Bearer " + access_token);

            call.enqueue(new Callback<List<SyncContactsModel>>() {
                @Override
                public void onFailure(Call<List<SyncContactsModel>> call, Throwable t) {
                    if (dialog != null && dialog.isShowing()) {
                        dialog.dismiss();
                    }
                    new MiscHelper().retroLogFailure(t, "getUserProfile ", "JAY");
                    Toast.makeText(Soapp.getInstance().getApplicationContext(), R
                            .string.onfailure, Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onResponse(Call<List<SyncContactsModel>> call, Response<List<SyncContactsModel>> response) {
                    if (!response.isSuccessful()) {

                        new MiscHelper().retroLogUnsuc(response, "getUserProfile ", "JAY");

                        if (dialog != null && dialog.isShowing()) {
                            dialog.dismiss();
                        }

                        Toast.makeText(Soapp.getInstance().getApplicationContext(), R
                                .string.onresponse_unsuccessful, Toast.LENGTH_SHORT).show();
                        return;
                    }

                    List<SyncContactsModel> userList = response.body();
                    final int listSize = userList.size();


                    if (listSize > 0) {
                        String displayName = userList.get(0).getName();
                        String phone = userList.get(0).getPhone_number();
                        String imageURL = userList.get(0).getResource_url();
                        String jid = userList.get(0).getUser_jid();
                        String image_data = userList.get(0).getImage_data();

                        databaseHelper.checkAndAddUserProfileToContactRoster(phone, GlobalVariables.string1,
                                displayName, jid, imageURL, image_data, null);

                        //add new phone number to phone book then clear global var
                        new AddContactHelper(AddNewContactDet.this).createContactPhoneBook(GlobalVariables.string1, contactNumber);
                        GlobalVariables.string1 = null;

                        //got jid, means added to soapp
                        Toast.makeText(AddNewContactDet.this, R.string.contact_added_soapp, Toast
                                .LENGTH_SHORT).show();


                    } else {
                        //no jid, add to phone book and invite to soapp?
                        Toast.makeText(AddNewContactDet.this, R.string.contact_added_phone, Toast
                                .LENGTH_SHORT).show();
                        new AddContactHelper(AddNewContactDet.this).createContactPhoneBook(GlobalVariables.string1, contactNumber);

                    }


                    //remove progress dialog and toast user
                    if (dialog != null && dialog.isShowing()) {
                        dialog.dismiss();
                    }
                    finish();
                }
            });
        }
    }
}
