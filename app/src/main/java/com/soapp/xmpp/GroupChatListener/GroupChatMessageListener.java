package com.soapp.xmpp.GroupChatListener;

import android.util.Log;

import com.soapp.R;
import com.soapp.global.GlobalVariables;
import com.soapp.global.Preferences;
import com.soapp.global.StateCheck;
import com.soapp.setup.Soapp;
import com.soapp.sql.DatabaseHelper;
import com.soapp.xmpp.GlobalMessageHelper.GlobalHeaderHelper;
import com.soapp.xmpp.GroupChatHelper.GroupChatStanza;

import org.jivesoftware.smack.filter.StanzaFilter;
import org.jivesoftware.smack.packet.ExtensionElement;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.StandardExtensionElement;
import org.jivesoftware.smack.packet.Stanza;
import org.jivesoftware.smack.util.PacketParserUtils;
import org.jivesoftware.smackx.delay.packet.DelayInformation;
import org.jivesoftware.smackx.pubsub.EventElement;
import org.jivesoftware.smackx.pubsub.Item;
import org.jivesoftware.smackx.pubsub.ItemsExtension;
import org.jivesoftware.smackx.pubsub.PayloadItem;
import org.jivesoftware.smackx.pubsub.SimplePayload;

import java.util.List;

/* Created by chang on 10/07/2017. */

public class GroupChatMessageListener implements StanzaFilter {

    private Preferences preferences = Preferences.getInstance();
    private DatabaseHelper databaseHelper = DatabaseHelper.getInstance();
    private GlobalHeaderHelper globalHeaderHelper = new GlobalHeaderHelper();
    private GroupChatStanza groupChatStanza = new GroupChatStanza();

    // encryption & decryption
//    private EncryptionHelper encryptionHelper = new EncryptionHelper();
//    private DecryptionHelper decryptionHelper = new DecryptionHelper();

    //for delayed playsound
    private long currentDate;
    private static final String TAG = "wtf";
    private boolean playSound;

