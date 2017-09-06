/*
 * Copyright (c) 2017. This code was written by iClaude. All rights reserved.
 */

package com.danielkim.soundrecorder.activities;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.DatePicker;
import android.widget.TextView;

import com.danielkim.soundrecorder.R;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

/**
 * Activity used to add a new scheduled recording.
 */

public class AddScheduledRecordingActivity extends AppCompatActivity implements AddScheduledRecordingActivity.DatePickerFragment.MyOnDateSetListener {
    public static final String EXTRA_DATE_LONG = "com.danielkim.soundrecorder.activities.EXTRA_DATE_LONG";
    public static final int DATE_START = 0;
    public static final int DATE_END = 1;

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
        initVariables(selectedDateLong);
        Date date = new Date(selectedDateLong);
        tvDateStart.setText(dateFormat.format(date));
        tvDateEnd.setText(dateFormat.format(date));
        tvTimeStart.setText(timeFormat.format(new Date(System.currentTimeMillis())));
        tvTimeEnd.setText(timeFormat.format(new Date(System.currentTimeMillis() + 1000 * 60 * 60)));
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

    private void displayDatesAndTimes() {
        tvDateStart.setText(dateFormat.format(new Date(new GregorianCalendar(yearStart, monthStart, dayStart).getTimeInMillis())));
        tvDateEnd.setText(dateFormat.format(new Date(new GregorianCalendar(yearEnd, monthEnd, dayEnd).getTimeInMillis())));

    }


    public void showDatePickerDialog(View view) {
        int dateType = view.getId() == R.id.tvDateStart ? DATE_START : DATE_END;
        DialogFragment dateFragment = DatePickerFragment.newInstance(dateType);
        dateFragment.show(getFragmentManager(), "datePicker");
    }

    // Shows a dialog to pick a date.
    public static class DatePickerFragment extends DialogFragment implements DatePickerDialog.OnDateSetListener {
        private static final String DATE_TYPE = "dateType";

        private int dateType;
        private MyOnDateSetListener listener;

        public static DatePickerFragment newInstance(int dateType) {
            DatePickerFragment f = new DatePickerFragment();
            Bundle bundle = new Bundle();
            bundle.putInt(DATE_TYPE, dateType);
            f.setArguments(bundle);

            return f;
        }

        public int getDateType() {
            return getArguments().getInt(DATE_TYPE, 0);
        }

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            final Calendar c = Calendar.getInstance();
            int year = c.get(Calendar.YEAR);
            int month = c.get(Calendar.MONTH);
            int day = c.get(Calendar.DAY_OF_MONTH);

            return new DatePickerDialog(getActivity(), this, year, month, day);
        }

        @Override
        public void onAttach(Context context) {
            super.onAttach(context);

            try {
                listener = (MyOnDateSetListener) context;
            } catch (ClassCastException e) {
                throw new ClassCastException(((Activity) context).toString() + "must implement DatePickerFragment.MyOnDateSetListener");
            }
        }

        @Override
        public void onDateSet(DatePicker datePicker, int year, int month, int day) {
            if (listener != null) {
                listener.onDateSet(getArguments().getInt(DATE_TYPE, 0), year, month, day);
            }
        }

        // Interface form communication with the Activity.
        interface MyOnDateSetListener {
            void onDateSet(int dateType, int year, int month, int day);
        }
    }


    // Callback method for DatePickerFragment.
    @Override
    public void onDateSet(int dateType, int year, int month, int day) {
        if (dateType == DATE_START) {
            yearStart = year;
            monthStart = month;
            dayStart = day;
            tvDateStart.setText(dateFormat.format(new GregorianCalendar(year, month, day).getTime()));
        } else {
            yearEnd = year;
            monthEnd = month;
            dayEnd = day;
            tvDateEnd.setText(dateFormat.format(new GregorianCalendar(year, month, day).getTime()));
        }
    }
}
