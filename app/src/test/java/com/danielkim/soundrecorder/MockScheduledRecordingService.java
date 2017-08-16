/*
 * Copyright (c) 2017. This code was written by iClaude. All rights reserved.
 */

package com.danielkim.soundrecorder;

import android.app.AlarmManager;
import android.content.Context;
import android.content.Intent;

/**
 * Created by iClaude on 26/07/2017.
 * This is a mock class of ScheduledRecordingService created to test the service with
 * Robolectric.
 * In this mock class you provide a Context and an AlarmManager through the constructor. The
 * alarms are set in the main thread, while in the original service they are set in a
 * background thread.
 */

public class MockScheduledRecordingService extends ScheduledRecordingService {

    public MockScheduledRecordingService(Context context, AlarmManager alarmManager) {
        this.context = context;
        this.alarmManager = alarmManager;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        onStartCommandCalls++; // just for testing

        // Is this a wakeful Service? In this case we have to release the wake-lock at the end.
        wakeful = intent.getBooleanExtra(EXTRA_WAKEFUL, false);
        startIntent = intent;

        resetAlarmManager(); // cancel all pending alarms
        scheduleNextRecording();

        return START_REDELIVER_INTENT;
    }
}
