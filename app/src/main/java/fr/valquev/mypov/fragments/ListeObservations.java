package fr.valquev.mypov.fragments;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;

import java.util.List;

import fr.valquev.mypov.MyPOV;
import fr.valquev.mypov.MyPOVClient;
import fr.valquev.mypov.MyPOVResponse;
import fr.valquev.mypov.Observation;
import fr.valquev.mypov.OnObservationListItemClickListener;
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

    private int page = 1;

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
                getListeObservation(page);
            }
        });
    }

    private void getListeObservation(int page) {
        mSwipeRefreshLayout.setRefreshing(true);
        int iduser = -1;
        if (mUser.getPreferedFilter()) {
            iduser = mUser.getId_user();
        }
        MyPOVClient.client.getListeObservations(position.latitude, position.longitude, page, iduser, mUser.getPreferedTri(), mUser.getMail(), mUser.getPassword()).enqueue(new Callback<MyPOVResponse<List<Observation>>>() {
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

        refreshList();
    }

    private void refreshList() {
        mSwipeRefreshLayout.post(new Runnable() {
            @Override
            public void run() {
                getListeObservation(page);
            }
        });
    }

    public void update(LatLng position) {
        this.position = position;
    }

    public void openFilterDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext, R.style.AlertDialogStyle);
        final View v = getActivity().getLayoutInflater().inflate(R.layout.filter_dialog, null);
        final RadioGroup rg = (RadioGroup) v.findViewById(R.id.filter_rb);
        final RadioButton rbDistance = (RadioButton) v.findViewById(R.id.filter_rb_distance);
        final RadioButton rbDate = (RadioButton) v.findViewById(R.id.filter_rb_date);
        final CheckBox cb = (CheckBox) v.findViewById(R.id.filter_cb_mine);
        cb.setChecked(mUser.getPreferedFilter());
        if (mUser.getPreferedTri().equalsIgnoreCase("date")) {
            rbDate.setChecked(true);
            rbDistance.setChecked(false);
        } else {
            rbDate.setChecked(false);
            rbDistance.setChecked(true);
        }
        builder.setView(v);
        builder.setTitle("Trier par");
        builder.setPositiveButton("Valider", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                mUser.setPreferedFilter(cb.isChecked());

                if (rg.getCheckedRadioButtonId() == rbDate.getId()) {
                    mUser.setPreferedTri("date");
                } else {
                    mUser.setPreferedTri("distance");
                }

                refreshList();
            }
        });
        builder.setNegativeButton("Annuler", null);
        builder.show();
    }
}
