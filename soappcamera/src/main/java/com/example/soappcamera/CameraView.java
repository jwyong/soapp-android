package com.example.soappcamera;

import android.Manifest;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.TypedArray;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.location.Location;
import android.media.MediaActionSound;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import static android.view.View.MeasureSpec.AT_MOST;
import static android.view.View.MeasureSpec.EXACTLY;
import static android.view.View.MeasureSpec.UNSPECIFIED;
import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;


public class CameraView extends FrameLayout {

    private final static String TAG = CameraView.class.getSimpleName();
    private static final CameraLogger LOG = CameraLogger.create(TAG);

    public final static int PERMISSION_REQUEST_CODE = 16;

    final static int DEFAULT_JPEG_QUALITY = 100;
    final static boolean DEFAULT_CROP_OUTPUT = false;
    final static boolean DEFAULT_PLAY_SOUNDS = true;

    // Self managed parameters
    private int mJpegQuality;
    private boolean mCropOutput;
    private float mZoomValue;
    private float mExposureCorrectionValue;
    private boolean mPlaySounds;
    private HashMap<Gesture, GestureAction> mGestureMap = new HashMap<>(4);

    // Components
    /* for tests */ CameraCallbacks mCameraCallbacks;
    private CameraPreview mCameraPreview;
    private OrientationHelper mOrientationHelper;
    private CameraController mCameraController;
    private ArrayList<CameraListener> mListeners = new ArrayList<>(2);
    private MediaActionSound mSound;

    // Views
    GridLinesLayout mGridLinesLayout;
    PinchGestureLayout mPinchGestureLayout;
    TapGestureLayout mTapGestureLayout;
    ScrollGestureLayout mScrollGestureLayout;
    private boolean mKeepScreenOn;
    public int recordTime = 0;
    // Threading
    private Handler mUiHandler;
    private WorkerHandler mWorkerHandler;

    public CameraView(@NonNull Context context) {
        super(context, null);
        init(context, null);
    }

    public CameraView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    //region Init

    @SuppressWarnings("WrongConstant")
    private void init(@NonNull Context context, @Nullable AttributeSet attrs) {
        setWillNotDraw(false);
        TypedArray a = context.getTheme().obtainStyledAttributes(attrs, R.styleable.CameraView, 0, 0);
        // Self managed
        int jpegQuality = a.getInteger(R.styleable.CameraView_cameraJpegQuality, DEFAULT_JPEG_QUALITY);
        boolean cropOutput = a.getBoolean(R.styleable.CameraView_cameraCropOutput, DEFAULT_CROP_OUTPUT);
        boolean playSounds = a.getBoolean(R.styleable.CameraView_cameraPlaySounds, DEFAULT_PLAY_SOUNDS);

        // Camera controller params
        Facing facing = Facing.fromValue(a.getInteger(R.styleable.CameraView_cameraFacing, Facing.DEFAULT.value()));
        Flash flash = Flash.fromValue(a.getInteger(R.styleable.CameraView_cameraFlash, Flash.DEFAULT.value()));
        Grid grid = Grid.fromValue(a.getInteger(R.styleable.CameraView_cameraGrid, Grid.DEFAULT.value()));
        WhiteBalance whiteBalance = WhiteBalance.fromValue(a.getInteger(R.styleable.CameraView_cameraWhiteBalance, WhiteBalance.DEFAULT.value()));
        VideoQuality videoQuality = VideoQuality.fromValue(a.getInteger(R.styleable.CameraView_cameraVideoQuality, VideoQuality.DEFAULT.value()));
        SessionType sessionType = SessionType.fromValue(a.getInteger(R.styleable.CameraView_cameraSessionType, SessionType.DEFAULT.value()));
        Hdr hdr = Hdr.fromValue(a.getInteger(R.styleable.CameraView_cameraHdr, Hdr.DEFAULT.value()));
        Audio audio = Audio.fromValue(a.getInteger(R.styleable.CameraView_cameraAudio, Audio.DEFAULT.value()));

        // Gestures
        GestureAction tapGesture = GestureAction.fromValue(a.getInteger(R.styleable.CameraView_cameraGestureTap, GestureAction.DEFAULT_TAP.value()));
        GestureAction longTapGesture = GestureAction.fromValue(a.getInteger(R.styleable.CameraView_cameraGestureLongTap, GestureAction.DEFAULT_LONG_TAP.value()));
        GestureAction pinchGesture = GestureAction.fromValue(a.getInteger(R.styleable.CameraView_cameraGesturePinch, GestureAction.DEFAULT_PINCH.value()));
        GestureAction scrollHorizontalGesture = GestureAction.fromValue(a.getInteger(R.styleable.CameraView_cameraGestureScrollHorizontal, GestureAction.DEFAULT_SCROLL_HORIZONTAL.value()));
        GestureAction scrollVerticalGesture = GestureAction.fromValue(a.getInteger(R.styleable.CameraView_cameraGestureScrollVertical, GestureAction.DEFAULT_SCROLL_VERTICAL.value()));
        a.recycle();

        // Components
        mCameraCallbacks = new Callbacks();
        mCameraController = instantiateCameraController(mCameraCallbacks);
        mUiHandler = new Handler(Looper.getMainLooper());
        mWorkerHandler = WorkerHandler.get("CameraViewWorker");

        // Views
        mGridLinesLayout = new GridLinesLayout(context);
        mPinchGestureLayout = new PinchGestureLayout(context);
        mTapGestureLayout = new TapGestureLayout(context);
        mScrollGestureLayout = new ScrollGestureLayout(context);
        addView(mGridLinesLayout);
        addView(mPinchGestureLayout);
        addView(mTapGestureLayout);
        addView(mScrollGestureLayout);

        // Apply self managed
        setCropOutput(cropOutput);
        setJpegQuality(jpegQuality);
        setPlaySounds(playSounds);

        // Apply camera controller params
        setFacing(facing);
        setFlash(flash);
        setSessionType(sessionType);
        setVideoQuality(videoQuality);
        setWhiteBalance(whiteBalance);
        setGrid(grid);
        setHdr(hdr);
        setAudio(audio);

        // Apply gestures
        mapGesture(Gesture.TAP, tapGesture);
        // mapGesture(Gesture.DOUBLE_TAP, doubleTapGesture);
        mapGesture(Gesture.LONG_TAP, longTapGesture);
        mapGesture(Gesture.PINCH, pinchGesture);
        mapGesture(Gesture.SCROLL_HORIZONTAL, scrollHorizontalGesture);
        mapGesture(Gesture.SCROLL_VERTICAL, scrollVerticalGesture);

        if (!isInEditMode()) {
            mOrientationHelper = new OrientationHelper(context, mCameraCallbacks);
        }
    }

