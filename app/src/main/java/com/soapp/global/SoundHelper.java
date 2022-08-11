package com.soapp.global;

/* Created by Soapp on 18/09/2017. */

import android.content.Context;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Vibrator;

import com.soapp.setup.Soapp;

public class SoundHelper {
    public void playRawSound(Context c, int rid) {
        //play notification sound in using ringtone channel
        try {
            Uri alarmSound = Uri.parse("android.resource://" + c.getPackageName() + "/" + rid);
            Ringtone r = RingtoneManager.getRingtone(Soapp.getInstance().getApplicationContext(), alarmSound);
            r.play();
            Vibrator vibe = (Vibrator) Soapp.getInstance().getApplicationContext().getSystemService(Context
                    .VIBRATOR_SERVICE);
            vibe.vibrate(300);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}