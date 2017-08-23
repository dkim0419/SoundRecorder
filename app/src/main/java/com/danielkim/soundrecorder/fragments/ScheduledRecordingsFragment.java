/*
 * Copyright (c) 2017. This code was written by iClaude. All rights reserved.
 */

package com.danielkim.soundrecorder.fragments;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.danielkim.soundrecorder.R;
import com.danielkim.soundrecorder.ScheduledRecordingItem;
import com.danielkim.soundrecorder.activities.AddScheduledRecordingActivity;
import com.danielkim.soundrecorder.database.DBHelper;
import com.github.sundeepk.compactcalendarview.CompactCalendarView;
import com.github.sundeepk.compactcalendarview.domain.Event;
import com.melnykov.fab.FloatingActionButton;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * This Fragment shows all scheduled recordings using a CalendarView.
 * <p>
 * Created by iClaude on 16/08/2017.
 */

public class ScheduledRecordingsFragment extends Fragment {

    private static final String ARG_POSITION = "position";
    private static final int ADD_SCHEDULED_RECORDING = 0;

    private CompactCalendarView calendarView;
    private TextView tvMonth;
    private TextView tvDate;

    private RecyclerView.Adapter adapter;
    private List<ScheduledRecordingItem> scheduledRecordings;
    private Date selectedDate;

    public static ScheduledRecordingsFragment newInstance(int position) {
        ScheduledRecordingsFragment f = new ScheduledRecordingsFragment();
        Bundle b = new Bundle();
        b.putInt(ARG_POSITION, position);
        f.setArguments(b);

        return f;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_scheduled_recordings, container, false);

        // Title.
        tvMonth = (TextView) v.findViewById(R.id.tvMonth);
        String month = new SimpleDateFormat("MMMM", Locale.getDefault()).format(Calendar.getInstance().getTime());
        tvMonth.setText(month);
        // Calendar view.
        calendarView = (CompactCalendarView) v.findViewById(R.id.compactcalendar_view);
        calendarView.setListener(myCalendarViewListener);
        // List of events for the selected day.
        RecyclerView recyclerView = (RecyclerView) v.findViewById(R.id.rvRecordings);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(layoutManager);
        scheduledRecordings = new ArrayList<>();
        adapter = new ItemAdapter(scheduledRecordings);
        recyclerView.setAdapter(adapter);
        // Selected day.
        tvDate = (TextView) v.findViewById(R.id.tvDate);
        // Add new scheduled recording button.
        FloatingActionButton mRecordButton = (FloatingActionButton) v.findViewById(R.id.fab_add);
        mRecordButton.setColorNormal(ContextCompat.getColor(getActivity(), R.color.primary));
        mRecordButton.setColorPressed(ContextCompat.getColor(getActivity(), R.color.primary_dark));
        mRecordButton.setOnClickListener(addScheduledRecordingListener);

        new GetScheduledRecordingsTask().execute();

