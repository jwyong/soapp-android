package com.soapp.sql.room;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.soapp.global.GlobalVariables;
import com.soapp.global.Preferences;
import com.soapp.sql.room.entity.Appointment;
import com.soapp.sql.room.entity.AppointmentHist;
import com.soapp.sql.room.entity.ApptWorkUUID;
import com.soapp.sql.room.entity.Booking;
import com.soapp.sql.room.entity.ChatList;
import com.soapp.sql.room.entity.ContactRoster;
import com.soapp.sql.room.entity.ErrorLogs;
import com.soapp.sql.room.entity.GroupMem;
import com.soapp.sql.room.entity.GroupMem_Status;
import com.soapp.sql.room.entity.MediaWorker;
import com.soapp.sql.room.entity.Message;
import com.soapp.sql.room.entity.PushNotification;
import com.soapp.sql.room.entity.ResReview;
import com.soapp.sql.room.entity.Restaurant;
import com.soapp.sql.room.entity.Reward;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;


/* Created by ibrahim on 13/03/2018. */

@Database(entities = {ContactRoster.class, GroupMem.class, ChatList.class,
        Message.class, Restaurant.class, Appointment.class, Booking.class, GroupMem_Status.class, ResReview.class, ErrorLogs.class, AppointmentHist.class,
        PushNotification.class, Reward.class, ApptWorkUUID.class, MediaWorker.class}, version = 8, exportSchema = false)

//@TypeConverters(TimeStampConverter.class)
public abstract class SoappDatabase extends RoomDatabase {
    private static SoappDatabase INSTANCE;

