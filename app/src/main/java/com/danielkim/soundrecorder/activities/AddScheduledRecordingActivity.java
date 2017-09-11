/*
 * Copyright (c) 2017. This code was written by iClaude. All rights reserved.
 */

package com.danielkim.soundrecorder.activities;

import android.app.DialogFragment;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.danielkim.soundrecorder.R;
import com.danielkim.soundrecorder.database.DBHelper;
import com.danielkim.soundrecorder.fragments.DatePickerFragment;
import com.danielkim.soundrecorder.fragments.DatePickerFragment.MyOnDateSetListener;
import com.danielkim.soundrecorder.fragments.TimePickerFragment;
import com.danielkim.soundrecorder.fragments.TimePickerFragment.MyOnTimeSetListener;

import java.text.DateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;

/**
 * Activity used to add a new scheduled recording.
 */

public class AddScheduledRecordingActivity extends AppCompatActivity implements MyOnDateSetListener, MyOnTimeSetListener {
    public static final String EXTRA_DATE_LONG = "com.danielkim.soundrecorder.activities.EXTRA_DATE_LONG";

    private TextView tvDateStart;
    private TextView tvDateEnd;
    private TextView tvTimeStart;
    private TextView tvTimeEnd;

    private final DateFormat dateFormat = DateFormat.getDateInstance(DateFormat.MEDIUM);
    private int yearStart, monthStart, dayStart, hourStart, minuteStart;
    private int yearEnd, monthEnd, dayEnd, hourEnd, minuteEnd;
    private boolean timesCorrect = true;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_scheduled_recording);
        Toolbar myToolbar = (Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);


        tvDateStart = (TextView) findViewById(R.id.tvDateStart);
        tvDateEnd = (TextView) findViewById(R.id.tvDateEnd);
        tvTimeStart = (TextView) findViewById(R.id.tvTimeStart);
        tvTimeEnd = (TextView) findViewById(R.id.tvTimeEnd);

        long selectedDateLong = getIntent().getLongExtra(EXTRA_DATE_LONG, System.currentTimeMillis());
        initVariables(selectedDateLong);
        displayDatesAndTimes();
    }

    // Initialize starting and ending days and times.
    private void initVariables(long selectedDateLong) {
        Calendar cal = new GregorianCalendar();
        cal.setTimeInMillis(selectedDateLong);
        yearStart = yearEnd = cal.get(Calendar.YEAR);
        monthStart = monthEnd = cal.get(Calendar.MONTH);
        dayStart = dayEnd = cal.get(Calendar.DAY_OF_MONTH);
        hourStart = 0;
        minuteStart = 0;
        hourEnd = 1;
        minuteEnd = 0;
    }

    // When dates and times change, display them again.
    private void displayDatesAndTimes() {
        tvDateStart.setText(dateFormat.format(new Date(new GregorianCalendar(yearStart, monthStart, dayStart).getTimeInMillis())));
        tvDateEnd.setText(dateFormat.format(new Date(new GregorianCalendar(yearEnd, monthEnd, dayEnd).getTimeInMillis())));
        tvTimeStart.setText(String.format(Locale.getDefault(), "%1$02d:%2$02d", hourStart, minuteStart));
        tvTimeEnd.setText(String.format(Locale.getDefault(), "%1$02d:%2$02d", hourEnd, minuteEnd));
    }

    public void showDatePickerDialog(View view) {
        DialogFragment datePicker = DatePickerFragment.newInstance(view.getId());
        datePicker.show(getFragmentManager(), "datePicker");
    }

    public void showTimePickerDialog(View view) {
        DialogFragment timePicker = TimePickerFragment.newInstance(view.getId());
        timePicker.show(getFragmentManager(), "timePicker");
    }

    // Callback methods for DatePickerFragment and TimePickerFragment.
    @Override
    public void onDateSet(long viewId, int year, int month, int day) {
        if (viewId == R.id.tvDateStart) {
            yearStart = year;
            monthStart = month;
            dayStart = day;
        } else if (viewId == R.id.tvDateEnd) {
            yearEnd = year;
            monthEnd = month;
            dayEnd = day;
        }

        checkDatesAndTimes();
        displayDatesAndTimes();
    }

    @Override
    public void onTimeSet(long viewId, int hour, int minute) {
        if (viewId == R.id.tvTimeStart) {
            hourStart = hour;
            minuteStart = minute;
        } else if (viewId == R.id.tvTimeEnd) {
            hourEnd = hour;
            minuteEnd = minute;
        }

        checkDatesAndTimes();
        displayDatesAndTimes();
    }

    private void checkDatesAndTimes() {
        if (!datesTimesCorrect()) {
            timesCorrect = false;
            tvDateStart.setTextColor(ContextCompat.getColor(this, R.color.primary_dark));
            tvTimeStart.setTextColor(ContextCompat.getColor(this, R.color.primary_dark));

        } else {
            timesCorrect = true;
            tvDateStart.setTextColor(ContextCompat.getColor(this, R.color.primary_text));
            tvTimeStart.setTextColor(ContextCompat.getColor(this, R.color.primary_text));
        }
    }

    // Dates and times are correct?
    private boolean datesTimesCorrect() {
        Calendar start = new GregorianCalendar(yearStart, monthStart, dayStart, hourStart, minuteStart);
        Calendar end = new GregorianCalendar(yearEnd, monthEnd, dayEnd, hourEnd, minuteEnd);
        return end.after(start);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_addscheduledrecording, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_save:
                addScheduledRecording();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void addScheduledRecording() {
        if (timesCorrect) {
            new AddScheduledRecordingsTask().execute();
        } else {
            Toast.makeText(this, getString(R.string.toast_time_uncorrect), Toast.LENGTH_SHORT).show();
        }
    }

    private class AddScheduledRecordingsTask extends AsyncTask<Void, Void, Long> {
        private final DBHelper dbHelper = new DBHelper(AddScheduledRecordingActivity.this);

        protected Long doInBackground(Void... params) {
            long startLong = new GregorianCalendar(yearStart, monthStart, dayStart, hourStart, minuteStart).getTimeInMillis();
            long endLong = new GregorianCalendar(yearEnd, monthEnd, dayEnd, hourEnd, minuteEnd).getTimeInMillis();
            return dbHelper.addScheduledRecording(startLong, endLong);
        }

        protected void onPostExecute(Long rowId) {
            String msg = rowId == -1 ? getString(R.string.toast_scheduledrecording_added_error) : getString(R.string.toast_scheduledrecording_added);
            Toast.makeText(AddScheduledRecordingActivity.this, msg, Toast.LENGTH_SHORT).show();
            finish();
        }
    }

}
