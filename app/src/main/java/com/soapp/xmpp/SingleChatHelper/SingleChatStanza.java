package com.soapp.xmpp.SingleChatHelper;

import com.soapp.R;
import com.soapp.global.GlobalVariables;
import com.soapp.global.Preferences;
import com.soapp.setup.Soapp;
import com.soapp.sql.DatabaseHelper;
import com.soapp.xmpp.SmackHelper;

import org.jivesoftware.smack.chat2.Chat;
import org.jivesoftware.smack.chat2.ChatManager;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.StandardExtensionElement;
import org.jxmpp.jid.EntityBareJid;
import org.jxmpp.jid.impl.JidCreate;
import org.webrtc.IceCandidate;
import org.webrtc.SessionDescription;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;

/* Created by chang on 03/07/2017. */
public class SingleChatStanza {
    private StandardExtensionElement standardExtensionElement;
    // setThread: chat = "chat", appt = "appointment", restaurant = "restaurant"

    public void SoappRejectCallStanza(String To, String body, String uniqueID) {
        String self_username = Preferences.getInstance().getValue(Soapp.getInstance(), GlobalVariables.STRPREF_USERNAME);
        EntityBareJid jid = null;
        try {
            jid = JidCreate.entityBareFrom(To + GlobalVariables.xmppURL);
            ChatManager chatManager = ChatManager.getInstanceFor(SmackHelper.getXMPPConnection());
            Chat chat = chatManager.chatWith(jid);
            final Message message = new Message(jid, Message.Type.chat);
            message.setStanzaId(uniqueID);
            message.setBody("Call terminated");
            message.setThread("call");
            message.setSubject(self_username);
            StandardExtensionElement standardExtensionElement = StandardExtensionElement.builder(
                    "data", "urn:xmpp:soapp")
                    .addAttribute("ChatID", "VoiceCall")
                    .addAttribute("type", "reject")
                    .setText(body != null ? body : "")
                    .build();

            message.addExtension(standardExtensionElement);

            chat.send(message);
        } catch (Exception e) {

            DatabaseHelper.getInstance().saveLogsToDb("SMACK OUT SoappChatStanza", e.toString(), System.currentTimeMillis());
        }
    }

    public void SoappIceCandidateStanza(String To, IceCandidate iceCandidate, String uniqueID) {
        String self_username = Preferences.getInstance().getValue(Soapp.getInstance(), GlobalVariables.STRPREF_USERNAME);
        EntityBareJid jid = null;
        try {
            jid = JidCreate.entityBareFrom(To + GlobalVariables.xmppURL);
            ChatManager chatManager = ChatManager.getInstanceFor(SmackHelper.getXMPPConnection());
            Chat chat = chatManager.chatWith(jid);
            final Message message = new Message(jid, Message.Type.chat);
            message.setStanzaId(uniqueID);
            message.setBody("Candidate received");
            message.setThread("call");
            message.setSubject(self_username);
            StandardExtensionElement standardExtensionElement = StandardExtensionElement.builder(
                    "data", "urn:xmpp:soapp")
                    .addAttribute("ChatID", "VoiceCall")
                    .addAttribute("type", "candidate")
                    .addAttribute("label", String.valueOf(iceCandidate.sdpMLineIndex))
                    .addAttribute("id", iceCandidate.sdpMid)
                    .addAttribute("candidate", iceCandidate.sdp)
                    .build();

            message.addExtension(standardExtensionElement);
            chat.send(message);
        } catch (Exception e) {

            DatabaseHelper.getInstance().saveLogsToDb("SMACK OUT SoappChatStanza", e.toString(), System.currentTimeMillis());
        }
    }

