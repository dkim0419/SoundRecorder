package com.danielkim.soundrecorder.adapters;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.danielkim.soundrecorder.fragments.FileViewerFragment;
import com.danielkim.soundrecorder.fragments.RecordFragment;

public class FragmentsAdapter extends FragmentPagerAdapter {

    private String[] mTitles;

    public FragmentsAdapter(FragmentManager fm, String[] titles) {
        super(fm);
        this.mTitles = titles;
    }

    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case 0: {
                return RecordFragment.newInstance(position);
            }
            case 1: {
                return FileViewerFragment.newInstance(position);
            }
        }
        return null;
    }

    @Override
    public int getCount() {
        return mTitles != null ? mTitles.length : 0;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return mTitles[position];
    }
}