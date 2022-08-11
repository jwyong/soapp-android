package com.soapp.xmpp.PubsubHelper;

import org.jivesoftware.smack.packet.IQ;

/**
 * Created by chang on 25/08/2017.
 */

//individual update profile
public class PubsubUserStatus extends IQ {

    public static final String ELEMENT = "pubsub";
    public static final String NAMESPACE = "http://jabber.org/protocol/pubsub";
    String nodeId;
    String userJid;
    String ID;

    public PubsubUserStatus(IQ iq) {
        super(iq);
    }

    public PubsubUserStatus(String nodeId, String userJid, String ID) {
        super(ELEMENT, NAMESPACE);
        this.nodeId = nodeId;
        this.userJid = userJid;
        this.ID = ID;
    }

    @Override
    protected IQChildElementXmlStringBuilder getIQChildElementBuilder(IQChildElementXmlStringBuilder xml) {
        xml.rightAngleBracket();
        xml.append("<publish node='").append(nodeId).append("'>").append("<item id='").append(ID).append("'>").append("<message").append(" type='").append("chat").append("'").append(" from='").append(userJid).append("'").append(" id='memberinfo'>").append("<single xmlns='urn:xmpp:pubsubsingle'").append("/></message></item></publish>");
        return xml;
    }
}