        return v;
    }

    // Listener for the CompactCalendarView.
    private final CompactCalendarView.CompactCalendarViewListener myCalendarViewListener = new CompactCalendarView.CompactCalendarViewListener() {
        @Override
        public void onDayClick(Date date) {
            selectedDate = date;
            displayScheduledRecordings(date);
        }

        @Override
        public void onMonthScroll(Date date) {
            DateFormat dateFormat = new SimpleDateFormat("MMMM", Locale.getDefault());
            String month = dateFormat.format(date);
            tvMonth.setText(month);
        }
    };

    // Display the list of scheduled recordings for the selected day.
    private void displayScheduledRecordings(Date date) {
        List<Event> events = calendarView.getEvents(date.getTime());
        // Put the events in a list of ScheduledRecordingItem suitable for the adapter.
        scheduledRecordings.clear();
        for (Event event : events) {
            scheduledRecordings.add((ScheduledRecordingItem) event.getData());
        }
        Collections.sort(scheduledRecordings);
        ((ItemAdapter) adapter).setItems(scheduledRecordings);
        adapter.notifyDataSetChanged();

        tvDate.setText(new SimpleDateFormat("EEEE d", Locale.getDefault()).format(date));
    }

    // Retrieve all scheduled recordings in a separate thread.
    private class GetScheduledRecordingsTask extends AsyncTask<Void, Void, List<ScheduledRecordingItem>> {
        private final DBHelper dbHelper = new DBHelper(getActivity());

        public GetScheduledRecordingsTask() {
        }

        protected List<ScheduledRecordingItem> doInBackground(Void... params) {
            return dbHelper.getAllScheduledRecordings();
        }

        protected void onPostExecute(List<ScheduledRecordingItem> scheduledRecordings) {
            calendarView.removeAllEvents();
            for (ScheduledRecordingItem item : scheduledRecordings) {
                Event event = new Event(ContextCompat.getColor(getActivity(), R.color.accent), item.getStart(), item);
                calendarView.addEvent(event, false);
            }
            calendarView.invalidate(); // refresh the calendar view
            myCalendarViewListener.onDayClick(new Date(System.currentTimeMillis())); // click to show current day
        }
    }

    // Adapter of the RecyclerView.
    public class ItemAdapter extends RecyclerView.Adapter<ItemAdapter.ItemViewHolder> {

        // ViewHolder.
        public class ItemViewHolder extends RecyclerView.ViewHolder {
            private final TextView tvStart;
            private final TextView tvEnd;
            private final TextView tvColor;

            public ItemViewHolder(View v) {
                super(v);
                tvStart = (TextView) v.findViewById(R.id.tvStart);
                tvEnd = (TextView) v.findViewById(R.id.tvEnd);
                tvColor = (TextView) v.findViewById(R.id.tvColor);
            }
        }

        // Adapter.
        private List<ScheduledRecordingItem> items;
        private final int[] colors = {Color.argb(255, 255, 193, 7),
                Color.argb(255, 244, 67, 54), Color.argb(255, 99, 233, 112),
                Color.argb(255, 7, 168, 255), Color.argb(255, 255, 7, 251),
                Color.argb(255, 255, 61, 7), Color.argb(255, 205, 7, 255)};

        public ItemAdapter(List<ScheduledRecordingItem> items) {
            this.items = items;
        }

        public void setItems(List<ScheduledRecordingItem> items) {
            this.items = items;
        }

        @Override
        public int getItemCount() {
            return items.size();
        }

        @Override
        public ItemViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
            View view = LayoutInflater
                    .from(viewGroup.getContext())
                    .inflate(R.layout.fragment_scheduled_recordings_item, viewGroup, false);

            return new ItemViewHolder(view);
        }

        @Override
        public void onBindViewHolder(ItemViewHolder holder, int position) {
            ScheduledRecordingItem item = items.get(position);
            if (item == null) return;
            DateFormat dateFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
            holder.tvStart.setText(dateFormat.format(new Date(item.getStart())));
            holder.tvEnd.setText(dateFormat.format(new Date(item.getEnd())));

            int posCol = position % (colors.length);
            holder.tvColor.setBackgroundColor(colors[posCol]);
        }
    }

    // Click listener of the button to add a new scheduled recording.
    private final View.OnClickListener addScheduledRecordingListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            Intent intent = new Intent(getActivity(), AddScheduledRecordingActivity.class);
            intent.putExtra(AddScheduledRecordingActivity.EXTRA_DATE_LONG, selectedDate.getTime());
            startActivityForResult(intent, ADD_SCHEDULED_RECORDING);
        }
    };

    // After a new scheduled recording has been added, get all the recordings and refresh the layout.
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == ADD_SCHEDULED_RECORDING && resultCode == Activity.RESULT_OK) {
            new GetScheduledRecordingsTask().execute();
        }
    }
}
