package com.aerifiu.popularmovies.service;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;
import android.util.Log;

import com.aerifiu.popularmovies.model.net.MovieDetailResponse;
import com.aerifiu.popularmovies.util.AppConstants;
import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Network code adapted from the udacity Sunshine app
 */
public class FetchMovieDetailIntentService extends IntentService {

	public enum SortOrder {
		// note: query with vote_average.asc does not return anything - so i used descending
		POPULARITY("popularity.desc"), RATING("vote_average.desc");

		protected final String query;

		SortOrder(String query) {
			this.query = query;
		}
	}

	public static final String TAG = FetchMovieDetailIntentService.class.getSimpleName();
	public static final String DATA_LOADED = "intentMovieDetailDataLoaded";
	public static final String EXTRA_MOVIE_DETAILS = "intentExtraMovieDetails";
	private static final String EXTRA_SORT_ORDER = "intentExtraSortOrder";
	private Gson gson;

	public FetchMovieDetailIntentService() {
		super(FetchMovieDetailIntentService.class.getSimpleName());
		gson = new GsonBuilder().setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES).create();
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		Log.v(TAG, "starting movie sync");

		final SortOrder sortOrder = (SortOrder) intent.getSerializableExtra(EXTRA_SORT_ORDER);
		HttpURLConnection urlConnection = null;
		BufferedReader reader = null;
		String jsonResponse = null;

		try {

			final String BASE_URL = "http://api.themoviedb.org/3/discover/movie?";
			final String SORT_BY = "sort_by";
			final String API_KEY = "api_key";

			if (TextUtils.isEmpty(AppConstants.MOVIE_DB_API_KEY)) {
				throw new IllegalArgumentException("add your api key");
			}

			Uri builtUri = Uri.parse(BASE_URL).buildUpon()
					.appendQueryParameter(SORT_BY, sortOrder.query)
					.appendQueryParameter(API_KEY, AppConstants.MOVIE_DB_API_KEY)
					.build();

			URL url = new URL(builtUri.toString());

			urlConnection = (HttpURLConnection) url.openConnection();
			urlConnection.setRequestMethod("GET");
			urlConnection.connect();

			InputStream inputStream = urlConnection.getInputStream();
			StringBuilder buffer = new StringBuilder();
			if (inputStream == null) {
				// Nothing to do.
				return;
			}
			reader = new BufferedReader(new InputStreamReader(inputStream));

			String line;
			while ((line = reader.readLine()) != null) {
				buffer.append(line);
			}

			if (buffer.length() == 0) {
				return;
			}
			jsonResponse = buffer.toString();

		} catch (IOException e) {
			e.printStackTrace();

		} finally {
			if (urlConnection != null) {
				urlConnection.disconnect();
			}
			if (urlConnection != null) {
				try {
					if (reader != null) {
						reader.close();
					}
				} catch (final IOException ignore) {
				}
			}
		}

		if (TextUtils.isEmpty(jsonResponse)) {
			return;
		}

		final MovieDetailResponse response = gson.fromJson(jsonResponse, MovieDetailResponse.class);

		Intent i = new Intent(DATA_LOADED);
		i.putExtra(EXTRA_MOVIE_DETAILS, response);

		LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(i);
		Log.v(TAG, "movie sync finished");
	}

	public static Intent getIntent(Context context, SortOrder sortOrder) {
		Intent i = new Intent(context, FetchMovieDetailIntentService.class);
		i.putExtra(EXTRA_SORT_ORDER, sortOrder);
		return i;
	}
}
