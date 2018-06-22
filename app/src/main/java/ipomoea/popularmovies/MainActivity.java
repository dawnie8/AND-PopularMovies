package ipomoea.popularmovies;

import android.content.Intent;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import data.FavoriteMoviesContract;
import model.Movie;
import utils.JsonParser;
import utils.NetworkUtils;

public class MainActivity extends AppCompatActivity implements MovieAdapter.MovieItemClickListener {

    private List<Movie> movies = new ArrayList<>();
    private RecyclerView recyclerView;
    private MovieAdapter movieAdapter;
    private TextView errorMessage;
    private TextView emptyFavoritesMessage;
    private int selectedSortOrder = 0;
    private RecyclerView.LayoutManager layoutManager;
    private Parcelable savedRecyclerLayoutState;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        recyclerView = findViewById(R.id.movies_rv);
        errorMessage = findViewById(R.id.error_message);
        emptyFavoritesMessage = findViewById(R.id.empty_favorites_message);

        layoutManager = new GridLayoutManager(this, 2);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setHasFixedSize(true);
        movieAdapter = new MovieAdapter(this);
        recyclerView.setAdapter(movieAdapter);

        if (savedInstanceState != null) {
            selectedSortOrder = savedInstanceState.getInt("sortOrder");
        }
    }

    // MoviesAsyncTask is started from onResume() so that the movie list is updated when the user
    // navigates back to MainActivity after deleting a movie from favorites in DetailActivity
    @Override
    protected void onResume() {
        super.onResume();
        new MoviesAsyncTask().execute();
    }

    @Override
    public void onListItemClick(int index) {
        Intent intent = new Intent(this, DetailActivity.class);
        intent.putExtra("selected_movie", movies.get(index));
        startActivity(intent);
    }

    private void showErrorMessage() {
        recyclerView.setVisibility(View.INVISIBLE);
        errorMessage.setVisibility(View.VISIBLE);

        errorMessage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new MoviesAsyncTask().execute();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int selectedItem = item.getItemId();

        switch (selectedItem) {
            case R.id.most_popular:
                selectedSortOrder = 0;
                new MoviesAsyncTask().execute();
                return true;

            case R.id.highest_rated:
                selectedSortOrder = 1;
                new MoviesAsyncTask().execute();
                return true;

            case R.id.favorites:
                selectedSortOrder = 2;
                new MoviesAsyncTask().execute();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt("sortOrder", selectedSortOrder);
        outState.putParcelable("saved_state", recyclerView.getLayoutManager().onSaveInstanceState());
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        if (savedInstanceState != null) {
            savedRecyclerLayoutState = savedInstanceState.getParcelable("saved_state");
            recyclerView.getLayoutManager().onRestoreInstanceState(savedRecyclerLayoutState);
        }
    }

    public class MoviesAsyncTask extends AsyncTask<Void, Void, List<Movie>> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            errorMessage.setVisibility(View.INVISIBLE);
            emptyFavoritesMessage.setVisibility(View.INVISIBLE);
        }

        @Override
        protected List<Movie> doInBackground(Void... voids) {

            movies.clear();

            if (selectedSortOrder == 2) {

                Cursor cursor = getContentResolver().query(FavoriteMoviesContract.MovieEntry.CONTENT_URI,
                        null,
                        null,
                        null,
                        null);

                for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {

                    movies.add(new Movie(cursor.getInt(cursor.getColumnIndex(FavoriteMoviesContract.MovieEntry.COLUMN_MOVIE_ID)),
                            cursor.getString(cursor.getColumnIndex(FavoriteMoviesContract.MovieEntry.COLUMN_TITLE)),
                            cursor.getString(cursor.getColumnIndex(FavoriteMoviesContract.MovieEntry.COLUMN_POSTER_PATH)),
                            cursor.getString(cursor.getColumnIndex(FavoriteMoviesContract.MovieEntry.COLUMN_BACKDROP_PATH)),
                            cursor.getString(cursor.getColumnIndex(FavoriteMoviesContract.MovieEntry.COLUMN_OVERVIEW)),
                            cursor.getDouble(cursor.getColumnIndex(FavoriteMoviesContract.MovieEntry.COLUMN_VOTE_AVERAGE)),
                            cursor.getString(cursor.getColumnIndex(FavoriteMoviesContract.MovieEntry.COLUMN_RELEASE_DATE))
                    ));
                }
                cursor.close();

                return movies;

            } else {

                URL url = NetworkUtils.createUrlToMovies(selectedSortOrder);

                try {
                    String json = NetworkUtils.getResponseFromHttpUrl(url);
                    movies = JsonParser.extractFeatureFromMoviesJson(json);
                    return movies;

                } catch (IOException e) {
                    e.printStackTrace();
                    return null;
                }
            }
        }

        @Override
        protected void onPostExecute(List<Movie> movieList) {
            if (!movies.isEmpty()) {
                recyclerView.setVisibility(View.VISIBLE);
                movieAdapter.setMovieData(movieList);
                recyclerView.getLayoutManager().onRestoreInstanceState(savedRecyclerLayoutState);
            } else {
                if (selectedSortOrder == 2) {
                    recyclerView.setVisibility(View.INVISIBLE);
                    emptyFavoritesMessage.setVisibility(View.VISIBLE);
                } else {
                    showErrorMessage();
                }
            }
        }
    }

}
