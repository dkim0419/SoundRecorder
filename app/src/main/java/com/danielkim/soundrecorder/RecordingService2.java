/*
 * Copyright (c) 2017. This code was written by iClaude. All rights reserved.
 */

package com.danielkim.soundrecorder;


import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.MediaRecorder;
import android.os.Binder;
import android.os.Environment;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

import com.danielkim.soundrecorder.activities.MainActivity;
import com.danielkim.soundrecorder.database.DBHelper;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

import static android.media.MediaRecorder.MEDIA_RECORDER_INFO_MAX_DURATION_REACHED;

/**
 * Created by iClaude on 25/09/2017.
 * Service used to record audio. This class implements an hybrid Service (bound and started
 * Service).
 * Compared with the original Service, this class adds 2 new features:
 * 1) record scheduled recordings
 * 2) bound Service features to connect this Service to an Activity
 */

public class RecordingService2 extends Service {
    private static final String TAG = "SCHEDULED_RECORDER_TAG";
    private static final String EXTRA_ITEM = "com.danielkim.soundrecorder.ITEM";
    private static final int ONGOING_NOTIFICATION = 1;

    private boolean isManualRecording = true;
    private String mFileName = null;
    private String mFilePath = null;
    private MediaRecorder mRecorder = null;
    private DBHelper mDatabase;
    private long mStartingTimeMillis = 0;
    private int mElapsedSeconds = 0;

    private static final SimpleDateFormat mTimerFormat = new SimpleDateFormat("mm:ss", Locale.getDefault());
    private TimerTask mIncrementTimerTask = null;

    private final IBinder myBinder = new LocalBinder();
    private ScheduledRecordingItem scheduledRecordingItem = null;
    private boolean isRecording = false;


    /*
        Static factory method used to create an Intent to start this Service for a normal
        recording.
    */
    public static Intent makeIntent(Context context) {
        return new Intent(context, RecordingService2.class);
    }

    /*
        Static factory method used to create an Intent to start this Service for
        a scheduled recording.
    */
    public static Intent makeIntent(Context context, ScheduledRecordingItem item) {
        Intent intent = new Intent(context, RecordingService2.class);
        intent.putExtra(EXTRA_ITEM, item);
        return intent;
    }

    /*
        The following code implements a bound Service used to connect this Service to an Activity.
    */

    public class LocalBinder extends Binder {
        public RecordingService2 getService() {
            return RecordingService2.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return myBinder;
    }

    /*
        Interface used to communicate the seconds of the current recording to the connected Activity.
     */
    public interface OnTimerChangedListener {
        void onTimerChanged(int seconds);
    }

    private OnTimerChangedListener onTimerChangedListener = null;

    public void setOnTimerChangedListener(OnTimerChangedListener onTimerChangedListener) {
        this.onTimerChangedListener = onTimerChangedListener;
    }

    /*
        Interface used to communicate the start/stop of a scheduled recording to a connected
        Activity, so that the UI can be updated accordingly.
     */
    public interface OnScheduledRecordingListener {
        void onScheduledRecordingStart();
        void onScheduledRecordingStop();
    }

    private OnScheduledRecordingListener onScheduledRecordingListener = null;

    public void setOnScheduledRecordingListener(OnScheduledRecordingListener listener) {
        this.onScheduledRecordingListener = listener;
    }

    /*
        The following code implements a started Service.
    */
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "Service - onStartCommand");
        scheduledRecordingItem = intent.getParcelableExtra(EXTRA_ITEM);

        int duration = 0;
        if (scheduledRecordingItem != null) { // automatic scheduled recording
            isManualRecording = false;
            duration = (int) (scheduledRecordingItem.getEnd() - scheduledRecordingItem.getStart());
            // Schedule next recording.
            startService(ScheduledRecordingService.makeIntent(this, false));

            if (onScheduledRecordingListener != null) { // if an Activity is connected, inform it that a scheduled recording has started
                onScheduledRecordingListener.onScheduledRecordingStart();
            }
            startRecording(duration);
        }

