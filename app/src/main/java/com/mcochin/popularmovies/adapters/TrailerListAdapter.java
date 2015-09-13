package com.mcochin.popularmovies.adapters;

import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.mcochin.popularmovies.R;
import com.mcochin.popularmovies.constants.AdapterViewType;
import com.mcochin.popularmovies.pojo.Movie;
import com.mcochin.popularmovies.pojo.Trailer;

import java.util.List;

/**
 * This is the adapter for the trailer list recyclerView in MovieDetailFragment
 */
public class TrailerListAdapter extends RecyclerView.Adapter<TrailerListAdapter.TrailerItemHolder>{
    private List<Trailer> mTrailerList;
    private Callback mCallback;

    public interface Callback{
        void onTrailerItemClick(Trailer trailer);
    }

    public static class TrailerItemHolder extends RecyclerView.ViewHolder{
        TextView mTrailerName;

        public TrailerItemHolder(View itemView) {
            super(itemView);

            mTrailerName = (TextView)itemView.findViewById(R.id.trailer_name_textview);
        }
    }

    public TrailerListAdapter(List<Trailer> trailerList){
        mTrailerList = trailerList;
    }

    @Override
    public TrailerItemHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View v;

        if(viewType == AdapterViewType.FIRST_ITEM){
            v = inflater.inflate(R.layout.list_item_trailer_first, parent, false);
        } else {
            v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.list_item_trailer, parent, false);
        }
        return new TrailerItemHolder(v);
    }

    @Override
    public void onBindViewHolder(TrailerItemHolder holder, int position) {
        final Trailer trailer = mTrailerList.get(position);
        holder.mTrailerName.setText(trailer.getName());

        holder.mTrailerName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mCallback != null) {
                    mCallback.onTrailerItemClick(trailer);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return mTrailerList.size();
    }

    @Override
    public int getItemViewType(int position) {
        return position == 0 ? AdapterViewType.FIRST_ITEM : AdapterViewType.OTHER_ITEM;
    }

    public void setTrailerList(List<Trailer> trailerList){
        mTrailerList = trailerList;
        notifyDataSetChanged();
    }

    public void setCallback(Callback callback){
        mCallback = callback;
    }
}
