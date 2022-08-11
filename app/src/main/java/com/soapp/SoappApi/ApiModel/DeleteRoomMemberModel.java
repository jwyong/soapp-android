package com.soapp.SoappApi.ApiModel;

/* Created by chang on 29/08/2017. */

public class DeleteRoomMemberModel {
    private String room_id;
    private String member_id;

    private String message_id;
    private String pubsub_host;
    private String xmpp_host;
    private String xmpp_resources;

    //response
    private String message;

    public DeleteRoomMemberModel(String room_id, String member_id, String message_id, String pubsub_host, String xmpp_host, String xmpp_resources) {
        this.room_id = room_id;
        this.member_id = member_id;
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

    public String getMember_id() {
        return member_id;
    }

    public void setMember_id(String member_id) {
        this.member_id = member_id;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
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
        return String.format("%s %s %s %s %s %s %s", room_id, member_id, message_id, pubsub_host, xmpp_host, xmpp_resources, message);
    }
}
