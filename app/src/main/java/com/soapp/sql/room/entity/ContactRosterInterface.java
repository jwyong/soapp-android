package com.soapp.sql.room.entity;

/**
 * Created by ibrahim on 16/03/2018.
 */

public interface ContactRosterInterface {

    Integer getContactRow();

    String getContactJid();

    String getPhonenumber();

    String getPhonename();

    String getDisplayname();

    String getPhotourl();

    String getCRResourceID();

    byte[] getProfilephoto();

    byte[] getProfilefull();

    Integer getSelected();

    Integer getDisabledStatus();
}
