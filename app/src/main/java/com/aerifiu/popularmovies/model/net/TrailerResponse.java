package com.aerifiu.popularmovies.model.net;

import com.google.gson.annotations.Expose;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class TrailerResponse implements Serializable {

	@Expose
	private Integer id;
	@Expose
	private List<TrailerResult> results = new ArrayList<TrailerResult>();

	/**
	 * @return The id
	 */
	public Integer getId() {
		return id;
	}

	/**
	 * @param id The id
	 */
	public void setId(Integer id) {
		this.id = id;
	}

	/**
	 * @return The results
	 */
	public List<TrailerResult> getResults() {
		return results;
	}

	/**
	 * @param results The results
	 */
	public void setResults(List<TrailerResult> results) {
		this.results = results;
	}

}