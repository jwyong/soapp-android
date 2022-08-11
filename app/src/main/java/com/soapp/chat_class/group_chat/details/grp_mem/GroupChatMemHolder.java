package com.soapp.chat_class.group_chat.details.grp_mem;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.provider.Settings;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.load.resource.bitmap.BitmapTransitionOptions;
import com.bumptech.glide.request.RequestOptions;
import com.soapp.R;
import com.soapp.SoappApi.ApiModel.AddRoomAdminModel;
import com.soapp.SoappApi.ApiModel.DeleteRoomMemberModel;
import com.soapp.SoappApi.Interface.DeleteRoomMember;
import com.soapp.SoappApi.Interface.GroupAddAdmin;
import com.soapp.SoappApi.RetrofitAPIClient;
import com.soapp.chat_class.group_chat.details.GroupChatDetail;
import com.soapp.chat_class.single_chat.IndiChatLog;
import com.soapp.chat_class.single_chat.details.IndiChatDetail;
import com.soapp.global.AddContactHelper;
import com.soapp.global.GlideAPI.GlideApp;
import com.soapp.global.GlobalVariables;
import com.soapp.global.ImagePreviewByte;
import com.soapp.global.MiscHelper;
import com.soapp.global.Preferences;
import com.soapp.global.UIHelper;
import com.soapp.sql.DatabaseHelper;
import com.soapp.sql.room.joiners.GrpMemList;
import com.soapp.xmpp.SingleChatHelper.SingleChatStanza;
import com.soapp.xmpp.SmackHelper;

import java.io.IOException;
import java.util.UUID;

import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.RecyclerView;
import io.github.rockerhieu.emojicon.EmojiconTextView;

/* Created by chang on 13/08/2017. */

public class GroupChatMemHolder extends RecyclerView.ViewHolder {
    //basics
    private Context context;
    private DatabaseHelper databaseHelper = DatabaseHelper.getInstance();
    private Preferences preferences = Preferences.getInstance();
    private UIHelper uiHelper = new UIHelper();

    //elements
    private ImageView profile_item_profile_img;
    private EmojiconTextView profile_item_profile_name;
    private TextView profile_item_online_status, profile_item_admin_status;

    GroupChatMemHolder(View itemView) {
        super(itemView);
        context = itemView.getContext();

        //set elements
        profile_item_profile_img = itemView.findViewById(R.id.profile_item_profile_img);
        profile_item_profile_name = itemView.findViewById(R.id.profile_item_profile_name);
//        profile_item_online_status = itemView.findViewById(R.id.profile_item_online_status);
        profile_item_admin_status = itemView.findViewById(R.id.profile_item_admin_status);
    }

