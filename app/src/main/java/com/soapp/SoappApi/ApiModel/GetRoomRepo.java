package com.soapp.SoappApi.ApiModel;

import java.util.Arrays;
import java.util.List;

/* Created by chang on 16/08/2017. */

public class GetRoomRepo {

    private String room_name, resource_id, resource_url, image_data;
    private List<GetMemberListModel> users;
    private List<GetGrpApptModel> room_appointments;

    public String getRoom_name() {
        return room_name;
    }

    public void setRoom_name(String room_name) {
        this.room_name = room_name;
    }

    public String getResource_id() {
        return resource_id;
    }

    public void setResource_id(String resource_id) {
        this.resource_id = resource_id;
    }

    public String getResource_url() {
        return resource_url;
    }

    public void setResource_url(String resource_url) {
        this.resource_url = resource_url;
    }

    public List getUsers() {
        return users;
    }

    public void setUsers(List users) {
        this.users = users;
    }

    public String getImage_data() {
        return image_data;
    }

    public void setImage_data(String image_data) {
        this.image_data = image_data;
    }

    public List<GetGrpApptModel> getRoom_appointments() {
        return room_appointments;
    }

    public void setRoom_appointments(List<GetGrpApptModel> room_appointments) {
        this.room_appointments = room_appointments;
    }

    @Override
    public String toString() {
        return String.format("%s %s %s %s  ", room_name, resource_id, resource_url, image_data, Arrays.toString(room_appointments.toArray()));
    }
}
