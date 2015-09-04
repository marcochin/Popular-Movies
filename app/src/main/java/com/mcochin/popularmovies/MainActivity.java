/**
 * Copyright (C) Marco Chin
 */
package com.mcochin.popularmovies;

import android.content.Intent;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.mcochin.popularmovies.fragments.MovieDetailFragment;
import com.mcochin.popularmovies.fragments.MovieGridFragment;
import com.mcochin.popularmovies.fragments.NetworkFragment;
import com.mcochin.popularmovies.pojo.JsonResults;
import com.mcochin.popularmovies.pojo.Movie;

import retrofit.RetrofitError;
import retrofit.client.Response;

public class MainActivity extends AppCompatActivity
        implements NetworkFragment.Callback, MovieGridFragment.Callback{
    private boolean mTwoPane;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mTwoPane = findViewById(R.id.movie_detail_container) != null;

        //Instantiate the retained networkFragment
        if(savedInstanceState == null) {
            NetworkFragment networkFragment = new NetworkFragment();

            getSupportFragmentManager().beginTransaction()
                    .add(networkFragment, NetworkFragment.TAG)
                    .commit();

            getSupportFragmentManager().executePendingTransactions();
        }

        //set callbacks for fragments
        ((MovieGridFragment)getSupportFragmentManager()
                .findFragmentById(R.id.fragment_movie_grid)).setCallback(this);

        NetworkFragment networkFragment = ((NetworkFragment) getSupportFragmentManager()
                        .findFragmentByTag(NetworkFragment.TAG));
        networkFragment.setCallback(this);

        //Load popular movies on first open
        if(savedInstanceState == null) {
            networkFragment.loadPopularMovies();
        }
    }

    @Override
    public void onNetworkSuccess(JsonResults jsonResults, Response response, int sortType) {
        String title = "";

        switch(sortType){
            case NetworkFragment.SORT_POPULAR:
                title = getString(R.string.title_popular_movies);
                break;
            case NetworkFragment.SORT_HIGHEST_RATED:
                title = getString(R.string.title_highest_rated_movies);
                break;
        }

        //Change the title of actionBar corresponding to the sort type
        setTitle(title);

        //Send the movieList to the MovieGridFragment to display
        MovieGridFragment movieGridfragment = (MovieGridFragment)getSupportFragmentManager()
                .findFragmentById(R.id.fragment_movie_grid);

        movieGridfragment.setMovieList(jsonResults.getResults());
    }

    @Override
    public void onNetworkFailure(RetrofitError error) {
    }

    @Override
    public void onMovieItemClick(Movie movie) {
        Bundle args = new Bundle();
        args.putParcelable(Movie.MOVIE_KEY, movie);

        if(mTwoPane){
            //If we are on tablet just load a Fragment;
            Fragment movieDetailFragment = new MovieDetailFragment();
            movieDetailFragment.setArguments(args);

            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.movie_detail_container, movieDetailFragment)
                    .commit();
        } else {
            //If we are on phone load an Activity
            Intent movieDetailIntent = new Intent(this, MovieDetailActivity.class);
            movieDetailIntent.putExtra(Movie.MOVIE_KEY, movie);
            movieDetailIntent.putExtra(MovieDetailActivity.TITLE_KEY, getTitle());
            startActivity(movieDetailIntent);
        }
    }
}
