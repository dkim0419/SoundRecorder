/*
 * Copyright (c) 2017. This code was written by iClaude. All rights reserved.
 */

package com.danielkim.soundrecorder.fragments;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
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
import android.widget.Toast;

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

public class ScheduledRecordingsFragment extends Fragment implements ScheduledRecordingsFragmentItemAdapter.MyOnItemClickListener {

    private static final String ARG_POSITION = "position";
    private static final int ADD_SCHEDULED_RECORDING = 0;
    private static final int EDIT_SCHEDULED_RECORDING = 1;
    private static final String TAG = "SRFragment";

    private CompactCalendarView calendarView;
    private TextView tvMonth;
    private TextView tvDate;

    private RecyclerView.Adapter adapter;
    private List<ScheduledRecordingItem> scheduledRecordings;
    private Date selectedDate = new Date(System.currentTimeMillis());

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
        adapter = new ScheduledRecordingsFragmentItemAdapter(scheduledRecordings, this, recyclerView);
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
        ((ScheduledRecordingsFragmentItemAdapter) adapter).setItems(scheduledRecordings);
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
            myCalendarViewListener.onDayClick(selectedDate); // click to show current day
        }
    }

    // Click listener for the elements of the RecyclerView (for editing scheduled recordings).
    @Override
    public void onItemClick(ScheduledRecordingItem item) {
        Intent intent = AddScheduledRecordingActivity.makeIntent(getActivity(), item);
        startActivityForResult(intent, EDIT_SCHEDULED_RECORDING);
    }

    // Long click listener for the elements of the RecyclerView (for deleting or renaming scheduled recordings).
    @Override
    public void onItemLongClick(ScheduledRecordingItem item) {
        // Item delete confirm
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(getString(R.string.dialog_title_delete));
        builder.setMessage(R.string.dialog_text_delete_generic);
        builder.setPositiveButton(R.string.dialog_action_ok,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        new DeleteItemTask().execute(item.getId());
                    }
                });
        builder.setCancelable(true);
        builder.setNegativeButton(getString(R.string.dialog_action_cancel),
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });

        AlertDialog alert = builder.create();
        alert.show();
    }

    // Retrieve all scheduled recordings in a separate thread.
    private class DeleteItemTask extends AsyncTask<Long, Void, Integer> {
        private final DBHelper dbHelper = new DBHelper(getActivity());

        protected Integer doInBackground(Long... params) {
            return dbHelper.removeScheduledRecording(params[0]);
        }

        protected void onPostExecute(Integer result) {
            if (result > 0) {
                Toast.makeText(getActivity(), getString(R.string.toast_scheduledrecording_deleted), Toast.LENGTH_SHORT).show();
                new GetScheduledRecordingsTask().execute();
            } else {
                Toast.makeText(getActivity(), getString(R.string.toast_scheduledrecording_deleted_error), Toast.LENGTH_SHORT).show();
            }
        }
    }

    // Click listener of the button to add a new scheduled recording.
    private final View.OnClickListener addScheduledRecordingListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            Intent intent = AddScheduledRecordingActivity.makeIntent(getActivity(), selectedDate.getTime());
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
