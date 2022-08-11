package com.soapp.new_chat_schedule.group.create_grp;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.soapp.R;
import com.soapp.SoappApi.APIGlobalVariables;
import com.soapp.SoappApi.ApiModel.CreateGroupModel;
import com.soapp.SoappApi.Interface.CreateGroupChat;
import com.soapp.SoappApi.RetrofitAPIClient;
import com.soapp.base.BaseActivity;
import com.soapp.chat_class.group_chat.GroupChatLog;
import com.soapp.global.CameraHelper;
import com.soapp.global.DirectoryHelper;
import com.soapp.global.GlideAPI.GlideApp;
import com.soapp.global.GlobalVariables;
import com.soapp.global.ImageHelper;
import com.soapp.global.MediaGallery.GalleryMainActivity;
import com.soapp.global.MiscHelper;
import com.soapp.global.Preferences;
import com.soapp.global.UIHelper;
import com.soapp.setup.Soapp;
import com.soapp.sql.DatabaseHelper;
import com.soapp.sql.room.entity.ContactRoster;
import com.soapp.xmpp.GroupChatHelper.GroupChatStanza;
import com.soapp.xmpp.PubsubHelper.PubsubNodeCall;
import com.soapp.xmpp.SmackHelper;

import java.io.File;
import java.util.List;
import java.util.UUID;

import androidx.lifecycle.LiveData;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;

/* Created by chang on 11/08/2017. */

public class NewCreateGroupController extends BaseActivity implements View.OnClickListener {
    //basics
    private DatabaseHelper databaseHelper = DatabaseHelper.getInstance();
    private UIHelper uiHelper = new UIHelper();

    //public statics for grp profile img
    public static Uri selectedImage;
    public static String bigImagePath;

    Bitmap grpImgBitmap;
    String newGrpName;

    //recyclerview
    private RecyclerView new_g_create_rv;
    private NewCreateGroupAdapter newCreateGroupAdapter;

