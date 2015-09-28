package fr.valquev.mypov.adapters;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

import fr.valquev.mypov.Comment;
import fr.valquev.mypov.R;

/**
 * Created by ValQuev on 28/09/15.
 */
public class ObservationDetailsCommentsAdapter extends RecyclerView.Adapter<ObservationDetailsCommentsAdapter.ObservationDetailsCommentsViewHolder> {

    private List<Comment> mCommentList;

    public ObservationDetailsCommentsAdapter(List<Comment> commentList) {
        mCommentList = commentList;
    }

    @Override
    public ObservationDetailsCommentsViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View itemView = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.observation_details_comments_row, viewGroup, false);

        return new ObservationDetailsCommentsViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(ObservationDetailsCommentsViewHolder observationDetailsCommentsViewHolder, final int position) {
        Comment comment = mCommentList.get(position);

        observationDetailsCommentsViewHolder.pseudo.setText(comment.getUtilisateur().getPseudo());
        observationDetailsCommentsViewHolder.texte.setText(comment.getTexte());
    }

    @Override
    public int getItemCount() {
        return mCommentList.size();
    }

    protected class ObservationDetailsCommentsViewHolder extends RecyclerView.ViewHolder {

        protected TextView pseudo;
        protected TextView texte;

        public ObservationDetailsCommentsViewHolder(View itemView) {
            super(itemView);

            pseudo = (TextView) itemView.findViewById(R.id.obs_details_comments_row_pseudo);
            texte = (TextView) itemView.findViewById(R.id.obs_details_comments_row_text);
        }

    }
}