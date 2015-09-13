package com.mcochin.popularmovies.fragments;

import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.mcochin.popularmovies.MovieDetailActivity;
import com.mcochin.popularmovies.R;
import com.mcochin.popularmovies.adapters.ReviewListAdapter;
import com.mcochin.popularmovies.adapters.TrailerListAdapter;
import com.mcochin.popularmovies.pojo.Movie;
import com.mcochin.popularmovies.pojo.MovieExtraResults;
import com.mcochin.popularmovies.pojo.Review;
import com.mcochin.popularmovies.pojo.Trailer;
import com.mcochin.popularmovies.services.NetworkService;
import com.squareup.picasso.Picasso;

import org.solovyev.android.views.llm.DividerItemDecoration;
import org.solovyev.android.views.llm.LinearLayoutManager;

import java.util.ArrayList;
import java.util.List;

/**
 * Fragment that displays the details of the movie a user has clicked on
 */
public class MovieDetailFragment extends Fragment implements TrailerListAdapter.Callback{
    public static final String TAG = MovieDetailActivity.class.getSimpleName();
    public static final int ERROR = -1;

    private static final String DATE_SPLIT_TOKEN = "-";
    private static final String TRAILER_LIST_KEY = "trailerList";
    private static final String REVIEW_LIST_KEY = "reviewList";
    private static final String RUNTIME_KEY = "runTime";
    public static final int RESULT_REMOVE_FAVORITE = 0;
    public static final int RESULT_ADD_FAVORITE = 0;

    private View mProgressWheel;
    private TextView mRuntimeTextView;
    private TrailerListAdapter mTrailerListAdapter;
    private ReviewListAdapter mReviewListAdapter;
    private List<Trailer> mTrailerList;
    private List<Review> mReviewList;
    private Movie mMovie;

    private Button mFavoriteButton;
    private boolean mFavorite;

    private MovieGridFragment mMovieGridFragment;
    private DetailBroadcastReceiver mDetailBroadcastReceiver;
    private DetailProgressReceiver mDetailProgressReceiver;

    /**
     * Broadcast receiver that receives responses from the Network Service after it has done some
     * network operations. This broadcast receiver is in charge of runtime, trailers, and reviews.
     */
    public class DetailBroadcastReceiver extends BroadcastReceiver {
        public static final String INTENT_ACTION
                = "com.mcochin.popularmovies.fragments.MovieGridFragment$DetailBroadcastReceiver";

        @Override
        public void onReceive(Context context, Intent intent) {
            //Send the movieList to the MovieGridFragment to display
            MovieExtraResults results = intent.getParcelableExtra(NetworkService.RESULTS_KEY);
            mRuntimeTextView.setText(getString(R.string.minutes_runtime,
                    Integer.toString(results.getRuntime())));

            mTrailerList = results.getTrailerList().getTrailers();
            mTrailerListAdapter.setTrailerList(mTrailerList);

            mReviewList = results.getReviewList().getReviews();
            mReviewListAdapter.setReviewList(mReviewList);
        }
    }

    /**
     * This broadcast receiver is in charge of showing and hiding the progress wheel.
     */
    public class DetailProgressReceiver extends BroadcastReceiver {
        public static final String INTENT_ACTION
                = "com.mcochin.popularmovies.fragments.MovieGridFragment$DetailProgressReceiver";

        @Override
        public void onReceive(Context context, Intent intent) {
            int progressVisibility = intent.getIntExtra(NetworkService.PROGRESS_KEY, ERROR);
            if(progressVisibility == NetworkService.HIDE_PROGRESS_WHEEL){
                mProgressWheel.setVisibility(View.INVISIBLE);
            } else {
                mProgressWheel.setVisibility(View.VISIBLE);
            }
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //register receiver to receive responses from NetworkService
        mDetailBroadcastReceiver = new DetailBroadcastReceiver();
        getActivity().registerReceiver(mDetailBroadcastReceiver,
                new IntentFilter(DetailBroadcastReceiver.INTENT_ACTION));

        mDetailProgressReceiver = new DetailProgressReceiver();
        getActivity().registerReceiver(mDetailProgressReceiver,
                new IntentFilter(DetailProgressReceiver.INTENT_ACTION));

        //Used to determine if two pane or not
        mMovieGridFragment = (MovieGridFragment)getActivity().getSupportFragmentManager()
                        .findFragmentById(R.id.fragment_movie_grid);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_movie_detail, container, false);
    }

