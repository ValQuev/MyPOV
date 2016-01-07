package fr.valquev.mypov;

import com.google.android.gms.maps.model.LatLng;

/**
 * Created by ValQuev on 16/12/2015.
 */
public interface PositionMapCenter {

    void update(LatLng position, float zoom);

}
