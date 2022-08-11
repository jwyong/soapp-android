package com.soapp.sql.room;

import android.database.Cursor;

import com.soapp.sql.room.entity.Appointment;
import com.soapp.sql.room.entity.AppointmentHist;
import com.soapp.sql.room.entity.ApptWorkUUID;
import com.soapp.sql.room.entity.Booking;
import com.soapp.sql.room.entity.ChatList;
import com.soapp.sql.room.entity.ContactRoster;
import com.soapp.sql.room.entity.ErrorLogs;
import com.soapp.sql.room.entity.MediaWorker;
import com.soapp.sql.room.entity.Message;
import com.soapp.sql.room.entity.PushNotification;
import com.soapp.sql.room.entity.Restaurant;
import com.soapp.sql.room.entity.Reward;
import com.soapp.sql.room.joiners.Applist;
import com.soapp.sql.room.joiners.BookingDetails;
import com.soapp.sql.room.joiners.BookingList;
import com.soapp.sql.room.joiners.ChatTabList;
import com.soapp.sql.room.joiners.GroupChatLogDB;
import com.soapp.sql.room.joiners.GrpMemList;
import com.soapp.sql.room.joiners.IndiChatLogDB;
import com.soapp.sql.room.joiners.SharingList;

import java.util.List;

import androidx.lifecycle.LiveData;
import androidx.paging.DataSource;
import androidx.room.Dao;
import androidx.room.Query;
import androidx.room.RoomWarnings;
import androidx.room.Transaction;
import io.reactivex.Maybe;

/* Created by ibrahim on 25/03/2018. */

@Dao
public interface GeneralSelectQuery {
    // >>>> POPULATE LIST RELATED <<<< //
    //// --- Global --- ////
    //sharinglist (global)
    @Transaction
    @SuppressWarnings(RoomWarnings.CURSOR_MISMATCH)
    @Query("select CONTACT_ROSTER.ContactRow, CHAT_LIST.LastDateReceived, CONTACT_ROSTER.ContactJid, CONTACT_ROSTER.DisabledStatus, " +
            "CONTACT_ROSTER.PhoneName, CONTACT_ROSTER.PhoneNumber, CONTACT_ROSTER.DisplayName, " +
            "CONTACT_ROSTER.ProfilePhoto, CONTACT_ROSTER.Selected from CHAT_LIST INNER JOIN CONTACT_ROSTER " +
            "on CHAT_LIST.ChatJid = CONTACT_ROSTER.ContactJid where CHAT_LIST.LastDispMsg IS NOT NULL AND CHAT_LIST.LastDispMsg != '' " +
            "ORDER BY LastDateReceived DESC")
    LiveData<List<SharingList>> getSharing();

    @Transaction
    @SuppressWarnings(RoomWarnings.CURSOR_MISMATCH)
    @Query("select CONTACT_ROSTER.ContactRow, CHAT_LIST.LastDateReceived, CONTACT_ROSTER.ContactJid, " +
            "CONTACT_ROSTER.DisabledStatus, CONTACT_ROSTER.PhoneName, CONTACT_ROSTER.PhoneNumber, " +
            "CONTACT_ROSTER.DisplayName, CONTACT_ROSTER.ProfilePhoto, CONTACT_ROSTER.Selected " +
            "from CONTACT_ROSTER  LEFT OUTER JOIN CHAT_LIST  on CONTACT_ROSTER.ContactJid = CHAT_LIST.ChatJid " +
            "where (length(CONTACT_ROSTER.ContactJid) = 12 AND CONTACT_ROSTER.PhoneName IS NOT NULL " +
            "AND CONTACT_ROSTER.PhoneName != '' AND CONTACT_ROSTER.PhoneName LIKE '%' || :query || '%') " +
            "OR (length(CONTACT_ROSTER.ContactJid) > 12 AND CONTACT_ROSTER.DisplayName LIKE '%' || :query || '%') " +
            "ORDER BY length(CONTACT_ROSTER.ContactJid) ASC, CONTACT_ROSTER.PhoneName COLLATE NOCASE ASC, CONTACT_ROSTER.DisplayName COLLATE NOCASE ASC")
    List<SharingList> searchSharing(String query);

//    @Transaction
//    @Query("select * from CONTACT_ROSTER")
//    LiveData<List<SharingList>> getSharing();
//
//    @Transaction
//    @Query("select CONTACT_ROSTER._id, CHAT_LIST.LastDateReceived, CONTACT_ROSTER.ContactJid, " +
//            "CHAT_LIST.DisabledStatus, CONTACT_ROSTER.PhoneName, CONTACT_ROSTER.PhoneNumber, " +
//            "CONTACT_ROSTER.DisplayName, CONTACT_ROSTER.ProfilePhoto, CONTACT_ROSTER.Selected " +
//            "from CONTACT_ROSTER LEFT OUTER JOIN CHAT_LIST on CONTACT_ROSTER.ContactJid = CHAT_LIST.ChatJid " +
//            "where (length(CONTACT_ROSTER.ContactJid) = 12 AND CONTACT_ROSTER.PhoneName IS NOT NULL " +
//            "AND CONTACT_ROSTER.PhoneName != '' AND CONTACT_ROSTER.PhoneName LIKE '%' || :query || '%') " +
//            "OR (length(CONTACT_ROSTER.ContactJid) > 12 AND CONTACT_ROSTER.DisplayName LIKE '%' || :query || '%') " +
//            "ORDER BY length(CONTACT_ROSTER.ContactJid) DESC, CONTACT_ROSTER.PhoneName COLLATE NOCASE ASC, CONTACT_ROSTER.DisplayName COLLATE NOCASE ASC")
//    List<SharingList> searchSharing(String query);

    ////[END] --- Global --- ////

    //// --- HomeTab --- ////
    //Favourites
    @Query("select  RESTAURANT.* from RESTAURANT where ResFavourited = 1 ORDER BY RESTAURANT.ResTitle ASC")
    LiveData<List<Restaurant>> getFavourites();

    //Booking
//    @Query("select Booking.*,RESTAURANT.* FROM BOOKING INNER JOIN RESTAURANT ON BOOKING.BookingResID = RESTAURANT.ResID" +
//            " where Booking.BookerJid =  :user_jid  AND Booking.BookingStatus > 0 AND Booking.BookingSharedJid IS NULL " +
//            "ORDER BY Booking.BookingDate ASC, Booking.BookingTime ASC")
//    LiveData<List<BookingList>> getResBookings(String user_jid);

