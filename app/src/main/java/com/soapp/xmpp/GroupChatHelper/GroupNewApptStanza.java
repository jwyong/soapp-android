package com.soapp.xmpp.GroupChatHelper;

import org.jivesoftware.smack.packet.IQ;

public class GroupNewApptStanza extends IQ {

    public static final String ELEMENT = "pubsub";
    public static final String NAMESPACE = "http://jabber.org/protocol/pubsub";
    String nodeId;
    String userJid;
    String userName;
    String ID;
    String Body;
    String RoomName;
    String apptID;

    public GroupNewApptStanza(IQ iq) {
        super(iq);
    }

    public GroupNewApptStanza(String nodeId, String userJid, String userName, String ID, String Body, String RoomName, String apptID) {
        super(ELEMENT, NAMESPACE);
        this.nodeId = nodeId;
        this.userJid = userJid;
        this.userName = userName;
        this.ID = ID;
        this.Body = Body;

        //here;
        this.RoomName = RoomName;
        this.apptID = apptID;
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
        xml.append("appointment");
        xml.closeElement("thread");

        xml.halfOpenElement("data");
        xml.xmlnsAttribute("urn:xmpp:soapp");
        xml.attribute("ChatID", "NewAppointmentID");
        xml.attribute("apptID", apptID);
        xml.closeEmptyElement();

        xml.closeElement("message");
        xml.closeElement("item");
        xml.closeElement("publish");

        return xml;
    }
}