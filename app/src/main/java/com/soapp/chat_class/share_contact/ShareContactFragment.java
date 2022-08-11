package com.soapp.chat_class.share_contact;

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

import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.loader.app.LoaderManager;
import androidx.loader.content.CursorLoader;
import androidx.loader.content.Loader;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class ShareContactFragment extends Fragment implements SearchView.OnQueryTextListener,
        LoaderManager.LoaderCallbacks<Cursor> {
    static final String[] CONTACTS_SUMMARY_PROJECTION = new String[]{
            ContactsContract.CommonDataKinds.Phone.CONTACT_ID,
            ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
    };
    RecyclerView mRecycleView;
    ShareContactAdapter mAdapter;
    Cursor data;
    View view;

    //for search bar
    private String queryString;
    private Bundle searchBundle = new Bundle();
    private Handler handler = new Handler();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle
            savedInstanceState) {
        view = inflater.inflate(R.layout.contact_list, container, false);
        mRecycleView = view.findViewById(R.id.lv_contact);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getContext());
        mRecycleView.setLayoutManager(mLayoutManager);
        mAdapter = new ShareContactAdapter(getContext(), data);
        mRecycleView.setAdapter(mAdapter);
        getLoaderManager().initLoader(0, null, this);

        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

//    @Override
//    public void onPrepareOptionsMenu(Menu menu) {
//        super.onPrepareOptionsMenu(menu);
//    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        switch (id) {
            case 0:
                return new CursorLoader(getActivity().getApplicationContext(), ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                        CONTACTS_SUMMARY_PROJECTION, null, null, ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME);

            case 10:
                return new CursorLoader(getActivity().getApplicationContext(), ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
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
        inflater = getActivity().getMenuInflater();
        inflater.inflate(R.menu.sharing, menu);

        MenuItem search = menu.findItem(R.id.action_sharing_search);
        SearchView searchView = (SearchView) search.getActionView();
        searchView.setOnQueryTextListener(this);

        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
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
//        getActivity().getWindow().setSoftInputMode(WindowManager
//                .LayoutParams.SOFT_INPUT_STATE_VISIBLE | WindowManager
//                .LayoutParams.SOFT_INPUT_ADJUST_RESIZE);

        queryString = newText;
        handler.removeCallbacksAndMessages(null);

        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (newText.length() == 0) { //clear search results
                    getLoaderManager().restartLoader(0, null, ShareContactFragment.this);
                } else { //show search results
                    searchBundle.putStringArray("query", new String[]{"%" + queryString + "%"});

                    //push view up when click on search bar
//                    ShareContactActivity.activity.getWindow().setSoftInputMode(WindowManager
//                            .LayoutParams.SOFT_INPUT_STATE_VISIBLE | WindowManager
//                            .LayoutParams.SOFT_INPUT_ADJUST_RESIZE);

                    getLoaderManager().restartLoader(10, searchBundle, ShareContactFragment.this);
                }
            }
        }, 300);

        return true;
    }
}
