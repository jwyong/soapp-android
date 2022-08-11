package com.soapp.chat_class.group_add_member;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.soapp.R;
import com.soapp.SoappApi.ApiModel.AddRoomMemberBulkModel;
import com.soapp.SoappApi.Interface.AddGroupMemberBulk;
import com.soapp.SoappApi.RetrofitAPIClient;
import com.soapp.base.BaseActivity;
import com.soapp.global.GlobalVariables;
import com.soapp.global.MiscHelper;
import com.soapp.global.Preferences;
import com.soapp.global.UIHelper;
import com.soapp.new_chat_schedule.group.create_grp.NewCreateGroupController;
import com.soapp.setup.Soapp;
import com.soapp.sql.DatabaseHelper;
import com.soapp.sql.room.entity.ContactRoster;
import com.soapp.xmpp.SmackHelper;

import java.util.List;
import java.util.UUID;

import androidx.appcompat.widget.SearchView;
import androidx.lifecycle.LiveData;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

/* Created by chang on 27/08/2017. */

public class AddListController extends BaseActivity implements SearchView.OnQueryTextListener {
    //basics
    private DatabaseHelper databaseHelper = DatabaseHelper.getInstance();
    private UIHelper uiHelper = new UIHelper();

    //recyclerview
    private RecyclerView add_list_rv;
    private AddListAdapter addListAdapter;

    //variables
    private String room_id, from;
    private boolean gotSelected;
    private LiveData<List<ContactRoster>> liveDataAddGrp;

    //for search bar
    private boolean isSearching;
    private Bundle searchBundle = new Bundle();
    private Handler handler = new Handler();
    private String queryString;

