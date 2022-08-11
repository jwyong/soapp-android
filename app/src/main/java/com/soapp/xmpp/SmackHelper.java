package com.soapp.xmpp;

import android.os.AsyncTask;
import android.util.Log;

import com.soapp.global.GlobalVariables;
import com.soapp.global.Preferences;
import com.soapp.setup.Soapp;
import com.soapp.sql.DatabaseHelper;
import com.soapp.xmpp.GroupChatListener.GroupChatListenerHelper;
import com.soapp.xmpp.GroupChatListener.GroupChatMessageListener;
import com.soapp.xmpp.SingleChatHelper.IncomingMessageListener;
import com.soapp.xmpp.XmppListener.Pingfailed;
import com.soapp.xmpp.connection.MessagingService;
import com.soapp.xmpp.connection.XMPPConnectionListener;

import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.ReconnectionManager;
import org.jivesoftware.smack.SmackConfiguration;
import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.android.AndroidSmackInitializer;
import org.jivesoftware.smack.chat2.ChatManager;
import org.jivesoftware.smack.tcp.XMPPTCPConnection;
import org.jivesoftware.smack.tcp.XMPPTCPConnectionConfiguration;
import org.jivesoftware.smackx.debugger.android.AndroidDebugger;
import org.jivesoftware.smackx.ping.PingManager;
import org.jivesoftware.smackx.ping.android.ServerPingWithAlarmManager;

import java.io.IOException;
import java.net.InetAddress;
import java.util.concurrent.TimeUnit;

public class SmackHelper {

    public static String xmppUsername, xmppPass;
    public static XMPPTCPConnection mConnection;
    private static final String HOST = "xmpp.soappchat.com";
    private static final String DOMAIN = "xmpp.soappchat.com";
    private static final int PORT = 5222;
    private static final String TAG = "SmackHelper";
    boolean isConnecting = false;
    boolean isLogin = false;


