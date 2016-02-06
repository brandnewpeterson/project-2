package com.example.brandnewpeterson.projecttwo.adapters;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.brandnewpeterson.projecttwo.CustomParcelable;
import com.example.brandnewpeterson.projecttwo.MovieDetailActivity;
import com.example.brandnewpeterson.projecttwo.MovieGridActivity;
import com.example.brandnewpeterson.projecttwo.R;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by brandnewpeterson on 2/3/16.
 */
public class CustomRecyclerAdapter extends RecyclerView.Adapter<CustomRecyclerAdapter.ViewHolder> {

    private String BASE_URL = "http://image.tmdb.org/t/p/w185";
    private JSONArray moviesArray;
    private Activity parentActivity;
    int mode;
    int selectedItem;
    Boolean mTwoPane;

    public CustomRecyclerAdapter(JSONArray moviesArray, Activity parentActivity, Boolean mTwoPane) {
        this.moviesArray = moviesArray;
        this.parentActivity = parentActivity;
        int mode = ((MovieGridActivity) parentActivity).mode;
        int selectedItem = ((MovieGridActivity) parentActivity).selectedItem;
        this.mTwoPane = mTwoPane;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.movie_grid_content, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        JSONObject movieObj = null;
        try {
            movieObj = moviesArray.getJSONObject(position);
            Picasso.with(parentActivity.getApplicationContext()).
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



            if (mode == ((MovieGridActivity) parentActivity).MOST_POPULAR_MODE){
                holder.mRankingView.setText("Popularity: " + movieObj.getString("popularity"));
            }
            else if (mode == ((MovieGridActivity) parentActivity).HIGHEST_RATED_MODE){
                holder.mRankingView.setText("Avg. Rating: " + movieObj.getString("vote_average"));
            }
            else if (mode == ((MovieGridActivity) parentActivity).FAVORITES_MODE){
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
                    ((MovieGridActivity) parentActivity).selectedMovieAsJSON = moviesArray.getJSONObject(position);
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                CustomParcelable mParcelable = null;
                try {
                    mParcelable = ((MovieGridActivity) parentActivity).prepareParcelable(position);
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                if (mTwoPane) {
                    ((MovieGridActivity) parentActivity).makeTwoPaneDetailsFragment(mParcelable);

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

    }


}


