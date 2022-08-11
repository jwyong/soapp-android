package com.soapp.xmpp.connection;

import com.soapp.global.CheckInternetAvaibility;
import com.soapp.sql.DatabaseHelper;

import org.jivesoftware.smack.AbstractXMPPConnection;
import org.jivesoftware.smack.ConnectionListener;
import org.jivesoftware.smack.XMPPConnection;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.UnknownHostException;

/* Created by chang on 11/07/2017. */

public class XMPPConnectionListener implements ConnectionListener {
    public static String loggedin;
    public static boolean firstTimeConnect;
    AbstractXMPPConnection connection;
    private DatabaseHelper databaseHelper = DatabaseHelper.getInstance();
    private String userName = "";
    private String passWord = "";


    @Override
    public void connected(XMPPConnection connection) {
        loggedin = "connected";
    }

    @Override
    public void authenticated(final XMPPConnection connection, boolean resumed) {
        loggedin = "auth";
        firstTimeConnect = true;

        databaseHelper.getSendOfflineMsg();

        CheckInternetAvaibility.xmppauth.postValue(true);
    }


    @Override
    public void connectionClosed() {
        CheckInternetAvaibility.xmppauth.postValue(false);
    }

    @Override
    public void connectionClosedOnError(Exception e) {
        databaseHelper.saveLogsToDb("SMACK connectionClosedOnError", getStackTraceString(e), System.currentTimeMillis());

        CheckInternetAvaibility.xmppauth.postValue(false);
    }

    public static String getStackTraceString(Throwable tr) {
        if (tr == null) {
            return "";
        }

        // This is to reduce the amount of log spew that apps do in the non-error
        // condition of the network being unavailable.
        Throwable t = tr;
        while (t != null) {
            if (t instanceof UnknownHostException) {
                return "";
            }
            t = t.getCause();
        }

        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        tr.printStackTrace(pw);
        pw.flush();

        return sw.toString();
    }
}