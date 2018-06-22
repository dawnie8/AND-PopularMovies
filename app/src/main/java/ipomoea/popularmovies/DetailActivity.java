package ipomoea.popularmovies;

import android.content.ActivityNotFoundException;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import data.FavoriteMoviesContract;
import model.Movie;
import utils.JsonParser;
import utils.NetworkUtils;

public class DetailActivity extends AppCompatActivity {

    private int id;
    private String originalTitle;
    private String posterPath;
    private String backdropPath;
    private String overview;
    private double voteAverage;
    private String releaseDate;

    private Movie movie;
    private List<String> reviews = new ArrayList<>();
    private List<String> videos = new ArrayList<>();
    private CheckBox checkbox;
    private ScrollView scrollView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        ImageView picture_iv = findViewById(R.id.picture);
        TextView title_tv = findViewById(R.id.title);
        TextView rating_tv = findViewById(R.id.rating);
        TextView releaseDate_tv = findViewById(R.id.release_date);
        TextView overview_tv = findViewById(R.id.overview);
        checkbox = findViewById(R.id.checkbox);
        scrollView = findViewById(R.id.scrollView);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Intent intent = getIntent();
        if (intent.hasExtra("selected_movie")) {
            Bundle bundle = intent.getExtras();
            if (bundle != null) {
                movie = bundle.getParcelable("selected_movie");
            }
        }

        id = movie.getId();
        originalTitle = movie.getOriginalTitle();
        posterPath = movie.getPosterPath();
        backdropPath = movie.getBackdropPath();
        overview = movie.getOverview();
        voteAverage = movie.getVoteAverage();
        releaseDate = movie.getReleaseDate();

        Picasso.with(this).load(movie.createBackdropUri()).into(picture_iv);

        title_tv.setText(originalTitle);
        String rating = getString(R.string.rating, String.valueOf(movie.getVoteAverage()));
        rating_tv.setText(rating);
        String releaseYear = releaseDate.substring(0, 4);
        releaseDate_tv.setText(releaseYear);
        overview_tv.setText(overview);

        // if the movie has already been added to the favorites, we want the checkbox to appear as checked
        String[] projection = {FavoriteMoviesContract.MovieEntry.COLUMN_MOVIE_ID};
        String selection = FavoriteMoviesContract.MovieEntry.COLUMN_MOVIE_ID + "=" + id;
        Cursor cursor = getContentResolver().query(FavoriteMoviesContract.MovieEntry.CONTENT_URI,
                projection,
                selection,
                null,
                null);
        if (cursor.getCount() > 0) {
            checkbox.setChecked(true);
        }
        cursor.close();

        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = null;
        if (cm != null) {
            activeNetwork = cm.getActiveNetworkInfo();
        }
        boolean isConnected = activeNetwork != null && activeNetwork.isConnectedOrConnecting();

