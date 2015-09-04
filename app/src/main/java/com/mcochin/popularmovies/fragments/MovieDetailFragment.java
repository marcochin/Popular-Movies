package com.mcochin.popularmovies.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.mcochin.popularmovies.R;
import com.mcochin.popularmovies.pojo.Movie;
import com.squareup.picasso.Picasso;

/**
 * Fragment that displays the details of the movie a user has clicked on
 */
public class MovieDetailFragment extends Fragment{
    private static final String DATE_SPLIT_TOKEN = "-";
    private Movie movie;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_movie_detail, container, false);
    }

    @Override
    public void onViewCreated(View v, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(v, savedInstanceState);

        if(savedInstanceState == null) {
            movie = getArguments().getParcelable(Movie.MOVIE_KEY);
        }else{
            movie = savedInstanceState.getParcelable(Movie.MOVIE_KEY);
        }

        ((TextView) v.findViewById(R.id.movie_title_textview)).setText(movie.getOriginalTitle());

        ((TextView) v.findViewById(R.id.movie_plot_textview)).setText(movie.getOverview());

        ((TextView) v.findViewById(R.id.movie_date_textview))
                .setText(getYearFromReleaseDate(movie.getReleaseDate()));

        ((TextView) v.findViewById(R.id.movie_rating_textview))
                .setText( getString(R.string.rating_out_of_10,
                        Float.toString(movie.getVoteAverage())));

        ImageView posterImageView = (ImageView) v.findViewById(R.id.movie_poster_imageview);
        Picasso.with(getContext()).load(movie.getPosterPath())
                .into(posterImageView);
        posterImageView.setContentDescription(movie.getOriginalTitle());

    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable(Movie.MOVIE_KEY, movie);
    }

    /**
     * Helper method to help extract the year from the date. This only works if the date is in
     * yyyy-MM-dd format or something similar, as long as the Year is first and followed by a "-".
     * @param date The date to extract the year from.
     * @return The extracted year.
     */
    private static String getYearFromReleaseDate(String date){
        return date == null || date.isEmpty()? "" : date.split(DATE_SPLIT_TOKEN)[0];
    }
}
