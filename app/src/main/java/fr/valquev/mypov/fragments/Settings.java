package fr.valquev.mypov.fragments;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import fr.valquev.mypov.R;
import fr.valquev.mypov.User;
import fr.valquev.mypov.activities.Login;
import fr.valquev.mypov.activities.ObservationDetails;

/**
 * Created by juleno on 14/12/2015.
 */
public class Settings extends BaseFragment {

    private Context mContext;
    private User mUser;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mContext = getActivity();
        mUser = new User(mContext);

        if (!mUser.isLogged()) {
            startActivity(new Intent(mContext, Login.class));
            ((ObservationDetails) mContext).finish();
        }

        return inflater.inflate(R.layout.settings, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {

    }
}
