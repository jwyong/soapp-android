/*
 * Copyright 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.soapp.camera;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.SurfaceTexture;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CameraMetadata;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.CaptureResult;
import android.hardware.camera2.TotalCaptureResult;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.media.Image;
import android.media.ImageReader;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;
import android.util.Size;
import android.util.SparseIntArray;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.soapp.R;
import com.soapp.global.DirectoryHelper;
import com.soapp.global.GlobalVariables;
import com.soapp.global.MiscHelper;
import com.soapp.global.PermissionHelper;
import com.soapp.global.Preferences;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;

import static android.content.Context.SENSOR_SERVICE;

public class Camera2Fragment extends Fragment
        implements View.OnClickListener, ActivityCompat.OnRequestPermissionsResultCallback, SensorEventListener {

    final int MAX_IMAGES = 3;

    /**
     * Conversion from screen rotation to JPEG orientation.
     */
    private static final SparseIntArray ORIENTATIONS = new SparseIntArray();
    private static final int REQUEST_CAMERA_PERMISSION = 1;
    private static final String FRAGMENT_DIALOG = "dialog";
    private static final String[] CAMERA_PERMISSIONS = {
            Manifest.permission.CAMERA,
            Manifest.permission.RECORD_AUDIO,
    };

    private Preferences preferences = Preferences.getInstance();

    /**
     * Tag for the {@link Log}.
     */
    private static final String TAG = "Camera2BasicFragment";
    /**
     * Camera state: Showing camera preview.
     */
    private static final int STATE_PREVIEW = 0;
    /**
     * Camera state: Waiting for the focus to be locked.
     */
    private static final int STATE_WAITING_LOCK = 1;
    /**
     * Camera state: Waiting for the exposure to be precapture state.
     */
    private static final int STATE_WAITING_PRECAPTURE = 2;
    /**
     * Camera state: Waiting for the exposure state to be something other than precapture.
     */
    private static final int STATE_WAITING_NON_PRECAPTURE = 3;
    /**
     * Camera state: Picture was taken.
     */
    private static final int STATE_PICTURE_TAKEN = 4;
    /**
     * Max preview width that is guaranteed by Camera2 API
     */
    private static final int MAX_PREVIEW_WIDTH = 1920;
    /**
     * Max preview height that is guaranteed by Camera2 API
     */
    private static final int MAX_PREVIEW_HEIGHT = 1080;
    private static final int SENSOR_ORIENTATION_DEFAULT_DEGREES = 90;
    private static final int SENSOR_ORIENTATION_INVERSE_DEGREES = 270;
    private static final SparseIntArray DEFAULT_ORIENTATIONS = new SparseIntArray();
    private static final SparseIntArray INVERSE_ORIENTATIONS = new SparseIntArray();
    /**
     * {@link TextureView.SurfaceTextureListener} handles several lifecycle events on a
     * {@link TextureView}.
     */

    public static int desiredFacing = CameraCharacteristics.LENS_FACING_FRONT;
    public static String desiredFlash = "AUTO";

    static {
        ORIENTATIONS.append(Surface.ROTATION_0, 90);
        ORIENTATIONS.append(Surface.ROTATION_90, 0);
        ORIENTATIONS.append(Surface.ROTATION_180, 270);
        ORIENTATIONS.append(Surface.ROTATION_270, 180);
    }

    static {
        DEFAULT_ORIENTATIONS.append(Surface.ROTATION_0, 90);
        DEFAULT_ORIENTATIONS.append(Surface.ROTATION_90, 0);
        DEFAULT_ORIENTATIONS.append(Surface.ROTATION_180, 270);
        DEFAULT_ORIENTATIONS.append(Surface.ROTATION_270, 180);
    }

    static {
        INVERSE_ORIENTATIONS.append(Surface.ROTATION_0, 270);
        INVERSE_ORIENTATIONS.append(Surface.ROTATION_90, 180);
        INVERSE_ORIENTATIONS.append(Surface.ROTATION_180, 90);
        INVERSE_ORIENTATIONS.append(Surface.ROTATION_270, 0);
    }

    Calendar timer = Calendar.getInstance();
    String from;
    ImageButton switchFlashImgBtn;
    ImageButton switchCameraImgBtn;
    ImageButton capturephotoImgBtn;
    // video instance
    File mVideoFile;
    int actionMove = 0;
    int recordingTimeLimit = 62000;
    int recordDelayedSecond = 4;
    int resID;
    private ProgressBar progressBarCircle;
    private RelativeLayout forthetimer;
    /**
     * ID of the current {@link CameraDevice}.
     */
    private String mCameraId;
    /**
     * An {@link AutoFitTextureView} for camera preview.
     */
    private AutoFitTextureView mTextureView;
    /**
     * A {@link CameraCaptureSession } for camera preview.
     */
    private CameraCaptureSession mCaptureSession;
    /**
     * A reference to the opened {@link CameraDevice}.
     */
    private CameraDevice mCameraDevice;
    /**
     * The {@link android.util.Size} of camera preview.
     */
    private Size mPreviewSize;
    /**
     * An additional thread for running tasks that shouldn't block the UI.
     */
    private HandlerThread mBackgroundThread;
    /**
     * A {@link Handler} for running tasks in the background.
     */
    private Handler mBackgroundHandler;
    /**
     * An {@link ImageReader} that handles still image capture.
     */
    private ImageReader mImageReader;
    /**
     * This is the output file for our picture.
     */
    private File mFile;
    /**
     * This a callback object for the {@link ImageReader}. "onImageAvailable" will be called when a
     * still image is ready to be saved.
     */
    private final ImageReader.OnImageAvailableListener mOnImageAvailableListener
            = new ImageReader.OnImageAvailableListener() {

        @Override
        public void onImageAvailable(ImageReader reader) {
            mBackgroundHandler.post(new ImageSaver(reader.acquireNextImage(), mFile));
        }

    };
    /**
     * {@link CaptureRequest.Builder} for the camera preview
     */
    private CaptureRequest.Builder mPreviewRequestBuilder;
    /**
     * {@link CaptureRequest} generated by {@link #mPreviewRequestBuilder}
     */
    private CaptureRequest mPreviewRequest;
    /**
     * The current state of camera state for taking pictures.
     *
     * @see #mCaptureCallback
     */
    private int mState = STATE_PREVIEW;
    /**
     * A {@link Semaphore} to prevent the app from exiting before closing the camera.
     */
    private Semaphore mCameraOpenCloseLock = new Semaphore(1);
    /**
     * Whether the current camera device supports Flash or not.
     */
    private boolean mFlashSupported;
    /**
     * Orientation of the camera sensor
     */
    private int mSensorOrientation;
    /**
     * A {@link CameraCaptureSession.CaptureCallback} that handles events related to JPEG capture.
     */
    private CameraCaptureSession.CaptureCallback mCaptureCallback = new CameraCaptureSession.CaptureCallback() {
        private void process(CaptureResult result) {
            switch (mState) {
                case STATE_PREVIEW: {
                    // We have nothing to do when the camera preview is working normally.
                    break;
                }
                case STATE_WAITING_LOCK: {
                    Integer afState = result.get(CaptureResult.CONTROL_AF_STATE);
                    if (afState == null||afState == 0) {
                        mState = STATE_PICTURE_TAKEN;
                        captureStillPicture();

                    } else if (CaptureResult.CONTROL_AF_STATE_PASSIVE_FOCUSED == afState || CaptureResult.CONTROL_AF_STATE_FOCUSED_LOCKED == afState ||
                            CaptureResult.CONTROL_AF_STATE_NOT_FOCUSED_LOCKED == afState) {
                        // CONTROL_AE_STATE can be null on some devices
                        Integer aeState = result.get(CaptureResult.CONTROL_AE_STATE);

                        if (aeState == null ||
                                aeState == CaptureResult.CONTROL_AE_STATE_CONVERGED) {
                            mState = STATE_PICTURE_TAKEN;
                            captureStillPicture();
                        } else {
                            runPrecaptureSequence();
                        }
                    }
                    break;
                }
                case STATE_WAITING_PRECAPTURE: {
                    // CONTROL_AE_STATE can be null on some devices
                    Integer aeState = result.get(CaptureResult.CONTROL_AE_STATE);
                    if (aeState == null ||
                            aeState == CaptureResult.CONTROL_AE_STATE_PRECAPTURE ||
                            aeState == CaptureRequest.CONTROL_AE_STATE_FLASH_REQUIRED) {
                        mState = STATE_WAITING_NON_PRECAPTURE;
                    }
                    break;
                }
                case STATE_WAITING_NON_PRECAPTURE: {
                    // CONTROL_AE_STATE can be null on some devices
                    Integer aeState = result.get(CaptureResult.CONTROL_AE_STATE);
                    if (aeState == null || aeState != CaptureResult.CONTROL_AE_STATE_PRECAPTURE) {
                        mState = STATE_PICTURE_TAKEN;
                        captureStillPicture();
                    }
                    break;
                }
            }
        }

        @Override
        public void onCaptureProgressed(@NonNull CameraCaptureSession session,
                                        @NonNull CaptureRequest request,
                                        @NonNull CaptureResult partialResult) {
            process(partialResult);
        }

        @Override
        public void onCaptureCompleted(@NonNull CameraCaptureSession session,
                                       @NonNull CaptureRequest request,
                                       @NonNull TotalCaptureResult result) {
            process(result);
        }

    };
    /**
     * {@link CameraDevice.StateCallback} is called when {@link CameraDevice} changes its state.
     */
    private final CameraDevice.StateCallback mStateCallback = new CameraDevice.StateCallback() {

        @Override
        public void onOpened(@NonNull CameraDevice cameraDevice) {
            // This method is called when the camera is opened.  We start camera preview here.
            mCameraOpenCloseLock.release();
            mCameraDevice = cameraDevice;
            createCameraPreviewSession();
        }

        @Override
        public void onDisconnected(@NonNull CameraDevice cameraDevice) {
            mCameraOpenCloseLock.release();
            cameraDevice.close();
            mCameraDevice = null;
        }

        @Override
        public void onError(@NonNull CameraDevice cameraDevice, int error) {
            mCameraOpenCloseLock.release();
            cameraDevice.close();
            mCameraDevice = null;
            Activity activity = getActivity();
            if (null != activity) {
                activity.finish();
            }
        }

    };
    private CaptureRequest.Builder mPreviewBuilder;
    /**
     * A reference to the current {@link android.hardware.camera2.CameraCaptureSession} for
     * preview.
     */
    private CameraCaptureSession mPreviewSession;
    /**
     * The {@link android.util.Size} of video recording.
     */
    private Size mVideoSize;
    private final TextureView.SurfaceTextureListener mSurfaceTextureListener
            = new TextureView.SurfaceTextureListener() {

        @Override
        public void onSurfaceTextureAvailable(SurfaceTexture texture, int width, int height) {
            if (from.equals("UserProfile") || from.equals("ProfilePic")) {
                desiredFacing = CameraCharacteristics.LENS_FACING_BACK;
                openCamera(width, height, desiredFacing);
            } else {
                openCamera(width, height, desiredFacing);
            }
//            openCamera(1280, 720, desiredFacing);
        }

        @Override
        public void onSurfaceTextureSizeChanged(SurfaceTexture texture, int width, int height) {
            configureTransform(width, height);
//            configureTransform(1280, 720);
        }

        @Override
        public boolean onSurfaceTextureDestroyed(SurfaceTexture texture) {
            return true;
        }

        @Override
        public void onSurfaceTextureUpdated(SurfaceTexture texture) {
        }

    };
    /**
     * Whether the app is recording video now
     */
    private boolean mIsRecordingVideo;
    /**
     * MediaRecorder
     */
    private MediaRecorder mMediaRecorder;
    private TextView recordSeconds_txtview;
    private Handler mHandler = new Handler();
    private boolean wasRun = false;

    /**
     * Given {@code choices} of {@code Size}s supported by a camera, choose the smallest one that
     * is at least as large as the respective texture view size, and that is at most as large as the
     * respective max size, and whose aspect ratio matches with the specified value. If such size
     * doesn't exist, choose the largest one that is at most as large as the respective max size,
     * and whose aspect ratio matches with the specified value.
     *
     * @param choices           The indiScheList of sizes that the camera supports for the intended output
     *                          class
     * @param textureViewWidth  The width of the texture view relative to sensor coordinate
     * @param textureViewHeight The height of the texture view relative to sensor coordinate
     * @param maxWidth          The maximum width that can be chosen
     * @param maxHeight         The maximum height that can be chosen
     * @param aspectRatio       The aspect ratio
     * @return The optimal {@code Size}, or an arbitrary one if none were big enough
     */
    private static Size chooseOptimalSize(Size[] choices, int textureViewWidth,
                                          int textureViewHeight, int maxWidth, int maxHeight, Size aspectRatio) {

//        boolean hasDesiredSize1280720 = false;
//        boolean hasDesiredSize864480 = false;
//        boolean hasDesiredSize800450 = false;
        // Collect the supported resolutions that are at least as big as the preview Surface
        List<Size> bigEnough = new ArrayList<>();
        // Collect the supported resolutions that are smaller than the preview Surface
        List<Size> notBigEnough = new ArrayList<>();
        int w = aspectRatio.getWidth();
        int h = aspectRatio.getWidth() / 16 * 9;

        for (Size option : choices) {
//            if (option.getHeight() == 480 && option.getWidth() == 864) {
//                hasDesiredSize864480 = true;
//            } else if (option.getHeight() == 450 && option.getWidth() == 800) {
//                hasDesiredSize800450 = true;
//            } else if (option.getHeight() == 720 && option.getWidth() == 1280) {
//                hasDesiredSize1280720 = true;
//            }

            if (option.getWidth() <= maxWidth && option.getHeight() <= maxHeight &&
                    option.getHeight() == option.getWidth() * h / w) {
                if (option.getWidth() >= textureViewWidth &&
                        option.getHeight() >= textureViewHeight) {
                    bigEnough.add(option);
                } else {
                    notBigEnough.add(option);
                }
            }
        }

        // Pick the smallest of those big enough. If there is no one big enough, pick the
        // largest of those not big enough.
        if (bigEnough.size() > 0) {
            return Collections.min(bigEnough, new CompareSizesByArea());
        } else if (notBigEnough.size() > 0) {
            return Collections.max(notBigEnough, new CompareSizesByArea());
        } else {
            //was choices[0] its 4:3, changed to choices[1] its 16:9

//            if (hasDesiredSize864480) {
//                return new Size(864, 480);
//            } else if (hasDesiredSize800450) {
//                return new Size(800, 450);
//            } else if (hasDesiredSize1280720) {
//                return new Size(1280, 720);
//            }
            return choices[0];
        }
    }

    public static Camera2Fragment newInstance() {
        return new Camera2Fragment();
    }

    private static Size chooseVideoSize(Size[] choices) {
        for (Size size : choices) {
            if (size.getWidth() == 864 && size.getHeight() == 480) {
                return size;
            } else if (size.getWidth() == 800 && size.getHeight() == 450) {
                return size;
            } else if (size.getWidth() == 1280 && size.getHeight() == 720) {
                return size;
            }
//            if (size.getWidth() == size.getHeight() * 4 / 3 && size.getWidth() <= 1080) {
//                return size;
//            }
        }
        return choices[choices.length - 1];
    }

    /**
     * Shows a {@link Toast} on the UI thread.
     *
     * @param text The message to show
     */
    private void showToast(final String text) {
        final Activity activity = getActivity();
        if (activity != null) {
            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(activity, text, Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_camera2, container, false);
    }

    protected CameraCharacteristics cameraCharacteristics;
//    protected CameraCaptureSession captureSession;
//    protected CaptureRequest.Builder previewRequestBuilder;

    //Zooming
    protected float fingerSpacing = 0;
    protected float zoomLevel = 1f;
    protected float maximumZoomLevel;
    protected Rect zoom;
    private ImageButton imgbtn_focus;

    @Override
    public void onViewCreated(final View view, Bundle savedInstanceState) {
        sensorManager = (SensorManager) getActivity().getSystemService(SENSOR_SERVICE);
        Intent intent = getActivity().getIntent();
        from = intent.getStringExtra("from");
        view.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent event) {
//                if(event.getPointerCount() == 1) {
//                    switch (event.getAction()) {
//                        case MotionEvent.ACTION_DOWN:
//                            imgbtn_focus.setVisibility(View.VISIBLE);
//                            Rect rect = cameraCharacteristics.get(CameraCharacteristics.SENSOR_INFO_ACTIVE_ARRAY_SIZE);
//
//                            final int y = (int)((event.getX() / (float)view.getWidth())  * (float)rect.height());
//                            final int x = (int)((event.getY() / (float)view.getHeight()) * (float)rect.width());
//                            final int halfTouchWidth  = 150; //(int)motionEvent.getTouchMajor(); //
// TODO: this doesn't represent actual touch size in pixel. Values range in [3, 10]...
//                            final int halfTouchHeight = 150; //(int)motionEvent.getTouchMinor();
//                            MeteringRectangle focusAreaTouch = new MeteringRectangle(Math.max(x - halfTouchWidth,  0),
//                                    Math.max(y - halfTouchHeight, 0),
//                                    halfTouchWidth  * 2,
//                                    halfTouchHeight * 2,
//                                    MeteringRectangle.METERING_WEIGHT_MAX - 1);
//                            mPreviewRequestBuilder.set(CaptureRequest.CONTROL_AF_REGIONS, new MeteringRectangle[]{focusAreaTouch});
//                            RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT); //The WRAP_CONTENT parameters can be replaced by an absolute width and height or the FILL_PARENT option)
//                            params.leftMargin = (int)event.getX(); //Your X coordinate
//                            params.topMargin = (int)event.getY(); //Your Y coordinate
//                            imgbtn_focus.setLayoutParams(params);
//                            break;
//
//                        case MotionEvent.ACTION_UP:
//                            imgbtn_focus.setVisibility(View.GONE);
//                            break;
//                    }
//                }
                try {
                    Rect rect = cameraCharacteristics.get(CameraCharacteristics.SENSOR_INFO_ACTIVE_ARRAY_SIZE);
                    if (rect == null) return false;

                    float currentFingerSpacing;
                    if (event.getPointerCount() == 2) { //Multi touch.
                        currentFingerSpacing = getFingerSpacing(event);
                        float delta = 0.05f; //Control this value to control the zooming sensibility
                        if (fingerSpacing != 0) {
                            if (currentFingerSpacing > fingerSpacing) { //Don't over zoom-in
                                if ((maximumZoomLevel - zoomLevel) <= delta) {
                                    delta = maximumZoomLevel - zoomLevel;
                                }
                                zoomLevel = zoomLevel + delta;
                            } else if (currentFingerSpacing < fingerSpacing) { //Don't over zoom-out
                                if ((zoomLevel - delta) < 1f) {
                                    delta = zoomLevel - 1f;
                                }
                                zoomLevel = zoomLevel - delta;
                            }
                            float ratio = (float) 1 / zoomLevel; //This ratio is the ratio of cropped Rect to Camera's original(Maximum) Rect
                            //croppedWidth and croppedHeight are the pixels cropped away, not pixels after cropped
                            int croppedWidth = rect.width() - Math.round((float) rect.width() * ratio);
                            int croppedHeight = rect.height() - Math.round((float) rect.height() * ratio);
                            //Finally, zoom represents the zoomed visible area
                            zoom = new Rect(croppedWidth / 2, croppedHeight / 2,
                                    rect.width() - croppedWidth / 2, rect.height() - croppedHeight / 2);
                            mPreviewRequestBuilder.set(CaptureRequest.SCALER_CROP_REGION, zoom);
                        }
                        fingerSpacing = currentFingerSpacing;
                    } else { //Single touch point, needs to return true in order to detect one more touch point
                        return true;
                    }
                    mCaptureSession.setRepeatingRequest(mPreviewRequestBuilder.build(), mCaptureCallback, null);
                    return true;
                } catch (final Exception e) {
                    //Error handling up to you
                    return true;
                }
//                return false;
            }
        });
        if (intent.hasExtra("resID")) {
            resID = intent.getIntExtra("resID", -1);
        }
        //camera focus
        imgbtn_focus = view.findViewById(R.id.imgbtn_focus);
        // timer record
        recordSeconds_txtview = view.findViewById(R.id.recordSeconds_txtview);
        progressBarCircle = view.findViewById(R.id.progressBarCircle);
        forthetimer = view.findViewById(R.id.forthetimer);

        capturephotoImgBtn = view.findViewById(R.id.capturePhoto2);
        capturephotoImgBtn.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                switch (motionEvent.getAction()) {
                    case MotionEvent.ACTION_DOWN:

                        if (!from.equals("UserProfile") && !from.equals("ProfilePic") && !from.equals
                                ("GroupDetail") && !from.equals("CreateGroup")) {
                            wasRun = true;
                            // UI
                            capturephotoImgBtn.setImageResource(R.drawable.ic_camera_red_300px);

                            mHandler.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    if (wasRun) {
                                        actionMove++;
                                        if (PackageManager.PERMISSION_DENIED == getContext().checkCallingOrSelfPermission(Manifest.permission.RECORD_AUDIO)) {

                                            new PermissionHelper().CheckPermissions(getContext(), 1001, R.string.permission_txt);


                                        } else {
                                            if (actionMove >= 1 && !mIsRecordingVideo) {
                                                startRecordingVideo();
                                            }
                                            if (mIsRecordingVideo) {
                                                if (actionMove > 1) {
                                                    forthetimer.setVisibility(View.VISIBLE);

                                                }
                                                timer.setTimeInMillis((actionMove - 2) * 1000);
                                                int TSec = timer.get(Calendar.SECOND);
                                                int TMin = timer.get(Calendar.MINUTE) - 30;

                                                String TTimer = String.format(Locale.ENGLISH, "%02d:%02d",
                                                        TMin, TSec);

                                                progressBarCircle.setProgress(timer.get(Calendar.SECOND));
                                                recordSeconds_txtview.setText(TTimer);

                                            }
                                            mHandler.postDelayed(this, 1000);

                                            //whatever you want to do if run
                                            //you can add you want to increase variable here
                                        }
                                    }
                                }

                            }, 1000);

                        }
                        break;
                    case MotionEvent.ACTION_UP:
                        wasRun = false;
                        capturephotoImgBtn.setImageResource(android.R.color.transparent);
                        mHandler.removeCallbacksAndMessages(null);
                        if (PackageManager.PERMISSION_GRANTED == getContext().checkCallingOrSelfPermission(Manifest.permission.RECORD_AUDIO)) {
                            if (actionMove > 1 && mIsRecordingVideo) {

                                stopRecordingVideo();
                                forthetimer.setVisibility(View.GONE);
                                actionMove = 0;

                            } else {
                                takePicture();
                                actionMove = 0;

                            }
                        }
                        break;
                }
                return false;
            }
        });
        capturephotoImgBtn.setOnClickListener(this);
        switchCameraImgBtn = view.findViewById(R.id.switchCamera2);
        switchCameraImgBtn.setOnClickListener(this);
        switchFlashImgBtn = view.findViewById(R.id.switchFlash2);
        switchFlashImgBtn.setOnClickListener(this);
        switchFlashImgBtn.setImageResource(R.drawable.xml_ic_flash_auto_black_24dp);
