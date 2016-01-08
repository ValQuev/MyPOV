package fr.valquev.mypov.activities;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import fr.valquev.mypov.Observation;
import fr.valquev.mypov.R;
import fr.valquev.mypov.User;

/**
 * Created by ValQuev on 08/01/2016.
 */
public class NewLocation extends AppCompatActivity implements OnMapReadyCallback {

    private Context mContext;
    private Observation mObservation;
    private User mUser;
    private LatLng newLocation;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.new_location);

        mContext = this;

        mUser = new User(mContext);

        if (!mUser.isLogged()) {
            startActivity(new Intent(mContext, Login.class));
            finish();
        }

        mObservation = (Observation) getIntent().getExtras().get("observation");

        Toolbar toolbar = (Toolbar) findViewById(R.id.observation_details_toolbar);

        toolbar.setTitle("Localisation : " + mObservation.getNom());
        setSupportActionBar(toolbar);

        findViewById(R.id.new_location_validate).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent returnIntent = new Intent();
                returnIntent.putExtra("location", newLocation);
                setResult(RESULT_OK, returnIntent);
                finish();
            }
        });

        ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.new_location_map)).getMapAsync(this);

        setAB();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        googleMap.setMyLocationEnabled(true);

        googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(mObservation.getLat(), mObservation.getLng()), mUser.getZoom()));

        googleMap.addMarker(new MarkerOptions().position(new LatLng(mObservation.getLat(), mObservation.getLng())).draggable(true));

        googleMap.setOnMarkerDragListener(new GoogleMap.OnMarkerDragListener() {
            @Override
            public void onMarkerDragStart(Marker marker) {

            }

            @Override
            public void onMarkerDrag(Marker marker) {

            }

            @Override
            public void onMarkerDragEnd(Marker marker) {
                newLocation = marker.getPosition();
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        switch(id) {
            case android.R.id.home:
                Intent returnIntent = new Intent();
                returnIntent.putExtra("location", newLocation);
                setResult(RESULT_CANCELED, returnIntent);
                finish();
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    private void setAB() {
        ActionBar actionBar = getSupportActionBar();

        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }
}
