package com.soapp.xmpp.GroupChatHelper;

import org.jivesoftware.smack.packet.IQ;

/* Created by chang on 06/07/2017. */

public class GroupReplyStanza extends IQ {

    public static final String ELEMENT = "pubsub";
    public static final String NAMESPACE = "http://jabber.org/protocol/pubsub";
    String nodeId;
    String userJid;
    String userName;
    String ID;
    String Body;
    String RoomName;
    String reply, replyjid, replyid, imageid, replytype;

    public GroupReplyStanza(IQ iq) {
        super(iq);
    }

    public GroupReplyStanza(String nodeId, String userJid, String userName, String ID, String Body, String RoomName, String reply, String replyjid, String replyid, String imageid, String replytype) {
        super(ELEMENT, NAMESPACE);
        this.nodeId = nodeId;
        this.userJid = userJid;
        this.userName = userName;
        this.ID = ID;
        this.Body = Body;
        this.RoomName = RoomName;
        this.reply = reply;
        this.replyjid = replyjid;
        this.replyid = replyid;
        this.imageid = imageid;
        this.replytype = replytype;
    }

    @Override
    protected IQChildElementXmlStringBuilder getIQChildElementBuilder(IQChildElementXmlStringBuilder xml) {
        xml.rightAngleBracket();
        xml.append("<publish node='").append(nodeId).append("'>").append("<item id='").append(ID).append("'>").
                append("<message").append(" type='").append("chat").append("'").append(" from='").append(userJid).
                append("'").append(" id='").append(userName).append("'>").append("<body>").append(Body).append("</body>").
                append("<subject>").append(RoomName).append("# ").append(userName).append("</subject>").append("<thread>chat</thread>").
                append("<data xmlns='urn:xmpp:soapp' ChatID='ReplyID'").append(" reply='").append(reply).append("'").append(" replyjid='").
                append(replyjid).append("'").append(" replyid='").append(replyid).append("'").append(" replytype='").append(replytype).append("'").
                append(" imageid='").append(imageid).append("'").append("/></message></item></publish>");
        return xml;
    }
}
