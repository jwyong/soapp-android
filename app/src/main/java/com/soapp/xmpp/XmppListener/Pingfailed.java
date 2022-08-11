package com.soapp.xmpp.XmppListener;

import com.soapp.xmpp.SmackHelper;

import org.jivesoftware.smack.ReconnectionManager;
import org.jivesoftware.smackx.ping.PingFailedListener;

/* Created by chang on 02/10/2017. */

public class Pingfailed implements PingFailedListener {
    @Override
    public void pingFailed() {
        ReconnectionManager.getInstanceFor(SmackHelper.getXMPPConnection()).enableAutomaticReconnection();
    }
}