//        view.findViewById(R.id.info).setOnClickListener(this);
        mTextureView = view.findViewById(R.id.texture);
    }

    private float getFingerSpacing(MotionEvent event) {
        float x = event.getX(0) - event.getX(1);
        float y = event.getY(0) - event.getY(1);
        return (float) Math.sqrt(x * x + y * y);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        mFile = new File(getActivity().getCacheDir(), "pic.jpg");
    }

    @Override
    public void onResume() {
        super.onResume();
        sensorManager.registerListener(this, sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD), SensorManager.SENSOR_DELAY_GAME);
        sensorManager.registerListener(this, sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_NORMAL);
        forthetimer.setVisibility(View.GONE);
        startBackgroundThread();

        // When the screen is turned off and turned back on, the SurfaceTexture is already
        // available, and "onSurfaceTextureAvailable" will not be called. In that case, we can open
        // a camera and start preview from here (otherwise, we wait until the surface is ready in
        // the SurfaceTextureListener).
        if (mTextureView.isAvailable()) {
            openCamera(mTextureView.getWidth(), mTextureView.getHeight(), desiredFacing);
        } else {
            mTextureView.setSurfaceTextureListener(mSurfaceTextureListener);
        }
        fingerSpacing = 0;
        zoomLevel = 1f;
        if (cameraCharacteristics != null) {
            zoom = cameraCharacteristics.get(CameraCharacteristics.SENSOR_INFO_ACTIVE_ARRAY_SIZE);
        }
    }

    @Override
    public void onPause() {
        closeCamera();
        stopBackgroundThread();
        forthetimer.setVisibility(View.GONE);
        sensorManager.unregisterListener(this);

        //release recording state
        if (mMediaRecorder != null) {
            mMediaRecorder.release();
        }

        super.onPause();
    }

    @Override
    public void onStop() {
        //release recording state
        if (mMediaRecorder != null) {
            mMediaRecorder.release();
        }

        super.onStop();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case 1001:
                for (int i = 0, len = permissions.length; i < len; i++) {
                    String permission = permissions[i];

                    if (grantResults[i] == PackageManager.PERMISSION_DENIED) {
                        boolean showRationale = false;
                        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                            showRationale = shouldShowRequestPermissionRationale(permission);

                        }

                        if (!showRationale) {

                            preferences.save(getContext(), "askAgain", "false");
                            Toast.makeText(getContext(), R.string.need_camera, Toast.LENGTH_SHORT)
                                    .show();
                            break;
                        }

                    } else {

                        if (permission.equals(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                            new DirectoryHelper().checkAndCreateDir(getContext(), true);
                        }
                        openCamera(mTextureView.getWidth(), mTextureView.getHeight(), desiredFacing);
                        break;
                    }
                }
                break;
        }

