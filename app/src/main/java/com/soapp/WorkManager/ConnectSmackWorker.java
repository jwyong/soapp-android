package com.soapp.WorkManager;

import com.soapp.xmpp.SmackHelper;

import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.XMPPException;

import java.io.IOException;

import androidx.annotation.NonNull;
import androidx.work.Worker;

public class ConnectSmackWorker extends Worker {
    @NonNull
    @Override
    public Result doWork() {
        if (SmackHelper.getXMPPConnection() != null && !SmackHelper.getXMPPConnection().isAuthenticated()) {
            try {
                SmackHelper.getXMPPConnection().connect();
                SmackHelper.getXMPPConnection().login();
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

        return Result.SUCCESS;
    }
}
