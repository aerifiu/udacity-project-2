package com.aerifiu.popularmovies.ui;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.aerifiu.popularmovies.R;

public class MainActivity extends AppCompatActivity {

	private static final String BUNDLE_IS_TABLET = "bundleIsTablet";
	private boolean isTabletLayout;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		setTitle(getString(R.string.app_name));

		if (savedInstanceState == null) {
			if (findViewById(R.id.activity_main_detail_container) != null) {
				isTabletLayout = true;
				getSupportFragmentManager().beginTransaction()
						.replace(R.id.activity_main_detail_container, DetailFragment.getInstance(null), DetailFragment.TAG)
						.commit();
			}
		} else {
			isTabletLayout = savedInstanceState.getBoolean(BUNDLE_IS_TABLET);
		}
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putBoolean(BUNDLE_IS_TABLET, isTabletLayout);
	}

	public boolean isTabletLayout() {
		return isTabletLayout;
	}
}
