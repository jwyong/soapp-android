package com.soapp.xmpp.GroupChatHelper;

import org.jivesoftware.smack.packet.IQ;

/**
 * Created by chang on 06/07/2017.
 */

public class GroupTrackStanza extends IQ {
    public static final String ELEMENT = "pubsub";
    public static final String NAMESPACE = "http://jabber.org/protocol/pubsub";
    String nodeId;
    String userJid;
    String Lat;
    String Long;
    String userName;
    String ID;


    public GroupTrackStanza(IQ iq) {
        super(iq);
    }

    public GroupTrackStanza(String nodeId, String userJid, String Lat, String Long, String userName, String ID) {
        super(ELEMENT, NAMESPACE);
        this.nodeId = nodeId;
        this.userJid = userJid;
        this.userName = userName;
        this.ID = ID;
        this.Lat = Lat;
        this.Long = Long;
    }

    @Override
    protected IQChildElementXmlStringBuilder getIQChildElementBuilder(IQChildElementXmlStringBuilder xml) {
        xml.rightAngleBracket();
        xml.append("<publish node='").append(nodeId).append("'>").append("<item id='").append(ID).append("'>").append("<message").append(" type='chat'").append(" from='").append(userName).append("'").append(" id='").append(userJid).append("'").append(">").append("<track xmlns='urn:xmpp:tracking'").append(" latitude='").append(Lat).append("'").append(" longtitude='").append(Long).append("'/></message></item></publish>");
        return xml;
    }
}
