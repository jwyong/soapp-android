package com.soapp.SoappApi.ApiModel;

import java.util.List;

/* Created by chang on 29/09/2017. */

public class AddRoomMemberBulkModel {

    private String room_id;
    private List<String> member_ids;

    private String message_id;
    private String pubsub_host;
    private String xmpp_host;
    private String xmpp_resources;

    public AddRoomMemberBulkModel(String room_id, List<String> member_ids, String message_id, String pubsub_host, String xmpp_host, String xmpp_resources) {
        this.room_id = room_id;
        this.member_ids = member_ids;
        this.message_id = message_id;
        this.pubsub_host = pubsub_host;
        this.xmpp_host = xmpp_host;
        this.xmpp_resources = xmpp_resources;
    }

    public String getRoom_id() {
        return room_id;
    }

    public void setRoom_id(String room_id) {
        this.room_id = room_id;
    }

    public List<String> getList() {
        return member_ids;
    }

    public void setList(List<String> user_id) {
        this.member_ids = user_id;
    }

    public List<String> getMember_ids() {
        return member_ids;
    }

    public void setMember_ids(List<String> member_ids) {
        this.member_ids = member_ids;
    }

    public String getMessage_id() {
        return message_id;
    }

    public void setMessage_id(String message_id) {
        this.message_id = message_id;
    }

    public String getPubsub_host() {
        return pubsub_host;
    }

    public void setPubsub_host(String pubsub_host) {
        this.pubsub_host = pubsub_host;
    }

    public String getXmpp_host() {
        return xmpp_host;
    }

    public void setXmpp_host(String xmpp_host) {
        this.xmpp_host = xmpp_host;
    }

    public String getXmpp_resources() {
        return xmpp_resources;
    }

    public void setXmpp_resources(String xmpp_resources) {
        this.xmpp_resources = xmpp_resources;
    }

    @Override
    public String toString() {
        return String.format("%s %s %s %s %s %s ", room_id, member_ids, message_id, pubsub_host, xmpp_host, xmpp_resources);
    }
}
