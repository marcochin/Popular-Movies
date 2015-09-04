package com.mcochin.popularmovies.retrofit;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mcochin.popularmovies.pojo.Movie;
import com.mcochin.popularmovies.pojo.JsonResults;

import java.util.List;

import retrofit.Callback;
import retrofit.RestAdapter;
import retrofit.converter.GsonConverter;
import retrofit.http.GET;
import retrofit.http.Query;

/**
 * This class uses the Retrofit REST Library to connect to themoviedb.org API
 */
public class TheMovieDbApiProvider {
    private TheMovieDbApi mTheMovieDbApi;

    /**
     * This Interface is used by the Retrofit RestAdapter to connect you to the JSON feeds.
     * Specify your desired feeds here!
     */
    public interface TheMovieDbApi {
        String THE_MOVIE_DB_ENDPOINT ="https://api.themoviedb.org/3/discover";
        String MOVIE_ENDPOINT = "/movie?sort_by=";
        String POPULAR_MOVIE_QUERY = MOVIE_ENDPOINT + "popularity.desc";
        String HIGHEST_RATED_QUERY = MOVIE_ENDPOINT + "vote_average.desc";
        String API_KEY = "api_key";

        @GET(POPULAR_MOVIE_QUERY)
        void getMostPopular(@Query(API_KEY)String apiKey, Callback<JsonResults> response);

        @GET(HIGHEST_RATED_QUERY)
        void getHighestRated(@Query(API_KEY)String apiKey, Callback<JsonResults> response);
    }

    public TheMovieDbApiProvider(){
        // Gson instance that will convert all fields from lower case with underscores to camel case
        // and vice versa.
        Gson gson = new GsonBuilder()
                .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
                .create();

        //Set up a class that will route our code to the api!
        RestAdapter mRestAdapter = new RestAdapter.Builder()
                .setLogLevel(RestAdapter.LogLevel.FULL)
                .setEndpoint(TheMovieDbApi.THE_MOVIE_DB_ENDPOINT)
                .setConverter(new GsonConverter(gson))
                .build();

        mTheMovieDbApi = mRestAdapter.create(TheMovieDbApi.class);
    }

    public TheMovieDbApi getApi(){
        return mTheMovieDbApi;
    }
}
