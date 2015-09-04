package com.mcochin.popularmovies.custom;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.widget.ImageView;

/**
 * Custom ImageView so that it is the optimal height for a movie poster
 */
public class MoviePosterGridItemImageView extends ImageView {
    public MoviePosterGridItemImageView(Context context) {
        super(context);
    }

    public MoviePosterGridItemImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public MoviePosterGridItemImageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public MoviePosterGridItemImageView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        //The height should be 33% longer than the width.
        float newMeasuredHeight = getMeasuredWidth() / (2f/3);
        setMeasuredDimension(getMeasuredWidth(), (int)newMeasuredHeight);
    }
}
