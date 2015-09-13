package com.mcochin.popularmovies.services;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.annotation.IntDef;
import android.util.Log;
import android.widget.Toast;

import com.mcochin.popularmovies.R;
import com.mcochin.popularmovies.fragments.MovieDetailFragment;
import com.mcochin.popularmovies.fragments.MovieGridFragment;
import com.mcochin.popularmovies.interfaces.Results;
import com.mcochin.popularmovies.pojo.Movie;
import com.mcochin.popularmovies.pojo.MovieExtraResults;
import com.mcochin.popularmovies.pojo.MovieResults;
import com.mcochin.popularmovies.retrofit.TheMovieDbApiProvider;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import retrofit.RetrofitError;
import retrofit.client.Response;

/**
 * The is a service that will access the network and retrieve JSON responses for the Fragments.
 * This is done through the communication of BroadcastReceiveers.
 */
public class NetworkService extends IntentService{
    public static final String TAG = NetworkService.class.getSimpleName();
    public static final int ERROR = -1;

    public static final String ILLEGAL_REQUEST_TYPE
            = "Please pass NetworkService.NetworkRequestType as an argument for your intent " +
            "and use NetworkService,REQUEST_TYPE_KEY for the key.";

    public static final String ILLEGAL_MOVIE_ID = "Please pass in a valid movie id.";

    public static final String REQUEST_TYPE_KEY = "requestType";
    public static final String RESULTS_KEY = "results";
    public static final String PROGRESS_KEY = "progress";
    public static final String MOVIE_ID_KEY = "movieId";
    private String mApiKey;

    private TheMovieDbApiProvider.TheMovieDbApi mTheMovieDbApi;
    private int mNetworkRequestType;

    // Keep track of the last id to be processed when looping through the shared preferences
    // for favorites and therefore we can determine when to hide the progress wheel.
    private int mLastFavoriteIdInLoop = -1;


    /**
     * We use these constants to let us know what data we are requesting and also
     * so the main activity can change the title to reflect the current content
     */
    @IntDef({MOVIE_POPULAR, MOVIE_HIGHEST_RATED, MOVIE_FAVORITES})
    @Retention(RetentionPolicy.SOURCE)
    public @interface NetworkRequestType {}
    public static final int MOVIE_POPULAR = 0;
    public static final int MOVIE_HIGHEST_RATED = 1;
    public static final int MOVIE_FAVORITES = 2;

    /**
     * These are network request types that need to be accompanied specifically with a movieId.
     */
    @IntDef({MOVIE_FAVORITES_SINGLE, MOVIE_EXTRAS})
    @Retention(RetentionPolicy.SOURCE)
    public @interface NetworkRequestTypeWithId {}
    public static final int MOVIE_FAVORITES_SINGLE = 3;
    //Extras are runtime, trailers, and reviews
    public static final int MOVIE_EXTRAS = 4;

    /**
     * Constants to show and hide the progress wheel
     */
    @IntDef({SHOW_PROGRESS_WHEEL, HIDE_PROGRESS_WHEEL})
    @Retention(RetentionPolicy.SOURCE)
    public @interface ProgressWheelStatus {}
    public static final int SHOW_PROGRESS_WHEEL = 1;
    public static final int HIDE_PROGRESS_WHEEL = 0;


    public NetworkService(String name) {
        super(name);
    }

    public NetworkService(){
        super(TAG);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        mNetworkRequestType = intent.getIntExtra(REQUEST_TYPE_KEY, ERROR);
        if(mNetworkRequestType == ERROR){
            throw new IllegalArgumentException(ILLEGAL_REQUEST_TYPE);
        }

        int movieId;

        // Retrieve the API object created by retrofit so we can make network calls
        mTheMovieDbApi = TheMovieDbApiProvider.getInstance().getApi();
        mApiKey = getString(R.string.themoviedb_api_key);

        switch(mNetworkRequestType){
            case MOVIE_POPULAR:
                loadPopularMovies();
                break;
            case MOVIE_HIGHEST_RATED:
                loadHighestRatedMovies();
                break;
            case MOVIE_FAVORITES:
                SharedPreferences prefs = getSharedPreferences(MovieGridFragment.SHARED_PREFS_NAME,
                        Context.MODE_PRIVATE);

                // Loop through all favorites in the shared prefs and load them one by one
                Object[] favoritesList = prefs.getAll().values().toArray();
                for(int i = 0; i < favoritesList.length; i++){
                    if(favoritesList[i] instanceof Integer){
                        loadFavoriteMovies((Integer)favoritesList[i]);

                        // We need to flag the last favorite in the list so the
                        // UI will know when to hide the progress wheel.
                        if(i == favoritesList.length -1){
                            mLastFavoriteIdInLoop = (Integer)favoritesList[i];
                        }
                    }
                }
                break;
            case MOVIE_FAVORITES_SINGLE:
                movieId = intent.getIntExtra(MOVIE_ID_KEY, ERROR);
                mLastFavoriteIdInLoop = movieId;

                if(movieId == ERROR || movieId < 0){
                    throw new IllegalArgumentException(ILLEGAL_MOVIE_ID);
                }
                loadFavoriteMovies(movieId);
                break;
            case MOVIE_EXTRAS:
                movieId = intent.getIntExtra(MOVIE_ID_KEY, ERROR);
                if(movieId == ERROR || movieId < 0){
                    throw new IllegalArgumentException(ILLEGAL_MOVIE_ID);
                }
                loadMovieExtras(movieId);
                break;
        }
    }

