package com.soapp.sql.room.joiners;

import com.soapp.sql.room.entity.Appointment;
import com.soapp.sql.room.entity.ContactRoster;

import androidx.room.Embedded;

/* Created by ibrahim on 06/04/2018. */

public class Applist {

    @Embedded
    private Appointment appointment;
    @Embedded
    private ContactRoster contactRoster;

    public ContactRoster getContactRoster() {
        return contactRoster;
    }

    public void setContactRoster(ContactRoster contactRoster) {
        this.contactRoster = contactRoster;
    }

    public Appointment getAppointment() {
        return appointment;
    }

    public void setAppointment(Appointment appointment) {
        this.appointment = appointment;
    }

}
