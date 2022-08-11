package com.soapp.xmpp.GroupChatHelper;

import com.soapp.R;
import com.soapp.setup.Soapp;

import org.jivesoftware.smack.packet.IQ;

/* Created by chang on 14/07/2017. */

public class GroupAudioStanza extends IQ {
    public static final String ELEMENT = "pubsub";
    public static final String NAMESPACE = "http://jabber.org/protocol/pubsub";
    String nodeId;
    String userJid;
    String userName;
    String ID;
    String RoomName;
    String Body;
    String resource_id;
    String resource_url;

    public GroupAudioStanza(String nodeId, String userJid, String userName, String ID, String
            resource_id, String resource_url, String RoomName) {
        super(ELEMENT, NAMESPACE);
        this.nodeId = nodeId;
        this.userJid = userJid;
        this.userName = userName;
        this.ID = ID;
        this.Body = Soapp.getInstance().getApplicationContext().getString(R.string.audio_received);
        this.RoomName = RoomName;
        this.resource_id = resource_id;
        this.resource_url = resource_url;
    }

    @Override
    protected IQChildElementXmlStringBuilder getIQChildElementBuilder(IQChildElementXmlStringBuilder xml) {
        xml.rightAngleBracket();
        xml.append("<publish node='").append(nodeId).append("'>").append("<item id='").append(ID).append("'>")
                .append("<message").append(" type='").append("chat").append("'").append(" from='").append(userJid).append("'").append(" id='").append(userName).append("'>").append("<body>").append(Body).append("</body>").append("<subject>").append(RoomName).append("</subject>").append("<thread>chat</thread>").append("<data ").append("xmlns='urn:xmpp:soapp' ChatID='AudioID' resource_url='").append(resource_url).append("'").append(" resource_id='").append(resource_id).append("'/></message></item></publish>");
        return xml;
    }
}
