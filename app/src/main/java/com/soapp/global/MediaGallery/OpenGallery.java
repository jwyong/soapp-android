package com.soapp.global.MediaGallery;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.soapp.R;
import com.soapp.base.BaseActivity;
import com.soapp.camera.ImageCropPreviewActivity;
import com.soapp.camera.ImageNormalPreviewActivity;
import com.soapp.chat_class.group_chat.details.images.GroupChatImgAdapter;
import com.soapp.chat_class.single_chat.details.images.IndiChatImgAdapter;
import com.soapp.global.GlobalVariables;
import com.soapp.global.ImagePreviewSlide;
import com.soapp.global.MediaGallery.Adapters.MainAdapter;
import com.soapp.global.MediaGallery.Fragments.ImagesFragment;
import com.soapp.global.MediaGallery.Fragments.VideosFragment;
import com.soapp.sql.room.entity.Message;

import java.util.ArrayList;
import java.util.List;

import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

//import com.erikagtierrez.multiple_media_picker.R;

public class OpenGallery extends BaseActivity {
    private List<Boolean> selected = new ArrayList<>();
//    public static ArrayList<String> imagesSelected = new ArrayList<>();
    public static List<Message> listAllView;
    public static Activity activity;

    private FloatingActionButton fab;
    private RecyclerView recyclerView;
    private MainAdapter mAdapter;
    private List<String> mediaList = new ArrayList<>();

    //variables
    private String title;
    private boolean isLongClicked = false;
    private String parent;
    private String urlPath;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.gallery_activity_open_gallery);
        setupToolbar();

        //for selected null unwrap
        if (selected == null) {
            selected = new ArrayList<>();
        }

        activity = this;
        fab = findViewById(R.id.fab);
        fab.setOnClickListener(view -> {
            finish();
        });
        fab.setVisibility(View.GONE);

        title = getIntent().getStringExtra("title");
        if (title == null || title.length() == 0) {
            title = getString(R.string.select_media);
        }
        setTitle(title);

