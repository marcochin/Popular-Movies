/**
 * Copyright (C) Marco Chin
 */
package com.mcochin.popularmovies;

import android.content.Intent;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;

import com.mcochin.popularmovies.fragments.MovieDetailFragment;
import com.mcochin.popularmovies.fragments.MovieGridFragment;
import com.mcochin.popularmovies.pojo.Movie;
import com.mcochin.popularmovies.services.NetworkService;

public class MainActivity extends AppCompatActivity implements MovieGridFragment.Callback{
    public static final String TITLE_KEY = "title";
    private boolean mTwoPane;
    private MovieGridFragment mMovieGridFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Determine if tablet or phone
        mTwoPane = findViewById(R.id.movie_detail_container) != null;

        mMovieGridFragment = ((MovieGridFragment)getSupportFragmentManager().
                findFragmentById(R.id.fragment_movie_grid));
        mMovieGridFragment.setCallback(this);

        if(savedInstanceState != null){
            setTitle(savedInstanceState.getCharSequence(TITLE_KEY));
        }
    }

    @Override
    public void onMovieItemClick(Movie movie, int position) {
        Bundle args = new Bundle();
        args.putParcelable(Movie.MOVIE_KEY, movie);
        args.putInt(MovieGridFragment.GRID_POSITION_KEY, position);

        if(mTwoPane){
            // If we are on tablet just load a Fragment;
            Fragment movieDetailFragment = new MovieDetailFragment();
            movieDetailFragment.setArguments(args);

            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.movie_detail_container, movieDetailFragment)
                    .commit();
        } else {
            // If we are on phone load an Activity
            args.putCharSequence(TITLE_KEY, getTitle());

            Intent movieDetailIntent = new Intent(this, MovieDetailActivity.class);
            movieDetailIntent.putExtra(MovieDetailActivity.ARGUMENTS_KEY, args);
            startActivityForResult(movieDetailIntent, MovieDetailActivity.REQUEST_CODE);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putCharSequence(TITLE_KEY, getTitle());
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // If the user is using a phone and removes a movie from favorites,it will be removed when
        // he exits the MovieDetailActivity and this if statement will be executed.
        if(requestCode == MovieDetailActivity.REQUEST_CODE){
            if (resultCode == MovieDetailFragment.RESULT_REMOVE_FAVORITE && data != null){
                mMovieGridFragment.removeFavoriteMovie(data.getIntExtra(MovieGridFragment.GRID_POSITION_KEY,
                                MovieGridFragment.ERROR));
            }
        }
    }
}
