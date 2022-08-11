package com.soapp.chat_class.group_chat;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import com.soapp.R;
import com.soapp.databinding.ChatlogDateBinding;
import com.soapp.databinding.ChatlogGrpStatusBinding;
import com.soapp.databinding.ChatlogInGrpTextBinding;
import com.soapp.databinding.ChatlogInTextBinding;
import com.soapp.databinding.ChatlogOutImageBinding;
import com.soapp.databinding.ChatlogOutTextBinding;
import com.soapp.sql.room.joiners.GroupChatLogDB;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.databinding.ViewDataBinding;
import androidx.paging.PagedList;
import androidx.paging.PagedListAdapter;
import androidx.recyclerview.widget.DiffUtil;

/* Created by chang on 12/08/2017. */

public class GroupChatAdapter extends PagedListAdapter<GroupChatLogDB, GroupChatHolder> {
    private static final DiffUtil.ItemCallback<GroupChatLogDB> DIFF_CALLBACK =
            new DiffUtil.ItemCallback<GroupChatLogDB>() {
                @Override

                public boolean areItemsTheSame(
                        @NonNull GroupChatLogDB oldUser, @NonNull GroupChatLogDB newUser) {
                    // User properties may have changed if reloaded from the DB, but ID is fixed
                    return oldUser.getMessage().getMsgRow().equals(newUser.getMessage().getMsgRow());
                }

                @Override
                public boolean areContentsTheSame(
                        @NonNull GroupChatLogDB oldUser, @NonNull GroupChatLogDB newUser) {
                    // NOTE: if you use equals, your object must properly override Object#equals()
                    // Incorrectly returning false here will result in too many animations.
                    return oldUser.equals(newUser);
                }
            };

    GroupChatAdapter() {
        super(DIFF_CALLBACK);
    }

    @Override
    public GroupChatHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        ViewDataBinding binding;

        switch (viewType) {
            case 0: //date/time header
                binding = DataBindingUtil.inflate(inflater, R.layout.chatlog_date, parent, false);
                return new GroupChatHolder((ChatlogDateBinding) binding);

            case 1: //unread msg
                return new GroupChatHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout
                        .chattab_msg_unread, parent, false));

            case 2: //grp status msg
                binding = DataBindingUtil.inflate(inflater, R.layout.chatlog_grp_status, parent, false);
                return new GroupChatHolder((ChatlogGrpStatusBinding) binding);

            case 10: //outgoing text msg
                binding = DataBindingUtil.inflate(inflater, R.layout.chatlog_out_text, parent, false);
                return new GroupChatHolder((ChatlogOutTextBinding) binding);

            case 11: //incoming text msg with displayName
                binding = DataBindingUtil.inflate(inflater, R.layout.chatlog_in_grp_text, parent, false);
                return new GroupChatHolder((ChatlogInGrpTextBinding) binding);

            case 12: //incoming text msg - no displayName
                binding = DataBindingUtil.inflate(inflater, R.layout.chatlog_in_text, parent, false);
                return new GroupChatHolder((ChatlogInTextBinding) binding);

            case 20: //incoming text msg - no displayName
                binding = DataBindingUtil.inflate(inflater, R.layout.chatlog_out_image, parent, false);
                return new GroupChatHolder((ChatlogOutImageBinding) binding);

            default: //update soapp msg (new issender case)
                return new GroupChatHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout
                        .chattab_msg_update_soapp, parent, false));
