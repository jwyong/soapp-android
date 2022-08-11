package com.soapp.chat_class.group_chat.details;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.RequestBuilder;
import com.bumptech.glide.load.resource.bitmap.BitmapTransitionOptions;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.target.Target;
import com.bumptech.glide.request.transition.Transition;
import com.soapp.R;
import com.soapp.SoappApi.ApiModel.GroupModel;
import com.soapp.SoappApi.Interface.UpdateGroupProfileNameOnly;
import com.soapp.SoappApi.RetrofitAPIClient;
import com.soapp.SoappModel.AddMember;
import com.soapp.base.BaseActivity;
import com.soapp.chat_class.group_add_member.AddListController;
import com.soapp.chat_class.group_chat.GroupChatLog;
import com.soapp.chat_class.group_chat.details.events.GroupChatEventsAdapter;
import com.soapp.chat_class.group_chat.details.grp_mem.GroupChatMemAdapter;
import com.soapp.chat_class.group_chat.details.images.GroupChatImgAdapter;
import com.soapp.global.ChatHelper;
import com.soapp.global.GlideAPI.GlideApp;
import com.soapp.global.GlobalVariables;
import com.soapp.global.ImagePreviewByte;
import com.soapp.global.MediaGallery.OpenGallery;
import com.soapp.global.MiscHelper;
import com.soapp.global.Preferences;
import com.soapp.global.UIHelper;
import com.soapp.setup.Soapp;
import com.soapp.sql.DatabaseHelper;
import com.soapp.sql.room.entity.Appointment;
import com.soapp.sql.room.entity.ContactRoster;
import com.soapp.sql.room.entity.Message;
import com.soapp.sql.room.joiners.GrpMemList;
import com.soapp.xmpp.GroupChatHelper.GroupChatStanza;
import com.soapp.xmpp.PubsubHelper.PubsubNodeCall;
import com.soapp.xmpp.SmackHelper;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import io.fabric.sdk.android.services.concurrency.AsyncTask;
import io.github.rockerhieu.emojicon.EmojiconTextView;
import okhttp3.MediaType;
import okhttp3.RequestBody;

/* Created by chang on 13/08/2017 . */

public class GroupChatDetail extends BaseActivity implements View.OnClickListener {
    //debug
    String tag = "diao";
    String className = getClass().getSimpleName();
    ///ryan
    private byte[] grp_profile_image_thumb;
    private byte[] grp_profile_image_full;
    boolean wasFullImageNull = false;
    ///ryan
    List<Message> list = new ArrayList<>();

    //public statics
    //self admin status (for holder admin actions)
    public static int selfAdminStatus;
    //jid for holder access - other than that need to change to liveData soon for grp details updating
    public static String jid, groupName;
    //basics
    private static DatabaseHelper databaseHelper = DatabaseHelper.getInstance();
    //static for static listener
    private static ImageView grp_profile_grp_img;
    //progress dialog
    private static ProgressDialog progressDialog;
    //old variables for grp title and img change - need to tidy up with LiveData soon
    private Preferences preferences = Preferences.getInstance();
    private UIHelper uiHelper = new UIHelper();
    //stanzas
    private GroupChatStanza groupChatStanza = new GroupChatStanza();
    private PubsubNodeCall pubsubNodeCall = new PubsubNodeCall();
    //variables
    private String userJid;
    //elements
    private ImageView grp_profile_self_img, grp_profile_edit_grpname;
    private EmojiconTextView grp_profile_self_name, grp_profile_grp_name;
    private LinearLayout grp_profile_view_all_mem;
    private TextView tv_grp_in_grp, grp_profile_self_admin, tv_clear_chat, tv_leave_grp, grp_profile_add_mem,
            grp_View_All, grp_profile_view_all_tv;
    //recyclerviews
    //shared contents
    private RecyclerView grp_profile_shared_content_rv;
    private GroupChatImgAdapter groupChatImageAdapter;
    //grp mem
    private RecyclerView grp_profile_member_rv;
    private GroupChatMemAdapter groupChatMemAdapter;
    //grp events
    private RecyclerView grp_profile_events_rv;
    private GroupChatEventsAdapter groupChatEventsAdapter;
    private String from;
    public static String newGrpName;

