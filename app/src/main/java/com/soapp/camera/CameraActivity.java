package com.soapp.camera;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.soappcamera.CameraListener;
import com.example.soappcamera.CameraLogger;
import com.example.soappcamera.CameraOptions;
import com.example.soappcamera.CameraView;
import com.example.soappcamera.Facing;
import com.example.soappcamera.Flash;
import com.example.soappcamera.SessionType;
import com.example.soappcamera.Size;
import com.soapp.R;
import com.soapp.global.GlobalVariables;
import com.soapp.global.ImageHelper;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Calendar;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

public class CameraActivity extends AppCompatActivity implements View.OnClickListener, View.OnLongClickListener {

    public static Activity context;
    public CameraView camera;
    public ImageButton capturePhotoImgBtn;
    public boolean isRecording = false;
    int recordingTimeLimit = 62000;
    int recordDelayedSecond = 4;
    String from;
    TextView recordSeconds_txtview;
    ImageButton switchCamerImgBtn;
    int actionMove = 0;
    int resID;
    Calendar timer = Calendar.getInstance();
    private ViewGroup controlPanel;
    private boolean mCapturingPicture;
    private boolean mCapturingVideo;
    // To show stuff in the callback
    private Size mCaptureNativeSize;
    private long mCaptureTime;
    private ImageView flashSwitch;
    private Facing tempFacing;
    private ProgressBar progressBarCircle;
    private RelativeLayout forthetimer;
    private Handler mHandler = new Handler();
    private boolean wasRun = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = getIntent();
        from = intent.getStringExtra("from");
        if (intent.hasExtra("resID")) {
            resID = intent.getIntExtra("resID", -1);
        }
        context = this;

        //hide status bar
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED,
                WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED);
        setContentView(R.layout.camera_activity);
        CameraLogger.setLogLevel(CameraLogger.LEVEL_VERBOSE);
        camera = findViewById(R.id.camera_jon);
        if (savedInstanceState == null) {
            if (from.equals("ProfilePic") || from.equals("UserProfile")) {
                camera.setFacing(Facing.FRONT);
            }
//            else if (from.equals("GroupDetail")) {
//                camera.setFacing(Facing.BACK);
//            }
            else {
                camera.setFacing(Facing.BACK);
            }
        } else {
            camera.setFacing(tempFacing);
        }

        if (savedInstanceState != null) {
            recordingTimeLimit = 61000;
        }
        camera.addCameraListener(new CameraListener() {
            public void onCameraOpened(CameraOptions options) {
                onOpened();
            }

            public void onPictureTaken(byte[] jpeg) {
                onPicture(jpeg);
            }

            @Override
            public void onVideoTaken(File video) {
                super.onVideoTaken(video);
                onVideo(video);
            }
        });
        camera.setFlash(Flash.AUTO);
        flashSwitch = findViewById(R.id.switchFlash);
        flashSwitch.setImageResource(R.drawable.xml_ic_flash_auto_black_24dp);

        // timer record
        recordSeconds_txtview = findViewById(R.id.recordSeconds_txtview);
        progressBarCircle = findViewById(R.id.progressBarCircle);
        forthetimer = findViewById(R.id.forthetimer);

        capturePhotoImgBtn = findViewById(R.id.capturePhoto);

        capturePhotoImgBtn.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                switch (motionEvent.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        if (!from.equals("UserProfile") && !from.equals("ProfilePic") && !from.equals
                                ("GroupDetail") && !from.equals("CreateGroup") && !from.equals("CreateAppt")) {
                            wasRun = true;
                            capturePhotoImgBtn.setImageResource(R.drawable.ic_camera_red_300px);
                            mHandler.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    if (wasRun) {
                                        actionMove += 1;

                                        if (actionMove >= 1 && !isRecording) {
                                            recordVideo();
                                            isRecording = true;
                                        }

                                        if (isRecording) {
                                            if (actionMove > 1) {
                                                forthetimer.setVisibility(View.VISIBLE);
                                            }
                                            int timerInMilis = (actionMove - 2) * 1000;
                                            timer.setTimeInMillis(timerInMilis);

                                            int TSec = timer.get(Calendar.SECOND);
                                            long TMin = TimeUnit.MILLISECONDS.toMinutes(timerInMilis);

                                            //set the circle progress bar
                                            progressBarCircle.setProgress(TSec);

                                            //set timer text xx:xx
                                            String TTimer = String.format(Locale.ENGLISH, "%02d:%02d", TMin, TSec);
                                            recordSeconds_txtview.setText(TTimer);
                                        }
                                        mHandler.postDelayed(this, 1000);
                                        //whatever you want to do if run
                                        //you can add you want to increase variable here
                                    }
                                }
                            }, 1000);
                        }
                        break;
                    case MotionEvent.ACTION_UP:
                        wasRun = false;
                        capturePhotoImgBtn.setImageResource(android.R.color.transparent);
                        mHandler.removeCallbacksAndMessages(null);
                        if (actionMove > 1 && isRecording) {
                            camera.stopCapturingVideo();
                            forthetimer.setVisibility(View.GONE);
                            actionMove = 0;
                        } else {
                            capturePhoto();
                            actionMove = 0;
                        }
                        break;
                }

                return false;
            }
        });

        switchCamerImgBtn = findViewById(R.id.switchCamera);
        switchCamerImgBtn.setOnClickListener(this);
        flashSwitch.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.capturePhoto:
