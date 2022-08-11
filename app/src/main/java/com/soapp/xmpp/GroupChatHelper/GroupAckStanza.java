package com.soapp.xmpp.GroupChatHelper;

import org.jivesoftware.smack.packet.IQ;

/* Created by chang on 06/07/2017. */

public class GroupAckStanza extends IQ {

    public static final String ELEMENT = "pubsub";
    public static final String NAMESPACE = "http://jabber.org/protocol/pubsub";
    String nodeId;
    String userJid;
    String jidFrom;
    String ID;

    public GroupAckStanza(IQ iq) {
        super(iq);
    }

    public GroupAckStanza(String nodeId, String userJid, String jidFrom, String ID) {
        super(ELEMENT, NAMESPACE);
        this.nodeId = nodeId;
        this.userJid = userJid;
        this.jidFrom = jidFrom;
        this.ID = ID;
    }

    @Override
    protected IQChildElementXmlStringBuilder getIQChildElementBuilder(IQChildElementXmlStringBuilder xml) {
        xml.rightAngleBracket();
        xml.append("<publish node='").append(nodeId).append("'>").append("<item id='").append(ID).append("'>").
                append("<message type='").append("chat").append("'").append(" from='").append(userJid).append("'").
                append(" to='").append(jidFrom).append("'").append(" id='ackgroup'>").append("<received ").
                append("xmlns='urn:xmpp:receipts' id='").append(ID).append("'/></message></item></publish>");
        return xml;
    }
}