    public static void groupProfileChangeListener(String roomJid) {
        byte[] profileImgByte = databaseHelper.getImageBytesThumbFromContactRoster(roomJid);

        grp_profile_grp_img.setImageResource(R.drawable.grp_propic_circle_150px);

        if (profileImgByte != null) {
            GlideApp.with(Soapp.getInstance().getApplicationContext())
                    .asBitmap()
                    .load(profileImgByte)
                    .placeholder(R.drawable.grp_propic_circle_150px)
                    .thumbnail(0.5f)
                    .encodeQuality(50)
                    .transforms(new CircleCrop())
                    .transition(BitmapTransitionOptions.withCrossFade())
//                    .override(600, 600)
                    .into(grp_profile_grp_img).waitForLayout();
        }
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.chat_profile_grp);
        setupToolbar();
        setTitle("");
        //get variable values from intent and preferences
        jid = getIntent().getStringExtra("jid");
        from = getIntent().getStringExtra("from");

        userJid = preferences.getValue(this, GlobalVariables.STRPREF_USER_ID);

        //init variables for elements
        grp_profile_grp_name = findViewById(R.id.grp_profile_grp_name);
        grp_profile_grp_img = findViewById(R.id.grp_profile_grp_img);
        grp_profile_shared_content_rv = findViewById(R.id.grp_profile_shared_content_rv);
        grp_profile_self_img = findViewById(R.id.grp_profile_self_img);
        grp_profile_self_name = findViewById(R.id.grp_profile_self_name);
        grp_profile_self_admin = findViewById(R.id.grp_profile_self_admin);
        grp_profile_add_mem = findViewById(R.id.grp_profile_add_mem);
        grp_profile_member_rv = findViewById(R.id.grp_profile_member_rv);
        grp_profile_edit_grpname = findViewById(R.id.grp_profile_edit_grpname);
        tv_grp_in_grp = findViewById(R.id.tv_grp_in_grp);
        tv_clear_chat = findViewById(R.id.tv_clear_chat);
        tv_leave_grp = findViewById(R.id.tv_leave_grp);
        grp_profile_view_all_mem = findViewById(R.id.grp_profile_view_all_mem);
        grp_profile_view_all_tv = findViewById(R.id.grp_profile_view_all_tv);
        grp_profile_events_rv = findViewById(R.id.grp_profile_events_rv);
        grp_View_All = findViewById(R.id.grp_View_All);

        //get group profile from sqlite
        List<AddMember> grpProfileList = databaseHelper.get_grpProfile(jid);
        groupName = grpProfileList.get(0).getDisplayname();

        //set grp profile
//        byte[] byteArray = databaseHelper.getImageBytesFullFromContactRoster(jid);
//        if (byteArray != null) {
//            byteArray = databaseHelper.SPdeleteDefaultImage(byteArray, jid, true, false);
//            GlideApp.with(this)
//                    .load(byteArray)
//                    .placeholder(R.drawable.default_propic_small_round)
//                    .dontTransform()
//                    .apply(RequestOptions.circleCropTransform())
//                    .into(grp_profile_grp_img);
//        }
//        else {
//
//        }
        //
        liveDataforProfileImageAndGroupName();

        grp_profile_grp_img.setOnClickListener(this);

        Animation myFadeInAnimation = AnimationUtils.loadAnimation(GroupChatDetail.this, R.anim.fadein);
        grp_profile_grp_img.startAnimation(myFadeInAnimation);

        grp_View_All.setOnClickListener(this);
        grp_profile_grp_name.setOnClickListener(this);
        grp_profile_edit_grpname.setOnClickListener(this);

        //shared content (images for now)
        groupChatImageAdapter = new GroupChatImgAdapter();

