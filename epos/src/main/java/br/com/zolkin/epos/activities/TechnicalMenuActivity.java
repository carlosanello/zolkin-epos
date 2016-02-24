package br.com.zolkin.epos.activities;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.AvoidXfermode;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.net.UnknownHostException;

import br.com.zolkin.epos.R;
import br.com.zolkin.epos.utils.Constants;
import br.com.zolkin.epos.utils.HttpAPI;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by Zolkin on 04/02/16.
 */
public class TechnicalMenuActivity extends AppCompatActivity {
    private Button network_config;
    private Button initalize_department;
    private Button unlink_department;
    private Button tests;
    private Button logs;
    private SharedPreferences preferences;

    private static final String alertMessageTechMenu = "Esta operação irá reiniciar todo o app ePOS. Você deseja continuar?";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_technicalmenu);

        network_config = (Button) findViewById(R.id.network_config);
        initalize_department = (Button) findViewById(R.id.initalize_department);
        unlink_department = (Button) findViewById(R.id.unlink_department);
        tests = (Button) findViewById(R.id.tests);
        logs = (Button) findViewById(R.id.logs);

        preferences = getSharedPreferences(Constants.App, MODE_PRIVATE);
        if (!preferences.contains(Constants.network_key) || !preferences.contains(Constants.initalize_key)) {
            unlink_department.setVisibility(View.INVISIBLE);
            tests.setVisibility(View.INVISIBLE);
            logs.setVisibility(View.INVISIBLE);
        }

        initalize_department.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (preferences.contains(Constants.initalize_key)) {
                    Toast.makeText(
                            getApplicationContext(),
                            "POS já inicializado, devincule para uma nova configuração",
                            Toast.LENGTH_LONG).show();
                    return;
                }


                if (!preferences.contains(Constants.network_key)) {
                    createAlert();
                } else if (!preferences.contains(Constants.initalize_key)) {
                    Intent i = new Intent(TechnicalMenuActivity.this, AmbientSettingsActivity.class);
                    startActivity(i);
                } else {
                    AlertDialog alert;
                    final AlertDialog.Builder builder = new AlertDialog.Builder(TechnicalMenuActivity.this);
                    builder.setTitle("Menu Técnico");
                    builder.setMessage("O POS já esta inicializado, para que o mesmo seja reinializado, desvincule o mesmo do estabelecimento")
                            .setCancelable(false)
                            .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    //TODO DO NOTHING
                                }
                            });
                    alert = builder.create();
                    alert.show();
                }
            }
        });

        network_config.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(TechnicalMenuActivity.this, NetworkConfigActivity.class);
                startActivity(i);
            }
        });

        unlink_department.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final SharedPreferences preferences = getSharedPreferences(Constants.App, MODE_PRIVATE);
                final String departmentId, posId;
                departmentId = String.valueOf(preferences.getInt(Constants.pos_department_id, 0));
                posId = Settings.Secure.getString(getContentResolver(),
                        Settings.Secure.ANDROID_ID);

                AlertDialog alert;
                final AlertDialog.Builder builder = new AlertDialog.Builder(TechnicalMenuActivity.this);
                builder.setTitle("Menu Técnico");
                builder.setMessage(alertMessageTechMenu)
                        .setCancelable(false)
                        .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                preferences.edit().clear().apply();
                                HttpAPI api = HttpAPI.getInstance(getApplicationContext());
                                Call<Void> unlink =
                                        api.getService().postUnlinkDepartment(departmentId, posId);
                                unlink.enqueue(new Callback<Void>() {
                                    @Override
                                    public void onResponse(Response<Void> response) {
                                        if (response.isSuccess())
                                            Log.i(Constants.App, "Response: " + response.code());
                                            System.exit(0);
                                    }

                                    @Override
                                    public void onFailure(Throwable t) {
                                        Log.e(Constants.App, t.getMessage());
                                    }
                                });
                            }
                        })
                        .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                //TODO DO NOTHING
                            }
                        });

                alert = builder.create();
                alert.show();

            }
        });
    }

    @Override
    protected void onPostResume() {
        super.onPostResume();
        if (preferences.contains(Constants.network_key) && preferences.contains(Constants.initalize_key)) {
            unlink_department.setVisibility(View.VISIBLE);
            tests.setVisibility(View.VISIBLE);
            logs.setVisibility(View.VISIBLE);
        }
    }

    protected void createAlert() {
        AlertDialog alert;
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Menu Técnico");
        builder.setMessage(R.string.alerttechmenu)
                .setCancelable(false)
                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //TODO DO NOTHING
                    }
                });
        alert = builder.create();
        alert.show();
    }
}
