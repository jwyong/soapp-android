package com.soapp.sql.room.joiners;

import com.soapp.sql.room.entity.ChatList;
import com.soapp.sql.room.entity.ContactRoster;

import androidx.room.Embedded;

public class SharingList {

    @Embedded
    private ContactRoster contactRoster;

    @Embedded
    private ChatList chatList;

    public ContactRoster getContactRoster() {
        return contactRoster;
    }

    public void setContactRoster(ContactRoster contactRoster) {
        this.contactRoster = contactRoster;
    }

    public ChatList getChatList() {
        return chatList;
    }

    public void setChatList(ChatList chatList) {
        this.chatList = chatList;
    }

}
