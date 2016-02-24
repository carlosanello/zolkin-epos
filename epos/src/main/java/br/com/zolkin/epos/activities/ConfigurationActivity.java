package br.com.zolkin.epos.activities;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.inputmethodservice.Keyboard;
import android.media.session.MediaSession;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
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
 * Created by Zolkin on 03/02/16.
 */
public class ConfigurationActivity extends AppCompatActivity {
    private EditText edSenha;
    private Button initialize;
    private boolean valid = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_configuration);

        edSenha = (EditText) findViewById(R.id.edSenha);
        edSenha.setTransformationMethod(PasswordTransformationMethod.getInstance());


        initialize = (Button) findViewById(R.id.initialize);
        initialize.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0);

                HttpAPI api = HttpAPI.getInstance(getApplicationContext());
                api.addHeader("Content-Type", "application/x-www-form-urlencoded");

                Call<Void> init = api.getService().postData(edSenha.getText().toString());
                init.enqueue(new Callback<Void>() {
                    @Override
                    public void onResponse(Response<Void> response) {
                        if (response.isSuccess()) {
                            Intent i = new Intent(getApplicationContext(), TechnicalMenuActivity.class);
                            startActivity(i);
                        } else {
                            AlertDialog.Builder dialog = new AlertDialog.Builder(ConfigurationActivity.this);
                            dialog.setTitle(R.string.technicaldialog_title);
                            dialog.setMessage(R.string.messege_technicalpassword)
                                    .setCancelable(false)
                                    .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            edSenha.setText(null);
                                            edSenha.requestFocus();
                                        }
                                    });

                            AlertDialog alert = dialog.create();
                            alert.show();

                        }
                    }

                    public void onFailure(Throwable t) {
                        Log.e(Constants.App + "Erro:", t.getMessage());
                    }
                });
            }
        });
    }
}
