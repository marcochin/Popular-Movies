package com.mcochin.popularmovies.pojo;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * JsonResults.java's "results" variable contains a list of Movies.
 * This Movie class contains information for a specific movie in the list.
 */

public class Movie implements Parcelable {
    // Valid image sizes: "w92", "w154", "w185", "w342", "w500", "w780", or "original"
    private static final String POSTER_IMG_BASE_URL = "http://image.tmdb.org/t/p/w185";
    public static final String MOVIE_KEY = "movie";

    // IMPORTANT! The variables must match the keys in the JSON feed.
    // lowercase with underscores were converted to camelCase with GSON library.
    // Example JSON feed:
    // https://api.themoviedb.org/3/discover/movie?sort_by=popularity.desc&api_key=API_KEY
    private int id;
    private String originalTitle;
    private String posterPath;
    private String overview;
    private float voteAverage;
    private String releaseDate;

    public Movie(){

    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getOriginalTitle() {
        return originalTitle;
    }

    public void setOriginalTitle(String originalTitle) {
        this.originalTitle = originalTitle;
    }

    public String getPosterPath() {
        return POSTER_IMG_BASE_URL + posterPath;
    }

    public void setPosterPath(String posterPath) {
        this.posterPath = posterPath;
    }

    public String getOverview() {
        return overview;
    }

    public void setOverview(String overview) {
        this.overview = overview;
    }

    public float getVoteAverage() {
        return voteAverage;
    }

    public void setVoteAverage(float voteAverage) {
        this.voteAverage = voteAverage;
    }

    public String getReleaseDate() {
        return releaseDate;
    }

    public void setReleaseDate(String releaseDate) {
        this.releaseDate = releaseDate;
    }

    protected Movie(Parcel in) {
        originalTitle = in.readString();
        posterPath = in.readString();
        overview = in.readString();
        voteAverage = in.readFloat();
        releaseDate = in.readString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(originalTitle);
        dest.writeString(posterPath);
        dest.writeString(overview);
        dest.writeFloat(voteAverage);
        dest.writeString(releaseDate);
    }

    @SuppressWarnings("unused")
    public static final Parcelable.Creator<Movie> CREATOR = new Parcelable.Creator<Movie>() {
        @Override
        public Movie createFromParcel(Parcel in) {
            return new Movie(in);
        }

        @Override
        public Movie[] newArray(int size) {
            return new Movie[size];
        }
    };
}