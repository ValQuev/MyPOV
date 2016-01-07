package fr.valquev.mypov.fragments;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.ViewFlipper;

import com.squareup.picasso.Picasso;

import fr.valquev.mypov.Notes;
import fr.valquev.mypov.Observation;
import fr.valquev.mypov.ObservationPhoto;
import fr.valquev.mypov.R;
import fr.valquev.mypov.SwipeGestureListener;

/**
 * Created by ValQuev on 28/09/15.
 */
public class ObservationDetailsContent extends BaseFragment {

    private Context mContext;
    private Observation mObservation;
    private ViewFlipper mViewFlipper;
    private SwipeGestureListener swipeGestureListener;

    private int page = 0;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mContext = getActivity();
        mObservation = getArguments().getParcelable("observation");
        return inflater.inflate(R.layout.observation_details_content, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        TextView affirmation = (TextView) view.findViewById(R.id.tv_affirmation);
        TextView infirmation = (TextView) view.findViewById(R.id.tv_infirmation);
        TextView texte = (TextView) view.findViewById(R.id.observation_content_text);

        Notes notes = mObservation.getNotes();
        affirmation.setText(String.format("%d", notes.getNbAffirmations()));
        infirmation.setText(String.format("%d", notes.getNbInfirmations()));

        texte.setText(String.format("%s %s", mObservation.getDescription(), mObservation.getSuperbDate()));

        mViewFlipper = (ViewFlipper) view.findViewById(R.id.annonce_vf);

        ImageView map = new ImageView(mContext);
        String pos = mObservation.getLat() + "," + mObservation.getLng();
        Picasso.with(mContext).load("https://maps.googleapis.com/maps/api/staticmap?center=" + pos + "&zoom=13&markers=" + pos + "&size=640x480&scale=2").centerCrop().fit().into(map);
        mViewFlipper.addView(map);

        for (ObservationPhoto photo : mObservation.getPhotos()) {
            ImageView imageView = new ImageView(mContext);
            Picasso.with(mContext).load("https://mypov.fr/uploads/observations/" + mObservation.getId() + "/" + photo.getId() + "." + photo.getFormat()).centerCrop().fit().into(imageView);
            mViewFlipper.addView(imageView);
        }

        swipeGestureListener = new SwipeGestureListener(mContext);

        mViewFlipper.setOnTouchListener(swipeGestureListener);

        if (mViewFlipper.getChildCount() > 1) {
            view.findViewById(R.id.show_annonce_left).setVisibility(View.VISIBLE);
            view.findViewById(R.id.show_annonce_right).setVisibility(View.VISIBLE);
            view.findViewById(R.id.show_annonce_left).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    previous();
                }
            });
            view.findViewById(R.id.show_annonce_right).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    next();
                }
            });
        }
    }

    public void previous() {
        if (mViewFlipper.getChildCount() > 1) {
            if (page > 0) {
                page--;
            } else {
                page = mViewFlipper.getChildCount() - 1;
            }
            mViewFlipper.setInAnimation(mContext, R.anim.right_in);
            mViewFlipper.setOutAnimation(mContext, R.anim.right_out);
            mViewFlipper.setDisplayedChild(page);
        }
    }

    public void next() {
        if (mViewFlipper.getChildCount() > 1) {
            if (page < mViewFlipper.getChildCount() - 1) {
                page++;
            } else {
                page = 0;
            }
            mViewFlipper.setInAnimation(mContext, R.anim.left_in);
            mViewFlipper.setOutAnimation(mContext, R.anim.left_out);
            mViewFlipper.setDisplayedChild(page);
        }
    }

    public void addImageToFlipper(Bitmap bitmap) {
        ImageView imageView = new ImageView(mContext);
        imageView.setImageBitmap(bitmap);
        imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
        mViewFlipper.addView(imageView);
    }

    public void setFlipperToLastPic() {
        page = mViewFlipper.getChildCount() - 1;
        mViewFlipper.setDisplayedChild(page);
    }

    public boolean onTouchEvent(MotionEvent event) {
        return swipeGestureListener.getDetector().onTouchEvent(event);
    }
}
