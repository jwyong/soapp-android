package com.soapp.global.sharing;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.soapp.R;
import com.soapp.SoappApi.ApiModel.RestaurantLocModel;
import com.soapp.SoappApi.Interface.RestaurantLocation;
import com.soapp.SoappApi.RetrofitAPIClient;
import com.soapp.camera.ImageNormalPreviewActivity;
import com.soapp.chat_class.group_chat.GroupChatLog;
import com.soapp.chat_class.single_chat.IndiChatLog;
import com.soapp.global.GlobalVariables;
import com.soapp.global.MiscHelper;
import com.soapp.global.Preferences;
import com.soapp.global.ImageLoadHelper;
import com.soapp.global.UIHelper;
import com.soapp.schedule_class.new_appt.NewIndiExistApptActivity;
import com.soapp.sql.DatabaseHelper;
import com.soapp.sql.room.joiners.SharingList;
import com.soapp.xmpp.GlobalMessageHelper.GlobalHeaderHelper;
import com.soapp.xmpp.GroupChatHelper.GroupChatStanza;
import com.soapp.xmpp.SingleChatHelper.SingleChatStanza;
import com.soapp.xmpp.SmackHelper;

import java.util.UUID;

import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

/* Created by Soapp on 28/11/2017. */

public class SharingExistHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
    //for accessing variables from main activity
    private SharingController sharingController;

    public static String orient;
    private TextView list_name;
    private ImageView list_profileImage;
    private Preferences preferences = Preferences.getInstance();
    private SingleChatStanza singleChatStanza = new SingleChatStanza();
    private GroupChatStanza groupChatStanza = new GroupChatStanza();
    private DatabaseHelper databaseHelper = DatabaseHelper.getInstance();
    private ProgressDialog progressDialog;
    private String jid;

    private Context context = itemView.getContext();
    private Activity activity = (Activity) context;
    private ImageLoadHelper imageLoadHelper = new ImageLoadHelper();

    //action for updating booking to appt
    private Runnable actionBookingUpdateApptPositive = new Runnable() {
        public void run() {
            String mapURL = new MiscHelper().getGmapsStaticURL(sharingController.resLat, sharingController.resLon);

            //update appt location to sqlite [KIV]
//            databaseHelper.outgoingApptLocation(jid, mapURL, resLat, resLon, restitle, infoid, image_id);

            //update appt title to sqlite [KIV'ed - need apptID, do 1 shot with booking impl.)
//            String apptTitle = context.getString(R.string.booking_confirmed) + ": " + restitle;
//            databaseHelper.outgoingApptTitle(jid, a apptTitle);

            //update appt date/time to sqlite
//            apptID
//            databaseHelper.outgoingApptDateTime(jid, bookingDate, -2, bookingTime, -2);

            String pushTitle = context.getString(R.string
                    .appt_updated) + ": " + sharingController.resTitle;
            Intent intent;

            if (jid.length() == 12) { //indi
                //update appt status to "Hosting"
//                databaseHelper.outgoingApptStatus(jid, 4);

                //send stanza to others
//                singleChatStanza.SoappAppointmentStanza(jid,
//                        apptTitle, mapURL, restitle, resLat, resLon, bookingDate, bookingTime,
//                        context.getString(R.string.appt_going), "", UUID.randomUUID().toString(), infoid,
//                        image_id, apptID);

                String user_displayname = preferences.getValue(context, GlobalVariables.STRPREF_USERNAME);

                intent = new Intent(context, IndiChatLog.class);

            } else { //grp
                //update appt status to "Hosting"
//                databaseHelper.outgoingApptStatus(jid, 4);

                String self_username = preferences.getValue(context,
                        GlobalVariables.STRPREF_USERNAME);
                String self_jid = preferences.getValue(context,
                        GlobalVariables.STRPREF_USER_ID);
                String roomName = databaseHelper.getNameFromContactRoster(jid);

//                groupChatStanza.GroupAppointment(jid, self_jid, UUID.randomUUID().toString(),
//                        apptTitle, mapURL, restitle, resLat, resLon, bookingDate, bookingTime,
//                        context.getString(R.string.appt_host), infoid, image_id, apptID);

                intent = new Intent(context, GroupChatLog.class);
            }
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            intent.putExtra("jid", jid);

            context.startActivity(intent);

            activity.finish();
        }
    };

    //action for cancel updating booking to appt
    private Runnable actionBookingUpdateApptNegative = new Runnable() {
        public void run() {
            Intent intent;
            if (jid.length() == 12) { //indi
                intent = new Intent(context, IndiChatLog.class);
            } else { //grp
                intent = new Intent(context, GroupChatLog.class);
            }
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            intent.putExtra("jid", jid);

            context.startActivity(intent);

            activity.finish();
        }
    };

    //function for doing action based on "from"
    private Runnable actionOk = new Runnable() {
        public void run() {
            String uniqueID = UUID.randomUUID().toString();
            long currentDate = System.currentTimeMillis();

            new GlobalHeaderHelper().GlobalHeaderTime(jid);

            if (jid.length() == 12) { //indi
                switch (sharingController.from) {
                    case "foodChat": //share restaurant to chat
                        if (sharingController.resID != null && sharingController.resTitle != null) {
                            String self_username = preferences.getValue(context, GlobalVariables.STRPREF_USERNAME);

                            if (SmackHelper.getXMPPConnection() != null && SmackHelper.getXMPPConnection().isAuthenticated()) {
                                singleChatStanza.SoappRestaurantStanza(sharingController.resID, jid,
                                        sharingController.resTitle, self_username, uniqueID);
                                databaseHelper.RestaurantOutgoingDatabase(uniqueID, currentDate,
                                        sharingController.resImgURL, sharingController.resID, jid, sharingController.resTitle);

                            } else {
                                databaseHelper.saveMessageAndSendWhenOnline("restaurant", jid,
                                        null, sharingController.resTitle, uniqueID, currentDate,
                                        null, null, null, null,
                                        sharingController.resImgURL, sharingController.resID);
                            }

                            Intent intent = new Intent(context, IndiChatLog.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            intent.putExtra("jid", jid);

                            context.startActivity(intent);
                            activity.finish();
                        }
                        break;

                    case "foodAppt": //set restaurant to appt location - go to create new appt activity
                        Intent indiScheIntent = new Intent(context, NewIndiExistApptActivity.class);

                        indiScheIntent.putExtra("jid", jid);

                        //for restaurant details (as location details now)
                        indiScheIntent.putExtra("apptLoc", sharingController.resTitle);

                        //no address yet, need to fix API first
//                        grpScheIntent.putExtra("apptAddress", resAddress);

                        indiScheIntent.putExtra("apptLon", sharingController.resLon);
                        indiScheIntent.putExtra("apptLat", sharingController.resLat);

                        //using imgURL for now - if need mapURL see in future
//                        String mapURL = "https://maps.googleapis.com/maps/api/staticmap?center=" +
//                                resLat + "," + resLon + "&zoom=16&size=400x200&maptype=roadmap&format=png&visual_refresh" +
//                                "=true&markers=size:mid%7Ccolor:0xff0000%7Clabel:%7C" + resLat + "," + resLon;
                        indiScheIntent.putExtra("apptMapURL", sharingController.resImgURL);

                        context.startActivity(indiScheIntent);

                        activity.finish();
                        break;

                    case "bookingChat": //share restaurant BOOKING to chat
                        if (sharingController.bookingQR != null) {
                            if (SmackHelper.getXMPPConnection() != null && SmackHelper.getXMPPConnection().isAuthenticated()) {
                                String self_username = preferences.getValue(context,
                                        GlobalVariables.STRPREF_USERNAME);

                                databaseHelper.outgoingResBookingShare(jid, sharingController.bookingQR, uniqueID, System
                                        .currentTimeMillis(), sharingController.resImgURL, sharingController.resID,
                                        sharingController.resTitle, sharingController.resLat, sharingController.resLon,
                                        sharingController.bookingid);

                                singleChatStanza.SoappShareBookingStanza(sharingController.bookingQR, jid,
                                        sharingController.bookingDate, sharingController.bookingTime,
                                        sharingController.pax, sharingController.resID, sharingController.resTitle,
                                        sharingController.promo, self_username, uniqueID, sharingController.bookingid);

                                //alert dialog to confirm updating to appt
                                new UIHelper().dialog2Btns(context, context.getString(R.string.booking_set_appt_title),
                                        context.getString(R.string.booking_set_appt_msg), R.string.ok_label, R
                                                .string.cancel,
                                        R.color.white, R.color.black,
                                        actionBookingUpdateApptPositive, actionBookingUpdateApptNegative, false);

                            } else {
                                databaseHelper.saveMessageAndSendWhenOnline("booking", jid,
                                        null, "Booking for " + sharingController.resTitle, uniqueID,
                                        currentDate, null, null,
                                        sharingController.resLat, sharingController.resLon, sharingController.resImgURL,
                                        sharingController.bookingid);
                            }
                            actionBookingUpdateApptNegative.run();
                        }
                        break;

                    case "image": //ext share img
                        if (SmackHelper.getXMPPConnection() != null && SmackHelper.getXMPPConnection().isAuthenticated()) {
//                            new AsynSendImg().execute(jid);
                            Intent intent = new Intent(context, ImageNormalPreviewActivity.class);
                            intent.putExtra("image_address", sharingController.str_uri);
                            intent.putExtra("jid", jid);
                            intent.putExtra("from", "external_image");
                            context.startActivity(intent);
                            activity.finishAffinity();
                        } else {
                            Toast.makeText(context, context.getString
                                    (R.string.xmpp_waiting_connection), Toast.LENGTH_SHORT).show();
                        }
                        break;

                    case "text": //ext share url
                        String self_username = preferences.getValue(context, GlobalVariables.STRPREF_USERNAME);

                        if (SmackHelper.getXMPPConnection() != null && SmackHelper.getXMPPConnection().isAuthenticated()) {
                            databaseHelper.urlOutputDatabase(jid, sharingController.webstring, uniqueID, currentDate);
                            singleChatStanza.SoappChatStanza(sharingController.webstring, jid, self_username, uniqueID);
                        } else {
                            databaseHelper.saveMessageAndSendWhenOnline("url", jid, null,
                                    sharingController.webstring, uniqueID, currentDate, null,
                                    null, null, null, null, null);
                        }
                        Intent intent = new Intent(context, IndiChatLog.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        intent.putExtra("jid", jid);
                        intent.putExtra("remoteChat", "1");
                        context.startActivity(intent);

                        activity.finish();
                        break;

                    default:
                        break;
                }
            } else { //grp
                String userName = preferences.getValue(context, GlobalVariables.STRPREF_USERNAME);
                String userJid = preferences.getValue(context, GlobalVariables.STRPREF_USER_ID);
                String roomName = list_name.getText().toString(); //new

                switch (sharingController.from) {
                    case "forward":
                        switch (sharingController.forwardIssender) {
                            case 10: //outgoing text msg
                                if (SmackHelper.getXMPPConnection() != null && SmackHelper.getXMPPConnection().isAuthenticated()) {
                                    databaseHelper.GroupMessageOutputDatabase(jid, sharingController.forwardMsgData,
                                            uniqueID, currentDate, userJid);

                                    groupChatStanza.GroupMessage(jid, userJid, userName, sharingController.forwardMsgData,
                                            uniqueID, roomName);

                                } else {
                                    databaseHelper.saveMessageAndSendWhenOnline("message", jid, userJid,
                                            sharingController.forwardMsgData, uniqueID, currentDate,
                                            null, null, null, null, null, null);
                                }
                                break;

//                            case 20: //outgoing img
//                                databaseHelper.GroupImageOutputDatabase(jid, uniqueID, currentDate, address, orient, "image", false);
//                                break;

//                            case 24: //outgoing vid
//                                break;
                        }

                        Intent intentFW = new Intent(context, GroupChatLog.class);
                        intentFW.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        intentFW.putExtra("jid", jid);

                        context.startActivity(intentFW);

                        activity.finish();
                        break;

                    case "foodChat":
                        if (sharingController.resID != null && sharingController.resTitle != null) {
                            if (SmackHelper.getXMPPConnection() != null && SmackHelper.getXMPPConnection().isAuthenticated()) {
                                groupChatStanza.GroupRestaurant(jid, userJid, userName, sharingController.resTitle,
                                        uniqueID, sharingController.resID, roomName);

                                databaseHelper.RestaurantGroupOutputDatabase(jid,
                                        sharingController.resImgURL, sharingController.resID, uniqueID,
                                        sharingController.resTitle, currentDate);
                            } else {
                                databaseHelper.saveMessageAndSendWhenOnline("restaurant",
                                        jid, userJid, sharingController.resTitle, uniqueID, currentDate,
                                        null, null, null, null,
                                        sharingController.resImgURL, sharingController.resID);
                            }

                            Intent intent = new Intent(context, GroupChatLog.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            intent.putExtra("jid", jid);

                            context.startActivity(intent);

                            activity.finish();
                        }
                        break;

                    case "foodAppt":
                        if (sharingController.resID != null && sharingController.resTitle != null) {
                            Intent grpScheIntent = new Intent(context, NewIndiExistApptActivity.class);

                            grpScheIntent.putExtra("jid", jid);
                            grpScheIntent.putExtra("exist", 1);

                            //for restaurant details (as location details now)
                            grpScheIntent.putExtra("apptLoc", sharingController.resTitle);

                            //no address yet, need to fix API first
//                        grpScheIntent.putExtra("apptAddress", resAddress);

                            grpScheIntent.putExtra("apptLon", sharingController.resLon);
                            grpScheIntent.putExtra("apptLat", sharingController.resLat);

                            //using imgURL for now - if need mapURL see in future
//                        String mapURL = "https://maps.googleapis.com/maps/api/staticmap?center=" +
//                                resLat + "," + resLon + "&zoom=16&size=400x200&maptype=roadmap&format=png&visual_refresh" +
//                                "=true&markers=size:mid%7Ccolor:0xff0000%7Clabel:%7C" + resLat + "," + resLon;
                            grpScheIntent.putExtra("apptMapURL", sharingController.resImgURL);

                            context.startActivity(grpScheIntent);

                            activity.finish();
                        }
                        break;

                    case "bookingChat": //share restaurant BOOKING to chat
                        String self_username = preferences.getValue(context, GlobalVariables.STRPREF_USERNAME);
                        String self_jid = preferences.getValue(context, GlobalVariables.STRPREF_USER_ID);

                        if (sharingController.bookingQR != null) {
                            if (SmackHelper.getXMPPConnection() != null && SmackHelper.getXMPPConnection().isAuthenticated()) {
                                databaseHelper.outgoingResBookingShare(jid, sharingController.bookingQR,
                                        uniqueID, currentDate, sharingController.resImgURL,
                                        sharingController.resID, sharingController.resTitle, sharingController.resLat,
                                        sharingController.resLon, sharingController.bookingid);

                                groupChatStanza.GroupShareBooking(sharingController.bookingid, jid,
                                        self_jid, self_username, sharingController.resTitle, uniqueID,
                                        roomName, sharingController.bookingDate, sharingController.bookingTime,
                                        sharingController.pax, sharingController.resID, sharingController.promo,
                                        sharingController.bookingQR);

                                //alert dialog to confirm updating to appt
                                new UIHelper().dialog2Btns(context, context.getString(R.string.booking_set_appt_title),
                                        context.getString(R
                                                .string.booking_set_appt_msg), R.string.ok_label, R
                                                .string.cancel,
                                        R.color.white, R.color.black,
                                        actionBookingUpdateApptPositive, actionBookingUpdateApptNegative, false);

                            } else {
                                databaseHelper.saveMessageAndSendWhenOnline("booking",
                                        jid, self_jid, sharingController.bookingQR, uniqueID, currentDate,
                                        null, null, sharingController.resLat,
                                        sharingController.resLon, sharingController.resImgURL, sharingController.bookingid);
                            }
                            actionBookingUpdateApptNegative.run();
                        }
                        break;

                    case "image":
                        final String roomname = list_name.getText().toString();

                        if (SmackHelper.getXMPPConnection() != null && SmackHelper.getXMPPConnection().isAuthenticated()) {
//                            AsyncShareImgGrp runBitmapCompressionBackground = new AsyncShareImgGrp();
//                            new AsyncShareImgGrp().execute(jid, roomname);//new
                            Intent intent = new Intent(context, ImageNormalPreviewActivity.class);
                            intent.putExtra("image_address", sharingController.str_uri);
                            intent.putExtra("jid", jid);
                            intent.putExtra("from", "external_image");
                            context.startActivity(intent);
                            activity.finishAffinity();
                        } else {
                            Toast.makeText(context, context.getString
                                    (R.string.xmpp_waiting_connection), Toast
                                    .LENGTH_SHORT).show();
                        }
                        break;

                    case "text":
                        if (SmackHelper.getXMPPConnection() != null && SmackHelper.getXMPPConnection().isAuthenticated()) {

                            databaseHelper.GroupUrlOutputDatabase(jid, sharingController.webstring,
                                    uniqueID, currentDate, userJid);

                            groupChatStanza.GroupMessage(jid, userJid, roomName, sharingController.webstring,
                                    uniqueID, roomName);

                        } else {
                            databaseHelper.saveMessageAndSendWhenOnline("url", jid, userJid,
                                    sharingController.webstring, uniqueID, currentDate, null,
                                    null, null, null, null, null);
                        }

                        Intent intent = new Intent(context, GroupChatLog.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        intent.putExtra("jid", jid);
                        intent.putExtra("remoteChat", "1");

                        context.startActivity(intent);

                        activity.finish();

                        break;

                    default:
                        break;
                }
            }
        }
    };

    SharingExistHolder(View itemView) {
        super(itemView);
        list_name = itemView.findViewById(R.id.list_name);
        list_profileImage = itemView.findViewById(R.id.list_profileImage);

        itemView.setOnClickListener(this);

        sharingController = (SharingController) context;
    }

    public void setData(SharingList item) {
        jid = item.getContactRoster().getContactJid();

        String name;
        if (jid.length() == 12) { //indi
            name = item.getContactRoster().getPhonename();

            if (name == null || name.length() == 0) {
                name = item.getContactRoster().getDisplayname() + " " + item.getContactRoster().getDisplayname();
            }

            imageLoadHelper.setImgByte(list_profileImage, item.getContactRoster().getProfilephoto(), R.drawable.in_propic_circle_150px);

        } else { //grp
            name = item.getContactRoster().getDisplayname();

            imageLoadHelper.setImgByte(list_profileImage, item.getContactRoster().getProfilephoto(), R.drawable.grp_propic_circle_150px);

        }

        list_name.setText(name);

        Integer disabled = item.getContactRoster().getDisabledStatus();
        if (disabled != null)
            if (disabled == 1) { //user has left room
                itemView.setClickable(false);

                list_profileImage.setColorFilter(ContextCompat.getColor(context, R.color
                        .grey5));
                list_name.setTextColor(ContextCompat.getColor(context, R.color
                        .grey5));
            }
    }

    @Override
    public void onClick(View v) {
        String message = context.getString(R.string.share_confirm) + " " + list_name.getText().toString() + "?";

        new UIHelper().dialog2Btns(context, context.getString(R.string.share_confirm_title),
                message, R.string.ok_label, R.string.cancel, R.color.white, R.color.black,
                actionOk, null, true);
    }

    //get lat and long of restaurant from server (grp)
    private void getResLatLongGrp(final String jid) {
        String access_token = preferences.getValue(context, GlobalVariables.STRPREF_ACCESS_TOKEN);
        final String resID = sharingController.resID;

        //build retrofit
        RestaurantLocation client = RetrofitAPIClient.getClient().create(RestaurantLocation.class);
        retrofit2.Call<RestaurantLocModel> call = client.resLocation(resID, "Bearer " + access_token);
        call.enqueue(new retrofit2.Callback<RestaurantLocModel>() {
            @Override
            public void onResponse(retrofit2.Call<RestaurantLocModel> call, final retrofit2.Response<RestaurantLocModel> response) {
                if (!response.isSuccessful()) {
                    new Handler(Looper.getMainLooper())
                            .post(new Runnable() {
                                @Override
                                public void run() {
                                    if (progressDialog != null && progressDialog.isShowing()) {
                                        progressDialog.dismiss();
                                    }
                                    new MiscHelper().retroLogUnsuc(response, "getReslatlogGrp  ", "JAY");
                                    Toast.makeText(context, R
                                            .string.onresponse_unsuccessful, Toast
                                            .LENGTH_SHORT).show();
                                }
                            });
                    return;
                }
                String lat = response.body().getLatitude();
                String lon = response.body().getLongitude();


            }

            @Override
            public void onFailure(retrofit2.Call<RestaurantLocModel> call, Throwable t) {
                new Handler(Looper.getMainLooper())
                        .post(new Runnable() {
                            @Override
                            public void run() {
                                if (progressDialog != null && progressDialog.isShowing()) {
                                    progressDialog.dismiss();
                                }
                                new MiscHelper().retroLogFailure(t, "getReslatlogGrp  ", "JAY");
                                Toast.makeText(context, R
                                        .string.onfailure, Toast
                                        .LENGTH_SHORT).show();
                            }
                        });
            }
        });
    }

    private void getResLatLong(final String jid) {
        String access_token = preferences.getValue(context, GlobalVariables.STRPREF_ACCESS_TOKEN);
        final String resID = sharingController.resID;

        //build retrofit
        RestaurantLocation client = RetrofitAPIClient.getClient().create(RestaurantLocation.class);
        retrofit2.Call<RestaurantLocModel> call = client.resLocation(resID, "Bearer " + access_token);

        call.enqueue(new retrofit2.Callback<RestaurantLocModel>() {
            @Override
            public void onResponse(retrofit2.Call<RestaurantLocModel> call, retrofit2.Response<RestaurantLocModel> response) {
                if (!response.isSuccessful()) {
                    new Handler(Looper.getMainLooper())
                            .post(new Runnable() {
                                @Override
                                public void run() {
                                    if (progressDialog != null && progressDialog.isShowing()) {
                                        progressDialog.dismiss();
                                    }
                                    new MiscHelper().retroLogUnsuc(response, "getReslatlong ", "JAY");
                                    Toast.makeText(context, R
                                            .string.onresponse_unsuccessful, Toast
                                            .LENGTH_SHORT).show();
                                }
                            });
                    return;
                }
                String lat = response.body().getLatitude();
                String lon = response.body().getLongitude();


            }

            @Override
            public void onFailure(retrofit2.Call<RestaurantLocModel> call, Throwable t) {
                new Handler(Looper.getMainLooper())
                        .post(new Runnable() {
                            @Override
                            public void run() {
                                if (progressDialog != null && progressDialog.isShowing()) {
                                    progressDialog.dismiss();
                                }
                                new MiscHelper().retroLogFailure(t, "getReslatlong  ", "JAY");
                                Toast.makeText(context, R
                                        .string.onfailure, Toast
                                        .LENGTH_SHORT).show();
                            }
                        });
            }
        });
    }

    //async task for setting restaurant to appt (grp)
    private class AsynShareResApptGrp extends AsyncTask<String, Integer, Void> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            //Display progressDialog before download starts
            progressDialog = ProgressDialog.show(context,
                    null, context.getString(R.string.progress_sending_res_appt));
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);

        }

        @Override
        protected Void doInBackground(String... params) {
            final String roomJid = params[0];

            getResLatLongGrp(roomJid);
            return null;
        }
    }

    //async task for setting restaurant to appt (indi)
    private class AsynShareResAppt extends AsyncTask<String, Integer, Void> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            //Display progressDialog before download starts
            progressDialog = ProgressDialog.show(context, null, context.getString(R.string.progress_sending_res_appt));
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);

        }

        @Override
        protected Void doInBackground(String... params) {
            final String jid = params[0];

            getResLatLong(jid);
            return null;
        }
    }
}
