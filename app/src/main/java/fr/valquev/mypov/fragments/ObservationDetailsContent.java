package fr.valquev.mypov.fragments;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.ViewFlipper;

import com.squareup.picasso.Picasso;

import fr.valquev.mypov.Observation;
import fr.valquev.mypov.ObservationPhoto;
import fr.valquev.mypov.R;

/**
 * Created by ValQuev on 28/09/15.
 */
public class ObservationDetailsContent extends BaseFragment {

    private Context mContext;
    private Observation mObservation;

    private int page = 0;

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

        final ViewFlipper viewFlipper = (ViewFlipper) view.findViewById(R.id.annonce_vf);
        view.findViewById(R.id.show_annonce_left).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (page > 0) {
                    page--;
                } else {
                    page = viewFlipper.getChildCount() - 1;
                }
                viewFlipper.setDisplayedChild(page);
            }
        });
        view.findViewById(R.id.show_annonce_right).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (page < viewFlipper.getChildCount() - 1) {
                    page++;
                } else {
                    page = 0;
                }
                viewFlipper.setDisplayedChild(page);
            }
        });

        ImageView map = new ImageView(mContext);
        String pos = mObservation.getLat() + "," + mObservation.getLng();
        Picasso.with(mContext).load("https://maps.googleapis.com/maps/api/staticmap?center=" + pos + "&zoom=13&markers=" + pos + "&size=640x480&scale=2").centerCrop().fit().into(map);
        viewFlipper.addView(map);

        for (ObservationPhoto photo : mObservation.getPhotos()) {
            ImageView imageView = new ImageView(mContext);
            Picasso.with(mContext).load("https://mypov.fr/uploads/observations/" + mObservation.getId() + "/" + photo.getId() + "." + photo.getFormat()).centerCrop().fit().into(imageView);
            viewFlipper.addView(imageView);
        }
    }
}
