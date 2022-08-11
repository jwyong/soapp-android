package com.soapp.xmpp.PubsubHelper;

import org.jivesoftware.smack.packet.IQ;

/* Created by chang on 29/08/2017. */

public class LastActivity extends IQ {

    public static final String ELEMENT = "lastActivity";
    public static final String NAMESPACE = "urn:xmpp:lastActivity";
    String userjid;
    String otherJid;

    public LastActivity(IQ iq) {
        super(iq);
    }

    public LastActivity(String userjid, String otherjid) {
        super(ELEMENT, NAMESPACE);
        this.userjid = userjid;
        this.otherJid=otherjid;
    }

    @Override
    protected IQChildElementXmlStringBuilder getIQChildElementBuilder(IQChildElementXmlStringBuilder xml) {
        xml.rightAngleBracket();
        xml.append("<item jid='").append(userjid).append("'/>");
        return xml;
    }
}
