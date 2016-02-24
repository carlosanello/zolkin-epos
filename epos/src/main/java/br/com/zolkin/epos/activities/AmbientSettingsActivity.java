package br.com.zolkin.epos.activities;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.util.List;

import br.com.zolkin.epos.R;
import br.com.zolkin.epos.utils.Constants;
import br.com.zolkin.epos.utils.HttpAPI;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by Zolkin on 10/02/16.
 */
public class AmbientSettingsActivity extends AppCompatActivity {
    private EditText edDepartmentCode;
    private EditText edDepartmentToken;
    private Button btPOSLink;
    private HttpAPI.DepartmentStore departmentSettings;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ambientsettings);

        edDepartmentCode = (EditText) findViewById(R.id.edDepartmentCode);
        edDepartmentToken = (EditText) findViewById(R.id.edDepartmentToken);
        btPOSLink = (Button) findViewById(R.id.btPOSLink);

        edDepartmentCode.requestFocus();

        btPOSLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                HttpAPI api = HttpAPI.getInstance(getApplicationContext());
                Log.i(Constants.App,
                        "Codigo EC:" + edDepartmentCode.getText().toString() +
                                " Token:" + edDepartmentToken.getText().toString() +
                                " DeviceID:" + Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID));

                Call<HttpAPI.DepartmentStore> settings = api.getService()
                        .postInitializeDepartmentStore(
                                edDepartmentCode.getText().toString(),
                                edDepartmentToken.getText().toString(),
                                Settings.Secure.getString(getContentResolver(),
                                        Settings.Secure.ANDROID_ID));

                settings.enqueue(new Callback<HttpAPI.DepartmentStore>() {
                    @Override
                    public void onResponse(Response<HttpAPI.DepartmentStore> response) {
                        if (response.isSuccess()) {
                            departmentSettings = response.body();
                            if (departmentSettings != null) {
                                SharedPreferences preferences = getSharedPreferences(Constants.App, MODE_PRIVATE);
                                SharedPreferences.Editor editor = preferences.edit();

                                editor.putString(Constants.pos_user, departmentSettings.usuario);
                                editor.putString(Constants.pos_passwd, departmentSettings.senha);

                                editor.putInt(Constants.pos_department_id, departmentSettings.estabelecimento.id);
                                editor.putString(Constants.pos_department_name, departmentSettings.estabelecimento.nome);
                                editor.putString(Constants.pos_department_logo, departmentSettings.estabelecimento.logoImageUrl);

                                editor.putInt(Constants.pos_configuration_minexpenditure, departmentSettings.configuracoes.consumoMinimo);
                                editor.putInt(Constants.pos_configurarion_maxexpenditure, departmentSettings.configuracoes.consumoMaximo);
                                editor.putString(Constants.pos_configuration_customize, departmentSettings.configuracoes.campoCustomizavel);
                                editor.putBoolean(Constants.pos_configuration_changelocation, departmentSettings.configuracoes.permiteAlterarLocalizacao);
                                editor.putBoolean(Constants.pos_configuration_haveclerk, departmentSettings.configuracoes.possuiAtendente);
                                editor.putBoolean(Constants.pos_configuration_changedate, departmentSettings.configuracoes.permiteAlterarData);
                                editor.putBoolean(Constants.pos_configuration_divideaccount, departmentSettings.configuracoes.permiteDividirConta);
                                editor.putBoolean(Constants.pos_configuration_authdoc, departmentSettings.configuracoes.autenticaCpfSenha);
                                editor.putBoolean(Constants.pos_configuration_authqr, departmentSettings.configuracoes.autenticaQRCode);
                                editor.putBoolean(Constants.pos_configuration_totalvaluewithserv, departmentSettings.configuracoes.valorTotalComServico);
                                editor.putBoolean(Constants.pos_configuration_totalvaluewithouserv, departmentSettings.configuracoes.valorTotalSemServico);
                                editor.putBoolean(Constants.pos_configuration_totalvalueexserv,departmentSettings.configuracoes.valorTotalExcluiServico);
                                editor.putBoolean(Constants.pos_configuration_sendSMS, departmentSettings.configuracoes.geraComprovateSMS);

                                editor.putString(Constants.initalize_key, "1");

                                editor.apply();
                                finish();
                            }
                        } else {
                            switch (response.code()) {
                                case 404:
                                    Toast.makeText(AmbientSettingsActivity.this,
                                            "Token não encontrado, ou já expirado",
                                            Toast.LENGTH_SHORT).show();
                                    break;
                                case 409:
                                    Toast.makeText(AmbientSettingsActivity.this,
                                            "POS já vinculado ao estabelecimento",
                                            Toast.LENGTH_LONG).show();
                                    break;
                                default:
                                    break;
                            }
                        }
                    }

                    @Override
                    public void onFailure(Throwable t) {
                        Log.e(Constants.App + "- ERROR:", t.getMessage());
                    }
                });

            }
        });

    }
}
