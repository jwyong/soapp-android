package com.soapp.chat_class.single_chat;

import android.Manifest;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Rect;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Handler;
import android.os.Vibrator;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Base64;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.request.RequestOptions;
import com.soapp.R;
import com.soapp.base.BaseActivity;
import com.soapp.chat_class.share_contact.ShareContactActivity;
import com.soapp.chat_class.single_chat.details.IndiChatDetail;
import com.soapp.global.CameraHelper;
import com.soapp.global.DirectoryHelper;
import com.soapp.global.EncryptionHelper;
import com.soapp.global.GlideAPI.GlideApp;
import com.soapp.global.GlobalVariables;
import com.soapp.global.ImageHelper;
import com.soapp.global.MiscHelper;
import com.soapp.global.PermissionHelper;
import com.soapp.global.Preferences;
import com.soapp.global.SyncHelper;
import com.soapp.global.UIHelper;
import com.soapp.global.chat_log_selection.DetailsLookup;
import com.soapp.global.chat_log_selection.KeyProvider;
import com.soapp.global.chat_log_selection.SelectionState;
import com.soapp.home.Home;
import com.soapp.schedule_class.new_appt.NewIndiExistApptActivity;
import com.soapp.schedule_class.single_appt.IndiScheLog;
import com.soapp.setup.Soapp;
import com.soapp.sql.DatabaseHelper;
import com.soapp.sql.room.entity.ContactRoster;
import com.soapp.sql.room.joiners.IndiChatLogDB;
import com.soapp.xmpp.GlobalMessageHelper.GlobalHeaderHelper;
import com.soapp.xmpp.SingleChatHelper.SingleChatStanza;
import com.soapp.xmpp.SmackHelper;
import com.soapp.xmpp.soapp_call.PeerClient;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.selection.SelectionTracker;
import androidx.recyclerview.selection.StorageStrategy;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import io.github.rockerhieu.emojicon.EmojiconEditText;
import io.github.rockerhieu.emojicon.EmojiconTextView;
import io.github.rockerhieu.emojicon.EmojiconsPopup;
import io.github.rockerhieu.emojicon.EmojiconsPopup.OnSoftKeyboardOpenCloseListener;
import jp.wasabeef.glide.transformations.BlurTransformation;

