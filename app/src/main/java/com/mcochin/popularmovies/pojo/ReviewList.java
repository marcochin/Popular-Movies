package com.mcochin.popularmovies.pojo;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.List;

/**
 * Holds the list of reviews. This class is used in MovieExtraResults.
 */
public class ReviewList implements Parcelable {
    private List<Review> results;

    public List<Review> getReviews() {
        return results;
    }

    public void setReviews(List<Review> results) {
        this.results = results;
    }

    protected ReviewList(Parcel in) {
        if (in.readByte() == 0x01) {
            results = new ArrayList<Review>();
            in.readList(results, Review.class.getClassLoader());
        } else {
            results = null;
        }
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        if (results == null) {
            dest.writeByte((byte) (0x00));
        } else {
            dest.writeByte((byte) (0x01));
            dest.writeList(results);
        }
    }

    @SuppressWarnings("unused")
    public static final Parcelable.Creator<ReviewList> CREATOR = new Parcelable.Creator<ReviewList>() {
        @Override
        public ReviewList createFromParcel(Parcel in) {
            return new ReviewList(in);
        }

        @Override
        public ReviewList[] newArray(int size) {
            return new ReviewList[size];
        }
    };
}
