package com.danielkim.soundrecorder.activities;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import com.astuetz.PagerSlidingTabStrip;
import com.danielkim.soundrecorder.R;
import com.danielkim.soundrecorder.fragments.FileViewerFragment;
import com.danielkim.soundrecorder.fragments.LicensesFragment;
import com.danielkim.soundrecorder.fragments.RecordFragment;
import com.danielkim.soundrecorder.fragments.ScheduledRecordingsFragment;


public class MainActivity extends ActionBarActivity{

    private static final String LOG_TAG = MainActivity.class.getSimpleName();

    private PagerSlidingTabStrip tabs;
    private ViewPager pager;

    //@RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Test: TODO delete

        /*DBHelper dbHelper = new DBHelper(this);
        dbHelper.restoreDatabase();
        dbHelper.addScheduledRecording(new GregorianCalendar(2017, 7, 25, 13, 30).getTimeInMillis(), new GregorianCalendar(2017, 7, 25, 13, 45).getTimeInMillis());
        dbHelper.addScheduledRecording(new GregorianCalendar(2017, 7, 25, 10, 00).getTimeInMillis(), new GregorianCalendar(2017, 7, 25, 11, 00).getTimeInMillis());
        dbHelper.addScheduledRecording(new GregorianCalendar(2017, 7, 28, 15, 00).getTimeInMillis(), new GregorianCalendar(2017, 7, 28, 16, 30).getTimeInMillis());
        dbHelper.addScheduledRecording(new GregorianCalendar(2017, 7, 28, 7, 00).getTimeInMillis(), new GregorianCalendar(2017, 7, 28, 8, 00).getTimeInMillis());
        dbHelper.addScheduledRecording(new GregorianCalendar(2017, 7, 28, 21, 00).getTimeInMillis(), new GregorianCalendar(2017, 7, 28, 21, 30).getTimeInMillis());
        dbHelper.addScheduledRecording(new GregorianCalendar(2017, 7, 21, 23, 00).getTimeInMillis(), new GregorianCalendar(2017, 7, 21, 23, 30).getTimeInMillis());
        dbHelper.addScheduledRecording(new GregorianCalendar(2017, 7, 21, 20, 15).getTimeInMillis(), new GregorianCalendar(2017, 7, 21, 20, 30).getTimeInMillis());
*/
        // end test

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
                    return RecordFragment.newInstance(position);
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
}
