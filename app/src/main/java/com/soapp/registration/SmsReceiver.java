package com.soapp.registration;

/* Created by ash on 15/01/2018. */

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.SmsMessage;

public class SmsReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {

        //don't run anything unless phone verification is running
        if (!PhoneVerification.isRunning) {
            return;
        }

        final Bundle bundle = intent.getExtras();
        try {
            if (bundle != null) {
                final Object[] pdusObj = (Object[]) bundle.get("pdus");

                for (int i = 0; i < pdusObj.length; i++) {
                    SmsMessage currentMessage = SmsMessage.createFromPdu((byte[]) pdusObj[i]);
                    String message = currentMessage.getDisplayMessageBody();

                    if (message.contains("Soapp")) { //msg from soapp
                        String confirmCode = message.replaceAll("[^0-9]", "");
                        confirmCode = confirmCode.substring(confirmCode.length() - 6);

                        PhoneVerification Sms = new PhoneVerification();
                        Sms.receiveSms(confirmCode);
                    }
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
