package com.soapp.xmpp.PubsubHelper;

import com.soapp.global.GlobalVariables;
import com.soapp.sql.DatabaseHelper;
import com.soapp.xmpp.SmackHelper;

import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.packet.IQ;
import org.jxmpp.jid.impl.JidCreate;
import org.jxmpp.stringprep.XmppStringprepException;

/* Created by chang on 05/07/2017. */

public class PubsubNodeCall {
    //when user changes user profile, send to
    public void PubsubSingle(String Node, String Jid, String uniqueID) {
        if (SmackHelper.getXMPPConnection() != null) {

            try {
                org.jxmpp.jid.Jid to = JidCreate.from("pubsub.xmpp.soappchat.com");
                PubsubUserStatus pubsubUserStatus = new PubsubUserStatus(Node, Jid, uniqueID);
                pubsubUserStatus.setType(IQ.Type.set);
                pubsubUserStatus.setTo(to);
                pubsubUserStatus.setStanzaId("publishUser");

                SmackHelper.getXMPPConnection().sendStanza(pubsubUserStatus);
            } catch (SmackException.NotConnectedException | InterruptedException ignored) {
                DatabaseHelper.getInstance().saveLogsToDb("SMACK out PubsubSingle", ignored.toString(), System.currentTimeMillis());
            } catch (XmppStringprepException e) {
                e.printStackTrace();
            }
        }
    }

    public void PubsubGroup(String Node, String MyJid, String uniqueID, String publishGroupInfo, String deleteGroup) {
        if (SmackHelper.getXMPPConnection() != null) {

            try {
                org.jxmpp.jid.Jid to = JidCreate.from("pubsub.xmpp.soappchat.com");

                PubsubGroupStatus pubsubGroupStatus = new PubsubGroupStatus(Node, MyJid, uniqueID, publishGroupInfo,
                        deleteGroup);
                pubsubGroupStatus.setType(IQ.Type.set);
                pubsubGroupStatus.setTo(JidCreate.from("pubsub.xmpp.soappchat.com"));
                pubsubGroupStatus.setStanzaId("publishUser");

                SmackHelper.getXMPPConnection().sendStanza(pubsubGroupStatus);
            } catch (SmackException.NotConnectedException | InterruptedException ignored) {
                DatabaseHelper.getInstance().saveLogsToDb("SMACK out PubsubGroup", ignored.toString(), System.currentTimeMillis());
            } catch (XmppStringprepException e) {
                e.printStackTrace();
            }
        }
    }

    public void block(String jid, String myJid) {
        if (SmackHelper.getXMPPConnection() != null) {
            try {
                BlockingIq block = new BlockingIq(jid + GlobalVariables.xmppURL);
                block.setType(IQ.Type.set);
                block.setFrom(JidCreate.from(myJid + GlobalVariables.xmppURL));
                block.setStanzaId("blockuser");

                SmackHelper.getXMPPConnection().sendStanza(block);
            } catch (SmackException.NotConnectedException | InterruptedException ignored) {
                DatabaseHelper.getInstance().saveLogsToDb("SMACK out block", ignored.toString(), System.currentTimeMillis());
            } catch (XmppStringprepException e) {
                e.printStackTrace();
            }
        }
    }

    public void unblock(String jid) {
        if (SmackHelper.getXMPPConnection() != null) {
            UnBlockingiq unBlock = new UnBlockingiq(jid + GlobalVariables.xmppURL);
            unBlock.setType(IQ.Type.set);
            unBlock.setStanzaId("unblockuser");
            try {
                SmackHelper.getXMPPConnection().sendStanza(unBlock);
            } catch (SmackException.NotConnectedException | InterruptedException ignored) {
                DatabaseHelper.getInstance().saveLogsToDb("SMACK out unblock", ignored.toString(), System.currentTimeMillis());
            }
        }
    }

}
