package com.soapp.global;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;

import com.soapp.R;
import com.soapp.new_chat_schedule.group.NewGrpChatController;
import com.soapp.new_chat_schedule.individual.NewChatController;

public class HomeTabHelper {
    //basics
    private Preferences preferences = Preferences.getInstance();
    private UIHelper uiHelper = new UIHelper();

    //start new chat (open CR)
    public void startNewChat(Context context) {
        Runnable newChatAction = () -> {
            if (PackageManager.PERMISSION_DENIED == context.checkCallingOrSelfPermission(Manifest.permission.READ_CONTACTS)) {

                new PermissionHelper().CheckPermissions(context, 1007, R.string.permission_txt);
                preferences.save(context, "contactPermission", "chat");
            } else {

                Intent intent = new Intent(context, NewChatController.class);
                intent.putExtra("From", "chat");
                context.startActivity(intent);
            }
        };

        //action for new grp chat
        Runnable newGrpChatAction = () -> {
            if (PackageManager.PERMISSION_DENIED == context.checkCallingOrSelfPermission(Manifest.permission.READ_CONTACTS)) {

                new PermissionHelper().CheckPermissions(context, 1007, R.string.permission_txt);
                preferences.save(context, "contactPermission", "groupchat");
            } else {

                Intent intent2 = new Intent(context, NewGrpChatController.class);
                intent2.putExtra("From", "groupchat");
                context.startActivity(intent2);

            }
        };

        uiHelper.bottomDialog5Btns(context, context.getString(R.string.new_chat), newChatAction,
                context.getString(R.string.new_g_chat), newGrpChatAction,
                null, null, null, null, null, null);
    }

    //start new schedule (open CR)
    public void startNewSche(Context context, long date) {
        Runnable newIndiApptAction = () -> {

            if (PackageManager.PERMISSION_DENIED == context.checkCallingOrSelfPermission(Manifest.permission.READ_CONTACTS)) {
                new PermissionHelper().CheckPermissions(context, 1007, R.string.permission_txt);
                preferences.save(context, "contactPermission", "schedule");
                preferences.save(context, "dwn", String.valueOf(date));
            } else {
                Intent intent = new Intent(context, NewChatController.class);
                intent.putExtra("From", "schedule");

                if (date != 0) {
                    intent.putExtra("Date", date);
                }
                context.startActivity(intent);

            }
        };

        Runnable newGrpApptAction = () -> {
            if (PackageManager.PERMISSION_DENIED == context.checkCallingOrSelfPermission(Manifest.permission.READ_CONTACTS)) {
                new PermissionHelper().CheckPermissions(context, 1007, R.string.permission_txt);
                preferences.save(context, "contactPermission", "groupschedule");

            } else {
                Intent intent = new Intent(context, NewGrpChatController.class);
                intent.putExtra("From", "schedule");

                if (date != 0) {
                    intent.putExtra("Date", date);
                }

                context.startActivity(intent);
            }
        };

        Runnable newExistApptAction = () -> {
            Intent intent = new Intent(context, NewGrpChatController.class);
            intent.putExtra("From", "existing");

            if (date != 0) {
                intent.putExtra("Date", date);
            }

            context.startActivity(intent);
        };

        uiHelper.bottomDialog5Btns(context, context.getString(R.string.new_indi_appt), newIndiApptAction,
                context.getString(R.string.new_g_appt), newGrpApptAction,
                context.getString(R.string.exist_chatrooms), newExistApptAction,
                null, null, null, null);
    }
}
