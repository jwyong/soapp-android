package com.soapp.global;


import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import com.soapp.R;
import com.soapp.base.BaseActivity;
import com.soapp.sql.room.entity.Message;

import java.util.List;

import androidx.viewpager.widget.ViewPager;

//ImagePreviewSlide is for previewing images under ChatImageActivity

public class ImagePreviewSlide extends BaseActivity {
    public static List<Message> list;
    public int currentViewingPosition = -1;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.img_preview);

        ViewPager slideView = findViewById(R.id.slideView);

        int pos = getIntent().getIntExtra("position", 0);
        ImagePreviewHolder viewHolder = new ImagePreviewHolder(this);
        viewHolder.setcount(list);
        slideView.setAdapter(viewHolder);
        slideView.setCurrentItem(pos);

        slideView.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
//
//                //stop video immediately
//                //get previous and next position
//                int previousPosition = position - 1;
//                int nextPosition = position + 1;
//                //get type of previous and next view
//                int previousViewType = -1;
//                int nextViewType = -1;
//                if (previousPosition != -1) {
//                    previousViewType = list.get(previousPosition).getIsSender();
//                    View v = slideView.getChildAt(previousPosition);
//
//                    if (previousViewType != 20 && previousViewType != 21) {
//                        if (v != null) {
//                            VideoView previewVideo = v.findViewById(R.id.previewVideo);
//                            ImageView playbtn = v.findViewById(R.id.playbtn);
//                            ConstraintLayout vid_controller = v.findViewById(R.id.vid_controller);
//
//                            playbtn.setVisibility(View.VISIBLE);
//                            vid_controller.setVisibility(View.VISIBLE);
//                            playbtn.setBackground(getResources().getDrawable(R.drawable.ic_video_play_gradient_150px));
//
//                            previewVideo.pause();
//                            previewVideo.seekTo(0);
//                        }
//                    }
//                }
//                if (nextPosition < list.size()) {
//                    nextViewType = list.get(nextPosition).getIsSender();
//                    View v = slideView.getChildAt(nextPosition);
//
//                    if (nextViewType != 20 && nextViewType != 21) {
//                        if (v != null) {
//                            VideoView previewVideo = v.findViewById(R.id.previewVideo);
//                            ImageView playbtn = v.findViewById(R.id.playbtn);
//                            ConstraintLayout vid_controller = v.findViewById(R.id.vid_controller);
//
//                            playbtn.setVisibility(View.VISIBLE);
//                            vid_controller.setVisibility(View.VISIBLE);
//                            playbtn.setBackground(getResources().getDrawable(R.drawable.ic_video_play_gradient_150px));
//
//                            previewVideo.pause();
//                            previewVideo.seekTo(0);
//
//                        }
//                    }
//                }
            }

            @Override
            public void onPageSelected(int position) {
//                currentViewingPosition = position;
                View vImage = slideView.getChildAt(position);

                if (vImage != null) {
                    ImageView previewImage = vImage.findViewById(R.id.previewImage);

                    previewImage.setOnClickListener(v_Image -> {
                        if (getSupportActionBar().isShowing()) {
                            getSupportActionBar().hide();

                        } else {

                            getSupportActionBar().show();
                        }

                    });
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
        setupToolbar();
    }
}