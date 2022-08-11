package com.soapp.xmpp.PubsubHelper;

import org.jivesoftware.smack.packet.IQ;

/* Created by chang on 29/08/2017. */

public class BlockingIq extends IQ {

    public static final String ELEMENT = "block";
    public static final String NAMESPACE = "urn:xmpp:blocking";
    String userjid;

    public BlockingIq(IQ iq) {
        super(iq);
    }

    public BlockingIq(String userjid) {
        super(ELEMENT, NAMESPACE);
        this.userjid = userjid;
    }

    @Override
    protected IQChildElementXmlStringBuilder getIQChildElementBuilder(IQChildElementXmlStringBuilder xml) {
        xml.rightAngleBracket();
        xml.append("<item jid='").append(userjid).append("'/>");
        return xml;
    }
}