    AsyncTask<Void, Void, Boolean> connectionThread = new AsyncTask<Void, Void,
            Boolean>() {
        @Override
        protected synchronized Boolean doInBackground(Void... arg0) {
            try {
                mConnection.connect();
            } catch (SmackException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (XMPPException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            return false;
        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            super.onPostExecute(aBoolean);
            isConnecting = false;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            isConnecting = true;
        }
    };

    AsyncTask<Void, Void, Boolean> loginThread =
            new AsyncTask<Void, Void,
                    Boolean>() {
                @Override
                protected synchronized Boolean doInBackground(Void... arg0) {
                    try {

                        if (xmppUsername == null || xmppUsername.isEmpty()) {
                            xmppUsername =
                                    Preferences.getInstance().getValue(Soapp.getInstance(),
                                            GlobalVariables.STRPREF_USER_ID);
                        }

                        if (xmppPass == null || xmppPass.isEmpty()) {
                            xmppPass = Preferences.getInstance().getValue(Soapp.getInstance(),
                                    GlobalVariables.STRPREF_XMPP_PASSWORD);
                        }

                        mConnection.login(xmppUsername, xmppPass);
                        // Set the status to available
                        Log.i("LOGIN",
                                "Yey! We're connected to the Xmpp server!" + xmppUsername);
                    } catch (XMPPException | SmackException | IOException | InterruptedException e) {
                        e.printStackTrace();
                    }

                    return false;
                }

                @Override
                protected void onPostExecute(Boolean aBoolean) {
                    super.onPostExecute(aBoolean);
                    isLogin = false;

                }

                @Override
                protected void onPreExecute() {
                    super.onPreExecute();
                    isLogin = true;
                }
            };


    public static SmackHelper instance = null;
    MessagingService service;

    public static SmackHelper getInstance(MessagingService service, String username, String pass) {
        if (instance == null) {
            instance = new SmackHelper(service, username, pass);
        }
        return instance;
    }

    public SmackHelper(MessagingService service, String xmppUsername, String xmppPass) {
        SmackHelper.xmppUsername = xmppUsername;
        SmackHelper.xmppPass = xmppPass;
        this.service = service;
        init();
    }


    public void init() {
        try {

            AndroidSmackInitializer.initialize(service);
            InetAddress addr = InetAddress.getByName(HOST);
            XMPPTCPConnectionConfiguration.Builder configBuilder =
                    XMPPTCPConnectionConfiguration.builder()
                            .setHost(HOST)
                            .setHostAddress(addr)
                            .setXmppDomain(DOMAIN)
                            .setPort(PORT)
                            .setSecurityMode(ConnectionConfiguration.SecurityMode.required)
                            .setKeystoreType(null).setDebuggerFactory(AndroidDebugger::new)
                            .setUsernameAndPassword(xmppUsername, xmppPass)
                            .setDebuggerFactory(AndroidDebugger::new)
                            .setResource("Android");

            //remove when production
            SmackConfiguration.DEBUG = true;
            AndroidDebugger.printInterpreted = true;

            if (mConnection == null) {
                mConnection = new XMPPTCPConnection(configBuilder.build());
                XMPPConnectionListener connectionListener = new XMPPConnectionListener();
                mConnection.addConnectionListener(connectionListener);
//            connection.setUnknownIqRequestReplyMode();
                mConnection.setUseStreamManagementResumption(false);
                mConnection.setUseStreamManagement(false);
                mConnection.setReplyTimeout(30000);

                GroupChatListenerHelper groupChatListenerHelper = new GroupChatListenerHelper();
                GroupChatMessageListener groupChatMessageListener = new GroupChatMessageListener();
//                mConnection.addAsyncStanzaListener(packet -> Log.d("smack", "processStanza()
// called with: packet =
// [" + packet + "]"), new StanzaFilter() {
//                    @Override
//                    public boolean accept(Stanza stanza) {
//                        try {
////                            if (stanza.hasExtension("event", "http://jabber
// .org/protocol/pubsub#event")) {
////                                EventElement eventElement = stanza.getExtension("event",
// "http://jabber
// .org/protocol/pubsub#event");
////                                eventElement.
////                                        Log.d(TAG, "accept() called with: stanza = [" +
// Arrays.toString
// (eventElement.getExtensions().toArray()) + "]");
////                            } else {
////                                Log.d(TAG, "accept: no extensions");
////                            }
//                        } catch (Exception e) {
//                            Log.e("smack", "accept: ", e);
//                        }
//                        return false;
//                    }
//                });
                mConnection.addAsyncStanzaListener(groupChatListenerHelper,
                        groupChatMessageListener);

                IncomingMessageListener incomingMessageListener = new IncomingMessageListener();
                ChatManager chatManager = ChatManager.getInstanceFor(mConnection);


                Pingfailed pingFailedListener = new Pingfailed();
                PingManager pm = PingManager.getInstanceFor(mConnection);

                //ping interval is in seconds
                pm.setPingInterval((int) TimeUnit.MINUTES.toSeconds(2));
                pm.registerPingFailedListener(() -> {
                    Log.d(TAG, "pingf: ");
                    mConnection.instantShutdown();
                    init();
                });
                chatManager.addIncomingListener(incomingMessageListener);
                PingManager.getInstanceFor(mConnection);

                //alarm manager to ping to server every
                ReconnectionManager.getInstanceFor(mConnection).enableAutomaticReconnection();
                ServerPingWithAlarmManager.onCreate(service);
                ServerPingWithAlarmManager.getInstanceFor(mConnection).setEnabled(true);
                ReconnectionManager.setEnabledPerDefault(true);


            }
        } catch (Exception e) {
            DatabaseHelper.getInstance().saveLogsToDb("SMACK CfgBuilder", e.toString(),
                    System.currentTimeMillis());
        }
    }

    public void connect() {
        if (!isConnecting) {
            getConnectionThread().execute();
        }


//        try {
//            mConnection.connect();
//        } catch (SmackException e) {
//            e.printStackTrace();
//        } catch (IOException e) {
//            e.printStackTrace();
//        } catch (XMPPException e) {
//            e.printStackTrace();
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }
    }

    public void login() {
        if (!isLogin) {
            getLoginThread().execute();
        }


//        try {
//            mConnection.login(xmppUsername, xmppPass);
//            // Set the status to available
//            Log.i("LOGIN", "Yey! We're connected to the Xmpp server!" + xmppUsername);
//        } catch (XMPPException | SmackException | IOException | InterruptedException e) {
//            e.printStackTrace();
//        }
    }

    public void disconnect() {
        new Thread(() -> mConnection.disconnect()).start();
    }

    public static XMPPTCPConnection getXMPPConnection() {
        return mConnection;
    }

    public AsyncTask<Void, Void, Boolean> getConnectionThread() {
        return connectionThread;
    }

    public AsyncTask<Void, Void, Boolean> getLoginThread() {
        return loginThread;
    }

    public static String getXmppUsername() {
        if (xmppUsername == null) {
            xmppUsername = Preferences.getInstance().getValue(Soapp.getInstance(), GlobalVariables.STRPREF_USER_ID);
        }
        return xmppUsername;
    }

    public static String getXmppPass() {
        if (xmppPass == null) {
            xmppPass = Preferences.getInstance().getValue(Soapp.getInstance(), GlobalVariables.STRPREF_XMPP_PASSWORD);
        }
        return xmppPass;
    }
}

