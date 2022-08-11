package com.soapp.chat_class.single_chat.details;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.RequestBuilder;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;
import com.soapp.R;
import com.soapp.SoappModel.AddMember;
import com.soapp.base.BaseActivity;
import com.soapp.chat_class.single_chat.IndiChatLog;
import com.soapp.chat_class.single_chat.details.events.IndiChatEventsAdapter;
import com.soapp.chat_class.single_chat.details.grps.IndiChatGrpsAdapter;
import com.soapp.chat_class.single_chat.details.images.IndiChatImgAdapter;
import com.soapp.global.AddContactHelper;
import com.soapp.global.ChatHelper;
import com.soapp.global.GlideAPI.GlideApp;
import com.soapp.global.GlobalVariables;
import com.soapp.global.ImageFullView;
import com.soapp.global.MediaGallery.OpenGallery;
import com.soapp.global.Preferences;
import com.soapp.global.UIHelper;
import com.soapp.setup.Soapp;
import com.soapp.sql.DatabaseHelper;
import com.soapp.sql.room.entity.Appointment;
import com.soapp.sql.room.entity.ContactRoster;
import com.soapp.sql.room.entity.Message;
import com.soapp.xmpp.PubsubHelper.PubsubNodeCall;

import java.util.Arrays;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.lifecycle.LiveData;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

/* Created by chang on 13/08/2017. */

public class IndiChatDetail extends BaseActivity implements View.OnClickListener {
    //
    byte[] indi_profile_image_full;
    byte[] indi_profile_image_thumb;
    //public static jid for access from holder
    public static String jid;
    //stanza
    PubsubNodeCall pubsubNodeCall = new PubsubNodeCall();
    //basics
    private Preferences preferences = Preferences.getInstance();
    private DatabaseHelper databaseHelper = DatabaseHelper.getInstance();
    private UIHelper uiHelper = new UIHelper();
    //elements
    private static ImageView indi_profile_img;
    private TextView indi_profile_name, indi_profile_number, tv_clear_chat, tv_block_user,
            tv_add_to_contact, tv_msg_user, tv_call_user, tv_sharedcontent, tv_indi_events, tv_common_grps,
            indi_profile_view_all_common, share_image_vid_view_all;
    //variables
    private String displayName, phoneNumber;

    //recyclerviews
    //shared contents
    private RecyclerView indi_profile_events_rv;
    private IndiChatImgAdapter indiChatImgAdapter;

    //common grps
    private RecyclerView indi_profile_common_grps_rv;
    private IndiChatGrpsAdapter indiChatGrpsAdapter;

    //common events
    private RecyclerView indi_profile_shared_content_rv;
    private IndiChatEventsAdapter indiChatEventsAdapter;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.chat_profile_indi);
        setupToolbar();
        setTitle("");

        jid = getIntent().getStringExtra("jid");

        //profile
        indi_profile_img = findViewById(R.id.indi_profile_img);
        indi_profile_name = findViewById(R.id.indi_profile_name);
        indi_profile_number = findViewById(R.id.indi_profile_number);

        //recyclerviews
        //shared contents
        indi_profile_shared_content_rv = findViewById(R.id.indi_profile_shared_content_rv);
        tv_sharedcontent = findViewById(R.id.tv_sharedcontent);
        share_image_vid_view_all = findViewById(R.id.share_image_vid_view_all);
        //common grps
        indi_profile_common_grps_rv = findViewById(R.id.indi_profile_common_grps_rv);
        indi_profile_view_all_common = findViewById(R.id.indi_profile_view_all_common);
        tv_common_grps = findViewById(R.id.tv_common_grps);

        //common events
        indi_profile_events_rv = findViewById(R.id.indi_profile_events_rv);
        tv_indi_events = findViewById(R.id.tv_indi_events);

        //settings
        tv_add_to_contact = findViewById(R.id.tv_add_to_contact);
        tv_msg_user = findViewById(R.id.tv_msg_user);
        tv_call_user = findViewById(R.id.tv_call_user);
        tv_clear_chat = findViewById(R.id.tv_clear_chat);
        tv_block_user = findViewById(R.id.tv_block_user);

        //set onclick listeners
        tv_add_to_contact.setOnClickListener(this);
        tv_msg_user.setOnClickListener(this);
        tv_call_user.setOnClickListener(this);
        tv_clear_chat.setOnClickListener(this);
        tv_block_user.setOnClickListener(this);

        //show/hide add to contacts depending on added or not
        if (databaseHelper.getPhoneNameExist(jid) > 0) { //contact already exists
            tv_add_to_contact.setVisibility(View.GONE);
        } else { //contact doesnt exist
            tv_add_to_contact.setVisibility(View.VISIBLE);
            tv_add_to_contact.setOnClickListener(this);
        }
        //show/hide block/unblock depending on status
        if (databaseHelper.getDisabledStatus(jid) == 1) { //blocked
            tv_block_user.setText(R.string.unblock_user);
            tv_block_user.setTextColor(getResources().getColor(R.color.primaryDark3));
        } else { //not blocked
            tv_block_user.setText(R.string.block_user);
            tv_block_user.setTextColor(getResources().getColor(R.color.red));
        }

        //get user profile from sqlite
        List<AddMember> userProfileList = databaseHelper.get_userProfile(jid);

        displayName = userProfileList.get(0).getDisplayname();
        phoneNumber = userProfileList.get(0).getPhonenumber();

