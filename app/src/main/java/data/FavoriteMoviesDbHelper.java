package data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class FavoriteMoviesDbHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "favorite_movies.db";

    private static final int DATABASE_VERSION = 1;

    public FavoriteMoviesDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {

        final String SQL_CREATE_MOVIES_TABLE = "CREATE TABLE " + FavoriteMoviesContract.MovieEntry.TABLE_NAME + " (" +
                FavoriteMoviesContract.MovieEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                FavoriteMoviesContract.MovieEntry.COLUMN_MOVIE_ID + " INTEGER NOT NULL, " +
                FavoriteMoviesContract.MovieEntry.COLUMN_TITLE + " TEXT NOT NULL, " +
                FavoriteMoviesContract.MovieEntry.COLUMN_POSTER_PATH + " TEXT NOT NULL, " +
                FavoriteMoviesContract.MovieEntry.COLUMN_BACKDROP_PATH + " TEXT NOT NULL, " +
                FavoriteMoviesContract.MovieEntry.COLUMN_OVERVIEW + " TEXT NOT NULL, " +
                FavoriteMoviesContract.MovieEntry.COLUMN_VOTE_AVERAGE + " INTEGER NOT NULL, " +
                FavoriteMoviesContract.MovieEntry.COLUMN_RELEASE_DATE + " TEXT NOT NULL" +
                "); ";

        sqLiteDatabase.execSQL(SQL_CREATE_MOVIES_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + FavoriteMoviesContract.MovieEntry.TABLE_NAME);
        onCreate(sqLiteDatabase);
    }
}