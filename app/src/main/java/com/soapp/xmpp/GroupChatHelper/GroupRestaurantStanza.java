package com.soapp.xmpp.GroupChatHelper;

import com.soapp.R;
import com.soapp.setup.Soapp;

import org.jivesoftware.smack.packet.IQ;

/* Created by chang on 27/09/2017. */

public class GroupRestaurantStanza extends IQ {
    public static final String ELEMENT = "pubsub";
    public static final String NAMESPACE = "http://jabber.org/protocol/pubsub";
    String nodeId;
    String userJid;
    String userName;
    String ID;
    String resID;
    String RestaurantTitle;
    String RoomName;

    public GroupRestaurantStanza(IQ iq) {
        super(iq);
    }

    public GroupRestaurantStanza(String nodeId, String userJid, String userName, String RestaurantTitle, String ID, String resID, String RoomName) {
        super(ELEMENT, NAMESPACE);
        this.nodeId = nodeId;
        this.userJid = userJid;
        this.userName = userName;
        this.ID = ID;
        this.resID = resID;
        this.RestaurantTitle = RestaurantTitle;
        this.RoomName = RoomName;
    }

    @Override
    protected IQChildElementXmlStringBuilder getIQChildElementBuilder(IQChildElementXmlStringBuilder xml) {
        xml.rightAngleBracket();
        xml.append("<publish node='").append(nodeId).append("'>").append("<item id='").append(ID).append("'>").append("<message").append(" type='chat'").append(" from='").append(userJid).append("'").append(" id='").append(userName).append("'").append(">").append("<body>").append(Soapp.getInstance().getApplicationContext().getString(R.string
                .restaurant_received)).append("</body>").append("<subject>").append(RoomName).append("</subject>").append("<thread>chat</thread>").append("<data ").append("xmlns='urn:xmpp:soapp' ChatID='RestaurantID'").append(" RestaurantTitle='").append(RestaurantTitle).append("' InfoId='").append(resID).append("'/></message></item></publish>");
        return xml;
    }
}
