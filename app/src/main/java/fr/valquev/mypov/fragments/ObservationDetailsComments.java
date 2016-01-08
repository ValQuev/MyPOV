package fr.valquev.mypov.fragments;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import java.util.List;

import fr.valquev.mypov.Comment;
import fr.valquev.mypov.MyPOVClient;
import fr.valquev.mypov.MyPOVResponse;
import fr.valquev.mypov.Observation;
import fr.valquev.mypov.R;
import fr.valquev.mypov.User;
import fr.valquev.mypov.activities.Login;
import fr.valquev.mypov.activities.ObservationDetails;
import fr.valquev.mypov.adapters.ObservationDetailsCommentsAdapter;
import retrofit.Callback;
import retrofit.Response;
import retrofit.Retrofit;

/**
 * Created by ValQuev on 28/09/15.
 */
public class ObservationDetailsComments extends BaseFragment {

    private Context mContext;
    private Observation mObservation;
    private RecyclerView mCommentList;
    private SwipeRefreshLayout mSwipeRefreshLayout;

    private User mUser;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mContext = getActivity();
        mObservation = getArguments().getParcelable("observation");
        mUser = new User(mContext);

        if (!mUser.isLogged()) {
            startActivity(new Intent(mContext, Login.class));
            ((ObservationDetails) mContext).finish();
        }

        return inflater.inflate(R.layout.observation_details_comments, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        mCommentList = (RecyclerView) view.findViewById(R.id.comments_list);
        mSwipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.comments_swipe);

        mCommentList.setHasFixedSize(true);
        LinearLayoutManager llm = new LinearLayoutManager(mContext);
        llm.setOrientation(LinearLayoutManager.VERTICAL);
        mCommentList.setLayoutManager(llm);

        mSwipeRefreshLayout.setColorSchemeColors(getResources().getColor(R.color.primary), getResources().getColor(R.color.accent));

        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                getComments(true);
            }
        });
    }

    public void getComments(final boolean firstLoad) {
        mSwipeRefreshLayout.setRefreshing(true);
        MyPOVClient.client.getComments(mObservation.getId(), mUser.getMail(), mUser.getPassword()).enqueue(new Callback<MyPOVResponse<List<Comment>>>() {
            @Override
            public void onResponse(Response<MyPOVResponse<List<Comment>>> response, Retrofit retrofit) {
                if (response.isSuccess()) {
                    if (response.body().getStatus() == 0) {
                        List<Comment> commentList = response.body().getObject();
                        if(commentList != null) {
                            mCommentList.setAdapter(new ObservationDetailsCommentsAdapter(commentList));
                            if (!firstLoad) {
                                //((ObservationDetails) mContext).setNbCommentsTitle(commentList.size());
                            }
                            mSwipeRefreshLayout.setRefreshing(false);
                        } else {
                            //TODO PAS DE COMMENTAIRES
                            mSwipeRefreshLayout.setRefreshing(false);
                        }
                    } else {
                        mSwipeRefreshLayout.setRefreshing(false);
                        Toast.makeText(mContext, response.body().getMessage(), Toast.LENGTH_LONG).show();
                        mUser.logout();
                        ((ObservationDetails) mContext).finish();
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
                getComments(false);
            }
        });
    }

}
