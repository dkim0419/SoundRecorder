/*
 * Copyright (c) 2017. This code was written by iClaude. All rights reserved.
 */

package com.danielkim.soundrecorder.activities;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.widget.DatePicker;
import android.widget.TextView;

import com.danielkim.soundrecorder.R;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.GregorianCalendar;

/**
 * Activity used to add a new scheduled recording.
 */

public class AddScheduledRecordingActivity extends AppCompatActivity {
    public static final String EXTRA_DATE_LONG = "com.danielkim.soundrecorder.activities.EXTRA_DATE_LONG";

    private TextView tvDateStart;
    private TextView tvDateEnd;
    private TextView tvTimeStart;
    private TextView tvTimeEnd;

    private DateFormat dateFormat = new SimpleDateFormat("EEE d MMM yyyy");
    private DateFormat timeFormat = new SimpleDateFormat("HH:mm");
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
        tvDateStart.setText(dateFormat.format(new Date(selectedDateLong)));
        tvDateEnd.setText(dateFormat.format(new Date(selectedDateLong)));
        tvTimeStart.setText(timeFormat.format(new Date(System.currentTimeMillis())));
        tvTimeEnd.setText(timeFormat.format(new Date(System.currentTimeMillis() + 1000 * 60 * 60)));
    }

    // Listener of DatePickerDialog for start date.
    DatePickerDialog.OnDateSetListener dateStartListener = new DatePickerDialog.OnDateSetListener() {
        @Override
        public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
            yearStart = year;
            monthStart = month;
            dayStart = dayOfMonth;
            tvDateStart.setText(dateFormat.format(new GregorianCalendar(year, month, dayOfMonth).getTime()));
        }
    };

    // Listener of DatePickerDialog for end date.
    DatePickerDialog.OnDateSetListener dateEndListener = new DatePickerDialog.OnDateSetListener() {
        @Override
        public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
            yearEnd = year;
            monthEnd = month;
            dayEnd = dayOfMonth;
            tvDateEnd.setText(dateFormat.format(new GregorianCalendar(year, month, dayOfMonth).getTime()));
        }
    };
}
