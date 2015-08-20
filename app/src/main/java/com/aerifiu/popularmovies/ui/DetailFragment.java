package com.aerifiu.popularmovies.ui;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.ShareCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.ShareActionProvider;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.aerifiu.popularmovies.R;
import com.aerifiu.popularmovies.model.db.MovieDetail;
import com.aerifiu.popularmovies.model.net.ReviewResponse;
import com.aerifiu.popularmovies.model.net.ReviewResult;
import com.aerifiu.popularmovies.model.net.TrailerResponse;
import com.aerifiu.popularmovies.model.net.TrailerResult;
import com.aerifiu.popularmovies.service.FetchTrailerIntentService;
import com.aerifiu.popularmovies.service.FetchUserRatingsIntentService;
import com.aerifiu.popularmovies.util.AppConstants;
import com.aerifiu.popularmovies.util.DatabaseHelper;
import com.bumptech.glide.Glide;

import java.util.List;

public class DetailFragment extends Fragment implements View.OnClickListener {

	public static final String TAG = DetailFragment.class.getSimpleName();
	private static final IntentFilter FETCH_TRAILER_BROADCAST = new IntentFilter(FetchTrailerIntentService.DATA_LOADED);
	private static final IntentFilter FETCH_RATINGS_BROADCAST = new IntentFilter(FetchUserRatingsIntentService.DATA_LOADED);
	private static final String BUNDLE_PARAM_RESULT = "fragmentBundleParamResult";
	private static final String BUNDLE_MOVIE_DETAIL = "budleMovieDetail";

	private ShareActionProvider shareActionProvider;
	private MovieDetail movieDetail;

	private TextView reviewDetailHeader;
	private TextView trailerDetailHeader;
	private LinearLayout reviewDetailContainer;
	private LinearLayout trailerDetailContainer;

	private boolean isFav;

	private final BroadcastReceiver trailerBroadcastReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			if (getActivity().isFinishing()) {
				return;
			}
			TrailerResponse trailerResponse = (TrailerResponse) intent.getSerializableExtra(FetchTrailerIntentService
					.DATA_LOADED_EXTRA);

