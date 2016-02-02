package com.example.brandnewpeterson.projecttwo;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.squareup.picasso.Picasso;

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
 * A fragment representing a single Movie detail screen.
 * This fragment is either contained in a {@link MovieGridActivity}
 * in two-pane mode (on tablets) or a {@link MovieDetailActivity}
 * on handsets.
 */
public class MovieDetailFragment extends Fragment {

    private CustomParcelable mParcelable;
    private boolean toolbarExists = true;
    private String movieID;
    private View rootView;
    private Activity activity;
    public static final int VIDEO_RETRIEVAL_MODE = 0;
    public static final int REVIEW_RETRIEVAL_MODE = 1;
    public static final int BACKDROP_RETRIEVAL_MODE = 3;

    private SharedPreferences settings;
    /**
     * The fragment argument representing the item ID that this fragment
     * represents.
     */
    //public static final String ARG_ITEM_ID = "item_id";

    /**
     * The dummy content this fragment is presenting.
     */
    // private DummyContent.DummyItem mItem;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public MovieDetailFragment() {
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mParcelable = (CustomParcelable) getArguments().getParcelable("mParcelable");
        toolbarExists = (boolean) getArguments().getBoolean("toolbarExists");


        PreferenceManager.getDefaultSharedPreferences(getActivity());
        settings = PreferenceManager.getDefaultSharedPreferences(getActivity());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.movie_detail, container, false);

        final RelativeLayout rl = (RelativeLayout) rootView.findViewById(R.id.noToolbarDetailsContainer);


        String urlStringVideo = "http://api.themoviedb.org/3/movie/" + mParcelable.id +
                "/videos?" + "&api_key=" + getResources().getString(R.string.tmdb_api_key);
        new MultiFetcher(urlStringVideo, VIDEO_RETRIEVAL_MODE).execute();

        String urlStringReview = "http://api.themoviedb.org/3/movie/" + mParcelable.id +
                "/reviews?" + "&api_key=" + getResources().getString(R.string.tmdb_api_key);
        new MultiFetcher(urlStringReview, REVIEW_RETRIEVAL_MODE).execute();

        if (!toolbarExists){//Set up some extra views if fragment not hosted by own activity.
            //System.out.println("FD No toolbar exists.");
            rl.setVisibility(View.VISIBLE);

            String urlStringImages = "http://api.themoviedb.org/3/movie/" + mParcelable.id +
                    "/images?" + "&api_key=" + getResources().getString(R.string.tmdb_api_key);
            new MultiFetcher(urlStringImages, BACKDROP_RETRIEVAL_MODE).execute();


            TextView title = (TextView) rootView.findViewById(R.id.titleViewDetailNoToolbar);
            title.setText(mParcelable.title);

        }


        TextView synopsis = (TextView)rootView.findViewById(R.id.detailSynopsisContent);
        synopsis.setText(mParcelable.synopsis);

        TextView rank = (TextView)rootView.findViewById(R.id.detailPopularityContent);
        rank.setText(mParcelable.rank);

        TextView rating = (TextView)rootView.findViewById(R.id.detailRatingContent);
        rating.setText(mParcelable.rating+"/10");

        TextView year = (TextView)rootView.findViewById(R.id.detailDateContent);
        year.setText(mParcelable.year);

        final ToggleButton tb = (ToggleButton)rootView.findViewById(R.id.faveToggle);

        JSONArray favesAsJSONArray = null;
        String oldFavesString = settings.getString("faves", null);
        if (oldFavesString != null) {//If some favorites have been marked
            try {
                favesAsJSONArray = new JSONArray(oldFavesString);//Load the existing faves
            } catch (JSONException e) {
                e.printStackTrace();
            }

            for (int i = 0; i < favesAsJSONArray.length(); i++) {
                try {
                    if (favesAsJSONArray.getString(i).contains(mParcelable.id)) {
                        //System.out.println("Found match.");
                        tb.setChecked(true);
                        tb.refreshDrawableState();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                } {

                }
            }
        }


        tb.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                JSONArray favesAsJSONArray = null;
                String oldFavesString = settings.getString("faves", null);
                if (oldFavesString != null) {
                    try {
                        favesAsJSONArray = new JSONArray(oldFavesString);//Load the existing faves
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                } else {//If there aren't any, start from scratch.
                    favesAsJSONArray = new JSONArray();
                }

                if ((tb.getText().toString().contains("Un"))) {
                    favesAsJSONArray.put(mParcelable.id);
                    settings.edit().putString("faves", favesAsJSONArray.toString()).commit(); // Saved to prefs
                    Toast.makeText(getActivity(), "Saved " + mParcelable.title + " as a favorite.", Toast.LENGTH_SHORT).show();
                    if (!toolbarExists) {//Clean up the grid so it shows the new fave.
                        MovieGridActivity parentActivity = (MovieGridActivity) getActivity();
                        if (parentActivity.mode == 2){
                            try {
                                parentActivity.moviesArray.put(new JSONObject (mParcelable.movieObjToString));
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                        if (parentActivity.spinnerStringArrayAdapter.getCount() == 2){
                            parentActivity.modes.add(2, "My Favorites");//Add option to spinner now that there's a saved fave.
                            parentActivity.spinnerStringArrayAdapter.notifyDataSetChanged();
                        }

                    }

                } else {
                    for (int i = 0; i < favesAsJSONArray.length(); i++) {
                        try {
                            if (favesAsJSONArray.getString(i).contains(mParcelable.id)) {
                                favesAsJSONArray.remove(i);
                                settings.edit().putString("faves", favesAsJSONArray.toString()).commit(); // Removed from saved faves.
                                Toast.makeText(getActivity(), "Removed " + mParcelable.title + " from favorites.", Toast.LENGTH_SHORT).show();

                                if (!toolbarExists) {//Clean up the grid so it doesn't show the item as a fave anymore.
                                    MovieGridActivity parentActivity = (MovieGridActivity) getActivity();

                                    if (parentActivity.mode == 2){
                                        parentActivity.moviesArray.remove(parentActivity.selectedItem);
                                        parentActivity.recyclerViewAdapter.notifyDataSetChanged();

                                        if (parentActivity.moviesArray.length() == 0){
                                            Intent intent = getActivity().getIntent();
                                            getActivity().overridePendingTransition(0, 0);
                                            getActivity().finish();

                                            getActivity().overridePendingTransition(0, 0);
                                            startActivity(intent); //Reset parent so as to repopulate grid.
                                        }
                                    }
                                }
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        {

                        }
                    }
                }

                //System.out.println("faves: " + favesAsJSONArray.toString());


            }
        });

        return rootView;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

    public class MultiFetcher extends AsyncTask<String, Void, String> {
        HttpURLConnection urlConnection;
        String urlString;
        int mode;

        public MultiFetcher(String urlString, int mode) {
            this.urlString = urlString;
            this.mode = mode;

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

            if (mode==VIDEO_RETRIEVAL_MODE){
                try {
                    setupTrailerViews(result);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }else if (mode==REVIEW_RETRIEVAL_MODE){
                try {
                    setupReviewViews(result);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }else{
                try {
                    setupBackdropViews(result);
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }

        }

    }

    private void setupTrailerViews(String objString) throws JSONException {

        final Context mContext = rootView.getContext();

        JSONObject obj = new JSONObject(objString);
        JSONArray trailers = obj.getJSONArray("results");

        if (trailers.length() > 0){

            for (int i = 0; i < trailers.length(); i++) {
                final JSONObject trailer = trailers.getJSONObject(i);

                if (trailer.getString("site").contains("YouTube")) {

                    TextView trailerHeading = (TextView) rootView.findViewById(R.id.detailTrailersHeading);
                    trailerHeading.setVisibility(View.VISIBLE);

                    TableLayout tl = (TableLayout) rootView.findViewById(R.id.detailTrailersTable);
                    tl.setVisibility(View.VISIBLE);

                    View line = (View) rootView.findViewById(R.id.detailTrailersLine);
                    line.setVisibility(View.VISIBLE);

                    TableRow tr = new TableRow(mContext);
                    tr.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT));

                    //Button
                    final ImageButton b = new ImageButton(mContext);
                    b.setImageResource(R.drawable.ic_action_av_play_arrow);
                    b.setBackgroundColor(0x00000000);//Transparent
                    b.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT));
                    //System.out.println("Key: " + trailer.getString("key"));
                    //System.out.println("ID: " + trailer.getString("id"));

                    b.setOnClickListener(new View.OnClickListener() {
                        private View v;

                        @Override
                        public void onClick(View v) {
                            this.v = v;
                            Intent intent = new Intent(mContext, PlayerActivity.class);
                            try {
                                intent.putExtra("id", trailer.getString("key")); // Deliberately reversed as API uses reverse terminology to YouTube API.
                                intent.putExtra("key", trailer.getString("id"));
                                intent.putExtra("name", trailer.getString("name"));

                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                            mContext.startActivity(intent);
                        }
                    });

                    tr.addView(b);

                    //Text
                    int p = (int) (getResources().getDimension(R.dimen.table_text_padding));

                    TextView tv = new TextView(mContext);
                    tv.setText(trailer.getString("name"));
                    tv.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.MATCH_PARENT));
                    tv.setGravity(Gravity.CENTER_VERTICAL);
                    tv.setPadding(p, p, p, p);
                    tr.addView(tv);

                    tl.addView(tr, new TableLayout.LayoutParams(TableLayout.LayoutParams.MATCH_PARENT, TableLayout.LayoutParams.WRAP_CONTENT));

                }
            }

        }

    }


    private void setupReviewViews(String objString) throws JSONException {

        final Context mContext = rootView.getContext();

        JSONObject obj = new JSONObject(objString);
        JSONArray reviews = obj.getJSONArray("results");

        if (reviews.length() > 0){

            for (int i = 0; i < reviews.length(); i++) {
                final JSONObject review = reviews.getJSONObject(i);

                if (review.getString("author").length() > 0 &&
                        review.getString("content").length() > 0 &&
                        review.getString("url").length() > 0
                        ) {

                    TextView reviewsHeading = (TextView) rootView.findViewById(R.id.detailReviewsHeading);
                    reviewsHeading.setVisibility(View.VISIBLE);

                    TableLayout tl = (TableLayout) rootView.findViewById(R.id.detailReviewsTable);
                    tl.setVisibility(View.VISIBLE);

                    TableRow tr = new TableRow(mContext);
                    tr.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT));
                    int p = (int) (getResources().getDimension(R.dimen.table_text_padding));

                    //Author
                    TextView tv1 = new TextView(mContext);
                    tv1.setText(review.getString("author"));
                    tv1.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.MATCH_PARENT));
                    tv1.setGravity(Gravity.CENTER_VERTICAL|Gravity.LEFT);
                    tv1.setPadding(p, p, p, p);

                    tr.addView(tv1);

                    //Comment
                    String comment = review.getString("content");

                    //Strip HTML cruft
                    comment = comment.replaceAll("<(.*?)\\>", " ");//Removes all items in brackets
                    comment = comment.replaceAll("<(.*?)\\\n", " ");//Must be undeneath
                    comment = comment.replaceFirst("(.*?)\\>", " ");//Removes any connected item to the last bracket
                    comment = comment.replaceAll("&nbsp;", " ");
                    comment = comment.replaceAll("&amp;", " ");

                    //System.out.println("Full comment: " + comment);

                    //Keep 25 words only, if longer

                    String[] words = comment.split(" ");

                    Boolean condensed = false;

                    if (words.length > 25){
                        condensed = true;

                        String[] keeperWords = new String[24];
                        System.arraycopy(words, 0, keeperWords, 0, 24);

                        comment = "";

                        for (int j = 0; j < keeperWords.length; j++){
                            comment = comment + keeperWords[j] + " ";
                        }
                        comment = comment.trim();
                        comment = comment + "... ";


                    }

                    TextView tv2 = new TextView(mContext);
                    tv2.setText(comment);
                    tv2.setTypeface(tv2.getTypeface(), Typeface.ITALIC);
                    tv2.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.MATCH_PARENT));
                    tv2.setGravity(Gravity.CENTER_VERTICAL | Gravity.LEFT);
                    tv2.setPadding(p, p, p, p);

                    tr.addView(tv2);

                    final String url = review.getString("url");

                    if (condensed && url != null && url.contains("http")){
                        //Button
                        final ImageButton b = new ImageButton(mContext);
                        b.setImageResource(R.drawable.ic_action_navigation_more_horiz);
                        b.setBackgroundColor(0x00000000);//Transparent
                        b.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.MATCH_PARENT));

                        //Bring up web link to full review.
                        b.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Uri uri = null;
                                uri = Uri.parse(url);
                                Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                                startActivity(intent);
                            }
                        });
                        tr.addView(b);
                    }


                    tl.addView(tr, new TableLayout.LayoutParams(TableLayout.LayoutParams.MATCH_PARENT, TableLayout.LayoutParams.WRAP_CONTENT));

                }
            }

        }

    }

    private void setupBackdropViews(String objString) throws JSONException {

        ImageView poster = (ImageView) rootView.findViewById(R.id.posterViewDetailNoToolbar);
        String urlBaseString = "http://image.tmdb.org/t/p/w780/";


        String backdropPath = getHighestRatedBackdropPath(objString);

                if (backdropPath != null) {//A backdrop exists so load it.

                    Picasso.with(getActivity()).
                            load(urlBaseString + backdropPath).
                            into(poster, new com.squareup.picasso.Callback() {
                                @Override
                                public void onSuccess() {
                                }

                                @Override
                                public void onError() {
                                    // TODO Auto-generated method stub

                                }
                            });                }

                //else {//Just use cropped poster. Looks shite, but better than nothing.
                //    poster.setImageBitmap(mParcelable.poster);
                //}

        if (!toolbarExists){
            //Scroll to bottom so we can see image to start.
            ScrollView parentSV = ((ScrollView) getView().getParent());
            if (parentSV != null){
                parentSV.scrollTo(0, parentSV.getTop());
                parentSV.invalidate();
                parentSV.requestLayout();
            }

        }
    }

    public String getHighestRatedBackdropPath(String objString) throws JSONException {
        JSONObject obj = new JSONObject(objString);
        //System.out.println("Backdrop obj:" + obj.toString());
        JSONArray backdrops = obj.getJSONArray("backdrops");
        String backdropPath = null;

        if (backdrops.length()>0){
            int maxVotes = 0;
            int pos = 0;

            for (int i = 0; i < backdrops.length(); i++){
                JSONObject o = backdrops.getJSONObject(i);
                int votes = o.getInt("vote_count");
                if (votes > maxVotes){
                    maxVotes = votes;
                    pos = i;
                    backdropPath = o.getString("file_path");
                }
            }


        }

        return backdropPath;
    }


}
