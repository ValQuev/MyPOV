package fr.valquev.mypov.activities;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import fr.valquev.mypov.MyPOV;
import fr.valquev.mypov.MyPOVClient;
import fr.valquev.mypov.MyPOVResponse;
import fr.valquev.mypov.R;
import fr.valquev.mypov.User;
import fr.valquev.mypov.Utils;
import retrofit.Callback;
import retrofit.Response;
import retrofit.Retrofit;

/**
 * Created by ValQuev on 28/09/15.
 */
public class Login extends AppCompatActivity {

    private Context mContext;

    private EditText mMail;
    private EditText mPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login);

        mContext = this;

        if (new User(mContext).isLogged()) {
            exit();
            return;
        }

        mMail = (EditText) findViewById(R.id.login_mail_et);
        mPassword = (EditText) findViewById(R.id.login_passwd_et);

        findViewById(R.id.login_go_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(mMail.getText().toString().equals("")) {
                    mMail.setError("Vide");
                    return;
                }

                if(mPassword.getText().toString().equals("")) {
                    mPassword.setError("Vide");
                    return;
                }

                final ProgressDialog dialog = ProgressDialog.show(mContext, "Connexion en cours", "Chargement, veuillez patienter...", true);

                MyPOVClient.client.login(mMail.getText().toString(), Utils.getMd5Hash(mPassword.getText().toString())).enqueue(new Callback<MyPOVResponse<User>>() {
                    @Override
                    public void onResponse(Response<MyPOVResponse<User>> response, Retrofit retrofit) {
                        if (response.isSuccess()) {
                            if (response.body().getStatus() == 0) {
                                User user = response.body().getObject();
                                user.setPassword(Utils.getMd5Hash(mPassword.getText().toString()));
                                new User(mContext).regist(user);
                                exit();
                            } else {
                                Toast.makeText(mContext, response.body().getMessage(), Toast.LENGTH_LONG).show();
                            }
                        } else {
                            Toast.makeText(mContext, response.code() + " - " + response.message(), Toast.LENGTH_LONG).show();
                        }
                        dialog.cancel();
                    }

                    @Override
                    public void onFailure(Throwable t) {
                        Toast.makeText(mContext, t.getMessage(), Toast.LENGTH_LONG).show();
                        dialog.cancel();
                    }
                });
            }
        });

        findViewById(R.id.register_go_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(mContext, Register.class));
                finish();
            }
        });
    }

    private void exit() {
        startActivity(new Intent(mContext, MyPOV.class));
        finish();
    }
}
