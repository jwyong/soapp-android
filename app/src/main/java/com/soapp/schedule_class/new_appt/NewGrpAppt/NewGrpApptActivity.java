package com.soapp.schedule_class.new_appt.NewGrpAppt;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.Window;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.load.resource.bitmap.BitmapTransitionOptions;
import com.bumptech.glide.request.RequestOptions;
import com.soapp.R;
import com.soapp.SoappApi.APIGlobalVariables;
import com.soapp.SoappApi.ApiModel.CreateGroupModel;
import com.soapp.SoappApi.Interface.CreateGroupAppt;
import com.soapp.SoappApi.RetrofitAPIClient;
import com.soapp.SoappModel.ApptCalendarModel;
import com.soapp.base.BaseActivity;
import com.soapp.global.DirectoryHelper;
import com.soapp.global.GlideAPI.GlideApp;
import com.soapp.global.GlobalVariables;
import com.soapp.global.ImageHelper;
import com.soapp.global.MapGps;
import com.soapp.global.MiscHelper;
import com.soapp.global.Preferences;
import com.soapp.global.UIHelper;
import com.soapp.schedule_class.group_appt.GroupScheLog;
import com.soapp.sql.DatabaseHelper;
import com.soapp.sql.room.entity.ContactRoster;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;

/* Created by Jayyong on 20/04/2018. */

public class NewGrpApptActivity extends BaseActivity implements View.OnClickListener {
    //basics
    private UIHelper uiHelper = new UIHelper();
    private Preferences preferences = Preferences.getInstance();
    //    private ProgressDialog progressDialog;
    private DatabaseHelper databaseHelper = DatabaseHelper.getInstance();

    public static Uri selectedImage;

    //public static elements
    public static long apptDate, apptTime;
    public static TextView schelog_date_long, schelog_time;
    private String selfJid, roomName, apptTitle;

    //elements
    private EditText schenew_appt_title;
    private ImageView schelog_map;
    private ImageView schenew_grp_profile;
    private TextView schelog_loc_title, grpmem_no;

    private NewGrpApptAdapter mAdapter;
    private EditText schenew_grp_title;

    //variables for holding values
    private String apptLat, apptLon, apptLoc, apptMapURL;

    //progress bar
    LinearLayout ll_progress_loading;
    //default text loading
    TextView tv_progress_loading;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.scheduletab_new_grp_appt);

        Window window = this.getWindow();
        window.setBackgroundDrawable(getResources().getDrawable(R.drawable.ly_schelog_background));

        //set status bar colour
        uiHelper.setStatusBarColor(this, false, R.color.grey10);

        setupToolbar();

        //bind elements
        schenew_appt_title = findViewById(R.id.schenew_appt_title);
        schelog_date_long = findViewById(R.id.schelog_date_long);
        schelog_time = findViewById(R.id.schelog_time);
        schelog_map = findViewById(R.id.schelog_map);
        schelog_loc_title = findViewById(R.id.schelog_loc_title);
        ImageView schelog_self_profile = findViewById(R.id.schelog_self_profile);
        ImageView btn_confirm = findViewById(R.id.btn_confirm);

        //bind grp elements
        schenew_grp_profile = findViewById(R.id.schenew_grp_profile);
        //grp elements
        RecyclerView new_grp_appt_rv = findViewById(R.id.new_grp_appt_rv);
        grpmem_no = findViewById(R.id.grpmem_no);
        schenew_grp_title = findViewById(R.id.schenew_grp_title);
        ConstraintLayout new_g_appt_date_time = findViewById(R.id.new_g_appt_date_time);

        //set listeners on elements
        new_g_appt_date_time.setOnClickListener(this);
        schelog_loc_title.setOnClickListener(this);
        schelog_map.setOnClickListener(this);
        new_grp_appt_rv.setOnClickListener(this);
        btn_confirm.setOnClickListener(this);

        //set listeners on grp elements
        schenew_grp_profile.setOnClickListener(this);
        new_grp_appt_rv.setOnClickListener(this);

        //init progress bar
        initProgressBar();

        //has date selected already (schetab calendar)
        if (getIntent().hasExtra("Date")) {
            apptDate = getIntent().getLongExtra("Date", 0);
        }

