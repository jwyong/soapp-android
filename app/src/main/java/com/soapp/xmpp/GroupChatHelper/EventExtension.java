package com.soapp.xmpp.GroupChatHelper;

import org.jivesoftware.smack.packet.ExtensionElement;
import org.jivesoftware.smack.util.XmlStringBuilder;

/**
 * Created by chang on 10/07/2017.
 */

public class EventExtension implements ExtensionElement {

    public static final String ELEMENT = "event";
    public static final String NAMESPACE = "http://jabber.org/protocol/pubsub#event";


    @Override
    public String getNamespace() {
        return NAMESPACE;
    }

    @Override
    public String getElementName() {
        return ELEMENT;
    }

    @Override
    public CharSequence toXML(String enclosingNamespace) {
        XmlStringBuilder xml = new XmlStringBuilder(this);
        xml.closeEmptyElement();
        return xml;
    }
}
