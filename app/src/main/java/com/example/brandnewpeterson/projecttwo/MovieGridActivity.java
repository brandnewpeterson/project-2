package com.example.brandnewpeterson.projecttwo;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Parcelable;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;


import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * An activity representing a list of Movies. This activity
 * has different presentations for handset and tablet-size devices. On
 * handsets, the activity presents a list of items, which when touched,
 * lead to a {@link MovieDetailActivity} representing
 * item details. On tablets, the activity presents the list of items and
 * item details side-by-side using two vertical panes.
 */
public class MovieGridActivity extends AppCompatActivity {

    /**
     * Whether or not the activity is in two-pane mode, i.e. running on a tablet
     * device.
     */
    private boolean mTwoPane;
    private static int MAX_API_PAGES = 3;
    private static int MOST_POPULAR_MODE = 0;
    private static int HIGHEST_RATED_MODE = 1;
    private static int FAVORITES_MODE = 2;
    public int mode = MOST_POPULAR_MODE;
    public List<String> modes = new ArrayList<>(Arrays.asList("Most Popular", "Highest Rated"));
    public JSONArray moviesArray;
    private SharedPreferences settings;
    private JSONArray favesAsJSONArray;
    public int selectedItem =0;
    public SimpleItemRecyclerViewAdapter recyclerViewAdapter;
    public JSONObject selectedMovieAsJSON;
    public ArrayAdapter<String> spinnerStringArrayAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movie_grid);

        //Check first that they've got a data connnection.
        try {
            if (!isConnected()){
                //System.out.println("Not connected.");

                //Lock orientation to prevent activity restart w/ dialog up
                if(getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
                    setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                } else setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

                new AlertDialog.Builder(this)
                        .setTitle("No Data Connection Found")
                        .setMessage("Please check your settings.")
                        .setPositiveButton("Settings", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {

                                //Again allow orientation changes
                                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);

                                startActivityForResult(new Intent(android.provider.Settings.ACTION_SETTINGS), 0);




                            }
                        })
                        .setNeutralButton("Exit", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                finish();
                            }
                        })
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .show();

            }else{
                Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
                setSupportActionBar(toolbar);
                toolbar.setTitle(getTitle());

                MovieDiscoverDataFetcher mAPIHelper = new MovieDiscoverDataFetcher(mode);
                mAPIHelper.execute();
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }





    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        System.out.println("Saving instance.");

        if (moviesArray != null){
            outState.putString("myJSON", moviesArray.toString());
        }
        outState.putInt("myMODE", mode);
        outState.putInt("mySelection", selectedItem);


    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        if (savedInstanceState != null){
            if (savedInstanceState.getString("myJSON") != null) {
                try {
                    moviesArray = new JSONArray(savedInstanceState.getString("myJSON"));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }else{
                MovieDiscoverDataFetcher mAPIHelper = new MovieDiscoverDataFetcher(mode);
                mAPIHelper.execute();
            }

            mode = savedInstanceState.getInt("myMODE");
            selectedItem = savedInstanceState.getInt("mySelection");

        }

    }

    @Override
    public void onResume(){
        super.onResume();

        refreshSpinner();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.menu, menu);
        MenuItem item = menu.findItem(R.id.spinner);
        Spinner spinner = (Spinner) MenuItemCompat.getActionView(item);
        spinnerStringArrayAdapter=
                new ArrayAdapter<String>(this,
                        android.R.layout.simple_spinner_dropdown_item,
                        modes);
        spinner.setAdapter(spinnerStringArrayAdapter);

        if (mode >= modes.size()){
            mode = 0; //Reset to 0 for case that user returns to activity after deleting all faves.
        }

        spinner.setSelection(mode);


        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            /**
             * Called when a new item is selected (in the Spinner)
             */
            public void onItemSelected(AdapterView<?> parent, View view,
                                       int pos, long id) {
                // An spinnerItem was selected. You can retrieve the selected item using
                // parent.getItemAtPosition(pos)

                Toast.makeText(getApplicationContext(), "Fetching " + parent.getItemAtPosition(pos).toString().toLowerCase() + " movies.", Toast.LENGTH_SHORT).show();

                mode = pos;


                if (mode == MOST_POPULAR_MODE || mode == HIGHEST_RATED_MODE) {
                    MovieDiscoverDataFetcher mAPIHelper = new MovieDiscoverDataFetcher(pos);
                    mAPIHelper.execute();
                }else {
                    //Check to see if fave has been added (in case in tablet layout).
                    String favesString = settings.getString("faves", null);
                    if (favesString != null) {//If some favorites have been marked
                        try {
                            favesAsJSONArray = new JSONArray(favesString);//Load the existing faves
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }

                    FavoriteMoveFetcher mAPIHelper = new FavoriteMoveFetcher(favesAsJSONArray);
                    mAPIHelper.execute();
                }

                //selectedItem = 0;


            }

            public void onNothingSelected(AdapterView<?> parent) {
                // Do nothing, just another required interface callback
            }

        });
        return super.onCreateOptionsMenu(menu);

    }

    private void setupRecyclerView(@NonNull RecyclerView recyclerView) {
        recyclerViewAdapter = new SimpleItemRecyclerViewAdapter();
        recyclerView.setAdapter(recyclerViewAdapter);

        if (mTwoPane){
            CustomParcelable mParcelable = null;
            try {
                mParcelable = prepareParcelable(selectedItem);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            makeTwoPaneDetailsFragment(mParcelable);
        }
        recyclerView.getLayoutManager().scrollToPosition(selectedItem);
    }

    public class SimpleItemRecyclerViewAdapter
            extends RecyclerView.Adapter<SimpleItemRecyclerViewAdapter.ViewHolder> {

        //private final List<DummyContent.DummyItem> mValues;
        private String BASE_URL = "http://image.tmdb.org/t/p/w185";


        //public SimpleItemRecyclerViewAdapter(List<DummyContent.DummyItem> items) {
        //    mValues = items;
        //}

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.movie_grid_content, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(final ViewHolder holder, final int position) {
            //holder.mItem = mValues.get(position);


            JSONObject movieObj = null;
            try {
                movieObj = moviesArray.getJSONObject(position);
                Picasso.with(getApplicationContext()).
                        load(BASE_URL + movieObj.getString("poster_path")).
                        into(holder.mPosterView, new com.squareup.picasso.Callback() {
                            @Override
                            public void onSuccess() {
                            }

                            @Override
                            public void onError() {
                                // TODO Auto-generated method stub

                            }
                        });
                if (mode == MOST_POPULAR_MODE){
                    holder.mRankingView.setText("Popularity: " + movieObj.getString("popularity"));
                }
                else if (mode == HIGHEST_RATED_MODE){
                    holder.mRankingView.setText("Avg. Rating: " + movieObj.getString("vote_average"));
                }
                else if (mode == FAVORITES_MODE){
                    holder.mRankingView.setText("\u2606");
                }

                if (position != selectedItem){
                    holder.mRankingView.setSelected(false);
                }else{
                    holder.mRankingView.setSelected(true);
                }
                holder.mRankingView.getBackground().setAlpha(128);

            } catch (JSONException e) {
                e.printStackTrace();
            }


            holder.mView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    notifyItemChanged(selectedItem);
                    holder.mRankingView.setSelected(true);
                    selectedItem = position;
                    try {
                        selectedMovieAsJSON = moviesArray.getJSONObject(position);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    /*
                    Bitmap bitmap = null;
                    ImageView iv = (ImageView) v.findViewById(R.id.posterViewGrid);
                    Drawable drawable = iv.getDrawable();
                    BitmapDrawable d = (BitmapDrawable) drawable;
                    bitmap = d.getBitmap();//Extract bitmap to send to detail activity/fragment.
                    */

                    CustomParcelable mParcelable = null;
                    try {
                        mParcelable = prepareParcelable(position);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    if (mTwoPane) {
                        makeTwoPaneDetailsFragment(mParcelable);

                    } else {


                        Context context = v.getContext();
                        Intent intent = new Intent(context, MovieDetailActivity.class);
                        intent.putExtra("mParcelable", mParcelable); // using the (String name, Parcelable value) overload!

                        context.startActivity(intent);
                    }
                }
            });
        }

        @Override
        public int getItemCount() {
            return moviesArray.length();
        }

        public class ViewHolder extends RecyclerView.ViewHolder {
            public final View mView;
            public final ImageView mPosterView;
            public final TextView mRankingView;
            //public DummyContent.DummyItem mItem;

            public ViewHolder(View view) {
                super(view);
                mView = view;
                mPosterView = (ImageView) view.findViewById(R.id.posterViewGrid);
                mRankingView = (TextView) view.findViewById(R.id.rankingTextView);


            }

            //@Override
            //public String toString() {
            //    return super.toString() + " '" + mContentView.getText() + "'";
            //}
        }
    }

    public class MovieDiscoverDataFetcher extends AsyncTask<String, Void, String[]> {
        HttpURLConnection urlConnection;
        Integer MODE;
        String BASE_URL = "http://api.themoviedb.org/3/discover/movie?";
        String POPULARITY_FLAG = "sort_by=popularity.desc";
        String RATINGS_FLAG = "sort_by=vote_average.desc&vote_average.gte=1&&vote_count.gte=100";
        String urlString;
        String[] jsonStrings = new String[MAX_API_PAGES];

        public MovieDiscoverDataFetcher(Integer mode) {
            MODE = mode;
        }

        @Override
        protected String[] doInBackground(String... args) {

            for (int i = 0; i < MAX_API_PAGES; i++) {//Query multiple pages from API so as to fill screen. (Smarter implementation could re-query on scroll.)

                if (MODE == MOST_POPULAR_MODE) {
                    urlString = BASE_URL + POPULARITY_FLAG + "&page=" + (i + 1) + "&api_key=" + getResources().getString(R.string.tmdb_api_key);
                } else {
                    urlString = BASE_URL + RATINGS_FLAG + "&page=" + (i + 1) + "&api_key=" + getResources().getString(R.string.tmdb_api_key);
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
                concatMoviesArray(result);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            try {
                setupGridViews();
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

    }

    public class FavoriteMoveFetcher extends AsyncTask<String, Void, String[]> {
        HttpURLConnection urlConnection;
        JSONArray ids;
        String BASE_URL = "https://api.themoviedb.org/3/movie/";
        String urlString;
        String[] jsonStrings;

        public FavoriteMoveFetcher(JSONArray ids) {
            this.ids = ids;
            jsonStrings = new String[ids.length()];
        }

        @Override
        protected String[] doInBackground(String... args) {

            for (int i = 0; i < ids.length(); i++) {//Query each of the fave ids.

                try {
                    urlString = BASE_URL + ids.getString(i) +
                            "?&api_key=" +
                            getResources().getString(R.string.tmdb_api_key);

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
                setupGridViews();
            } catch (JSONException e) {
                e.printStackTrace();
            }

        }

    }

    //Concat api results into one big array.
    private void concatMoviesArray(String[] result) throws JSONException {
        JSONObject moviesObj = null;
        moviesObj = new JSONObject(result[0]);

        moviesArray = moviesObj.getJSONArray("results");

        for (int i = 1; i < result.length; i++) {//Concat page results into one big array.
            moviesObj = new JSONObject(result[i]);

            JSONArray moviesArrayAdd = null;
            moviesArrayAdd = moviesObj.getJSONArray("results");

            for (int j = 0; j < moviesArrayAdd.length(); j++) {
                moviesArray.put(moviesArrayAdd.getJSONObject(j));
            }
        }
    }

    private void setupGridViews() throws JSONException {
        View recyclerView = findViewById(R.id.movie_grid);
        assert recyclerView != null;
        setupRecyclerView((RecyclerView) recyclerView);

        if (findViewById(R.id.movie_detail_container) != null) {
            // The detail container view will be present only in the
            // large-screen layouts (res/values-w560dp).
            // If this view is present, then the
            // activity should be in two-pane mode.
            mTwoPane = true;

        }

        ProgressBar pb = (ProgressBar)findViewById(R.id.gridViewProgressBar);
        pb.setVisibility(View.GONE);


    }

    public boolean isConnected() throws InterruptedException, IOException
    {
        ConnectivityManager cm =
                (ConnectivityManager)this.getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        return  activeNetwork != null &&
                activeNetwork.isConnectedOrConnecting();
    }

    private CustomParcelable prepareParcelable(int pos) throws JSONException {

        JSONObject movieObj = moviesArray.getJSONObject(pos);
        CustomParcelable mParcelable = new CustomParcelable(
                movieObj.getString("title"),
                movieObj.getString("overview"),
                movieObj.getString("popularity"),
                movieObj.getString("vote_average"),
                movieObj.getString("release_date"),
                movieObj.getString("id"),
                movieObj.toString() //Redundant data, but need the whole object for special case
                // of adding fav in dual pane mode, and the pre-extracted strings keep exceptions down in fragment.
                );

        return mParcelable;
    }

    private void makeTwoPaneDetailsFragment(CustomParcelable mParcelable){
        Bundle arguments = new Bundle();
        //arguments.putString(MovieDetailFragment.ARG_ITEM_ID, holder.mItem.id);
        MovieDetailFragment fragment = new MovieDetailFragment();
        arguments.putParcelable("mParcelable", mParcelable);
        arguments.putBoolean("toolbarExists", false); //Starting fragment directly so they'll be no toolbar.
        fragment.setArguments(arguments);
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.movie_detail_container, fragment)
                .commit();

        //TextView msg = (TextView) findViewById(R.id.dualPaneInstuctions);
        //msg.setVisibility(View.GONE);
    }

    public void refreshSpinner(){
        //Set spinner options to Discover modes only till we know if faves are saved.
        modes = new ArrayList<>(Arrays.asList("Most Popular", "Highest Rated"));

        PreferenceManager.getDefaultSharedPreferences(this);
        settings = PreferenceManager.getDefaultSharedPreferences(this);

        String favesString = settings.getString("faves", null);
        if (favesString != null) {//If some favorites have been marked
            try {
                favesAsJSONArray = new JSONArray(favesString);//Load the existing faves
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        if (favesAsJSONArray != null && favesAsJSONArray.length()>0){
            modes.add(2, "My Favorites"); //Make an Spinner option only if favorites have previously been saved.

        }
        invalidateOptionsMenu();
    }

}
