package com.example.soappcamera;


/**
 * Hdr values indicate whether to use high dynamic range techniques when capturing pictures.
 *
 * @see CameraView#setHdr(Hdr)
 */
public enum Hdr {

    /**
     * No HDR.
     */
    OFF(0),

    /**
     * Using HDR.
     */
    ON(1);

    final static Hdr DEFAULT = OFF;

    private int value;

    Hdr(int value) {
        this.value = value;
    }

    int value() {
        return value;
    }

    static Hdr fromValue(int value) {
        Hdr[] list = Hdr.values();
        for (Hdr action : list) {
            if (action.value() == value) {
                return action;
            }
        }
        return null;
    }
}
