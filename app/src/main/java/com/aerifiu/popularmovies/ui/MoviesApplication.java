package com.aerifiu.popularmovies.ui;

import android.app.Application;

import com.aerifiu.popularmovies.BuildConfig;
import com.facebook.stetho.Stetho;

public class MoviesApplication extends Application {

	@Override
	public void onCreate() {
		super.onCreate();
		if (BuildConfig.DEBUG) {
			Stetho.initialize(
					Stetho.newInitializerBuilder(this)
							.enableDumpapp(Stetho.defaultDumperPluginsProvider(this))
							.enableWebKitInspector(Stetho.defaultInspectorModulesProvider(this))
							.build());
		}
	}
}
