package com.mcochin.popularmovies.pojo;

import java.util.List;

/**
 * This class will hold the list of movies returned as a result from a JSON call
 */
public class JsonResults {
    private List<Movie> results;

    public List<Movie> getResults() {
        return results;
    }

    public void setResults(List<Movie> results) {
        this.results = results;
    }
}