//        if (imagesSelected.size() > 0) {
//            setTitle(String.valueOf(imagesSelected.size()) + " Selected");
//        }
        recyclerView = findViewById(R.id.recycler_view);
        parent = getIntent().getExtras().getString("FROM");
        mediaList.clear();
        selected.clear();

        if (parent.equals("Images")) {
            mediaList.addAll(ImagesFragment.imagesList);

            selected.addAll(ImagesFragment.selected);

        } else if (parent.equals("allView")) {

            // later remove public static wan change this function dun using looping using .addAll
            for (int i = 0; i < listAllView.size(); i++) {

                if (listAllView.get(i).getIsSender() == 20) { // outgoing

                    urlPath = GlobalVariables.IMAGES_SENT_PATH + "/";

                } else if (listAllView.get(i).getIsSender() == 21) { // incoming

                    urlPath = GlobalVariables.IMAGES_PATH;

                }
                // dun delete after will add the vid path
//                else if (listAllView.get(i).getIsSender() == 24) {
//
//                } else if (listAllView.get(i).getIsSender() == 25) {
//
//                }

                mediaList.add(urlPath + listAllView.get(i).getMsgInfoUrl());
                selected.add(false);
            }

        } else {
            mediaList.addAll(VideosFragment.videosList);
            selected.addAll(VideosFragment.selected);
        }
        populateRecyclerView();
    }


    private void populateRecyclerView() {
        for (int i = 0; i < selected.size(); i++) {
//            if (imagesSelected.contains(mediaList.get(i))) {
//                selected.set(i, true);
//            } else {
            selected.set(i, false);
//            }
        }
        mAdapter = new MainAdapter(mediaList, selected, getApplicationContext());
        RecyclerView.LayoutManager mLayoutManager = new GridLayoutManager(getApplicationContext(), 3);
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.getItemAnimator().setChangeDuration(0);
        recyclerView.setAdapter(mAdapter);
        recyclerView.addOnItemTouchListener(new RecyclerTouchListener(this, recyclerView, new ClickListener() {
            @Override
            public void onClick(View view, int position) {
                if (isLongClicked) {


//                    if (!selected.get(position).equals(true) && imagesSelected.size() < Gallery.maxSelection) {
//                        imagesSelected.add(mediaList.get(position));
//                        selected.set(position, !selected.get(position));
//                        mAdapter.notifyItemChanged(position);
//                    } else if (selected.get(position).equals(true)) {
//                        if (imagesSelected.indexOf(mediaList.get(position)) != -1) {
//                            imagesSelected.remove(imagesSelected.indexOf(mediaList.get(position)));
//                            selected.set(position, !selected.get(position));
//                            mAdapter.notifyItemChanged(position);
//                        }
//                    }
//                    Gallery.selectionTitle = imagesSelected.size();
//                    if (imagesSelected.size() != 0) {
//                        setTitle(String.valueOf(imagesSelected.size()) + " Selected");
//                    } else {
//                        setTitle("Select Media");
//                    }
                } else {
//                    if (!selected.get(position).equals(true) && imagesSelected.size() < 1) {
//                        imagesSelected.add(mediaList.get(position));
//                        selected.set(position, !selected.get(position));
//                        mAdapter.notifyItemChanged(position);

                        if (GalleryMainActivity.from != null) {
                            if (!GalleryMainActivity.from.equals("chat") && !GalleryMainActivity.from.equals("chatmedia")) {
                                Intent in = new Intent(OpenGallery.this, ImageCropPreviewActivity.class);
                                in.putExtra("from", GalleryMainActivity.from);
                                if (parent.equals("Images")) {
                                    in.putExtra("image_address", mediaList.get(position));
                                } else {
                                    in.putExtra("video", mediaList.get(position));
                                }
                                in.putExtra("jid", GalleryMainActivity.jidStr);
                                startActivity(in);
                            } else {
                                Intent in = new Intent(OpenGallery.this, ImageNormalPreviewActivity.class);
                                in.putExtra("from", GalleryMainActivity.from);
                                if (parent.equals("Images")) {
                                    in.putExtra("image_address", mediaList.get(position));
                                } else {
                                    in.putExtra("video", mediaList.get(position));
                                }
                                in.putExtra("jid", GalleryMainActivity.jidStr);
                                startActivity(in);
                            }
                        } else {
                            Intent in = new Intent(OpenGallery.this, ImagePreviewSlide.class);
                            in.putExtra("position", position);
                            if (getIntent().getStringExtra("viewAllImg").equals("group")) {

                                ImagePreviewSlide.list = GroupChatImgAdapter.grpChatImgList;

                            } else {

                                ImagePreviewSlide.list = IndiChatImgAdapter.chatImgList;
                            }

                            startActivity(in);
                        }

//                    } else if (selected.get(position).equals(true)) {
//                        if (imagesSelected.indexOf(mediaList.get(position)) != -1) {
//                            imagesSelected.remove(imagesSelected.indexOf(mediaList.get(position)));
//                            selected.set(position, !selected.get(position));
//                            mAdapter.notifyItemChanged(position);
//                        }
//                    }
//                    Gallery.selectionTitle = imagesSelected.size();
//                    if (imagesSelected.size() > 0) {
//                        setTitle(String.valueOf(imagesSelected.size()) + " Selected");
//                    } else {
//                        setTitle(title);
//                    }
//                    Gallery.isFirstTime = false;
//                    if(parent.equals("Images")){
//                        OneFragment.visited = false;
//                    }else{
//                        TwoFragment.visited = false;
//                    }
//                    finish();
                }
            }

//            @Override
//            public void onLongClick(View view, int position) {
//
//                Log.d("jason" , "long click here first ?");
//                isLongClicked = true;
//                fab.setVisibility(View.VISIBLE);
//                if (!selected.get(position).equals(true) && imagesSelected.size() < Gallery.maxSelection) {
//                    imagesSelected.add(mediaList.get(position));
//                    selected.set(position, !selected.get(position));
//                    mAdapter.notifyItemChanged(position);
//                } else if (selected.get(position).equals(true)) {
//                    if (imagesSelected.indexOf(mediaList.get(position)) != -1) {
//                        imagesSelected.remove(imagesSelected.indexOf(mediaList.get(position)));
//                        selected.set(position, !selected.get(position));
//                        mAdapter.notifyItemChanged(position);
//                    }
//                }
//                Gallery.selectionTitle = imagesSelected.size();
//                if (imagesSelected.size() != 0) {
//                    setTitle(String.valueOf(imagesSelected.size()) + " Selected");
//                } else {
////                    setTitle(Gallery.title);
//                }
//            }

        }));
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    @Override
    protected void onResume() {
        super.onResume();

    }

    public interface ClickListener {
        void onClick(View view, int position);

//        void onLongClick(View view, int position);
    }

    public static class RecyclerTouchListener implements RecyclerView.OnItemTouchListener {
        private GestureDetector gestureDetector;
        private OpenGallery.ClickListener clickListener;

        public RecyclerTouchListener(Context context, final RecyclerView recyclerView, final OpenGallery.ClickListener clickListener) {
            this.clickListener = clickListener;
            gestureDetector = new GestureDetector(context, new GestureDetector.SimpleOnGestureListener() {
                @Override
                public boolean onSingleTapUp(MotionEvent e) {
                    return true;
                }
//
//                @Override
//                public void onLongPress(MotionEvent e) {
//                    View child = recyclerView.findChildViewUnder(e.getX(), e.getY());
//                    if (child != null && clickListener != null) {
//                        clickListener.onLongClick(child, recyclerView.getChildPosition(child));
//                    }
//                }
            });
        }

        @Override
        public boolean onInterceptTouchEvent(RecyclerView rv, MotionEvent e) {
            View child = rv.findChildViewUnder(e.getX(), e.getY());
            if (child != null && clickListener != null && gestureDetector.onTouchEvent(e)) {
                clickListener.onClick(child, rv.getChildLayoutPosition(child));
            }
            return false;
        }

        @Override
        public void onTouchEvent(RecyclerView rv, MotionEvent e) {
        }

        @Override
        public void onRequestDisallowInterceptTouchEvent(boolean disallowIntercept) {
        }
    }

    @Override
    protected void onDestroy() {
        activity = null;
        listAllView = null;

        super.onDestroy();
    }
}

