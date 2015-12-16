package fr.valquev.mypov.fragments;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;

import java.util.List;

import fr.valquev.mypov.MyPOVClient;
import fr.valquev.mypov.MyPOVResponse;
import fr.valquev.mypov.Observation;
import fr.valquev.mypov.R;
import fr.valquev.mypov.User;
import fr.valquev.mypov.activities.Login;
import fr.valquev.mypov.activities.ObservationDetails;
import fr.valquev.mypov.adapters.ListeObservationsAdapter;
import retrofit.Callback;
import retrofit.Response;
import retrofit.Retrofit;

/**
 * Created by juleno on 13/12/2015.
 */
public class ListeObservations extends BaseFragment {

    private Context mContext;
    private User mUser;

    private List<Observation> observationsList;
    private RecyclerView mObservationsListe;

    private SwipeRefreshLayout mSwipeRefreshLayout;

    private LatLng position;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mContext = getActivity();
        mUser = new User(mContext);

        if (!mUser.isLogged()) {
            startActivity(new Intent(mContext, Login.class));
        }

        return inflater.inflate(R.layout.liste_observations, container, false);

    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        mObservationsListe = (RecyclerView) view.findViewById(R.id.observations_liste);
        mSwipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.observations_liste_swipe);

        mObservationsListe.setHasFixedSize(true);
        LinearLayoutManager llm = new LinearLayoutManager(mContext);
        llm.setOrientation(LinearLayoutManager.VERTICAL);
        mObservationsListe.setLayoutManager(llm);

        mSwipeRefreshLayout.setColorSchemeColors(getResources().getColor(R.color.primary), getResources().getColor(R.color.accent));

        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                getListeObservation();
            }
        });
    }

    private void getListeObservation() {
        mSwipeRefreshLayout.setRefreshing(true);
        MyPOVClient.client.getListeObservations(position.latitude, position.longitude, 1, "distance", mUser.getMail(), mUser.getPassword()).enqueue(new Callback<MyPOVResponse<List<Observation>>>() {
            @Override
            public void onResponse(Response<MyPOVResponse<List<Observation>>> response, Retrofit retrofit) {
                if (response.isSuccess()) {
                    if (response.body().getStatus() == 0) {
                        observationsList = response.body().getObject();
                        if (observationsList != null) {
                            mObservationsListe.setAdapter(new ListeObservationsAdapter(observationsList, mContext));
                            mSwipeRefreshLayout.setRefreshing(false);
                        } else {
                            mSwipeRefreshLayout.setRefreshing(false);
                            Toast.makeText(mContext, "Aucune observation à afficher", Toast.LENGTH_LONG).show();
                        }
                    } else {
                        mSwipeRefreshLayout.setRefreshing(false);
                        Toast.makeText(mContext, response.body().getMessage(), Toast.LENGTH_LONG).show();
                    }
                } else {
                    mSwipeRefreshLayout.setRefreshing(false);
                    Toast.makeText(mContext, response.code() + " - " + response.message(), Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Throwable t) {
                mSwipeRefreshLayout.setRefreshing(false);
                Toast.makeText(mContext, t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();

        mSwipeRefreshLayout.post(new Runnable() {
            @Override
            public void run() {
                getListeObservation();
            }
        });
    }

    public void update(LatLng position) {
        this.position = position;
    }
}
