package fr.valquev.mypov.fragments;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.VisibleRegion;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.RequestBody;

import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.List;

import fr.valquev.mypov.ImagePicker;
import fr.valquev.mypov.MyPOV;
import fr.valquev.mypov.MyPOVClient;
import fr.valquev.mypov.MyPOVResponse;
import fr.valquev.mypov.Observation;
import fr.valquev.mypov.PositionMapCenter;
import fr.valquev.mypov.R;
import fr.valquev.mypov.User;
import fr.valquev.mypov.activities.ObservationDetails;
import retrofit.Callback;
import retrofit.Response;
import retrofit.Retrofit;


/**
 * Created by ValQuev on 22/09/15.
 */

public class Map extends BaseFragment implements OnMapReadyCallback {

    private static final int PICK_IMAGE = 100;

    private Context mContext;
    private GoogleMap mapInstance;
    private CoordinatorLayout mLayout;
    private User mUser;

    private FloatingActionButton fab;

    private List<Observation> mObservationList;
    private HashMap<Marker, Observation> markerObservationHashMap;

    private PositionMapCenter positionMapCenter;

    private ProgressDialog dialoading;
    private ProgressDialog dialogadding;

    private LatLng centerScreen;
    private LatLng addMarkerPos;

    private MarkerOptions addMarker;

    private Bitmap imageFromResult;
    private View dialogView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mContext = getActivity();

        mUser = new User(mContext);

        markerObservationHashMap = new HashMap<>();

