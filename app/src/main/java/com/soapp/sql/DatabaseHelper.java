package com.soapp.sql;

import android.Manifest;
import android.app.AlarmManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.provider.ContactsContract;
import android.util.Base64;
import android.util.Log;
import android.widget.Toast;

import com.soapp.R;
import com.soapp.SoappApi.ApiModel.AppointmentMemStatus;
import com.soapp.SoappApi.ApiModel.DeleteRoomMemberModel;
import com.soapp.SoappApi.ApiModel.GetGrpApptModel;
import com.soapp.SoappApi.ApiModel.GetMemberListModel;
import com.soapp.SoappApi.ApiModel.GetRoomRepo;
import com.soapp.SoappApi.ApiModel.GetUserRepo;
import com.soapp.SoappApi.ApiModel.Resource1v3Model;
import com.soapp.SoappApi.ApiModel.RestaurantInfo;
import com.soapp.SoappApi.ApiModel.SPgetAllResModel;
import com.soapp.SoappApi.Interface.CreateApptForExistGrp;
import com.soapp.SoappApi.Interface.DeleteRoomMember;
import com.soapp.SoappApi.Interface.DownloadFromUrlInterface;
import com.soapp.SoappApi.Interface.GetProfileThumb;
import com.soapp.SoappApi.Interface.GetRoomProfile;
import com.soapp.SoappApi.Interface.IndiAPIInterface;
import com.soapp.SoappApi.Interface.Resource1v3Interface;
import com.soapp.SoappApi.Interface.RestaurantDetails;
import com.soapp.SoappApi.RetrofitAPIClient;
import com.soapp.SoappModel.AddMember;
import com.soapp.SoappModel.ResMyBookingModel;
import com.soapp.SoappModel.ResRatingModel;
import com.soapp.SoappModel.TrackingModel;
import com.soapp.WorkManager.DeleteApptWorker;
import com.soapp.WorkManager.ExactTimeWorker;
import com.soapp.WorkManager.ReminderWorker;
import com.soapp.chat_class.group_chat.GroupChatLog;
import com.soapp.chat_class.single_chat.IndiChatLog;
import com.soapp.global.ChatHelper;
import com.soapp.global.CheckInternetAvaibility;
import com.soapp.global.DateTime.DateTimePickerHelper.DateHelper.CalendarDay;
import com.soapp.global.DecryptionHelper;
import com.soapp.global.EncryptionHelper;
import com.soapp.global.GlobalVariables;
import com.soapp.global.ImageHelper;
import com.soapp.global.MiscHelper;
import com.soapp.global.Preferences;
import com.soapp.global.SoundHelper;
import com.soapp.global.StateCheck;
import com.soapp.home.Home;
import com.soapp.schedule_class.Schedulelist.ScheduleTab;
import com.soapp.schedule_class.group_appt.GroupScheLog;
import com.soapp.schedule_class.single_appt.IndiScheLog;
import com.soapp.setup.Soapp;
import com.soapp.soapp_tab.bookinglist.ResBookingsController;
import com.soapp.sql.room.GeneralSelectQuery;
import com.soapp.sql.room.SoappDatabase;
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
import com.soapp.sql.room.entity.Restaurant;
import com.soapp.sql.room.entity.Reward;
import com.soapp.xmpp.GlobalMessageHelper.GlobalHeaderHelper;
import com.soapp.xmpp.GroupChatHelper.GroupChatStanza;
import com.soapp.xmpp.PubsubHelper.PubsubNodeCall;
import com.soapp.xmpp.SingleChatHelper.SingleChatStanza;
import com.soapp.xmpp.SmackHelper;

import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.app.RemoteInput;
import androidx.sqlite.db.SupportSQLiteDatabase;
import androidx.work.BackoffPolicy;
import androidx.work.Data;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;
import androidx.work.WorkRequest;
import io.reactivex.Maybe;
import io.reactivex.MaybeObserver;
import io.reactivex.MaybeSource;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;
import me.leolin.shortcutbadger.ShortcutBadgeException;
import me.leolin.shortcutbadger.ShortcutBadger;
import okhttp3.MediaType;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/* Created by chang on 24/07/2017 */

//=============================== TABLE OF CONTENTS ===============================//
//
//  [J00] RDB-RELATED [J00E] // for updating RDB to refresh UI etc.
////      [K00_01] update-related [K00_01E]
////      [K00_01] delete-related [K00_01E]
//
//  [J01] CHAT-RELATED [J01E] <Ibrahim DONE>
//      [K01_01] Indi-related [K01_01E]
//          [K01_01_01] Indi-Incoming [K01_01_01E]
//          [K01_01_02] Indi-Outgoing [K01_01_02E]
//      [K01_02] Group-related [K01_02E]
//          [K01_02_01] Group-Incoming [K01_02_01E]
//          [K01_02_02] Group-Outgoing [K01_02_02E]
//      [K01_03] Seen-related [K01_03E]
//      [K01_99] Others [K01_99E]
//
//  [J02] APPOINTMENT-RELATED [J02E] <JAY>
//      [K02_01] Outgoing-related [K02_01E]
//      [K02_02] Incoming-related [K02_02E]
//
//  [J03] GROUP-DETAIL-RELATED [J03E] <Ibrahim>
//      [K03_01] Incoming-Related [K03_01E]
//      [K03_02] Outgoing-Related [K03_02E]
//      [K03_99] Others [K03_99E]
//
//  [J04] GET-FROM-DB-RELATED [J04E] <Ibrahim DONE>
//      [K04_01] Image-related [K04_01E]
//      [K04_02] Check-Exist-related [K04_02E]
//      [K04_03] Single-Info-related [K04_03E]
//          [K04_03_01] appointment [K04_03_01E]

//      [K04_04] Multi-Info-related [K04_04E]
//      [K04_05] Load-related [K04_05E]
//
//  [J05] UPDATE-DB-RELATED [J06E]
//      [K05_01] Contact-Roster-related [K05_01E]
//      [K05_02] Roster-indiScheList-related [K05_02E]
//      [K05_03] Message-related [K05_03E]
//      [K05_04] GroupMem-related [K05_04E]
//      [K05_05] Download-related [K05_05E]
//      [K05_06] Contact-related [K05_06E]
//      [K05_99] Others [K05_99E]
//
//  [J06] MISC-TASK-RELATED [J06E]
//      [K06_01] Position-related [K06_01E]
//          [K06_01_05] IndiScheLog-Related [K06_01_05E]
//          [K06_01_06] GrpScheLog-Related [K06_01_06E]
//          [K06_01_07] GrpMembers-Related [K06_01_07E]
//
//      [K06_02] Refresh-related [K06_02E]
//      [K06_03] Delete/Clear-related [K06_03E]
//
//  [J07] NOTIFICATION-RELATED [J07E]
//      [K07_01] Push-related [K07_01E]
//      [K07_01] Local-related [K07_01E]
//
//  [J99] OTHERS [J99E]
//          [K99_01] Profile-related [K99_01E]
//          [K99_02] Booking-related [K99_02E]
//          [K99_03] Badge-related [K99_03E]
//          [K99_05] Tracking-related [K99_05E]
//          [K99_06] Restaurant-related [K99_06E]
//
//=============================== [END] TABLE OF CONTENTS ===============================//

public class DatabaseHelper {
    //CHAT_LIST
    public static final String CL_TABLE_NAME = "CHAT_LIST";
    public static final String CL_COLUMN_ROW = "ChatRow";
    public static final String CL_COLUMN_JID = "ChatJid";
    public static final String CL_COLUMN_DATE = "LastDateReceived";
    public static final String CL_COLUMN_LASTMSG = "LastDispMsg";
    public static final String CL_COLUMN_TYPINGSTATUS = "TypingStatus";
    public static final String CL_COLUMN_NOTIBADGE = "NotiBadge";
    public static final String CL_COLUMN_LASTSENDERNAME = "LastSenderName";
    public static final String CL_COLUMN_ONLINESTATUS = "OnlineStatus";
    public static final String CL_COLUMN_TYPINGNAME = "TypingName";
    public static final String CL_COLUMN_ADMIN_SELF = "Admin_Self";

    //CONTACT_ROSTER
    public static final String CR_TABLE_NAME = "CONTACT_ROSTER";
    public static final String CR_COLUMN_JID = "ContactJid";
    public static final String CR_COLUMN_DISPLAYNAME = "DisplayName";
    public static final String CR_COLUMN_PHOTOURL = "PhotoUrl";
    public static final String CR_COLUMN_PROFILEPHOTO = "ProfilePhoto";
    public static final String CR_COLUMN_PROFILEFULL = "ProfileFull";
    public static final String CR_COLUMN_PHONENUMBER = "PhoneNumber";
    public static final String CR_COLUMN_PHONENAME = "PhoneName";
    public static final String CR_COLUMN_SELECTED = "Selected";
    public static final String CR_COLUMN_DISABLEDSTATUS = "DisabledStatus";

    //APPOINTMENT
    public static final String A_TABLE_NAME = "APPOINTMENT";
    public static final String A_COLUMN_ID = "ApptID";
    public static final String A_COLUMN_JID = "ApptJid";
    public static final String A_COLUMN_TITLE = "ApptTitle";
    public static final String A_COLUMN_DATE = "ApptDate";
    public static final String A_COLUMN_TIME = "ApptTime";
    public static final String A_COLUMN_DESC = "ApptDesc";
    public static final String A_COLUMN_APPT_NOTI_BADGE = "ApptNotiBadge";
    public static final String A_COLUMN_APPT_NOTI_TITLE = "ApptNotiBadgeTitle";
    public static final String A_COLUMN_APPT_NOTI_DATE = "ApptNotiBadgeDate";
    public static final String A_COLUMN_APPT_NOTI_LOC = "ApptNotiBadgeLoc";
    public static final String A_COLUMN_APPT_NOTI_DESC = "ApptNotiBadgeDesc";
    public static final String A_COLUMN_LOCATION = "ApptLocation";
    public static final String A_COLUMN_LATITUDE = "ApptLatitude";
    public static final String A_COLUMN_LONGITUDE = "ApptLongitude";
    public static final String A_COLUMN_LOCMAPURL = "ApptMapURL";
    public static final String A_COLUMN_FRIENDSTATUS = "Friend_Status";
    public static final String A_COLUMN_RESID = "ApptResID";
    public static final String A_COLUMN_SELFSTATUS = "Self_Status";
    public static final String A_COLUMN_RESIMGURL = "ApptResImgURL";
    public static final String A_COLUMN_REMINDER_TIME = "ApptReminderTime";
    public static final String A_COLUMN_REMINDER_UUID = "ApptReminderUUID";

    //APPOINTMENT_HIST
    public static final String AH_TABLE_NAME = "APPOINTMENT_HIST";
    public static final String AH_COLUMN_ID = "ApptH_ID";
    public static final String AH_COLUMN_JID = "ApptH_Jid";
    public static final String AH_COLUMN_TITLE = "ApptH_Title";
    public static final String AH_COLUMN_DATE = "ApptH_Date";
    public static final String AH_COLUMN_TIME = "ApptH_Time";
    public static final String AH_COLUMN_DESC = "ApptH_Desc";
    public static final String AH_COLUMN_LOCATION = "ApptH_Location";
    public static final String AH_COLUMN_LATITUDE = "ApptH_Latitude";
    public static final String AH_COLUMN_LONGITUDE = "ApptH_Longitude";
    public static final String AH_COLUMN_LOCMAPURL = "ApptH_MapURL";
    public static final String AH_COLUMN_FRIENDSTATUS = "ApptH_Friend_Status";
    public static final String AH_COLUMN_RESID = "ApptH_ResID";
    public static final String AH_COLUMN_SELFSTATUS = "ApptH_Self_Status";
    public static final String AH_COLUMN_RESIMGURL = "ApptH_ResImgURL";
    public static final String AH_COLUMN_ISDELETED = "ApptH_isDeleted";

    //APPTWORKUUID
    public static final String AWID_TABLE_APPTWORKUUID = "ApptWorkUUID";
    public static final String AWID_COLUMN_APPTWORKROW = "ApptWorkRow";
    public static final String AWID_COLUMN_APPTID = "ApptID";
    public static final String AWID_COLUMN_REMINDERUUID = "reminderUUID";
    public static final String AWID_COLUMN_EXACTUUID = "exactUUID";
    public static final String AWID_COLUMN_DELETEUUID = "deleteUUID";

    //GROUPMEM
    public static final String GM_TABLE_NAME = "GROUPMEM";
    public static final String GM_COLUMN_ROOMJID = "RoomJid";
    public static final String GM_COLUMN_MEMBERJID = "MemberJid";
    public static final String GM_COLUMN_ADMIN = "Admin";
    public static final String GM_COLUMN_TEXTCOLOR = "TextColor";
    public static final String GM_COLUMN_APPTSTATUS = "Appt_Status";
    public static final String GM_COLUMN_LATITUDE = "Latitude";
    public static final String GM_COLUMN_LONGITUDE = "Longitude";

    //GROUPMEM_STATUS
    public static final String GMS_TABLE_NAME = "GROUPMEM_STATUS";
    public static final String GMS_COLUMN_ROOMJID = "GMSRoomJid";
    public static final String GMS_COLUMN_MEMBERJID = "GMSMemberJid";
    public static final String GMS_COLUMN_APPTID = "GMSApptID";
    public static final String GMS_COLUMN_APPTSTATUS = "GMSStatus";

    //MESSAGE
    public static final String MSG_TABLE_NAME = "MESSAGE";
    public static final String MSG_ROW = "MsgRow";
    public static final String MSG_JID = "MsgJid";
    public static final String MSG_SENDERJID = "SenderJid";
    public static final String MSG_CHATMARKER = "ChatMarker";
    public static final String MSG_ISSENDER = "IsSender";
    public static final String MSG_MSGDATE = "MsgDate";
    public static final String MSG_MSGUNIQUEID = "MsgUniqueId";
    public static final String MSG_MSGDATA = "MsgData";
    public static final String MSG_MSGINFOID = "MsgInfoId";
    public static final String MSG_MSGINFOURL = "MsgInfoUrl";
    public static final String MSG_MSGLATITUDE = "MsgLatitude";
    public static final String MSG_MSGLONGITUDE = "MsgLongitude";
    public static final String MSG_MEDIASTATUS = "MediaStatus";
    public static final String MSG_MSGOFFLINE = "MsgOffline";
    public static final String MSG_CONTACT = "contact";
    public static final String MSG_CONTACTNAME = "contactname";
    public static final String MSG_CONTACTNUMBER = "contactnumber";

    //RESTAURANT
    public static final String RES_TABLE_NAME = "RESTAURANT";
    public static final String RES_ID = "ResID";
    public static final String RES_TITLE = "ResTitle";
    public static final String RES_LOCATION = "ResLocation";
    public static final String RES_STATE = "ResState";
    public static final String RES_MAINCUISINE = "ResMainCuisine";
    public static final String RES_LATITUDE = "ResLatitude";
    public static final String RES_LONGITUDE = "ResLongitude";
    public static final String RES_IMAGEURL = "ResImageUrl";
    public static final String RES_PHONENUM = "ResPhoneNum";
    public static final String RES_OVERALLRATING = "ResOverallRating";
    public static final String RES_FAVOURITED = "ResFavourited";

    //BOOKING
    public static final String BOOK_TABLE_NAME = "BOOKING";
    public static final String BOOK_ID = "BookingId";
    public static final String BOOK_JID = "BookerJid";
    public static final String BOOK_ResId = "BookingResId";
    public static final String BOOK_Status = "BookingStatus";
    public static final String BOOK_QRCode = "BookingQRCode";
    public static final String BOOK_Date = "BookingDate";
    public static final String BOOK_Time = "BookingTime";
    public static final String BOOK_ResOwnerJid = "ResOwnerJid";
    public static final String BOOK_Pax = "BookingPax";
    public static final String BOOK_Promo = "BookingPromo";
    public static final String BOOK_Attempts = "BookingAttempts";
    public static final String BOOK_SharedJid = "BookingSharedJid";

    //REWARD
    public static final String REWARD_TABLE_NAME = "REWARD";
    public static final String REWARD_ID = "RewardID";
    public static final String REWARD_IMG_URL = "RewardImgURL";
    public static final String REWARD_TITLE = "RewardTitle";
    public static final String REWARD_DESCRIPTION = "RewardDesc";
    public static final String REWARD_DATE_END = "RewardDateEnd";
    public static final String REWARD_RESTAURANT_ID = "RewardResID";
    public static final String REWARD_REDEMPTION_ID = "RewardRedeemID";

    //MEDIA WORKER
    public static final String MEDIA_WORKER_TABLE_NAME = "MEDIAWORKER";
    public static final String MEDIA_WORKER_MESSAGE_ROW_ID = "MediaWorkerMessageRowID";
    public static final String MEDIA_WORKER_WORKER_ENQUEUE_ID = "MediaWorkerWorkerEnqueueID";

    //errorlog
    private static final String EL_TABLE_NAME = "ErrorLogs";


    private static final String TAG = "wtf";

    public static int pushChatNotiCount = 0, pushScheNotiCount = 0, pushBookingNotiCount = 0;

    //singleton database
    private static DatabaseHelper sInstance;
    public final SoappDatabase rdb;
    private final SupportSQLiteDatabase sqLiteDatabase;

    //basics
    private final Context context = Soapp.getInstance().getApplicationContext();
    private final Preferences preferences = Preferences.getInstance();

    //stanza
    private final SingleChatStanza singleChatStanza = new SingleChatStanza();
    private final GroupChatStanza groupChatStanza = new GroupChatStanza();
    private final PubsubNodeCall pubsubNodeCall = new PubsubNodeCall();

    //encryption
    private final DecryptionHelper decryptionHelper = new DecryptionHelper();

    //push notification variables
    private String prevChatJID;
    private String prevScheJID;
    private String titleChat1;
    private String titleChat2;
    private String titleChat3;
    private String titleChat4;
    private String titleChat5;
    private String contentChat1;
    private String contentChat2;
    private String contentChat3;
    private String contentChat4;
    private String contentChat5;
    private String titleSche1;
    private String titleSche2;
    private String titleSche3;
    private String titleSche4;
    private String titleSche5;
    private String contentSche1;
    private String contentSche2;
    private String contentSche3;
    private String contentSche4;
    private String contentSche5;
    private String titleBooking1;
    private String titleBooking2;
    private String titleBooking3;
    private String titleBooking4;
    private String titleBooking5;
    private String contentBooking1;
    private String contentBooking2;
    private String contentBooking3;
    private String contentBooking4;
    private String contentBooking5;

    private MiscHelper miscHelper = new MiscHelper();

    public DatabaseHelper() {
        rdb = Soapp.getDatabase();

        sqLiteDatabase = rdb.getOpenHelper().getWritableDatabase();
    }

    public static synchronized DatabaseHelper getInstance() {
        if (sInstance == null) {
            sInstance = new DatabaseHelper();
        }
        return sInstance;
    }

    /*  ============================== [J00 - RDB-RELATED ============================== */
    /*///////// [K00_01] RDB: update-related ///////////////*/
    //update rdb 1 column
    public void updateRDB1Col(String tableName, ContentValues cv, String colName1, String value1) {
        rdb.runInTransaction((Runnable) () -> sqLiteDatabase.update(tableName, SQLiteDatabase.CONFLICT_IGNORE,
                cv, String.format("%s = ?", colName1), new String[]{value1})
        );
    }

    //update rdb 2 columns
    public void updateRDB2Col(String tableName, ContentValues cv, String colName1, String colName2,
                              String value1, String value2) {
        rdb.runInTransaction((Runnable) () -> sqLiteDatabase.update(tableName, SQLiteDatabase.CONFLICT_IGNORE,
                cv, String.format("%s = ? AND %s = ?", colName1, colName2), new String[]{value1, value2}));
    }

    //update rdb 2 columns + 1 IS NULL or IS NOT NULL
    public void updateRDB2Col1isNull(String tableName, ContentValues cv, String colName1, String colName2,
                                     String colName3, String value1, String value2, boolean isNull) {
        rdb.runInTransaction(() -> {
            if (isNull) {
                sqLiteDatabase.update(tableName, SQLiteDatabase.CONFLICT_IGNORE, cv, String.format("%s = ? AND %s = ? AND %s IS NULL",
                        colName1, colName2, colName3), new String[]{value1, value2});
            } else {
                sqLiteDatabase.update(tableName, SQLiteDatabase.CONFLICT_IGNORE, cv, String.format("%s = ? AND %s = ? AND %s IS NOT NULL",
                        colName1, colName2, colName3), new String[]{value1, value2});
            }
        });
    }

    //update rdb 3 columns
    public void updateRDB3Col(String tableName, ContentValues cv, String colName1, String colName2,
                              String colName3, String value1, String value2, String value3) {
        rdb.runInTransaction((Runnable) () -> sqLiteDatabase.update(tableName, SQLiteDatabase.CONFLICT_IGNORE, cv, String.format("%s = ? AND %s = ? AND %s = ?",
                colName1, colName2, colName3), new String[]{value1, value2, value3}));
    }
    /*///////// [K00_01_E] [END] RDB: update-related ///////////////*/


    /*///////// [K01_02] RDB: delete-related ///////////////*/
    //delete rdb 1 column
    public void deleteRDB1Col(String tableName, String colName1, String value1) {
        rdb.runInTransaction((Runnable) () -> sqLiteDatabase.delete(tableName, String.format("%s = ?", colName1), new String[]{value1}));
    }

    //delete rdb 2 columns
    public void deleteRDB2Col(String tableName, String colName1, String colName2,
                              String value1, String value2) {
        rdb.runInTransaction((Runnable) () -> sqLiteDatabase.delete(tableName, String.format("%s = ? AND %s = ?", colName1, colName2),
                new String[]{value1, value2}));
    }

    //delete rdb 2 columns + 1 IS NULL or IS NOT NULL
    public void deleteRDB2Col1isNull(String tableName, String colName1, String colName2, String colName3,
                                     String value1, String value2, boolean isNull) {
        rdb.runInTransaction(() -> {
            if (isNull) {
                sqLiteDatabase.delete(tableName, String.format("%s = ? AND %s = ? AND %s IS NULL", colName1,
                        colName2, colName3), new String[]{value1, value2});
            } else {
                sqLiteDatabase.delete(tableName, String.format("%s = ? AND %s = ? AND %s IS NOT NULL", colName1,
                        colName2, colName3), new String[]{value1, value2});
            }
        });
    }
    /*///////// [K01_02_E] [END] RDB: delete-related ///////////////*/
    /*  ============================== [J00E - [END] RDB-UPDATE-RELATED ============================== */



    /*  ============================== [J01 - CHAT-RELATED (messages)] ============================== */
    /*///////// [K01_01] CHAT: Indi-related ///////////////*/
    /* ----------- [K01_01_01] Indi-Incoming ----------- */

    //[DONE CR] when receive "NewID" in single chat (when your friend registers on Soapp)
    public void incomingNewSoappUser(String jid, String uniqueID) {
        //check if contacts permission allowed
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) { //more than M, need check permission
            if ((PackageManager.PERMISSION_GRANTED == context.checkSelfPermission(Manifest.permission.WRITE_CONTACTS)) &&
                    (PackageManager.PERMISSION_GRANTED == context.checkSelfPermission(Manifest.permission.READ_CONTACTS))) {
                //post to retro to get phone number
                retroGetNewSoapperProfile(jid, uniqueID);

            } else { //permission denied, don't do anything
                //send acknowledgement stanza back to server
                singleChatStanza.SoappAckStanza(jid, uniqueID);
            }
        } else { //lower than M, no need check - need to handle exception soon
            //post to retro to get phone number
            retroGetNewSoapperProfile(jid, uniqueID);
        }
    }

    //retro for posting jid to get profile of user
    private void retroGetNewSoapperProfile(String jid, String uniqueID) {
        if (getPhoneNameExist(jid) == 0) { //user NOT saved in CR yet (no phone name in CR)
            String access_token = preferences.getValue(context, GlobalVariables.STRPREF_ACCESS_TOKEN);

            //build retrofit
            String size = miscHelper.getDeviceDensity(context);
            IndiAPIInterface indiAPIInterface = RetrofitAPIClient.getClient().create(IndiAPIInterface.class);
            Call<GetUserRepo> call = indiAPIInterface.getIndiProfile(jid, size, "Bearer " + access_token);

            call.enqueue(new Callback<GetUserRepo>() {
                @Override
                public void onResponse(Call<GetUserRepo> call, Response<GetUserRepo> response) {
                    if (!response.isSuccessful()) {
                        new MiscHelper().retroLogUnsuc(response, "incomingNewSoappUser ", "JAY");
                        return;
                    }

                    //check if phone number exists in phonebook
                    //server always returns phone number starting with +60
                    String phoneNumber = String.valueOf(response.body().getPhone());
                    String phoneName = checkPhoneBookExists(phoneNumber);

                    if (phoneName == null) { //doesn't exist, try remove +6
                        //remove +6 from front (first 2 characters)
                        phoneNumber = phoneNumber.substring(2);
                        phoneName = checkPhoneBookExists(phoneNumber);

                        if (phoneName == null) { //still doesn't exist, remove one more
                            //remove 0 from front (first characters)
                            phoneNumber = phoneNumber.substring(1);
                            phoneName = checkPhoneBookExists(phoneNumber);

                            if (phoneName == null) { //confirmed not exist, ack
                                singleChatStanza.SoappAckStanza(jid, uniqueID);
                                return;
                            }
                        }
                    }

                    //exists in phonebook
                    String displayName = String.valueOf(response.body().getName());
                    String imageURL = String.valueOf(response.body().getResource_url());
                    String base64ImgData = String.valueOf(response.body().getImage_data());

                    //got img data from server
                    byte[] userImgByte = null;
                    if (base64ImgData != null && !base64ImgData.equals("Empty")) {
                        userImgByte = Base64.decode(base64ImgData, Base64.DEFAULT);
                    }

                    //add user profile to CR using phoneName
                    addNewContactToCR(phoneName, phoneNumber, jid, displayName, imageURL, userImgByte);

                    //send acknowledgement stanza back to server
                    singleChatStanza.SoappAckStanza(jid, uniqueID);
                }

                @Override
                public void onFailure(Call<GetUserRepo> call, Throwable t) {
                    new MiscHelper().retroLogFailure(t, "incomingNewSoappUser ", "JAY");
                }
            });
        } else { //user exists in CR, just ack
            singleChatStanza.SoappAckStanza(jid, uniqueID);
        }
    }

    //update ChatList for when msg incoming/outgoing (indi and grp)
    private void updateChatListLastMessage(String jid, String Body, String lastSender, Long Date,
                                           boolean incoming, boolean playSound, String uniqueID) {
        if (getChatListExist(jid) > 0) {
            ContentValues cvCL = new ContentValues();

            cvCL.put(CL_COLUMN_DATE, Date);

            //need to trim for newline cases
            cvCL.put(CL_COLUMN_LASTMSG, Body);
            cvCL.put(CL_COLUMN_LASTSENDERNAME, lastSender);

            //currently not catering for multi-typing conflicts
            if (incoming) { //only need update typing status if incoming
                cvCL.put(CL_COLUMN_TYPINGSTATUS, 0);
            }

            updateRDB1Col(CL_TABLE_NAME, cvCL, CL_COLUMN_JID, jid);

            playInMsgSoundUI(playSound);

            //acknowledge for single only, grp ack has to be done outside
            if (uniqueID != null) {
                singleChatStanza.SoappAckStanza(jid, uniqueID);
            }
        } else {
            if (!incoming) {
                createNewIndiChat(jid, Body, Date, false);
            } else {
                incomingNewIndiChat(jid, Body, Date, uniqueID, playSound);
            }
        }

    }

    //update media status to MESSAGE based on row id and return results
    public int updateMsgMediaStatusReturn(int mediaStatus, String rowID) {
        ContentValues cvMSG = new ContentValues();

        cvMSG.put("MediaStatus", mediaStatus);

        return rdb.runInTransaction(() -> sqLiteDatabase.update(MSG_TABLE_NAME, SQLiteDatabase.CONFLICT_IGNORE,
                cvMSG, String.format("%s = ?", MSG_ROW), new String[]{rowID}));
    }

    //[DONE CR] Incoming Single Message Stanza To SQlitedatabase for both Rosterlist & Message
    public void MessageInputDatabase(String Jid, String Body, String Id, long Date, boolean playSound) {
        Log.d(TAG, "MessageInputDatabase() called with: Jid = [" + Jid + "], Body = [" + Body + "], Id = [" + Id + "], Date = [" + Date + "], playSound = [" + playSound + "]");
        ContentValues cvMSG = new ContentValues();

        cvMSG.put(MSG_JID, Jid);
        cvMSG.put(MSG_ISSENDER, 11);
        cvMSG.put(MSG_MSGDATA, Body);
        cvMSG.put(MSG_MSGUNIQUEID, Id);
        cvMSG.put(MSG_MSGDATE, Date);
        cvMSG.put(MSG_MSGOFFLINE, 0);

        rdb.insertQuery().insertMessage(Message.fromContentValues(cvMSG));

        addChatBadgeUnread(Jid, Date);

        updateChatListLastMessage(Jid, Body, getNameFromContactRoster(Jid), Date, true, playSound, Id);
    }

    public void replyInputDatabase(String Jid, String Body, String Id, long Date, boolean playSound, String replyid, String replytype) {

        ContentValues cvMSG = new ContentValues();
        cvMSG.put(MSG_JID, Jid);
        cvMSG.put(MSG_MSGDATA, Body);
        cvMSG.put(MSG_MSGUNIQUEID, Id);
        cvMSG.put(MSG_MSGDATE, Date);
        cvMSG.put(MSG_MSGOFFLINE, 0);
        cvMSG.put(MSG_MSGINFOID, replyid);

        if (replytype.equals("1")) {
            cvMSG.put(MSG_ISSENDER, 61);
        } else {
            cvMSG.put(MSG_ISSENDER, 63);
        }

        rdb.insertQuery().insertMessage(Message.fromContentValues(cvMSG));

        addChatBadgeUnread(Jid, Date);

        updateChatListLastMessage(Jid, Body, getNameFromContactRoster(Jid), Date, true, playSound, Id);
    }

    //[DONE CR] Incoming Single Contact
    public void ContactInputDatabase(final String Jid, final String Id, final long Date, final
    String contactname, final String contactnumber, final boolean playSound) {
        ContentValues cvMSG = new ContentValues();

        cvMSG.put(MSG_JID, Jid);
        cvMSG.put(MSG_ISSENDER, 33);
        cvMSG.put(MSG_MSGDATA, "");
        cvMSG.put(MSG_CONTACT, 0);
        cvMSG.put(MSG_MSGUNIQUEID, Id);
        cvMSG.put(MSG_MSGDATE, Date);
        if (contactname != null) {
            cvMSG.put(MSG_CONTACTNAME, contactname);
        }
        if (contactnumber != null) {
            cvMSG.put(MSG_CONTACTNUMBER, contactnumber);
        }
        rdb.insertQuery().insertMessage(Message.fromContentValues(cvMSG));

        addChatBadgeUnread(Jid, Date);

        updateChatListLastMessage(Jid, context.getString(R.string.contact_received), getNameFromContactRoster(Jid),
                Date, true, playSound, Id);
    }

    //[DONE CR] Incoming Single Map Stanza To SQlitedatabase fo both Rosterlist & Message
    public void MapInputDatabase(final String Jid, final String Body, final String Id, final
    long Date, final String Lat, final String Long, final boolean playSound, String placeName, String address) {
        ContentValues cvMSG = new ContentValues();

        cvMSG.put(MSG_JID, Jid);
        cvMSG.put(MSG_ISSENDER, 31);
        cvMSG.put(MSG_MSGINFOURL, Body);
        cvMSG.put(MSG_MSGUNIQUEID, Id);
        cvMSG.put(MSG_MSGDATE, Date);
        cvMSG.put(MSG_MSGLATITUDE, Lat);
        cvMSG.put(MSG_MSGLONGITUDE, Long);
        cvMSG.put(MSG_MSGINFOID, placeName);
        cvMSG.put(MSG_MSGDATA, address);

        rdb.insertQuery().insertMessage(Message.fromContentValues(cvMSG));

        addChatBadgeUnread(Jid, Date);

        updateChatListLastMessage(Jid, context.getString(R.string.location_received), getNameFromContactRoster(Jid),
                Date, true, playSound, Id);
    }

    //incoming single video
    public void VideoInputDatabase(final String Jid, final String resourceID, final String uniqueId, final
    long Date, String orientation, final boolean playSound, String video_thumb_base64) {
        String access_token = preferences.getValue(context, GlobalVariables.STRPREF_ACCESS_TOKEN);
        boolean isNeedDowload = checkIfNeedDownload("video");

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (PackageManager.PERMISSION_DENIED == context.checkCallingOrSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                isNeedDowload = false;
            }
        }
        ContentValues cvMSG = new ContentValues();
        cvMSG.put(MSG_JID, Jid);
        cvMSG.put(MSG_ISSENDER, 25);
        cvMSG.put(MSG_MSGINFOID, resourceID);
        cvMSG.put(MSG_MSGUNIQUEID, uniqueId);
        cvMSG.put(MSG_MSGDATE, Date);
        cvMSG.put(MSG_MSGLATITUDE, video_thumb_base64);
        if (isNeedDowload) {
            cvMSG.put(MSG_MEDIASTATUS, 1);
        } else {
            cvMSG.put(MSG_MEDIASTATUS, -3);
        }
        long rowid = rdb.insertQuery().insertMessage(Message.fromContentValues(cvMSG));

        addChatBadgeUnread(Jid, Date);

        updateChatListLastMessage(Jid, context.getString(R.string.video_received), getNameFromContactRoster(Jid),
                Date, true, playSound, uniqueId);
        if (isNeedDowload) {
            Resource1v3Interface resource1v3Interface = RetrofitAPIClient.getClient().create(Resource1v3Interface.class);
            retrofit2.Call<Resource1v3Model> call = resource1v3Interface.getResource(resourceID, "Bearer " + access_token);
            call.enqueue(new retrofit2.Callback<Resource1v3Model>() {
                @Override
                public void onResponse(retrofit2.Call<Resource1v3Model> call, retrofit2.Response<Resource1v3Model> response) {
                    if (!response.isSuccessful()) {
                        new MiscHelper().retroLogUnsuc(response, "videoinputdatabse ", "JAY");

                        return;
                    }

                    String videoUrl = response.body().getResource_url();
                    String thumbnailBase64 = response.body().getThumbnail();
                    String str_row_id = String.valueOf(rowid);

                    ContentValues cvMSG = new ContentValues();
                    cvMSG.put(MSG_MSGINFOURL, videoUrl);

                    updateRDB1Col(MSG_TABLE_NAME, cvMSG, MSG_ROW, str_row_id);

                    convertBase64ToBimapAndSaveToFolder(thumbnailBase64, resourceID);

                    new ChatHelper().startDownloadWorker(resourceID, videoUrl, str_row_id, "video");
                }

                @Override
                public void onFailure(retrofit2.Call<Resource1v3Model> call, Throwable t) {
                    new MiscHelper().retroLogFailure(t, "VideoInputDatabase", "JAY");

                }
            });
        }
    }

    //function to save base64 to file
    public void convertBase64ToBimapAndSaveToFolder(String base64Image, String name) {
        byte[] decodedBase64String = Base64.decode(base64Image, Base64.DEFAULT);
        Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedBase64String, 0, decodedBase64String.length);
        if (decodedByte != null) {
            File thumbnailOutputFile = new File(GlobalVariables.VIDEO_THUMBNAIL_PATH + "/" + name + ".jpg");
            FileOutputStream fileOutputStream;
            try {
                fileOutputStream = new FileOutputStream(thumbnailOutputFile);
                decodedByte.compress(Bitmap.CompressFormat.JPEG, 25, fileOutputStream);
            } catch (FileNotFoundException e) {
            }
        }
    }

    //[DONE CR] Incoming Single Image
    public void ImageInputDatabase(final String Jid, final String resourceID, final String uniqueID, final
    long Date, final String orientation, final boolean playSound, final String rotation, final String image_base64) {
        String access_token = preferences.getValue(context, GlobalVariables.STRPREF_ACCESS_TOKEN);

        ContentValues cvMSG = new ContentValues();
        cvMSG.put(MSG_JID, Jid);
        cvMSG.put(MSG_ISSENDER, 21);
        cvMSG.put(MSG_MSGINFOID, resourceID);
        cvMSG.put(MSG_MSGUNIQUEID, uniqueID);
        cvMSG.put(MSG_MSGDATE, Date);
        cvMSG.put(MSG_MSGLATITUDE, image_base64);

        boolean isNeedDownload = checkIfNeedDownload("image");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (PackageManager.PERMISSION_DENIED == context.checkCallingOrSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {

                isNeedDownload = false;
            }
        }

        if (isNeedDownload) {
            cvMSG.put(MSG_MEDIASTATUS, 1);
        } else {
            cvMSG.put(MSG_MEDIASTATUS, -3);
        }
        long rowid = rdb.insertQuery().insertMessage(Message.fromContentValues(cvMSG));

        addChatBadgeUnread(Jid, Date);

        updateChatListLastMessage(Jid, context.getString(R.string.image_received), getNameFromContactRoster(Jid), Date,
                true, playSound, uniqueID);

        if (isNeedDownload) {
            Resource1v3Interface resource1v3Interface = RetrofitAPIClient.getClient().create(Resource1v3Interface.class);
            retrofit2.Call<Resource1v3Model> call = resource1v3Interface.getResource(resourceID, "Bearer " + access_token);
            call.enqueue(new retrofit2.Callback<Resource1v3Model>() {
                @Override
                public void onResponse(retrofit2.Call<Resource1v3Model> call, retrofit2.Response<Resource1v3Model> response) {
                    if (!response.isSuccessful()) {
                        new MiscHelper().retroLogUnsuc(response, "VoiceInputDatabase ", "JAY");

                        return;
                    }

                    String imageURL = response.body().getResource_url();
                    String str_row_id = String.valueOf(rowid);

                    ContentValues cvRetry = new ContentValues();
                    cvRetry.put(MSG_MSGINFOURL, imageURL);
                    updateRDB1Col(MSG_TABLE_NAME, cvRetry, MSG_ROW, str_row_id);

                    new ChatHelper().startDownloadWorker(resourceID, imageURL, str_row_id, "image");
                }

                @Override
                public void onFailure(retrofit2.Call<Resource1v3Model> call, Throwable t) {
                    new MiscHelper().retroLogFailure(t, "VoiceInputDatabase", "JAY");
                }
            });
        }
    }

    //[DONE CR] Incoming Single Audio
    public void VoiceInputDatabase(final String Jid, final String resourceID, final String uniqueID, final
    long Date, final boolean playSound) {
        String access_token = preferences.getValue(context, GlobalVariables.STRPREF_ACCESS_TOKEN);
        ContentValues cvMSG = new ContentValues();
        cvMSG.put(MSG_JID, Jid);
        cvMSG.put(MSG_ISSENDER, 23);
        cvMSG.put(MSG_MSGINFOID, resourceID);
        cvMSG.put(MSG_MSGUNIQUEID, uniqueID);
        cvMSG.put(MSG_MSGDATE, Date);
        boolean isNeedDownload = checkIfNeedDownload("audio");

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (PackageManager.PERMISSION_DENIED == context.checkCallingOrSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                isNeedDownload = false;
            }
        }

        if (isNeedDownload) {
            cvMSG.put(MSG_MEDIASTATUS, 1);
        } else {
            cvMSG.put(MSG_MEDIASTATUS, -3);
        }

        long rowId = rdb.insertQuery().insertMessage(Message.fromContentValues(cvMSG));

        addChatBadgeUnread(Jid, Date);

        updateChatListLastMessage(Jid, context.getString(R.string.audio_received), getNameFromContactRoster(Jid),
                Date, true, playSound, uniqueID);

        if (isNeedDownload) {
            try {
                Resource1v3Interface resource1v3Interface = RetrofitAPIClient.getClient().create(Resource1v3Interface.class);
                retrofit2.Call<Resource1v3Model> call = resource1v3Interface.getResource(resourceID, "Bearer " + access_token);
                call.enqueue(new retrofit2.Callback<Resource1v3Model>() {
                    @Override
                    public void onResponse(retrofit2.Call<Resource1v3Model> call, retrofit2.Response<Resource1v3Model> response) {
                        if (!response.isSuccessful()) {
                            return;
                        }
                        String audioURL = response.body().getResource_url();
                        ContentValues cvMSG = new ContentValues();
                        cvMSG.put(MSG_MSGINFOURL, audioURL);
                        String str_row_id = String.valueOf(rowId);

                        updateRDB1Col(MSG_TABLE_NAME, cvMSG, MSG_ROW, str_row_id);

                        new ChatHelper().startDownloadWorker(resourceID, audioURL, str_row_id, "audio");
                    }

                    @Override
                    public void onFailure(retrofit2.Call<Resource1v3Model> call, Throwable t) {
                    }
                });
            } catch (Exception e) {
            }
        }
    }

    //[DONE CR] Incoming Single Restaurant
    public void RestaurantInputDatabase(final String Jid, final String infoID, final String Id,
                                        final String res_title, final long date, final
                                        boolean playSound) {
        RestaurantInfo model = new RestaurantInfo(infoID);
        String access_token = preferences.getValue(context, GlobalVariables.STRPREF_ACCESS_TOKEN);

        //build retrofit
        RestaurantDetails client = RetrofitAPIClient.getClient().create(RestaurantDetails.class);
        retrofit2.Call<RestaurantInfo> call = client.nearbyRes(model, "Bearer " + access_token);

        call.enqueue(new retrofit2.Callback<RestaurantInfo>() {
            @Override
            public void onResponse(retrofit2.Call<RestaurantInfo> call, retrofit2.Response<RestaurantInfo> response) {
                if (!response.isSuccessful()) {

                    new MiscHelper().retroLogUnsuc(response, "RestaurantInputDatabase ", "JAY");

                    return;
                }

                ContentValues cvMSG = new ContentValues();
                cvMSG.put(MSG_MSGUNIQUEID, Id);
                cvMSG.put(MSG_MSGINFOID, infoID);
                cvMSG.put(MSG_JID, Jid);
                cvMSG.put(MSG_ISSENDER, 35);
                cvMSG.put(MSG_MSGDATE, date);
                if (res_title != null) {
                    cvMSG.put(MSG_MSGDATA, res_title);

                }
                final String image_id = response.body().getPropic();
                if (image_id != null) {
                    cvMSG.put(MSG_MSGINFOURL, image_id);
                }
                rdb.insertQuery().insertMessage(Message.fromContentValues(cvMSG));

                addChatBadgeUnread(Jid, date);

                updateChatListLastMessage(Jid, context.getString(R.string.restaurant_received), getNameFromContactRoster(Jid),
                        date, true, playSound, Id);
            }

            @Override
            public void onFailure(retrofit2.Call<RestaurantInfo> call, Throwable t) {
                new MiscHelper().retroLogFailure(t, "RestaurantInputDatabase", "JAY");
                Toast.makeText(context, R.string.onfailure, Toast
                        .LENGTH_SHORT).show();
            }
        });
    }

    private void incomingNewIndiChat(String jid, String allMsg, long date, String uniqueID, boolean playSound) {
        //check if incoming jid exists in CR
        if (getContactRosterExist(jid) == 0) {
            String access_token = preferences.getValue(context, GlobalVariables.STRPREF_ACCESS_TOKEN);
            String size = miscHelper.getDeviceDensity(context);

            //build retrofit
            IndiAPIInterface indiAPIInterface = RetrofitAPIClient.getClient().create(IndiAPIInterface.class);
            retrofit2.Call<GetUserRepo> call = indiAPIInterface.getIndiProfile(jid, size, "Bearer " + access_token);

            call.enqueue(new retrofit2.Callback<GetUserRepo>() {
                @Override
                public void onResponse(retrofit2.Call<GetUserRepo> call, retrofit2.Response<GetUserRepo> response) {
                    if (!response.isSuccessful()) {
                        new MiscHelper().retroLogUnsuc(response, "incomingNewIndiChat ", "JAY");

                        return;
                    }
                    final String name = String.valueOf(response.body().getName());
                    final String imageURL = String.valueOf(response.body().getResource_url());
                    final String phone = String.valueOf(response.body().getPhone());
                    final String image_data = String.valueOf(response.body().getImage_data());

                    //add new indi chat to chatlist (chattab only)
                    createNewIndiChat(jid, allMsg, date, true);

                    //add "Add to contact" button in room
                    checkAndInsertAddToContactMessage(jid, date);

                    checkAndAddUserProfileToContactRoster(phone, null, name, jid, imageURL, image_data, null);

                    //add date header
                    GlobalHeaderHelper globalHeaderHelper = new GlobalHeaderHelper();
                    globalHeaderHelper.GlobalHeaderTime(jid);

                    //send acknowledgement stanza
                    if (uniqueID != null) {
                        singleChatStanza.SoappAckStanza(jid, uniqueID);
                    }

                    playInMsgSoundUI(playSound);
                }

                @Override
                public void onFailure(retrofit2.Call<GetUserRepo> call, Throwable t) {
                    new MiscHelper().retroLogFailure(t, "incomingNewIndiChat", "JAY");
                    saveLogsToDb("incomingNewIndiChat", t.getMessage(), System.currentTimeMillis());

                }
            });

        } else { //user jid exists in contact roster, get info from there
            //add new indi chat to roster indiScheList (chattab only)
            createNewIndiChat(jid, allMsg, date, true);

            //send acknowledgement stanza
            if (uniqueID != null) {
                singleChatStanza.SoappAckStanza(jid, uniqueID);
            }

            playInMsgSoundUI(playSound);
        }
    }
    /*----------- [K01_01_01E] Indi-Incoming-END ----------- */


    //----------- [K01_01_02] Indi-Outgoing ----------- //
    //[DONE CR] Block User from chat (only indi outgoing for now)
    public void blockUnblockIndi(final String Jid, final String Body, final long date, int
            blockBoolean) {
        ContentValues cvMSG = new ContentValues();

        if (rdb.selectQuery().getMsgBlockedExist(Jid) == 0 && blockBoolean == 1) {
            cvMSG.put(MSG_JID, Jid);
            cvMSG.put(MSG_ISSENDER, 4);
            cvMSG.put(MSG_MSGDATA, Body);
            cvMSG.put(MSG_MSGDATE, date);

            rdb.insertQuery().insertMessage(Message.fromContentValues(cvMSG));
        } else {
            deleteRDB2Col(MSG_TABLE_NAME, MSG_JID, MSG_ISSENDER, Jid, "4");
        }

        if (getContactRosterExist(Jid) == 1) {
            ContentValues cvCR = new ContentValues();

            cvCR.put(CR_COLUMN_DISABLEDSTATUS, blockBoolean);

            updateRDB1Col(CR_TABLE_NAME, cvCR, CR_COLUMN_JID, Jid);

        }

        updateChatListLastMessage(Jid, Body, null, date, false, false, null);

    }

    //[DONE CR] Outgoing Single Message Stanza To SQlitedatabase for both Rosterlist & Message
    public void MessageOutputDatabase(final String Jid, final String Body, final String uniqueID, final
    long date) {
        //get chat marker based on jid from messagestatus in rosterlist
        final int chatMarker = getOnlineStatus(Jid);

        ContentValues cvMSG = new ContentValues();

        cvMSG.put(MSG_JID, Jid);
        cvMSG.put(MSG_ISSENDER, 10);
        cvMSG.put(MSG_MSGDATA, Body);
        cvMSG.put(MSG_MSGUNIQUEID, uniqueID);
        cvMSG.put(MSG_MSGDATE, date);
        cvMSG.put(MSG_CHATMARKER, chatMarker);
        cvMSG.put(MSG_MSGOFFLINE, 0);

        if (getMsgExistBasedUniqueId(uniqueID) > 0) {
            updateRDB1Col(MSG_TABLE_NAME, cvMSG, MSG_MSGUNIQUEID, uniqueID);
        } else {
            rdb.insertQuery().insertMessage(Message.fromContentValues(cvMSG));
        }

        updateChatListLastMessage(Jid, Body, "You", date, false, false, null);

    }


    //[DONE CR] Outgoing Single URL Stanza To SQlitedatabase for both Rosterlist & Message
    public void urlOutputDatabase(final String Jid, final String Body, String uniqueID, final long Date) {  //new
        final int chatMarker = getOnlineStatus(Jid);

        ContentValues cvMSG = new ContentValues();

        cvMSG.put(MSG_JID, Jid);
        cvMSG.put(MSG_ISSENDER, 10);
        cvMSG.put(MSG_MSGDATA, Body);
        cvMSG.put(MSG_MSGUNIQUEID, uniqueID);
        cvMSG.put(MSG_MSGDATE, Date);
        cvMSG.put(MSG_CHATMARKER, chatMarker);
        cvMSG.put(MSG_MSGOFFLINE, 0);

        if (getMsgExistBasedUniqueId(uniqueID) > 0) {
            updateRDB1Col(MSG_TABLE_NAME, cvMSG, MSG_MSGUNIQUEID, uniqueID);
        } else {
            rdb.insertQuery().insertMessage(Message.fromContentValues(cvMSG));
        }

        updateChatListLastMessage(Jid, Body, "You", Date, false, false, null);
    }

    public void replyOutputDatabase(final String Jid, final String Body, String uniqueID, final long Date, String replyUniqueID, String replytype) {  //new
        final int chatMarker = getOnlineStatus(Jid);

        ContentValues cvMSG = new ContentValues();

        cvMSG.put(MSG_JID, Jid);
        cvMSG.put(MSG_MSGDATA, Body);
        cvMSG.put(MSG_MSGUNIQUEID, uniqueID);
        cvMSG.put(MSG_MSGDATE, Date);
        cvMSG.put(MSG_CHATMARKER, chatMarker);
        cvMSG.put(MSG_MSGOFFLINE, 0);
        cvMSG.put(MSG_MSGINFOID, replyUniqueID);

        cvMSG.put(MSG_ISSENDER, replytype.equals("1") ? 60 : 62);

        if (getMsgExistBasedUniqueId(uniqueID) > 0) {
            updateRDB1Col(MSG_TABLE_NAME, cvMSG, MSG_MSGUNIQUEID, uniqueID);
        } else {
            rdb.insertQuery().insertMessage(Message.fromContentValues(cvMSG));
        }

        updateChatListLastMessage(Jid, Body, "You", Date, false, false, null);
    }

    //[DONE CR] Outgoing Single Chat Contact
    public void ContactOutputDatabase(final String Jid, final String uniqueID, final long Date, final
    String contactname, final String contactnumber) {
        final int chatMarker = getOnlineStatus(Jid);
        final String allMsg = context.getString(R.string.contact_sent);


        ContentValues cvMSG = new ContentValues();
        cvMSG.put(MSG_JID, Jid);
        cvMSG.put(MSG_ISSENDER, 32);
        cvMSG.put(MSG_MSGDATA, "");
        cvMSG.put(MSG_CONTACT, 1);
        cvMSG.put(MSG_MSGUNIQUEID, uniqueID);
        cvMSG.put(MSG_MSGDATE, Date);
        cvMSG.put(MSG_CHATMARKER, chatMarker);
        cvMSG.put(MSG_MSGOFFLINE, 0);

        if (contactname != null) {
            cvMSG.put(MSG_CONTACTNAME, contactname);
        }
        if (contactnumber != null) {
            cvMSG.put(MSG_CONTACTNUMBER, contactnumber);
        }

        if (getMsgExistBasedUniqueId(uniqueID) > 0) {
            updateRDB1Col(MSG_TABLE_NAME, cvMSG, MSG_MSGUNIQUEID, uniqueID);
        } else {
            rdb.insertQuery().insertMessage(Message.fromContentValues(cvMSG));
        }
        updateChatListLastMessage(Jid, allMsg, "You", Date, false, false, null);
    }

    private int getMsgExistBasedUniqueId(String uniqueid) {

        return rdb.selectQuery().getMsgExistBasedonUniqueId(uniqueid);
    }

    //check if appt update msg already exists (apptID + issender)
    private int getApptUpdateMsgExist(String apptID, int isSender) {
        return rdb.selectQuery().getApptUpdateMsgExist(apptID, isSender);
    }

    //check if appt update msg already exists (apptID + issender)
    private int getApptUpdateMsgStatusExist(String apptID, int isSender, String otherJid) {
        return rdb.selectQuery().getApptUpdateMsgStatusExist(apptID, isSender, otherJid);
    }

    //[DONE CR] Outgoing Single Map Stanza To SQlitedatabase fo both Rosterlist & Message
    public void MapOutputDatabase(String Jid, String url, String uniqueID, long Date, String latitude,
                                  String longitude, String placeName, String placeAddress) {
        int chatMarker = getOnlineStatus(Jid);

        ContentValues cvMSG = new ContentValues();
        cvMSG.put(MSG_JID, Jid);
        cvMSG.put(MSG_ISSENDER, 30);
        cvMSG.put(MSG_MSGINFOURL, url);
        cvMSG.put(MSG_MSGUNIQUEID, uniqueID);
        cvMSG.put(MSG_MSGDATE, Date);
        cvMSG.put(MSG_MSGLATITUDE, latitude);
        cvMSG.put(MSG_MSGLONGITUDE, longitude);
        cvMSG.put(MSG_CHATMARKER, chatMarker);
        cvMSG.put(MSG_MSGOFFLINE, 0);
        cvMSG.put(MSG_MSGINFOID, placeAddress);
        cvMSG.put(MSG_MSGDATA, placeName);

        if (getMsgExistBasedUniqueId(uniqueID) > 0) {
            updateRDB1Col(MSG_TABLE_NAME, cvMSG, MSG_MSGUNIQUEID, uniqueID);
        } else {
            rdb.insertQuery().insertMessage(Message.fromContentValues(cvMSG));
        }

        updateChatListLastMessage(Jid, context.getString(R.string.location_sent), "You", Date, false, false, null);
    }

    //[DONE CR] Outgoing Single Image/ video thumbnail
    public void ImageOutputDatabase(final String Jid, final String UniqueId, final long Date, final String
            imagePath, String orient, String type, boolean camera) {
        int issender;
        String allmessage;

        if (type.equals("video")) {
            issender = 24;
            allmessage = context.getString(R.string.video_sent);
        } else {
            issender = 20;
            allmessage = context.getString(R.string.image_sent);
        }

        final int chatMarker = getOnlineStatus(Jid);

        ContentValues cvMSG = new ContentValues();

        cvMSG.put(MSG_JID, Jid);
        cvMSG.put(MSG_ISSENDER, issender);
        cvMSG.put(MSG_MSGINFOURL, imagePath);
        cvMSG.put(MSG_MSGUNIQUEID, UniqueId);
        cvMSG.put(MSG_MSGDATE, Date);
        cvMSG.put(MSG_CHATMARKER, chatMarker);

        if (type.equals("video") && !camera) {
            //to compress
            cvMSG.put(MSG_MEDIASTATUS, -6);
        } else if (type.equals("video") && camera) {
            cvMSG.put(MSG_MEDIASTATUS, -5);
        }

        //images compress for both from camera or gallery, but if from camera need to copy to IMAGES folder
        if (type.equals("image") && !camera) {
            //image and not from camera = gallery
            //so no need copy to IMAGES folder
            //just do compression
            cvMSG.put(MSG_MEDIASTATUS, -6);
        } else if (type.equals("image") && camera) {
            //image and from camera
            //so need copy to IMAGES folder
            //make a copy first
            cvMSG.put(MSG_MEDIASTATUS, -5);
        }

        try {
            rdb.insertQuery().insertMessage(Message.fromContentValues(cvMSG));
            updateChatListLastMessage(Jid, allmessage, getNameFromContactRoster(Jid), Date, false,
                    false, null);
        } catch (Exception e) {
            Log.i("ryan", "ImageOutputDatabase: " + e.toString());
        }
    }

    //[DONE CR] Outgoing Single Audio
    public String voiceOutput(final String Jid, final String UniqueId, final long Date, final String
            AudioPath) {
        String allmessage = context.getString(R.string.audio_sent);
        int chatMarker = getOnlineStatus(Jid);

        ContentValues cvMSG = new ContentValues();
        cvMSG.put(MSG_JID, Jid);
        cvMSG.put(MSG_ISSENDER, 22);
        cvMSG.put(MSG_MSGINFOURL, AudioPath);
        cvMSG.put(MSG_MSGUNIQUEID, UniqueId);
        cvMSG.put(MSG_MSGDATE, Date);
        cvMSG.put(MSG_CHATMARKER, chatMarker);
        cvMSG.put(MSG_MEDIASTATUS, -4);

        long rowID = rdb.insertQuery().insertMessage(Message.fromContentValues(cvMSG));

        updateChatListLastMessage(Jid, allmessage, getNameFromContactRoster(Jid), Date, false,
                false, null);

        return String.valueOf(rowID);
    }

    //Outgoing Single Restaurant
    public void RestaurantOutgoingDatabase(final String uniqueID, long date, final String imageurl, final
    String infoid, final String Jid, final String res_title) {
        String allmessage = context.getString(R.string.restaurant_sent);
        int chatMarker = getOnlineStatus(Jid);

        ContentValues cvMSG = new ContentValues();
        cvMSG.put(MSG_MSGUNIQUEID, uniqueID);
        cvMSG.put(MSG_MSGDATE, date);
        cvMSG.put(MSG_MSGINFOID, infoid);
        cvMSG.put(MSG_JID, Jid);
        cvMSG.put(MSG_ISSENDER, 34);
        cvMSG.put(MSG_CHATMARKER, chatMarker);
        cvMSG.put(MSG_MSGOFFLINE, 0);

        if (imageurl != null) {
            cvMSG.put(MSG_MSGINFOURL, imageurl);
        }
        cvMSG.put(MSG_MSGDATA, res_title);

        if (getMsgExistBasedUniqueId(uniqueID) > 0) {
            updateRDB1Col(MSG_TABLE_NAME, cvMSG, MSG_MSGUNIQUEID, uniqueID);
        } else {
            rdb.insertQuery().insertMessage(Message.fromContentValues(cvMSG));
        }

        updateChatListLastMessage(Jid, allmessage, "You", date, false, false, null);
    }

    //outgoing indi tracking - image-like chatbox for tracking
    public void TrackingOutgoingDatabase(
            String jid, String uniqueID, long date, long trackTime, String mapURL) {
        String allmessage = context.getString(R.string.tracking_sent);
        int chatMarker = getOnlineStatus(jid);

        //update MESSAGE db
        ContentValues cvMSG = new ContentValues();

        cvMSG.put(MSG_MSGUNIQUEID, uniqueID);
        cvMSG.put(MSG_MSGDATE, date);
        cvMSG.put(MSG_JID, jid);
        cvMSG.put(MSG_ISSENDER, 40);
        cvMSG.put(MSG_CHATMARKER, chatMarker);
        cvMSG.put(MSG_MSGINFOURL, mapURL);

        rdb.insertQuery().insertMessage(Message.fromContentValues(cvMSG));

        //update/insert to ROSTER_LIST db
        updateChatListLastMessage(jid, allmessage, getNameFromContactRoster(jid), date, false,
                false, null);

    }

    /*----------- [K01_01_02E] Indi-Outgoing-END ----------- */
    /*////////////// [K01_01E] CHAT: Indi-related-END //////////////*/


    /*////////////// [K01_02] CHAT: group-related ////////////////*/
    /*----------- [K01_02_01] Group-Incoming ----------- */

    //[DONE CR] Incoming Group Message Stanza To SQlitedatabase fo both Rosterlist & Message
    public void GroupMessageInputDatabase(
            final String roomJid, final String Body, final String Id, final long Date, final String
            otherjid, final boolean playSound) {

        if (getChatListExist(roomJid) != 0) {
            Cursor cursor = rdb.selectQuery().GroupMessageInputDatabase(roomJid);

            int isSender = 11;
            if (cursor.getCount() > 1) { //if room has more than 1 messages
                //need to check last sender to know if need show displayName
                cursor.move(1);
                String otherJid2 = cursor.getString(0);

                if (otherJid2.equals(otherjid)) { //if latest msg and 2nd latest msg are from the
                    // same person, don't have to show name (issender = 2)
                    isSender = 12;
                }
            }
            cursor.close();

            ContentValues cvMSG = new ContentValues();

            cvMSG.put(MSG_JID, roomJid);
            cvMSG.put(MSG_SENDERJID, otherjid);
            cvMSG.put(MSG_MSGUNIQUEID, Id);
            cvMSG.put(MSG_MSGDATE, Date);
            cvMSG.put(MSG_MSGDATA, Body);
            cvMSG.put(MSG_ISSENDER, isSender);

            rdb.insertQuery().insertMessage(Message.fromContentValues(cvMSG));

            updateChatListLastMessage(roomJid, Body, getNameFromContactRoster(otherjid), Date,
                    true, playSound, null);

            addChatBadgeUnread(roomJid, Date);

            String user_jid = preferences.getValue(context, GlobalVariables.STRPREF_USER_ID);
            groupChatStanza.GroupAckStanza(roomJid, user_jid, otherjid, Id);
        }
    }

    public void GroupReplyInputDatabase(
            final String roomJid, final String Body, final String Id, final long Date, final String
            otherjid, final boolean playSound, String replyid, String replytype) {
        int isSender;
        isSender = replytype.equals("1") ? 61 : 63;

        ContentValues cvMSG = new ContentValues();

        cvMSG.put(MSG_JID, roomJid);
        cvMSG.put(MSG_SENDERJID, otherjid);
        cvMSG.put(MSG_MSGUNIQUEID, Id);
        cvMSG.put(MSG_MSGDATE, Date);
        cvMSG.put(MSG_MSGDATA, Body);
        cvMSG.put(MSG_ISSENDER, isSender);
        cvMSG.put(MSG_MSGINFOID, replyid);

        rdb.insertQuery().insertMessage(Message.fromContentValues(cvMSG));

        updateChatListLastMessage(roomJid, Body, getNameFromContactRoster(otherjid), Date,
                true, playSound, null);

        addChatBadgeUnread(roomJid, Date);

        String user_jid = preferences.getValue(context, GlobalVariables.STRPREF_USER_ID);
        groupChatStanza.GroupAckStanza(roomJid, user_jid, otherjid, Id);
    }


    //[DONE CR] Incoming Group Contact Stanza
    public void GroupContactInputDatabase(
            final String roomJid, final String Id, final long Date, final String otherjid, final
    String contactname, final String contactnumber, final boolean playSound) {
        String allmessage = context.getString(R.string.contact_received);

        ContentValues cvMSG = new ContentValues();

        cvMSG.put(MSG_JID, roomJid);
        cvMSG.put(MSG_SENDERJID, otherjid);
        cvMSG.put(MSG_ISSENDER, 33);
        cvMSG.put(MSG_MSGDATA, "");
        cvMSG.put(MSG_CONTACT, 0);
        cvMSG.put(MSG_MSGUNIQUEID, Id);
        cvMSG.put(MSG_MSGDATE, Date);

        if (contactname != null) {
            cvMSG.put(MSG_CONTACTNAME, contactname);
        }
        if (contactnumber != null) {
            cvMSG.put(MSG_CONTACTNUMBER, contactnumber);
        }
        rdb.insertQuery().insertMessage(Message.fromContentValues(cvMSG));

        updateChatListLastMessage(roomJid, allmessage, getNameFromContactRoster(otherjid), Date,
                true, playSound, null);
        addChatBadgeUnread(roomJid, Date);

        String user_jid = preferences.getValue(context, GlobalVariables.STRPREF_USER_ID);
        groupChatStanza.GroupAckStanza(roomJid, user_jid, otherjid, Id);
    }

    //[DONE CR] Incoming Group Map Stanza
    public void GroupMapInputDatabase(
            final String roomJid, final String Body, final String Id, final long Date, final String
            otherjid, final String Lat, final String Long, final boolean playSound, final String placeName, final String address) {
        String allmessage = context.getString(R.string.location_received);
        ContentValues cvMSG = new ContentValues();

        cvMSG.put(MSG_JID, roomJid);
        cvMSG.put(MSG_SENDERJID, otherjid);
        cvMSG.put(MSG_MSGUNIQUEID, Id);
        cvMSG.put(MSG_MSGDATE, Date);
        cvMSG.put(MSG_ISSENDER, 31);
        cvMSG.put(MSG_MSGINFOURL, Body);
        cvMSG.put(MSG_MSGLATITUDE, Lat);
        cvMSG.put(MSG_MSGLONGITUDE, Long);
        cvMSG.put(MSG_MSGDATA, address);
        cvMSG.put(MSG_MSGINFOID, placeName);

        rdb.insertQuery().insertMessage(Message.fromContentValues(cvMSG));

        updateChatListLastMessage(roomJid, allmessage, getNameFromContactRoster(otherjid), Date,
                true, playSound, null);

        addChatBadgeUnread(roomJid, Date);

        String user_jid = preferences.getValue(context, GlobalVariables.STRPREF_USER_ID);
        groupChatStanza.GroupAckStanza(roomJid, user_jid, otherjid, Id);
    }

    //2018 jan 8 mon 5:30pm, incoming video group
    public void GroupVideoInputDatabase(
            final String roomJid, final String resourceID, final String uniqueID, final long Date, final String
            otherjid, final String orientation, final boolean playSound, String video_thumb_base64) {
        ContentValues cvMSG = new ContentValues();
        cvMSG.put(MSG_JID, roomJid);
        cvMSG.put(MSG_ISSENDER, 25);
        cvMSG.put(MSG_MSGINFOID, resourceID);
        cvMSG.put(MSG_MSGUNIQUEID, uniqueID);
        cvMSG.put(MSG_MSGDATE, Date);
        cvMSG.put(MSG_SENDERJID, otherjid);
        cvMSG.put(MSG_MSGLATITUDE, video_thumb_base64);

        boolean isNeedDownload = checkIfNeedDownload("video");

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (PackageManager.PERMISSION_DENIED == context.checkCallingOrSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                isNeedDownload = false;
            }
        }

        if (isNeedDownload) {
            cvMSG.put(MSG_MEDIASTATUS, 1);
        } else {
            cvMSG.put(MSG_MEDIASTATUS, -3);
        }

        long rowId = rdb.insertQuery().insertMessage(Message.fromContentValues(cvMSG));
        final String allmessage = context.getString(R.string.video_received);
        updateChatListLastMessage(roomJid, allmessage, getNameFromContactRoster(otherjid), Date,
                true, playSound, null);

        String user_jid = preferences.getValue(context, GlobalVariables.STRPREF_USER_ID);
        groupChatStanza.GroupAckStanza(roomJid, user_jid, otherjid, uniqueID);

        addChatBadgeUnread(roomJid, Date);
        if (isNeedDownload) {
            String access_token = preferences.getValue(context, GlobalVariables.STRPREF_ACCESS_TOKEN);
            Resource1v3Interface resource1v3Interface = RetrofitAPIClient.getClient().create(Resource1v3Interface.class);
            retrofit2.Call<Resource1v3Model> call = resource1v3Interface.getResource(resourceID, "Bearer " + access_token);
            call.enqueue(new retrofit2.Callback<Resource1v3Model>() {
                @Override
                public void onResponse(retrofit2.Call<Resource1v3Model> call, retrofit2.Response<Resource1v3Model> response) {
                    if (!response.isSuccessful()) {
                        new MiscHelper().retroLogUnsuc(response, "GroupVideoInputDatabase ", "JAY");
                        return;
                    }

                    final String videoUrl = response.body().getResource_url();
                    final String thumbnailBase64 = response.body().getThumbnail();
                    final String displayName = getNameFromContactRoster(otherjid);

                    ContentValues cvMSG = new ContentValues();
                    cvMSG.put(MSG_MSGINFOURL, videoUrl);
                    String str_row_id = String.valueOf(rowId);

                    updateRDB1Col(MSG_TABLE_NAME, cvMSG, MSG_ROW, str_row_id);

                    convertBase64ToBimapAndSaveToFolder(thumbnailBase64, resourceID);

//                    download(resourceID, videoUrl, str_row_id, "video");
                    new ChatHelper().startDownloadWorker(resourceID, videoUrl, str_row_id, "video");

                }

                @Override
                public void onFailure(retrofit2.Call<Resource1v3Model> call, Throwable t) {
                    new MiscHelper().retroLogFailure(t, "GroupVideoInputDatabase", "JAY");
                }
            });
        }
    }

    //[DONE CR] Incoming Group Image
    public void GroupImageInputDatabase(
            final String roomJid, final String resourceID, final String uniqueID, final long Date, final String
            otherjid, final String orientation, final boolean playSound, final String rotation, String image_base64) {

        String access_token = preferences.getValue(context, GlobalVariables.STRPREF_ACCESS_TOKEN);
        ContentValues cvMSG = new ContentValues();
        cvMSG.put(MSG_JID, roomJid);
        cvMSG.put(MSG_ISSENDER, 21);
        cvMSG.put(MSG_MSGINFOID, resourceID);
        cvMSG.put(MSG_MSGUNIQUEID, uniqueID);
        cvMSG.put(MSG_MSGDATE, Date);
        cvMSG.put(MSG_SENDERJID, otherjid);
        cvMSG.put(MSG_MSGLATITUDE, image_base64);

        boolean isNeedDownload = checkIfNeedDownload("image");

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (PackageManager.PERMISSION_DENIED == context.checkCallingOrSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                isNeedDownload = false;
            }
        }

        if (isNeedDownload) {
            cvMSG.put(MSG_MEDIASTATUS, 1);
        } else {
            cvMSG.put(MSG_MEDIASTATUS, -3);
        }

        long rowid = rdb.insertQuery().insertMessage(Message.fromContentValues(cvMSG));

        final String allmessage = context.getString(R.string.image_received);

        updateChatListLastMessage(roomJid, allmessage, getNameFromContactRoster(otherjid), Date,
                true, playSound, null);

        addChatBadgeUnread(roomJid, Date);

        String user_jid = preferences.getValue(context, GlobalVariables.STRPREF_USER_ID);
        groupChatStanza.GroupAckStanza(roomJid, user_jid, otherjid, uniqueID);

        if (isNeedDownload) {
            //get file name from date and looping
//            final String fileName = ImageHelper.getFileNameForSavingMedia(GlobalVariables.IMAGES_PATH, "IMG");
            Resource1v3Interface resource1v3Interface = RetrofitAPIClient.getClient().create(Resource1v3Interface.class);
            retrofit2.Call<Resource1v3Model> call = resource1v3Interface.getResource(resourceID, "Bearer " + access_token);
            call.enqueue(new retrofit2.Callback<Resource1v3Model>() {
                @Override
                public void onResponse(retrofit2.Call<Resource1v3Model> call, retrofit2.Response<Resource1v3Model> response) {
                    if (!response.isSuccessful()) {
                        new MiscHelper().retroLogUnsuc(response, "GroupImageInputDatabase ", "JAY");

                        return;
                    }
                    final String imageURL = response.body().getResource_url();
                    String str_row_id = String.valueOf(rowid);

                    ContentValues cvMSG = new ContentValues();
                    cvMSG.put(MSG_MSGINFOURL, imageURL);
                    updateRDB1Col(MSG_TABLE_NAME, cvMSG, MSG_ROW, str_row_id);

//                    download(resourceID, imageURL, str_row_id, "image");
                    new ChatHelper().startDownloadWorker(resourceID, imageURL, str_row_id, "image");
                }

                @Override
                public void onFailure(retrofit2.Call<Resource1v3Model> call, Throwable t) {
                    new MiscHelper().retroLogFailure(t, "GroupImageInputDatabase", "JAY");

                }
            });
        }
    }

    //[DONE CR] Incoming Group Audio
    public void Groupvoiceinput(final String roomJid, final String resourceID, final String uniqueID,
                                final long Date, final String otherjid, final boolean playSound) {
        final String allmessage = context.getString(R.string.audio_received);

        ContentValues cvMSG = new ContentValues();
        cvMSG.put(MSG_JID, roomJid);
        cvMSG.put(MSG_ISSENDER, 23);
        cvMSG.put(MSG_MSGINFOID, resourceID);
        cvMSG.put(MSG_MSGUNIQUEID, uniqueID);
        cvMSG.put(MSG_MSGDATE, Date);
        cvMSG.put(MSG_SENDERJID, otherjid);
        boolean isNeedDownload = checkIfNeedDownload("audio");

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (PackageManager.PERMISSION_DENIED == context.checkCallingOrSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                isNeedDownload = false;
            }
        }

        if (isNeedDownload) {
            cvMSG.put(MSG_MEDIASTATUS, 1);
        } else {
            cvMSG.put(MSG_MEDIASTATUS, -3);
        }
        long rowId = rdb.insertQuery().insertMessage(Message.fromContentValues(cvMSG));

        updateChatListLastMessage(roomJid, allmessage, getNameFromContactRoster(otherjid), Date,
                true, playSound, null);

        addChatBadgeUnread(roomJid, Date);
        String user_jid = preferences.getValue(context, GlobalVariables.STRPREF_USER_ID);
        groupChatStanza.GroupAckStanza(roomJid, user_jid, otherjid, uniqueID);

        if (isNeedDownload) {
            String access_token = preferences.getValue(context, GlobalVariables.STRPREF_ACCESS_TOKEN);
            Resource1v3Interface resource1v3Interface = RetrofitAPIClient.getClient().create(Resource1v3Interface.class);
            retrofit2.Call<Resource1v3Model> call = resource1v3Interface.getResource(resourceID, "Bearer " + access_token);
            call.enqueue(new retrofit2.Callback<Resource1v3Model>() {
                @Override
                public void onResponse(retrofit2.Call<Resource1v3Model> call, retrofit2.Response<Resource1v3Model> response) {
                    if (!response.isSuccessful()) {
                        new MiscHelper().retroLogUnsuc(response, "Groupvoiceinput ", "JAY");

                        return;
                    }
                    String audioURL = response.body().getResource_url();
                    ContentValues cvMSG = new ContentValues();
                    cvMSG.put(MSG_MSGINFOURL, audioURL);
                    final String str_row_id = String.valueOf(rowId);
                    updateRDB1Col(MSG_TABLE_NAME, cvMSG,
                            MSG_ROW, str_row_id);
//                    download(resourceID, audioURL, str_row_id, "audio");
                    new ChatHelper().startDownloadWorker(resourceID, audioURL, str_row_id, "audio");
                }

                @Override
                public void onFailure(retrofit2.Call<Resource1v3Model> call, Throwable t) {
                    new MiscHelper().retroLogFailure(t, "Groupvoiceinput", "JAY");

                }
            });
        }
    }


    //[DONE CR] Incoming Group Restaurant
    public void RestaurantGroupInputDatabase(final String roomJid, final String infoIDRes, final String
            Id, final String res_title, final long currentDate, final String otherjid, final boolean playSound) {
        RestaurantInfo model = new RestaurantInfo(infoIDRes);

        String access_token = preferences.getValue(context, GlobalVariables.STRPREF_ACCESS_TOKEN);

        //build retrofit
        RestaurantDetails client = RetrofitAPIClient.getClient().create(RestaurantDetails.class);
        retrofit2.Call<RestaurantInfo> call = client.nearbyRes(model, "Bearer " + access_token);
        call.enqueue(new retrofit2.Callback<RestaurantInfo>() {
            @Override
            public void onResponse(retrofit2.Call<RestaurantInfo> call, retrofit2.Response<RestaurantInfo> response) {
                if (!response.isSuccessful()) {

                    new MiscHelper().retroLogUnsuc(response, "RestaurantGroupInputDatabase ", "JAY");

                    return;
                }
                final String allmessage = context.getString(R.string.restaurant_received);

                ContentValues cvMSG = new ContentValues();

                cvMSG.put(MSG_MSGUNIQUEID, Id);
                cvMSG.put(MSG_MSGINFOID, infoIDRes);
                cvMSG.put(MSG_JID, roomJid);
                cvMSG.put(MSG_ISSENDER, 35);
                cvMSG.put(MSG_MSGDATE, currentDate);
                cvMSG.put(MSG_SENDERJID, otherjid);
                if (res_title != null) {
                    cvMSG.put(MSG_MSGDATA, res_title);
                }
                final String image_id = response.body().getPropic();
                if (image_id != null) {
                    cvMSG.put(MSG_MSGINFOURL, image_id);
                }
                rdb.insertQuery().insertMessage(Message.fromContentValues(cvMSG));

                updateChatListLastMessage(roomJid, allmessage, getNameFromContactRoster(otherjid),
                        currentDate, true, playSound, null);

                addChatBadgeUnread(roomJid, currentDate);

                String user_jid = preferences.getValue(context, GlobalVariables.STRPREF_USER_ID);
                groupChatStanza.GroupAckStanza(roomJid, user_jid, otherjid, Id);
            }

            @Override
            public void onFailure(retrofit2.Call<RestaurantInfo> call, Throwable t) {
                new MiscHelper().retroLogFailure(t, "RestaurantGroupInputDatabase", "JAY");
                Toast.makeText(context, R.string.onfailure, Toast
                        .LENGTH_SHORT).show();
            }
        });
    }
    /*----------- [K01_02_01E] Group-Incoming-END ----------- */


    /*----------- [K01_02_02] Group-Outgoing ----------- */

    //Outgoing group text msg
    public void GroupMessageOutputDatabase(final String Jid, final String Body,
                                           final String uniqueID, final long Date, final String otherjid) {
        final String you = context.getString(R.string.you);

        ContentValues cvMSG = new ContentValues();
        cvMSG.put(MSG_JID, Jid);
        cvMSG.put(MSG_SENDERJID, otherjid);
        cvMSG.put(MSG_MSGUNIQUEID, uniqueID);
        cvMSG.put(MSG_MSGDATE, Date);
        cvMSG.put(MSG_MSGDATA, Body);
        cvMSG.put(MSG_CHATMARKER, 0);
        cvMSG.put(MSG_ISSENDER, 10);
        cvMSG.put(MSG_MSGOFFLINE, 0);

        if (getMsgExistBasedUniqueId(uniqueID) > 0) {
            updateRDB1Col(MSG_TABLE_NAME, cvMSG, MSG_MSGUNIQUEID, uniqueID);
        } else {
            rdb.insertQuery().insertMessage(Message.fromContentValues(cvMSG));
        }

        updateChatListLastMessage(Jid, Body, you, Date, false, false, null);
    }

    //outgoing group reply msg
    public void GroupReplyOutputDatabase(final String Jid, final String Body,
                                         final String uniqueID, final long Date, final String otherjid, final String replyid, final String replytype) {
        final String you = context.getString(R.string.you);

        int issender;
        issender = replytype.equals("1") ? 60 : 62;

        ContentValues cvMSG = new ContentValues();
        cvMSG.put(MSG_JID, Jid);
        cvMSG.put(MSG_SENDERJID, otherjid);
        cvMSG.put(MSG_MSGUNIQUEID, uniqueID);
        cvMSG.put(MSG_MSGDATE, Date);
        cvMSG.put(MSG_MSGDATA, Body);
        cvMSG.put(MSG_CHATMARKER, 0);
        cvMSG.put(MSG_ISSENDER, issender);
        cvMSG.put(MSG_MSGOFFLINE, 0);
        cvMSG.put(MSG_MSGINFOID, replyid);

        if (getMsgExistBasedUniqueId(uniqueID) > 0) {
            updateRDB1Col(MSG_TABLE_NAME, cvMSG, MSG_MSGUNIQUEID, uniqueID);
        } else {
            rdb.insertQuery().insertMessage(Message.fromContentValues(cvMSG));
        }

        updateChatListLastMessage(Jid, Body, you, Date, false, false, null);
    }

    //Outgoing Group URL Stanza To SQlitedatabase fo both Rosterlist & Message
    public void GroupUrlOutputDatabase(final String Jid, final String Body, final String uniqueID,
                                       final
                                       long Date, final String otherjid) {
        final String you = context.getString(R.string.you);

        ContentValues cvMSG = new ContentValues();
        cvMSG.put(MSG_JID, Jid);
        cvMSG.put(MSG_SENDERJID, otherjid);
        cvMSG.put(MSG_MSGUNIQUEID, uniqueID);
        cvMSG.put(MSG_MSGDATE, Date);
        cvMSG.put(MSG_MSGDATA, Body);
        cvMSG.put(MSG_CHATMARKER, 0);
        cvMSG.put(MSG_ISSENDER, 10);
        cvMSG.put(MSG_MSGOFFLINE, 0);

        if (getMsgExistBasedUniqueId(uniqueID) > 0) {
            updateRDB1Col(MSG_TABLE_NAME, cvMSG, MSG_MSGUNIQUEID, uniqueID);
        } else {
            rdb.insertQuery().insertMessage(Message.fromContentValues(cvMSG));
        }


        updateChatListLastMessage(Jid, Body, you, Date, false, false, null);
    }

    //Outgoing Group Contact Stanza
    public void GroupContactOutputDatabase(String Jid, String uniqueID, long Date, String otherjid,
                                           String contactname, String contactnumber) {
        ContentValues cvMSG = new ContentValues();
        final String you = context.getString(R.string.you);
        final String allmessage = context.getString(R.string
                .contact_sent);


        cvMSG.put(MSG_JID, Jid);
        cvMSG.put(MSG_SENDERJID, otherjid);
        cvMSG.put(MSG_MSGUNIQUEID, uniqueID);
        cvMSG.put(MSG_MSGDATE, Date);
        cvMSG.put(MSG_MSGDATA, "");
        cvMSG.put(MSG_CHATMARKER, 0);
        cvMSG.put(MSG_ISSENDER, 32);
        cvMSG.put(MSG_CONTACT, 1);
        cvMSG.put(MSG_MSGOFFLINE, 0);

        if (contactname != null) {
            cvMSG.put(MSG_CONTACTNAME, contactname);
        }
        if (contactnumber != null) {
            cvMSG.put(MSG_CONTACTNUMBER, contactnumber);
        }

        if (getMsgExistBasedUniqueId(uniqueID) > 0) {
            updateRDB1Col(MSG_TABLE_NAME, cvMSG, MSG_MSGUNIQUEID, uniqueID);
        } else {
            rdb.insertQuery().insertMessage(Message.fromContentValues(cvMSG));
        }

        updateChatListLastMessage(Jid, allmessage, you, Date, false, false, null);
    }

    //Outgoing Group Map Stanza
    public void GroupMapOutputDatabase(final String Jid, final String Body, final String uniqueID,
                                       final long Date, final String otherjid, final String Lat, final String Long, final String placeName, final String address) {
        final String you = context.getString(R.string.you);
        final String allmessage = context.getString(R.string
                .location_sent);

        ContentValues cvMSG = new ContentValues();
        cvMSG.put(MSG_JID, Jid);
        cvMSG.put(MSG_SENDERJID, otherjid);
        cvMSG.put(MSG_MSGUNIQUEID, uniqueID);
        cvMSG.put(MSG_MSGDATE, Date);
        cvMSG.put(MSG_CHATMARKER, 0);
        cvMSG.put(MSG_ISSENDER, 30);
        cvMSG.put(MSG_MSGLATITUDE, Lat);
        cvMSG.put(MSG_MSGLONGITUDE, Long);
        cvMSG.put(MSG_MSGINFOURL, Body);
        cvMSG.put(MSG_MSGOFFLINE, 0);
        cvMSG.put(MSG_MSGDATA, placeName);
        cvMSG.put(MSG_MSGINFOID, address);

        if (getMsgExistBasedUniqueId(uniqueID) > 0) {
            updateRDB1Col(MSG_TABLE_NAME, cvMSG, MSG_MSGUNIQUEID, uniqueID);
        } else {
            rdb.insertQuery().insertMessage(Message.fromContentValues(cvMSG));
        }

        updateChatListLastMessage(Jid, allmessage, you, Date, false, false, null);
    }

    //Outgoing Group Image/video thumbnail
    public void GroupImageOutputDatabase(final String Jid, final String UniqueId, final long Date, final String
            imagePath, String orient, String type, boolean camera) {
        final String you = context.getString(R.string.you);
        final int issender;
        String allmessage;

        if (type.equals("video")) {
            issender = 24;
            allmessage = context.getString(R.string
                    .video_sent);
        } else {
            issender = 20;
            allmessage = context.getString(R.string
                    .image_sent);
        }

        final String user_jid = preferences.getValue(context, GlobalVariables.STRPREF_USER_ID);

        ContentValues cvMSG = new ContentValues();
        cvMSG.put(MSG_JID, Jid);
        cvMSG.put(MSG_ISSENDER, issender);
        cvMSG.put(MSG_MSGINFOURL, imagePath);
        cvMSG.put(MSG_MSGUNIQUEID, UniqueId);
        cvMSG.put(MSG_MSGDATE, Date);
        cvMSG.put(MSG_CHATMARKER, 0);
        cvMSG.put(MSG_MSGOFFLINE, 0);
        cvMSG.put(MSG_SENDERJID, user_jid);
        if (type.equals("video") && !camera) {
            cvMSG.put(MSG_MEDIASTATUS, -6);
        } else if (type.equals("video") && camera) {
            cvMSG.put(MSG_MEDIASTATUS, -5);
        }

        //images compress for both from camera or gallery, but if from camera need to copy to IMAGES folder
        if (type.equals("image") && !camera) {
            //image and not from camera = gallery
            //so no need copy to IMAGES folder
            //just do compression
            cvMSG.put(MSG_MEDIASTATUS, -6);
        } else if (type.equals("image") && camera) {
            //image and from camera
            //so need copy to IMAGES folder
            //make a copy first
            cvMSG.put(MSG_MEDIASTATUS, -5);
        }
        rdb.insertQuery().insertMessage(Message.fromContentValues(cvMSG));

        updateChatListLastMessage(Jid, allmessage, you, Date, false, false, null);

    }

    //Outgoing Group Audio
    public String GroupvoiceOutput(final String Jid, final String UniqueId, final long Date,
                                   final String AudioPath) {
        final String user_jid = preferences.getValue(context, GlobalVariables.STRPREF_USER_ID);
        final String you = context.getString(R.string.you);
        final String allmessage = context.getString(R.string
                .audio_sent);

        ContentValues cvMSG = new ContentValues();
        cvMSG.put(MSG_JID, Jid);
        cvMSG.put(MSG_CHATMARKER, 0);
        cvMSG.put(MSG_ISSENDER, 22);
        cvMSG.put(MSG_MSGINFOURL, AudioPath);
        cvMSG.put(MSG_MSGUNIQUEID, UniqueId);
        cvMSG.put(MSG_MSGDATE, Date);
        cvMSG.put(MSG_MEDIASTATUS, -4);
        cvMSG.put(MSG_SENDERJID, user_jid);
        cvMSG.put(MSG_MSGOFFLINE, 0);

        long rowId = rdb.insertQuery().insertMessage(Message.fromContentValues(cvMSG));

        updateChatListLastMessage(Jid, allmessage, you, Date, false, false, null);

        return String.valueOf(rowId);
    }

    //Outgoing Group Restaurant
    public void RestaurantGroupOutputDatabase(final String Jid, final String imageurl,
                                              final String infoid, final String uniqueID, final String res_title, final long currentDate) {
        final String user_jid = preferences.getValue(context, GlobalVariables.STRPREF_USER_ID);
        final String you = context.getString(R.string.you);
        final String allmessage = context.getString(R.string
                .restaurant_sent);

        ContentValues cvMSG = new ContentValues();
        cvMSG.put(MSG_MSGUNIQUEID, uniqueID);
        cvMSG.put(MSG_MSGDATE, currentDate);
        cvMSG.put(MSG_MSGINFOID, infoid);
        cvMSG.put(MSG_JID, Jid);
        cvMSG.put(MSG_ISSENDER, 34);
        cvMSG.put(MSG_SENDERJID, user_jid);
        cvMSG.put(MSG_MSGOFFLINE, 0);

        if (imageurl != null) {
            cvMSG.put(MSG_MSGINFOURL, imageurl);
        }
        cvMSG.put(MSG_MSGDATA, res_title);

        if (getMsgExistBasedUniqueId(uniqueID) > 0) {
            updateRDB1Col(MSG_TABLE_NAME, cvMSG, MSG_MSGUNIQUEID, uniqueID);
        } else {
            rdb.insertQuery().insertMessage(Message.fromContentValues(cvMSG));
        }

        updateChatListLastMessage(Jid, allmessage, you, currentDate, false, false, null);
    }

    /*----------- [K01_02_02E] Group-Outgoing-END ----------- */
    /*/////////////// [K01_02E] CHAT: group-related-END ///////////////*/


    /*/////////////// [K01_03] CHAT: offline-related ///////////////*/
    //outgoing send offline msges when detect stream
    public void saveMessageAndSendWhenOnline(String from, String Jid, String otherjid, String Body, String uniqueId,
                                             long currentDate, String contactname, String contactnumber,
                                             String Lat, String Long, String infourl, String infoid) {
        ContentValues cvMSG = new ContentValues();
        cvMSG.put(MSG_JID, Jid);
        cvMSG.put(MSG_MSGDATE, currentDate);
        cvMSG.put(MSG_MSGUNIQUEID, uniqueId);
        cvMSG.put(MSG_MSGOFFLINE, 1);

        if (Jid.length() == 12) {
            switch (from) {
                case "message":
                    cvMSG.put(MSG_ISSENDER, 10);
                    cvMSG.put(MSG_MSGDATA, Body);

                    break;

                case "url":
                    cvMSG.put(MSG_ISSENDER, 10);
                    cvMSG.put(MSG_MSGDATA, Body);

                    break;

                case "contact":
                    cvMSG.put(MSG_ISSENDER, 32);
                    cvMSG.put(MSG_MSGDATA, Body);
                    cvMSG.put(MSG_CONTACT, 1);

                    if (contactname != null) {
                        cvMSG.put(MSG_CONTACTNAME, contactname);
                    }
                    if (contactnumber != null) {
                        cvMSG.put(MSG_CONTACTNUMBER, contactnumber);
                    }
                    break;

                case "map":
                    cvMSG.put(MSG_MSGDATA, Body);
                    cvMSG.put(MSG_ISSENDER, 30);
                    cvMSG.put(MSG_MSGINFOURL, infourl);
                    cvMSG.put(MSG_MSGLATITUDE, Lat);
                    cvMSG.put(MSG_MSGLONGITUDE, Long);
                    cvMSG.put(MSG_MSGINFOID, infoid);

                    break;
                case "restaurant":

                    cvMSG.put(MSG_MSGINFOID, infoid);
                    cvMSG.put(MSG_ISSENDER, 34);

                    if (infourl != null) {
                        cvMSG.put(MSG_MSGINFOURL, infourl);
                    }
                    cvMSG.put(MSG_MSGDATA, Body);
                    break;

                case "booking":
                    cvMSG.put(MSG_ISSENDER, 50);
                    cvMSG.put(MSG_MSGINFOURL, infourl);
                    cvMSG.put(MSG_MSGINFOID, infoid);
                    cvMSG.put(MSG_MSGLATITUDE, Lat);
                    cvMSG.put(MSG_MSGLONGITUDE, Long);
                    cvMSG.put(MSG_MSGUNIQUEID, uniqueId);
                    cvMSG.put(MSG_MSGDATE, currentDate);
                    cvMSG.put(MSG_SENDERJID, otherjid);
                    break;

                case "reply":
                    cvMSG.put(MSG_JID, Jid);
                    cvMSG.put(MSG_MSGDATA, Body);
                    cvMSG.put(MSG_MSGUNIQUEID, uniqueId);
                    cvMSG.put(MSG_MSGDATE, currentDate);
                    cvMSG.put(MSG_MSGINFOID, infoid);
                    cvMSG.put(MSG_ISSENDER, contactname.equals("1") ? 60 : 62);
                    break;
                default:
                    break;
            }
        } else {
            switch (from) {
                case "message":

                    cvMSG.put(MSG_SENDERJID, otherjid);
                    cvMSG.put(MSG_MSGDATA, Body);
                    cvMSG.put(MSG_ISSENDER, 10);

                    break;

                case "url":

                    cvMSG.put(MSG_SENDERJID, otherjid);
                    cvMSG.put(MSG_MSGDATA, Body);
                    cvMSG.put(MSG_ISSENDER, 10);

                    break;

                case "contact":

                    cvMSG.put(MSG_SENDERJID, otherjid);
                    cvMSG.put(MSG_MSGDATA, Body);
                    cvMSG.put(MSG_CHATMARKER, 0);
                    cvMSG.put(MSG_ISSENDER, 32);
                    cvMSG.put(MSG_CONTACT, 1);

                    if (contactname != null) {
                        cvMSG.put(MSG_CONTACTNAME, contactname);
                    }
                    if (contactnumber != null) {
                        cvMSG.put(MSG_CONTACTNUMBER, contactnumber);
                    }
                    break;

                case "map":

                    cvMSG.put(MSG_SENDERJID, otherjid);
                    cvMSG.put(MSG_MSGDATA, Body);
                    cvMSG.put(MSG_ISSENDER, 30);
                    cvMSG.put(MSG_MSGLATITUDE, Lat);
                    cvMSG.put(MSG_MSGLONGITUDE, Long);
                    cvMSG.put(MSG_MSGINFOURL, infourl);
                    cvMSG.put(MSG_MSGINFOID, infoid);
                    break;

                case "restaurant":
                    String userJid = preferences.getValue(context, GlobalVariables.STRPREF_USER_ID);
                    cvMSG.put(MSG_MSGDATE, currentDate);
                    cvMSG.put(MSG_MSGINFOID, infoid);
                    cvMSG.put(MSG_ISSENDER, 34);
                    cvMSG.put(MSG_SENDERJID, userJid);

                    if (infourl != null) {
                        cvMSG.put(MSG_MSGINFOURL, infourl);
                    }
                    cvMSG.put(MSG_MSGDATA, Body);

                    break;

                case "booking":
                    cvMSG.put(MSG_ISSENDER, 50);
                    cvMSG.put(MSG_MSGINFOURL, infourl);
                    cvMSG.put(MSG_MSGINFOID, infoid);
                    cvMSG.put(MSG_MSGLATITUDE, Lat);
                    cvMSG.put(MSG_MSGLONGITUDE, Long);
                    cvMSG.put(MSG_MSGUNIQUEID, uniqueId);
                    cvMSG.put(MSG_MSGDATE, currentDate);
                    cvMSG.put(MSG_SENDERJID, otherjid);
                    cvMSG.put(MSG_MSGDATA, Body);
                    break;

                case "reply":
                    cvMSG.put(MSG_SENDERJID, otherjid);
                    cvMSG.put(MSG_MSGDATA, Body);
                    cvMSG.put(MSG_MSGUNIQUEID, uniqueId);
                    cvMSG.put(MSG_MSGDATE, currentDate);
                    cvMSG.put(MSG_MSGINFOID, infoid);
                    //case for text 60, media reply 62
                    cvMSG.put(MSG_ISSENDER, contactname.equals("1") ? 60 : 62);
                    //used for saving image thumbnail base64
                    cvMSG.put(MSG_MSGLATITUDE, Lat);
                    break;
                default:
                    break;

            }
        }
        rdb.insertQuery().insertMessage(Message.fromContentValues(cvMSG));

        updateChatListLastMessage(Jid, from.equals("booking") ? context.getString(R.string.booking_sent) : Body,
                "You", currentDate, false, false, null);
    }

    //ibrahim
    public void updateOfflineMsgToSend(String Jid, String Body, String uniqueId, long currentDate, int issender) {
        ContentValues cvMSG = new ContentValues();
        cvMSG.put(MSG_JID, Jid);
        cvMSG.put(MSG_ISSENDER, issender);
        cvMSG.put(MSG_MSGDATA, Body);
        cvMSG.put(MSG_MSGUNIQUEID, uniqueId);
        cvMSG.put(MSG_MSGDATE, currentDate);
        cvMSG.put(MSG_MSGOFFLINE, 0);

        updateRDB1Col(MSG_TABLE_NAME, cvMSG, MSG_MSGUNIQUEID, uniqueId);
    }

    //send out offline msges when xmpp reconnected
    public void getSendOfflineMsg() {
        ContentValues cv;
        List<Message> offlineMsg = rdb.selectQuery().getOfflineMsg();
        if (offlineMsg != null && offlineMsg.size() > 0) {
            for (Message message : offlineMsg
                    ) {
                String self_username = Preferences.getInstance().getValue(context, GlobalVariables.STRPREF_USERNAME);

                int issender = message.getIsSender();
                String jid = message.getMsgJid();
                if (jid.length() == 12) {
                    switch (issender) {

                        case 10:
                            String encryptedText = new EncryptionHelper().encryptText(message.getMsgData());

                            singleChatStanza.SoappChatStanza(encryptedText, message.getMsgJid(), self_username, message.getMsgUniqueId());

                            MessageOutputDatabase(message.getMsgJid(), message.getMsgData(),
                                    message.getMsgUniqueId(), message.getMsgDate());
                            break;

                        case 20:
                            singleChatStanza.SoappImageStanza(message.getContactname(), message.getMsgJid(), message.getMsgLongitude(), message.getContactnumber(), self_username, "0", message.getMsgUniqueId(), message.getMsgLatitude());
                            cv = new ContentValues();
                            cv.put(MSG_MSGOFFLINE, 0);
                            updateRDB1Col(MSG_TABLE_NAME, cv, MSG_MSGUNIQUEID, message.getMsgUniqueId());
                            break;

                        case 22:
                            singleChatStanza.SoappAudioStanza(message.getContactname(), message.getMsgJid(), message.getContactnumber(), self_username, message.getMsgUniqueId());
                            cv = new ContentValues();
                            cv.put(MSG_MSGOFFLINE, 0);
                            updateRDB1Col(MSG_TABLE_NAME, cv, MSG_MSGUNIQUEID, message.getMsgUniqueId());
                            break;

                        case 24:
                            singleChatStanza.SoappVideoStanza(message.getContactname(), message.getMsgJid(), message.getMsgLongitude(), message.getContactnumber(), self_username, message.getMsgUniqueId(), message.getMsgLatitude());
                            cv = new ContentValues();
                            cv.put(MSG_MSGOFFLINE, 0);
                            updateRDB1Col(MSG_TABLE_NAME, cv, MSG_MSGUNIQUEID, message.getMsgUniqueId());
                            break;


                        case 32:
                            singleChatStanza.SoappContactStanza(jid, message.getContactname(),
                                    message.getContactnumber(), message.getMsgUniqueId(), self_username);
                            ContactOutputDatabase(message.getMsgJid(), message.getMsgUniqueId(),
                                    message.getMsgDate(), message.getContactname(), message.getContactnumber());
                            break;

                        case 30:
                            singleChatStanza.SoappMapStanza(message.getMsgInfoUrl(), jid, message.getMsgLatitude(),
                                    message.getMsgLongitude(), message.getMsgUniqueId(), self_username, message.getMsgData(), message.getMsgInfoId());
                            MapOutputDatabase(message.getMsgJid(), message.getMsgInfoUrl(), message.getMsgUniqueId(),
                                    message.getMsgDate(), message.getMsgLatitude(), message.getMsgLongitude(), message.getMsgData(), message.getMsgInfoId());
                            break;

                        case 34:
                            singleChatStanza.SoappRestaurantStanza(message.getMsgInfoId(), jid,
                                    message.getMsgData(), self_username, message.getMsgUniqueId());
                            RestaurantOutgoingDatabase(message.getMsgUniqueId(), message.getMsgDate(),
                                    message.getMsgInfoUrl(), message.getMsgInfoId(), message.getMsgJid(),
                                    message.getMsgData());
                            break;

                        case 50:
                            Booking offbooking = rdb.selectQuery().getBookInfoforOff(message.getMsgInfoId());
                            Restaurant resinfo = rdb.selectQuery().getResTaurantInfoBasedResID(offbooking.getBookingResId());
                            singleChatStanza.SoappShareBookingStanza(offbooking.getBookingQRCode(), jid,
                                    offbooking.getBookingDate().toString(), offbooking.getBookingTime().toString(),
                                    offbooking.getBookingPax().toString(), offbooking.getBookingResId(), resinfo.getResTitle(),
                                    offbooking.getBookingPromo(), self_username, message.getMsgUniqueId(), message.getMsgInfoId());
                            outgoingResBookingShare(jid, offbooking.getBookingQRCode(), message.getMsgUniqueId(), message.getMsgDate(),
                                    message.getMsgInfoUrl(), offbooking.getBookingResId(), resinfo.getResTitle(), resinfo.getResLatitude(),
                                    resinfo.getResLongitude(), message.getMsgInfoId());

                            break;
                        case 60:
                            singleChatStanza.SoappReplyStanza(message.getMsgData(), message.getMsgJid(),
                                    self_username, message.getMsgUniqueId(), message.getMsgData(),
                                    message.getMsgJid(), message.getMsgInfoId(), "1", message.getMsgLatitude());

                            replyOutputDatabase(message.getMsgJid(), message.getMsgData(),
                                    message.getMsgUniqueId(), message.getMsgDate(), message.getMsgInfoId(), "1");
                            break;

                        case 62:
                            singleChatStanza.SoappReplyStanza(message.getMsgData(), message.getMsgJid(), self_username, message.getMsgUniqueId(), message.getMsgData(), message.getMsgJid(), message.getMsgInfoId(), "2", message.getMsgLatitude());

                            replyOutputDatabase(message.getMsgJid(), message.getMsgData(),
                                    message.getMsgUniqueId(), message.getMsgDate(), message.getMsgInfoId(), "2");
                            break;
                    }
                } else {
                    switch (issender) {

                        case 10:
                            groupChatStanza.GroupMessage(jid, message.getSenderJid(), self_username, new EncryptionHelper().encryptText(message.getMsgData()), message.getMsgUniqueId(), getNameFromContactRoster(jid));
                            GroupMessageOutputDatabase(message.getMsgJid(), message.getMsgData(), message.getMsgUniqueId(),
                                    message.getMsgDate(), message.getSenderJid());
                            break;

                        case 20:
                            groupChatStanza.GroupImage(jid, message.getSenderJid(), self_username, message.getMsgUniqueId(),
                                    message.getMsgLongitude(), message.getContactname(), message.getContactnumber(), getNameFromContactRoster(jid), "0", message.getMsgLatitude());

                            cv = new ContentValues();
                            cv.put(MSG_MSGOFFLINE, 0);
                            updateRDB1Col(MSG_TABLE_NAME, cv, MSG_MSGUNIQUEID, message.getMsgUniqueId());

                            break;

                        case 22:
                            groupChatStanza.GroupAudio(jid, message.getSenderJid(), self_username, message.getContactname(), message.getContactnumber(), message.getMsgUniqueId(), getNameFromContactRoster(jid));

                            cv = new ContentValues();
                            cv.put(MSG_MSGOFFLINE, 0);
                            updateRDB1Col(MSG_TABLE_NAME, cv, MSG_MSGUNIQUEID, message.getMsgUniqueId());
                            break;

                        case 24:
                            groupChatStanza.GroupVideo(jid, message.getSenderJid(), self_username, message.getMsgLongitude(), message.getMsgUniqueId(), message.getContactname(), getNameFromContactRoster(jid), message.getMsgLatitude());

                            cv = new ContentValues();
                            cv.put(MSG_MSGOFFLINE, 0);
                            updateRDB1Col(MSG_TABLE_NAME, cv, MSG_MSGUNIQUEID, message.getMsgUniqueId());
                            break;

                        case 32:
                            groupChatStanza.GroupContact(jid, message.getSenderJid(), self_username, message.getContactname(), message.getContactnumber(), message.getMsgUniqueId(), getNameFromContactRoster(jid));
                            GroupContactOutputDatabase(message.getMsgJid(), message.getMsgUniqueId(), message.getMsgDate(), message.getSenderJid(), message.getContactname(), message.getContactnumber());
                            break;

                        case 30:
                            groupChatStanza.GroupMap(jid, message.getSenderJid(), self_username, message.getMsgLatitude(), message.getMsgLongitude(), message.getMsgUniqueId(),
                                    message.getMsgInfoUrl(), getNameFromContactRoster(jid), message.getMsgData(), message.getMsgInfoId());
                            GroupMapOutputDatabase(message.getMsgJid(), message.getMsgInfoUrl(), message.getMsgUniqueId(), message.getMsgDate(), message.getSenderJid(),
                                    message.getMsgLatitude(), message.getMsgLongitude(), message.getMsgData(), message.getMsgInfoId());
                            break;

                        case 34:
                            groupChatStanza.GroupRestaurant(jid, message.getSenderJid(), self_username,
                                    message.getMsgData(), message.getMsgUniqueId(), message.getMsgInfoId(),
                                    getNameFromContactRoster(jid));
                            RestaurantGroupOutputDatabase(message.getMsgJid(), message.getMsgInfoUrl(), message.getMsgInfoId(), message.getMsgUniqueId(), message.getMsgData(), message.getMsgDate());
                            break;

                        case 50:
                            Booking offbooking = rdb.selectQuery().getBookInfoforOff(message.getMsgInfoId());
                            Restaurant resinfo = rdb.selectQuery().getResTaurantInfoBasedResID(offbooking.getBookingResId());
                            singleChatStanza.SoappShareBookingStanza(offbooking.getBookingQRCode(), jid, offbooking.getBookingDate().toString(),
                                    offbooking.getBookingTime().toString(), offbooking.getBookingPax().toString(), offbooking.getBookingResId(),
                                    resinfo.getResTitle(), offbooking.getBookingPromo(), self_username, message.getMsgUniqueId(), message.getMsgInfoId());
                            outgoingResBookingShare(jid, offbooking.getBookingQRCode(), message.getMsgUniqueId(), message.getMsgDate(),
                                    message.getMsgInfoUrl(), offbooking.getBookingResId(), resinfo.getResTitle(), resinfo.getResLatitude(),
                                    resinfo.getResLongitude(), message.getMsgInfoId());
                            break;

                        case 60:
                            groupChatStanza.GroupReply(jid, message.getSenderJid(), self_username, new EncryptionHelper().encryptText(message.getMsgData()), message.getMsgUniqueId(), getNameFromContactRoster(jid), message.getMsgData(), message.getSenderJid(), message.getMsgInfoId(), "1", message.getMsgLatitude());
                            GroupReplyOutputDatabase(message.getMsgJid(), message.getMsgData(), message.getMsgUniqueId(),
                                    message.getMsgDate(), message.getSenderJid(), message.getMsgInfoId(), "1");
                            break;
                        case 62:
                            groupChatStanza.GroupReply(jid, message.getSenderJid(), self_username, new EncryptionHelper().encryptText(message.getMsgData()), message.getMsgUniqueId(), getNameFromContactRoster(jid), message.getMsgData(), message.getSenderJid(), message.getMsgInfoId(), "2", message.getMsgLatitude());
                            GroupReplyOutputDatabase(message.getMsgJid(), message.getMsgData(), message.getMsgUniqueId(),
                                    message.getMsgDate(), message.getSenderJid(), message.getMsgInfoId(), "2");
                            break;
                    }
                }
            }
        }
        cv = null;
    }
    /*/////////////// [K01_03E] CHAT: offline-related ///////////////*/


    /*////////////// [K01_04] CHAT: Seen-related //////////////*/

    //other user seen msg, change to green tick
    public void SeenInputDatabase(String jid, String Body, String Id) {
        ContentValues cvMSG = new ContentValues();
        if (Body.equals("active")) {
            cvMSG.put(MSG_CHATMARKER, 1);

            updateRDB2Col(MSG_TABLE_NAME, cvMSG, MSG_JID, MSG_CHATMARKER, jid, "0");
        } else if (Body.equals("gone")) {
            cvMSG.put(MSG_CHATMARKER, 0);

            updateRDB2Col(MSG_TABLE_NAME, cvMSG, MSG_JID, MSG_CHATMARKER, jid, "1");
        }

        if (getChatListExist(jid) > 0) {
            ContentValues cvCL = new ContentValues();
            if (Body.equals("active")) {
                cvCL.put(CL_COLUMN_ONLINESTATUS, 1);
            } else if (Body.equals("gone")) {
                cvCL.put(CL_COLUMN_ONLINESTATUS, 0);

            }
            updateRDB1Col(CL_TABLE_NAME, cvCL, CL_COLUMN_JID, jid);
        }

        singleChatStanza.SoappAckStanza(jid, Id);
    }

    //chat status - typing: when opponent is typing
    public void chatStatusTyping(final String Jid, final String otherJid) {
        if (getChatListExist(Jid) > 0) {
            String typingName = "";

            //update typing... to chattab
            ContentValues cvCL = new ContentValues();

            cvCL.put(CL_COLUMN_TYPINGSTATUS, 1);

            if (Jid.length() > 12) { //grp, add typing name
                typingName = getIndiNameFromCR(otherJid);
                cvCL.put(CL_COLUMN_TYPINGNAME, typingName);
            }

            updateRDB1Col(CL_TABLE_NAME, cvCL, CL_COLUMN_JID, Jid);
        }
    }

    //chat status - when opponent stopped typing
    public void chatStatusStoppedTyping(final String Jid) {
        if (getChatListExist(Jid) > 0) {
            //remove typing... from chattab
            ContentValues cvCL = new ContentValues();

            cvCL.put(CL_COLUMN_TYPINGSTATUS, 0);

            updateRDB1Col(CL_TABLE_NAME, cvCL, CL_COLUMN_JID, Jid);
        }
    }

    //chat status - clear typing status on destroy of chattab
    public void chatStatusClear() {
        List<Integer> typingList = rdb.selectQuery().get_typing1FromCL();

        int typingRow;
        for (Integer i : typingList) {
            ContentValues cvCL = new ContentValues();

            cvCL.put(CL_COLUMN_TYPINGSTATUS, 0);

            updateRDB1Col(CL_TABLE_NAME, cvCL, CL_COLUMN_ROW, String.valueOf(i));
        }
    }

    /*////////////// [K01_04E] CHAT: Seen-related-END //////////////*/


    /*////////////// [K01_99] CHAT: OTHERS //////////////*/
    //[DONE CR] create new indi chatroom (chattab only, in and out)
    private void createNewIndiChat(String jid, String allMsg, long date, boolean isIncoming) {
        ContentValues cvCL = new ContentValues();

        if (isIncoming) {
            cvCL.put(CL_COLUMN_NOTIBADGE, 1);
        } else {
            cvCL.put(CL_COLUMN_NOTIBADGE, 0);
        }
        cvCL.put(CL_COLUMN_LASTMSG, allMsg);
        cvCL.put(CL_COLUMN_DATE, date);

        if (getChatListExist(jid) == 0) {
            cvCL.put(CL_COLUMN_JID, jid);

            rdb.insertQuery().insertChatList(ChatList.fromContentValues(cvCL));
        } else {
            updateRDB1Col(CL_TABLE_NAME, cvCL, CL_COLUMN_JID, jid);
        }
    }

    //create new CHAT group in chatlist (in and out)
    public void createNewGrpChat(String room_id, long currentDate, String room_name, byte[] groupimage33,
                                 String resource_url, boolean selfIsAdmin, String otherJid, boolean isAdding) {
        //set info for Contact Roster first (either new grp or existing grp needs updating)
        ContentValues cvCR = new ContentValues();

        cvCR.put(CR_COLUMN_DISPLAYNAME, room_name);
        if (resource_url != null) {
            cvCR.put(CR_COLUMN_PHOTOURL, resource_url);
        }

        cvCR.put(CR_COLUMN_PROFILEPHOTO, groupimage33);
        cvCR.put(CR_COLUMN_DISABLEDSTATUS, 0);

        if (getContactRosterExist(room_id) == 0) {
            cvCR.put(CR_COLUMN_JID, room_id);

            rdb.insertQuery().insertContactRoster(ContactRoster.fromContentValues(cvCR));
        } else {
            updateRDB1Col(CR_TABLE_NAME, cvCR, CR_COLUMN_JID, room_id);
        }

        //set info for chatlist
        ContentValues cvCL = new ContentValues();

        cvCL.put(CL_COLUMN_JID, room_id);
        cvCL.put(CL_COLUMN_DATE, currentDate);

        if (!selfIsAdmin) {
            cvCL.put(CL_COLUMN_ADMIN_SELF, 0);
        } else {
            cvCL.put(CL_COLUMN_ADMIN_SELF, 1);
        }

        String allMsg;

        if (otherJid == null) {
            cvCL.put(CL_COLUMN_LASTSENDERNAME, context.getString(R.string.you));
        } else {
            cvCL.put(CL_COLUMN_LASTSENDERNAME, getNameFromContactRoster(otherJid));
        }

        //set msg for create grp or add grp
        if (isAdding) {
            allMsg = context.getString(R.string.grp_added);
        } else {
            allMsg = context.getString(R.string.grp_created);
        }

        cvCL.put(CL_COLUMN_LASTMSG, allMsg);
        if (getChatListExist(room_id) == 0) { //room doesn't exist, create new group
            cvCL.put(CL_COLUMN_JID, room_id);

            rdb.insertQuery().insertChatList(ChatList.fromContentValues(cvCL));

        } else { //room exists, only update values
            updateRDB1Col(CL_TABLE_NAME, cvCL, CL_COLUMN_JID, room_id);
        }

        //add grp status msg to MESSAGE
        if (otherJid == null) {//self
            groupStatusMsgPlaySound(room_id, context.getString(R.string.you), allMsg, false, false);
        } else { //incoming
            groupStatusMsgPlaySound(room_id, getNameFromContactRoster(otherJid), allMsg, false, false);
        }
    }

    //Add date header in chatlog
    public void addNewTime(String jid, long date) {
//
//        Cursor cursor = db.rawQuery("select _id from Message where jidstr = '" + jid + "' AND " +
//                "date = '" + date + "'", null);
        Cursor cursor = rdb.selectQuery().addNewTime(jid, date);
        if (cursor.getCount() == 0) {
            ContentValues cvMSG = new ContentValues();

            cvMSG.put(MSG_JID, jid);
            cvMSG.put(MSG_MSGDATE, date);
            cvMSG.put(MSG_ISSENDER, 0);

            if (jid.length() > 12) { //add other jid for grp
                cvMSG.put(MSG_SENDERJID, "");
            }
            rdb.insertQuery().insertMessage(Message.fromContentValues(cvMSG));
        }
        cursor.close();
    }

    //delete indi chat room
    public void selfDeleteIndiChat(String jid) {
        //clear msges of the room in MESSAGE
        clearMsgesInMsg(jid);

        //delete chat room in CL
        deleteRDB1Col(CL_TABLE_NAME, CL_COLUMN_JID, jid);

        //delete all appts accosiated to room
        deleteRDB1Col(A_TABLE_NAME, A_COLUMN_JID, jid);
    }

    /*////////////// [K01_99E] CHAT: OTHERS-END //////////////*/
    /*================================ [J01E - CHAT-RELATED-END] ===============================*/


    /*============================ [J02 - APPOINTMENT-RELATED] ===========================*/

    /*////////////// [K02_01] APPT: Outgoing-related //////////////*/
    //[DONE INDI] create NEW indi chat sche (in and out), create new SCHE for grps (in and out)
    //--this does NOT create new group, just creates new SCHE for grp--
    private void createNewChatListFromAppt(String jid) {
        String allmsg = context.getString(R.string.appt_created);
        long date = System.currentTimeMillis();

        //create new chat room in chatlist
        ContentValues cvCL = new ContentValues();

        cvCL.put(CL_COLUMN_JID, jid);
        cvCL.put(CL_COLUMN_NOTIBADGE, 0);
        cvCL.put(CL_COLUMN_DATE, date);
        cvCL.put(CL_COLUMN_LASTMSG, allmsg);

        rdb.insertQuery().insertChatList(ChatList.fromContentValues(cvCL));
    }

    public void getNewGrpApptDetailsFromApi(String roomjid, String otherjid, String uniqueID, boolean playsound, String apptID) {
        String access_token = preferences.getValue(context, GlobalVariables.STRPREF_ACCESS_TOKEN);
        String selfJid = preferences.getValue(context, GlobalVariables.STRPREF_USER_ID);

        CreateApptForExistGrp client = RetrofitAPIClient.getClient().create(CreateApptForExistGrp.class);

        retrofit2.Call<GetGrpApptModel> call = client.getGrpApptInfo("Bearer "
                + access_token, apptID);

        call.enqueue(new retrofit2.Callback<GetGrpApptModel>() {
            @Override
            public void onResponse(retrofit2.Call<GetGrpApptModel> call, retrofit2.Response<GetGrpApptModel> response) {
                if (!response.isSuccessful()) {
                    //send error to server
                    try {
                        saveLogsToDb("getNewGrpApptDetailsFromApi", "jid = " + roomjid + " otherjid = " + otherjid +
                                " uniqueID = " + uniqueID + " apptID = " + apptID + " respUnsuc = " + response.errorBody().string(), System.currentTimeMillis());
                    } catch (IOException ignore) {
                    }

                    int errorCode = response.code();

                    switch (errorCode) {
                        case 403:
                        case 404:
                            //ack server if appt not found/expired/no permission (self already left grp)
                            groupChatStanza.GroupAckStanza(roomjid, selfJid, otherjid, uniqueID);

                            break;

                        default:
                            new MiscHelper().retroLogUnsuc(response, "onResponse: ", "JAY");
                            break;
                    }
                    return;
                }
                GetGrpApptModel res = response.body();

                for (AppointmentMemStatus s : res.getAppointment_users()
                        ) {
                    checkAndAddStatusToGrpMemStatus(s.getUser_jid(), roomjid, apptID, s.getAppointment_status());
                }

                incomingApptDetails(roomjid, uniqueID, res.getAppointment_title(), res.getLocation_map_url(),
                        res.getLocation_address(), res.getLocation_lon(), res.getLocation_lat(),
                        res.getAppointment_date(), res.getAppointment_time(), null,
                        "", "", playsound, otherjid, apptID, "5");
            }

            @Override
            public void onFailure(retrofit2.Call<GetGrpApptModel> call, Throwable t) {
                new MiscHelper().retroLogFailure(t, "getNewGrpApptDetailsFromApi", "JAY");
            }
        });


    }

    //[DONE INDI] create NEW appt ONLY (new apptID, grp and single)
    public String createNewAppt(String jid, String apptTitle, String apptMapURL, String apptLat,
                                String apptLon, long apptDate, long apptTime, String apptLoc,
                                String resID, String resImgURL, String apptID, String uniqueID) {
        ContentValues cvAppt = new ContentValues();
        String myjid = Preferences.getInstance().getValue(Soapp.getInstance().getApplicationContext(), GlobalVariables.STRPREF_USER_ID);

        //new apptID = jid + currentTime mili
        if (apptID == null) {
            apptID = String.format("%s-%s-%s", jid, myjid, System.currentTimeMillis());
        }

        cvAppt.put(A_COLUMN_ID, apptID);
        cvAppt.put(A_COLUMN_JID, jid);
        cvAppt.put(A_COLUMN_TITLE, apptTitle);
        cvAppt.put(A_COLUMN_LOCATION, apptLoc);
        cvAppt.put(A_COLUMN_LOCMAPURL, apptMapURL);
        cvAppt.put(A_COLUMN_LATITUDE, apptLat);
        cvAppt.put(A_COLUMN_LONGITUDE, apptLon);
        cvAppt.put(A_COLUMN_DATE, apptDate);
        cvAppt.put(A_COLUMN_TIME, apptTime);
        cvAppt.put(A_COLUMN_SELFSTATUS, 0); //hosting

        //add res if got (cater for future make appt with res
        if (resID != null) {
            cvAppt.put(A_COLUMN_RESID, resID);
            cvAppt.put(A_COLUMN_RESIMGURL, resImgURL);
        }

        //insert new row to appt table
        rdb.insertQuery().insertAppointment(Appointment.fromContentValues(cvAppt));

        //if chatroom not exist, create new chat room
        if (getChatListExist(jid) == 0) {
            createNewChatListFromAppt(jid);
        }

        //insert "appt created" msg
        String allMsg = context.getString(R.string.appt_created);
        long currentTime = System.currentTimeMillis();

        checkAndAddApptUpdateMsg(jid, null, allMsg, uniqueID, currentTime, false, false,
                apptID, null, 70);

        //if grp, need to add to GROUPMEM_STATUS table as well
        if (jid.length() > 12) { //grp
            List<String> grpMemJidList = get_jidFromGrpMem(jid);

            for (int i = 0; i < grpMemJidList.size(); i++) {
                checkAndAddStatusToGrpMemStatus(grpMemJidList.get(i), jid, apptID, 2);
            }
        }

        return apptID;
    }

    //outgoing create new grp CHAT and APPT (new ApptID)
    public void outgoingNewGrpChatAppt(String roomJid, String apptTitle, String apptMapURL, String apptLat,
                                       String apptLon, long apptDate, long apptTime, String apptLoc,
                                       String resID, String resImgURL, String roomName, byte[] grpProfileImg,
                                       String resourceURL, String apptID, String uniqueID) {
        //create new chat for chatlist
        String allMsg = context.getString(R.string.grp_created);
        long currentTime = System.currentTimeMillis();

        ContentValues cvCL = new ContentValues();

        cvCL.put(CL_COLUMN_JID, roomJid);
        cvCL.put(CL_COLUMN_DATE, currentTime);
        cvCL.put(CL_COLUMN_ADMIN_SELF, 1);

        rdb.insertQuery().insertChatList(ChatList.fromContentValues(cvCL));

        //create chatroom in chatlist with grp creation msg
        groupStatusMsgPlaySound(roomJid, context.getString(R.string.you), allMsg, false, false);

        //create appt creation msg
        allMsg = context.getString(R.string.grp_appt_created);
        checkAndAddApptUpdateMsg(roomJid, null, allMsg, uniqueID, currentTime + 1,
                false, false, apptID, null, 70);

        //create new row for CR (room profile)
        ContentValues cvCR = new ContentValues();

        cvCR.put(CR_COLUMN_JID, roomJid);
        cvCR.put(CR_COLUMN_DISPLAYNAME, roomName);
        cvCR.put(CR_COLUMN_PROFILEPHOTO, grpProfileImg);

        //for incoming full img - outgoing full img already in folder
        if (resourceURL != null) {
            cvCR.put(CR_COLUMN_PHOTOURL, resourceURL);
        }
        rdb.insertQuery().insertContactRoster(ContactRoster.fromContentValues(cvCR));

        //create new row for appt (grp appt)
        ContentValues cvAppt = new ContentValues();

        cvAppt.put(A_COLUMN_ID, apptID);
        cvAppt.put(A_COLUMN_JID, roomJid);
        cvAppt.put(A_COLUMN_TITLE, apptTitle);
        cvAppt.put(A_COLUMN_LOCATION, apptLoc);
        cvAppt.put(A_COLUMN_LOCMAPURL, apptMapURL);
        cvAppt.put(A_COLUMN_LATITUDE, apptLat);
        cvAppt.put(A_COLUMN_LONGITUDE, apptLon);
        cvAppt.put(A_COLUMN_DATE, apptDate);
        cvAppt.put(A_COLUMN_TIME, apptTime);
        cvAppt.put(A_COLUMN_SELFSTATUS, 0); //hosting

        //add res if got (cater for future make appt with res
        if (resID != null) {
            cvAppt.put(A_COLUMN_RESID, resID);
            cvAppt.put(A_COLUMN_RESIMGURL, resImgURL);
        }

        //insert to appt table
        rdb.insertQuery().insertAppointment(Appointment.fromContentValues(cvAppt));
    }

    //outgoing appt title (indi and grp)
    public void outgoingApptTitle(String jid, String apptID, String apptTitle, String uniqueID) {
        //get old appt title before updating appt db
        String oldApptTitle = getApptCurrentTitle(apptID);

        ContentValues cvAppt = new ContentValues();

        cvAppt.put(A_COLUMN_TITLE, apptTitle);

        updateRDB1Col(A_TABLE_NAME, cvAppt, A_COLUMN_ID, apptID);

        //insert appt status msg to MSG
        String selfJid = preferences.getValue(context, GlobalVariables.STRPREF_USER_ID);
        String allMsg = context.getString(R.string.appt_title_changed);

        checkAndAddApptUpdateMsg(jid, selfJid, allMsg, uniqueID, System.currentTimeMillis(), false,
                false, apptID, oldApptTitle, 73);
    }

    //outgoing change appt date or time
    public void outgoingApptDateTime(String jid, String apptID, long apptDate, long apptTime, String uniqueID) {
        //get old loc
        String oldApptTime = String.valueOf(getApptTime(apptID));

        ContentValues cvAppt = new ContentValues();

        cvAppt.put(A_COLUMN_DATE, apptDate);
        cvAppt.put(A_COLUMN_TIME, apptTime);

        updateRDB1Col(A_TABLE_NAME, cvAppt, A_COLUMN_ID, apptID);

        //insert appt status msg to MSG
        String selfJid = preferences.getValue(context, GlobalVariables.STRPREF_USER_ID);
        String allMsg = context.getString(R.string.appt_location_changed);

        checkAndAddApptUpdateMsg(jid, selfJid, allMsg, uniqueID, System.currentTimeMillis(), false,
                false, apptID, oldApptTime, 75);

//        setReminderUsingWorker(apptID, Long.valueOf(String.format("%d%d",apptDate , apptTime)));
    }

    public void setReminderInDB(String apptId, long reminderTime) {
        ContentValues values = new ContentValues();
        long reminderTimeMilli = TimeUnit.MINUTES.toMillis(reminderTime);
        values.put("ApptReminderTime", reminderTimeMilli);

        updateRDB1Col(A_TABLE_NAME, values, A_COLUMN_ID, apptId);

    }

    public int setReminderUsingWorker(String apptID, final Long apptime, long customReminderTime, String uuid, String jid) {
        if (uuid != null && !uuid.isEmpty()) {
            WorkManager.getInstance().cancelWorkById(UUID.fromString(uuid));
        }
        //get from db, no value take from prefrences
        customReminderTime = customReminderTime < 0 ? TimeUnit.MINUTES.toMillis(Preferences.getInstance().getIntValue(Soapp.getInstance().getApplicationContext(), GlobalVariables.STRPREF_NOTIFICATION_TIME)) : customReminderTime;

        //if preferences -1 (null) take from default : 60min
        customReminderTime = customReminderTime < 0 ? TimeUnit.MINUTES.toMillis(GlobalVariables.defaultReminderAlert) : customReminderTime;

        // milliseconds from now until reminder
        long remindertime = apptime - customReminderTime - System.currentTimeMillis();


        //if reminder time is already in the past, just alert user count
        int pastApptReminderCount = 0;
        if (remindertime < 0) {
            pastApptReminderCount++;

        } else {
            Data data = new Data.Builder()
                    .putString("apptId", apptID)
                    .putLong("time", TimeUnit.MILLISECONDS.toMinutes(customReminderTime))
                    .putString("jid", jid)
                    .build();

            OneTimeWorkRequest oneTimeWorkRequest = new OneTimeWorkRequest.Builder(ReminderWorker.class)
                    .setInputData(data)
                    .addTag("ReminderAppt")
                    .setInitialDelay(remindertime, TimeUnit.MILLISECONDS)
                    .setBackoffCriteria(BackoffPolicy.LINEAR, WorkRequest.MIN_BACKOFF_MILLIS, TimeUnit.SECONDS)
                    .build();

            WorkManager.getInstance().enqueue(oneTimeWorkRequest);

            ContentValues cv = new ContentValues();
            cv.put(AWID_COLUMN_REMINDERUUID, oneTimeWorkRequest.getId().toString());

            if (getApptWorkIDExits(apptID))
                updateRDB1Col(AWID_TABLE_APPTWORKUUID, cv, AWID_COLUMN_APPTID, apptID);
            else
                rdb.insertQuery().insertApptWorkId(new ApptWorkUUID(apptID, cv.getAsString(AWID_COLUMN_REMINDERUUID), "", ""));
        }

        return pastApptReminderCount;
    }

    public void exactTimeApptReminder(String apptId, final Long apptime, String uuid, String jid) {
        if (uuid != null && !uuid.isEmpty()) {
            WorkManager.getInstance().cancelWorkById(UUID.fromString(uuid));
        }

        Data data = new Data.Builder()
                .putString("apptId", apptId)
                .putString("jid", jid)
                .build();

        long delaytime = apptime - System.currentTimeMillis();

        OneTimeWorkRequest oneTimeWorkRequest = new OneTimeWorkRequest.Builder(ExactTimeWorker.class)
                .setInputData(data)
                .addTag("ExactReminderAppt")
                .setInitialDelay(delaytime, TimeUnit.MILLISECONDS)
                .setBackoffCriteria(BackoffPolicy.LINEAR, WorkRequest.MIN_BACKOFF_MILLIS, TimeUnit.SECONDS)
                .build();

        WorkManager.getInstance().enqueue(oneTimeWorkRequest);

        ContentValues cv = new ContentValues();
        cv.put(AWID_COLUMN_EXACTUUID, oneTimeWorkRequest.getId().toString());

        if (getApptWorkIDExits(apptId))
            updateRDB1Col(AWID_TABLE_APPTWORKUUID, cv, AWID_COLUMN_APPTID, apptId);
        else
            rdb.insertQuery().insertApptWorkId(new ApptWorkUUID(apptId, "", cv.getAsString(AWID_COLUMN_EXACTUUID), ""));

    }

    public void setClearAfter3HWorker(String apptId, final Long apptime, String uuid) {

        if (uuid != null && !uuid.isEmpty()) {
            WorkManager.getInstance().cancelWorkById(UUID.fromString(uuid));
        }

        Data data = new Data.Builder()
                .putString("apptId", apptId)
                .build();

        long delaytime = (apptime + TimeUnit.HOURS.toMillis(3)) - System.currentTimeMillis();

        OneTimeWorkRequest oneTimeWorkRequest = new OneTimeWorkRequest.Builder(DeleteApptWorker.class)
                .setInputData(data)
                .addTag("DeleteReminderAppt")
                .setInitialDelay(delaytime, TimeUnit.MILLISECONDS)
                .setBackoffCriteria(BackoffPolicy.LINEAR, WorkRequest.MIN_BACKOFF_MILLIS, TimeUnit.SECONDS)
                .build();

        WorkManager.getInstance().enqueue(oneTimeWorkRequest);

        ContentValues cv = new ContentValues();
        cv.put(AWID_COLUMN_DELETEUUID, oneTimeWorkRequest.getId().toString());
        updateRDB1Col(AWID_TABLE_APPTWORKUUID, cv, AWID_COLUMN_APPTID, apptId);

        if (getApptWorkIDExits(apptId))
            updateRDB1Col(AWID_TABLE_APPTWORKUUID, cv, AWID_COLUMN_APPTID, apptId);
        else
            rdb.insertQuery().insertApptWorkId(new ApptWorkUUID(apptId, "", "", cv.getAsString(AWID_COLUMN_DELETEUUID)));
    }

    //outgoing appt location (including restaurant sharing to appt and QR sharing to appt)
    public void outgoingApptLocation(String jid, String apptID, String mapURL, String latitude,
                                     String longitude, String locationName, String resID, String resImgURL,
                                     String uniqueID) {
        //get current loc title
        String oldApptLoc = getApptLoc(apptID);

        ContentValues cvAppt = new ContentValues();

        cvAppt.put(A_COLUMN_LOCMAPURL, mapURL);
        cvAppt.put(A_COLUMN_LATITUDE, latitude);
        cvAppt.put(A_COLUMN_LONGITUDE, longitude);

        if (locationName == null || locationName.length() == 0) {
            locationName = latitude + ", " + longitude;
        }
        cvAppt.put(A_COLUMN_LOCATION, locationName);

        //if restaurant, need add resID as well
        if (!resID.equals("")) {
            cvAppt.put(A_COLUMN_RESID, resID);
            cvAppt.put(A_COLUMN_RESIMGURL, resImgURL);
        }

        updateRDB1Col(A_TABLE_NAME, cvAppt, A_COLUMN_ID, apptID);

        //insert appt status msg to MSG
        String selfJid = preferences.getValue(context, GlobalVariables.STRPREF_USER_ID);
        String allMsg = context.getString(R.string.appt_location_changed);

        checkAndAddApptUpdateMsg(jid, selfJid, allMsg, uniqueID, System.currentTimeMillis(), false,
                false, apptID, oldApptLoc, 77);
    }

    //[DONE INDI] outgoing single appointment status (going, undecided...)
    public void outgoingApptStatus(String jid, String apptID, int apptStatus, String uniqueID) {
        //get self's current status
        String selfStatus = String.valueOf(getApptStatusSelf(apptID));

        ContentValues cvAppt = new ContentValues();

        cvAppt.put(A_COLUMN_SELFSTATUS, apptStatus);

        updateRDB1Col(A_TABLE_NAME, cvAppt, A_COLUMN_ID, apptID);

        //insert appt status msg to MSG
        String selfJid = preferences.getValue(context, GlobalVariables.STRPREF_USER_ID);
        String allMsg = context.getString(R.string.appt_location_changed);

        checkAndAddApptUpdateMsg(jid, selfJid, allMsg, uniqueID, System.currentTimeMillis(), false,
                false, apptID, selfStatus, 78);

        //if user set to not going, clear all appt noti badges
        if (apptStatus == 3) {
            zeroBadgeApptRoom(apptID, jid.length() == 12 ? 0 : 1);
        }
    }

    //[DONE INDI] self delete appt ONLY, just change setdate to 0 to keep other info
    public int deleteApptID(String jid, String apptID, boolean needCheckHost, String deletedByJid, String uniqueID, boolean isIncoming) {
        if (needCheckHost && getApptStatusSelf(apptID) != 0) { //not host CAN'T delete
            return 0;
        }

        //all other cases (can straight delete)
        //get list of appt details from appt table
        Appointment appointmentList = getAllApptDet(apptID);

        if (appointmentList != null) {
            //build cv for apptH table
            ContentValues cvAH = new ContentValues();

            cvAH.put(AH_COLUMN_ID, appointmentList.getApptID());
            cvAH.put(AH_COLUMN_JID, appointmentList.getApptJid());
            cvAH.put(AH_COLUMN_TITLE, appointmentList.getApptTitle());
            cvAH.put(AH_COLUMN_DATE, appointmentList.getApptDate());
            cvAH.put(AH_COLUMN_TIME, appointmentList.getApptTime());
            cvAH.put(AH_COLUMN_LOCATION, appointmentList.getApptLocation());
            cvAH.put(AH_COLUMN_LATITUDE, appointmentList.getApptLatitude());
            cvAH.put(AH_COLUMN_LONGITUDE, appointmentList.getApptLongitude());
            cvAH.put(AH_COLUMN_LOCMAPURL, appointmentList.getApptMapURL());
            cvAH.put(AH_COLUMN_DESC, appointmentList.getApptDesc());
            cvAH.put(AH_COLUMN_SELFSTATUS, appointmentList.getSelf_Status());
            cvAH.put(AH_COLUMN_FRIENDSTATUS, appointmentList.getFriend_Status());
            cvAH.put(AH_COLUMN_RESID, appointmentList.getApptResID());
            cvAH.put(AH_COLUMN_RESIMGURL, appointmentList.getApptResImgURL());

            //action for manually deleted
            if (deletedByJid != null) {
                cvAH.put(AH_COLUMN_ISDELETED, deletedByJid);

                //insert appt status msg to MSG
                String allMsg = context.getString(R.string.appt_deleted);

                if (isIncoming) {
                    checkAndAddApptUpdateMsg(jid, deletedByJid, allMsg, uniqueID, System.currentTimeMillis(), false,
                            false, apptID, null, 81);
                } else {
                    checkAndAddApptUpdateMsg(jid, deletedByJid, allMsg, uniqueID, System.currentTimeMillis(), false,
                            false, apptID, null, 80);
                }
            }

            //insert details to apptHist table
            if (getApptHistExist(apptID) == 0) {
                rdb.insertQuery().insertAppointmentHist(AppointmentHist.fromContentValues(cvAH));
            } else {
                updateRDB1Col(AH_TABLE_NAME, cvAH, AH_COLUMN_ID, apptID);
            }
        }

        //delete details from appt table
        deleteRDB1Col(A_TABLE_NAME, A_COLUMN_ID, apptID);

        return 1;
    }

    /*////////////// [K02_01E] APPT: Outgoing-related-END //////////////*/


    /*////////////// [K02_02] APPT: Incoming-related //////////////*/
    //[DONE INDI] Incoming Appt Details (indi + grp) - existing and new appt
    public void incomingApptDetails(
            String jid, String uniqueID, String apptTitle, String apptMapURL, String apptLocation,
            String apptLon, String apptLat, String apptDate, String apptTime, String apptStatusStr,
            String resID, String resImgURL, boolean playSound, String otherJid, String apptID, String type) {

        //start set cv for appointment
        ContentValues cvAppt = new ContentValues();

        long dateLong, timeLong = 0;
        String lastMsgCL = null;

        //set notificaiton type
        boolean needNotiBadge = false;
        int selfStatus = getApptStatusSelf(apptID);
        if (selfStatus != 3 && !preferences.getValue(context, "seeing_current_appt").equals(apptID)) {
            //only add badge if self status is NOT not going and NOT seeing current room
            //set boolean for need noti badge
            needNotiBadge = true;
        }

        //friend changes title
        if (apptTitle.length() > 0) {
            cvAppt.put(A_COLUMN_TITLE, apptTitle);
        }

        //friend changes map (location)
        if (apptMapURL.length() > 0) {
            cvAppt.put(A_COLUMN_LOCMAPURL, apptMapURL);
            cvAppt.put(A_COLUMN_LOCATION, apptLocation);
            cvAppt.put(A_COLUMN_LATITUDE, apptLat);
            cvAppt.put(A_COLUMN_LONGITUDE, apptLon);
            if (resID != null) {
                cvAppt.put(A_COLUMN_RESID, resID);
            }
            if (resImgURL != null) {
                cvAppt.put(A_COLUMN_RESIMGURL, resImgURL);
            }
        }

        //friend changes date
        if (apptDate.length() > 0) {
            if (apptDate.equals("nil")) { //friend clears appt
                //re-route to codes below for unwrapping
                type = "0";
            } else { //date/time updated
                SimpleDateFormat dateFormat = new SimpleDateFormat("EEE, dd MMM yyyy", Locale.ENGLISH);
                DateFormat dateTimeFormat = new SimpleDateFormat("EEE, dd MMM yyyy h:mm a", Locale.ENGLISH);

                try {
                    dateLong = dateFormat.parse(apptDate).getTime();

                    cvAppt.put(A_COLUMN_DATE, dateLong);

                    //need to get full time long for android sqlite (for checking reminder/etc purposes)
                    timeLong = dateTimeFormat.parse(apptDate + " " + apptTime).getTime();
                    cvAppt.put(A_COLUMN_TIME, timeLong);

                } catch (ParseException e) {
                    cvAppt.put(A_COLUMN_DATE, getExactDateToday());
                    cvAppt.put(A_COLUMN_TIME, System.currentTimeMillis());

                    cancelPendingAlarm(apptID);
                }
            }
        }

        //friend changes status (going, undecided, not going)
        int apptStatusInt; //default use 99 in order to be higher than 1
        if (apptStatusStr != null && apptStatusStr.length() > 0) {
            //get user's phonename
            String phoneName;
            if (jid.length() == 12) {
                phoneName = getNameFromContactRoster(jid);
            } else {
                phoneName = getNameFromContactRoster(otherJid);
            }

            //change status str to int
            switch (apptStatusStr) {
                case "0":
                case "Host":
                    apptStatusInt = 0;
                    lastMsgCL = phoneName + " " + context.getString(R.string.appt_is_hosting);
                    break;

                case "1":
                case "Going":
                    apptStatusInt = 1;
                    lastMsgCL = phoneName + " " + context.getString(R.string.appt_is_going);
                    break;

                case "2":
                case "Undecided":
                    apptStatusInt = 2;
                    lastMsgCL = phoneName + " " + context.getString(R.string.appt_is_undecided);
                    break;

                case "3":
                case "Not Going":
                    apptStatusInt = 3;
                    lastMsgCL = phoneName + " " + context.getString(R.string.appt_is_not_going);
                    break;

                default:
                    apptStatusInt = 2;
                    lastMsgCL = phoneName + " " + context.getString(R.string.appt_is_undecided);
                    break;
            }

            if (jid.length() == 12) { //indi
                cvAppt.put(A_COLUMN_FRIENDSTATUS, apptStatusInt);
            } else { //grp
                checkAndAddStatusToGrpMemStatus(otherJid, jid, apptID, apptStatusInt);
            }
        }
        cvAppt.put(A_COLUMN_JID, jid);

        //create new chat room if not exist in chat list
        if (getChatListExist(jid) == 0) {
            if (jid.length() == 12) {
                createNewChatListFromAppt(jid);
            } else {
                incomingCreateNewGrpChat(jid, System.currentTimeMillis(), otherJid, uniqueID, playSound,
                        false);
            }
        }

        //add appt status msg based on type and play sound
        int isSender = 82;
        String oldApptDetails = null;
        switch (type) {
            case "0": //incoming delete appt (currently taken care by date = "nil")
                //clear pending reminder/clear appt det alarms
                cancelPendingAlarm(apptID);

                //acknowledge then return
                if (jid.length() == 12) {
                    deleteApptID(jid, apptID, false, jid, uniqueID, true);

                    singleChatStanza.SoappAckStanza(jid, uniqueID);
                } else {
                    deleteApptID(jid, apptID, false, otherJid, uniqueID, true);

                    String user_jid = preferences.getValue(context, GlobalVariables.STRPREF_USER_ID);

                    if (uniqueID != null) {
                        groupChatStanza.GroupAckStanza(jid, user_jid, otherJid, uniqueID);
                    }
                }
                //return since appt deleted
                return;

            case "1": //incoming title updated
                lastMsgCL = context.getString(R.string.appt_title_changed);
                isSender = 73;
                oldApptDetails = getApptCurrentTitle(apptID);

                //add noti badge for title
                if (needNotiBadge) {
                    cvAppt.put(A_COLUMN_APPT_NOTI_BADGE, 1);
                    cvAppt.put(A_COLUMN_APPT_NOTI_TITLE, 1);
                }
                break;

            case "2": //incoming date/time updated
                lastMsgCL = context.getString(R.string.appt_date_changed) + " " + apptDate;
                isSender = 75;
                oldApptDetails = String.valueOf(getApptTime(apptID));

                //add noti badge for date
                if (needNotiBadge) {
                    cvAppt.put(A_COLUMN_APPT_NOTI_BADGE, 1);
                    cvAppt.put(A_COLUMN_APPT_NOTI_DATE, 1);
                }
                break;

            case "3": //incoming location updated
                lastMsgCL = context.getString(R.string.appt_location_changed);
                isSender = 77;
                oldApptDetails = getApptLoc(apptID);

                //add noti badge for location
                if (needNotiBadge) {
                    cvAppt.put(A_COLUMN_APPT_NOTI_BADGE, 1);
                    cvAppt.put(A_COLUMN_APPT_NOTI_LOC, 1);
                }
                break;

            case "4": //incoming status updated
                lastMsgCL = context.getString(R.string.appt_status_changed);
                isSender = 79;
                oldApptDetails = String.valueOf(getApptStatusGM(apptID, jid, otherJid));
                break;

            case "5": //incoming create appt
                lastMsgCL = context.getString(R.string.appt_created);
                isSender = 71;

                if (needNotiBadge) {
                    cvAppt.put(A_COLUMN_APPT_NOTI_BADGE, 1);
                }
                break;

            case "99": //from syncing groups, just show "apptTitle details updated"
                lastMsgCL = context.getString(R.string.appt_updated);
                isSender = 82;
                break;
        }

        //update appt msg (don't update if type 99)
        if (!type.equals("99")) {
            checkAndAddApptUpdateMsg(jid, otherJid, lastMsgCL, uniqueID, System.currentTimeMillis(), playSound,
                    true, apptID, oldApptDetails, isSender);
        }

        //insert or update APPOINTMENT sqlite
        if (getApptExist(apptID) > 0) { //appt id exists
            updateRDB1Col(A_TABLE_NAME, cvAppt, A_COLUMN_ID, apptID);
        } else { //appt not exist, set self status to invited (2) and host to friend or sender
            if (jid.length() > 12) { //only need add new GMS rows if grp
                //since appt does NOT exist, need to add all grpmems into GMS with 2 as status
                List<String> grpMemJidList = get_jidFromGrpMem(jid);

                String memberJid;
                for (int i = 0; i < grpMemJidList.size(); i++) {
                    memberJid = grpMemJidList.get(i);

                    //for non-sender, set to invited/undecided
                    if (!memberJid.equals(otherJid)) {
                        checkAndAddStatusToGrpMemStatus(memberJid, jid, apptID, 2);
                    } else { //for sender, set to host
                        checkAndAddStatusToGrpMemStatus(memberJid, jid, apptID, 0);
                    }
                }
            } else { //indi, just set friend's status to hosting
                cvAppt.put(A_COLUMN_FRIENDSTATUS, 0);
            }
            cvAppt.put(A_COLUMN_SELFSTATUS, 2);
            cvAppt.put(A_COLUMN_ID, apptID);

            rdb.insertQuery().insertAppointment(Appointment.fromContentValues(cvAppt));
        }

        //acknowledge server
        if (jid.length() == 12) {
            singleChatStanza.SoappAckStanza(jid, uniqueID);
        } else {
            String user_jid = preferences.getValue(context, GlobalVariables.STRPREF_USER_ID);

            if (uniqueID != null) {
                groupChatStanza.GroupAckStanza(jid, user_jid, otherJid, uniqueID);
            }
        }
        //for workmanager reminder + del
        if (timeLong > System.currentTimeMillis()) {
            scheduleLocalNotification(apptID);
        }
    }

    //[DONE] appt update msg - otherJid only when group (indi and grp, in and out)
    private void checkAndAddApptUpdateMsg(String jid, String otherJid, String lastMsgCL, String uniqueID, long dateLong,
                                          boolean playSound, boolean needNotiBadge, String apptID,
                                          String oldApptDet, int isSender) {
        if (getChatListExist(jid) > 0) {
            //update chat room msg
            ContentValues cvMSG = new ContentValues();

            //basic info
            cvMSG.put(MSG_JID, jid);
            cvMSG.put(MSG_MSGDATE, dateLong);
            cvMSG.put(MSG_MSGUNIQUEID, uniqueID);

            //only update sender jid if not null (grp)
            if (otherJid != null && !otherJid.equals("")) {
                cvMSG.put(MSG_SENDERJID, otherJid);
            }

            //update old appt details (title, loc, date)
            if (oldApptDet != null) {
                cvMSG.put(MSG_MSGINFOURL, oldApptDet);
            }

            //status - need 3 columns
            if (isSender == 79 && otherJid != null && !otherJid.equals("")) { //only need for grp
                if (getApptUpdateMsgStatusExist(apptID, isSender, otherJid) > 0) { //appt update msg exists
                    updateRDB3Col(MSG_TABLE_NAME, cvMSG, MSG_MSGINFOID, MSG_ISSENDER, MSG_SENDERJID,
                            apptID, String.valueOf(isSender), otherJid);

                } else { //not exist
                    cvMSG.put(MSG_ISSENDER, isSender);
                    cvMSG.put(MSG_MSGINFOID, apptID);

                    rdb.insertQuery().insertMessage(Message.fromContentValues(cvMSG));
                }

            } else { //others (out status no need sender jid)
                if (getApptUpdateMsgExist(apptID, isSender) > 0) { //appt update msg exists
                    updateRDB2Col(MSG_TABLE_NAME, cvMSG, MSG_MSGINFOID, MSG_ISSENDER,
                            apptID, String.valueOf(isSender));

                } else { //not exist
                    cvMSG.put(MSG_ISSENDER, isSender);
                    cvMSG.put(MSG_MSGINFOID, apptID);

                    rdb.insertQuery().insertMessage(Message.fromContentValues(cvMSG));
                }
            }

            //update chat list
            ContentValues cvCL = new ContentValues();

            cvCL.put(CL_COLUMN_DATE, dateLong);
            cvCL.put(CL_COLUMN_LASTMSG, lastMsgCL);

            if (otherJid != null) {
                cvCL.put(CL_COLUMN_LASTSENDERNAME, getNameFromContactRoster(otherJid));
            }

            updateRDB1Col(CL_TABLE_NAME, cvCL, CL_COLUMN_JID, jid);

            //insert noti badge to chat if it's incoming
            if (needNotiBadge) {
                addChatBadgeUnread(jid, dateLong);
            }
            playInMsgSoundUI(playSound);
        }
    }

    /*////////////// [K02_02E] APPT: Incoming-related-END //////////////*/


    /*/////////////////// [K02_03] APPT: Others ////////////////////*/
    //[DONE INDI]get date to exact milis
    private long getExactDateToday() {
        Calendar today = Calendar.getInstance();

        today.set(Calendar.MILLISECOND, 0);
        today.set(Calendar.SECOND, 0);
        today.set(Calendar.MINUTE, 0);
        today.set(Calendar.HOUR_OF_DAY, 0);

        return today.getTimeInMillis();
    }
    /*/////////////////// [K02_03E] APPT: Others ///////////////////*/

    /*============================ [J02E - APPOINTMENT-RELATED-END] ===========================*/



    /*============================ [J03 - GROUP-DETAIL-RELATED] ===========================*/
    /*/////////////// [K03_01] GROUP-DETAIL: Incoming-Related ///////////////*/

    //Incoming create NEW group chat
    public void incomingCreateNewGrpChat(String room_id, long currentDate, String otherJid, String uniqueID,
                                         boolean playSound, boolean isAdding) {

        String access_token = preferences.getValue(context, GlobalVariables.STRPREF_ACCESS_TOKEN);
        String user_jid = preferences.getValue(context, GlobalVariables.STRPREF_USER_ID);
        String size = miscHelper.getDeviceDensity(context);

        //build retrofit
        GetRoomProfile client = RetrofitAPIClient.getClient().create(GetRoomProfile.class);
        retrofit2.Call<GetRoomRepo> call = client.getRoomProfile(room_id, size, "Bearer " + access_token);

        call.enqueue(new retrofit2.Callback<GetRoomRepo>() {
            @Override
            public void onResponse(retrofit2.Call<GetRoomRepo> call, final retrofit2.Response<GetRoomRepo> response) {
                if (!response.isSuccessful()) {
                    new MiscHelper().retroLogUnsuc(response, "incomingCreateNewGrpChat ", "JAY");

                    return;
                }

                //get room name and resource url from response body
                String room_name = String.valueOf(response.body().getRoom_name());
                String resource_url = String.valueOf(response.body().getResource_url());
                String image_data = String.valueOf(response.body().getImage_data());

                //add room info into Roster List
                byte[] apptImgByte = null;
                if (!image_data.equals("null") && !image_data.equals("Empty")) {
                    apptImgByte = Base64.decode(image_data, Base64.DEFAULT);
                }
                createNewGrpChat(room_id, currentDate, room_name, apptImgByte, resource_url, false, otherJid, isAdding);


                //loop member info from response
                List<GetMemberListModel> member_list = response.body().getUsers();
                String admin_type, member_jid, color, member_name, phone, member_resource_url;

                for (int i = 0; i < member_list.size(); i++) {
                    admin_type = member_list.get(i).getType();
                    member_jid = member_list.get(i).getUser_jid();
                    color = MiscHelper.getColorForGrpDisplayName(i);

                    if (!member_jid.equals(user_jid)) { //only add members other than self
                        //add new members to grp mem
                        checkAndAddJidToGrpMem(member_jid, room_id, admin_type, color);

                        //only add info to contact roster if not exist
                        if (getContactRosterExist(member_jid) == 0) {
                            member_name = member_list.get(i).getName();
                            phone = member_list.get(i).getPhone_number();
                            member_resource_url = member_list.get(i).getResource_url();

                            //phone name = null coz not in contact roster
                            checkAndAddUserProfileToContactRoster(phone, null, member_name,
                                    member_jid, member_resource_url, null, null);

                            //try download image (full and thumb)
                            if (member_resource_url != null && !member_resource_url.equals("") && !member_resource_url.equals("null")) {
                                saveFullandThumbFromImgURL(member_resource_url, member_jid);
                            }
                        }
                    }
                }

                //add appt to db
                if (response.body().getRoom_appointments() != null) {
                    for (GetGrpApptModel appt : response.body().getRoom_appointments()
                            ) {
                        if (getApptExist(appt.getAppointment_id()) == 0) {
                            incomingApptDetails(room_id, null, appt.getAppointment_title(),
                                    appt.getLocation_map_url(), appt.getLocation_name(), appt.getLocation_lon(),
                                    appt.getLocation_lat(), appt.getAppointment_date(), appt.getAppointment_time(),
                                    "", "", "", playSound, otherJid, appt.getAppointment_id(), "99");

                            for (AppointmentMemStatus status : appt.getAppointment_users()) {
                                checkAndAddStatusToGrpMemStatus(status.getUser_jid(), room_id, appt.getAppointment_id(), status.getAppointment_status());
                            }
                        }
                    }
                }

                addChatBadgeUnread(room_id, currentDate);

                playInMsgSoundUI(playSound);

                groupChatStanza.GroupAckStanza(room_id, user_jid, otherJid, uniqueID);
            }

            @Override
            public void onFailure(retrofit2.Call<GetRoomRepo> call, final Throwable t) {
                new Handler(Looper.getMainLooper())
                        .post(() -> Toast.makeText(context, R.string.onfailure, Toast.LENGTH_SHORT).show());
                new MiscHelper().retroLogFailure(t, "incomingCreateNewGrpChat", "JAY");

            }
        });
    }

    //[DONE CR] incoming new group member (other ppl add member to existing group)
    public void groupAddReceived(String jid, final String addedJid, final String otherJid, final String uniqueID, boolean playSound) {
        String access_token = preferences.getValue(context, GlobalVariables.STRPREF_ACCESS_TOKEN);
        final String user_id = preferences.getValue(context, GlobalVariables.STRPREF_USER_ID);

        if (addedJid.equals(user_id)) { //added member is self, just acknowledge (safety unwrapping)
            groupChatStanza.GroupAckStanza(jid, user_id, otherJid, uniqueID);

        } else { //added member is not self
            //add added jid to GrpMem
            if (getMemberExist(jid, addedJid) == 0) { //if added member does not exist in this grp
                String color = MiscHelper.getColorForGrpDisplayName(getMemCountFromGrpMem(jid));

                if (getContactRosterExist(addedJid) != 0) { //added member exists in contact roster
                    // exists in contact roster, just add jid to grpmem
                    checkAndAddJidToGrpMem(addedJid, jid, "member", color);

                    //add to GMS for each existing appt
                    List<String> apptList = get_apptIDFromRoom(jid);

                    for (String apptID : apptList) {
                        checkAndAddStatusToGrpMemStatus(addedJid, jid, apptID, 2);
                    }

                    //update appt-like msg to grp
                    String addedMemName = getNameFromContactRoster(addedJid);
                    String body = addedMemName + " " + context.getString(R.string.single_member_added);

                    groupStatusMsgPlaySound(jid, getNameFromContactRoster(otherJid), body, true, playSound);

                    groupChatStanza.GroupAckStanza(jid, user_id, otherJid, uniqueID);

                } else { //not found in CR, try download user info
                    //build retrofit
                    String size = miscHelper.getDeviceDensity(context);
                    IndiAPIInterface indiAPIInterface = RetrofitAPIClient.getClient().create(IndiAPIInterface.class);
                    retrofit2.Call<GetUserRepo> call = indiAPIInterface.getIndiProfile(addedJid, size, "Bearer " + access_token);

                    call.enqueue(new retrofit2.Callback<GetUserRepo>() {
                        @Override
                        public void onResponse(retrofit2.Call<GetUserRepo> call, retrofit2.Response<GetUserRepo> response) {
                            if (!response.isSuccessful()) {
                                new MiscHelper().retroLogUnsuc(response, "groupAddReceived ", "JAY");

                                return;
                            }

                            //success, prepare to update database
                            final String displayName = String.valueOf(response.body().getName());
                            final String phoneName = String.valueOf(response.body().getPhone());
                            final String imageURL = String.valueOf(response.body().getResource_url());
                            final String image_data = String.valueOf(response.body().getImage_data());

                            //add jid to grpmem
                            checkAndAddJidToGrpMem(addedJid, jid, "member", color);

                            //add to GMS for each existing appt
                            List<String> apptList = get_apptIDFromRoom(jid);

                            for (String apptID : apptList) {
                                checkAndAddStatusToGrpMemStatus(addedJid, jid, apptID, 2);
                            }

                            checkAndAddUserProfileToContactRoster(phoneName, null, displayName,
                                    addedJid, imageURL, image_data, null);

                            //update appt-like msg to grp
                            String body = displayName + " " + context.getString(R.string.single_member_added);
                            groupStatusMsgPlaySound(jid, getNameFromContactRoster(otherJid), body, true, playSound);

                            if (imageURL != null && !imageURL.equals("") && !imageURL.equals("null")) {
                                //try download and save image to grpmem_img
                                saveFullandThumbFromImgURL(imageURL, addedJid);
                            }

                            groupChatStanza.GroupAckStanza(jid, user_id, otherJid, uniqueID);

                        }

                        @Override
                        public void onFailure(retrofit2.Call<GetUserRepo> call, Throwable t) {
                            new MiscHelper().retroLogFailure(t, "groupAddReceived", "JAY");

                        }
                    });
                }

            } else {
                //add to GMS for each existing appt
                List<String> apptList = get_apptIDFromRoom(jid);

                for (String apptID : apptList) {
                    checkAndAddStatusToGrpMemStatus(addedJid, jid, apptID, 2);
                }
                groupChatStanza.GroupAckStanza(jid, user_id, otherJid, uniqueID);
            }
        }

    }

    //[DONE CR] Incoming self is removed from group
    public void selfRemovedFromGrp(String otherJid, String roomJid, String uniqueID, boolean playSound) {
        if (getChatListExist(roomJid) > 0) {
            ContentValues cvCL = new ContentValues();

            cvCL.put(CL_COLUMN_ADMIN_SELF, 0);

            updateRDB1Col(CL_TABLE_NAME, cvCL, CL_COLUMN_JID, roomJid);

            ContentValues cvCR = new ContentValues();

            cvCR.put(CR_COLUMN_DISABLEDSTATUS, 1);

            updateRDB1Col(CR_TABLE_NAME, cvCR, CR_COLUMN_JID, roomJid);

            //delete all grp mem from rdb
            deleteRDB1Col(GM_TABLE_NAME, GM_COLUMN_ROOMJID, roomJid);

            groupStatusMsgPlaySound(roomJid, getNameFromContactRoster(otherJid), context.getString(R.string.self_removed_grp), true, playSound);
        }
        singleChatStanza.SoappAckStanza(roomJid, uniqueID);
    }

    //[DONE CR] incoming other member removed from group
    public void grpOtherMemberRemoved(final String roomJid, final String deletedJid, String
            otherJid, String uniqueID, boolean playSound) {

        final String user_id = preferences.getValue(context, GlobalVariables.STRPREF_USER_ID);
        if (getMemberExist(roomJid, deletedJid) != 0) {
            //delete member from GRPMEM
            deleteRDB2Col(GM_TABLE_NAME, GM_COLUMN_ROOMJID, GM_COLUMN_MEMBERJID, roomJid, deletedJid);

            //delete member from GMS (all rows)
            deleteRDB2Col(GMS_TABLE_NAME, GMS_COLUMN_ROOMJID, GMS_COLUMN_MEMBERJID, roomJid, deletedJid);

            String displayName = getNameFromContactRoster(deletedJid);

            String body;
            if (otherJid.equals(deletedJid)) { //member left group
                body = displayName + " " + context.getString(R.string.other_left_grp);

            } else { //member removed from grp
                body = displayName + " " + context.getString(R.string.other_removed_grp);
            }

            groupStatusMsgPlaySound(roomJid, getNameFromContactRoster(otherJid), body, true, playSound);

        }
        groupChatStanza.GroupAckStanza(roomJid, user_id, otherJid, uniqueID);

    }

    //[DONE CR] incoming group admin (self or other member promoted) - need to do demoted later
    public void groupChangeAdminReceived(final String node, final String promotedJid, String
            otherJid, String uniqueID, boolean playSound) {

        String msg;
        long date = System.currentTimeMillis();

        ContentValues cvMSG = new ContentValues();

        cvMSG.put(MSG_JID, node);
        cvMSG.put(MSG_ISSENDER, 2);
        cvMSG.put(MSG_MSGDATE, date);

        final String user_id = preferences.getValue(context, GlobalVariables.STRPREF_USER_ID);
        if (promotedJid.equals(user_id)) { //if self is the one being promoted to admin
            if (getChatListExist(node) > 0) { //if room jid exists in chatlist
                ContentValues cvCL = new ContentValues();

                cvCL.put(CL_COLUMN_ADMIN_SELF, 1);

                msg = String.format("You %s", context.getString(R.string.assign_admin_you));

                cvMSG.put(MSG_MSGDATA, msg);

                updateRDB1Col(CL_TABLE_NAME, cvCL, CL_COLUMN_JID, node);

                rdb.insertQuery().insertMessage(Message.fromContentValues(cvMSG));

                updateChatListLastMessage(node, msg, getNameFromContactRoster(otherJid), date, true, false, null);
            }

        } else { //other group member promoted to admin
            if (getMemberExist(node, promotedJid) != 0) { //if to-be-promoted member exists in group
                ContentValues cvGM = new ContentValues();
                cvGM.put(GM_COLUMN_ADMIN, 1);
                cvGM.put(GM_COLUMN_MEMBERJID, promotedJid);

                updateRDB1Col(GM_TABLE_NAME, cvGM, GM_COLUMN_MEMBERJID, promotedJid);

                msg = String.format("%s %s", getNameFromContactRoster(promotedJid), context.getString(R.string.assign_admin));
                cvMSG.put(MSG_MSGDATA, msg);

                rdb.insertQuery().insertMessage(Message.fromContentValues(cvMSG));

                updateChatListLastMessage(node, msg, getNameFromContactRoster(otherJid), date, true, false, null);

            }
        }
        playInMsgSoundUI(playSound);

        groupChatStanza.GroupAckStanza(node, user_id, otherJid, uniqueID);
    }

    //[DONE CR] Incoming group profile changed (group name/profile image)
    public void grpProfileUpdated(String node, String otherJid, String uniqueID, boolean playSound) {
        String user_id = preferences.getValue(context, GlobalVariables.STRPREF_USER_ID);

        if (getChatListExist(node) > 0) {
            final String access_token = preferences.getValue(context, GlobalVariables.STRPREF_ACCESS_TOKEN);

            //build retrofit
            String size = miscHelper.getDeviceDensity(context);
            GetRoomProfile client = RetrofitAPIClient.getClient().create(GetRoomProfile.class);
            retrofit2.Call<GetRoomRepo> call = client.getRoomProfile(node, size, "Bearer " + access_token);

            call.enqueue(new retrofit2.Callback<GetRoomRepo>() {
                @Override
                public void onResponse(retrofit2.Call<GetRoomRepo> call, retrofit2.Response<GetRoomRepo> response) {
                    if (!response.isSuccessful()) {
                        new MiscHelper().retroLogUnsuc(response, "grpProfileUpdated ", "JAY");

                        return;
                    }
                    final String room_name = String.valueOf(response.body().getRoom_name());
                    final String resource_url = String.valueOf(response.body().getResource_url());
                    final String image_data = String.valueOf(response.body().getImage_data());

                    ryanGroupSaveFullOnlyFromImgURL(image_data, room_name, node, resource_url, otherJid, playSound);
                    //show "grp profile updated" status msg
//                    String displayName = getNameFromContactRoster(otherJid);
//                    String allMsg = displayName + " " + context.getString(R.string.updated_grp_profile);
//                    groupStatusMsgPlaySound(node, displayName, allMsg, true, playSound);

                    //ack
                    groupChatStanza.GroupAckStanza(node, user_id, otherJid, uniqueID);

                }

                @Override
                public void onFailure(retrofit2.Call<GetRoomRepo> call, Throwable t) {
                    new MiscHelper().retroLogFailure(t, "grpProfileUpdated", "JAY");

                }
            });
        }
    }

    public void ryanGroupSaveFullOnlyFromImgURL(String image_data, String room_name, String node, String resource_url, String otherJid, boolean playSound) {
        DownloadFromUrlInterface client = RetrofitAPIClient.getClient().create(DownloadFromUrlInterface.class);
        retrofit2.Call<ResponseBody> call = client.downloadFileWithDynamicUrlAsync(resource_url);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (!response.isSuccessful()) {
                    updateGroupCrUrl(image_data, room_name, node, resource_url, otherJid, playSound, null);
                    return;
                }
                try {
                    byte[] image100 = response.body().bytes();
                    updateGroupCrUrl(image_data, room_name, node, resource_url, otherJid, playSound, image100);

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                updateGroupCrUrl(image_data, room_name, node, resource_url, otherJid, playSound, null);
            }
        });
    }

    public void updateGroupCrUrl(String image_data, String room_name, String node, String resource_url, String otherJid, boolean playSound, byte[] image100) {
        checkAndAddUserProfileToContactRoster(null, null, room_name, node,
                resource_url, image_data, image100);

        String displayName = getNameFromContactRoster(otherJid);
        String allMsg = displayName + " " + context.getString(R.string.updated_grp_profile);
        groupStatusMsgPlaySound(node, displayName, allMsg, true, playSound);
    }
    /*/////////////// [K03_01E] GROUP-DETAIL: Incoming-Related-END ///////////////*/


    /*/////////////// [K03_02] GROUP-DETAIL: Outgoing-Related ///////////////*/

    //[DONE CR] outgoing assign admin (no push/msg yet)
    public void assignMemberAsAdmin(String roomJid, final String promotedJid) {
        if (getMemberExist(roomJid, promotedJid) != 0) { //if to-be-promoted member exists in group
            ContentValues cvGM = new ContentValues();
            cvGM.put(GM_COLUMN_ADMIN, 1);

            updateRDB2Col(GM_TABLE_NAME, cvGM, GM_COLUMN_ROOMJID, GM_COLUMN_MEMBERJID, roomJid, promotedJid);
        }
    }

    //self delete/leave room (indi or grp also can)
    public void selfLeaveDeleteRoom(String roomJid, boolean isDeleteRoom) {
        //for delete room: check if user already left room
        if (isDeleteRoom && getDisabledStatus(roomJid) == 1) { //already left room, just need to delete from sqlite
            //delete all grp mem from rdb
            deleteRDB1Col(GM_TABLE_NAME, GM_COLUMN_ROOMJID, roomJid);

            clearMsgesInMsg(roomJid);

            //delete room from cl
            deleteRDB1Col(CL_TABLE_NAME, CL_COLUMN_JID, roomJid);

            clearCR(roomJid);

            //delete all appts accosiated to room
            deleteRDB1Col(A_TABLE_NAME, A_COLUMN_JID, roomJid);
            return;
        }

        //just post to API to delete room
        retroLeaveDeleteGrp(roomJid, isDeleteRoom);
    }

    //post leave/delete group retrofit to server
    private void retroLeaveDeleteGrp(String roomJid, boolean isDeleteRoom) {
        String userjid = preferences.getValue(context, GlobalVariables.STRPREF_USER_ID);
        String username = preferences.getValue(context, GlobalVariables.STRPREF_USERNAME);
        String access_token = preferences.getValue(context, GlobalVariables.STRPREF_ACCESS_TOKEN);
        String uniqueID = UUID.randomUUID().toString();
        DeleteRoomMemberModel model2 = new DeleteRoomMemberModel(roomJid, userjid, uniqueID, GlobalVariables.pubsubHost, GlobalVariables.xmppHost, "ANDROID");

        //build retrofit
        DeleteRoomMember client2 = RetrofitAPIClient.getClient().create(DeleteRoomMember.class);
        retrofit2.Call<DeleteRoomMemberModel> call1 = client2.deleteMemberApi(model2, "Bearer " + access_token);
        call1.enqueue(new retrofit2.Callback<DeleteRoomMemberModel>() {
            @Override
            public void onResponse(retrofit2.Call<DeleteRoomMemberModel> call1, retrofit2.Response<DeleteRoomMemberModel> response) {
                if (!response.isSuccessful()) {

                    //only need clear sqlite if deleting room
                    if (isDeleteRoom) {
                        try {
                            String errorStr = response.errorBody().string();
                            JSONObject jObjError = new JSONObject(errorStr);

                            switch (jObjError.getString("message")) {
                                case "User is not a member of this room":
                                case "Room not found":
                                    //clear room from CL
                                    deleteRDB1Col(CL_TABLE_NAME, CL_COLUMN_JID, roomJid);

                                    //clear room from CR
                                    clearCR(roomJid);

                                    //clear msges from MSG
                                    clearMsgesInMsg(roomJid);

                                    //clear all grp members from GRPMEM
                                    deleteRDB1Col(GM_TABLE_NAME, GM_COLUMN_ROOMJID, roomJid);

                                    //clear appts from APPT
                                    deleteRDB1Col(A_TABLE_NAME, A_COLUMN_JID, roomJid);

                                    //delete ALL GMS associated to this room
                                    deleteRDB1Col(GMS_TABLE_NAME, GMS_COLUMN_ROOMJID, roomJid);
                                    break;

                                default:
                                    new MiscHelper().retroLogUnsuc(response, "retroLeaveDeleteGrp ", "JAY");
                                    Toast.makeText(context, R
                                            .string.onresponse_unsuccessful, Toast.LENGTH_SHORT).show();
                                    break;
                            }
                        } catch (Exception e) {
                        }
                    }
                    return;
                }
                //universal
                String pushMsg = username + " " + context.getString(R.string.has_left_grp);

                //pubsub to others about self leaving grp
                pubsubNodeCall.PubsubGroup(roomJid, userjid, UUID.randomUUID().toString(), "", userjid);

                //clear all grp members from GRPMEM
                deleteRDB1Col(GM_TABLE_NAME, GM_COLUMN_ROOMJID, roomJid);

                //clear appts from APPT
                deleteRDB1Col(A_TABLE_NAME, A_COLUMN_JID, roomJid);

                //delete ALL GMS associated to this room
                deleteRDB1Col(GMS_TABLE_NAME, GMS_COLUMN_ROOMJID, roomJid);

                if (isDeleteRoom) { //delete grp
                    //delete room from CL
                    deleteRDB1Col(CL_TABLE_NAME, CL_COLUMN_JID, roomJid);

                    //delete room from CR
                    clearCR(roomJid);

                    //clear msges from MSG
                    clearMsgesInMsg(roomJid);

                } else { //leave grp
                    //set self admin to 0 and disabled status to 1 for CL
                    clearSelfAdmin(roomJid);

                    //add left grp msg to grp msg
                    groupStatusMsgPlaySound(roomJid, context.getString(R.string.you), context.getString(R.string.self_left_grp), false, false);
                }
            }

            @Override
            public void onFailure(retrofit2.Call<DeleteRoomMemberModel> call1, Throwable t) {
                new MiscHelper().retroLogFailure(t, "retroLeaveDeleteGrp", "JAY");

                Toast.makeText(context, R.string.onfailure, Toast.LENGTH_SHORT).show();
            }
        });
    }

    //[DONE CR] self delete group member
    public void selfDeleteGrpMember(String roomJid, String memberJid, String body) {
        //remove memberJid from grpMem
        deleteRDB2Col(GM_TABLE_NAME, GM_COLUMN_ROOMJID, GM_COLUMN_MEMBERJID, roomJid, memberJid);

        //remove memberJid from GMS (All rows)
        deleteRDB2Col(GMS_TABLE_NAME, GMS_COLUMN_ROOMJID, GMS_COLUMN_MEMBERJID, roomJid, memberJid);

        //add member deleted msg
        groupStatusMsgPlaySound(roomJid, context.getString(R.string.you), body, false, false);

    }

    //[DONE CR] self changed group profile name and image
    public void selfUpdateGrpProfile(byte[] imageByte33, byte[] imageByte, String name, String
            room_id) {
        ContentValues cvCR = new ContentValues();

        cvCR.put(CR_COLUMN_JID, room_id);
        if (imageByte33 != null) {
            cvCR.put(CR_COLUMN_PROFILEPHOTO, imageByte33);
            cvCR.put(CR_COLUMN_PROFILEFULL, imageByte);
        }
        cvCR.put(CR_COLUMN_DISPLAYNAME, name);

        updateRDB1Col(CR_TABLE_NAME, cvCR, CR_COLUMN_JID, room_id);
    }

    /*/////////////// [K03_02E] GROUP-DETAIL: Outgoing-Related-END ///////////////*/


    /*/////////////// [K03_99] GROUP-DETAIL: Others ///////////////*/
    //[DONE CR] status msg for "user left room"Integer getApptNotiBadgeLoc()
    public void groupStatusMsgPlaySound(String room_id, String lastSenderName, String allMsg, boolean needBadge, boolean playSound) {
        final long Date = System.currentTimeMillis();
        if (allMsg != null) {
            ContentValues cvMSG = new ContentValues();

            cvMSG.put(MSG_JID, room_id);
            cvMSG.put(MSG_MSGDATE, Date);//mili
            cvMSG.put(MSG_MSGDATA, allMsg);
            cvMSG.put(MSG_ISSENDER, 2);
            cvMSG.put(MSG_SENDERJID, "");

            rdb.insertQuery().insertMessage(Message.fromContentValues(cvMSG));

            if (getChatListExist(room_id) > 0) {
                ContentValues cvCL = new ContentValues();

                cvCL.put(CL_COLUMN_DATE, Date);
                cvCL.put(CL_COLUMN_LASTMSG, allMsg);
                cvCL.put(CL_COLUMN_LASTSENDERNAME, lastSenderName);

                updateRDB1Col(CL_TABLE_NAME, cvCL, CL_COLUMN_JID, room_id);
            }
        }
        if (needBadge) { //add badge
            addChatBadgeUnread(room_id, Date);
        }

        //play sound
        playInMsgSoundUI(playSound);
    }

    //reset selected rows in grp member selection list (when create new grp or add new grp mem)
    public void clearCRSelected() {
        ContentValues cvCR = new ContentValues();

        cvCR.put("Selected", 0);

        updateRDB1Col(CR_TABLE_NAME, cvCR, CR_COLUMN_SELECTED, "1");
    }

    //update selected value to CR
    public void updateCRSelected(String jid, int selected) {
        ContentValues cvCR = new ContentValues();

        cvCR.put("Selected", selected);

        updateRDB1Col(CR_TABLE_NAME, cvCR, CR_COLUMN_JID, jid);
    }
    /*/////////////// [K03_99E] GROUP-DETAIL: Others-END ///////////////*/
    /*============================ [J03E - GROUP-DETAIL-RELATED-END] ===========================*/


    /*============================ [J04 - GET-FROM-DB-RELATED] ===========================*/
    /*/////////////// [K04_01] GET: Check-Exist-Related ///////////////*/
    /*----------- [K04_02_01] Chat-related ----------- */
    //Check if chatroom exists in chatlist
    private int getChatListExist(String jid) {
        Cursor cursor = rdb.selectQuery().getCLExist(jid);
        int count = cursor.getCount();
        cursor.close();

        return count;
    }

    //check if uniqueID already exists in msg table
    public boolean getUniqueIDExists(String uniqueID) {
        return rdb.selectQuery().getUniqueIDExist(uniqueID) > 0;
    }

    /*----------- [K04_02_01E] [END]Chat-related ----------- */

    public Message getMessageFromUniqueID(String uniqueid) {
        return rdb.selectQuery().getMessageFromUniqueID(uniqueid);

    }

    /*----------- [K04_02_02] Appointment-related ----------- */
    //check if jid exists in APPOINTMENT table
    public int getApptExist(String apptID) {
        return rdb.selectQuery().getAppointmentExist(apptID);
    }

    //check if jid exists in APPOINTMENT_HIST table
    private int getApptHistExist(String apptID) {
        return rdb.selectQuery().getAppointmentHistExist(apptID);
    }

    //check if memberjid in a grp in an appt exists in GMS
    private int getApptInGMSExist(String roomJid, String memberJid, String apptID) {
        Cursor cursor = rdb.selectQuery().getApptInGMSExist(roomJid, memberJid, apptID);

        int count = cursor.getCount();
        cursor.close();

        return count;
    }

    private boolean getApptWorkIDExits(String apptID) {
        return rdb.selectQuery().getApptWorkIDExits(apptID);
    }
    /*----------- [K04_02_02E] [END]Appointment-related ----------- */


    /*----------- [K04_02_03] Restaurant-related ----------- */
    //check if res booking made by self already exists
    public int getSelfResBookingExist(String bookingid) {
        int count = rdb.selectQuery().getSelfResBookingExist(bookingid);
        return count;
    }

    public int getBookingExistBasedOnResID(String resid) {

        int count = rdb.selectQuery().getBookingExistBasedOnResID(resid);
        return count;
    }

    public int getRestaurantExist(String restaurantId) {
        Cursor cursor = rdb.selectQuery().getRestaurantExist(restaurantId);
        if (cursor != null) {
            int count = cursor.getCount();
            if (count > 0) {
                cursor.close();

                return count;
            } else {
                cursor.close();

                return 0;
            }
        } else {
            cursor.close();

            return 0;
        }
    }
    /*----------- [K04_02_03E] [END]Restaurant-related ----------- */


    /*----------- [K04_02_04] GroupMem-related ----------- */
    //check if user jid exists in a specific room
    private int getMemberExist(String roomjid, String userjid) {
        Cursor cursor = rdb.selectQuery().getMemberInGrpMemExist(roomjid, userjid);
        int count = cursor.getCount();
        cursor.close();

        return count;
    }
    /*----------- [K04_02_04E] [END]Group-related ----------- */


    /*----------- [K04_02_05] Contacts-related ----------- */
    //Check if user jid exists in contact roster
    public int getContactRosterExist(String jid) {
        return rdb.selectQuery().getContactRosterExist(jid);
    }

    //Check if user has phonename in contact roster based on jid (
    public int getPhoneNameExist(String jid) {
        Cursor cursor = rdb.selectQuery().getPhoneNameExist(jid);
        int count = cursor.getCount();
        cursor.close();

        return count;
    }

    //Check if user has phonename in contact roster based on phonenumber (
    public int getPhoneNameExistNumber(String phoneNumber) {

//        Cursor cursor = db.rawQuery("select _id from CONTACT_ROSTER where phonenumber = '" +
//                phoneNumber + "' AND phonename IS NOT NULL AND phonename != ''", null);
        Cursor cursor = rdb.selectQuery().getPhoneNameExistNumber(phoneNumber);
        int count = cursor.getCount();
        cursor.close();

        return count;
    }

    //check if room has "Add To Contacts" button
    private int getAddToContactExist(String jid) {
        Cursor cursor = rdb.selectQuery().getAddToContactExist(jid);
        if (cursor != null) {
            int count = cursor.getCount();
            cursor.close();

            return count;
        }
        return 0;
    }
    /*----------- [K04_02_05E] [END]Contacts-related ----------- */


    /*----------- [K04_02_06] MESSAGE-related ----------- */
    //check if unread msg exists in a room
    public int getUnreadMsgExist(String jid) {
        Cursor cursor = rdb.selectQuery().getUnreadMsgExist(jid);
        if (cursor != null) {
            int count = cursor.getCount();
            cursor.close();

            return count;
        }
        return 0;
    }
    /*----------- [K04_02_06E][END] MESSAGE-related ----------- */


    /*----------- [K04_02_07] Others ----------- */
    /*----------- [K04_02_07E] [END]Others ----------- */
    /*/////////////// [K04_01E] GET: Check-Exist-Related-END ///////////////*/


    /*/////////////// [K04_02] GET: Single-Info-Related ///////////////*/
    /*----------- [K04_03_01] ContactRoster ----------- */
    //get user/room image thumb from contact roster using jid
    public byte[] getImageBytesThumbFromContactRoster(String jid) {
        return rdb.selectQuery().getImageBytesThumbFromContactRoster(jid);
    }

    //get user/room image full from contact roster using jid
    public byte[] getImageBytesFullFromContactRoster(String jid) {
        byte[] imageByte = null;

        Cursor cursor = rdb.selectQuery().getImageBytesFullFromContactRoster(jid);
        if (cursor.moveToFirst()) {
            imageByte = cursor.getBlob(0);
        }
        cursor.close();

        return imageByte;
    }

    //Get resource URL from CR
    public String getResourceURLFromContactRoster(String jid) {
        Cursor cursor = rdb.selectQuery().getResourceURLFromContactRoster(jid);

        if (cursor.moveToFirst()) {
            String resourceURL = cursor.getString(0);
            cursor.close();

            return resourceURL;
        } else {
            return null;
        }
    }

    //Get phonename from contact roster
    public String getNameFromContactRoster(String jid) {
        if (jid.length() > 12) { //grp, get displayname only
            Cursor cursor = rdb.selectQuery().getNameFromContactRoster(jid);
            if (cursor.moveToFirst()) {
                String displayName = cursor.getString(1);

                cursor.close();

                return displayName;
            }
        } else {
            String user_jid = preferences.getValue(context, GlobalVariables.STRPREF_USER_ID);

            if (jid.equals(user_jid)) {
                return preferences.getValue(context, GlobalVariables.STRPREF_USERNAME);
            } else {
                Cursor cursor = rdb.selectQuery().getNameFromContactRoster(jid);

                if (cursor.moveToFirst()) {
                    String phoneName = cursor.getString(0);
                    if (phoneName == null || phoneName.equals("")) {
                        String displayName = cursor.getString(1);
                        String phoneNumber = cursor.getString(2);
                        phoneName = displayName + " " + phoneNumber;
                    }

                    cursor.close();

                    return phoneName;
                }
            }
        }
        return "";
    }

    //Get phonename OR displayname of individual from contact roster
    private String getIndiNameFromCR(String jid) {
        Cursor cursor = rdb.selectQuery().getIndiNameFromCR(jid);
        if (cursor.moveToFirst()) {
            String phoneName = cursor.getString(0);
            if (phoneName == null || phoneName.equals("")) {
                phoneName = cursor.getString(1);
            }
            cursor.close();

            return phoneName;
        } else {
            return "";
        }
    }
    /*----------- [K04_03_01E] [END] ContactRoster ----------- */


    /*----------- [K04_03_02] ChatList ----------- */
    //Get disabled status from chatlist
    public int getDisabledStatus(String jid) {
        return rdb.selectQuery().getDisabledStatus(jid);

    }

    //get online status of a chat room from ChatList
    private int getOnlineStatus(String jid) {
        return rdb.selectQuery().getChatMarker(jid);
    }

    //Get chat notification badge count from chatlist
    private int getChatNotiCount(String jid) {
        Cursor cursor = rdb.selectQuery().getBadgeFromRosterChat(jid);
        if (cursor.moveToFirst()) {
            int badgeCount = cursor.getInt(0);
            cursor.close();

            return badgeCount;
        } else { //jid does not exist
            cursor.close();
            return 0;
        }
    }

    //get notification badge count for chattab
    public int getChatNotiForChatTab() {
        Cursor cursor = rdb.selectQuery().getBadgeCountForChatTab();
        int count = cursor.getCount();
        cursor.close();

        return count;
    }
    /*----------- [K04_03_02E] [END] ChatList ----------- */


    /*----------- [K04_03_03] Appointment ----------- */
    public Appointment getAllApptDet(String apptID) {
        return rdb.selectQuery().getAllApptDetails(apptID);
    }

    public List<AppointmentHist> getApptHBasicDet(String apptID) {
        return rdb.selectQuery().getApptHBasicDet(apptID);
    }

    //get CURRENT appt title from APPT db
    public String getApptCurrentTitle(String apptID) {
        return rdb.selectQuery().getApptCurrentTitle(apptID);
    }

    //get appt title from APPT_Hist db
    public String getApptHistTitle(String apptID) {
        return rdb.selectQuery().getApptHistTitle(apptID);
    }

    //get appt title from APPT or APPT_H db
    public String getApptTitle(String apptID) {
        String apptTitle = rdb.selectQuery().getApptCurrentTitle(apptID);

        if (apptTitle == null || apptTitle.equals("")) {
            apptTitle = rdb.selectQuery().getApptHistTitle(apptID);

            if (apptTitle == null || apptTitle.equals("")) {
                apptTitle = context.getString(R.string.appt_no_appt_title);
            }
        }

        return apptTitle;
    }

    //get appt date and time and return as long (full date + time)
    public long getApptTime(String apptID) {
        return rdb.selectQuery().getApptTime(apptID);
    }

    //get apptremindet time
    public long getApptReminderTime(String apptID) {
        return rdb.selectQuery().getApptReminderTime(apptID);
    }

    //get appt location
    public String getApptLoc(String apptID) {
        return rdb.selectQuery().getApptLoc(apptID);
    }

    //get self's appt status for an appt
    public int getApptStatusSelf(String apptID) {
        return rdb.selectQuery().getApptStatusSelf(apptID);
    }

    //get friend's appt status for an INDI appt
    public int getApptStatusFriend(String apptID) {
        return rdb.selectQuery().getApptStatusFriend(apptID);
    }

    //get member's appt status for a GRP appt
    public int getApptStatusGM(String apptID, String roomJid, String memberJid) {
        return rdb.selectQuery().getApptStatusGM(apptID, roomJid, memberJid);
    }

    //get number of appts in a chat room
    public int getApptCount(String jid) {
        Cursor cursor = rdb.selectQuery().getApptCount(jid);
        int count = cursor.getCount();
        cursor.close();

        return count;
    }

    //get count of not going appointments for same date
    public int getApptNGSameDateCount(long apptDate) {
        Cursor cursor = rdb.selectQuery().getApptNGSameDateCount(apptDate);
        int count = cursor.getCount();
        cursor.close();

        return count;
    }

    //Get appt notification badge count for an apptID
    private int getApptNotiBadge1Appt(String apptID) {
        return rdb.selectQuery().getApptNotiBadge1Appt(apptID);
    }

    //Get apptID with closest date and appt noti badge for a room jid
    public String getClosestApptIDWithNoti(String jid) {
        return rdb.selectQuery().getClosestApptIDWithNoti(jid);
    }

    //get notification badge count for schetab
    public int getApptNotiForScheTab() {
        Cursor cursor = rdb.selectQuery().getBadgeCountForScheTab();
        int count = cursor.getCount();
        cursor.close();

        return count;
    }

    //get host name for a grp appt
    public String getApptHostNameGrp(String apptID) {
        Cursor cursor = rdb.selectQuery().getApptHostNameGrp(apptID);

        if (cursor.moveToFirst()) {
            String phoneName = cursor.getString(0);
            if (phoneName == null || phoneName.equals("")) {
                String displayName = cursor.getString(1);
                String phoneNumber = cursor.getString(2);
                phoneName = displayName + " " + phoneNumber;
            }
            cursor.close();

            return phoneName;
        }

        return null;
    }

    //get number of going members in an appt
    public int getApptGoingMemCount(String apptID) {
        Cursor cursor = rdb.selectQuery().getGoingMemCount(apptID);

        int count = cursor.getCount();
        cursor.close();

        return count;
    }

    //get number of undecided members in an appt
    public int getApptUndecMemCount(String apptID) {
        Cursor cursor = rdb.selectQuery().getUndecMemCount(apptID);
        int count = cursor.getCount();
        cursor.close();

        return count;
    }

    //get number of not going members in an appt
    public int getNGMemCount(String apptID) {
        Cursor cursor = rdb.selectQuery().getNGMemCount(apptID);
        int count = cursor.getCount();
        cursor.close();

        return count;
    }

    //get list of dates for appts with selfApptStatus as going
    public List<CalendarDay> getHostingGoingCDList() {
        List<CalendarDay> calendarDayList = new ArrayList<>();

        Cursor cursor = rdb.selectQuery().getHostingGoingCDList();
        if (cursor.moveToFirst()) {
            do {
                calendarDayList.add(CalendarDay.from(new Date(cursor.getLong(0))));
            } while (cursor.moveToNext());
        }
        cursor.close();

        return calendarDayList;
    }

    public boolean checkIfDateHasAppt(Long date) {
        int Count = 0;
        Cursor cursor = rdb.selectQuery().getHasApptCount_date(date);
        if (cursor.moveToFirst()) {
            return cursor.getCount() > 0;
        }

        return false;
    }

    //get list of dates for appts with selfApptStatus as inputted
    public List<CalendarDay> getApptCDList(int selfApptStatus) {
        List<CalendarDay> calendarDayList = new ArrayList<>();

        Cursor cursor = rdb.selectQuery().getApptCDList(selfApptStatus);
        if (cursor.moveToFirst()) {
            do {
                calendarDayList.add(CalendarDay.from(new Date(cursor.getLong(0))));
            } while (cursor.moveToNext());
        }
        cursor.close();

        return calendarDayList;

    }

    /*----------- [K04_03_03E] [END] Appointment ----------- */


    /*----------- [K04_03_04] GroupMem ----------- */
    //get count of members in a group
    private int getMemCountFromGrpMem(String roomJid) {
//
//        Cursor cursor = db.rawQuery("select distinct jid from GROUPMEM where roomjid = '" +
//                roomJid + "'", null);
        Cursor cursor = rdb.selectQuery().getMemCountFromGrpMem(roomJid);
        int count = cursor.getCount();
        cursor.close();

        return count;
    }
    /*----------- [K04_03_04E] [END] GroupMem ----------- */


    /*----------- [K04_03_99] Others ----------- */
    //get total notification badge count for all chat/sche rooms
    public int getTotalBadgeCountChatSche() {
        return rdb.selectQuery().getTotalBadgeCountChat() + rdb.selectQuery().getTotalBadgeCountSche();
    }
    /*----------- [K04_03_99E] [END] Others ----------- */
    /*/////////////// [K04_02E] GET: Single-Info-Related-END ///////////////*/


    /*/////////////// [K04_03] GET: Multi-Info-Related ///////////////*/
    //get list of jid from CR with status = 1
    public List<String> get_jidStatus1FromCR() {
        List<String> list = new ArrayList<>();

        Cursor cursor = rdb.selectQuery().get_jidStatus1FromCR();
        if (cursor.moveToFirst()) {
            String jid;
            String userJid = preferences.getValue(context, GlobalVariables.STRPREF_USER_ID);
            do {
                jid = cursor.getString(0);
                if (!jid.equals(userJid)) {
                    list.add(jid);
                }
            } while (cursor.moveToNext());
        }
        cursor.close();

        return list;
    }

    //get list of jid from CR with status = 1 NOT EXIST in group
    public List<String> get_jidStatus1FromCRGrpMem(String roomJid) {
        List<String> list = new ArrayList<>();

        Cursor cursor = rdb.selectQuery().get_jidStatus1FromCRGrpMem();
        if (cursor.moveToFirst()) {
            String jid;
            String userJid = preferences.getValue(context, GlobalVariables.STRPREF_USER_ID);
            do {
                jid = cursor.getString(0);

                //only add jid to list if not userJid or already exist in room
                if (!jid.equals(userJid) && getMemberExist(roomJid, jid) == 0) {
                    list.add(jid);
                }
            } while (cursor.moveToNext());
        }
        cursor.close();

        return list;
    }

    //get list of member jid from grpmem in a grp
    public List<String> get_jidFromGrpMem(String roomJid) {
        List<String> list = new ArrayList<>();

        Cursor cursor = rdb.selectQuery().get_jidFromGrpMem(roomJid);
        if (cursor.moveToFirst()) {
            do {
                list.add(cursor.getString(0));
            } while (cursor.moveToNext());
        }
        cursor.close();

        return list;
    }

    //get list of apptIDs from a room based on jid
    public List<String> get_apptIDFromRoom(String jid) {
        List<String> list = new ArrayList<>();

        Cursor cursor = rdb.selectQuery().get_apptIDFromRoom(jid);
        if (cursor.moveToFirst()) {
            do {
                list.add(cursor.getString(0));
            } while (cursor.moveToNext());
        }
        cursor.close();

        return list;
    }

    //get user profile (name, number, photo) from contact roster
    public List<AddMember> get_userProfile(String jid) {
        List<AddMember> list = new ArrayList<>();

        Cursor cursor = rdb.selectQuery().get_userProfile(jid);

        if (cursor.moveToFirst()) {
            String phoneName = cursor.getString(0);
            String phoneNumber = cursor.getString(1);
            byte[] imgByteThumb = cursor.getBlob(2);

            if (phoneName == null || phoneName.equals("")) {
                phoneName = cursor.getString(3) + " " + phoneNumber;
            }
            cursor.close();

            list.add(new AddMember(phoneName, jid, phoneNumber, imgByteThumb));
        } else {
            list = null;
        }
        return list;
    }

    //get user displayname and number from contact roster
    public List<AddMember> get_displayNamePhone(String jid) {
        List<AddMember> list = new ArrayList<>();

        Cursor cursor = rdb.selectQuery().get_displayNamePhone(jid);

        if (cursor.moveToFirst()) {
            String displayName = cursor.getString(0);
            String phoneNumber = cursor.getString(1);

            cursor.close();

            list.add(new AddMember(displayName, jid, phoneNumber, null));
        } else {
            list = null;
        }
        return list;
    }

    //get group profile from contact roster
    public List<AddMember> get_grpProfile(String jid) {
        List<AddMember> list = new ArrayList<>();

        Cursor cursor = rdb.selectQuery().get_grpProfile(jid);

        if (cursor.moveToFirst()) {
            byte[] imgByteThumb = cursor.getBlob(0);
            String displayName = cursor.getString(1);

            cursor.close();

            list.add(new AddMember(displayName, jid, null, imgByteThumb));
        } else {
            list = null;
        }
        return list;
    }

    /*/////////////// [K04_03E] GET: Multi-Info-Related-END ///////////////*/
    /*============================ [J04E - GET-FROM-DB-END] ===========================*/


    /*============================ [J05 - UPDATE-DB-RELATED] ===========================*/
    /*/////////////// [K05_01] UPDATE: Contact-Roster-Related ///////////////*/

    //[DONE CR] update user profile into contact roster (not in phonebook)
    public void checkAndAddUserProfileToContactRoster(
            String phonenumber, String phoneName, String displayName, String jid, String
            imageURL, String image_data, byte[] imageByte100) {
        try {
            ContentValues cvCR = new ContentValues();
            cvCR.put(CR_COLUMN_JID, jid);
            cvCR.put(CR_COLUMN_DISPLAYNAME, displayName);
            cvCR.put(CR_COLUMN_PHOTOURL, imageURL);

            if (phonenumber != null) {
                cvCR.put(CR_COLUMN_PHONENUMBER, phonenumber);
            }
            if (phoneName != null) {
                cvCR.put(CR_COLUMN_PHONENAME, phoneName);
            }

            //check has thumb
            if (image_data != null && !image_data.equals("Empty")) {
                cvCR.put(CR_COLUMN_PROFILEPHOTO, Base64.decode(image_data, Base64.DEFAULT));
            }

            //check has full
            if (imageByte100 != null) {
                cvCR.put(CR_COLUMN_PROFILEFULL, imageByte100);
            } else {
                cvCR.putNull(CR_COLUMN_PROFILEFULL);
            }

            //check has imgURL
            if (imageURL != null) {
                cvCR.put(CR_COLUMN_PHOTOURL, imageURL);
            }

            if (getContactRosterExist(jid) > 0) { //exists update
                updateRDB1Col(CR_TABLE_NAME, cvCR, CR_COLUMN_JID, jid);
            } else { //not exist, insert
                rdb.insertQuery().insertContactRoster(ContactRoster.fromContentValues(cvCR));
            }

        } catch (Exception e) {
            Log.e(TAG, "checkAndAddUserProfileToContactRoster: ", e);
            saveLogsToDb("checkAndAddUserProfileToContactRoster", e.getMessage(), System.currentTimeMillis());
        }
    }

    //function to check if phone number is in phonebook
    private String checkPhoneBookExists(String phoneNumber) {
        String[] projection = new String[]{
                ContactsContract.PhoneLookup.DISPLAY_NAME,
                ContactsContract.PhoneLookup._ID};

        Uri contactUri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(phoneNumber));

        Cursor cursor = context.getContentResolver().query(contactUri, projection, null, null, null);

        if (cursor != null) {
            if (cursor.moveToFirst()) { //exists, return phone name
                String phoneName = cursor.getString(cursor.getColumnIndex(ContactsContract.PhoneLookup.DISPLAY_NAME));
                cursor.close();

                return phoneName;
            } else { //not exist, return null
                cursor.close();

                return null;
            }
        } else { //not exist, return null
            return null;
        }
    }

    //add new contact to CR without any other actions
    private void addNewContactToCR(String phoneName, String phoneNumber, String jid, String displayName,
                                   String imageURL, byte[] imageByte) {
        ContentValues cvCR = new ContentValues();

        cvCR.put(CR_COLUMN_JID, jid);
        cvCR.put(CR_COLUMN_PHONENUMBER, phoneNumber);
        cvCR.put(CR_COLUMN_DISPLAYNAME, displayName);
        cvCR.put(CR_COLUMN_PHONENAME, phoneName);
        cvCR.put(CR_COLUMN_PHOTOURL, imageURL);
        cvCR.put(CR_COLUMN_PROFILEPHOTO, imageByte);

        updateRDB1Col(CR_TABLE_NAME, cvCR, CR_COLUMN_JID, jid);
    }

    //update profile image to contact roster
    public void updateIMGContactRoster(String jid, byte[] imageByteThumb, byte[] imageByteFull) {
        try {
            ContentValues cvCR = new ContentValues();
            cvCR.put(CR_COLUMN_JID, jid);
            cvCR.put(CR_COLUMN_PROFILEPHOTO, imageByteThumb);
            cvCR.put(CR_COLUMN_PROFILEFULL, imageByteFull);

            if (getContactRosterExist(jid) > 0) { //if self jid exists in roster, update
                updateRDB1Col(CR_TABLE_NAME, cvCR, CR_COLUMN_JID, jid);
            } else { //not exist, insert
                rdb.insertQuery().insertContactRoster(ContactRoster.fromContentValues(cvCR));
            }
        } catch (Exception e) {
        }
    }

    //[DONE CR] update new contact to DBs
    public void updateNewContactToContactRoster(String jid, String phonenumber, String
            phoneName, String imageURL, String image_data) {
        //delete add to contact from MESSAGE
        deleteRDB2Col(MSG_TABLE_NAME, MSG_JID, MSG_ISSENDER, jid, "3");

        //save to contact roster
        ContentValues cvCR = new ContentValues();

        cvCR.put(CR_COLUMN_JID, jid);
        if (image_data != null && !image_data.equals("Empty")) {
            byte[] imgByte = Base64.decode(image_data, Base64.DEFAULT);
            cvCR.put(CR_COLUMN_PROFILEPHOTO, imgByte);
        }
        if (imageURL != null && !imageURL.equals("")) {
            cvCR.put(CR_COLUMN_PHOTOURL, imageURL);
        }
        cvCR.put(CR_COLUMN_PHONENAME, phoneName);
        if (phonenumber != null) {
            cvCR.put(CR_COLUMN_PHONENUMBER, phonenumber);
        }

        if (getContactRosterExist(jid) > 0) {
            updateRDB1Col(CR_TABLE_NAME, cvCR, CR_COLUMN_JID, jid);
        } else {
            cvCR.put(CR_COLUMN_JID, jid);

            rdb.insertQuery().insertContactRoster(ContactRoster.fromContentValues(cvCR));
        }
    }

    /*/////////////// [K05_01E] UPDATE: Contact-Roster-Related-END ///////////////*/


    /*/////////////// [K05_02] UPDATE: Chat-List-Related ///////////////*/

    //update self admin status
    public void updateSelfAdminCL(String roomJid, String adminType) {
        if (getChatListExist(roomJid) > 0) { //if room jid exists in roster indiScheList
            ContentValues cvCL = new ContentValues();

            if (adminType.equals("admin")) {
                cvCL.put(CL_COLUMN_ADMIN_SELF, 1);

            } else {
                cvCL.put(CL_COLUMN_ADMIN_SELF, 0);

            }
            updateRDB1Col(CL_TABLE_NAME, cvCL, CL_COLUMN_JID, roomJid);
        }

    }

    /*/////////////// [K05_02E] UPDATE: Chat-List-Related-END ///////////////*/


    /*/////////////// [K05_03] UPDATE: Message-Related ///////////////*/

    //add unknown contact to message
    public void checkAndInsertAddToContactMessage(String jid, long date) {
        ContentValues cvMSG = new ContentValues();

        cvMSG.put(MSG_JID, jid);
        cvMSG.put(MSG_MSGDATE, date);

        if (getAddToContactExist(jid) == 0) {
            cvMSG.put(MSG_ISSENDER, 3);

            rdb.insertQuery().insertMessage(Message.fromContentValues(cvMSG));
        } else {
            updateRDB2Col(MSG_TABLE_NAME, cvMSG, MSG_JID, MSG_ISSENDER, jid, "3");
        }
    }

    /*/////////////// [K05_03E] UPDATE: Message-Related-END ///////////////*/


    /*/////////////// [K05_04] UPDATE: GROUPMEM-Related ///////////////*/
    //check and add new members to grpmem (jid etc, no user profile)
    public void checkAndAddJidToGrpMem(String memberJid, String roomJid, String adminType,
                                       String color) {
        ContentValues cvGM = new ContentValues();
        if (color != null) {
            cvGM.put(GM_COLUMN_TEXTCOLOR, color);
        }

        if (adminType != null && adminType.equals("admin")) {
            cvGM.put(GM_COLUMN_ADMIN, 1);
        } else {
            cvGM.put(GM_COLUMN_ADMIN, 0);
        }

        if (getMemberExist(roomJid, memberJid) == 0) { //member does not exist in room
            cvGM.put(GM_COLUMN_MEMBERJID, memberJid);
            cvGM.put(GM_COLUMN_ROOMJID, roomJid);

            rdb.insertQuery().insertGroupMem(GroupMem.fromContentValues(cvGM));
        } else {
            updateRDB2Col(GM_TABLE_NAME, cvGM, GM_COLUMN_ROOMJID, GM_COLUMN_MEMBERJID, roomJid, memberJid);
        }
    }

    /*/////////////// [K05_04E] UPDATE: GROUPMEM-Related-END ///////////////*/


    /*/////////////// [K05_05] UPDATE: GROUPMEM_STATUS-Related ///////////////*/
    //update/insert appt status to grpmem_status
    public void checkAndAddStatusToGrpMemStatus(String memberJid, String roomJid, String apptID, int apptStatus) {
        String selfJid = Preferences.getInstance().getValue(Soapp.getInstance(), GlobalVariables.STRPREF_USER_ID);

        if (!memberJid.equals(selfJid)) { //only need add to GMS if not self's jid
            ContentValues cvGMS = new ContentValues();

            cvGMS.put(GMS_COLUMN_APPTSTATUS, apptStatus);

            if (getApptInGMSExist(roomJid, memberJid, apptID) == 0) { //member status does not exist in GMS
                cvGMS.put(GMS_COLUMN_ROOMJID, roomJid);
                cvGMS.put(GMS_COLUMN_MEMBERJID, memberJid);
                cvGMS.put(GMS_COLUMN_APPTID, apptID);

                rdb.insertQuery().insertGMS(GroupMem_Status.fromContentValues(cvGMS));

            } else { //update to GMS
                updateRDB3Col(GMS_TABLE_NAME, cvGMS, GMS_COLUMN_ROOMJID, GMS_COLUMN_MEMBERJID, GMS_COLUMN_APPTID,
                        roomJid, memberJid, apptID);
            }
        } else { //self's status, add to appt table
            ContentValues cvAppt = new ContentValues();
            cvAppt.put(A_COLUMN_SELFSTATUS, apptStatus);

            updateRDB1Col(A_TABLE_NAME, cvAppt, A_COLUMN_ID, apptID);
        }
    }

    /*/////////////// [K05_05E] UPDATE: GROUPMEM_STATUS-Related-END ///////////////*/


    /*/////////////// [K05_06] UPDATE: Download-Related ///////////////*/
    //[DONE CR] download image bytes from imageURL and save to DB (no refresh UI etc)
    public void saveFullandThumbFromImgURL(String imageURL, String jid) {
        DownloadFromUrlInterface client = RetrofitAPIClient.getClient().create(DownloadFromUrlInterface.class);
        retrofit2.Call<ResponseBody> call = client.downloadFileWithDynamicUrlAsync(imageURL);
        call.enqueue(new retrofit2.Callback<ResponseBody>() {
            @Override
            public void onResponse(retrofit2.Call<ResponseBody> call, retrofit2.Response<ResponseBody> response) {
                if (!response.isSuccessful()) {

                    new MiscHelper().retroLogUnsuc(response, "saveFullandThumbFromImgURL ", "JAY");
                    return;
                }
                try {
                    byte[] imageByte100 = response.body().bytes();
                    byte[] imgByteThumb = ImageHelper.resizeByteFromByteIMGThumb60dp(imageByte100, context, 60);

                    if (getContactRosterExist(jid) > 0) {
                        ContentValues cvCR = new ContentValues();

                        cvCR.put(CR_COLUMN_PROFILEFULL, imageByte100);
                        cvCR.put(CR_COLUMN_PROFILEPHOTO, imgByteThumb);
                        cvCR.put(CR_COLUMN_PHOTOURL, "");

                        updateRDB1Col(CR_TABLE_NAME, cvCR, CR_COLUMN_JID, jid);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(retrofit2.Call<ResponseBody> call, Throwable t) {
                new MiscHelper().retroLogFailure(t, "saveFullandThumbFromImgURL", "JAY");

            }
        });
    }

    /*/////////////// [K05_06E] UPDATE: Download-Related-END ///////////////*/
    /*============================ [J05E - UPDATE-DB-RELATED-END] ===========================*/


    /*=============================== [J06] MISC-TASKS-RELATED ===============================*/
    /*//////////////////////// [K06_03] MISC: Delete/Clear-Related ////////////////////////*/

    //delete images in chat detail images
    public void clearMediaInMSG(String path) {
        deleteRDB1Col(MSG_TABLE_NAME, MSG_MSGINFOURL, path);
    }

    //clear chat room messages
    public void clearMsgesInMsg(String jid) {
        deleteRDB1Col(MSG_TABLE_NAME, MSG_JID, jid);
    }

    //delete row of jid from contact roster
    public void clearCR(String jid) {
        deleteRDB1Col(CR_TABLE_NAME, CR_COLUMN_JID, jid);
    }

    //demote self admin status and set left room (disabled status = 1)
    private void clearSelfAdmin(String jid) {
        ContentValues cvCL = new ContentValues();

        cvCL.put(CL_COLUMN_ADMIN_SELF, 0);
        cvCL.put(CL_COLUMN_LASTMSG, context.getString(R.string.self_left_grp));

        updateRDB1Col(CL_TABLE_NAME, cvCL, CL_COLUMN_JID, jid);

        ContentValues cvCR = new ContentValues();
        cvCR.put(CR_COLUMN_DISABLEDSTATUS, 1);
        updateRDB1Col(CR_TABLE_NAME, cvCR, CR_COLUMN_JID, jid);
    }

    //delete all members in grpmem for a room jid
    public void clearGrpMem(String roomJid) {
        deleteRDB1Col(GM_TABLE_NAME, GM_COLUMN_ROOMJID, roomJid);
    }
    /*//////////////////////// [K06_03E] MISC: Delete/Clear-Related-END ////////////////////////*/


    /*///////////////////////////////// [K06_99] MISC: Others /////////////////////////////////*/

    //play in-appt msg tone (can customise more sounds in future)
    private void playInMsgSoundUI(boolean playSound) {
        if (StateCheck.foreground && playSound) {
            switch (preferences.getValue(Soapp.getInstance().getApplicationContext(), "devSwitchSound")) {
                case "nil": //ori
                    new SoundHelper().playRawSound(context, R.raw.soapp_msg_in);
                    break;

                case "1": //dev sound 1
                    new SoundHelper().playRawSound(context, R.raw.soapp_msg_in_bubble);
                    break;

                default: //ori
                    new SoundHelper().playRawSound(context, R.raw.soapp_msg_in);
                    break;
            }
        }
    }

    /*/////////////////////////////// [K06_99E] MISC: Others-END ///////////////////////////////*/

    /*============================== [J06E] MISC-TASK-RELATED-END ============================*/



    /*=============================== [J07 - NOTIFICATION-RELATED] ============================= */
    /*//////////////////////// [K07_01] NOTIFICATION: Push-Related ////////////////////////*/

    //INDI push notification for messages (text, images, audio...)
    public void push_go_indi_chat(String sender_jid, String title, String content) {
        String chatRingtone;
        Uri savedSoundUri;
        long[] vibrate;

        chatRingtone = preferences.getValue(Soapp.getInstance()
                .getApplicationContext(), "ChatRingtone");

        switch (chatRingtone) { //indi chat ringtone
            case "nil": //haven't picked ringtone, use default
                savedSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
                vibrate = new long[]{300, 300};
                break;

            case "null": //silent
                savedSoundUri = null;
                vibrate = null;
                break;

            default:
                savedSoundUri = Uri.parse(chatRingtone);
                vibrate = new long[]{300, 300};
                break;
        }

        pushChatNotiCount++;
        String current_room = preferences.getValue(context, "current_room_id");

        if (pushChatNotiCount == 1) { //if only 1 new message
            Intent intent;
            PendingIntent pendingIntent;

            prevChatJID = sender_jid;

            //get phoneName or displayName + phoneNumber from contact roster
            String name = getNameFromContactRoster(sender_jid);

            if (!name.equals("")) {
                title = name;
            }

            intent = new Intent(context, IndiChatLog.class);

            intent.putExtra("jid", sender_jid);
            intent.putExtra("remoteChat", "1");

            if (current_room == null || current_room.equals("")) { //not in an indichatlog
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                pendingIntent = PendingIntent.getActivity(context, 0 /* Request code */,
                        intent, PendingIntent.FLAG_UPDATE_CURRENT);
            } else if (sender_jid.equals(current_room)) { //in same indichatlog

                intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                pendingIntent = PendingIntent.getActivity(context, 0 /* Request code */,
                        intent, PendingIntent.FLAG_ONE_SHOT);

            } else { //in another indichatlog

                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                pendingIntent = PendingIntent.getActivity(context, 0 /* Request code */,
                        intent, PendingIntent.FLAG_ONE_SHOT);

            }

            titleChat1 = title;
            contentChat1 = content;
            NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(context)
                    .setSmallIcon(R.mipmap.push_small_logo)
                    .setLargeIcon(BitmapFactory.decodeResource(context.getResources(), R.mipmap.push_chat))
                    .setColor(context.getResources().getColor(R.color.primaryLogo))
                    .setContentTitle(titleChat1)
                    .setContentText(contentChat1)
                    .setAutoCancel(true)
                    .setSound(savedSoundUri)
                    .setVibrate(vibrate)
                    .setNumber(pushChatNotiCount)
                    .setGroup(sender_jid)
                    .setStyle(new NotificationCompat.InboxStyle()
                            .addLine(content)
                            .setBigContentTitle(titleChat1))
                    .setContentIntent(pendingIntent);

            NotificationManager notificationManager =
                    (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

            notificationManager.notify(1, notificationBuilder.build());
        } else { //if more than 1 new messages
            Intent intent;
            PendingIntent pendingIntent;
            NotificationCompat.Builder notificationBuilder;

            title = getNameFromContactRoster(sender_jid);

            if (sender_jid.equals(prevChatJID)) { //current JID same as previous
                intent = new Intent(context, IndiChatLog.class);

                intent.putExtra("jid", sender_jid);
                intent.putExtra("remoteChat", "1");

                if (current_room == null || current_room.equals("")) {
                    intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                    pendingIntent = PendingIntent.getActivity(context, 0 /* Request code */,
                            intent, PendingIntent.FLAG_UPDATE_CURRENT);
                } else if (sender_jid.equals(current_room)) { //same jid
                    intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                    pendingIntent = PendingIntent.getActivity(context, 0 /* Request code */,
                            intent, PendingIntent.FLAG_ONE_SHOT);

                } else {
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    pendingIntent = PendingIntent.getActivity(context, 0 /* Request code */,
                            intent, PendingIntent.FLAG_ONE_SHOT);
                }

            } else { //not same JID, go to chattab in home
                intent = new Intent(context, Home.class);
                pendingIntent = PendingIntent.getActivity(context, 0 /* Request code */, intent,
                        PendingIntent.FLAG_ONE_SHOT);
            }

            //outer msg (you have xx messages)
            notificationBuilder = new NotificationCompat.Builder(context)
                    .setSmallIcon(R.mipmap.push_small_logo)
                    .setLargeIcon(BitmapFactory.decodeResource(context.getResources(), R.mipmap.push_chat))
                    .setColor(context.getResources().getColor(R.color.primaryLogo))
                    .setContentTitle("Soapp")
                    .setContentText("You have " + pushChatNotiCount + " messages")
                    .setAutoCancel(true)
                    .setSound(savedSoundUri)
                    .setVibrate(vibrate)
                    .setNumber(pushChatNotiCount)
                    .setGroup(sender_jid)
                    .setContentIntent(pendingIntent);

            //inner msg
            NotificationCompat.InboxStyle inboxStyle = new NotificationCompat.InboxStyle(notificationBuilder);
            notificationBuilder.setStyle(inboxStyle);
            inboxStyle.setBigContentTitle("Soapp");

            switch (pushChatNotiCount) {
                case 2:
                    titleChat2 = title;
                    contentChat2 = content;
                    inboxStyle.addLine(titleChat1 + ": " + contentChat1);
                    inboxStyle.addLine(title + ": " + content);
                    break;

                case 3:
                    titleChat3 = title;
                    contentChat3 = content;
                    inboxStyle.addLine(titleChat1 + ": " + contentChat1);
                    inboxStyle.addLine(titleChat2 + ": " + contentChat2);
                    inboxStyle.addLine(title + ": " + content);
                    break;

                case 4:
                    titleChat4 = title;
                    contentChat4 = content;
                    inboxStyle.addLine(titleChat1 + ": " + contentChat1);
                    inboxStyle.addLine(titleChat2 + ": " + contentChat2);
                    inboxStyle.addLine(titleChat3 + ": " + contentChat3);
                    inboxStyle.addLine(title + ": " + content);
                    break;

                case 5:
                    titleChat5 = title;
                    contentChat5 = content;
                    inboxStyle.addLine(titleChat1 + ": " + contentChat1);
                    inboxStyle.addLine(titleChat2 + ": " + contentChat2);
                    inboxStyle.addLine(titleChat3 + ": " + contentChat3);
                    inboxStyle.addLine(titleChat4 + ": " + contentChat4);
                    inboxStyle.addLine(title + ": " + content);
                    break;

                //more than 5 msges
                default:
                    inboxStyle.addLine(titleChat2 + ": " + contentChat2);
                    inboxStyle.addLine(titleChat3 + ": " + contentChat3);
                    inboxStyle.addLine(titleChat4 + ": " + contentChat4);
                    inboxStyle.addLine(titleChat5 + ": " + contentChat5);
                    inboxStyle.addLine(title + ": " + content);
                    inboxStyle.addLine(" ");
                    inboxStyle.addLine("You have " + pushChatNotiCount + " unread messages");

                    titleChat2 = titleChat3;
                    titleChat3 = titleChat4;
                    titleChat4 = titleChat5;
                    titleChat5 = title;

                    contentChat2 = contentChat3;
                    contentChat3 = contentChat4;
                    contentChat4 = contentChat5;
                    contentChat5 = content;
                    break;
            }

            NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.notify(1, notificationBuilder.build());
        }
    }

    //set sound URI for push notis
    private Uri setSoundURIForPush(String prefName) {
        String chatRingtone = preferences.getValue(context, prefName);

        switch (chatRingtone) { //chat ringtone
            case "nil": //haven't picked ringtone, use default
                return RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

            case "null": //silent
                return null;

            default:
                return Uri.parse(chatRingtone);
        }
    }

    //set vibration for push notis
    private long[] setVibrateForPush(String prefName) {
        switch (preferences.getValue(context, prefName)) { //chat ringtone
            case "nil": //haven't picked ringtone, use default
                return new long[]{300, 300};

            case "null": //silent
                return null;

            default:
                return new long[]{300, 300};
        }
    }

    public void saveNotiDataToDB(Map<String, String> data, int isGroup, String apptid) {
        rdb.insertQuery().insertPushNoti(new PushNotification(data.get("sender_jid"), data.get("group_id"), data.get("title"), data.get("body"), data.get("type").split(":")[0], isGroup, apptid));
    }

    public void deleteNotiFromDB(String id, int isGroup, boolean isAppt) {
        rdb.runInTransaction(() -> {
            if (isAppt) {
                sqLiteDatabase.delete("PushNotification", "apptId = ?", new String[]{id});
            } else {
                sqLiteDatabase.delete("PushNotification", isGroup == 0 ? "senderJid = ?" : "groupJid = ? ", new String[]{id});
            }

        });
    }

    //chat
    public void push_bundle_for_NAbove(String jid1) {

        NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(context);

        prevChatJID = "";

        ArrayList<Integer> notijidSplit = new ArrayList<>();
        rdb.selectQuery().getPushMsgs("chat").observeOn(Schedulers.io())
                .flatMap((Function<List<PushNotification>, MaybeSource<List<List<PushNotification>>>>) pushNotifications -> {

                    for (int i = 0; i <= pushNotifications.size(); i++) {
                        if (i == pushNotifications.size()) {
                            notijidSplit.add(i);
                        } else {
                            if (pushNotifications.get(i).getIsGroup() == 0) {

                                if (!pushNotifications.get(i).getSenderJid().equals(prevChatJID)) {
                                    notijidSplit.add(i);
                                    prevChatJID = pushNotifications.get(i).getSenderJid();
                                } else {
                                    prevChatJID = pushNotifications.get(i).getSenderJid();
                                }

                            } else {
                                if (!pushNotifications.get(i).getGroupJid().equals(prevChatJID)) {
                                    notijidSplit.add(i);
                                    prevChatJID = pushNotifications.get(i).getGroupJid();
                                } else {
                                    prevChatJID = pushNotifications.get(i).getGroupJid();
                                }
                            }
                        }
                    }

                    List<List<PushNotification>> totalList = new ArrayList<>();
                    int start = 0, end = 0;
                    for (int i = 0; i < notijidSplit.size(); i++) {
                        start = notijidSplit.get(i);
                        if ((notijidSplit.size() - 1) == i) {
                            end = start;
                        } else {
                            end = (notijidSplit.get(i + 1));
                        }
                        totalList.add(pushNotifications.subList(start, end));
                        List<PushNotification> test = pushNotifications.subList(start, end);
                    }
                    return Maybe.create(emitter -> emitter.onSuccess(totalList));

                })
                .subscribe(new MaybeObserver<List<List<PushNotification>>>() {
                    @Override
                    public void onSubscribe(Disposable d) {

                    }

                    @Override
                    public void onSuccess(List<List<PushNotification>> lists) {
                        if (lists != null && !lists.isEmpty() && !lists.get(0).isEmpty()) {
                            Intent summaryIntent = new Intent(context, Home.class);

                            NotificationCompat.Builder summaryNotification =
                                    new NotificationCompat.Builder(context, GlobalVariables.chatNotiGroup)
                                            .setSmallIcon(R.mipmap.push_small_logo)
                                            .setLargeIcon(BitmapFactory.decodeResource(context.getResources(), R.mipmap.push_chat))
                                            .setColor(context.getResources().getColor(R.color.primaryLogo))
                                            .setContentTitle("Soapp Chat")
                                            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                                            .setGroup(GlobalVariables.chatNotiGroup)
                                            .setOnlyAlertOnce(true)
                                            .setGroupSummary(true)
                                            .setAutoCancel(true)
                                            .setLights(Color.BLUE, (int) TimeUnit.SECONDS.toMillis(1), ((int) TimeUnit.SECONDS.toMillis(1)))
                                            .setContentIntent(PendingIntent.getActivity(context, 1, summaryIntent, PendingIntent.FLAG_UPDATE_CURRENT));

                            NotificationCompat.InboxStyle summaryStyle = new NotificationCompat.InboxStyle();
                            summaryNotification.setStyle(summaryStyle);

                            String current_room = preferences.getValue(context, "current_room_id");

                            int isGroup = 0;
                            for (List<PushNotification> list : lists) {

                                int listSize = list.size();
                                int lastElement = listSize - 1;

                                String jid2 = "";
                                String groupName = "";

                                if (list != null && !list.isEmpty()) {

                                    Class intentTarget;
                                    final Intent replyIntent = new Intent("com.soapp.MsgReply");

                                    isGroup = list.get(lastElement).getIsGroup();
                                    if (isGroup == 0) {
                                        jid2 = list.get(lastElement).getSenderJid();
                                        intentTarget = IndiChatLog.class;
                                        replyIntent.putExtra("isGroup", 0);

                                    } else {
                                        jid2 = list.get(lastElement).getGroupJid();
                                        intentTarget = GroupChatLog.class;
                                        replyIntent.putExtra("isGroup", 1);
                                        groupName = getNameFromContactRoster(jid2);
                                        replyIntent.putExtra("groupname", groupName);

                                    }

                                    final Intent intent = new Intent(context, intentTarget);
                                    final String jidIntent = jid2;

                                    replyIntent.putExtra("jid", jidIntent);
                                    replyIntent.putExtra("type", "reply");

                                    intent.putExtra("jid", jidIntent);
                                    intent.putExtra("remoteChat", "1");

                                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);

                                    PendingIntent pendingIntent;
                                    if (current_room == null || current_room.equals("")) { //not in an indichatlog
                                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                        pendingIntent = PendingIntent.getActivity(context, jidIntent.hashCode() /* Request code */,
                                                intent, PendingIntent.FLAG_UPDATE_CURRENT);

                                    } else if (jidIntent.equals(current_room)) { //in same indichatlog

                                        intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                                        pendingIntent = PendingIntent.getActivity(context, jidIntent.hashCode() /* Request code */,
                                                intent, PendingIntent.FLAG_ONE_SHOT);
                                    } else { //in another indichatlog
                                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                        pendingIntent = PendingIntent.getActivity(context, jidIntent.hashCode() /* Request code */,
                                                intent, PendingIntent.FLAG_ONE_SHOT);
                                    }

                                    RemoteInput remoteInput = new RemoteInput.Builder("Reply")
                                            .setLabel("Reply")
                                            .build();

                                    PendingIntent replyPendingIntent =
                                            PendingIntent.getBroadcast(context,
                                                    ("reply" + jidIntent).hashCode(),
                                                    replyIntent,
                                                    PendingIntent.FLAG_UPDATE_CURRENT);

                                    NotificationCompat.Action replyAction =
                                            new NotificationCompat.Action.Builder(R.drawable.ic_toolbar_arrow_back_white,
                                                    "Reply", replyPendingIntent)
                                                    .addRemoteInput(remoteInput)
                                                    .build();

                                    NotificationCompat.Builder notificationbuilder = new NotificationCompat.Builder(context, GlobalVariables.chatNotiChnlID)
                                            .setSmallIcon(R.mipmap.push_small_logo)
                                            .setLargeIcon(BitmapFactory.decodeResource(context.getResources(), R.mipmap.push_chat))
                                            .setColor(context.getResources().getColor(R.color.primaryLogo))
                                            .setContentTitle(isGroup == 0 ? getNameFromContactRoster(list.get(lastElement).getSenderJid()) : groupName)
                                            .setContentText(listSize == 1 ? list.get(lastElement).getPushBody() : listSize + " new messages")
                                            .setPriority(jidIntent.equals(jid1) ? NotificationCompat.PRIORITY_MAX : NotificationCompat.PRIORITY_LOW)
                                            .setSound(jidIntent.equals(jid1) ? setSoundURIForPush(isGroup == 0 ? "ChatRingtone" : "GrpChatRingtone") : null)
                                            .setVibrate(jidIntent.equals(jid1) ? setVibrateForPush(isGroup == 0 ? "ChatRingtone" : "GrpChatRingtone") : null)
                                            .setGroup(GlobalVariables.chatNotiGroup)
                                            .addAction(replyAction)
                                            .setAutoCancel(true)
                                            .setContentIntent(pendingIntent);

                                    NotificationCompat.InboxStyle inboxStyle = new NotificationCompat.InboxStyle();
                                    notificationbuilder.setStyle(inboxStyle);


                                    int lastmsg = (lastElement - 3) < 0 ? 0 : (lastElement - 3);
                                    for (int i = lastmsg; i < listSize; i++) {
                                        PushNotification content = list.get(i);
                                        if (isGroup == 0) {
                                            inboxStyle.addLine(content.getPushBody());
                                        } else {
                                            String senderName = getNameFromContactRoster(content.getSenderJid());
                                            inboxStyle.addLine(String.format("%s : %s", senderName, content.getPushBody()));
                                        }
                                    }

                                    if (listSize > 4) {
                                        inboxStyle.addLine(String.format("\n %d more messages", listSize - 4));
                                    }

                                    notificationManagerCompat.notify("chat", jidIntent.hashCode(), notificationbuilder.build());
                                    summaryStyle.addLine(list.get(lastElement).getPushBody());
                                }
                            }
                            notificationManagerCompat.notify(1, summaryNotification.build());
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        e.printStackTrace();
                    }

                    @Override
                    public void onComplete() {
                    }
                });
    }

    //appt
    public void pushBundleAppt_Nabove(String apptId, String jid) {
        NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(context);
        ArrayList<Integer> notiApptId = new ArrayList();
        prevScheJID = "";

        rdb.selectQuery().getPushMsgs("appointment").observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .flatMap(pushNotifications -> {

                            for (int i = 0; i <= pushNotifications.size(); i++) {
                                if (i == pushNotifications.size()) {
                                    notiApptId.add(i);
                                } else {
                                    if (!pushNotifications.get(i).getApptId().equals(prevScheJID)) {
                                        notiApptId.add(i);
                                        prevScheJID = pushNotifications.get(i).getApptId();
                                    } else {
                                        prevScheJID = pushNotifications.get(i).getApptId();
                                    }
                                }
                            }

                            List<List<PushNotification>> totalList = new ArrayList<>();
                            int start = 0, end = 0;
                            for (int i = 0; i < notiApptId.size(); i++) {
                                start = notiApptId.get(i);
                                if ((notiApptId.size() - 1) == i) {
                                    end = start;
                                } else {
                                    end = (notiApptId.get(i + 1));
                                }
                                totalList.add(pushNotifications.subList(start, end));
                            }


//                    return Maybe.create(emitter -> emitter.onSuccess(totalList));
                            return Maybe.just(totalList);

                        }
                ).subscribe(new MaybeObserver<List<List<PushNotification>>>() {
            @Override
            public void onSubscribe(Disposable d) {

            }

            @Override
            public void onSuccess(List<List<PushNotification>> lists) {
                if (lists != null && !lists.isEmpty() && !lists.get(0).isEmpty()) {
                    Intent summaryIntent = new Intent(context, ScheduleTab.class);

                    NotificationCompat.Builder summaryNotification =
                            new NotificationCompat.Builder(context, GlobalVariables.apptNotiGroup)
                                    .setSmallIcon(R.mipmap.push_small_logo)
                                    .setLargeIcon(BitmapFactory.decodeResource(context.getResources(), R.mipmap.push_chat))
                                    .setColor(context.getResources().getColor(R.color.primaryLogo))
                                    .setContentTitle("Soapp Appointment")
                                    .setPriority(NotificationCompat.PRIORITY_MIN)
                                    .setGroup(GlobalVariables.apptNotiGroup)
                                    .setOnlyAlertOnce(true)
                                    .setGroupSummary(true)
                                    .setAutoCancel(true)
                                    .setLights(Color.BLUE, (int) TimeUnit.SECONDS.toMillis(1), ((int) TimeUnit.SECONDS.toMillis(1)))
                                    .setContentIntent(PendingIntent.getActivity(context, 1, summaryIntent, PendingIntent.FLAG_UPDATE_CURRENT));

                    NotificationCompat.InboxStyle summaryStyle = new NotificationCompat.InboxStyle();
                    summaryNotification.setStyle(summaryStyle);

                    String current_room = preferences.getValue(context, "current_room_id");

                    int isGroup = 0;
                    for (List<PushNotification> list : lists) {
                        Collections.reverse(list);

                        int listSize = list.size();
                        int lastElement = listSize - 1;

                        String jid2 = "";
                        String apptid = "";
                        String groupName = "";
                        String status = "";

                        if (list != null && !list.isEmpty()) {

                            Class intentTarget;
                            final Intent replyIntent = new Intent("com.soapp.MsgReply");
                            apptid = list.get(lastElement).getApptId();
                            isGroup = list.get(lastElement).getIsGroup();
                            if (isGroup == 0) {
                                jid2 = list.get(lastElement).getSenderJid();
                                intentTarget = IndiScheLog.class;
                                replyIntent.putExtra("isGroup", 0);

                            } else {
                                jid2 = list.get(lastElement).getGroupJid();
                                intentTarget = GroupScheLog.class;
                                replyIntent.putExtra("isGroup", 1);
                                groupName = getNameFromContactRoster(jid2);
                                replyIntent.putExtra("groupname", groupName);

                            }

                            final Intent intent = new Intent(context, intentTarget);
                            final String jidIntent = jid2;

                            replyIntent.putExtra("jid", jidIntent);
                            replyIntent.putExtra("apptID", apptid);
                            replyIntent.putExtra("type", "appointment");

                            intent.putExtra("apptID", apptid);
                            intent.putExtra("jid", jidIntent);

                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);

                            PendingIntent pendingIntent;
                            if (current_room == null || current_room.equals("")) { //not in an indichatlog
                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                pendingIntent = PendingIntent.getActivity(context, apptid.hashCode() /* Request code */,
                                        intent, PendingIntent.FLAG_UPDATE_CURRENT);

                            } else if (jidIntent.equals(current_room)) { //in same indichatlog

                                intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                                pendingIntent = PendingIntent.getActivity(context, apptid.hashCode() /* Request code */,
                                        intent, PendingIntent.FLAG_ONE_SHOT);
                            } else { //in another indichatlog
                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                pendingIntent = PendingIntent.getActivity(context, apptid.hashCode() /* Request code */,
                                        intent, PendingIntent.FLAG_ONE_SHOT);
                            }

                            final Intent goingintent = replyIntent;
                            goingintent.putExtra("status", 1);


                            final PendingIntent replyGoingPendingIntent =
                                    PendingIntent.getBroadcast(context,
                                            ("replyApptGoing" + apptid).hashCode(),
                                            goingintent,
                                            PendingIntent.FLAG_UPDATE_CURRENT);

                            final Intent ntgoingintent = replyIntent;
                            ntgoingintent.putExtra("status", 3);
                            final PendingIntent replyNtGoingPendingIntent =
                                    PendingIntent.getBroadcast(context,
                                            ("replyApptNtGoing" + apptid).hashCode(),
                                            ntgoingintent,
                                            PendingIntent.FLAG_UPDATE_CURRENT);

                            NotificationCompat.Action replyGoingAction =
                                    new NotificationCompat.Action.Builder(R.drawable.ic_toolbar_arrow_back_white,
                                            "Going", replyGoingPendingIntent)
                                            .build();
                            NotificationCompat.Action replyNtGoingAction =
                                    new NotificationCompat.Action.Builder(R.drawable.ic_toolbar_arrow_back_white,
                                            "Not Going", replyNtGoingPendingIntent)
                                            .build();

                            NotificationCompat.Builder notificationbuilder = new NotificationCompat.Builder(context, GlobalVariables.apptNotiChnlID)
                                    .setSmallIcon(R.mipmap.push_small_logo)
                                    .setLargeIcon(BitmapFactory.decodeResource(context.getResources(), R.mipmap.push_appt))
                                    .setColor(context.getResources().getColor(R.color.primaryLogo))
                                    .setContentTitle(String.format("%s with %s", getApptTitle(apptid), isGroup == 0 ? getNameFromContactRoster(list.get(lastElement).getSenderJid()) : groupName))
                                    .setContentText(listSize == 1 ? list.get(lastElement).getPushBody() : listSize + " new messages")
                                    .setPriority(apptId.equals(apptid) ? NotificationCompat.PRIORITY_MAX : NotificationCompat.PRIORITY_LOW)
                                    .setSound(jidIntent.equals(jid) ? setSoundURIForPush(isGroup == 0 ? "ChatRingtone" : "GrpChatRingtone") : null)
                                    .setVibrate(jidIntent.equals(jid) ? setVibrateForPush(isGroup == 0 ? "ChatRingtone" : "GrpChatRingtone") : null)
                                    .setGroup(GlobalVariables.apptNotiGroup)
                                    .setAutoCancel(true)
                                    .setContentIntent(pendingIntent);

                            switch (getApptStatusSelf(apptid)) {

                                case 0:
                                    status = "Host";
                                    break;

                                case 1:
                                    status = "Going";
                                    notificationbuilder.addAction(replyNtGoingAction);
                                    break;

                                case 3:
                                    status = "Not Going";
                                    notificationbuilder.addAction(replyGoingAction);
                                    break;

                                default:
                                    status = "Undecided";
                                    notificationbuilder.addAction(replyGoingAction)
                                            .addAction(replyNtGoingAction);
                                    break;
                            }

                            NotificationCompat.InboxStyle inboxStyle = new NotificationCompat.InboxStyle();
                            notificationbuilder.setStyle(inboxStyle);

                            int lastmsg = (lastElement - 3) < 0 ? 0 : (lastElement - 3);
                            for (int i = lastmsg; i < listSize; i++) {
                                PushNotification content = list.get(i);
                                inboxStyle.addLine(content.getPushBody());
                            }

                            if (listSize > 4) {
                                inboxStyle.addLine(String.format("\n %d more messages", listSize - 4));
                            }
                            inboxStyle.addLine("Current Status : " + status);

                            notificationManagerCompat.notify("appt", apptid.hashCode(), notificationbuilder.build());
                            summaryStyle.addLine(list.get(lastElement).getPushBody());
                        }
                    }
                    notificationManagerCompat.notify(2, summaryNotification.build());
                }
            }

            @Override
            public void onError(Throwable e) {

            }

            @Override
            public void onComplete() {

            }
        });
    }

    public void pushMethodCaller(String jid, String apptid) {
        push_bundle_for_NAbove(jid);
        pushBundleAppt_Nabove(apptid, jid);
    }

    //GRP push notification for messages (text, images, audio...)
    public void push_go_grp_chat(String sender_jid, String title, String content, String
            group_id) {
        String chatRingtone;
        Uri savedSoundUri;
        long[] vibrate;

        chatRingtone = preferences.getValue(Soapp.getInstance().getApplicationContext(), "GrpChatRingtone");

        switch (chatRingtone) { //grp chat ringtone
            case "nil": //haven't picked ringtone, use default
                savedSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
                vibrate = new long[]{300, 300};
                break;

            case "null": //silent
                savedSoundUri = null;
                vibrate = null;
                break;

            default:
                savedSoundUri = Uri.parse(chatRingtone);
                vibrate = new long[]{300, 300};
                break;
        }

        pushChatNotiCount++;
        String current_room = preferences.getValue(context, "current_room_id");

        if (pushChatNotiCount == 1) { //if only 1 new message
            Intent intent;
            PendingIntent pendingIntent;

            prevChatJID = group_id;

            Cursor cursor = rdb.selectQuery().push_go_grp_chat(group_id);
            if (cursor.moveToFirst()) { //grp exists in roster indiScheList
                title = cursor.getString(0);

                String displayName = getNameFromContactRoster(sender_jid);

                //change content to displayname: content if got member displayname
                if (displayName != null && !displayName.trim().equals("") && !displayName
                        .equals("null")) {
                    title = title + " #" + displayName;
                }
            }
            cursor.close();

            intent = new Intent(context, GroupChatLog.class);

            intent.putExtra("remoteChat", "1");
            intent.putExtra("jid", group_id);

            if (current_room == null || current_room.equals("")) { //not in a grpchatlog
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                pendingIntent = PendingIntent.getActivity(context,
                        0 /* Request code */, intent, PendingIntent.FLAG_UPDATE_CURRENT);

            } else if (group_id.equals(current_room)) { //in same grpchatlog
                intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                pendingIntent = PendingIntent.getActivity(context, 0 /* Request code */, intent,
                        PendingIntent.FLAG_ONE_SHOT);

            } else { //in another grpchatlog
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                pendingIntent = PendingIntent.getActivity(context, 0 /* Request code */, intent,
                        PendingIntent.FLAG_ONE_SHOT);

            }

            titleChat1 = title;
            contentChat1 = content;
            NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(context)
                    .setSmallIcon(R.mipmap.push_small_logo)
                    .setLargeIcon(BitmapFactory.decodeResource(context.getResources(), R.mipmap.push_chat))
                    .setColor(context.getResources().getColor(R.color.primaryLogo))
                    .setContentTitle(titleChat1)
                    .setContentText(contentChat1)
                    .setAutoCancel(true)
                    .setSound(savedSoundUri)
                    .setVibrate(vibrate)
                    .setNumber(pushChatNotiCount)
                    .setGroup(group_id)
                    .setStyle(new NotificationCompat.InboxStyle()
                            .addLine(content)
                            .setBigContentTitle(titleChat1))
                    .setContentIntent(pendingIntent);

            NotificationManager notificationManager =
                    (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

            notificationManager.notify(1, notificationBuilder.build());
        } else { //if more than 1 new messages
            Intent intent;
            PendingIntent pendingIntent;
            NotificationCompat.Builder notificationBuilder;


//            Cursor cursor = db.rawQuery("select displayname from CONTACT_ROSTER where jid = '" + group_id + "'", null);
            Cursor cursor = rdb.selectQuery().push_go_grp_chat(group_id);

            if (cursor.moveToFirst()) { //grp exists in roster indiScheList
                title = cursor.getString(0);

                String displayName = getNameFromContactRoster(sender_jid);

                //change content to displayname: content if got member displayname
                if (displayName != null && !displayName.trim().equals("") && !displayName
                        .equals("null")) {
                    title = title + " #" + displayName;
                }
            }
            cursor.close();

            if (group_id.equals(prevChatJID)) { //current JID same as previous
                intent = new Intent(context, GroupChatLog.class);

                intent.putExtra("jid", group_id);
                intent.putExtra("remoteChat", "1");

                if (current_room == null || current_room.equals("")) {
                    intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                    pendingIntent = PendingIntent.getActivity(context,
                            0 /* Request code */, intent, PendingIntent.FLAG_UPDATE_CURRENT);

                } else if (group_id.equals(current_room)) { //same jid
                    intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                    pendingIntent = PendingIntent.getActivity(context, 0 /* Request code */, intent,
                            PendingIntent.FLAG_ONE_SHOT);

                } else {
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    pendingIntent = PendingIntent.getActivity(context, 0 /* Request code */, intent,
                            PendingIntent.FLAG_ONE_SHOT);

                }

            } else {
                intent = new Intent(context, Home.class);
                pendingIntent = PendingIntent.getActivity(context, 0 /* Request code */, intent,
                        PendingIntent.FLAG_ONE_SHOT);
            }

            //outer msg (you have xx messages)
            notificationBuilder = new NotificationCompat.Builder(context)
                    .setSmallIcon(R.mipmap.push_small_logo)
                    .setLargeIcon(BitmapFactory.decodeResource(context.getResources(), R.mipmap.push_chat))
                    .setColor(context.getResources().getColor(R.color.primaryLogo))
                    .setContentTitle("Soapp")
                    .setContentText("You have " + pushChatNotiCount + " messages")
                    .setAutoCancel(true)
                    .setSound(savedSoundUri)
                    .setVibrate(vibrate)
                    .setNumber(pushChatNotiCount)
                    .setGroup(group_id)
                    .setContentIntent(pendingIntent);

            //inner msg
            NotificationCompat.InboxStyle inboxStyle = new NotificationCompat.InboxStyle(notificationBuilder);
            notificationBuilder.setStyle(inboxStyle);
            inboxStyle.setBigContentTitle("Soapp");

            switch (pushChatNotiCount) {
                case 2:
                    titleChat2 = title;
                    contentChat2 = content;
                    inboxStyle.addLine(titleChat1 + ": " + contentChat1);
                    inboxStyle.addLine(title + ": " + content);
                    break;

                case 3:
                    titleChat3 = title;
                    contentChat3 = content;
                    inboxStyle.addLine(titleChat1 + ": " + contentChat1);
                    inboxStyle.addLine(titleChat2 + ": " + contentChat2);
                    inboxStyle.addLine(title + ": " + content);
                    break;

                case 4:
                    titleChat4 = title;
                    contentChat4 = content;
                    inboxStyle.addLine(titleChat1 + ": " + contentChat1);
                    inboxStyle.addLine(titleChat2 + ": " + contentChat2);
                    inboxStyle.addLine(titleChat3 + ": " + contentChat3);
                    inboxStyle.addLine(title + ": " + content);
                    break;

                case 5:
                    titleChat5 = title;
                    contentChat5 = content;
                    inboxStyle.addLine(titleChat1 + ": " + contentChat1);
                    inboxStyle.addLine(titleChat2 + ": " + contentChat2);
                    inboxStyle.addLine(titleChat3 + ": " + contentChat3);
                    inboxStyle.addLine(titleChat4 + ": " + contentChat4);
                    inboxStyle.addLine(title + ": " + content);
                    break;

                //more than 5 msges
                default:
                    inboxStyle.addLine(titleChat2 + ": " + contentChat2);
                    inboxStyle.addLine(titleChat3 + ": " + contentChat3);
                    inboxStyle.addLine(titleChat4 + ": " + contentChat4);
                    inboxStyle.addLine(titleChat5 + ": " + contentChat5);
                    inboxStyle.addLine(title + ": " + content);
                    inboxStyle.addLine(" ");
                    inboxStyle.addLine("You have " + pushChatNotiCount + " unread messages");

                    titleChat2 = titleChat3;
                    titleChat3 = titleChat4;
                    titleChat4 = titleChat5;
                    titleChat5 = title;

                    contentChat2 = contentChat3;
                    contentChat3 = contentChat4;
                    contentChat4 = contentChat5;
                    contentChat5 = content;
                    break;
            }

            NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.notify(1, notificationBuilder.build());
        }
    }

    //INDI push notification for schedule (date, time, status...)
    public void push_go_indi_sche(String sender_jid, String title, String content, String apptID) {
        String chatRingtone;
        Uri savedSoundUri;
        long[] vibrate;

        chatRingtone = preferences.getValue(context, "ChatRingtone");

        switch (chatRingtone) { //indi chat ringtone - customise in future for sche ringtone
            case "nil": //haven't picked ringtone, use default
                savedSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
                vibrate = new long[]{300, 300};
                break;

            case "null": //silent
                savedSoundUri = null;
                vibrate = null;
                break;

            default:
                savedSoundUri = Uri.parse(chatRingtone);
                vibrate = new long[]{300, 300};
                break;
        }

        pushScheNotiCount++;
        String current_room = preferences.getValue(context, "current_room_id");

        if (pushScheNotiCount == 1) { //if only 1 new message
            Intent intent;
            PendingIntent pendingIntent;

            prevScheJID = sender_jid;

            title = getNameFromContactRoster(sender_jid);

            intent = new Intent(context, IndiScheLog.class);

            intent.putExtra("remoteSche", "1");
            intent.putExtra("jid", sender_jid);
            intent.putExtra("apptID", apptID);

            if (current_room == null || current_room.equals("")) {
                intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                pendingIntent = PendingIntent.getActivity(context,
                        0 /* Request code */, intent, PendingIntent.FLAG_UPDATE_CURRENT);
            } else if (sender_jid.equals(current_room)) { //same jid
                intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                pendingIntent = PendingIntent.getActivity(context, 0 /* Request code */, intent,
                        PendingIntent.FLAG_ONE_SHOT);
            } else {
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                pendingIntent = PendingIntent.getActivity(context, 0 /* Request code */, intent,
                        PendingIntent.FLAG_ONE_SHOT);
            }

            titleSche1 = title;
            contentSche1 = content;
            NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(context)
                    .setSmallIcon(R.mipmap.push_small_logo)
                    .setLargeIcon(BitmapFactory.decodeResource(context.getResources(), R.mipmap.push_appt))
                    .setColor(context.getResources().getColor(R.color.primaryLogo))
                    .setContentTitle(title)
                    .setContentText(content)
                    .setAutoCancel(true)
                    .setSound(savedSoundUri)
                    .setVibrate(vibrate)
                    .setNumber(pushScheNotiCount)
                    .setGroup(sender_jid)
                    .setContentIntent(pendingIntent);

            NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.notify(2, notificationBuilder.build());

        } else { //if more than 1 new messages
            Intent intent;
            PendingIntent pendingIntent;
            NotificationCompat.Builder notificationBuilder;

            if (sender_jid.equals(prevScheJID)) { //current JID same as previous
                intent = new Intent(context, IndiScheLog.class);

                intent.putExtra("remoteSche", "1");
                intent.putExtra("jid", sender_jid);
                intent.putExtra("apptID", apptID);

                if (current_room == null || current_room.equals("")) {
                    intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                    pendingIntent = PendingIntent.getActivity(context,
                            0 /* Request code */, intent, PendingIntent.FLAG_UPDATE_CURRENT);
                } else if (sender_jid.equals(current_room)) { //same jid
                    intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                    pendingIntent = PendingIntent.getActivity(context, 0 /* Request code */, intent,
                            PendingIntent.FLAG_ONE_SHOT);
                } else {
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    pendingIntent = PendingIntent.getActivity(context, 0 /* Request code */, intent,
                            PendingIntent.FLAG_ONE_SHOT);
                }

            } else { //not same JID, go to chattab in home
                intent = new Intent(context, Home.class);
                pendingIntent = PendingIntent.getActivity(context, 0 /* Request code */, intent,
                        PendingIntent.FLAG_ONE_SHOT);
            }

            //outer msg (you have xx appointment updates)
            notificationBuilder = new NotificationCompat.Builder(context)
                    .setSmallIcon(R.mipmap.push_small_logo)
                    .setLargeIcon(BitmapFactory.decodeResource(context.getResources(), R.mipmap.push_appt))
                    .setColor(context.getResources().getColor(R.color.primaryLogo))
                    .setContentTitle("Soapp")
                    .setContentText("You have " + pushScheNotiCount + " appointment updates")
                    .setAutoCancel(true)
                    .setSound(savedSoundUri)
                    .setVibrate(vibrate)
                    .setNumber(pushScheNotiCount)
                    .setGroup(sender_jid)
                    .setContentIntent(pendingIntent);

            //inner msg
            NotificationCompat.InboxStyle inboxStyle = new NotificationCompat.InboxStyle(notificationBuilder);
            notificationBuilder.setStyle(inboxStyle);
            inboxStyle.setBigContentTitle("Soapp");

            switch (pushScheNotiCount) {
                case 2:
                    titleSche2 = title;
                    contentSche2 = content;
                    inboxStyle.addLine(titleSche1 + ": " + contentSche1);
                    inboxStyle.addLine(title + ": " + content);
                    break;

                case 3:
                    titleSche3 = title;
                    contentSche3 = content;
                    inboxStyle.addLine(titleSche1 + ": " + contentSche1);
                    inboxStyle.addLine(titleSche2 + ": " + contentSche2);
                    inboxStyle.addLine(title + ": " + content);
                    break;

                case 4:
                    titleSche4 = title;
                    contentSche4 = content;
                    inboxStyle.addLine(titleSche1 + ": " + contentSche1);
                    inboxStyle.addLine(titleSche2 + ": " + contentSche2);
                    inboxStyle.addLine(titleSche3 + ": " + contentSche3);
                    inboxStyle.addLine(title + ": " + content);
                    break;

                case 5:
                    titleSche5 = title;
                    contentSche5 = content;
                    inboxStyle.addLine(titleSche1 + ": " + contentSche1);
                    inboxStyle.addLine(titleSche2 + ": " + contentSche2);
                    inboxStyle.addLine(titleSche3 + ": " + contentSche3);
                    inboxStyle.addLine(titleSche4 + ": " + contentSche4);
                    inboxStyle.addLine(title + ": " + content);
                    break;

                //more than 5 msges
                default:
                    inboxStyle.addLine(titleSche2 + ": " + contentSche2);
                    inboxStyle.addLine(titleSche3 + ": " + contentSche3);
                    inboxStyle.addLine(titleSche4 + ": " + contentSche4);
                    inboxStyle.addLine(titleSche5 + ": " + contentSche5);
                    inboxStyle.addLine(title + ": " + content);
                    inboxStyle.addLine(" ");
                    inboxStyle.addLine("You have " + pushScheNotiCount + " appointment updates");

                    titleSche2 = titleSche3;
                    titleSche3 = titleSche4;
                    titleSche4 = titleSche5;
                    titleSche5 = title;

                    contentSche2 = contentSche3;
                    contentSche3 = contentSche4;
                    contentSche4 = contentSche5;
                    contentSche5 = content;
                    break;
            }

            NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.notify(2, notificationBuilder.build());
        }
    }

    //GRP push notification for schedule (date, time, status...)
    public void push_go_grp_sche(String sender_jid, String title, String content, String
            group_id, String apptID) {
        String chatRingtone;
        Uri savedSoundUri;
        long[] vibrate;

        chatRingtone = preferences.getValue(context, "GrpChatRingtone");

        switch (chatRingtone) { //indi chat ringtone - customise in future for sche ringtone
            case "nil": //haven't picked ringtone, use default
                savedSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
                vibrate = new long[]{300, 300};
                break;

            case "null": //silent
                savedSoundUri = null;
                vibrate = null;
                break;

            default:
                savedSoundUri = Uri.parse(chatRingtone);
                vibrate = new long[]{300, 300};
                break;
        }

        pushScheNotiCount++;
        String current_room = preferences.getValue(context, "current_room_id");

        if (pushScheNotiCount == 1) { //if only 1 new message
            Intent intent;
            PendingIntent pendingIntent;

            prevScheJID = group_id;


//            Cursor cursor = db.rawQuery("select DisplayName from CONTACT_ROSTER where ContactJid = '" + group_id + "'", null);
            Cursor cursor = rdb.selectQuery().push_go_grp_chat(group_id);
            if (cursor.moveToFirst()) { //grp exists in roster indiScheList
                title = cursor.getString(0);

                String displayName = getNameFromContactRoster(sender_jid);

                //change content to displayname: content if got member displayname
                if (displayName != null && !displayName.trim().equals("") && !displayName
                        .equals("null")) {
                    title = title + " #" + displayName;
                }
            }
            cursor.close();

            intent = new Intent(context, GroupScheLog.class);

            intent.putExtra("remoteSche", "1");
            intent.putExtra("jid", group_id);
            intent.putExtra("apptID", apptID);

            if (current_room == null || current_room.equals("")) {
                intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                pendingIntent = PendingIntent.getActivity(context,
                        0 /* Request code */, intent, PendingIntent.FLAG_UPDATE_CURRENT);
            } else if (group_id.equals(current_room)) { //same jid
                intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                pendingIntent = PendingIntent.getActivity(context, 0 /* Request code */, intent,
                        PendingIntent.FLAG_ONE_SHOT);
            } else {
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_TASK_ON_HOME);
                pendingIntent = PendingIntent.getActivity(context, 0 /* Request code */, intent,
                        PendingIntent.FLAG_ONE_SHOT);
            }

            titleSche1 = title;
            contentSche1 = content;
            NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(context)
                    .setSmallIcon(R.mipmap.push_small_logo)
                    .setLargeIcon(BitmapFactory.decodeResource(context.getResources(), R.mipmap.push_appt))
                    .setColor(context.getResources().getColor(R.color.primaryLogo))
                    .setContentTitle(title)
                    .setContentText(content)
                    .setAutoCancel(true)
                    .setSound(savedSoundUri)
                    .setVibrate(vibrate)
                    .setNumber(pushScheNotiCount)
                    .setGroup(group_id)
                    .setContentIntent(pendingIntent);

            NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.notify(2, notificationBuilder.build());

        } else { //if more than 1 new messages
            Intent intent;
            PendingIntent pendingIntent;
            NotificationCompat.Builder notificationBuilder;


//            Cursor cursor = db.rawQuery("select displayname from CONTACT_ROSTER where jid = '" + group_id + "'", null);
            Cursor cursor = rdb.selectQuery().push_go_grp_chat(group_id);
            if (cursor.moveToFirst()) { //grp exists in roster indiScheList
                title = cursor.getString(0);

                String displayName = getNameFromContactRoster(sender_jid);

                //change content to displayname: content if got member displayname
                if (displayName != null && !displayName.trim().equals("") && !displayName.equals("null")) {
                    title = title + " #" + displayName;
                }
            }
            cursor.close();

            if (group_id.equals(prevScheJID)) { //current JID same as previous
                intent = new Intent(context, GroupScheLog.class);

                intent.putExtra("remoteSche", "1");
                intent.putExtra("jid", group_id);
                intent.putExtra("apptID", apptID);

                if (current_room == null || current_room.equals("")) {
                    intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                    pendingIntent = PendingIntent.getActivity(context,
                            0 /* Request code */, intent, PendingIntent.FLAG_UPDATE_CURRENT);
                } else if (group_id.equals(current_room)) { //same jid
                    intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                    pendingIntent = PendingIntent.getActivity(context, 0 /* Request code */, intent,
                            PendingIntent.FLAG_ONE_SHOT);
                } else {
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_TASK_ON_HOME);
                    pendingIntent = PendingIntent.getActivity(context, 0 /* Request code */, intent,
                            PendingIntent.FLAG_ONE_SHOT);
                }

            } else {
                intent = new Intent(context, Home.class);
                pendingIntent = PendingIntent.getActivity(context, 0 /* Request code */, intent,
                        PendingIntent.FLAG_ONE_SHOT);
            }

            //outer msg (you have xx appointment updates)
            notificationBuilder = new NotificationCompat.Builder(context)
                    .setSmallIcon(R.mipmap.push_small_logo)
                    .setLargeIcon(BitmapFactory.decodeResource(context.getResources(), R.mipmap.push_appt))
                    .setColor(context.getResources().getColor(R.color.primaryLogo))
                    .setContentTitle("Soapp")
                    .setContentText("You have " + pushScheNotiCount + " appointment updates")
                    .setAutoCancel(true)
                    .setSound(savedSoundUri)
                    .setVibrate(vibrate)
                    .setNumber(pushScheNotiCount)
                    .setGroup(group_id)
                    .setContentIntent(pendingIntent);

            //inner msg
            NotificationCompat.InboxStyle inboxStyle = new NotificationCompat.InboxStyle(notificationBuilder);
            notificationBuilder.setStyle(inboxStyle);
            inboxStyle.setBigContentTitle("Soapp");

            switch (pushScheNotiCount) {
                case 2:
                    titleSche2 = title;
                    contentSche2 = content;
                    inboxStyle.addLine(titleSche1 + ": " + contentSche1);
                    inboxStyle.addLine(title + ": " + content);
                    break;

                case 3:
                    titleSche3 = title;
                    contentSche3 = content;
                    inboxStyle.addLine(titleSche1 + ": " + contentSche1);
                    inboxStyle.addLine(titleSche2 + ": " + contentSche2);
                    inboxStyle.addLine(title + ": " + content);
                    break;

                case 4:
                    titleSche4 = title;
                    contentSche4 = content;
                    inboxStyle.addLine(titleSche1 + ": " + contentSche1);
                    inboxStyle.addLine(titleSche2 + ": " + contentSche2);
                    inboxStyle.addLine(titleSche3 + ": " + contentSche3);
                    inboxStyle.addLine(title + ": " + content);
                    break;

                case 5:
                    titleSche5 = title;
                    contentSche5 = content;
                    inboxStyle.addLine(titleSche1 + ": " + contentSche1);
                    inboxStyle.addLine(titleSche2 + ": " + contentSche2);
                    inboxStyle.addLine(titleSche3 + ": " + contentSche3);
                    inboxStyle.addLine(titleSche4 + ": " + contentSche4);
                    inboxStyle.addLine(title + ": " + content);
                    break;

                //more than 5 msges
                default:
                    inboxStyle.addLine(titleSche2 + ": " + contentSche2);
                    inboxStyle.addLine(titleSche3 + ": " + contentSche3);
                    inboxStyle.addLine(titleSche4 + ": " + contentSche4);
                    inboxStyle.addLine(titleSche5 + ": " + contentSche5);
                    inboxStyle.addLine(title + ": " + content);
                    inboxStyle.addLine(" ");
                    inboxStyle.addLine("You have " + pushScheNotiCount + " appointment updates");

                    titleSche2 = titleSche3;
                    titleSche3 = titleSche4;
                    titleSche4 = titleSche5;
                    titleSche5 = title;

                    contentSche2 = contentSche3;
                    contentSche3 = contentSche4;
                    contentSche4 = contentSche5;
                    contentSche5 = content;
                    break;
            }

            NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.notify(2, notificationBuilder.build());
        }
    }

    //INDI ONLY push notification for booking (accepted/rejected by biz owner)
    public void push_go_res_booking(String sender_jid, String title, String content) {
        Uri savedSoundUri = Uri.parse("android.resource://com.soapp/" + R.raw.soapp_appt);
        long[] vibrate = new long[]{300, 300};

        pushBookingNotiCount++;

        if (pushBookingNotiCount == 1) { //if only 1 new message
            Intent intent;
            PendingIntent pendingIntent;

            //jid not required as of now
            String prevBookingJID = sender_jid;

            intent = new Intent(context, ResBookingsController.class);
            intent.putExtra("remoteBooking", "1");
//            intent.putExtra(MSG_JID, sender_jid);

            intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);

            pendingIntent = PendingIntent.getActivity(context,
                    0 /* Request code */, intent, PendingIntent.FLAG_UPDATE_CURRENT);

//            if (current_room == null || current_room.equals("")) {
//                intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
//                pendingIntent = PendingIntent.getActivity(context,
//                        0 /* Request code */, intent, PendingIntent.FLAG_UPDATE_CURRENT);
//            } else if (sender_jid.equals(current_room)) { //same jid
//                intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
//                pendingIntent = PendingIntent.getActivity(context, 0 /* Request code */, intent,
//                        PendingIntent.FLAG_ONE_SHOT);
//            } else {
//                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
//                pendingIntent = PendingIntent.getActivity(context, 0 /* Request code */, intent,
//                        PendingIntent.FLAG_ONE_SHOT);
//            }

            titleBooking1 = title;

            contentBooking1 = content;
            NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(Soapp.getInstance()
                    .getApplicationContext())
                    .setSmallIcon(R.mipmap.push_small_logo)
                    .setLargeIcon(BitmapFactory.decodeResource(context.getResources(), R.mipmap.push_booking))
                    .setColor(context.getResources().getColor(R.color.primaryLogo))
                    .setContentTitle(title)
                    .setContentText(content)
                    .setAutoCancel(true)
                    .setSound(savedSoundUri)
                    .setVibrate(vibrate)
                    .setNumber(pushBookingNotiCount)
                    .setGroup(sender_jid)
                    .setContentIntent(pendingIntent);

            NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.notify(3, notificationBuilder.build());

        } else { //if more than 1 new messages
            Intent intent;
            PendingIntent pendingIntent;
            NotificationCompat.Builder notificationBuilder;

//            if (sender_jid.equals(prevBookingJID)) { //current JID same as previous
            intent = new Intent(context, ResBookingsController.class);
            intent.putExtra("remoteBooking", "1");
//                intent.putExtra(MSG_JID, sender_jid);

            intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
            pendingIntent = PendingIntent.getActivity(context,
                    0 /* Request code */, intent, PendingIntent.FLAG_UPDATE_CURRENT);

//                if (current_room == null || current_room.equals("")) {
//                    intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
//                    pendingIntent = PendingIntent.getActivity(context,
//                            0 /* Request code */, intent, PendingIntent.FLAG_UPDATE_CURRENT);
//                } else if (sender_jid.equals(current_room)) { //same jid
//                    intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
//                    pendingIntent = PendingIntent.getActivity(context, 0 /* Request code */, intent,
//                            PendingIntent.FLAG_ONE_SHOT);
//                } else {
//                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
//                    pendingIntent = PendingIntent.getActivity(context, 0 /* Request code */, intent,
//                            PendingIntent.FLAG_ONE_SHOT);
//                }

//            } else { //not same JID, go to chattab in home
//                intent = new Intent(context, Home.class);
//                pendingIntent = PendingIntent.getActivity(context, 0 /* Request code */, intent,
//                        PendingIntent.FLAG_ONE_SHOT);
//            }

            //outer msg (you have xx appointment updates)
            notificationBuilder = new NotificationCompat.Builder(context)
                    .setSmallIcon(R.mipmap.push_small_logo)
                    .setLargeIcon(BitmapFactory.decodeResource(context.getResources(), R.mipmap.push_appt))
                    .setColor(context.getResources().getColor(R.color.primaryLogo))
                    .setContentTitle("Soapp")
                    .setContentText("You have " + pushBookingNotiCount + " restaurant booking updates")
                    .setAutoCancel(true)
                    .setSound(savedSoundUri)
                    .setVibrate(vibrate)
                    .setNumber(pushBookingNotiCount)
                    .setGroup(sender_jid)
                    .setContentIntent(pendingIntent);

            //inner msg
            NotificationCompat.InboxStyle inboxStyle = new NotificationCompat.InboxStyle(notificationBuilder);
            notificationBuilder.setStyle(inboxStyle);
            inboxStyle.setBigContentTitle("Soapp");

            switch (pushBookingNotiCount) {
                case 2:
                    titleBooking2 = title;
                    contentBooking2 = content;
                    inboxStyle.addLine(titleBooking1 + ": " + contentBooking1);
                    inboxStyle.addLine(title + ": " + content);
                    break;

                case 3:
                    titleBooking3 = title;
                    contentBooking3 = content;
                    inboxStyle.addLine(titleBooking1 + ": " + contentBooking1);
                    inboxStyle.addLine(titleBooking2 + ": " + contentBooking2);
                    inboxStyle.addLine(title + ": " + content);
                    break;

                case 4:
                    titleBooking4 = title;
                    contentBooking4 = content;
                    inboxStyle.addLine(titleBooking1 + ": " + contentBooking1);
                    inboxStyle.addLine(titleBooking2 + ": " + contentBooking2);
                    inboxStyle.addLine(titleBooking3 + ": " + contentBooking3);
                    inboxStyle.addLine(title + ": " + content);
                    break;

                case 5:
                    titleBooking5 = title;
                    contentBooking5 = content;
                    inboxStyle.addLine(titleBooking1 + ": " + contentBooking1);
                    inboxStyle.addLine(titleBooking2 + ": " + contentBooking2);
                    inboxStyle.addLine(titleBooking3 + ": " + contentBooking3);
                    inboxStyle.addLine(titleBooking4 + ": " + contentBooking4);
                    inboxStyle.addLine(title + ": " + content);
                    break;

                //more than 5 msges
                default:
                    inboxStyle.addLine(titleBooking2 + ": " + contentBooking2);
                    inboxStyle.addLine(titleBooking3 + ": " + contentBooking3);
                    inboxStyle.addLine(titleBooking4 + ": " + contentBooking4);
                    inboxStyle.addLine(titleBooking5 + ": " + contentBooking5);
                    inboxStyle.addLine(title + ": " + content);
                    inboxStyle.addLine(" ");
                    inboxStyle.addLine("You have " + pushBookingNotiCount + " restaurant booking updates");

                    titleBooking2 = titleBooking3;
                    titleBooking3 = titleBooking4;
                    titleBooking4 = titleBooking5;
                    titleBooking5 = title;

                    contentBooking2 = contentBooking3;
                    contentBooking3 = contentBooking4;
                    contentBooking4 = contentBooking5;
                    contentBooking5 = content;
                    break;
            }

            NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.notify(3, notificationBuilder.build());
        }
    }

    /*//////////////////////// [K07_01E] NOTIFICATION: Push-Related-END ////////////////////////*/


    /*//////////////////////// [K07_02] NOTIFICATION: Local-Related ////////////////////////*/

    //local notification for appointment reminder
    public int scheduleLocalNotification(final String apptID) {
        Appointment list = getAllApptDet(apptID);
        if (list.getApptTime() < System.currentTimeMillis() + TimeUnit.HOURS.toMillis(30)) {
            ApptWorkUUID apptWorkUUID = rdb.selectQuery().getApptWorkUUID(apptID);

            //if not going NO NEED reminder
            int count = 0;

            if (list.getSelf_Status() != 3) {
                long reminderTime = getApptReminderTime(apptID);
                count = setReminderUsingWorker(apptID, list.getApptTime(), reminderTime, apptWorkUUID != null ? apptWorkUUID.getReminderUUID() : null, list.getApptJid());

                exactTimeApptReminder(apptID, list.getApptTime(), apptWorkUUID != null ? apptWorkUUID.getExactUUID() : null, list.getApptJid());
            }

            setClearAfter3HWorker(apptID, list.getApptTime(), apptWorkUUID != null ? apptWorkUUID.getDeleteUUID() : null);

            return count;
        }
        return 0;
    }

    //change all appointment reminder when user changes alert time (15 mins before to 1 hour before)
//    public void updatePendingAlarms(int newReminderAlert, boolean needClearPendingAlarms) {
//        //first, get jid of appts which has date AND time set
//        Cursor cursor = rdb.selectQuery().getUpdatePendingAlarms();
//
//        if (cursor.moveToFirst()) { //got appt with date/time set
//            //set currentTime first before looping
//            long currentTime = System.currentTimeMillis();
//
//            do { //for each jid with time AND date
//                long apptTime = cursor.getLong(0);
//                String jid = cursor.getString(1);
//                String apptID = cursor.getString(2);
//                int selfStatus = cursor.getInt(3);
//
//                String displayName = getNameFromContactRoster(jid);
//
//                //clear pending alarms for reminder alert and clear appt details
//                if (needClearPendingAlarms) {
//                    cancelPendingAlarm(apptID);
//                }
//
//                //time of appointment + 2 hours (clear appt details)
//                long apptClearTime = apptTime + 7200000;
//                long next24hrs = currentTime + 86400000;
//
//                //if appointment already past 2 hours of currenttime, clear appt details
//                if (apptClearTime < currentTime) {
//                    deleteApptID(null, apptID, false, null, null, false);
//                } else if (apptClearTime < next24hrs) { //if not yet past,
//                    // set alarm for clearing appt details if within the next 24 hours
//                    setAlarmClearApptDet(apptID, apptClearTime);
//                }
//
//                //time of appt + reminder period in miliseconds
//                long apptReminderTime = apptTime - newReminderAlert * 60000;
//
//                //if self status is hosting or going only set reminder
//                if (selfStatus >= 1) {
//                    //if appt - reminder alert haven't past current time AND within the next 24
//                    // hours only set reminder
//                    if (apptReminderTime > currentTime && apptReminderTime < next24hrs) {
//                        setAlarmApptReminder(apptID, apptReminderTime, displayName);
//                    }
//                }
//            } while (cursor.moveToNext());
//        }
//        cursor.close();
//    }

    //build alarm manager based on jid and time
//    private void setAlarmClearApptDet(String apptID, long milisecondClearAppt) {
//        //set time to clear appt details based on time
//        int yearDateClear = Integer.parseInt(new SimpleDateFormat
//                ("yyyy", Locale.ENGLISH).format(milisecondClearAppt));
//        int monthDateClear = Integer.parseInt(new SimpleDateFormat
//                ("MM", Locale.ENGLISH).format(milisecondClearAppt));
//        int dayDateClear = Integer.parseInt(new SimpleDateFormat
//                ("dd", Locale.ENGLISH).format(milisecondClearAppt));
//        int hourTimeClear = Integer.parseInt(new SimpleDateFormat
//                ("HH", Locale.ENGLISH).format(milisecondClearAppt));
//        int minuteTimeClear = Integer.parseInt(new SimpleDateFormat
//                ("mm", Locale.ENGLISH).format(milisecondClearAppt));
//
//        Calendar calendarClear = Calendar.getInstance();
//        calendarClear.set(yearDateClear, monthDateClear - 1, dayDateClear, hourTimeClear, minuteTimeClear, 0);
//
//        //set intent for clear appt
//        int hashApptIDClear = getUniqueInteger(apptID + "clear");
//
//        Intent intentClearAppt = new Intent("com.soapp.localSche");
//        intentClearAppt.setClass(context, LocalBroadcastReceiver.class);
//        intentClearAppt.putExtra("apptIDClear", apptID);
//        intentClearAppt.putExtra("tag", "" + hashApptIDClear);
//
//        //set alarm using alarm manager
//        setAlarmManager(hashApptIDClear, intentClearAppt, calendarClear);
//    }

//    private void setAlarmApptReminder(String apptID, long milisecondApptReminder, String
//            displayName) {
//        //set time for appt reminders
//        int yearDate2 = Integer.parseInt(new SimpleDateFormat
//                ("yyyy", Locale.ENGLISH).format(milisecondApptReminder));
//        int monthDate2 = Integer.parseInt(new SimpleDateFormat
//                ("MM", Locale.ENGLISH).format(milisecondApptReminder));
//        int dayDate2 = Integer.parseInt(new SimpleDateFormat
//                ("dd", Locale.ENGLISH).format(milisecondApptReminder));
//        int hourTime2 = Integer.parseInt(new SimpleDateFormat("HH", Locale.ENGLISH).format(milisecondApptReminder));
//        int minuteTime2 = Integer.parseInt(new SimpleDateFormat
//                ("mm", Locale.ENGLISH).format(milisecondApptReminder));
//
//        Calendar calendar = Calendar.getInstance();
//        calendar.set(yearDate2, monthDate2 - 1, dayDate2, hourTime2, minuteTime2, 0);
//
//        //set intent for local appt reminder
//        int hashApptIDReminder = getUniqueInteger(apptID);
//
//        Intent intent = new Intent("com.soapp.localSche");
//        intent.setClass(context, LocalBroadcastReceiver.class);
//        intent.putExtra("displayname", displayName);
//        intent.putExtra("apptID", apptID);
//        intent.putExtra("tag", "" + hashApptIDReminder);
//
//        //set alarm using alarm manager
//        setAlarmManager(hashApptIDReminder, intent, calendar);
//    }

    //set alarm manager for all android versions
    private void setAlarmManager(int hashCode, Intent intent, Calendar calendar) {
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, hashCode, intent,
                PendingIntent.FLAG_UPDATE_CURRENT);

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        if (alarmManager != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) { //>= android 6 with doze
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
            } else { //< android 6, no doze
                alarmManager.setExact(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
            }
        }
    }


//    old setAlarm functions
//    //build alarm manager based on jid and time
//    private void setAlarmClearApptDet(String apptID, long milisecondClearAppt) {
//        //set intent for clear appt
//        int hashApptIDClear = getUniqueInteger(apptID + "clear");
//
//        Intent intentClearAppt = new Intent("com.soapp.localSche");
//        intentClearAppt.setClass(context, LocalBroadcastReceiver.class);
//        intentClearAppt.putExtra("apptID", apptID);
//        intentClearAppt.putExtra("tag", "" + hashApptIDClear);
//
//        PendingIntent pendingIntentClear = PendingIntent.getBroadcast(Soapp
//                .getInstance().getApplicationContext(), hashApptIDClear, intentClearAppt, PendingIntent.FLAG_UPDATE_CURRENT);
//
//        AlarmManager alarmManagerClear = (AlarmManager) Soapp.getInstance()
//                .getApplicationContext().getSystemService(Context.ALARM_SERVICE);
//
//        //set time to clear appt details based on time
//        int yearDateClear = Integer.parseInt(new SimpleDateFormat
//                ("yyyy", Locale.ENGLISH).format(milisecondClearAppt));
//        int monthDateClear = Integer.parseInt(new SimpleDateFormat
//                ("MM", Locale.ENGLISH).format(milisecondClearAppt));
//        int dayDateClear = Integer.parseInt(new SimpleDateFormat
//                ("dd", Locale.ENGLISH).format(milisecondClearAppt));
//        int hourTimeClear = Integer.parseInt(new SimpleDateFormat
//                ("HH", Locale.ENGLISH).format(milisecondClearAppt));
//        int minuteTimeClear = Integer.parseInt(new SimpleDateFormat
//                ("mm", Locale.ENGLISH).format(milisecondClearAppt));
//
//        Calendar calendarClear = Calendar.getInstance();
//        calendarClear.set(yearDateClear, monthDateClear - 1, dayDateClear, hourTimeClear,
//                minuteTimeClear, 0);
//
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//            alarmManagerClear.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, calendarClear.getTimeInMillis(),
//                    pendingIntentClear);
//        } else {
//            alarmManagerClear.setExact(AlarmManager.RTC_WAKEUP, calendarClear.getTimeInMillis(), pendingIntentClear);
//        }
//    }
//
//    private void setAlarmApptReminder(String apptID, long milisecondApptReminder, String
//            displayName) {
//        int yearDate2 = Integer.parseInt(new SimpleDateFormat
//                ("yyyy", Locale.ENGLISH).format(milisecondApptReminder));
//        int monthDate2 = Integer.parseInt(new SimpleDateFormat
//                ("MM", Locale.ENGLISH).format(milisecondApptReminder));
//        int dayDate2 = Integer.parseInt(new SimpleDateFormat
//                ("dd", Locale.ENGLISH).format(milisecondApptReminder));
//        int hourTime2 = Integer.parseInt(new SimpleDateFormat("HH", Locale.ENGLISH).format(milisecondApptReminder));
//        int minuteTime2 = Integer.parseInt(new SimpleDateFormat
//                ("mm", Locale.ENGLISH).format(milisecondApptReminder));
//
//        Calendar calendar = Calendar.getInstance();
//        calendar.set(yearDate2, monthDate2 - 1, dayDate2, hourTime2,
//                minuteTime2, 0);
//
//        //set intent for local appt reminder
//        int hashApptIDReminder = getUniqueInteger(apptID);
//
//        Intent intent = new Intent("com.soapp.localSche");
//        intent.setClass(context, LocalBroadcastReceiver.class);
//        intent.putExtra("displayname", displayName);
//        intent.putExtra("displayname", displayName);
//        intent.putExtra("tag", "" + hashApptIDReminder);
//
//        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, hashApptIDReminder, intent,
//                PendingIntent.FLAG_UPDATE_CURRENT);
//
//        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
//
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) { //above android 6 with doze
//            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
//        } else { //below android 6, no doze
//            alarmManager.setExact(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
//        }
//    }

    //hash jid into unique integer for pending intent id
    private int getUniqueInteger(String name) {
        int hash = name.hashCode();
        MessageDigest m;

        try {
            m = MessageDigest.getInstance("MD5");
            m.reset();
            m.update(name.getBytes());
            byte[] digest = m.digest();
            BigInteger bigInt = new BigInteger(1, digest);
            StringBuilder hashtext = new StringBuilder(bigInt.toString(10));
            // Now we need to zero pad it if you actually want the full 32 chars.
            while (hashtext.length() < 32) {
                hashtext.insert(0, "0");
            }
            int temp = 0;
            for (int i = 0; i < hashtext.length(); i++) {
                char c = hashtext.charAt(i);
                temp += (int) c;
            }
            return hash + temp;
        } catch (NoSuchAlgorithmException e) {
        }
        return hash;
    }

//get jid of all pending alarms
//    public List getSetAlarmsJid() {
//        List indiScheList = new ArrayList();
//        Context context = context;
//
//
//        Cursor cursor = db.rawQuery("select jid from ROSTER_LIST where appointmentdate IS NOT NULL AND appointmenttime IS NOT NULL", null);
//        if (cursor.moveToFirst()) { //got appt with date/time set
//            do {
//                String jid = cursor.getString(0);
//                int jidHash = getUniqueInteger(jid);
//
//                PendingIntent pendingIntent = PendingIntent.getBroadcast(context, jidHash,
//                        new Intent("com.soapp.localSche"), PendingIntent.FLAG_NO_CREATE);
//
//                if (pendingIntent != null) { //alarm for jid is set
//                    indiScheList.add(jid);
//                    String phoneName = getNameFromContactRoster(jid);
//                }
//            } while (cursor.moveToNext());
//            cursor.close();
//
//            return indiScheList;
//        } else {
//            cursor.close();
//
//            return null;
//        }
//    }

    //cancel existing alarm from alarm manager
    public void cancelPendingAlarm(String apptID) {

        ApptWorkUUID uuid = rdb.selectQuery().getApptWorkUUID(apptID);

        if (uuid != null) {
            WorkManager.getInstance().cancelWorkById(UUID.fromString(uuid.getReminderUUID()));
            WorkManager.getInstance().cancelWorkById(UUID.fromString(uuid.getExactUUID()));
            WorkManager.getInstance().cancelWorkById(UUID.fromString(uuid.getDeleteUUID()));
        }

//        Intent localIntent = new Intent("com.soapp.localSche");

//        //clear alarm for reminder alert
//        int apptIDHashReminder = getUniqueInteger(apptID);
//        PendingIntent oldPendingIntent = PendingIntent.getActivity(context, apptIDHashReminder,
//                localIntent, PendingIntent.FLAG_UPDATE_CURRENT);
//        oldPendingIntent.cancel();
//
//        AlarmManager alarmManagerOld = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
//        alarmManagerOld.cancel(oldPendingIntent);
//
//
//        //clear alarm for appt details clear
//        int apptIDHashClear = getUniqueInteger(apptID + "clear");
//        PendingIntent oldPendingIntent2 = PendingIntent.getActivity(context, apptIDHashClear,
//                localIntent, PendingIntent.FLAG_UPDATE_CURRENT);
//        oldPendingIntent.cancel();
//
//        AlarmManager alarmManagerOld2 = (AlarmManager) context.getSystemService(Context
//                .ALARM_SERVICE);
//        alarmManagerOld2.cancel(oldPendingIntent2);
    }

    /*//////////////////////// [K07_02E] NOTIFICATION: Local-Related-END ////////////////////////*/
    /*  =========================== [J07E - NOTIFICATION-RELATED-END] =========================== */



    /*  ============================== [J99 - OTHERS] ============================== */
    /*/////////////////// [K99_01 - Profile-Related] ////////////////////////*/

    //[DONE CR] incoming user profile updated (profile name and image)
    public void incomingUserProfileUpdate(String jid, String otherJid, String uniqueID) {
        String access_token = preferences.getValue(context, GlobalVariables.STRPREF_ACCESS_TOKEN);

        //build retrofit
        String size = miscHelper.getDeviceDensity(context);
        IndiAPIInterface indiAPIInterface = RetrofitAPIClient.getClient().create(IndiAPIInterface.class);
        retrofit2.Call<GetUserRepo> call = indiAPIInterface.getIndiProfile(jid, size, "Bearer " + access_token);

        call.enqueue(new retrofit2.Callback<GetUserRepo>() {
            @Override
            public void onResponse(retrofit2.Call<GetUserRepo> call, retrofit2.Response<GetUserRepo> response) {
                if (!response.isSuccessful()) {
                    new MiscHelper().retroLogUnsuc(response, "incomingUserProfileUpdate ", "JAY");
                    return;
                }

                final String name = String.valueOf(response.body().getName());
                final String imageURL = String.valueOf(response.body().getResource_url());
                final String image_data = String.valueOf(response.body().getImage_data());
                //first, update info to database where exists
                if (getContactRosterExist(jid) > 0) {
//                    ContentValues cvCR = new ContentValues();
//                    cvCR.put(CR_COLUMN_JID, jid);
//                    if (!image_data.equals("Empty")) {
//                        byte[] memImgByte = Base64.decode(image_data, Base64.DEFAULT);
//                        cvCR.put(CR_COLUMN_PROFILEPHOTO, memImgByte);
//                        cvCR.put(CR_COLUMN_PHOTOURL, imageURL);
//                    }
//                    cvCR.put(CR_COLUMN_DISPLAYNAME, name);
//
//                    updateRDB1Col(CR_TABLE_NAME, cvCR, CR_COLUMN_JID, jid);
                    ryanIndiSaveFullOnlyFromImgURL(jid, image_data, imageURL, name);
                }

                //acknowledge receiving profile change stanza
                final String user_id = preferences.getValue(context, GlobalVariables.STRPREF_USER_ID);

                if (SmackHelper.getXMPPConnection() != null && SmackHelper.getXMPPConnection().isAuthenticated()) {
                    groupChatStanza.GroupAckStanza(jid, user_id, otherJid, uniqueID);
                }
            }

            @Override
            public void onFailure(retrofit2.Call<GetUserRepo> call, Throwable t) {
                new MiscHelper().retroLogFailure(t, "incomingUserProfileUpdate", "JAY");

            }
        });
    }

    public void ryanIndiSaveFullOnlyFromImgURL(String jid, String image_data, String imageURL, String name) {
        DownloadFromUrlInterface client = RetrofitAPIClient.getClient().create(DownloadFromUrlInterface.class);
        retrofit2.Call<ResponseBody> call = client.downloadFileWithDynamicUrlAsync(imageURL);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (!response.isSuccessful()) {
                    updateIndiCrUrl("unsuccessful", jid, image_data, imageURL, name, null);
                    return;
                }
                try {
                    byte[] imageByte100 = response.body().bytes();
                    updateIndiCrUrl("successful", jid, image_data, imageURL, name, imageByte100);
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                updateIndiCrUrl("failed", jid, image_data, imageURL, name, null);
            }
        });
    }

    public void updateIndiCrUrl(String status, String jid, String image_data, String imageURL, String name, byte[] fullImage) {
        ContentValues cvCR = new ContentValues();
        cvCR.put(CR_COLUMN_JID, jid);
        //has thumbnail
        if (!image_data.equals("Empty")) {
            byte[] memImgByte = Base64.decode(image_data, Base64.DEFAULT);
            cvCR.put(CR_COLUMN_PROFILEPHOTO, memImgByte);
            if (status.equals("unsuccessful") || status.equals("failed")) {
                //update url column to null, so next time will use resource id to get url
                cvCR.putNull(CR_COLUMN_PHOTOURL);
                cvCR.putNull(CR_COLUMN_PROFILEFULL);
            } else {
                cvCR.put(CR_COLUMN_PHOTOURL, imageURL);
                cvCR.put(CR_COLUMN_PROFILEFULL, fullImage);
            }
        }
        cvCR.put(CR_COLUMN_DISPLAYNAME, name);

        updateRDB1Col(CR_TABLE_NAME, cvCR, CR_COLUMN_JID, jid);
    }
    /*/////////////////// [K99_01E - Profile-Related-END] ////////////////////////*/


    /*/////////////////// [K99_02 - Booking-Related] ////////////////////////*/
    /* ------------------ [K99_02_01] Outgoing-Related ------------------ */

    //outgoing res booking (indi only no group) - no booking code yet coz need wait biz owner accept
    public void outgoingResBooking(String bookingId,
                                   String resID, long bookingDate, long bookingTime,
                                   String pax, String promo, String resPropicURL, String resLat, String resLon, String
                                           resTitle, String resLoc, String resState, String resPhone, int
                                           bookingAttempt, String resJid, String mainCuisine, String overallRating) {

        String user_jid = preferences.getValue(context, GlobalVariables.STRPREF_USER_ID);

        ContentValues cvBook = new ContentValues();
        cvBook.put(BOOK_Status, 2);
        cvBook.put(BOOK_Date, bookingDate);
        cvBook.put(BOOK_Time, bookingTime);
        cvBook.put(BOOK_Pax, pax);
        cvBook.put(BOOK_Promo, promo);
        cvBook.put(BOOK_Attempts, bookingAttempt);
        cvBook.put(BOOK_QRCode, "");
        cvBook.put(BOOK_ResOwnerJid, resJid);
        cvBook.put(BOOK_ResId, resID);


        //check if booking already exists with self as booker
        if (getSelfResBookingExist(bookingId) == 0) { //not exist
            cvBook.put(BOOK_ID, bookingId);
            cvBook.put(BOOK_JID, user_jid);

            rdb.insertQuery().insertResBookings(Booking.fromContentValues(cvBook));
        } else { //exists, need to check if got share before
            //pubsub booking deleted to all shared users first
//            pubsubBookingDeleted(resID, user_jid);

            //then update ALL bookings for this restaurant including shared ones
            updateRDB2Col(BOOK_TABLE_NAME, cvBook, BOOK_ResId, BOOK_JID, resID, user_jid);
        }

        ContentValues cvRes = new ContentValues();
        cvRes.put(RES_LATITUDE, resLat);
        cvRes.put(RES_LONGITUDE, resLon);
        cvRes.put(RES_TITLE, resTitle);
        cvRes.put(RES_LOCATION, resLoc);
        cvRes.put(RES_STATE, resState);
        cvRes.put(RES_PHONENUM, resPhone);
        cvRes.put(RES_IMAGEURL, resPropicURL);
        cvRes.put(RES_MAINCUISINE, mainCuisine);
        cvRes.put(RES_OVERALLRATING, overallRating);
        if (getRestaurantExist(resID) == 0) {
            cvRes.put(RES_ID, resID);

            rdb.insertQuery().insertFavourites(Restaurant.fromContentValues(cvRes));
        } else {
            updateRDB1Col(RES_TABLE_NAME, cvRes, RES_ID, resID);
        }
    }

    //insert new row of restaurant to favourites
    public void insertCVToFav(ContentValues cv) {
        rdb.insertQuery().insertFavourites(Restaurant.fromContentValues(cv));
    }

    //update self's restaurant booking status
    public void updateSelfResBookingStatus(String resID, int bookingStatus) {
        ContentValues cvBook = new ContentValues();

        cvBook.put(BOOK_Status, bookingStatus);
        cvBook.put(BOOK_QRCode, "");

        if (getRestaurantExist(resID) > 0) {
            String user_jid = preferences.getValue(context, GlobalVariables.STRPREF_USER_ID);

            updateRDB2Col1isNull(BOOK_TABLE_NAME, cvBook, BOOK_ResId, BOOK_JID, BOOK_SharedJid, resID, user_jid, true);
        }
    }

    //cancel self's restaurant booking
    public void outgoingCancelSelfResBooking(String bookingid) {
        //update status of self's booking first

        ContentValues cvBook = new ContentValues();

        cvBook.put(BOOK_Status, -1);
        cvBook.put(BOOK_Date, 0);
        cvBook.put(BOOK_Time, 0);
        cvBook.put(BOOK_Pax, "1");
        cvBook.put(BOOK_Promo, "");
        cvBook.put(BOOK_Attempts, -1);
        cvBook.put(BOOK_QRCode, "");

        updateRDB1Col(BOOK_TABLE_NAME, cvBook, BOOK_ID, bookingid);

        String user_jid = preferences.getValue(context, GlobalVariables.STRPREF_USER_ID);

        //pubsub booking deleted to all shared users first
        pubsubBookingDeleted(bookingid, user_jid);


        //then delete ALL bookings because self cancelled booking
        deleteRDB1Col(BOOK_TABLE_NAME, BOOK_ID, bookingid);
    }

    //delete booking (for non-bookers, no pubsub)
    public void deleteBookingNonBooker(String resID, String jid) {
        //then delete ALL bookings because self cancelled booking
        deleteRDB2Col(BOOK_TABLE_NAME, BOOK_ResId, BOOK_SharedJid, resID, jid);
    }

    //get remaining booking attempts of an existing self's res booking
    public int getBookingAttempts(String bookingid) {

        String user_jid = preferences.getValue(context, GlobalVariables.STRPREF_USER_ID);

        return rdb.selectQuery().getBookingAttempts(bookingid, user_jid);
    }

    //outgoing restaurant booking qr code sharing (indi and grp, chat only)
    public void outgoingResBookingShare(String jid, String bookingQR, String uniqueID, long date,
                                        String resImgUrl, String resID, String resTitle, String
                                                resLat, String resLon, String bookingid) {
        try {
            //insert new row of res booking to RESTAURANT
            String user_jid = preferences.getValue(context, GlobalVariables.STRPREF_USER_ID);

            ContentValues cvbook = new ContentValues();
            cvbook.put(BOOK_SharedJid, jid);
            updateRDB1Col(BOOK_TABLE_NAME, cvbook, BOOK_ID, bookingid);

//insert share booking case to MESSAGE
            final int chatMarker = getOnlineStatus(jid);

            ContentValues cvMSG = new ContentValues();

            cvMSG.put(MSG_JID, jid);
            cvMSG.put(MSG_ISSENDER, 50);
            cvMSG.put(MSG_MSGDATA, "Booking for " + resTitle);
            cvMSG.put(MSG_MSGINFOURL, resImgUrl);
            cvMSG.put(MSG_MSGINFOID, bookingid);

            cvMSG.put(MSG_MSGLATITUDE, resLat);
            cvMSG.put(MSG_MSGLONGITUDE, resLon);
            cvMSG.put(MSG_MSGUNIQUEID, uniqueID);
            cvMSG.put(MSG_MSGDATE, date);
            cvMSG.put(MSG_CHATMARKER, chatMarker);
            cvMSG.put(MSG_MSGOFFLINE, 1);

            if (jid.length() > 12) { //grp
                cvMSG.put(MSG_SENDERJID, user_jid);
            }
            rdb.insertQuery().insertMessage(Message.fromContentValues(cvMSG));

            //insert shared booking display msg to ROSTER_LIST
            String allmessage = context.getString(R.string.booking_sent);

            if (getChatListExist(jid) > 0) {
                ContentValues cvCL = new ContentValues();

                cvCL.put(CL_COLUMN_DATE, date);
                cvCL.put(CL_COLUMN_LASTMSG, allmessage);

                if (jid.length() > 12) { //grp
                    cvCL.put(CL_COLUMN_LASTSENDERNAME, context.getString(R.string.you));
                }
                updateRDB1Col(CL_TABLE_NAME, cvCL, CL_COLUMN_JID, jid);

            } else { //not exist in roster, add new to roster
                if (jid.length() == 12) { //only do for indi
                    createNewIndiChat(jid, allmessage, date, false);
                }
            }
        } catch (Exception e) {
        }
    }

    //pubsub latest booking details to all users previously shared
    private void pubsubBookingAccepted(String bookingid, String resID, String user_jid, String bookingQR) {
        //get indiScheList of latest booking info from self's booking first
        List<ResMyBookingModel> resBookingList = getResBookingDetails(resID, null);

        Cursor cursor = rdb.selectQuery().pubsubBookingAccepted(resID, user_jid);

        if (cursor.moveToFirst()) {
            //set variables for self's booking info before loop shared jids
            String restitle = resBookingList.get(0).getResTitle();
            String resImgURL = resBookingList.get(0).getResImgURL();
            String resLat = resBookingList.get(0).getResLat();
            String resLon = resBookingList.get(0).getResLon();
            long bookingDate = resBookingList.get(0).getBookingDate();
            long bookingTime = resBookingList.get(0).getBookingTime();
            String pax = resBookingList.get(0).getPax();
            String promo = resBookingList.get(0).getPromo();

            DateFormat dateformat = new SimpleDateFormat("dd/MM/yy", Locale.ENGLISH);
            String bookingDateStr = dateformat.format(bookingDate);

            DateFormat timeformat = new SimpleDateFormat("h:mm a", Locale.ENGLISH);
            String bookingTimeStr = timeformat.format(bookingTime);

            String jid, uniqueID;
            String self_username = preferences.getValue(context, GlobalVariables.STRPREF_USERNAME);

            do {
                jid = cursor.getString(0);

                uniqueID = UUID.randomUUID().toString();

                //add res booking row to sqlite and msg sqlite
                outgoingResBookingShare(jid, bookingQR, uniqueID, System.currentTimeMillis(),
                        resImgURL, resID, restitle, resLat, resLon, bookingid);

                if (jid.length() == 12) { //indi
                    //pubsub new booking details to all shared jids (includes push as well)
                    singleChatStanza.SoappShareBookingStanza(bookingQR, jid, bookingDateStr,
                            bookingTimeStr, pax, resID, restitle, promo, self_username, uniqueID, bookingid);
                } else { //grp
                    String roomName = getNameFromContactRoster(jid);

                    groupChatStanza.GroupShareBooking(bookingid, jid, user_jid, self_username,
                            restitle, uniqueID, roomName, bookingDateStr, bookingTimeStr,
                            pax, resID, promo, bookingQR);
                }

            } while (cursor.moveToNext());
        }
        cursor.close();
    }

    //pubsub resBooking deleted to all shared users
    private void pubsubBookingDeleted(String bookingid, String user_jid) {
        //get indiScheList of jids where booking has been previously shared
        Cursor cursor = rdb.selectQuery().pubsubBookingDeleted(bookingid, user_jid);

        if (cursor.moveToFirst()) {
            String jid, restitle, roomName;
            String self_username = preferences.getValue(context, GlobalVariables.STRPREF_USERNAME);

            do {
                jid = cursor.getString(0);
                restitle = cursor.getString(1);

                //pubsub to all jids previously shared before about cancellation
                if (jid.length() == 12) { //indi
                    singleChatStanza.SoappDeleteShareBookingStanza(jid, bookingid, restitle,
                            self_username, UUID.randomUUID().toString());

                } else { //grp
                    roomName = getNameFromContactRoster(jid);
                    groupChatStanza.GroupDeleteShareBooking(bookingid, jid, user_jid, self_username,
                            UUID.randomUUID().toString(), roomName, restitle);

                }
            } while (cursor.moveToNext());
        }
        cursor.close();
    }

    /* ------------------ [K99_02_01E] Outgoing-Related-END ------------------ */

    /* ------------------ [K99_02_02] Incoming-Related ------------------ */
    public String getBookingCountBasedResId(String resID) {
        if (rdb.selectQuery().getBookingIdCountBasedOnResId(resID) == 1) {
            String bookingId = rdb.selectQuery().getBookingIdFromResId(resID);
            return bookingId;
        }
        return null;
    }

    //Incoming Booking Confirmed/rejected by biz owner
    public void incomingResBooking(String bookingid, String resID, String resJid, final String qrCode, String uniqueID) {


        ContentValues cvBook = new ContentValues();
        cvBook.put(BOOK_ResOwnerJid, resJid);

        final String bookingStatusStr;

        if (qrCode.equals("Declined")) { //biz owner declined
            cvBook.put(BOOK_Status, 3);
            cvBook.put(BOOK_QRCode, "");
        } else {
            cvBook.put(BOOK_Status, 1);
            cvBook.put(BOOK_QRCode, qrCode);
        }

        String user_jid = preferences.getValue(context, GlobalVariables.STRPREF_USER_ID);

        if (getSelfResBookingExist(bookingid) == 0) { //self booking not exist, just insert
            cvBook.put(BOOK_ResId, resID);
            cvBook.put(BOOK_JID, user_jid);
            cvBook.put(BOOK_ID, bookingid);

            rdb.insertQuery().insertResBookings(Booking.fromContentValues(cvBook));
        } else { //self booking already exists, update self's row and pubsub to other shared ones
            updateRDB2Col1isNull(BOOK_TABLE_NAME, cvBook, BOOK_ID, BOOK_JID, BOOK_SharedJid, bookingid,
                    user_jid, true);

            if (qrCode.equals("Declined")) { //biz owner declined
                //delete all rows of shared restaurants because owner DECLINED booking
                deleteRDB2Col1isNull(BOOK_TABLE_NAME, BOOK_ResId, BOOK_JID, BOOK_SharedJid, resID, user_jid, false);

            } else { //biz owner accepted
                //share full booking to previously shared users
                pubsubBookingAccepted(bookingid, resID, user_jid, qrCode);
            }

            singleChatStanza.SoappAckStanza(resJid, uniqueID);

            if (StateCheck.foreground) {
                //play in-app msg tone
                new SoundHelper().playRawSound(context, R.raw.soapp_appt);
            }
        }
    }
    
    //incoming friend share restaurant booking (indi and grp)
    public void incomingResBookingShare(final String bookingid,
                                        final String jid, final String sharedQRCode, final String uniqueID, final String
                                                bookingDateStr, final String bookingTimeStr, final String resID, final String
                                                resTitle, final String pax, final String promo, final boolean playSound,
                                        final long currentDate, final String otherJid) {
        RestaurantInfo model = new RestaurantInfo(resID);
        String access_token = preferences.getValue(context, GlobalVariables.STRPREF_ACCESS_TOKEN);

        //get long for booking date and time
        long bookingDateLong, bookingTimeLong;

        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yy", Locale.ENGLISH);
        try {
            bookingDateLong = dateFormat.parse(bookingDateStr).getTime();
        } catch (ParseException e) {
            bookingDateLong = 0;
        }

        DateFormat timeFormat = new SimpleDateFormat("h:mm a", Locale.ENGLISH);
        try {
            bookingTimeLong = timeFormat.parse(bookingTimeStr).getTime();
        } catch (ParseException e) {
            bookingTimeLong = 0;
        }

        ContentValues cvBook = new ContentValues();

        if (jid.length() > 12) { //if grp, use other jid
            cvBook.put(BOOK_JID, otherJid);
        } else { //indi, use jid
            cvBook.put(BOOK_JID, jid);
        }
        cvBook.put(BOOK_Status, 1);
        cvBook.put(BOOK_Date, Long.valueOf(bookingDateStr));
        cvBook.put(BOOK_Time, Long.valueOf(bookingTimeStr));
        cvBook.put(BOOK_Pax, pax);
        cvBook.put(BOOK_Promo, promo);
        cvBook.put(BOOK_ResId, resID);

        cvBook.put(BOOK_QRCode, sharedQRCode);

        //check if booking shared from jid already exists
        if (getSelfResBookingExist(bookingid) == 0) {
            cvBook.put(BOOK_ID, bookingid);
            cvBook.put(BOOK_SharedJid, jid);

            rdb.insertQuery().insertResBookings(Booking.fromContentValues(cvBook));
        } else {
            updateRDB2Col(BOOK_TABLE_NAME, cvBook, BOOK_ID, BOOK_SharedJid, bookingid, jid);
        }

        //build retrofit
        RestaurantDetails client = RetrofitAPIClient.getClient().create(RestaurantDetails.class);
        retrofit2.Call<RestaurantInfo> call = client.nearbyRes(model, "Bearer " + access_token);

        call.enqueue(new retrofit2.Callback<RestaurantInfo>() {
            @Override
            public void onResponse(retrofit2.Call<RestaurantInfo> call, retrofit2.Response<RestaurantInfo> response) {
                if (!response.isSuccessful()) {

                    new MiscHelper().retroLogUnsuc(response, "incomingResBookingShare ", "JAY");

                    return;
                }
                String allmessage = context.getString(R.string
                        .booking_confirmed) + ": " + resTitle;
                String image_id = response.body().getPropic();
                String resLat = response.body().getLatitude();
                String resLon = response.body().getLongitude();

                try {
                    //update MESSAGE sqlite
                    ContentValues cvMSG = new ContentValues();

                    cvMSG.put(MSG_JID, jid);
                    cvMSG.put(MSG_ISSENDER, 51);
                    cvMSG.put(MSG_MSGDATA, sharedQRCode);
                    cvMSG.put(MSG_MSGINFOURL, image_id);
                    cvMSG.put(MSG_MSGINFOID, bookingid);
//                    cv.put(MSG_, resTitle);
                    cvMSG.put(MSG_MSGLATITUDE, resLat);
                    cvMSG.put(MSG_MSGLONGITUDE, resLon);
                    cvMSG.put(MSG_MSGUNIQUEID, uniqueID);
                    cvMSG.put(MSG_MSGDATE, currentDate);


                    if (jid.length() > 12) { //grp
                        cvMSG.put(MSG_SENDERJID, otherJid);
                        String otherRecName = getNameFromContactRoster(otherJid);
                    }
                    rdb.insertQuery().insertMessage(Message.fromContentValues(cvMSG));

                    addChatBadgeUnread(jid, currentDate);


                    //update RESTAURANT sqlite
                    String resLoc = response.body().getLocation();
                    String resState = response.body().getState();
                    String resPhone = response.body().getPhone1();


                    ContentValues cvRes = new ContentValues();

                    cvRes.put(RES_IMAGEURL, image_id);
                    cvRes.put(RES_LATITUDE, resLat);
                    cvRes.put(RES_LONGITUDE, resLon);
                    cvRes.put(RES_TITLE, resTitle);
                    cvRes.put(RES_LOCATION, resLoc);
                    cvRes.put(RES_STATE, resState);
                    cvRes.put(RES_PHONENUM, resPhone);


                    if (getRestaurantExist(resID) == 0) {
                        cvRes.put(RES_ID, resID);

                        rdb.insertQuery().insertFavourites(Restaurant.fromContentValues(cvRes));
                    } else {
                        updateRDB1Col(RES_TABLE_NAME, cvRes, RES_ID, resID);
                    }


                    //update chattab
                    if (getChatListExist(jid) > 0) {
                        ContentValues cvCL = new ContentValues();

                        cvCL.put(CL_COLUMN_DATE, currentDate);
                        cvCL.put(CL_COLUMN_LASTMSG, allmessage);

                        if (jid.length() > 12) { //grp
                            String otherRecName = getNameFromContactRoster(otherJid);
                            cvCL.put(CL_COLUMN_LASTSENDERNAME, otherRecName);
                        }
                        updateRDB1Col(CL_TABLE_NAME, cvCL, CL_COLUMN_JID, jid);

                        if (jid.length() == 12) { //single ack
                            singleChatStanza.SoappAckStanza(jid, uniqueID);
                        } else { //grp ack
                            String user_jid = preferences.getValue(context, GlobalVariables.STRPREF_USER_ID);
                            groupChatStanza.GroupAckStanza(jid, user_jid, otherJid, uniqueID);
                        }

                        playInMsgSoundUI(playSound);
                    } else { //not exist in roster, add new to roster
                        if (jid.length() == 12) { //only do for indi
                            createNewIndiChat(jid, allmessage, currentDate, playSound);
                        }
                    }
                } catch (Exception e) {
                }
            }

            @Override
            public void onFailure(retrofit2.Call<RestaurantInfo> call, Throwable t) {
                new MiscHelper().retroLogFailure(t, "incomingResBookingShare", "JAY");
                Toast.makeText(context, R.string.onfailure, Toast
                        .LENGTH_SHORT).show();
            }
        });
    }

    //when previously incoming shared res booking gets cancelled (DeleteShareBookingID)
    public void incomingResBookingCancelled(String jid, String resID, String uniqueID, String
            otherJid, final boolean playSound) {
        String user_jid = preferences.getValue(context, GlobalVariables.STRPREF_USER_ID);

        //update MESSAGE sqlite to inform user of booking cancellation
        ContentValues cvMSG = new ContentValues();

        String allmessage;
        long currentDate = System.currentTimeMillis();

        if (jid.length() > 12) { //grp
            allmessage = context.getString(R.string.booking_cancelled) + " by "
                    + getNameFromContactRoster(otherJid) + ": " + getResTitle(resID);

            cvMSG.put(MSG_SENDERJID, otherJid);
        } else {
            allmessage = context.getString(R.string.booking_cancelled) + ": " + getResTitle(resID);
        }

        cvMSG.put(MSG_ISSENDER, 53);
        cvMSG.put(MSG_JID, jid);
        cvMSG.put(MSG_MSGDATA, allmessage);
        cvMSG.put(MSG_MSGUNIQUEID, uniqueID);
        cvMSG.put(MSG_MSGDATE, currentDate);

        rdb.insertQuery().insertMessage(Message.fromContentValues(cvMSG));

        //update chattab
        if (getChatListExist(jid) > 0) {
            ContentValues cvCL = new ContentValues();

            cvCL.put(CL_COLUMN_DATE, currentDate);
            cvCL.put(CL_COLUMN_LASTMSG, allmessage);

            if (jid.length() > 12) { //grp
                String otherRecName = getNameFromContactRoster(otherJid);
                cvCL.put(CL_COLUMN_LASTSENDERNAME, otherRecName);
            }
            updateRDB1Col(CL_TABLE_NAME, cvCL, CL_COLUMN_JID, jid);

            //acknowledge stanza
            if (jid.length() == 12) { //single ack
                singleChatStanza.SoappAckStanza(jid, uniqueID);
            } else { //grp ack
                groupChatStanza.GroupAckStanza(jid, user_jid, otherJid, uniqueID);
            }

            playInMsgSoundUI(playSound);
        } else { //not exist in roster, add new to roster
            if (jid.length() == 12) { //only do for indi
                createNewIndiChat(jid, allmessage, currentDate, playSound);
            }
        }

        //delete booking which is cancelled by friend, which is NOT booked by self
        rdb.runInTransaction(() -> sqLiteDatabase.delete(BOOK_TABLE_NAME, String.format("%s = ? AND %s != ? AND %s = ?",
                BOOK_ResId, BOOK_JID, BOOK_SharedJid), new String[]{resID, user_jid, jid}));
    }

    /* ------------------ [K99_02_02E] Incoming-Related-END ------------------ */


    /* ------------------ [K99_02_03] Get-Info-Related ------------------ */
//get indiScheList of booking info based on resID
    public List<ResMyBookingModel> getResBookingDetails(String resID, String sharedJid) {
        List<ResMyBookingModel> list = new ArrayList<>();

        Cursor cursor;
        if (sharedJid != null) { //got shared jid, query using combination
//            cursor = db.rawQuery("select booker, bookingcode, resjid, pax, promo, " +
//                    "restaurantimage, restaurantlat, restaurantlon, restaurantlocation, " +
//                    "restauranttitle, restaurantstate, booking, fav, date, time, resphone from " +
//                    "RESTAURANT where restaurantid = '" + resID + "' AND SharedJid = '" + sharedJid +
//                    "'", null);
            cursor = rdb.selectQuery().getResBookingDetails1(resID);
        } else { //no shared jid, just query sharedJid = null
//            cursor = db.rawQuery("select booker, bookingcode, resjid, pax, promo, " +
//                    "restaurantimage, restaurantlat, restaurantlon, restaurantlocation, " +
//                    "restauranttitle, restaurantstate, booking, fav, date, time, resphone from " +
//                    "RESTAURANT where restaurantid = '" + resID + "' AND SharedJid IS NULL", null);
            cursor = rdb.selectQuery().getResBookingDetails1(resID);
        }

        if (cursor.moveToFirst()) {
            String bookerJID = cursor.getString(0);
            String bookingQRCode = cursor.getString(1);
            String resJID = cursor.getString(2);
            String pax = cursor.getString(3);
            String promo = cursor.getString(4);
            String resPropicURL = cursor.getString(5);
            String resLat = cursor.getString(6);
            String resLon = cursor.getString(7);
            String resLoc = cursor.getString(8);
            String resTitle = cursor.getString(9);
            String resState = cursor.getString(10);
            int bookingStatus = cursor.getInt(11);
            int fav = cursor.getInt(12);
            long bookingDate = cursor.getLong(13);
            long bookingTime = cursor.getLong(14);
            String resPhone = cursor.getString(15);

            list.add(new ResMyBookingModel(bookerJID, bookingQRCode, resJID, pax, promo,
                    resPropicURL, resLat, resLon, resLoc, resTitle, resState, bookingStatus, fav,
                    bookingDate, bookingTime, resPhone));
        }

        cursor.close();

        return list;
    }

    //get restaurant title based on resID
    public String getResTitle(String resID) {


//        Cursor cursor = db.rawQuery("select restauranttitle from RESTAURANT where restaurantid = '" + resID
//                + "'", null);
        Cursor cursor = rdb.selectQuery().getResTitle(resID);
        if (cursor.moveToFirst()) {
            String resTitle = cursor.getString(0);

            cursor.close();

            return resTitle;
        }
        return null;
    }

    /* ------------------ [K99_02_03E] Get-Info-Related-END ------------------ */
    /*/////////////////// [K99_02E - Booking-Related-END] ////////////////////////*/


    /*/////////////////// [K99_03 - Badge-Related] ////////////////////////*/

    //Increase Badge For Incoming Messages and unread indicator
    private void addChatBadgeUnread(String jid, long date) {
        String seeingCurrentRoom = preferences.getValue(context, "seeing_current_chat");
        if (!seeingCurrentRoom.equals(jid)) { //only add badge if not seeing current room
            Cursor cursor = rdb.selectQuery().getBadgeFromRosterChat(jid);

            if (cursor.moveToFirst()) {
                int badge = cursor.getInt(0);

                ContentValues cvCL = new ContentValues();

                cvCL.put(CL_COLUMN_NOTIBADGE, badge + 1);

                updateRDB1Col(CL_TABLE_NAME, cvCL, CL_COLUMN_JID, jid);
            }
            cursor.close();

            Cursor cursor2 = rdb.selectQuery().addChatBadgeUnread(jid);

            if (cursor2.getCount() == 0) { //only add unread if not exist
                ContentValues cvMSG = new ContentValues();

                cvMSG.put(MSG_JID, jid);
                cvMSG.put(MSG_ISSENDER, 1);
                cvMSG.put(MSG_MSGDATE, date - 1);

                rdb.insertQuery().insertMessage(Message.fromContentValues(cvMSG));
            }
            cursor2.close();
        }
    }

    //clear noti badge for a room
    public void zeroBadgeChatRoom(String jid, int isGrp) {
        //clear push noti
        NotificationManagerCompat.from(context).cancel("chat", jid.hashCode());
        deleteNotiFromDB(jid, isGrp, false);

        if (getChatNotiCount(jid) > 0) { //only clear if got count to begin with
            ContentValues cvCL = new ContentValues();

            cvCL.put(CL_COLUMN_NOTIBADGE, 0);

            updateRDB1Col(CL_TABLE_NAME, cvCL, CL_COLUMN_JID, jid);

            //TO BE MOVED TO LIVEDATA SOON
            //update home badge as well
            try {
                ShortcutBadger.applyCountOrThrow(context, getTotalBadgeCountChatSche());
            } catch (ShortcutBadgeException ignore) {
            }
        }
    }

    //clear appt noti badge of an apptID
    public void zeroBadgeApptRoom(String apptID, int isGrp) {
        if (apptID != null) {
            //clear push noti
            NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            if (notificationManager != null) {
                notificationManager.cancel("appt", apptID.hashCode());
            }
            deleteNotiFromDB(apptID, isGrp, true);

            if (getApptNotiBadge1Appt(apptID) > 0) { //only do action if noti is not 0
                ContentValues cvAppt = new ContentValues();

                cvAppt.put(A_COLUMN_APPT_NOTI_BADGE, 0);

                //clear all other noti badge
                cvAppt.put(A_COLUMN_APPT_NOTI_TITLE, 0);
                cvAppt.put(A_COLUMN_APPT_NOTI_DATE, 0);
                cvAppt.put(A_COLUMN_APPT_NOTI_LOC, 0);
                cvAppt.put(A_COLUMN_APPT_NOTI_DESC, 0);

                updateRDB1Col(A_TABLE_NAME, cvAppt, A_COLUMN_ID, apptID);

                //TO BE MOVED TO LIVEDATA SOON
                //update home badge as well
                try {
                    ShortcutBadger.applyCountOrThrow(context, getTotalBadgeCountChatSche());
                } catch (ShortcutBadgeException ignore) {
                }
            }
        }
    }

    //clear appt noti badge of ALL appts
    public void zeroBadgeApptALL() {
//        ContentValues cvAppt = new ContentValues();
//
//        cvAppt.put(A_COLUMN_APPT_NOTI_BADGE, 0);
//        cvAppt.put(A_COLUMN_APPT_NOTI_TITLE, 0);
//
//        String tableName = CR_TABLE_NAME;
//        String colName1 = CR_COLUMN_JID;
//
//        rdb.runInTransaction((Runnable) () -> sqLiteDatabase.update(tableName, SQLiteDatabase.CONFLICT_IGNORE,
//                cvAppt, String.format("%s IS NOT NULL", colName1), new String[]{})
//        );

//        updateRDB1Col(A_TABLE_NAME, cvAppt, A_COLUMN_ID, apptID);
    }

    /*/////////////////// [K99_03E - Batch-Related-END] ////////////////////////*/


    /*/////////////////// [K99_05 - Tracking-Related] ////////////////////////*/

    //update incoming grp tracking stanza to sqlite
    public void GroupTrackingUpdate(final String node, final String jid, final String userlat,
                                    final String userlon, final String ID) {
        if (getMemberExist(node, jid) > 0) {
            ContentValues cv = new ContentValues();

            cv.put(GM_COLUMN_LATITUDE, userlat);
            cv.put(GM_COLUMN_LONGITUDE, userlon);
            cv.put(GM_COLUMN_ROOMJID, node);
            cv.put(GM_COLUMN_MEMBERJID, jid);

            updateRDB2Col(GM_TABLE_NAME, cv, GM_COLUMN_ROOMJID, GM_COLUMN_MEMBERJID, node, jid);
        }
    }

    //update incoming indi tracking stanza to sqlite
    public void TrackingUpdate(final String jid, final String userlat, final String userlon,
                               final String ID) {
        if (getApptExist(jid) > 0) {
            ContentValues cv = new ContentValues();

            cv.put(A_COLUMN_LATITUDE, userlat);
            cv.put(A_COLUMN_LONGITUDE, userlon);
            cv.put(A_COLUMN_JID, jid);

            updateRDB1Col(A_TABLE_NAME, cv, A_COLUMN_JID, jid);
        }
    }

    //get indi location of friend from sqlite and populate to MapTracking
    public List<TrackingModel> getlocationsingle(String jid, String displayname) {
        List<TrackingModel> list = new ArrayList<>();

//
//        Cursor cursor = db.rawQuery("select userlat, userlong from APPOINTMENT where jid = '" + jid + "'", null);
        Cursor cursor = rdb.selectQuery().getlocationsingle(jid);
        if (cursor.moveToFirst()) {
            String userlat = cursor.getString(0);
            String userlong = cursor.getString(1);

            if (userlat != null && !userlat.equals("")) {
                list.add(new TrackingModel(displayname, userlat, userlong));

            }
        }
        cursor.close();
        return list;
    }

    //get grp location of friends from sqlite and populate to MapTracking
    public List<TrackingModel> getlocationgroup(String room_jid) {
        List<TrackingModel> list = new ArrayList<>();

//
//        Cursor cursor = db.rawQuery("select CONTACT_ROSTER.phonename, CONTACT_ROSTER" +
//                ".displayname, CONTACT_ROSTER.phonenumber, GROUPMEM.latitude, " +
//                "GROUPMEM.longtitude from GROUPMEM INNER JOIN CONTACT_ROSTER on GROUPMEM.jid = CONTACT_ROSTER.jid where " +
//                "GROUPMEM.roomjid = '" + room_jid + "'", null);
        Cursor cursor = rdb.selectQuery().getlocationgroup(room_jid);
        if (cursor.moveToFirst()) {
            String phoneName;
            do {
                phoneName = cursor.getString(0);

                if (phoneName == null || phoneName.equals("")) {
                    phoneName = cursor.getString(1) + " " + cursor.getString(2);
                }
                String userlat = cursor.getString(3);
                String userlong = cursor.getString(4);

                if (userlat != null && !userlat.equals("")) {
                    list.add(new TrackingModel(phoneName, userlat, userlong));

                }
                //change end
            } while (cursor.moveToNext());
        }
        cursor.close();
        return list;
    }

    /*/////////////////// [K99_05E - Tracking-Related-END] ////////////////////////*/


    /*/////////////////// [K99_06 - Restaurant-Related] ////////////////////////*/

    //check if restaurant is favourited
    public int checkFavRestaurant(String restaurantId) {
//
//        Cursor cursor = db.rawQuery("select _id from RESTAURANT where restaurantid = '" + restaurantId + "' AND fav = " + 1,
//                null);
        Cursor cursor = rdb.selectQuery().checkFavRestaurant(restaurantId);
        if (cursor != null) {
            int count = cursor.getCount();
            if (count > 0) {
                cursor.close();

                return count;
            } else {
                cursor.close();

                return 0;
            }
        } else {
            cursor.close();

            return 0;
        }
    }

    //get restaurant rating values from sqlite
    public List<ResRatingModel> getResRating(String resID) {
        List<ResRatingModel> list = new ArrayList<>();

//
//
//        Cursor cursor = db.rawQuery("select rating1, rating2 from RESTAURANT where restaurantid" +
//                " = '" + resID + "'", null);
        Cursor cursor = rdb.selectQuery().getResRating(resID);
        if (cursor.moveToFirst()) {
            list.add(new ResRatingModel(cursor.getInt(0), cursor.getInt(1)));
        }
        cursor.close();

        return list;
    }

    /*/////////////////// [K99_06E - Restaurant-Related-END] ////////////////////////*/


    /*/////////////////// [K99_99 - SPECIAL] ////////////////////////*/
//[haven't implemented] special function to download new image thumbnails
    public void SPnewImages(String dpi) {


//        Cursor cursor = rdb.rawQuery("select DISTINCT jid from CONTACT_ROSTER", null);
        Cursor cursor = rdb.selectQuery().SPnewImages();
        if (cursor.moveToFirst()) {
            StringBuilder listString = new StringBuilder();
            do {
                listString.append(cursor.getString(0)).append(",");
            } while (cursor.moveToNext());

            //create parts for single strings
            RequestBody size = RequestBody.create(MediaType.parse("text/plain"), dpi);
            RequestBody jidListString = RequestBody.create(MediaType.parse("text/plain"),
                    listString.toString());
            //post retrofit with access token as header
            String access_token = preferences.getValue(context, GlobalVariables
                    .STRPREF_ACCESS_TOKEN);

            //build retrofit
            GetProfileThumb client = RetrofitAPIClient.getClient().create(GetProfileThumb.class);
            retrofit2.Call<List<SPgetAllResModel>> call2 = client.spGetAllRes("Bearer " + access_token, jidListString, size);
            call2.enqueue(new retrofit2.Callback<List<SPgetAllResModel>>() {
                @Override
                public void onResponse(retrofit2.Call<List<SPgetAllResModel>> call, final retrofit2.Response<List<SPgetAllResModel>> response) {
                    if (!response.isSuccessful()) {

                        new MiscHelper().retroLogUnsuc(response, "SPnewImages ", "JAY");

                        return;
                    }

                    List<SPgetAllResModel> spList = response.body();

                    String jid;
                    byte[] imageBytes;

                    //response: room_id, resource_url
                    for (int i = 0; i < spList.size(); i++) {
                        jid = spList.get(i).getId();

                        imageBytes = Base64.decode(spList.get(i).getImage_data(), Base64.DEFAULT);

                        ContentValues cvCR = new ContentValues();
                        cvCR.put(CR_COLUMN_JID, jid);
                        cvCR.put(CR_COLUMN_PROFILEPHOTO, imageBytes);

                        updateRDB1Col(CR_TABLE_NAME, cvCR, CR_COLUMN_JID, jid);
                    }

                    preferences.save(context, "getNewImg3", "1");

                }

                @Override
                public void onFailure(retrofit2.Call<List<SPgetAllResModel>> call, Throwable t) {
                    new MiscHelper().retroLogFailure(t, "SPnewImages", "JAY");

                }
            });
        }
        cursor.close();
    }

    //call this when the service died, which mean compressing half way so make it continue
    public String[] getOneCompressingVideo() {
        String media = "";
        String resourceid = "";
        String id = "";
        String jidstr = "";
        String issender = "";
        int rowId = -1;
//
//        Cursor cursor = db.rawQuery("select media, resourceid , id, jidstr, issender from MESSAGE where status = -7 order by _id ASC", null);
        Cursor cursor = rdb.selectQuery().getOneCompressingVideo();
        if (cursor.moveToFirst()) {
            media = cursor.getString(0);
            resourceid = cursor.getString(1);
            id = cursor.getString(2);
            jidstr = cursor.getString(3);
            issender = cursor.getString(4);
            rowId = cursor.getInt(5);
        }
        cursor.close();
        return new String[]{media, resourceid, id, jidstr, issender, "" + rowId};
    }

    public String[] getOneQueueingVideo() {
        String media = "";
        String resourceid = "";
        String id = "";
        String jidstr = "";
        String issender = "";
        String display_name = "";
        int rowId = -1;
//
//        Cursor cursor = db.rawQuery("select media, resourceid , id, jidstr, issender from MESSAGE where status = 4 order by _id ASC", null);
        Cursor cursor = rdb.selectQuery().getOneQueueingVideo();
        if (cursor.moveToFirst()) {
            media = cursor.getString(0);
            resourceid = cursor.getString(1);
            id = cursor.getString(2);
            jidstr = cursor.getString(3);
            issender = cursor.getString(4);
            rowId = cursor.getInt(5);
            display_name = cursor.getString(6);
        }
        cursor.close();
        return new String[]{media, resourceid, id, jidstr, issender, String.valueOf(rowId), display_name};
    }

    public boolean checkForIsVideoCompressing() {
        //compressing is -7
//
//        Cursor cursor = db.rawQuery("select media, resourceid , id, jidstr, issender from MESSAGE where status = -7", null);
        Cursor cursor = rdb.selectQuery().checkForIsVideoCompressing();
        return cursor.moveToFirst();
    }

    public boolean checkExistingOfUmcompressedVideo() {
        //maybe make 4 to in queue ?
//
//        Cursor cursor = db.rawQuery("select media, resourceid , id, jidstr, issender from MESSAGE where status = 4", null);
        Cursor cursor = rdb.selectQuery().checkExistingOfUmcompressedVideo();
        return cursor.moveToFirst();
    }

    /*/////////////////// [K99_99E - SPECIAL-END] ////////////////////////*/

    /*  ============================= [J99E - OTHERS-END] ============================= */

    public boolean checkIfNeedDownload(String mediatype) {
        switch (mediatype) {
            case "audio":
                final String audioData = preferences.getValue(context, "AData");
                final String audioWifi = preferences.getValue(context, "AWifi");
                if (CheckInternetAvaibility.gotInternet) {
                    //if wifi
                    if (CheckInternetAvaibility.networktype == 1) {
                        return audioWifi.equals("on");
                    }
                    //if data
                    else {
                        return audioData.equals("on");
                    }
                } else {
                    return false;
                }

            case "video":
                final String videoData = preferences.getValue(context, "VData");
                final String videoWifi = preferences.getValue(context, "VWifi");
                if (CheckInternetAvaibility.gotInternet) {
                    //if wifi
                    if (CheckInternetAvaibility.networktype == 1) {
                        return videoWifi.equals("on");
                    }
                    //if data
                    else {
                        return videoData.equals("on");
                    }
                } else {
                    return false;
                }

            case "image":
                final String imageData = preferences.getValue(context, "PData");
                final String imageWifi = preferences.getValue(context, "PWifi");
                if (CheckInternetAvaibility.gotInternet) {
                    //if wifi
                    if (CheckInternetAvaibility.networktype == 1) {
                        return imageWifi.equals("on");
                    }
                    //if data
                    else {
                        return imageData.equals("on");
                    }
                } else {
                    return false;
                }

            default:
                return false;
        }
    }

    //downdload image -- end --

    /**
     * for error logging
     */
    public void saveLogsToDb(String errTitle, String errDesc, long createdAt) {

        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String timestamp = formatter.format(new Date(createdAt));

        if (errTitle.contains("SMACK") && CheckInternetAvaibility.gotInternet) {
            rdb.insertQuery().insertErrorLog(new ErrorLogs(errTitle, errDesc, timestamp));
        }
    }

    public void deleteLogsFromDB() {
        sqLiteDatabase.delete(EL_TABLE_NAME, null, null);

    }

    //reward related
//    reward_id, resource_url, title, description, date_end, restaurant_id, redemption_id
    public void rewardInputDatabase(String reward_id, String resource_url, String title, String description, String date_end,
                                    String restaurant_id, String redemption_id) {

        ContentValues cvReward = new ContentValues();
        cvReward.put(REWARD_ID, reward_id);
        cvReward.put(REWARD_IMG_URL, resource_url);
        cvReward.put(REWARD_TITLE, title);
        cvReward.put(REWARD_DESCRIPTION, description);
        cvReward.put(REWARD_RESTAURANT_ID, restaurant_id);
        cvReward.put(REWARD_REDEMPTION_ID, redemption_id);

        rdb.insertQuery().insertReward(Reward.fromContentValues(cvReward));
    }

    //delete default image in cr
    public void SPdeleteDefaultImage() {
        //sizes for default image on front end (in bytes)
        Integer[] defaultImageSizesFull = new Integer[]{40289, 8493, 4541, 60621, 42508, 7571, 8564};
        Integer[] defaultImageSizesThumb = new Integer[]{823};

        ArrayList<Integer> defaultImageSizesFullList = new ArrayList<>(Arrays.asList(defaultImageSizesFull));
        ArrayList<Integer> defaultImageSizesThumbList = new ArrayList<>(Arrays.asList(defaultImageSizesThumb));

        byte[] profileThumb, profileFull;

        //start loop
        Cursor cursor = rdb.selectQuery().SPdelAllPropic();

        if (cursor != null && cursor.moveToFirst()) {
            do {
                profileThumb = cursor.getBlob(0);
                profileFull = cursor.getBlob(1);

                ContentValues cv = new ContentValues();
                if ((profileThumb != null && profileThumb.length < 10) ||
                        profileFull != null && profileFull.length < 10) {

                    cv.putNull(DatabaseHelper.CR_COLUMN_PROFILEPHOTO);
                    cv.putNull(DatabaseHelper.CR_COLUMN_PHOTOURL);

                }
                cv.putNull(DatabaseHelper.CR_COLUMN_PROFILEFULL);

                updateRDB1Col(DatabaseHelper.CR_TABLE_NAME, cv, DatabaseHelper.CR_COLUMN_JID, cursor.getString(2));

//                }
            } while (cursor.moveToNext());
        }
    }

    //clear outgoing video media status (-7 to 7)
    public void SPClearOutVid() {
        ContentValues contentValues = new ContentValues();

        contentValues.put(MSG_MEDIASTATUS, 7);

        rdb.runInTransaction((Runnable) () -> sqLiteDatabase.update(MSG_TABLE_NAME, SQLiteDatabase.CONFLICT_IGNORE,
                contentValues, String.format("%s = 24 AND %s = -7", MSG_ISSENDER, MSG_MEDIASTATUS), new String[]{}));
    }

    public void listAllMediaWorkers() {
        List<MediaWorker> mediaWorkers = rdb.selectQuery().getAllMediaWorkers();
        for (MediaWorker mw :
                mediaWorkers) {
        }
    }

    //*********** fellows workers helper start ***********
    public boolean newMediaWorkerRow(String row_id, String worker_id) {
        try {
            ContentValues cv = new ContentValues();
            cv.put(DatabaseHelper.MEDIA_WORKER_MESSAGE_ROW_ID, row_id);
            cv.put(DatabaseHelper.MEDIA_WORKER_WORKER_ENQUEUE_ID, worker_id);
            rdb.insertQuery().insertMediaWorker(MediaWorker.fromContentValues(cv));
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public boolean updateMediaWorkerRow(String row_id, String worker_id) {
        try {
            ContentValues cv = new ContentValues();
            cv.put(MEDIA_WORKER_WORKER_ENQUEUE_ID, worker_id);
            updateRDB1Col(MEDIA_WORKER_TABLE_NAME, cv, MEDIA_WORKER_MESSAGE_ROW_ID, row_id);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public boolean insertOrUpdateMediaWorkerRow(String row_id, String worker_id) {
        String mediaWorkerId = rdb.selectQuery().getMediaWorkerId(row_id);
        if (mediaWorkerId != null) {
            return updateMediaWorkerRow(row_id, worker_id);
        } else {
            return newMediaWorkerRow(row_id, worker_id);
        }
    }
    //*********** fellows worker helper end ***********
}