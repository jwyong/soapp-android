package com.soapp.sql.room.joiners;

import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import com.soapp.R;
import com.soapp.global.GlideAPI.GlideApp;
import com.soapp.global.GlobalVariables;
import com.soapp.sql.room.entity.Appointment;
import com.soapp.sql.room.entity.AppointmentHist;
import com.soapp.sql.room.entity.ContactRoster;
import com.soapp.sql.room.entity.GroupMem;
import com.soapp.sql.room.entity.GroupMem_Status;
import com.soapp.sql.room.entity.Message;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import androidx.databinding.BindingAdapter;
import androidx.room.Embedded;

public class GroupChatLogDB {

    @Embedded
    private
    ContactRoster contactRoster;

    @Embedded
    private
    Message message;

    @Embedded
    private
    GroupMem groupMem;

    @Embedded
    private
    Appointment appointment;

    @Embedded
    private
    AppointmentHist appointmentHist;

    @Embedded
    private
    GroupMem_Status groupMemStatus;

    public ContactRoster getContactRoster() {
        return contactRoster;
    }

    public void setContactRoster(ContactRoster contactRoster) {
        this.contactRoster = contactRoster;
    }

    public Message getMessage() {
        return message;
    }

    public void setMessage(Message message) {
        this.message = message;
    }

    public GroupMem getGroupMem() {
        return groupMem;
    }

    public void setGroupMem(GroupMem groupMem) {
        this.groupMem = groupMem;
    }

    public GroupMem_Status getGroupMemStatus() {
        return groupMemStatus;
    }

    public void setGroupMemStatus(GroupMem_Status groupMemStatus) {
        this.groupMemStatus = groupMemStatus;
    }

    public AppointmentHist getAppointmentHist() {
        return appointmentHist;
    }

    public void setAppointmentHist(AppointmentHist appointmentHist) {
        this.appointmentHist = appointmentHist;
    }

    public Appointment getAppointment() {
        return appointment;
    }

    public void setAppointment(Appointment appointment) {
        this.appointment = appointment;
    }

    //functions for data binding
    //date/time header
    public String getDateHeader() {
        return new SimpleDateFormat("EEE, dd MMM yyyy", Locale.ENGLISH).format(message.getMsgDate());
    }

    //msg time
    public String getMsgTime() {
        return DateFormat.getTimeInstance(DateFormat.SHORT, Locale.ENGLISH).format(new Date(message.getMsgDate()));
    }

    //member displayName
    public String getDisplayName() {
        //set displayname
        String phoneName = contactRoster.getPhonename();

        if (phoneName == null || phoneName.equals("")) {
            String displayName = contactRoster.getDisplayname();
            if (displayName == null) {
                phoneName = "";
            } else {
                phoneName = displayName + " " + contactRoster.getPhonenumber();
            }
        }

        return phoneName;
    }

    //glide img
    public String getImgPath() {
        return GlobalVariables.IMAGES_SENT_PATH + "/" + message.getMsgInfoUrl();
    }

    @BindingAdapter(value = {"glideImg"})
    public static void glideChatImg(ImageView view, String imgPath) {
        GlideApp.with(view)
                .load(imgPath)
                .placeholder(R.drawable.default_img_400)
                .dontTransform()
                .centerCrop()
                .thumbnail(0.5f)
                .override(300, 300)
                .encodeQuality(50)
                .into(view);
    }
}
