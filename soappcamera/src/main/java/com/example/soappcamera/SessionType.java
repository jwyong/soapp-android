package com.example.soappcamera;



/**
 * Type of the session to be opened or to move to.
 * Session types have influence over the capture and preview size, ability to shoot pictures,
 * focus modes, runtime permissions needed.
 *
 * @see CameraView#setSessionType(SessionType)
 */
public enum SessionType {

    /**
     * Session optimized to capture pictures.
     *
     * - Trying to take videos in this session will throw an exception
     * - Only the camera permission is requested
     * - Preview and capture size is chosen as the max available size
     */
    PICTURE(0),

    /**
     * Session optimized to capture videos.
     *
     * - Trying to take pictures in this session will work, though with lower quality
     * - Trying to take pictures while recording a video will work if supported
     * - Camera and audio record permissions are requested
     * - Preview and capture size are chosen to respect the {@link VideoQuality} aspect ratio
     *
     * @see CameraOptions#isVideoSnapshotSupported()
     */
    VIDEO(1);

    static final SessionType DEFAULT = PICTURE;

    private int value;

    SessionType(int value) {
        this.value = value;
    }

    int value() {
        return value;
    }

    static SessionType fromValue(int value) {
        SessionType[] list = SessionType.values();
        for (SessionType action : list) {
            if (action.value() == value) {
                return action;
            }
        }
        return null;
    }
}