    public void setData(final GrpMemList data) {
        //profile img
        GlideApp.with(itemView)
                .asBitmap()
                .load(data.getContactRoster().getProfilephoto())
                .placeholder(R.drawable.in_propic_circle_150px)
                .apply(RequestOptions.circleCropTransform())
                .transition(BitmapTransitionOptions.withCrossFade())
                .into(profile_item_profile_img);

        //set member jid to final variable
        final String memberJid = data.getContactRoster().getContactJid();

        profile_item_profile_img.setOnClickListener(v -> {
            Intent myimageintent = new Intent(context, ImagePreviewByte.class);
            myimageintent.putExtra("jid", memberJid);
//            byte[] byteArray = data.getContactRoster().getProfilefull();
//            myimageintent.putExtra("byteArray", byteArray);
//            myimageintent.putExtra("from", "grp");
            context.startActivity(myimageintent);
        });

        //phone number - for passing into intent then calling
        String phoneNumber = data.getContactRoster().getPhonenumber();

        //displayname
        String memberName = data.getContactRoster().getPhonename();
        String displayName = data.getContactRoster().getDisplayname();

        if (memberName == null) {
            memberName = displayName + " " + phoneNumber;
        }
        profile_item_profile_name.setText(memberName);

        //admin status
        if (data.getGroupMem() != null) {
            if (data.getGroupMem().getAdmin() == 1) {
                profile_item_admin_status.setVisibility(View.VISIBLE);
            } else {
                profile_item_admin_status.setVisibility(View.GONE);
            }
        }

        //go to indi profile on click
        itemView.setOnClickListener(view -> {
            Intent memberDetailsIntent = new Intent(context, IndiChatDetail.class);
            memberDetailsIntent.putExtra("jid", memberJid);

            context.startActivity(memberDetailsIntent);
        });

        //long click opens bottom sheet for grp mem options
        itemView.setOnLongClickListener(view -> {
            //init global runnable actions first
            //msg action
            Runnable msgAction = () -> {
                Intent messageIntent = new Intent(context, IndiChatLog.class);
                messageIntent.putExtra("jid", memberJid);
                context.startActivity(messageIntent);
            };

            //call action
            Runnable callAction = () -> {
                //action for confirm call alert dialog
                Runnable confirmCallAction = () -> {
                    Intent callIntent = new Intent(Intent.ACTION_CALL);
                    callIntent.setData(Uri.parse("tel:" + phoneNumber));

                    if (ActivityCompat.checkSelfPermission(context, Manifest.permission.CALL_PHONE)
                            != PackageManager.PERMISSION_GRANTED) { //no permission

                        Runnable callPermissionAction = () -> {
                            Intent intent = new Intent();
                            intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                            Uri uri = Uri.fromParts("package", context.getPackageName(), null);
                            intent.setData(uri);
                            context.startActivity(intent);
                        };

                        uiHelper.dialog2Btns(context, context.getString(R.string.need_phone_title),
                                context.getString(R.string.need_phone),
                                R.string.open_settings, R.string.cancel, R.color.white, R.color.primaryDark3,
                                callPermissionAction, null, false);
                    } else { //got permission
                        context.startActivity(callIntent);
                    }
                };

                uiHelper.dialog2Btns(context, context.getString(R.string.contact_call),
                        context.getString(R.string.contact_call_msg),
                        R.string.ok_label, R.string.cancel, R.color.white, R.color.black,
                        confirmCallAction, null, true);
            };

            //show bottom dialog accordingly
            Activity activity = (Activity) context;
            //final int for checking if contact exists
            final int contactExists = databaseHelper.getPhoneNameExist(memberJid);

            if (GroupChatDetail.selfAdminStatus == 0) { //self is not admin
                if (contactExists == 1) { //not admin, contact (2)
                    uiHelper.bottomDialog5Btns(activity, context.getString(R.string.msg_user), msgAction,
                            context.getString(R.string.call_user), callAction
                            , null, null, null, null, null, null);
                } else { //not admin, not in contact (3)

                    //add to contacts action
                    Runnable addToContactsAction = () -> {
                        if (contactExists == 0) { //contact doesn't exist
                            //add to phonebook first
                            new AddContactHelper(context).createContactPhoneBook(GlobalVariables.string1, phoneNumber);

                            //update displayname to sqlite phonename
                            databaseHelper.updateNewContactToContactRoster(memberJid, phoneNumber, GlobalVariables.string1,
                                    null, null);

                            GlobalVariables.string1 = null;

                            Toast.makeText(context, R.string.contact_added_soapp, Toast.LENGTH_SHORT).show();
                        } else { //contact already exists
                            Toast.makeText(context, R.string.contact_exists, Toast.LENGTH_SHORT).show();
                        }
                    };

                    Runnable goToDialog = () -> {

                        new UIHelper().dialog2Btns2Eview(context,
                                context.getString(R.string.add_contacts),
                                displayName,
                                phoneNumber,
                                addToContactsAction,
                                null,
                                true);
                    };

                    uiHelper.bottomDialog5Btns(context, context.getString(R.string.msg_user), msgAction,
                            context.getString(R.string.call_user), callAction,
                            context.getString(R.string.add_to_contact), goToDialog,
                            null, null, null, null);
                }
            } else { //self is admin
                //assign admin action
                Runnable assignAdminAction = () -> {
                    assignAdmin(GroupChatDetail.jid, memberJid);
                };

                //delete member action
                Runnable deleteGrpMemAction = () -> {

                    Runnable deleteRoomConfirm = () -> {
                        deleteMember(GroupChatDetail.jid, memberJid, profile_item_profile_name.getText().toString());
                    };
                    uiHelper.dialog2Btns(context, context.getString(R.string.delete_grp_mem),
                            context.getString(R.string.delete_grp_mem_msg),
                            R.string.delete, R.string.cancel, R.color.red, R.color.black, deleteRoomConfirm, null, true);
                };
                if (contactExists == 1) { //admin, contact (4)
                    uiHelper.bottomDialog5Btns(context, context.getString(R.string.msg_user), msgAction,
                            context.getString(R.string.call_user), callAction,
                            context.getString(R.string.assign_adm), assignAdminAction,
                            context.getString(R.string.delete_grp_mem), deleteGrpMemAction,
                            null, null);

                } else { //admin, not in contact (5)
                    //add to contacts action
                    Runnable addToContactsAction = () -> {
                        if (contactExists == 0) { //contact doesn't exist
                            //add to phonebook first
                            new AddContactHelper(context).createContactPhoneBook(GlobalVariables.string1, phoneNumber);

                            //update displayname to sqlite phonename
                            databaseHelper.updateNewContactToContactRoster(memberJid, phoneNumber, GlobalVariables.string1,
                                    null, null);

                            GlobalVariables.string1 = null;

                            Toast.makeText(context, R.string.contact_added_soapp, Toast.LENGTH_SHORT).show();
                        } else { //contact already exists
                            Toast.makeText(context, R.string.contact_exists, Toast.LENGTH_SHORT).show();
                        }
                    };

                    Runnable goToDialog = () -> {
                        new UIHelper().dialog2Btns2Eview(context,
                                context.getString(R.string.add_contacts),
                                displayName,
                                phoneNumber,
                                addToContactsAction,
                                null,
                                true);
                    };

                    uiHelper.bottomDialog5Btns(context, context.getString(R.string.msg_user),
                            msgAction, context.getString(R.string.call_user),
                            callAction, context.getString(R.string.assign_adm),
                            assignAdminAction, context.getString(R.string.delete_grp_mem),
                            deleteGrpMemAction, context.getString(R.string.add_to_contact),
                            goToDialog);
                }
            }

            return true;
        });
    }

