package fr.valquev.mypov;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.List;

import retrofit.Callback;
import retrofit.Response;

public class MainActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mapInstance;
    private Context mContext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mContext = this;

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    @Override
    public void onMapReady(GoogleMap map) {
        mapInstance = map;

        mapInstance.setMyLocationEnabled(true);

        MyPOVClient.client.getObservations().enqueue(new Callback<MyPOVResponse<List<Observation>>>() {
            @Override
            public void onResponse(Response<MyPOVResponse<List<Observation>>> response) {
                if (response.isSuccess()) {
                    if (response.body().getStatus() == 0) {
                        for (Observation observation : response.body().getObject()) {
                            mapInstance.addMarker(new MarkerOptions().position(new LatLng(observation.getLat(), observation.getLng())).title(observation.getNom()));
                        }
                    } else {
                        Toast.makeText(mContext, response.body().getMessage(), Toast.LENGTH_LONG).show();
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

    public void fabClicked(View v) {
        if (mapInstance.getMapType() == GoogleMap.MAP_TYPE_NORMAL) {
            mapInstance.setMapType(GoogleMap.MAP_TYPE_HYBRID);
        } else if (mapInstance.getMapType() == GoogleMap.MAP_TYPE_HYBRID) {
            mapInstance.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        }
    }

    /*@Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }*/
}
