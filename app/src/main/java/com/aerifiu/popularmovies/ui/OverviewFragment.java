package com.aerifiu.popularmovies.ui;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.Toast;

import com.aerifiu.popularmovies.R;
import com.aerifiu.popularmovies.model.db.MovieDetail;
import com.aerifiu.popularmovies.model.net.MovieDetailResponse;
import com.aerifiu.popularmovies.service.FetchMovieDetailIntentService;
import com.aerifiu.popularmovies.util.DatabaseHelper;

import java.util.ArrayList;
import java.util.List;

public class OverviewFragment extends Fragment implements AdapterView.OnItemClickListener {

	public static final String TAG = OverviewFragment.class.getSimpleName();
	private static final IntentFilter FETCH_MOVIES_BROADCAST = new IntentFilter(FetchMovieDetailIntentService.DATA_LOADED);
	private MoviesAdapter adapter;
	private int currentMenuItem;

	public OverviewFragment() {
	}

	private final BroadcastReceiver movieDetailsBroadcastReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			if (!getActivity().isFinishing() && adapter != null) {
				MovieDetailResponse movieDetailResponse = (MovieDetailResponse) intent.getSerializableExtra(FetchMovieDetailIntentService
						.EXTRA_MOVIE_DETAILS);
				if (movieDetailResponse != null && movieDetailResponse.getResults() != null && currentMenuItem != R.id.action_favs) {
					boolean wasEmpty = adapter.getCount() == 0;
					ArrayList<MovieDetail> list = new ArrayList<>(DatabaseHelper.convert(movieDetailResponse.getResults()));
					adapter.swapDataSet(list);
					if (((MainActivity) getActivity()).isTabletLayout() && wasEmpty) {
						getActivity().getSupportFragmentManager().beginTransaction()
								.replace(R.id.activity_main_detail_container,
										DetailFragment.getInstance(list.get(0)), DetailFragment.TAG).commit();
					}
				}
			}
		}
	};

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setHasOptionsMenu(true);

		if (!isOnline()) {
			Toast.makeText(getActivity(), getString(R.string.no_network), Toast.LENGTH_LONG).show();
		} else {
			getActivity().setTitle(getString(R.string.sort_most_popular));
			getActivity().startService(FetchMovieDetailIntentService.getIntent(getActivity(), FetchMovieDetailIntentService
					.SortOrder.POPULARITY));
		}
	}

	@Override
	public void onStart() {
		super.onStart();
		LocalBroadcastManager.getInstance(getActivity()).registerReceiver(movieDetailsBroadcastReceiver, FETCH_MOVIES_BROADCAST);
	}

	@Override
	public void onStop() {
		super.onStop();
		LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(movieDetailsBroadcastReceiver);
	}

	@Override
	public void onResume() {
		super.onResume();
		if (currentMenuItem == R.id.action_favs) {
			List<MovieDetail> details = DatabaseHelper.getInstance(getActivity()).queryMovieDetails();
			adapter.swapDataSet(new ArrayList<>(details));
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
	                         Bundle savedInstanceState) {
		final View root = inflater.inflate(R.layout.fragment_movies_overview, container, false);

		final GridView gridview = (GridView) root.findViewById(R.id.fragment_movies_gridview);
		gridview.setOnItemClickListener(this);

		// okay, this is a bit hacky but I don't know another way of fitting them exactly to the screen
		root.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
			@Override
			public void onGlobalLayout() {
				root.getViewTreeObserver().removeGlobalOnLayoutListener(this);
				// we need to wait until the view has been inflated to get sizes
				final int width = (int) (Math.ceil(root.getWidth() * 0.5f));
				final int height = (int) (Math.ceil(root.getHeight() * 0.5f));
				adapter = new MoviesAdapter(getActivity(), height > width ? height : width);
				gridview.setAdapter(adapter);
			}
		});

		return root;
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		final MovieDetail result = (MovieDetail) adapter.getItem((int) id);
		boolean isTabletLayout = ((MainActivity) getActivity()).isTabletLayout();

		if (isTabletLayout) {
			getActivity().getSupportFragmentManager().beginTransaction()
					.replace(R.id.activity_main_detail_container, DetailFragment.getInstance(result), DetailFragment.TAG)
					.commit();
		} else {
			Intent i = new Intent(getActivity(), DetailActivity.class);
			i.putExtra(DetailActivity.EXTRA_RESULT, result);
			getActivity().startActivity(i);
		}
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		inflater.inflate(R.menu.menu, menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		final int id = item.getItemId();
		currentMenuItem = id;

		switch (id) {
		case R.id.action_most_popular:
			getActivity().setTitle(getString(R.string.sort_most_popular));
			getActivity().startService(FetchMovieDetailIntentService.getIntent(getActivity(), FetchMovieDetailIntentService.
					SortOrder.POPULARITY));
			return true;

		case R.id.action_highest_rated:
			getActivity().setTitle(getString(R.string.highest_rated));
			getActivity().startService(FetchMovieDetailIntentService.getIntent(getActivity(), FetchMovieDetailIntentService
					.SortOrder.RATING));
			return true;

		case R.id.action_favs:
			if (adapter != null) {
				getActivity().setTitle(getString(R.string.favs));
				List<MovieDetail> details = DatabaseHelper.getInstance(getActivity()).queryMovieDetails();
				adapter.swapDataSet(new ArrayList<>(details));
				return true;
			}
		}

		return super.onOptionsItemSelected(item);
	}

	private boolean isOnline() {
		ConnectivityManager manager = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo info = manager.getActiveNetworkInfo();
		return info != null && info.isConnectedOrConnecting();
	}
}