    /**
     * Makes a call to the themoviedb.org API and fetches popular movies
     */
    public void loadPopularMovies(){
        mNetworkRequestType = MOVIE_POPULAR;
        setGridFragmentProgressWheelVisibility(SHOW_PROGRESS_WHEEL);

        mTheMovieDbApi.getMostPopular(mApiKey, new retrofit.Callback<MovieResults>() {
            @Override
            public void success(MovieResults movieResults, Response response) {
                onNetworkSuccess(movieResults, response);
            }

            @Override
            public void failure(RetrofitError error) {
                onNetworkFailure(error);
            }
        });
    }

    /**
     * Makes a call to the themoviedb.org API and fetches highest rated movies
     */
    public void loadHighestRatedMovies(){
        mNetworkRequestType = MOVIE_HIGHEST_RATED;
        setGridFragmentProgressWheelVisibility(SHOW_PROGRESS_WHEEL);

        mTheMovieDbApi.getHighestRated(mApiKey, new retrofit.Callback<MovieResults>() {
            @Override
            public void success(MovieResults movieResults, Response response) {
                onNetworkSuccess(movieResults, response);
            }

            @Override
            public void failure(RetrofitError error) {
                onNetworkFailure(error);
            }
        });
    }

    /**
     * Makes a call to the themoviedb.org API and fetches move extras
     * (movie runtime, trailers, and reviews).
     */
    public void loadMovieExtras(int movieId){
        mNetworkRequestType = MOVIE_EXTRAS;
        setDetailFragmentProgressWheelVisibility(SHOW_PROGRESS_WHEEL);

        mTheMovieDbApi.getMovieExtras(mApiKey, movieId, new retrofit.Callback<MovieExtraResults>() {
            @Override
            public void success(MovieExtraResults movieExtraResults, Response response) {
                onNetworkSuccess(movieExtraResults, response);
            }

            @Override
            public void failure(RetrofitError error) {
                onNetworkFailure(error);
            }
        });
    }

    /**
     * Makes a call to the themoviedb.org API and fetches the movies specified in shared prefs.
     */
    public void loadFavoriteMovies(int movieId){
        mNetworkRequestType = MOVIE_FAVORITES;
        setGridFragmentProgressWheelVisibility(SHOW_PROGRESS_WHEEL);

        mTheMovieDbApi.getFavoriteMovies(mApiKey, movieId, new retrofit.Callback<Movie>() {
            @Override
            public void success(Movie movie, Response response) {
                onNetworkSuccess(movie, response);
            }

            @Override
            public void failure(RetrofitError error) {
                onNetworkFailure(error);
            }
        });
    }

    /**
     * Called when an API request has been successfully completed
     * @param results The JSON data that was processed mapped to an object courtesy of GSON library.
     * @param response The network response containing status codes etc.
     */
    private void onNetworkSuccess(Results results, Response response){
        Intent resultsIntent;

        // Deciding which broadcast to send amd load results into the broadcast
        if(results instanceof MovieResults) {
            resultsIntent = new Intent(MovieGridFragment.GridBroadcastReceiver.INTENT_ACTION);
            resultsIntent.putExtra(RESULTS_KEY, (MovieResults) results);
            setGridFragmentProgressWheelVisibility(HIDE_PROGRESS_WHEEL);

        } else if (results instanceof Movie){

            resultsIntent = new Intent(MovieGridFragment.GridBroadcastReceiver.INTENT_ACTION);
            resultsIntent.putExtra(RESULTS_KEY, (Movie) results);

            if(mLastFavoriteIdInLoop == ((Movie) results).getId()) {
                mLastFavoriteIdInLoop = -1;
                setGridFragmentProgressWheelVisibility(HIDE_PROGRESS_WHEEL);
            }
        } else{
            resultsIntent = new Intent(MovieDetailFragment.DetailBroadcastReceiver.INTENT_ACTION);
            resultsIntent.putExtra(RESULTS_KEY, (MovieExtraResults) results);
            setDetailFragmentProgressWheelVisibility(HIDE_PROGRESS_WHEEL);
        }

        resultsIntent.putExtra(REQUEST_TYPE_KEY, mNetworkRequestType);

        // Sends a broadcast to either the MovieGridFragment or MovieDetailFragment
        sendBroadcast(resultsIntent);
    }

    /**
     * Called when an API request has failed.
     * @param error The RetrofitError object containing error messages and other useful error info.
     */
    private void onNetworkFailure(RetrofitError error){
        Log.e(TAG, error.getUrl() + " "
                + error.getMessage());
        setGridFragmentProgressWheelVisibility(HIDE_PROGRESS_WHEEL);
        setDetailFragmentProgressWheelVisibility(HIDE_PROGRESS_WHEEL);

        Toast.makeText(NetworkService.this,
                getString(R.string.toast_msg_network_error),
                Toast.LENGTH_SHORT).show();
    }

    private void setGridFragmentProgressWheelVisibility(@ProgressWheelStatus int visibility){
        Intent progressWheelIntent = new Intent((MovieGridFragment.GridProgressReceiver.INTENT_ACTION));
        progressWheelIntent.putExtra(PROGRESS_KEY, visibility);
        sendBroadcast(progressWheelIntent);
    }

    private void setDetailFragmentProgressWheelVisibility(@ProgressWheelStatus int visibility){
        Intent progressWheelIntent = new Intent((MovieDetailFragment.DetailProgressReceiver.INTENT_ACTION));
        progressWheelIntent.putExtra(PROGRESS_KEY, visibility);
        sendBroadcast(progressWheelIntent);
    }

}
