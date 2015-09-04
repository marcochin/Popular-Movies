package com.mcochin.popularmovies.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.mcochin.popularmovies.R;
import com.mcochin.popularmovies.pojo.Movie;
import com.squareup.picasso.Picasso;

import java.util.List;

/**
 * The adapter class to the AutoFitRecyclerView in MovieGridFragment.java
 */
public class MovieGridAdapter extends RecyclerView.Adapter<MovieGridAdapter.MoviePosterHolder>{
    private Context mContext;
    private List<Movie> mMovieList;
    private Callback mCallback;

    /**
     * This Callback is for communicating when an item has been touched
     */
    public interface Callback {
        void onItemFocusChange(View v, boolean hasFocus);
        boolean onItemTouch(View v, MotionEvent event, Movie movie);
    }

    public static class MoviePosterHolder extends RecyclerView.ViewHolder{
        ImageView moviePoster;

        public MoviePosterHolder(View itemView) {
            super(itemView);
            moviePoster = (ImageView)itemView.findViewById(R.id.movie_poster_imageview);
        }
    }

    public MovieGridAdapter(Context context, List<Movie> movieList){
        mContext = context;
        mMovieList = movieList;
    }

    @Override
    public MoviePosterHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.grid_item_movie, parent, false);
        return new MoviePosterHolder(v);
    }

    @Override
    public void onBindViewHolder(MoviePosterHolder holder, int position) {
        final Movie movie = mMovieList.get(position);

        Picasso.with(mContext).load(movie.getPosterPath())
                .error(R.drawable.movie_poster_error)
                .into(holder.moviePoster);
        holder.moviePoster.setContentDescription(movie.getOriginalTitle());

        if(mCallback != null) {
            holder.itemView.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    return mCallback.onItemTouch(v, event, movie);

                }
            });

            holder.itemView.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                @Override
                public void onFocusChange(View v, boolean hasFocus) {
                    mCallback.onItemFocusChange(v, hasFocus);
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return mMovieList.size();
    }

    public void setMovieList(List<Movie> movieList){
        mMovieList = movieList;
        notifyDataSetChanged();
    }

    public void setCallback(Callback callback){
        mCallback = callback;
    }
}
