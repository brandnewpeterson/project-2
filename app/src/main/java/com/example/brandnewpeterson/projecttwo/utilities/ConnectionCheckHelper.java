package com.example.brandnewpeterson.projecttwo.utilities;

import android.app.Activity;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import java.io.IOException;

/**
 * Created by brandnewpeterson on 2/3/16.
 */
public class ConnectionCheckHelper {
    private Activity parentActivity;

    public ConnectionCheckHelper(Activity parentActivity) {
        this.parentActivity = parentActivity;
    }

    public boolean isConnected() throws InterruptedException, IOException
    {
        ConnectivityManager cm =
                (ConnectivityManager)parentActivity.getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        return  activeNetwork != null &&
                activeNetwork.isConnectedOrConnecting();
    }
}
