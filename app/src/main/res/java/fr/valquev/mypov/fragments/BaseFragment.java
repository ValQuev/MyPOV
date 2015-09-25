package java.fr.valquev.mypov.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * Created by ValQuev on 22/09/15.
 */

public abstract class BaseFragment extends Fragment {

    public abstract View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState);

    public abstract void onViewCreated(View view, Bundle savedInstanceState);

}