    protected CameraController instantiateCameraController(CameraCallbacks callbacks) {
        return new Camera1(callbacks);
    }

    protected CameraPreview instantiatePreview(Context context, ViewGroup container) {
        // TextureView is not supported without hardware acceleration.

        return isHardwareAccelerated() ?
                new TextureCameraPreview(context, container, null) :
                new SurfaceCameraPreview(context, container, null);
    }

    /* for tests */ void instantiatePreview() {
        mCameraPreview = instantiatePreview(getContext(), this);
        mCameraController.setPreview(mCameraPreview);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (mCameraPreview == null) {
            // isHardwareAccelerated will return the real value only after we are
            // attached. That's why we instantiate the preview here.
            instantiatePreview();
        }
        if (!isInEditMode()) {
            mOrientationHelper.enable(getContext());
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        if (!isInEditMode()) {
            mOrientationHelper.disable();
        }
        super.onDetachedFromWindow();
    }

    //endregion

    //region Measuring behavior

    private String ms(int mode) {
        switch (mode) {
            case AT_MOST: return "AT_MOST";
            case EXACTLY: return "EXACTLY";
            case UNSPECIFIED: return "UNSPECIFIED";
        }
        return null;
    }

    /**
     * Measuring is basically controlled by layout params width and height.
     * The basic semantics are:
     *
     * - MATCH_PARENT: CameraView should completely fill this dimension, even if this might mean
     *                 not respecting the preview aspect ratio.
     * - WRAP_CONTENT: CameraView should try to adapt this dimension to respect the preview
     *                 aspect ratio.
     *
     * When both dimensions are MATCH_PARENT, CameraView will fill its
     * parent no matter the preview. Thanks to what happens in {@link CameraPreview}, this acts like
     * a CENTER CROP scale type.
     *
     * When both dimensions are WRAP_CONTENT, CameraView will take the biggest dimensions that
     * fit the preview aspect ratio. This acts like a CENTER INSIDE scale type.
     */
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        Size previewSize = getPreviewSize();
        if (previewSize == null) {

            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
            return;
        }

        // Let's which dimensions need to be adapted.
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        final int widthValue = MeasureSpec.getSize(widthMeasureSpec);
        final int heightValue = MeasureSpec.getSize(heightMeasureSpec);
        final boolean flip = mCameraController.shouldFlipSizes();
        final float previewWidth = flip ? previewSize.getHeight() : previewSize.getWidth();
        final float previewHeight = flip ? previewSize.getWidth() : previewSize.getHeight();

        // Pre-process specs
        final ViewGroup.LayoutParams lp = getLayoutParams();
        if (!mCameraPreview.supportsCropping()) {
            // We can't allow EXACTLY constraints in this case.
            if (widthMode == EXACTLY) widthMode = AT_MOST;
            if (heightMode == EXACTLY) heightMode = AT_MOST;
        } else {
            // If MATCH_PARENT is interpreted as AT_MOST, transform to EXACTLY
            // to be consistent with our semantics (and our docs).
            if (widthMode == AT_MOST && lp.width == MATCH_PARENT) widthMode = EXACTLY;
            if (heightMode == AT_MOST && lp.height == MATCH_PARENT) heightMode = EXACTLY;
        }

        // (1) If we have fixed dimensions (either 300dp or MATCH_PARENT), there's nothing we should do,
        // other than respect it. The preview will eventually be cropped at the sides (by PreviewImpl scaling)
        // except the case in which these fixed dimensions manage to fit exactly the preview aspect ratio.
        if (widthMode == EXACTLY && heightMode == EXACTLY) {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
            return;
        }

        // (2) If both dimensions are free, with no limits, then our size will be exactly the
        // preview size. This can happen rarely, for example in 2d scrollable containers.
        if (widthMode == UNSPECIFIED && heightMode == UNSPECIFIED) {
            super.onMeasure(
                    MeasureSpec.makeMeasureSpec((int) previewWidth, EXACTLY),
                    MeasureSpec.makeMeasureSpec((int) previewHeight, EXACTLY));
            return;
        }

        // It's sure now that at least one dimension can be determined (either because EXACTLY or AT_MOST).
        // This starts to seem a pleasant situation.

        // (3) If one of the dimension is completely free (e.g. in a scrollable container),
        // take the other and fit the ratio.
        // One of the two might be AT_MOST, but we use the value anyway.
        float ratio = previewHeight / previewWidth;
        if (widthMode == UNSPECIFIED || heightMode == UNSPECIFIED) {
            boolean freeWidth = widthMode == UNSPECIFIED;
            int height, width;
            if (freeWidth) {
                height = heightValue;
                width = (int) (height / ratio);
            } else {
                width = widthValue;
                height = (int) (width * ratio);
            }
            super.onMeasure(MeasureSpec.makeMeasureSpec(width, EXACTLY),
                    MeasureSpec.makeMeasureSpec(height, EXACTLY));
            return;
        }

        // (4) At this point both dimensions are either AT_MOST-AT_MOST, EXACTLY-AT_MOST or AT_MOST-EXACTLY.
        // Let's manage this sanely. If only one is EXACTLY, we can TRY to fit the aspect ratio,
        // but it is not guaranteed to succeed. It depends on the AT_MOST value of the other dimensions.
        if (widthMode == EXACTLY || heightMode == EXACTLY) {
            boolean freeWidth = widthMode == AT_MOST;
            int height, width;
            if (freeWidth) {
                height = heightValue;
                width = Math.min((int) (height / ratio), widthValue);
            } else {
                width = widthValue;
                height = Math.min((int) (width * ratio), heightValue);
            }
            super.onMeasure(MeasureSpec.makeMeasureSpec(width, EXACTLY),
                    MeasureSpec.makeMeasureSpec(height, EXACTLY));
            return;
        }

        // (5) Last case, AT_MOST and AT_MOST. Here we can SURELY fit the aspect ratio by
        // filling one dimension and adapting the other.
        int height, width;
        float atMostRatio = (float) heightValue / (float) widthValue;
        if (atMostRatio >= ratio) {
            // We must reduce height.
            width = widthValue;
            height = (int) (width * ratio);
        } else {
            height = heightValue;
            width = (int) (height / ratio);
        }
        super.onMeasure(MeasureSpec.makeMeasureSpec(width, EXACTLY),
                MeasureSpec.makeMeasureSpec(height, EXACTLY));
    }