    public void SoappCallStanza(String To, SessionDescription sdp, String uniqueID) {
        String self_username = Preferences.getInstance().getValue(Soapp.getInstance(), GlobalVariables.STRPREF_USERNAME);
        EntityBareJid jid = null;
        try {
            jid = JidCreate.entityBareFrom(To + GlobalVariables.xmppURL);
            ChatManager chatManager = ChatManager.getInstanceFor(SmackHelper.getXMPPConnection());
            Chat chat = chatManager.chatWith(jid);
            final Message message = new Message(jid, Message.Type.chat);
            message.setStanzaId(uniqueID);
            message.setBody("Incoming call");
            message.setThread("call");
            message.setSubject(self_username);
            StandardExtensionElement standardExtensionElement = StandardExtensionElement.builder(
                    "data", "urn:xmpp:soapp")
                    .addAttribute("ChatID", "VoiceCall")
                    .addAttribute("type", "incoming")
                    .addAttribute("SdpType", sdp.type.canonicalForm())
                    .setText(sdp.description)
                    .build();

            message.addExtension(standardExtensionElement);

            chat.send(message);
        } catch (Exception e) {

            DatabaseHelper.getInstance().saveLogsToDb("SMACK OUT SoappChatStanza", e.toString(), System.currentTimeMillis());
        }
    }

    public void SoappChatStanza(String Body, String To, String Subject, String uniqueID) {
        EntityBareJid jid = null;
        try {
            jid = JidCreate.entityBareFrom(To + GlobalVariables.xmppURL);
            ChatManager chatManager = ChatManager.getInstanceFor(SmackHelper.getXMPPConnection());
            Chat chat = chatManager.chatWith(jid);
            final Message message = new Message();
            message.setStanzaId(uniqueID);
            message.setType(Message.Type.chat);
            message.setBody(Body);
            message.setThread("chat");
            message.setSubject(Subject);
            StandardExtensionElement standardExtensionElement = StandardExtensionElement.builder(
                    "data", "urn:xmpp:soapp")
                    .addAttribute("ChatID", "Message")
                    .build();
            message.addExtension(standardExtensionElement);

            chat.send(message);
        } catch (Exception e) {

            DatabaseHelper.getInstance().saveLogsToDb("SMACK OUT SoappChatStanza", e.toString(), System.currentTimeMillis());
        }

    }

    public void SoappReplyStanza(String Body, String To, String Subject, String uniqueID, String reply, String replyjid, String replyid, String type, String imageid) {
        if (SmackHelper.getXMPPConnection() != null) {
            EntityBareJid jid = null;
            try {
                jid = JidCreate.entityBareFrom(To + GlobalVariables.xmppURL);

                ChatManager chatManager = ChatManager.getInstanceFor(SmackHelper.getXMPPConnection());
                Chat chat = chatManager.chatWith(jid);
                final Message message = new Message();
                message.setStanzaId(uniqueID);
                message.setType(Message.Type.chat);
                message.setBody(Body);
                message.setThread("chat");
                message.setSubject(Subject);
                StandardExtensionElement standardExtensionElement = StandardExtensionElement.builder(
                        "data", "urn:xmpp:soapp")
                        .addAttribute("ChatID", "ReplyID")
                        .addAttribute("reply", reply)
                        .addAttribute("replyjid", replyjid)
                        .addAttribute("replyid", replyid)
                        .addAttribute("replytype", type)
                        .addAttribute("imageid", imageid)
                        .build();
                message.addExtension(standardExtensionElement);

                chat.send(message);
            } catch (Exception e) {
                DatabaseHelper.getInstance().saveLogsToDb("SMACK SoappReplyStanza SoappChatStanza", e.toString(), System.currentTimeMillis());
            }
        }
    }

