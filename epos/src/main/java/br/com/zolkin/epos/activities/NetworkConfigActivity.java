package br.com.zolkin.epos.activities;

import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import java.util.List;

import br.com.zolkin.epos.R;
import br.com.zolkin.epos.utils.Constants;
import br.com.zolkin.epos.utils.HttpAPI;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by Zolkin on 05/02/16.
 */
public class NetworkConfigActivity extends AppCompatActivity {
    private RadioGroup radiogroup_networks;
    private Button saveconfig;
    private List<HttpAPI.Ambients> sites;
    private int checkedRow;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_networkconfig);

        radiogroup_networks = (RadioGroup) findViewById(R.id.radiogroup_networks);
        saveconfig = (Button) findViewById(R.id.saveconfig);

        HttpAPI api = HttpAPI.getInstance(getApplicationContext());
        Call<List<HttpAPI.Ambients>> ambients = api.getService().getAmbients();
        ambients.enqueue(new Callback<List<HttpAPI.Ambients>>() {
            @Override
            public void onResponse(Response<List<HttpAPI.Ambients>> response) {
                if (response.isSuccess()) {
                    sites = response.body();
                    for(int row = 0; row < sites.size(); row++) {

                        RadioButton rdbtn = new RadioButton(NetworkConfigActivity.this);
                        rdbtn.setId((row * 2) + row);
                        rdbtn.setText(String.valueOf(row + 1) + " - " + sites.get(row).nome);
                        rdbtn.setTextColor(getResources().getColor(R.color.identity_green));
                        rdbtn.setTextSize(22);
                        rdbtn.setTypeface(null, Typeface.BOLD);

                        radiogroup_networks.addView(rdbtn);
                    }
                }
            }

            @Override
            public void onFailure(Throwable t) {
                Log.e(Constants.App, t.getMessage());
            }
        });

        radiogroup_networks.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                checkedRow = checkedId;
            }
        });

        saveconfig.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i(Constants.App, sites.get(checkedRow).nome);
                SharedPreferences preferences = getSharedPreferences(Constants.App, MODE_PRIVATE);
                SharedPreferences.Editor editor = preferences.edit();
                editor.putString(Constants.network_key, sites.get(checkedRow).url);
                editor.apply();
                finish();

            }
        });
    }
}