//        byte[] friendImage = userProfileList.get(0).getProfilephoto();
//
//        if (friendImage == null) { //no image byte, load default then try download
//            Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.default_img_240);
//            friendImage = DirectoryHelper.getBytesFromBitmap33(bitmap);
//
//            String imageURL = databaseHelper.getResourceURLFromContactRoster(jid);
//
//            //try download
//            if (imageURL != null && !imageURL.equals("")) {
//                databaseHelper.saveFullOnlyFromImgURL(imageURL, jid, "IndiChatDetail");
//            } else {
//                databaseHelper.saveProfileImgBytesFromResourceID(jid, jid, "IndiChatDetail");
//            }
//        }

        //set friend's profile name and image here
//        indi_profile_name.setText(displayName);
//        indi_profile_number.setText(phoneNumber);
        //check has full image
//        byte[] byteArray = databaseHelper.getImageBytesFullFromContactRoster(jid);
//        if (byteArray != null) {
//            databaseHelper.SPdeleteDefaultImage(byteArray, jid,true, false);
//            GlideApp.with(this)
//                    .load(byteArray)
//                    .dontTransform()
//                    .apply(RequestOptions.circleCropTransform())
//                    .into(indi_profile_img);
//        } else {
//            GlideApp.with(this)
//                    .load(friendImage)
//                    .placeholder(R.drawable.default_propic_small_round)
//                    .thumbnail(0.5f)
//                    .encodeQuality(50)
//                    .apply(RequestOptions.circleCropTransform())
//                    .transition(withCrossFade())
//                    .override(180, 180)
//                    .into(indi_profile_img);
//            if (jid != null) {
//                String access_token = Preferences.getInstance().getValue(this, GlobalVariables.STRPREF_ACCESS_TOKEN);
//                Resource1v3Interface resource1v3Interface = RetrofitAPIClient.getClient().create(Resource1v3Interface.class);
//                retrofit2.Call<Resource1v3Model> call = resource1v3Interface.getResource(jid, "Bearer " + access_token);
//                call.enqueue(new retrofit2.Callback<Resource1v3Model>() {
//                    @Override
//                    public void onResponse(retrofit2.Call<Resource1v3Model> call, retrofit2.Response<Resource1v3Model> response) {
//                        if (!response.isSuccessful()) {
//                            new MiscHelper().retroLogUnsuc(IndiChatDetail.this, response, "downloadProfileImg", "JAY");
//
//                            Toast.makeText(IndiChatDetail.this, R.string.onresponse_unsuccessful, Toast.LENGTH_SHORT).show();
//                            return;
//                        }
//                        String resourceURL = response.body().getResource_url();
//
//                        if (resourceURL != null && !resourceURL.equals("") && !resourceURL.equals("null")) {
//                            //try to download image from resource url and load to preview
//                            databaseHelper.saveFullOnlyFromImgURL(resourceURL, jid, "indi_chat_detail");
//                        }
//                    }
//
//                    @Override
//                    public void onFailure(retrofit2.Call<Resource1v3Model> call, Throwable t) {
//                        new MiscHelper().retroLogFailure(IndiChatDetail.this, t, "downloadProfileImg ", "JAY");
//                        Toast.makeText(IndiChatDetail.this, R.string.onfailure, Toast.LENGTH_SHORT).show();
//                    }
//                });
//            }
//        }

        indi_profile_img.setOnClickListener(this);

        //shared content
        Animation myFadeInAnimation = AnimationUtils.loadAnimation(IndiChatDetail.this, R.anim.fadein);
        indi_profile_img.startAnimation(myFadeInAnimation);

        share_image_vid_view_all.setOnClickListener(this);

        indiChatImgAdapter = new IndiChatImgAdapter();

        LinearLayoutManager llmImg = new LinearLayoutManager(this);
        llmImg.setOrientation(LinearLayoutManager.HORIZONTAL);

        indi_profile_shared_content_rv.setLayoutManager(llmImg);
        indi_profile_shared_content_rv.setItemAnimator(null);

        LiveData<List<Message>> indiChatDetImgLD = Soapp.getDatabase().selectQuery().load_chatDetImageVideo(jid);
        indiChatDetImgLD.observe(this, indiChatImgList -> {
            //only show "view all" if got images
            if (indiChatImgList != null && indiChatImgList.size() > 0) {
                share_image_vid_view_all.setVisibility(View.VISIBLE);
                share_image_vid_view_all.setOnClickListener(this);
            } else {
                share_image_vid_view_all.setVisibility(View.GONE);
            }

            indiChatImgAdapter.submitList(indiChatImgList);
            indiChatImgAdapter.notifyDataSetChanged();

        });
        indi_profile_shared_content_rv.setAdapter(indiChatImgAdapter);

        //common grps
        indiChatGrpsAdapter = new IndiChatGrpsAdapter();
        indiChatGrpsAdapter.setHasStableIds(true);

        LinearLayoutManager llmGrps = new LinearLayoutManager(this);
        llmGrps.setOrientation(RecyclerView.VERTICAL);

        indi_profile_common_grps_rv.setLayoutManager(llmGrps);
        indi_profile_common_grps_rv.setItemAnimator(null);

        //for smooth scrolling of rv in scrollview
        indi_profile_common_grps_rv.setNestedScrollingEnabled(false);

        LiveData<List<ContactRoster>> indiChatCommonGrpsLD = Soapp.getDatabase().selectQuery().load_commonGrpsChatDetail(jid);
        indiChatCommonGrpsLD.observe(this, indiChatCommonGrpsList -> {
            indiChatGrpsAdapter.submitList(indiChatCommonGrpsList);
            indiChatGrpsAdapter.notifyDataSetChanged();

            //only show "view all" for grp mem if more than count
            if (indiChatCommonGrpsList != null && indiChatCommonGrpsList.size() > 3) {
                tv_common_grps.setVisibility(View.VISIBLE);
                indi_profile_common_grps_rv.setVisibility(View.VISIBLE);
                indi_profile_view_all_common.setVisibility(View.VISIBLE);
            } else {
                tv_common_grps.setVisibility(View.VISIBLE);
                indi_profile_common_grps_rv.setVisibility(View.VISIBLE);
                indi_profile_view_all_common.setVisibility(View.GONE);
            }
        });
        indi_profile_view_all_common.setOnClickListener(this);

        indi_profile_common_grps_rv.setAdapter(indiChatGrpsAdapter);

        //common events
        indiChatEventsAdapter = new IndiChatEventsAdapter();

        LinearLayoutManager llmEvents = new LinearLayoutManager(this);
        llmEvents.setOrientation(LinearLayoutManager.HORIZONTAL);

        indi_profile_events_rv.setLayoutManager(llmEvents);
        indi_profile_events_rv.setItemAnimator(null);

        LiveData<List<Appointment>> indiChatEventsLD = Soapp.getDatabase().selectQuery().load_scheLog(jid);
        indiChatEventsLD.observe(this, indiChatEventsList -> {
            indiChatEventsAdapter.submitList(indiChatEventsList);
            indiChatEventsAdapter.notifyDataSetChanged();
        });
        indi_profile_events_rv.setAdapter(indiChatEventsAdapter);
        //
        liveDataforProfileImageAndName();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            //friend's profile img
            case (R.id.indi_profile_img):
                Intent intentProfileImage = new Intent(IndiChatDetail.this, ImageFullView.class);
