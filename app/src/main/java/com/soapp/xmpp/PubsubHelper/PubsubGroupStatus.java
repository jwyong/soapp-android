package com.soapp.xmpp.PubsubHelper;

import org.jivesoftware.smack.packet.IQ;

/**
 * Created by chang on 26/08/2017.
 */

public class PubsubGroupStatus extends IQ {
    public static final String ELEMENT = "pubsub";
    public static final String NAMESPACE = "http://jabber.org/protocol/pubsub";
    String nodeId;
    String userJid;
    String ID;
    String publishGroupInfo;
    String deleteGroup;

    public PubsubGroupStatus(IQ iq) {
        super(iq);
    }

    public PubsubGroupStatus(String nodeId, String userJid, String ID, String updateGrpProfileJid, String deleteGroup) {
        super(ELEMENT, NAMESPACE);
        this.nodeId = nodeId;
        this.userJid = userJid;
        this.ID = ID;
        this.publishGroupInfo = updateGrpProfileJid;
        this.deleteGroup = deleteGroup;
    }

    @Override
    protected IQChildElementXmlStringBuilder getIQChildElementBuilder(IQChildElementXmlStringBuilder xml) {
        xml.rightAngleBracket();
        xml.append("<publish node='").append(nodeId).append("'>").append("<item id='").append(ID).append("'>").append("<message").append(" type='").append("chat").append("'").append(" from='").append(userJid).append("'").append(" id='deleteaddassign'>").append("<pubsubmember xmlns='urn:xmpp:pubsubmember'").append(" publishGroupInfo='").append(publishGroupInfo).append("'").append(" groupJid=''").append(" addingGroup=''").append(" addingAdmin=''").append(" deleteGroup='").append(deleteGroup).append("'").append("/></message></item></publish>");
        return xml;
    }
}