package com.soapp.SoappApi.ApiModel;

import java.util.List;

/* Created by Soapp on 30/10/2017. */

public class GetOldGrpModel {

    private String room_id, room_name, resource_id, resource_url, resource_version, image_data;

    private List<GetOldGrpMemberModel> users;

    private List<GetGrpApptModel> room_appointments;

    public List getUsers() {
        return users;
    }

    public void setUsers(List users) {
        this.users = users;
    }

    public String getRoom_id() {
        return room_id;
    }

    public void setRoom_id(String room_id) {
        this.room_id = room_id;
    }

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

    public String getResource_version() {
        return resource_version;
    }

    public void setResource_version(String resource_version) {
        this.resource_version = resource_version;
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
        return String.format("%s %s ",room_id, room_name);
    }
}
