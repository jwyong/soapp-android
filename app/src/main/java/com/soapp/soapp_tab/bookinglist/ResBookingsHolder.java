package com.soapp.soapp_tab.bookinglist;

import android.content.Intent;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.soapp.R;
import com.soapp.soapp_tab.booking.ResBookingActivity;
import com.soapp.sql.room.joiners.BookingList;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Locale;

import androidx.recyclerview.widget.RecyclerView;

/* Created by chang on 04/08/2017. */

public class ResBookingsHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

    private final ImageView mybooking_status;
    private TextView res_title, mybooking_date, mybooking_time, mybooking_pax;
    private ImageView res_propic;
    private String res_id;
    private String bookId;

    public ResBookingsHolder(final View itemView) {

        super(itemView);
        res_title = itemView.findViewById(R.id.bookinglist_title);
//        res_propic = itemView.findViewById(R.id.res_propic);
//        res_id = (TextView) itemView.findViewById(R.id.res_id);
//        res_jid = (TextView) itemView.findViewById(R.id.res_jid);
        mybooking_status = itemView.findViewById(R.id.statusBookingColor);
        mybooking_date = itemView.findViewById(R.id.bookinglist_date);
        mybooking_time = itemView.findViewById(R.id.bookinglist_time);
        mybooking_pax = itemView.findViewById(R.id.bookinglist_pax);

        itemView.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
//        String resID = res_id.getText().toString();

        //ash - put intent booking details activity here
        Intent intent = new Intent(itemView.getContext(), ResBookingActivity.class);

        intent.putExtra("resID", res_id);
        intent.putExtra("bookingid", bookId);

        itemView.getContext().startActivity(intent);
    }


    public void setData(BookingList item) {
        if (item != null && item.getBooking().getBookingResId() != null) {

            res_id = item.getBooking().getBookingResId();
            bookId = item.getBooking().getBookingId();


            res_title.setText(item.getRestaurant().getResTitle());

//            res_id.setText(item.getBooking().getBookingResId());

//            res_jid.setText(item.getBooking().getResOwnerJid());


//            GlideApp.with(itemView)
//                    .asBitmap()
//                    .load(item.getRestaurant().getResImageUrl())
//                    .placeholder(R.drawable.default_propic_small_round)
//                    .thumbnail(0.5f)
//                    .encodeQuality(50)
//                    .apply(RequestOptions.circleCropTransform())
//                    .transition(BitmapTransitionOptions.withCrossFade())
//                    .override(180, 180)
//                    .into(res_propic);

            //info from cursorloader for future use
//        mybooking_booker.setText(c.getString(c.getColumnIndex("booker")));
            mybooking_pax.setText(item.getBooking().getBookingPax() + " pax");
//        mybooking_promo.setText(c.getString(c.getColumnIndex("promo")));
//        mybooking_date.setText(c.getString(c.getColumnIndex("date")));
            long timeInMilli = item.getBooking().getBookingTime();


            DateFormat dateformat = new SimpleDateFormat("dd MMM", Locale.ENGLISH);
            String dateStr = dateformat.format(item.getBooking().getBookingDate());

            DateFormat timeformat = new SimpleDateFormat("hh:mm a", Locale.ENGLISH);
            String timeStr = timeformat.format(item.getBooking().getBookingTime());

            mybooking_date.setText(dateStr);
            mybooking_time.setText(timeStr);

            switch (item.getBooking().getBookingStatus()) {
                case 1: //accepted by biz owner
                    mybooking_status.setImageResource(R.drawable.ic_green_dot_42px);
                    break;

                case 2: //pending action from biz owner
                    mybooking_status.setImageResource(R.drawable.ic_grey_dot_42px);
                    break;

                case 3: //rejected by biz owner
                    mybooking_status.setImageResource(R.drawable.ic_red_dot);
                    break;

                default:
                    mybooking_status.setImageResource(R.drawable.ic_grey_dot_42px);
                    break;
            }
        }
    }
}