    @Query("select BOOKING.*, RESTAURANT.* FROM BOOKING INNER JOIN RESTAURANT ON BOOKING.BookingResID = RESTAURANT.ResID")
    LiveData<List<BookingList>> getResBookings();

    @Query("select BOOKING.*, RESTAURANT.* FROM BOOKING INNER JOIN RESTAURANT ON BOOKING.BookingResID = RESTAURANT.ResID " +
            "where BookingStatus > 0 AND (ResTitle LIKE '%' || :query || '%' OR ResLocation LIKE '%' || :query || '%' OR " +
            "ResState LIKE  '%' || :query || '%' ) ORDER BY BookingDate ASC, BookingTime ASC")
    List<BookingList> searchResBookings(String query);
    ////[END] --- HomeTab --- ////


    //// --- ChatTab --- ////
    //chatlist (old cursor loader)
    @Query("select CONTACT_ROSTER.ContactRow, CHAT_LIST.Admin_Self, CHAT_LIST.NotiBadge, CHAT_LIST.LastDateReceived, " +
            "CHAT_LIST.LastDispMsg, CHAT_LIST.ChatJid, CHAT_LIST.OnlineStatus, CHAT_LIST.LastSenderName, " +
            "CHAT_LIST.TypingName, CONTACT_ROSTER.DisabledStatus, CONTACT_ROSTER.PhoneName, CONTACT_ROSTER.PhoneNumber, " +
            "CONTACT_ROSTER.DisplayName, CONTACT_ROSTER.ProfilePhoto, CHAT_LIST.TypingStatus " +
            "from CHAT_LIST INNER JOIN CONTACT_ROSTER on CHAT_LIST.ChatJid = CONTACT_ROSTER.ContactJid " +
            "where CHAT_LIST.LastDispMsg IS NOT NULL AND CHAT_LIST.LastDispMsg != '' " +
            "ORDER BY LastDateReceived DESC")
    Cursor getChattab();

    //load chattab (liveData)
    @Transaction
    @Query("select * from CHAT_LIST " +
            "LEFT OUTER JOIN CONTACT_ROSTER on CHAT_LIST.ChatJid = CONTACT_ROSTER.ContactJid " +
            "ORDER BY LastDateReceived DESC")
    LiveData<List<ChatTabList>> getChattabList();

    //indichatlog
    @SuppressWarnings(RoomWarnings.CURSOR_MISMATCH)
    @Query("select MESSAGE.MsgRow, MESSAGE.IsSender, MESSAGE.MsgDate, MESSAGE.contact, MESSAGE.contactname, " +
            "MESSAGE.contactnumber, MESSAGE.MsgUniqueId, MESSAGE.MsgInfoId, MESSAGE.MsgJid, MESSAGE.MsgLatitude, " +
            "MESSAGE.MsgLongitude,MESSAGE.SenderJid, MESSAGE.MsgData, " +
            "MESSAGE.MediaStatus, MESSAGE.MsgInfoUrl, MESSAGE.MsgOffline, " +
            "APPOINTMENT.Self_Status, APPOINTMENT.Friend_Status, APPOINTMENT.ApptTitle, APPOINTMENT.ApptTime, APPOINTMENT.ApptLocation, " +
            "APPOINTMENT_HIST.ApptH_Title, APPOINTMENT_HIST.ApptH_Time, APPOINTMENT_HIST.ApptH_Location " +
            "FROM MESSAGE " +
            "LEFT OUTER JOIN APPOINTMENT on MESSAGE.MsgJid = APPOINTMENT.ApptJid AND MESSAGE.MsgInfoId = APPOINTMENT.ApptID " +
            "LEFT OUTER JOIN APPOINTMENT_HIST on MESSAGE.MsgJid = APPOINTMENT_HIST.ApptH_Jid AND MESSAGE.MsgInfoId = APPOINTMENT_HIST.ApptH_ID " +
            "WHERE MsgJid = :jid ORDER BY MsgDate DESC")
    DataSource.Factory<Integer, IndiChatLogDB> getIndiChatLogPaging(String jid);

    //grp chat log (joined)
    @SuppressWarnings(RoomWarnings.CURSOR_MISMATCH)
    @Query("SELECT MESSAGE.MsgRow, MESSAGE.contact, MESSAGE.IsSender,MESSAGE.MsgDate, MESSAGE.contactname, " +
            "MESSAGE.contactnumber, MESSAGE.MsgUniqueId, MESSAGE.MsgInfoId, MESSAGE.MsgJid, MESSAGE.MsgLatitude, " +
            "MESSAGE.MsgLongitude,MESSAGE.SenderJid, MESSAGE.MsgData, CONTACT_ROSTER.ProfilePhoto, CONTACT_ROSTER.PhoneName, CONTACT_ROSTER.DisplayName, " +
            "CONTACT_ROSTER.PhoneNumber, GROUPMEM.TextColor, MESSAGE.MediaStatus, MESSAGE.MsgInfoUrl, MESSAGE.MsgOffline, " +
            "APPOINTMENT.Self_Status, APPOINTMENT.ApptTitle, APPOINTMENT.ApptTime, APPOINTMENT.ApptLocation, " +
            "APPOINTMENT_HIST.ApptH_Title, APPOINTMENT_HIST.ApptH_Time, APPOINTMENT_HIST.ApptH_Location, GROUPMEM_STATUS.GMSStatus " +
            "FROM MESSAGE " +
            "LEFT OUTER JOIN CONTACT_ROSTER on MESSAGE.SenderJid = CONTACT_ROSTER.ContactJid " +
            "LEFT OUTER JOIN GROUPMEM on MESSAGE.SenderJid = GROUPMEM.MemberJid AND MESSAGE.MsgJid = GROUPMEM.RoomJid " +
            "LEFT OUTER JOIN APPOINTMENT on MESSAGE.MsgJid = APPOINTMENT.ApptJid AND MESSAGE.MsgInfoId = APPOINTMENT.ApptID " +
            "LEFT OUTER JOIN APPOINTMENT_HIST on MESSAGE.MsgJid = APPOINTMENT_HIST.ApptH_Jid AND MESSAGE.MsgInfoId = APPOINTMENT_HIST.ApptH_ID " +
            "LEFT OUTER JOIN GROUPMEM_STATUS on MESSAGE.SenderJid = GROUPMEM_STATUS.GMSMemberJid AND MESSAGE.MsgInfoId = GROUPMEM_STATUS.GMSApptID " +
            "where MESSAGE.MsgJid = :jid ORDER BY MESSAGE.MsgDate DESC")
    DataSource.Factory<Integer, GroupChatLogDB> getGrpChatLogPaging(String jid);
    ////[END] --- ChatTab --- ////


