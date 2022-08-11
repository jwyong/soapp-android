package com.soapp.new_chat_schedule.individual;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;

import com.soapp.R;
import com.soapp.base.BaseActivity;
import com.soapp.global.SharingHelper;
import com.soapp.setup.Soapp;
import com.soapp.sql.room.entity.ContactRoster;

import java.util.ArrayList;
import java.util.List;

import androidx.appcompat.widget.SearchView;
import androidx.lifecycle.LiveData;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

/* Created by chang on 05/08/2017. */

public class NewChatController extends BaseActivity implements SearchView.OnQueryTextListener {
    //public statics
    //from for onclick in holder
    public static String from;
    LiveData<List<ContactRoster>> liveDataNewChat;
    //recyclerview
    private RecyclerView new_chatlist_rv;
    private NewChatAdapter newChatAdapter;

    public static long date;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.new_chatlist);
        setupToolbar();

        //set title based on from whr
        from = getIntent().getStringExtra("From");

        switch (from) {
            case "chat":
                setTitle(R.string.new_chat);
                break;

            case "schedule":
                setTitle(R.string.new_indi_appt);
                if (getIntent().hasExtra("Date")) {
                    date = getIntent().getLongExtra("Date", 0);
                }
                break;

            default:
                break;
        }

        //set elements
        new_chatlist_rv = findViewById(R.id.new_chatlist_rv);

        //populate recyclerview
        newChatAdapter = new NewChatAdapter();
        newChatAdapter.setHasStableIds(true);

        LinearLayoutManager llm = new LinearLayoutManager(this);
        llm.setOrientation(RecyclerView.VERTICAL);

        new_chatlist_rv.setLayoutManager(llm);
        new_chatlist_rv.setItemAnimator(null);


        liveDataNewChat = Soapp.getDatabase().selectQuery().getNewIndi();
        liveDataNewChat.observe(this, newChatList -> {
            newChatAdapter.submitList(newChatList);
            newChatAdapter.notifyDataSetChanged();
        });
        new_chatlist_rv.setAdapter(newChatAdapter);

        //invite friends
        LinearLayout invite_friends = findViewById(R.id.invite_friends);
        invite_friends.setOnClickListener(v -> {
            new SharingHelper().shareSoappToFriends(this, findViewById(R.id.progress_bar_ll), v);
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.newchat, menu);

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
                newChatAdapter.submitList(liveDataNewChat.getValue());
            }
        });

        return true;
    }


    //for search bar
    @Override
    public boolean onQueryTextSubmit(String query) {
        return false;
    }

    @Override
    public boolean onQueryTextChange(String newText) {

        if (newText.trim().length() == 0) {
            newChatAdapter.submitList(liveDataNewChat.getValue());
        } else {
            if (liveDataNewChat.getValue() != null) {
                List<ContactRoster> contactRosterList = new ArrayList<>();


                for (ContactRoster cr: liveDataNewChat.getValue()
                ) {
                    if ((cr.getDisplayname() != null && cr.getDisplayname().toLowerCase().contains(newText.toLowerCase()))
                            || (cr.getPhonename() != null && cr.getPhonename().toLowerCase().contains(newText.toLowerCase())) || (cr.getPhonenumber() != null && cr.getPhonenumber().toLowerCase().contains(newText.toLowerCase()))) {
                        contactRosterList.add(cr);
                    }

                }
                newChatAdapter.submitList(contactRosterList);
            }
        }
        return true;
    }
}
