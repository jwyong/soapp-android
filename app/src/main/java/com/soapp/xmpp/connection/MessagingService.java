package com.soapp.xmpp.connection;

import android.app.Notification;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;

import com.soapp.global.CheckInternetAvaibility;
import com.soapp.global.GlobalVariables;
import com.soapp.global.Preferences;
import com.soapp.xmpp.SmackHelper;

import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.tcp.XMPPTCPConnection;
import org.jivesoftware.smackx.ping.android.ServerPingWithAlarmManager;

import java.io.IOException;

public class MessagingService extends Service {

    private SmackHelper smackHelper;
    private static XMPPTCPConnection mConnection;
    private boolean mConnecting = false;
    private Thread mThread;
    private Handler mHandler;
    public static String user, pass;
    public static Boolean isRunning = false;

    public MessagingService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void onCreate() {
        super.onCreate();

        //only start in foreground if >= android 8.0
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            startForeground(999, new Notification.Builder(this).build());
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        isRunning = true;
        CheckInternetAvaibility.getINSTANCE();
        mConnection = SmackHelper.getXMPPConnection();
        if (CheckInternetAvaibility.gotInternet) {
            start();
        }

        return START_REDELIVER_INTENT;
    }

    public static void start(Context context, String user, String pass) {
        Intent intent = new Intent(context, MessagingService.class);

        if (isRunning) {
            context.stopService(intent);
        }

        context.startService(intent);

        MessagingService.user = user;
        MessagingService.pass = pass;
    }

    public void start() {
        if (!mConnecting) {
            mConnecting = true;

            // Create ConnectionThread Loop
            if (mThread == null || !mThread.isAlive()) {
                mThread = new Thread(() -> {
                    Looper.prepare();
                    mHandler = new Handler();
                    initConnection();
                    Looper.loop();
                });
                mThread.start();
            }
        }
    }

    public void stop() {
        mConnecting = false;
    }

    private void initConnection() {
        if (isRunning) {
            if (SmackHelper.getXMPPConnection() == null) {
                smackHelper = SmackHelper.getInstance(this, user, pass);
            }

            try {
                if (user == null || pass == null) {
                    user = Preferences.getInstance().getValue(this, GlobalVariables.STRPREF_USER_ID);
                    pass = Preferences.getInstance().getValue(this, GlobalVariables.STRPREF_XMPP_PASSWORD);
                }
                SmackHelper.getXMPPConnection().connect();
                SmackHelper.getXMPPConnection().login(user, pass);

            } catch (XMPPException e) {
                e.printStackTrace();
            } catch (SmackException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onDestroy() {
        stop();
        isRunning = false;

        mThread = null;
        if (SmackHelper.getXMPPConnection() != null) {
            try {
                ServerPingWithAlarmManager.onDestroy();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        super.onDestroy();
    }
}