    public static synchronized SoappDatabase getInstance(Context context) {
        if (INSTANCE == null) {
            INSTANCE = Room.databaseBuilder(
                    context.getApplicationContext(),
                    SoappDatabase.class,
                    "soappDB")
                    .fallbackToDestructiveMigration()
                    .allowMainThreadQueries()
                    .addCallback(new Callback() {
                        @Override
                        public void onOpen(@NonNull SupportSQLiteDatabase db) {
                            super.onOpen(db);
                        }
                    })

                    .addCallback(new Callback() {

                        @Override
                        public void onCreate(@NonNull SupportSQLiteDatabase db) {
                            super.onCreate(db);

                            //delete old "soapp" db
                            context.deleteDatabase("soapp");

                            //force user to re-register
                            Preferences.getInstance().save(context, GlobalVariables.STRPREF_LOGIN_STATUS, "");

                            Intent i = context.getPackageManager().getLaunchIntentForPackage(context.getPackageName());
                            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            context.startActivity(i);
                        }
                    })
                    .addMigrations(new Migration(1, 2) {
                                       @Override
                                       public void migrate(@NonNull SupportSQLiteDatabase database) {

                                           database.execSQL("ALTER TABLE CONTACT_ROSTER ADD COLUMN DisabledStatus INTEGER DEFAULT 0");

                                           database.execSQL("CREATE TABLE tempCL(ChatRow INTEGER PRIMARY KEY AUTOINCREMENT, ChatJid TEXT, " +
                                                   "LastDateReceived INTEGER, Admin_Self INTEGER, NotiBadge INTEGER, " +
                                                   "LastDispMsg TEXT, TypingStatus INTEGER DEFAULT 0, OnlineStatus INTEGER, TypingName TEXT, LastSenderName TEXT) ");

                                           database.execSQL("INSERT INTO tempCL SELECT ChatRow , ChatJid, " +
                                                   "LastDateReceived , Admin_Self , NotiBadge , " +
                                                   "LastDispMsg , TypingStatus, OnlineStatus , TypingName , LastSenderName FROM CHAT_LIST");
                                           database.execSQL("DROP TABLE CHAT_LIST");

                                           database.execSQL("ALTER TABLE tempCL RENAME TO CHAT_LIST");
                                       }
                                   }, new Migration(2, 3) {
                                       @Override
                                       public void migrate(@NonNull SupportSQLiteDatabase database) {
                                           database.execSQL("CREATE TABLE ErrorLogs (errorID INTEGER PRIMARY KEY AUTOINCREMENT, errorTitle TEXT, errorDesc TEXT, createdTimeStamp TEXT)");

                                           ContentValues cv = new ContentValues();

                                           try (Cursor cursor = database.query("SELECT * FROM MESSAGE")) {
                                               if (cursor != null) {
                                                   if (cursor.moveToFirst()) {
                                                       do {
                                                           int issender = cursor.getInt(cursor.getColumnIndex("IsSender"));
                                                           if (issender == 20 || issender == 21 || issender == 22 || issender == 23 || issender == 24 || issender == 25) {

                                                               //check if current longitude column is null
                                                               String msgLong = cursor.getString(cursor.getColumnIndex("MsgLongitude"));

                                                               if (msgLong != null) { //not null
                                                                   //check for media status 100
                                                                   if (cursor.getInt(cursor.getColumnIndex("MediaStatus")) == 100) {
                                                                       if (msgLong.substring(0, 1).equals("[")) { //byte - remove
                                                                           cv.put("MsgLongitude", "");

                                                                       } else { //correct path, migrate
                                                                           cv.put("MsgInfoUrl", msgLong);
                                                                           cv.put("MsgLongitude", "");

                                                                       }
                                                                   } else {
                                                                       cv.put("MsgLongitude", "");
                                                                   }
                                                                   database.update("MESSAGE", SQLiteDatabase.CONFLICT_REPLACE, cv,
                                                                           "MsgRow = ?", new String[]{cursor.getString(cursor.getColumnIndex("MsgRow"))});
                                                               }
                                                           }
                                                       } while (cursor.moveToNext());
                                                   }
                                                   cursor.close();
                                               }
                                           }
                                       }
                                   }, new Migration(3, 4) {
                                       @Override
                                       public void migrate(@NonNull SupportSQLiteDatabase database) {
                                           //add columns to APPT table
                                           // v4 changelog:
                                           // Appointment:
                                           // +ApptExpiring
                                           // +ApptDesc
                                           // +ApptNotiBadgeTitle
                                           // +ApptNotiBadgeDate
                                           // +ApptNotiBadgeLoc
                                           // +ApptNotiBadgeDesc

                                           database.execSQL("ALTER TABLE APPOINTMENT ADD COLUMN ApptExpiring INTEGER DEFAULT 0");
                                           database.execSQL("ALTER TABLE APPOINTMENT ADD COLUMN ApptNotiBadgeTitle INTEGER DEFAULT 0");
                                           database.execSQL("ALTER TABLE APPOINTMENT ADD COLUMN ApptNotiBadgeDate INTEGER DEFAULT 0");
                                           database.execSQL("ALTER TABLE APPOINTMENT ADD COLUMN ApptNotiBadgeLoc INTEGER DEFAULT 0");
                                           database.execSQL("ALTER TABLE APPOINTMENT ADD COLUMN ApptNotiBadgeDesc INTEGER DEFAULT 0");
                                           database.execSQL("ALTER TABLE APPOINTMENT ADD COLUMN ApptDesc TEXT");

                                           //create new column called APPTHISTORY
                                           database.execSQL("CREATE TABLE APPOINTMENT_HIST (ApptH_Row INTEGER PRIMARY KEY AUTOINCREMENT, " +
                                                   "ApptH_ID TEXT, ApptH_Jid TEXT, ApptH_Title TEXT, ApptH_Date INTEGER DEFAULT 0, " +
                                                   "ApptH_Time INTEGER DEFAULT 0, ApptH_Location TEXT, ApptH_Latitude TEXT, " +
                                                   "ApptH_Longitude TEXT, ApptH_MapURL TEXT, ApptH_Desc TEXT, " +
                                                   "ApptH_Self_Status INTEGER DEFAULT 0, ApptH_Friend_Status INTEGER DEFAULT 2, " +
                                                   "ApptH_ResID TEXT, ApptH_ResImgURL TEXT, ApptH_isDeleted TEXT)");
                                       }
                                   }, new Migration(4, 5) {
                                       @Override
                                       public void migrate(@NonNull SupportSQLiteDatabase database) {
                                           //push notification
                                           database.execSQL("CREATE TABLE PushNotification(pnRow INTEGER PRIMARY KEY AUTOINCREMENT, " +
                                                   "senderJid TEXT, groupJid TEXT, senderName TEXT,pushBody TEXT,pushType TEXT,isGroup INTEGER)");

                                           //reward
                                           database.execSQL("CREATE TABLE REWARD(RewardRow INTEGER PRIMARY KEY AUTOINCREMENT, " +
                                                   "RewardID TEXT, RewardImgURL TEXT, RewardTitle TEXT, RewardDesc TEXT," +
                                                   "RewardDateEnd TEXT, RewardResID TEXT, RewardRedeemID TEXT)");

                                           //add ApptReminderTime to APPT table
                                           database.execSQL("ALTER TABLE APPOINTMENT ADD COLUMN ApptReminderTime INTEGER DEFAULT -1");
                                       }
                                   }, new Migration(5, 6) {
                                       @Override
                                       public void migrate(@NonNull SupportSQLiteDatabase database) {
                                           //ibrahim - new work manager table for reminders
                                           database.execSQL("CREATE TABLE ApptWorkUUID(ApptWorkRow INTEGER PRIMARY KEY AUTOINCREMENT, " +
                                                   "ApptID TEXT, reminderUUID TEXT, exactUUID TEXT, deleteUUID TEXT)");

                                           //ryan - new column in CR for img resource id
                                           database.execSQL("ALTER TABLE CONTACT_ROSTER ADD COLUMN CRResourceID TEXT");
                                       }
                                   }, new Migration(6, 7) {
                                       @Override
                                       public void migrate(@NonNull SupportSQLiteDatabase database) {
                                           //ibrahim
                                           database.execSQL("ALTER TABLE PushNotification ADD COLUMN apptId TEXT");
                                       }
                                   }, new Migration(7, 8) {
                                       @Override
                                       public void migrate(@NonNull SupportSQLiteDatabase database) {
                                           //ryan
                                           database.execSQL("CREATE TABLE IF NOT EXISTS MediaWorker(MediaWorkerMessageRowID TEXT PRIMARY KEY Not Null, " +
                                                   "MediaWorkerWorkerEnqueueID TEXT)");
                                       }
                                   }
                    ).build();
        }

        return INSTANCE;
    }

    public static void destroyInstance() {
        INSTANCE = null;
    }

    //to be used in future for loading chatlist using room - currently still using cursorloader
//    public abstract ChatTabDao getQuery();

    public abstract GeneralSelectQuery selectQuery();

    public abstract GeneralInsertQuery insertQuery();

    public abstract GeneralUpdateQuery updateQuery();

    public abstract GeneralDeleteQuery deleteQuery();

}