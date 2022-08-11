package com.soapp.xmpp.GroupChatHelper;

import com.soapp.R;
import com.soapp.setup.Soapp;

import org.jivesoftware.smack.packet.IQ;

/**
 * Created by chang on 06/07/2017.
 */

public class GroupContactStanza extends IQ {
    public static final String ELEMENT = "pubsub";
    public static final String NAMESPACE = "http://jabber.org/protocol/pubsub";
    String nodeId;
    String userJid;
    String userName;
    String ID;
    String contactName;
    String contactPhone;
    String RoomName;

    public GroupContactStanza(String nodeId, String userJid, String userName, String contactName,
                              String contactPhone, String ID, String RoomName) {
        super(ELEMENT, NAMESPACE);
        this.nodeId = nodeId;
        this.userJid = userJid;
        this.userName = userName;
        this.ID = ID;
        this.contactName = contactName;
        this.contactPhone = contactPhone;
        this.RoomName = RoomName;
    }

    @Override
    protected IQChildElementXmlStringBuilder getIQChildElementBuilder(IQChildElementXmlStringBuilder xml) {
        xml.rightAngleBracket();
        xml.append("<publish node='").append(nodeId).append("'>").append("<item id='").append(ID).append("'>").append("<message").append(" type='chat'").append(" from='").append(userJid).append("'").append(" id='").append(userName).append("'").append(">").append("<body>").append(Soapp.getInstance().getApplicationContext().getString(R.string
                .contact_received)).append("</body>").append("<subject>").append(RoomName).append("</subject>").append("<thread>chat</thread>").append("<data xmlns='urn:xmpp:soapp' ChatID='ContactID'").append(" ContactName='").append(contactName).append("'").append(" ContactNumber='").append(contactPhone).append("'/></message></item></publish>");
        return xml;
    }
}