    public void SoappAudioStanza(String resourceID, String To, String url, String Subject, String
            uniqueId) {
        if (SmackHelper.getXMPPConnection() != null) {
            EntityBareJid jid = null;
            try {
                jid = JidCreate.entityBareFrom(To + GlobalVariables.xmppURL);

                ChatManager chatManager = ChatManager.getInstanceFor(SmackHelper.getXMPPConnection());
                Chat chat = chatManager.chatWith(jid);
                final Message message = new Message();
                message.setStanzaId(uniqueId);
                message.setType(Message.Type.chat);
                message.setBody(Soapp.getInstance().getApplicationContext().getString(R.string.audio_received));
                message.setSubject(Subject);
                message.setThread("chat");

                StandardExtensionElement standardExtensionElement = StandardExtensionElement.builder(
                        "data", "urn:xmpp:soapp")
                        .addAttribute("ChatID", "AudioID")
                        .addAttribute("resource_url", url)
                        .addAttribute("resource_id", resourceID)
                        .build();
                message.addExtension(standardExtensionElement);

                chat.send(message);
            } catch (Exception e) {
                DatabaseHelper.getInstance().saveLogsToDb("SMACK OUT SoappAudioStanza", e.toString(), System.currentTimeMillis());
            }
        }
    }

    public void SoappImageStanza(String resourceID, String To, String Direction, String url, String
            Subject, String rotation, String uniqueId, String image_thumb_base64) {
        if (SmackHelper.getXMPPConnection() != null) {
            EntityBareJid jid = null;
            try {
                jid = JidCreate.entityBareFrom(To + GlobalVariables.xmppURL);

                ChatManager chatManager = ChatManager.getInstanceFor(SmackHelper.getXMPPConnection());
                Chat chat = chatManager.chatWith(jid);
                final Message message = new Message();
                message.setStanzaId(uniqueId);
                message.setType(Message.Type.chat);
                message.setBody(Soapp.getInstance().getApplicationContext().getString(R.string.image_received));
                message.setSubject(Subject);
                message.setThread("chat");

                StandardExtensionElement standardExtensionElement = StandardExtensionElement.builder(
                        "data", "urn:xmpp:soapp")
                        .addAttribute("ChatID", "ImageID")
                        .addAttribute("direction", Direction)
                        .addAttribute("rotation", rotation)
                        .addAttribute("resource_url", url)
                        .addAttribute("resource_id", resourceID)
                        .addAttribute("thumbnail", image_thumb_base64)
                        .build();
                message.addExtension(standardExtensionElement);

                chat.send(message);
            } catch (Exception e) {
                DatabaseHelper.getInstance().saveLogsToDb("SMACK OUT SoappImageStanza", e.toString(), System.currentTimeMillis());
            }
        }
    }

    public void SoappVideoStanza(String resourceID, String To, String Direction, String videoUrl, String
            Subject, String uniqueId, String video_thumb_base64) {
        if (SmackHelper.getXMPPConnection() != null) {
            EntityBareJid jid = null;
            try {
                jid = JidCreate.entityBareFrom(To + GlobalVariables.xmppURL);

                ChatManager chatManager = ChatManager.getInstanceFor(SmackHelper.getXMPPConnection());
                Chat chat = chatManager.chatWith(jid);
                final Message message = new Message();
                message.setStanzaId(uniqueId);
                message.setType(Message.Type.chat);
                message.setBody(Soapp.getInstance().getApplicationContext().getString(R.string.video_received));
                message.setSubject(Subject);
                message.setThread("chat");

                StandardExtensionElement standardExtensionElement = StandardExtensionElement.builder(
                        "data", "urn:xmpp:soapp")
                        .addAttribute("ChatID", "VideoID")
                        .addAttribute("direction", Direction)
                        .addAttribute("resource_id", resourceID)
                        .addAttribute("thumbnail", video_thumb_base64)
                        .build();
                message.addExtension(standardExtensionElement);

                chat.send(message);
            } catch (Exception e) {
                DatabaseHelper.getInstance().saveLogsToDb("SMACK OUT SoappVideoStanza", e.toString(), System.currentTimeMillis());
            }
        }
    }

