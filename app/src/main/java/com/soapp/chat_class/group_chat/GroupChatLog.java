package com.soapp.chat_class.group_chat;

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
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Vibrator;
import android.provider.Settings;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Base64;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
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
import com.soapp.chat_class.group_chat.details.GroupChatDetail;
import com.soapp.chat_class.share_contact.ShareContactActivity;
import com.soapp.databinding.ChattabRoomGrpBinding;
import com.soapp.global.CameraHelper;
import com.soapp.global.DecryptionHelper;
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
import com.soapp.schedule_class.group_appt.GroupScheLog;
import com.soapp.schedule_class.new_appt.NewIndiExistApptActivity;
import com.soapp.setup.Soapp;
import com.soapp.sql.DatabaseHelper;
import com.soapp.sql.room.entity.ChatList;
import com.soapp.sql.room.entity.ContactRoster;
import com.soapp.sql.room.joiners.GroupChatLogDB;
import com.soapp.xmpp.GlobalMessageHelper.GlobalHeaderHelper;
import com.soapp.xmpp.GroupChatHelper.GroupChatStanza;
import com.soapp.xmpp.SmackHelper;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import androidx.annotation.FloatRange;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.databinding.DataBindingUtil;
import androidx.dynamicanimation.animation.DynamicAnimation;
import androidx.dynamicanimation.animation.SpringAnimation;
import androidx.dynamicanimation.animation.SpringForce;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.selection.SelectionTracker;
import androidx.recyclerview.selection.StorageStrategy;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import io.github.rockerhieu.emojicon.EmojiconEditText;
import io.github.rockerhieu.emojicon.EmojiconTextView;
import io.github.rockerhieu.emojicon.EmojiconsPopup;
import jp.wasabeef.glide.transformations.BlurTransformation;

import static com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions.withCrossFade;

/* Created by chang on 12/08/2017. */

public class GroupChatLog extends BaseActivity implements View.OnClickListener, View.OnTouchListener, TextWatcher {
    //profile image in tool bar
    ImageView grp_chat_image;
    private byte[] profileImgThumb;

    //for hold menu (selection)
    public static SelectionTracker<Long> selectionTracker;
    public static MutableLiveData<Integer> selectionSize;
    public static List<GroupChatLogDB> groupChatLogDBS;

    private RelativeLayout toolbar_hold_menu;
    private Integer holdIsSender = -1, holdPosition = -1;
    private String holdSenderJID = "", holdSenderText = "", holdUniqueID = "", holdSenderName = "",
            holdImgID = "", holdType = "";

    public static int ChatLogImageSize = 300;

    //for reply
    private MutableLiveData<Boolean> isReplyMode = new MutableLiveData<>();
    private ImageView reply_media;
    private ConstraintLayout reply_msg_cl;
    private EmojiconTextView chattab_display_reply_name;
    private EmojiconTextView chattab_text_reply_msg;
    private String userJid;

    // handle permission sync
    public SyncHelper syncHelper = new SyncHelper(this, "normalSync");

    //basics
    private UIHelper uiHelper = new UIHelper();

    //for typing...
    public TextView chat_info;

    //jid for holder access
    public static String jid;

    //group displayName for change grp name access
    public static String displayName;
    private final int RESULT_RECORD_AUDIO = 1;
    SpringAnimation x1Animation;
    float STIFFNESS = SpringForce.STIFFNESS_VERY_LOW; // STIFFNESS_MEDIUM
    float DAMPING_RATIO = SpringForce.DAMPING_RATIO_LOW_BOUNCY; // DAMPING_RATIO_HIGH_BOUNCY
    float dX, touchX, touchXfinish;
    //stanza and other basics
    private GroupChatStanza groupChatStanza = new GroupChatStanza();
    private GlobalHeaderHelper globalHeaderHelper = new GlobalHeaderHelper();
    private DatabaseHelper databaseHelper = DatabaseHelper.getInstance();
    private Preferences preferences = Preferences.getInstance();

    //elements
    private ImageView msg_plus;
    private ImageView scroll_bottom;
    private byte[] profileImg;
    private GroupChatAdapter mAdapter;
    private EmojiconEditText emojiconEditText;
    private ImageView emojiImageView;
    private TextView record_audio_timer, chatTitle, chatlog_appt_noti_badge;
    private RecyclerView grp_chat_rv;
    private Button msg_send;
    private ImageButton audioRecordButton;
    private ImageButton grp_track, grp_appt;
    private ConstraintLayout chatlog_record_audio_bar, msg_bar_type;
    //for audio recording
    private MediaRecorder mediaRecorder;
    private Handler handler = new Handler();
    private Handler handler2 = new Handler();
    private Handler handler4 = new Handler();
    private Handler handler3 = new Handler();
    private int audioTimer = 0;
    //handler for only sending out paused stanza after xx ms from user stop typing
    private Handler pausedHandler = new Handler();
    //boolean to stop spamming "composing" stanza every single time user type a letter
    private Boolean wasTyping = false;
    //boolean for recording audio
    private Boolean isRecord;