//        if (requestCode == REQUEST_CAMERA_PERMISSION) {
//            if (grantResults.length == CAMERA_PERMISSIONS.length) {
//                for (int result : grantResults) {
//                    if (result != PackageManager.PERMISSION_GRANTED) {
//                        ErrorDialog.newInstance("give me permission")
//                                .show(getChildFragmentManager(), FRAGMENT_DIALOG);
//                        break;
//                    }
//                }
//            } else {
//                ErrorDialog.newInstance("give me permission")
//                        .show(getChildFragmentManager(), FRAGMENT_DIALOG);
//            }
////            if (grantResults.length != 1 || grantResults[0] != PackageManager.PERMISSION_GRANTED) {
////                ErrorDialog.newInstance("give me permission")
////                        .show(getChildFragmentManager(), FRAGMENT_DIALOG);
////            }
//        } else {
//            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
//        }

    }

    private boolean hasPermissionsGranted(String[] permissions) {
        for (String permission : permissions) {
            if (ActivityCompat.checkSelfPermission(getActivity(), permission)
                    != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

    /**
     * Sets up member variables related to camera.
     *
     * @param width  The width of available size for camera preview
     * @param height The height of available size for camera preview
     */
    @RequiresApi(api = Build.VERSION_CODES.M)
    @SuppressWarnings("SuspiciousNameCombination")
    private void setUpCameraOutputs(int width, int height, int desiredFacing) {
        Activity activity = getActivity();
        CameraManager manager = (CameraManager) activity.getSystemService(Context.CAMERA_SERVICE);
        try {
            for (String cameraId : manager.getCameraIdList()) {

                CameraCharacteristics characteristics
                        = manager.getCameraCharacteristics(cameraId);

                cameraCharacteristics = characteristics;
                StreamConfigurationMap map = characteristics.get(
                        CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
                mVideoSize = chooseVideoSize(map.getOutputSizes(MediaRecorder.class));
                maximumZoomLevel = characteristics.get(CameraCharacteristics.SCALER_AVAILABLE_MAX_DIGITAL_ZOOM);
                // We don't use a front facing camera in this sample.
                Integer facing = characteristics.get(CameraCharacteristics.LENS_FACING);

                if (facing != null && facing == desiredFacing) {
                    continue;
                }

//                StreamConfigurationMap map = characteristics.get(
//                        CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
                if (map == null) {
                    continue;
                }

                // For still image captures, we use the largest available size.
                Size largest = Collections.max(Arrays.asList(map.getOutputSizes(ImageFormat.JPEG)),
                        new CompareSizesByArea());

                //set camera image output i guess
                mImageReader = ImageReader.newInstance(1280, 720,
                        ImageFormat.JPEG, /*maxImages*/MAX_IMAGES);
                mImageReader.setOnImageAvailableListener(
                        mOnImageAvailableListener, mBackgroundHandler);

                // Find out if we need to swap dimension to get the preview size relative to sensor
                // coordinate.
                int displayRotation = activity.getWindowManager().getDefaultDisplay().getRotation();

                //noinspection ConstantConditions
                mSensorOrientation = characteristics.get(CameraCharacteristics.SENSOR_ORIENTATION);
                boolean swappedDimensions = false;
                switch (displayRotation) {
                    case Surface.ROTATION_0:
                        break;

                    case Surface.ROTATION_180:
                        if (mSensorOrientation == 90 || mSensorOrientation == 270) {
                            swappedDimensions = true;
                        }
                        break;

                    case Surface.ROTATION_90:
                        break;

                    case Surface.ROTATION_270:
                        if (mSensorOrientation == 0 || mSensorOrientation == 180) {
                            swappedDimensions = true;
                        }
                        break;

                    default:
                        break;
                }

                Point displaySize = new Point();
                activity.getWindowManager().getDefaultDisplay().getSize(displaySize);
                int rotatedPreviewWidth = width;
                int rotatedPreviewHeight = height;
                int maxPreviewWidth = displaySize.x;
                int maxPreviewHeight = displaySize.y;

                if (swappedDimensions) {
                    rotatedPreviewWidth = height;
                    rotatedPreviewHeight = width;
                    maxPreviewWidth = displaySize.y;
                    maxPreviewHeight = displaySize.x;
                }

                if (maxPreviewWidth > MAX_PREVIEW_WIDTH) {
                    maxPreviewWidth = MAX_PREVIEW_WIDTH;
                }

                if (maxPreviewHeight > MAX_PREVIEW_HEIGHT) {
                    maxPreviewHeight = MAX_PREVIEW_HEIGHT;
                }

                // Danger, W.R.! Attempting to use too large a preview size could  exceed the camera
                // bus' bandwidth limitation, resulting in gorgeous previews but the storage of
                // garbage capture data.
                mPreviewSize = chooseOptimalSize(map.getOutputSizes(SurfaceTexture.class),
                        rotatedPreviewWidth, rotatedPreviewHeight, maxPreviewWidth,
                        maxPreviewHeight, largest);

                // We fit the aspect ratio of TextureView to the size of preview we picked.
                int orientation = getResources().getConfiguration().orientation;
                if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
                    mTextureView.setAspectRatio(
                            mPreviewSize.getWidth(), mPreviewSize.getHeight());
                } else {
                    mTextureView.setAspectRatio(
                            mPreviewSize.getHeight(), mPreviewSize.getWidth());
                }

                // Check if the flash is supported.
                Boolean available = characteristics.get(CameraCharacteristics.FLASH_INFO_AVAILABLE);
                mFlashSupported = available == null ? false : available;

                mCameraId = cameraId;
                return;
            }
        } catch (CameraAccessException e) {
            e.printStackTrace();
        } catch (NullPointerException e) {
            // Currently an NPE is thrown when the Camera2API is used but not supported on the
            // device this code runs.
            ErrorDialog.newInstance("camera error").show(getChildFragmentManager(), FRAGMENT_DIALOG);
            e.printStackTrace();
        }
    }

    /**
     * Opens the camera specified by {@link Camera2Fragment#mCameraId}.
     */
    private void openCamera(int width, int height, int desiredFacing) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            setUpCameraOutputs(width, height, desiredFacing);

            configureTransform(width, height);
            Activity activity = getActivity();

            if (activity != null) {
                CameraManager manager = (CameraManager) activity.getSystemService(Context.CAMERA_SERVICE);
                try {
                    // was 2500
                    if (!mCameraOpenCloseLock.tryAcquire(1000, TimeUnit.MILLISECONDS)) {
                        throw new RuntimeException("Time out waiting to lock camera opening.");
                    }

                    if (activity.checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                        // TODO: Consider calling
                        //    Activity#requestPermissions
                        // here to request the missing permissions, and then overriding
                        //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                        //                                          int[] grantResults)
                        // to handle the case where the user grants the permission. See the documentation
                        // for Activity#requestPermissions for more details.
                        return;
                    }

                    if (manager != null) {
                        manager.openCamera(mCameraId, mStateCallback, mBackgroundHandler);
//                    } else {
//                        new CameraHelper().finishCameraIntent();
                    }
                } catch (CameraAccessException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    throw new RuntimeException("Interrupted while trying to lock camera opening.", e);
                }
            }
//        } else {
//            new CameraHelper().finishCameraIntent();
        }
    }

    /**
     * Closes the current {@link CameraDevice}.
     */
    private void closeCamera() {
        try {
            mCameraOpenCloseLock.acquire();
            if (null != mCaptureSession) {
                mCaptureSession.close();
                mCaptureSession = null;
            }
            if (null != mCameraDevice) {
                mCameraDevice.close();
                mCameraDevice = null;
            }
            if (null != mImageReader) {
                mImageReader.close();
                mImageReader = null;
            }
        } catch (InterruptedException e) {
            throw new RuntimeException("Interrupted while trying to lock camera closing.", e);
        } finally {
            mCameraOpenCloseLock.release();
        }
    }

    /**
     * Starts a background thread and its {@link Handler}.
     */
    private void startBackgroundThread() {
        mBackgroundThread = new HandlerThread("CameraBackground");
        mBackgroundThread.start();
        mBackgroundHandler = new Handler(mBackgroundThread.getLooper());
    }

    /**
     * Stops the background thread and its {@link Handler}.
     */
    private void stopBackgroundThread() {
        mBackgroundThread.quitSafely();
        try {
            mBackgroundThread.join();
            mBackgroundThread = null;
            mBackgroundHandler = null;
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * Creates a new {@link CameraCaptureSession} for camera preview.
     */
    private void createCameraPreviewSession() {
        try {
            SurfaceTexture texture = mTextureView.getSurfaceTexture();
            assert texture != null;

            // We configure the size of default buffer to be the size of camera preview we want.
            texture.setDefaultBufferSize(mPreviewSize.getWidth(), mPreviewSize.getHeight());

            // This is the output Surface we need to start preview.
            Surface surface = new Surface(texture);

            // We set up a CaptureRequest.Builder with the output Surface.
            mPreviewRequestBuilder
                    = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
            mPreviewRequestBuilder.addTarget(surface);

            // Here, we create a CameraCaptureSession for camera preview.
            mCameraDevice.createCaptureSession(Arrays.asList(surface, mImageReader.getSurface()),
                    new CameraCaptureSession.StateCallback() {

                        @Override
                        public void onConfigured(@NonNull CameraCaptureSession cameraCaptureSession) {
                            // The camera is already closed
                            if (null == mCameraDevice) {
                                return;
                            }

                            // When the session is ready, we start displaying the preview.
                            mCaptureSession = cameraCaptureSession;
                            try {
                                // Auto focus should be continuous for camera preview.
                                mPreviewRequestBuilder.set(CaptureRequest.CONTROL_AF_MODE,
                                        CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE);
                                // Flash is automatically enabled when necessary.
                                setAutoFlash(mPreviewRequestBuilder);

                                // Finally, we start displaying the camera preview.
                                mPreviewRequest = mPreviewRequestBuilder.build();
                                mCaptureSession.setRepeatingRequest(mPreviewRequest,
                                        mCaptureCallback, mBackgroundHandler);
                            } catch (CameraAccessException e) {
                                e.printStackTrace();
                            }
                        }

                        @Override
                        public void onConfigureFailed(
                                @NonNull CameraCaptureSession cameraCaptureSession) {
                            showToast("Failed");
                        }
                    }, null
            );
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    /**
     * Configures the necessary {@link android.graphics.Matrix} transformation to `mTextureView`.
     * This method should be called after the camera preview size is determined in
     * setUpCameraOutputs and also the size of `mTextureView` is fixed.
     *
     * @param viewWidth  The width of `mTextureView`
     * @param viewHeight The height of `mTextureView`
     */
    private void configureTransform(int viewWidth, int viewHeight) {
        Activity activity = getActivity();
        if (null == mTextureView || null == mPreviewSize || null == activity) {
            return;
        }
        int rotation = activity.getWindowManager().getDefaultDisplay().getRotation();
        Matrix matrix = new Matrix();
        RectF viewRect = new RectF(0, 0, viewWidth, viewHeight);
        RectF bufferRect = new RectF(0, 0, mPreviewSize.getHeight(), mPreviewSize.getWidth());
        float centerX = viewRect.centerX();
        float centerY = viewRect.centerY();
        if (Surface.ROTATION_90 == rotation || Surface.ROTATION_270 == rotation) {
            bufferRect.offset(centerX - bufferRect.centerX(), centerY - bufferRect.centerY());
            matrix.setRectToRect(viewRect, bufferRect, Matrix.ScaleToFit.FILL);
            float scale = Math.max(
                    (float) viewHeight / mPreviewSize.getHeight(),
                    (float) viewWidth / mPreviewSize.getWidth());
            matrix.postScale(scale, scale, centerX, centerY);
            matrix.postRotate(90 * (rotation - 2), centerX, centerY);
        } else if (Surface.ROTATION_180 == rotation) {
            matrix.postRotate(180, centerX, centerY);
        }
        mTextureView.setTransform(matrix);
    }

    /**
     * Initiate a still image capture.
     */
    private void takePicture() {
        lockFocus();
    }

    /**
     * Lock the focus as the first step for a still image capture.
     */
    private void lockFocus() {
        try {
            // This is how to tell the camera to lock focus.
            mPreviewRequestBuilder.set(CaptureRequest.CONTROL_AF_TRIGGER,
                    CameraMetadata.CONTROL_AF_TRIGGER_START);
            // Tell #mCaptureCallback to wait for the lock.
            mState = STATE_WAITING_LOCK;

            mCaptureSession.capture(mPreviewRequestBuilder.build(), mCaptureCallback, mBackgroundHandler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    /**
     * Run the precapture sequence for capturing a still image. This method should be called when
     * we get a response in {@link #mCaptureCallback} from {@link #lockFocus()}.
     */
    private void runPrecaptureSequence() {
        try {
            // This is how to tell the camera to trigger.
            mPreviewRequestBuilder.set(CaptureRequest.CONTROL_AE_PRECAPTURE_TRIGGER,
                    CaptureRequest.CONTROL_AE_PRECAPTURE_TRIGGER_START);
            // Tell #mCaptureCallback to wait for the precapture sequence to be set.
            mState = STATE_WAITING_PRECAPTURE;
            mCaptureSession.capture(mPreviewRequestBuilder.build(), mCaptureCallback, mBackgroundHandler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    /**
     * Capture a still picture. This method should be called when we get a response in
     * {@link #mCaptureCallback} from both {@link #lockFocus()}.
     */
    private void captureStillPicture() {
        try {
            final Activity activity = getActivity();
            if (null == activity || null == mCameraDevice) {
                return;
            }
            // This is the CaptureRequest.Builder that we use to take a picture.
            final CaptureRequest.Builder captureBuilder = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE);
            captureBuilder.addTarget(mImageReader.getSurface());
            if (zoom != null) {
                captureBuilder.set(CaptureRequest.SCALER_CROP_REGION, zoom);
            }
            // Use the same AE and AF modes as the preview.
            captureBuilder.set(CaptureRequest.CONTROL_AF_MODE, CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE);

            if (desiredFlash.equals("AUTO")) {
                setAutoFlash(captureBuilder);
            } else if (desiredFlash.equals("OFF")) {
                setOffFlash(captureBuilder);
            } else {
                setOnFlash(captureBuilder);
            }

            // Orientation
            int rotation = activity.getWindowManager().getDefaultDisplay().getRotation();
            switch (mSensorOrientation) {
                case SENSOR_ORIENTATION_DEFAULT_DEGREES:
                    switch (sensor_orientation) {
                        case "inverse_portrait":
                            rotation = 270;
                            break;
                        case "landscape_right":
                            rotation = 0;
                            break;
                        case "landscape_left":
                            rotation = 180;
                            break;
                        default:
                            rotation = 90;
                            break;
                    }
                    break;

                case SENSOR_ORIENTATION_INVERSE_DEGREES:
                    switch (sensor_orientation) {
                        case "inverse_portrait":
                            rotation = 90;
                            break;
                        case "landscape_right":
                            rotation = 0;
                            break;
                        case "landscape_left":
                            rotation = 180;
                            break;
                        default:
                            rotation = 270;
                            break;
                    }
                    break;
            }

//            captureBuilder.set(CaptureRequest.JPEG_ORIENTATION, getOrientation(rotation));
            captureBuilder.set(CaptureRequest.JPEG_ORIENTATION, rotation);

            CameraCaptureSession.CaptureCallback CaptureCallback = new CameraCaptureSession.CaptureCallback() {

                @Override
                public void onCaptureCompleted(@NonNull CameraCaptureSession session,
                                               @NonNull CaptureRequest request,
                                               @NonNull TotalCaptureResult result) {
                    unlockFocus();
                    Intent intent;
                    if (from.equals("chat") || from.equals("chatmedia")) {
                        intent = new Intent(getActivity(), ImageNormalPreviewActivity.class);
                        intent.putExtra("image_address", mFile.getPath());
                        intent.putExtra("jid", getActivity().getIntent().getStringExtra("jid"));
                        intent.putExtra("from", from);
                        intent.putExtra("resID", resID);
                        intent.putExtra("sensor_orientation", sensor_orientation);
                        intent.putExtra("camera", "yes");
                        startActivity(intent);
                    } else {
                        intent = new Intent(getActivity(), ImageCropPreviewActivity.class);
                        intent.putExtra("image_address", mFile.getPath());
                        intent.putExtra("jid", getActivity().getIntent().getStringExtra("jid"));
                        intent.putExtra("from", from);
                        intent.putExtra("resID", resID);
                        intent.putExtra("sensor_orientation", sensor_orientation);
                        intent.putExtra("camera", "yes");
                        startActivity(intent);
                    }
                }
            };

            mCaptureSession.stopRepeating();
//            mCaptureSession.abortCaptures();

            mCaptureSession.capture(captureBuilder.build(), CaptureCallback, null);

        } catch (CameraAccessException e) {
            mCameraDevice.close();
            new MiscHelper().retroLogFailure(e, "camera2CameraAccess Exception", "JAY");
            e.printStackTrace();
        }
    }

    /**
     * Retrieves the JPEG orientation from the specified screen rotation.
     *
     * @param rotation The screen rotation.
     * @return The JPEG orientation (one of 0, 90, 270, and 360)
     */
    private int getOrientation(int rotation) {
        // Sensor orientation is 90 for most devices, or 270 for some devices (eg. Nexus 5X)
        // We have to take that into account and rotate JPEG properly.
        // For devices with orientation of 90, we simply return our mapping from ORIENTATIONS.
        // For devices with orientation of 270, we need to rotate the JPEG 180 degrees.

        return (ORIENTATIONS.get(rotation) + mSensorOrientation + 270) % 360;
    }

    /**
     * Unlock the focus. This method should be called when still image capture sequence is
     * finished.
     */
    private void unlockFocus() {
        try {
            // Reset the auto-focus trigger
            mPreviewRequestBuilder.set(CaptureRequest.CONTROL_AF_TRIGGER,
                    CameraMetadata.CONTROL_AF_TRIGGER_CANCEL);
            setAutoFlash(mPreviewRequestBuilder);
            mCaptureSession.capture(mPreviewRequestBuilder.build(), mCaptureCallback, mBackgroundHandler);
            // After this, the camera will go back to the normal state of preview.
            mState = STATE_PREVIEW;
            mCaptureSession.setRepeatingRequest(mPreviewRequest, mCaptureCallback, mBackgroundHandler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onDestroy() {
        desiredFacing = CameraCharacteristics.LENS_FACING_FRONT;

        super.onDestroy();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.capturePhoto2: {
//                takePicture();
//                startRecordingVideo();
                break;
            }
            case R.id.switchCamera2: {
                closeCamera();
                if (mTextureView.isAvailable()) {
                    if (desiredFacing != CameraCharacteristics.LENS_FACING_FRONT) {
                        mCameraId = "0";
                        desiredFacing = CameraCharacteristics.LENS_FACING_FRONT;
                        openCamera(mTextureView.getWidth(), mTextureView.getHeight(), desiredFacing);
                    } else {
                        mCameraId = "1";
                        desiredFacing = CameraCharacteristics.LENS_FACING_BACK;
                        openCamera(mTextureView.getWidth(), mTextureView.getHeight(), desiredFacing);
                    }
                } else {
                    mTextureView.setSurfaceTextureListener(mSurfaceTextureListener);
                }
                break;
            }
            case R.id.switchFlash2: {

                if (desiredFlash.equals("AUTO")) {
                    desiredFlash = "OFF";
                    switchFlashImgBtn.setImageResource(R.drawable.xml_ic_flash_off_black_24dp);
                } else if (desiredFlash.equals("OFF")) {
                    desiredFlash = "ON";
                    switchFlashImgBtn.setImageResource(R.drawable.xml_ic_flash_on_black_24dp);
                } else {
                    desiredFlash = "AUTO";
                    switchFlashImgBtn.setImageResource(R.drawable.xml_ic_flash_auto_black_24dp);
                }
                break;
            }
        }
    }

    private void setAutoFlash(CaptureRequest.Builder requestBuilder) {
        if (mFlashSupported) {
            requestBuilder.set(CaptureRequest.CONTROL_AE_MODE,
                    CaptureRequest.CONTROL_AE_MODE_ON_AUTO_FLASH);
        }
    }

    private void setOffFlash(CaptureRequest.Builder requestBuilder) {
        if (mFlashSupported) {
            requestBuilder.set(CaptureRequest.CONTROL_AE_MODE,
                    CaptureRequest.CONTROL_AE_MODE_OFF);
        }
    }

    private void setOnFlash(CaptureRequest.Builder requestBuilder) {
        if (mFlashSupported) {
            requestBuilder.set(CaptureRequest.CONTROL_AE_MODE,
                    CaptureRequest.CONTROL_AE_MODE_ON_ALWAYS_FLASH);
        }
    }

    //video start
    private void stopRecordingVideo() {
        // UI
        mIsRecordingVideo = false;
        // Stop recording
        mMediaRecorder.stop();
        mMediaRecorder.reset();

        Intent intent = new Intent(getActivity(), ImageNormalPreviewActivity.class);
        intent.putExtra("video", Uri.fromFile(mVideoFile).getPath());
        intent.putExtra("jid", getActivity().getIntent().getStringExtra("jid"));
        intent.putExtra("from", from);
        intent.putExtra("camera", "yes");
        startActivity(intent);
//        startPreview();
    }

    private void closePreviewSession() {
        if (mPreviewSession != null) {
            mPreviewSession.close();
            mPreviewSession = null;
        }
    }

    private void setUpMediaRecorder() throws IOException {
        mMediaRecorder = new MediaRecorder();
        final Activity activity = getActivity();
        if (null == activity) {
            return;
        }
        mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mMediaRecorder.setVideoSource(MediaRecorder.VideoSource.SURFACE);
        mMediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
//        if (mNextVideoAbsolutePath == null || mNextVideoAbsolutePath.isEmpty()) {
//            mNextVideoAbsolutePath = getVideoFilePath(getActivity());
//        }
        String timeStamp = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
        File file = new File(GlobalVariables.VIDEO_SENT_PATH + "/", "VID" + timeStamp + ".mp4");
        mVideoFile = file;
        mMediaRecorder.setOutputFile(file.getPath());
        mMediaRecorder.setVideoEncodingBitRate(2000000);
        mMediaRecorder.setVideoFrameRate(30);
        //ryan pls check for the video size available
        mMediaRecorder.setVideoSize(mVideoSize.getWidth(), mVideoSize.getHeight());

//        mMediaRecorder.setVideoSize(1280, 720);
        mMediaRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.H264);
//        mMediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
        mMediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.DEFAULT);
        mMediaRecorder.setAudioEncodingBitRate(30);

        switch (mSensorOrientation) {
            case SENSOR_ORIENTATION_DEFAULT_DEGREES:
                switch (sensor_orientation) {
                    case "inverse_portrait":
                        mMediaRecorder.setOrientationHint(INVERSE_ORIENTATIONS.get(0));
                        break;
                    case "landscape_right":
                        mMediaRecorder.setOrientationHint(DEFAULT_ORIENTATIONS.get(8));
                        break;
                    case "landscape_left":
                        mMediaRecorder.setOrientationHint(DEFAULT_ORIENTATIONS.get(3));
                        break;
                    default:
                        mMediaRecorder.setOrientationHint(DEFAULT_ORIENTATIONS.get(0));
                        break;
                }
                break;
            case SENSOR_ORIENTATION_INVERSE_DEGREES:
                switch (sensor_orientation) {
                    case "inverse_portrait":
                        mMediaRecorder.setOrientationHint(DEFAULT_ORIENTATIONS.get(0));
                        break;
                    case "landscape_right":
//                        mMediaRecorder.setOrientationHint(INVERSE_ORIENTATIONS.get(1));
                        mMediaRecorder.setOrientationHint(DEFAULT_ORIENTATIONS.get(8));
                        break;
                    case "landscape_left":
//                        mMediaRecorder.setOrientationHint(INVERSE_ORIENTATIONS.get(6));
                        mMediaRecorder.setOrientationHint(DEFAULT_ORIENTATIONS.get(3));
                        break;
                    default:
                        mMediaRecorder.setOrientationHint(INVERSE_ORIENTATIONS.get(0));
                        break;
                }
                break;
        }
        mMediaRecorder.prepare();
    }
    //////// ---------- take picture -------------- //////////////

    /**
     * Update the camera preview. {@link #startPreview()} needs to be called in advance.
     */
    private void updatePreview() {
        if (null == mCameraDevice) {
            return;
        }
        try {
            setUpCaptureRequestBuilder(mPreviewBuilder);
            HandlerThread thread = new HandlerThread("CameraPreview");
            thread.start();
            mPreviewSession.setRepeatingRequest(mPreviewBuilder.build(), null, mBackgroundHandler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    private void setUpCaptureRequestBuilder(CaptureRequest.Builder builder) {
        builder.set(CaptureRequest.CONTROL_MODE, CameraMetadata.CONTROL_AF_MODE_AUTO);
    }

    private void startPreview() {
        if (null == mCameraDevice || !mTextureView.isAvailable() || null == mPreviewSize) {
            return;
        }
        try {
            closePreviewSession();
            SurfaceTexture texture = mTextureView.getSurfaceTexture();
            assert texture != null;
            texture.setDefaultBufferSize(mPreviewSize.getWidth(), mPreviewSize.getHeight());
            mPreviewBuilder = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);

            Surface previewSurface = new Surface(texture);
            mPreviewBuilder.addTarget(previewSurface);

            mCameraDevice.createCaptureSession(Collections.singletonList(previewSurface),
                    new CameraCaptureSession.StateCallback() {

                        @Override
                        public void onConfigured(@NonNull CameraCaptureSession session) {
                            mPreviewSession = session;
                            updatePreview();
                        }

                        @Override
                        public void onConfigureFailed(@NonNull CameraCaptureSession session) {
                            Activity activity = getActivity();
                            if (null != activity) {
                                Toast.makeText(activity, "Failed", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }, mBackgroundHandler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    private void startRecordingVideo() {
        if (null == mCameraDevice || !mTextureView.isAvailable() || null == mPreviewSize) {
            return;
        }


        try {
            closePreviewSession();
            setUpMediaRecorder();
            SurfaceTexture texture = mTextureView.getSurfaceTexture();

            assert texture != null;
            texture.setDefaultBufferSize(mPreviewSize.getWidth(), mPreviewSize.getHeight());

            mPreviewBuilder = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_RECORD);
            List<Surface> surfaces = new ArrayList<>();

            // Set up Surface for the camera preview
            Surface previewSurface = new Surface(texture);
            surfaces.add(previewSurface);
            mPreviewBuilder.addTarget(previewSurface);

            // Set up Surface for the MediaRecorder
            Surface recorderSurface = mMediaRecorder.getSurface();
            surfaces.add(recorderSurface);
            mPreviewBuilder.addTarget(recorderSurface);

            // Start a capture session
            // Once the session starts, we can update the UI and start recording
            mCameraDevice.createCaptureSession(surfaces, new CameraCaptureSession.StateCallback() {

                @Override
                public void onConfigured(@NonNull CameraCaptureSession cameraCaptureSession) {
                    mPreviewSession = cameraCaptureSession;
                    updatePreview();
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mIsRecordingVideo = true;
                            // Start recording
                            mMediaRecorder.start();
                        }
                    });
                }

                @Override
                public void onConfigureFailed(@NonNull CameraCaptureSession cameraCaptureSession) {
                    Activity activity = getActivity();
                    if (null != activity) {
                        Toast.makeText(activity, "Failed", Toast.LENGTH_SHORT).show();
                    }
                }
            }, mBackgroundHandler);
        } catch (CameraAccessException | IOException e) {
            e.printStackTrace();
        }

    }

    /**
     * Saves a JPEG {@link Image} into the specified {@link File}.
     */
    private static class ImageSaver implements Runnable {

        /**
         * The JPEG image
         */
        private final Image mImage;
        /**
         * The file we save the image into.
         */
        private final File mFile;

        ImageSaver(Image image, File file) {
            mImage = image;
            mFile = file;
        }

        @Override
        public void run() {
            ByteBuffer buffer = mImage.getPlanes()[0].getBuffer();
            byte[] bytes = new byte[buffer.remaining()];
            buffer.get(bytes);
            FileOutputStream output = null;
            try {
                output = new FileOutputStream(mFile);
                output.write(bytes);
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                mImage.close();
                if (null != output) {
                    try {
                        output.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }

    }

    /**
     * Compares two {@code Size}s based on their areas.
     */
    static class CompareSizesByArea implements Comparator<Size> {

        @Override
        public int compare(Size lhs, Size rhs) {
            // We cast here to ensure the multiplications won't overflow
            return Long.signum((long) lhs.getWidth() * lhs.getHeight() -
                    (long) rhs.getWidth() * rhs.getHeight());
        }

    }

    /**
     * Shows an error message dialog.
     */
    public static class ErrorDialog extends DialogFragment {

        private static final String ARG_MESSAGE = "message";

        public static ErrorDialog newInstance(String message) {
            ErrorDialog dialog = new ErrorDialog();
            Bundle args = new Bundle();
            args.putString(ARG_MESSAGE, message);
            dialog.setArguments(args);
            return dialog;
        }

        @NonNull
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            final Activity activity = getActivity();

            return new AlertDialog.Builder(activity)
                    .setMessage(getArguments().getString(ARG_MESSAGE))
                    .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            activity.finish();
                        }
                    })
                    .create();
        }

    }

    /**
     * Shows OK/Cancel confirmation dialog about camera permission.
     */
    public static class ConfirmationDialog extends DialogFragment {

        @NonNull
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            final Fragment parent = getParentFragment();
            return new AlertDialog.Builder(getActivity())
                    .setMessage("give me permission")
                    .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            parent.requestPermissions(new String[]{Manifest.permission.CAMERA},
                                    REQUEST_CAMERA_PERMISSION);
                        }
                    })
                    .setNegativeButton(android.R.string.cancel,
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    Activity activity = parent.getActivity();
                                    if (activity != null) {
                                        activity.finish();
                                    }
                                }
                            })
                    .create();
        }
    }

    private SensorManager sensorManager;
    private float[] lastMagFields = new float[3];
    ;
    private float[] lastAccels = new float[3];
    ;
    private float[] rotationMatrix = new float[16];
    private float[] orientation = new float[4];

    //set default orientation as portrait
    public String sensor_orientation = "portrait";

    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }

    public void onSensorChanged(SensorEvent event) {
        switch (event.sensor.getType()) {
            case Sensor.TYPE_ACCELEROMETER:
                if (Math.abs(event.values[1]) > Math.abs(event.values[0])) {
                    //Mainly portrait
                    if (event.values[1] > 1) {
                        sensor_orientation = "portrait";
                    } else if (event.values[1] < -1) {
                        sensor_orientation = "inverse_portrait";
                    }
                } else {
                    //Mainly landscape
                    if (event.values[0] > 1) {
                        sensor_orientation = "landscape_right";
                    } else if (event.values[0] < -1) {
                        sensor_orientation = "landscape_left";
                    }
                }
                System.arraycopy(event.values, 0, lastAccels, 0, 3);
                break;

            default:
                break;
        }

//        if (SensorManager.getRotationMatrix(rotationMatrix, null, lastAccels, lastMagFields)) {
//            SensorManager.getOrientation(rotationMatrix, orientation);
//
//            float xAxis = (float) Math.toDegrees(orientation[1]);
//            float yAxis = (float) Math.toDegrees(orientation[2]);
//            int orientation = Configuration.ORIENTATION_UNDEFINED;
//            if ((yAxis <= 25) && (yAxis >= -25) && (xAxis >= -160)) {
//                sensor_orientation = "portrait";
//                orientation = Configuration.ORIENTATION_PORTRAIT;
//            } else if ((yAxis < -25) && (xAxis >= -20)) {
//                sensor_orientation = "landscape_right";
//                orientation = Configuration.ORIENTATION_LANDSCAPE;
//            } else if ((yAxis > 25) && (xAxis >= -20)){
//                orientation = Configuration.ORIENTATION_LANDSCAPE;
//                sensor_orientation = "landscape_left";
//            }
//        }
    }
}
