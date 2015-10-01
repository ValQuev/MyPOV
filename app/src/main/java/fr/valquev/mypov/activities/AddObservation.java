package fr.valquev.mypov.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import fr.valquev.mypov.MyPOVClient;
import fr.valquev.mypov.MyPOVResponse;
import fr.valquev.mypov.R;
import fr.valquev.mypov.User;
import retrofit.Callback;
import retrofit.Response;

public class AddObservation extends AppCompatActivity implements OnMapReadyCallback, GoogleMap.OnMarkerDragListener {

    private LatLng observation;
    private MarkerOptions observation_marker;

    private EditText nomET;
    private EditText descriptionET;

    private User mUser;
    private Context mContext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_observation);
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        observation_marker = new MarkerOptions();

        mContext = this;

        mUser = new User(mContext);

        if (!mUser.isLogged()) {
            startActivity(new Intent(mContext, Login.class));
            finish();
        }

        nomET = (EditText) findViewById(R.id.add_observation_nom_et);
        descriptionET = (EditText) findViewById(R.id.add_observation_description_et);
    }



    @Override
    public void onMapReady(GoogleMap map) {
        observation = new LatLng(48, 4);
        map.addMarker(observation_marker.position(observation).title("Lieu marqué").draggable(true));
        map.moveCamera(CameraUpdateFactory.newLatLng(observation));
        map.setOnMarkerDragListener(this);

        findViewById(R.id.add_observation_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String nom = nomET.getText().toString();
                if (nom.equals("")) {
                    nomET.setError("Vide");
                    return;
                }

                String description = descriptionET.getText().toString();
                if (description.equals("")) {
                    descriptionET.setError("Vide");
                    return;
                }

                MyPOVClient.client.addObservation(nom, description, observation.latitude, observation.longitude, mUser.getMail(), mUser.getPassword()).enqueue(new Callback<MyPOVResponse<String>>() {
                    @Override
                    public void onResponse(Response<MyPOVResponse<String>> response) {
                        if (response.isSuccess()) {
                            if (response.body().getStatus() == 0) {
                                finish();
                                // TODO START ACTIVITY WITH INTENT RESULT TO REFRESH MAP
                                Toast.makeText(mContext, "Observation ajoutée", Toast.LENGTH_LONG).show();
                            } else {
                                Toast.makeText(mContext, response.body().getMessage(), Toast.LENGTH_LONG).show();
                                mUser.logout();
                                finish();
                            }
                        } else {
                            Toast.makeText(mContext, response.code() + " - " + response.message(), Toast.LENGTH_LONG).show();
                        }
                    }

                    @Override
                    public void onFailure(Throwable t) {
                        Toast.makeText(mContext, t.getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
            }
        });
    }

    @Override
    public void onMarkerDragStart(Marker marker) {

    }

    @Override
    public void onMarkerDrag(Marker marker) {

    }

    @Override
    public void onMarkerDragEnd(Marker marker) {
        observation = marker.getPosition();
    }
}