    //boolean for first time scroll to unread
    private Boolean alreadyScrolledUnread = false;

    //for keyboard open close scrolling
    private int firstVisibleItem;
    private Boolean keyBoardScrolled = false;
    private Boolean setingfalse;
    // encryption & decryption
    private EncryptionHelper encryptionHelper = new EncryptionHelper();
    private DecryptionHelper decryptionHelper = new DecryptionHelper();

    private PermissionHelper permissionHelper = new PermissionHelper();

    public String testStr = "start";

    public String returnStr() {
        return "fucked";
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ChattabRoomGrpBinding chattabRoomGrpBinding = DataBindingUtil.setContentView(this, R.layout.chattab_room_grp);
        chattabRoomGrpBinding.setUser(this);
//        chattabRoomGrpBinding.executePendingBindings();

//        setContentView(R.layout.chattab_room_grp);

        isRecord = false;
        setupToolbar();
        setTitle("");

        // permission whitelisted  & commment dun delete
        permissionHelper.whitelisted(this);

        //get intent extras
        jid = getIntent().getStringExtra("jid");
        testStr = databaseHelper.getNameFromContactRoster(jid);

        Log.d("FUCK","testStr = "+testStr);

        //set appt icon noti badge ONLY if not from link (not coming from schelog)
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

        //save current room id on create
        preferences.save(GroupChatLog.this, "current_room_id", jid);
        userJid = preferences.getValue(Soapp.getInstance().getApplicationContext(), GlobalVariables.STRPREF_USER_ID);

        //bind and set contents to ids
        LinearLayout grpChatTitle = findViewById(R.id.group_chat_title);
        grpChatTitle.setOnClickListener(this);

        //for hold menu
        toolbar_hold_menu = findViewById(R.id.toolbar_hold_menu);
        ImageView reply_btn = findViewById(R.id.reply_btn);
        ImageView copy_btn = findViewById(R.id.copy_btn);
        ImageView forward_btn = findViewById(R.id.forward_btn);

        chat_info = findViewById(R.id.chat_info);
        grp_appt = findViewById(R.id.grp_appt);
        chatlog_appt_noti_badge = findViewById(R.id.chatlog_appt_noti_badge);
        grp_track = findViewById(R.id.grp_track);
        msg_plus = findViewById(R.id.msg_plus);
        msg_send = findViewById(R.id.msg_send);
        audioRecordButton = findViewById(R.id.msg_record_audio);
        scroll_bottom = findViewById(R.id.scroll_bottom);
        reply_msg_cl = findViewById(R.id.reply_msg_cl);
        chattab_display_reply_name = findViewById(R.id.chattab_display_reply_name);
        chattab_text_reply_msg = findViewById(R.id.chattab_text_reply_msg);
        reply_media = findViewById(R.id.reply_media);
        ImageView reply_close_btn = findViewById(R.id.reply_close_btn);
        chatlog_record_audio_bar = findViewById(R.id.chatlog_record_audio_bar);
        msg_bar_type = findViewById(R.id.msg_bar_type);
        chatTitle = findViewById(R.id.chat_title);
        record_audio_timer = findViewById(R.id.record_audio_timer);
        grp_chat_rv = findViewById(R.id.grp_chat_rv);

        //emoji
        emojiImageView = findViewById(R.id.emoji_btn_indi);
        emojiconEditText = findViewById(R.id.msg_input);
        View rootView = findViewById(R.id.grp_chat_rl);

        scroll_bottom.setOnClickListener(this);
        grp_appt.setOnClickListener(this);
        reply_close_btn.setOnClickListener(this);
        reply_btn.setOnClickListener(this);
        forward_btn.setOnClickListener(this);
        copy_btn.setOnClickListener(this);
        grp_track.setOnClickListener(this);
        msg_send.setOnClickListener(this);
        msg_plus.setOnClickListener(this);
        emojiconEditText.addTextChangedListener(this);

        audioRecordButton.setOnTouchListener(this);

        //round image on title bar
        grp_chat_image = findViewById(R.id.grp_chat_image);

        // Give the topmost view of your activity layout hierarchy.
        // This will be used to measure soft keyboard height
        final EmojiconsPopup popup = new EmojiconsPopup(rootView, this);

        //Will automatically set size according to the soft keyboard size
        popup.setSizeForSoftKeyboard();

        //If the emoji popup is dismissed, change emojiButton to smiley icon
        popup.setOnDismissListener(new EmojiconsPopup.OnDismissListener() {

            @Override
            public void onDismiss() {
                changeEmojiKeyboardIcon(emojiImageView, R.drawable.ic_emoji_100px);
            }
        });

        //If the text keyboard closes, also dismiss the emoji popup
        popup.setOnSoftKeyboardOpenCloseListener(
                new EmojiconsPopup.OnSoftKeyboardOpenCloseListener() {

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
            mAdapter = new GroupChatAdapter();
            mAdapter.setHasStableIds(true);

            grp_chat_rv.setAdapter(mAdapter);

            //setup selection for recyclerview
            selectionTracker = new SelectionTracker.Builder<>("grpChatLogST", grp_chat_rv,
                    new KeyProvider(),
                    new DetailsLookup(grp_chat_rv),
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
                    grp_appt.setEnabled(false);

                    //get highlighted position using loop
                    int itemCount = mAdapter.getItemCount();
                    for (long i = 0; i < itemCount; i++) {
                        if (selectionTracker.isSelected(i)) { //get selected position
                            holdPosition = (int) i;
                            break;
                        }
                    }

                    //get issender based on position
                    holdIsSender = groupChatLogDBS.get(holdPosition).getMessage().getIsSender();

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

                    toolbar_hold_menu.setVisibility(View.VISIBLE);
                } else {
                    //re-enable appt btn
                    grp_appt.setEnabled(true);

                    toolbar_hold_menu.setVisibility(View.GONE);
                }
            });

            LinearLayoutManager llm = new LinearLayoutManager(this);
            llm.setOrientation(RecyclerView.VERTICAL);
            llm.setReverseLayout(true);
            llm.setStackFromEnd(true);

            grp_chat_rv.setLayoutManager(llm);
            grp_chat_rv.setItemAnimator(null);

            //testing performance
//            grp_chat_rv.setItemViewCacheSize(30);
            grp_chat_rv.setDrawingCacheEnabled(true);

            GroupChatVM groupChatVM = ViewModelProviders.of(this).get(GroupChatVM.class);
            groupChatVM.setJid(jid);
            groupChatVM.init(Soapp.getDatabase().selectQuery());
            groupChatVM.messagelist.observe(this, grpChatLogList -> {
                mAdapter.submitList(grpChatLogList);
                groupChatLogDBS = grpChatLogList;

                //for scrolling unread or latest msg
                if (!alreadyScrolledUnread) { //first time enter, scroll either to unread or bottom
                    if (databaseHelper.getUnreadMsgExist(jid) == 0) { //scroll to bottom if no unread msges
                        llm.scrollToPosition(0);
                        firstVisibleItem = 0;

                    } else { //scroll to unread msg if got
                        scrollToUnread(grpChatLogList);
                    }
                    alreadyScrolledUnread = true;
                } else { //not first time entering, scroll to bottom if got new msgs
                    if (firstVisibleItem == 0) {
                        grp_chat_rv.smoothScrollToPosition(-10);
                    }
                }
            });

            groupChatVM.getGetGrpTypingStatus().observe(this, new Observer<ChatList>() {
                @Override
                public void onChanged(ChatList chatList) {
                    if (chatList != null) {
                        if (chatList.getTypingStatus() == 1)
                            chat_info.setText(String.format("%s is %d", chatList, R.string.typing));
                        else chat_info.setText(R.string.indi_chat_status);
                    }
                }
            });

            //set onscroll listener on recyclerview
            grp_chat_rv.addOnScrollListener(new RecyclerView.OnScrollListener() {
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
                            grp_chat_rv.smoothScrollToPosition(0);
                        }
                        keyBoardScrolled = true;
                    }

                } else {
                    if (keyBoardScrolled) { //scroll to position only once
                        //keyboard is closed
                        if (firstVisibleItem <= 0) { //only scroll to 0 if user not scrolled
                            grp_chat_rv.smoothScrollToPosition(0);
                        }
                        keyBoardScrolled = false;
                    }
                }
            });
        }

        audioRecordButton.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @SuppressLint("NewApi")
            @Override

            public void onGlobalLayout() {
                x1Animation = createSpringAnimation(
                        audioRecordButton,
                        SpringAnimation.X,
                        audioRecordButton.getX(),
                        STIFFNESS, DAMPING_RATIO);

                audioRecordButton
                        .getViewTreeObserver()
                        .removeOnGlobalLayoutListener(this);
            }
        });

        isReplyMode.setValue(false);
        isReplyMode.observe(this, GroupChatLog.this::updateUIforReplyMode);

        //for detecting changes in room name/profile img
        liveDataforProfileImageAndGroupName();
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

                    holdSenderText = groupChatLogDBS.get(holdPosition).getMessage().getMsgData();
                    break;

                //incoming text + reply
                case 11: //in text with name
                case 12: //in text no name
                case 61: //in reply text
                case 63: //in reply media
                    holdSenderJID = groupChatLogDBS.get(holdPosition).getMessage().getSenderJid();
                    holdSenderName = getDisplayName();
                    holdType = "1";

                    holdSenderText = groupChatLogDBS.get(holdPosition).getMessage().getMsgData();
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
                    holdImgID = groupChatLogDBS.get(holdPosition).getMessage().getMsgLatitude();
                    break;

                //incoming img + video
                case 21:
                case 25:
                    holdSenderJID = groupChatLogDBS.get(holdPosition).getMessage().getSenderJid();
                    holdSenderName = getDisplayName();
                    holdType = "2";

                    //set text as media/video depending on which type
                    if (holdIsSender == 20) { //img
                        holdSenderText = getString(R.string.reply_out_img);
                    } else { //vid
                        holdSenderText = getString(R.string.reply_out_vid);
                    }

                    //media details
                    holdImgID = groupChatLogDBS.get(holdPosition).getMessage().getMsgLatitude();
                    break;
            }
            holdUniqueID = groupChatLogDBS.get(holdPosition).getMessage().getMsgUniqueId();

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

    private String getDisplayName() {
        //set displayname
        String phoneName;
        if (groupChatLogDBS.get(holdPosition).getContactRoster() != null) {
            phoneName = groupChatLogDBS.get(holdPosition).getContactRoster().getPhonename();

            if (phoneName == null || phoneName.equals("")) {
                String displayName = groupChatLogDBS.get(holdPosition).getContactRoster().getDisplayname();
                if (displayName == null) {
                    phoneName = "";
                } else {
                    phoneName = displayName + " " + groupChatLogDBS.get(holdPosition).getContactRoster().getPhonenumber();
                }
            }
        } else {
            phoneName = "";
        }

        return phoneName;
    }

    private void scrollToUnread(List<GroupChatLogDB> messageList) {
        int index = 0;

        for (GroupChatLogDB list : messageList) {
            if (list.getMessage().getIsSender() != null && list.getMessage().getIsSender() == 1) {
                grp_chat_rv.scrollToPosition(index + 1);

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
            case (R.id.group_chat_title):
                Intent intent = new Intent(this, GroupChatDetail.class);
                intent.putExtra("jid", jid);

                startActivity(intent);
                break;

            case (R.id.grp_appt):
                Intent grpScheIntent;
                if (databaseHelper.getApptCount(jid) == 0) { //no appt exists for this chat room
                    grpScheIntent = new Intent(this, NewIndiExistApptActivity.class);

                } else {
                    grpScheIntent = new Intent(this, GroupScheLog.class);

                    grpScheIntent.putExtra("link", "1");

                    //go to closest apptID if got noti badge
                    String apptID = databaseHelper.getClosestApptIDWithNoti(jid);
                    if (apptID != null && !apptID.equals("")) {
                        grpScheIntent.putExtra("apptID", apptID);
                    }

                }
                grpScheIntent.putExtra("jid", jid);

                startActivity(grpScheIntent);
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
                holdSenderName = getDisplayName();
                holdSenderText = groupChatLogDBS.get(holdPosition).getMessage().getMsgData();

                ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText(holdSenderName, holdSenderText);
                clipboard.setPrimaryClip(clip);

                selectionTracker.clearSelection();
                selectionSize.setValue(0);

                Toast toast = Toast.makeText(this, "Text Message Copied", Toast.LENGTH_SHORT);
                toast.setGravity(Gravity.CENTER, 0, 0);
                toast.show();
                break;

            //forward btn - disabled for now
//            case R.id.forward_btn:
//                holdSenderText = groupChatLogDBS.get(holdPosition).getMessage().getMsgData();
//
//                Intent intentForward = new Intent(this, SharingController.class);
//                intentForward.putExtra("from", "forward");
//
//                switch (holdIsSender) {
//                    //text
//                    case 10: //out text
//                    case 11: //in text with name
//                    case 12: //in text no name
//                    case 60: //out reply text
//                    case 61: //in reply text
//                    case 62: //out reply media
//                    case 63: //in reply media
//                        intentForward.putExtra("forwardIssender", 10);
//                        intentForward.putExtra("forwardMsgData", holdSenderText);
//                        break;
//
//                    //img
//                    case 20:
//                    case 21:
//                        intentForward.putExtra("forwardIssender", 20);
//                        intentForward.putExtra("forwardIssender", 20);
//                        break;
//
//                    //video
//                    case 24:
//                    case 25:
//                        intentForward.putExtra("forwardIssender", 24);
//                        break;
//                }
//                intentForward.putExtra("from", "forward");
//                intentForward.putExtra("from", "forward");
//
//                startActivity(intentForward);
//                break;

            case (R.id.msg_send):
                String text = emojiconEditText.getText().toString().trim();

                if (text.length() > 0) {
                    String uniqueId = UUID.randomUUID().toString();

                    String userName = preferences.getValue(Soapp.getInstance().getApplicationContext(), GlobalVariables.STRPREF_USERNAME);
                    long currentDate = System.currentTimeMillis();

                    String text2 = text.replace("&", "&amp;").replace("<", "&lt;");

                    emojiconEditText.setText("");

                    databaseHelper.deleteRDB2Col(DatabaseHelper.MSG_TABLE_NAME, DatabaseHelper.MSG_JID,
                            DatabaseHelper.MSG_ISSENDER, jid, "1");

                    globalHeaderHelper.GlobalHeaderTime(jid);

                    if (!isReplyMode.getValue()) { //normal msg
                        if (SmackHelper.getXMPPConnection() != null && SmackHelper.getXMPPConnection().isAuthenticated()) {

                            String GencryptTxt = encryptionHelper.encryptText(text2);

                            databaseHelper.GroupMessageOutputDatabase(jid, text, uniqueId, currentDate, userJid);

                            // stanza send to server and base64 clear
                            groupChatStanza.GroupMessage(jid, userJid, userName, GencryptTxt, uniqueId, displayName);


                        } else {
                            databaseHelper.saveMessageAndSendWhenOnline("message", jid, userJid, text, uniqueId, currentDate,
                                    null, null, null, null, null, null);
                        }
                    } else { //reply msg
                        if (SmackHelper.getXMPPConnection() != null && SmackHelper.getXMPPConnection().isAuthenticated()) {

                            String GencryptTxt = encryptionHelper.encryptText(text2);

                            databaseHelper.GroupReplyOutputDatabase(jid, text, uniqueId, currentDate, userJid, holdUniqueID, holdType);

                            // stanza send to server and base64 clear \n
                            groupChatStanza.GroupReply(jid, userJid, userName, GencryptTxt.replace("\n", ""),
                                    uniqueId, displayName, holdSenderText, holdSenderJID, holdUniqueID, holdType, holdImgID);

                        } else {
                            databaseHelper.saveMessageAndSendWhenOnline("reply", jid, userJid,
                                    text, uniqueId, currentDate, holdType, null,
                                    null, null, null, null);
                        }
                        isReplyMode.setValue(false);
                    }

                    //scroll to latest when sending outgoing
                    grp_chat_rv.scrollToPosition(0);
                    firstVisibleItem = 0;

                }
                break;

            case (R.id.msg_plus):
//                databaseHelper.deleteAllFullImage();
                //close soft keyboard if open
                View focusedView = this.getCurrentFocus();
                if (focusedView != null) {
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                }

                new UIHelper().showChatLogPopup(this, jid);

                break;

            case (R.id.scroll_bottom):
                grp_chat_rv.scrollToPosition(0);
                firstVisibleItem = 0;
                break;

            default:
                break;
        }
    }

    //function for text input listener
    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
    }

    @Override
    public void onTextChanged(CharSequence text, int start, int before, int count) {
        //prepare final string user jid first for all cases
        final String userJid = preferences.getValue(Soapp.getInstance().getApplicationContext(), GlobalVariables.STRPREF_USER_ID);

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
                    String userName = preferences.getValue(Soapp.getInstance().getApplicationContext(), GlobalVariables.STRPREF_USERNAME);

                    groupChatStanza.GroupComposingMessage(jid, userJid, userName);
                }
            }

            //handler to send out pause stanza after user pauses typing for xx ms
            pausedHandler.postDelayed(() -> {
                wasTyping = false;

                //send out paused stanza
                if (SmackHelper.getXMPPConnection() != null && SmackHelper.getXMPPConnection().isAuthenticated()) {
                    groupChatStanza.GroupPausedMessage(jid, userJid);
                }
            }, 1000);
        } else { //if no text inputted - could be from first time click in or erase text
            if (wasTyping) {
                wasTyping = false;

                audioRecordButton.setVisibility(View.VISIBLE);
                msg_send.setVisibility(View.INVISIBLE);

                if (SmackHelper.getXMPPConnection() != null && SmackHelper.getXMPPConnection().isAuthenticated()) {
                    groupChatStanza.GroupPausedMessage(jid, userJid);
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
                    case MotionEvent.ACTION_DOWN:
                        if (GroupChatHolder.mediaPlayer2 != null) {
                            GroupChatHolder.mediaPlayer2.stop();
                            GroupChatHolder.mediaPlayer2.release();
                            GroupChatHolder.mediaPlayer2 = null;
                            ContentValues cv2 = new ContentValues();
                            cv2.put("MediaStatus", 100);
                            databaseHelper.updateRDB2Col(DatabaseHelper.MSG_TABLE_NAME,
                                    cv2,
                                    DatabaseHelper.MSG_JID,
                                    DatabaseHelper.MSG_MEDIASTATUS, jid, "-2");
                        }

                        //check audio mic permission
                        int permissionStorage = ActivityCompat.checkSelfPermission(this,
                                Manifest.permission.WRITE_EXTERNAL_STORAGE);
                        int permissionMic = ActivityCompat.checkSelfPermission(this,
                                Manifest.permission.RECORD_AUDIO);

                        if (permissionStorage == PackageManager.PERMISSION_DENIED
                                || permissionMic == PackageManager.PERMISSION_DENIED) {
                            //no permission to write to storage
                            String[] PERMISSIONS_WRITE_RECORD_AUDIO = {
                                    Manifest.permission.RECORD_AUDIO,
                                    Manifest.permission.READ_EXTERNAL_STORAGE,
                                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                            };
                            ActivityCompat.requestPermissions(this,
                                    PERMISSIONS_WRITE_RECORD_AUDIO, RESULT_RECORD_AUDIO);
                        } else {

                            int pixels = (int) TypedValue.applyDimension(
                                    TypedValue.COMPLEX_UNIT_DIP,
                                    90, getResources().getDisplayMetrics());

                            ViewGroup.LayoutParams layoutParams =
                                    audioRecordButton.getLayoutParams();
                            layoutParams.height = pixels;
                            layoutParams.width = pixels;

                            //set icon for big record audio
                            audioRecordButton.setLayoutParams(layoutParams);

                            msg_bar_type.setVisibility(View.INVISIBLE);
                            chatlog_record_audio_bar.setVisibility(View.VISIBLE);

                            dX = event.getX();
                            touchX = event.getRawX();
                            touchXfinish = event.getRawX() - 200;
//                            x1Animation.cancel();
                            setingfalse = true;

                            record_audio_timer.setText("00.00");
                            handler.postDelayed(startAudioRecord, 500);

                            Vibrator vibe = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
                            vibe.vibrate(200);

                        }
                        break;

                    case MotionEvent.ACTION_MOVE:
                        if (event.getRawX() < touchXfinish) {
                            handler4.postDelayed(cancelAudioRecord, 500);

                            msg_bar_type.setVisibility(View.VISIBLE);
                            chatlog_record_audio_bar.setVisibility(View.GONE);
                            handler.removeCallbacks(startAudioRecord);
                            handler2.removeCallbacks(updateAudioRecordTimer);
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
                                anim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                                    @Override
                                    public void onAnimationUpdate(ValueAnimator valueAnimator) {
                                        int val = (Integer) valueAnimator.getAnimatedValue();
                                        ViewGroup.LayoutParams layoutParams =
                                                audioRecordButton.getLayoutParams();
                                        layoutParams.height = val;
                                        layoutParams.width = val;
                                        audioRecordButton.setLayoutParams(layoutParams);
                                    }
                                });
                                anim.setDuration(100);
                                anim.start();

                                setingfalse = false;

                            }
                        } else if (event.getRawX() > touchXfinish && event.getRawX() < touchX) {

//                             doing dun delete
//                            if (setingfalse) {
//                                audioRecordButton.animate().x(event.getRawX() - dX)
//                                        .setDuration(0)
//                                        .start();
////
//                                timesetting = false;
//                            }
                        }

                        break;

                    case MotionEvent.ACTION_UP:
                        String checkByteAudio = preferences.getValue(GroupChatLog.this, GlobalVariables.STRURL_AUD_PATH);
                        String path = GlobalVariables.AUDIO_SENT_PATH + checkByteAudio;

                        File checkbyteFile = new File(path);

                        int pixels = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                                50, getResources().getDisplayMetrics());

                        ViewGroup.LayoutParams layoutParams
                                = audioRecordButton.getLayoutParams();
                        layoutParams.height = pixels;
                        layoutParams.width = pixels;
                        audioRecordButton.setLayoutParams(layoutParams);

                        handler3.postDelayed(releaseAudioRecord, 500);

                        audioTimer = 0;
                        handler.removeCallbacks(startAudioRecord);
                        handler2.removeCallbacks(updateAudioRecordTimer);
                        msg_bar_type.setVisibility(View.VISIBLE);
                        chatlog_record_audio_bar.setVisibility(View.GONE);

                        break;
                }
                break;
        }
        return false;
    }

    //permissions requested when click on camera or gallery
    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case RESULT_RECORD_AUDIO: //audio permissions
                boolean allow1 = true;
                String permission1 = null;
                for (int i = 0, len = permissions.length; i < len; i++) {
                    permission1 = permissions[i];

                    if (grantResults[i] == PackageManager.PERMISSION_DENIED) {

                        allow1 = false;
                    }
                }
                if (allow1) {
                    if (permission1.equals(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                        new DirectoryHelper().checkAndCreateDir(this, true);
                    }

                    Toast.makeText(this, R.string.start_audio, Toast.LENGTH_SHORT).show();
                } else {
                    Runnable actionOpenSettings = new Runnable() {
                        @Override
                        public void run() {
                            Intent intent = new Intent();
                            intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                            Uri uri = Uri.fromParts("package", getPackageName(), null);
                            intent.setData(uri);
                            startActivity(intent);
                        }
                    };

                    uiHelper.dialog2Btns(this, getString(R.string.need_audio_title),
                            getString(R.string.need_audio), R.string.open_settings, R.string
                                    .cancel, R.color.white, R.color.black, actionOpenSettings, null, true);

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

                        if (showRationale == false) {

                            preferences.save(this, "askAgain", "false");
                            Toast.makeText(this, R.string.need_camera, Toast.LENGTH_SHORT)
                                    .show();
                            break;
                        }

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

            default:
                break;

        }

    }

    //runnables for audio recording
    //start to record audio (on action down)
    Runnable startAudioRecord = new Runnable() {
        public void run() {
            try {
                if (!isRecord) {
                    handler2.post(updateAudioRecordTimer);
                    mediaRecorder = new MediaRecorder();
                    isRecord = true;
                    createAudioFile();
                    mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
                    mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
                    mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
                    mediaRecorder.setMaxDuration(60000);
                    mediaRecorder.setAudioEncodingBitRate(28800);
                    mediaRecorder.setAudioSamplingRate(44100);
                    mediaRecorder.prepare();
                    mediaRecorder.start();
                }
            } catch (IOException e) {
            }
        }
    };

    //update timer when recording audio - will auto-stop recording at 60 secs
    private Runnable updateAudioRecordTimer = new Runnable() {
        @Override
        public void run() {
            handler2.postDelayed(updateAudioRecordTimer, 1000);
            if (audioTimer < 9) {
                audioTimer = audioTimer + 1;
                record_audio_timer.setText("00.0" + audioTimer);
            } else if (audioTimer < 60) {
                audioTimer = audioTimer + 1;
                record_audio_timer.setText("00." + audioTimer);
            } else if (audioTimer == 60) {
                isRecord = true;
                record_audio_timer.setText("Stop");
                handler.removeCallbacks(startAudioRecord);

                if (mediaRecorder != null) {
                    mediaRecorder.stop();
                    mediaRecorder.release();
                    mediaRecorder = null;
                    uploadAudioFile();
                }
            }
        }
    };

    //when release audio record btn, send audio file
    Runnable releaseAudioRecord = () -> {
        if (mediaRecorder != null && isRecord) {
            mediaRecorder.stop();
            mediaRecorder.release();
            uploadAudioFile();
        }
        isRecord = false;
        mediaRecorder = null;
    };

    //cancel recording audio and delete (shift to left, onpause, etc)
    Runnable cancelAudioRecord = new Runnable() {
        public void run() {
            if (isRecord && mediaRecorder != null) {
                mediaRecorder.stop();
                mediaRecorder.release();
            }
            mediaRecorder = null;
            isRecord = false;
            String audioPath = preferences.getValue(GroupChatLog.this, GlobalVariables.STRURL_AUD_PATH);
            if (audioPath != null) {
                File file = new File(new File(GlobalVariables.AUDIO_PATH), audioPath);
                if (file.exists()) {
                    file.delete();
                }
            }
        }
    };

    private void createAudioFile() {
        String fileName = ImageHelper.getFileNameForSavingMedia(GlobalVariables.AUDIO_SENT_PATH, "AUD");
        mediaRecorder.setOutputFile(GlobalVariables.AUDIO_SENT_PATH + fileName);
        preferences.save(GroupChatLog.this, GlobalVariables.STRURL_AUD_PATH, fileName);
    }

    public void uploadAudioFile() {
        final String uniqueId = UUID.randomUUID().toString();
        String audioPath = preferences.getValue(GroupChatLog.this, GlobalVariables.STRURL_AUD_PATH);

        databaseHelper.GroupvoiceOutput(jid, uniqueId, System.currentTimeMillis(), audioPath);
    }

    @SuppressLint("Range")
    SpringAnimation createSpringAnimation(View view,
                                          DynamicAnimation.ViewProperty property,
                                          Float finalPosition,
                                          @FloatRange(from = 0.0) Float stiffness,
                                          @FloatRange(from = 0.0) Float dampingRatio) {
        SpringAnimation animation = new SpringAnimation(view, property);
        SpringForce spring = new SpringForce(finalPosition);
        spring.setStiffness(stiffness);
        spring.setDampingRatio(dampingRatio);
        animation.setSpring(spring);
        return animation;

    }

    public void liveDataforProfileImageAndGroupName() {
        LiveData<ContactRoster> live_grpProfileList = Soapp.getDatabase().selectQuery().live_get_cr_profile(jid);
        live_grpProfileList.observe(this, grpProfile_list -> {
            String new_display_name = grpProfile_list.getDisplayname();
            if (new_display_name != null && !new_display_name.equals(displayName)) {
                displayName = new_display_name;
                chatTitle.setText(displayName);
            }

            byte[] new_profileImgThumb = grpProfile_list.getProfilephoto();
            if (new_profileImgThumb != null && !Arrays.equals(new_profileImgThumb, profileImgThumb)) {
                profileImgThumb = new_profileImgThumb;
                GlideApp.with(this)
                        .load(new_profileImgThumb)
                        .placeholder(R.drawable.grp_propic_circle_150px)
                        .thumbnail(0.5f)
                        .encodeQuality(50)
                        .apply(RequestOptions.circleCropTransform())
                        .transition(withCrossFade())
                        .into(grp_chat_image)
                        .waitForLayout();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();

        //save seeing current room on start
        preferences.save(GroupChatLog.this, "seeing_current_chat", jid);

        //clear room + homescreen noti badge on start
        databaseHelper.zeroBadgeChatRoom(jid, 1);

        //clear chattab noti badge
        Home.chatBadgeListener();

        //disable view if user blocked friend
        if (databaseHelper.getDisabledStatus(jid) == 0) { //user haven't blocked friend
            //set room title back to displayname
            chatTitle.setText(displayName);

            //show items
            msg_bar_type.setVisibility(View.VISIBLE);
            audioRecordButton.setVisibility(View.VISIBLE);

            //hide sche linking button if came from sche log chat linking button
            if (getIntent().hasExtra("link")) {
                grp_appt.setVisibility(View.GONE);
                chatlog_appt_noti_badge.setVisibility(View.GONE);
            } else {
                grp_appt.setVisibility(View.VISIBLE);
            }

        } else { //user has blocked "friend"
            //set room title as "blocked"
            String blockedDisplayname = getString(R.string.removed_user_title) + " " + displayName;
            chatTitle.setText(blockedDisplayname);

            //hide items
            msg_bar_type.setVisibility(View.GONE);
            audioRecordButton.setVisibility(View.GONE);
            grp_appt.setVisibility(View.GONE);

        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        //clear seeing current room on pause
        preferences.save(GroupChatLog.this, "seeing_current_chat", "");

        //stop recording
        if (isRecord && mediaRecorder != null) {
            handler4.postDelayed(cancelAudioRecord, 500);
            msg_plus.setVisibility(View.VISIBLE);
            emojiconEditText.setVisibility(View.VISIBLE);
            emojiImageView.setVisibility(View.VISIBLE);
            chatlog_record_audio_bar.setVisibility(View.GONE);
            handler.removeCallbacks(startAudioRecord);
            handler2.removeCallbacks(updateAudioRecordTimer);
            audioTimer = 0;
        }

        //stop playing audio
        if (GroupChatHolder.mediaPlayer2 != null) {
            GroupChatHolder.mediaPlayer2.stop();
            GroupChatHolder.mediaPlayer2.release();
            GroupChatHolder.mediaPlayer2 = null;

            ContentValues cv2 = new ContentValues();

            cv2.put("MediaStatus", 100);

            databaseHelper.updateRDB2Col(DatabaseHelper.MSG_TABLE_NAME, cv2, DatabaseHelper.MSG_JID,
                    DatabaseHelper.MSG_MEDIASTATUS, jid, "-2");
        }

        databaseHelper.deleteRDB2Col(DatabaseHelper.MSG_TABLE_NAME, DatabaseHelper.MSG_JID,
                DatabaseHelper.MSG_ISSENDER, jid, "1");
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

            if (getIntent().hasExtra("remoteChat")) {
                Intent intentHome = new Intent(this, Home.class);
                intentHome.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intentHome);
            }
        }
    }

    @Override
    protected void onDestroy() {
        //clear both current room on destroy
        preferences.save(GroupChatLog.this, "seeing_current_chat", "");
        preferences.save(GroupChatLog.this, "current_room_id", "");

        databaseHelper.deleteRDB2Col(DatabaseHelper.MSG_TABLE_NAME, DatabaseHelper.MSG_JID,
                DatabaseHelper.MSG_ISSENDER, jid, "1");

        selectionTracker = null;
        selectionSize = null;
        groupChatLogDBS = null;

        super.onDestroy();
    }
}