        return inflater.inflate(R.layout.map, container, false);
    }

    @Override
    public void onViewCreated(final View view, Bundle savedInstanceState) {
        if (mUser.getLastLatLng().latitude == 0.0 && mUser.getLastLatLng().longitude == 0.0) {
            dialoading = ProgressDialog.show(mContext, "Chargement", "En attente d'information sur votre localisation...", true);
        }

        ((SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map)).getMapAsync(this);

        mLayout = (CoordinatorLayout) view.findViewById(R.id.layout_map);

        fab = (FloatingActionButton) view.findViewById(R.id.fab_add_observation);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fab.setVisibility(View.GONE);
                addMarkerPos = centerScreen;
                addMarker = new MarkerOptions().position(addMarkerPos).draggable(true);
                mapInstance.addMarker(addMarker);
            }
        });
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mapInstance = googleMap;

        if (mUser.getLastLatLng().latitude != 0.0 && mUser.getLastLatLng().longitude != 0.0) {
            mapInstance.animateCamera(CameraUpdateFactory.newLatLngZoom(mUser.getLastLatLng(), mUser.getZoom()));
        }

        if (((MyPOV) mContext).switchCompat.isChecked()) {
            mapInstance.setMapType(GoogleMap.MAP_TYPE_HYBRID);
        } else {
            mapInstance.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        }

        if (positionMapCenter != null) {
            positionMapCenter.update(mapInstance.getCameraPosition().target, mapInstance.getCameraPosition().zoom);
        }

        mapInstance.setOnCameraChangeListener(new GoogleMap.OnCameraChangeListener() {
            @Override
            public void onCameraChange(CameraPosition cameraPosition) {
                centerScreen = cameraPosition.target;

                VisibleRegion vr = mapInstance.getProjection().getVisibleRegion();
                double bottom = vr.latLngBounds.southwest.latitude;
                double left = vr.latLngBounds.southwest.longitude;

                Location center = new Location("center");
                center.setLatitude(centerScreen.latitude);
                center.setLongitude(centerScreen.longitude);

                Location middleLeftCornerLocation = new Location("center");
                middleLeftCornerLocation.setLatitude(bottom);
                middleLeftCornerLocation.setLongitude(left);

                int dis = (int) Math.ceil(center.distanceTo(middleLeftCornerLocation) / 1000);
                getObservations(dis);
                if (positionMapCenter != null) {
                    positionMapCenter.update(centerScreen, cameraPosition.zoom);
                }
            }
        });

        mapInstance.setOnMarkerDragListener(new GoogleMap.OnMarkerDragListener() {
            @Override
            public void onMarkerDragStart(Marker marker) {

            }

            @Override
            public void onMarkerDrag(Marker marker) {

            }

            @Override
            public void onMarkerDragEnd(Marker marker) {
                addMarkerPos = marker.getPosition();
                addMarker = new MarkerOptions().position(addMarkerPos).draggable(true);
            }
        });

        fab.setVisibility(View.VISIBLE);
    }

    private void getObservations(final int distance) {
        mapInstance.setMyLocationEnabled(true);
        mapInstance.getUiSettings().setMapToolbarEnabled(false);

        MyPOVClient.client.getObservations(mapInstance.getCameraPosition().target.latitude, mapInstance.getCameraPosition().target.longitude, distance, mUser.getMail(), mUser.getPassword()).enqueue(new Callback<MyPOVResponse<List<Observation>>>() {
            @Override
            public void onResponse(Response<MyPOVResponse<List<Observation>>> response, Retrofit retrofit) {
                mapInstance.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
                    @Override
                    public boolean onMarkerClick(Marker marker) {
                        Log.v("TEST", "DRAGGABLE = " + marker.isDraggable());
                        if (marker.isDraggable()) {
                            dialogAddObservation();
                        } else {
                            Observation observationClicked = markerObservationHashMap.get(marker);
                            Intent intent = new Intent(mContext, ObservationDetails.class);
                            intent.putExtra("observation", observationClicked);
                            startActivity(intent);
                        }
                        return false;
                    }
                });
                if (response.isSuccess()) {
                    if (response.body().getStatus() == 0) {
                        mapInstance.clear();
                        if (addMarker != null) {
                            mapInstance.addMarker(addMarker);
                        }
                        mObservationList = response.body().getObject();
                        if (mObservationList != null) {
                            for (Observation observation : mObservationList) {
                                BitmapDescriptor color;
                                if (observation.getObservateur().getId_user() == mUser.getId_user()) {
                                    color = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN);
                                } else {
                                    color = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE);
                                }
                                Marker marker = mapInstance.addMarker(new MarkerOptions().position(new LatLng(observation.getLat(), observation.getLng())).icon(color));
                                markerObservationHashMap.put(marker, observation);
                            }
                        } else {
                            if (dialoading != null) {
                                if (!dialoading.isShowing()) {
                                    Snackbar snackbar = Snackbar.make(mLayout, "Aucune observation dans ce secteur", Snackbar.LENGTH_SHORT);
                                    View view = snackbar.getView();
                                    TextView tv = (TextView) view.findViewById(android.support.design.R.id.snackbar_text);
                                    tv.setTextColor(Color.WHITE);
                                    snackbar.show();
                                }
                            }
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
        } else if (mapInstance.getMapType() == GoogleMap.MAP_TYPE_HYBRID) {
            mapInstance.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        }
    }

    private void dialogAddObservation() {
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext, R.style.AlertDialogStyle);
        dialogView = getActivity().getLayoutInflater().inflate(R.layout.add_observation_dialog, null);
        ((DatePicker) dialogView.findViewById(R.id.observation_add_date)).setMaxDate(System.currentTimeMillis());
        ((DatePicker) dialogView.findViewById(R.id.observation_add_date)).setCalendarViewShown(false);
        dialogView.findViewById(R.id.observation_add_pic).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent chooseImageIntent = ImagePicker.getPickImageIntent(mContext);
                startActivityForResult(chooseImageIntent, PICK_IMAGE);
            }
        });
        builder.setView(dialogView);
        builder.setTitle("Ajouter une observation");
        builder.setPositiveButton("Ajouter", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                EditText nomET = (EditText) dialogView.findViewById(R.id.observation_add_name_text);
                EditText descriptionET = (EditText) dialogView.findViewById(R.id.observation_add_description_text);
                DatePicker datePicker = (DatePicker) dialogView.findViewById(R.id.observation_add_date);

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

                dialogadding = ProgressDialog.show(mContext, "Ajout de l'observation", "Chargement, veuillez patienter...", true);
//datePicker.getCalendarView().getDate()
                MyPOVClient.client.addObservation(nom, description, System.currentTimeMillis() / 1000, addMarker.getPosition().latitude, addMarker.getPosition().longitude, mUser.getMail(), mUser.getPassword()).
                enqueue(new Callback<MyPOVResponse<Observation>>() {
                    @Override
                    public void onResponse(Response<MyPOVResponse<Observation>> response, Retrofit retrofit) {
                        if (response.isSuccess()) {
                            if (response.body().getStatus() == 0) {
                                if (imageFromResult != null) {
                                    uploadImage(imageFromResult, response.body().getObject().getId());
                                } else {
                                    addMarker = null;
                                    setCameraPosition(centerScreen);
                                    fab.setVisibility(View.VISIBLE);
                                    dialogadding.cancel();
                                }
                            } else {
                                dialogadding.cancel();
                                Toast.makeText(mContext, response.body().getMessage(), Toast.LENGTH_LONG).show();
                                mUser.logout();
                                ((MyPOV) mContext).finish();
                            }
                        } else {
                            Toast.makeText(mContext, response.code() + " - " + response.message(), Toast.LENGTH_LONG).show();
                            dialogadding.cancel();
                        }
                    }

                    @Override
                    public void onFailure(Throwable t) {
                        Toast.makeText(mContext, t.getMessage(), Toast.LENGTH_LONG).show();
                        dialogadding.cancel();
                    }
                });
            }
        });
        builder.setNegativeButton("Annuler", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                addMarker = null;
                setCameraPosition(centerScreen);
                fab.setVisibility(View.VISIBLE);
            }
        });
        builder.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                addMarker = null;
                fab.setVisibility(View.VISIBLE);
            }
        });
        builder.show();
    }

    public void setCameraPosition(LatLng position) {
        if (mapInstance != null) {
            mapInstance.animateCamera(CameraUpdateFactory.newLatLngZoom(position, 15));
            if (dialoading != null) {
                if (dialoading.isShowing()) {
                    dialoading.cancel();
                }
            }
        }
    }

    public void setPositionMapCenter(PositionMapCenter positionMapCenter) {
        this.positionMapCenter = positionMapCenter;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);

        if(resultCode == Activity.RESULT_OK) {
            switch(requestCode) {
                case PICK_IMAGE:
                    imageFromResult = ImagePicker.getImageFromResult(mContext, resultCode, intent);
                    dialogView.findViewById(R.id.observation_add_pic).setVisibility(View.GONE);
                    ((ImageView) dialogView.findViewById(R.id.observation_add_thumbnail)).setImageBitmap(imageFromResult);
                    dialogView.findViewById(R.id.observation_add_thumbnail).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            ((ImageView) dialogView.findViewById(R.id.observation_add_thumbnail)).setImageBitmap(null);
                            imageFromResult = null;
                            dialogView.findViewById(R.id.observation_add_pic).setVisibility(View.VISIBLE);
                        }
                    });
                    break;

                default:
                    break;
            }
        }
    }

    private void uploadImage(Bitmap bitmap, int id_obs) {
        final Bitmap userPic = ImagePicker.getResizedBitmap(bitmap, 512);

        double size = (userPic.getByteCount() / 8) / 1000000;

        if(size < 10D) {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            userPic.compress(Bitmap.CompressFormat.JPEG, 100, out);

            RequestBody requestBody = RequestBody.create(MediaType.parse("multipart/form-data"), out.toByteArray());

            MyPOVClient.client.addPhotoObservation(requestBody, id_obs, mUser.getMail(), mUser.getPassword()).enqueue(new Callback<MyPOVResponse<String>>() {
                @Override
                public void onResponse(Response<MyPOVResponse<String>> response, Retrofit retrofit) {
                    if (response.isSuccess()) {
                        if (response.body().getStatus() == 0) {
                            addMarker = null;
                            imageFromResult = null;
                            setCameraPosition(centerScreen);
                            fab.setVisibility(View.VISIBLE);
                            dialogadding.cancel();
                        } else {
                            dialogadding.cancel();
                            Toast.makeText(mContext, response.body().getMessage(), Toast.LENGTH_LONG).show();
                            mUser.logout();
                            ((MyPOV) mContext).finish();
                        }
                    } else {
                        Toast.makeText(mContext, response.code() + " - " + response.raw().message(), Toast.LENGTH_LONG).show();
                    }
                    dialogadding.cancel();
                }

                @Override
                public void onFailure(Throwable t) {
                    Toast.makeText(mContext, t.getMessage(), Toast.LENGTH_LONG).show();
                    dialogadding.cancel();
                }
            });
        } else {
            Toast.makeText(mContext, "L'image doit faire moins de 10mo", Toast.LENGTH_LONG).show();
        }
    }
}