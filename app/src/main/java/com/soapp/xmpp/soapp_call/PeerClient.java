package com.soapp.xmpp.soapp_call;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Vibrator;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import com.soapp.R;
import com.soapp.base.BaseActivity;
import com.soapp.global.GlideAPI.GlideApp;
import com.soapp.setup.Soapp;
import com.soapp.sql.DatabaseHelper;
import com.soapp.xmpp.SingleChatHelper.SingleChatStanza;
import com.soapp.xmpp.SmackHelper;

import org.webrtc.AudioSource;
import org.webrtc.AudioTrack;
import org.webrtc.IceCandidate;
import org.webrtc.MediaConstraints;
import org.webrtc.MediaStream;
import org.webrtc.PeerConnection;
import org.webrtc.PeerConnectionFactory;
import org.webrtc.SessionDescription;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;
import java.util.UUID;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.MutableLiveData;

public class PeerClient extends BaseActivity {
    private static final String TAG = "pobs2";

    private static Activity activity;
    public static PeerConnection peerConnection;
    public static boolean callActive = false;
    /**
     * connection status
     * 1 = connecting
     * 2 = connected
     * 3 = disconnected
     * 4 = rejected
     */
    public static MutableLiveData<Integer> connectedState = new MutableLiveData<>();
    static CountDownTimer countDownTimer;
    public String toJid;
    public boolean isCaller;
    PeerConnectionFactory pcFactory;
    List<PeerConnection.IceServer> iceServers = new ArrayList<>();
    AudioManager audioManager;
    private PeerConnectionFactory.InitializationOptions pcfInitOpt;
    private PeerConnectionFactory.Options pcfOpt;
    private AudioSource audioSource;
    private AudioTrack audioTrack;
    private MediaStream localMediaStream;
    private PObserver pObserver;
    private PeerConnection.RTCConfiguration rtcConfig;
    private customSdpObs sdpobs = new customSdpObs() {

        @Override
        public void onSetSuccess() {
            super.onSetSuccess();

            if (!isCaller && !peerConnection.signalingState().equals(PeerConnection.SignalingState.STABLE)) {
                Log.d(TAG, "onSetSuccess: creatr");
                peerConnection.createAnswer(sdpobs, new MediaConstraints());
            }
        }

        @Override
        public void onCreateSuccess(SessionDescription sessionDescription) {
            super.onCreateSuccess(sessionDescription);
            if (sessionDescription.type == SessionDescription.Type.OFFER) {
                peerConnection.setLocalDescription(sdpobs, sessionDescription);

                new SingleChatStanza().SoappCallStanza(toJid, sessionDescription, UUID.randomUUID().toString());
                Log.d(TAG, "onCreateSuccess() called with: sessionDescription OFFER= [" + sessionDescription.description + "]");
            } else if (sessionDescription.type == SessionDescription.Type.ANSWER) {
                peerConnection.setLocalDescription(sdpobs, sessionDescription);

//                SessionDescription sdp = peerConnection.getLocalDescription();
                new SingleChatStanza().SoappCallStanza(toJid, sessionDescription, UUID.randomUUID().toString());
                Log.d(TAG, "onCreateSuccess() called with: sessionDescription ANSWER = [" + sessionDescription.description + "]");
            }
        }
    };
    private MediaConstraints mediaConstraints;
    private ImageView acceptBtn;
    private ImageView rejectBtn;
    private TextView callerName;
    private ImageView callerPic;
    private ImageView loud_audio_btn;
    private ImageView video_btn;
    private ImageView mute_imgbtn;
    private TextView callDescription;
    private String callerNameString;
    private byte[] callerProfile;
    private Ringtone r;
    private Vibrator vibe;
    public static List<IceCandidate> iceCandidateList = new ArrayList<>();


    public static PeerConnection getPeerConnection() {
        return peerConnection;
    }

    public static boolean addIceCandidate(IceCandidate ice) {
        Log.d(TAG, "addIceCandidate() called with: ice = [" + ice + "]");
        if (peerConnection != null) {
            peerConnection.addIceCandidate(ice);
            return true;
        }
        return false;
    }