//
//            //[DONE] incoming image
//            case 21:
//                return new GroupChatHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout
//                        .chattab_in_g_image, parent, false));
//
//            //[DONE] outgoing audio
//            case 22:
//                return new GroupChatHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout
//                        .chattab_out_audio, parent, false));
//
//            //[DONE] incoming audio
//            case 23:
//                return new GroupChatHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout
//                        .chattab_in_g_audio, parent, false));
//
//            //[DONE] outgoing video
//            case 24:
//                return new GroupChatHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout
//                        .chattab_out_video, parent, false));
//
//            //[DONE] incoming video
//            case 25:
//                return new GroupChatHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout
//                        .chattab_in_g_video, parent, false));
//
//            //[DONE] outgoing location
//            case 30:
//                return new GroupChatHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout
//                        .chattab_out_map, parent, false));
//
//            //[DONE] incoming location
//            case 31:
//                return new GroupChatHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout
//                        .chattab_in_g_map, parent, false));
//
//            //[DONE] outgoing contact
//            case 32:
//                return new GroupChatHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout
//                        .chattab_out_contact, parent, false));
//
//            //[DONE] incoming contact
//            case 33:
//                return new GroupChatHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout
//                        .chattab_in_g_contact, parent, false));
//
//            //[DONE] outgoing restaurant
//            case 34:
//                return new GroupChatHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout
//                        .chattab_out_res, parent, false));
//
//            //[DONE] incoming restaurant
//            case 35:
//                return new GroupChatHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout
//                        .chattab_in_g_res, parent, false));
//
//            //[KIV] outgoing tracking
//            case 40:
//                return new GroupChatHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout
//                        .chattab_out_track, parent, false));
//
//            //[KIV] outgoing tracking stopped
//            case 41:
//                return new GroupChatHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout
//                        .chattab_out_map, parent, false));
//
//            //outgoing restaurant booking sharing
//            case 50:
//                return new GroupChatHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout
//                        .chattab_out_res_booking, parent, false));
//
//            //incoming restaurant booking sharing
//            case 51:
//                return new GroupChatHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout
//                        .chattab_in_g_res_booking, parent, false));
//
//            //incoming previously shared res booking cancelled
//            case 53:
//                return new GroupChatHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout
//                        .chatlog_grp_status, parent, false));
//
////            //outgoing booking expired
////            case 54:
//
////            //incoming booking expired
////            case 55:
//
//            //outgoing reply text
//            case 60:
//                return new GroupChatHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout
//                        .chattab_out_reply_text, parent, false));
//
//            //incoming reply text
//            case 61:
//                return new GroupChatHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout
//                        .chattab_in_g_reply_text, parent, false));
//
//            //outgoing reply img + video
//            case 62:
//                return new GroupChatHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout
//                        .chattab_out_reply_media, parent, false));
//
//            //incoming reply image + video
//            case 63:
//                return new GroupChatHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout
//                        .chattab_in_g_reply_media, parent, false));
//
//            //[DONE] outgoing appt created
//            case 70:
//                return new GroupChatHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout
//                        .chattab_out_create_appt, parent, false));
//
//            //[DONE] incoming appt created
//            case 71:
//                return new GroupChatHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout
//                        .chattab_in_g_create_appt, parent, false));
//
//            //appt title updated
//            case 72: //out
//            case 73: //in
//                return new GroupChatHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout
//                        .chattab_msg_appt_text_msg, parent, false));
//
//            //appt date/time updated
//            case 74: //out
//            case 75: //in
//                return new GroupChatHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout
//                        .chattab_msg_appt_date_time, parent, false));
//
//            //appt location updated
//            case 76: //out
//            case 77: //in
//                return new GroupChatHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout
//                        .chattab_msg_appt_location, parent, false));
//
//            //outgoing appt status updated
//            case 78:
//                return new GroupChatHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout
//                        .chattab_msg_appt_text_msg, parent, false));
//
//            //incoming appt status updated
//            case 79:
//                return new GroupChatHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout
//                        .chattab_msg_appt_status, parent, false));
//
//            //appt deleted
//            case 80: //out
//            case 81: //in
//                return new GroupChatHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout
//                        .chattab_msg_appt_text_msg, parent, false));
//
//
//            //appt details updated (just for syncing)
//            case 82:
//                return new GroupChatHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout
//                        .chattab_msg_appt_general, parent, false));
//
//            //no case, ask user to update soapp
//            default:
//                return new GroupChatHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout
//                        .chattab_msg_update_soapp, parent, false));
        }
    }

    @Override
    public void onBindViewHolder(@NonNull GroupChatHolder holder, int position) {
        holder.bind(getItem(position), position);
    }

    @Override
    public long getItemId(int position) {
        return getItem(position).getMessage().getMsgRow();
    }

    @Override
    public int getItemViewType(int position) {
        if (getItem(position).getMessage().getIsSender() != null) {
            return getItem(position).getMessage().getIsSender();
        } else return -1;
    }

    @Override
    public void onCurrentListChanged(@Nullable PagedList<GroupChatLogDB> currentList) {
        super.onCurrentListChanged(currentList);
        notifyDataSetChanged();
    }
}