        LinearLayoutManager llmImg = new LinearLayoutManager(this);
        llmImg.setOrientation(LinearLayoutManager.HORIZONTAL);

        grp_profile_shared_content_rv.setLayoutManager(llmImg);
        grp_profile_shared_content_rv.setItemAnimator(null);

        LiveData<List<Message>> grpChatDetImgList = Soapp.getDatabase().selectQuery().load_chatDetImageVideo(jid);
        grpChatDetImgList.observe(this, msgImgList -> {
            //only show "view all" if got images
            if (msgImgList != null && msgImgList.size() > 0) {
                grp_View_All.setVisibility(View.VISIBLE);
                grp_View_All.setOnClickListener(this);
            } else {
                grp_View_All.setVisibility(View.GONE);
            }

            list = msgImgList;

            groupChatImageAdapter.submitList(msgImgList);
            groupChatImageAdapter.notifyDataSetChanged();

        });
        grp_profile_shared_content_rv.setAdapter(groupChatImageAdapter);

        //grp members
        //self profile img
        byte[] selfProfileImgBytes = databaseHelper.getImageBytesThumbFromContactRoster(userJid);

        if (selfProfileImgBytes != null) {
            GlideApp.with(this)
                    .asBitmap()
                    .load(selfProfileImgBytes)
                    .placeholder(R.drawable.grp_propic_circle_520px)
                    .thumbnail(0.5f)
                    .encodeQuality(50)
                    .apply(RequestOptions.circleCropTransform())
                    .transition(BitmapTransitionOptions.withCrossFade())
                    .override(180, 180)
                    .into(grp_profile_self_img).waitForLayout();
        }
        grp_profile_self_img.setOnClickListener(this);

        //self profile name
        String user_displayname = preferences.getValue(this, GlobalVariables.STRPREF_USERNAME);
        grp_profile_self_name.setText(user_displayname);

        //self admin status
        LiveData<Integer> liveDataAdminStatus = Soapp.getDatabase().selectQuery().getSelfAdminStatus(jid);
        liveDataAdminStatus.observe(this, memberList -> {
            if (liveDataAdminStatus.getValue() != null && liveDataAdminStatus.getValue() == 1) { //self is admin
                //self admin status
                selfAdminStatus = liveDataAdminStatus.getValue();
                grp_profile_self_admin.setVisibility(View.VISIBLE);

                //add more members button
                grp_profile_add_mem.setOnClickListener(GroupChatDetail.this);
                grp_profile_add_mem.setVisibility(View.VISIBLE);
            } else { //self is NOT admin
                selfAdminStatus = 0;

                grp_profile_self_admin.setVisibility(View.GONE);
                grp_profile_add_mem.setVisibility(View.GONE);
            }
        });

        //recyclerview for grp mem
        groupChatMemAdapter = new GroupChatMemAdapter();
        groupChatMemAdapter.setHasStableIds(true);

        LinearLayoutManager llmGrpMem = new LinearLayoutManager(this);
        llmGrpMem.setOrientation(RecyclerView.VERTICAL);

        grp_profile_member_rv.setLayoutManager(llmGrpMem);
        grp_profile_member_rv.setItemAnimator(null);

        //for smooth scrolling of rv in scrollview
        grp_profile_member_rv.setNestedScrollingEnabled(false);

        LiveData<List<GrpMemList>> list = Soapp.getDatabase().selectQuery().load_grpMemChatDetail(jid);
        list.observe(this, memberList -> {


            groupChatMemAdapter.submitList(memberList);
            groupChatMemAdapter.notifyDataSetChanged();

            //only show "view all" for grp mem if more than count
            if (memberList != null && memberList.size() > 3) {
                grp_profile_view_all_mem.setVisibility(View.VISIBLE);
            } else {
                grp_profile_view_all_mem.setVisibility(View.GONE);
            }

        });
        grp_profile_view_all_mem.setOnClickListener(this);

        grp_profile_member_rv.setAdapter(groupChatMemAdapter);

