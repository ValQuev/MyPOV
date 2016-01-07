package fr.valquev.mypov.fragments;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import fr.valquev.mypov.MyPOV;
import fr.valquev.mypov.MyPOVClient;
import fr.valquev.mypov.MyPOVResponse;
import fr.valquev.mypov.R;
import fr.valquev.mypov.User;
import fr.valquev.mypov.Utils;
import fr.valquev.mypov.activities.Login;
import fr.valquev.mypov.activities.ObservationDetails;
import retrofit.Callback;
import retrofit.Response;
import retrofit.Retrofit;

/**
 * Created by juleno on 14/12/2015.
 */
public class Settings extends PreferenceFragmentCompat {
    private Context mContext;
    private User mUser;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mContext = getActivity();
        mUser = new User(mContext);

        if (!mUser.isLogged()) {
            startActivity(new Intent(mContext, Login.class));
            ((ObservationDetails) mContext).finish();
        }

        addPreferencesFromResource(R.xml.settings);

        findPreference("pref_change_passwd").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                AlertDialog.Builder builder = new AlertDialog.Builder(mContext, R.style.AlertDialogStyle);
                builder.setTitle("Modifier mon mot de passe");
                final View v = getActivity().getLayoutInflater().inflate(R.layout.dialog_update_password, null);
                builder.setView(v);
                builder.setNegativeButton("Annuler", null);
                builder.setPositiveButton("Modifier", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(final DialogInterface dialog, int which) {
                        EditText currentPassWD = (EditText) v.findViewById(R.id.dialog_updatepwd_currentpasswd);
                        EditText newPassWD = (EditText) v.findViewById(R.id.dialog_updatepwd_newpasswd);
                        EditText newPassWD2 = (EditText) v.findViewById(R.id.dialog_updatepwd_newpasswd2);

                        String current = currentPassWD.getText().toString();

                        if (current.equals("")) {
                            currentPassWD.setError("Vide");
                            return;
                        }

                        final String newPass = newPassWD.getText().toString();

                        if (newPass.equals("")) {
                            newPassWD.setError("Vide");
                            return;
                        }

                        String newPass2 = newPassWD2.getText().toString();

                        if (newPass2.equals("")) {
                            newPassWD2.setError("Vide");
                            return;
                        }

                        if (!newPass.equals(newPass2)) {
                            newPassWD.setError("Les mots de passe ne correspondent pas");
                            newPassWD2.setError("Les mots de passe ne correspondent pas");
                            Toast.makeText(mContext, "Les mots de passe ne correspondent pas", Toast.LENGTH_LONG).show();
                            return;
                        }

                        final ProgressDialog dialogupdate = ProgressDialog.show(mContext, "Changement de mot de passe", "Chargement, veuillez patienter...", true);
                        MyPOVClient.client.updatePassword(mUser.getMail(), mUser.getPassword(), Utils.getMd5Hash(current), Utils.getMd5Hash(newPass), Utils.getMd5Hash(newPass2)).enqueue(new Callback<MyPOVResponse<String>>() {
                            @Override
                            public void onResponse(Response<MyPOVResponse<String>> response, Retrofit retrofit) {
                                if (response.isSuccess()) {
                                    if (response.body().getStatus() == 0) {
                                        mUser.updatePassword(Utils.getMd5Hash(newPass));
                                        Toast.makeText(mContext, "Mot de passe modifié avec succès !", Toast.LENGTH_LONG).show();
                                    } else {
                                        Toast.makeText(mContext, response.body().getMessage(), Toast.LENGTH_LONG).show();
                                    }
                                } else {
                                    Toast.makeText(mContext, response.code() + " - " + response.message(), Toast.LENGTH_LONG).show();
                                }
                                dialogupdate.cancel();
                            }

                            @Override
                            public void onFailure(Throwable t) {
                                Toast.makeText(mContext, t.getMessage(), Toast.LENGTH_LONG).show();
                                dialogupdate.cancel();
                            }
                        });
                    }
                });
                builder.show();
                return true;
            }
        });

        findPreference("pref_logout").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                mUser.logout();
                startActivity(new Intent(mContext, Login.class));
                ((MyPOV) mContext).finish();
                Toast.makeText(mContext, "Déconnecté", Toast.LENGTH_SHORT).show();
                return true;
            }
        });

        findPreference("pref_del_account").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                AlertDialog.Builder builder = new AlertDialog.Builder(mContext, R.style.AlertDialogStyle);
                builder.setTitle("Supprimer mon compte");
                builder.setMessage("Attention ! Il est impossible de restaurer un compte supprimée. Êtes-vous bien sûr de vouloir supprimer votre compte ?");
                builder.setNegativeButton("Non", null);
                builder.setPositiveButton("Oui, supprimer", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        final ProgressDialog dialogdel = ProgressDialog.show(mContext, "Suppression en cours", "Chargement, veuillez patienter...", true);
                        MyPOVClient.client.delAccount(mUser.getMail(), mUser.getPassword()).enqueue(new Callback<MyPOVResponse<String>>() {
                            @Override
                            public void onResponse(Response<MyPOVResponse<String>> response, Retrofit retrofit) {
                                if (response.isSuccess()) {
                                    if (response.body().getStatus() == 0) {
                                        mUser.logout();
                                        startActivity(new Intent(mContext, Login.class));
                                        ((MyPOV) mContext).finish();
                                        Toast.makeText(mContext, "Déconnecté", Toast.LENGTH_SHORT).show();
                                    } else {
                                        Toast.makeText(mContext, response.body().getMessage(), Toast.LENGTH_LONG).show();
                                        mUser.logout();
                                        ((MyPOV) mContext).finish();
                                    }
                                } else {
                                    Toast.makeText(mContext, response.code() + " - " + response.raw().message(), Toast.LENGTH_LONG).show();
                                }
                                dialogdel.cancel();
                            }

                            @Override
                            public void onFailure(Throwable t) {
                                Toast.makeText(mContext, t.getMessage(), Toast.LENGTH_LONG).show();
                                dialogdel.cancel();
                            }
                        });
                    }
                });
                builder.show();
                return true;
            }
        });
    }

    @Override
    public void onCreatePreferences(Bundle bundle, String s) { }
}
