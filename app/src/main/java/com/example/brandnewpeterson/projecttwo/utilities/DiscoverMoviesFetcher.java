package com.example.brandnewpeterson.projecttwo.utilities;

import android.os.AsyncTask;

import com.example.brandnewpeterson.projecttwo.MovieGridActivity;
import com.example.brandnewpeterson.projecttwo.R;

import org.json.JSONException;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by brandnewpeterson on 2/3/16.
 */
public class DiscoverMoviesFetcher extends AsyncTask<String, Void, String[]> {

    private HttpURLConnection urlConnection;
    private Integer mode;
    private String BASE_URL = "http://api.themoviedb.org/3/discover/movie?";
    private String POPULARITY_FLAG = "sort_by=popularity.desc";
    private String RATINGS_FLAG = "sort_by=vote_average.desc&vote_average.gte=1&&vote_count.gte=100";
    private String urlString;
    private MovieGridActivity parentActivity;
    private int pageMax;
    private String[] jsonStrings;

    public DiscoverMoviesFetcher(Integer MODE, MovieGridActivity parentActivity, int pageMax) {
        this.mode = MODE;
        this.parentActivity = parentActivity;
        this.pageMax = pageMax;
        jsonStrings = new String[pageMax];

    }

    @Override
    protected String[] doInBackground(String... args) {

        for (int i = 0; i < pageMax; i++) {//Query multiple pages from API so as to fill screen. (Smarter implementation could re-query on scroll.)

            if (mode == parentActivity.MOST_POPULAR_MODE) {
                urlString = BASE_URL + POPULARITY_FLAG + "&page=" + (i + 1) + "&api_key=" + parentActivity.getResources().getString(R.string.tmdb_api_key);
            } else {
                urlString = BASE_URL + RATINGS_FLAG + "&page=" + (i + 1) + "&api_key=" + parentActivity.getResources().getString(R.string.tmdb_api_key);
            }

            //System.out.println("Input URL: " + urlString);

            StringBuilder result = new StringBuilder();

            try {
                URL url = new URL(urlString);
                urlConnection = (HttpURLConnection) url.openConnection();
                InputStream in = new BufferedInputStream(urlConnection.getInputStream());

                BufferedReader reader = new BufferedReader(new InputStreamReader(in));

                String line;
                while ((line = reader.readLine()) != null) {
                    result.append(line);
                }

            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                urlConnection.disconnect();
            }

            jsonStrings[i] = result.toString();
        }

        return jsonStrings;
    }

    @Override
    protected void onPostExecute(String[] result) {
        try {
            parentActivity.concatMoviesArray(result);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        try {
            parentActivity.setupGridViews();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

}
