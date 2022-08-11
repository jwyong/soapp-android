package com.soapp.camera;

import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.MediaController;
import android.widget.VideoView;

import com.bumptech.glide.request.target.SimpleTarget;
import com.soapp.R;
import com.soapp.base.BaseActivity;
import com.soapp.global.TouchImageView;
import com.soapp.sql.DatabaseHelper;
import com.soapp.sql.room.entity.Message;

import java.io.File;
import java.util.List;

import androidx.annotation.Nullable;


public class VideoPreviewActivity extends BaseActivity {

    public static int VideoTimer;
    int pos = -1;
    File videoFile;
    Uri videoUri;
    String streamLinkPath;
    int lastStopAt;
    private VideoView videoView;
    public List<Message> list;
    public String[] urlList;
    private TouchImageView fullscreen_img;
    public SimpleTarget targetUri;
    private DatabaseHelper databaseHelper = DatabaseHelper.getInstance();

    @Override
    protected void onCreate(@Nullable final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.video_playback_activity);
//        setupToolbar();
//        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        videoView = findViewById(R.id.video);
        videoView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                playVideo();
            }
        });
//        final MessageView actualResolution =(MessageView) findViewById(R.id.actualResolution);
        Intent intent = getIntent();

        if (intent.hasExtra("outgoingVideo")) {
            String videoUriString = intent.getStringExtra("outgoingVideo");
            videoFile = new File(videoUriString);
            videoUri = Uri.fromFile(videoFile);
        } else if (intent.hasExtra("incomingVideo")) {
            String videoUriString = intent.getStringExtra("incomingVideo");
            videoFile = new File(videoUriString);
            videoUri = Uri.fromFile(videoFile);
        } else if (intent.hasExtra("streamLink")) {
            streamLinkPath = intent.getStringExtra("streamLink");
            videoUri = Uri.parse(streamLinkPath);
            lastStopAt = intent.getIntExtra("lastStopAt", 0);
        } else if (intent.hasExtra("video")) {
            String videoUriString = intent.getStringExtra("video");
            videoFile = new File(videoUriString);
            videoUri = Uri.fromFile(videoFile);
        } else {
            String streamLinkPath = intent.getStringExtra("streamLink1");
            videoFile = new File(streamLinkPath);
            videoUri = Uri.fromFile(videoFile);
            lastStopAt = intent.getIntExtra("lastStopAt", 0);
        }

        MediaController controller = new MediaController(this);
        controller.setAnchorView(videoView);
        controller.setMediaPlayer(videoView);
        videoView.setMediaController(controller);
        videoView.setVideoURI(videoUri);
        videoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
//                actualResolution.setTitle("Actual resolution");
//                actualResolution.setMessage(mp.getVideoWidth() + " x " + mp.getVideoHeight());
                ViewGroup.LayoutParams lp = videoView.getLayoutParams();
                float videoWidth = mp.getVideoWidth();
                float videoHeight = mp.getVideoHeight();
                float viewWidth = videoView.getWidth();
                float viewHeight = videoView.getHeight();
                if (videoHeight > videoWidth) {
                    //vertical
                    if (viewWidth > viewHeight) {
                        //the view is horizontal
                        lp.width = (int) (viewWidth * (videoWidth / videoHeight));
                    }
                } else {
                    lp.height = (int) (viewHeight * (videoWidth / videoHeight));
                }

                videoView.setLayoutParams(lp);
                if (lastStopAt != -1 && savedInstanceState == null) {
                    videoView.seekTo(lastStopAt);
                }

                if (pos == -1) {
                    playVideo();
                }
//                else{
//                    controller.show();
//                }
            }
        });

    }

    void playVideo() {
        if (videoView.isPlaying()) return;
        videoView.start();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putInt("position", videoView.getCurrentPosition());
        videoView.pause();
        super.onSaveInstanceState(outState);
        }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {

        pos = savedInstanceState.getInt("position");
        videoView.seekTo(pos);
        videoView.start();
        super.onRestoreInstanceState(savedInstanceState);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        videoView.pause();
        if (videoView.getCurrentPosition() == videoView.getDuration()) {
            VideoTimer = 0;
        } else {
            VideoTimer = videoView.getCurrentPosition();
        }

        finish();
    }
}
