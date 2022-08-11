package com.soapp.sql.room.entity;

import android.content.ContentValues;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

/* Created by ibrahim on 19/03/2018. */

@Entity(tableName = "CHAT_LIST")
public class ChatList implements ChatListInterface {

    @PrimaryKey(autoGenerate = true)
    private Integer ChatRow;

    private String ChatJid;

    private Long LastDateReceived = 0L;

    private Integer Admin_Self = 0;

    private Integer NotiBadge = 0;

    private String LastDispMsg;

    private Integer TypingStatus = 0;

    private Integer OnlineStatus = 0;

    private String TypingName;

    private String LastSenderName;

    //    @RequiresApi(api = Build.VERSION_CODES.N)
    @Ignore
    public ChatList(String chatJid, Long lastDateReceived, Integer admin_Self, Integer notiBadge, String lastDispMsg, Integer typingStatus, Integer onlineStatus, String typingName, String lastSenderName) {
        ChatJid = chatJid;
        LastDateReceived = lastDateReceived;
        Admin_Self = admin_Self;
        NotiBadge = notiBadge;
        LastDispMsg = lastDispMsg;
        TypingStatus = typingStatus;
        OnlineStatus = onlineStatus;
        TypingName = typingName;
        LastSenderName = lastSenderName;
    }

    public ChatList() {
    }

    public ChatList(int chatRow) {
        ChatRow = chatRow;
    }

    public static ChatList fromContentValues(ContentValues values) {

        final ChatList chatList = new ChatList();

        if (values.containsKey("ChatJid")) {
            chatList.ChatJid = values.getAsString("ChatJid");
        }
        if (values.containsKey("LastDateReceived")) {
            chatList.LastDateReceived = values.getAsLong("LastDateReceived");
        }
        if (values.containsKey("Admin_Self")) {
            chatList.Admin_Self = values.getAsInteger("Admin_Self");
        }

        if (values.containsKey("NotiBadge")) {
            chatList.NotiBadge = values.getAsInteger("NotiBadge");
        }
        if (values.containsKey("LastDispMsg")) {
            chatList.LastDispMsg = values.getAsString("LastDispMsg");
        }
        if (values.containsKey("TypingStatus")) {
            chatList.TypingStatus = values.getAsInteger("TypingStatus");
        }
        if (values.containsKey("OnlineStatus")) {
            chatList.OnlineStatus = values.getAsInteger("OnlineStatus");
        }
        if (values.containsKey("TypingName")) {
            chatList.TypingName = values.getAsString("TypingName");
        }
        if (values.containsKey("LastSenderName")) {
            chatList.LastSenderName = values.getAsString("LastSenderName");
        }


        return chatList;
    }


    public String getChatJid() {
        return ChatJid;
    }

    public void setChatJid(String chatJid) {
        ChatJid = chatJid;
    }

    public Long getLastDateReceived() {
        return LastDateReceived;
    }

    public void setLastDateReceived(Long lastDateReceived) {
        LastDateReceived = lastDateReceived;
    }

    public Integer getAdmin_Self() {
        return Admin_Self;
    }

    public void setAdmin_Self(Integer admin_Self) {
        Admin_Self = admin_Self;
    }


    public Integer getNotiBadge() {
        return NotiBadge;
    }

    public void setNotiBadge(Integer notiBadge) {
        NotiBadge = notiBadge;
    }

    public String getLastDispMsg() {
        return LastDispMsg;
    }

    public void setLastDispMsg(String lastDispMsg) {
        LastDispMsg = lastDispMsg;
    }

    public Integer getTypingStatus() {
        return TypingStatus;
    }

    public void setTypingStatus(Integer typingStatus) {
        TypingStatus = typingStatus;
    }

    public Integer getOnlineStatus() {
        return OnlineStatus;
    }

    public void setOnlineStatus(Integer onlineStatus) {
        OnlineStatus = onlineStatus;
    }

    public String getTypingName() {
        return TypingName;
    }

    public void setTypingName(String typingName) {
        TypingName = typingName;
    }

    public String getLastSenderName() {
        return LastSenderName;
    }

    public void setLastSenderName(String lastSenderName) {
        LastSenderName = lastSenderName;
    }

    public Integer getChatRow() {
        return ChatRow;
    }

    public void setChatRow(Integer chatRow) {
        ChatRow = chatRow;
    }

    @NonNull
    @Override
    public String toString() {
        return "ChatList() called with: chatJid = [" + ChatJid + "], lastDateReceived = [" + LastDateReceived + "]," +
                " admin_Self = [" + Admin_Self + "], notiBadge = [" + NotiBadge + "], lastDispMsg = [" + LastDispMsg + "]," +
                " typingStatus = [" + TypingStatus + "], onlineStatus = [" + OnlineStatus + "], typingName = [" + TypingName + "]," +
                " lastSenderName = [" + LastSenderName + "]";
    }
}