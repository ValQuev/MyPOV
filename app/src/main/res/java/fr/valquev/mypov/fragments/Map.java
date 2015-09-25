package java.fr.valquev.mypov.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.List;

import fr.valquev.mypov.MyPOVClient;
import fr.valquev.mypov.MyPOVResponse;
import fr.valquev.mypov.Observation;
import fr.valquev.mypov.R;
import retrofit.Callback;
import retrofit.Response;

/**
 * Created by ValQuev on 22/09/15.
 */

public class Map extends BaseFragment implements OnMapReadyCallback {

    private Context mContext;
    private GoogleMap mapInstance;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mContext = getActivity();
        return inflater.inflate(R.layout.map, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        ((SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map)).getMapAsync(this);

        // Ici on met les listeners pour les widgets et les textview.setText("coucou"); etc...
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mapInstance = googleMap;

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

    public void toggleView() {
        if (mapInstance.getMapType() == GoogleMap.MAP_TYPE_NORMAL) {
            mapInstance.setMapType(GoogleMap.MAP_TYPE_HYBRID);
            ((NavigationView) getActivity().findViewById(R.id.navigation)).getMenu().getItem(3).setTitle(getString(R.string.action_normal_view));
        } else if (mapInstance.getMapType() == GoogleMap.MAP_TYPE_HYBRID) {
            mapInstance.setMapType(GoogleMap.MAP_TYPE_NORMAL);
            ((NavigationView) getActivity().findViewById(R.id.navigation)).getMenu().getItem(3).setTitle(getString(R.string.action_satellite_view));
        }
    }
}
