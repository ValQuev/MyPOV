package fr.valquev.mypov.activities;

import android.content.Context;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

import fr.valquev.mypov.Observation;
import fr.valquev.mypov.R;
import fr.valquev.mypov.adapters.ObservationDetailsFragmentsAdapter;
import fr.valquev.mypov.fragments.ObservationDetailsComments;
import fr.valquev.mypov.fragments.ObservationDetailsContent;

/**
 * Created by ValQuev on 27/09/15.
 */
public class ObservationDetails extends AppCompatActivity {

    private Context mContext;
    private ViewPager mViewPager;
    private TabLayout mTabLayout;
    private FloatingActionButton addCommentFAB;

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
        addCommentFAB = (FloatingActionButton) findViewById(R.id.fab_observation_details_add_comment);
        addCommentFAB.hide();

        toolbar.setTitle(mObservation.getNom() + " par " + mObservation.getObservateur().getPseudo());
        setSupportActionBar(toolbar);

        setAB();

        setupViewPager();
    }

    private void setupViewPager() {
        ObservationDetailsFragmentsAdapter adapter = new ObservationDetailsFragmentsAdapter(getSupportFragmentManager());
        Bundle observation = new Bundle();
        observation.putParcelable("observation", mObservation);
        adapter.addFrag(ObservationDetailsContent.instantiate(mContext, ObservationDetailsContent.class.getName(), observation), getResources().getString(R.string.description_up));
        adapter.addFrag(ObservationDetailsComments.instantiate(mContext, ObservationDetailsComments.class.getName(), observation), getResources().getString(R.string.comments_up));

        mViewPager.setAdapter(adapter);
        mViewPager.setCurrentItem(0);

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
                mViewPager.setCurrentItem(tab.getPosition());
            }
        });

        mViewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                switch (position) {
                    case 0:
                        addCommentFAB.hide();
                        break;

                    case 1:
                        addCommentFAB.show();
                        break;
                }
            }

            @Override
            public void onPageSelected(int position) {

            }

            @Override
            public void onPageScrollStateChanged(int state) {

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
