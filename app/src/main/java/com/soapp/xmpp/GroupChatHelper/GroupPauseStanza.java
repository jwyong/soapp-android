package com.soapp.xmpp.GroupChatHelper;

import org.jivesoftware.smack.packet.IQ;

/**
 * Created by chang on 06/07/2017.
 */

public class GroupPauseStanza extends IQ {

    public static final String ELEMENT = "pubsub";
    public static final String NAMESPACE = "http://jabber.org/protocol/pubsub";
    String nodeId;
    String userJid;

    public GroupPauseStanza(IQ iq) {
        super(iq);
    }

    public GroupPauseStanza(String nodeId, String userJid) {
        super(ELEMENT, NAMESPACE);
        this.nodeId = nodeId;
        this.userJid = userJid;
    }

    @Override
    protected IQChildElementXmlStringBuilder getIQChildElementBuilder(IQChildElementXmlStringBuilder xml) {
        xml.rightAngleBracket();
        xml.append("<publish node='").append(nodeId).append("'>").append("<item>").append("<message type = '").append("chat").append("'").append(" from='").append(userJid).append("'>").append("<paused xmlns='http://jabber.org/protocol/chatstates'/></message></item></publish>");
        return xml;
    }
}
