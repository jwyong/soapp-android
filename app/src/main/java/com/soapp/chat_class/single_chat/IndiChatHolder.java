package com.soapp.chat_class.single_chat;

import android.Manifest;
import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Handler;
import android.provider.Settings;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.util.Base64;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.load.MultiTransformation;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;
import com.soapp.R;
import com.soapp.SoappModel.AddMember;
import com.soapp.camera.VideoPreviewActivity;
import com.soapp.chat_class.add_new_contact.AddNewContactDet;
import com.soapp.food.food_detail.FoodDetailLog;
import com.soapp.global.AddContactHelper;
import com.soapp.global.CameraHelper;
import com.soapp.global.ChatHelper;
import com.soapp.global.DecryptionHelper;
import com.soapp.global.DownloadUploadHelper;
import com.soapp.global.EncryptionHelper;
import com.soapp.global.GlideAPI.GlideApp;
import com.soapp.global.GlobalVariables;
import com.soapp.global.ImageFullView;
import com.soapp.global.MapGps;
import com.soapp.global.PermissionHelper;
import com.soapp.global.Preferences;
import com.soapp.global.ImageLoadHelper;
import com.soapp.global.UIHelper;
import com.soapp.global.UploadImageRequestBody;
import com.soapp.global.UploadVideoRequestBody;
import com.soapp.global.chat_log_selection.SelectionDetails;
import com.soapp.schedule_class.single_appt.IndiScheLog;
import com.soapp.sql.DatabaseHelper;
import com.soapp.sql.room.entity.Message;
import com.soapp.sql.room.joiners.IndiChatLogDB;
import com.soapp.xmpp.SingleChatHelper.SingleChatStanza;
import com.soapp.xmpp.SmackHelper;

import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.CircularProgressDrawable;
import androidx.work.WorkManager;
import io.github.rockerhieu.emojicon.EmojiconTextView;
import jp.wasabeef.glide.transformations.BlurTransformation;

import static com.soapp.sql.DatabaseHelper.MSG_MEDIASTATUS;
import static com.soapp.sql.DatabaseHelper.MSG_ROW;
import static com.soapp.sql.DatabaseHelper.MSG_TABLE_NAME;

/* Created by Soapp on 03/08/2017. */

