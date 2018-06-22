package utils;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import model.Movie;

public class JsonParser {

    private static final String RESULTS = "results";
    private static final String ID = "id";
    private static final String ORIGINAL_TITLE = "original_title";
    private static final String POSTER_PATH = "poster_path";
    private static final String BACKDROP_PATH = "backdrop_path";
    private static final String OVERVIEW = "overview";
    private static final String VOTE_AVERAGE = "vote_average";
    private static final String RELEASE_DATE = "release_date";
    private static final String CONTENT = "content";
    private static final String KEY = "key";

    public static List<Movie> extractFeatureFromMoviesJson(String moviesJSON) {

        List<Movie> movies = new ArrayList<>();

        int id = 0;
        double voteAverage = 0;
        String originalTitle = "";
        String posterPath = "";
        String backdropPath = "";
        String overview = "";
        String releaseDate = "";

        try {

            JSONObject rootJsonObject = new JSONObject(moviesJSON);
            JSONArray resultsJsonArray = rootJsonObject.getJSONArray(RESULTS);

            for (int i = 0; i < resultsJsonArray.length(); i++) {
                JSONObject results = resultsJsonArray.getJSONObject(i);

                if (results.has(ID)) {
                    id = results.optInt(ID);
                }

                if (results.has(ORIGINAL_TITLE)) {
                    originalTitle = results.optString(ORIGINAL_TITLE);
                }

                if (results.has(POSTER_PATH)) {
                    posterPath = results.optString(POSTER_PATH);
                }

                if (results.has(BACKDROP_PATH)) {
                    backdropPath = results.optString(BACKDROP_PATH);
                }

                if (results.has(OVERVIEW)) {
                    overview = results.optString(OVERVIEW);
                }

                if (results.has(VOTE_AVERAGE)) {
                    voteAverage = results.optDouble(VOTE_AVERAGE);
                }

                if (results.has(RELEASE_DATE)) {
                    releaseDate = results.optString(RELEASE_DATE);
                }

                Movie movie = new Movie(id, originalTitle, posterPath, backdropPath, overview, voteAverage, releaseDate);
                movies.add(movie);
            }

        } catch (JSONException e) {
            Log.e("JsonParser", "An error occurred with JSON parsing", e);
        }

        return movies;
    }

    public static List<String> extractFeatureFromReviewsJson(String reviewsJSON) {

        List<String> reviews = new ArrayList<>();

        String content = "";

        try {

            JSONObject rootJsonObject = new JSONObject(reviewsJSON);
            JSONArray resultsJsonArray = rootJsonObject.getJSONArray(RESULTS);

            for (int i = 0; i < resultsJsonArray.length(); i++) {
                JSONObject results = resultsJsonArray.getJSONObject(i);

                if (results.has(CONTENT)) {
                    content = results.optString(CONTENT);
                }

                reviews.add(content);
            }

        } catch (JSONException e) {
            Log.e("JsonParser", "An error occurred with JSON parsing", e);
        }

        return reviews;
    }

    public static List<String> extractFeatureFromVideosJson(String videosJSON) {

        List<String> videos = new ArrayList<>();

        String videoKey = "";

        try {

            JSONObject rootJsonObject = new JSONObject(videosJSON);
            JSONArray resultsJsonArray = rootJsonObject.getJSONArray(RESULTS);

            for (int i = 0; i < resultsJsonArray.length(); i++) {
                JSONObject results = resultsJsonArray.getJSONObject(i);

                if (results.has(KEY)) {
                    videoKey = results.optString(KEY);
                }

                videos.add(videoKey);
            }

        } catch (JSONException e) {
            Log.e("JsonParser", "An error occurred with JSON parsing", e);
        }

        return videos;
    }
}
