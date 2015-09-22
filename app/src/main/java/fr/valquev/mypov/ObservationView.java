package fr.valquev.mypov;

import android.support.v4.app.FragmentActivity;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnMarkerDragListener;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

public class ObservationView extends FragmentActivity implements OnMapReadyCallback {

    LatLng observation;
    MarkerOptions observation_marker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_observation);
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        observation_marker = new MarkerOptions();
    }

    @Override
    public void onMapReady(GoogleMap map) {
        observation = new LatLng(48, 4);
        map.addMarker(observation_marker.position(observation).title("Lieu marqué").draggable(true));
        map.moveCamera(CameraUpdateFactory.newLatLng(observation));
    }

}
