package fr.valquev.mypov;

import android.content.Context;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

import fr.valquev.mypov.activities.ObservationDetails;

/**
 * Created by ValQuev on 06/01/2016.
 */
public class SwipeGestureListener extends GestureDetector.SimpleOnGestureListener implements View.OnTouchListener {

    private Context mContext;
    private GestureDetector gDetector;

    public SwipeGestureListener(Context context) {
        this(context, null);
    }

    public SwipeGestureListener(Context context, GestureDetector gDetector) {
        super();
        if(gDetector == null)
            gDetector = new GestureDetector(context, this);

        this.mContext = context;
        this.gDetector = gDetector;
    }

    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
        if (e1.getX() > e2.getX()) {
            ((ObservationDetails) mContext).next();
        }

        if (e1.getX() < e2.getX()) {
            ((ObservationDetails) mContext).previous();
        }

        return super.onFling(e1, e2, velocityX, velocityY);
    }

    @Override
    public boolean onSingleTapConfirmed(MotionEvent e) {
        return super.onSingleTapConfirmed(e);
    }

    public boolean onTouch(View v, MotionEvent event) {
        return gDetector.onTouchEvent(event);
    }

    public GestureDetector getDetector() {
        return gDetector;
    }
}
