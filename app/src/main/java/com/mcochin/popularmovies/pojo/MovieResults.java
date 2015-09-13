package com.mcochin.popularmovies.pojo;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import com.mcochin.popularmovies.interfaces.Results;

import java.util.ArrayList;
import java.util.List;

/**
 * This class will hold the list of movies returned as a result from a JSON call
 */
public class MovieResults implements Results, Parcelable {
    private List<Movie> results;

    public List<Movie> getMovieList() {
        return results;
    }

    public void setMovieList(List<Movie> results) {
        this.results = results;
    }

    protected MovieResults(Parcel in) {
        if (in.readByte() == 0x01) {
            results = new ArrayList<Movie>();
            in.readList(results, Movie.class.getClassLoader());
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
    public static final Parcelable.Creator<MovieResults> CREATOR = new Parcelable.Creator<MovieResults>() {
        @Override
        public MovieResults createFromParcel(Parcel in) {
            return new MovieResults(in);
        }

        @Override
        public MovieResults[] newArray(int size) {
            return new MovieResults[size];
        }
    };
}