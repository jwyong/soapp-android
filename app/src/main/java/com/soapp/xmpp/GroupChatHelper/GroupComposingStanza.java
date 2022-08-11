package com.soapp.xmpp.GroupChatHelper;

import org.jivesoftware.smack.packet.IQ;

/* Created by chang on 06/07/2017. */

public class GroupComposingStanza extends IQ {

    public static final String ELEMENT = "pubsub";
    public static final String NAMESPACE = "http://jabber.org/protocol/pubsub";
    String nodeId;
    String userJid;
    String userName;

    public GroupComposingStanza(IQ iq) {
        super(iq);
    }

    public GroupComposingStanza(String nodeId, String userJid, String userName) {
        super(ELEMENT, NAMESPACE);
        this.nodeId = nodeId;
        this.userJid = userJid;
        this.userName = userName;
    }

    @Override
    protected IQChildElementXmlStringBuilder getIQChildElementBuilder(IQChildElementXmlStringBuilder xml) {
        xml.rightAngleBracket();
        xml.append("<publish node='").append(nodeId).append("'>").append("<item>").append("<message type = '")
                .append("chat").append("'").append(" from='").append(userJid).append("'").append(" name='")
                .append(userName).append("'>").append("<composing xmlns='http://jabber.org/protocol/chatstates'/></message></item></publish>");
        return xml;
    }
}
