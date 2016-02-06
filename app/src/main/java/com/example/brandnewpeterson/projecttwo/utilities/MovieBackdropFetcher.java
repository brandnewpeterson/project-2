package com.example.brandnewpeterson.projecttwo.utilities;

import android.app.Activity;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.widget.ImageView;

import com.example.brandnewpeterson.projecttwo.MovieDetailFragment;
import com.example.brandnewpeterson.projecttwo.R;
import com.squareup.picasso.Picasso;

import org.json.JSONException;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by brandnewpeterson on 2/5/16.
 */
public class MovieBackdropFetcher extends AsyncTask<String, Void, String> {
    private HttpURLConnection urlConnection;
    private String urlString;
    private AppCompatActivity parentActivity;

    public MovieBackdropFetcher(String urlString, AppCompatActivity parentActivity) {
        this.urlString = urlString;
        this.parentActivity = parentActivity;
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
            urlConnection.disconnect();
        }

        return result.toString();

    }

    @Override
    protected void onPostExecute(String result) {

        if (!isCancelled()) {

            ImageView appBarLayoutPoster = (ImageView) parentActivity.findViewById(R.id.posterViewDetail);

            String urlBaseString = "http://image.tmdb.org/t/p/w780/";

            MovieDetailFragment fragment = (MovieDetailFragment) parentActivity.getSupportFragmentManager()
                    .findFragmentByTag("currMovieDetailFrag");

            String backdropPath = null;
            try {
                backdropPath = fragment.getHighestRatedBackdropPath(result);
            } catch (JSONException e) {
                e.printStackTrace();
            }

            if (backdropPath != null) {//A backdrop exists so load it.

                Picasso.with(parentActivity.getApplicationContext()).
                        load(urlBaseString + backdropPath).
                        into(appBarLayoutPoster, new com.squareup.picasso.Callback() {
                            @Override
                            public void onSuccess() {
                            }

                            @Override
                            public void onError() {
                                // TODO Auto-generated method stub

                            }
                        });
            }

        }
    }
}