    public void SoappMapStanza(String mapurl, String To, String Latitude, String Longitude, String
            uniqueId, String Subject, String placeName, String address) {
        if (SmackHelper.getXMPPConnection() != null) {
            EntityBareJid jid = null;
            try {
                jid = JidCreate.entityBareFrom(To + GlobalVariables.xmppURL);

                ChatManager chatManager = ChatManager.getInstanceFor(SmackHelper.getXMPPConnection());
                Chat chat = chatManager.chatWith(jid);
                final Message message = new Message();
                message.setStanzaId(uniqueId);
                message.setType(Message.Type.chat);
                message.setBody(Soapp.getInstance().getApplicationContext().getString(R.string
                        .location_received));
                message.setSubject(Subject);
                message.setThread("chat");

                StandardExtensionElement standardExtensionElement = StandardExtensionElement.builder(
                        "data", "urn:xmpp:soapp")
                        .addAttribute("ChatID", "MapID")
                        .addAttribute("latitude", Latitude)
                        .addAttribute("longtitude", Longitude)
                        .addAttribute("staticurl", mapurl)
                        .addAttribute("infoid", placeName)
                        .addAttribute("address", address)

                        .build();
                message.addExtension(standardExtensionElement);

                chat.send(message);
            } catch (Exception e) {
                DatabaseHelper.getInstance().saveLogsToDb("SMACK OUT SoappMapStanza", e.toString(), System.currentTimeMillis());
            }
        }
    }

    //appt stanza for individual
    //type:
    //0 = delete appt
    //1 = title
    //2 = date/time
    //3 = location
    //4 = status
    //5 = create appt
    public void SoappAppointmentStanza(String jid, String pushMsg, String apptTitle, String apptMapURL,
                                       String apptLoc, String apptAdd, String apptLat, String apptLon,
                                       String apptDate, String apptTime, String apptStatus, String apptDet,
                                       String uniqueID, String apptInfoID, String apptInfoImgURL,
                                       String apptID, String displayName, String pushThread, String isCreating,
                                       String apptUpdateType) {
        if (SmackHelper.getXMPPConnection() != null) {
            EntityBareJid jidBare = null;
            try {
                jidBare = JidCreate.entityBareFrom(jid + GlobalVariables.xmppURL);

                ChatManager chatManager = ChatManager.getInstanceFor(SmackHelper.getXMPPConnection());
                Chat chat = chatManager.chatWith(jidBare);
                final Message message = new Message();
                message.setStanzaId(uniqueID);
                message.setType(Message.Type.chat);
                message.setBody(pushMsg);
                message.setSubject(displayName);

                //set apptID to thread
                message.setThread(pushThread + ":" + apptID);

                String timestamp = "";
                if (apptDate != null && apptTime != null && !apptDate.isEmpty() && !apptTime.isEmpty()) {
                    SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    timestamp = formatter.format(Date.parse(apptDate + " " + apptTime));
                }

                StandardExtensionElement standardExtensionElement = StandardExtensionElement.builder(
                        "appointment", "urn:xmpp:appointment")
                        .addAttribute("apptID", apptID)
                        .addAttribute("title", apptTitle)
                        .addAttribute("map", apptMapURL)
                        .addAttribute("location", apptLoc) //apptLoc
                        .addAttribute("address", apptAdd) //apptAdd
                        .addAttribute("latitude", apptLat)
                        .addAttribute("longitude", apptLon)
                        .addAttribute("date", apptDate)
                        .addAttribute("time", apptTime)
                        .addAttribute("timestamp", timestamp)
                        .addAttribute("status", apptStatus)
                        .addAttribute("detail", apptDet)
                        .addAttribute("infoid", apptInfoID) //resID for now
                        .addAttribute("infoimgurl", apptInfoImgURL) //resImgURL for now
                        .addAttribute("type", apptUpdateType) //resImgURL for now
                        .addAttribute("new", isCreating) // when create new appt =1 , 0 for other lke status
                        .build();
                message.addExtension(standardExtensionElement);

                chat.send(message);
            } catch (Exception e) {
                DatabaseHelper.getInstance().saveLogsToDb("SMACK OUT SoappAppointmentStanza", e.toString(), System.currentTimeMillis());
            }
        }
    }

