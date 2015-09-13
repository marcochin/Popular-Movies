package com.mcochin.popularmovies.pojo;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.List;

/**
 * Holds the list of trailers. This class is used in MovieExtraResults.
 */
public class TrailerList implements Parcelable {
    List<Trailer> youtube;

    public List<Trailer> getTrailers() {
        return youtube;
    }

    public void setTrailers(List<Trailer> youtube) {
        this.youtube = youtube;
    }

    protected TrailerList(Parcel in) {
        if (in.readByte() == 0x01) {
            youtube = new ArrayList<Trailer>();
            in.readList(youtube, Trailer.class.getClassLoader());
        } else {
            youtube = null;
        }
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        if (youtube == null) {
            dest.writeByte((byte) (0x00));
        } else {
            dest.writeByte((byte) (0x01));
            dest.writeList(youtube);
        }
    }

    @SuppressWarnings("unused")
    public static final Parcelable.Creator<TrailerList> CREATOR = new Parcelable.Creator<TrailerList>() {
        @Override
        public TrailerList createFromParcel(Parcel in) {
            return new TrailerList(in);
        }

        @Override
        public TrailerList[] newArray(int size) {
            return new TrailerList[size];
        }
    };
}