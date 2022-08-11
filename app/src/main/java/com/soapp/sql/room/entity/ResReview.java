package com.soapp.sql.room.entity;

import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

@Entity
public class ResReview {

    @PrimaryKey(autoGenerate = true)
    private Integer ResReviewRow;

    private String ReviewResId;
    private Integer SelfReview1 = 0;
    private Integer SelfReview2 = 0;

    public ResReview() {
    }

    @Ignore
    public ResReview(int resReviewRow, String reviewResId, int selfReview1, int selfReview2) {
        ResReviewRow = resReviewRow;
        ReviewResId = reviewResId;
        SelfReview1 = selfReview1;
        SelfReview2 = selfReview2;
    }

    public int getResReviewRow() {
        return ResReviewRow;
    }

    public void setResReviewRow(int resReviewRow) {
        ResReviewRow = resReviewRow;
    }

    public String getReviewResId() {
        return ReviewResId;
    }

    public void setReviewResId(String reviewResId) {
        ReviewResId = reviewResId;
    }

    public int getSelfReview1() {
        return SelfReview1;
    }

    public void setSelfReview1(int selfReview1) {
        SelfReview1 = selfReview1;
    }

    public int getSelfReview2() {
        return SelfReview2;
    }

    public void setSelfReview2(int selfReview2) {
        SelfReview2 = selfReview2;
    }

}
