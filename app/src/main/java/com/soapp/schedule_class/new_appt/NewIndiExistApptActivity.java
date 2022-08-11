package com.soapp.schedule_class.new_appt;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.load.resource.bitmap.BitmapTransitionOptions;
import com.bumptech.glide.request.RequestOptions;
import com.soapp.R;
import com.soapp.SoappApi.ApiModel.CreateApptForExistGrpModel;
import com.soapp.SoappApi.Interface.CreateApptForExistGrp;
import com.soapp.SoappApi.RetrofitAPIClient;
import com.soapp.SoappModel.AddMember;
import com.soapp.SoappModel.ApptCalendarModel;
import com.soapp.base.BaseActivity;
import com.soapp.global.GlideAPI.GlideApp;
import com.soapp.global.GlobalVariables;
import com.soapp.global.ImagePreviewByte;
import com.soapp.global.MapGps;
import com.soapp.global.MiscHelper;
import com.soapp.global.Preferences;
import com.soapp.global.UIHelper;
import com.soapp.schedule_class.group_appt.GroupScheLog;
import com.soapp.schedule_class.group_appt.gms_collapsed.MemberListAdapter;
import com.soapp.schedule_class.single_appt.IndiScheLog;
import com.soapp.setup.Soapp;
import com.soapp.sql.DatabaseHelper;
import com.soapp.sql.room.entity.ContactRoster;
import com.soapp.xmpp.GroupChatHelper.GroupChatStanza;
import com.soapp.xmpp.SingleChatHelper.SingleChatStanza;
import com.soapp.xmpp.SmackHelper;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/* Created by Jayyong on 20/04/2018. */

public class NewIndiExistApptActivity extends BaseActivity implements View.OnClickListener {
    //basics
    private UIHelper uiHelper = new UIHelper();
    private ProgressDialog progressDialog;

    //public static for date/time picker
    public static long apptDate, apptTime;
    public static TextView schelog_date_long, schelog_time;

    //grp
    RecyclerView rv_grp_mem_list;
    private DatabaseHelper databaseHelper = DatabaseHelper.getInstance();
    private String jid, selfJid, apptTitle;

    //elements
    private EditText schenew_appt_title;
    private ImageView schelog_map;
    private ImageView frnd_profile;
    private TextView schelog_loc_title;

    //variables for holding values
    private String apptLat, apptLon, apptLoc, apptAddress, apptMapURL, friendName;

    //preferences
    private Preferences preferences = Preferences.getInstance();
    private MemberListAdapter memberListAdapter;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.scheduletab_new_indi_exist_appt);


        Window window = this.getWindow();
        window.setBackgroundDrawable(getResources().getDrawable(R.drawable.ly_schelog_background));

        //set status bar colour
        uiHelper.setStatusBarColor(this, false, R.color.grey10);

        setupToolbar();

        //set title for existing if come from existing
        if (getIntent().hasExtra("exist")) {
            setTitle(R.string.new_g_appt);

        } else {
            setTitle(R.string.new_indi_appt);
        }

        //has date selected already (schetab calendar)
        if (getIntent().hasExtra("Date")) {
            apptDate = getIntent().getLongExtra("Date", 0);
        }

        //has location selected already (share res to appt, future maybe only map location without res)
        if (getIntent().hasExtra("apptLoc")) {
            if (getIntent().hasExtra("resID")) { //restaurant, need add resID as well
                //set res details to class variables
                //pending backend
            }

            //set location details to class variables
            apptLoc = getIntent().getStringExtra("apptLoc");
//            apptAddress = getIntent().getStringExtra("apptAddress");
            apptLon = getIntent().getStringExtra("apptLon");
            apptLat = getIntent().getStringExtra("apptLat");
            apptMapURL = getIntent().getStringExtra("apptMapURL");
        }

        //get intent extras (room jid and appt id)
        jid = getIntent().getStringExtra("jid");
        //bind elements
        schenew_appt_title = findViewById(R.id.schenew_appt_title);
        ConstraintLayout new_appt_date_time = findViewById(R.id.new_appt_date_time);
        schelog_date_long = findViewById(R.id.schelog_date_long);
        schelog_time = findViewById(R.id.schelog_time);
        schelog_map = findViewById(R.id.schelog_map);
        schelog_loc_title = findViewById(R.id.schelog_loc_title);
        ImageView schelog_self_profile = findViewById(R.id.schelog_self_profile);
        frnd_profile = findViewById(R.id.schelog_frnd_profile);
