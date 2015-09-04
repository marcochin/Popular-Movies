package com.mcochin.popularmovies.fragments;

import android.graphics.Point;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
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
import com.mcochin.popularmovies.custom.AutoFitRecyclerView;
import com.mcochin.popularmovies.pojo.Movie;

import java.util.ArrayList;
import java.util.List;

/**
 * Fragment for showing a grid of movie posters
 */
public class MovieGridFragment extends Fragment implements MovieGridAdapter.Callback {
    private static final String TAG = MovieGridFragment.class.getSimpleName();
    private static final String MOVIE_LIST_KEY = "movieList";

    private MovieGridAdapter mMovieGridAdapter;
    private AutoFitRecyclerView mRecyclerView;
    private Callback mCallback;
    private List<Movie> mMovieList;
    private View mProgressWheel;

    // Slop constant for this device
    private int mTouchSlop;

    // Initial touch point
    private Point mInitialTouch;

    // Flag to keep track if the poster is in a pressed state or not
    // Used to optimize code for onItemTouch()'s ACTION_MOVE
    private boolean mPosterScaledNormal = true;

    // The current touched grid item of interest. We retain a reference so we can cancel the
    // animation when we detect SCROLL_STATE_DRAGGING
    private View mCurrentGridItemView;

    public interface Callback{
        void onMovieItemClick(Movie movie);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Add this line in order for this fragment to handle menu events.
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_main, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        NetworkFragment networkFragment = (NetworkFragment) getActivity()
                .getSupportFragmentManager().findFragmentByTag(NetworkFragment.TAG);

        switch (id){
            case R.id.action_most_popular:
                networkFragment.loadPopularMovies();
                break;
            case R.id.action_highest_rated:
                networkFragment.loadHighestRatedMovies();
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

        mTouchSlop = ViewConfiguration.get(getContext()).getScaledTouchSlop();
        mInitialTouch = new Point();
        mMovieList = new ArrayList<>();

        //GridLayoutManager is set in the constructor of the AutoFitRecyclerView
        mRecyclerView = (AutoFitRecyclerView)view.findViewById(R.id.poster_grid_recyclerview);
        mProgressWheel = view.findViewById(R.id.progress_wheel);

        mMovieGridAdapter = new MovieGridAdapter(getContext(), mMovieList);
        mMovieGridAdapter.setCallback(this);
        mRecyclerView.setAdapter(mMovieGridAdapter);

        //Use the already loaded movie list on rotation
        if(savedInstanceState != null) {
            mMovieList = savedInstanceState.getParcelableArrayList(MOVIE_LIST_KEY);
            if (mMovieList != null && !mMovieList.isEmpty()) {
                mMovieGridAdapter.setMovieList(mMovieList);
            }
        }

        mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                if (newState == RecyclerView.SCROLL_STATE_DRAGGING) {
                    mCurrentGridItemView.clearAnimation();
                    mPosterScaledNormal = true;
                }
            }
        });
    }

    //onItemFocusChange and onItemTouch are callbacks for when an item is selected
    @Override
    public void onItemFocusChange(View v, boolean hasFocus) {
        Animation anim;

        if (hasFocus) {
            anim = AnimationUtils.loadAnimation(getContext(), R.anim.poster_scale_down);
        } else {
            anim = AnimationUtils.loadAnimation(getContext(), R.anim.poster_scale_normal);
        }
        v.startAnimation(anim);
    }

    //This method is to scale the grid item down and up for better touch feedback
    @Override
    public boolean onItemTouch(View v, MotionEvent event, Movie movie) {
        Animation anim;
        int action = event.getAction();

        switch (action){
            case MotionEvent.ACTION_DOWN:
                mCurrentGridItemView = v;
                mInitialTouch.set((int) event.getX(), (int) event.getY());

                anim = AnimationUtils.loadAnimation(getContext(), R.anim.poster_scale_down);
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
                    anim = AnimationUtils.loadAnimation(getContext(), R.anim.poster_scale_normal);
                    v.startAnimation(anim);

                    if(mCallback != null){
                        mCallback.onMovieItemClick(movie);
                    }
                    mPosterScaledNormal = true;
                }
                break;
        }
        return false;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putParcelableArrayList(MOVIE_LIST_KEY,
                (ArrayList<? extends Parcelable>) mMovieList);
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onDestroy() {
        if(mRecyclerView != null){
            mRecyclerView.removeOnScrollListener(null);
        }

        super.onDestroy();
    }

    public void setMovieList(List<Movie> movieList){
        mMovieList = movieList;
        mMovieGridAdapter.setMovieList(mMovieList);
    }

    public void showProgressWheel(){
        mProgressWheel.setVisibility(View.VISIBLE);
    }

    public void hideProgressWheel(){
        mProgressWheel.setVisibility(View.INVISIBLE);
    }

    public void showNetworkErrorToast(){
        Toast.makeText(getContext(),
                getString(R.string.network_error_msg),
                Toast.LENGTH_SHORT).show();
    }

    public void setCallback(Callback callback){
        mCallback = callback;
    }


}
