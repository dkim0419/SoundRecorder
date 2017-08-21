/*
 * Copyright (c) 2017. This code was written by iClaude. All rights reserved.
 */

package com.danielkim.soundrecorder.fragments;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.danielkim.soundrecorder.R;
import com.danielkim.soundrecorder.ScheduledRecordingItem;
import com.danielkim.soundrecorder.database.DBHelper;
import com.github.sundeepk.compactcalendarview.CompactCalendarView;
import com.github.sundeepk.compactcalendarview.domain.Event;

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
    private static final String TAG = "CalendarFragment";

    private CompactCalendarView calendarView;
    private TextView tvMonth;
    private TextView tvMsg;

    private RecyclerView.Adapter adapter;
    private List<ScheduledRecordingItem> scheduledRecordings;

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
        // Msg.
        tvMsg = (TextView) v.findViewById(R.id.tvMsg);

        new GetScheduledRecordingsTask().execute();

        return v;
    }

    // Listener for the CompactCalendarView.
    private final CompactCalendarView.CompactCalendarViewListener myCalendarViewListener = new CompactCalendarView.CompactCalendarViewListener() {
        @Override
        public void onDayClick(Date date) {
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

        String msg = scheduledRecordings.size() > 0 ? getString(R.string.frag_sched_details) : getString(R.string.frag_sched_no_recordings);
        tvMsg.setText(msg);
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
            for (ScheduledRecordingItem item : scheduledRecordings) {
                Log.d(TAG, item.toString());
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
            private final TextView tvTime;
            private final TextView tvDate;

            public ItemViewHolder(View v) {
                super(v);
                tvTime = (TextView) v.findViewById(R.id.tvTime);
                tvDate = (TextView) v.findViewById(R.id.tvDate);
            }
        }

        // Adapter.
        private List<ScheduledRecordingItem> items;

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
            holder.tvTime.setText(dateFormat.format(new Date(item.getStart())) + " - " + dateFormat.format(new Date(item.getEnd())));
            holder.tvDate.setText(new SimpleDateFormat("dd/MM/yy", Locale.getDefault()).format(new Date(item.getStart())));
        }
    }
}
