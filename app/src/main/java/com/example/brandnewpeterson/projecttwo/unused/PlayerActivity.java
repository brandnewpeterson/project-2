package com.example.brandnewpeterson.projecttwo.unused;

import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;

import com.example.brandnewpeterson.projecttwo.R;

public class PlayerActivity extends AppCompatActivity {
    private String API_KEY;
    private String VID_ID;
    private String NAME;

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("API_KEY", API_KEY);
        outState.putString("VID_ID", VID_ID);
        outState.putString("NAME", NAME);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        if (savedInstanceState != null){
            API_KEY = savedInstanceState.getString("API_KEY");
            VID_ID = savedInstanceState.getString("VID_ID");
            NAME = savedInstanceState.getString("NAME");
        }

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player);

        Bundle extras = getIntent().getExtras();
        if (extras != null){
            API_KEY = extras.getString("key");
            VID_ID = extras.getString("id");
            NAME = extras.getString("name");

        }


        //Show the Up button in the action bar.
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setTitle(NAME);
        }

        if (savedInstanceState == null) {
            // Create the detail fragment and add it to the activity
            // using a fragment transaction.
            Bundle arguments = new Bundle();
            arguments.putString("key", API_KEY);
            arguments.putString("id", VID_ID);
            PlayerFragment fragment = new PlayerFragment();
            fragment.setArguments(arguments);
            FragmentManager manager = getSupportFragmentManager();
            manager.beginTransaction()
                    .replace(R.id.playerFragmentContainer, fragment)
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
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

}
