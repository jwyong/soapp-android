package com.soapp.sql.room.entity;

/* Created by ibrahim on 19/03/2018. */

interface GroupMemInterface {


    Integer getGrpMem_Id();

    String getRoomJid();

    String getMemberJid();

    Integer getAdmin();

    String getTextColor();

    Integer getAppt_Status();

    String getLatitude();

    String getLongitude();
}
