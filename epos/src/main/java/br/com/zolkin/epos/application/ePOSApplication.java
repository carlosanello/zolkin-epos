package br.com.zolkin.epos.application;

import android.app.Application;
import android.content.ComponentName;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

import br.com.zolkin.epos.utils.Constants;
import br.com.zolkin.epos.utils.HttpAPI;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by Zolkin on 01/02/16.
 */
public class ePOSApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        SharedPreferences preferences = getSharedPreferences(Constants.App, MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        ConnectivityManager conn = (ConnectivityManager)
                getSystemService(CONNECTIVITY_SERVICE);
        NetworkInfo info = conn.getActiveNetworkInfo();
        if (info != null && info.isConnectedOrConnecting())
            editor.putBoolean(Constants.network_connection, true);
        else
            editor.putBoolean(Constants.network_connection, false);
    }
}
