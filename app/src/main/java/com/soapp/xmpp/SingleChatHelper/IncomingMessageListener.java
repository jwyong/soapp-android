package com.soapp.xmpp.SingleChatHelper;

import android.content.Intent;
import android.util.Log;

import com.soapp.global.DecryptionHelper;
import com.soapp.global.EncryptionHelper;
import com.soapp.global.GlobalVariables;
import com.soapp.global.Preferences;
import com.soapp.global.StateCheck;
import com.soapp.setup.Soapp;
import com.soapp.soapp_tab.reward.reward_personal_info.RewardPersonalInfoActivity;
import com.soapp.sql.DatabaseHelper;
import com.soapp.xmpp.GlobalMessageHelper.GlobalHeaderHelper;
import com.soapp.xmpp.soapp_call.PeerClient;

import org.jivesoftware.smack.chat2.Chat;
import org.jivesoftware.smack.chat2.IncomingChatMessageListener;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.StandardExtensionElement;
import org.jivesoftware.smackx.delay.packet.DelayInformation;
import org.jxmpp.jid.EntityBareJid;
import org.webrtc.IceCandidate;
import org.webrtc.SessionDescription;

import java.util.UUID;

/* Created by chang on 04/07/2017. */

public class IncomingMessageListener implements IncomingChatMessageListener {
    //basics
    private DatabaseHelper databaseHelper = DatabaseHelper.getInstance();
    private Preferences preferences = Preferences.getInstance();
    private SingleChatStanza singleChatStanza = new SingleChatStanza();

    // encryption & decryption
    private EncryptionHelper encryptionHelper = new EncryptionHelper();
    private DecryptionHelper decryptionHelper = new DecryptionHelper();

    //for delayed playsound
    private boolean playSound;
    private long currentDate;

    private String uniqueID;
    private String jid;