    //function for assigning admin
    private void assignAdmin(String room_id, String memberJid) {
        if (SmackHelper.getXMPPConnection() != null && SmackHelper.getXMPPConnection().isAuthenticated()) {
            String selfJid = preferences.getValue(context, GlobalVariables.STRPREF_USER_ID);
            String access_token = preferences.getValue(context, GlobalVariables.STRPREF_ACCESS_TOKEN);
            String uniqueID = UUID.randomUUID().toString();
            AddRoomAdminModel model = new AddRoomAdminModel(room_id, memberJid, "admin", uniqueID, GlobalVariables.pubsubHost, GlobalVariables.xmppHost, "ANDROID");

            //build retrofit
            GroupAddAdmin client = RetrofitAPIClient.getClient().create(GroupAddAdmin.class);
            retrofit2.Call<AddRoomAdminModel> call2 = client.addAdminApi(model, "Bearer " + access_token);
            call2.enqueue(new retrofit2.Callback<AddRoomAdminModel>() {
                @Override
                public void onResponse(retrofit2.Call<AddRoomAdminModel> call2, retrofit2.Response<AddRoomAdminModel> response) {
                    if (!response.isSuccessful()) {

                        new MiscHelper().retroLogUnsuc(response, "assignAdmin ", "JAY");
                        Toast.makeText(context, R.string.onresponse_unsuccessful, Toast.LENGTH_SHORT).show();
                        return;
                    }
                    databaseHelper.assignMemberAsAdmin(room_id, memberJid);
//                    new PubsubNodeCall().PubsubGroup(room_id, selfJid, UUID.randomUUID().toString(),
//                            "", room_id, "", memberJid, "");
                }

                @Override
                public void onFailure(retrofit2.Call<AddRoomAdminModel> call2, Throwable t) {
                    new MiscHelper().retroLogFailure(t, "assignAdmin ", "JAY");
                    Toast.makeText(context, R.string.onfailure, Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            Toast.makeText(context, R.string.xmpp_waiting_connection, Toast.LENGTH_SHORT).show();
        }
    }

    //function for deleting member
    private void deleteMember(String room_id, String memberJid, String memberDisplayName) {
        if (SmackHelper.getXMPPConnection() != null && SmackHelper.getXMPPConnection().isAuthenticated()) {
            String selfJid = preferences.getValue(context, GlobalVariables.STRPREF_USER_ID);
            String access_token = preferences.getValue(context, GlobalVariables.STRPREF_ACCESS_TOKEN);
            String uniqueID = UUID.randomUUID().toString();
            DeleteRoomMemberModel model = new DeleteRoomMemberModel(room_id, memberJid, uniqueID, GlobalVariables.pubsubHost, GlobalVariables.xmppHost, "ANDROID");

            //build retrofit
            DeleteRoomMember client = RetrofitAPIClient.getClient().create(DeleteRoomMember.class);
            retrofit2.Call<DeleteRoomMemberModel> call2 = client.deleteMemberApi(model, "Bearer " + access_token);
            call2.enqueue(new retrofit2.Callback<DeleteRoomMemberModel>() {
                @Override
                public void onResponse(retrofit2.Call<DeleteRoomMemberModel> call2, retrofit2.Response<DeleteRoomMemberModel> response) {
                    String body = context.getString(R.string.single_member_self_removed) + " " + memberDisplayName;

                    if (!response.isSuccessful()) {
                        try {
                            String error = response.errorBody().string();

                            switch (error) {
                                case "{\"message\":\"Room does not exist or user is not a member of this room\"}":
                                    databaseHelper.selfDeleteGrpMember(room_id, memberJid, body);
                                    break;

                                default:
                                    new MiscHelper().retroLogUnsuc(response, "deleteMenber ", "JAY");
                                    Toast.makeText(context, R.string.onresponse_unsuccessful, Toast.LENGTH_SHORT).show();
                                    break;
                            }
                        } catch (IOException ignored) {
                        }
                        return;
                    }

                    //update db (delete member for groupmem)
                    databaseHelper.selfDeleteGrpMember(room_id, memberJid, body);

                    //send stanza to deleted member to let him know he's deleted and do action
                    new SingleChatStanza().SoappDeleteGrpMemStanza(memberJid, room_id);
                }

                @Override
                public void onFailure(retrofit2.Call<DeleteRoomMemberModel> call2, Throwable t) {
                    new MiscHelper().retroLogFailure(t, "deleteMenber ", "JAY");
                    Toast.makeText(context, R.string.onfailure, Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            Toast.makeText(context, R.string.xmpp_waiting_connection, Toast.LENGTH_SHORT).show();
        }
    }
}