    public static void setAnswerforCaller(SessionDescription sdp) {
        peerConnection.setRemoteDescription(new customSdpObs() {
            @Override
            public void onCreateSuccess(SessionDescription sessionDescription) {
                Log.d(TAG, "onCreateSuccess() called with: sessionDescription = [" + sessionDescription + "]");
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
        }, sdp);
    }

    public static void callRejected() {
        Log.d(TAG, "callRejected: ");
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
        if (activity != null) {
            activity.runOnUiThread(() -> {
                connectedState.setValue(4);
                CountDownTimer call_ended_timer = new CountDownTimer(3000, 1000) {
                    @Override
                    public void onTick(long millisUntilFinished) {

                    }

                    @Override
                    public void onFinish() {
                        if (activity != null) {
                            activity.finish();
                        }
                    }
                }.start();
            });
        }
    }

    void initPeer() {
        pcfInitOpt = PeerConnectionFactory.InitializationOptions.builder(this)
                .createInitializationOptions();

        pcfOpt = new PeerConnectionFactory.Options();

        PeerConnectionFactory.initialize(pcfInitOpt);
        pcFactory = PeerConnectionFactory.builder()
                .setOptions(pcfOpt)
                .createPeerConnectionFactory();

        mediaConstraints = new MediaConstraints();
        mediaConstraints.mandatory.add(new MediaConstraints.KeyValuePair("OfferToReceiveAudio", "true"));
        mediaConstraints.optional.add(new MediaConstraints.KeyValuePair("DtlsSrtpKeyAgreement", "true"));
//        mediaConstraints.optional.add(new MediaConstraints.KeyValuePair("RtpDataChannels", "true"));

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) ==
                PackageManager.PERMISSION_GRANTED) {
            // put your code for Version>=Marshmallow
        }

        audioSource = pcFactory.createAudioSource(mediaConstraints);

        audioTrack = pcFactory.createAudioTrack("100", audioSource);
        localMediaStream = pcFactory.createLocalMediaStream("audio");
        localMediaStream.addTrack(audioTrack);
        pObserver = new PObserver() {
            @Override
            public void onIceGatheringChange(PeerConnection.IceGatheringState iceGatheringState) {
                super.onIceGatheringChange(iceGatheringState);
            }

            @Override
            public void onIceConnectionChange(PeerConnection.IceConnectionState iceConnectionState) {
                super.onIceConnectionChange(iceConnectionState);
//                if (iceConnectionState == PeerConnection.IceConnectionState.FAILED) {
//                    SessionDescription sdp = new SessionDescription(SessionDescription.Type.PRANSWER, peerConnection.getLocalDescription().description);
//
//                    new SingleChatStanza().SoappCallStanza(toJid, sdp, UUID.randomUUID().toString());
//                }
                connectedState.postValue(getConnectionStateInt(iceConnectionState));

            }

            @Override
            public void onIceCandidate(IceCandidate iceCandidate) {
                super.onIceCandidate(iceCandidate);
                if (peerConnection != null) {
                    if (!isCaller) {
                        new SingleChatStanza().SoappIceCandidateStanza(toJid, iceCandidate, UUID.randomUUID().toString());
                    }
                    iceCandidateList.add(iceCandidate);
                }
            }
        };

        iceServers.add(PeerConnection.IceServer.builder("stun:xmpp.soappchat.com:3478")
                .setTlsCertPolicy(PeerConnection.TlsCertPolicy.TLS_CERT_POLICY_INSECURE_NO_CHECK)
                .createIceServer());
        iceServers.add(PeerConnection.IceServer.builder("turn:xmpp.soappchat.com:3478")
                .setUsername(SmackHelper.getXmppUsername())
                .setPassword("test123456")
                .setTlsCertPolicy(PeerConnection.TlsCertPolicy.TLS_CERT_POLICY_INSECURE_NO_CHECK)
                .createIceServer());
//        iceServers.add(PeerConnection.IceServer.builder("stun:35.187.243.58 :3478")
//                .setTlsCertPolicy(PeerConnection.TlsCertPolicy.TLS_CERT_POLICY_INSECURE_NO_CHECK)
//                .createIceServer());
////        iceServers.add(PeerConnection.IceServer.builder("turn:35.187.243.58:3478")
////                .setUsername(isCaller ? "rlwt" : "test1")
////                .setPassword("123456")
////                .setTlsCertPolicy(PeerConnection.TlsCertPolicy.TLS_CERT_POLICY_INSECURE_NO_CHECK)
//                .createIceServer());
        rtcConfig = new PeerConnection.RTCConfiguration(iceServers);
        rtcConfig.tcpCandidatePolicy = PeerConnection.TcpCandidatePolicy.ENABLED;
//        rtcConfig.candidateNetworkPolicy = PeerConnection.CandidateNetworkPolicy.LOW_COST;

