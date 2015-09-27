package fr.valquev.mypov.activities;

import android.content.Context;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

import fr.valquev.mypov.Observation;
import fr.valquev.mypov.R;
import fr.valquev.mypov.fragments.Map;
import fr.valquev.mypov.fragments.ObservationDetailsComments;
import fr.valquev.mypov.fragments.ObservationDetailsContent;

/**
 * Created by ValQuev on 27/09/15.
 */
public class ObservationDetails extends AppCompatActivity {

    private Context mContext;
    private ViewPager mViewPager;
    private TabLayout mTabLayout;

    private Observation mObservation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_observation_details);

        mContext = this;
        mObservation = getIntent().getParcelableExtra("observation");

        mViewPager = (ViewPager) findViewById(R.id.observation_details_viewpager);
        Toolbar toolbar = (Toolbar) findViewById(R.id.observation_details_toolbar);
        mTabLayout = (TabLayout) findViewById(R.id.observation_details_tabs);

        toolbar.setTitle(mObservation.getNom() + " par " + mObservation.getObservateur().getPseudo());
        setSupportActionBar(toolbar);

        setAB();

        setupViewPager();
    }

    private void setupViewPager() {
        ObservationDetailsFragmentsAdapter adapter = new ObservationDetailsFragmentsAdapter(getSupportFragmentManager());
        Bundle observation = new Bundle();
        observation.putParcelable("observation", mObservation);
        adapter.addFrag(ObservationDetailsContent.instantiate(mContext, ObservationDetailsContent.class.getName(), observation), "DESCRIPTION");
        adapter.addFrag(ObservationDetailsComments.instantiate(mContext, ObservationDetailsComments.class.getName(), observation), "COMMENTAIRES");

        mViewPager.setAdapter(adapter);

        mTabLayout.setupWithViewPager(mViewPager);

        mTabLayout.setOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                mViewPager.setCurrentItem(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
            }
        });
    }

    private void setAB() {
        ActionBar actionBar = getSupportActionBar();

        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        switch(id) {
            case android.R.id.home:
                finish();
                break;
        }

        return super.onOptionsItemSelected(item);
    }
}
