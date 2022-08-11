package com.soapp.sql.room.joiners;

import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.soapp.R;
import com.soapp.chat_class.group_chat.GroupChatLog;
import com.soapp.chat_class.single_chat.IndiChatLog;
import com.soapp.global.ImageLoadHelper;
import com.soapp.sql.room.entity.ChatList;
import com.soapp.sql.room.entity.ContactRoster;
import com.soapp.sql.room.entity.Message;

import java.text.DateFormat;
import java.util.Date;
import java.util.List;

import androidx.databinding.BindingAdapter;
import androidx.room.Embedded;
import androidx.room.Relation;

public class ChatTabList {
    @Relation(parentColumn = "ChatJid", entityColumn = "MsgJid", entity = Message.class, projection = "MsgOffline")
    private List<Integer> msgOffline;

    @Embedded
    public ContactRoster contactRosterList;
    @Embedded
    private ChatList chatList;

    public ChatList getChatList() {
        return chatList;
    }

    public void setChatList(ChatList chatList) {
        this.chatList = chatList;
    }

    public ContactRoster getCR() {
        return contactRosterList;
    }

    public List<Integer> getMsgOffline() {
        return msgOffline;
    }

    public void setMsgOffline(List<Integer> msgOffline) {
        this.msgOffline = msgOffline;
    }

    //profile img
    public byte[] getImgByte() {
        return contactRosterList.getProfilephoto();
    }

    public int getPlaceHolder() {
        return chatList.getChatJid().length() == 12 ? R.drawable.in_propic_circle_150px : R.drawable.grp_propic_circle_150px;
    }

    @BindingAdapter(value = {"imgByte", "placeHolder"}, requireAll = false)
    public static void getProfileImg(ImageView view, byte[] imgByte, int drawable) {
        new ImageLoadHelper().setImgByte(view, imgByte, drawable);
    }

    //function to show typing status
    public String getTypingStatus() {
        int typingStatus = chatList.getTypingStatus();
        if (chatList.getChatJid().length() == 12) { //indi
            if (typingStatus == 1) {
                return "typing...";
            } else {
                return chatList.getLastDispMsg();
            }
        } else { //grp
            if (typingStatus == 1) {
                return String.format("%s is typing...", chatList.getTypingName());
            } else {
                return chatList.getLastDispMsg();
            }
        }
    }

    //function to get displayName
    public String getDisplayName() {
        if (chatList.getChatJid().length() == 12) { //single chat
            String phoneName = contactRosterList.getPhonename();
            if (phoneName != null && !phoneName.equals("")) {
                return phoneName;
            } else {
                return String.format("%s %s", contactRosterList.getDisplayname(), contactRosterList.getPhonenumber());
            }

        } else { //group
            return contactRosterList.getDisplayname();
        }
    }

    //function to get member name
    public String getMemberName() {
        if (chatList.getChatJid().length() > 12) { //grp
            String otherRec = chatList.getLastSenderName();
            if (otherRec == null) { //no other receipient yet
                return "you";
            } else {
                return String.format("%s:", otherRec);
            }
        }
        return "";
    }

    //onclick
    public void onClickChatHolder(View view) {
        Context context = view.getContext();
        Intent intent;
        if (chatList.getChatJid().length() == 12) { //indi
            intent = new Intent(context, IndiChatLog.class);
        } else { //group
            intent = new Intent(context, GroupChatLog.class);
        }
        intent.putExtra("jid", chatList.getChatJid());

        context.startActivity(intent);
    }

    //offline msg
    public int getOfflineMsgStatus() {
        if (msgOffline.size() > 0) {
            int msgoff = msgOffline.get(msgOffline.size() - 1);

            if (msgoff == 1) {
                return View.VISIBLE;
            } else {
                return View.GONE;
            }
        } else {
            return View.GONE;
        }
    }

    //bold last disp msg
    @BindingAdapter(value = {"android:textStyles"})
    public static void setTextStyles(TextView v, boolean gotNoti) {
        Context context = v.getContext();

        if (gotNoti) {
            v.setTypeface(null, Typeface.BOLD);
            v.setTextColor(context.getResources().getColor(R.color.black));
        } else {
            v.setTypeface(null, Typeface.NORMAL);
            v.setTextColor(context.getResources().getColor(R.color.grey9));
        }
    }

    public String getDateTimeStr() {
        Date date = new Date(chatList.getLastDateReceived());
        DateFormat timeformat = DateFormat.getTimeInstance(DateFormat.SHORT);

        return timeformat.format(date);
    }
}