    @Override
    public void onViewCreated(View v, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(v, savedInstanceState);

        // Retrieve the movie that was passed in from the GridFragment
        mMovie = getArguments().getParcelable(Movie.MOVIE_KEY);

        // Setup TextViews, Buttons,and ProgressWheel
        ((TextView) v.findViewById(R.id.movie_title_textview)).setText(mMovie.getOriginalTitle());
        ((TextView) v.findViewById(R.id.movie_plot_textview)).setText(mMovie.getOverview());
        mRuntimeTextView = (TextView)v.findViewById(R.id.movie_runtime_textview);
        mProgressWheel = v.findViewById(R.id.progress_wheel);

        ((TextView) v.findViewById(R.id.movie_date_textview))
                .setText(getYearFromReleaseDate(mMovie.getReleaseDate()));

        ((TextView) v.findViewById(R.id.movie_rating_textview))
                .setText(getString(R.string.rating_out_of_10,
                        Float.toString(mMovie.getVoteAverage())));

        // Add CLickListener to Favorite Button
        mFavoriteButton = (Button)v.findViewById(R.id.favorite_button);
        mFavoriteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mFavorite){
                    removeMovieFromFavorites();
                    showAddToFavoritesButton();
                } else {
                    addMovieToFavorites();
                    showRemoveFromFavoritesButton();
                }
            }
        });

        // If the movie shown is already in favorites change the "Add to Favorites" button to
        // "Remove from Favorites" button.
        SharedPreferences sharedPreferences = getActivity().getSharedPreferences(MovieGridFragment.SHARED_PREFS_NAME, Context.MODE_PRIVATE);
        if(sharedPreferences.contains(Integer.toString(mMovie.getId()))){
            showRemoveFromFavoritesButton();
        }

        // Retrieve Lists from savedInstanceState else create a new one
        if(savedInstanceState == null) {
            mTrailerList = new ArrayList<>();
            mReviewList = new ArrayList<>();
        } else {
            mTrailerList = savedInstanceState.getParcelableArrayList(TRAILER_LIST_KEY);
            mReviewList = savedInstanceState.getParcelableArrayList(REVIEW_LIST_KEY);
            mRuntimeTextView.setText(savedInstanceState.getString(RUNTIME_KEY));
        }

        // Setup Trailers Recycler View
        RecyclerView trailersRecyclerView = (RecyclerView)v.findViewById(R.id.trailers_recyclerview);
        mTrailerListAdapter = new TrailerListAdapter(mTrailerList);
        mTrailerListAdapter.setCallback(this);
        trailersRecyclerView.setAdapter(mTrailerListAdapter);
        trailersRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        trailersRecyclerView.addItemDecoration(new DividerItemDecoration(getActivity(), null));

        // Setup Reviews RecyclerView
        RecyclerView reviewsRecyclerView = (RecyclerView)v.findViewById(R.id.reviews_recyclerview);
        mReviewListAdapter = new ReviewListAdapter(mReviewList);
        reviewsRecyclerView.setAdapter(mReviewListAdapter);
        reviewsRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        // Set the poster image of the movie
        ImageView posterImageView = (ImageView) v.findViewById(R.id.movie_poster_imageview);
        Picasso.with(getActivity()).load(mMovie.getPosterPath())
                .error(R.drawable.movie_poster_error)
                .into(posterImageView);
        posterImageView.setContentDescription(mMovie.getOriginalTitle());

        // Start an api request for the runtime, trailers and reviews if first time
        if (savedInstanceState == null) {
            requestNetworkFeedWithId(NetworkService.MOVIE_EXTRAS, mMovie.getId());
        }
    }

    @Override
    public void onTrailerItemClick(Trailer trailer) {
        watchYoutubeVideo(trailer.getSource());
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(RUNTIME_KEY, mRuntimeTextView.getText().toString());

        outState.putParcelableArrayList(TRAILER_LIST_KEY,
                (ArrayList<? extends Parcelable>) mTrailerList);

        outState.putParcelableArrayList(REVIEW_LIST_KEY,
                (ArrayList<? extends Parcelable>) mReviewList);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        getActivity().unregisterReceiver(mDetailBroadcastReceiver);
        getActivity().unregisterReceiver(mDetailProgressReceiver);
    }

    /**
     * Performs an api request that requires a movieId to be able to retrieve data.
     * @param requestType The api operation you want the NetworkService to perform
     * @param movieId Id of the movie
     */
    private void requestNetworkFeedWithId(@NetworkService.NetworkRequestTypeWithId int requestType,
                                          int movieId) {
        Intent networkIntent = new Intent(getActivity(), NetworkService.class);
        networkIntent.putExtra(NetworkService.REQUEST_TYPE_KEY, requestType);
        networkIntent.putExtra(NetworkService.MOVIE_ID_KEY, movieId);
        getActivity().startService(networkIntent);
    }

    /**
     * Adds favorite movie to shared preferences.
     */
    private void addMovieToFavorites(){
        int movieId = mMovie.getId();

        SharedPreferences prefs = getActivity()
                .getSharedPreferences(MovieGridFragment.SHARED_PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putInt(Integer.toString(movieId), movieId);
        editor.apply();

        if(getActivity().getTitle().equals(getString(R.string.title_favorite_movies))){
            if(mMovieGridFragment != null){
                requestNetworkFeedWithId(NetworkService.MOVIE_FAVORITES_SINGLE, movieId);
            } else {
                getActivity().setResult(RESULT_ADD_FAVORITE);
            }
        }

        Toast.makeText(getActivity(),
                getString(R.string.toast_msg_added_to_favorites),
                Toast.LENGTH_SHORT).show();
    }

    /**
     * Removes favorite movie from shared preferences.
     */
    private void removeMovieFromFavorites(){
        int movieId = mMovie.getId();
        int position = getArguments().getInt(MovieGridFragment.GRID_POSITION_KEY);

        SharedPreferences prefs = getActivity()
                .getSharedPreferences(MovieGridFragment.SHARED_PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.remove(Integer.toString(movieId));
        editor.apply();

        if(getActivity().getTitle().equals(getString(R.string.title_favorite_movies))) {
            if(mMovieGridFragment != null){
                mMovieGridFragment.removeFavoriteMovie(position);
            } else {
                Intent removeFavoriteIntent = new Intent();
                removeFavoriteIntent.putExtra(MovieGridFragment.GRID_POSITION_KEY, position);

                getActivity().setResult(RESULT_REMOVE_FAVORITE, removeFavoriteIntent);
            }
        }

        Toast.makeText(getActivity(),
                getString(R.string.toast_msg_removed_from_favorites),
                Toast.LENGTH_SHORT).show();
    }

    /**
     * Launches an intent to watch the YouTube video
     * @param id The id of the YouTube video
     */
    private void watchYoutubeVideo(String id){
        try{
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("vnd.youtube:" + id));
            startActivity(intent);
        }catch (ActivityNotFoundException ex){
            Intent intent=new Intent(Intent.ACTION_VIEW,
                    Uri.parse("http://www.youtube.com/watch?v="+id));
            startActivity(intent);
        }
    }

    /**
     * Helper method to help extract the year from the date. This only works if the date is in
     * yyyy-MM-dd format or something similar, as long as the year is first and followed by a "-".
     * @param date The date to extract the year from.
     * @return The extracted year.
     */
    private static String getYearFromReleaseDate(String date){
        return date == null || date.isEmpty()? "" : date.split(DATE_SPLIT_TOKEN)[0];
    }

    private void showAddToFavoritesButton(){
        mFavorite = false;
        mFavoriteButton.setBackgroundResource(R.drawable.favorite_button_add);
        mFavoriteButton.setText(getString(R.string.text_favorite_button_add));
    }

    private void showRemoveFromFavoritesButton(){
        mFavorite = true;
        mFavoriteButton.setBackgroundResource(R.drawable.favorite_button_remove);
        mFavoriteButton.setText(getString(R.string.text_favorite_button_remove));
    }
}
