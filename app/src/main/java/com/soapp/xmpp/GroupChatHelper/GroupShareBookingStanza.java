package com.soapp.xmpp.GroupChatHelper;

import com.soapp.R;
import com.soapp.setup.Soapp;

import org.jivesoftware.smack.packet.IQ;

/* Created by chang on 27/09/2017. */

public class GroupShareBookingStanza extends IQ {
    public static final String ELEMENT = "pubsub";
    public static final String NAMESPACE = "http://jabber.org/protocol/pubsub";
    String nodeId;
    String userJid;
    String userName;
    String ID;
    String bookingId, resTitle, RoomName, bookingDate, bookingTime, pax, resID, promo, qrCode;

    public GroupShareBookingStanza(String bookingId, String nodeId, String userJid, String userName, String resTitle,
                                   String ID, String RoomName, String bookingDate,
                                   String bookingTime, String pax, String resID, String promo,
                                   String qrCode) {
        super(ELEMENT, NAMESPACE);

        this.bookingId = bookingId;
        this.nodeId = nodeId;
        this.userJid = userJid;
        this.userName = userName;
        this.ID = ID;
        this.resTitle = resTitle;
        this.RoomName = RoomName;
        this.bookingDate = bookingDate;
        this.bookingTime = bookingTime;
        this.pax = pax;
        this.resID = resID;
        this.promo = promo;
        this.qrCode = qrCode;
    }

    @Override
    protected IQChildElementXmlStringBuilder getIQChildElementBuilder(IQChildElementXmlStringBuilder xml) {
        String allMsg = Soapp.getInstance().getApplicationContext().getString(R.string
                .booking_confirmed) + ": " + resTitle;

        xml.rightAngleBracket();
        xml.append("<publish node='").append(nodeId).append("'>").append("<item id='").append(ID).append("'>").append("<message").append(" type='chat'").append(" from='").append(userJid).append("'").append(" id='").append(userName).append("'").append(">").append("<body>").append(allMsg).append("</body>").append("<subject>").append(RoomName).append("</subject>").append("<thread>chat</thread>").append("<data xmlns='urn:xmpp:soapp' ChatID='ShareBookingID'").append(" bookingid='").append(bookingId).append("'").append(" bookingdate='").append(bookingDate).append("'").append(" bookingtime='").append(bookingTime).append("' ").append("pax='").append(pax).append("' resid='").append(resID).append("' restitle='").append(resTitle).append("' promo='").append(promo).append("' booker='").append(userName).append("' qrcode='").append(qrCode).append("'/></message></item></publish>");
        return xml;
    }
}
