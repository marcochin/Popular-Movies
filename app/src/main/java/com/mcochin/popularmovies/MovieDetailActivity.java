package com.mcochin.popularmovies;

import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;

import com.mcochin.popularmovies.fragments.MovieDetailFragment;
import com.mcochin.popularmovies.pojo.Movie;

/**
 * This Activity is for phones and it shows the details of the movie clicked
 */
public class MovieDetailActivity extends AppCompatActivity{
    public static final String ARGUMENTS_KEY = "arguments";
    public static final int REQUEST_CODE = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movie_detail);

        //set title of the activity to match that of the main activity;
        Bundle args = getIntent().getBundleExtra(ARGUMENTS_KEY);
        setTitle(args.getCharSequence(MainActivity.TITLE_KEY));

        //Set arguments to be passed into the MovieDetailFragment
        Fragment movieDetailFragment;
        if (savedInstanceState == null) {
            movieDetailFragment = new MovieDetailFragment();
            movieDetailFragment.setArguments(args);

        } else {
            movieDetailFragment = getSupportFragmentManager()
                    .findFragmentByTag(MovieDetailFragment.TAG);

            setTitle(savedInstanceState.getCharSequence(MainActivity.TITLE_KEY));
        }

        //add fragment into the FragmentManager and put in the UI container
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.movie_detail_container, movieDetailFragment, MovieDetailFragment.TAG)
                .commit();

    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putCharSequence(MainActivity.TITLE_KEY, getTitle());
    }
}
