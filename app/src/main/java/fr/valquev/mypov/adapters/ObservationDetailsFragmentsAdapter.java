package fr.valquev.mypov.adapters;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ValQuev on 28/09/15.
 */
public class ObservationDetailsFragmentsAdapter extends FragmentPagerAdapter {

    private final List<Fragment> mFragmentList = new ArrayList<>();
    private final List<String> mFragmentTitleList = new ArrayList<>();

    public ObservationDetailsFragmentsAdapter(FragmentManager manager) {
        super(manager);
    }

    @Override
    public Fragment getItem(int position) {
        return mFragmentList.get(position);
    }

    @Override
    public int getCount() {
        return mFragmentList.size();
    }

    public void addFrag(Fragment fragment, String title) {
        mFragmentList.add(fragment);
        mFragmentTitleList.add(title);
    }

    public void setCommentTitle(String title) {
        mFragmentTitleList.set(1, title);
    }

    @Override
    public CharSequence getPageTitle(int position) {
        //Log.v("TEST", "TITLE = " + mFragmentTitleList.get(position));
        return mFragmentTitleList.get(position);
    }
}