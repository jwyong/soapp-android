package com.soapp.sql.room.entity;

/* Created by ibrahim on 19/03/2018. */

interface GroupMemStatusInterface {
    Integer getGMSRow();

    String getGMSRoomJid();

    String getGMSMemberJid();

    String getGMSApptID();

    Integer getGMSStatus();
}
