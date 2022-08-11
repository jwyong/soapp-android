package com.soapp.SoappApi.ApiModel;

/* Created by Soapp on 18/10/2017. */

public class CreateGroupModel {

    private String room_id, appointment_id;

    public CreateGroupModel(String room_id, String appointment_id) {
        this.room_id = room_id;
        this.appointment_id = appointment_id;
    }

    public String getRoom_id() {
        return room_id;
    }

    public void setRoom_id(String room_id) {
        this.room_id = room_id;
    }

    public String getAppointment_id() {
        return appointment_id;
    }

    public void setAppointment_id(String appointment_id) {
        this.appointment_id = appointment_id;
    }
}