//                capturePhoto();
                break;

            case R.id.switchCamera:
                switchCamera();
                break;

            case R.id.switchFlash:
                switchFlash();
                break;

            default:
                break;
        }
    }


    @Override
    public boolean onLongClick(View view) {
        return false;
    }

    public void recordVideo() {

        camera.setSessionType(SessionType.VIDEO);
        // default quality is
//        startStop();
        //                progressBarCircle.setProgress((int) (millisUntilFinished / 1000));
        mCapturingVideo = true;

        String videoName = ImageHelper.getFileNameForSavingMedia(GlobalVariables.VIDEO_PATH, "VID");
        File file = new File(GlobalVariables.VIDEO_PATH + videoName);

        if (!file.exists()) {
            try {
                boolean ok = file.createNewFile();
                if (ok) {
                    camera.startCapturingVideo(file, recordingTimeLimit);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


    private void onVideo(File video) {
        mCapturingVideo = false;
        Intent intent = new Intent(this, ImageNormalPreviewActivity.class);
        intent.putExtra("video", Uri.fromFile(video).getPath());
        intent.putExtra("jid", getIntent().getStringExtra("jid"));
        intent.putExtra("from", from);
        intent.putExtra("camera", "yes");
        startActivity(intent);
    }

    private void switchCamera() {
        if (mCapturingPicture) return;
        switch (camera.toggleFacing()) {
            case BACK:
                tempFacing = Facing.BACK;
                break;

            case FRONT:
                tempFacing = Facing.FRONT;
                break;
        }
    }

    private void switchFlash() {
        if (camera.getFlash() == Flash.ON || camera.getFlash() == Flash.TORCH) {
            camera.setFlash(Flash.OFF);
            flashSwitch.setImageResource(R.drawable.xml_ic_flash_off_black_24dp);

        } else if (camera.getFlash() == Flash.OFF) {
            camera.setFlash(Flash.AUTO);
            flashSwitch.setImageResource(R.drawable.xml_ic_flash_auto_black_24dp);

        } else if (camera.getFlash() == Flash.AUTO) {
            camera.setFlash(Flash.ON);
            flashSwitch.setImageResource(R.drawable.xml_ic_flash_on_black_24dp);
        }
    }

    public void capturePhoto() {
        if (mCapturingPicture) return;
        mCapturingPicture = true;
        mCaptureTime = System.currentTimeMillis();
        mCaptureNativeSize = camera.getCaptureSize();
//        message("Capturing picture...", false);
        camera.capturePicture();
    }

    private void onPicture(byte[] jpeg) {
        mCapturingPicture = false;
        long callbackTime = System.currentTimeMillis();
        if (mCapturingVideo) {
//            message("Captured while taking video. Size=" + mCaptureNativeSize, false);
            return;
        }

        // This can happen if picture was taken with a gesture.
        if (mCaptureTime == 0) mCaptureTime = callbackTime - 300;
        if (mCaptureNativeSize == null) mCaptureNativeSize = camera.getCaptureSize();

        new SavePhotoTask().execute(jpeg);

        mCaptureTime = 0;
        mCaptureNativeSize = null;
    }

    private void onOpened() {

    }

    private void message(String content, boolean important) {
        int length = important ? Toast.LENGTH_LONG : Toast.LENGTH_SHORT;
        Toast.makeText(this, content, length).show();
    }

    @Override
    protected void onResume() {
        super.onResume();

        actionMove = 0;
        wasRun = false;
        capturePhotoImgBtn.setImageResource(android.R.color.transparent);
        forthetimer.setVisibility(View.GONE);
        camera.start();
    }


    //region Boilerplate

    @Override
    protected void onPause() {
        super.onPause();

        actionMove = 0;
        wasRun = false;

        if (isRecording) {

            recordingTimeLimit = 61000;
            isRecording = false;
            recordDelayedSecond = 2;
        }
        forthetimer.setVisibility(View.GONE);
        camera.stop();
    }

    @Override
    protected void onDestroy() {
        capturePhotoImgBtn.setImageResource(android.R.color.transparent);
        camera.destroy();

        super.onDestroy();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        boolean valid = true;
        for (int grantResult : grantResults) {
            valid = valid && grantResult == PackageManager.PERMISSION_GRANTED;
            onBackPressed();
        }
        if (valid && !camera.isStarted()) {
            camera.start();
        }
    }

    class SavePhotoTask extends AsyncTask<byte[], String, String> {
        @Override
        protected String doInBackground(byte[]... jpeg) {
            File photo = new File(getCacheDir(), Util.getFileName());

            if (photo.exists()) {
                photo.delete();
            }

            try {
                FileOutputStream fos = new FileOutputStream(photo.getPath());

                fos.write(jpeg[0]);
                fos.close();
            } catch (java.io.IOException e) {
                return null;
            }

            return photo.getPath();
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            if (from.equals("chat") || from.equals("chatmedia")) {
                Intent intent = new Intent(getApplicationContext(), ImageNormalPreviewActivity.class);
                intent.putExtra("image_address", s);
                intent.putExtra("jid", getIntent().getStringExtra("jid"));
                intent.putExtra("from", from);
                intent.putExtra("resID", resID);
                intent.putExtra("camera", "yes");
                startActivity(intent);
            } else {
                Intent intent = new Intent(getApplicationContext(), ImageCropPreviewActivity.class);
                intent.putExtra("image_address", s);
                intent.putExtra("jid", getIntent().getStringExtra("jid"));
                intent.putExtra("from", from);
                intent.putExtra("resID", resID);
                intent.putExtra("camera", "yes");
                startActivity(intent);
            }

        }
    }


}
