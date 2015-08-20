package com.aerifiu.popularmovies.model.db;

import java.io.Serializable;

public class MovieDetail implements Serializable {

	public Long _id;
	public String originalTitle;
	public String releaseDate;
	public double voteAverage;
	public String overview;
	public String posterPath;
	public String title;

	public MovieDetail() {
	}

	public MovieDetail(Long id, String originalTitle, String releaseDate, double voteAverage, String overview, String posterPath, String
			title) {
		this._id = id;
		this.originalTitle = originalTitle;
		this.releaseDate = releaseDate;
		this.voteAverage = voteAverage;
		this.overview = overview;
		this.posterPath = posterPath;
		this.title = title;
	}
}