    //elements
    private ImageView new_g_create_btn;
    private TextView new_g_create_gallery;
    private ImageView new_g_create_grp_img;
    private RecyclerView mRecyclerView;
    private NewCreateGroupAdapter mAdapter;
    private ProgressDialog dialog;
    private EditText new_g_create_grp_name;
    private GroupChatStanza groupChatStanza = new GroupChatStanza();
    private Preferences preferences = Preferences.getInstance();
    private PubsubNodeCall pubsubNodeCall = new PubsubNodeCall();
    private boolean isUploaded = false;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.new_g_chat_create);
        setupToolbar();
        setTitle(R.string.create_grp);

        //init elements
        new_g_create_btn = findViewById(R.id.new_g_create_btn);
        new_g_create_grp_img = findViewById(R.id.new_g_create_grp_img);
        new_g_create_gallery = findViewById(R.id.new_g_create_gallery);
        new_g_create_grp_name = findViewById(R.id.new_g_create_grp_name);
        new_g_create_rv = findViewById(R.id.new_g_create_rv);

        //set listeners
        new_g_create_grp_img.setOnClickListener(this);
        new_g_create_gallery.setOnClickListener(this);
        new_g_create_btn.setOnClickListener(this);

        //populate recyclerview
        newCreateGroupAdapter = new NewCreateGroupAdapter();
        newCreateGroupAdapter.setHasStableIds(true);

        LinearLayoutManager llmImg = new LinearLayoutManager(this);
        llmImg.setOrientation(RecyclerView.VERTICAL);

        new_g_create_rv.setLayoutManager(llmImg);
        new_g_create_rv.setItemAnimator(null);

        LiveData<List<ContactRoster>> addGrpList = Soapp.getDatabase().selectQuery().getNewGroupSelected();
        addGrpList.observe(this, newCreateGrpList -> {
            newCreateGroupAdapter.submitList(newCreateGrpList);
            newCreateGroupAdapter.notifyDataSetChanged();
        });
        new_g_create_rv.setAdapter(newCreateGroupAdapter);

        //set enter button action
        new_g_create_grp_name.setOnEditorActionListener((view, actionId, event) -> {
            if ((event != null && (event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) || (actionId == EditorInfo.IME_ACTION_DONE)) {
                new_g_create_btn.performClick();
            }
            return false;
        });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.new_g_create_btn:
                String newGrpName = new_g_create_grp_name.getText().toString().trim();
                if (newGrpName.length() == 0) { //no grp name inputted
                    uiHelper.dialog1Btn(this, getString(R.string.grp_no_title),
                            getString(R.string.grp_no_title_msg), R.string.ok_label, R.color.black,
                            null, true, false);
                } else {
                    if (SmackHelper.getXMPPConnection() != null && SmackHelper.getXMPPConnection().isAuthenticated()) {
                        //get roomname and imgbyte for new grp
                        String roomName = new_g_create_grp_name.getText().toString().trim();

                        new AsyncCreateGrp().execute(roomName);
                    } else {
                        Toast.makeText(NewCreateGroupController.this, R.string.xmpp_waiting_connection, Toast.LENGTH_SHORT).show();
                    }
                }
                break;

            case R.id.new_g_create_grp_img: //launch camera
                Intent cameraintent = new CameraHelper().startCameraIntent(this);

                cameraintent.putExtra("from", "CreateGroup");

                startActivity(cameraintent);
                break;

            case R.id.new_g_create_gallery: //select from image media library
                Intent intent = new Intent(this, GalleryMainActivity.class);

                intent.putExtra("title", "Select media");
                intent.putExtra("mode", 2);
                intent.putExtra("maxSelection", 8);
                intent.putExtra("from", "createGroupMedia");

                startActivity(intent);
                break;

            default:
                break;
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
                    .skipMemoryCache(true)
                    .diskCacheStrategy(DiskCacheStrategy.NONE)
                    .thumbnail(0.5f)
                    .encodeQuality(50)
                    .apply(RequestOptions.circleCropTransform())
                    .override(180, 180)
                    .into(new_g_create_grp_img);
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        if (!isUploaded && selectedImage != null) {
            File wastedCropImage = new File(selectedImage.getPath());
            wastedCropImage.delete();

            if (bigImagePath != null) {
                File wastedImage = new File(bigImagePath);
                wastedImage.delete();
            }
        }
    }

    @Override
    protected void onDestroy() {
        selectedImage = null;
        bigImagePath = null;

        super.onDestroy();
    }

    private class AsyncCreateGrp extends AsyncTask<String, Integer, Void> {
        byte[] imageBytes100;
        byte[] imageBytes33;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            //Display progressDialog before download starts
            dialog = ProgressDialog.show(NewCreateGroupController.this, null, getString(R.string.grp_creating));
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
        }

        @Override
        protected Void doInBackground(String... params) {
            isUploaded = true;

            //room name
            String roomName = params[0];

            //room image
            RequestBody image = null;
            MultipartBody.Part room_image = null;

            if (selectedImage != null) {
                grpImgBitmap = new ImageHelper(NewCreateGroupController.this).getBitmapForProfile(selectedImage);
                imageBytes100 = DirectoryHelper.getBytesFromBitmap100(grpImgBitmap);
                imageBytes33 = DirectoryHelper.getBytesFromBitmap33(grpImgBitmap);
                image = RequestBody.create(APIGlobalVariables.JPEG, imageBytes100);
                room_image = MultipartBody.Part.createFormData("room_image", "name.jpeg", image);
            }

            //member jid list string
            final List<String> jidList = databaseHelper.get_jidStatus1FromCR();
            String memberJidListString = "";
            for (String s : jidList) {
                memberJidListString += s + ",";
            }

            //uniqueID as message_id
            String uniqueID = UUID.randomUUID().toString();

            //post retrofit with access token as header
            String access_token = preferences.getValue(NewCreateGroupController.this, GlobalVariables
                    .STRPREF_ACCESS_TOKEN);

            //set all request bodies from strings
            RequestBody roomNameRB = RequestBody.create(MediaType.parse("text/plain"), roomName);
            RequestBody memberJidRB = RequestBody.create(MediaType.parse("text/plain"), memberJidListString);
            RequestBody uniqueIDRB = RequestBody.create(MediaType.parse("text/plain"), uniqueID);
            RequestBody pubsubHostRB = RequestBody.create(MediaType.parse("text/plain"), GlobalVariables.pubsubHost);
            RequestBody xmppHostRB = RequestBody.create(MediaType.parse("text/plain"), GlobalVariables.xmppHost);
            RequestBody xmppResRB = RequestBody.create(MediaType.parse("text/plain"), "ANDROID");

            //build retrofit
            CreateGroupChat client = RetrofitAPIClient.getClient().create(CreateGroupChat.class);
            retrofit2.Call<CreateGroupModel> call2 = client.createGroupApi("Bearer " +
                            access_token, roomNameRB, room_image, memberJidRB, uniqueIDRB, pubsubHostRB,
                    xmppHostRB, xmppResRB);

//            process callback (onresponse, onfailure...)
            call2.enqueue(new retrofit2.Callback<CreateGroupModel>() {
                @Override
                public void onResponse(retrofit2.Call<CreateGroupModel> call, final retrofit2.Response<CreateGroupModel> response) {
                    if (!response.isSuccessful()) { //response unsuccessful, toast user
                        runOnUiThread(() -> {
                            new MiscHelper().retroLogUnsuc(response, "createGroupcontroller  ", "JAY");
                            Toast.makeText(NewCreateGroupController.this, R.string
                                    .onresponse_unsuccessful, Toast.LENGTH_SHORT).show();
                            if (dialog != null && dialog.isShowing()) {
                                dialog.dismiss();
                            }
                        });
                        return;
                    }
                    //response: room_id, resource_url
                    String room_id = String.valueOf(response.body().getRoom_id());
                    long currentDate = System.currentTimeMillis();

                    //create new grp in sqlite
                    databaseHelper.createNewGrpChat(room_id, currentDate, roomName, imageBytes33, null, true, null, false);

                    //loop through each member to pubsub and update database
                    for (int i = 0; i < jidList.size(); i++) {
                        String memberJid = jidList.get(i);

                        //update group info to GROUPMEM
                        String color = MiscHelper.getColorForGrpDisplayName(i);
                        databaseHelper.checkAndAddJidToGrpMem(memberJid, room_id, "member", color);
                    }
                    //go to grp chat log
                    Intent intent = new Intent(NewCreateGroupController.this, GroupChatLog.class);

                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    intent.putExtra("jid", room_id);
                    intent.putExtra("admin", true);

                    if (dialog != null && dialog.isShowing()) {
                        dialog.dismiss();
                    }

                    //finish previous intent
                    Intent backIntent = new Intent();
                    setResult(Activity.RESULT_OK, backIntent);

                    startActivity(intent);

                    //finish current intent
                    finish();
                }

                @Override
                public void onFailure(retrofit2.Call<CreateGroupModel> call, Throwable t) {
                    new Handler(Looper.getMainLooper()).post(new Runnable() {
                        @Override
                        public void run() {
                            new MiscHelper().retroLogFailure(t, "createGroupcontroller  ", "JAY");
                            Toast.makeText(NewCreateGroupController.this, R.string.onfailure,
                                    Toast.LENGTH_SHORT).show();
                            if (dialog != null && dialog.isShowing()) {
                                dialog.dismiss();
                            }
                        }
                    });
                }
            });

            return null;
        }
    }
}