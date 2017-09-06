/*
 * Copyright (c) 2017. This code was written by iClaude. All rights reserved.
 */

package com.danielkim.soundrecorder.activities;

import android.app.DialogFragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;

import com.danielkim.soundrecorder.R;
import com.danielkim.soundrecorder.fragments.DatePickerFragment;
import com.danielkim.soundrecorder.fragments.DatePickerFragment.MyOnDateSetListener;

import java.text.DateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;

/**
 * Activity used to add a new scheduled recording.
 */

public class AddScheduledRecordingActivity extends AppCompatActivity implements MyOnDateSetListener {
    public static final String EXTRA_DATE_LONG = "com.danielkim.soundrecorder.activities.EXTRA_DATE_LONG";

    private TextView tvDateStart;
    private TextView tvDateEnd;
    private TextView tvTimeStart;
    private TextView tvTimeEnd;

    private final DateFormat dateFormat = DateFormat.getDateInstance(DateFormat.MEDIUM);
    private int yearStart, monthStart, dayStart, hourStart, minuteStart;
    private int yearEnd, monthEnd, dayEnd, hourEnd, minuteEnd;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_scheduled_recording);

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
        tvTimeStart.setText(String.format(Locale.getDefault(), "%1$2d:%2$2d", hourStart, minuteStart));
        tvTimeEnd.setText(String.format(Locale.getDefault(), "%1$2d:%2$2d", hourEnd, minuteEnd));
    }

    public void showDatePickerDialog(View view) {
        DialogFragment datePicker = DatePickerFragment.newInstance(view.getId());
        datePicker.show(getFragmentManager(), "datePicker");
    }

    // Callback method for DatePickerFragment.
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

        displayDatesAndTimes();
    }
}
