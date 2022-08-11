package com.soapp.new_chat_schedule.group;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;

import com.soapp.R;
import com.soapp.base.BaseActivity;
import com.soapp.global.SharingHelper;
import com.soapp.global.UIHelper;
import com.soapp.new_chat_schedule.group.create_grp.NewCreateGroupController;
import com.soapp.schedule_class.new_appt.NewGrpAppt.NewGrpApptActivity;
import com.soapp.setup.Soapp;
import com.soapp.sql.DatabaseHelper;
import com.soapp.sql.room.entity.ContactRoster;
import com.soapp.sql.room.joiners.ChatTabList;

import java.util.List;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

/* Created by chang on 30/07/2017. */

public class NewGrpChatController extends BaseActivity implements SearchView.OnQueryTextListener {
    LiveData<List<ContactRoster>> addGrpList;
    LiveData<List<ChatTabList>> addGrpListExist;
    //basics
    private DatabaseHelper databaseHelper = DatabaseHelper.getInstance();
    private UIHelper uiHelper = new UIHelper();
    //recyclerview (new grp)
    private RecyclerView new_chatlist_rv;
    private NewGrpChatAdapter newGrpChatAdapter;
    //recyclerview (existing grps)
    private ExistingGrpAdapter existingGrpAdapter;

    //variables
    private boolean gotSelected = false;
    private String from;
    //for search bar
    private boolean isSearching;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.new_chatlist);
        setupToolbar();

        //clear all selected grp mems in CR
        databaseHelper.clearCRSelected();

        //declare elements
        new_chatlist_rv = findViewById(R.id.new_chatlist_rv);
        isSearching = false;

        //set title and load rv based on intent
        from = getIntent().getStringExtra("From");
        switch (from) {
            case "groupchat":
                setTitle(R.string.new_g_chat);

                //create new grp by selecting members from CR
                loadRVNewGrp();
                break;

            case "schedule":
                setTitle(R.string.new_g_appt);

                //create new grp by selecting members from CR
                loadRVNewGrp();
                break;

            case "existing":
                setTitle(R.string.exist_g_schedule);

                //create new appt from existing grps
                loadRVExistGrp();
                break;

            default:
                break;
        }

        //invite friends
        LinearLayout invite_friends = findViewById(R.id.invite_friends);
        invite_friends.setOnClickListener(v -> {
            new SharingHelper().shareSoappToFriends(this, findViewById(R.id.progress_bar_ll), v);
        });
    }

    //function for populating recyclerview for new grp
    private void loadRVNewGrp() {
        //populate recyclerview
        newGrpChatAdapter = new NewGrpChatAdapter();
        newGrpChatAdapter.setHasStableIds(true);

        LinearLayoutManager llmImg = new LinearLayoutManager(this);
        llmImg.setOrientation(LinearLayoutManager.VERTICAL);

        new_chatlist_rv.setLayoutManager(llmImg);
        new_chatlist_rv.setItemAnimator(null);

        addGrpList = Soapp.getDatabase().selectQuery().getNewGroup();
        addGrpList.observe(this, newGrpList -> {
            if (!isSearching) {
                newGrpChatAdapter.submitList(newGrpList);
            }

            gotSelected = false;
            for (ContactRoster list : newGrpList) {
                if (list.getSelected() != null && list.getSelected() == 1) {
                    //set gotSelected to true if got at least 1 member selected
                    gotSelected = true;
                    break;
                }
            }
        });
        new_chatlist_rv.setAdapter(newGrpChatAdapter);
    }

    //function for populating recyclerview for existing grps
    private void loadRVExistGrp() {
        //populate recyclerview
        existingGrpAdapter = new ExistingGrpAdapter();
        existingGrpAdapter.setHasStableIds(true);

        LinearLayoutManager llm = new LinearLayoutManager(this);
        llm.setOrientation(LinearLayoutManager.VERTICAL);

        new_chatlist_rv.setLayoutManager(llm);
        new_chatlist_rv.setItemAnimator(null);


        addGrpListExist = Soapp.getDatabase().selectQuery().getExistGroup();
        addGrpListExist.observe(this, existGrpList -> {
            if (!isSearching) {
                existingGrpAdapter.submitList(existGrpList);
            }
        });
        new_chatlist_rv.setAdapter(existingGrpAdapter);
    }

    //finish current activity if next screen click ok
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 288) {
            if (resultCode == Activity.RESULT_OK) {
                finish();
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.new_g_chat, menu);

        MenuItem search = menu.findItem(R.id.action_newchat_search);
        SearchView searchView = (SearchView) search.getActionView();

        search.setOnMenuItemClickListener(item -> {
            searchView.setIconified(false);
            return false;
        });

        searchView.setOnQueryTextListener(this);

        //for when searchbar open/closed
        searchView.addOnAttachStateChangeListener(new View.OnAttachStateChangeListener() {
            @Override
            public void onViewAttachedToWindow(View v) { //opened
            }

            @Override
            public void onViewDetachedFromWindow(View v) { //closed
                isSearching = false;

                //show different list based on from
                if (addGrpList != null) {
                    newGrpChatAdapter.submitList(addGrpList.getValue());
                } else {
                    existingGrpAdapter.submitList(addGrpListExist.getValue());
                }
//                switch (from) {
//                    case "groupchat":
//                    case "schedule":
//                        newGrpChatAdapter.submitList(addGrpList.getValue());
//                        break;
//
//                    case "existing":
//                        existingGrpAdapter.submitList(addGrpListExist.getValue());
//                        break;
//
//                    default:
//                        break;
//                }
            }
        });
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case (R.id.next_btn):
                if (!gotSelected) { //no member selected
                    uiHelper.dialog1Btn(this, getString(R.string.grp_no_member),
                            getString(R.string.grp_no_member_msg), R.string.ok_label, R.color.black,
                            null, true, false);
                } else {
                    Intent intent;
                    switch (from) {
                        case "schedule": //go to new grp appt activity, DON'T create grp yet
                            long apptDate = getIntent().getLongExtra("Date", 0);

                            intent = new Intent(this, NewGrpApptActivity.class);

                            intent.putExtra("Date", apptDate);

                            startActivityForResult(intent, 288);
                            break;

                        default: //from "groupchat", go to create grp directly
                            intent = new Intent(this, NewCreateGroupController.class);
                            intent.putExtra("from", from);

                            startActivityForResult(intent, 288);
                            break;
                    }
                }
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    //for search bar
    @Override
    public boolean onQueryTextSubmit(String query) {
        return false;
    }

    @Override
    public boolean onQueryTextChange(String newText) {

        switch (from) {
            case "groupchat":
                if (newText.trim().length() == 0) {
                    isSearching = false;
                    newGrpChatAdapter.submitList(addGrpList.getValue());

                } else {
                    isSearching = true;
                    LiveData<List<ContactRoster>> crlist = Soapp.getDatabase().selectQuery().searchNewGroup(newText);
                    crlist.observe(this, new Observer<List<ContactRoster>>() {
                        @Override
                        public void onChanged(@Nullable List<ContactRoster> contactRosters) {
                            newGrpChatAdapter.submitList(contactRosters);
                        }
                    });

                }
                break;

            case "schedule":
                if (newText.trim().length() == 0) {
                    isSearching = false;
                    newGrpChatAdapter.submitList(addGrpList.getValue());
                } else {
                    isSearching = true;
                    LiveData<List<ContactRoster>> crlist = Soapp.getDatabase().selectQuery().searchNewGroup(newText);
                    crlist.observe(this, new Observer<List<ContactRoster>>() {
                        @Override
                        public void onChanged(@Nullable List<ContactRoster> contactRosters) {
                            newGrpChatAdapter.submitList(contactRosters);
                        }
                    });

                }
                break;

            case "existing":
                if (newText.trim().length() == 0) {
                    isSearching = false;
                    existingGrpAdapter.submitList(addGrpListExist.getValue());
                } else {
                    isSearching = true;
                    List<ChatTabList> chatTabLists = Soapp.getDatabase().selectQuery().searchExistGroup(newText);
                    existingGrpAdapter.submitList(chatTabLists);
                }
                break;

            default:
                break;
        }

        return false;
    }
}
