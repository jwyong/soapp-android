package com.soapp.SoappApi.ApiModel;

import java.util.List;

public class GetGrpApptModel {
    private String appointment_id;
    private String appointment_title;
    private String appointment_date;
    private String appointment_time;
    private String appointment_timestamp;
    private String location_name;
    private String location_address;
    private String location_lon;
    private String location_lat;
    private String location_map_url;
    private List<AppointmentMemStatus> appointment_users;

    public GetGrpApptModel(String appointment_id) {
        this.appointment_id = appointment_id;
    }

    public String getAppointment_id() {
        return appointment_id;
    }

    public void setAppointment_id(String appointment_id) {
        this.appointment_id = appointment_id;
    }

    public String getAppointment_title() {
        return appointment_title;
    }

    public void setAppointment_title(String appointment_title) {
        this.appointment_title = appointment_title;
    }

    public String getAppointment_date() {
        return appointment_date;
    }

    public void setAppointment_date(String appointment_date) {
        this.appointment_date = appointment_date;
    }

    public String getAppointment_time() {
        return appointment_time;
    }

    public void setAppointment_time(String appointment_time) {
        this.appointment_time = appointment_time;
    }

    public String getAppointment_timestamp() {
        return appointment_timestamp;
    }

    public void setAppointment_timestamp(String appointment_timestamp) {
        this.appointment_timestamp = appointment_timestamp;
    }

    public String getLocation_name() {
        return location_name;
    }

    public void setLocation_name(String location_name) {
        this.location_name = location_name;
    }

    public String getLocation_address() {
        return location_address;
    }

    public void setLocation_address(String location_address) {
        this.location_address = location_address;
    }

    public String getLocation_lon() {
        return location_lon;
    }

    public void setLocation_lon(String location_lon) {
        this.location_lon = location_lon;
    }

    public String getLocation_lat() {
        return location_lat;
    }

    public void setLocation_lat(String location_lat) {
        this.location_lat = location_lat;
    }

    public String getLocation_map_url() {
        return location_map_url;
    }

    public void setLocation_map_url(String location_map_url) {
        this.location_map_url = location_map_url;
    }

    public List<AppointmentMemStatus> getAppointment_users() {
        return appointment_users;
    }

    public void setAppointment_users(List<AppointmentMemStatus> appointment_users) {
        this.appointment_users = appointment_users;
    }
}

