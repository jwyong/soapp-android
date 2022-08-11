package com.soapp.sql.room.entity;

/* Created by ibrahim on 21/03/2018. */

interface ApptInterface {

    Integer getApptRow();

    String getApptID();

    String getApptJid();

    String getApptTitle();

    Long getApptDate();

    Long getApptTime();

    Long getApptReminderTime();

    Integer getApptNotiBadge();

    String getApptLocation();

    String getApptLatitude();

    String getApptLongitude();

    String getApptMapURL();

    Integer getFriend_Status();

    String getApptResID();

    Integer getSelf_Status();

    String getApptResImgURL();

    Integer getApptNotiBadgeTitle();

    Integer getApptNotiBadgeDate();

    Integer getApptNotiBadgeLoc();

    Integer getApptExpiring();

    String getApptDesc();

    Integer getApptNotiBadgeDesc();
}