        peerConnection = pcFactory.createPeerConnection(rtcConfig, pObserver);

        peerConnection.addStream(localMediaStream);
        audioManager = (AudioManager) getSystemService(AUDIO_SERVICE);
        audioManager.setMode(AudioManager.MODE_IN_CALL);
        audioManager.setStreamVolume(AudioManager.STREAM_VOICE_CALL, AudioManager.ADJUST_SAME, AudioManager.FLAG_SHOW_UI);
        audioManager.setSpeakerphoneOn(false);
    }

    public void createOffer() {
        peerConnection.createOffer(sdpobs, new MediaConstraints());
    }

    public void createAnswer(SessionDescription sdp) {
        peerConnection.setRemoteDescription(sdpobs, sdp);
    }

    void destroy() {
        if (r != null) {
            r.stop();
        }

        if (vibe != null) {
            vibe.cancel();
        }

//        audioSource.dispose();
//        audioTrack.dispose();

        if (peerConnection != null) {
            peerConnection.close();
//        localMediaStream.dispose();
            peerConnection.dispose();
            peerConnection = null;

            pcFactory.dispose();
        }

        callActive = false;

    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        activity = this;
        if (connectedState == null) {
            connectedState = new MutableLiveData<>();
        }
        connectedState.setValue(99);
        toJid = getIntent().getStringExtra("jid");
        isCaller = getIntent().getBooleanExtra("isCaller", false);


        initPeer();
        initActivityUI();
        if (isCaller) {
            createOffer();
        }

        connectedState.observe(this, integer -> {
            switch (integer) {
                case 2:
                    getCountDownTimer(integer).start();
                    break;

                case 4:
                    callDescription.setText(getConnectionStateText(integer));
                    break;

                default:
                    callDescription.setText(getConnectionStateText(integer));
                    break;
            }
        });

//        if (isCaller) {
//
//        } else {
//            SessionDescription sdp;
//            String[] sdparray = getIntent().getStringArrayExtra("sdp");
//            Log.d(TAG, "onCreate: " + Arrays.toString(sdparray));
//            sdp = new SessionDescription(SessionDescription.Type.fromCanonicalForm(sdparray[0]), sdparray[1]);
//            Log.d(TAG, "onCreate: " + sdp.description);
//
//        }
    }

    private void initActivityUI() {

        callerNameString = DatabaseHelper.getInstance().getNameFromContactRoster(toJid);
        callerProfile = DatabaseHelper.getInstance().getImageBytesThumbFromContactRoster(toJid);

        if (isCaller) {
            onCallUI();
        } else {
            incomingcallUI();

        }
    }

    private void onCallUI() {
        setContentView(R.layout.pickup_call);

        callerPic = findViewById(R.id.user_profile2);
        rejectBtn = findViewById(R.id.reject);
        callDescription = findViewById(R.id.tv_time);
        callerName = findViewById(R.id.tv_user_name);
        loud_audio_btn = findViewById(R.id.loud_audio_btn);

        callerName.setText(callerNameString);

        GlideApp.with(this)
                .load(callerProfile)
                .placeholder(R.drawable.in_propic_circle_520px)
                .into(callerPic);

        connectedState.setValue(0);

        rejectBtn.setOnClickListener(v1 -> {
            new SingleChatStanza().SoappRejectCallStanza(toJid, "Call Terminated", UUID.randomUUID().toString());
            finish();
        });

        loud_audio_btn.setOnClickListener(v -> audioManager.setSpeakerphoneOn(true));


    }

    private void incomingcallUI() {
        setContentView(R.layout.incoming_call);

        final Window win = getWindow();
        win.addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED | WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);
        win.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);

        acceptBtn = findViewById(R.id.accept);
        rejectBtn = findViewById(R.id.reject);
        callerName = findViewById(R.id.tv_user_name);
        callerPic = findViewById(R.id.user_profile2);
        callDescription = findViewById(R.id.tv_incoming);

        Log.d(TAG, "initActivityUI: " + toJid);

        if (callerNameString != null) {
            Log.d(TAG, "initActivityUI: " + callerNameString);
            callerName.setText(String.valueOf(callerNameString));
        }


        GlideApp.with(this)
                .load(callerProfile)
                .placeholder(R.drawable.in_propic_circle_520px)
                .into(callerPic);

        r = RingtoneManager.getRingtone(this, RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE));
