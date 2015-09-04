package com.mcochin.popularmovies;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;

import com.mcochin.popularmovies.fragments.MovieDetailFragment;
import com.mcochin.popularmovies.fragments.NetworkFragment;
import com.mcochin.popularmovies.pojo.Movie;

/**
 * This Activity is for phones and it shows the details of the movie clicked
 */
public class MovieDetailActivity extends AppCompatActivity{
    public static final String TITLE_KEY = "title";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movie_detail);

        setTitle(getIntent().getStringExtra(TITLE_KEY));

        //Delegate the movie data passed in to this Activity to the movieDetailFragment
        Bundle args = new Bundle();
        args.putParcelable(Movie.MOVIE_KEY, getIntent().getParcelableExtra(Movie.MOVIE_KEY));

        Fragment movieDetailFragment = new MovieDetailFragment();
        movieDetailFragment.setArguments(args);

        getSupportFragmentManager().beginTransaction()
                .replace(R.id.movie_detail_container, movieDetailFragment)
                .commit();
    }

}
