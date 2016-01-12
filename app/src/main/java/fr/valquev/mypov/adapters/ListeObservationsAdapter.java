package fr.valquev.mypov.adapters;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import fr.valquev.mypov.Observation;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import fr.valquev.mypov.OnObservationListItemClickListener;
import fr.valquev.mypov.R;
import fr.valquev.mypov.activities.ObservationDetails;

/**
 * Created by juleno on 13/12/2015.
 */
public class ListeObservationsAdapter extends RecyclerView.Adapter<ListeObservationsAdapter.ListeObservationsViewHolder>{

    private OnObservationListItemClickListener listener;
    private List<Observation> mObservationsListe;

    public ListeObservationsAdapter(List<Observation> observationsListe, Context context) {
        listener = (OnObservationListItemClickListener) context;
        mObservationsListe = observationsListe;
    }

    @Override
    public ListeObservationsViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View itemView = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.liste_observations_row, viewGroup, false);

        return new ListeObservationsViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(ListeObservationsViewHolder listeObservationsViewHolder, final int position) {
        final Observation observation = mObservationsListe.get(position);

        listeObservationsViewHolder.nom.setText(observation.getNom());
        listeObservationsViewHolder.observateur.setText(observation.getObservateur().getPseudo());
        listeObservationsViewHolder.date.setText(observation.getSuperbDate() + " - " + observation.getSuperbDistance());

        listeObservationsViewHolder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.onObservationClick(observation);
            }
        });
    }

    @Override
    public int getItemCount() {
        return mObservationsListe.size();
    }

    protected class ListeObservationsViewHolder extends RecyclerView.ViewHolder {

        protected TextView nom;
        protected TextView observateur;
        protected TextView date;

        public ListeObservationsViewHolder(View itemView) {
            super(itemView);

            nom = (TextView) itemView.findViewById(R.id.obs_details_comments_row_nom);
            observateur = (TextView) itemView.findViewById(R.id.obs_details_comments_row_observateur);
            date = (TextView) itemView.findViewById(R.id.obs_details_comments_row_date);
        }

    }

}