    //stanza sent to request multi appt details when user added to grp or received new created grp
    public void SoappRequestMultiAppt(String To, String roomJid, String uniqueID) {
        if (SmackHelper.getXMPPConnection() != null) {
            EntityBareJid jid = null;
            try {
                jid = JidCreate.entityBareFrom(To + GlobalVariables.xmppURL);

                ChatManager chatManager = ChatManager.getInstanceFor(SmackHelper.getXMPPConnection());
                Chat chat = chatManager.chatWith(jid);
                final Message message = new Message();
                message.setStanzaId(uniqueID);
                message.setType(Message.Type.chat);
                message.setBody("");
                StandardExtensionElement standardExtensionElement = StandardExtensionElement.builder(
                        "data", "urn:xmpp:soapp")
                        .addAttribute("ChatID", "MultiApptID")
                        .addAttribute("groupjid", roomJid)

                        .build();
                message.addExtension(standardExtensionElement);

                chat.send(message);
            } catch (Exception e) {
                DatabaseHelper.getInstance().saveLogsToDb("SMACK OUT SoappRequestMultiAppt", e.toString(), System.currentTimeMillis());
            }
        }
    }

    public void SoappContactStanza(String To, String Name, String Phone, String uniqueId, String Subject) {
        if (SmackHelper.getXMPPConnection() != null) {
            EntityBareJid jid = null;
            try {
                jid = JidCreate.entityBareFrom(To + GlobalVariables.xmppURL);

                ChatManager chatManager = ChatManager.getInstanceFor(SmackHelper.getXMPPConnection());
                Chat chat = chatManager.chatWith(jid);
                final Message message = new Message();
                message.setStanzaId(uniqueId);
                message.setType(Message.Type.chat);
                message.setBody(Soapp.getInstance().getApplicationContext().getString(R.string
                        .contact_received));
                message.setSubject(Subject);
                message.setThread("chat");

                StandardExtensionElement standardExtensionElement = StandardExtensionElement.builder(
                        "data", "urn:xmpp:soapp")
                        .addAttribute("ChatID", "ContactID")
                        .addAttribute("name", Name)
                        .addAttribute("phone", Phone)
                        .build();
                message.addExtension(standardExtensionElement);

                chat.send(message);
            } catch (Exception e) {
                DatabaseHelper.getInstance().saveLogsToDb("SMACK OUT SoappContactStanza", e.toString(), System.currentTimeMillis());
            }
        }
    }

    public void SoappRestaurantStanza(String infoID, String To, String Title, String Subject,
                                      String uniqueId) {
        if (SmackHelper.getXMPPConnection() != null) {
            EntityBareJid jid = null;
            try {
                jid = JidCreate.entityBareFrom(To + GlobalVariables.xmppURL);

                ChatManager chatManager = ChatManager.getInstanceFor(SmackHelper.getXMPPConnection());
                Chat chat = chatManager.chatWith(jid);
                final Message message = new Message();
                message.setStanzaId(uniqueId);
                message.setType(Message.Type.chat);
                message.setBody(Soapp.getInstance().getApplicationContext().getString(R.string
                        .restaurant_received));
                message.setSubject(Subject);
                message.setThread("chat");

                StandardExtensionElement standardExtensionElement = StandardExtensionElement.builder(
                        "data", "urn:xmpp:soapp")
                        .addAttribute("ChatID", "RestaurantID")
                        .addAttribute("RestaurantTitle", Title)
                        .addAttribute("InfoId", infoID)
                        .build();
                message.addExtension(standardExtensionElement);

                chat.send(message);
            } catch (Exception e) {
                DatabaseHelper.getInstance().saveLogsToDb("SMACK OUT SoappRestaurantStanza", e.toString(), System.currentTimeMillis());
            }
        }
    }