        if (isConnected) {
            new ReviewsAsyncTask().execute();
            new VideosAsyncTask().execute();
        }
    }

    public void onFavoritesButtonClick(View view) {

        if (checkbox.isChecked()) {
            ContentValues contentValues = new ContentValues();
            contentValues.put(FavoriteMoviesContract.MovieEntry.COLUMN_MOVIE_ID, id);
            contentValues.put(FavoriteMoviesContract.MovieEntry.COLUMN_TITLE, originalTitle);
            contentValues.put(FavoriteMoviesContract.MovieEntry.COLUMN_POSTER_PATH, posterPath);
            contentValues.put(FavoriteMoviesContract.MovieEntry.COLUMN_BACKDROP_PATH, backdropPath);
            contentValues.put(FavoriteMoviesContract.MovieEntry.COLUMN_OVERVIEW, overview);
            contentValues.put(FavoriteMoviesContract.MovieEntry.COLUMN_VOTE_AVERAGE, voteAverage);
            contentValues.put(FavoriteMoviesContract.MovieEntry.COLUMN_RELEASE_DATE, releaseDate);

            getContentResolver().insert(FavoriteMoviesContract.MovieEntry.CONTENT_URI, contentValues);

        } else {
            String selection = FavoriteMoviesContract.MovieEntry.COLUMN_MOVIE_ID + "=" + id;
            getContentResolver().delete(FavoriteMoviesContract.MovieEntry.CONTENT_URI, selection, null);
        }

    }

    public static void watchYoutubeVideo(Context context, String key) {
        Intent appIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("vnd.youtube:" + key));
        Intent webIntent = new Intent(Intent.ACTION_VIEW,
                Uri.parse("http://www.youtube.com/watch?v=" + key));
        try {
            context.startActivity(appIntent);
        } catch (ActivityNotFoundException ex) {
            context.startActivity(webIntent);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putIntArray("scroll_position",
                new int[]{ scrollView.getScrollX(), scrollView.getScrollY()});
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        final int[] position = savedInstanceState.getIntArray("scroll_position");
        if (position != null)
            scrollView.post(new Runnable() {
                public void run() {
                    scrollView.scrollTo(position[0], position[1]);
                }
            });
    }

    public class ReviewsAsyncTask extends AsyncTask<Void, Void, List<String>> {

        @Override
        protected List<String> doInBackground(Void... voids) {
            URL url = NetworkUtils.createUrlToReviews(id);

            try {
                String json = NetworkUtils.getResponseFromHttpUrl(url);
                reviews = JsonParser.extractFeatureFromReviewsJson(json);
                return reviews;

            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }
        }

        private View createDividerLine() {
            View dividerLine = new View(getApplicationContext());
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, 3);
            dividerLine.setLayoutParams(params);
            dividerLine.setBackgroundColor(getResources().getColor(R.color.colorAccent));
            return dividerLine;
        }

        @Override
        protected void onPostExecute(List<String> reviews) {
            LinearLayout layout = findViewById(R.id.detail_linear_layout);
            if (!reviews.isEmpty()) {
                layout.addView(createDividerLine());

                for (int i = 0; i < reviews.size(); i++) {
                    TextView tv = new TextView(getApplicationContext());
                    LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT);
                    lp.setMargins(0, 18, 0, 18);
                    tv.setLayoutParams(lp);
                    String review = getString(R.string.review_text, reviews.get(i));
                    tv.setText(review);
                    tv.setTextSize(15);
                    tv.setTextColor(Color.parseColor("#ffffff"));
                    layout.addView(tv);

                    layout.addView(createDividerLine());
                }

            } else {
                TextView tv = new TextView(getApplicationContext());
                LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT);
                tv.setLayoutParams(lp);
                tv.setText("-");
                tv.setTextColor(Color.parseColor("#ffffff"));
                layout.addView(tv);
            }

        }
    }

    public class VideosAsyncTask extends AsyncTask<Void, Void, List<String>> {

        @Override
        protected List<String> doInBackground(Void... voids) {
            URL url = NetworkUtils.createUrlToVideos(id);

            try {
                String json = NetworkUtils.getResponseFromHttpUrl(url);
                videos = JsonParser.extractFeatureFromVideosJson(json);
                return videos;

            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPostExecute(List<String> strings) {
            LinearLayout layout = findViewById(R.id.videos_layout);
            if (!videos.isEmpty()) {
                for (int i = 0; i < videos.size(); i++) {
                    ImageView iv = new ImageView(getApplicationContext());
                    LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.WRAP_CONTENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT);
                    lp.setMarginEnd(15);
                    iv.setLayoutParams(lp);
                    layout.addView(iv);

                    final String video_key = videos.get(i);

                    Picasso.with(getApplicationContext()).load("https://img.youtube.com/vi/" + video_key + "/0.jpg").into(iv);

                    iv.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            watchYoutubeVideo(view.getContext(), video_key);
                        }
                    });

                }
            } else {

                TextView tv = new TextView(getApplicationContext());
                LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT);
                tv.setLayoutParams(lp);
                tv.setText("-");
                tv.setTextColor(Color.parseColor("#ffffff"));
                layout.addView(tv);
            }
        }
    }

}