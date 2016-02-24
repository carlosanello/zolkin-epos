package br.com.zolkin.epos.activities;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import br.com.zolkin.epos.R;
import br.com.zolkin.epos.utils.CheckList;
import br.com.zolkin.epos.utils.Constants;

/**
 * Created by Zolkin on 01/02/16.
 */
public class SplashActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        SharedPreferences preferences = getSharedPreferences(Constants.App, Context.MODE_PRIVATE);

        if (preferences.getBoolean(Constants.network_connection, false)) {
            Toast.makeText(
                    getApplicationContext(),
                    "O ePOS não pode trabalhar em offline, verifique sua conexão a internet e tente novamente",
                    Toast.LENGTH_LONG).show();
            System.exit(0);
        }

        if (!preferences.contains(Constants.network_key) || !preferences.contains(Constants.initalize_key)) {
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    Intent i = new Intent(SplashActivity.this, ConfigurationActivity.class);
                    startActivity(i);
                    finish();
                }
            }, 2500);
        } else {
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    Intent i = new Intent(SplashActivity.this, TransactionActivity.class);
                    startActivity(i);
                    finish();
                }
            }, 2500);
        }
    }
}
