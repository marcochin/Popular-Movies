package com.mcochin.popularmovies.adapters;

import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.mcochin.popularmovies.R;
import com.mcochin.popularmovies.constants.AdapterViewType;
import com.mcochin.popularmovies.pojo.Review;
import com.mcochin.popularmovies.pojo.Trailer;

import java.util.List;

/**
 * This is the adapter for the review list recyclerView in MovieDetailFragment
 */
public class ReviewListAdapter extends RecyclerView.Adapter<ReviewListAdapter.ReviewItemHolder>{
    private List<Review> mReviewList;

    public static class ReviewItemHolder extends RecyclerView.ViewHolder{
        TextView mAuthor;
        TextView mContent;

        public ReviewItemHolder(View itemView) {
            super(itemView);

            mAuthor = (TextView)itemView.findViewById(R.id.review_author_textview);
            mContent = (TextView)itemView.findViewById(R.id.review_content_textview);
        }
    }

    public ReviewListAdapter(List<Review> reviewList){
        mReviewList = reviewList;
    }

    @Override
    public ReviewItemHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View v;

        if(viewType == AdapterViewType.FIRST_ITEM){
            v = inflater.inflate(R.layout.list_item_review_first, parent, false);
        } else {
            v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.list_item_review_other, parent, false);
        }

        return new ReviewItemHolder(v);
    }

    @Override
    public void onBindViewHolder(ReviewItemHolder holder, int position) {
        Review review = mReviewList.get(position);
        holder.mAuthor.setText(review.getAuthor());
        holder.mContent.setText(review.getContent());
    }

    @Override
    public int getItemCount() {
        return mReviewList.size();
    }

    @Override
    public int getItemViewType(int position) {
        return position == 0 ? AdapterViewType.FIRST_ITEM : AdapterViewType.OTHER_ITEM;
    }

    public void setReviewList(List<Review> reviewList){
        mReviewList = reviewList;
        notifyDataSetChanged();
    }
}
