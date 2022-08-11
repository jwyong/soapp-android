package com.soapp.global.sharing;

import com.soapp.setup.Soapp;
import com.soapp.sql.room.joiners.SharingList;

import java.util.List;
import java.util.Objects;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.ViewModel;

public class SharingTabVM extends ViewModel {

    LiveData<List<SharingList>> list;
    private MediatorLiveData<List<SharingList>> sharinglist;
    private MediatorLiveData<Boolean> isSearching;
    private MediatorLiveData<String> searchString;

    public SharingTabVM() {

        sharinglist = new MediatorLiveData<>();
        isSearching = new MediatorLiveData<>();
        searchString = new MediatorLiveData<>();

        isSearching.setValue(false);
    }

    public void init() {
        list = Soapp.getInstance().getRepository().getSharinglist();

        if (isSearching.getValue()) {
            List<SharingList> searchlist;
//            List<SharingList> searchlist = new ArrayList<>();
            if (searchString.getValue() != null && !searchString.getValue().isEmpty()) {

//                for (SharingList clist : Objects.requireNonNull(list.getValue())
//                        ) {
//                    if (clist.getContactRoster() != null) {
//                        if (clist.getContactRoster().getPhonename() != null &&
//                                clist.getContactRoster().getPhonename().toLowerCase().contains(searchString.getValue())) {
//                            searchlist.add(clist);
//                        }
//                        if (clist.getContactRoster().getDisplayname() != null &&
//                                clist.getContactRoster().getDisplayname().toLowerCase().contains(searchString.getValue())) {
//                            searchlist.add(clist);
//                        }
//                    }
//
//                }
                searchlist = Objects.requireNonNull(Soapp.getDatabase()).selectQuery().searchSharing(searchString.getValue());
                sharinglist.setValue(searchlist);
            }
        } else {
            sharinglist.setValue(list.getValue());
        }

    }


    public MediatorLiveData<List<SharingList>> getSharinglist() {
        return sharinglist;
    }

    public void setSharinglist(MediatorLiveData<List<SharingList>> sharinglist) {
        this.sharinglist = sharinglist;
    }

    public MediatorLiveData<Boolean> getIsSearching() {
        return isSearching;
    }

    public void setIsSearching(boolean isSearching) {
        this.isSearching.setValue(isSearching);
    }

    public MediatorLiveData<String> getSearchString() {
        return searchString;
    }

    public void setSearchString(String searchString) {
        this.searchString.setValue(searchString);
    }

    public void addSource() {
        sharinglist.addSource(list, sharinglist::setValue);
    }

    public void removeSource() {
        list = Soapp.getInstance().getRepository().getSharinglist();
        sharinglist.removeSource(list);
    }
}