    @Override
    public boolean accept(Stanza stanza) {
        try {
            final String selfJid = preferences.getValue(Soapp.getInstance().getApplicationContext(), GlobalVariables.STRPREF_USER_ID);

            if (stanza.hasExtension("event", "http://jabber.org/protocol/pubsub#event")) {

                EventElement eventElement = stanza.getExtension("event", "http://jabber.org/protocol/pubsub#event");

                if (eventElement != null) {
                    final String grpJid = eventElement.getEvent().getNode();
                    List<ExtensionElement> itemExtensions = eventElement.getExtensions();

                    for (int i = 0; i < itemExtensions.size(); i++) {
                        ItemsExtension itemsExtension = (ItemsExtension) itemExtensions.get(i);
                        List<Item> items = (List<Item>) itemsExtension.getItems();

                        if (items != null) {
                            for (int j = 0; j < items.size(); j++) {
                                PayloadItem it = (PayloadItem) items.get(j);

                                //filter out same unique id
                                final String uniqueID = it.getId();

                                SimplePayload payloadElement = (SimplePayload) it.getPayload();

                                String element = payloadElement.toXML("http://jabber.org/protocol/pubsub#event");

                                try {

                                    Message message = PacketParserUtils.parseStanza(element);
                                    String otherJid = message.getFrom().toString();

                                    //filter out RECV errors, self jid and duplicate uniqueID
                                    if (otherJid.equals(selfJid) || databaseHelper.getUniqueIDExists(uniqueID)) {
                                        groupChatStanza.GroupAckStanza(grpJid, selfJid, otherJid, uniqueID);
                                        return false;
                                    }

                                    //set currentdate and playsound first
                                    currentDate = System.currentTimeMillis();
                                    playSound = true;

                                    //check if delayed then don't play sound + use delayed date
                                    checkSoundforDelay(stanza);

                                    if (message.hasExtension("data", "urn:xmpp:soapp")) {

                                        StandardExtensionElement extension = (StandardExtensionElement) message.getExtension("urn:xmpp:soapp");
                                        StandardExtensionElement bodyExtension = message.getExtension("body", "http://jabber.org/protocol/pubsub#event");
//                                        Log.d(TAG, String.format("accept: %s\n %s \n%s\n %s", extension.getElementName(), extension.getNamespace(), extension.getAttributes(), extension.getText()));
                                        final String Body = bodyExtension.getText();

//                                        String Gdecryptbase64 = decryptionHelper.decryptText(Body);
                                        String decryptedText, escapedText, current_room;
                                        switch (extension.getAttributeValue("ChatID")) {
                                            case "Message": //[DONE notify + displayName]
                                                // output decryption Txt
                                                decryptedText = Body;
                                                //convert &amp; and &lt; back to "&" and "<"
                                                escapedText = decryptedText.replace("&amp;", "&").replace("&lt;", "<");
                                                databaseHelper.GroupMessageInputDatabase(grpJid, escapedText, uniqueID, currentDate, otherJid, playSound);
                                                break;

                                            case "MapID": //[DONE notify + displayName]
                                                String lat = extension.getAttributeValue("latitude");
                                                String lon = extension.getAttributeValue("longtitude");
                                                String mapURL = extension.getAttributeValue("staticurl");
                                                String placename = extension.getAttributeValue("infoid");
                                                String address = extension.getAttributeValue("address");

                                                databaseHelper.GroupMapInputDatabase(grpJid, mapURL,
                                                        uniqueID, currentDate, otherJid, lat, lon, playSound, placename, address);
                                                break;

                                            case "ImageID": //[DONE notify + displayName]
                                                String orient = extension.getAttributeValue("direction");
                                                String resourceID = extension.getAttributeValue("resource_id");
                                                String rotation = extension.getAttributeValue("rotation");
                                                String thumbnail = extension.getAttributeValue("thumbnail");

                                                if (rotation == null) {
                                                    rotation = "0";
                                                }
                                                if (resourceID != null) {
                                                    databaseHelper.GroupImageInputDatabase(grpJid, resourceID, uniqueID, currentDate,
                                                            otherJid, orient, playSound, rotation, thumbnail);
                                                } else {
                                                    databaseHelper.GroupImageInputDatabase(grpJid, Body, uniqueID, currentDate,
                                                            otherJid, orient, playSound, rotation, thumbnail);
                                                }
                                                break;

                                            case "ContactID": //[DONE notify + displayName]
                                                String contactName = extension.getAttributeValue
                                                        ("ContactName");
                                                String contactNo = extension.getAttributeValue
                                                        ("ContactNumber");

                                                databaseHelper.GroupContactInputDatabase(grpJid,
                                                        uniqueID, currentDate, otherJid, contactName, contactNo, playSound);
                                                break;

                                            case "AudioID": //[DONE notify + displayName]
                                                String resourceID2 = extension.getAttributeValue
                                                        ("resource_id");
                                                if (resourceID2 != null) {
                                                    databaseHelper.Groupvoiceinput(grpJid, resourceID2, uniqueID,
                                                            currentDate, otherJid, playSound);
                                                } else {
                                                    databaseHelper.Groupvoiceinput(grpJid, Body, uniqueID,
                                                            currentDate, otherJid, playSound);
                                                }
                                                break;

                                            case "VideoID":
                                                //YET TO MAKE
                                                String orientVideo = extension.getAttributeValue("direction");
                                                String resourceIDVideo = extension.getAttributeValue("resource_id");
                                                String video_thumb_base64 = extension.getAttributeValue("thumbnail");
                                                if (resourceIDVideo != null) {
                                                    databaseHelper.GroupVideoInputDatabase(grpJid, resourceIDVideo, uniqueID, currentDate,
                                                            otherJid, orientVideo, playSound, video_thumb_base64);
                                                } else {
                                                    databaseHelper.GroupVideoInputDatabase(grpJid, Body, uniqueID, currentDate,
                                                            otherJid, orientVideo, playSound, video_thumb_base64);
                                                }
                                                break;

                                            case "RestaurantID": //[DONE notify + displayName]
                                                String resTitle = extension.getAttributeValue("RestaurantTitle");
                                                String infoIDRes = extension.getAttributeValue("InfoId");

                                                databaseHelper.RestaurantGroupInputDatabase(grpJid,
                                                        infoIDRes, uniqueID, resTitle, currentDate,
                                                        otherJid, playSound);
                                                break;

                                            case "ShareBookingID":
                                                String bookingid = extension.getAttributeValue("bookingid");
                                                String bookingdate = extension.getAttributeValue("bookingdate");
                                                String bookingtime = extension.getAttributeValue("bookingtime");
                                                String resid = extension.getAttributeValue("resid");
                                                String restitle = extension.getAttributeValue("restitle");
                                                String pax = extension.getAttributeValue("pax");
                                                String promo = extension.getAttributeValue("promo");
                                                String sharedQRCode = extension.getAttributeValue("qrcode");


                                                databaseHelper.incomingResBookingShare(bookingid, grpJid,
                                                        sharedQRCode, uniqueID, bookingdate, bookingtime,
                                                        resid, restitle, pax, promo, playSound,
                                                        currentDate, otherJid);
                                                break;

                                            //incoming booking details shared by friends
                                            case "DeleteShareBookingID":
                                                String deleteResID = extension.getAttributeValue("bookingid");

                                                databaseHelper.incomingResBookingCancelled(grpJid, deleteResID, uniqueID, otherJid, playSound);
                                                break;

                                            //when other ppl create a NEW group with me in it - will check for appt details thru API
                                            case "NewID":
                                                databaseHelper.incomingCreateNewGrpChat(grpJid, currentDate, otherJid, uniqueID, playSound, false);
                                                break;

                                            //new Incoming existing group appointment
                                            case "NewAppointmentID":
                                                //yet to be done
                                                String apptID = extension.getAttributeValue("apptID");
                                                databaseHelper.getNewGrpApptDetailsFromApi(grpJid, otherJid, uniqueID, playSound, apptID);
                                                break;

                                            //no sound from this point
                                            case "nID":
                                                //yet to be done
                                                break;

                                            case "ReplyID":
                                                decryptedText = Body;

                                                //convert &amp; and &lt; back to "&" and "<"
                                                escapedText = decryptedText.replace("&amp;", "&").replace("&lt;", "<");
                                                String replyid = extension.getAttributeValue("replyid");

                                                databaseHelper.GroupReplyInputDatabase(grpJid, escapedText, uniqueID, currentDate, otherJid, playSound, replyid, extension.getAttributeValue("replytype"));
                                                break;

                                            //old stanza used for grp status updates - just acknowledge
                                            case "AppointmentPushID":
                                                groupChatStanza.GroupAckStanza(grpJid, selfJid, otherJid, uniqueID);
                                                break;
                                            default:
                                                break;
                                        }
                                        globalHeaderHelper.GlobalHeaderTime(grpJid);

                                    } else if (message.hasExtension("track", "urn:xmpp:tracking")) { //tracking
                                        String from = otherJid.substring(0, 12);
                                        String Id = message.getStanzaId();

                                        StandardExtensionElement extension = (StandardExtensionElement) message.getExtension("urn:xmpp:tracking");
                                        String Longtitude = extension.getAttributeValue("longtitude");
                                        String Latitude = extension.getAttributeValue("latitude");
                                        databaseHelper.GroupTrackingUpdate(grpJid, from, Latitude, Longtitude, Id);

                                    } else if (message.hasExtension("appointment", "urn:xmpp:appointment")) { //appt
                                        StandardExtensionElement extension = (StandardExtensionElement) message.getExtension("urn:xmpp:appointment");

                                        //jid of member who sent the appt details update (can be used for notification badge etc in future)
                                        String apptID = extension.getAttributeValue("apptID");
                                        String title = extension.getAttributeValue("title");
                                        String mapURL = extension.getAttributeValue("map");
                                        String locationName = extension.getAttributeValue("location");
                                        String latitude = extension.getAttributeValue("latitude");
                                        String longitude = extension.getAttributeValue("longitude");
                                        String date = extension.getAttributeValue("date");
                                        String time = extension.getAttributeValue("time");
                                        String apptStatus = extension.getAttributeValue("status");
                                        String resID = extension.getAttributeValue("resID");
                                        String resImgURL = extension.getAttributeValue("resImgUrl");
                                        String type = extension.getAttributeValue("type");

                                        if (type == null || type.equals("")) { //safety unwrap for null type
                                            type = "99";
                                        }

                                        //update appointment details to DBs if appt exists
                                        if (databaseHelper.getApptExist(apptID) != 0) {
                                            databaseHelper.incomingApptDetails(grpJid, uniqueID, title, mapURL, locationName,
                                                    longitude, latitude, date, time, apptStatus, resID, resImgURL, playSound,
                                                    otherJid, apptID, type);

                                            //set local notifications
//                                    databaseHelper.scheduleLocalNotification(grpJid, apptID);

                                            globalHeaderHelper.GlobalHeaderTime(grpJid);
                                        } else { //appt already expired - just ack
                                            groupChatStanza.GroupAckStanza(grpJid, selfJid, otherJid, uniqueID);
                                        }

                                        //TO BE DELETED SOON - after API done
                                    } else if (message.hasExtension("pubsubmember", "urn:xmpp:pubsubmember")) {
                                        //get group status from pubsub (adding admin, delete, etc)
                                        StandardExtensionElement extension = (StandardExtensionElement) message.getExtension("urn:xmpp:pubsubmember");

                                        //incoming group profile updated
                                        if (extension.getAttributeValue("publishGroupInfo").length() != 0 && extension.getAttributeValue("publishGroupInfo") != null) {
                                            databaseHelper.grpProfileUpdated(grpJid, otherJid, uniqueID, playSound);
                                        } else {

                                            //incoming new member added to existing group
                                            if (extension.getAttributeValue("addingGroup").length() != 0 && extension.getAttributeValue("addingGroup") != null) {
                                                String addedJid = extension.getAttributeValue("addingGroup");
                                                for (String everyjid : addedJid.split(",")) {
                                                    if (!selfJid.equals(everyjid)) { //not self added
                                                        databaseHelper.groupAddReceived(grpJid, everyjid, otherJid, uniqueID, playSound);

                                                    } else { //self added to new grp
                                                        databaseHelper.incomingCreateNewGrpChat(grpJid, currentDate, otherJid, uniqueID, playSound,
                                                                true);
                                                    }
                                                }

                                            } else {

                                                //[DONE] incoming other member deleted from grp
                                                if (extension.getAttributeValue("deleteGroup").length() != 0 && extension.getAttributeValue("deleteGroup") != null) {
                                                    String deleteGroup = extension.getAttributeValue("deleteGroup");
                                                    if (!selfJid.equals(deleteGroup)) { //unwrap for self
                                                        databaseHelper.grpOtherMemberRemoved(grpJid, deleteGroup, otherJid, uniqueID, playSound);
                                                    }
                                                } else {

                                                    //[DONE] other members or self assigned as admin
                                                    if (extension.getAttributeValue("addingAdmin").length() != 0) {
                                                        databaseHelper.groupChangeAdminReceived(grpJid, extension.getAttributeValue("addingAdmin"), otherJid, uniqueID, playSound);
                                                    }
                                                }
                                            }
                                        }

                                    } else if (message.hasExtension("single", "urn:xmpp:pubsubsingle")) {
                                        //incoming indi user profile changed (profile name and image)
                                        databaseHelper.incomingUserProfileUpdate(grpJid, otherJid, uniqueID);

                                    } else if (message.hasExtension("composing", "http://jabber.org/protocol/chatstates")) {
                                        databaseHelper.chatStatusTyping(grpJid, otherJid);

                                    } else if (message.hasExtension("paused", "http://jabber.org/protocol/chatstates")) {
                                        databaseHelper.chatStatusStoppedTyping(grpJid);

                                    } else if (message.hasExtension("active", "http://jabber.org/protocol/chatstates")) {
                                        databaseHelper.chatStatusStoppedTyping(grpJid);

                                    } else { //acknowledge leftover
                                        groupChatStanza.GroupAckStanza(grpJid, selfJid, otherJid, uniqueID);
                                    }
                                } catch (Exception e) {
                                    databaseHelper.saveLogsToDb("SMACK GRP IN", e.toString(), System.currentTimeMillis());
                                }
                            }
                        }
                    }
                }
            } else { //for indi typing and others (must check for hasextension first else crash
                try {
                    if (stanza.hasExtension("composing", "http://jabber.org/protocol/chatstates")) {
                        Message message = (Message) stanza;
                        String From = String.valueOf(message.getFrom());
                        String Jid = From.substring(0, 12);

                        //filter out received error and self's jid
                        if (!String.valueOf(message.getType()).equals("error") && !Jid.equals(selfJid)) {
                            databaseHelper.chatStatusTyping(Jid, null);
                        }

                    } else if (stanza.hasExtension("paused", "http://jabber.org/protocol/chatstates")) {
                        Message message = (Message) stanza;
                        String From = String.valueOf(message.getFrom());
                        String Jid = From.substring(0, 12);

                        if (!String.valueOf(message.getType()).equals("error") && !Jid.equals(selfJid)) {
                            databaseHelper.chatStatusStoppedTyping(Jid);
                        }

                    } else if (stanza.hasExtension("active", "http://jabber.org/protocol/chatstates")) {
                        Message message = (Message) stanza;
                        String From = String.valueOf(message.getFrom());
                        String Jid = From.substring(0, 12);

                        if (!String.valueOf(message.getType()).equals("error") && !Jid.equals(selfJid)) {
                            databaseHelper.chatStatusStoppedTyping(Jid);
                        }

                    } else if (stanza.hasExtension("track", "urn:xmpp:tracking")) {
                        Message message = (Message) stanza;
                        String From = String.valueOf(message.getFrom());
                        String Jid = From.substring(0, 12);
                        String Id = message.getStanzaId();

                        if (!String.valueOf(message.getType()).equals("error") && !Jid.equals(selfJid)) {
                            StandardExtensionElement extension = (StandardExtensionElement) stanza.getExtension("urn:xmpp:tracking");
                            String Longtitude = extension.getAttributeValue("longtitude");
                            String Latitude = extension.getAttributeValue("latitude");
                            databaseHelper.TrackingUpdate(Jid, Latitude, Longtitude, Id);
                        }
                    } else if (stanza.hasExtension("chat_ack", "urn:xmpp:chat_ack")) {
                        Log.wtf("wtf", "newIncomingMessage: testack");

                    } else {
                    }
                } catch (Exception e) {
                    databaseHelper.saveLogsToDb("SMACK GRP IN", e.toString(), System.currentTimeMillis());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            Log.e("wtf", "accept: ", e);
        }
        return false;

    }

    private void checkSoundforDelay(Stanza stanza) {
        try {
            if (stanza.hasExtension("delay", "urn:xmpp:delay")) { //delayed msg
                DelayInformation delayInformation = (DelayInformation) stanza.getExtensions().get(1);
                currentDate = delayInformation.getStamp().getTime();

                playSound = false;
            } else { //not delayed msg
                //check if grp sound settings turned off or in background
                if (!StateCheck.foreground || preferences.getValue(Soapp.getInstance().getApplicationContext(),
                        "GrpInMsg").equals("off")) {
                    playSound = false;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            databaseHelper.saveLogsToDb("SMACK GRP IN checkSoundforDelay", e.toString(), System.currentTimeMillis());
        }
    }
}