//        frnd_name = findViewById(R.id.frnd_name);
        ImageView btn_confirm = findViewById(R.id.btn_confirm);

        if (jid.length() > 12) {
            rv_grp_mem_list = findViewById(R.id.rv_grp_mem_list);

            memberListAdapter = new MemberListAdapter();

            LinearLayoutManager llm = new LinearLayoutManager(this);
            llm.setOrientation(LinearLayoutManager.HORIZONTAL);

            rv_grp_mem_list.setLayoutManager(llm);
            rv_grp_mem_list.setItemAnimator(null);
            rv_grp_mem_list.setNestedScrollingEnabled(false);

            LiveData<List<ContactRoster>> list = Soapp.getDatabase().selectQuery().load_grpMemSche(jid);
            list.observe(this, new Observer<List<ContactRoster>>() {
                @Override
                public void onChanged(@Nullable List<ContactRoster> memberList) {
                    memberListAdapter.submitList(memberList);

                    rv_grp_mem_list.setVisibility(View.VISIBLE);
                    frnd_profile.setVisibility(View.GONE);
//                    frnd_name.setVisibility(View.GONE);
                }
            });
            rv_grp_mem_list.setAdapter(memberListAdapter);
        }

        //set listeners on elements
        schelog_loc_title.setOnClickListener(this);
        schelog_map.setOnClickListener(this);
        new_appt_date_time.setOnClickListener(this);

        //set date and time long and UI if already got
        if (apptDate != 0) {
            apptTime = 0;
            SimpleDateFormat timeFormat = new SimpleDateFormat("h:mm a", Locale.ENGLISH);
            String apptTimeStr = timeFormat.format(apptTime);

            SimpleDateFormat dateFormat = new SimpleDateFormat("EEE, dd MMM yyyy", Locale.ENGLISH);
            String apptDateStr = dateFormat.format(apptDate);

            schelog_date_long.setText(apptDateStr);
            schelog_time.setText(apptTimeStr);
        }

        //set location to UI if got apptLoc
        if (apptLoc != null) {
            schelog_loc_title.setText(apptLoc);
            GlideApp.with(this)
                    .asBitmap()
                    .load(apptMapURL)
                    .placeholder(R.drawable.ic_def_location_no_location_400px)
                    .transition(BitmapTransitionOptions.withCrossFade())
                    .into(schelog_map);
        }

        schelog_self_profile.setOnClickListener(this);
        frnd_profile.setOnClickListener(this);
        btn_confirm.setOnClickListener(this);

        //set self's profile img
        selfJid = preferences.getValue(this, GlobalVariables.STRPREF_USER_ID);
        byte[] selfProfileImgByte = databaseHelper.getImageBytesThumbFromContactRoster(selfJid);

        GlideApp.with(this)
                .load(selfProfileImgByte)
                .placeholder(R.drawable.in_propic_circle_150px)
                .apply(RequestOptions.circleCropTransform())
                .into(schelog_self_profile);

        //set friend's profile
        List<AddMember> userProfileList = databaseHelper.get_grpProfile(jid);