    //share booking qr details to friends after confirmed by biz owner
    public void SoappShareBookingStanza(String qrCode, String To, String bookingdate, String
            bookingtime, String pax, String resid, String resTitle, String promo, String Username, String uniqueID, String bookingid) {
        if (SmackHelper.getXMPPConnection() != null) {
            EntityBareJid jid = null;
            try {
                jid = JidCreate.entityBareFrom(To + GlobalVariables.xmppURL);

                ChatManager chatManager = ChatManager.getInstanceFor(SmackHelper.getXMPPConnection());
                Chat chat = chatManager.chatWith(jid);
                final Message message = new Message();
                message.setStanzaId(uniqueID);
                message.setType(Message.Type.chat);
                message.setSubject(Username);

                String allMsg = Soapp.getInstance().getApplicationContext().getString(R.string
                        .booking_confirmed) + ": " + resTitle;
                message.setBody(allMsg);

                //set thread to chat
                message.setThread("chat");

                StandardExtensionElement standardExtensionElement = StandardExtensionElement.builder(
                        "data", "urn:xmpp:soapp")
                        .addAttribute("ChatID", "ShareBookingID")
                        .addAttribute("booking_id", bookingid)
                        .addAttribute("bookingdate", bookingdate)
                        .addAttribute("bookingtime", bookingtime)
                        .addAttribute("pax", pax)
                        .addAttribute("resid", resid)
                        .addAttribute("restitle", resTitle)
                        .addAttribute("promo", promo)
                        .addAttribute("booker", Username)
                        .addAttribute("qrcode", qrCode)
                        .build();
                message.addExtension(standardExtensionElement);

                chat.send(message);
            } catch (Exception e) {
                DatabaseHelper.getInstance().saveLogsToDb("SMACK OUT SoappShareBookingStanza", e.toString(), System.currentTimeMillis());
            }
        }
    }

    //need update friends stanza for shared booking

    //tell friends whom shared to before that booking has been deleted
    public void SoappDeleteShareBookingStanza(String To, String bookingid, String resTitle, String Username, String uniqueID) {
        if (SmackHelper.getXMPPConnection() != null) {
            EntityBareJid jid = null;
            try {
                jid = JidCreate.entityBareFrom(To + GlobalVariables.xmppURL);

                ChatManager chatManager = ChatManager.getInstanceFor(SmackHelper.getXMPPConnection());
                Chat chat = chatManager.chatWith(jid);
                final Message message = new Message();
                message.setStanzaId(uniqueID);
                message.setType(Message.Type.chat);
                message.setSubject(Username);

                String allMsg = Soapp.getInstance().getApplicationContext().getString(R.string
                        .booking_cancelled) + ": " + resTitle;
                message.setBody(allMsg);

                //set thread to chat
                message.setThread("chat");

                StandardExtensionElement standardExtensionElement = StandardExtensionElement.builder(
                        "data", "urn:xmpp:soapp")
                        .addAttribute("ChatID", "DeleteShareBookingID")
                        .addAttribute("booking_id", bookingid)
                        .build();
                message.addExtension(standardExtensionElement);

                chat.send(message);
            } catch (Exception e) {
                DatabaseHelper.getInstance().saveLogsToDb("SMACK OUT SoappDeleteShareBookingStanza", e.toString(), System.currentTimeMillis());
            }
        }
    }

    public void SoappTrackStanza(String To, String Lat, String Long) {
        if (SmackHelper.getXMPPConnection() != null) {
            EntityBareJid jid = null;
            try {
                jid = JidCreate.entityBareFrom(To + GlobalVariables.xmppURL);

                ChatManager chatManager = ChatManager.getInstanceFor(SmackHelper.getXMPPConnection());
                Chat chat = chatManager.chatWith(jid);
                final Message message = new Message();
                message.setStanzaId(UUID.randomUUID().toString());
                message.setType(Message.Type.chat);
                message.setThread("chat");

                StandardExtensionElement standardExtensionElement = StandardExtensionElement.builder(
                        "track", "urn:xmpp:tracking")
                        .addAttribute("latitude", Lat)
                        .addAttribute("longtitude", Long)
                        .build();

                message.addExtension(standardExtensionElement);

                chat.send(message);
            } catch (Exception e) {
                DatabaseHelper.getInstance().saveLogsToDb("SMACK OUT SoappTrackStanza", e.toString(), System.currentTimeMillis());
            }
        }
    }

