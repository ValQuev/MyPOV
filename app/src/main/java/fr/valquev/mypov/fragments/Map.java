package fr.valquev.mypov.fragments;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
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
import fr.valquev.mypov.ObservationPhoto;
import fr.valquev.mypov.R;
import fr.valquev.mypov.User;
import fr.valquev.mypov.activities.AddObservation;
import fr.valquev.mypov.activities.Login;
import fr.valquev.mypov.activities.ObservationDetails;
import retrofit.Callback;
import retrofit.Response;
import retrofit.Retrofit;

/**
 * Created by ValQuev on 22/09/15.
 */

public class Map extends BaseFragment implements OnMapReadyCallback, GoogleMap.OnInfoWindowClickListener {

    public static final int ASK_COARSE_LOCATION_PERMISSION = 1;
    public static final int ASK_FINE_LOCATION_PERMISSION = 2;

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

        checkPermAndLaunchMap();
    }

    private void checkPermAndLaunchMap() {
        int permissionCoarseLocation = ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION);
        int permissionFineLocation = ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION);
        if (permissionCoarseLocation == PackageManager.PERMISSION_GRANTED) {
            if (permissionFineLocation == PackageManager.PERMISSION_GRANTED) {
                getObservations();
            } else {
                askPermsFineLocation();
            }
        } else {
            askPermsCoarseLocation();
        }
    }

    private void getObservations() {
        mapInstance.setMyLocationEnabled(true);
        mapInstance.getUiSettings().setMapToolbarEnabled(false);
        mapInstance.setOnInfoWindowClickListener(this);

        MyPOVClient.client.getObservations(48.078515, -0.766991, 1000, mUser.getMail(), mUser.getPassword()).enqueue(new Callback<MyPOVResponse<List<Observation>>>() {
            @Override
            public void onResponse(Response<MyPOVResponse<List<Observation>>> response, Retrofit retrofit) {
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
            ((NavigationView) getActivity().findViewById(R.id.mypov_navigation)).getMenu().getItem(3).setTitle(getString(R.string.action_normal_view)).setIcon(getResources().getDrawable(R.drawable.ic_layers_black_24dp));
        } else if (mapInstance.getMapType() == GoogleMap.MAP_TYPE_HYBRID) {
            mapInstance.setMapType(GoogleMap.MAP_TYPE_NORMAL);
            ((NavigationView) getActivity().findViewById(R.id.mypov_navigation)).getMenu().getItem(3).setTitle(getString(R.string.action_satellite_view)).setIcon(getResources().getDrawable(R.drawable.ic_satellite_black_24dp));
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

    public void askPermsCoarseLocation() {
        // Here, thisActivity is the current activity
        if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION)) {

                // Show an expanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.

            } else {

                // No explanation needed, we can request the permission.

                ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, ASK_COARSE_LOCATION_PERMISSION);

                // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
                // app-defined int constant. The callback method gets the
                // result of the request.
            }
        }
    }

    public void askPermsFineLocation() {
        // Here, thisActivity is the current activity
        if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION)) {

                // Show an expanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.

            } else {

                // No explanation needed, we can request the permission.

                ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, ASK_FINE_LOCATION_PERMISSION);

                // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
                // app-defined int constant. The callback method gets the
                // result of the request.
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case ASK_COARSE_LOCATION_PERMISSION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    checkPermAndLaunchMap();
                } else {
                    checkPermAndLaunchMap();
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }
                return;
            }

            case ASK_FINE_LOCATION_PERMISSION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    checkPermAndLaunchMap();
                } else {
                    checkPermAndLaunchMap();
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }
                return;
            }
        }
    }
}