    @Override
    public void newIncomingMessage(EntityBareJid from, Message message, Chat chat) {
        String From = String.valueOf(from);

        jid = From.substring(0, 12);

        //filter out same uniqueID first
        uniqueID = message.getStanzaId();
        if (databaseHelper.getUniqueIDExists(uniqueID)) {
            singleChatStanza.SoappAckStanza(jid, uniqueID);
            return;
        }

        final String Body = message.getBody();
        StandardExtensionElement extension;
        if (message.hasExtension("appointment")) {
            extension = (StandardExtensionElement) message.getExtension("urn:xmpp:appointment");
        } else {
            extension = (StandardExtensionElement) message.getExtension("urn:xmpp:soapp");
        }

        currentDate = System.currentTimeMillis();
        playSound = true;

        //check if delayed then don't play sound + use delayed date
//        checkSoundforDelay(message);
        if (message.hasExtension("data", "urn:xmpp:soapp")) {
            //chat
            String decryptbase64;

            switch (extension.getAttributeValue("ChatID")) {
                //indi incoming text
                case "Message":
                    try {
                        decryptbase64 = decryptionHelper.decryptText(Body);

                        databaseHelper.MessageInputDatabase(jid, decryptbase64, uniqueID, currentDate, playSound);

                        //remove typing... from chat log title

//                        current_room = preferences.getValue(Soapp.getInstance().getApplicationContext(), "current_room_id");
//                        if (current_room.equals(jid)) {
//                            final String indiChatStatus = Soapp.getInstance().getApplicationContext().getString(R.string.indi_chat_status);
//                            runOnUiThread(() -> {
//                                if (IndiChatLog.chat_info != null) {
//                                    IndiChatLog.chat_info.setText(indiChatStatus);
//                                }
//                            });
//                        }
                    } catch (Exception e) {
                        databaseHelper.saveLogsToDb("SMACK IN Message", e.toString(), System.currentTimeMillis());
                    }
                    break;

                //indi incoming map [DONE notify]
                case "MapID":
                    try {
                        final String Lat = extension.getAttributeValue("latitude");
                        final String Long = extension.getAttributeValue("longtitude");
                        final String mapURL = extension.getAttributeValue("staticurl");
                        final String placename = extension.getAttributeValue("infoid");
                        final String address = extension.getAttributeValue("address");

                        databaseHelper.MapInputDatabase(jid, mapURL, uniqueID, currentDate, Lat, Long,
                                playSound, placename, address);
                    } catch (Exception e) {
                        databaseHelper.saveLogsToDb("SMACK IN MapID", e.toString(), System.currentTimeMillis());
                    }
                    break;

                //indi incoming image [DONE notify]
                case "ImageID":
                    try {
                        String resourceID = extension.getAttributeValue("resource_id");
                        String rotation = extension.getAttributeValue("rotation");
                        final String thumbnail_ImageID = extension.getAttributeValue("thumbnail");

                        if (rotation == null) {
                            rotation = "0";
                        }

                        databaseHelper.ImageInputDatabase(jid, resourceID, uniqueID, currentDate, extension
                                .getAttributeValue("direction"), playSound, rotation, thumbnail_ImageID);
                    } catch (Exception e) {
                        databaseHelper.saveLogsToDb("SMACK IN ImageID", e.toString(), System.currentTimeMillis());
                    }
                    break;

                //indi incoming contact [DONE notify]
                case "ContactID":
                    try {
                        final String ContactName = extension.getAttributeValue("name");
                        final String ContactNumber = extension.getAttributeValue("phone");

                        databaseHelper.ContactInputDatabase(jid, uniqueID, currentDate, ContactName,
                                ContactNumber, playSound);
                    } catch (Exception e) {
                        databaseHelper.saveLogsToDb("SMACK IN ContactID", e.toString(), System.currentTimeMillis());
                    }
                    break;

                //indi incoming audio [DONE notify]
                case "AudioID":
                    try {
                        String resourceID2 = extension.getAttributeValue("resource_id");

                        databaseHelper.VoiceInputDatabase(jid, resourceID2, uniqueID, currentDate, playSound);
                    } catch (Exception e) {
                        databaseHelper.saveLogsToDb("SMACK IN AudioID", e.toString(), System.currentTimeMillis());
                    }
                    break;

                //indi incoming video
                case "VideoID":
                    try {
                        String resourceIDVideo = extension.getAttributeValue("resource_id");
                        final String thumbnail_VideoID = extension.getAttributeValue("thumbnail");

                        if (resourceIDVideo != null) {
                            databaseHelper.VideoInputDatabase(jid, resourceIDVideo, uniqueID, currentDate, extension
                                    .getAttributeValue("direction"), playSound, thumbnail_VideoID);
                        }
                    } catch (Exception e) {
                        databaseHelper.saveLogsToDb("SMACK IN VideoID", e.toString(), System.currentTimeMillis());
                    }
                    break;

                //indi incoming restaurant [DONE notify]
                case "RestaurantID":
                    try {
                        String infoID = extension.getAttributeValue("InfoId");
                        databaseHelper.RestaurantInputDatabase(jid, infoID, uniqueID, extension
                                .getAttributeValue("RestaurantTitle"), currentDate, playSound);
                    } catch (Exception e) {
                        databaseHelper.saveLogsToDb("SMACK IN RestaurantID", e.toString(), System.currentTimeMillis());
                    }
                    break;

                //incoming booking confirmed (from biz owner)
                case "BookingID":
                    String bookingid;
                    try {
                        bookingid = extension.getAttributeValue("booking_id");
                        String resID = extension.getAttributeValue("resid");
                        String qrCode = extension.getAttributeValue("qrcode");

                        //qrcode: if qrcode = "declined" means biz owner declined booking
                        databaseHelper.incomingResBooking(bookingid, resID, jid, qrCode, uniqueID);
                    } catch (Exception e) {
                        databaseHelper.saveLogsToDb("SMACK IN BookingID", e.toString(), System.currentTimeMillis());
                    }
                    break;

                //incoming booking details shared by friends
                case "ShareBookingID":
                    try {
                        bookingid = extension.getAttributeValue("booking_id");
                        String bookingdate = extension.getAttributeValue("bookingdate");
                        String bookingtime = extension.getAttributeValue("bookingtime");
                        String resid = extension.getAttributeValue("resid");
                        String restitle = extension.getAttributeValue("restitle");
                        String pax = extension.getAttributeValue("pax");
                        String promo = extension.getAttributeValue("promo");
                        String sharedQRCode = extension.getAttributeValue("qrcode");

                        databaseHelper.incomingResBookingShare(bookingid, jid, sharedQRCode, uniqueID, bookingdate, bookingtime,
                                resid, restitle, pax, promo, playSound, currentDate, null);
                    } catch (Exception e) {
                        databaseHelper.saveLogsToDb("SMACK IN ShareBookingID", e.toString(), System.currentTimeMillis());
                    }
                    break;

                //incoming booking details shared by friends
                case "DeleteShareBookingID":
                    try {
                        String deleteResID = extension.getAttributeValue("booking_id");

                        databaseHelper.incomingResBookingCancelled(jid, deleteResID, uniqueID, null, playSound);
                    } catch (Exception e) {
                        databaseHelper.saveLogsToDb("SMACK IN DeleteShareBookingID", e.toString(), System.currentTimeMillis());
                    }
                    break;

                //From here onwards no need sound at all
                //indi incoming new user (friend who just registered on Soapp)
                case "NewID":
                    try {
                        String user_jid = preferences.getValue(Soapp.getInstance().getApplicationContext(), GlobalVariables.STRPREF_USER_ID);
                        if (!jid.equals(user_jid)) { //unwrap ownself pubsub
                            databaseHelper.incomingNewSoappUser(jid, uniqueID);
                        } else {
                            singleChatStanza.SoappAckStanza(jid, uniqueID);
                        }
                    } catch (Exception e) {
                        databaseHelper.saveLogsToDb("SMACK IN NewID", e.toString(), System.currentTimeMillis());
                    }
                    break;

                //indi incoming green tick (other has seen msg)
                case "SeenID":
                    try {
                        databaseHelper.SeenInputDatabase(jid, Body, uniqueID);
                    } catch (Exception e) {
                        databaseHelper.saveLogsToDb("SMACK IN SeenID", e.toString(), System.currentTimeMillis());
                    }
                    break;

                //self is deleted from group
                case "DeleteGroupID":
                    try {
                        databaseHelper.selfRemovedFromGrp(jid, Body, uniqueID, playSound);
                    } catch (Exception e) {
                        databaseHelper.saveLogsToDb("SMACK IN DeleteGroupID", e.toString(), System.currentTimeMillis());
                    }
                    break;

                case "ReplyID":
                    try {
                        decryptbase64 = decryptionHelper.decryptText(Body);

                        databaseHelper.replyInputDatabase(jid, decryptbase64, uniqueID, currentDate, playSound, extension.getAttributeValue("replyid"), extension.getAttributeValue("replytype"));
                    } catch (Exception e) {
                        databaseHelper.saveLogsToDb("SMACK IN ReplyID", e.toString(), System.currentTimeMillis());
                    }
                    break;

                case "InviteID":
                    preferences.save(Soapp.getInstance().getApplicationContext(), GlobalVariables.QUEST_LVL, "100-QT1,1,1");
                    new RewardPersonalInfoActivity().inviteListenser();
                    break;

                case "PointID":
                    String redemption_id = extension.getAttributeValue("redemption_id");
                    databaseHelper.deleteRDB1Col(DatabaseHelper.REWARD_TABLE_NAME, DatabaseHelper.REWARD_REDEMPTION_ID, redemption_id);
                    break;

                case "VoiceCall":
                    VoiceCallFunction(extension);

                    break;

                default:
                    break;
            }
        } else if (message.hasExtension("appointment", "urn:xmpp:appointment")) { //appt
            try {
                extension = (StandardExtensionElement) message.getExtension("urn:xmpp:appointment");
                String apptID2 = extension.getAttributeValue("apptID");
                String apptTitle2 = extension.getAttributeValue("title");
                String apptMapURL2 = extension.getAttributeValue("map");
                String apptLoc2 = extension.getAttributeValue("location");
                String apptLon2 = extension.getAttributeValue("longitude");
                String apptLat2 = extension.getAttributeValue("latitude");
                String apptDate2 = extension.getAttributeValue("date");
                String apptTime2 = extension.getAttributeValue("time");
                String apptStatusStr = extension.getAttributeValue("status");
                String apptResID = extension.getAttributeValue("infoid");
                String apptResImgURL = extension.getAttributeValue("infoimgurl");
                String type = extension.getAttributeValue("type");
                String isCreating = extension.getAttributeValue("new");

                if (type == null || type.equals("")) { //safety unwrap for null type
                    type = "99";
                }

                if (isCreating.equals("1")) { //new appt
                    type = "5";

                    databaseHelper.incomingApptDetails(jid, uniqueID, apptTitle2, apptMapURL2, apptLoc2,
                            apptLon2, apptLat2, apptDate2, apptTime2, apptStatusStr, apptResID,
                            apptResImgURL, playSound, jid, apptID2, type);

//                    databaseHelper.scheduleLocalNotification(jid, apptID2);
                } else {
                    //update appointment details to DBs ONLY if appt exists
                    if (databaseHelper.getApptExist(apptID2) != 0) {
                        databaseHelper.incomingApptDetails(jid, uniqueID, apptTitle2, apptMapURL2, apptLoc2,
                                apptLon2, apptLat2, apptDate2, apptTime2, apptStatusStr, apptResID,
                                apptResImgURL, playSound, jid, apptID2, type);

//                databaseHelper.scheduleLocalNotification(jid, apptID2);} else {
                        singleChatStanza.SoappAckStanza(jid, uniqueID);
                    }
                }
            } catch (Exception e) {
                databaseHelper.saveLogsToDb("SMACK IN appointment", e.toString(), System.currentTimeMillis());
            }
        } else if (message.hasExtension("read")) {
            singleChatStanza.SoappAckStanza(jid, uniqueID);
        } else if (message.hasExtension("business_booking")) {
            singleChatStanza.SoappAckStanza(jid, uniqueID);
        } else { //acknowledge whatever leftover stanzas
            singleChatStanza.SoappAckStanza(jid, uniqueID);
        }

        new GlobalHeaderHelper().GlobalHeaderTime(jid);
    }

