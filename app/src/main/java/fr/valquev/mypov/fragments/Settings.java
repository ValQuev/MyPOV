package fr.valquev.mypov.fragments;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.util.Log;
import android.widget.Toast;

import fr.valquev.mypov.MyPOV;
import fr.valquev.mypov.R;
import fr.valquev.mypov.User;
import fr.valquev.mypov.activities.Login;
import fr.valquev.mypov.activities.ObservationDetails;

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
                // TODO
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
    }

    @Override
    public void onCreatePreferences(Bundle bundle, String s) { }
}
