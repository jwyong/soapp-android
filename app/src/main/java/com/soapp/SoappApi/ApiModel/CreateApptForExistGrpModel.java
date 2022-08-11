package com.soapp.SoappApi.ApiModel;

public class CreateApptForExistGrpModel {

    private String room_id, appointment_title, appointment_date, appointment_time, location_name, location_address, location_lon, location_lat, location_map_url;

    private String appointment_id;

    public CreateApptForExistGrpModel(String room_id, String appointment_title, String appointment_date,
                                      String appointment_time, String location_name, String location_address,
                                      String location_lon, String location_lat, String location_map_url) {
        this.room_id = room_id;
        this.appointment_title = appointment_title;
        this.appointment_date = appointment_date;
        this.appointment_time = appointment_time;
        this.location_name = location_name;
        this.location_address = location_address;
        this.location_lon = location_lon;
        this.location_lat = location_lat;
        this.location_map_url = location_map_url;
    }

    public CreateApptForExistGrpModel(String appointment_id) {
        this.appointment_id = appointment_id;
    }

    public String getRoom_id() {
        return room_id;
    }

    public void setRoom_id(String room_id) {
        this.room_id = room_id;
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

    public String getAppointment_id() {
        return appointment_id;
    }

    public void setAppointment_id(String appointment_id) {
        this.appointment_id = appointment_id;
    }
}
