package com.danielkim.soundrecorder;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.media.MediaRecorder;
import android.os.Environment;
import android.os.SystemClock;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.media.MediaPlayer;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Chronometer;
import android.widget.ImageButton;

import com.astuetz.PagerSlidingTabStrip;
import com.danielkim.soundrecorder.fragments.RecordFragment;
import com.melnykov.fab.FloatingActionButton;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;


public class MainActivity extends ActionBarActivity {

    private static final String LOG_TAG = "SoundRecorder";

    private MediaPlayer mPlayer = null;

    private FloatingActionButton mPlayButton = null;

    //ProgressBar timerCircle = (ProgressBar) findViewById(R.id.timerCircle);

    private boolean mStartPlaying = true;

    private PagerSlidingTabStrip tabs;
    private ViewPager pager;

    private RecordFragment mRecordFragment;
    private com.danielkim.soundrecorder.FileViewerFragment mFileViewerFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        pager = (ViewPager) findViewById(R.id.pager);
        pager.setAdapter(new MyAdapter(getSupportFragmentManager()));
        tabs = (PagerSlidingTabStrip) findViewById(R.id.tabs);
        tabs.setViewPager(pager);


        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        mRecordFragment = new RecordFragment();
        mRecordFragment.setRetainInstance(true);

        mFileViewerFragment = new com.danielkim.soundrecorder.FileViewerFragment();
        mFileViewerFragment.setRetainInstance(true);

        //transaction.add(R.id.container, mRecordFragment);

        transaction.replace(R.id.container, mRecordFragment);
        //transaction.replace(R.id.fragment_file_viewer, mFileViewerFragment);
        transaction.commit();

        File folder = new File(Environment.getExternalStorageDirectory() + "/SoundRecorder");
        boolean success = true;

        //folder /SoundRecorder doesn't exist, create the folder
        if (!folder.exists()) {
            success = folder.mkdir();
        }

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setPopupTheme(R.style.ThemeOverlay_AppCompat_Light);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void onPlay(boolean start) {
        if (start) {
            startPlaying();
        } else {
            stopPlaying();
        }
    }

    private void startPlaying() {
        mPlayer = new MediaPlayer();
        try {
            //mPlayer.setDataSource(mFileName);
            mPlayer.prepare();
            mPlayer.start();
        } catch (IOException e) {
            Log.e(LOG_TAG, "prepare() failed");
        }
    }

    private void stopPlaying() {
        mPlayer.release();
        mPlayer = null;
    }

    public class MyAdapter extends FragmentPagerAdapter {
        private String[] titles = { "Record", "Saved Recordings" };
        public MyAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            switch(position){
                case 0:{
                    return new RecordFragment();
                }
                case 1:{
                    return new com.danielkim.soundrecorder.FileViewerFragment();
                }
            }
            return null;
        }

        @Override
        public int getCount() {
            return titles.length;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return titles[position];
        }
    }

    public MainActivity() {

    }
}
