package com.aerifiu.popularmovies.service;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;
import android.util.Log;

import com.aerifiu.popularmovies.model.net.ReviewResponse;
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

public class FetchUserRatingsIntentService extends IntentService {

	public static final String TAG = FetchUserRatingsIntentService.class.getSimpleName();
	private static final String MOVIE_ID_EXTRA = "intentExtraMovieId";
	public static final String DATA_LOADED = "intentRatingsLoaded";
	public static final String DATA_LOADED_EXTRA = "intentRatingsResponse";
	private Gson gson;

	public FetchUserRatingsIntentService() {
		super(FetchUserRatingsIntentService.class.getSimpleName());
		gson = new GsonBuilder().setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES).create();
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		Log.v(TAG, "starting movie sync");

		HttpURLConnection urlConnection = null;
		BufferedReader reader = null;
		String jsonResponse = null;

		try {

			final String BASE_URL = "http://api.themoviedb.org/3/movie";
			final String API_KEY = "api_key";
			final long id = intent.getLongExtra(MOVIE_ID_EXTRA, 0);

			if (TextUtils.isEmpty(AppConstants.MOVIE_DB_API_KEY)) {
				throw new IllegalArgumentException("add your api key");
			}

			Uri builtUri = Uri.parse(BASE_URL).buildUpon()
					.appendEncodedPath(String.valueOf(id))
					.appendQueryParameter(API_KEY, AppConstants.MOVIE_DB_API_KEY)
					.appendPath("reviews")
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

		final ReviewResponse response = gson.fromJson(jsonResponse, ReviewResponse.class);

		Intent i = new Intent(DATA_LOADED);
		i.putExtra(DATA_LOADED_EXTRA, response);

		LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(i);
		Log.v(TAG, "movie sync finished");
	}

	public static Intent getIntent(Context context, long id) {
		Intent i = new Intent(context, FetchUserRatingsIntentService.class);
		i.putExtra(MOVIE_ID_EXTRA, id);
		return i;
	}
}