    //// --- New Chat/Sche --- ////
    //new indilist
    @SuppressWarnings(RoomWarnings.CURSOR_MISMATCH)
    @Query("select ContactRow, ContactJid, PhoneName, PhoneNumber, ProfilePhoto, PhotoUrl from CONTACT_ROSTER " +
            "where PhoneName IS NOT NULL AND PhoneName != '' ORDER BY PhoneName COLLATE NOCASE ASC")
    LiveData<List<ContactRoster>> getNewIndi();

    @Query("select ContactRow, ContactJid, PhoneName, PhoneNumber, ProfilePhoto, PhotoUrl from CONTACT_ROSTER " +
            "where PhoneName IS NOT NULL AND PhoneName != '' AND (PhoneName LIKE '%' || :query || '%') ORDER BY PhoneName COLLATE NOCASE ASC")
    Cursor searchNewIndi(String query);

    //new grplist
    @SuppressWarnings(RoomWarnings.CURSOR_MISMATCH)
    @Query("select ContactRow, ContactJid, PhoneName, PhoneNumber, DisplayName, ProfilePhoto, PhotoUrl, Selected " +
            "from CONTACT_ROSTER where PhoneName IS NOT NULL AND PhoneName != '' ORDER BY PhoneName COLLATE NOCASE ASC")
    LiveData<List<ContactRoster>> getNewGroup();

    @SuppressWarnings(RoomWarnings.CURSOR_MISMATCH)
    @Transaction
    @Query("select ContactRow, ContactJid, PhoneName, PhoneNumber, DisplayName, ProfilePhoto, PhotoUrl, Selected " +
            "from CONTACT_ROSTER where PhoneName IS NOT NULL AND PhoneName != '' AND (PhoneName LIKE '%' || :query || '%') ORDER BY PhoneName COLLATE NOCASE ASC")
    LiveData<List<ContactRoster>> searchNewGroup(String query);

    //create new group selected
    @SuppressWarnings(RoomWarnings.CURSOR_MISMATCH)
    @Query("select ContactRow, ContactJid, PhoneName, PhoneNumber, DisplayName, ProfilePhoto " +
            "from CONTACT_ROSTER where Selected = 1 ORDER BY PhoneName COLLATE NOCASE ASC, DisplayName COLLATE NOCASE ASC")
    LiveData<List<ContactRoster>> getNewGroupSelected();

    //[DONE NEW]load selected group members in CR (get profile imges)
    @SuppressWarnings(RoomWarnings.CURSOR_MISMATCH)
    @Query("select ProfilePhoto from CONTACT_ROSTER where Selected = 1 ORDER BY PhoneName COLLATE NOCASE ASC, " +
            "DisplayName COLLATE NOCASE ASC")
    LiveData<List<ContactRoster>> load_selectedGrpMemCR();

    //load existing groups
    @SuppressWarnings(RoomWarnings.CURSOR_MISMATCH)
    @Transaction
    @Query("select CHAT_LIST.ChatRow, CHAT_LIST.LastDateReceived, CONTACT_ROSTER.DisplayName, " +
            "CHAT_LIST.ChatJid, CONTACT_ROSTER.ProfilePhoto, CONTACT_ROSTER.DisabledStatus " +
            "from CHAT_LIST INNER JOIN CONTACT_ROSTER on CHAT_LIST.ChatJid = CONTACT_ROSTER.ContactJid " +
            "where length(CHAT_LIST.ChatJid) > 12 ORDER BY CHAT_LIST.LastDateReceived DESC")
    LiveData<List<ChatTabList>> getExistGroup();

    @SuppressWarnings(RoomWarnings.CURSOR_MISMATCH)
    @Transaction
    @Query("select CHAT_LIST.ChatRow, CHAT_LIST.LastDateReceived, CONTACT_ROSTER.DisplayName, " +
            "CHAT_LIST.ChatJid, CONTACT_ROSTER.ProfilePhoto, CONTACT_ROSTER.DisabledStatus " +
            "from CHAT_LIST INNER JOIN CONTACT_ROSTER on CHAT_LIST.ChatJid = CONTACT_ROSTER.ContactJid " +
            "where length(CHAT_LIST.ChatJid) > 12 AND CONTACT_ROSTER.DisplayName LIKE '%' || :query || '%' " +
            "ORDER BY CHAT_LIST.LastDateReceived DESC")
    List<ChatTabList> searchExistGroup(String query);
    ////[END] --- New Chat/Sche --- ////


    //// --- ScheTab --- ////
    //schelist (currently not sorting by noti badge yet)
    //can do order by based on sorting settings
    @SuppressWarnings(RoomWarnings.CURSOR_MISMATCH)
    @Query("select APPOINTMENT.ApptRow, APPOINTMENT.ApptID, APPOINTMENT.ApptNotiBadge, APPOINTMENT.ApptLocation, APPOINTMENT.ApptDate, APPOINTMENT.ApptTime, " +
            "APPOINTMENT.Self_Status, APPOINTMENT.ApptTitle, CONTACT_ROSTER.DisplayName, APPOINTMENT.ApptJid, " +
            "CONTACT_ROSTER.PhoneNumber, CONTACT_ROSTER.PhoneName, CONTACT_ROSTER.ProfilePhoto " +
            "from APPOINTMENT INNER JOIN CONTACT_ROSTER on APPOINTMENT.ApptJid = CONTACT_ROSTER.ContactJid " +
            "where APPOINTMENT.ApptTime != 0 " +
            "ORDER BY APPOINTMENT.ApptDate ASC, APPOINTMENT.Self_Status ASC, APPOINTMENT.ApptTime ASC")
    LiveData<List<Applist>> getScheTab();

    //schelist - not going
    @SuppressWarnings(RoomWarnings.CURSOR_MISMATCH)
    @Query("select APPOINTMENT.ApptRow, APPOINTMENT.ApptID, APPOINTMENT.ApptLocation, APPOINTMENT.ApptTime, " +
            "APPOINTMENT.Self_Status, APPOINTMENT.ApptTitle, CONTACT_ROSTER.DisplayName, APPOINTMENT.ApptJid, " +
            "CONTACT_ROSTER.PhoneNumber, CONTACT_ROSTER.PhoneName, CONTACT_ROSTER.ProfilePhoto " +
            "from APPOINTMENT INNER JOIN CONTACT_ROSTER on APPOINTMENT.ApptJid = CONTACT_ROSTER.ContactJid " +
            "WHERE APPOINTMENT.Self_Status = 3 AND APPOINTMENT.ApptDate = :apptDate " +
            "ORDER BY APPOINTMENT.ApptDate ASC, APPOINTMENT.ApptTime ASC")
    LiveData<List<Applist>> getScheTabNotGoing(long apptDate);