//        friendName = userProfileList.get(0).getDisplayname();
        byte[] frndProfileImgByte = userProfileList.get(0).getProfilephoto();

        GlideApp.with(this)
                .load(frndProfileImgByte)
                .placeholder(R.drawable.in_propic_circle_150px)
                .apply(RequestOptions.circleCropTransform())
                .into(frnd_profile);

    }

    @Override
    public void onClick(View view) {
        uiHelper.closeKeyBoard(this);

        switch (view.getId()) {
            //date/time (open time picker)
            case (R.id.new_appt_date_time):
                List<ApptCalendarModel> apptCalList = new ArrayList<>();
                apptCalList.add(0, new ApptCalendarModel(databaseHelper.getHostingGoingCDList(),
                        databaseHelper.getApptCDList(2), databaseHelper.getApptCDList(3)));

                uiHelper.dateTimePicker(this, "NewIndiAppt", null, null, null, apptCalList, String.valueOf(apptDate), String.valueOf(apptTime));
                break;

            //location title
            case (R.id.schelog_loc_title):
                Runnable editLocTitleAction = () -> {
                    apptLoc = GlobalVariables.string1;
                    schelog_loc_title.setText(apptLoc);

                    GlobalVariables.string1 = null;
                };

                uiHelper.editTextAlertDialog(this, getString(R.string.appt_loc_edit),
                        schelog_loc_title.getText().toString(), "newAppt", null, null,
                        true, editLocTitleAction);
                break;

            //location map
            case (R.id.schelog_map):
                Intent intent = new Intent(this, MapGps.class);

                //specify from
                intent.putExtra("from", "newAppt");

                if (apptLat != null && !apptLat.equals("")) {
                    intent.putExtra("placeName", apptLoc);
                    intent.putExtra("latitude", apptLat);
                    intent.putExtra("longitude", apptLon);
                }

                startActivityForResult(intent, 100);
                break;

            //self profile img
            case (R.id.schelog_self_profile):
                Intent myimageintent = new Intent(this, ImagePreviewByte.class);

                myimageintent.putExtra("jid", selfJid);

                startActivity(myimageintent);
                break;

            //friend profile img
            case (R.id.schelog_frnd_profile):
                Intent friendimageintent = new Intent(this, ImagePreviewByte.class);

                friendimageintent.putExtra("jid", jid);

                startActivity(friendimageintent);
                break;

            //create appt button
            case (R.id.btn_confirm):
                apptTitle = schenew_appt_title.getText().toString().trim();

                //check if all fields are already filled in
                if (!apptTitle.equals("") && apptDate != 0 && apptLat != null && !apptLat.equals("")) {
                    if (SmackHelper.getXMPPConnection() != null && SmackHelper.getXMPPConnection().isAuthenticated()) {
                        new AsyncCreateAppt().execute();
                    } else {
                        Toast.makeText(this, R.string.xmpp_waiting_connection, Toast.LENGTH_SHORT).show();
                    }

                } else {
                    uiHelper.dialog1Btn(this, getString(R.string.appt_no_title),
                            getString(R.string.appt_no_msg), R.string.ok_label, R.color.black,
                            null, true, false);
                }
                break;

            default:
                break;
        }
    }

    //callback for mapschedule results
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 100) { //MapSchedule
            if (resultCode == RESULT_OK) {
                //set results to variables
                apptLat = data.getStringExtra("latitude");
                apptLon = data.getStringExtra("longitude");
                apptLoc = data.getStringExtra("locationName");
                //get map URL string from lat and long
                apptMapURL = new MiscHelper().getGmapsStaticURL(apptLat, apptLon);

                //set map and title all
                schelog_loc_title.setText(apptLoc);
                GlideApp.with(this)
                        .asBitmap()
                        .load(apptMapURL)
                        .placeholder(R.drawable.ic_def_location_no_location_400px)
                        .transition(BitmapTransitionOptions.withCrossFade())
                        .into(schelog_map);
            }
        }
    }

    //async task for creating grp chat + appt
    private class AsyncCreateAppt extends AsyncTask<String, Integer, Void> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            //Display progressDialog before download starts
            progressDialog = ProgressDialog.show(NewIndiExistApptActivity.this, null, getString(R.string.appt_creating));
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
        }

        @Override
        protected Void doInBackground(String... params) {
            //set self status to "Host" for stanza
            String uniqueID = UUID.randomUUID().toString();
            String selfUsername = preferences.getValue(NewIndiExistApptActivity.this, GlobalVariables.STRPREF_USERNAME);

            //set variable for appt id first;
            String pushMsg = getString(R.string.appt_created);
            String apptDateStr = schelog_date_long.getText().toString();
            String apptTimeStr = schelog_time.getText().toString().toLowerCase();

            if (jid.length() > 12) { //grp
                CreateApptForExistGrpModel body = new CreateApptForExistGrpModel(jid,
                        apptTitle, apptDateStr, apptTimeStr, apptLoc, "", apptLon, apptLat, apptMapURL);
                CreateApptForExistGrp client = RetrofitAPIClient.getClient().create(CreateApptForExistGrp.class);

                retrofit2.Call<CreateApptForExistGrpModel> call = client.createNewAppt("Bearer "
                        + Preferences.getInstance().getValue(NewIndiExistApptActivity.this, GlobalVariables
                        .STRPREF_ACCESS_TOKEN), body);

                call.enqueue(new Callback<CreateApptForExistGrpModel>() {
                    @Override
                    public void onResponse(Call<CreateApptForExistGrpModel> call, Response<CreateApptForExistGrpModel> response) {
                        if (!response.isSuccessful()) {
                            if (progressDialog != null && progressDialog.isShowing()) {
                                progressDialog.dismiss();
                            }

                            new MiscHelper().retroLogUnsuc(response, "AsyncCreateAppt", "JAY");
                            Toast.makeText(NewIndiExistApptActivity.this, R.string.onresponse_unsuccessful, Toast.LENGTH_SHORT).show();
                            return;
                        }
                        String apptID = response.body().getAppointment_id();

                        //send out appt stanza
                        String grpName = databaseHelper.getNameFromContactRoster(jid);

                        //format date + time to 2018-07-23 12:42:32 for "timestamp" format for API server
                        SimpleDateFormat dateTimeFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH);
                        String apptDateTimeStr = dateTimeFormat.format(apptTime);

                        new GroupChatStanza().GroupNewAppointment(jid, selfJid, selfUsername, "", uniqueID, grpName, apptID);

                        databaseHelper.createNewAppt(jid, apptTitle, apptMapURL, apptLat, apptLon,
                                apptDate, apptTime, apptLoc, null, null, apptID, uniqueID);

                        //set local notification
                        databaseHelper.scheduleLocalNotification(apptID);

                        Intent intent = new Intent(NewIndiExistApptActivity.this, GroupScheLog.class);

                        intent.putExtra("admin", true);
                        intent.putExtra("jid", jid);
                        intent.putExtra("apptID", apptID);

                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

                        startActivity(intent);

                        if (progressDialog != null && progressDialog.isShowing()) {
                            progressDialog.dismiss();
                        }

                        finish();
                    }

                    @Override
                    public void onFailure(Call<CreateApptForExistGrpModel> call, Throwable t) {
                        if (progressDialog != null && progressDialog.isShowing()) {
                            progressDialog.dismiss();
                        }

                        Toast.makeText(NewIndiExistApptActivity.this, R.string.onfailure, Toast.LENGTH_SHORT).show();
                    }
                });

            } else { //indi
                //update new appt info to sqlite to get apptID
                String apptID = databaseHelper.createNewAppt(jid, apptTitle, apptMapURL, apptLat, apptLon,
                        apptDate, apptTime, apptLoc, null, null, null, uniqueID);

                new SingleChatStanza().SoappAppointmentStanza(jid, pushMsg, apptTitle,
                        apptMapURL, apptLoc, "", apptLat, apptLon, apptDateStr, apptTimeStr,
                        String.valueOf(0), "", uniqueID, "", "", apptID,
                        selfUsername, "appointment", "1", "5");

                Intent intent = new Intent(NewIndiExistApptActivity.this, IndiScheLog.class);

                //set local notification
                databaseHelper.scheduleLocalNotification(apptID);

                intent.putExtra("jid", jid);
                intent.putExtra("apptID", apptID);

                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

                startActivity(intent);

                if (progressDialog != null && progressDialog.isShowing()) {
                    progressDialog.dismiss();
                }

                finish();
            }

            return null;
        }
    }

    @Override
    protected void onDestroy() {
        apptDate = 0;
        apptTime = 0;
        schelog_date_long = null;
        schelog_time = null;

        super.onDestroy();
    }
}