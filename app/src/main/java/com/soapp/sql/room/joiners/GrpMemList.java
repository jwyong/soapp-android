package com.soapp.sql.room.joiners;

import com.soapp.sql.room.entity.ContactRoster;
import com.soapp.sql.room.entity.GroupMem;

import androidx.room.Embedded;

/* Created by ibrahim on 06/04/2018. */

public class GrpMemList {

    @Embedded
    private GroupMem groupMem;
    @Embedded
    private ContactRoster contactRoster;

    public ContactRoster getContactRoster() {
        return contactRoster;
    }

    public void setContactRoster(ContactRoster contactRoster) {
        this.contactRoster = contactRoster;
    }

    public GroupMem getGroupMem() {
        return groupMem;
    }

    public void setGroupMem(GroupMem groupMem) {
        this.groupMem = groupMem;
    }
}