    //search schelist
    @SuppressWarnings(RoomWarnings.CURSOR_MISMATCH)
    @Transaction
    @Query("select APPOINTMENT.ApptRow, APPOINTMENT.ApptID, APPOINTMENT.ApptNotiBadge, APPOINTMENT.ApptLocation, APPOINTMENT.ApptDate, APPOINTMENT.ApptTime, " +
            "APPOINTMENT.Self_Status, APPOINTMENT.ApptTitle, CONTACT_ROSTER.DisplayName, APPOINTMENT.ApptJid, " +
            "CONTACT_ROSTER.PhoneNumber, CONTACT_ROSTER.PhoneName, CONTACT_ROSTER.ProfilePhoto " +
            "from APPOINTMENT INNER JOIN CONTACT_ROSTER on APPOINTMENT.ApptJid = CONTACT_ROSTER.ContactJid " +
            "where (CONTACT_ROSTER.PhoneName LIKE '%' || :query || '%' OR CONTACT_ROSTER.DisplayName LIKE '%' || :query || '%' " +
            "OR APPOINTMENT.ApptTitle LIKE '%' || :query || '%') AND CONTACT_ROSTER.DisabledStatus != 1 AND APPOINTMENT.ApptTime != 0 " +
            "ORDER BY APPOINTMENT.ApptDate ASC, APPOINTMENT.Self_Status ASC, APPOINTMENT.ApptTime ASC")
    List<Applist> searchScheTab(String query);

    //load sche log
    @SuppressWarnings(RoomWarnings.CURSOR_MISMATCH)
    @Query("select * from APPOINTMENT where ApptJid = :jid AND APPOINTMENT.ApptTime != 0 " +
            "ORDER BY ApptDate ASC, ApptTime ASC")
    LiveData<List<Appointment>> load_scheLog(String jid);

    //grpschelog
    //get profile images of grp members in a grp
    @SuppressWarnings(RoomWarnings.CURSOR_MISMATCH)
    @Query("select CONTACT_ROSTER.ProfilePhoto from CONTACT_ROSTER INNER JOIN GROUPMEM " +
            "on GROUPMEM.MemberJid = CONTACT_ROSTER.ContactJid " +
            "where GROUPMEM.RoomJid = :roomJid ORDER BY CASE " +
            "WHEN CONTACT_ROSTER.PhoneName IS NOT NULL THEN " +
            "CONTACT_ROSTER.PhoneName ELSE " +
            "CONTACT_ROSTER.DisplayName END, " +
            "CONTACT_ROSTER.PhoneName COLLATE NOCASE")
    LiveData<List<ContactRoster>> load_grpMemSche(String roomJid);

    //get profile images of grp members in a grp based on appt status
    @SuppressWarnings(RoomWarnings.CURSOR_MISMATCH)
    @Query("select CONTACT_ROSTER.ProfilePhoto from CONTACT_ROSTER " +
            "LEFT OUTER JOIN GROUPMEM on GROUPMEM.MemberJid = CONTACT_ROSTER.ContactJid " +
            "LEFT OUTER JOIN GROUPMEM_STATUS on GROUPMEM_STATUS.GMSMemberJid = CONTACT_ROSTER.ContactJid " +
            "where GROUPMEM.RoomJid = :roomJid " +
            "AND GROUPMEM_STATUS.GMSRoomJid = :roomJid " +
            "AND GROUPMEM_STATUS.GMSApptID = :apptID " +
            "ORDER BY GROUPMEM_STATUS.GMSStatus ASC")
//            "CASE " +
//            "WHEN CONTACT_ROSTER.PhoneName IS NOT NULL THEN " +
//            "CONTACT_ROSTER.PhoneName ELSE " +
//            "CONTACT_ROSTER.DisplayName END, " +
//            "GROUPMEM_STATUS.GMSStatus ASC, CONTACT_ROSTER.PhoneName COLLATE NOCASE")
    LiveData<List<ContactRoster>> load_grpMemScheGoingFirst(String roomJid, String apptID);

    @SuppressWarnings(RoomWarnings.CURSOR_MISMATCH)
    @Query("select CONTACT_ROSTER.ContactJid, CONTACT_ROSTER.ProfilePhoto,  CONTACT_ROSTER.displayname,  CONTACT_ROSTER.phonename, CONTACT_ROSTER.phonenumber " +
            "from GROUPMEM_STATUS INNER JOIN CONTACT_ROSTER on GROUPMEM_STATUS.GMSMemberJid = CONTACT_ROSTER.ContactJid " +
            "where GROUPMEM_STATUS.GMSApptID = :apptID AND GROUPMEM_STATUS.GMSStatus = :status")
    LiveData<List<ContactRoster>> load_grpMemSche_with_status(String apptID, Integer status);
    ////[END] --- ScheTab --- ////


    //// --- Room-Details --- ////
    @SuppressWarnings(RoomWarnings.CURSOR_MISMATCH)
    @Query("select MsgDate, IsSender, MsgInfoUrl, MsgLongitude, MsgInfoId from MESSAGE where MsgJid = :jid AND MsgInfoUrl IS NOT NULL " +
            "AND MediaStatus = 100 AND (IsSender = 20 OR IsSender = 21 ) ORDER BY MsgDate ASC")
    LiveData<List<Message>> load_chatDetImageVideo(String jid);

    //load grp-in-grp
    @SuppressWarnings(RoomWarnings.CURSOR_MISMATCH)
    @Query("select CONTACT_ROSTER.ContactRow, CONTACT_ROSTER.ContactJid, CONTACT_ROSTER.PhoneName, " +
            "CONTACT_ROSTER.PhoneNumber, CONTACT_ROSTER.DisplayName, CONTACT_ROSTER.ProfilePhoto, " +
            "CONTACT_ROSTER.PhotoUrl, CONTACT_ROSTER.Selected " +
            "from GROUPMEM INNER JOIN CONTACT_ROSTER on GROUPMEM.MemberJid = CONTACT_ROSTER.ContactJid " +
            "where GROUPMEM.RoomJid = :roomJid ORDER BY CONTACT_ROSTER.PhoneName ASC, CONTACT_ROSTER.DisplayName ASC")
    LiveData<List<ContactRoster>> getGroupInGroup(String roomJid);