        //grp events
        groupChatEventsAdapter = new GroupChatEventsAdapter();

        LinearLayoutManager llmEvents = new LinearLayoutManager(this);
        llmEvents.setOrientation(LinearLayoutManager.HORIZONTAL);

        grp_profile_events_rv.setLayoutManager(llmEvents);
        grp_profile_events_rv.setItemAnimator(null);

        LiveData<List<Appointment>> grpChatEventsLD = Soapp.getDatabase().selectQuery().load_scheLog(jid);
        grpChatEventsLD.observe(this, appointmentList -> {
            groupChatEventsAdapter.submitList(appointmentList);
            groupChatEventsAdapter.notifyDataSetChanged();
        });
        grp_profile_events_rv.setAdapter(groupChatEventsAdapter);

        //for grp settings items
        tv_grp_in_grp.setOnClickListener(this);
        tv_clear_chat.setOnClickListener(this);
        tv_leave_grp.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            //grp profile img - go to edit group profile activity
            case (R.id.grp_profile_grp_img):
//                if (grp_profile_image_thumb != null) {
                    Intent profileintent = new Intent(this, GroupProfilePic.class);
                    profileintent.putExtra("from", "GroupDetail");
                    profileintent.putExtra("jid", jid);
                    startActivity(profileintent);
//                } else { //show options to choose img (camera or gallery) since no img
//                    new UIHelper().showCameraGalleryPopup(this, "GroupDetail", "GroupDetailMedia");

//                    Toast.makeText(GroupChatDetail.this, "No profile photo", Toast.LENGTH_SHORT).show();
//                }
                break;

            //edit grp name
            case (R.id.grp_profile_grp_name):
            case (R.id.grp_profile_edit_grpname):
                //show dialogbox for editing grp name
                editGrpNameDialog();
                break;

            //self profile img - go to full screen img preview
            case (R.id.grp_profile_self_img):
                Intent myimageintent = new Intent(GroupChatDetail.this, ImagePreviewByte.class);
                myimageintent.putExtra("jid", userJid);

                startActivity(myimageintent);
                break;

            case (R.id.grp_profile_view_all_mem):
                if (groupChatMemAdapter.getShowAllGrpMem()) { //showing all grp mem
                    grp_profile_view_all_tv.setText(R.string.view_all);
                    groupChatMemAdapter.setShowAllGrpMem(false);
                    groupChatMemAdapter.notifyDataSetChanged();
                } else { //not showing all grp mem
                    grp_profile_view_all_tv.setText(R.string.view_less);
                    groupChatMemAdapter.setShowAllGrpMem(true);
                    groupChatMemAdapter.notifyDataSetChanged();
                }
                break;

            //add grp mem
            case (R.id.grp_profile_add_mem):
                Intent AddMember = new Intent(this, AddListController.class);
                AddMember.putExtra("jid", jid);
                AddMember.putExtra("from", "addMember");

                startActivity(AddMember);
                break;

            //grp in grp button
            case (R.id.tv_grp_in_grp):
                Intent grpInGrp = new Intent(this, AddListController.class);
                grpInGrp.putExtra("jid", jid);
                grpInGrp.putExtra("from", "grpInGrpChat");

                startActivity(grpInGrp);
                break;

            //clear chat
            case (R.id.tv_clear_chat):
                Runnable clearChatAction = () -> databaseHelper.clearMsgesInMsg(jid);

                uiHelper.dialog2Btns(this, getString(R.string.clear_chat), getString(R.string.clear_chat_msg),
                        R.string.clear, R.string.cancel, R.color.white, R.color.primaryDark3,
                        clearChatAction, null, true);
                break;

            //leave grp
            case (R.id.tv_leave_grp):
                Runnable leaveGrpAction = () -> {
                    if (SmackHelper.getXMPPConnection() != null && SmackHelper.getXMPPConnection().isAuthenticated()) {
                        databaseHelper.selfLeaveDeleteRoom(jid, false);
                    } else {
                        Toast.makeText(GroupChatDetail.this, R.string.xmpp_waiting_connection,
                                Toast.LENGTH_SHORT).show();
                    }
                };

