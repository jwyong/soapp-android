package com.soapp.SoappApi.ApiModel;

public class AppointmentMemStatus {
    private String user_jid;
    private Integer appointment_status;
    private String updated_at;

    public String getUser_jid() {
        return user_jid;
    }

    public void setUser_jid(String user_jid) {
        this.user_jid = user_jid;
    }

    public Integer getAppointment_status() {
        return appointment_status;
    }

    public void setAppointment_status(Integer appointment_status) {
        this.appointment_status = appointment_status;
    }

    public String getUpdated_at() {
        return updated_at;
    }

    public void setUpdated_at(String updated_at) {
        this.updated_at = updated_at;
    }
}