    @SuppressWarnings(RoomWarnings.CURSOR_MISMATCH)
    @Query("select CONTACT_ROSTER.ContactJid, CONTACT_ROSTER.PhoneName, " +
            "CONTACT_ROSTER.PhoneNumber, CONTACT_ROSTER.displayname, CONTACT_ROSTER.profilephoto, " +
            "CONTACT_ROSTER.PhotoUrl, CONTACT_ROSTER.Selected " +
            "from GROUPMEM INNER JOIN CONTACT_ROSTER on GROUPMEM.MemberJid = CONTACT_ROSTER.ContactJid " +
            "where GROUPMEM.RoomJid = :b AND (CONTACT_ROSTER.PhoneName LIKE '%' || :a || '%' " +
            "OR ((CONTACT_ROSTER.PhoneName IS NULL OR CONTACT_ROSTER.PhoneName = '') " +
            "AND CONTACT_ROSTER.DisplayName LIKE '%' || :a || '%')) " +
            "ORDER BY CONTACT_ROSTER.PhoneName ASC, CONTACT_ROSTER.DisplayName ASC")
    LiveData<List<ContactRoster>> searchGroupInGroup(String a, String b);

    //indi chat profile load common grps
    @SuppressWarnings(RoomWarnings.CURSOR_MISMATCH)
    @Query("select CONTACT_ROSTER.DisplayName, CONTACT_ROSTER.ContactJid, CONTACT_ROSTER.PhotoUrl, " +
            "CONTACT_ROSTER.ProfilePhoto from " +
            "CONTACT_ROSTER INNER JOIN GROUPMEM on CONTACT_ROSTER.ContactJid = GROUPMEM.RoomJid where " +
            "GROUPMEM.MemberJid = :jid ORDER BY " +
            "CONTACT_ROSTER.DisplayName COLLATE NOCASE")
    LiveData<List<ContactRoster>> load_commonGrpsChatDetail(String jid);

    //group profile load members
    @SuppressWarnings(RoomWarnings.CURSOR_MISMATCH)
    @Query("select GROUPMEM.Admin, CONTACT_ROSTER.DisplayName, " +
            "CONTACT_ROSTER.ContactJid, CONTACT_ROSTER.PhoneName, CONTACT_ROSTER.PhoneNumber, " +
            "CONTACT_ROSTER.PhotoUrl, CONTACT_ROSTER.ProfilePhoto, CONTACT_ROSTER.ProfileFull from " +
            "GROUPMEM INNER JOIN CONTACT_ROSTER on GROUPMEM.MemberJid = CONTACT_ROSTER.ContactJid where " +
            "GROUPMEM.RoomJid = :roomjid ORDER BY CASE " +
            "WHEN CONTACT_ROSTER.PhoneName IS NOT NULL THEN " +
            "CONTACT_ROSTER.PhoneName ELSE " +
            "'zzz' END, " +
            "CONTACT_ROSTER.PhoneName COLLATE NOCASE")
    LiveData<List<GrpMemList>> load_grpMemChatDetail(String roomjid);
    ////[END] --- Group-Details --- ////
    //[END] >>>> POPULATE LIST RELATED <<<< //


    //// --- Check-Exist-Related --- ////
    @Query("select ChatJid from CHAT_LIST where ChatJid = :jid")
    Cursor getCLExist(String jid);

    @Query("select count(MsgUniqueId) from MESSAGE where MsgUniqueId = :uniqueID")
    int getUniqueIDExist(String uniqueID);

    @Query("select COUNT(BookingId) from BOOKING where BookingId = :bookingid AND BookingSharedJid = :sharedJid AND BookingStatus > 0")
    Cursor getSharedOutResBookingExist(String bookingid, String sharedJid);

    @Query("select count(ApptID) from Appointment where ApptID = :apptID")
    int getAppointmentExist(String apptID);

    @Query("select count(ApptH_ID) from APPOINTMENT_HIST where ApptH_ID = :apptID")
    int getAppointmentHistExist(String apptID);

    @Query("select GMSRow from GROUPMEM_STATUS where GMSRoomJid = :roomjid AND GMSMemberJid = :memberjid " +
            "AND GMSApptID = :apptID")
    Cursor getApptInGMSExist(String roomjid, String memberjid, String apptID);

    @Query("select exists(select 1 from ApptWorkUUID where ApptID = :apptid)")
    boolean getApptWorkIDExits(String apptid);

    @Query("select GMSMemberJid, GMSStatus from GROUPMEM_STATUS where GMSRoomJid = :roomjid AND GMSApptID = :apptID " +
            "AND GMSMemberJid != :requestorJID")
    Cursor getGrpMemAllExceptRequestorStatus(String roomjid, String apptID, String requestorJID);

    @Query("select GroupRow from GROUPMEM where RoomJid = :roomjid AND MemberJid = :memberJid ")
    Cursor getMemberInGrpMemExist(String roomjid, String memberJid);

    @Query("select GMSRow from GROUPMEM_STATUS where GMSRoomJid = :roomjid AND GMSMemberJid = :memberJid ")
    Cursor getMemberInGMSExist(String roomjid, String memberJid);

    @Query("select GroupRow from GROUPMEM where RoomJid = :roomjid")
    Cursor getGrpInGrpMemExist(String roomjid);

    @Query("select GMSRow from GROUPMEM_STATUS where GMSRoomJid = :roomjid")
    Cursor getGrpInGMSExist(String roomjid);

    @Query("select count(ContactJid) from CONTACT_ROSTER where ContactJid = :jid ")
    int getContactRosterExist(String jid);

    @Query("select ContactRow from CONTACT_ROSTER where ContactJid = :jid AND PhoneName IS NOT NULL AND PhoneName != ''")
    Cursor getPhoneNameExist(String jid);

    @Query("select ContactRow from CONTACT_ROSTER where PhoneNumber = :phoneNumber AND PhoneName IS NOT NULL AND PhoneName != ''")
    Cursor getPhoneNameExistNumber(String phoneNumber);

    @Query("select count(BookingId) from BOOKING where BookingId = :id AND BookingStatus > 0")
    int getSelfResBookingExist(String id);

    @Query("select count(BookingId) from BOOKING where BookingResId = :id AND BookingStatus > 0")
    int getBookingExistBasedOnResID(String id);


    @Query("SELECT COUNT(BookingId) FROM BOOKING WHERE BookingResId = :resID")
    int getBookingIdCountBasedOnResId(String resID);

    @Query("SELECT BookingId FROM BOOKING WHERE BookingResId = :resID")
    String getBookingIdFromResId(String resID);

