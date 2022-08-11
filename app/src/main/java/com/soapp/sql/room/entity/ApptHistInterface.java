package com.soapp.sql.room.entity;

/* Created by ibrahim on 21/03/2018. */

interface ApptHistInterface {
    Integer getApptH_Row();

    String getApptH_ID();

    String getApptH_Jid();

    String getApptH_Title();

    Long getApptH_Date();

    Long getApptH_Time();

    String getApptH_Location();

    String getApptH_Latitude();

    String getApptH_Longitude();

    String getApptH_MapURL();

    String getApptH_Desc();

    Integer getApptH_Self_Status();

    Integer getApptH_Friend_Status();

    String getApptH_ResID();

    String getApptH_ResImgURL();

    String getApptH_isDeleted();
}
