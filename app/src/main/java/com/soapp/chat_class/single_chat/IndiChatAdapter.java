package com.soapp.chat_class.single_chat;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import com.soapp.R;
import com.soapp.sql.room.joiners.IndiChatLogDB;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.paging.PagedList;
import androidx.paging.PagedListAdapter;
import androidx.recyclerview.widget.DiffUtil;

public class IndiChatAdapter extends PagedListAdapter<IndiChatLogDB, IndiChatHolder> {
    private static final DiffUtil.ItemCallback<IndiChatLogDB> DIFF_CALLBACK =
            new DiffUtil.ItemCallback<IndiChatLogDB>() {
                @Override

                public boolean areItemsTheSame(
                        @NonNull IndiChatLogDB oldUser, @NonNull IndiChatLogDB newUser) {
                    // User properties may have changed if reloaded from the DB, but ID is fixed
                    return oldUser.getMessage().getMsgJid().equals(newUser.getMessage().getMsgJid());
                }

                @Override
                public boolean areContentsTheSame(
                        @NonNull IndiChatLogDB oldUser, @NonNull IndiChatLogDB newUser) {
                    // NOTE: if you use equals, your object must properly override Object#equals()
                    // Incorrectly returning false here will result in too many animations.

                    return oldUser.equals(newUser);
                }
            };

    IndiChatAdapter() {
        super(DIFF_CALLBACK);
    }

    @NonNull
    @Override
    public IndiChatHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        switch (viewType) {
            //date header
            case 0:
                return new IndiChatHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout
                        .chatlog_date, parent, false));

            //unread messages
            case 1:
                return new IndiChatHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout
                        .chattab_msg_unread, parent, false));

            //add new contact (from unknown number)
            case 3:
                return new IndiChatHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout
                        .chattab_msg_new_contact, parent, false));

            //blocked/unblocked user (outgoing only)
            case 4:
                return new IndiChatHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout
                        .chattab_msg_block, parent, false));

            //outgoing text msg
            case 10:
                return new IndiChatHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout
                        .chatlog_out_text, parent, false));

            //incoming text msg
            case 11:
                return new IndiChatHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.
                        chatlog_in_text, parent, false));

            //outgoing image
            case 20:
                return new IndiChatHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout
                        .chatlog_out_image, parent, false));

            //incoming image
            case 21:
                return new IndiChatHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout
                        .chattab_in_image, parent, false));

            //outgoing audio
            case 22:
                return new IndiChatHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout
                        .chattab_out_audio, parent, false));

            //incoming audio
            case 23:
                return new IndiChatHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout
                        .chattab_in_audio, parent, false));

            //outgoing video
            case 24:
                return new IndiChatHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout
                        .chattab_out_video, parent, false));

            //incoming video
            case 25:
                return new IndiChatHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout
                        .chattab_in_video, parent, false));

            //outgoing location
            case 30:
                return new IndiChatHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout
                        .chattab_out_map, parent, false));

            //incoming location
            case 31:
                return new IndiChatHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout
                        .chattab_in_map, parent, false));

            //outgoing contact
            case 32:
                return new IndiChatHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout
                        .chattab_out_contact, parent, false));

            //incoming contact
            case 33:
                return new IndiChatHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout
                        .chattab_in_contact, parent, false));

            //outgoing restaurant
            case 34:
                return new IndiChatHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout
                        .chattab_out_res, parent, false));

            //incoming restaurant
            case 35:
                return new IndiChatHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout
                        .chattab_in_res, parent, false));

            //outgoing tracking
            case 40:
                return new IndiChatHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout
                        .chattab_out_track, parent, false));

            //outgoing tracking stopped
            case 41:
                return new IndiChatHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout
                        .chattab_out_map, parent, false));

            //outgoing share booking QR Code halfway
            case 50:
                return new IndiChatHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout
                        .chattab_out_res_booking, parent, false));

            //incoming share booking QR Code  halfway , had changed the backgroud to bubble and diabled the cardview however its not showing
            case 51:
                return new IndiChatHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout
                        .chattab_in_res_booking, parent, false));

            //incoming res booking cancelled (shared previously)
            case 53:
                return new IndiChatHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout
                        .chatlog_grp_status, parent, false));

            //out reply text
            case 60:
                return new IndiChatHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout
                        .chattab_out_reply_text, parent, false));

            //in reply text
            case 61:
                return new IndiChatHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout
                        .chattab_in_reply_text, parent, false));

            //out reply media
            case 62:
                return new IndiChatHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout
                        .chattab_out_reply_media, parent, false));

            //in reply media
            case 63:
                return new IndiChatHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout
                        .chattab_in_reply_media, parent, false));

            //outgoing appt created
            case 70:
                return new IndiChatHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout
                        .chattab_out_create_appt, parent, false));

            //incoming appt created
            case 71:
                return new IndiChatHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout
                        .chattab_in_create_appt, parent, false));

            //appt title updated
            case 72: //out
            case 73: //in
                return new IndiChatHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout
                        .chattab_msg_appt_text_msg, parent, false));

            //appt date/time updated
            case 74: //out
            case 75: //in
                return new IndiChatHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout
                        .chattab_msg_appt_date_time, parent, false));

            //appt location updated
            case 76: //out
            case 77: //in
                return new IndiChatHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout
                        .chattab_msg_appt_location, parent, false));

            //outgoing appt status updated
            case 78:
                return new IndiChatHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout
                        .chattab_msg_appt_text_msg, parent, false));

            //incoming appt status updated
            case 79:
                return new IndiChatHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout
                        .chattab_msg_appt_status, parent, false));

            //appt deleted
            case 80: //out
            case 81: //in
                return new IndiChatHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout
                        .chattab_msg_appt_text_msg, parent, false));

            //appt details updated (just for syncing)
            case 82:
                return new IndiChatHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout
                        .chattab_msg_appt_general, parent, false));

            //ask user update soapp
            default:
                return new IndiChatHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout
                        .chattab_msg_update_soapp, parent, false));
        }
    }

    @Override
    public void onBindViewHolder(@NonNull IndiChatHolder holder, int position) {
        holder.setData(getItem(position), position);
    }

    @Override
    public int getItemViewType(int position) {
        if (getItem(position).getMessage().getIsSender() != null) {
            return getItem(position).getMessage().getIsSender();
        } else {
            return 0;
        }
    }

    @Override
    public long getItemId(int position) {
        return getItem(position).getMessage().getMsgRow();
    }

    @Override
    public int getItemCount() {
        return super.getItemCount();
    }

    @Override
    public void onCurrentListChanged(@Nullable PagedList<IndiChatLogDB> currentList) {
        super.onCurrentListChanged(currentList);
        notifyDataSetChanged();

    }
}