//        long apptDate = getIntent().getLongExtra("Date", 0);

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

        //set self's profile img
        selfJid = preferences.getValue(this, GlobalVariables.STRPREF_USER_ID);
        byte[] selfProfileImgByte = databaseHelper.getImageBytesThumbFromContactRoster(selfJid);

        GlideApp.with(this)
                .asBitmap()
                .load(selfProfileImgByte)
                .placeholder(R.drawable.in_propic_circle_150px)
                .transition(BitmapTransitionOptions.withCrossFade())
                .apply(RequestOptions.circleCropTransform())
                .into(schelog_self_profile);

        //set recyclerview for grpmem images
        LinearLayoutManager llm = new LinearLayoutManager(this);
        llm.setOrientation(LinearLayoutManager.HORIZONTAL);

        new_grp_appt_rv.setLayoutManager(llm);

        mAdapter = new NewGrpApptAdapter();

        NewGrpApptVM viewModel = ViewModelProviders.of(this).get(NewGrpApptVM.class);
        viewModel.getData();
        viewModel.getmObservableNewGrpAppt().observe(this, new Observer<List<ContactRoster>>() {
                    @Override
                    public void onChanged(@Nullable List<ContactRoster> profileImgList) {
                        mAdapter.submitList(profileImgList);

                        //set selected members count
                        grpmem_no.setText(String.valueOf(mAdapter.getRealItemCount()));
                    }
                }
        );
        new_grp_appt_rv.setAdapter(mAdapter);
    }

    @Override
    public void onClick(View view) {
        uiHelper.closeKeyBoard(this);

        switch (view.getId()) {
            case R.id.schenew_grp_profile: //show popup for camera and gallery
                uiHelper.showCameraGalleryPopup(this, "NewGrpAppt", "NewGrpApptMedia");
                break;

            //date/time (open time picker)
            case (R.id.new_g_appt_date_time):
                List<ApptCalendarModel> apptCalList = new ArrayList<>();
                apptCalList.add(0, new ApptCalendarModel(databaseHelper.getHostingGoingCDList(),
                        databaseHelper.getApptCDList(2), databaseHelper.getApptCDList(3)));

                uiHelper.dateTimePicker(this, "NewGrpAppt", null, null, null, apptCalList, String.valueOf(apptDate), String.valueOf(apptTime));
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

            //grp mem images recyclerview (show popup)
            case (R.id.new_grp_appt_rv):
                break;

            //create appt button
            case (R.id.btn_confirm):
                roomName = schenew_grp_title.getText().toString().trim();
                apptTitle = schenew_appt_title.getText().toString().trim();

                //check if grp title filled in first
                if (roomName.equals("")) {
                    uiHelper.dialog1Btn(this, getString(R.string.appt_no_title),
                            getString(R.string.appt_no_grp_msg), R.string.ok_label, R.color.black,
                            null, true, false);

                    //check if all fields are already filled in
                } else if (!apptTitle.equals("") && apptDate != 0 && apptLat != null && !apptLat.equals("")) {
                    new AsyncCreateGrp().execute(roomName);

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

    @Override
    protected void onResume() {
        super.onResume();
        if (selectedImage != null) {
            GlideApp.with(this)
                    .asBitmap()
                    .placeholder(R.drawable.grp_propic_circle_150px)
                    .load(selectedImage)
                    .thumbnail(0.5f)
                    .encodeQuality(50)
                    .apply(RequestOptions.circleCropTransform())
                    .override(180, 180)
                    .into(schenew_grp_profile);
        }
    }

    @Override
    protected void onDestroy() {
        apptDate = 0;
        apptTime = 0;
        schelog_date_long = null;
        schelog_time = null;
        grpmem_no = null;
        selectedImage = null;

        super.onDestroy();
    }

    //async task for creating grp chat + appt
    private class AsyncCreateGrp extends AsyncTask<String, Integer, Void> {
        byte[] imageBytes100;
        Bitmap grpImgBitmap = null;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            //Display progressDialog before download starts
            ll_progress_loading.setVisibility(View.VISIBLE);
            tv_progress_loading.setText(R.string.grp_creating);
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
        }

        @Override
        protected Void doInBackground(String... params) {
            //create room_image multipart
            RequestBody image = null;
            MultipartBody.Part roomImageMP = null;
            if (selectedImage != null) {
                //get bytes from selectedImage
                grpImgBitmap = new ImageHelper(NewGrpApptActivity.this).getBitmapForProfile(selectedImage);
                imageBytes100 = DirectoryHelper.getBytesFromBitmap100(grpImgBitmap);
                image = RequestBody.create(APIGlobalVariables.JPEG, imageBytes100);
                roomImageMP = MultipartBody.Part.createFormData("room_image", "name.jpeg", image);
            }

            //create part member_ids (need list later for pubsub loop members
            final List<String> jidList = databaseHelper.get_jidStatus1FromCR();
            String memberJidList = "";
            for (String s : jidList) {
                memberJidList += s + ",";
            }

            //uniqdueID (message_id)
            String uniqueID = UUID.randomUUID().toString();

            //appt details
            String apptDateStr = schelog_date_long.getText().toString();
            String apptTimeStr = schelog_time.getText().toString().toLowerCase();

            //set all request bodies from strings
            RequestBody roomNameRB = RequestBody.create(MediaType.parse("text/plain"), roomName);
            RequestBody memberJidRB = RequestBody.create(MediaType.parse("text/plain"), memberJidList);
            RequestBody uniqueIDRB = RequestBody.create(MediaType.parse("text/plain"), uniqueID);
            RequestBody pubsubHostRB = RequestBody.create(MediaType.parse("text/plain"), GlobalVariables.pubsubHost);
            RequestBody xmppHostRB = RequestBody.create(MediaType.parse("text/plain"), GlobalVariables.xmppHost);
            RequestBody xmppResRB = RequestBody.create(MediaType.parse("text/plain"), "ANDROID");
            RequestBody apptTitleRB = RequestBody.create(MediaType.parse("text/plain"), apptTitle);

            RequestBody apptDateRB = RequestBody.create(MediaType.parse("text/plain"), apptDateStr);
            RequestBody apptTimeRB = RequestBody.create(MediaType.parse("text/plain"), apptTimeStr);
            RequestBody apptLocRB = RequestBody.create(MediaType.parse("text/plain"), apptLoc);
            RequestBody apptLonRB = RequestBody.create(MediaType.parse("text/plain"), apptLon);
            RequestBody apptLatRB = RequestBody.create(MediaType.parse("text/plain"), apptLat);
            RequestBody apptMapURLRB = RequestBody.create(MediaType.parse("text/plain"), apptMapURL);

            //post retrofit with access token as header
            String access_token = preferences.getValue(NewGrpApptActivity.this, GlobalVariables
                    .STRPREF_ACCESS_TOKEN);

            //build retrofit
            CreateGroupAppt client = RetrofitAPIClient.getClient().create(CreateGroupAppt.class);
            retrofit2.Call<CreateGroupModel> call2 = client.createGroupApi("Bearer " +
                            access_token, roomNameRB, roomImageMP, memberJidRB, uniqueIDRB, pubsubHostRB,
                    xmppHostRB, xmppResRB, apptTitleRB, apptDateRB, apptTimeRB, apptLocRB, apptLonRB,
                    apptLatRB, apptMapURLRB);

            call2.enqueue(new retrofit2.Callback<CreateGroupModel>() {
                @Override
                public void onResponse(retrofit2.Call<CreateGroupModel> call, final retrofit2.Response<CreateGroupModel> response) {
                    if (!response.isSuccessful()) { //response unsuccessful, toast user
                        try {
                            String error = response.errorBody().string();
                            Toast.makeText(NewGrpApptActivity.this, error, Toast.LENGTH_SHORT).show();

                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        runOnUiThread(() -> {
                            new MiscHelper().retroLogUnsuc(response, "CreateGroupAppt ", "JAY");
                            ll_progress_loading.setVisibility(View.GONE);
                        });
                        return;
                    }
                    //response: room_id, appointment_id
                    String roomID = String.valueOf(response.body().getRoom_id());
                    String apptID = String.valueOf(response.body().getAppointment_id());

                    //create img thumb based on full img
                    byte[] grpImgByteThumb = null;
                    if (grpImgBitmap != null) {
                        grpImgByteThumb = DirectoryHelper.getBytesFromBitmap33(grpImgBitmap);
                    }

                    //update appt info to sqlite
                    databaseHelper.outgoingNewGrpChatAppt(roomID, apptTitle, apptMapURL, apptLat, apptLon,
                            apptDate, apptTime, apptLoc, null, null, roomName, grpImgByteThumb,
                            null, apptID, uniqueID);

                    //loop through each member to pubsub and update database
                    for (int i = 0; i < jidList.size(); i++) {
                        String memberJid = jidList.get(i);

                        //add each grp member to GROUPMEM
                        String color = MiscHelper.getColorForGrpDisplayName(i);
                        databaseHelper.checkAndAddJidToGrpMem(memberJid, roomID, "member", color);

                        //add each grp member to GROUPMEM_STATUS (status = 2 - undecided)
                        databaseHelper.checkAndAddStatusToGrpMemStatus(memberJid, roomID, apptID, 2);
                    }
                    //set local notification
                    databaseHelper.scheduleLocalNotification(apptID);

                    //go to grp sche log
                    Intent intent = new Intent(NewGrpApptActivity.this, GroupScheLog.class);

                    intent.putExtra("jid", roomID);
                    intent.putExtra("admin", true);
                    intent.putExtra("apptID", apptID);
                    ll_progress_loading.setVisibility(View.GONE);
                    startActivity(intent);

                    //finish previous intent
                    Intent backIntent = new Intent();
                    setResult(Activity.RESULT_OK, backIntent);

                    //finish current intent
                    finish();
                }

                @Override
                public void onFailure(retrofit2.Call<CreateGroupModel> call, Throwable t) {
                    new Handler(Looper.getMainLooper()).post(() -> {
                        Toast.makeText(NewGrpApptActivity.this, R.string.onfailure, Toast.LENGTH_SHORT).show();
                        new MiscHelper().retroLogFailure(t, "CreateGroupAppt ", "JAY");
                        ll_progress_loading.setVisibility(View.GONE);
                    });
                }
            });

            return null;
        }
    }

    public void initProgressBar() {
        ll_progress_loading = findViewById(R.id.ll_progress_loading);
        ll_progress_loading.setVisibility(View.GONE);
        tv_progress_loading = findViewById(R.id.tv_progress_loading);

    }
}