    private void checkSoundforDelay(Message message) {
        if (message.hasExtension("delay", "urn:xmpp:delay")) {
            //since delayed, don't play sound and use delayed date
            DelayInformation delayInformation = (DelayInformation) message.getExtensions().get(1);
            currentDate = delayInformation.getStamp().getTime();
            playSound = false;

        } else { //not delay - check for foreground AND preferences
            if (!StateCheck.foreground || preferences.getValue(Soapp.getInstance().getApplicationContext(),
                    "IndiInMsg").equals("off")) {
                playSound = false;
            }
        }
    }


    private void VoiceCallFunction(StandardExtensionElement el) {
        if (el.getAttributeValue("type") != null) {
            switch (el.getAttributeValue("type")) {
                case "incoming":
                    String Type = el.getAttributeValue("SdpType");
                    String desc = el.getText();
                    singleChatStanza.SoappAckStanza(jid, uniqueID);

                    if (Type.equals(SessionDescription.Type.PRANSWER.canonicalForm())) {
                        PeerClient.setAnswerforCaller(new SessionDescription(
                                SessionDescription.Type.fromCanonicalForm(Type), desc));

                    } else if (PeerClient.callActive) {
                        Log.d("wtf", "VoiceCallFunction: ");
                        singleChatStanza.SoappRejectCallStanza(jid, "User Rejected", UUID.randomUUID().toString());

                    } else {
                        Log.d("wtf", "VoiceCallFunction: 2");

                        if (Type.equals(SessionDescription.Type.ANSWER.canonicalForm())) {
                            PeerClient.setAnswerforCaller(new SessionDescription(
                                    SessionDescription.Type.fromCanonicalForm(Type), desc));
                            if (PeerClient.iceCandidateList != null && PeerClient.iceCandidateList.size() > 0) {
                                for (IceCandidate iceCandidate : PeerClient.iceCandidateList) {
                                    new SingleChatStanza().SoappIceCandidateStanza(jid, iceCandidate, UUID.randomUUID().toString());
                                }
                            }
                        } else {

                            Log.d("wtf", "newIncomingMessage: " + Type + "\n" + desc);

                            Intent callpeer = new Intent(Soapp.getInstance(), PeerClient.class);
                            callpeer.putExtra("jid", jid);
                            callpeer.putExtra("isCaller", false);
                            callpeer.putExtra("sdp", new String[]{Type, desc});
                            callpeer.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

                            Soapp.getInstance().startActivity(callpeer);

                        }
                    }
                    break;

                case "candidate":
                    singleChatStanza.SoappAckStanza(jid, uniqueID);
                    Log.d("wtf", "VoiceCallFunction: candidate");
                    if (PeerClient.addIceCandidate(
                            new IceCandidate(el.getAttributeValue("id")
                                    , Integer.parseInt(el.getAttributeValue("label"))
                                    , el.getAttributeValue("candidate")))) {
                    }
                    break;

                case "reject":
                    Log.d("wtf", "VoiceCallFunction: reject");
                    singleChatStanza.SoappAckStanza(jid, uniqueID);
//                Handler handler = new Handler(Looper.getMainLooper());
//                handler.post(() -> new PeerClient().callRejected());
                    PeerClient.callRejected();
                    break;
            }
        } else {
            singleChatStanza.SoappAckStanza(jid, uniqueID);
        }
    }
}