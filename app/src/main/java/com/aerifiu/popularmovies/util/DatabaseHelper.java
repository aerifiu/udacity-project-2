package com.aerifiu.popularmovies.util;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import com.aerifiu.popularmovies.model.db.MovieDetail;
import com.aerifiu.popularmovies.model.net.MovieDetailResult;

import java.util.ArrayList;
import java.util.List;

import static nl.qbusict.cupboard.CupboardFactory.cupboard;

public class DatabaseHelper {

	private static volatile DatabaseHelper instance;
	private CupboardSQLiteOpenHelper sqlOpenHelper;

	private DatabaseHelper(Context context) {
		sqlOpenHelper = new CupboardSQLiteOpenHelper(context);
	}

	public static DatabaseHelper getInstance(Context context) {
		if (instance == null) {
			synchronized (DatabaseHelper.class) {
				if (instance == null) {
					instance = new DatabaseHelper(context.getApplicationContext());
				}
			}
		}
		return instance;
	}

	public static MovieDetail convert(MovieDetailResult result) {
		return new MovieDetail((long) result.getId(), result.getOriginalTitle(), result.getReleaseDate(), result.getVoteAverage(), result
				.getOverview(), result.getPosterPath(), result.getTitle());
	}

	public static List<MovieDetail> convert(List<MovieDetailResult> results) {
		List<MovieDetail> list = new ArrayList<>(results.size());
		for (MovieDetailResult r : results) {
			list.add(convert(r));
		}
		return list;
	}

	public ArrayList<MovieDetail> queryMovieDetails() {
		SQLiteDatabase db = sqlOpenHelper.getReadableDatabase();
		List<MovieDetail> list = cupboard().withDatabase(db).query(MovieDetail.class).list();
		return new ArrayList<>(list);
	}

	public MovieDetail queryMovieDetail(final long id) {
		SQLiteDatabase db = sqlOpenHelper.getReadableDatabase();
		return cupboard().withDatabase(db).query(MovieDetail.class).withSelection("_id = ?", String.valueOf(id)).get();
	}

	public void insertOrUpdateMovieDetail(MovieDetail detail) {
		SQLiteDatabase db = sqlOpenHelper.getWritableDatabase();
		cupboard().withDatabase(db).put(detail);
	}

	public void insertMovieDetails(List<MovieDetail> details) {
		SQLiteDatabase db = sqlOpenHelper.getWritableDatabase();
		cupboard().withDatabase(db).put(details);
	}

	public void deleteMovieDetail(final long id) {
		SQLiteDatabase db = sqlOpenHelper.getWritableDatabase();
		cupboard().withDatabase(db).delete(MovieDetail.class, "_id = ?", String.valueOf(id));
	}

	public void updateMovieDetails(MovieDetail movieDetail) {
		SQLiteDatabase db = sqlOpenHelper.getWritableDatabase();
		cupboard().withDatabase(db).put(movieDetail);
	}

}