//                intentProfileImage.putExtra("jid", jid);
//                byte[] byteArray = databaseHelper.getImageBytesFullFromContactRoster(jid);
                intentProfileImage.putExtra("byteArray", indi_profile_image_full);
                intentProfileImage.putExtra("byteArrayThumb", indi_profile_image_thumb);
                startActivity(intentProfileImage);
                break;

            case (R.id.indi_profile_view_all_common):
                if (indiChatGrpsAdapter.getShowAllCommonGrps()) { //showing all grp mem
                    indiChatGrpsAdapter.setShowAllCommonGrps(false);
                } else { //not showing all grp mem
                    indiChatGrpsAdapter.setShowAllCommonGrps(true);
                }
                indiChatGrpsAdapter.notifyDataSetChanged();
                break;

            //add to contact
            case (R.id.tv_add_to_contact):
                Runnable addContactAction = () -> {
                    //add to phonebook first
                    new AddContactHelper(IndiChatDetail.this).createContactPhoneBook(GlobalVariables.string1, phoneNumber);

                    //update displayname to sqlite phonename
                    databaseHelper.updateNewContactToContactRoster(jid, phoneNumber, GlobalVariables.string1, null, null);
                    indi_profile_name.setText(GlobalVariables.string1);

                    GlobalVariables.string1 = null;

                    //refresh UI
                    tv_add_to_contact.setVisibility(View.GONE);

                    Toast.makeText(IndiChatDetail.this, R.string.contact_added_soapp, Toast.LENGTH_SHORT).show();
                };

                new UIHelper().dialog2Btns2Eview(this,
                        getString(R.string.add_contacts),
                        displayName,
                        phoneNumber,
                        addContactAction,
                        null,
                        true);
                break;

            //msg friend
            case (R.id.tv_msg_user):
                Intent messageIntent = new Intent(this, IndiChatLog.class);

                messageIntent.putExtra("jid", jid);

                startActivity(messageIntent);
                break;

            //call friend
            case (R.id.tv_call_user):
                Runnable callUserAction = () -> {
                    Intent callIntent = new Intent(Intent.ACTION_CALL);
                    callIntent.setData(Uri.parse("tel:" + indi_profile_number.getText().toString()));

                    if (ActivityCompat.checkSelfPermission(IndiChatDetail.this, Manifest.permission.CALL_PHONE)
                            != PackageManager.PERMISSION_GRANTED) {
                        return;
                    }
                    startActivity(callIntent);
                };

                new UIHelper().dialog2Btns(this, getString(R.string.contact_call),
                        getString(R.string.contact_call_msg), R.string.ok_label, R.string.cancel,
                        R.color.white, R.color.black,
                        callUserAction, null, true);
                break;

            //clear chat
            case (R.id.tv_clear_chat):
                Runnable clearChatAction = () -> databaseHelper.clearMsgesInMsg(jid);

                uiHelper.dialog2Btns(this, getString(R.string.clear_chat), getString(R.string.clear_chat_msg),
                        R.string.clear, R.string.cancel, R.color.white, R.color.primaryDark3,
                        clearChatAction, null, true);
                break;

            //block user
            case (R.id.tv_block_user):
                long currentDate = System.currentTimeMillis();

                if (databaseHelper.getDisabledStatus(jid) == 0) { //user not blocked, block user
                    Runnable blockUserAction = () -> {
                        String user_jid = preferences.getValue(IndiChatDetail.this, GlobalVariables.STRPREF_USER_ID);

                        pubsubNodeCall.block(jid, user_jid);
                        databaseHelper.blockUnblockIndi(jid, IndiChatDetail.this.getString(R.string.blocked_user),
                                currentDate, 1);
                        //update UI to "unblock user"
                        tv_block_user.setText(R.string.unblock_user);
                        tv_block_user.setTextColor(getResources().getColor(R.color.primaryDark3));
                    };

                    uiHelper.dialog2Btns(this, getString(R.string.block_user), getString(R.string.block_user_msg),
                            R.string.block, R.string.cancel, R.color.white, R.color.primaryDark3,
                            blockUserAction, null, true);
                } else { //user blocked, unblock user
                    Runnable unblockUserAction = () -> {

                        pubsubNodeCall.unblock(jid);
                        databaseHelper.blockUnblockIndi(jid, IndiChatDetail.this.getString(R.string.unblocked_user),
                                currentDate, 0);
                        //update UI to "block user"
                        tv_block_user.setText(R.string.block_user);
                        tv_block_user.setTextColor(getResources().getColor(R.color.red));
                    };

                    uiHelper.dialog2Btns(this, getString(R.string.unblock_user), getString(R.string.unblock_contact),
                            R.string.unblock_user, R.string.cancel, R.color.white, R.color.primaryDark3,
                            unblockUserAction, null, true);
                }
                break;

            case R.id.share_image_vid_view_all:

                Intent imgVidViewAll = new Intent(this, OpenGallery.class);
                imgVidViewAll.putExtra("FROM", "allView");
                imgVidViewAll.putExtra("title", "Images");
                imgVidViewAll.putExtra("viewAllImg", "indi");
                OpenGallery.listAllView = IndiChatImgAdapter.chatImgList;
