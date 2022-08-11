package com.soapp.xmpp.GroupChatHelper;

import org.jivesoftware.smack.packet.IQ;

/* Created by chang on 06/07/2017. */

public class GroupAppointmentStanza extends IQ {
    public static final String ELEMENT = "pubsub";
    public static final String NAMESPACE = "http://jabber.org/protocol/pubsub";
    String nodeId;
    String userJid;
    String uniqueID;
    String apptTitle;
    String apptMapURL;
    String apptDet;
    String apptLoc;
    String apptAdd;
    String apptLat;
    String apptLon;
    String apptDate;
    String apptTime;
    String apptStatus;
    String apptInfoID;
    String apptInfoImgURL;
    String apptID;
    String roomName;
    String pushMsg;
    String apptDateTime;
    String apptUpdateType;

    //for determining where push goes to: "chat" and "appointment"
    String pushThread;

    public GroupAppointmentStanza(String nodeId, String userJid, String uniqueID, String apptTitle,
                                  String apptMapURL, String apptLoc, String apptDet, String apptAdd,
                                  String apptLat, String apptLon, String apptDate, String apptTime,
                                  String apptStatus, String apptInfoID, String apptInfoImgURL,
                                  String apptID, String roomName, String pushMsg, String pushThread,
                                  String apptDateTime, String apptUpdateType) {
        super(ELEMENT, NAMESPACE);

        //basic room details
        this.nodeId = nodeId;
        this.userJid = userJid;
        this.uniqueID = uniqueID;
        this.roomName = roomName;
        this.pushMsg = pushMsg;
        this.pushThread = pushThread;

        //appt details
        this.apptTitle = apptTitle;
        this.apptMapURL = apptMapURL;
        this.apptLoc = apptLoc;
        this.apptDet = apptDet;
        this.apptAdd = apptAdd;
        this.apptLat = apptLat;
        this.apptLon = apptLon;
        this.apptDate = apptDate;
        this.apptTime = apptTime;
        this.apptStatus = apptStatus;
        this.apptInfoID = apptInfoID;
        this.apptInfoImgURL = apptInfoImgURL;
        this.apptID = apptID;
        this.apptDateTime = apptDateTime;
        this.apptUpdateType = apptUpdateType;
    }

    @Override
    protected IQChildElementXmlStringBuilder getIQChildElementBuilder(IQChildElementXmlStringBuilder xml) {
        xml.rightAngleBracket();
        xml.append("<publish node='").append(nodeId).append("'>").append("<item id='").append(uniqueID)
                .append("'>").append("<message from='").append(userJid).append("'").append(" type='chat'")
                .append(" id='appointid'>").append("<body>").append(pushMsg).append("</body>")
                .append("<subject>").append(roomName).append("</subject>").append("<thread>")
                .append(pushThread).append(":").append(apptID).append("</thread>")
                .append("<appointment xmlns='urn:xmpp:appointment'").append(" new='0'")
                .append(" timestamp='").append(apptDateTime).append("'").append(" apptID='")
                .append(apptID).append("'").append(" title='").append(apptTitle).append("'")
                .append(" detail='").append(apptDet).append("'").append(" map='").append(apptMapURL)
                .append("'").append(" address='").append(apptAdd).append("'").append(" location='")
                .append(apptLoc).append("'").append(" longitude='").append(apptLon).append("'")
                .append(" date='").append(apptDate).append("'").append(" latitude='").append(apptLat)
                .append("'").append(" time='").append(apptTime).append("'").append(" status='")
                .append(apptStatus).append("'").append(" infoid='").append(apptInfoID).append("'")
                .append(" infoimgurl='").append(apptInfoImgURL).append("'").append(" type='")
                .append(apptUpdateType).append("'").append("/></message></item></publish>");
        return xml;
    }

}
