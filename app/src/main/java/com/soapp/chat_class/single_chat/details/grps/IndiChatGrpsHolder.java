package com.soapp.chat_class.single_chat.details.grps;

import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.widget.ImageView;

import com.bumptech.glide.load.resource.bitmap.BitmapTransitionOptions;
import com.bumptech.glide.request.RequestOptions;
import com.soapp.R;
import com.soapp.chat_class.group_chat.GroupChatLog;
import com.soapp.global.GlideAPI.GlideApp;
import com.soapp.global.ImageFullView;
import com.soapp.global.ImagePreviewByte;
import com.soapp.global.Preferences;
import com.soapp.global.UIHelper;
import com.soapp.sql.DatabaseHelper;
import com.soapp.sql.room.entity.ContactRoster;

import androidx.recyclerview.widget.RecyclerView;
import io.github.rockerhieu.emojicon.EmojiconTextView;

/* Created by chang on 13/08/2017. */

public class IndiChatGrpsHolder extends RecyclerView.ViewHolder {
    //basics
    private Context context;
    private DatabaseHelper databaseHelper = DatabaseHelper.getInstance();
    private Preferences preferences = Preferences.getInstance();
    private UIHelper uiHelper = new UIHelper();

    //elements
    private ImageView profile_item_profile_img;
    private EmojiconTextView profile_item_profile_name;

    IndiChatGrpsHolder(View itemView) {
        super(itemView);
        context = itemView.getContext();

        //set elements
        profile_item_profile_img = itemView.findViewById(R.id.profile_item_profile_img);
        profile_item_profile_name = itemView.findViewById(R.id.profile_item_profile_name);
    }