			if (trailerResponse != null) {
				List<TrailerResult> trailerResults = trailerResponse.getResults();
				if (trailerResults != null && !trailerResults.isEmpty()) {
					trailerDetailHeader.setVisibility(View.VISIBLE);
					trailerDetailContainer.setVisibility(View.VISIBLE);
					LayoutInflater layoutInflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);

					boolean isShareSet = false;
					for (TrailerResult r : trailerResults) {
						if (r.getSite().equalsIgnoreCase("YouTube")) {
							View view = layoutInflater.inflate(R.layout.review_trailer, null);
							view.setOnClickListener(DetailFragment.this);
							TextView name = (TextView) view.findViewById(R.id.review_trailer_title);
							name.setText(r.getName());
							view.setTag(r.getKey());
							trailerDetailContainer.addView(view);

							if (!isShareSet) {
								isShareSet = true;
								setShareIntent(r.getKey());
							}
						}
					}
				}
			}
		}
	};

	private void setShareIntent(String id) {
		final String text = getResources().getString(R.string.share_content) + "http://www.youtube.com/watch?v=" + id;
		Intent shareIntent = ShareCompat.IntentBuilder.from(getActivity()).setType("text/plain").setText(text)
				.getIntent();
		// Set the share Intent
		if (shareActionProvider != null) {
			shareActionProvider.setShareIntent(shareIntent);
			DetailFragment.this.getActivity().invalidateOptionsMenu();
		}
	}

	private final BroadcastReceiver reviewBroadcastReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			if (getActivity().isFinishing()) {
				return;
			}
			ReviewResponse reviewResponse = (ReviewResponse) intent.getSerializableExtra(FetchUserRatingsIntentService
					.DATA_LOADED_EXTRA);

			if (reviewResponse != null) {
				List<ReviewResult> reviewResults = reviewResponse.getResults();
				if (!reviewResults.isEmpty()) {
					reviewDetailHeader.setVisibility(View.VISIBLE);
					reviewDetailContainer.setVisibility(View.VISIBLE);
					LayoutInflater layoutInflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);

					for (ReviewResult r : reviewResults) {
						View view = layoutInflater.inflate(R.layout.review_detail, null);
						TextView author = (TextView) view.findViewById(R.id.review_detail_author);
						TextView content = (TextView) view.findViewById(R.id.review_detail_content);
						author.setText(r.getAuthor());
						content.setText(r.getContent());
						reviewDetailContainer.addView(view);
					}
				}
			}
		}
	};

	public DetailFragment() {
	}

	public static DetailFragment getInstance(MovieDetail result) {
		DetailFragment fragment = new DetailFragment();
		Bundle bundle = new Bundle();
		bundle.putSerializable(BUNDLE_PARAM_RESULT, result);
		fragment.setArguments(bundle);
		return fragment;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setHasOptionsMenu(true);

		if (savedInstanceState == null) {
			Bundle args = getArguments();
			movieDetail = (MovieDetail) args.getSerializable(BUNDLE_PARAM_RESULT);

			if (movieDetail != null) {
				MovieDetail favMovie = DatabaseHelper.getInstance(getActivity()).queryMovieDetail(movieDetail._id);
				if (favMovie != null) {
					isFav = true;
					DatabaseHelper.getInstance(getActivity()).insertOrUpdateMovieDetail(movieDetail); // update db
				}

				getActivity().startService(FetchTrailerIntentService.getIntent(getActivity(), movieDetail._id));
				getActivity().startService(FetchUserRatingsIntentService.getIntent(getActivity(), movieDetail._id));
			}

		} else {
			movieDetail = (MovieDetail) savedInstanceState.getSerializable(BUNDLE_MOVIE_DETAIL);
			if (movieDetail != null) {
				getActivity().startService(FetchTrailerIntentService.getIntent(getActivity(), movieDetail._id));
				getActivity().startService(FetchUserRatingsIntentService.getIntent(getActivity(), movieDetail._id));
			}
		}
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putSerializable(BUNDLE_MOVIE_DETAIL, movieDetail);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
	                         Bundle savedInstanceState) {

		View root = inflater.inflate(R.layout.fragment_detail, container, false);
		if (movieDetail == null) {
			return root;
		}

		try {

			TextView textViewTitle = (TextView) root.findViewById(R.id.fragment_movies_detail_title);
			textViewTitle.setText(movieDetail.title);

			ImageView imageViewPoster = (ImageView) root.findViewById(R.id.fragment_movies_detail_poster);
			Glide.with(getActivity()).load(AppConstants.POSTER_URI + movieDetail.posterPath).placeholder(R.drawable.ic_shape_rect)
					.centerCrop().into(imageViewPoster);

			TextView textViewOverview = (TextView) root.findViewById(R.id.fragment_movies_detail_overview);
			textViewOverview.setText(String.valueOf(movieDetail.overview));

			TextView textViewDate = (TextView) root.findViewById(R.id.fragment_movies_detail_date);
			textViewDate.setText(movieDetail.releaseDate.substring(0, 4));

			TextView textViewVote = (TextView) root.findViewById(R.id.fragment_movies_detail_vote_avg);
			textViewVote.setText(String.valueOf(movieDetail.voteAverage));

			reviewDetailHeader = (TextView) root.findViewById(R.id.fragment_detail_review_header);
			reviewDetailContainer = (LinearLayout) root.findViewById(R.id.fragment_detail_review_container);

			trailerDetailHeader = (TextView) root.findViewById(R.id.fragment_detail_trailer_header);
			trailerDetailContainer = (LinearLayout) root.findViewById(R.id.fragment_detail_trailer_container);

			final ImageView favIcon = (ImageView) root.findViewById(R.id.fragment_detail_fav);
			favIcon.setColorFilter(getColorForFav(isFav));
			favIcon.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					if (!isFav) {
						DatabaseHelper.getInstance(getActivity()).insertOrUpdateMovieDetail(movieDetail);
					} else { // un-faved
						DatabaseHelper.getInstance(getActivity()).deleteMovieDetail(movieDetail._id);
					}
					isFav = !isFav;
					favIcon.setColorFilter(getColorForFav(isFav));
				}
			});
		} catch (Exception e) {
			// maybe we are dealing with malformed/missing json data on this movie
			// lets not show the user half empty screens and skip this movie
			Toast.makeText(getActivity(), getString(R.string.movie_details_unknown), Toast.LENGTH_SHORT).show();

			final ImageView favIcon = (ImageView) root.findViewById(R.id.fragment_detail_fav);
			favIcon.setVisibility(View.GONE);

			if (getActivity() instanceof DetailActivity) {
				getActivity().finish();
			}

			e.printStackTrace();
		}

		return root;
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		MenuItem item = menu.findItem(R.id.menu_detail_share);
		shareActionProvider = new ShareActionProvider(getActivity());
		MenuItemCompat.setActionProvider(item, shareActionProvider);
	}

	@Override
	public void onStart() {
		super.onStart();
		LocalBroadcastManager.getInstance(getActivity()).registerReceiver(trailerBroadcastReceiver, FETCH_TRAILER_BROADCAST);
		LocalBroadcastManager.getInstance(getActivity()).registerReceiver(reviewBroadcastReceiver, FETCH_RATINGS_BROADCAST);
	}

	@Override
	public void onStop() {
		super.onStop();
		LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(trailerBroadcastReceiver);
		LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(reviewBroadcastReceiver);
	}

	@Override
	public void onClick(View v) {
		final String id = (String) v.getTag();
		if (!TextUtils.isEmpty(id)) {
			startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.youtube.com/watch?v=" + id)));
		}
	}

	private int getColorForFav(boolean isFav) {
		return isFav ? getResources().getColor(R.color.orange) : getResources().getColor(R.color.light_grey);
	}
}

