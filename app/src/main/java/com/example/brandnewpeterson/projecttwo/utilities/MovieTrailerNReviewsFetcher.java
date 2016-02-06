package com.example.brandnewpeterson.projecttwo.utilities;

import android.os.AsyncTask;

import com.example.brandnewpeterson.projecttwo.MovieDetailFragment;
import com.example.brandnewpeterson.projecttwo.MovieGridActivity;

import org.json.JSONException;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by brandnewpeterson on 2/4/16.
 */
public class MovieTrailerNReviewsFetcher extends AsyncTask<String, Void, String> {

    HttpURLConnection urlConnection;
    String urlString;
    int mode;
    private WeakReference<MovieDetailFragment> movieDetailFragment;


    public MovieTrailerNReviewsFetcher(String urlString, int mode, MovieDetailFragment movieDetailFragment) {
        this.urlString = urlString;
        this.movieDetailFragment = new WeakReference<MovieDetailFragment>(movieDetailFragment);

    }

    @Override
    protected String doInBackground(String... args) {


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
            if (urlConnection != null){
                urlConnection.disconnect();
            }
        }

        return result.toString();

    }

    @Override
    protected void onPostExecute(String result) {

        if (result != null && !isCancelled() && movieDetailFragment.get() != null) {
            if (movieDetailFragment.get().getActivity() != null) {

                if (mode == movieDetailFragment.get().VIDEO_RETRIEVAL_MODE) {
                    try {
                        movieDetailFragment.get().setupTrailerViews(result);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                } else if (mode == movieDetailFragment.get().REVIEW_RETRIEVAL_MODE) {
                    try {
                        movieDetailFragment.get().setupReviewViews(result);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                } else if (mode == movieDetailFragment.get().BACKDROP_RETRIEVAL_MODE) {
                    try {
                        movieDetailFragment.get().setupBackdropViews(result);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                }

            }
        }
    }

}