//                imgVidViewAll.putParcelableArrayListExtra("arrayMessage", indiChatImgAdapter.chatImgList);
                startActivity(imgVidViewAll);

                break;

            default:
                break;
        }
    }

    @Override
    protected void onDestroy() {
        jid = null;
        IndiChatImgAdapter.chatImgList = null;

        super.onDestroy();
    }

    public static void loadImageWhenRdyListenser(byte[] byteArray) {
        if (byteArray != null) {
            GlideApp.with(Soapp.getInstance().getApplicationContext())
                    .load(byteArray)
                    .dontTransform()
                    .apply(RequestOptions.circleCropTransform())
                    .into(indi_profile_img);
        }
    }

    public void liveDataforProfileImageAndName() {
        LiveData<ContactRoster> live_grpProfileList = Soapp.getDatabase().selectQuery().live_get_cr_profile(jid);
        live_grpProfileList.observe(this, indi_info -> {
            //
            byte[] new_indi_profile_image_full = indi_info.getProfilefull();
            indi_profile_image_full = new_indi_profile_image_full;
            byte[] new_indi_profile_image_thumb = indi_info.getProfilephoto();
            indi_profile_image_thumb = new_indi_profile_image_thumb;

            if(new_indi_profile_image_full == null){
                indi_profile_image_full = indi_profile_image_thumb;
            }
            //
            String new_photo_url = indi_info.getPhotourl();

            String displayname = indi_info.getDisplayname();
            String phone_number = indi_info.getPhonenumber();
            String phone_name = indi_info.getPhonename();
            indi_profile_number.setText(phone_number);
            //
            boolean isSameName;

            if (phone_name != null) {
                isSameName = displayName.equals(phone_name);
                displayName = phone_name;
            } else {
                String combineNamePhone = displayname + " " + phone_number;
                isSameName = displayName.equals(combineNamePhone);
                displayName = combineNamePhone;
            }
            indi_profile_name.setText(displayName);
            indi_profile_number.setText(phone_number);

            //
            boolean isSameThumbImage = Arrays.equals(new_indi_profile_image_thumb, indi_profile_image_thumb);
            boolean isSameFullImage = Arrays.equals(new_indi_profile_image_full, indi_profile_image_full);

            if(new_indi_profile_image_full == null){
                if(new_photo_url != null){
                    new ChatHelper().downloadFromUrl(new_photo_url, jid, null);
                } else {
                    new ChatHelper().resourceRetro(IndiChatDetail.this, jid);
                }
            }
            if(isSameName) {
                RequestBuilder<Bitmap> thumbnailRequest =
                        GlideApp.with(IndiChatDetail.this)
                        .asBitmap()
                        .load(new_indi_profile_image_thumb)
                        .apply(RequestOptions.circleCropTransform());

                GlideApp.with(IndiChatDetail.this)
                        .asBitmap()
                        .load(new_indi_profile_image_full)
                        .thumbnail(thumbnailRequest)
                        .apply(RequestOptions.circleCropTransform())
                        .into(new SimpleTarget<Bitmap>() {
                            @Override
                            public void onLoadFailed(@Nullable Drawable errorDrawable) {
                                super.onLoadFailed(errorDrawable);
                                    indi_profile_img.setImageDrawable(getDrawable(R.drawable.in_propic_circle_520px));
                            }

                            @Override
                            public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                                indi_profile_img.setImageBitmap(resource);
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
        canvas.drawBitmap(bitmap, -cx+radius, -cy+radius, paint);
        return targetBitmap;
    }
}