package fr.valquev.mypov.fragments;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import fr.valquev.mypov.Observation;
import fr.valquev.mypov.R;

/**
 * Created by ValQuev on 28/09/15.
 */
public class ObservationDetailsContent extends BaseFragment {

    private Context mContext;
    private Observation mObservation;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mContext = getActivity();
        mObservation = getArguments().getParcelable("observation");
        return inflater.inflate(R.layout.observation_details_content, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        TextView texte = (TextView) view.findViewById(R.id.observation_content_text);

        texte.setText(mObservation.getDescription());
    }
}
