package com.soapp.sql.room;

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

import androidx.room.Dao;
import androidx.room.Insert;

/* Created by ibrahim on 25/03/2018. */

@Dao
public interface GeneralInsertQuery {

    @Insert
    void insertFavourites(Restaurant restaurant);

    @Insert
    long insertResBookings(Booking booking);

    @Insert
    long insertContactRoster(ContactRoster contactRoster);

    @Insert
    long insertMessage(Message message);

    @Insert
    void insertChatList(ChatList chatList);

//    @Insert
//    void insertContact(Contact contact);

    @Insert
    void insertGroupMem(GroupMem groupMem);

    @Insert
    void insertAppointment(Appointment appointment);

    @Insert
    void insertAppointmentHist(AppointmentHist appointmentHist);

    @Insert
    void insertGMS(GroupMem_Status groupMem_status);

    @Insert
    void insertErrorLog(ErrorLogs errorLogs);

    @Insert
    void insertPushNoti(PushNotification pushNotification);

    @Insert
    void insertReward(Reward reward);

    @Insert
    void insertApptWorkId(ApptWorkUUID apptWorkUUID);

    @Insert
    void insertMediaWorker(MediaWorker mediaWorker);
}