public class IndiChatHolder extends RecyclerView.ViewHolder implements UploadVideoRequestBody.UploadCallBack,
        UploadImageRequestBody.UploadCallBack {
    private ImageLoadHelper imageLoadHelper = new ImageLoadHelper();
    Calendar timer1 = Calendar.getInstance();
    //basics
    private Context context;
    private Activity activity;

    //selection
    private SelectionDetails selectionDetails = new SelectionDetails();

    //audio
    private MediaPlayer player;
    static MediaPlayer mediaPlayer2;
    private String audioURL;

    private Preferences preferences = Preferences.getInstance();
    private ChatHelper chatHelper = new ChatHelper();

    private DatabaseHelper databaseHelper = DatabaseHelper.getInstance();
    private UIHelper uiHelper = new UIHelper();

    //stanza
    private SingleChatStanza singleChatStanza = new SingleChatStanza();
    //elements (layouts)
    private ConstraintLayout chat_log_bubble_cl;
    private LinearLayout chat_log_contact_call;
    //elements (others)
    private TextView chat_log_time, chat_log_map_title, chat_log_map_address, chat_log_progress_percent, chat_log_contact_number,
            chat_log_media_timer, chat_log_contact_name, chat_log_appt_status_msg, chat_log_res_title, chat_log_date_header;
    private EmojiconTextView chat_log_text_msg;
    private ImageView chat_log_contact_img, chat_log_profile_img, chat_log_new_contact, chat_log_retry,
            chat_log_map, chat_log_media_thumb, chat_log_audio_play, chat_log_res_img, chat_log_video_play,
            chat_log_offline_waiting, chat_log_res_appt, chat_log_download;
    private ImageButton chat_log_navigate;
    private ProgressBar chat_log_audio_timer_bar, bg_progress, chat_log_loading_round;
    // encryption & decryption
    private EncryptionHelper encryptionHelper = new EncryptionHelper();
    private DecryptionHelper decryptionHelper = new DecryptionHelper();

    //define holder variables first
    private DateFormat timeformat = DateFormat.getTimeInstance(DateFormat.SHORT);
    private Date date;
    private String dateStr, textStr, text, videoPath, path, lat, lon, contactName, contactNumber, media, infoID;
    private Spannable spannable;
    private ClipboardManager clipboard;
    private ClipData clipData;
    private Intent intent;
    private DecimalFormat df;
    private int status;
    private ContentValues cv, cv2;
    private int rowId, videoDownloadStatus, videoDownloadId, videoUploadStatus;
    private String videoUrl, originalVideoFilePath, desireFileName, desireFileNameWOExtension, nameInResourceId;
    private File thumbnailWithUniqueIdNaming, thumbnailWithDesiredNaming, mainFile, desiredFile;
    private String imageUrl;
    //    private boolean forceStopAsyncTask;
    private int AcMove, countAcMove;
    private boolean isPlaying;
    private String TTimerfinish;
    private int TimerSec;
    private long TMin;
    private int SectimerInMilis;
    private Handler mHandler;

    private Message replied;
    private EmojiconTextView chat_log_reply_display_name;
    private EmojiconTextView chat_log_reply_msg;
    private String replydisplayName;
    private ImageView reply_media;

    //for appt updates
    private TextView chat_log_appt_title;

    private String displayName, phoneNumber;

    //for glide loading
    CircularProgressDrawable circularProgressDrawable;

    //new helper [ryan]
    private CameraHelper cameraHelper = new CameraHelper();
    private DownloadUploadHelper downloadUploadHelper = new DownloadUploadHelper();

    //final byte for friend's profile img
    private final byte[] friendImgByte = databaseHelper.getImageBytesThumbFromContactRoster(IndiChatLog.jid);
    private final byte[] selfImgByte = databaseHelper.getImageBytesThumbFromContactRoster(preferences.getValue(itemView.getContext(), GlobalVariables.STRPREF_USER_ID));

    // permission
    public PermissionHelper permissionHelper = new PermissionHelper();

    IndiChatHolder(View itemView) {
        super(itemView);

        context = itemView.getContext();
        activity = (Activity) context;

        //glide loading
        circularProgressDrawable = new CircularProgressDrawable(context);
        circularProgressDrawable.setStyle(CircularProgressDrawable.LARGE);
        circularProgressDrawable.setStrokeWidth(5f);
        circularProgressDrawable.setCenterRadius(60f);
        circularProgressDrawable.start();

        //main bubbles
        chat_log_bubble_cl = itemView.findViewById(R.id.chat_log_bubble_cl);

        //global
        chat_log_time = itemView.findViewById(R.id.chat_log_time);
        chat_log_date_header = itemView.findViewById(R.id.chat_log_date_header);
        chat_log_offline_waiting = itemView.findViewById(R.id.chat_log_offline_waiting);

        //text
        chat_log_text_msg = itemView.findViewById(R.id.chat_log_text_msg);

        //share location
        chat_log_map = itemView.findViewById(R.id.chat_log_map);
        chat_log_map_title = itemView.findViewById(R.id.chat_log_map_title);
        chat_log_map_address = itemView.findViewById(R.id.chat_log_map_address);

        //share contact
        chat_log_contact_name = itemView.findViewById(R.id.chat_log_contact_name);
        chat_log_contact_call = itemView.findViewById(R.id.chat_log_contact_call);

        //share restaurant
        chat_log_res_img = itemView.findViewById(R.id.chat_log_res_img);
        chat_log_res_title = itemView.findViewById(R.id.chat_log_res_title);
        chat_log_res_appt = itemView.findViewById(R.id.chat_log_res_appt);

        //navigation button
//        chat_log_navigate = itemView.findViewById(R.id.chat_log_navigate);

        //appt status update msg
        chat_log_appt_status_msg = itemView.findViewById(R.id.chat_log_appt_status_msg);
        chat_log_appt_title = itemView.findViewById(R.id.chat_log_appt_title);

        //share media (img + video)
        chat_log_media_thumb = itemView.findViewById(R.id.chat_log_media_thumb);
        chat_log_loading_round = itemView.findViewById(R.id.chat_log_loading_round);
        chat_log_retry = itemView.findViewById(R.id.chat_log_retry);
        chat_log_download = itemView.findViewById(R.id.chat_log_download);
//        chat_log_progress_percent = itemView.findViewById(R.id.chat_log_progress_percent);

        //share video
        chat_log_video_play = itemView.findViewById(R.id.chat_log_video_play);

        //send audio
        chat_log_audio_play = itemView.findViewById(R.id.chat_log_audio_play);
        chat_log_profile_img = itemView.findViewById(R.id.chat_log_profile_img);
        chat_log_audio_timer_bar = itemView.findViewById(R.id.chat_log_audio_timer_bar);
        bg_progress = itemView.findViewById(R.id.bg_progress);

        //media length timer (for audio and video)
        chat_log_media_timer = itemView.findViewById(R.id.chat_log_media_timer);

        //indi only
        //add new contact
        chat_log_new_contact = itemView.findViewById(R.id.chat_log_new_contact);
    }

    //for selection
    public SelectionDetails getSelectionDetails() {
        return selectionDetails;
    }

    public void setData(final IndiChatLogDB data, int position) {
        //for selection
        selectionDetails.position = position;

        if (IndiChatLog.selectionTracker != null) {
            if (IndiChatLog.selectionTracker.isSelected(selectionDetails.getSelectionKey())) {
                itemView.setBackgroundColor(context.getResources().getColor(R.color.primaryDark3));
            } else {
                //don't set bg to null for add new contact
                int isSender = data.getMessage().getIsSender();
                if (isSender != 3 && isSender != 1) {
                    itemView.setBackground(null);
                }
            }



            //update value in liveData
            IndiChatLog.selectionSize.setValue(IndiChatLog.selectionTracker.getSelection().size());
        }

        //get jid of row first (used for many functions - forward, reply, etc)
        final String jid = data.getMessage().getMsgJid();

        Date date = new Date(data.getMessage().getMsgDate());
        String dateStr = timeformat.format(date);

        //global
        //time
        if (chat_log_time != null) {
            chat_log_time.setText(dateStr);
        }

        //text
        if (chat_log_text_msg != null) {
            //set text
            chat_log_text_msg.setText(data.getMessage().getMsgData());
        }

        //offline msg
        if (chat_log_offline_waiting != null) {
            chat_log_offline_waiting.setVisibility(data.getMessage().getMsgOffline() == 1 ? View.VISIBLE : View.GONE);
        }

        //non-global
        //clear images for holder first
//        if (chat_log_media_thumb != null) {
//            Glide.with(itemView).clear(chat_log_media_thumb);
//        } else if (chat_log_profile_img != null) {
//            Glide.with(itemView).clear(chat_log_profile_img);
//        } else if (chat_log_res_img != null) {
//            Glide.with(itemView).clear(chat_log_res_img);
//        } else if (reply_media != null) {
//            Glide.with(itemView).clear(reply_media);
//        } else if (chat_log_map != null) {
//            Glide.with(itemView).clear(chat_log_map);
//        }

        switch (data.getMessage().getIsSender()) {
            //date header
            case 0:
                DateFormat timeformat30_0 = new SimpleDateFormat("EEE", Locale.ENGLISH);
                DateFormat timeformat30_1 = new SimpleDateFormat("dd MMM yyyy", Locale.ENGLISH);
                String date30 = timeformat30_0.format(data.getMessage().getMsgDate()) + ", " +
                        timeformat30_1.format(data.getMessage().getMsgDate());

                chat_log_date_header.setText(date30);
                break;

            //unread message
            case 1:
                break;

            //add new contact (from unknown number)
            case 3:
                itemView.setOnClickListener(view -> {
                    if (SmackHelper.getXMPPConnection() != null && SmackHelper.getXMPPConnection().isAuthenticated()) {
                        List<AddMember> namePhoneList = databaseHelper.get_displayNamePhone(IndiChatLog.jid);

                        displayName = namePhoneList.get(0).getDisplayname();
                        phoneNumber = namePhoneList.get(0).getPhonenumber();

                        //action for add new contact
                        Runnable addContactAction = () -> {
                            if (IndiChatLog.jid != null) {
                                //add to phonebook first
                                new AddContactHelper(context).createContactPhoneBook(GlobalVariables.string1, phoneNumber);

                                //update displayname to sqlite phonename
                                databaseHelper.updateNewContactToContactRoster(IndiChatLog.jid,
                                        phoneNumber, GlobalVariables.string1, null, null);
                                GlobalVariables.string1 = null;
                            }
                            Toast.makeText(context, R.string.contact_added_soapp, Toast.LENGTH_SHORT).show();
                        };

                        uiHelper.dialog2Btns2Eview(context,
                                context.getString(R.string.add_contacts),
                                displayName,
                                phoneNumber,
                                addContactAction,
                                null,
                                true);

                    } else {
                        Toast.makeText(context, R.string.xmpp_waiting_connection, Toast.LENGTH_SHORT).show();
                    }
                });
                break;

            //blocked/unblocked user
            case 4:
                break;

            //outgoing image
            case 20:
                final String uniqueID20 = data.getMessage().getMsgUniqueId();
                //original path or name
                final String originalPath20 = data.getMessage().getMsgInfoUrl();
                //its a full path of choosen image
//                originalVideoFilePath = data.getMsgInfoUrl();
                path = GlobalVariables.IMAGES_SENT_PATH + "/" + originalPath20;
                int status20 = data.getMessage().getMediaStatus();
                int rowId20 = data.getMessage().getMsgRow();
                //
                switch (status20) {
                    case -1:
                        //upload
                        path = GlobalVariables.IMAGES_SENT_PATH + "/" + originalPath20;
                        chat_log_retry.setVisibility(View.INVISIBLE);
                        chat_log_loading_round.setVisibility(View.VISIBLE);
                        break;

                    //uploading
                    case 1:
                        chat_log_retry.setVisibility(View.VISIBLE);
                        chat_log_retry.setImageResource(R.drawable.ic_cancel_grey_100px);
                        chat_log_loading_round.setVisibility(View.VISIBLE);
                        chatHelper.uploadingWorkerObserver(databaseHelper, "" + rowId20, context);
                        break;

                    //show retry
                    case -3:
                        chat_log_retry.setVisibility(View.VISIBLE);
                        chat_log_retry.setImageResource(R.drawable.ic_media_upload_gradient_100px);
                        chat_log_loading_round.setVisibility(View.GONE);
                        break;

                    //uploading, from -3 to 3 to prevent user from spam clicking when uploading
//                    case 3:
//                        break;

                    case -5:
                        //from camera
                        path = originalPath20;
                        chatHelper.startCompressImageWorker(null, "" + rowId20, path, "vertical", jid, uniqueID20, "-5");
                        break;

                    case -6:
                        //
                        path = originalPath20;
//                        String desireFileNameWOExtension = chatHelper.outgoingImage_Negative6(path);
//                        checkandCompressImage(desireFileNameWOExtension, uniqueID20);
                        chatHelper.startCompressImageWorker(null, "" + rowId20, path, "vertical", jid, uniqueID20, "-6");
                        break;

                    default:
                        chat_log_retry.setVisibility(View.INVISIBLE);
                        chat_log_loading_round.setVisibility(View.GONE);
                        break;
                }

                imageLoadHelper.setImgPath(itemView, chat_log_media_thumb, path, R.drawable.default_img_400);

//                GlideApp.with(itemView)
//                        .load(path)
//                        .diskCacheStrategy(DiskCacheStrategy.NONE)
//                        .placeholder(circularProgressDrawable)
//                        .error(R.drawable.default_img_400)
//                        .centerCrop()
//                        .override(IndiChatLog.ChatLogImageSize, IndiChatLog.ChatLogImageSize)
//                        .into(chat_log_media_thumb);

                //disable long clicks on media
                chat_log_bubble_cl.setOnLongClickListener(view -> true);

                chat_log_bubble_cl.setOnClickListener(v -> {
                    switch (status20) {
                        //cancel upload
                        case 1:
                            chatHelper.stopImageWorker("" + rowId20);
                            break;

                        case -3:
                            chatHelper.startUploadImageWorker(null, "" + rowId20, path, "vertical", jid, uniqueID20);

                            break;

                        default:
                            intent = new Intent(context, ImageFullView.class);
                            intent.putExtra("imageUri", path);
                            context.startActivity(intent);
                            break;
                    }
                });
                break;

            //incoming image
            case 21:
                final int downloadID21 = data.getMessage().getContact();
                final int rowId21 = data.getMessage().getMsgRow();
                final String imageUrl21 = data.getMessage().getMsgInfoUrl();
                final String path21 = GlobalVariables.IMAGES_PATH + imageUrl21;
                final int status21 = data.getMessage().getMediaStatus();
                final String image_base6421 = data.getMessage().getMsgLatitude();
                final String resourceID21 = data.getMessage().getMsgInfoId();
                //
                switch (status21) {
                    //downloading
                    case 1:
                        chat_log_loading_round.setVisibility(View.VISIBLE);
                        chat_log_download.setVisibility(View.VISIBLE);
                        chat_log_download.setImageResource(R.drawable.ic_cancel_grey_100px);
                        break;

                    //retry download
                    case -3:
                        chat_log_loading_round.setVisibility(View.GONE);
                        chat_log_download.setVisibility(View.VISIBLE);
                        chat_log_download.setImageResource(R.drawable.ic_media_download_gradient_150px);
                        break;

                    //download finish
                    case 100:
                        chat_log_loading_round.setVisibility(View.GONE);
                        chat_log_download.setVisibility(View.GONE);
                        break;

                    default:
                        chat_log_loading_round.setVisibility(View.GONE);
                        chat_log_download.setVisibility(View.GONE);
                        break;
                }
                //
                if (status21 != 100 && image_base6421 != null) {
                    final byte[] thumb = Base64.decode(image_base6421, Base64.DEFAULT);

                    imageLoadHelper.setImgByte(chat_log_media_thumb, thumb, R.drawable.default_img_400);

//                    GlideApp.with(itemView)
//                            .load(thumb)
//                            .placeholder(circularProgressDrawable)
//                            .error(R.drawable.default_img_400)
//                            .transforms(new BlurTransformation(15), new CenterCrop())
//                            .thumbnail(0.33f)
//                            .override(IndiChatLog.ChatLogImageSize, IndiChatLog.ChatLogImageSize)
//                            .into(new SimpleTarget<Drawable>() {
//                                @Override
//                                public void onResourceReady(@NonNull Drawable resource, @Nullable Transition<? super Drawable> transition) {
//                                    chat_log_media_thumb.setImageDrawable(resource);
//                                }
//                            });
                } else {
                    imageLoadHelper.setImgPath(itemView, chat_log_media_thumb, path21, R.drawable.default_img_400);

//                    GlideApp.with(itemView)
//                            .load(path21)
//                            .diskCacheStrategy(DiskCacheStrategy.NONE)
//                            .placeholder(circularProgressDrawable)
//                            .error(R.drawable.default_img_400)
//                            .centerCrop()
//                            .into(chat_log_media_thumb)
//                            .waitForLayout();
                }

                //disable long clicks on media
                chat_log_bubble_cl.setOnLongClickListener(view -> true);

                chat_log_bubble_cl.setOnClickListener(v -> {
                    if (PackageManager.PERMISSION_GRANTED != activity.checkCallingOrSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                        permissionHelper.CheckPermissions(activity, 1006, R.string.permission_txt);

                    } else {
                        final String str_row_id = String.valueOf(rowId21);
                        switch (status21) {
                            //stop download
                            case 1:
                                chatHelper.stopDownloadWorker(str_row_id, downloadID21);
                                break;

                            //download
                            case -3:
                                boolean isNeedGetUrlFromResourceId = chatHelper.incomingStartDownload(databaseHelper, imageUrl21, str_row_id, resourceID21, "image");
                                if (isNeedGetUrlFromResourceId) {
//                                    IndiChatHolder.this.getUrlFromResourceId(resourceID21, str_row_id, "image");
                                    chatHelper.startDownloadRetorfitWorker(resourceID21, "" + rowId21, "image");
                                }
                                break;

                            case 100:
                                intent = new Intent(context, ImageFullView.class);
                                intent.putExtra("imageUri", path21);
                                context.startActivity(intent);
                                break;
                        }
                    }
                });
                break;

            //outgoing audio msg
            case 22:
//                setProfileImg(true);
                imageLoadHelper.setImgByte(chat_log_profile_img, selfImgByte, R.drawable.in_propic_circle_150px);

                final String uniqueID14 = data.getMessage().getMsgUniqueId();
                media = data.getMessage().getMsgInfoUrl();
                path = GlobalVariables.AUDIO_SENT_PATH + media;
                status = data.getMessage().getMediaStatus();
                rowId = data.getMessage().getMsgRow();
                String strRowId22 = "" + rowId;
                String workerId22 = databaseHelper.rdb.selectQuery().getMediaWorkerId(strRowId22);
                //set audio timer
                TTimerfinish = cameraHelper.getVideoTimeFormat(path);
                chat_log_media_timer.setText(TTimerfinish);

                switch (status) {
                    //show cancel upload button in future
//                    case -1:
//                        break;
                    case 1:
                        chat_log_audio_play.setImageResource(R.drawable.ic_cancel_grey_100px);
                        chat_log_loading_round.setVisibility(View.VISIBLE);
                        if (workerId22 != null) {
                            chatHelper.audioWorkerObserver(context, workerId22, strRowId22);
                        }
                        break;

                    case -2:
                        chat_log_audio_play.setImageResource(R.drawable.ic_video_pause_gradient_150px);
                        chat_log_loading_round.setVisibility(View.GONE);
                        break;

                    case -3:
                        chat_log_audio_play.setImageResource(R.drawable.ic_media_upload_gradient_100px);
                        chat_log_loading_round.setVisibility(View.GONE);
                        break;

                    case -4:
                        chat_log_loading_round.setVisibility(View.VISIBLE);
                        chatHelper.startUploadAudioWorker(media, "" + rowId, uniqueID14, jid, null);
                        break;

                    default:
                        chat_log_audio_play.setImageResource(R.drawable.ic_video_play_gradient_150px);
                        chat_log_loading_round.setVisibility(View.GONE);
                        if (mHandler != null) {
                            mHandler.removeCallbacksAndMessages(null);
                            AcMove = 0;
                            chat_log_audio_timer_bar.setProgress(0);
                            bg_progress.setProgress(0);
                            chat_log_media_timer.setText(TTimerfinish);
                        }
                        break;
                }

                chat_log_audio_play.setOnClickListener(v -> {
                    if (PackageManager.PERMISSION_GRANTED != activity.checkCallingOrSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {

                        permissionHelper.CheckPermissions(activity, 1006, R.string.permission_txt);

                    } else {
                        switch (status) {
                            //uploading audio (can implement cancel upload next time)
//                            case -1:
//                                break;

                            case 1:
//                                indiChatHelper.cancelMediaRetrofitUpload(String.valueOf(rowId));
                                if (workerId22 != null) {
                                    chatHelper.stopUploadAudioWorker(workerId22);
                                }
                                break;

                            case -2: //playing (showing pause symbol)
                                if (mediaPlayer2 != null) {
                                    mediaPlayer2.stop();
                                    mediaPlayer2.release();
                                    mediaPlayer2 = null;
                                }

                                //change back to play symbol
                                cv = new ContentValues();
                                cv.put("MediaStatus", 100);

                                databaseHelper.updateRDB1Col(MSG_TABLE_NAME, cv,
                                        MSG_ROW, String.valueOf(rowId));
                                break;

                            case -3: //retry download
//                                indiChatHelper.uploadAudio(String.valueOf(rowId), context, path, uniqueID14, jid);
                                chatHelper.startUploadAudioWorker(media, "" + rowId, uniqueID14, jid, null);
                                break;

                            case 100: //ready for playback (showing play button)
                                player = new MediaPlayer();
                                try {
                                    if (mediaPlayer2 != null) {
                                        mediaPlayer2.stop();
                                        mediaPlayer2.release();
                                        mediaPlayer2 = null;
                                    }
                                    //update all rows status to 100 first (pause all others)
                                    cv2 = new ContentValues();
                                    cv2.put("MediaStatus", 100);

                                    databaseHelper.updateRDB2Col(MSG_TABLE_NAME, cv2,
                                            DatabaseHelper.MSG_JID, MSG_MEDIASTATUS,
                                            IndiChatLog.jid, "-2");

                                    //update current row status to -2 (show pause button)
                                    cv = new ContentValues();
                                    cv.put("MediaStatus", -2);

                                    databaseHelper.updateRDB1Col(MSG_TABLE_NAME, cv, MSG_ROW, String.valueOf(rowId));

                                    player.setDataSource(path);
                                    player.prepare();
                                    player.start();
                                    mediaPlayer2 = player;
                                    setAudioTimer(player);
                                } catch (IOException e) {
                                }

                                player.setOnCompletionListener(mp -> {
                                    mediaPlayer2 = null;
                                    player.release();

                                    cv = new ContentValues();
                                    cv.put("MediaStatus", 100);

                                    databaseHelper.updateRDB1Col(MSG_TABLE_NAME, cv,
                                            MSG_ROW, String.valueOf(rowId));
                                });
                                break;

                            default:
                                break;
                        }
                    }
                });
                break;

            //incoming audio msg
            case 23:
                //set value of audio length
                //set sender img
//                setProfileImg(false);
                imageLoadHelper.setImgByte(chat_log_profile_img, friendImgByte, R.drawable.in_propic_circle_150px);

                final int rowId23 = data.getMessage().getMsgRow();
                final String audioURL23 = data.getMessage().getMsgInfoUrl();
                final String path23 = GlobalVariables.AUDIO_PATH + audioURL23;
                final int status23 = data.getMessage().getMediaStatus();
                final String resourceID23 = data.getMessage().getMsgInfoId();
                final int downloadID23 = data.getMessage().getContact();
                //set audio timer
                TTimerfinish = cameraHelper.getVideoTimeFormat(path23);
                chat_log_media_timer.setText(TTimerfinish);

                switch (status23) {
                    case 1:
                        chat_log_audio_play.setImageResource(R.drawable.ic_cancel_grey_100px);
                        chat_log_loading_round.setVisibility(View.VISIBLE);
                        break;

                    case -3:
                        chat_log_loading_round.setVisibility(View.GONE);
                        chat_log_audio_play.setImageResource(R.drawable.ic_media_download_gradient_150px);
                        break;

                    case -2:
                        chat_log_loading_round.setVisibility(View.GONE);
                        chat_log_audio_play.setImageResource(R.drawable.ic_video_pause_gradient_150px);
                        break;

                    default:
                        chat_log_loading_round.setVisibility(View.GONE);
                        chat_log_loading_round.setVisibility(View.GONE);
                        chat_log_audio_play.setImageResource(R.drawable.ic_video_play_gradient_150px);
                        if (mHandler != null) {
                            mHandler.removeCallbacksAndMessages(null);
                            AcMove = 0;
                            chat_log_audio_timer_bar.setProgress(0);
                            bg_progress.setProgress(0);
                            chat_log_media_timer.setText(TTimerfinish);
                        }
                        break;

                }

                chat_log_audio_play.setOnClickListener(v -> {
                    if (PackageManager.PERMISSION_GRANTED != activity.checkCallingOrSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                        permissionHelper.CheckPermissions(activity, 1006, R.string.permission_txt);

                    } else {
                        final String str_row_id = String.valueOf(rowId23);
                        switch (status23) {
                            case 1:
                                chatHelper.stopDownloadWorker(str_row_id, downloadID23);
                                break;

                            case -2:
                                if (mediaPlayer2 != null) {
                                    mediaPlayer2.stop();
                                    mediaPlayer2.release();
                                    mediaPlayer2 = null;
                                }
                                cv = new ContentValues();
                                cv.put(MSG_MEDIASTATUS, 100);

                                databaseHelper.updateRDB1Col(MSG_TABLE_NAME, cv,
                                        MSG_ROW, String.valueOf(rowId23));
                                break;

                            case -3:
                                boolean isNeedGetUrlFromResourceId = chatHelper.incomingStartDownload(databaseHelper, audioURL23, str_row_id, resourceID23, "audio");
                                if (isNeedGetUrlFromResourceId) {
                                    chatHelper.startDownloadRetorfitWorker(resourceID23, str_row_id, "audio");
                                }
                                break;

                            default:
                                //click to play update all rows to 100, but this row to -2
                                player = new MediaPlayer();
                                try {
                                    if (mediaPlayer2 != null) {
                                        mediaPlayer2.stop();
                                        mediaPlayer2.release();
                                        mediaPlayer2 = null;
                                    }
                                    //update all rows status to 0 first
                                    cv2 = new ContentValues();
                                    cv2.put(MSG_MEDIASTATUS, 100);

                                    databaseHelper.updateRDB2Col(MSG_TABLE_NAME, cv2,
                                            DatabaseHelper.MSG_JID, MSG_MEDIASTATUS,
                                            IndiChatLog.jid, "-2");

                                    //update current row status to 1
                                    cv = new ContentValues();
                                    cv.put(MSG_MEDIASTATUS, -2);

                                    databaseHelper.updateRDB1Col(MSG_TABLE_NAME, cv,
                                            MSG_ROW, String.valueOf(rowId23));

                                    player.setDataSource(path23);
                                    player.prepare();
                                    player.start();
                                    mediaPlayer2 = player;
                                    setAudioTimer(player);
                                } catch (IOException e) {
                                }

                                player.setOnCompletionListener(mp -> {
                                    mediaPlayer2 = null;
                                    player.release();

                                    cv = new ContentValues();
                                    cv.put(MSG_MEDIASTATUS, 100);

                                    databaseHelper.updateRDB1Col(MSG_TABLE_NAME, cv,
                                            MSG_ROW, String.valueOf(rowId23));
                                });
                                break;
                        }
                    }
                });
                break;

            //outgoing video
            case 24:
                final String uniqueID24 = data.getMessage().getMsgUniqueId();
                originalVideoFilePath = data.getMessage().getMsgInfoUrl();
                final int videoUploadStatus24 = data.getMessage().getMediaStatus();
                nameInResourceId = data.getMessage().getMsgInfoId();
                final int rowId24 = data.getMessage().getMsgRow();
                final String strRowId24 = "" + rowId24;
                path = GlobalVariables.VIDEO_SENT_PATH + "/" + originalVideoFilePath + ".mp4";
                //
                chat_log_loading_round.setIndeterminate(true);
                //
                chatHelper.videoLoadingListener(videoUploadStatus24, context, chat_log_loading_round, rowId24);

                if (rowId24 == data.getMessage().getMsgRow()) {
                    MediaPlayer VideoTime = new MediaPlayer();
                    try {
                        VideoTime.setDataSource(path);
                        VideoTime.prepare();
                        timer1.setTimeInMillis(VideoTime.getDuration() + 1000);
                        TimerSec = timer1.get(Calendar.SECOND);
                        TMin = TimeUnit.MILLISECONDS.toMinutes(TimerSec);
                        TTimerfinish = String.format(Locale.ENGLISH, "%02d:%02d", TMin, TimerSec);
                        chat_log_media_timer.setText(TTimerfinish);

                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                switch (videoUploadStatus24) {
                    //upload video after compress or copy file
                    case -1:
                        videoPath = GlobalVariables.VIDEO_SENT_PATH + "/" + nameInResourceId + ".mp4";
                        path = GlobalVariables.VIDEO_SENT_THUMBNAIL_PATH + "/" + nameInResourceId + ".jpg";
                        chat_log_loading_round.setVisibility(View.VISIBLE);
                        //
//                        indiChatHelper.uploadVideo(context, String.valueOf(rowId24), jid, videoPath, uniqueID24, path, "vertical");
                        //
                        chat_log_retry.setVisibility(View.VISIBLE);
                        chat_log_retry.setImageResource(R.drawable.ic_cancel_grey_100px);
                        chatHelper.startUploadVideoWorker(videoPath, strRowId24, path, jid, null, "vertical", uniqueID24);
                        break;

                    case 1:
                        chat_log_loading_round.setIndeterminate(false);
                        chat_log_loading_round.setProgressDrawable(context.getDrawable(R.drawable.xml_custom_progress_bar_2));
                        videoPath = GlobalVariables.VIDEO_SENT_PATH + "/" + nameInResourceId + ".mp4";
                        path = GlobalVariables.VIDEO_SENT_THUMBNAIL_PATH + "/" + nameInResourceId + ".jpg";
                        chat_log_loading_round.setVisibility(View.VISIBLE);
                        chat_log_retry.setVisibility(View.VISIBLE);
                        chat_log_retry.setImageResource(R.drawable.ic_cancel_grey_100px);
                        chatHelper.videoUploadWorkerObserver("" + rowId24, context);
                        break;

                    //failed to upload
                    case -3:
                        videoPath = GlobalVariables.VIDEO_SENT_PATH + "/" + nameInResourceId + ".mp4";
                        path = GlobalVariables.VIDEO_SENT_THUMBNAIL_PATH + "/" + nameInResourceId + ".jpg";
                        chat_log_retry.setImageResource(R.drawable.ic_media_upload_gradient_100px);
                        chat_log_video_play.setVisibility(View.VISIBLE);
                        chat_log_loading_round.setVisibility(View.GONE);
                        chat_log_retry.setVisibility(View.VISIBLE);
                        break;

                    //compress video
                    case -4:
                        videoPath = originalVideoFilePath;
                        path = GlobalVariables.VIDEO_SENT_THUMBNAIL_PATH + "/" + nameInResourceId + ".jpg";
                        chat_log_video_play.setVisibility(View.VISIBLE);
                        chat_log_loading_round.setVisibility(View.VISIBLE);
//                        chatHelper.outGoingVideoStartService(context, databaseHelper, videoPath, "" + rowId24, nameInResourceId, jid, 19, uniqueID24);
                        chatHelper.startCompressVideoWorker(context, "" + rowId24, videoPath, nameInResourceId, "" + jid, null, 19, uniqueID24);
                        break;

                    case 4:
                        //video is in queue
                        //show spinner
                        videoPath = originalVideoFilePath;
                        path = GlobalVariables.VIDEO_SENT_THUMBNAIL_PATH + "/" + nameInResourceId + ".jpg";
                        chat_log_loading_round.setVisibility(View.VISIBLE);
                        chat_log_retry.setVisibility(View.VISIBLE);
                        chat_log_retry.setImageResource(R.drawable.ic_cancel_grey_100px);
                        break;

                    //process thumbnail only without compression
                    case -5:
                        //uniqueID used as name for the video thumbnail and video, so must use uniqueID to renamed the file
                        videoPath = originalVideoFilePath;
                        path = GlobalVariables.VIDEO_SENT_THUMBNAIL_PATH + "/" + uniqueID24 + ".jpg";
                        //go to -1 to upload
                        chatHelper.outGoingVideoNegative5_Negative6(databaseHelper, videoPath, uniqueID24, rowId24, -5);
                        break;

                    //rename thumbnail only
                    case -6:
                        String fileName = chatHelper.outGoingVideoNegative5_Negative6(databaseHelper, null, uniqueID24, rowId24, -6);
                        path = GlobalVariables.VIDEO_SENT_THUMBNAIL_PATH + "/" + fileName + ".jpg";
                        chat_log_retry.setVisibility(View.INVISIBLE);
                        break;

                    //compressing
                    //28 august 2018 note: temporary 9 is complete compressing
                    case -7:
                        chat_log_loading_round.setIndeterminate(false);
                        videoPath = originalVideoFilePath;
                        path = GlobalVariables.VIDEO_SENT_THUMBNAIL_PATH + "/" + nameInResourceId + ".jpg";
                        chat_log_loading_round.setVisibility(View.VISIBLE);
                        chat_log_retry.setVisibility(View.VISIBLE);
                        chat_log_retry.setImageResource(R.drawable.ic_cancel_grey_100px);
                        chatHelper.videoCompressWorkerObserver("" + rowId24, context);
                        break;

                    //force stop compressing
                    case 7:
                        chat_log_loading_round.setVisibility(View.GONE);
                        path = GlobalVariables.VIDEO_SENT_THUMBNAIL_PATH + "/" + nameInResourceId + ".jpg";
                        chat_log_retry.setVisibility(View.VISIBLE);
                        chat_log_retry.setImageResource(R.drawable.ic_media_upload_gradient_100px);
                        break;

                    //in queue but abandoned
                    case -8:
                        path = GlobalVariables.VIDEO_SENT_THUMBNAIL_PATH + "/" + nameInResourceId + ".jpg";
                        videoPath = originalVideoFilePath;
                        chat_log_loading_round.setVisibility(View.GONE);
                        chat_log_retry.setVisibility(View.VISIBLE);
                        chat_log_retry.setImageResource(R.drawable.ic_media_upload_gradient_100px);
                        break;

                    //success
                    default:
                        videoPath = GlobalVariables.VIDEO_SENT_PATH + "/" + nameInResourceId + ".mp4";
                        path = GlobalVariables.VIDEO_SENT_THUMBNAIL_PATH + "/" + nameInResourceId + ".jpg";
                        if (!chatHelper.checkIsVideo(context, videoPath)) {
                            videoPath = originalVideoFilePath;
                        }
                        chat_log_loading_round.setVisibility(View.GONE);
                        chat_log_retry.setVisibility(View.INVISIBLE);
                        chat_log_video_play.setImageResource(R.drawable.ic_media_play_gradient_150px);
                        break;
                }

                //set video timer length based on metadata
                if (videoPath == null) {
                    videoPath = originalVideoFilePath;
                }

                try {
                    chat_log_media_timer.setText(cameraHelper.getVideoTimeFormat(videoPath));
                } catch (Exception e) {
                    e.printStackTrace();
                }

                //set thumb
                imageLoadHelper.setImgPath(itemView, chat_log_media_thumb, path, R.drawable.default_img_400);

//                GlideApp.with(itemView)
//                        .load(path)
//                        .placeholder(circularProgressDrawable)
//                        .error(R.drawable.default_img_400)
//                        .diskCacheStrategy(DiskCacheStrategy.NONE)
////                        .skipMemoryCache(true)
//                        .error(R.drawable.default_img_400)
//                        .dontTransform()
//                        .centerCrop()
//                        .into(chat_log_media_thumb)
//                        .waitForLayout();

                chat_log_retry.setOnClickListener(view -> {

                    switch (videoUploadStatus24) {
                        case 1:
                            String worker_id_2 = databaseHelper.rdb.selectQuery().getMediaWorkerId(strRowId24);
                            if (worker_id_2 != null) {
                                WorkManager.getInstance().cancelWorkById(UUID.fromString(worker_id_2));
                            }
                            break;

                        case -3:
                            chat_log_loading_round.setProgress(0);
                            chatHelper.startUploadVideoWorker(videoPath, strRowId24, path, jid, null, "vertical", uniqueID24);
                            break;

                        case -7:
                            chat_log_loading_round.setProgress(0);
                            chatHelper.stopCompressVideo(strRowId24);
                            break;

                        //put back in queue if has video compressing, else compress video
                        case -8:
                        case 7:
                            chatHelper.startCompressVideoWorker(context, strRowId24, videoPath, nameInResourceId, "" + jid, null, 19, uniqueID24);
                            break;

                        case 4://cancel video in queue
                            databaseHelper.updateMsgMediaStatusReturn(-8, strRowId24);
                            break;
                    }
                });

                //disable long clicks on media
                chat_log_bubble_cl.setOnLongClickListener(view -> true);

                chat_log_bubble_cl.setOnClickListener(v -> {
                    if (PackageManager.PERMISSION_GRANTED != activity.checkCallingOrSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                        permissionHelper.CheckPermissions(activity, 1006, R.string.permission_txt);

                    } else {
                        intent = new Intent(context, VideoPreviewActivity.class);
                        intent.putExtra("outgoingVideo", videoPath);
                        context.startActivity(intent);
                    }
                });
                break;

            //incoming video
            case 25:
                final int downloadID25 = data.getMessage().getContact();
                final String resourceID25 = data.getMessage().getMsgInfoId();
                final int rowId25 = data.getMessage().getMsgRow();
                final String videoUrl25 = data.getMessage().getMsgInfoUrl();
                final String video_thumb_base6425 = data.getMessage().getMsgLatitude();
                final String path25 = GlobalVariables.VIDEO_THUMBNAIL_PATH + "/" + videoUrl25 + ".jpg";
                final String videoPath25 = GlobalVariables.VIDEO_PATH + videoUrl25 + ".mp4";
                final int videoDownloadStatus25 = data.getMessage().getMediaStatus();
                chat_log_loading_round.setIndeterminate(true);
                IntentFilter filter25 = null;

//                if (videoDownloadStatus25 == 1){
//                    Log.i("diao","videoUploadStatus24 == 1");
//                    filter25 = new IntentFilter("incomingVideoDownload"+rowId25);
//                    receiver = new MediaRequestReceiver(chat_log_loading_round);
//                    context.registerReceiver( receiver, filter25);
//                }

                switch (videoDownloadStatus25) {
                    //retry download
                    case -3:
                        chat_log_video_play.setImageResource(R.drawable.ic_media_download_gradient_150px);
                        chat_log_loading_round.setVisibility(View.GONE);
                        break;

                    //downloading
                    case 1:
                        chat_log_loading_round.setIndeterminate(false);
                        chat_log_video_play.setImageResource(R.drawable.ic_cancel_grey_100px);
                        chat_log_loading_round.setVisibility(View.VISIBLE);
                        break;

                    //finish download
                    case 100:
                        chat_log_loading_round.setVisibility(View.GONE);
                        chat_log_video_play.setImageResource(R.drawable.ic_media_play_gradient_150px);

                        //set video timer length based on metadata
                        chat_log_media_timer.setText(cameraHelper.getVideoTimeFormat(videoPath25));
                        break;

                    default:
                        chat_log_loading_round.setIndeterminate(true);
                        chat_log_loading_round.setVisibility(View.VISIBLE);
//                        chat_log_video_play.setImageResource(R.drawable.ic_media_download_gradient_150px);
                        break;
                }

                if (videoDownloadStatus25 != 100 && video_thumb_base6425 != null) {
                    final byte[] thumb = Base64.decode(video_thumb_base6425, Base64.DEFAULT);

                    imageLoadHelper.setImgByte(chat_log_media_thumb, thumb, R.drawable.default_img_400);

//                    GlideApp.with(itemView)
//                            .load(thumb)
//                            .placeholder(R.drawable.default_img_400)
//                            .transform(new MultiTransformation<>(
//                                    new BlurTransformation(15),
//                                    new CenterCrop()
//                            )).into(new SimpleTarget<Drawable>() {
//                        @Override
//                        public void onResourceReady(@NonNull Drawable resource, @Nullable Transition<? super Drawable> transition) {
//                            chat_log_media_thumb.setImageDrawable(resource);
//                        }
//                    });
                } else {
                    imageLoadHelper.setImgPath(itemView, chat_log_media_thumb, path25, R.drawable.default_img_400);

//                    GlideApp.with(itemView)
//                            .load(path25)
//                            .placeholder(circularProgressDrawable)
//                            .error(R.drawable.default_img_400)
//                            .centerCrop()
//                            .into(chat_log_media_thumb)
//                            .waitForLayout();
                }

                //disable long clicks on media
                chat_log_bubble_cl.setOnLongClickListener(view -> true);

                chat_log_bubble_cl.setOnClickListener(v -> {
                    if (PackageManager.PERMISSION_GRANTED != activity.checkCallingOrSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {

                        permissionHelper.CheckPermissions(activity, 1006, R.string.permission_txt);

                    } else {
                        final String str_row_id = String.valueOf(rowId25);
                        switch (videoDownloadStatus25) {
                            //start download
                            case -3:
                                boolean isNeedGetUrlFromResourceId = chatHelper.incomingStartDownload(databaseHelper, videoUrl25, str_row_id, resourceID25, "video");
                                if (isNeedGetUrlFromResourceId) {
                                    chatHelper.startDownloadRetorfitWorker(resourceID25, str_row_id, "video");
                                }
                                break;

                            //stopdownload
                            case 1:
                                chatHelper.stopDownloadWorker(str_row_id, downloadID25);
//                                chatHelper.incomingCancelDownload(downloadUploadHelper, videoUrl25, str_row_id, downloadID25, rowId25);
                                break;

                            case 100:
                                intent = new Intent(context, VideoPreviewActivity.class);
                                intent.putExtra("incomingVideo", videoPath25);
                                context.startActivity(intent);
                                break;
                        }
                    }
                });
                break;

            case 30: //outgoing map
            case 31: //incoming map
                lat = data.getMessage().getMsgLatitude();
                lon = data.getMessage().getMsgLongitude();
                text = data.getMessage().getMsgData(); //placeName
                textStr = data.getMessage().getMsgInfoId(); //placeAddress

                path = data.getMessage().getMsgInfoUrl();
                imageLoadHelper.setImgPath(itemView, chat_log_map, path, R.drawable.ic_def_location_loading_400px);

//                GlideApp.with(itemView)
//                        .load(data.getMessage().getMsgInfoUrl())
//                        .placeholder(circularProgressDrawable)
//                        .error(R.drawable.ic_def_location_loading_400px)
//                        .dontAnimate()
//                        .into(chat_log_map)
//                        .waitForLayout();

                chat_log_map_title.setText(text);
                chat_log_map_address.setText(textStr);

                chat_log_bubble_cl.setOnClickListener(v -> {
                    if (lat != null && !lat.equals("")) {
                        intent = new Intent(context, MapGps.class);

                        //specify from
                        intent.putExtra("from", "chatHolder");

                        intent.putExtra("latitude", lat);
                        intent.putExtra("longitude", lon);
                        intent.putExtra("placeName", text);
                        intent.putExtra("placeAddress", textStr);

                        context.startActivity(intent);
                    }
                });
                break;

            case 32: //outgoing contact
            case 33: //incoming contact
                final String contactNumber = data.getMessage().getContactnumber();

                chat_log_contact_name.setText(data.getMessage().getContactname());

                //add to contacts when click on bubble
                chat_log_bubble_cl.setOnClickListener(v -> {
                    intent = new Intent(context, AddNewContactDet.class);

                    intent.putExtra("nametxt", chat_log_contact_name.getText().toString());
                    intent.putExtra("numbertxt", contactNumber);

                    if (data.getMessage().getIsSender() == 32) {
                        intent.putExtra("outgoing", "1");
                    }

                    context.startActivity(intent);
                });

                //call user when click on call btn
                chat_log_contact_call.setOnClickListener(v -> {
                    Runnable confirmCallAction = () -> {
                        Intent callIntent = new Intent(Intent.ACTION_CALL);
                        callIntent.setData(Uri.parse("tel:" + contactNumber));

                        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.CALL_PHONE)
                                != PackageManager.PERMISSION_GRANTED) { //no permission

                            Runnable callPermissionAction = () -> {
                                Intent intent = new Intent();
                                intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                                Uri uri = Uri.fromParts("package", context.getPackageName(), null);
                                intent.setData(uri);
                                context.startActivity(intent);
                            };
                            uiHelper.dialog2Btns(context, context.getString(R.string.need_phone_title),
                                    context.getString(R.string.need_phone), R.string.open_settings,
                                    R.string.cancel, R.color.white, R.color.primaryDark3,
                                    callPermissionAction, null, false);

                        } else { //got permission
                            context.startActivity(callIntent);
                        }
                    };

                    uiHelper.dialog2Btns(context, context.getString(R.string.contact_call),
                            context.getString(R.string.contact_call_msg),
                            R.string.ok_label, R.string.cancel, R.color.white, R.color.black,
                            confirmCallAction, null, true);
                });
                break;

            case 34: //outgoing restaurant
            case 35: //incoming restaurant
                media = data.getMessage().getMsgInfoUrl();
                text = data.getMessage().getMsgData();
                infoID = data.getMessage().getMsgInfoId();

                imageLoadHelper.setImgPath(itemView, chat_log_res_img, media, R.drawable.ic_res_default_no_image_black_640px);

//                GlideApp.with(itemView)
//                        .load(media)
//                        .placeholder(R.drawable.ic_res_default_no_image_black_640px)
//                        .dontTransform()
//                        .into(chat_log_res_img);

                chat_log_bubble_cl.setOnClickListener(v -> {
                    intent = new Intent(context, FoodDetailLog.class);

                    intent.putExtra("resID", infoID);
                    intent.putExtra("resName", text);
                    intent.putExtra("resPropic", media);

                    context.startActivity(intent);
                });

                chat_log_res_title.setText(text);
                break;

            //out reply text
            case 60:
                replied = databaseHelper.getMessageFromUniqueID(data.getMessage().getMsgInfoId());

                if (replied != null) {
                    chat_log_reply_display_name = itemView.findViewById(R.id.chat_log_reply_display_name);
                    chat_log_reply_msg = itemView.findViewById(R.id.chat_log_reply_msg);

                    if (replied.getIsSender() == 10 || replied.getIsSender() == 60 || replied.getIsSender() == 62) { //out
                        replydisplayName = itemView.getResources().getString(R.string.you);

                    } else if (replied.getIsSender() == 11 || replied.getIsSender() == 61 || replied.getIsSender() == 63) { //in
                        replydisplayName = IndiChatLog.displayName;

                    }
                    chat_log_reply_display_name.setText(replydisplayName);
                    chat_log_reply_msg.setText(replied.getMsgData());
                }
                break;

            //in reply text
            case 61:
                replied = databaseHelper.getMessageFromUniqueID(data.getMessage().getMsgInfoId());

                chat_log_reply_display_name = itemView.findViewById(R.id.chat_log_reply_display_name);
                chat_log_reply_msg = itemView.findViewById(R.id.chat_log_reply_msg);

                if (replied != null) {
                    if (replied.getIsSender() == 10 || replied.getIsSender() == 60 || replied.getIsSender() == 62) { //out
                        replydisplayName = itemView.getResources().getString(R.string.you);

                    } else if (replied.getIsSender() == 11 || replied.getIsSender() == 61 || replied.getIsSender() == 63) { //in
                        replydisplayName = IndiChatLog.displayName;

                    }
                    chat_log_reply_display_name.setText(replydisplayName);
                    chat_log_reply_msg.setText(replied.getMsgData());

                } else {
                    chat_log_reply_display_name.setVisibility(View.INVISIBLE);
                    chat_log_reply_msg.setText("Message No Longer Exists");
                }
                break;

            //out reply media
            case 62:
                replied = databaseHelper.getMessageFromUniqueID(data.getMessage().getMsgInfoId());

                if (replied != null) {
                    chat_log_reply_display_name = itemView.findViewById(R.id.chat_log_reply_display_name);
                    chat_log_reply_msg = itemView.findViewById(R.id.chat_log_reply_msg);
                    reply_media = itemView.findViewById(R.id.reply_media);

                    final byte[] replythumb = Base64.decode(replied.getMsgLatitude(), Base64.DEFAULT);
                    switch (replied.getIsSender()) {
                        case 20: //out img
                            replydisplayName = itemView.getResources().getString(R.string.you);

                            chat_log_reply_msg.setText(R.string.reply_out_img);
                            break;

                        case 21: //in img
                            replydisplayName = IndiChatLog.displayName;

                            chat_log_reply_msg.setText(R.string.reply_in_img);
                            break;

                        case 24: //out video
                            replydisplayName = itemView.getResources().getString(R.string.you);

                            chat_log_reply_msg.setText(R.string.reply_out_vid);
                            break;

                        case 25: //in video
                            replydisplayName = IndiChatLog.displayName;

                            chat_log_reply_msg.setText(R.string.reply_in_vid);
                            break;

                        default:
                            break;
                    }
                    chat_log_reply_display_name.setText(replydisplayName);

                    if (replied.getMsgLatitude() != null) {
                        imageLoadHelper.setImgByte(reply_media, replythumb, R.drawable.default_img_400);

//                        GlideApp.with(itemView)
//                                .load(replythumb)
//                                .placeholder(circularProgressDrawable)
//                                .error(R.drawable.default_img_400)
//                                .transforms(new BlurTransformation(5), new CenterCrop())
////                                .into(reply_media);
//                                .into(new SimpleTarget<Drawable>() {
//                                    @Override
//                                    public void onResourceReady(@NonNull Drawable resource, @Nullable Transition<? super Drawable> transition) {
//                                        reply_media.setImageDrawable(resource);
//                                    }
//                                });
                    }

                }
                break;

            //in reply media
            case 63:
                replied = databaseHelper.getMessageFromUniqueID(data.getMessage().getMsgInfoId());

                chat_log_reply_display_name = itemView.findViewById(R.id.chat_log_reply_display_name);
                chat_log_reply_msg = itemView.findViewById(R.id.chat_log_reply_msg);
                reply_media = itemView.findViewById(R.id.reply_media);

                if (replied != null) {
                    final byte[] replythumb = Base64.decode(replied.getMsgLatitude(), Base64.DEFAULT);
                    switch (replied.getIsSender()) {
                        case 20: //out img
                            replydisplayName = itemView.getResources().getString(R.string.you);

                            chat_log_reply_msg.setText(R.string.reply_out_img);
                            break;

                        case 21: //in img
                            replydisplayName = IndiChatLog.displayName;

                            chat_log_reply_msg.setText(R.string.reply_in_img);
                            break;

                        case 24: //out vid
                            replydisplayName = itemView.getResources().getString(R.string.you);

                            chat_log_reply_msg.setText(R.string.reply_out_vid);
                            break;

                        case 25: //in vid
                            replydisplayName = IndiChatLog.displayName;

                            chat_log_reply_msg.setText(R.string.reply_in_vid);
                            break;

                        default:
                            break;
                    }
                    chat_log_reply_display_name.setText(replydisplayName);

                    if (replied.getMsgLatitude() != null) {
                        imageLoadHelper.setImgByte(reply_media, replythumb, R.drawable.default_img_400);

//                        GlideApp.with(itemView)
//                                .load(replythumb)
//                                .placeholder(circularProgressDrawable)
//                                .error(R.drawable.default_img_400)
//                                .transforms(new BlurTransformation(5), new CenterCrop())
//                                .into(new SimpleTarget<Drawable>() {
//                                    @Override
//                                    public void onResourceReady(@NonNull Drawable resource, @Nullable Transition<? super Drawable> transition) {
//                                        reply_media.setImageDrawable(resource);
//                                    }
//                                });
                    }
                } else {
                    chat_log_reply_display_name.setVisibility(View.INVISIBLE);
                    chat_log_reply_msg.setText("Media No Longer Exists");

                    CardView reply_media_cv = itemView.findViewById(R.id.reply_media_cv);
                    reply_media_cv.setVisibility(View.GONE);
                }
                break;

            //appt created
            case 70: //out
            case 71: //in
                //apptID
                infoID = data.getMessage().getMsgInfoId();

                //v2.0.12 (if no apptID don't proceed)
                if (infoID != null && !infoID.equals("")) {
                    //hide status text and show img
                    chat_log_bubble_cl.setVisibility(View.VISIBLE);
                    chat_log_appt_status_msg.setVisibility(View.GONE);

                    //get app details
                    text = getApptTitle(data);
                    long apptTimeLong = getApptTime(data);
                    String apptLoc = getApptLoc(data);

                    //set appt date
                    TextView chat_log_appt_date = itemView.findViewById(R.id.chat_log_appt_date);
                    TextView chat_log_appt_month = itemView.findViewById(R.id.chat_log_appt_month);

                    DateFormat apptDayFormat = new SimpleDateFormat("dd", Locale.ENGLISH);
                    DateFormat apptMonthFormat = new SimpleDateFormat("MMM", Locale.ENGLISH);

                    String apptDay = apptDayFormat.format(apptTimeLong);
                    String apptMonth = apptMonthFormat.format(apptTimeLong);

                    chat_log_appt_date.setText(apptDay);
                    chat_log_appt_month.setText(apptMonth);

                    //set appt title
                    chat_log_appt_title.setText(text);

                    //set appt time
                    TextView chat_log_appt_time = itemView.findViewById(R.id.chat_log_appt_time);
                    DateFormat apptTimeFormat = new SimpleDateFormat("h:mm a", Locale.ENGLISH);
                    String apptTime = apptTimeFormat.format(apptTimeLong);

                    chat_log_appt_time.setText(apptTime);

                    //set appt location
                    TextView chat_log_appt_loc = itemView.findViewById(R.id.chat_log_appt_loc);
                    chat_log_appt_loc.setText(apptLoc);

                    //show/hide btns based on self status
                    ImageView chat_going_btn = itemView.findViewById(R.id.chat_going_btn);
                    ImageView chat_not_going_btn = itemView.findViewById(R.id.chat_not_going_btn);
                    View chat_log_black_fore = itemView.findViewById(R.id.chat_log_black_fore);

                    if (data.getAppointment() != null) { //got appt in APPT table
                        //set onclick to go to appt room
                        chat_log_bubble_cl.setOnClickListener(v -> goToAppt(IndiChatLog.jid, infoID));

                        //set appt btns
                        int apptSelfStatus = data.getAppointment().getSelf_Status();

                        if (apptSelfStatus == 2) { //undecided, show btns and set onclick
                            chat_going_btn.setVisibility(View.VISIBLE);
                            chat_going_btn.setOnClickListener(v -> { //going = 1
                                apptStatusConfirmation(infoID, 1);
                            });

                            chat_not_going_btn.setVisibility(View.VISIBLE);
                            chat_not_going_btn.setOnClickListener(v -> { //not going = 3
                                apptStatusConfirmation(infoID, 3);
                            });
                        } else { //decided, hide btns
                            chat_going_btn.setVisibility(View.GONE);
                            chat_not_going_btn.setVisibility(View.GONE);
                        }

                        //hide black shade
                        chat_log_black_fore.setVisibility(View.GONE);

                    } else { //no appt, don't set onclick and show greyed
                        //remove onclick
                        chat_log_bubble_cl.setOnClickListener(null);

                        //remove going/not going btns
                        chat_going_btn.setVisibility(View.GONE);
                        chat_not_going_btn.setVisibility(View.GONE);

                        //show black shade
                        chat_log_black_fore.setVisibility(View.VISIBLE);
                    }

                } else { //no apptID - show text only
                    //hide img and show text
                    chat_log_bubble_cl.setVisibility(View.GONE);
                    chat_log_appt_status_msg.setVisibility(View.VISIBLE);

                    textStr = context.getString(R.string.appt_created);

                    chat_log_appt_status_msg.setText(textStr);
                }
                break;

            //appt title updated
            case 72: //outgoing
            case 73: //incoming
                //apptID
                infoID = data.getMessage().getMsgInfoId();

                //old appt title
                media = data.getMessage().getMsgInfoUrl();

                //current title
                text = getApptTitle(data);

                if (media != null && !media.equals("")) {
                    //style span for old title
                    SpannableString oldTitle = new SpannableString(media);
                    oldTitle.setSpan(new StyleSpan(Typeface.BOLD), 0, oldTitle.length(), 0);

                    //style span for new title
                    SpannableString newTitle = new SpannableString(text);
                    newTitle.setSpan(new StyleSpan(Typeface.BOLD), 0, newTitle.length(), 0);
//                    newTitle.setSpan(new ForegroundColorSpan(Color.BLACK), 0, newTitle.length(), 0);

                    SpannableStringBuilder styledStr = new SpannableStringBuilder();

                    if (data.getMessage().getSenderJid() != null) {
                        if (data.getMessage().getSenderJid().equals(preferences.getValue(context, GlobalVariables.STRPREF_USER_ID))) { //out - you, background green
                            styledStr.append(context.getString(R.string.appt_title_updated_self_from));
//                        chat_log_appt_title.setBackgroundTintList(context.getResources().getColorStateList(R.color.primaryLight1));

                        } else { //in - member displayname, backgroun grey
                            styledStr.append(IndiChatLog.displayName);
                            styledStr.append(" ");
                            styledStr.append(context.getString(R.string.appt_title_updated_in_from));
//                        chat_log_appt_title.setBackgroundTintList(context.getResources().getColorStateList(R.color.grey2));

                        }
                        styledStr.append(" ");
                        styledStr.append(oldTitle);
                        styledStr.append(" ");
                        styledStr.append(context.getString(R.string.to));
                        styledStr.append(" ");
                        styledStr.append(newTitle);

                        chat_log_appt_title.setText(styledStr);
                    }
                } else {
                    media = text + ": " + context.getString(R.string.appt_title_changed);
                    chat_log_appt_title.setText(media);
                }

                //set onclick to go to appt room
                chat_log_appt_title.setOnClickListener(v -> goToAppt(IndiChatLog.jid, infoID));
                break;

            //appt date/time updated
            case 74: //out
            case 75: //in
                //apptID
                infoID = data.getMessage().getMsgInfoId();

                //current title
                text = getApptTitle(data);
                chat_log_appt_title.setText(text);

                //old appt date/time long value in str format
                long oldApptDateTime = Long.valueOf(data.getMessage().getMsgInfoUrl());

                //current appt date/time long value
                long newApptDateTime = getApptTime(data);

                //appt date format
                ConstraintLayout chat_log_appt_date_cl = itemView.findViewById(R.id.chat_log_appt_date_cl);
                TextView chat_log_appt_date = itemView.findViewById(R.id.chat_log_appt_date);

                DateFormat apptDateFormat = new SimpleDateFormat("dd MMM yyyy", Locale.ENGLISH);

                //show current date if same date
                if (apptDateFormat.format(oldApptDateTime).equals(apptDateFormat.format(newApptDateTime))) { //same, hide
                    chat_log_appt_date_cl.setVisibility(View.INVISIBLE);
                    chat_log_appt_date.setVisibility(View.VISIBLE);

                    chat_log_appt_date.setText(apptDateFormat.format(newApptDateTime));

                } else { //diff, show
                    TextView chat_log_app_old_date_month = itemView.findViewById(R.id.chat_log_appt_old_date_month);
                    TextView chat_log_app_old_date = itemView.findViewById(R.id.chat_log_app_old_date);
                    TextView chat_log_app_new_date_month = itemView.findViewById(R.id.chat_log_app_new_date_month);
                    TextView chat_log_app_new_date = itemView.findViewById(R.id.chat_log_app_new_date);

                    DateFormat apptMonthFormat = new SimpleDateFormat("MMM yyyy", Locale.ENGLISH);
                    DateFormat apptDayFormat = new SimpleDateFormat("dd", Locale.ENGLISH);

                    media = apptMonthFormat.format(oldApptDateTime); //old month year
                    path = apptDayFormat.format(oldApptDateTime); //old day
                    lat = apptMonthFormat.format(newApptDateTime); //new month year
                    lon = apptDayFormat.format(newApptDateTime); //new day

                    chat_log_app_old_date_month.setText(media);
                    chat_log_app_old_date.setText(path);
                    chat_log_app_new_date_month.setText(lat);
                    chat_log_app_new_date.setText(lon);

                    chat_log_appt_date_cl.setVisibility(View.VISIBLE);
                    chat_log_appt_date.setVisibility(View.INVISIBLE);
                }

                //appt time format
                ConstraintLayout chat_log_appt_time_cl = itemView.findViewById(R.id.chat_log_appt_time_cl);
                TextView chat_log_appt_time = itemView.findViewById(R.id.chat_log_appt_time);

                DateFormat apptTimeFullFormat = new SimpleDateFormat("h:mm a", Locale.ENGLISH);

                //don't show appt time update if same time
                if (apptTimeFullFormat.format(oldApptDateTime).equals(apptTimeFullFormat.format(newApptDateTime))) { //same, hide
                    chat_log_appt_time_cl.setVisibility(View.INVISIBLE);
                    chat_log_appt_time.setVisibility(View.VISIBLE);

                    chat_log_appt_time.setText(apptTimeFullFormat.format(newApptDateTime));

                } else { //diff, show
                    TextView chat_log_app_old_time_format = itemView.findViewById(R.id.chat_log_app_old_time_format);
                    TextView chat_log_app_old_time = itemView.findViewById(R.id.chat_log_app_old_time);
                    TextView chat_log_app_new_time_format = itemView.findViewById(R.id.chat_log_app_new_time_format);
                    TextView chat_log_app_new_time = itemView.findViewById(R.id.chat_log_app_new_time);

                    DateFormat apptTimeAMPMFormat = new SimpleDateFormat("a", Locale.ENGLISH);
                    DateFormat apptTimeFormat = new SimpleDateFormat("h:mm", Locale.ENGLISH);

                    media = apptTimeAMPMFormat.format(oldApptDateTime); //old AMPM
                    path = apptTimeFormat.format(oldApptDateTime); //old time
                    lat = apptTimeAMPMFormat.format(newApptDateTime); //new AMPM
                    lon = apptTimeFormat.format(newApptDateTime); //new time

                    chat_log_app_old_time_format.setText(media);
                    chat_log_app_old_time.setText(path);
                    chat_log_app_new_time_format.setText(lat);
                    chat_log_app_new_time.setText(lon);

                    chat_log_appt_time_cl.setVisibility(View.VISIBLE);
                    chat_log_appt_time.setVisibility(View.INVISIBLE);
                }

                //set onclick to go to appt room
                chat_log_bubble_cl.setOnClickListener(v -> goToAppt(IndiChatLog.jid, infoID));
                break;

            //appt location updated
            case 76: //out
            case 77: //in
                //apptID
                infoID = data.getMessage().getMsgInfoId();

                //current appt details
                text = getApptTitle(data); //appt title
                path = getApptLoc(data); //appt loc

                //old location
                media = data.getMessage().getMsgInfoUrl();
                if (media == null || media.equals("")) {
                    media = context.getString(R.string.appt_no_loc_title);
                }

                //set details to elements
                TextView chat_log_appt_old_loc = itemView.findViewById(R.id.chat_log_appt_old_loc);
                TextView chat_log_appt_new_loc = itemView.findViewById(R.id.chat_log_appt_new_loc);

                chat_log_appt_title.setText(text);
                chat_log_appt_old_loc.setText(media);
                chat_log_appt_new_loc.setText(path);

                //set onclick to go to appt room
                chat_log_bubble_cl.setOnClickListener(v -> goToAppt(IndiChatLog.jid, infoID));
                break;

            //outgoing appt status
            case 78:
                //apptID (testing non-final see if can)
                infoID = data.getMessage().getMsgInfoId();

                //appt title
                text = getApptTitle(data);

                //self appt status
                ForegroundColorSpan selfStatusColour;
                switch (getApptSelfStatus(data)) {
                    case 0: //host
                    case 1: //going
                        media = context.getString(R.string.appt_going);
                        path = context.getString(R.string.to);
                        selfStatusColour = new ForegroundColorSpan(context.getResources().getColor(R.color.primaryDark2));
                        break;

                    case 2: //undecided
                        media = context.getString(R.string.appt_undecided);
                        path = context.getString(R.string.appt_for);
                        selfStatusColour = new ForegroundColorSpan(context.getResources().getColor(R.color.grey10));
                        break;

                    case 3: //not going
                        media = context.getString(R.string.appt_not_going);
                        path = context.getString(R.string.to);
                        selfStatusColour = new ForegroundColorSpan(context.getResources().getColor(R.color.red));
                        break;

                    default:
                        media = context.getString(R.string.appt_undecided);
                        path = context.getString(R.string.appt_for);
                        selfStatusColour = new ForegroundColorSpan(context.getResources().getColor(R.color.grey10));
                        break;
                }

                //style span for self status
                SpannableString selfStatusStr = new SpannableString(media);
                selfStatusStr.setSpan(selfStatusColour, 0, selfStatusStr.length(), 0);

                //style span for appt title
                SpannableString apptTitle = new SpannableString(text);
                apptTitle.setSpan(new StyleSpan(Typeface.BOLD), 0, apptTitle.length(), 0);

                SpannableStringBuilder selfStatusStyledStr = new SpannableStringBuilder();

                selfStatusStyledStr.append(context.getString(R.string.appt_you_are));
                selfStatusStyledStr.append(" ");
                selfStatusStyledStr.append(selfStatusStr);
                selfStatusStyledStr.append(" ");
                selfStatusStyledStr.append(path);
                selfStatusStyledStr.append(" ");
                selfStatusStyledStr.append(apptTitle);

                chat_log_appt_title.setText(selfStatusStyledStr);
                chat_log_appt_title.setBackgroundTintList(context.getResources().getColorStateList(R.color.grey2));

                //set onclick to go to appt room
                chat_log_appt_title.setOnClickListener(v -> goToAppt(IndiChatLog.jid, infoID));
                break;

            case 79: //in
                //apptID
                infoID = data.getMessage().getMsgInfoId();

                //set sender img
//                setProfileImg(false);
                imageLoadHelper.setImgByte(chat_log_profile_img, friendImgByte, R.drawable.in_propic_circle_150px);

                //displayname
                TextView chat_log_display_name = itemView.findViewById(R.id.chat_log_display_name);
                chat_log_display_name.setText(IndiChatLog.displayName);

                //current title
                text = getApptTitle(data);
                chat_log_appt_title.setText(text);

                //get current self status
                TextView chat_log_appt_user_status = itemView.findViewById(R.id.chat_log_appt_user_status);
                int gmsStatus = 2;
                if (data.getAppointment() != null) {
                    gmsStatus = data.getAppointment().getFriend_Status();
                }
                switch (gmsStatus) {
                    case 0: //host
                    case 1: //going
                        chat_log_appt_user_status.setText(R.string.appt_going);
                        chat_log_appt_user_status.setBackgroundTintList(context.getResources().getColorStateList(R.color.primaryDark2));
                        break;

                    case 2: //undecided
                        chat_log_appt_user_status.setText(R.string.appt_undecided);
                        chat_log_appt_user_status.setBackgroundTintList(context.getResources().getColorStateList(R.color.grey10));
                        break;

                    case 3: //not going
                        chat_log_appt_user_status.setText(R.string.appt_not_going);
                        chat_log_appt_user_status.setBackgroundTintList(context.getResources().getColorStateList(R.color.red));
                        break;

                    default:
                        chat_log_appt_user_status.setText(R.string.appt_undecided);
                        chat_log_appt_user_status.setBackgroundTintList(context.getResources().getColorStateList(R.color.grey10));
                        break;
                }

                //set onclick to go to appt room
                chat_log_bubble_cl.setOnClickListener(v -> goToAppt(IndiChatLog.jid, infoID));
                break;

            // appt deleted
            case 80: //out
            case 81: //in
                //apptID (testing non-final see if can)
                infoID = data.getMessage().getMsgInfoId();

                //appt title
                text = getApptTitle(data);

                //style span for appt title
                SpannableString apptTitleDel = new SpannableString(text);
                apptTitleDel.setSpan(new StyleSpan(Typeface.BOLD), 0, apptTitleDel.length(), 0);

                SpannableStringBuilder delStyledStr = new SpannableStringBuilder();

                //xx has/have deleted
                String apptDelFront;
                if (data.getMessage().getIsSender() == 80) { //out
                    apptDelFront = context.getString(R.string.appt_self_delete) + " ";

                } else { //in
                    apptDelFront = IndiChatLog.displayName + " " + context.getString(R.string.appt_other_delete) + " ";

                }
                delStyledStr.append(apptDelFront);
                delStyledStr.append(apptTitleDel);

                chat_log_appt_title.setText(delStyledStr);
                chat_log_appt_title.setTextColor(context.getResources().getColor(R.color.white));
                chat_log_appt_title.setBackgroundTintList(context.getResources().getColorStateList(R.color.red));

                //set onclick to go to appt room
//                chat_log_appt_title.setOnClickListener(v -> goToAppt(GroupChatLog.jid, infoID));


//                //apptID
//                infoID = data.getMsgInfoId();
//
//                //appt title
//                text = databaseHelper.getApptHistTitle(infoID);
//
//                if (text != null && !text.equals("")) {
//                    textStr = text + context.getString(R.string.appt_name_deleted);
//                } else {
//                    textStr = context.getString(R.string.appt_deleted);
//                }
//
//                chat_log_appt_status_msg.setText(textStr);
                break;

            //incoming appt details updated (to cater for old)
            case 82:
                //apptID
                infoID = data.getMessage().getMsgInfoId();

                //current title
                text = databaseHelper.getApptTitle(infoID);

                //appt update msg
                if (text == null || text.equals("")) { //unwrap
                    textStr = context.getString(R.string.appt_updated);
                } else {
                    textStr = text + context.getString(R.string.appt_det_updated);
                }

                chat_log_appt_status_msg.setText(textStr);
                break;

            default:
                break;
        }
    }

    //set profile img based on jid
//    private void setProfileImg(boolean isSelf) {
//
//        GlideApp.with(itemView)
//                .asBitmap()
//                .load(isSelf ? selfImgByte : friendImgByte)
//                .placeholder(R.drawable.in_propic_circle_150px)
//                .apply(RequestOptions.circleCropTransform())
//                .thumbnail(0.3f)
//                .encodeQuality(33)
//                .override(60, 60)
//                .into(new SimpleTarget<Bitmap>() {
//                    @Override
//                    public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
//                        chat_log_profile_img.setImageBitmap(resource);
//                    }
//                });
//    }

    //function for onclick go to appt room
    private void goToAppt(String jid, String apptID) {
        if (databaseHelper.getApptExist(apptID) > 0) { //go to schelog ONLY if apptID exists
            intent = new Intent(context, IndiScheLog.class);

            intent.putExtra("link", "1");
            intent.putExtra("jid", jid);
            intent.putExtra("apptID", apptID);

            context.startActivity(intent);
        } else { //appt not exist
            Toast.makeText(context, "This appointment no longer exists", Toast.LENGTH_SHORT).show();
        }
    }

    //get current appt title (safe)
    private String getApptTitle(IndiChatLogDB data) {
        if (data.getAppointment() != null) {
            return data.getAppointment().getApptTitle();
        } else if (data.getAppointmentHist() != null) {
            return data.getAppointmentHist().getApptH_Title();
        } else {
            return context.getString(R.string.appt_no_appt_title);
        }
    }

    //get current appt time long (safe)
    private long getApptTime(IndiChatLogDB data) {
        if (data.getAppointment() != null && data.getAppointment().getApptTime() != 0) {
            return data.getAppointment().getApptTime();

        } else if (data.getAppointmentHist() != null && data.getAppointmentHist().getApptH_Time() != 0) {
            return data.getAppointmentHist().getApptH_Time();

        } else {
            return 0;
        }
    }

    //get current appt location (safe)
    private String getApptLoc(IndiChatLogDB data) {
        if (data.getAppointment() != null) {
            return data.getAppointment().getApptLocation();
        } else if (data.getAppointmentHist() != null) {
            return data.getAppointmentHist().getApptH_Location();
        } else {
            return context.getString(R.string.appt_no_loc_title);
        }
    }

    //get current appt self status (safe)
    private int getApptSelfStatus(IndiChatLogDB data) {
        if (data.getAppointment() != null) {
            return data.getAppointment().getSelf_Status();
        } else if (data.getAppointmentHist() != null) {
            return data.getAppointmentHist().getApptH_Self_Status();
        } else {
            return 2;
        }
    }

    //function for showing appt status confirmation alertdialog
    private void apptStatusConfirmation(String apptID, int selfStatus) {
        if (SmackHelper.getXMPPConnection() != null && SmackHelper.getXMPPConnection().isAuthenticated()) {
            Runnable updateStatusAction = () -> {
                String selfUserName = preferences.getValue(context, GlobalVariables.STRPREF_USERNAME);

                String pushMsg;
                if (selfStatus == 1) { //going
                    pushMsg = selfUserName + " " + context.getString(R.string.appt_is_going);
                } else { //not going
                    pushMsg = selfUserName + " " + context.getString(R.string.appt_is_not_going);
                }
                String uniqueID = UUID.randomUUID().toString();

                singleChatStanza.SoappAppointmentStanza(IndiChatLog.jid, pushMsg, "",
                        "", "", "", "", "", "", "",
                        String.valueOf(selfStatus), "", uniqueID, "", "", apptID,
                        selfUserName, "appointment", "0", "4");

                databaseHelper.outgoingApptStatus(IndiChatLog.jid, apptID, selfStatus, uniqueID);

                //set reminder alarm ONLY if going - need to be done at end since getting info from sqlite
                if (selfStatus != 3) {
                    databaseHelper.scheduleLocalNotification(apptID);
                }
            };

            uiHelper.dialog2Btns(context, context.getString(R.string.schelog_appt_status_title),
                    context.getString(R.string.schelog_appt_status_msg), R.string.ok_label, R.string
                            .cancel, R.color.white, R.color.black,
                    updateStatusAction, null, true);
        } else {
            Toast.makeText(context, R.string.xmpp_waiting_connection, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onError() {

    }

    @Override
    public void onFinish(String uniqueId) {
    }

    @Override
    public void onProgressUpdate(int percentage, String uniqueId) {

    }

    // still wan to check what better
    public void setAudioTimer(MediaPlayer audioDuration) {
        mHandler = new Handler();
        boolean wasRun = true;

        int coutBartimer = audioDuration.getDuration();
        chat_log_audio_timer_bar.setMax(coutBartimer);
        bg_progress.setMax(coutBartimer);

        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (wasRun) {
                    AcMove += 100;
                    if (AcMove >= 1) {
                        isPlaying = true;
                    }

                    if (isPlaying) {
                        SectimerInMilis = AcMove + 1000;

                        countAcMove = AcMove;
                        Calendar timer3 = Calendar.getInstance();

                        timer3.setTimeInMillis(SectimerInMilis);
                        int TimerSec3 = timer3.get(Calendar.SECOND);
                        long TMin3 = TimeUnit.MILLISECONDS.toMinutes(TimerSec3);

                        String TTimer = String.format(Locale.ENGLISH, "%02d:%02d", TMin3, TimerSec3);
                        chat_log_media_timer.setText(TTimer);
                        chat_log_audio_timer_bar.setProgress(countAcMove);
                        bg_progress.setProgress(countAcMove);
                    }

                    mHandler.postDelayed(this, 100);

                    if (countAcMove > coutBartimer) {
                        mHandler.removeCallbacksAndMessages(null);
                        AcMove = 0;
                        chat_log_audio_timer_bar.setProgress(0);
                        bg_progress.setProgress(0);
                        chat_log_media_timer.setText(TTimerfinish);
                    }
                }
            }
        }, 10);
    }
}