    //endregion

    //region Gesture APIs

    /**
     * Maps a {@link Gesture} to a certain gesture action.
     * For example, you can assign zoom control to the pinch gesture by just calling:
     * <code>
     *     cameraView.mapGesture(Gesture.PINCH, GestureAction.ZOOM);
     * </code>
     *
     * Not all actions can be assigned to a certain gesture. For example, zoom control can't be
     * assigned to the Gesture.TAP gesture. Look at {@link Gesture} to know more.
     * This method returns false if they are not assignable.
     *
     * @param gesture which gesture to map
     * @param action which action should be assigned
     * @return true if this action could be assigned to this gesture
     */
    public boolean mapGesture(@NonNull Gesture gesture, GestureAction action) {
        GestureAction none = GestureAction.NONE;
        if (gesture.isAssignableTo(action)) {
            mGestureMap.put(gesture, action);
            switch (gesture) {
                case PINCH:
                    mPinchGestureLayout.enable(mGestureMap.get(Gesture.PINCH) != none);
                    break;
                case TAP:
                // case DOUBLE_TAP:
                case LONG_TAP:
                    mTapGestureLayout.enable(
                            mGestureMap.get(Gesture.TAP) != none ||
                            // mGestureMap.get(Gesture.DOUBLE_TAP) != none ||
                            mGestureMap.get(Gesture.LONG_TAP) != none);
                    break;
                case SCROLL_HORIZONTAL:
                case SCROLL_VERTICAL:
                    mScrollGestureLayout.enable(
                            mGestureMap.get(Gesture.SCROLL_HORIZONTAL) != none ||
                            mGestureMap.get(Gesture.SCROLL_VERTICAL) != none);
                    break;
            }
            return true;
        }
        mapGesture(gesture, none);
        return false;
    }


    /**
     * Clears any action mapped to the given gesture.
     * @param gesture which gesture to clear
     */
    public void clearGesture(@NonNull Gesture gesture) {
        mapGesture(gesture, GestureAction.NONE);
    }


    /**
     * Returns the action currently mapped to the given gesture.
     *
     * @param gesture which gesture to inspect
     * @return mapped action
     */
    public GestureAction getGestureAction(@NonNull Gesture gesture) {
        return mGestureMap.get(gesture);
    }


    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        return true; // Steal our own events.
    }


    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (!isStarted()) return true;

        // Pass to our own GestureLayouts
        CameraOptions options = mCameraController.getCameraOptions(); // Non null
        if (mPinchGestureLayout.onTouchEvent(event)) {
            onGesture(mPinchGestureLayout, options);
        } else if (mScrollGestureLayout.onTouchEvent(event)) {
            onGesture(mScrollGestureLayout, options);
        } else if (mTapGestureLayout.onTouchEvent(event)) {
            onGesture(mTapGestureLayout, options);
        }
        return true;
    }


    // Some gesture layout detected a gesture. It's not known at this moment:
    // (1) if it was mapped to some action (we check here)
    // (2) if it's supported by the camera (CameraController checks)
    private boolean onGesture(GestureLayout source, @NonNull CameraOptions options) {
        Gesture gesture = source.getGestureType();
        GestureAction action = mGestureMap.get(gesture);
        PointF[] points = source.getPoints();
        float oldValue, newValue;
        switch (action) {

            case CAPTURE:
                return mCameraController.capturePicture();

            case FOCUS:
            case FOCUS_WITH_MARKER:
                return mCameraController.startAutoFocus(gesture, points[0]);

            case ZOOM:
                oldValue = mZoomValue;
                newValue = source.scaleValue(oldValue, 0, 1);
                if (mCameraController.setZoom(newValue)) {
                    mZoomValue = newValue;
                    mCameraCallbacks.dispatchOnZoomChanged(newValue, points);
                    return true;
                }
                break;

            case EXPOSURE_CORRECTION:
                oldValue = mExposureCorrectionValue;
                float minValue = options.getExposureCorrectionMinValue();
                float maxValue = options.getExposureCorrectionMaxValue();
                newValue = source.scaleValue(oldValue, minValue, maxValue);
                float[] bounds = new float[]{minValue, maxValue};
                if (mCameraController.setExposureCorrection(newValue)) {
                    mExposureCorrectionValue = newValue;
                    mCameraCallbacks.dispatchOnExposureCorrectionChanged(newValue, bounds, points);
                    return true;
                }
                break;
        }
        return false;
    }

    //endregion

    //region Lifecycle APIs

    /**
     * Returns whether the camera has started showing its preview.
     * @return whether the camera has started
     */
    public boolean isStarted() {
        return mCameraController.getState() >= CameraController.STATE_STARTED;
    }

    private boolean isStopped() {
        return mCameraController.getState() == CameraController.STATE_STOPPED;
    }


    /**
     * Starts the camera preview, if not started already.
     * This should be called onResume(), or when you are ready with permissions.
     */
    public void start() {
        if (!isEnabled()) return;

        if (checkPermissions(getSessionType(), getAudio())) {
            // Update display orientation for current CameraController
            mOrientationHelper.enable(getContext());
            mCameraController.start();
        }
    }


    /**
     * Checks that we have appropriate permissions for this session type.
     * Throws if session = audio and manifest did not add the microphone permissions.     
     * @param sessionType
     * @param audio
     * @return true if we can go on, false otherwise.
     */
    @SuppressLint("NewApi")
    protected boolean checkPermissions(SessionType sessionType, Audio audio) {
        checkPermissionsManifestOrThrow(sessionType, audio);
        // Manifest is OK at this point. Let's check runtime permissions.
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) return true;

        Context c = getContext();
        boolean needsCamera = true;
