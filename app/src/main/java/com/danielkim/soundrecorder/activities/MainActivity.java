package com.danielkim.soundrecorder.activities;

import android.content.ComponentName;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.astuetz.PagerSlidingTabStrip;
import com.danielkim.soundrecorder.R;
import com.danielkim.soundrecorder.RecordingService2;
import com.danielkim.soundrecorder.fragments.FileViewerFragment;
import com.danielkim.soundrecorder.fragments.LicensesFragment;
import com.danielkim.soundrecorder.fragments.RecordFragment;
import com.danielkim.soundrecorder.fragments.ScheduledRecordingsFragment;


public class MainActivity extends ActionBarActivity implements RecordingService2.OnTimerChangedListener, RecordFragment.ServiceOperationsListener {

    private static final String TAG = "SCHEDULED_RECORDER_TAG";

    private PagerSlidingTabStrip tabs;
    private ViewPager pager;
    private RecordFragment recordFragment = null;

    private RecordingService2 recordingService;
    private boolean serviceConnected = false;


    //@RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        pager = (ViewPager) findViewById(R.id.pager);
        pager.setAdapter(new MyAdapter(getSupportFragmentManager()));
        tabs = (PagerSlidingTabStrip) findViewById(R.id.tabs);
        tabs.setViewPager(pager);

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
        // Handle presses on the action bar items
        switch (item.getItemId()) {
            case R.id.action_licenses:
                openLicenses();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void openLicenses(){
        LicensesFragment licensesFragment = new LicensesFragment();
        licensesFragment.show(getSupportFragmentManager().beginTransaction(), "dialog_licenses");
    }

    public class MyAdapter extends FragmentPagerAdapter {
        private String[] titles = { getString(R.string.tab_title_record),
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
        startService(RecordingService2.makeIntent(this));
        bindService(RecordingService2.makeIntent(this), serviceConnection, BIND_AUTO_CREATE);
    }

    // Disconnection from local Service.
    @Override
    protected void onStop() {
        super.onStop();

        if (serviceConnected) {
            Log.d(TAG, "MainActivity - call unbind from Service");
            unbindService(serviceConnection);
            if (!isRecording()) stopService(RecordingService2.makeIntent(this));
            recordingService = null;
            serviceConnected = false;
            if (recordFragment != null) {
                recordFragment.serviceConnection(serviceConnected);
            }
        }
    }

    /*
        Implementation of RecordFragment.ServiceOperationsListener.
        RecordFragment uses this interface to communicate with this Activity in order to interact
        with RecordingService (the connection with the Service is managed by this Activity).
     */
    @Override
    public void onStartRecord() {
        if (recordingService != null && !isRecording()) {
            recordingService.startRecording(0);
        }
    }

    @Override
    public void onStopRecord() {
        if (recordingService != null) {
            recordingService.stopRecording();
        }
    }

    @Override
    public boolean isConnected() {
        return serviceConnected;
    }

    @Override
    public boolean serviceIsRecording() {
        return isRecording();
    }

    /*
        Implementation of ServiceConnection interface.
        The interaction with the Service is managed by this Activity.
    */
    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            Log.d(TAG, "MainActivity - Service connected");
            recordingService = ((RecordingService2.LocalBinder) iBinder).getService();
            serviceConnected = true;
            if (recordFragment != null) {
                recordFragment.serviceConnection(serviceConnected);
            }
            recordingService.setOnTimerChangedListener(MainActivity.this);
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            Log.d(TAG, "MainActivity - Service disconnected");
            recordingService = null;
            serviceConnected = false;
            if (recordFragment != null) {
                recordFragment.serviceConnection(serviceConnected);
            }
            recordingService.setOnTimerChangedListener(null);
        }
    };

    // Is the connected Service currently recording?
    public boolean isRecording() {
        if (recordingService != null) {
            return recordingService.isRecording();
        }
        return false;
    }

    /*
        Implementation of RecordingService2.OnTimerChangedListener interface.
        The Service uses this interface to communicate the progress of the recording in seconds.
        The caller of this method is on a separate thread.
    */
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
}
