package com.mcochin.popularmovies.pojo;

import android.os.Parcel;
import android.os.Parcelable;

import com.mcochin.popularmovies.interfaces.Results;

/**
 * The movies extras class contains information that was not available in the first API request
 * when retrieving the most popular/ highest rated movies for the grid.
 * When a user clicks on a movie it will loads make an additional API call to retrieve this data.
 */
public class MovieExtraResults implements Results, Parcelable {
    private int runtime;
    private TrailerList trailers;
    private ReviewList reviews;

    public int getRuntime() {
        return runtime;
    }

    public void setRuntime(int runtime) {
        this.runtime = runtime;
    }

    public TrailerList getTrailerList() {
        return trailers;
    }

    public void setTrailerList(TrailerList trailerList) {
        this.trailers = trailerList;
    }

    public ReviewList getReviewList() {
        return reviews;
    }

    public void setReviewList(ReviewList reviewList) {
        this.reviews = reviewList;
    }

    protected MovieExtraResults(Parcel in) {
        runtime = in.readInt();
        trailers = (TrailerList) in.readValue(TrailerList.class.getClassLoader());
        reviews = (ReviewList) in.readValue(ReviewList.class.getClassLoader());
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(runtime);
        dest.writeValue(trailers);
        dest.writeValue(reviews);
    }

    @SuppressWarnings("unused")
    public static final Parcelable.Creator<MovieExtraResults> CREATOR = new Parcelable.Creator<MovieExtraResults>() {
        @Override
        public MovieExtraResults createFromParcel(Parcel in) {
            return new MovieExtraResults(in);
        }

        @Override
        public MovieExtraResults[] newArray(int size) {
            return new MovieExtraResults[size];
        }
    };
}