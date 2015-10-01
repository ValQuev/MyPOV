package fr.valquev.mypov.fragments;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.List;

import fr.valquev.mypov.MyPOV;
import fr.valquev.mypov.MyPOVClient;
import fr.valquev.mypov.MyPOVResponse;
import fr.valquev.mypov.Observation;
import fr.valquev.mypov.R;
import fr.valquev.mypov.User;
import fr.valquev.mypov.activities.AddObservation;
import fr.valquev.mypov.activities.Login;
import fr.valquev.mypov.activities.ObservationDetails;
import retrofit.Callback;
import retrofit.Response;

/**
 * Created by ValQuev on 22/09/15.
 */

public class Map extends BaseFragment implements OnMapReadyCallback, GoogleMap.OnInfoWindowClickListener {

    private Context mContext;
    private GoogleMap mapInstance;
    private CoordinatorLayout mLayout;
    private User mUser;

    private List<Observation> mObservationList;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mContext = getActivity();

        mUser = new User(mContext);

        return inflater.inflate(R.layout.map, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        ((SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map)).getMapAsync(this);

        mLayout = (CoordinatorLayout) view.findViewById(R.id.layout_map);

        view.findViewById(R.id.fab_add_observation).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(mContext, AddObservation.class));
            }
        });
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mapInstance = googleMap;
        mapInstance.setMyLocationEnabled(true);
        mapInstance.getUiSettings().setMapToolbarEnabled(false);
        mapInstance.setOnInfoWindowClickListener(this);

        MyPOVClient.client.getObservations(48.078515, -0.766991, 100, mUser.getMail(), mUser.getPassword()).enqueue(new Callback<MyPOVResponse<List<Observation>>>() {
            @Override
            public void onResponse(Response<MyPOVResponse<List<Observation>>> response) {
                if (response.isSuccess()) {
                    if (response.body().getStatus() == 0) {
                        mObservationList = response.body().getObject();
                        if (mObservationList != null) {
                            for (Observation observation : mObservationList) {
                                BitmapDescriptor color;
                                if (observation.getObservateur().getId_user() == mUser.getId_user()) {
                                    color = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN);
                                } else {
                                    color = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE);
                                }
                                mapInstance.addMarker(new MarkerOptions().position(new LatLng(observation.getLat(), observation.getLng())).title(observation.getNom()).icon(color));
                            }
                        } else {
                            Snackbar.make(mLayout, "Aucune observation à proximité", Snackbar.LENGTH_LONG)
                                    .setAction("Ok", null)
                                    .show();
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
            ((NavigationView) getActivity().findViewById(R.id.navigation)).getMenu().getItem(3).setTitle(getString(R.string.action_normal_view)).setIcon(getResources().getDrawable(R.drawable.ic_layers_black_24dp));
        } else if (mapInstance.getMapType() == GoogleMap.MAP_TYPE_HYBRID) {
            mapInstance.setMapType(GoogleMap.MAP_TYPE_NORMAL);
            ((NavigationView) getActivity().findViewById(R.id.navigation)).getMenu().getItem(3).setTitle(getString(R.string.action_satellite_view)).setIcon(getResources().getDrawable(R.drawable.ic_satellite_black_24dp));
        }
    }

    @Override
    public void onInfoWindowClick(Marker marker) {
        int position = Integer.parseInt(marker.getId().replace("m", ""));

        Observation observationClicked = mObservationList.get(position);

        Intent intent = new Intent(mContext, ObservationDetails.class);
        intent.putExtra("observation", observationClicked);
        startActivity(intent);
    }
}
