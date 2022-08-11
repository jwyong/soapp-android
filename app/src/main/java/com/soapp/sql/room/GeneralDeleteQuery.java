package com.soapp.sql.room;

import com.soapp.sql.room.entity.Appointment;
import com.soapp.sql.room.entity.ChatList;
import com.soapp.sql.room.entity.ContactRoster;
import com.soapp.sql.room.entity.GroupMem;
import com.soapp.sql.room.entity.GroupMem_Status;
import com.soapp.sql.room.entity.Message;
import com.soapp.sql.room.entity.Restaurant;

import androidx.room.Dao;
import androidx.room.Delete;

/* Created by ibrahim on 29/03/2018. */

@Dao
public interface GeneralDeleteQuery {

    @Delete
    void deleteFavourites(Restaurant restaurant);

    @Delete
    void deleteResBookings(Restaurant restaurant);

    @Delete
    void deleteContactRoster(ContactRoster contactRoster);

    @Delete
    void deleteMessage(Message message);

    @Delete
    int deleteChatList(ChatList chatList);

//    @Delete
//    void deleteContact(Contact contact);

    @Delete
    void deleteGroupMem(GroupMem groupMem);

    @Delete
    int deleteGroupMemStatus(GroupMem_Status groupMem_status);

    @Delete
    void deleteAppointment(Appointment appointment);
}
