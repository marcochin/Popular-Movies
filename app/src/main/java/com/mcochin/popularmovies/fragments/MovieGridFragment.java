package com.mcochin.popularmovies.fragments;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Point;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Toast;

import com.mcochin.popularmovies.R;
import com.mcochin.popularmovies.adapters.MovieGridAdapter;
import com.mcochin.popularmovies.custom.AutoFitGridRecyclerView;
import com.mcochin.popularmovies.interfaces.Results;
import com.mcochin.popularmovies.pojo.Movie;
import com.mcochin.popularmovies.pojo.MovieResults;
import com.mcochin.popularmovies.services.NetworkService;

import java.util.ArrayList;
import java.util.List;

/**
 * Fragment for showing a grid of movie posters
 */
public class MovieGridFragment extends Fragment
        implements MovieGridAdapter.Callback{
    private static final String TAG = MovieGridFragment.class.getSimpleName();
    private static final String MOVIE_LIST_KEY = "movieList";
    public static final String SHARED_PREFS_NAME = "Favorite Movies";
    public static final String GRID_POSITION_KEY = "gridPosition";

    public static final int ERROR = -1;

    private MovieGridAdapter mMovieGridAdapter;
    private AutoFitGridRecyclerView mRecyclerView;
    private Callback mCallback;
    private View mProgressWheel;
    private GridBroadcastReceiver mGridBroadcastReceiver;
    private GridProgressReceiver mGridProgressReceiver;
    private List<Movie> mMovieList;

    // Flag to notify if the user pressed "Favorites" from the menu, so we can refresh the grid
    private boolean mFavoritesMenuClicked;

    // Slop constant for this device
    private int mTouchSlop;

    // Initial touch point. Used to determine touch slop range.
    private Point mInitialTouch;

    // Flag to keep track if the poster is in a pressed state or not
    // Used to optimize code for onMovieItemTouch()'s ACTION_MOVE
    private boolean mPosterScaledNormal = true;

    // The current touched grid item of interest. We retain a reference so we can cancel the
    // animation when we detect SCROLL_STATE_DRAGGING
    private View mCurrentGridItemView;

    public interface Callback{
        void onMovieItemClick(Movie movie, int position);
    }

    /**
     * Broadcast receiver that receives responses from the Network Service after it has done some
     * network operations. This broadcast receiver is in charge of displaying popular,
     * highest rated and favorite movies.
     */
    public class GridBroadcastReceiver extends BroadcastReceiver{
        public static final String INTENT_ACTION
                = "com.mcochin.popularmovies.fragments.MovieGridFragment$GridBroadcastReceiver";

        @Override
        public void onReceive(Context context, Intent intent) {
            // Send the movieList to the MovieGridFragment to display
            Results results = intent.getParcelableExtra(NetworkService.RESULTS_KEY);
            String title = "";

            if (results instanceof MovieResults){
                // Display highest rated or most popular movies
                mMovieList = ((MovieResults)intent.getParcelableExtra(NetworkService.RESULTS_KEY))
                        .getMovieList();
                mMovieGridAdapter.setMovieList(mMovieList);

            } else{
                // Display favorite movies
                if (mFavoritesMenuClicked){
                    mFavoritesMenuClicked = false;
                    mMovieList.clear();
                    title = getString(R.string.title_favorite_movies);
                }
                mMovieList.add((Movie)results);

                if(mMovieList.size() == 1) {
                    mMovieGridAdapter.setMovieList(mMovieList);
                } else {
                    mMovieGridAdapter.notifyItemInserted(mMovieList.size() - 1);
                }
            }

            int mNetworkRequestType = intent
                    .getIntExtra(NetworkService.REQUEST_TYPE_KEY, NetworkService.ERROR);

            //Change the title of actionBar corresponding to the request type
            switch(mNetworkRequestType){
                case NetworkService.MOVIE_POPULAR:
                    title = getString(R.string.title_popular_movies);
                    break;
                case NetworkService.MOVIE_HIGHEST_RATED:
                    title = getString(R.string.title_highest_rated_movies);
                    break;
            }

            if(!title.isEmpty()) {
                getActivity().setTitle(title);
            }
        }
    }

    /**
     * This broadcast receiver is in charge of showing and hiding the progress wheel.
     */
    public class GridProgressReceiver extends BroadcastReceiver {
        public static final String INTENT_ACTION
                = "com.mcochin.popularmovies.fragments.MovieGridFragment$GridProgressReceiver";

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
        // Add this line in order for this fragment to handle menu events.
        setHasOptionsMenu(true);

        //register receiver to receive responses from NetworkService
        mGridBroadcastReceiver = new GridBroadcastReceiver();
        getActivity().registerReceiver(mGridBroadcastReceiver,
                new IntentFilter(GridBroadcastReceiver.INTENT_ACTION));

        mGridProgressReceiver = new GridProgressReceiver();
        getActivity().registerReceiver(mGridProgressReceiver,
                new IntentFilter(GridProgressReceiver.INTENT_ACTION));
    }


    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_main, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        switch (id) {
            case R.id.action_most_popular:
                requestNetworkFeed(NetworkService.MOVIE_POPULAR);
                break;
            case R.id.action_highest_rated:
                requestNetworkFeed(NetworkService.MOVIE_HIGHEST_RATED);
                break;
            case R.id.action_favorites:
                mFavoritesMenuClicked = true;
                requestNetworkFeed(NetworkService.MOVIE_FAVORITES);
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_movie_grid, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mTouchSlop = ViewConfiguration.get(getActivity()).getScaledTouchSlop();
        mInitialTouch = new Point();

        // Use the already loaded movie list on rotation, else create a new one
        if(savedInstanceState == null) {
            mMovieList = new ArrayList<>();
        } else {
            mMovieList = savedInstanceState.getParcelableArrayList(MOVIE_LIST_KEY);
        }

        mProgressWheel = view.findViewById(R.id.progress_wheel);

        // Setup grid recyclerView to display movie posters
        // GridLayoutManager is set in the constructor of the AutoFitRecyclerView
        mRecyclerView = (AutoFitGridRecyclerView)view.findViewById(R.id.poster_grid_recyclerview);
        mMovieGridAdapter = new MovieGridAdapter(getActivity(), mMovieList);
        mMovieGridAdapter.setCallback(this);
        mRecyclerView.setAdapter(mMovieGridAdapter);

        // Add a onScrollListener to cancel the button animation on scroll
        mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                if (newState == RecyclerView.SCROLL_STATE_DRAGGING && mCurrentGridItemView != null) {
                    mCurrentGridItemView.clearAnimation();
                    mPosterScaledNormal = true;
                }
            }
        });

        //Load popular movies on first open
        if(savedInstanceState == null) {
            requestNetworkFeed(NetworkService.MOVIE_POPULAR);
        }
    }

    //onMovieItemFocusChange and onMovieItemTouch are callbacks for when an item is selected
    @Override
    public void onMovieItemFocusChange(View v, boolean hasFocus) {
        Animation anim;

        if (hasFocus) {
            anim = AnimationUtils.loadAnimation(getActivity(), R.anim.poster_scale_down);
        } else {
            anim = AnimationUtils.loadAnimation(getActivity(), R.anim.poster_scale_normal);
        }
        v.startAnimation(anim);
    }

    // This method is to scale the grid item down and up for better touch feedback
    @Override
    public boolean onMovieItemTouch(View v, MotionEvent event, Movie movie, RecyclerView.ViewHolder holder) {
        Animation anim;
        int action = event.getAction();

        switch (action){
            case MotionEvent.ACTION_DOWN:
                mCurrentGridItemView = v;
                mInitialTouch.set((int) event.getX(), (int) event.getY());

                anim = AnimationUtils.loadAnimation(getActivity(), R.anim.poster_scale_down);
                v.startAnimation(anim);

                mPosterScaledNormal = false;
                return true;

            case MotionEvent.ACTION_MOVE:
                //slop check to see if touch has moved outside of slop area (click range).
                //if movement is outside "slop area" clear the animation

                if ( Math.abs(event.getX() - mInitialTouch.x) > mTouchSlop
                        || Math.abs(event.getY() - mInitialTouch.y) > mTouchSlop ) {
                    if(!mPosterScaledNormal) {
                        v.clearAnimation();
                        mPosterScaledNormal = true;
                    }
                }
                break;

            case MotionEvent.ACTION_UP:
                if(!mPosterScaledNormal) {
                    anim = AnimationUtils.loadAnimation(getActivity(), R.anim.poster_scale_normal);
                    v.startAnimation(anim);

                    if(mCallback != null){
                        mCallback.onMovieItemClick(movie, holder.getLayoutPosition());
                    }
                    mPosterScaledNormal = true;
                }
                break;
        }
        return false;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelableArrayList(MOVIE_LIST_KEY,
                (ArrayList<? extends Parcelable>) mMovieList);
    }

    @Override
    public void onDestroy() {
        if(mRecyclerView != null){
            mRecyclerView.removeOnScrollListener(null);
        }

        getActivity().unregisterReceiver(mGridBroadcastReceiver);
        getActivity().unregisterReceiver(mGridProgressReceiver);
        super.onDestroy();
    }

    /**
     * Performs an api operation corresponding to a request type (NetworkService.NetworkRequestType)
     */
    public void requestNetworkFeed(@NetworkService.NetworkRequestType int requestType){
        Intent networkIntent = new Intent(getActivity(), NetworkService.class);
        networkIntent.putExtra(NetworkService.REQUEST_TYPE_KEY, requestType);
        getActivity().startService(networkIntent);
    }

    /**
     * This method is used to remove a movie from the favorites list
     * @param position The position in the list to be removed
     */
    public void removeFavoriteMovie(int position){
        mMovieList.remove(position);
        mMovieGridAdapter.notifyItemRemoved(position);
    }

    public void setCallback(Callback callback){
        mCallback = callback;
    }


}
