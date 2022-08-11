package com.example.soappcamera;


/**
 * Holds an error with the camera configuration.
 */
public class CameraException extends RuntimeException {

    CameraException(Throwable cause) {
        super(cause);
    }
}