    //    @Query("SELECT")
    @Query("select MsgRow from MESSAGE where MsgJid = :jid and IsSender = 3")
    Cursor getAddToContactExist(String jid);

    @Query("select ResID from RESTAURANT where ResID = :resid")
    Cursor getRestaurantExist(String resid);

    @Query("select MsgRow from MESSAGE where MsgJid = :jid AND IsSender = 40")
    Cursor getTrackingExist(String jid);

    @Query("select MsgRow from MESSAGE where MsgJid = :jid and IsSender = 1")
    Cursor getUnreadMsgExist(String jid);

    @Query("select count(MsgRow) from MESSAGE where MsgJid = :jid and IsSender = 4")
    int getMsgBlockedExist(String jid);

    ////[END] --- Check-Exist-Related --- ////


    //// --- Get-Info-Related --- ////
    /// Appointment ///
    //get appt details for migrating to apptHist table
    @SuppressWarnings(RoomWarnings.CURSOR_MISMATCH)
    @Query(" select * from APPOINTMENT where ApptID = :apptID")
    Appointment getAllApptDetails(String apptID);

    @SuppressWarnings(RoomWarnings.CURSOR_MISMATCH)
    @Query("select ApptH_Title, ApptH_Time, ApptH_Location from APPOINTMENT_HIST where ApptH_ID = :apptID")
    List<AppointmentHist> getApptHBasicDet(String apptID);

    @Query("select Self_Status from APPOINTMENT where ApptID = :apptID")
    int getApptStatusSelf(String apptID);

    @Query("select Friend_Status from APPOINTMENT where ApptID = :apptID")
    int getApptStatusFriend(String apptID);

    @Query("select GMSStatus from GROUPMEM_STATUS where GMSApptID = :apptID AND GMSRoomJid = :roomJid " +
            "AND GMSMemberJid = :memberJid")
    int getApptStatusGM(String apptID, String roomJid, String memberJid);

    @Query("select ApptID from APPOINTMENT where ApptJid = :jid ")
    Cursor getApptCount(String jid);

    @Query("select ApptJid from APPOINTMENT where ApptNotiBadge > 0 ")
    Cursor getBadgeCountForScheTab();

    @Query("select CONTACT_ROSTER.PhoneName, CONTACT_ROSTER.DisplayName, CONTACT_ROSTER.PhoneNumber " +
            "from GROUPMEM_STATUS INNER JOIN CONTACT_ROSTER on GROUPMEM_STATUS.GMSMemberJid = CONTACT_ROSTER.ContactJid " +
            "where GROUPMEM_STATUS.GMSApptID = :apptID AND GROUPMEM_STATUS.GMSStatus = 0")
    Cursor getApptHostNameGrp(String apptID);

    @Query("select ApptTitle from APPOINTMENT where ApptID = :apptID")
    String getApptCurrentTitle(String apptID);

    @Query("select ApptH_Title from APPOINTMENT_HIST where ApptH_ID = :apptID")
    String getApptHistTitle(String apptID);

    @Query("select ApptTime from APPOINTMENT where ApptID = :apptID")
    long getApptTime(String apptID);

    @Query("select ApptReminderTime from APPOINTMENT where ApptID = :apptID")
    long getApptReminderTime(String apptID);

    @Query("select ApptLocation from APPOINTMENT where ApptID = :apptID")
    String getApptLoc(String apptID);


    @Query("select GMSRow from GROUPMEM_STATUS where GMSApptID = :apptID AND GMSStatus = 1")
    Cursor getGoingMemCount(String apptID);

    @Query("select GMSRow from GROUPMEM_STATUS where GMSApptID = :apptID AND GMSStatus = 2")
    Cursor getUndecMemCount(String apptID);

    @Query("select GMSRow from GROUPMEM_STATUS where GMSApptID = :apptID AND GMSStatus = 3")
    Cursor getNGMemCount(String apptID);

    @Query("select ApptDate from APPOINTMENT where Self_Status <= 1")
    Cursor getHostingGoingCDList();

    @Query("select ApptDate from APPOINTMENT where Self_Status = :selfStatus")
    Cursor getApptCDList(int selfStatus);

    @Query("select ApptID, ApptDate from APPOINTMENT where ApptDate = :date AND Self_Status <= 3")
    Cursor getHasApptCount_date(Long date);

    /// [END] appt ///

    @Query("select ChatJid from CHAT_LIST where NotiBadge > 0 AND LastDispMsg IS NOT NULL AND LastDispMsg != ''")
    Cursor getBadgeCountForChatTab();

    @Query("select ProfilePhoto from CONTACT_ROSTER where ContactJid = :jid ")
    byte[] getImageBytesThumbFromContactRoster(String jid);

    @Query("select ProfileFull from CONTACT_ROSTER where ContactJid = :jid ")
    Cursor getImageBytesFullFromContactRoster(String jid);

    @Query("select BookingResId from BOOKING where BookingSharedJid = :roomJid AND BookingStatus > 0 ORDER BY BookingDate DESC")
    Cursor getSharedInOutResBookingExist(String roomJid);

    @Query("select PhotoUrl from CONTACT_ROSTER where ContactJid = :jid ")
    Cursor getResourceURLFromContactRoster(String jid);

    @Query("select PhoneName, DisplayName, PhoneNumber from CONTACT_ROSTER where ContactJid = :jid")
    Cursor getNameFromContactRoster(String jid);

    @Query("select PhoneName, DisplayName from CONTACT_ROSTER where ContactJid = :jid ")
    Cursor getIndiNameFromCR(String jid);

    @Query("select DisabledStatus from CONTACT_ROSTER where ContactJid = :jid")
    int getDisabledStatus(String jid);

    @Query("select OnlineStatus from CHAT_LIST where ChatJid = :jid ")
    int getChatMarker(String jid);

    @Query("select ApptJid from APPOINTMENT where ApptDate = :apptDate AND Self_Status = 3")
    Cursor getApptNGSameDateCount(long apptDate);

    @Query("select MsgInfoId from MESSAGE where MsgUniqueId = :id ")
    Cursor getResourceIDFromUniqueID(String id);

    @Query("select distinct MemberJid from GROUPMEM where RoomJid = :roomjid")
    Cursor getMemCountFromGrpMem(String roomjid);

    @Query("select NotiBadge from CHAT_LIST where ChatJid = :jid")
    Cursor getBadgeFromRosterChat(String jid);

