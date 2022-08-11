package com.soapp.chat_class.Chatlist;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SearchView;
import android.widget.TextView;

import com.soapp.R;
import com.soapp.global.HomeTabHelper;
import com.soapp.global.MiscHelper;
import com.soapp.global.Preferences;
import com.soapp.global.UIHelper;
import com.soapp.home.Home;
import com.soapp.sql.DatabaseHelper;

import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

/* Created by chang on 24/07/2017. */

public class ChatTab extends Fragment implements Home.ChatTabComm, SearchView.OnQueryTextListener,
        SearchView.OnCloseListener, View.OnClickListener {
    //basics
    private UIHelper uiHelper = new UIHelper();
    private Context context;
    private View view;
    private Preferences preferences = Preferences.getInstance();

    //elements
    private LinearLayout start_new_chat;
    private TextView start_new_chat_msg;
    private ImageView image_chat_logo, scroll_top;

    //for search bar
    private SearchView search_button;

    //recyclerview
    private RecyclerView mRecyclerView;
    private ChatAdapter mAdapter;
    private LinearLayoutManager llm;

    //livedata
    private ChatTabVM chatTabVM;

    //for recyclerview scrolling
    private boolean isFirstTime = true;

    //glide profile size
    public static int profileSize;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        context = this.getContext();

        profileSize = new MiscHelper().getPixelSizeforChattabProfile(context, 50);
        mAdapter = new ChatAdapter();

        chatTabVM = ViewModelProviders.of(this).get(ChatTabVM.class);
        chatTabVM.init();
        chatTabVM.addChatListSource();
        chatTabVM.getChatTabList().observe(this, chatTabLists -> {
            if (chatTabLists != null) {
                mAdapter.submitList(chatTabLists);

                if (chatTabLists.size() == 0) { //no items to show
                    if (chatTabVM.getIsSearching().getValue()) { //user is searching
                        //show UI for no search results
                        start_new_chat.setVisibility(View.VISIBLE);
                        start_new_chat.setEnabled(false);
                        start_new_chat_msg.setText(R.string.no_search_results);
                    } else {
                        //show UI for start new chat
                        start_new_chat.setVisibility(View.VISIBLE);
                        start_new_chat.setOnClickListener(ChatTab.this);
                        start_new_chat.setEnabled(true);
                        start_new_chat_msg.setText(R.string.start_new_chat);
                    }
                } else { //got items to show
                    start_new_chat.setVisibility(View.GONE);
                }

                //first time scroll to top
                if (isFirstTime) {
                    mRecyclerView.scrollToPosition(0);
                    isFirstTime = false;
                } else {
                    if (llm.findFirstVisibleItemPosition() == 0) { //only scroll to top if user is scrolled to top
                        if (search_button.isIconified()) { //only smooth scroll when NOT searching
                            mRecyclerView.smoothScrollToPosition(-10);
                        }
                    }
                }
            }
        });
        chatTabVM.getStringLiveData().observe(this, s -> chatTabVM.init());
        chatTabVM.getIsSearching().observe(this, aBoolean -> chatTabVM.init());
    }

    // initialise chattab searchbar close function
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        ((Home) context).chatTabComm = this;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle
            savedInstanceState) {
        if (view == null) {
            view = inflater.inflate(R.layout.chatlist, container, false);

            // setting the statusbar
            ImageView statusBar = view.findViewById(R.id.statusBar);
            ConstraintLayout.LayoutParams statusParams = new ConstraintLayout.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            uiHelper.getStatusBarHeight(getActivity()));
            statusBar.setLayoutParams(statusParams);
            //** end setting statusbar **//

            //reset seeing_current_chat
            preferences.save(getContext(), "seeing_current_chat", "");

            mRecyclerView = view.findViewById(R.id.chat_list_rv);
            start_new_chat = view.findViewById(R.id.start_new_chat);
            start_new_chat_msg = view.findViewById(R.id.start_new_chat_msg);
            CardView new_chat_btn = view.findViewById(R.id.new_chat_btn);
            search_button = view.findViewById(R.id.search_button);
            image_chat_logo = view.findViewById(R.id.image_chat_logo);
            scroll_top = view.findViewById(R.id.scroll_top);

            scroll_top.setOnClickListener(this);
            new_chat_btn.setOnClickListener(this);
            search_button.setOnSearchClickListener(this);
            search_button.setOnCloseListener(this);

//            uiHelper.statusBarBackground(getActivity(), view);

            //attach adapter to recycler view if madapter not null
            llm = new LinearLayoutManager(context);
            llm.setOrientation(RecyclerView.VERTICAL);

            mAdapter.setHasStableIds(true);

            mRecyclerView.setLayoutManager(llm);
            mRecyclerView.setItemAnimator(null);
            mRecyclerView.setAdapter(mAdapter);

            mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
                @Override
                public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                    if (llm.findLastVisibleItemPosition() > 10) {
                        scroll_top.setVisibility(View.VISIBLE);
                    } else {
                        scroll_top.setVisibility(View.GONE);
                    }
                }
            });

//            mRecyclerView.setItemViewCacheSize(10);
        }
        return view;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.scroll_top:
                mRecyclerView.scrollToPosition(0);
                break;

            case (R.id.start_new_chat):
            case (R.id.new_chat_btn):
                //action for new chat
                new HomeTabHelper().startNewChat(getContext());
                break;

            case (R.id.search_button):
                image_chat_logo.setVisibility(View.GONE);
                search_button.setOnQueryTextListener(this);
                break;
        }
    }

    //for search bar
    @Override
    public boolean onQueryTextChange(String newText) {
        if (newText.trim().length() == 0) { //no input
            //reset back to show original list
            chatTabVM.setIsSearching(false);

        } else { //got input
            chatTabVM.removeChatListSource();
            chatTabVM.setIsSearching(true);
            chatTabVM.setStringLiveData(newText.toLowerCase());
        }
        return false;
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        chatTabVM.setIsSearching(true);
        chatTabVM.setStringLiveData(query.toLowerCase());

        return false;
    }

    @Override
    public boolean onClose() {
        image_chat_logo.setVisibility(View.VISIBLE);
        chatTabVM.setIsSearching(false);
        chatTabVM.addChatListSource();

        llm.scrollToPosition(0);
        return false;
    }

    @Override
    public boolean isChatSearchIconified() {
        if (search_button != null) {
            return search_button.isIconified();
        } else { //return true (iconified = not searching) since no searchView
            return true;
        }
    }

    @Override
    public void closeChatTabSearch() {
        search_button.setIconified(true);
        search_button.clearFocus();
    }

    //clear "typing" from all rows in CL on destroy
    @Override
    public void onDestroy() {
        DatabaseHelper.getInstance().chatStatusClear();

        super.onDestroy();
    }
}