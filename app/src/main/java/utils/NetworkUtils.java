package utils;

import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Scanner;

import javax.net.ssl.HttpsURLConnection;

import ipomoea.popularmovies.BuildConfig;

public class NetworkUtils {

    private final static String BASE_URL = "https://api.themoviedb.org/3/";
    private final static String PATH_POPULAR = "movie/popular?";
    private final static String PATH_TOP_RATED = "movie/top_rated?";
    private final static String PARAM_API_KEY = "api_key=";
    private final static String API_KEY = BuildConfig.API_KEY;
    private final static String PARAM_MOVIE = "movie/";
    private final static String PARAM_REVIEWS = "/reviews?";
    private final static String PARAM_VIDEOS = "/videos?";

    public static URL createUrlToMovies(int selectedSortOrder) {

        URL url = null;

        String sortOrderPath;
        if (selectedSortOrder == 0) {
            sortOrderPath = PATH_POPULAR;
        } else {
            sortOrderPath = PATH_TOP_RATED;
        }

        try {
            url = new URL(BASE_URL + sortOrderPath + PARAM_API_KEY + API_KEY);
        } catch (MalformedURLException e) {
            Log.e("NetworkUtils", "Error while creating URL", e);
        }
        return url;
    }

    public static URL createUrlToReviews(int movieID) {

        URL url = null;

        try {
            url = new URL(BASE_URL + PARAM_MOVIE + movieID + PARAM_REVIEWS + PARAM_API_KEY + API_KEY);
        } catch (MalformedURLException e) {
            Log.e("NetworkUtils", "Error while creating URL", e);
        }
        return url;
    }

    public static URL createUrlToVideos(int movieID) {

        URL url = null;

        try {
            url = new URL(BASE_URL + PARAM_MOVIE + movieID + PARAM_VIDEOS + PARAM_API_KEY + API_KEY);
        } catch (MalformedURLException e) {
            Log.e("NetworkUtils", "Error while creating URL", e);
        }
        return url;
    }

    public static String getResponseFromHttpUrl(URL url) throws IOException {

        HttpsURLConnection urlConnection = (HttpsURLConnection) url.openConnection();

        try {
            InputStream inputStream = urlConnection.getInputStream();

            Scanner scanner = new Scanner(inputStream);
            scanner.useDelimiter("\\A");

            boolean hasInput = scanner.hasNext();
            if (hasInput) {
                return scanner.next();
            } else {
                return null;
            }
        } finally {
            urlConnection.disconnect();
        }
    }
}