    @Query("select ApptNotiBadge from APPOINTMENT where ApptID = :apptID ")
    int getApptNotiBadge1Appt(String apptID);

    @Query("select ApptID from APPOINTMENT where ApptJid = :jid AND ApptNotiBadge >0 " +
            "ORDER BY ApptDate ASC, ApptTime ASC limit 1")
    String getClosestApptIDWithNoti(String jid);

    @Query("select SUM(ApptNotiBadge) from APPOINTMENT where ApptJid = :jid ")
    LiveData<Integer> getApptNotiBadge1Jid(String jid);

    @Query("select ApptTitle from APPOINTMENT where ApptJid = :jid ")
    Cursor isApptTitleSet(String jid);

//    @Query("select SUM(CHAT_LIST.NotiBadge), SUM(APPOINTMENT.ApptNotiBadge) from " +
//            "CHAT_LIST, APPOINTMENT where CHAT_LIST.NotiBadge > 0 OR APPOINTMENT.ApptNotiBadge > 0")
//    Cursor getTotalBadgeCountChat();

    @Query("select SUM(CHAT_LIST.NotiBadge) from CHAT_LIST where CHAT_LIST.NotiBadge > 0")
    int getTotalBadgeCountChat();

    @Query("select SUM(APPOINTMENT.ApptNotiBadge) from APPOINTMENT where APPOINTMENT.ApptNotiBadge > 0")
    int getTotalBadgeCountSche();

    @Query("select ContactJid from CONTACT_ROSTER where Selected = 1")
    Cursor getStatusCountFromCR();

    @Query("select ContactJid from CONTACT_ROSTER where Selected = 1")
    Cursor get_jidStatus1FromCR();

    @Query("select ContactJid from CONTACT_ROSTER where Selected = 1")
    Cursor get_jidStatus1FromCRGrpMem();

    @Query("select MemberJid from GROUPMEM where RoomJid = :roomJid")
    Cursor get_jidFromGrpMem(String roomJid);

    @Query("select ApptID from APPOINTMENT where ApptJid = :jid")
    Cursor get_apptIDFromRoom(String jid);

    @Query("select ApptID, ApptTitle, ApptDate, ApptTime, ApptLocation, ApptLatitude, ApptLongitude, " +
            "ApptMapURL, ApptResID, ApptResImgURL, Self_Status from APPOINTMENT where ApptJid = :roomjid ")
    Cursor get_grpApptDetails(String roomjid);

    @Query("select PhoneName, PhoneNumber, ProfilePhoto, DisplayName from CONTACT_ROSTER where ContactJid= :jid")
    Cursor get_userProfile(String jid);

    @Query("select DisplayName, PhoneNumber from CONTACT_ROSTER where ContactJid= :jid ")
    Cursor get_displayNamePhone(String jid);

    @Query("select ProfilePhoto, DisplayName from CONTACT_ROSTER where ContactJid= :jid ")
    Cursor get_grpProfile(String jid);

    @SuppressWarnings(RoomWarnings.CURSOR_MISMATCH)
    @Query("select DisplayName, ProfilePhoto, ProfileFull, PhoneNumber, PhoneName, PhotoUrl from CONTACT_ROSTER where ContactJid= :jid ")
    LiveData<ContactRoster> live_get_cr_profile(String jid);
    ////[END] --- Get-Info-Related --- ////


    /*////////////// [K01_01] CHAT-RELATED: global ////////////////*/

    @Query("select ChatRow from CHAT_LIST where TypingStatus = 1")
    List<Integer> get_typing1FromCL();

    @Query("select TypingStatus FROM CHAT_LIST where ChatJid = :jid")
    LiveData<Integer> getTypingStatus(String jid);

    @Query("select TypingStatus FROM CHAT_LIST where ChatJid = :jid")
    LiveData<ChatList> getGrpTypingStatus(String jid);

    /*////////////// [K01_01E] END CHAT-RELATED: global ////////////////*/


    /*////////////// [K01_02] CHAT: group-related ////////////////*/
    /*----------- [K01_02_01] Group-Incoming ----------- */

    @Query("select SenderJid from MESSAGE WHERE MsgJid = :roomJid AND SenderJid is NOT NULL ORDER BY MsgDate DESC Limit 2 ")
    Cursor GroupMessageInputDatabase(String roomJid);

    @Query("select MsgRow from MESSAGE where MsgJid = :jid  AND MsgDate = :date ")
    Cursor addNewTime(String jid, long date);

    @Query("select Admin_Self from CHAT_LIST where ChatJid = :jid")
    LiveData<Integer> getSelfAdminStatus(String jid);

    @Query("select GroupRow from GROUPMEM where roomjid = :roomjid AND Admin = 1")
    Cursor getGrpMemAdminExist(String roomjid);

    @Query("select ContactJid from CONTACT_ROSTER where length(ContactJid) = 12 AND PhoneName != null AND PhoneName != '' ")
    Cursor getAllIndiContactFromCR();

    @Query("select ApptTime, ApptJid, ApptID, Self_Status from APPOINTMENT where ApptDate IS NOT NULL AND ApptTime IS NOT NULL")
    Cursor getUpdatePendingAlarms();

    @Query("select BookingAttempts from BOOKING where BookingId = :bookingid AND BookerJid = :userjid AND BookingSharedJid IS NULL")
    int getBookingAttempts(String bookingid, String userjid);

    @Query("select BookingSharedJid, ResID from BOOKING INNER JOIN RESTAURANT ON BOOKING.BookingResId = RESTAURANT.ResID where  BookingId = :bookingid AND BookerJid = :userjid AND BookingSharedJid IS NOT NULL")
    Cursor pubsubBookingDeleted(String bookingid, String userjid);

    @Query("select BookingSharedJid from BOOKING INNER JOIN RESTAURANT ON BOOKING.BookingResId = RESTAURANT.ResID where BookingId = :bookingid AND BookerJid = :userjid AND BookingSharedJid IS NOT NULL")
    Cursor pubsubBookingAccepted(String bookingid, String userjid);

    @Query("select Booking.*, RESTAURANT.* from BOOKING INNER JOIN RESTAURANT ON BOOKING.BookingResId = RESTAURANT.ResID where BOOKING.BookingId = :bookingid ")
    LiveData<BookingDetails> getResBookingDetails(String bookingid);

    @Query("select Booking.* ,RESTAURANT.* from BOOKING INNER JOIN RESTAURANT ON BOOKING.BookingResId = RESTAURANT.ResID where BookingResId = :resid AND BookingSharedJid IS NULL ")
    Cursor getResBookingDetails1(String resid);

