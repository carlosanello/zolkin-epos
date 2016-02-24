package br.com.zolkin.epos.activities;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import br.com.zolkin.epos.utils.Constants;

/**
 * Created by Zolkin on 17/02/16.
 */
public class TransactionActivity extends AppCompatActivity {
    private SharedPreferences preferences;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        preferences = getSharedPreferences(Constants.App, MODE_PRIVATE);
    }
}
