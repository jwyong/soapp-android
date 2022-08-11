package com.soapp.xmpp.GroupChatHelper;

import com.soapp.R;
import com.soapp.setup.Soapp;

import org.jivesoftware.smack.packet.IQ;

/* Created by chang on 06/07/2017. */

public class GroupImageStanza extends IQ {

    public static final String ELEMENT = "pubsub";
    public static final String NAMESPACE = "http://jabber.org/protocol/pubsub";
    String nodeId;
    String userJid;
    String userName;
    String ID;
    String Body;
    String Direction;
    String rotation;
    String resource_id;
    String resource_url;
    String RoomName;
    String image_base64;

    public GroupImageStanza(IQ iq) {
        super(iq);
    }

    public GroupImageStanza(String nodeId, String userJid, String userName, String Direction,
                            String ID, String resource_id, String resource_url, String RoomName, String rotation, String image_base64) {
        super(ELEMENT, NAMESPACE);
        this.nodeId = nodeId;
        this.userJid = userJid;
        this.userName = userName;
        this.ID = ID;
        this.Body = Soapp.getInstance().getApplicationContext().getString(R.string.image_received);
        this.Direction = Direction;
        this.resource_id = resource_id;
        this.resource_url = resource_url;
        this.RoomName = RoomName;
        this.rotation = rotation;
        this.image_base64 = image_base64;
    }

    @Override
    protected IQChildElementXmlStringBuilder getIQChildElementBuilder(IQChildElementXmlStringBuilder xml) {
        xml.rightAngleBracket();
        xml.append("<publish node='").append(nodeId).append("'>").append("<item id='").append(ID).append("'>").append("<message").append(" type='chat'").append(" from='").append(userJid).append("'").append(" id='").append(userName).append("'").append(">").append("<body>").append(Body).append("</body>").append("<subject>").append(RoomName).append("</subject>").append("<thread>chat</thread>").append("<data xmlns='urn:xmpp:soapp' ChatID='ImageID'").append(" direction='").append(Direction).append("' resource_url='").append(resource_url).append("'").append(" ").append("resource_id='").append(resource_id).append("' rotation='").append(rotation).append("' thumbnail='").append(image_base64).append("' /></message></item></publish>");
        return xml;
    }
}
