package com.soapp.sql.room.entity;

/**
 * Created by ibrahim on 19/03/2018.
 */

interface ChatListInterface {
    Integer getChatRow();

    String getChatJid();

    Long getLastDateReceived();

    Integer getAdmin_Self();

    Integer getNotiBadge();

    String getLastDispMsg();

    Integer getTypingStatus();

    Integer getOnlineStatus();

    String getTypingName();

    String getLastSenderName();

}
