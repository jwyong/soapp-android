package com.soapp.xmpp.SingleChatHelper;

import org.jivesoftware.smack.chat2.Chat;
import org.jivesoftware.smack.chat2.OutgoingChatMessageListener;
import org.jivesoftware.smack.packet.Message;
import org.jxmpp.jid.EntityBareJid;

/* Created by chang on 07/07/2017. */

public class OutgoingMessageListener implements OutgoingChatMessageListener {
    @Override
    public void newOutgoingMessage(EntityBareJid to, Message message, Chat chat) {
    }


}