//        boolean needsAudio = sessionType == SessionType.VIDEO && audio == Audio.ON;
        boolean needsAudio = true;

        needsCamera = needsCamera && c.checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED;
        needsAudio = needsAudio && c.checkSelfPermission(Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED;

        if (needsCamera || needsAudio) {
            requestPermissions(needsCamera, needsAudio);
            return false;
        }
        return true;
    }


    /**
     * If mSessionType == SESSION_TYPE_VIDEO we will ask for RECORD_AUDIO permission.
     * If the developer did not add this to its manifest, throw and fire warnings.
     * (Hoping this is not caught elsewhere... we should test).
     */
    private void checkPermissionsManifestOrThrow(SessionType sessionType, Audio audio) {
        if (sessionType == SessionType.VIDEO && audio == Audio.ON) {
            try {
                PackageManager manager = getContext().getPackageManager();
                PackageInfo info = manager.getPackageInfo(getContext().getPackageName(), PackageManager.GET_PERMISSIONS);
                for (String requestedPermission : info.requestedPermissions) {
                    if (requestedPermission.equals(Manifest.permission.RECORD_AUDIO)) {
                        return;
                    }
                }
                throw new IllegalStateException(CameraLogger.lastMessage);
            } catch (PackageManager.NameNotFoundException e) {
                // Not possible.
            }
        }
    }


    /**
     * Stops the current preview, if any was started.
     * This should be called onPause().
     */
    public void stop() {
        mCameraController.stop();
    }

    public void destroy() {
        // TODO: this is not strictly needed
        clearCameraListeners(); // Release
        mCameraController.stopImmediately();
    }

    //endregion

    //region Public APIs for controls

    /**
     * Returns a {@link CameraOptions} instance holding supported options for this camera
     * session. This might change over time. It's better to hold a reference from
     * {@link CameraListener#onCameraOpened(CameraOptions)}.
     *
     * @return an options map, or null if camera was not opened
     */
    @Nullable
    public CameraOptions getCameraOptions() {
        return mCameraController.getCameraOptions();
    }


    /**
     * If present, returns a collection of extra properties from the current camera
     * session.
     * @return an ExtraProperties object.
     */
    @Nullable
    public ExtraProperties getExtraProperties() {
        return mCameraController.getExtraProperties();
    }


    /**
     * Sets exposure adjustment, in EV stops. A positive value will mean brighter picture.
     *
     * If camera is not opened, this will have no effect.
     * If {@link CameraOptions#isExposureCorrectionSupported()} is false, this will have no effect.
     * The provided value should be between the bounds returned by {@link CameraOptions}, or it will
     * be capped.
     *
     * @see CameraOptions#getExposureCorrectionMinValue()
     * @see CameraOptions#getExposureCorrectionMaxValue()
     *
     * @param EVvalue exposure correction value.
     */
    public void setExposureCorrection(float EVvalue) {
        CameraOptions options = getCameraOptions();
        if (options != null) {
            float min = options.getExposureCorrectionMinValue();
            float max = options.getExposureCorrectionMaxValue();
            if (EVvalue < min) EVvalue = min;
            if (EVvalue > max) EVvalue = max;
            if (mCameraController.setExposureCorrection(EVvalue)) {
                mExposureCorrectionValue = EVvalue;
            }
        }
    }


    /**
     * Returns the current exposure correction value, typically 0
     * at start-up.
     * @return the current exposure correction value
     */
    public float getExposureCorrection() {
        return mExposureCorrectionValue;
    }


    /**
     * Sets a zoom value. This is not guaranteed to be supported by the current device,
     * but you can take a look at {@link CameraOptions#isZoomSupported()}.
     * This will have no effect if called before the camera is opened.
     *
     * Zoom value should be between 0 and 1, where 1 will be the maximum available zoom.
     * If it's not, it will be capped.
     *
     * @param zoom value in [0,1]
     */
    public void setZoom(float zoom) {
        if (zoom < 0) zoom = 0;
        if (zoom > 1) zoom = 1;
        if (mCameraController.setZoom(zoom)) {
            mZoomValue = zoom;
        }
    }


    /**
     * Returns the current zoom value, something between 0 and 1.
     * @return the current zoom value
     */
    public float getZoom() {
        return mZoomValue;
    }


    /**
     * Controls the grids to be drawn over the current layout.
     *
     * @see Grid#OFF
     * @see Grid#DRAW_3X3
     * @see Grid#DRAW_4X4
     * @see Grid#DRAW_PHI
     *
     * @param gridMode desired grid mode
     */
    public void setGrid(Grid gridMode) {
        mGridLinesLayout.setGridMode(gridMode);
    }


    /**
     * Gets the current grid mode.
     * @return the current grid mode
     */
    public Grid getGrid() {
        return mGridLinesLayout.getGridMode();
    }


    /**
     * Controls the grids to be drawn over the current layout.
     *
     * @see Hdr#OFF
     * @see Hdr#ON
     *
     * @param hdr desired hdr value
     */
    public void setHdr(Hdr hdr) {
        mCameraController.setHdr(hdr);
    }


    /**
     * Gets the current hdr value.
     * @return the current hdr value
     */
    public Hdr getHdr() {
        return mCameraController.getHdr();
    }


    /**
     * Set location coordinates to be found later in the jpeg EXIF header
     *
     * @param latitude current latitude
     * @param longitude current longitude
     */
    public void setLocation(double latitude, double longitude) {
        Location location = new Location("Unknown");
        location.setTime(System.currentTimeMillis());
        location.setAltitude(0);
        location.setLatitude(latitude);
        location.setLongitude(longitude);
        mCameraController.setLocation(location);
    }


    /**
     * Set location values to be found later in the jpeg EXIF header
     *
     * @param location current location
     */
    public void setLocation(Location location) {
        mCameraController.setLocation(location);
    }


    /**
     * Retrieves the location previously applied with setLocation().
     *
     * @return the current location, if any.
     */
    @Nullable
    public Location getLocation() {
        return mCameraController.getLocation();
    }

    /**
     * Sets desired white balance to current camera session.
     *
     * @see WhiteBalance#AUTO
     * @see WhiteBalance#INCANDESCENT
     * @see WhiteBalance#FLUORESCENT
     * @see WhiteBalance#DAYLIGHT
     * @see WhiteBalance#CLOUDY
     *
     * @param whiteBalance desired white balance behavior.
     */
    public void setWhiteBalance(WhiteBalance whiteBalance) {
        mCameraController.setWhiteBalance(whiteBalance);
    }


    /**
     * Returns the current white balance behavior.
     * @return white balance value.
     */
    public WhiteBalance getWhiteBalance() {
        return mCameraController.getWhiteBalance();
    }


    /**
     * Sets which camera sensor should be used.
     *
     * @see Facing#FRONT
     * @see Facing#BACK
     *
     * @param facing a facing value.
     */
    public void setFacing(final Facing facing) {
        mCameraController.setFacing(facing);
    }


    /**
     * Gets the facing camera currently being used.
     * @return a facing value.
     */
    public Facing getFacing() {
        return mCameraController.getFacing();
    }


    /**
     * Toggles the facing value between {@link Facing#BACK}
     * and {@link Facing#FRONT}.
     *
     * @return the new facing value
     */
    public Facing toggleFacing() {
        Facing facing = mCameraController.getFacing();
        switch (facing) {
            case BACK:
                setFacing(Facing.FRONT);
                break;

            case FRONT:
                setFacing(Facing.BACK);
                break;
        }

        return mCameraController.getFacing();
    }


    /**
     * Sets the flash mode.
     *
     * @see Flash#OFF
     * @see Flash#ON
     * @see Flash#AUTO
     * @see Flash#TORCH

     * @param flash desired flash mode.
     */
    public void setFlash(Flash flash) {
        mCameraController.setFlash(flash);
    }


    /**
     * Gets the current flash mode.
     * @return a flash mode
     */
    public Flash getFlash() {
        return mCameraController.getFlash();
    }


    /**
     * Toggles the flash mode between {@link Flash#OFF},
     * {@link Flash#ON} and {@link Flash#AUTO}, in this order.
     *
     * @return the new flash value
     */
    public Flash toggleFlash() {
        Flash flash = mCameraController.getFlash();
        switch (flash) {
            case OFF:
                setFlash(Flash.ON);
                break;

            case ON:
                setFlash(Flash.AUTO);
                break;

            case AUTO:
            case TORCH:
                setFlash(Flash.OFF);
                break;
        }

        return mCameraController.getFlash();
    }


    /**
     * Controls the audio mode.
     *
     * @see Audio#OFF
     * @see Audio#ON
     *
     * @param audio desired audio value
     */
    public void setAudio(Audio audio) {

        if (audio == getAudio() || isStopped()) {
            // Check did took place, or will happen on start().
            mCameraController.setAudio(audio);

        } else if (checkPermissions(getSessionType(), audio)) {
            // Camera is running. Pass.
            mCameraController.setAudio(audio);

        } else {
            // This means that the audio permission is being asked.
            // Stop the camera so it can be restarted by the developer onPermissionResult.
            // Developer must also set the audio value again...
            // Not ideal but good for now.
            stop();
        }
    }


    /**
     * Gets the current audio value.
     * @return the current audio value
     */
    public Audio getAudio() {
        return mCameraController.getAudio();
    }


    /**
     * Starts an autofocus process at the given coordinates, with respect
     * to the view width and height.
     *
     * @param x should be between 0 and getWidth()
     * @param y should be between 0 and getHeight()
     */
    public void startAutoFocus(float x, float y) {
        if (x < 0 || x > getWidth()) throw new IllegalArgumentException("x should be >= 0 and <= getWidth()");
        if (y < 0 || y > getHeight()) throw new IllegalArgumentException("y should be >= 0 and <= getHeight()");
        mCameraController.startAutoFocus(null, new PointF(x, y));
    }


    /**
     * Set the current session type to either picture or video.
     * When sessionType is video,
     * - {@link #startCapturingVideo(File)} will not throw any exception
     * - {@link #capturePicture()} might fallback to {@link #captureSnapshot()} or might not work
     *
     * @see SessionType#PICTURE
     * @see SessionType#VIDEO
     *
     * @param sessionType desired session type.
     */
    public void setSessionType(SessionType sessionType) {

        if (sessionType == getSessionType() || isStopped()) {
            // Check did took place, or will happen on start().
            mCameraController.setSessionType(sessionType);

        } else if (checkPermissions(sessionType, getAudio())) {
            // Camera is running. CameraImpl setSessionType will do the trick.
            mCameraController.setSessionType(sessionType);

        } else {
            // This means that the audio permission is being asked.
            // Stop the camera so it can be restarted by the developer onPermissionResult.
            // Developer must also set the session type again...
            // Not ideal but good for now.
            stop();
        }
    }


    /**
     * Gets the current session type.
     * @return the current session type
     */
    public SessionType getSessionType() {
        return mCameraController.getSessionType();
    }


    /**
     * Sets video recording quality. This is not guaranteed to be supported by current device.
     * If it's not, a lower quality will be chosen, until a supported one is found.
     * If sessionType is video, this might trigger a camera restart and a change in preview size.
     *
     * @see VideoQuality#LOWEST
     * @see VideoQuality#HIGHEST
     * @see VideoQuality#MAX_QVGA
     * @see VideoQuality#MAX_480P
     * @see VideoQuality#MAX_720P
     * @see VideoQuality#MAX_1080P
     * @see VideoQuality#MAX_2160P
     *
     * @param videoQuality requested video quality
     */
    public void setVideoQuality(VideoQuality videoQuality) {
        mCameraController.setVideoQuality(videoQuality);
    }


    /**
     * Gets the current video quality.
     * @return the current video quality
     */
    public VideoQuality getVideoQuality() {
        return mCameraController.getVideoQuality();
    }


    /**
     * Sets the JPEG compression quality for image outputs.
     * @param jpegQuality a 0-100 integer.
     */
    public void setJpegQuality(int jpegQuality) {
        if (jpegQuality <= 0 || jpegQuality > 100) {
            throw new IllegalArgumentException("JPEG quality should be > 0 and <= 100");
        }
        mJpegQuality = jpegQuality;
    }


    /**
     * Gets the JPEG compression quality for image outputs.
     * @return a 0-100 integer
     */
    public int getJpegQuality() {
        return mJpegQuality;
    }


    /**
     * Whether we should crop the picture output to match CameraView aspect ratio.
     * This is only relevant if CameraView dimensions were somehow constrained
     * (e.g. by fixed value or MATCH_PARENT) and do not match internal aspect ratio.
     *
     * Please note that this requires additional computations after the picture is taken.
     *
     * @param cropOutput whether to crop
     */
    public void setCropOutput(boolean cropOutput) {
        this.mCropOutput = cropOutput;
    }


    /**
     * Returns whether we should crop the picture output to match CameraView aspect ratio.
     *
     * @see #setCropOutput(boolean)
     * @return whether we crop
     */
    public boolean getCropOutput() {
        return mCropOutput;
    }


    /**
     * Sets a {@link CameraListener} instance to be notified of all
     * interesting events that will happen during the camera lifecycle.
     *
     * @param cameraListener a listener for events.
     * @deprecated use {@link #addCameraListener(CameraListener)} instead.
     */
    @Deprecated
    public void setCameraListener(CameraListener cameraListener) {
        mListeners.clear();
        addCameraListener(cameraListener);
    }


    /**
     * Adds a {@link CameraListener} instance to be notified of all
     * interesting events that happen during the camera lifecycle.
     *
     * @param cameraListener a listener for events.
     */
    public void addCameraListener(CameraListener cameraListener) {
        if (cameraListener != null) {
            mListeners.add(cameraListener);
        }
    }


    /**
     * Remove a {@link CameraListener} that was previously registered.
     *
     * @param cameraListener a listener for events.
     */
    public void removeCameraListener(CameraListener cameraListener) {
        if (cameraListener != null) {
            mListeners.remove(cameraListener);
        }
    }


    /**
     * Clears the list of {@link CameraListener} that are registered
     * to camera events.
     */
    public void clearCameraListeners() {
        mListeners.clear();
    }


    /**
     * Asks the camera to capture an image of the current scene.
     * This will trigger {@link CameraListener#onPictureTaken(byte[])} if a listener
     * was registered.
     *
     * Note that if sessionType is {@link SessionType#VIDEO}, this
     * might fall back to {@link #captureSnapshot()} (that is, we might capture a preview frame).
     *
     * @see #captureSnapshot()
     */
    public void capturePicture() {
        if (mCameraController.capturePicture() && mPlaySounds) {
            // TODO: playSound on Camera2
        }
    }


    /**
     * Asks the camera to capture a snapshot of the current preview.
     * This eventually triggers {@link CameraListener#onPictureTaken(byte[])} if a listener
     * was registered.
     *
     * The difference with {@link #capturePicture()} is that this capture is faster, so it might be
     * better on slower cameras, though the result can be generally blurry or low quality.
     *
     * @see #capturePicture()
     */
    public void captureSnapshot() {
        if (mCameraController.captureSnapshot() && mPlaySounds) {
            //noinspection all
            playSound(MediaActionSound.SHUTTER_CLICK);
        }
    }


    /**
     * Starts recording a video with selected options, in a file called
     * "video.mp4" in the default folder.
     * This is discouraged, please use {@link #startCapturingVideo(File)} instead.
     *
     * @deprecated see {@link #startCapturingVideo(File)}
     */
    @Deprecated
    public void startCapturingVideo() {
        startCapturingVideo(null);
    }


    /**
     * Starts recording a video with selected options. Video will be written to the given file,
     * so callers should ensure they have appropriate permissions to write to the file.
     *
     * @param file a file where the video will be saved
     */

    public void startCapturingVideo(File file) {
        if (file == null) {
            file = new File(getContext().getFilesDir(), "video.mp4");
        }
        mCameraController.startVideo(file);

        mUiHandler.post(() -> {
            mKeepScreenOn = getKeepScreenOn();
            if (!mKeepScreenOn) setKeepScreenOn(true);
        });
    }


    /**
     * Starts recording a video with selected options. Video will be written to the given file,
     * so callers should ensure they have appropriate permissions to write to the file.
     * Recording will be automatically stopped after durationMillis, unless
     * {@link #stopCapturingVideo()} is not called meanwhile.
     *
     * @param file a file where the video will be saved
     * @param durationMillis video max duration
     *
     * @throws IllegalArgumentException if durationMillis is less than 500 milliseconds
     */

    public void startCapturingVideo(File file, long durationMillis) {
        if (durationMillis < 500) {
            throw new IllegalArgumentException("Video duration can't be < 500 milliseconds");
        }
        startCapturingVideo(file);
        mUiHandler.postDelayed(new Runnable() {
            @Override
            public void run() {

                stopCapturingVideo();
            }
        }, durationMillis);
    }


    // TODO: pauseCapturingVideo and resumeCapturingVideo. There is mediarecorder.pause(), but API 24...


    /**
     * Stops capturing video, if there was a video record going on.
     * This will fire {@link CameraListener#onVideoTaken(File)}.
     */
    public void stopCapturingVideo() {
        mCameraController.endVideo();
        mUiHandler.post(new Runnable() {
            @Override
            public void run() {
                if (getKeepScreenOn() != mKeepScreenOn) setKeepScreenOn(mKeepScreenOn);
            }
        });

    }


    /**
     * Returns the size used for the preview,
     * or null if it hasn't been computed (for example if the surface is not ready).
     * @return a Size
     */
    @Nullable
    public Size getPreviewSize() {
        return mCameraController != null ? mCameraController.getPreviewSize() : null;
    }


    /**
     * Returns the size used for the capture,
     * or null if it hasn't been computed yet (for example if the surface is not ready).
     * @return a Size
     */
    @Nullable
    public Size getCaptureSize() {
        return mCameraController != null ? mCameraController.getCaptureSize() : null;
    }


    /**
     * Returns the size used for capturing snapshots.
     * This is equal to {@link #getPreviewSize()}.
     *
     * @return a Size
     */
    @Nullable
    public Size getSnapshotSize() {
        return getPreviewSize();
    }


    // If we end up here, we're in M.
    @TargetApi(Build.VERSION_CODES.M)
    private void requestPermissions(boolean requestCamera, boolean requestAudio) {
        Activity activity = null;
        Context context = getContext();
        while (context instanceof ContextWrapper) {
            if (context instanceof Activity) {
                activity = (Activity) context;
            }
            context = ((ContextWrapper) context).getBaseContext();
        }

        List<String> permissions = new ArrayList<>();
        if (requestCamera) permissions.add(Manifest.permission.CAMERA);
        if (requestAudio) permissions.add(Manifest.permission.RECORD_AUDIO);
        if (activity != null) {
            activity.requestPermissions(permissions.toArray(new String[permissions.size()]),
                    PERMISSION_REQUEST_CODE);
        }
    }

    //endregion

    //region Sounds

    @SuppressLint("NewApi")
    private void playSound(int soundType) {
        if (mPlaySounds) {
            if (mSound == null) mSound = new MediaActionSound();
            mSound.play(soundType);
        }
    }

    /**
     * Controls whether CameraView should play sound effects on certain
     * events (picture taken, focus complete). Note that:
     * - On API level {@literal <} 16, this flag is always false
     * - Camera1 will always play the shutter sound when taking pictures
     *
     * @param playSounds whether to play sound effects
     */
    public void setPlaySounds(boolean playSounds) {
        mPlaySounds = playSounds && Build.VERSION.SDK_INT >= 16;
    }

    /**
     * Gets the current sound effect behavior.
     *
     * @see #setPlaySounds(boolean)
     * @return whether sound effects are supported
     */
    public boolean getPlaySounds() {
        return mPlaySounds;
    }

    //endregion

    //region Callbacks and dispatching

    interface CameraCallbacks extends OrientationHelper.Callbacks {
        void dispatchOnCameraOpened(CameraOptions options);
        void dispatchOnCameraClosed();
        void onCameraPreviewSizeChanged();
        void processImage(byte[] jpeg, boolean consistentWithView, boolean flipHorizontally);
        void processSnapshot(YuvImage image, boolean consistentWithView, boolean flipHorizontally);
        void dispatchOnVideoTaken(File file);
        void dispatchOnFocusStart(@Nullable Gesture trigger, PointF where);
        void dispatchOnFocusEnd(@Nullable Gesture trigger, boolean success, PointF where);
        void dispatchOnZoomChanged(final float newValue, final PointF[] fingers);
        void dispatchOnExposureCorrectionChanged(float newValue, float[] bounds, PointF[] fingers);
    }

    private class Callbacks implements CameraCallbacks {

        // Outer listeners
        private CameraLogger mLogger = CameraLogger.create(CameraCallbacks.class.getSimpleName());

        // Orientation TODO: move this logic into OrientationHelper
        private Integer mDisplayOffset;
        private Integer mDeviceOrientation;

        Callbacks() {}

        @Override
        public void dispatchOnCameraOpened(final CameraOptions options) {
            mLogger.i("dispatchOnCameraOpened", options);
            mUiHandler.post(new Runnable() {
                @Override
                public void run() {
                    for (CameraListener listener : mListeners) {
                        listener.onCameraOpened(options);
                    }
                }
            });
        }

        @Override
        public void dispatchOnCameraClosed() {
            mLogger.i("dispatchOnCameraClosed");
            mUiHandler.post(new Runnable() {
                @Override
                public void run() {
                    for (CameraListener listener : mListeners) {
                        listener.onCameraClosed();
                    }
                }
            });
        }

        @Override
        public void onCameraPreviewSizeChanged() {
            mLogger.i("onCameraPreviewSizeChanged");
            // Camera preview size, as returned by getPreviewSize(), has changed.
            // Request a layout pass for onMeasure() to do its stuff.
            // Potentially this will change CameraView size, which changes Surface size,
            // which triggers a new Preview size. But hopefully it will converge.
            mUiHandler.post(new Runnable() {
                @Override
                public void run() {
                    requestLayout();
                }
            });
        }


        /**
         * What would be great here is to ensure the EXIF tag in the jpeg is consistent with what we expect,
         * and maybe add flipping when we have been using the front camera.
         * Unfortunately this is not easy, because
         * - You can't write EXIF data to a byte[] array, not with support library at least
         * - You don't know what byte[] is, see {@link android.hardware.Camera.Parameters#setRotation(int)}.
         *   Sometimes our rotation is encoded in the byte array, sometimes a rotated byte[] is returned.
         *   Depends on the hardware.
         *
         * So for now we ignore flipping.
         *
         * @param consistentWithView is the final image (decoded respecting EXIF data) consistent with
         *                           the view width and height? Or should we flip dimensions to have a
         *                           consistent measure?
         * @param flipHorizontally whether this picture should be flipped horizontally after decoding,
         *                         because it was taken with the front camera.
         */
        @Override
        public void processImage(final byte[] jpeg, final boolean consistentWithView, final boolean flipHorizontally) {
            mLogger.i("processImage");
            mWorkerHandler.post(new Runnable() {
                @Override
                public void run() {
                    byte[] jpeg2 = jpeg;
                    if (mCropOutput && mCameraPreview.isCropping()) {
                        // If consistent, dimensions of the jpeg Bitmap and dimensions of getWidth(), getHeight()
                        // Live in the same reference system.
                        int w = consistentWithView ? getWidth() : getHeight();
                        int h = consistentWithView ? getHeight() : getWidth();
                        AspectRatio targetRatio = AspectRatio.of(w, h);
                        mLogger.i("processImage", "is consistent?", consistentWithView);
                        mLogger.i("processImage", "viewWidth?", getWidth(), "viewHeight?", getHeight());
                        jpeg2 = CropHelper.cropToJpeg(jpeg, targetRatio, mJpegQuality);
                    }
                    dispatchOnPictureTaken(jpeg2);
                }
            });
        }

        @Override
        public void processSnapshot(final YuvImage yuv, final boolean consistentWithView, boolean flipHorizontally) {
            mLogger.i("processSnapshot");
            mWorkerHandler.post(new Runnable() {
                @Override
                public void run() {
                    byte[] jpeg;
                    if (mCropOutput && mCameraPreview.isCropping()) {
                        int w = consistentWithView ? getWidth() : getHeight();
                        int h = consistentWithView ? getHeight() : getWidth();
                        AspectRatio targetRatio = AspectRatio.of(w, h);
                        mLogger.i("processSnapshot", "is consistent?", consistentWithView);
                        mLogger.i("processSnapshot", "viewWidth?", getWidth(), "viewHeight?", getHeight());
                        jpeg = CropHelper.cropToJpeg(yuv, targetRatio, mJpegQuality);
                    } else {
                        ByteArrayOutputStream out = new ByteArrayOutputStream();
                        yuv.compressToJpeg(new Rect(0, 0, yuv.getWidth(), yuv.getHeight()), mJpegQuality, out);
                        jpeg = out.toByteArray();
                    }
                    dispatchOnPictureTaken(jpeg);
                }
            });
        }


        private void dispatchOnPictureTaken(byte[] jpeg) {
            mLogger.i("dispatchOnPictureTaken");
            final byte[] data = jpeg;
            mUiHandler.post(new Runnable() {
                @Override
                public void run() {
                    for (CameraListener listener : mListeners) {
                        listener.onPictureTaken(data);
                    }
                }
            });
        }

        @Override
        public void dispatchOnVideoTaken(final File video) {
            mLogger.i("dispatchOnVideoTaken", video);
            mUiHandler.post(new Runnable() {
                @Override
                public void run() {
                    for (CameraListener listener : mListeners) {
                        listener.onVideoTaken(video);
                    }
                }
            });
        }

        @Override
        public void dispatchOnFocusStart(@Nullable final Gesture gesture, final PointF point) {
            mLogger.i("dispatchOnFocusStart", gesture, point);
            mUiHandler.post(new Runnable() {
                @Override
                public void run() {
                    if (gesture != null && mGestureMap.get(gesture) == GestureAction.FOCUS_WITH_MARKER) {
                        mTapGestureLayout.onFocusStart(point);
                    }

                    for (CameraListener listener : mListeners) {
                        listener.onFocusStart(point);
                    }
                }
            });
        }

        @Override
        public void dispatchOnFocusEnd(@Nullable final Gesture gesture, final boolean success,
                                       final PointF point) {
            mLogger.i("dispatchOnFocusEnd", gesture, success, point);
            mUiHandler.post(new Runnable() {
                @Override
                public void run() {
                    if (success && mPlaySounds) {
                        //noinspection all
                        playSound(MediaActionSound.FOCUS_COMPLETE);
                    }

                    if (gesture != null && mGestureMap.get(gesture) == GestureAction.FOCUS_WITH_MARKER) {
                        mTapGestureLayout.onFocusEnd(success);
                    }

                    for (CameraListener listener : mListeners) {
                        listener.onFocusEnd(success, point);
                    }
                }
            });
        }

        @Override
        public void onDisplayOffsetChanged(int displayOffset) {
            mLogger.i("onDisplayOffsetChanged", displayOffset);
            mCameraController.onDisplayOffset(displayOffset);
            mDisplayOffset = displayOffset;
            if (mDeviceOrientation != null) {
                int value = (mDeviceOrientation + mDisplayOffset) % 360;
                dispatchOnOrientationChanged(value);
            }
        }

        @Override
        public void onDeviceOrientationChanged(int deviceOrientation) {
            mLogger.i("onDeviceOrientationChanged", deviceOrientation);
            mCameraController.onDeviceOrientation(deviceOrientation);
            mDeviceOrientation = deviceOrientation;
            if (mDisplayOffset != null) {
                int value = (mDeviceOrientation + mDisplayOffset) % 360;
                dispatchOnOrientationChanged(value);
            }
        }


        private void dispatchOnOrientationChanged(final int value) {
            mLogger.i("dispatchOnOrientationChanged", value);
            mUiHandler.post(new Runnable() {
                @Override
                public void run() {
                    for (CameraListener listener : mListeners) {
                        listener.onOrientationChanged(value);
                    }
                }
            });
        }

        @Override
        public void dispatchOnZoomChanged(final float newValue, final PointF[] fingers) {
            mLogger.i("dispatchOnZoomChanged", newValue);
            mUiHandler.post(new Runnable() {
                @Override
                public void run() {
                    for (CameraListener listener : mListeners) {
                        listener.onZoomChanged(newValue, new float[]{0, 1}, fingers);
                    }
                }
            });
        }

        @Override
        public void dispatchOnExposureCorrectionChanged(final float newValue,
                                                        final float[] bounds,
                                                        final PointF[] fingers) {
            mLogger.i("dispatchOnExposureCorrectionChanged", newValue);
            mUiHandler.post(new Runnable() {
                @Override
                public void run() {
                    for (CameraListener listener : mListeners) {
                        listener.onExposureCorrectionChanged(newValue, bounds, fingers);
                    }
                }
            });
        }
    }

    //endregion

    //region Deprecated

    /**
     * This does nothing.
     * @deprecated
     * @param focus no-op
     */
    @Deprecated
    public void setFocus(int focus) {
    }


    /**
     * This does nothing.
     * @return no-op
     * @deprecated
     */
    @Deprecated
    public int getFocus() {
        return 0;
    }


    /**
     * This does nothing.
     * @deprecated
     * @param method no-op
     */
    @Deprecated
    public void setCaptureMethod(int method) {}


    /**
     * This does nothing.
     * @deprecated
     * @param permissions no-op
     */
    @Deprecated
    public void setPermissionPolicy(int permissions) {}


    /**
     * Sets the zoom mode for the current session.
     *
     * @param zoom no-op
     * @deprecated use {@link #mapGesture(Gesture, GestureAction)} to map zoom control to gestures
     */
    @Deprecated
    public void setZoomMode(int zoom) {
    }


    /**
     * Gets the current zoom mode.
     * @return no-op
     * @deprecated use {@link #mapGesture(Gesture, GestureAction)} to map zoom control to gestures
     */
    @Deprecated
    public int getZoomMode() {
        return 0;
    }

    //endregion
}
