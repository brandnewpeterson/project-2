package com.example.brandnewpeterson.projecttwo;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v7.widget.Toolbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.ActionBar;
import android.support.v4.app.NavUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.squareup.picasso.Picasso;

import org.json.JSONException;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * An activity representing a single Movie detail screen. This
 * activity is only used narrow width devices. On tablet-size devices,
 * item details are presented side-by-side with a list of items
 * in a {@link MovieGridActivity}.
 */
public class MovieDetailActivity extends AppCompatActivity {

    private CustomParcelable mParcelable;

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable("mParcelable", mParcelable);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        if (savedInstanceState != null){
            mParcelable = savedInstanceState.getParcelable("mParcelable");
        }

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movie_detail);
        Toolbar toolbar = (Toolbar) findViewById(R.id.detail_toolbar);
        setSupportActionBar(toolbar);

        Bundle extras = getIntent().getExtras();
        if (extras != null){
            mParcelable = (CustomParcelable) extras.getParcelable("mParcelable");
        }

        //Show the Up button in the action bar.
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        CollapsingToolbarLayout appBarLayout = (CollapsingToolbarLayout) findViewById(R.id.toolbar_layout);
        if (appBarLayout != null) {
            appBarLayout.setTitle(mParcelable.title);
        }

        String urlStringImages = "http://api.themoviedb.org/3/movie/" + mParcelable.id +
                "/images?" + "&api_key=" + getResources().getString(R.string.tmdb_api_key);
        new BackdropFetcher(urlStringImages).execute();

        // savedInstanceState is non-null when there is fragment state
        // saved from previous configurations of this activity
        // (e.g. when rotating the screen from portrait to landscape).
        // In this case, the fragment will automatically be re-added
        // to its container so we don't need to manually add it.
        // For more information, see the Fragments API guide at:
        //
        // http://developer.android.com/guide/components/fragments.html
        //

        if (savedInstanceState == null) {
            // Create the detail fragment and add it to the activity
            // using a fragment transaction.
            Bundle arguments = new Bundle();
            arguments.putParcelable("mParcelable", mParcelable);
            arguments.putBoolean("toolbarExists", true); //Starting fragment from own activity so they'll be a toolbar
            MovieDetailFragment fragment = new MovieDetailFragment();
            fragment.setArguments(arguments);
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.movie_detail_container, fragment, "currMovieDetailFrag")
                    .commit();
        }

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            // This ID represents the Home or Up button. In the case of this
            // activity, the Up button is shown. Use NavUtils to allow users
            // to navigate up one level in the application structure. For
            // more details, see the Navigation pattern on Android Design:
            //
            // http://developer.android.com/design/patterns/navigation.html#up-vs-back
            //
            //NavUtils.navigateUpTo(this, new Intent(this, MovieGridActivity.class));
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public class BackdropFetcher extends AsyncTask<String, Void, String> {
        HttpURLConnection urlConnection;
        String urlString;
        int mode;

        public BackdropFetcher(String urlString) {
            this.urlString = urlString;

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

            ImageView appBarLayoutPoster = (ImageView) findViewById(R.id.posterViewDetail);

            String urlBaseString = "http://image.tmdb.org/t/p/w780/";

            MovieDetailFragment fragment = (MovieDetailFragment) getSupportFragmentManager()
                    .findFragmentByTag("currMovieDetailFrag");

            String backdropPath = null;
            try {
                backdropPath = fragment.getHighestRatedBackdropPath(result);
            } catch (JSONException e) {
                e.printStackTrace();
            }

            if (backdropPath != null) {//A backdrop exists so load it.

                Picasso.with(getApplicationContext()).
                        load(urlBaseString + backdropPath).
                        into(appBarLayoutPoster, new com.squareup.picasso.Callback() {
                            @Override
                            public void onSuccess() {
                            }

                            @Override
                            public void onError() {
                                // TODO Auto-generated method stub

                            }
                        });                }

            //else {//Just use cropped poster. Looks shite, but better than nothing.
              //  appBarLayoutPoster.setImageBitmap(mParcelable.poster);
            //}


        }



    }
}
