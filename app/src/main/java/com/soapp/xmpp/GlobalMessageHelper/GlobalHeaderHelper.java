package com.soapp.xmpp.GlobalMessageHelper;

import com.soapp.global.Preferences;
import com.soapp.setup.Soapp;
import com.soapp.sql.DatabaseHelper;

import java.util.Calendar;

/* Created by chang on 14/10/2017. */

public class GlobalHeaderHelper {
    public static DatabaseHelper databaseHelper = DatabaseHelper.getInstance();
    private Preferences preferences = Preferences.getInstance();

    public void GlobalHeaderTime(String jid) {
        String previous_room_id = preferences.getValue(Soapp.getInstance().getApplicationContext(), "previous_room_id");
        if (!previous_room_id.equals(jid)) { //only add date if jid is different
            Calendar cal = Calendar.getInstance();

            cal.setTimeInMillis(System.currentTimeMillis());
            cal.set(Calendar.HOUR_OF_DAY, 0); //set hours to zero
            cal.set(Calendar.MINUTE, 0); // set minutes to zero
            cal.set(Calendar.SECOND, 0); //set seconds to zero
            cal.set(Calendar.MILLISECOND, 0);

            long timeheader = cal.getTimeInMillis();

            databaseHelper.addNewTime(jid, timeheader);
        }
        preferences.save(Soapp.getInstance().getApplicationContext(), "previous_room_id", jid);
    }

}
