package com.mcochin.popularmovies.retrofit;

import android.util.Log;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mcochin.popularmovies.pojo.Movie;
import com.mcochin.popularmovies.pojo.MovieExtraResults;
import com.mcochin.popularmovies.pojo.MovieResults;

import retrofit.Callback;
import retrofit.RestAdapter;
import retrofit.converter.GsonConverter;
import retrofit.http.GET;
import retrofit.http.Path;
import retrofit.http.Query;

/**
 * This class uses the Retrofit REST Library to connect to themoviedb.org API
 */
public class TheMovieDbApiProvider {
    private static TheMovieDbApiProvider mTheMovieDbProvider;
    private TheMovieDbApi mTheMovieDbApi;

    /**
     * This Interface is used by the Retrofit RestAdapter to connect you to the JSON feeds.
     * Specify your desired feeds here!
     */
    public interface TheMovieDbApi {
        String THE_MOVIE_DB_API_URL ="https://api.themoviedb.org/3";
        String DISCOVER_ENDPOINT ="/discover";
        String MOVIE_ENDPOINT = "/movie";
        String MOVIE_ID = "/{id}";

        String POPULAR_MOVIES_QUERY = DISCOVER_ENDPOINT
                + MOVIE_ENDPOINT + "?sort_by=popularity.desc";

        String HIGHEST_RATED_MOVIES_QUERY = DISCOVER_ENDPOINT
                + MOVIE_ENDPOINT + "?sort_by=vote_average.desc";

        String MOVIE_ITEM_WITH_TRAILERS_AND_REVIEWS_QUERY
                = MOVIE_ENDPOINT +  MOVIE_ID  + "?append_to_response=trailers,reviews";

        String MOVIE_ITEM_QUERY = MOVIE_ENDPOINT + MOVIE_ID;

        String API_KEY = "api_key";

        @GET(POPULAR_MOVIES_QUERY)
        void getMostPopular(@Query(API_KEY)String apiKey, Callback<MovieResults> response);

        @GET(HIGHEST_RATED_MOVIES_QUERY)
        void getHighestRated(@Query(API_KEY)String apiKey, Callback<MovieResults> response);

        @GET(MOVIE_ITEM_WITH_TRAILERS_AND_REVIEWS_QUERY)
        void getMovieExtras(@Query(API_KEY)String apiKey, @Path("id")int movieId, Callback<MovieExtraResults> response);

        @GET(MOVIE_ITEM_QUERY)
        void getFavoriteMovies(@Query(API_KEY)String apiKey, @Path("id")int movieId, Callback<Movie> response);
    }

    private TheMovieDbApiProvider(){
        // Gson instance that will convert all fields from lower case with underscores to camel case
        // and vice versa.
        Gson gson = new GsonBuilder()
                .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
                .create();

        //Set up a class that will route our code to the api!
        RestAdapter mRestAdapter = new RestAdapter.Builder()
                .setLogLevel(RestAdapter.LogLevel.FULL)
                .setEndpoint(TheMovieDbApi.THE_MOVIE_DB_API_URL)
                .setConverter(new GsonConverter(gson))
                .build();

        mTheMovieDbApi = mRestAdapter.create(TheMovieDbApi.class);
    }

    public static TheMovieDbApiProvider getInstance(){
        if(mTheMovieDbProvider == null){
            mTheMovieDbProvider = new TheMovieDbApiProvider();
        }
        return mTheMovieDbProvider;
    }

    public TheMovieDbApi getApi(){
        return mTheMovieDbApi;
    }
}
