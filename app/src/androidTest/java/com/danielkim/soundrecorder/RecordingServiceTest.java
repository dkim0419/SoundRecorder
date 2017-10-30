/*
 * Copyright (c) 2017. This code was written by iClaude. All rights reserved.
 */

package com.danielkim.soundrecorder;

import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.support.test.InstrumentationRegistry;
import android.support.test.filters.LargeTest;
import android.support.test.rule.ServiceTestRule;
import android.support.test.runner.AndroidJUnit4;

import com.danielkim.soundrecorder.database.DBHelper;
import com.danielkim.soundrecorder.utils.Utils;

import org.junit.AfterClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.File;
import java.util.concurrent.TimeoutException;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;
import static junit.framework.TestCase.assertNotNull;

/**
 * Test RecordingService.
 */

@LargeTest
@RunWith(AndroidJUnit4.class)
public class RecordingServiceTest {

    @Rule
    public final ServiceTestRule mServiceRule = new ServiceTestRule() {
        @Override
        protected void afterService() {
            super.afterService();

            RecordingService.onCreateCalls = 0;
            RecordingService.onStartCommandCalls = 0;
            RecordingService.onDestroyCalls = 0;
        }
    };

    /*
        Test that the Local Binder Pattern for this Service works correctly.
     */
    @Test
    public void testLocalBinder() throws TimeoutException {
        // Create the service Intent.
        Intent serviceIntent = RecordingService.makeIntent(InstrumentationRegistry.getTargetContext(), true);

        // Bind the service and grab a reference to the binder.
        IBinder binder = mServiceRule.bindService(serviceIntent);

        // Get the reference to the service, or you can call
        // public methods on the binder directly.
        RecordingService service = ((RecordingService.LocalBinder) binder).getService();

        // Verify that the service is working correctly.
        assertNotNull("Service reference is null", service);

        mServiceRule.unbindService();
    }

    /*
        Test that the Service's lifecycle methods are called the exact number of times in response
        to binding, unbinding and calls to startService.
     */
    @Test
    public void testLifecyleMethodCalls() throws TimeoutException {
        // Create the service Intent.
        Intent serviceIntent = RecordingService.makeIntent(InstrumentationRegistry.getTargetContext(), true);

        mServiceRule.startService(serviceIntent);
        IBinder binder = mServiceRule.bindService(serviceIntent);
        RecordingService service = ((RecordingService.LocalBinder) binder).getService();
        mServiceRule.startService(serviceIntent);
        mServiceRule.startService(serviceIntent);

        assertNotNull("Service reference is null", service);
        assertEquals("onCreate called multiple times", 1, RecordingService.onCreateCalls);
        assertEquals("onStartCommand not called 3 times as expected", 3, RecordingService.onStartCommandCalls);

        mServiceRule.unbindService();
        assertEquals("onDestroy not called after unbinding from Service", 1, RecordingService.onCreateCalls);
    }

    /*
        Test that the Service starts and stops recording when asked to.
     */
    @Test
    public void testStartAndStopRecording() throws TimeoutException {
        // Bind to Service.
        Intent serviceIntent = RecordingService.makeIntent(InstrumentationRegistry.getTargetContext(), true);
        IBinder binder = mServiceRule.bindService(serviceIntent);
        RecordingService service = ((RecordingService.LocalBinder) binder).getService();
        assertNotNull("Service reference is null", service);

        // Start recording.
        service.startRecording(0);
        assertTrue("Service is not recording, but it should", service.isRecording());
        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // Stop recording.
        service.stopRecording();
        assertFalse("Service is recording, but it should not", service.isRecording());

    }

    /*
        Test the interface used by the Service to communicate information to a connected
        Activity. Test that the interface communicates the right information when starting, stopping
        the Service and when the recording is ongoing.
     */
    @Test
    public void testOnRecordingStatusChangedListener() throws TimeoutException {
        // Bind to Service.
        Intent serviceIntent = RecordingService.makeIntent(InstrumentationRegistry.getTargetContext(), true);
        IBinder binder = mServiceRule.bindService(serviceIntent);
        RecordingService service = ((RecordingService.LocalBinder) binder).getService();
        assertNotNull("Service reference is null", service);

        // Create listener and bind it to the Service.
        MyOnRecordingStatusChangedListener listener = new MyOnRecordingStatusChangedListener();
        service.setOnRecordingStatusChangedListener(listener);
        service.startRecording(0);
        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        service.stopRecording();

        assertTrue("The start of the recording was not communicated to the listener",
                listener.isRecordingStarted());
        assertTrue("The stop of the recording was not communicated to the listener",
                listener.isRecordingStopped());
        assertTrue("The elapsed seconds of the recording was not communicated to the listener",
                listener.getElapsedSeconds() > 0);
        assertTrue("The file path of the recording was not communicated to the listener",
                listener.getFilePath() != null && listener.getFilePath().length() > 0);

        service.setOnRecordingStatusChangedListener(null);
    }

    /*
        Delete all the data added by the tests.
     */
    @AfterClass
    public static void clean() {
        Context context = InstrumentationRegistry.getTargetContext();
        // Clear database.
        DBHelper dbHelper = new DBHelper(context);
        dbHelper.restoreDatabase();

        // Delete all files created.
        String mFilePath = Utils.getDirectoryPath(context);

        File dir = new File(mFilePath);
        for (File file : dir.listFiles()) {
            file.delete();
        }
        dir.delete();
    }

    private class MyOnRecordingStatusChangedListener implements RecordingService.OnRecordingStatusChangedListener {
        private boolean recordingStarted;
        private boolean recordingStopped;
        private int elapsedSeconds;
        private String filePath;

        @Override
        public void onRecordingStarted() {
            recordingStarted = true;
        }

        @Override
        public void onTimerChanged(int seconds) {
            elapsedSeconds = seconds;
        }

        @Override
        public void onRecordingStopped(String filePath) {
            recordingStopped = true;
            this.filePath = filePath;
        }

        public boolean isRecordingStarted() {
            return recordingStarted;
        }

        public boolean isRecordingStopped() {
            return recordingStopped;
        }

        public int getElapsedSeconds() {
            return elapsedSeconds;
        }

        public String getFilePath() {
            return filePath;
        }
    }

}
