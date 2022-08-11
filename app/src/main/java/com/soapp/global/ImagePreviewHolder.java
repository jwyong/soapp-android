package com.soapp.global;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Context;
import android.media.MediaPlayer;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.VideoView;

import com.soapp.R;
import com.soapp.global.GlideAPI.GlideApp;
import com.soapp.sql.room.entity.Message;

import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.viewpager.widget.PagerAdapter;

public class ImagePreviewHolder extends PagerAdapter {
    Context context;
    LayoutInflater layoutInflater;
    String mediaPath, imagePath;
    int count = 0;
    Handler handler = new Handler();
    public Runnable runnable;
    public List<Message> dataList;

    public ImagePreviewHolder(Context context) {
        this.context = context;

    }

    public void setcount(List<Message> messages) {
        this.dataList = messages;

    }

    @Override
    public int getCount() {

        return dataList.size();
    }

    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
        return (view == object);
    }


    @NonNull
    @Override
    public Object instantiateItem(@NonNull ViewGroup container, int position) {
        layoutInflater = (LayoutInflater) context.getSystemService(context.LAYOUT_INFLATER_SERVICE);
        View view = layoutInflater.inflate(R.layout.image_video_slide, container, false);


        TouchImageView previewImage = view.findViewById(R.id.previewImage);

//            // video id
        VideoView previewVideo = view.findViewById(R.id.previewVideo);
        ImageView playbtn = view.findViewById(R.id.playbtn);
        ConstraintLayout vid_controller = view.findViewById(R.id.vid_controller);
        TextView start_timer = view.findViewById(R.id.start_timer);
        TextView end_timer = view.findViewById(R.id.end_timer);
        SeekBar seek_progress = view.findViewById(R.id.seek_progress);
        playbtn.setBackground(context.getResources().getDrawable(R.drawable.ic_video_play_gradient_150px));


        switch (dataList.get(position).getIsSender()) {
            case 20: //outgoing image
                imagePath = GlobalVariables.IMAGES_SENT_PATH + "/" + dataList.get(position).getMsgInfoUrl();

                previewImage.setVisibility(View.VISIBLE);
                previewVideo.setVisibility(View.GONE);
                playbtn.setVisibility(View.GONE);
                vid_controller.setVisibility(View.GONE);

                GlideApp.with(context)
                        .load(imagePath)
                        .placeholder(R.drawable.default_img_full)
                        .into(previewImage);
                break;

            case 21: //incoming image
                imagePath = GlobalVariables.IMAGES_PATH + dataList.get(position).getMsgInfoUrl();

                previewImage.setVisibility(View.VISIBLE);
                previewVideo.setVisibility(View.GONE);
                playbtn.setVisibility(View.GONE);
                vid_controller.setVisibility(View.GONE);

                GlideApp.with(context)
                        .load(imagePath)
                        .placeholder(R.drawable.default_img_full)
                        .into(previewImage);

                break;

            case 24: //out vid
                mediaPath = GlobalVariables.VIDEO_SENT_PATH + "/" + dataList.get(position).getMsgInfoId() + ".mp4";
                previewVideo.setVisibility(View.VISIBLE);
                playbtn.setVisibility(View.VISIBLE);
                vid_controller.setVisibility(View.VISIBLE);
                previewImage.setVisibility(View.GONE);

                previewVideo.setVideoPath(mediaPath);
                previewVideo.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                    @Override
                    public void onPrepared(MediaPlayer mp) {
                        ViewGroup.LayoutParams lp = previewVideo.getLayoutParams();
                        float videoWidth = mp.getVideoWidth();
                        float videoHeight = mp.getVideoHeight();
                        float viewWidth = previewVideo.getWidth();
                        float viewHeight = previewVideo.getHeight();
                        if (videoHeight > videoWidth) {
                            //vertical
                            if (viewWidth > viewHeight) {
                                //the view is horizontal
                                lp.width = (int) (viewWidth * (videoWidth / videoHeight));
                            }
                        } else {
                            lp.height = (int) (viewHeight * (videoWidth / videoHeight));
                        }
                        previewVideo.setLayoutParams(lp);

                        // outgoing total timer

                        Calendar vEndTimer = Calendar.getInstance();

                        vEndTimer.setTimeInMillis(mp.getDuration());
                        int endTimerSec = vEndTimer.get(Calendar.SECOND);
                        long endTMin = TimeUnit.MILLISECONDS.toMinutes(endTimerSec);

                        String endTimer = String.format(Locale.ENGLISH, "%02d:%02d", endTMin, endTimerSec);

                        end_timer.setText(endTimer);
                        seek_progress.setMax(mp.getDuration());

                        // vid func
                        seek_progress.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                            @Override
                            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {

                            }

                            @Override
                            public void onStartTrackingTouch(SeekBar seekBar) {

                            }

                            @Override
                            public void onStopTrackingTouch(SeekBar seekBar) {

                                if (!previewVideo.isPlaying()) {
                                    handler.removeCallbacksAndMessages(null);
                                    previewVideo.seekTo(seekBar.getProgress());

                                    Calendar vSeekTimer = Calendar.getInstance();

                                    vSeekTimer.setTimeInMillis(seekBar.getProgress());
                                    int in1TimerSec = vSeekTimer.get(Calendar.SECOND);
                                    long in1TMin = TimeUnit.MILLISECONDS.toMinutes(in1TimerSec);

                                    String seekStartTimer = String.format(Locale.ENGLISH, "%02d:%02d", in1TMin, in1TimerSec);

                                    start_timer.setText(seekStartTimer);

                                } else {
                                    handler.removeCallbacksAndMessages(null);
                                    handler.postDelayed(runnable, 1000);
                                    previewVideo.seekTo(seekBar.getProgress());

                                    Calendar vSeekTimer = Calendar.getInstance();

                                    vSeekTimer.setTimeInMillis(seekBar.getProgress());
                                    int in1TimerSec = vSeekTimer.get(Calendar.SECOND);
                                    long in1TMin = TimeUnit.MILLISECONDS.toMinutes(in1TimerSec);

                                    String seekStartTimer = String.format(Locale.ENGLISH, "%02d:%02d", in1TMin, in1TimerSec);

                                    start_timer.setText(seekStartTimer);
                                }

                            }
                        });

                        // video function
                        playbtn.setOnClickListener(p -> {
                            if (!previewVideo.isPlaying()) {

                                previewVideo.start();
                                playbtn.setBackground(context.getResources().getDrawable(R.drawable.ic_video_pause_gradient_150px));
                                playbtn.setVisibility(View.GONE);
                                vid_controller.setVisibility(View.GONE);
//                                getSupportActionBar().hide();

                                handler.postDelayed(runnable = new Runnable() {
                                    @Override
                                    public void run() {
//                                            count++;

                                        Calendar vStartTimer = Calendar.getInstance();

                                        vStartTimer.setTimeInMillis(previewVideo.getCurrentPosition());
                                        int StartTimerSec = vStartTimer.get(Calendar.SECOND);
                                        long StartTMin = TimeUnit.MILLISECONDS.toMinutes(StartTimerSec);
                                        String startTimer = String.format(Locale.ENGLISH, "%02d:%02d", StartTMin, StartTimerSec);

                                        start_timer.setText(startTimer);
                                        seek_progress.setProgress(previewVideo.getCurrentPosition());


                                        handler.postDelayed(this, 0);
                                    }


                                }, 0);


                            } else {
                                previewVideo.pause();
                                playbtn.setBackground(context.getResources().getDrawable(R.drawable.ic_video_play_gradient_150px));
                                vid_controller.setVisibility(View.VISIBLE);
                                playbtn.setVisibility(View.VISIBLE);
//                                getSupportActionBar().show();
                                seek_progress.setProgress(previewVideo.getCurrentPosition());
                                handler.removeCallbacksAndMessages(null);
                                if (!previewVideo.isPlaying()) {
                                    playbtn.animate().setListener(new AnimatorListenerAdapter() {
                                        @Override
                                        public void onAnimationStart(Animator animation) {
                                            vid_controller.setVisibility(View.VISIBLE);
                                            playbtn.setVisibility(View.VISIBLE);

                                            super.onAnimationStart(animation);
                                        }
                                    });
                                }
                            }
                        });
                    }
                });


                previewVideo.setOnCompletionListener(mediaPlayer -> {

                    playbtn.setBackground(context.getResources().getDrawable(R.drawable.ic_video_play_gradient_150px));
                    playbtn.setVisibility(View.VISIBLE);
                    vid_controller.setVisibility(View.VISIBLE);
//                    getSupportActionBar().show();
                    start_timer.setText("00.00");
                    handler.removeCallbacksAndMessages(null);
                    previewVideo.seekTo(100);
                    seek_progress.setProgress(100);
                });
                break;

            case 25: //in vid
                mediaPath = GlobalVariables.VIDEO_PATH + "/" + dataList.get(position).getMsgInfoUrl() + ".mp4";

                previewVideo.setVisibility(View.VISIBLE);
                playbtn.setVisibility(View.VISIBLE);
                vid_controller.setVisibility(View.VISIBLE);
                previewImage.setVisibility(View.GONE);


                previewVideo.setVideoPath(mediaPath);

                previewVideo.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                    @Override
                    public void onPrepared(MediaPlayer mp) {
                        ViewGroup.LayoutParams lp = previewVideo.getLayoutParams();
                        float videoWidth = mp.getVideoWidth();
                        float videoHeight = mp.getVideoHeight();
                        float viewWidth = previewVideo.getWidth();
                        float viewHeight = previewVideo.getHeight();
                        if (videoHeight > videoWidth) {
                            //vertical
                            if (viewWidth > viewHeight) {
                                //the view is horizontal
                                lp.width = (int) (viewWidth * (videoWidth / videoHeight));
                            }
                        } else {
                            lp.height = (int) (viewHeight * (videoWidth / videoHeight));
                        }
                        previewVideo.setLayoutParams(lp);

                        // outgoing total timer

                        Calendar vEndTimer = Calendar.getInstance();

                        vEndTimer.setTimeInMillis(mp.getDuration());
                        int endTimerSec = vEndTimer.get(Calendar.SECOND);
                        long endTMin = TimeUnit.MILLISECONDS.toMinutes(endTimerSec);

                        String endTimer = String.format(Locale.ENGLISH, "%02d:%02d", endTMin, endTimerSec);

                        end_timer.setText(endTimer);
                        seek_progress.setMax(mp.getDuration());

                        // vid func
                        seek_progress.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                            @Override
                            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {

                            }

                            @Override
                            public void onStartTrackingTouch(SeekBar seekBar) {

                            }

                            @Override
                            public void onStopTrackingTouch(SeekBar seekBar) {

                                if (!previewVideo.isPlaying()) {
                                    handler.removeCallbacksAndMessages(null);
                                    previewVideo.seekTo(seekBar.getProgress());

                                    Calendar vSeekTimer = Calendar.getInstance();

                                    vSeekTimer.setTimeInMillis(seekBar.getProgress());
                                    int in1TimerSec = vSeekTimer.get(Calendar.SECOND);
                                    long in1TMin = TimeUnit.MILLISECONDS.toMinutes(in1TimerSec);

                                    String seekStartTimer = String.format(Locale.ENGLISH, "%02d:%02d", in1TMin, in1TimerSec);

                                    start_timer.setText(seekStartTimer);

                                } else {
                                    handler.removeCallbacksAndMessages(null);
                                    handler.postDelayed(runnable, 1000);
                                    previewVideo.seekTo(seekBar.getProgress());

                                    Calendar vSeekTimer = Calendar.getInstance();

                                    vSeekTimer.setTimeInMillis(seekBar.getProgress());
                                    int in1TimerSec = vSeekTimer.get(Calendar.SECOND);
                                    long in1TMin = TimeUnit.MILLISECONDS.toMinutes(in1TimerSec);

                                    String seekStartTimer = String.format(Locale.ENGLISH, "%02d:%02d", in1TMin, in1TimerSec);

                                    start_timer.setText(seekStartTimer);
                                }

                            }
                        });

                        // video function
                        playbtn.setOnClickListener(p -> {
                            if (!previewVideo.isPlaying()) {

                                previewVideo.start();
                                playbtn.setBackground(context.getResources().getDrawable(R.drawable.ic_video_pause_gradient_150px));
                                playbtn.setVisibility(View.GONE);
                                vid_controller.setVisibility(View.GONE);
//                                getSupportActionBar().hide();

                                handler.postDelayed(runnable = new Runnable() {
                                    @Override
                                    public void run() {
//                                            count++;

                                        Calendar vStartTimer = Calendar.getInstance();

                                        vStartTimer.setTimeInMillis(previewVideo.getCurrentPosition());
                                        int StartTimerSec = vStartTimer.get(Calendar.SECOND);
                                        long StartTMin = TimeUnit.MILLISECONDS.toMinutes(StartTimerSec);
                                        String startTimer = String.format(Locale.ENGLISH, "%02d:%02d", StartTMin, StartTimerSec);

                                        start_timer.setText(startTimer);
                                        seek_progress.setProgress(previewVideo.getCurrentPosition());

                                        handler.postDelayed(this, 0);
                                    }


                                }, 0);


                            } else {
                                previewVideo.pause();
                                playbtn.setBackground(context.getResources().getDrawable(R.drawable.ic_video_play_gradient_150px));
                                vid_controller.setVisibility(View.VISIBLE);
                                playbtn.setVisibility(View.VISIBLE);
//                                getSupportActionBar().show();
                                seek_progress.setProgress(previewVideo.getCurrentPosition());
                                handler.removeCallbacksAndMessages(null);
                                if (!previewVideo.isPlaying()) {
                                    playbtn.animate().setListener(new AnimatorListenerAdapter() {
                                        @Override
                                        public void onAnimationStart(Animator animation) {
                                            vid_controller.setVisibility(View.VISIBLE);
                                            playbtn.setVisibility(View.VISIBLE);

                                            super.onAnimationStart(animation);
                                        }
                                    });
                                }
                            }
                        });

                    }
                });


                previewVideo.setOnCompletionListener(mediaPlayer -> {

                    playbtn.setBackground(context.getResources().getDrawable(R.drawable.ic_video_play_gradient_150px));
                    playbtn.setVisibility(View.VISIBLE);
                    vid_controller.setVisibility(View.VISIBLE);
//                    getSupportActionBar().show();
                    start_timer.setText("00.00");
                    handler.removeCallbacksAndMessages(null);
                    previewVideo.seekTo(100);
                    seek_progress.setProgress(100);
                });

                break;
        }

        view.setOnClickListener(vV -> {

            if (playbtn.getVisibility() == View.VISIBLE) {
//                getSupportActionBar().hide();
                playbtn.setVisibility(View.GONE);
                vid_controller.setVisibility(View.GONE);
            } else {
                playbtn.setVisibility(View.VISIBLE);
                vid_controller.setVisibility(View.VISIBLE);
//                getSupportActionBar().show();
                if (previewVideo.isPlaying()) {
                    playbtn.animate()
                            .setDuration(2500)
                            .setListener(new AnimatorListenerAdapter() {
                                @Override
                                public void onAnimationEnd(Animator animation) {
                                    playbtn.setVisibility(View.GONE);
                                    vid_controller.setVisibility(View.GONE);
//                                    getSupportActionBar().hide();

                                    super.onAnimationEnd(animation);
                                }
                            })
                            .start();
                }
            }

        });


        container.addView(view, LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
        return view;

    }

    @Override
    public void destroyItem(@NonNull ViewGroup container, int position,
                            @NonNull Object object) {
        container.removeView((View) object);
    }

}