package com.aerifiu.popularmovies.util;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.aerifiu.popularmovies.model.db.MovieDetail;

import static nl.qbusict.cupboard.CupboardFactory.cupboard;

public class CupboardSQLiteOpenHelper extends SQLiteOpenHelper {

	private static final String DATABASE_NAME = "popular_movies.db";
	private static final int DATABASE_VERSION = 1;

	static {
		cupboard().register(MovieDetail.class);
	}

	public CupboardSQLiteOpenHelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		cupboard().withDatabase(db).createTables();
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		cupboard().withDatabase(db).upgradeTables();
	}
}
