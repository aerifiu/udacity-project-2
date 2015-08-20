
package com.aerifiu.popularmovies.model.net;

/**
 * this code was generated on http://www.jsonschema2pojo.org/
 */

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class MovieDetailResponse implements Serializable{

	@Expose
	private int page;
	@Expose
	private List<MovieDetailResult> results = new ArrayList<MovieDetailResult>();
	@SerializedName("total_pages")
	@Expose
	private int totalPages;
	@SerializedName("total_results")
	@Expose
	private int totalResults;

	/**
	 * @return The page
	 */
	public int getPage() {
		return page;
	}

	/**
	 * @param page The page
	 */
	public void setPage(int page) {
		this.page = page;
	}

	/**
	 * @return The results
	 */
	public List<MovieDetailResult> getResults() {
		return results;
	}

	/**
	 * @param results The results
	 */
	public void setResults(List<MovieDetailResult> results) {
		this.results = results;
	}

	/**
	 * @return The totalPages
	 */
	public int getTotalPages() {
		return totalPages;
	}

	/**
	 * @param totalPages The total_pages
	 */
	public void setTotalPages(int totalPages) {
		this.totalPages = totalPages;
	}

	/**
	 * @return The totalResults
	 */
	public int getTotalResults() {
		return totalResults;
	}

	/**
	 * @param totalResults The total_results
	 */
	public void setTotalResults(int totalResults) {
		this.totalResults = totalResults;
	}

}