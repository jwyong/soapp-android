package com.soapp.xmpp.soapp_call;

import android.util.Log;

import org.webrtc.SessionDescription;

class customSdpObs implements org.webrtc.SdpObserver {

    private static final String TAG = "customSdpObs";

    @Override
    public void onCreateSuccess(SessionDescription sessionDescription) {
        Log.d(TAG, "onCreateSuccess() called with: sessionDescription = [" + sessionDescription.type + "]");
        Log.d(TAG, "onCreateSuccess() called with: sessionDescription = [" + sessionDescription.description + "]");
    }

    @Override
    public void onSetSuccess() {
        Log.d(TAG, "onSetSuccess() called");
    }

    @Override
    public void onCreateFailure(String s) {
        Log.d(TAG, "onCreateFailure() called with: s = [" + s + "]");
    }

    @Override
    public void onSetFailure(String s) {
        Log.d(TAG, "onSetFailure() called with: s = [" + s + "]");
    }
}
