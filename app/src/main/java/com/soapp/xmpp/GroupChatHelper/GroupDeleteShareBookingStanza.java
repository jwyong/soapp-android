package com.soapp.xmpp.GroupChatHelper;

import com.soapp.R;
import com.soapp.setup.Soapp;

import org.jivesoftware.smack.packet.IQ;

/* Created by chang on 27/09/2017. */

public class GroupDeleteShareBookingStanza extends IQ {
    public static final String ELEMENT = "pubsub";
    public static final String NAMESPACE = "http://jabber.org/protocol/pubsub";
    private final String bookingid;
    String nodeId;
    String userJid;
    String userName;
    String ID;
    String resTitle, RoomName, resID;

    public GroupDeleteShareBookingStanza(String bookingid, String nodeId, String userJid, String userName, String ID,
                                         String RoomName, String resTitle) {
        super(ELEMENT, NAMESPACE);

        this.nodeId = nodeId;
        this.userJid = userJid;
        this.userName = userName;
        this.ID = ID;
        this.RoomName = RoomName;
        this.resTitle = resTitle;
        this.resID = resID;
        this.bookingid = bookingid;
    }

    @Override
    protected IQChildElementXmlStringBuilder getIQChildElementBuilder(IQChildElementXmlStringBuilder xml) {
        String allMsg = Soapp.getInstance().getApplicationContext().getString(R.string
                .booking_confirmed) + ": " + resTitle;

        xml.rightAngleBracket();
        xml.append("<publish node='").append(nodeId).append("'>").append("<item id='").append(ID).append("'>").append("<message").append(" type='chat'").append(" from='").append(userJid).append("'").append(" id='").append(userName).append("'").append(">").append("<body>").append(allMsg).append("</body>").append("<subject>").append(RoomName).append("</subject>").append("<thread>chat</thread>").append("<data xmlns='urn:xmpp:soapp' ChatID='DeleteShareBookingID'").append(" bookingid=").append(bookingid).append("'/></message></item></publish>");
        return xml;
    }
}
