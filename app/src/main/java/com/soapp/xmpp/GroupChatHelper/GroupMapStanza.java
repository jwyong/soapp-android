package com.soapp.xmpp.GroupChatHelper;

import com.soapp.R;
import com.soapp.setup.Soapp;

import org.jivesoftware.smack.packet.IQ;

/* Created by chang on 06/07/2017. */

public class GroupMapStanza extends IQ {
    public static final String ELEMENT = "pubsub";
    public static final String NAMESPACE = "http://jabber.org/protocol/pubsub";
    String nodeId;
    String userJid;
    String userName;
    String ID;
    String Body;
    String Lat;
    String Long;
    String RoomName;
    String placeName;
    String address;

    public GroupMapStanza(String nodeId, String userJid, String userName, String Lat, String Long, String ID, String Body, String RoomName, String placeName, String address) {
        super(ELEMENT, NAMESPACE);
        this.nodeId = nodeId;
        this.userJid = userJid;
        this.userName = userName;
        this.ID = ID;
        this.Body = Body;
        this.Lat = Lat;
        this.Long = Long;
        this.RoomName = RoomName;
        this.placeName = placeName;
        this.address = address;
    }

    @Override
    protected IQChildElementXmlStringBuilder getIQChildElementBuilder(IQChildElementXmlStringBuilder xml) {
        xml.rightAngleBracket();
        xml.append("<publish node='").append(nodeId).append("'>").append("<item id='").append(ID).append("'>").append("<message").append(" type='chat'").append(" from='").append(userJid).append("'").append(" id='").append(userName).append("'").append(">").append("<body>").append(Soapp.getInstance().getApplicationContext().getString(R.string
                .location_received)).append("</body>").append("<subject>").append(RoomName).append("</subject>").append("<thread>chat</thread>").append("<data xmlns='urn:xmpp:soapp' ChatID='MapID'").append(" latitude='").append(Lat).append("'").append(" longtitude='").append(Long).append("'").append(" staticurl='").append(Body).append("'").append(" infoid='").append(placeName).append("'").append(" address='").append(address).append("'").append("/></message></item></publish>");

        return xml;
    }
}


