package com.soapp.global.sharing;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;

import com.google.android.gms.common.util.IOUtils;
import com.soapp.R;
import com.soapp.base.BaseActivity;
import com.soapp.global.SharingHelper;

import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import androidx.appcompat.widget.SearchView;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

/* Created by Soapp on 28/11/2017. */

public class SharingController extends BaseActivity implements SearchView.OnQueryTextListener {
    //for forwarding
    int forwardIssender;
    String forwardMsgData;

    //other holder access
    String resTitle, resID, resImgURL, from, webstring, bookingQR, resLat, resLon,
            bookingDate, bookingTime, pax, promo, bookingid, str_uri;
    private SharingExistAdapter mAdapter;

    //for search bar
    SharingTabVM sharingTabVM;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.new_chatlist);
        setupToolbar();

        RecyclerView mRecyclerView = findViewById(R.id.new_chatlist_rv);
        mAdapter = new SharingExistAdapter();
        mAdapter.setHasStableIds(true);

        LinearLayoutManager llm = new LinearLayoutManager(this);
        llm.setOrientation(RecyclerView.VERTICAL);

        mRecyclerView.setLayoutManager(llm);
        mRecyclerView.setItemAnimator(null);
        mRecyclerView.setNestedScrollingEnabled(false);
        mRecyclerView.setAdapter(mAdapter);

        //testing performance
        mRecyclerView.setItemViewCacheSize(50);
        mRecyclerView.setDrawingCacheEnabled(true);
        mRecyclerView.setDrawingCacheQuality(View.DRAWING_CACHE_QUALITY_LOW);

        //invite friends
        LinearLayout invite_friends = findViewById(R.id.invite_friends);
        invite_friends.setOnClickListener(v -> {
            new SharingHelper().shareSoappToFriends(this, findViewById(R.id.progress_bar_ll), v);
        });

        sharingTabVM = ViewModelProviders.of(this).get(SharingTabVM.class);
        sharingTabVM.init();
        sharingTabVM.addSource();
        sharingTabVM.getSharinglist().observe(this, sharingLists -> mAdapter.submitList(sharingLists));
        sharingTabVM.getIsSearching().observe(this, aBoolean -> sharingTabVM.init());
        sharingTabVM.getSearchString().observe(this, s -> sharingTabVM.init());

        Intent intent = getIntent();
        String action = intent.getAction();
        String type = intent.getType();

        if (action != null) { //external sharing
            switch (action) {
                case Intent.ACTION_SEND: //external single sharing
                    if (type != null) {
                        if (type.startsWith("image/")) { //external img sharing
                            from = "image";
                            //get Uri of image shared from intent
                            Uri uri = intent.getParcelableExtra(Intent.EXTRA_STREAM);
                            str_uri = getFilePathFromURI(this, uri);

                        } else if (type.equals("text/plain")) { //ext url sharing
                            from = "text";
                            //get String of text shared from intent
                            webstring = intent.getStringExtra(Intent.EXTRA_TEXT);

                            //remove last "/" if got
                            if (webstring.substring(webstring.length() - 1).equals("/")) {
                                webstring = StringUtils.substring(webstring, 0, webstring.length() - 1);
                            }

                        }
                    }
                    break;

//                case Intent.ACTION_SEND_MULTIPLE: //external multiple sharing
//                    if (type.startsWith("image/")) {
//                    from = "imageMulti";
                //multi share image yet to do
//                    ArrayList<Uri> imageUris = intent.getParcelableArrayListExtra(Intent.EXTRA_STREAM);
//                    }
//                    break;

                default:
                    break;
            }
        } else { //internal sharing
            intent = getIntent();

            from = getIntent().getStringExtra("from");
            resID = getIntent().getStringExtra("infoid");
            resTitle = getIntent().getStringExtra("restitle");
            resImgURL = getIntent().getStringExtra("image_id");

            //booking/food appt
            bookingid = getIntent().getStringExtra("bookingid");
            bookingQR = getIntent().getStringExtra("bookingQR");
            resLat = getIntent().getStringExtra("resLat");
            resLon = getIntent().getStringExtra("resLon");
            bookingDate = getIntent().getStringExtra("bookingDate");
            bookingTime = getIntent().getStringExtra("bookingTime");
            pax = getIntent().getStringExtra("pax");
            promo = getIntent().getStringExtra("promo");

            switch (from) {
                case "foodAppt": //food share to appt
                    setTitle(R.string.share_to_appt);
                    break;

                case "foodChat": //food share to chat
                    setTitle(R.string.share_to_chat);
                    break;

                case "bookingChat": //food share to chat
                    setTitle(R.string.share_booking);
                    break;

                case "forward": //forward to new chatroom
                    setTitle(R.string.share_forward);

                    //get all variables from intent
                    forwardIssender = getIntent().getIntExtra("forwardIssender", 0);
                    forwardMsgData = getIntent().getStringExtra("forwardMsgData");
                    break;

                default:
                    setTitle(R.string.share_with);
                    break;
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.sharing, menu);

        MenuItem search = menu.findItem(R.id.action_sharing_search);
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
                //clear results
                sharingTabVM.setIsSearching(false);
            }
        });

        return true;
    }

    //for searching
    @Override
    public boolean onQueryTextChange(String newText) {
        if (newText.trim().length() == 0) { //no input
            //reset back to show original list
            sharingTabVM.setIsSearching(false);
        } else { //got input
            sharingTabVM.removeSource();
            sharingTabVM.setIsSearching(true);
            sharingTabVM.setSearchString(newText.toLowerCase());
        }
        return false;
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        sharingTabVM.setIsSearching(true);
        sharingTabVM.setSearchString(query.toLowerCase());
        return false;
    }

    public String getFilePathFromURI(Context context, Uri contentUri) {
        //copy file and send new file path
        String fileName = getFileName(contentUri);
        if (!TextUtils.isEmpty(fileName)) {
            File rootDataDir = context.getFilesDir();
            File copyFile = new File(rootDataDir + File.separator + fileName + ".jpg");
//            File copyFile = new File(getCacheDir() + File.separator + fileName);
            copy(context, contentUri, copyFile);
            return copyFile.getAbsolutePath();
        }
        return null;
    }

    public String getFileName(Uri uri) {
        if (uri == null) return null;
        String fileName = null;
        String path = uri.getPath();
        int cut = path.lastIndexOf('/');
        if (cut != -1) {
            fileName = path.substring(cut + 1);
        }
        return fileName;
    }

    public void copy(Context context, Uri srcUri, File dstFile) {
        try {
            InputStream inputStream = context.getContentResolver().openInputStream(srcUri);
            if (inputStream == null) return;
            OutputStream outputStream = new FileOutputStream(dstFile);
            IOUtils.copyStream(inputStream, outputStream);
            inputStream.close();
            outputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}