/* Created by Soapp on 03/08/2017. */
public class IndiChatLog extends BaseActivity implements View.OnClickListener, View
        .OnTouchListener, TextWatcher {
    //for hold menu (selection)
    public static SelectionTracker<Long> selectionTracker;
    public static MutableLiveData<Integer> selectionSize;
    public static List<IndiChatLogDB> indiChatLogDBS;

    private RelativeLayout toolbar_hold_menu;
    private Integer holdIsSender = -1, holdPosition = -1;
    private String holdSenderJID = "", holdSenderText = "", holdUniqueID = "", holdSenderName = "",
            holdImgID = "", holdType = "";

    private MutableLiveData<Boolean> isReplyMode = new MutableLiveData<>();
    private ImageView reply_media, reply_close_btn;

    //for glide chatimages
    public static int ChatLogImageSize = 300;
    //basics
    private UIHelper uiHelper = new UIHelper();

    //public static jid and displayname for access from chatholder
    public static String jid;
    public static String displayName;

    //for typing... indicator
    private TextView chat_info;
    //for audio recording
    float dX, touchX, touchXfinish;

    //stanza and other basics
    private GlobalHeaderHelper globalHeaderHelper = new GlobalHeaderHelper();
    private SingleChatStanza singleChatStanza = new SingleChatStanza();
    private Preferences preferences = Preferences.getInstance();
    private DatabaseHelper databaseHelper = DatabaseHelper.getInstance();
    //elements
    private MediaRecorder mediaRecorder;
    private EmojiconEditText emojiconEditText;
    private ImageView emojiImageView;
    private RecyclerView indi_chat_rv;
    private IndiChatAdapter mAdapter;
    private Button msg_send;
    private ImageButton audioRecordButton;
    private ImageButton indi_track, indi_appt;
    private ImageView msg_plus, indi_chat_image, callbtn;
    private TextView chatTitle, record_audio_timer, chatlog_appt_noti_badge;
    private byte[] profileImg;
    private ConstraintLayout chatlog_record_audio_bar, msg_bar_type;
    private Boolean setingfalse;
    private Handler handler = new Handler();
    private Handler handler2 = new Handler();
    private Handler handler3 = new Handler();
    private Handler handler4 = new Handler();
    private int audioTimer = 0;

    private ConstraintLayout reply_msg_cl;
    private EmojiconTextView chattab_display_reply_name;
    private EmojiconTextView chattab_text_reply_msg;

    public PermissionHelper permissionHelper = new PermissionHelper();

    //handler for only sending out paused stanza after xx ms from user stop typing
    private Handler pausedHandler = new Handler();

    //boolean to stop spamming "composing" stanza every single time user type a letter
    private Boolean wasTyping = false;

    // handle permission sync
    public SyncHelper syncHelper = new SyncHelper(this, "normalSync");

    //boolean for recording audio
    private Boolean isRecord;
    Runnable cancelRecordAudio = new Runnable() {
        public void run() {
            if (isRecord && mediaRecorder != null) {
                mediaRecorder.stop();
                mediaRecorder.release();
            }
            mediaRecorder = null;
            isRecord = false;
            String audioPath = preferences.getValue(IndiChatLog.this, GlobalVariables.STRURL_AUD_PATH);
            if (audioPath != null) {
                File file = new File(new File(GlobalVariables.AUDIO_SENT_PATH), audioPath);
                if (file.exists()) {
                    file.delete();
                }
            }
        }
    };
    //boolean for first time scroll to unread and bottom of page
    private Boolean alreadyScrolled = false;

    //for keyboard open close scrolling
    private int firstVisibleItem;
    private Boolean keyBoardScrolled = false;
    // encryption & decryption
    private EncryptionHelper encryptionHelper = new EncryptionHelper();
    Runnable stopRecordAudioAndSend = new Runnable() {
        public void run() {
            if (isRecord && mediaRecorder != null) {
                mediaRecorder.stop();
                mediaRecorder.release();
                uploadsendaudiofile();
            }
            isRecord = false;
            mediaRecorder = null;
        }
    };

    private Runnable runnableCode = new Runnable() {
        @Override
        public void run() {
            handler2.postDelayed(runnableCode, 1000);
            if (audioTimer < 9) {
                audioTimer = audioTimer + 1;
                record_audio_timer.setText("00.0" + audioTimer);
            } else if (audioTimer < 60) {
                audioTimer = audioTimer + 1;
                record_audio_timer.setText("00." + audioTimer);
            } else if (audioTimer == 60) {
                isRecord = true;
                record_audio_timer.setText("Stop");
                handler.removeCallbacks(startRecordAudio);
                if (mediaRecorder != null) {
                    mediaRecorder.stop();
                    mediaRecorder.release();
                    mediaRecorder = null;
                    uploadsendaudiofile();
                }
            }
        }
    };
    Runnable startRecordAudio = new Runnable() {
        public void run() {
            try {
                if (!isRecord) {
                    isRecord = true;
                    handler2.post(runnableCode);
                    mediaRecorder = new MediaRecorder();
                    setsentaudiofile();
                    mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
                    mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
                    mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
//                        mediaRecorder.setAudioEncoder(MediaRecorder.getAudioSourceMax());
                    mediaRecorder.setMaxDuration(60000);
                    //384000, 16 * 44100, 28800
                    mediaRecorder.setAudioEncodingBitRate(28800);
                    //48000
                    mediaRecorder.setAudioSamplingRate(44100);
                    mediaRecorder.prepare();
                    mediaRecorder.start();

                }
            } catch (Exception e) {
            }
        }
    };

    @SuppressLint("ClickableViewAccessibility")
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.chattab_room_indi);

        uiHelper.setStatusBarColor(this, false, R.color.primaryDark4);

        isRecord = false;
        setupToolbar();
        setTitle("");

        // permission whitelisted  & commment dun delete
        permissionHelper.whitelisted(this);

        //get jid from intent extras
        jid = getIntent().getStringExtra("jid");

        //set appt icon noti badge ONLY if no "link" in intent (not coming from schelog)
        if (!getIntent().hasExtra("link")) {
            LiveData<Integer> apptNotiBadgeLD = Soapp.getDatabase().selectQuery().getApptNotiBadge1Jid(jid);
            apptNotiBadgeLD.observe(this, apptNotiCount -> {

                if (apptNotiCount == null || apptNotiCount == 0) {
                    chatlog_appt_noti_badge.setVisibility(View.GONE);
                } else {
                    chatlog_appt_noti_badge.setVisibility(View.VISIBLE);
                    chatlog_appt_noti_badge.setText(String.valueOf(apptNotiCount));
                }
            });
        }

        ChatLogImageSize = new MiscHelper().getChatLogImageSize(this);

        //save current room id on create (to populate incoming messages)
        preferences.save(IndiChatLog.this, "current_room_id", jid);

        //check whether contact already added to phonebook
        if (databaseHelper.getPhoneNameExist(jid) == 0) { //if no phone name in CR
            databaseHelper.checkAndInsertAddToContactMessage(jid, System.currentTimeMillis());
        }

        //bind and set contents to ids
        View rootView = findViewById(R.id.indi_chat_rl);
        ImageView scroll_bottom = findViewById(R.id.scroll_bottom);
        LinearLayout indi_chat_title = findViewById(R.id.indi_chat_title);

        //for hold menu
        toolbar_hold_menu = findViewById(R.id.toolbar_hold_menu);
        ImageView reply_btn = findViewById(R.id.reply_btn);
        ImageView copy_btn = findViewById(R.id.copy_btn);
        ImageView forward_btn = findViewById(R.id.forward_btn);

        chatTitle = findViewById(R.id.chat_title);
        chat_info = findViewById(R.id.chat_info);
        indi_chat_rv = findViewById(R.id.indi_chat_rv);
        msg_plus = findViewById(R.id.msg_plus);
        msg_send = findViewById(R.id.msg_send);
        indi_appt = findViewById(R.id.indi_appt);
        chatlog_appt_noti_badge = findViewById(R.id.chatlog_appt_noti_badge);
        indi_track = findViewById(R.id.indi_track);
        audioRecordButton = findViewById(R.id.msg_record_audio);
        record_audio_timer = findViewById(R.id.record_audio_timer);
        reply_msg_cl = findViewById(R.id.reply_msg_cl);
        chattab_display_reply_name = findViewById(R.id.chattab_display_reply_name);
        chattab_text_reply_msg = findViewById(R.id.chattab_text_reply_msg);
        reply_media = findViewById(R.id.reply_media);
        reply_close_btn = findViewById(R.id.reply_close_btn);

        scroll_bottom.setOnClickListener(this);
        indi_chat_title.setOnClickListener(this);
        indi_appt.setOnClickListener(this);
        reply_btn.setOnClickListener(this);
        copy_btn.setOnClickListener(this);
        reply_close_btn.setOnClickListener(this);
        indi_track.setOnClickListener(this);
        msg_send.setOnClickListener(this);
        msg_plus.setOnClickListener(this);
        audioRecordButton.setOnTouchListener(this);

        //round image on title bar
        indi_chat_image = findViewById(R.id.indi_chat_image);

        chatlog_record_audio_bar = findViewById(R.id.chatlog_record_audio_bar);
        msg_bar_type = findViewById(R.id.msg_bar_type);

        //emoji
        emojiImageView = findViewById(R.id.emoji_btn_indi);
        emojiconEditText = findViewById(R.id.msg_input);
        emojiconEditText.setImeActionLabel("Enter", KeyEvent.KEYCODE_ENTER);
        emojiconEditText.addTextChangedListener(this);


        //todo call
        callbtn = findViewById(R.id.call_btn);
        callbtn.setOnClickListener(this::onClick);

        //chatinfo livedata
        chatInfoFunction();

        // Give the topmost view of your activity layout hierarchy. This will be used to measure soft keyboard height
        final EmojiconsPopup popup = new EmojiconsPopup(rootView, this);

        //Will automatically set size according to the soft keyboard size
        popup.setSizeForSoftKeyboard();

        //If the emoji popup is dismissed, change emojiButton to smiley icon
        popup.setOnDismissListener(() -> changeEmojiKeyboardIcon(emojiImageView, R.drawable.ic_emoji_100px));

        //If the text keyboard closes, also dismiss the emoji popup
        popup.setOnSoftKeyboardOpenCloseListener(new OnSoftKeyboardOpenCloseListener() {
            @Override
            public void onKeyboardOpen(int keyBoardHeight) {
            }

            @Override
            public void onKeyboardClose() {
                if (popup.isShowing())
                    popup.dismiss();
            }
        });

        //On emoji clicked, add it to edittext
        popup.setOnEmojiconClickedListener(emojicon -> {
            if (emojiconEditText == null || emojicon == null) {
                return;
            }

            int start = emojiconEditText.getSelectionStart();
            int end = emojiconEditText.getSelectionEnd();
            if (start < 0) {
                emojiconEditText.append(emojicon.getEmoji());
            } else {
                emojiconEditText.getText().replace(Math.min(start, end),
                        Math.max(start, end), emojicon.getEmoji(), 0,
                        emojicon.getEmoji().length());
            }
        });

        //On backspace clicked, emulate the KEYCODE_DEL key event
        popup.setOnEmojiconBackspaceClickedListener(v -> {
            KeyEvent event = new KeyEvent(
                    0, 0, 0, KeyEvent.KEYCODE_DEL, 0, 0, 0, 0, KeyEvent.KEYCODE_ENDCALL);
            emojiconEditText.dispatchKeyEvent(event);
        });

        // To toggle between text keyboard and emoji keyboard keyboard(Popup)
        emojiImageView.setOnClickListener(v -> {
            //If popup is not showing => emoji keyboard is not visible, we need to show it
            if (!popup.isShowing()) {
                //If keyboard is visible, simply show the emoji popup
                if (popup.isKeyBoardOpen()) {
                    popup.showAtBottom();
                    changeEmojiKeyboardIcon(emojiImageView, R.drawable.xml_ic_keyboard);
                }

                //else, open the text keyboard first and immediately after that show the emoji popup
                else {
                    emojiconEditText.setFocusableInTouchMode(true);
                    emojiconEditText.requestFocus();
                    popup.showAtBottomPending();
                    final InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    inputMethodManager.showSoftInput(emojiconEditText, InputMethodManager.SHOW_IMPLICIT);
                    changeEmojiKeyboardIcon(emojiImageView, R.drawable.xml_ic_keyboard);
                }
            }

            //If popup is showing, simply dismiss it to show the undelying text keyboard
            else {
                popup.dismiss();
            }
        });

        if (mAdapter == null) {
            mAdapter = new IndiChatAdapter();
            mAdapter.setHasStableIds(true);

            indi_chat_rv.setAdapter(mAdapter);

            //setup selection for recyclerview
            selectionTracker = new SelectionTracker.Builder<>("indiChatLogST", indi_chat_rv,
                    new KeyProvider(),
                    new DetailsLookup(indi_chat_rv),
                    StorageStrategy.createLongStorage())
                    .withSelectionPredicate(new SelectionState(true, false, false))
                    .build();

            //observer selection with liveData
            selectionSize = new MutableLiveData<>();
            selectionSize.setValue(selectionTracker.getSelection().size());
            selectionSize.observe(this, selectionInt -> {
                //got selection, show selection menu
                if (selectionInt > 0) {
                    //disable appt btn first
                    indi_appt.setEnabled(false);

                    //get highlighted position using loop
                    int itemCount = mAdapter.getItemCount();
                    for (long i = 0; i < itemCount; i++) {
                        if (selectionTracker.isSelected(i)) { //get selected position
                            holdPosition = (int) i;
                            break;
                        }
                    }

                    //get issender based on position
                    holdIsSender = indiChatLogDBS.get(holdPosition).getMessage().getIsSender();

                    //hide "copy" for media issenders
                    switch (holdIsSender) {
                        case 20:
                        case 21:
                        case 24:
                        case 25:
                            copy_btn.setVisibility(View.GONE);
                            reply_btn.setVisibility(View.GONE);
                            reply_btn.setVisibility(View.VISIBLE);
                            break;

                        default:
                            copy_btn.setVisibility(View.VISIBLE);
                            break;
                    }

                    toolbar_hold_menu.setVisibility(View.GONE);
                    toolbar_hold_menu.setVisibility(View.VISIBLE);

                } else {
                    //re-enable appt btn
                    indi_appt.setEnabled(true);

                    toolbar_hold_menu.setVisibility(View.GONE);
                }
            });

            //set layout manager
            LinearLayoutManager llm = new LinearLayoutManager(this);
            llm.setOrientation(RecyclerView.VERTICAL);
            llm.setReverseLayout(true);
            llm.setStackFromEnd(true);

            indi_chat_rv.setLayoutManager(llm);
            indi_chat_rv.setItemAnimator(null);

            //testing performance
//            grp_chat_rv.setItemViewCacheSize(30);
            indi_chat_rv.setDrawingCacheEnabled(true);

            IndiChatVM indiChatVM = ViewModelProviders.of(this).get(IndiChatVM.class);
            indiChatVM.setJid(jid);
            indiChatVM.init(Soapp.getDatabase().selectQuery());
            indiChatVM.messagelist.observe(this, pagedList -> {
                mAdapter.submitList(pagedList);
                indiChatLogDBS = pagedList;

                //for scrolling unread or latest msg
                if (!alreadyScrolled) { //first time enter, scroll either to unread or bottom
                    if (databaseHelper.getUnreadMsgExist(jid) == 0) { //scroll to bottom if no unread msges
                        llm.scrollToPosition(0);
                        firstVisibleItem = 0;

                    } else { //scroll to unread msg if got
                        scrollToUnread(pagedList);
                    }
                    alreadyScrolled = true;
                } else { //not first time entering, scroll to bottom if got new msgs
                    if (firstVisibleItem == 0) {
                        indi_chat_rv.smoothScrollToPosition(-10);
                    }
                }
            });

            //for chatinfo
            indiChatVM.getChatInfo();
            indiChatVM.getGetTypingStatus().observe(this, integer -> {
                if (integer != null) {
                    chat_info.setText(integer == 1 ? R.string.typing : R.string.indi_chat_status);
                }
            });

            //set onscroll listener on recyclerview
            indi_chat_rv.addOnScrollListener(new RecyclerView.OnScrollListener() {
                @Override
                public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                    super.onScrollStateChanged(recyclerView, newState);

                    firstVisibleItem = llm.findFirstVisibleItemPosition();
                }

                @Override
                public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                    super.onScrolled(recyclerView, dx, dy);

                    if (llm.findFirstVisibleItemPosition() > 6) {
                        scroll_bottom.setVisibility(View.VISIBLE);
                    } else {
                        scroll_bottom.setVisibility(View.GONE);
                    }
                }
            });

            //set softkeyboard open/close listener
            rootView.getViewTreeObserver().addOnGlobalLayoutListener(() -> {
                Rect r = new Rect();
                rootView.getWindowVisibleDisplayFrame(r);
                int screenHeight = rootView.getRootView().getHeight();

                // r.bottom is the position above soft keypad or device button.
                // if keypad is shown, the r.bottom is smaller than that before.
                int keypadHeight = screenHeight - r.bottom;

                if (keypadHeight > screenHeight * 0.15) { // 0.15 ratio is perhaps enough to determine keypad height.
                    // keyboard is opened
                    if (!keyBoardScrolled) { //scroll to position only once
                        if (firstVisibleItem <= 0) { //only scroll to 0 if user not scrolled
                            indi_chat_rv.scrollToPosition(0);
                        }
                        keyBoardScrolled = true;
                    }

                } else {
                    if (keyBoardScrolled) { //scroll to position only once
                        //keyboard is closed
                        if (firstVisibleItem <= 0) { //only scroll to 0 if user not scrolled
                            indi_chat_rv.scrollToPosition(0);
                        }
                        keyBoardScrolled = false;
                    }
                }
            });
        }

        isReplyMode.setValue(false);
        isReplyMode.observe(this, IndiChatLog.this::updateUIforReplyMode);

        liveDataforProfileImageAndGroupName();


