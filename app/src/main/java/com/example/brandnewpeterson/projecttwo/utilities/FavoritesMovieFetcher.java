package com.example.brandnewpeterson.projecttwo.utilities;

import android.os.AsyncTask;

import com.example.brandnewpeterson.projecttwo.MovieGridActivity;
import com.example.brandnewpeterson.projecttwo.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by brandnewpeterson on 2/3/16.
 */
public class FavoritesMovieFetcher extends AsyncTask<String, Void, String[]> {
    private HttpURLConnection urlConnection;
    private JSONArray ids;
    private String BASE_URL = "https://api.themoviedb.org/3/movie/";
    private String urlString;
    private String[] jsonStrings;
    private MovieGridActivity parentActivity;
    private JSONArray moviesArray;

    public FavoritesMovieFetcher(JSONArray ids, MovieGridActivity parentActivity, JSONArray moviesArray) {
        this.ids = ids;
        jsonStrings = new String[ids.length()];
        this.parentActivity = parentActivity;
        this.moviesArray = moviesArray;
    }

    @Override
    protected String[] doInBackground(String... args) {

        for (int i = 0; i < ids.length(); i++) {//Query each of the fave ids.

            try {
                urlString = BASE_URL + ids.getString(i) +
                        "?&api_key=" +
                        parentActivity.getResources().getString(R.string.tmdb_api_key);

                //System.out.println("HTTP query: " + urlString);
            } catch (JSONException e) {
                e.printStackTrace();
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
        //System.out.println("Results length: " + result.length);

        moviesArray = new JSONArray();

        for (int i  = 0; i< result.length; i++){

            try {
                moviesArray.put(new JSONObject(result[i]));
            } catch (JSONException e) {
                e.printStackTrace();
            }

        }

        try {
            parentActivity.setupGridViews();
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

}
