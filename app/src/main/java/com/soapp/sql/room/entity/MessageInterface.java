package com.soapp.sql.room.entity;

interface MessageInterface {
    Integer getMsgRow();

    String getMsgJid();

    String getSenderJid();

    Integer getChatMarker();

    Integer getIsSender();

    Long getMsgDate();

    String getMsgUniqueId();

    String getMsgData();

    String getMsgInfoId();

    String getMsgInfoUrl();

    String getMsgLatitude();

    String getMsgLongitude();

    Integer getMediaStatus();

    Integer getMsgOffline();

    Integer getContact();

    String getContactname();

    String getContactnumber();


}