    @Query("select ResTitle from RESTAURANT where ResID = :resid ")
    Cursor getResTitle(String resid);

    @Query("select MsgRow from MESSAGE where MsgJid = :jid AND IsSender = 1")
    Cursor addChatBadgeUnread(String jid);

    @Query("select ApptLatitude, ApptLongitude from APPOINTMENT where ApptJid = :jid ")
    Cursor getlocationsingle(String jid);

    @Query("select CONTACT_ROSTER.PhoneName, CONTACT_ROSTER.DisplayName, CONTACT_ROSTER.PhoneNumber, GROUPMEM.Latitude, " +
            "GROUPMEM.Longitude from GROUPMEM INNER JOIN CONTACT_ROSTER on GROUPMEM.MemberJid = CONTACT_ROSTER.ContactJid " +
            "where GROUPMEM.RoomJid = :roomjid")
    Cursor getlocationgroup(String roomjid);

    @Query("select ResID from RESTAURANT where ResID = :resid")
    Cursor checkFavRestaurant(String resid);

    @Query("select ResOverallRating from RESTAURANT where ResID =:resid ")
    Cursor getResRating(String resid);

    @Query("select DISTINCT RoomJid from GROUPMEM")
    Cursor SPresetGrpMemColors();

    @Query("select DISTINCT MemberJid from GROUPMEM where roomjid = :roomjid")
    Cursor SPresetGrpMemColors2(String roomjid);

    @Query("select MsgInfoUrl, MsgInfoId , MsgUniqueId, MsgJid, IsSender from MESSAGE where MediaStatus = 4 order by MsgRow ASC")
    Cursor getUncomprocessVideos();

    @Query("select MsgInfoUrl, MsgInfoId , MsgUniqueId, MsgJid, IsSender, MsgRow from MESSAGE where MediaStatus = -7 order by MsgRow ASC")
    Cursor getOneCompressingVideo();

    @Query("select MESSAGE.MsgInfoUrl, MESSAGE.MsgInfoId , MESSAGE.MsgUniqueId, MESSAGE.MsgJid, MESSAGE.IsSender, MESSAGE.MsgRow, CONTACT_ROSTER.DisplayName from MESSAGE JOIN CONTACT_ROSTER on MESSAGE.MsgJid = CONTACT_ROSTER.ContactJid where MESSAGE.MediaStatus = 4 order by MESSAGE.MsgRow ASC")
    Cursor getOneQueueingVideo();

    @Query("select MsgInfoUrl, MsgInfoId , MsgUniqueId, MsgJid, IsSender from MESSAGE where MediaStatus = -7")
    Cursor checkForIsVideoCompressing();

    @Query("select MsgInfoUrl, MsgInfoId , MsgUniqueId, MsgJid, IsSender from MESSAGE where MediaStatus = 4")
    Cursor checkExistingOfUmcompressedVideo();

    @Query("select DISTINCT MsgJid from MESSAGE")
    Cursor SPupdateChatMarker0();

    @Query("select DISTINCT ContactJid from CONTACT_ROSTER")
    Cursor SPnewImages();

    @Query("select DisplayName from CONTACT_ROSTER where ContactJid = :jid")
    Cursor push_go_grp_chat(String jid);

    @Query("select ApptTime, Self_Status from APPOINTMENT where ApptID = :apptID")
    Cursor scheduleLocalNotification(String apptID);

    @Query("UPDATE APPOINTMENT SET ApptTitle=:title WHERE ApptJid = :ApptJid")
    int UpdateApptTittle(String title, String ApptJid);

    @Query("select * FROM MESSAGE WHERE MsgOffline = 1")
    List<Message> getOfflineMsg();

    @Query("select COUNT(MsgRow) from MESSAGE where MsgUniqueId = :uniqueid")
    int getMsgExistBasedonUniqueId(String uniqueid);

    @Query("select COUNT(MsgRow) from MESSAGE where MsgInfoId = :apptID AND IsSender = :isSender AND " +
            "SenderJid = :otherJid")
    int getApptUpdateMsgStatusExist(String apptID, int isSender, String otherJid);

    @Query("select COUNT(MsgRow) from MESSAGE where MsgInfoId = :apptID AND IsSender = :isSender")
    int getApptUpdateMsgExist(String apptID, int isSender);

    @Query("select * from BOOKING where BookingId = :bookid")
    Booking getBookInfoforOff(String bookid);

    @Query("select * FROM RESTAURANT WHERE ResID = :resid")
    Restaurant getResTaurantInfoBasedResID(String resid);

    @SuppressWarnings(RoomWarnings.CURSOR_MISMATCH)
    @Query("SELECT IsSender, SenderJid, MsgData, MsgLatitude FROM MESSAGE where MsgUniqueId = :uniqueid")
    Message getMessageFromUniqueID(String uniqueid);

    @Query("SELECT * FROM ErrorLogs")
    List<ErrorLogs> getErrorLogs();

    //    << BOOKING RELATED>>


    //test notifications
    @Query("select ProfilePhoto from CONTACT_ROSTER where ContactJid = :jid")
    byte[] getNameAndPicFromCR(String jid);

    @Query("select * from PushNotification where pushType = :type ORDER BY groupJid ASC,pnRow ASC, senderJid ASC")
    Maybe<List<PushNotification>> getPushMsgs(String type);

    //    << REWARD RELATED>>
    @SuppressWarnings(RoomWarnings.CURSOR_MISMATCH)
    @Transaction
    @Query("select * from REWARD")
    LiveData<List<Reward>> getRewardList();

    @Query("SELECT * FROM APPOINTMENT WHERE ApptTime <= :endTime ORDER BY ApptTime ASC")
    List<Appointment> getRecentAppttoRemind(Long endTime);

    @Query("SELECT reminderUUID, exactUUID, deleteUUID FROM APPTWORKUUID WHERE ApptID = :apptID")
    ApptWorkUUID getApptWorkUUID(String apptID);

    //Special functions
    @Query("select ProfilePhoto, ProfileFull, ContactJid from CONTACT_ROSTER")
    Cursor SPdelAllPropic();

    //media worker
    @Query("SELECT MediaWorkerWorkerEnqueueID FROM MEDIAWORKER WHERE MediaWorkerMessageRowID = :row_id")
    String getMediaWorkerId(String row_id);

    @Query("SELECT contact FROM MESSAGE WHERE MsgRow = :row_id")
    int getMsgDownloadId(String row_id);

    @Query("SELECT * FROM MEDIAWORKER")
    List<MediaWorker> getAllMediaWorkers();
}