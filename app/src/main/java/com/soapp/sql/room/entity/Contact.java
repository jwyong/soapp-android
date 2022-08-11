package com.soapp.sql.room.entity;

import android.content.ContentValues;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

/**
 * Created by ibrahim on 13/03/2018.
 */

@Entity(tableName = "CONTACT")
public class Contact {

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "_id")
    private Integer _id;

    @ColumnInfo(name = "PhoneNumber", typeAffinity = 2)
    private String phonenumber;

    public Contact() {
    }

    @Ignore
    public Contact(int _id, String phonenumber) {
        this._id = _id;
        this.phonenumber = phonenumber;
    }

    public static Contact fromContentValues(ContentValues values) {

        final Contact contact = new Contact();

        if (values.containsKey("_id")) {
            contact._id = values.getAsInteger("_id");
        }
        if (values.containsKey("PhoneNumber")) {
            contact.phonenumber = values.getAsString("PhoneNumber");
        }

        return contact;
    }

    public Integer get_id() {
        return _id;
    }

    public void set_id(Integer _id) {
        this._id = _id;
    }

    public String getPhonenumber() {
        return phonenumber;
    }

    public void setPhonenumber(String phonenumber) {
        this.phonenumber = phonenumber;
    }
}
