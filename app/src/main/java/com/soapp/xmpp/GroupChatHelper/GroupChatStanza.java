package com.soapp.xmpp.GroupChatHelper;

import com.soapp.xmpp.SmackHelper;

import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.packet.IQ;
import org.jxmpp.jid.Jid;
import org.jxmpp.jid.impl.JidCreate;
import org.jxmpp.stringprep.XmppStringprepException;

/* Created by chang on 06/07/2017. */

public class GroupChatStanza {
    private Jid to;

    {
        try {
            to = JidCreate.from("pubsub.xmpp.soappchat.com");
        } catch (XmppStringprepException e) {
            e.printStackTrace();
        }
    }

    public void GroupAckStanza(String Node, String userJid, String jidFrom, String ID) {
        GroupAckStanza groupAckStanza = new GroupAckStanza(Node, userJid, jidFrom, ID);
        groupAckStanza.setType(IQ.Type.set);
        groupAckStanza.setTo(to);
        groupAckStanza.setStanzaId("publish");
        try {
            SmackHelper.getXMPPConnection().sendStanza(groupAckStanza);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void GroupComposingMessage(String Node, String userJid, String userName) {
        userName = userName.replace("&", "&amp;").replace("<", "&lt;");

        GroupComposingStanza groupComposingStanza = new GroupComposingStanza(Node, userJid, userName);
        groupComposingStanza.setType(IQ.Type.set);
        groupComposingStanza.setTo(to);
        groupComposingStanza.setStanzaId("publish");
        try {
            SmackHelper.getXMPPConnection().sendStanza(groupComposingStanza);
        } catch (SmackException.NotConnectedException | InterruptedException ignored) {
        }
    }

    public void GroupPausedMessage(String Node, String userJid) {
        GroupPauseStanza groupPauseStanza = new GroupPauseStanza(Node, userJid);
        groupPauseStanza.setType(IQ.Type.set);
        groupPauseStanza.setTo(to);
        groupPauseStanza.setStanzaId("publish");
        try {
            SmackHelper.getXMPPConnection().sendStanza(groupPauseStanza);
        } catch (SmackException.NotConnectedException | InterruptedException ignored) {
        }
    }

    public void GroupMessage(String Node, String userJid, String userName, String Body, String ID, String RoomName) {
        userName = userName.replace("&", "&amp;").replace("<", "&lt;");
        Body = Body.replace("&", "&amp;").replace("<", "&lt;");
        RoomName = RoomName.replace("&", "&amp;").replace("<", "&lt;");

        GroupMessageStanza groupMessageStanza = new GroupMessageStanza(Node, userJid, userName, ID, Body, RoomName);
        groupMessageStanza.setType(IQ.Type.set);
        groupMessageStanza.setTo(to);
        groupMessageStanza.setStanzaId("publish");
        try {
            SmackHelper.getXMPPConnection().sendStanza(groupMessageStanza);
        } catch (SmackException.NotConnectedException | InterruptedException ignored) {
        }
    }

    public void GroupReply(String Node, String userJid, String userName, String Body, String ID, String RoomName, String reply, String replyjid, String replyid, String type, String imageid) {
        userName = userName.replace("&", "&amp;").replace("<", "&lt;");
        Body = Body.replace("&", "&amp;").replace("<", "&lt;");
        RoomName = RoomName.replace("&", "&amp;").replace("<", "&lt;");
        reply = reply.replace("&", "&amp;").replace("<", "&lt;");

        GroupReplyStanza groupReplyStanza = new GroupReplyStanza(Node, userJid, userName, ID, Body, RoomName, reply, replyjid, replyid, imageid, type);
        groupReplyStanza.setType(IQ.Type.set);
        groupReplyStanza.setTo(to);
        groupReplyStanza.setStanzaId("publish");
        try {
            SmackHelper.getXMPPConnection().sendStanza(groupReplyStanza);
        } catch (SmackException.NotConnectedException | InterruptedException ignored) {
        }
    }

    public void GroupAudio(String Node, String userJid, String userName, String resourceID, String ID,
                           String url, String RoomName) {
        userName = userName.replace("&", "&amp;").replace("<", "&lt;");
        url = url.replace("&", "&amp;").replace("<", "&lt;");
        RoomName = RoomName.replace("&", "&amp;").replace("<", "&lt;");

        GroupAudioStanza groupAudioStanza = new GroupAudioStanza(Node, userJid, userName, ID, resourceID, url,
                RoomName);
        groupAudioStanza.setType(IQ.Type.set);
        groupAudioStanza.setTo(to);
        groupAudioStanza.setStanzaId("publish");
        try {
            SmackHelper.getXMPPConnection().sendStanza(groupAudioStanza);
        } catch (SmackException.NotConnectedException | InterruptedException ignored) {
        }
    }

    public void GroupMap(String Node, String userJid, String userName, String Lat, String Long,
                         String ID, String mapURL, String RoomName, String placeName, String address) {
        userName = userName.replace("&", "&amp;").replace("<", "&lt;");
        RoomName = RoomName.replace("&", "&amp;").replace("<", "&lt;");
        mapURL = mapURL.replace("&", "&amp;").replace("<", "&lt;");

        GroupMapStanza groupMapStanza = new GroupMapStanza(Node, userJid, userName, Lat, Long, ID, mapURL, RoomName, placeName, address);
        groupMapStanza.setType(IQ.Type.set);
        groupMapStanza.setTo(to);
        groupMapStanza.setStanzaId("publish");

        try {
            SmackHelper.getXMPPConnection().sendStanza(groupMapStanza);
        } catch (SmackException.NotConnectedException | InterruptedException ignored) {
        }
    }

    public void GroupImage(String Node, String userJid, String userName, String Direction, String
            ID, String imageID, String url, String RoomName, String rotation, String image_base64) {
        userName = userName.replace("&", "&amp;").replace("<", "&lt;");
        RoomName = RoomName.replace("&", "&amp;").replace("<", "&lt;");
        url = url.replace("&", "&amp;").replace("<", "&lt;");

        GroupImageStanza groupImageStanza = new GroupImageStanza(Node, userJid, userName, Direction, ID, imageID,
                url, RoomName, rotation, image_base64);
        groupImageStanza.setType(IQ.Type.set);
        groupImageStanza.setTo(to);
        groupImageStanza.setStanzaId("publish");
        try {
            SmackHelper.getXMPPConnection().sendStanza(groupImageStanza);
        } catch (SmackException.NotConnectedException | InterruptedException ignored) {
        }
    }

    public void GroupVideo(String Node, String userJid, String userName, String videoDirection, String
            ID, String videoID, String RoomName, String video_thumb_base64) {
        userName = userName.replace("&", "&amp;").replace("<", "&lt;");
        RoomName = RoomName.replace("&", "&amp;").replace("<", "&lt;");

        GroupVideoStanza groupVideoStanza = new GroupVideoStanza(Node, userJid, userName, videoDirection, ID,
                videoID, RoomName, video_thumb_base64);
        groupVideoStanza.setType(IQ.Type.set);
        groupVideoStanza.setTo(to);
        groupVideoStanza.setStanzaId("publish");
        try {
            SmackHelper.getXMPPConnection().sendStanza(groupVideoStanza);
        } catch (SmackException.NotConnectedException | InterruptedException ignored) {
        }
    }

    public void GroupRestaurant(String Node, String userJid, String userName, String
            RestaurantTitle, String ID, String resID, String RoomName) {
        userName = userName.replace("&", "&amp;").replace("<", "&lt;");
        RestaurantTitle = RestaurantTitle.replace("&", "&amp;").replace("<", "&lt;");
        RoomName = RoomName.replace("&", "&amp;").replace("<", "&lt;");

        GroupRestaurantStanza groupRestaurantStanza = new GroupRestaurantStanza(Node, userJid, userName,
                RestaurantTitle, ID, resID, RoomName);
        groupRestaurantStanza.setType(IQ.Type.set);
        groupRestaurantStanza.setTo(to);
        groupRestaurantStanza.setStanzaId("publish");
        try {
            SmackHelper.getXMPPConnection().sendStanza(groupRestaurantStanza);
        } catch (SmackException.NotConnectedException | InterruptedException ignored) {
        }
    }

    public void GroupShareBooking(String bookingId, String Node, String userJid, String userName, String
            RestaurantTitle, String ID, String RoomName, String bookingDate,
                                  String bookingTime, String pax, String resID, String promo,
                                  String qrCode) {
        userName = userName.replace("&", "&amp;").replace("<", "&lt;");
        RestaurantTitle = RestaurantTitle.replace("&", "&amp;").replace("<", "&lt;");
        RoomName = RoomName.replace("&", "&amp;").replace("<", "&lt;");

        GroupShareBookingStanza groupShareBookingStanza = new GroupShareBookingStanza(bookingId, Node, userJid, userName,
                RestaurantTitle, ID, RoomName, bookingDate, bookingTime, pax, resID, promo, qrCode);
        groupShareBookingStanza.setType(IQ.Type.set);
        groupShareBookingStanza.setTo(to);
        groupShareBookingStanza.setStanzaId("publish");


        try {
            SmackHelper.getXMPPConnection().sendStanza(groupShareBookingStanza);
        } catch (SmackException.NotConnectedException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void GroupDeleteShareBooking(String bookingid, String Node, String userJid, String userName, String ID,
                                        String RoomName, String resTitle) {
        userName = userName.replace("&", "&amp;").replace("<", "&lt;");
        RoomName = RoomName.replace("&", "&amp;").replace("<", "&lt;");
        resTitle = resTitle.replace("&", "&amp;").replace("<", "&lt;");

        GroupDeleteShareBookingStanza groupDeleteShareBookingStanza = new GroupDeleteShareBookingStanza(bookingid, Node, userJid, userName,
                ID, RoomName, resTitle);
        groupDeleteShareBookingStanza.setType(IQ.Type.set);
        groupDeleteShareBookingStanza.setTo(to);
        groupDeleteShareBookingStanza.setStanzaId("publish");
        try {
            SmackHelper.getXMPPConnection().sendStanza(groupDeleteShareBookingStanza);
        } catch (SmackException.NotConnectedException | InterruptedException ignored) {
        }
    }

    public void GroupContact(String Node, String userJid, String userName, String contactName, String contactPhone, String ID, String RoomName) {
        userName = userName.replace("&", "&amp;").replace("<", "&lt;");
        contactName = contactName.replace("&", "&amp;").replace("<", "&lt;");
        RoomName = RoomName.replace("&", "&amp;").replace("<", "&lt;");

        GroupContactStanza groupContactStanza = new GroupContactStanza(Node, userJid, userName, contactName, contactPhone, ID, RoomName);
        groupContactStanza.setType(IQ.Type.set);
        groupContactStanza.setTo(to);
        groupContactStanza.setStanzaId("publish");
        try {
            SmackHelper.getXMPPConnection().sendStanza(groupContactStanza);
        } catch (SmackException.NotConnectedException | InterruptedException ignored) {
        }
    }

    public void GroupTrack(String Node, String userJid, String Lat, String Long, String userName, String ID) {
        userName = userName.replace("&", "&amp;").replace("<", "&lt;");

        GroupTrackStanza groupTrackStanza = new GroupTrackStanza(Node, userJid, Lat, Long, userName, ID);
        groupTrackStanza.setType(IQ.Type.set);
        groupTrackStanza.setTo(to);
        groupTrackStanza.setStanzaId("publish");
        try {
            SmackHelper.getXMPPConnection().sendStanza(groupTrackStanza);
        } catch (SmackException.NotConnectedException | InterruptedException ignored) {
        }
    }

    //type:
    //0 = delete appt
    //1 = title
    //2 = date/time
    //3 = location
    //4 = status
    //5 = create
    public void GroupAppointment(String Node, String userJid, String uniqueID, String apptTitle, String
            apptMapURL, String apptLoc, String apptLat, String apptLon, String apptDate, String apptTime,
                                 String apptSelfStatus, String apptResID, String apptResImgURL, String apptID,
                                 String roomName, String pushMsg, String pushThread, String apptDateTime,
                                 String apptUpdateType) {
        apptTitle = apptTitle.replace("&", "&amp;").replace("<", "&lt;");
        apptMapURL = apptMapURL.replace("&", "&amp;").replace("<", "&lt;");
        apptLoc = apptLoc.replace("&", "&amp;").replace("<", "&lt;");
        apptResImgURL = apptResImgURL.replace("&", "&amp;").replace("<", "&lt;");
        roomName = roomName.replace("&", "&amp;").replace("<", "&lt;");

        GroupAppointmentStanza groupAppointmentStanza = new GroupAppointmentStanza(Node, userJid, uniqueID, apptTitle, apptMapURL,
                apptLoc, "", "", apptLat, apptLon, apptDate, apptTime, apptSelfStatus, apptResID, apptResImgURL,
                apptID, roomName, pushMsg, pushThread, apptDateTime, apptUpdateType);
        groupAppointmentStanza.setType(IQ.Type.set);
        groupAppointmentStanza.setTo(to);
        groupAppointmentStanza.setStanzaId("publish");

        try {
            SmackHelper.getXMPPConnection().sendStanza(groupAppointmentStanza);
        } catch (SmackException.NotConnectedException | InterruptedException ignored) {
        }
    }

    //create new appt for existing grp
    public void GroupNewAppointment(String Node, String userJid, String userName, String Body, String ID, String RoomName, String apptID) {
        userName = userName.replace("&", "&amp;").replace("<", "&lt;");
        Body = Body.replace("&", "&amp;").replace("<", "&lt;");
        RoomName = RoomName.replace("&", "&amp;").replace("<", "&lt;");

        GroupNewApptStanza groupNewApptStanza = new GroupNewApptStanza(Node, userJid, userName, ID, Body, RoomName, apptID);
        groupNewApptStanza.setType(IQ.Type.set);
        groupNewApptStanza.setTo(to);
        groupNewApptStanza.setStanzaId("publish");

        try {
            SmackHelper.getXMPPConnection().sendStanza(groupNewApptStanza);
        } catch (SmackException.NotConnectedException | InterruptedException ignored) {
        }
    }
}