    public void setData(final ContactRoster data) {
        //grp profile img
        GlideApp.with(itemView)
                .asBitmap()
                .load(data.getProfilephoto())
                .placeholder(R.drawable.grp_propic_circle_150px)
                .apply(RequestOptions.circleCropTransform())
                .transition(BitmapTransitionOptions.withCrossFade())
                .into(profile_item_profile_img);

        //set member jid to final variable
        final String roomJid = data.getContactJid();

        profile_item_profile_img.setOnClickListener(v -> {
            Intent myimageintent = new Intent(context, ImageFullView.class);
//            byte[] byteArray = data.getProfilephoto();
//            myimageintent.putExtra("byteArray", byteArray);
            myimageintent = new Intent(context, ImagePreviewByte.class);
            myimageintent.putExtra("jid", roomJid);
            context.startActivity(myimageintent);
        });

        //displayname
        String displayName = data.getDisplayname();
        profile_item_profile_name.setText(displayName);

        //go to grp chatroom on click
        itemView.setOnClickListener(view -> {
            Intent grpChatLogIntent = new Intent(context, GroupChatLog.class);
            grpChatLogIntent.putExtra("jid", roomJid);

            context.startActivity(grpChatLogIntent);
        });

        //ADD MORE FEATURES SOON
        //long click opens bottom sheet for grp mem options
//        itemView.setOnLongClickListener(view -> {
//            //init global runnable actions first
//            //msg action
//            Runnable msgAction = () -> {
//                Intent messageIntent = new Intent(context, IndiChatLog.class);
//                messageIntent.putExtra("jid", roomJid);
//                context.startActivity(messageIntent);
//            };
//
//            //call action
//            Runnable callAction = () -> {
//                //action for confirm call alert dialog
//                Runnable confirmCallAction = () -> {
//                    Intent callIntent = new Intent(Intent.ACTION_CALL);
//                    callIntent.setData(Uri.parse("tel:" + phoneNumber));
//
//                    if (ActivityCompat.checkSelfPermission(context, Manifest.permission.CALL_PHONE)
//                            != PackageManager.PERMISSION_GRANTED) { //no permission
//
//                        Runnable callPermissionAction = () -> {
//                            Intent intent = new Intent();
//                            intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
//                            Uri uri = Uri.fromParts("package", context.getPackageName(), null);
//                            intent.setData(uri);
//                            context.startActivity(intent);
//                        };
//
//                        uiHelper.Dialog2BtnsNew(context, context.getString(R.string.need_phone_title),
//                                context.getString(R.string.need_phone),
//                                R.string.open_settings, R.string.cancel, R.color.white, R.color.primaryDark3,
//                                R.color.primaryDark3, R.color.white, callPermissionAction, null, false);
//                    } else { //got permission
//                        context.startActivity(callIntent);
//                    }
//                };
//
//                uiHelper.Dialog2BtnsNew(context, context.getString(R.string.contact_call),
//                        context.getString(R.string.contact_call_msg),
//                        R.string.ok_label, R.string.cancel, R.color.white, R.color.black, R.color.primaryLight4,
//                        R.color.white, confirmCallAction, null, true);
//            };
//
//            //show bottom dialog accordingly
//            Activity activity = (Activity) context;
//            //final int for checking if contact exists
//            final int contactExists = databaseHelper.getPhoneNameExist(roomJid);
//
//            if (GroupChatDetail.selfAdminStatus == 0) { //self is not admin
//                if (contactExists == 1) { //not admin, contact (2)
//                    uiHelper.bottomDialog2Btns(activity, R.string.msg_user, msgAction, R.string.call_user,
//                            callAction);
//                } else { //not admin, not in contact (3)
//                    //add to contacts action
//                    Runnable addToContactsAction = () -> {
//                        if (contactExists == 0) { //contact doesn't exist
//                            //add to phonebook first
//                            new QrReader().createContactPhoneBook(context, displayName, phoneNumber);
//
//                            //update displayname to sqlite phonename
//                            databaseHelper.updateNewContactToContactRoster(roomJid, phoneNumber, displayName,
//                                    null, null);
//
//                            Toast.makeText(context, R.string.contact_added, Toast.LENGTH_SHORT).show();
//                        } else { //contact already exists
//                            Toast.makeText(context, R.string.contact_exists, Toast.LENGTH_SHORT).show();
//                        }
//                    };
//                    uiHelper.bottomDialog3Btns(activity, R.string.msg_user, msgAction, R.string.call_user,
//                            callAction, R.string.add_to_contact, addToContactsAction);
//                }
//            } else { //self is admin
//                //assign admin action
//                Runnable assignAdminAction = () -> {
//                    assignAdmin(GroupChatDetail.jid, roomJid);
//                };
//
//                //delete member action
//                Runnable deleteGrpMemAction = () -> {
//                    deleteMember(GroupChatDetail.jid, roomJid, profile_item_profile_name.getText().toString());
//                };
//                if (contactExists == 1) { //admin, contact (4)
//                    uiHelper.bottomDialog4Btns(activity, R.string.msg_user, msgAction, R.string.call_user,
//                            callAction, R.string.assign_adm, assignAdminAction, R.string.delete_grp_mem,
//                            deleteGrpMemAction);
//                } else { //admin, not in contact (5)
//                    //add to contacts action
//                    Runnable addToContactsAction = () -> {
//                        if (contactExists == 0) { //contact doesn't exist
//                            //add to phonebook first
//                            new QrReader().createContactPhoneBook(context, displayName, phoneNumber);
//
//                            //update displayname to sqlite phonename
//                            databaseHelper.updateNewContactToContactRoster(roomJid, phoneNumber, displayName,
//                                    null, null);
//
//                            Toast.makeText(context, R.string.contact_added, Toast.LENGTH_SHORT).show();
//                        } else { //contact already exists
//                            Toast.makeText(context, R.string.contact_exists, Toast.LENGTH_SHORT).show();
//                        }
//                    };
//                    uiHelper.bottomDialog5Btns(activity, R.string.msg_user, msgAction, R.string.call_user,
//                            callAction, R.string.assign_adm, assignAdminAction, R.string.delete_grp_mem,
//                            deleteGrpMemAction, R.string.add_to_contact, addToContactsAction);
//                }
//            }
//
//            return true;
//        });
    }
}