//        r.setAudioAttributes(new AudioAttributes.Builder()
//                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
//                .setFlags(AudioAttributes.FLAG_AUDIBILITY_ENFORCED)
//                .setUsage(AudioAttributes.USAGE_NOTIFICATION_COMMUNICATION_REQUEST)
//                .build());

        r.play();

        vibe = (Vibrator) Soapp.getInstance().getApplicationContext().getSystemService(Context
                .VIBRATOR_SERVICE);
        long[] vibpat = {300, 100, 300};
        vibe.vibrate(vibpat, 0);

        acceptBtn.setOnClickListener(v -> {
            r.stop();
            vibe.cancel();

            callActive = true;
            String[] sdparray = getIntent().getStringArrayExtra("sdp");
            SessionDescription sdp = new SessionDescription(SessionDescription.Type.fromCanonicalForm(sdparray[0]),
                    sdparray[1]);
            createAnswer(sdp);
            onCallUI();
        });

        rejectBtn.setOnClickListener(v1 -> {
            r.stop();
            vibe.cancel();

            new SingleChatStanza().SoappRejectCallStanza(toJid, "User Rejected", UUID.randomUUID().toString());
            finish();
        });

    }

    @Override
    protected void onDestroy() {
        destroy();
        countDownTimer = null;
        connectedState = null;
        activity = null;

        super.onDestroy();
    }

    private int getConnectionStateInt(PeerConnection.IceConnectionState state) {
        switch (state) {

            case NEW:
            case CHECKING:
                return 1;


            case CONNECTED:
                return 2;

            case DISCONNECTED:
            case CLOSED:
                return 3;

            case FAILED:
                return 9;

            default:
                return 0;
        }
    }

    private String getConnectionStateText(int state) {
        switch (state) {
            case 1:
                return "Connecting";

            case 2:
                return "Connected";

            case 3:
                return "Terminated";

            case 4:
                return "is Busy";

            case 0:
                return "Dialling";

            case 9:
                return "Failure : Retrying";

            default:
                return "Connecting";
        }
    }

    public CountDownTimer getCountDownTimer(int i) {
        Log.d(TAG, String.format("getCountDownTimer: %d : %d", TimeZone.getDefault().getRawOffset(), TimeZone.getDefault().getOffset(System.currentTimeMillis())));
        if (countDownTimer == null) {
            countDownTimer = new CountDownTimer(150000, 1000) {
                @Override
                public void onTick(long millisUntilFinished) {
                    callDescription.setText(String.format(Locale.ENGLISH, "%s\n%tT",
                            getConnectionStateText(i), 150000 - millisUntilFinished - 27000000));
                }

                @Override
                public void onFinish() {

                }
            };
        } else {
            countDownTimer.cancel();
        }

        return countDownTimer;
    }


    public void IncomingCallSound() {
        //play notification sound in using ringtone channel
        try {
            Uri call = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE);
            r = RingtoneManager.getRingtone(this, call);
            r.setAudioAttributes(new AudioAttributes.Builder()
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .setFlags(AudioAttributes.FLAG_AUDIBILITY_ENFORCED)
                    .setUsage(AudioAttributes.USAGE_NOTIFICATION_COMMUNICATION_REQUEST)
                    .build());

            r.play();

            Vibrator vibe = (Vibrator) Soapp.getInstance().getApplicationContext().getSystemService(Context
                    .VIBRATOR_SERVICE);
            vibe.vibrate(300);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void sendIceCandidate() {
        if (iceCandidateList.size() > 0) {
            for (IceCandidate iceCandidate : iceCandidateList) {
//                            peerConnection.addIceCandidate(iceCandidate);
                Log.d("pobs", "iceCandidateList");
                new SingleChatStanza().SoappIceCandidateStanza(toJid, iceCandidate, UUID.randomUUID().toString());
            }
        }
    }
}