//todo-ibrahim for last online time -- just apply this to implement
       /* Observable.fromCallable(() -> LastActivityManager.getInstanceFor(SmackHelper.getXMPPConnection()).getLastActivity(JidCreate.from(jid + GlobalVariables.xmppURL)))
                .observeOn(Schedulers.io())
                .subscribeOn(AndroidSchedulers.mainThread())
                .subscribe(new DefaultObserver<LastActivity>() {
                    @Override
                    public void onNext(LastActivity lastActivity) {
                        long seconds = lastActivity.getIdleTime();
                        Calendar cal = Calendar.getInstance();
                        cal.setTimeInMillis(System.currentTimeMillis() - TimeUnit.SECONDS.toMillis(seconds));
                        lastTime = String.format("%d:%d:%d", cal.getTime().getHours(), cal.getTime().getMinutes(), cal.getTime().getSeconds());
                    }

                    @Override
                    public void onError(Throwable e) {

                    }

                    @Override
                    public void onComplete() {
                        Log.wtf("wtf", lastTime);
                        chat_info.setText(lastTime);
                    }
                });*/

    }

    private void chatInfoFunction() {

    }

    private void scrollToUnread(List<IndiChatLogDB> messageList) {
        int index = 0;
        for (IndiChatLogDB list : messageList) {
            if (list.getMessage().getIsSender() != null && list.getMessage().getIsSender() == 1) {
                indi_chat_rv.scrollToPosition(index + 1);
            }
            index++;
        }
    }

    private void changeEmojiKeyboardIcon(ImageView iconToBeChanged, int drawableResourceId) {
        iconToBeChanged.setImageResource(drawableResourceId);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {

            //todo for call
            case (R.id.call_btn):
                Intent callpeer = new Intent(this, PeerClient.class);
                callpeer.putExtra("jid", jid);
                callpeer.putExtra("isCaller", true);
                startActivity(callpeer);
                break;


            case (R.id.indi_chat_title):
                Intent indiChatDetail = new Intent(this, IndiChatDetail.class);
                indiChatDetail.putExtra("jid", jid);

                startActivity(indiChatDetail);
                break;

            case (R.id.indi_appt):
                Intent indiScheIntent;
                if (databaseHelper.getApptCount(jid) == 0) { //no appt exists for this chat room
                    indiScheIntent = new Intent(this, NewIndiExistApptActivity.class);

                } else {
                    indiScheIntent = new Intent(this, IndiScheLog.class);

                    indiScheIntent.putExtra("link", "1");

                    //go to closest apptID if got noti badge
                    String apptID = databaseHelper.getClosestApptIDWithNoti(jid);
                    if (apptID != null && !apptID.equals("")) {
                        indiScheIntent.putExtra("apptID", apptID);
                    }
                }
                indiScheIntent.putExtra("jid", jid);

                startActivity(indiScheIntent);
                break;

            //show reply
            case R.id.reply_btn:
                isReplyMode.setValue(true);

                //clear selection
                selectionTracker.clearSelection();
                break;

            //close reply
            case R.id.reply_close_btn:
                isReplyMode.setValue(false);
                break;

            //copy btn
            case R.id.copy_btn:
                holdSenderName = displayName;
                holdSenderText = indiChatLogDBS.get(holdPosition).getMessage().getMsgData();

                ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText(holdSenderName, holdSenderText);
                clipboard.setPrimaryClip(clip);

                selectionTracker.clearSelection();
                selectionSize.setValue(0);

                Toast toast = Toast.makeText(this, "Text Message Copied", Toast.LENGTH_SHORT);
                toast.setGravity(Gravity.CENTER, 0, 0);
                toast.show();
                break;

            case (R.id.msg_send):
                String oriText = emojiconEditText.getText().toString().trim();
                if (oriText.length() > 0) {
                    long currentDate = System.currentTimeMillis();
                    String uniqueId = UUID.randomUUID().toString();
                    String self_username = preferences.getValue(IndiChatLog.this, GlobalVariables.STRPREF_USERNAME);

                    //set variables for send msg action
                    emojiconEditText.setText("");

                    databaseHelper.deleteRDB2Col(DatabaseHelper.MSG_TABLE_NAME, DatabaseHelper.MSG_JID, DatabaseHelper.MSG_ISSENDER,
                            jid, "1");

                    globalHeaderHelper.GlobalHeaderTime(jid);
                    if (!isReplyMode.getValue()) {
                        if (SmackHelper.getXMPPConnection() != null && SmackHelper.getXMPPConnection().isAuthenticated()) {
                            databaseHelper.MessageOutputDatabase(jid, oriText, uniqueId, currentDate);

                            //send original text to encryption helper to get encrypted text
                            String encryptedText = encryptionHelper.encryptText(oriText);
//                        singleChatStanza.SoappChatStanza(encryptedText.replace("\n", ""), jid, self_username, uniqueId);
                            singleChatStanza.SoappChatStanza(encryptedText, jid, self_username, uniqueId);
                            //remove handler to send out pause stanza if msg successfully sent
                            pausedHandler.removeCallbacksAndMessages(null);

                        } else {
                            databaseHelper.saveMessageAndSendWhenOnline("message", jid, null, oriText,
                                    uniqueId, currentDate, "", "", "", "", "", "");

                        }
                    } else {
                        if (SmackHelper.getXMPPConnection() != null && SmackHelper.getXMPPConnection().isAuthenticated()) {
                            databaseHelper.replyOutputDatabase(jid, oriText, uniqueId, currentDate, holdUniqueID, holdType);

                            //send original text to encryption helper to get encrypted text
                            String encryptedText = encryptionHelper.encryptText(oriText);
//                        singleChatStanza.SoappChatStanza(encryptedText.replace("\n", ""), jid, self_username, uniqueId);
                            singleChatStanza.SoappReplyStanza(encryptedText, jid, self_username, uniqueId, holdSenderText, holdSenderJID, holdUniqueID, holdType, holdImgID);
                            //remove handler to send out pause stanza if msg successfully sent
                            pausedHandler.removeCallbacksAndMessages(null);

                        } else {
                            databaseHelper.saveMessageAndSendWhenOnline("reply", jid, null,
                                    oriText, uniqueId, currentDate, holdType, "", holdImgID, "", "", holdUniqueID);

                        }
                        isReplyMode.setValue(false);
                    }
                }
                break;

            case (R.id.msg_plus):
                //close soft keyboard if open
                View focusedView = this.getCurrentFocus();
                if (focusedView != null) {
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                }

                uiHelper.showChatLogPopup(this, jid);
                break;

            case (R.id.scroll_bottom):
                indi_chat_rv.scrollToPosition(0);
                firstVisibleItem = 0;
                break;

            default:
                break;
        }
    }

    private void updateUIforReplyMode(Boolean bool) {
        if (bool) { //user long-clicked a row
            //get reply details based on issender
            switch (holdIsSender) {
                //outgoing text + reply
                case 10: //out text
                case 60: //out reply text
                case 62: //out reply media
                    holdSenderJID = Preferences.getInstance().getValue(this, GlobalVariables.STRPREF_USER_ID);
                    holdSenderName = getString(R.string.you);
                    holdType = "1";

                    holdSenderText = indiChatLogDBS.get(holdPosition).getMessage().getMsgData();
                    break;

                //incoming text + reply
                case 11: //in text with name
                case 12: //in text no name
                case 61: //in reply text
                case 63: //in reply media
                    holdSenderJID = jid;
                    holdSenderName = displayName;
                    holdType = "1";

                    holdSenderText = indiChatLogDBS.get(holdPosition).getMessage().getMsgData();
                    break;

                //outgoing image + video
                case 20:
                case 24:
                    holdSenderJID = Preferences.getInstance().getValue(this, GlobalVariables.STRPREF_USER_ID);
                    holdSenderName = getString(R.string.you);
                    holdType = "2";

                    //set text as media/video depending on which type
                    if (holdIsSender == 20) { //img
                        holdSenderText = getString(R.string.reply_out_img);
                    } else { //vid
                        holdSenderText = getString(R.string.reply_out_vid);
                    }

                    //media details
                    holdImgID = indiChatLogDBS.get(holdPosition).getMessage().getMsgLatitude();
                    break;

                //incoming img + video
                case 21:
                case 25:
                    holdSenderJID = jid;
                    holdSenderName = displayName;
                    holdType = "2";

                    //set text as media/video depending on which type
                    if (holdIsSender == 20) { //img
                        holdSenderText = getString(R.string.reply_out_img);
                    } else { //vid
                        holdSenderText = getString(R.string.reply_out_vid);
                    }

                    //media details
                    holdImgID = indiChatLogDBS.get(holdPosition).getMessage().getMsgLatitude();
                    break;
            }
            holdUniqueID = indiChatLogDBS.get(holdPosition).getMessage().getMsgUniqueId();

            //update reply UI in chatlog
            emojiconEditText.requestFocus();
            InputMethodManager imm = (InputMethodManager)
                    getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.showSoftInput(emojiconEditText, InputMethodManager.SHOW_IMPLICIT);

            reply_msg_cl.setVisibility(View.VISIBLE);
            chattab_text_reply_msg.setText(holdSenderText);
            chattab_display_reply_name.setText(holdSenderName);

            if (holdType.equals("2")) { //img/vid
                reply_media.setVisibility(View.VISIBLE);
                GlideApp.with(this)
                        .load(Base64.decode(holdImgID, Base64.DEFAULT))
                        .placeholder(R.drawable.default_img_240)
                        .transforms(
                                new BlurTransformation(5),
                                new CenterCrop())
                        .into(reply_media);
            } else { //text
                reply_media.setVisibility(View.GONE);
            }

        } else {
            emojiconEditText.setHint(R.string.et_text_hint);
            reply_msg_cl.setVisibility(View.GONE);
        }
    }

    //text change listener for "typing..." on chat input field
    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
    }

    @Override
    public void onTextChanged(CharSequence text, int start, int before, int count) {
        //set search input to class variable since need access from inner class handler
        CharSequence chatInputText = text;

        //remove previous handler for pause msg the moment user starts typing
        pausedHandler.removeCallbacksAndMessages(null);

        if (chatInputText.length() != 0) { //if got text inputted
            //show reply mode first if got highlighted row
            if (selectionSize.getValue() > 0) {
                isReplyMode.setValue(true);
                selectionSize.setValue(0);
                selectionTracker.clearSelection();
            }

            //for typing stanza
            if (!wasTyping) { //user was not typing, change to typing, so start do actions
                wasTyping = true;

                //hide audio button, show send button
                audioRecordButton.setVisibility(View.GONE);
                msg_send.setVisibility(View.VISIBLE);

                //send out typing... stanza
                if (SmackHelper.getXMPPConnection() != null && SmackHelper.getXMPPConnection().isAuthenticated()) {
                    singleChatStanza.SoappComposingStanza(jid);
                }
            }

            //handler to send out pause stanza after user pauses typing for xx ms
            pausedHandler.postDelayed(() -> {
                wasTyping = false;

                //send out paused stanza
                if (SmackHelper.getXMPPConnection() != null && SmackHelper.getXMPPConnection().isAuthenticated()) {
                    singleChatStanza.SoappPausedStanza(jid);
                }
            }, 1000);
        } else { //if no text inputted - could be from first time click in or erase text
            if (wasTyping) {
                wasTyping = false;

                audioRecordButton.setVisibility(View.VISIBLE);
                msg_send.setVisibility(View.INVISIBLE);

                if (SmackHelper.getXMPPConnection() != null && SmackHelper.getXMPPConnection().isAuthenticated()) {
                    singleChatStanza.SoappPausedStanza(jid);
                }
            }
        }
    }

    @Override
    public void afterTextChanged(Editable s) {
        if (s.length() > 0) {
            audioRecordButton.setVisibility(View.GONE);
            msg_send.setVisibility(View.VISIBLE);
        } else {
            audioRecordButton.setVisibility(View.VISIBLE);
            msg_send.setVisibility(View.INVISIBLE);
        }
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        switch (v.getId()) {
            case (R.id.msg_record_audio):
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN: //start recording audio
                        //stop playing audio in other holders
                        if (IndiChatHolder.mediaPlayer2 != null) {
                            IndiChatHolder.mediaPlayer2.stop();
                            IndiChatHolder.mediaPlayer2.release();
                            IndiChatHolder.mediaPlayer2 = null;

                            ContentValues cv2 = new ContentValues();

                            cv2.put("MediaStatus", 100);

                            databaseHelper.updateRDB2Col(DatabaseHelper.MSG_TABLE_NAME, cv2, DatabaseHelper.MSG_JID,
                                    DatabaseHelper.MSG_MEDIASTATUS, jid, "-2");
                        }

                        //check audio mic permission
                        int permissionStorage = ActivityCompat.checkSelfPermission(this, Manifest
                                .permission.WRITE_EXTERNAL_STORAGE);
                        int permissionMic = ActivityCompat.checkSelfPermission(this,
                                Manifest.permission.RECORD_AUDIO);


                        if (permissionStorage == PackageManager.PERMISSION_DENIED || permissionMic == PackageManager.PERMISSION_DENIED) {
                            //no permission to write to storage
                            new PermissionHelper().CheckPermissions(this, 1005, R.string.permission_txt);

                        } else {
                            int pixels = (int) TypedValue.applyDimension(
                                    TypedValue.COMPLEX_UNIT_DIP,
                                    90, getResources().getDisplayMetrics());

                            ViewGroup.LayoutParams layoutParams = audioRecordButton.getLayoutParams();

                            layoutParams.height = pixels;
                            layoutParams.width = pixels;

                            audioRecordButton.setLayoutParams(layoutParams);

                            msg_bar_type.setVisibility(View.INVISIBLE);
                            chatlog_record_audio_bar.setVisibility(View.VISIBLE);

                            dX = event.getX();
                            touchX = event.getRawX();
                            touchXfinish = event.getRawX() - 200;
//                            x1Animation.cancel();
                            setingfalse = true;

                            record_audio_timer.setText("00.00");
                            handler.postDelayed(startRecordAudio, 1000);

                            Vibrator vibe = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
                            vibe.vibrate(200);

                               /* if (record_audio_timer.equals("00.01") ){
                                    Vibrator vibe = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
                                    vibe.vibrate(200);

                                }
                                */

                        }
                        break;

                    case MotionEvent.ACTION_MOVE:
                        if (event.getRawX() < touchXfinish) {
                            handler4.postDelayed(cancelRecordAudio, 500);
//                            emojiconEditText.setVisibility(View.VISIBLE);
//                            emojiImageView.setVisibility(View.VISIBLE);
                            msg_bar_type.setVisibility(View.VISIBLE);
                            chatlog_record_audio_bar.setVisibility(View.GONE);
                            handler.removeCallbacks(startRecordAudio);
                            handler2.removeCallbacks(runnableCode);
                            audioTimer = 0;

                            // doing dun delete
//                            x1Animation.start();

                            if (setingfalse) {
                                int pixels = (int) TypedValue.applyDimension(
                                        TypedValue.COMPLEX_UNIT_DIP,
                                        50, getResources().getDisplayMetrics());
//
                                ValueAnimator anim = ValueAnimator.ofInt(
                                        audioRecordButton.getMeasuredHeight(),
                                        pixels);
                                anim.addUpdateListener(valueAnimator -> {
                                    int val = (Integer) valueAnimator.getAnimatedValue();
                                    ViewGroup.LayoutParams layoutParams =
                                            audioRecordButton.getLayoutParams();
                                    layoutParams.height = val;
                                    layoutParams.width = val;
                                    audioRecordButton.setLayoutParams(layoutParams);
                                });
                                anim.setDuration(100);
                                anim.start();

                                setingfalse = false;
                            }
                        }

                        break;
                    case MotionEvent.ACTION_UP:

                        String checkByteAudio = preferences.getValue(IndiChatLog.this, GlobalVariables.STRURL_AUD_PATH);
                        String path = GlobalVariables.AUDIO_SENT_PATH + checkByteAudio;

                        File checkbyteFile = new File(path);

                        int pixels = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                                50, getResources().getDisplayMetrics());

                        ViewGroup.LayoutParams layoutParams
                                = audioRecordButton.getLayoutParams();
                        layoutParams.height = pixels;
                        layoutParams.width = pixels;
                        audioRecordButton.setLayoutParams(layoutParams);

                        handler3.postDelayed(stopRecordAudioAndSend, 500);

                        audioTimer = 0;
                        handler.removeCallbacks(startRecordAudio);
                        handler2.removeCallbacks(runnableCode);
//                        msg_plus.setVisibility(View.VISIBLE);
//                        emojiconEditText.setVisibility(View.VISIBLE);
//                        emojiImageView.setVisibility(View.VISIBLE);
                        msg_bar_type.setVisibility(View.VISIBLE);
                        chatlog_record_audio_bar.setVisibility(View.GONE);
                        break;

                }
                break;
        }
        return false;
    }

    public void setsentaudiofile() {
        String fileName = ImageHelper.getFileNameForSavingMedia(GlobalVariables.AUDIO_SENT_PATH, "AUD");
        mediaRecorder.setOutputFile(GlobalVariables.AUDIO_SENT_PATH + fileName);
        preferences.save(IndiChatLog.this, GlobalVariables.STRURL_AUD_PATH, fileName);
    }

    public void uploadsendaudiofile() {
        final String uniqueId = UUID.randomUUID().toString();
        final String audioPath = preferences.getValue(IndiChatLog.this, GlobalVariables.STRURL_AUD_PATH);
        databaseHelper.voiceOutput(jid, uniqueId, System.currentTimeMillis(), audioPath);
    }

    //permissions requested when click on camera or gallery
    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case 1005:

                for (int i = 0; i < permissions.length; i++) {
                    String permission = permissions[i];

                    if (grantResults[i] == PackageManager.PERMISSION_DENIED) {
                        boolean showRationale = false;
                        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                            showRationale = shouldShowRequestPermissionRationale(permission);

                        }
                        if (showRationale != false) {
                            preferences.save(IndiChatLog.this, "askAgain", "true");
                            break;
                        } else {
                            preferences.save(IndiChatLog.this, "askAgain", "false");
                        }
                    } else {
                        if (permission.equals(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                            new DirectoryHelper().checkAndCreateDir(this, true);
                        }
                        Toast.makeText(this, R.string.start_audio, Toast.LENGTH_SHORT).show();
                    }
                }
                break;

            case 1007:
                for (int i = 0; i < permissions.length; i++) {
                    String permission = permissions[i];
                    if (grantResults[i] == PackageManager.PERMISSION_DENIED) {
                        boolean showRationale = false;
                        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                            showRationale = shouldShowRequestPermissionRationale(permission);

                        }
                        if (!showRationale) {
                            preferences.save(this, "askAgain", String.valueOf(showRationale));
                            break;
                        } else {
                            preferences.save(this, "askAgain", String.valueOf(showRationale));
                        }

                    } else {
                        syncHelper.new AsyncContactbtn().execute();

                        String chat_from = preferences.getValue(this, "contactPermission");
                        switch (chat_from) {

                            case "shareContact":

                                String dwname = preferences.getValue(this, "dwn");
                                Intent ShareContact = new Intent(this, ShareContactActivity.class);
                                ShareContact.putExtra("jid", dwname);
                                startActivity(ShareContact);

                                break;
                        }

                        break;
                    }
                }
                break;

            case 1001:
                for (int i = 0, len = permissions.length; i < len; i++) {
                    String permission = permissions[i];

                    if (grantResults[i] == PackageManager.PERMISSION_DENIED) {


                        boolean showRationale = false;
                        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                            showRationale = shouldShowRequestPermissionRationale(permission);

                        }

                        if (!showRationale) {

                            preferences.save(this, "askAgain", "false");
                            Toast.makeText(this, R.string.need_camera, Toast.LENGTH_SHORT)
                                    .show();
                            break;
                        } else {
                        }
                        break;
                    } else {
                        if (permission.equals(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                            new DirectoryHelper().checkAndCreateDir(this, true);
                        }
                        Intent cameraintent = new CameraHelper().startCameraIntent(this);

                        cameraintent.putExtra("jid", jid);
                        cameraintent.putExtra("from", "chat");

                        startActivity(cameraintent);

                        break;
                    }
                }
                break;
            case 1006:

                for (int i = 0; i < permissions.length; i++) {
                    String permission = permissions[i];

                    if (grantResults[i] == PackageManager.PERMISSION_DENIED) {
                        boolean showRationale = false;
                        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                            showRationale = shouldShowRequestPermissionRationale(permission);

                        }
                        if (!showRationale) {
                            preferences.save(this, "askAgain", "false");
                            break;
                        }

                    } else {

                        if (permission.equals(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                            new DirectoryHelper().checkAndCreateDir(this, true);
                        }
                    }
                }


                break;
        }
    }

    public void liveDataforProfileImageAndGroupName() {
        LiveData<ContactRoster> live_grpProfileList = Soapp.getDatabase().selectQuery().live_get_cr_profile(jid);
        live_grpProfileList.observe(this, indi_info -> {
            if (indi_info != null) {
                byte[] new_indi_profile_image_thumb = indi_info.getProfilephoto();
                String new_display_name = indi_info.getDisplayname();
                String new_phone_number = indi_info.getPhonenumber();
                String new_phone_name = indi_info.getPhonename();

                if (new_phone_name != null) {
                    displayName = new_phone_name;
                    chatTitle.setText(displayName);
                } else {
                    displayName = new_display_name + " " + new_phone_number;
                    chatTitle.setText(displayName);
                }

                boolean isSameThumbImage = Arrays.equals(profileImg, new_indi_profile_image_thumb);
                if (!isSameThumbImage) {
                    GlideApp.with(this)
                            .load(new_indi_profile_image_thumb)
                            .placeholder(R.drawable.in_propic_circle_150px)
                            .apply(RequestOptions.circleCropTransform())
                            .into(indi_chat_image).waitForLayout();
                    profileImg = new_indi_profile_image_thumb;
                }
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();

        //save seeing current room for unread badges etc
        preferences.save(IndiChatLog.this, "seeing_current_chat", jid);
        if (SmackHelper.getXMPPConnection() != null && SmackHelper.getXMPPConnection().isAuthenticated()) {
            singleChatStanza.SoappSeenStanza("active", jid);
        }

        //disable view if user blocked friend
        if (databaseHelper.getDisabledStatus(jid) == 0) { //user haven't blocked friend
            //set room title back to displayname
            chatTitle.setText(displayName);

            //show items
            msg_bar_type.setVisibility(View.VISIBLE);

            //hide sche linking button if came from sche log chat linking button
            if (getIntent().hasExtra("link")) {
                indi_appt.setVisibility(View.GONE);
            } else {
                indi_appt.setVisibility(View.VISIBLE);
            }

        } else { //user has blocked "friend"
            //set room title as "blocked"
            String blockedDisplayname = getString(R.string.blocked_user_title) + " " + displayName;
            chatTitle.setText(blockedDisplayname);

            //hide items
            msg_bar_type.setVisibility(View.GONE);
            audioRecordButton.setVisibility(View.GONE);
            indi_appt.setVisibility(View.GONE);

        }

        //clear chat noti badges upon seeing
        databaseHelper.zeroBadgeChatRoom(jid, 0);

        //clear home noti badge
        Home.chatBadgeListener();
    }

    //whenever there is another activity above this activity (maybe even partial cover like
    // alertdialog - need more testing)
    @Override
    protected void onPause() {
        super.onPause();

        //clear seeing current room on pause
        preferences.save(IndiChatLog.this, "seeing_current_chat", "");

        //stop recording
        if (isRecord && mediaRecorder != null) {
            handler4.postDelayed(cancelRecordAudio, 500);
//            bottom_audio_bar.setVisibility(View.GONE);
            handler.removeCallbacks(startRecordAudio);
            handler2.removeCallbacks(runnableCode);
            audioTimer = 0;
        }

        //stop playing audio
        if (IndiChatHolder.mediaPlayer2 != null) {
            IndiChatHolder.mediaPlayer2.stop();
            IndiChatHolder.mediaPlayer2.release();
            IndiChatHolder.mediaPlayer2 = null;

            ContentValues cv2 = new ContentValues();

            cv2.put("MediaStatus", 100);

            databaseHelper.updateRDB2Col(DatabaseHelper.MSG_TABLE_NAME, cv2, DatabaseHelper.MSG_JID,
                    DatabaseHelper.MSG_MEDIASTATUS, jid, "-2");
        }

        databaseHelper.deleteRDB2Col(DatabaseHelper.MSG_TABLE_NAME, DatabaseHelper.MSG_JID, DatabaseHelper.MSG_ISSENDER,
                jid, "1");

        if (SmackHelper.getXMPPConnection() != null && SmackHelper.getXMPPConnection().isAuthenticated()) {
            singleChatStanza.SoappSeenStanza("gone", jid);
        }
    }

    @Override
    public void onBackPressed() {
        if (selectionSize.getValue() > 0) { //selection menu on
            selectionTracker.clearSelection();
            selectionSize.setValue(0);

        } else if (isReplyMode.getValue()) {
            isReplyMode.setValue(false);
        } else {
            super.onBackPressed();
            //if came from push notification, go back to home
            if (getIntent().hasExtra("remoteChat")) {
                Intent intentHome = new Intent(Soapp.getInstance().getApplicationContext(), Home.class);
                intentHome.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intentHome);
            }
        }
    }

    @Override
    protected void onDestroy() {
        //clear all current room on destroy
        preferences.save(IndiChatLog.this, "seeing_current_chat", "");
        preferences.save(IndiChatLog.this, "current_room_id", "");

        databaseHelper.deleteRDB2Col(DatabaseHelper.MSG_TABLE_NAME, DatabaseHelper.MSG_JID, DatabaseHelper.MSG_ISSENDER,
                jid, "1");

        if (SmackHelper.getXMPPConnection() != null && SmackHelper.getXMPPConnection().isAuthenticated()) {
            singleChatStanza.SoappSeenStanza("gone", jid);
        }

        selectionTracker = null;
        selectionSize = null;
        indiChatLogDBS = null;

        super.onDestroy();
    }
}