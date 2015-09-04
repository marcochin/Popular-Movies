package com.mcochin.popularmovies.fragments;

import android.os.Bundle;
import android.support.annotation.IntDef;
import android.support.v4.app.Fragment;
import android.util.Log;

import com.mcochin.popularmovies.R;
import com.mcochin.popularmovies.pojo.JsonResults;
import com.mcochin.popularmovies.retrofit.TheMovieDbApiProvider;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

/**
 * UI-less Network fragment that is retained across orientation changes that processes API calls.
 */
public class NetworkFragment extends Fragment implements Callback<JsonResults>{
    public static final String TAG = NetworkFragment.class.getSimpleName();

    private Callback mCallback;
    private TheMovieDbApiProvider.TheMovieDbApi mTheMovieDbApi;
    private MovieGridFragment mMovieGridFragment;
    private int mMovieSortType;

    // We use these SORT constants to let us know what data we are requesting
    // so the main activity can change the title to reflect the current content
    @IntDef({SORT_POPULAR, SORT_HIGHEST_RATED})
    @Retention(RetentionPolicy.SOURCE)
    public @interface MovieSortType {}

    public static final int SORT_POPULAR = 0;
    public static final int SORT_HIGHEST_RATED = 1;

    /**
     * Callback to let a implementer know if Network operation succeeded or failed.
     */
    public interface Callback{
        void onNetworkSuccess(JsonResults jsonResults, Response response, @MovieSortType int sortType);
        void onNetworkFailure(RetrofitError error);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);

        mTheMovieDbApi = new TheMovieDbApiProvider().getApi();
        mMovieGridFragment = (MovieGridFragment) getActivity().getSupportFragmentManager()
                .findFragmentById(R.id.fragment_movie_grid);
    }

    @Override
    public void onDetach() {
        mCallback = null;
        super.onDetach();
    }

    @Override
    public void success(JsonResults jsonResults, Response response) {
        mMovieGridFragment.hideProgressWheel();

        if (mCallback != null) {
            mCallback.onNetworkSuccess(jsonResults, response, mMovieSortType);
        }
    }

    @Override
    public void failure(RetrofitError error) {
        mMovieGridFragment.hideProgressWheel();
        mMovieGridFragment.showNetworkErrorToast();

        Log.e(TAG, error.getUrl() + " "
                + error.getMessage());

        if (mCallback != null) {
            mCallback.onNetworkFailure(error);
        }
    }

    /**
     * Makes a call to the themoviedb.org API and fetches popular movies
     */
    public void loadPopularMovies(){
        mMovieSortType = SORT_POPULAR;
        mMovieGridFragment.showProgressWheel();
        mTheMovieDbApi.getMostPopular(getString(R.string.themoviedb_api_key), this);
    }

    /**
     * Makes a call to the themoviedb.org API and fetches highest rated movies
     */
    public void loadHighestRatedMovies(){
        mMovieSortType = SORT_HIGHEST_RATED;
        mMovieGridFragment.showProgressWheel();
        mTheMovieDbApi.getHighestRated(getString(R.string.themoviedb_api_key), this);
    }

    public int getSortType(){
        return mMovieSortType;
    }

    public void setCallback(Callback callback){
        mCallback = callback;
    }
}