                uiHelper.dialog2Btns(this, getString(R.string.leave_grp), getString(R.string.leave_grp_msg),
                        R.string.leave_grp, R.string.cancel, R.color.white, R.color.primaryDark3,
                        leaveGrpAction, null, true);
                break;


            //view all image
            case R.id.grp_View_All:
                Intent viewAllGrp = new Intent(this, OpenGallery.class);

                viewAllGrp.putExtra("FROM", "allView");
                viewAllGrp.putExtra("title", "Images");
                viewAllGrp.putExtra("viewAllImg", "group");

                OpenGallery.listAllView = GroupChatImgAdapter.grpChatImgList;
                startActivity(viewAllGrp);
                break;


            default:
                break;
        }
    }

    private void editGrpNameDialog() {

        Runnable grpRunnble = () -> new asyncUpdateGrpName().execute();

        new UIHelper().editTextAlertDialog(this, getResources().getString(R.string.grp_title_edit),
                groupName, "GrpChatDetail", jid, null, true, grpRunnble);
    }

    //when update group profile [JAY Resource ID]
    private void updateGrpNameRetro() {
        //set required params first
        String access_token = preferences.getValue(GroupChatDetail.this, GlobalVariables.STRPREF_ACCESS_TOKEN);
        String selfUsername = preferences.getValue(GroupChatDetail.this, GlobalVariables.STRPREF_USERNAME);
        String pushMsg = selfUsername + " " + getString(R.string.updated_grp_profile);

        RequestBody name = RequestBody.create(MediaType.parse("text/plain"), newGrpName);
        RequestBody roomid = RequestBody.create(MediaType.parse("text/plain"), jid);

        //no new image chosen, ONLY UPDATE NAME
        //build retro for no image
        UpdateGroupProfileNameOnly client = RetrofitAPIClient.getClient().create(UpdateGroupProfileNameOnly.class);

        retrofit2.Call<GroupModel> call = client.editUser("Bearer " + access_token, name, roomid);

        call.enqueue(new retrofit2.Callback<GroupModel>() {
            @Override
            public void onResponse(retrofit2.Call<GroupModel> call, retrofit2.Response<GroupModel> response) {
                if (!response.isSuccessful()) {
                    new Handler(Looper.getMainLooper()).post(() -> {

                        new MiscHelper().retroLogUnsuc(response, "updateGrpNameRetro ", "JAY");
                        Toast.makeText(GroupChatDetail.this, R.string
                                .onresponse_unsuccessful, Toast.LENGTH_SHORT).show();
                        if (progressDialog != null && progressDialog.isShowing()) {
                            progressDialog.dismiss();
                        }
                    });
                    return;
                }

                //pubsub to others
                pubsubNodeCall.PubsubGroup(jid, userJid, UUID.randomUUID().toString(), jid, "");
//                groupChatStanza.GroupStatusUpdateMsg(jid, userJid, selfUsername, pushMsg, UUID.randomUUID().toString()
//                        , groupName);

                if (GroupChatLog.displayName != null) {
                    GroupChatLog.displayName = newGrpName;
                    grp_profile_grp_name.setText(newGrpName);
                }

                //ONLY update name to db since no change image
                databaseHelper.selfUpdateGrpProfile(null, null, newGrpName, jid);

                new Handler(Looper.getMainLooper()).post(() -> {
                    if (progressDialog != null && progressDialog.isShowing()) {
                        progressDialog.dismiss();
                    }
                });
            }

            @Override
            public void onFailure(retrofit2.Call<GroupModel> call, Throwable t) {
                new Handler(Looper.getMainLooper()).post(() -> {
                    new MiscHelper().retroLogFailure(t, "updateGrpNameRetro ", "JAY");
                    Toast.makeText(GroupChatDetail.this, R.string.onfailure, Toast.LENGTH_SHORT).show();
                    if (progressDialog != null && progressDialog.isShowing()) {
                        progressDialog.dismiss();
                    }
                });
            }
        });
    }

    private class asyncUpdateGrpName extends AsyncTask<String, Integer, Void> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            //Display progressDialog before download starts
            progressDialog = ProgressDialog.show(GroupChatDetail.this, null, getString(R.string.updating_grp_profile));
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
        }

        @Override
        protected Void doInBackground(String... params) {
            updateGrpNameRetro();

            return null;
        }
    }

    public static void loadImageWhenRdyListenser(byte[] byteArray) {
        if (byteArray != null) {
            GlideApp.with(Soapp.getInstance().getApplicationContext())
                    .load(byteArray)
                    .dontTransform()
                    .apply(RequestOptions.circleCropTransform())
                    .into(grp_profile_grp_img);
        }
    }

    @Override
    protected void onDestroy() {
        jid = null;
        groupName = null;
        GroupChatImgAdapter.grpChatImgList = null;

        super.onDestroy();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    public void liveDataforProfileImageAndGroupName() {
        LiveData<ContactRoster> live_grpProfileList = Soapp.getDatabase().selectQuery().live_get_cr_profile(jid);
        live_grpProfileList.observe(this, grpProfile_list -> {
            //
            byte[] new_grp_profile_image_full = grpProfile_list.getProfilefull();
            byte[] new_grp_profile_image_thumb = grpProfile_list.getProfilephoto();
            grp_profile_image_full = new_grp_profile_image_full;
            grp_profile_image_thumb = new_grp_profile_image_thumb;

            String new_group_name = grpProfile_list.getDisplayname();
            String new_photo_url = grpProfile_list.getPhotourl();
            //

            boolean isSameName = new_group_name.equals(groupName);
            grp_profile_grp_name.setText(new_group_name);
            groupName = new_group_name;
            //
            boolean isSameThumbImage = Arrays.equals(new_grp_profile_image_thumb, grp_profile_image_thumb);

            if (new_grp_profile_image_full == null) {
                if (new_photo_url != null) {
                    new ChatHelper().downloadFromUrl(new_photo_url, jid, null);
                } else {
                    new ChatHelper().resourceRetro(GroupChatDetail.this, jid);
                }
            }

            if (isSameName) {
                RequestBuilder<Bitmap> thumbnailRequest =
                        GlideApp.with(GroupChatDetail.this)
                                .asBitmap()
                                .load(new_grp_profile_image_thumb)
                                .transition(BitmapTransitionOptions.withCrossFade())
                                .apply(RequestOptions.circleCropTransform());

                GlideApp.with(GroupChatDetail.this)
                        .asBitmap()
                        .load(new_grp_profile_image_full)
                        .thumbnail(thumbnailRequest)
                        .apply(RequestOptions.circleCropTransform())
                        .into(new SimpleTarget<Bitmap>(Target.SIZE_ORIGINAL, Target.SIZE_ORIGINAL) {
                            @Override
                            public void onLoadFailed(@Nullable Drawable errorDrawable) {
                                super.onLoadFailed(errorDrawable);
                                grp_profile_grp_img.setImageDrawable(getDrawable(R.drawable.grp_propic_circle_520px));
                            }

                            @Override
                            public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                                grp_profile_grp_img.setImageBitmap(resource);
                            }
                        });
            }
        });
    }

    private Bitmap getCroppedBitmap(Bitmap bitmap, Integer cx, Integer cy, Integer radius) {
        int diam = radius << 1;
        Bitmap targetBitmap = Bitmap.createBitmap(diam, diam, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(targetBitmap);
        final int color = 0xff424242;
        final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        canvas.drawARGB(0, 0, 0, 0);
        paint.setColor(color);
        canvas.drawCircle(radius, radius, radius, paint);
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(bitmap, -cx + radius, -cy + radius, paint);
        return targetBitmap;
    }
}
