package com.soapp.xmpp.GroupChatHelper;

import org.jivesoftware.smack.packet.IQ;

/* Created by chang on 06/07/2017. */

public class GroupMessageStanza extends IQ {

    public static final String ELEMENT = "pubsub";
    public static final String NAMESPACE = "http://jabber.org/protocol/pubsub";
    String nodeId;
    String userJid;
    String userName;
    String ID;
    String Body;
    String RoomName;

    public GroupMessageStanza(IQ iq) {
        super(iq);
    }

    public GroupMessageStanza(String nodeId, String userJid, String userName, String ID, String Body, String RoomName) {
        super(ELEMENT, NAMESPACE);
        this.nodeId = nodeId;
        this.userJid = userJid;
        this.userName = userName;
        this.ID = ID;
        this.Body = Body;
        this.RoomName = RoomName;
    }

    @Override
    protected IQChildElementXmlStringBuilder getIQChildElementBuilder(IQChildElementXmlStringBuilder xml) {

        xml.rightAngleBracket();
        xml.halfOpenElement("publish");
        xml.attribute("node", nodeId);
        xml.rightAngleBracket();

        xml.halfOpenElement("item");
        xml.attribute("id", ID);
        xml.rightAngleBracket();

        xml.halfOpenElement("message");
        xml.attribute("type", "chat");
        xml.attribute("from", userJid);
        xml.attribute("id", userName);
        xml.rightAngleBracket();


        xml.openElement("body");
        xml.append(Body);
        xml.closeElement("body");

        xml.openElement("subject");
        xml.append(RoomName).append("# ").append(userName);
        xml.closeElement("subject");
        xml.openElement("thread");
        xml.append("chat");
        xml.closeElement("thread");

        xml.halfOpenElement("data");
        xml.xmlnsAttribute("urn:xmpp:soapp");
        xml.attribute("ChatID", "Message");
        xml.closeEmptyElement();

        xml.closeElement("message");
        xml.closeElement("item");
        xml.closeElement("publish");


//        xml.append("<publish node='" + (nodeId) + "'>" + "<item id='" + (ID) + "'>" + "<message"
//                + " type='" + ("chat") + "'" + " from='" + (userJid) + "'" + " id='" + (userName) + "'>"
//                + "<body>" + (Body) + "</body>" + "<subject>" + (RoomName + "# " + userName) + "</subject>"
//                + "<thread>chat</thread>" + "<data xmlns='urn:xmpp:soapp' ChatID='Message'/></message></item></publish>");
        return xml;
    }
}
