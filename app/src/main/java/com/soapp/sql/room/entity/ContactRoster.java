package com.soapp.sql.room.entity;

import android.content.ContentValues;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

/* Created by ibrahim on 14/03/2018. */

@Entity(tableName = "CONTACT_ROSTER")
public class ContactRoster implements ContactRosterInterface {
    @PrimaryKey(autoGenerate = true)
    private Integer ContactRow;

    @ColumnInfo(name = "ContactJid")
    private String ContactJid;

    @ColumnInfo(name = "PhoneNumber")
    private String phonenumber;

    @ColumnInfo(name = "PhoneName")
    private String phonename;

    @ColumnInfo(name = "DisplayName")
    private String displayname;

    @ColumnInfo(name = "PhotoUrl")
    private String photourl;

    @ColumnInfo(name = "CRResourceID")
    private String CRResourceID;

    @ColumnInfo(name = "ProfilePhoto")
    private byte[] profilephoto;

    @ColumnInfo(name = "ProfileFull")
    private byte[] profilefull;

    @ColumnInfo(name = "Selected")
    private Integer selected;

    private Integer DisabledStatus = 0;

    public ContactRoster() {
    }

    @Ignore
    public ContactRoster(Integer contactRow, String contactJid, String phonenumber, String phonename,
                         String displayname, String photourl, byte[] profilephoto, byte[] profilefull,
                         Integer selected, Integer disabledStatus, String CRResourceID) {
        this.ContactRow = contactRow;
        this.ContactJid = contactJid;
        this.phonenumber = phonenumber;
        this.phonename = phonename;
        this.displayname = displayname;
        this.photourl = photourl;
        this.CRResourceID = CRResourceID;
        this.profilephoto = profilephoto;
        this.profilefull = profilefull;
        this.selected = selected;
        this.DisabledStatus = disabledStatus;
    }

    @Ignore
    public ContactRoster(String displayname, String ContactJid, String phonename, String phonenumber,
                         byte[] profilephoto, int selected, String photourl, byte[] profilefull, String CRResourceID) {
        this.displayname = displayname;
        this.ContactJid = ContactJid;
        this.phonename = phonename;
        this.phonenumber = phonenumber;
        this.profilephoto = profilephoto;
        this.selected = selected;
        this.photourl = photourl;
        this.CRResourceID = CRResourceID;
        this.profilefull = profilefull;
    }

    //for adding self's info to gms list
    @Ignore
    public ContactRoster(String phonename, byte[] profilephoto) {
        this.phonename = phonename;
        this.profilephoto = profilephoto;
    }

    @Ignore
    public ContactRoster(String displayname, byte[] profilephoto, byte[] profilefull, String phonenumber, String phonename) {
        this.displayname = displayname;
        this.profilephoto = profilephoto;
        this.profilefull = profilefull;
        this.phonenumber = phonenumber;
        this.phonename = phonename;
    }

    @Ignore
    public ContactRoster(String ContactJid) {
        this.ContactJid = ContactJid;
    }

    public static ContactRoster fromContentValues(ContentValues values) {
        final ContactRoster roster = new ContactRoster();

        if (values.containsKey("ContactJid")) {
            roster.ContactJid = values.getAsString("ContactJid");
        }
        if (values.containsKey("PhoneNumber")) {
            roster.phonenumber = values.getAsString("PhoneNumber");
        }
        if (values.containsKey("PhoneName")) {
            roster.phonename = values.getAsString("PhoneName");
        }
        if (values.containsKey("DisplayName")) {
            roster.displayname = values.getAsString("DisplayName");
        }
        if (values.containsKey("PhotoUrl")) {
            roster.photourl = values.getAsString("PhotoUrl");
        }
        if (values.containsKey("CRResourceID")) {
            roster.CRResourceID = values.getAsString("CRResourceID");
        }
        if (values.containsKey("ProfilePhoto")) {
            roster.profilephoto = values.getAsByteArray("ProfilePhoto");
        }
        if (values.containsKey("ProfileFull")) {
            roster.profilefull = values.getAsByteArray("ProfileFull");
        }
        if (values.containsKey("Selected")) {
            roster.selected = values.getAsInteger("Selected");
        }
        if (values.containsKey("DisabledStatus")) {
            roster.DisabledStatus = values.getAsInteger("DisabledStatus");
        }

        return roster;
    }

    public Integer getContactRow() {
        return ContactRow;
    }

    public void setContactRow(Integer contactRow) {
        ContactRow = contactRow;
    }

    public String getDisplayname() {
        return displayname;
    }

    public void setDisplayname(String displayname) {
        this.displayname = displayname;
    }

    public String getContactJid() {
        return ContactJid;
    }

    public void setContactJid(String contactJid) {
        this.ContactJid = contactJid;
    }

    public String getPhonename() {
        return phonename;
    }

    public void setPhonename(String phonename) {
        this.phonename = phonename;
    }

    public String getPhonenumber() {
        return phonenumber;
    }

    public void setPhonenumber(String phonenumber) {
        this.phonenumber = phonenumber;
    }

    public byte[] getProfilephoto() {
        return profilephoto;
    }

    public void setProfilephoto(byte[] profilephoto) {
        this.profilephoto = profilephoto;
    }

    public Integer getSelected() {
        return selected;
    }

    public void setSelected(Integer selected) {
        this.selected = selected;
    }

    public String getPhotourl() {
        return photourl;
    }

    public void setPhotourl(String photourl) {
        this.photourl = photourl;
    }

    public byte[] getProfilefull() {
        return profilefull;
    }

    public void setProfilefull(byte[] profilefull) {
        this.profilefull = profilefull;
    }

    public Integer getDisabledStatus() {
        return DisabledStatus;
    }

    public void setDisabledStatus(Integer disabledStatus) {
        DisabledStatus = disabledStatus;
    }

    public String getCRResourceID() {
        return CRResourceID;
    }

    public void setCRResourceID(String CRResourceID) {
        this.CRResourceID = CRResourceID;
    }
}
