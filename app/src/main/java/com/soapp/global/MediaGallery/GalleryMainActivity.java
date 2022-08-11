package com.soapp.global.MediaGallery;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.tabs.TabLayout;
import com.soapp.R;
import com.soapp.base.BaseActivity;
import com.soapp.global.MediaGallery.Fragments.ImagesFragment;
import com.soapp.global.MediaGallery.Fragments.VideosFragment;

import java.util.ArrayList;
import java.util.List;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

//import com.erikagtierrez.multiple_media_picker.R;

public class GalleryMainActivity extends BaseActivity {
    private int mode;

    public static Activity activity;
    public static String from;
    public static String jidStr;

    //for devOps video
//    private int devEnableVideo = 0;
//    private Preferences preferences = Preferences.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.gallery_activity_gallery);
        setupToolbar();

        //devOps - click 10 times to enable video
//        if (!preferences.getValue(this, "devEnableVideo").equals("1")) {
//            Toolbar toolbar = findViewById(R.id.toolbar);
//
//            toolbar.setOnClickListener(v -> {
//                if (devEnableVideo == 9) {
//                    Toast.makeText(GalleryMainActivity.this, "Video-sending enabled", Toast.LENGTH_SHORT).show();
//
//                    preferences.save(GalleryMainActivity.this, "devEnableVideo", "1");
//                } else {
//                    devEnableVideo++;
//                }
//            });
//        }

        activity = this;
        Intent in = getIntent();
        if (in.hasExtra("from")) {
            from = in.getStringExtra("from");
        }
        if (in.hasExtra("jid")) {
            jidStr = in.getStringExtra("jid");
        }

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(view -> {
//                returnResult();
        });

        fab.setVisibility(View.GONE);
        int maxSelection = getIntent().getExtras().getInt("maxSelection");
        if (maxSelection == 0) maxSelection = Integer.MAX_VALUE;
        mode = getIntent().getExtras().getInt("mode");

        ViewPager viewPager = findViewById(R.id.viewpager);
        setupViewPager(viewPager);
        TabLayout tabLayout = findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(viewPager);
//        OpenGallery.selected.clear();
//        OpenGallery.imagesSelected.clear();

    }

    //This method set up the tab view for images and videos
    private void setupViewPager(ViewPager viewPager) {
        ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager());
        if (mode == 1 || mode == 2) {
            adapter.addFragment(new ImagesFragment(), "IMAGES");
        }

        //devOps - disable videos sending
//        if (preferences.getValue(this, "devEnableVideo").equals("1")) {
            if (mode == 1 || mode == 3) {
                adapter.addFragment(new VideosFragment(), "Videos");
            }
//        }
        viewPager.setAdapter(adapter);
    }

    class ViewPagerAdapter extends FragmentPagerAdapter {
        private final List<Fragment> mFragmentList = new ArrayList<>();
        private final List<String> mFragmentTitleList = new ArrayList<>();

        public ViewPagerAdapter(FragmentManager manager) {
            super(manager);
        }

        @Override
        public Fragment getItem(int position) {
            return mFragmentList.get(position);
        }

        @Override
        public int getCount() {


            return mFragmentList.size();
        }

        public void addFragment(Fragment fragment, String title) {
            mFragmentList.add(fragment);
            mFragmentTitleList.add(title);
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return mFragmentTitleList.get(position);
        }

    }

    @Override
    protected void onDestroy() {
        activity = null;
        from = null;
        jidStr = null;

        super.onDestroy();
    }
}
