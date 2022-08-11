package com.soapp.xmpp.GroupChatHelper;

import com.soapp.R;
import com.soapp.setup.Soapp;

import org.jivesoftware.smack.packet.IQ;

/**
 * Created by ryan on 08/01/2018.
 */

public class GroupVideoStanza extends IQ {
    public static final String ELEMENT = "pubsub";
    public static final String NAMESPACE = "http://jabber.org/protocol/pubsub";
    String nodeId;
    String userJid;
    String userName;
    String ID;
    String Body;
    String Direction;
    String resource_id;
    String RoomName;
    String video_thumb_base64;

    public GroupVideoStanza(IQ iq) {
        super(iq);
    }

    public GroupVideoStanza(String nodeId, String userJid, String userName, String Direction,
                            String ID, String resource_id, String RoomName, String video_thumb_base64) {
        super(ELEMENT, NAMESPACE);
        this.nodeId = nodeId;
        this.userJid = userJid;
        this.userName = userName;
        this.ID = ID;
        this.Body = Soapp.getInstance().getApplicationContext().getString(R.string.video_received);
        this.Direction = Direction;
        this.resource_id = resource_id;
        this.RoomName = RoomName;
        this.video_thumb_base64 = video_thumb_base64;
    }

    @Override
    protected IQ.IQChildElementXmlStringBuilder getIQChildElementBuilder(IQ.IQChildElementXmlStringBuilder xml) {
        xml.rightAngleBracket();
        xml.append("<publish node='").append(nodeId).append("'>").append("<item id='").append(ID).append("'>").append("<message").append(" type='chat'").append(" from='").append(userJid).append("'").append(" id='").append(userName).append("'").append(">").append("<body>").append(Body).append("</body>").append("<subject>").append(RoomName).append("</subject>").append("<thread>chat</thread>").append("<data xmlns='urn:xmpp:soapp' ChatID='VideoID'").append(" direction='").append(Direction).append("' ").append("resource_id='").append(resource_id).append("' ").append("thumbnail='").append(video_thumb_base64).append("' /></message></item></publish>");
        return xml;
    }
}
