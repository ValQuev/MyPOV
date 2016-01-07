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
 * Created by ValQuev on 06/01/2016.
 */
public class Register extends AppCompatActivity {

    private Context mContext;

    private EditText mPseudo;
    private EditText mMail;
    private EditText mPassword1;
    private EditText mPassword2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.register);

        mContext = this;

        mPseudo = (EditText) findViewById(R.id.regist_pseudo);
        mMail = (EditText) findViewById(R.id.regist_mail);
        mPassword1 = (EditText) findViewById(R.id.regist_pwd1);
        mPassword2 = (EditText) findViewById(R.id.regist_pwd2);

        findViewById(R.id.btn_signup).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mPseudo.getText().toString().equals("")) {
                    mPseudo.setError("Vide");
                    return;
                }

                if(mMail.getText().toString().equals("")) {
                    mMail.setError("Vide");
                    return;
                }

                if(mPassword1.getText().toString().equals("")) {
                    mPassword1.setError("Vide");
                    return;
                }

                if(mPassword2.getText().toString().equals("")) {
                    mPassword2.setError("Vide");
                    return;
                }

                if(!mPassword2.getText().toString().equals(mPassword1.getText().toString())) {
                    mPassword1.setError("Les mots de passe ne correspondent pas");
                    mPassword2.setError("Les mots de passe ne correspondent pas");
                    return;
                }

                final ProgressDialog dialog = ProgressDialog.show(mContext, "Inscription en cours", "Chargement, veuillez patienter...", true);

                MyPOVClient.client.register(mPseudo.getText().toString(), mMail.getText().toString(), Utils.getMd5Hash(mPassword1.getText().toString()), Utils.getMd5Hash(mPassword2.getText().toString())).enqueue(new Callback<MyPOVResponse<User>>() {
                    @Override
                    public void onResponse(Response<MyPOVResponse<User>> response, Retrofit retrofit) {
                        if (response.isSuccess()) {
                            if (response.body().getStatus() == 0) {
                                User user = response.body().getObject();
                                user.setPassword(Utils.getMd5Hash(mPassword1.getText().toString()));
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

        findViewById(R.id.btn_login).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(mContext, Login.class));
                finish();
            }
        });
    }

    private void exit() {
        startActivity(new Intent(mContext, MyPOV.class));
        finish();
    }
}