    //others
    private static ProgressDialog addListDialog;
    public static int profileSize;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.chat_grp_add_list);
        setupToolbar();

        room_id = getIntent().getStringExtra("jid");
        from = getIntent().getStringExtra("from");

        profileSize = new MiscHelper().getPixelSizeforChattabProfile(this,60);

        //clear all selected grp mems in CR
        databaseHelper.clearCRSelected();

        //set title and show list based on coming from whr

        if (from.equals("addMember")) { //coming from add member
            setTitle(R.string.add_member_title);
            liveDataAddGrp = Soapp.getDatabase().selectQuery().getNewGroup();
        } else { //from grp in grp
            setTitle(R.string.create_grpInGrp_title);
            liveDataAddGrp = Soapp.getDatabase().selectQuery().getGroupInGroup(room_id);
        }

        //declare elements
        add_list_rv = findViewById(R.id.add_list_rv);
        isSearching = false;

        //populate recyclerview
        addListAdapter = new AddListAdapter();
        addListAdapter.setHasStableIds(true);

        LinearLayoutManager llmImg = new LinearLayoutManager(this);
        llmImg.setOrientation(LinearLayoutManager.VERTICAL);

        add_list_rv.setLayoutManager(llmImg);
        add_list_rv.setItemAnimator(null);

        liveDataAddGrp.observe(this, addGrpList -> {
            if (!isSearching) { //not searching, show actual list
                addListAdapter.submitList(addGrpList);

                //check if got member selected
                gotSelected = false;
                for (ContactRoster list : addGrpList) {
                    if (list.getSelected() != null && list.getSelected() == 1) {
                        //set gotSelected to true if got at least 1 member selected
                        gotSelected = true;
                        break;
                    }
                }
            } else { //show latest filtered list

            }
        });
        add_list_rv.setAdapter(addListAdapter);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.new_g_chat, menu);

        //search btn
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

                //clear results
                addListAdapter.submitList(liveDataAddGrp.getValue());
            }
        });

        //next btn
        MenuItem next_btn = menu.findItem(R.id.next_btn);
        next_btn.setOnMenuItemClickListener(item -> {
            if (!gotSelected) { //no member selected
                uiHelper.dialog1Btn(AddListController.this, getString(R.string.grp_no_member),
                        getString(R.string.grp_no_member_msg), R.string.ok_label, R.color.black,
                        null, true, false);
            } else {
                if (from.equals("addMember")) { //add new member
                    addMemberToGrp();
                } else { //grp in grp
                    Intent IntentMain = new Intent(AddListController.this, NewCreateGroupController.class);
                    IntentMain.putExtra("from", from);

                    startActivityForResult(IntentMain, 288);
                }
            }
            return false;
        });

        return true;
    }

    //finish current activity if next screen click ok
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 288) {
            if (resultCode == Activity.RESULT_OK) {
                finish();
            }
        }
    }

    //function for add member to grp
    private void addMemberToGrp() {
        //get selected users' jids from sqlite
        final List<String> jidList = databaseHelper.get_jidStatus1FromCRGrpMem(room_id);

        if (jidList.size() > 0) { //only proceed if list not 0
            if (SmackHelper.getXMPPConnection() != null && SmackHelper.getXMPPConnection().isAuthenticated()) {
                addListDialog = ProgressDialog.show(AddListController.this, null, getString(R.string.adding_members));

                Preferences preferences = Preferences.getInstance();
                final String user_id = preferences.getValue(Soapp.getInstance().getApplicationContext(), GlobalVariables.STRPREF_USER_ID);
                final String selfUserName = preferences.getValue(Soapp.getInstance().getApplicationContext(), GlobalVariables.STRPREF_USERNAME);
                String uniqueID = UUID.randomUUID().toString();
                AddRoomMemberBulkModel model = new AddRoomMemberBulkModel(room_id, jidList, uniqueID, GlobalVariables.pubsubHost, GlobalVariables.xmppHost, "ANDROID");
                String access_token = preferences.getValue(Soapp.getInstance().getApplicationContext(), GlobalVariables.STRPREF_ACCESS_TOKEN);

                //build retrofit
                AddGroupMemberBulk client = RetrofitAPIClient.getClient().create(AddGroupMemberBulk.class);
                retrofit2.Call<AddRoomMemberBulkModel> call2 = client.addMemberApi(model, "Bearer " + access_token);
                call2.enqueue(new retrofit2.Callback<AddRoomMemberBulkModel>() {
                    @Override
                    public void onResponse(retrofit2.Call<AddRoomMemberBulkModel> call2, retrofit2.Response<AddRoomMemberBulkModel> response) {
                        if (!response.isSuccessful()) {
                            new MiscHelper().retroLogUnsuc(response, "", "JAY");

                            Toast.makeText(AddListController.this, R.string.onresponse_unsuccessful, Toast.LENGTH_SHORT).show();
                            if (addListDialog != null && addListDialog.isShowing()) {
                                addListDialog.dismiss();
                            }
                            return;
                        }
                        //loop added members into sqlite
                        String phoneName = "", memberJid, apptID;
                        String color;
                        List<String> apptIDList;

                        for (int i = 0; i < jidList.size(); i++) {
                            memberJid = jidList.get(i);
                            color = MiscHelper.getColorForGrpDisplayName(i);

                            //add memberjid to grpMem
                            databaseHelper.checkAndAddJidToGrpMem(memberJid, room_id, "member", color);

                            //loop appts in grp
                            apptIDList = databaseHelper.get_apptIDFromRoom(room_id);
                            for (int j = 0; j < apptIDList.size(); j++) {
                                //add memberjid and appt status 2 to GMS
                                apptID = apptIDList.get(j);
                                databaseHelper.checkAndAddStatusToGrpMemStatus(memberJid, room_id, apptID, 2);
                            }

                            //pubsub subscribe newly added members so that they can make new grp
                            String pushMsg = getString(R.string.grp_added);

                            //get member profile from sqlite
                            phoneName = databaseHelper.getNameFromContactRoster(memberJid);
                        }

                        if (jidList.size() > 1) { //added multiple members
                            databaseHelper.groupStatusMsgPlaySound(room_id, getString(R.string.you), getString(R.string.multi_member_self_added), false, false);
                        } else { //added 1 member
                            databaseHelper.groupStatusMsgPlaySound(room_id, getString(R.string.you),getString(R.string.single_member_self_added) + " " + phoneName, false, false);
                        }

                        if (addListDialog != null && addListDialog.isShowing()) {
                            addListDialog.dismiss();
                        }
                        finish();
                    }

                    @Override
                    public void onFailure(retrofit2.Call<AddRoomMemberBulkModel> call2, Throwable t) {
                        new MiscHelper().retroLogFailure(t, "addMemberToGrp", "JAY");
                        Toast.makeText(AddListController.this, R.string.onfailure,
                                Toast.LENGTH_SHORT).show();
                        if (addListDialog != null && addListDialog.isShowing()) {
                            addListDialog.dismiss();
                        }
                    }
                });
            } else {
                Toast.makeText(AddListController.this, R.string.xmpp_waiting_connection, Toast.LENGTH_SHORT).show();
            }
        } else { //toast user that members already exist in group
            Toast.makeText(AddListController.this, R.string.exist_member_added, Toast.LENGTH_SHORT).show();
        }
    }

    //for search bar
    @Override
    public boolean onQueryTextSubmit(String query) {
        return false;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        LiveData<List<ContactRoster>> searchlist;
        if (newText.trim().length() == 0) {
            addListAdapter.submitList(liveDataAddGrp.getValue());
        } else {
            isSearching = true;

            if (from.equals("addMember")) { //coming from add member
                searchlist = Soapp.getDatabase().selectQuery().searchNewGroup(newText);

            } else { //from grp in grp
                searchlist = Soapp.getDatabase().selectQuery().searchGroupInGroup(newText, room_id);
            }
            searchlist.observe(this, contactRosters -> {
                addListAdapter.submitList(contactRosters);

                gotSelected = false;
                for (ContactRoster list : searchlist.getValue()) {
                    if (list.getSelected() != null && list.getSelected() == 1) {
                        //set gotSelected to true if got at least 1 member selected
                        gotSelected = true;
                        break;
                    }
                }
            });
        }
        return true;
    }
}