    //add new member to existing grp (new member will create grp then send request multiApptID)
    public void SoappGrpAddMemberStanza(String To, String groupjid, String Body, String userName) {
        if (SmackHelper.getXMPPConnection() != null) {
            EntityBareJid jid = null;
            try {
                jid = JidCreate.entityBareFrom(To + GlobalVariables.xmppURL);

                ChatManager chatManager = ChatManager.getInstanceFor(SmackHelper.getXMPPConnection());
                Chat chat = chatManager.chatWith(jid);
                final Message message = new Message();
                message.setStanzaId(UUID.randomUUID().toString());
                message.setType(Message.Type.chat);
                message.setBody(Body);
                message.setSubject(userName);
                message.setThread("chat");

                StandardExtensionElement standardExtensionElement = StandardExtensionElement.builder(
                        "data", "urn:xmpp:soapp")
                        .addAttribute("ChatID", "GroupID")
                        .addAttribute("groupjid", groupjid)
                        .build();

                message.addExtension(standardExtensionElement);


                chat.send(message);
            } catch (Exception e) {
                DatabaseHelper.getInstance().saveLogsToDb("SMACK OUT SoappGrpAddMemberStanza", e.toString(), System.currentTimeMillis());
            }
        }
    }

    //delete a member from existing grp - haven't added thread
    public void SoappDeleteGrpMemStanza(String To, String Body) {
        if (SmackHelper.getXMPPConnection() != null) {
            EntityBareJid jid = null;
            try {
                jid = JidCreate.entityBareFrom(To + GlobalVariables.xmppURL);

                ChatManager chatManager = ChatManager.getInstanceFor(SmackHelper.getXMPPConnection());
                Chat chat = chatManager.chatWith(jid);
                final Message message = new Message();
                message.setStanzaId(UUID.randomUUID().toString());
                message.setType(Message.Type.chat);
                message.setBody(Body);

                StandardExtensionElement standardExtensionElement = StandardExtensionElement.builder(
                        "data", "urn:xmpp:soapp")
                        .addAttribute("ChatID", "DeleteGroupID")
                        .build();
                message.addExtension(standardExtensionElement);


                chat.send(message);
            } catch (Exception e) {
                DatabaseHelper.getInstance().saveLogsToDb("SMACK OUT SoappDeleteGrpMemStanza", e.toString(), System.currentTimeMillis());
            }
        }
    }

    public void SoappAckStanza(String To, String Id) {
        if (SmackHelper.getXMPPConnection() != null) {
            EntityBareJid jid = null;
            try {
                jid = JidCreate.entityBareFrom(To + GlobalVariables.xmppURL);

                ChatManager chatManager = ChatManager.getInstanceFor(SmackHelper.getXMPPConnection());
                Chat chat = chatManager.chatWith(jid);
                final Message message = new Message();
                message.setStanzaId(Id);
                standardExtensionElement = new StandardExtensionElement("received", "urn:xmpp:receipts");
                StandardExtensionElement standardExtensionElement = StandardExtensionElement.builder(
                        "received", "urn:xmpp:receipts")
                        .addAttribute("id", Id)
                        .build();
                message.addExtension(standardExtensionElement);

                chat.send(message);
            } catch (Exception e) {
                DatabaseHelper.getInstance().saveLogsToDb("SMACK OUT SoappAckStanza", e.toString(), System.currentTimeMillis());
            }
        }
    }

    public void SoappSeenStanza(String Body, String To) { //new Comment :receiver sends seen stanza back to sender
        if (SmackHelper.getXMPPConnection() != null) {
            EntityBareJid jid = null;
            try {
                jid = JidCreate.entityBareFrom(To + GlobalVariables.xmppURL);

                ChatManager chatManager = ChatManager.getInstanceFor(SmackHelper.getXMPPConnection());
                Chat chat = chatManager.chatWith(jid);
                final Message message = new Message();
                message.setStanzaId(UUID.randomUUID().toString());
                message.setType(Message.Type.chat);
                message.setBody(Body);
                StandardExtensionElement standardExtensionElement = StandardExtensionElement.builder(
                        "data", "urn:xmpp:soapp")
                        .addAttribute("ChatID", "SeenID")
                        .build();
                message.addExtension(standardExtensionElement);


                chat.send(message);
            } catch (Exception e) {
                DatabaseHelper.getInstance().saveLogsToDb("SMACK OUT SoappSeenStanza", e.toString(), System.currentTimeMillis());
            }
        }
    }

