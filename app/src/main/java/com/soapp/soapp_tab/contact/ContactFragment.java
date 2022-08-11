package com.soapp.soapp_tab.contact;

import android.app.Activity;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.provider.ContactsContract;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.soapp.R;
import com.soapp.global.MiscHelper;
import com.soapp.global.Preferences;
import com.soapp.global.SyncHelper;
import com.soapp.global.UIHelper;
import com.soapp.sql.DatabaseHelper;
import com.soapp.xmpp.PubsubHelper.PubsubNodeCall;
import com.soapp.xmpp.SingleChatHelper.SingleChatStanza;

import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.loader.app.LoaderManager;
import androidx.loader.content.CursorLoader;
import androidx.loader.content.Loader;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class ContactFragment extends Fragment implements SearchView.OnQueryTextListener,
        LoaderManager.LoaderCallbacks<Cursor> {
    static final String[] CONTACTS_SUMMARY_PROJECTION = new String[]{
            ContactsContract.CommonDataKinds.Phone.CONTACT_ID,
            ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
    };
    public static DatabaseHelper databaseHelper = DatabaseHelper.getInstance();
    RecyclerView mRecycleView;
    ContactAdapter mAdapter;
    Cursor data;
    View view;
    String numbers = "1,";
    int contacts, j = 0, k = 0;
    //count contacts
    int r = 0;
    PubsubNodeCall pubsubNodeCall = new PubsubNodeCall();
    SingleChatStanza singleChatStanza = new SingleChatStanza();

    //array list for adding in phone numbers
    private RecyclerView.LayoutManager mLayoutManager;
    private Preferences preferences = null;
    //for search bar
    private String queryString;
    private Bundle searchBundle = new Bundle();
    private Handler handler = new Handler();
    private MiscHelper miscHelper = new MiscHelper();
    private UIHelper uiHelper = new UIHelper();
    private Activity activity;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        activity = getActivity();
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle
            savedInstanceState) {
        view = inflater.inflate(R.layout.contact_list, container, false);

        mRecycleView = view.findViewById(R.id.lv_contact);
        mLayoutManager = new LinearLayoutManager(getActivity().getApplicationContext());
        mRecycleView.setLayoutManager(mLayoutManager);
        mAdapter = new ContactAdapter(getActivity().getApplicationContext(), data);
        mRecycleView.setAdapter(mAdapter);
        getLoaderManager().initLoader(0,
                null, this);

        return view;
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        switch (id) {
            case 0:
                return new CursorLoader(getContext(), ContactsContract
                        .CommonDataKinds.Phone.CONTENT_URI, CONTACTS_SUMMARY_PROJECTION, null, null,
                        ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME);

            case 10:
                return new CursorLoader(getContext(), ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                        CONTACTS_SUMMARY_PROJECTION, ContactsContract.CommonDataKinds.Phone
                        .DISPLAY_NAME + " LIKE ?", args.getStringArray("query"),
                        ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME);

            default:
                return null;
        }
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        mAdapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater = activity.getMenuInflater();
        inflater.inflate(R.menu.contact, menu);

        MenuItem search = menu.findItem(R.id.action_contact_search);

        SearchView searchView = (SearchView) search.getActionView();
        searchView.setOnQueryTextListener(this);

        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_refresh:

                //action for allow notification
                Runnable pushNotificationPositive = () -> {
                    new SyncHelper(getContext(), "normalSync").new AsyncContactbtn().execute();
                };

                uiHelper.dialog2Btns(getActivity(),
                        getString(R.string.sync_contacts_title),
                        getString(R.string.sync_contacts_msg),
                        R.string.ok_label,
                        R.string.cancel,
                        R.color.white,
                        R.color.primaryDark3,
                        pushNotificationPositive,
                        null,
                        true);
                break;
            default:
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
    public boolean onQueryTextChange(final String newText) {
        queryString = newText;
        handler.removeCallbacksAndMessages(null);

        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (newText.length() == 0) { //clear search results
                    getLoaderManager().restartLoader(0, null, ContactFragment.this);
                } else { //show search results
                    searchBundle.putStringArray("query", new String[]{"%" + queryString + "%"});

                    getLoaderManager().restartLoader(10, searchBundle, ContactFragment.this);
                }
            }
        }, 300);

        return true;
    }
}