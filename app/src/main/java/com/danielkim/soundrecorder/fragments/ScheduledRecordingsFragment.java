/*
 * Copyright (c) 2017. This code was written by iClaude. All rights reserved.
 */

package com.danielkim.soundrecorder.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.danielkim.soundrecorder.R;
import com.github.sundeepk.compactcalendarview.CompactCalendarView;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/**
 * This Fragment shows all scheduled recordigs using a CalendarView.
 * <p>
 * Created by agost on 16/08/2017.
 */

public class ScheduledRecordingsFragment extends Fragment {

    private static final String ARG_POSITION = "position";
    private static final String LOG_TAG = "ScheduledRecordingsFragment";

    private CompactCalendarView calendarView;
    private TextView tvMonth;
    private int position;

    public static ScheduledRecordingsFragment newInstance(int position) {
        ScheduledRecordingsFragment f = new ScheduledRecordingsFragment();
        Bundle b = new Bundle();
        b.putInt(ARG_POSITION, position);
        f.setArguments(b);

        return f;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        position = getArguments().getInt(ARG_POSITION);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_scheduled_recordings, container, false);
        tvMonth = (TextView) v.findViewById(R.id.tvMonth);
        String month = new SimpleDateFormat("MMMM", Locale.getDefault()).format(Calendar.getInstance().getTime());
        tvMonth.setText(month);
        calendarView = (CompactCalendarView) v.findViewById(R.id.compactcalendar_view);
        calendarView.setListener(myCalendarViewListener);

        return v;
    }

    CompactCalendarView.CompactCalendarViewListener myCalendarViewListener = new CompactCalendarView.CompactCalendarViewListener() {
        @Override
        public void onDayClick(Date date) {

        }

        @Override
        public void onMonthScroll(Date date) {
            DateFormat dateFormat = new SimpleDateFormat("MMMM", Locale.getDefault());
            String month = dateFormat.format(date);
            tvMonth.setText(month);
        }
    };
}