    public void SoappComposingStanza(String To) {
        if (SmackHelper.getXMPPConnection() != null) {
            EntityBareJid jid = null;
            try {
                jid = JidCreate.entityBareFrom(To + GlobalVariables.xmppURL);

                ChatManager chatManager = ChatManager.getInstanceFor(SmackHelper.getXMPPConnection());
                Chat chat = chatManager.chatWith(jid);
                final Message message = new Message();
                message.setStanzaId(UUID.randomUUID().toString());
                message.setType(Message.Type.chat);

                standardExtensionElement = new StandardExtensionElement("composing", "http://jabber.org/protocol/chatstates");
                message.addExtension(standardExtensionElement);

                chat.send(message);
            } catch (Exception e) {
                DatabaseHelper.getInstance().saveLogsToDb("SMACK OUT SoappComposingStanza", e.toString(), System.currentTimeMillis());
            }
        }
    }

    public void SoappPausedStanza(String To) {
        if (SmackHelper.getXMPPConnection() != null) {
            EntityBareJid jid = null;
            try {
                jid = JidCreate.entityBareFrom(To + GlobalVariables.xmppURL);

                ChatManager chatManager = ChatManager.getInstanceFor(SmackHelper.getXMPPConnection());
                Chat chat = chatManager.chatWith(jid);
                final Message message = new Message();
                message.setStanzaId(UUID.randomUUID().toString());
                message.setType(Message.Type.chat);

                standardExtensionElement = new StandardExtensionElement("paused", "http://jabber.org/protocol/chatstates");
                message.addExtension(standardExtensionElement);

                chat.send(message);
            } catch (Exception e) {
                DatabaseHelper.getInstance().saveLogsToDb("SMACK OUT SoappPausedStanza", e.toString(), System.currentTimeMillis());
            }
        }
    }

    public void SoappActiveStanza(String To) {
        if (SmackHelper.getXMPPConnection() != null) {
            EntityBareJid jid = null;
            try {
                jid = JidCreate.entityBareFrom(To + GlobalVariables.xmppURL);

                ChatManager chatManager = ChatManager.getInstanceFor(SmackHelper.getXMPPConnection());
                Chat chat = chatManager.chatWith(jid);
                final Message message = new Message();
                message.setStanzaId(UUID.randomUUID().toString());
                message.setType(Message.Type.chat);

                standardExtensionElement = new StandardExtensionElement("active", "http://jabber.org/protocol/chatstates");
                message.addExtension(standardExtensionElement);

                chat.send(message);
            } catch (Exception e) {
                DatabaseHelper.getInstance().saveLogsToDb("SMACK OUT SoappActiveStanza", e.toString(), System.currentTimeMillis());
            }
        }
    }

    public void SoappGoneStanza(String To) {
        if (SmackHelper.getXMPPConnection() != null) {
            EntityBareJid jid = null;
            try {
                jid = JidCreate.entityBareFrom(To + GlobalVariables.xmppURL);

                ChatManager chatManager = ChatManager.getInstanceFor(SmackHelper.getXMPPConnection());
                Chat chat = chatManager.chatWith(jid);
                final Message message = new Message();
                message.setStanzaId(UUID.randomUUID().toString());
                message.setType(Message.Type.chat);

                standardExtensionElement = new StandardExtensionElement("gone", "http://jabber.org/protocol/chatstates");
                message.addExtension(standardExtensionElement);

                chat.send(message);
            } catch (Exception e) {
                DatabaseHelper.getInstance().saveLogsToDb("SMACK OUT SoappGoneStanza", e.toString(), System.currentTimeMillis());
            }
        }
    }


}