        return START_NOT_STICKY;
    }

    /*
        The following code is shared by both started and bound Service.
     */

    @Override
    public void onCreate() {
        Log.d(TAG, "Service - onCreate");
        super.onCreate();
        mDatabase = new DBHelper(getApplicationContext());
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "Service - onDestroy");
        super.onDestroy();
        if (mRecorder != null) {
            stopRecording();
        }

        if (onTimerChangedListener != null) onTimerChangedListener = null;
    }

    public void startRecording(int duration) {
        Log.d(TAG, "Service - startRecording");
        startForeground(ONGOING_NOTIFICATION, createNotification());

        setFileNameAndPath();
        mRecorder = new MediaRecorder();
        mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
        mRecorder.setOutputFile(mFilePath);
        mRecorder.setMaxDuration(duration); // if this is a scheduled recording, set the max duration, after which the Service is stopped
        mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
        mRecorder.setAudioChannels(1);
        mRecorder.setOnInfoListener(new MediaRecorder.OnInfoListener() {
            @Override
            // Called only if a max duration has been set (scheduled recordings).
            public void onInfo(MediaRecorder mediaRecorder, int what, int extra) {
                if (what == MEDIA_RECORDER_INFO_MAX_DURATION_REACHED) {
                    stopScheduledRecording();
                }
            }
        });

        try {
            mRecorder.prepare();
            mRecorder.start();
            mStartingTimeMillis = System.currentTimeMillis();
            isRecording = true;

            startTimer();
        } catch (IOException e) {
            Log.e(TAG, "prepare() failed");
        }
    }

    private void setFileNameAndPath() {
        int count = 0;
        File f;

        do {
            count++;

            mFileName = getString(R.string.default_file_name)
                    + " #" + (mDatabase.getSavedRecordingsCount() + count) + ".mp4";
            mFilePath = Environment.getExternalStorageDirectory().getAbsolutePath();
            mFilePath += "/SoundRecorder/" + mFileName;

            f = new File(mFilePath);
        } while (f.exists() && !f.isDirectory());
    }

    private void startTimer() {
        Timer mTimer = new Timer();
        mElapsedSeconds = 0;
        mIncrementTimerTask = new TimerTask() {
            @Override
            public void run() {
                mElapsedSeconds++;
                if (onTimerChangedListener != null) {
                    onTimerChangedListener.onTimerChanged(mElapsedSeconds);
                }
            }
        };
        mTimer.scheduleAtFixedRate(mIncrementTimerTask, 1000, 1000);
    }

    public void stopRecording() {
        Log.d(TAG, "Service - stopRecording");
        mRecorder.stop();
        long mElapsedMillis = (System.currentTimeMillis() - mStartingTimeMillis);
        mRecorder.release();
        isRecording = false;
        Toast.makeText(this, getString(R.string.toast_recording_finish) + " " + mFilePath, Toast.LENGTH_LONG).show();

        // Stop timer.
        if (mIncrementTimerTask != null) {
            mIncrementTimerTask.cancel();
            mIncrementTimerTask = null;
        }

        mRecorder = null;
        try {
            mDatabase.addRecording(mFileName, mFilePath, mElapsedMillis);
        } catch (Exception e) {
            Log.e(TAG, "exception", e);
        }

        stopForeground(true);
    }

    // Specific to scheduled recordings.
    private void stopScheduledRecording() {
        // Remove scheduled recording from database.
        mDatabase.removeScheduledRecording(scheduledRecordingItem.getId());

        // Save recording file to database.
        stopRecording();
        if (onScheduledRecordingListener != null)  // if an Activity is connected, inform it that the scheduled recording has stopped
            onScheduledRecordingListener.onScheduledRecordingStop();
        else
            stopSelf(); // stop the Service if no Activity is connected
    }

    private Notification createNotification() {
        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(getApplicationContext())
                        .setSmallIcon(R.drawable.ic_mic_white_36dp)
                        .setContentTitle(getString(R.string.notification_recording))
                        .setContentText(getString(R.string.notification_recording_text))
                        .setOngoing(true);

        mBuilder.setContentIntent(PendingIntent.getActivities(getApplicationContext(), 0,
                new Intent[]{new Intent(getApplicationContext(), MainActivity.class)}, 0));

        return mBuilder.build();
    }

    public boolean isRecording() {
        return isRecording;
    }
}
