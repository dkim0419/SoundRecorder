/*
 * Copyright (c) 2017 Claudio "iClaude" Agostini <agostini.claudio1@gmail.com>
 * Licensed under the Apache License, Version 2.0
 */

package com.danielkim.soundrecorder.fragments;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v13.app.FragmentCompat;
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
import com.danielkim.soundrecorder.ScheduledRecordingService;
import com.danielkim.soundrecorder.activities.AddScheduledRecordingActivity;
import com.danielkim.soundrecorder.database.DBHelper;
import com.danielkim.soundrecorder.didagger2.App;
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

import javax.inject.Inject;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

/**
 * This Fragment shows all scheduled recordings using a CalendarView.
 * <p>
 * Created by iClaude on 16/08/2017.
 */

public class ScheduledRecordingsFragment extends Fragment implements ScheduledRecordingsFragmentItemAdapter.MyOnItemClickListener {

    private static final String ARG_POSITION = "position";
    private static final int REQUEST_DANGEROUS_PERMISSIONS = 0;
    private static final int ADD_SCHEDULED_RECORDING = 0;
    private static final int EDIT_SCHEDULED_RECORDING = 1;

    private CompactCalendarView calendarView;
    private TextView tvMonth;
    private TextView tvDate;

    private RecyclerView.Adapter adapter;
    private List<ScheduledRecordingItem> scheduledRecordings;
    private Date selectedDate = new Date(System.currentTimeMillis());

    @Inject
    DBHelper dbHelper;
    private final CompositeDisposable compositeDisposable = new CompositeDisposable();

    public static ScheduledRecordingsFragment newInstance(int position) {
        ScheduledRecordingsFragment f = new ScheduledRecordingsFragment();
        Bundle b = new Bundle();
        b.putInt(ARG_POSITION, position);
        f.setArguments(b);

        return f;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        App.getComponent().inject(this);
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

        subscribeToGetRecordingsObservable();

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

    // Get all scheduled recordings and provide them.
    private final Observable<List<ScheduledRecordingItem>> getRecordingsObservable = Observable.create(emitter -> {
        List<ScheduledRecordingItem> recordings = dbHelper.getAllScheduledRecordings();
        emitter.onNext(recordings);
        emitter.onComplete();
    });

    private void subscribeToGetRecordingsObservable() {
        Disposable subscribe = getRecordingsObservable
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::updateUI);
        compositeDisposable.add(subscribe);
    }

    // Update the UI with the list of scheduled recordings.
    private void updateUI(List<ScheduledRecordingItem> scheduledRecordings) {
        calendarView.removeAllEvents();
        for (ScheduledRecordingItem item : scheduledRecordings) {
            Event event = new Event(ContextCompat.getColor(getActivity(), R.color.accent), item.getStart(), item);
            calendarView.addEvent(event, false);
        }
        calendarView.invalidate(); // refresh the calendar view
        myCalendarViewListener.onDayClick(selectedDate); // click to show current day
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
                (dialogInterface, i) -> {
                    final Observable<Integer> deleteItemObservable = Observable.create(emitter -> {
                        int numDeleted = dbHelper.removeScheduledRecording(item.getId());
                        emitter.onNext(numDeleted);
                        emitter.onComplete();
                    });
                    Disposable subscribe = deleteItemObservable
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(this::deleteItemCompleted);
                    compositeDisposable.add(subscribe);
                });
        builder.setCancelable(true);
        builder.setNegativeButton(getString(R.string.dialog_action_cancel),
                (dialog, id) -> dialog.cancel());

        AlertDialog alert = builder.create();
        alert.show();
    }

    // The item has just been deleted.
    private void deleteItemCompleted(int numDeleted) {
        Activity activity = getActivity();
        if (numDeleted > 0) {
            Toast.makeText(activity, activity.getString(R.string.toast_scheduledrecording_deleted), Toast.LENGTH_SHORT).show();
            subscribeToGetRecordingsObservable();
            activity.startService(ScheduledRecordingService.makeIntent(activity, false));
        } else {
            Toast.makeText(activity, activity.getString(R.string.toast_scheduledrecording_deleted_error), Toast.LENGTH_SHORT).show();
        }
    }

    // Click listener of the button to add a new scheduled recording.
    private final View.OnClickListener addScheduledRecordingListener = view -> checkPermissions();

    // Check dangerous permissions for Android Marshmallow+.
    @SuppressWarnings("ConstantConditions")
    private void checkPermissions() {
        // Check permissions.
        boolean writePerm = ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
        boolean audioPerm = ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED;
        String[] arrPermissions;
        if (!writePerm && !audioPerm) {
            arrPermissions = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.RECORD_AUDIO};
        } else if (!writePerm && audioPerm) {
            arrPermissions = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};
        } else if (writePerm && !audioPerm) {
            arrPermissions = new String[]{Manifest.permission.RECORD_AUDIO};
        } else {
            startAddScheduledRecordingActivity();
            return;
        }

        // Request permissions.
        FragmentCompat.requestPermissions(this, arrPermissions, REQUEST_DANGEROUS_PERMISSIONS);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        boolean granted = true;
        for (int grantResult : grantResults) { // we nee all permissions granted
            if (grantResult != PackageManager.PERMISSION_GRANTED)
                granted = false;
        }

        if (granted)
            startAddScheduledRecordingActivity();
        else
            Toast.makeText(getActivity(), getString(R.string.toast_permissions_denied), Toast.LENGTH_LONG).show();
    }

    private void startAddScheduledRecordingActivity() {
        Intent intent = AddScheduledRecordingActivity.makeIntent(getActivity(), selectedDate.getTime());
        startActivityForResult(intent, ADD_SCHEDULED_RECORDING);
    }

    // After a new scheduled recording has been added, get all the recordings and refresh the layout.
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if ((requestCode == ADD_SCHEDULED_RECORDING || requestCode == EDIT_SCHEDULED_RECORDING) && resultCode == Activity.RESULT_OK) {
            subscribeToGetRecordingsObservable();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if (!compositeDisposable.isDisposed())
            compositeDisposable.dispose();
    }
}
