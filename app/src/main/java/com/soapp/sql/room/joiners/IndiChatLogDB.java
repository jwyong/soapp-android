package com.soapp.sql.room.joiners;

import com.soapp.sql.room.entity.Appointment;
import com.soapp.sql.room.entity.AppointmentHist;
import com.soapp.sql.room.entity.Message;

import androidx.room.Embedded;

public class IndiChatLogDB {
    @Embedded
    private
    Message message;

    @Embedded
    private
    Appointment appointment;

    @Embedded
    private
    AppointmentHist appointmentHist;

    public Message getMessage() {
        return message;
    }

    public void setMessage(Message message) {
        this.message = message;
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
}
