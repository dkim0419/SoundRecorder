/*
 * Year: 2017. This class was edited by iClaude.
 */

package com.danielkim.soundrecorder.activities;

import android.app.Fragment;
import android.app.FragmentManager;
import android.content.ComponentName;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v13.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.astuetz.PagerSlidingTabStrip;
import com.danielkim.soundrecorder.R;
import com.danielkim.soundrecorder.RecordingService;
import com.danielkim.soundrecorder.fragments.FileViewerFragment;
import com.danielkim.soundrecorder.fragments.LicensesFragment;
import com.danielkim.soundrecorder.fragments.RecordFragment;
import com.danielkim.soundrecorder.fragments.ScheduledRecordingsFragment;


public class MainActivity extends AppCompatActivity implements RecordFragment.ServiceOperations {

    private static final String TAG = "SCHEDULED_RECORDER_TAG";

    private RecordFragment recordFragment = null;

    private RecordingService recordingService;
    private boolean serviceConnected = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ViewPager pager = (ViewPager) findViewById(R.id.pager);
        pager.setAdapter(new MyAdapter(getFragmentManager()));
        PagerSlidingTabStrip tabs = (PagerSlidingTabStrip) findViewById(R.id.tabs);
        tabs.setViewPager(pager);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setPopupTheme(R.style.ThemeOverlay_AppCompat_Light);
        setSupportActionBar(toolbar);
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
        // Handle presses on the action bar items
        switch (item.getItemId()) {
            case R.id.action_licenses:
                openLicenses();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void openLicenses() {
        LicensesFragment licensesFragment = new LicensesFragment();
        licensesFragment.show(getFragmentManager().beginTransaction(), "dialog_licenses");
    }

    public class MyAdapter extends FragmentPagerAdapter {
        private final String[] titles = {getString(R.string.tab_title_record),
                getString(R.string.tab_title_saved_recordings),
                getString(R.string.tab_title_scheduled_recordings)};

        public MyAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            switch(position){
                case 0:{
                    recordFragment = RecordFragment.newInstance(position);
                    return recordFragment;
                }
                case 1:{
                    return FileViewerFragment.newInstance(position);
                }
                case 2: {
                    return ScheduledRecordingsFragment.newInstance(position);
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

    // Connection with local Service.
    @Override
    protected void onStart() {
        super.onStart();

        Log.d(TAG, "MainActivity - call bind to Service");
        startService(RecordingService.makeIntent(this, true));
        bindService(RecordingService.makeIntent(this, true), serviceConnection, BIND_AUTO_CREATE);
    }

    // Disconnection from local Service.
    @Override
    protected void onStop() {
        super.onStop();

        if (serviceConnected) {
            Log.d(TAG, "MainActivity - call unbind from Service");
            unbindService(serviceConnection);
            if (!isServiceRecording()) stopService(RecordingService.makeIntent(this));
            recordingService.setOnRecordingStatusChangedListener(null);
            recordingService = null;
            serviceConnected = false;
            if (recordFragment != null) {
                recordFragment.serviceConnection(false);
            }
        }
    }

    /*
        Implementation of RecordFragment.ServiceOperations.
        RecordFragment uses this interface to communicate with this Activity in order to interact
        with RecordingService (the connection with the Service is managed by this Activity).
     */
    @Override
    public void requestStartRecording() {
        if (recordingService != null && !isServiceRecording()) {
            recordingService.startRecording(0);
        }
    }

    @Override
    public void requestStopRecording() {
        if (recordingService != null) {
            recordingService.stopRecording();
        }
    }

    @Override
    public boolean isServiceConnected() {
        return serviceConnected;
    }

    @Override
    public boolean isServiceRecording() {
        return recordingService != null && recordingService.isRecording();
    }

    /*
        Implementation of ServiceConnection interface.
        The interaction with the Service is managed by this Activity.
    */
    private final ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            Log.d(TAG, "MainActivity - Service connected");
            recordingService = ((RecordingService.LocalBinder) iBinder).getService();
            serviceConnected = true;
            if (recordFragment != null) {
                recordFragment.serviceConnection(true);
            }
            recordingService.setOnRecordingStatusChangedListener(onScheduledRecordingListener);
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            Log.d(TAG, "MainActivity - Service disconnected");
            recordingService.setOnRecordingStatusChangedListener(null);
            recordingService = null;
            serviceConnected = false;
            if (recordFragment != null) {
                recordFragment.serviceConnection(false);
            }
        }
    };

    /*
        Implementation of RecordingService.OnRecordingStatusChangedListener interface.
        The Service uses this interface to communicate to the connected Activity that a
        recording has started/stopped, and the seconds elapsed, so that the UI can be updated
        accordingly.
    */
    private final RecordingService.OnRecordingStatusChangedListener onScheduledRecordingListener = new RecordingService.OnRecordingStatusChangedListener() {
        @Override
        public void onRecordingStarted() {
            if (recordFragment != null)
                recordFragment.recordingStarted();
        }

        @Override
        public void onRecordingStopped(String filePath) {
            if (recordFragment != null)
                recordFragment.recordingStopped(filePath);
        }

        // This method is called from a separate thread.
        @Override
        public void onTimerChanged(int seconds) {
            if (recordFragment != null) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        recordFragment.timerChanged(seconds);
                    }
                });
            }
        }
    };

}
