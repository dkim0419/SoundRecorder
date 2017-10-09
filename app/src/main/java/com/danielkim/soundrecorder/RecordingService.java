/*
 * Copyright (c) 2017. This code was written by iClaude. All rights reserved.
 */

package com.danielkim.soundrecorder;


import android.Manifest;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.MediaRecorder;
import android.os.Binder;
import android.os.Environment;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import com.danielkim.soundrecorder.activities.MainActivity;
import com.danielkim.soundrecorder.database.DBHelper;

import java.io.File;
import java.io.IOException;
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

public class RecordingService extends Service {
    private static final String TAG = "SCHEDULED_RECORDER_TAG";
    private static final String EXTRA_ACTIVITY_STARTER = "com.danielkim.soundrecorder.EXTRA_ACTIVITY_STARTER";
    private static final int ONGOING_NOTIFICATION = 1;

    private String mFileName = null;
    private String mFilePath = null;
    private MediaRecorder mRecorder = null;
    private DBHelper mDatabase;
    private long mStartingTimeMillis = 0;
    private int mElapsedSeconds = 0;

    private TimerTask mIncrementTimerTask = null;

    private final IBinder myBinder = new LocalBinder();
    private boolean isRecording = false;


    /*
        Static factory method used to create an Intent to start this Service. The boolean value
        activityStarter is true if this method is called by an Activity, false otherwise (i.e.
        Service started by an AlarmManager for a scheduled recording.
    */
    public static Intent makeIntent(Context context, boolean activityStarter) {
        Intent intent = new Intent(context, RecordingService.class);
        intent.putExtra(EXTRA_ACTIVITY_STARTER, activityStarter);
        return intent;
    }

    /*
        Other convenient method used to retrieve an empty Intent (i.e to stop this Service).
     */
    public static Intent makeIntent(Context context) {
        Intent intent = new Intent(context, RecordingService.class);
        return intent;
    }

    /*
        The following code implements a bound Service used to connect this Service to an Activity.
    */

    public class LocalBinder extends Binder {
        public RecordingService getService() {
            return RecordingService.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return myBinder;
    }

    /*
        Interface used to communicate to a connected Activity changes in the status of a
        recording:
        - recording started
        - recording stopped (with file path)
        - seconds elapsed
     */
    public interface OnRecordingStatusChangedListener {
        void onRecordingStarted();
        void onTimerChanged(int seconds);
        void onRecordingStopped(String filePath);
    }

    private OnRecordingStatusChangedListener onRecordingStatusChangedListener = null;

    public void setOnRecordingStatusChangedListener(OnRecordingStatusChangedListener onRecordingStatusChangedListener) {
        this.onRecordingStatusChangedListener = onRecordingStatusChangedListener;
    }

    /*
        The following code implements a started Service.
    */
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        boolean activityStarter = intent.getBooleanExtra(EXTRA_ACTIVITY_STARTER, false);
        Log.d(TAG, "RecordingService - onStartCommand - activity starter? " + activityStarter);
        int duration;
        if (!activityStarter) { // automatic scheduled recording
            // Get next recording data.
            ScheduledRecordingItem item = mDatabase.getNextScheduledRecording();
            duration = (int) (item.getEnd() - item.getStart());
            // Remove scheduled recording from database and schedule next recording.
            mDatabase.removeScheduledRecording(item.getId());
            startService(ScheduledRecordingService.makeIntent(this, false));

            if (!isRecording && hasPermissions()) {
                startRecording(duration);
            }
        }

        return START_NOT_STICKY;
    }

    /*
        The following code is shared by both started and bound Service.
     */

    @Override
    public void onCreate() {
        Log.d(TAG, "RecordingService - onCreate");
        super.onCreate();
        mDatabase = new DBHelper(getApplicationContext());
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "RecordingService - onDestroy");
        super.onDestroy();
        if (mRecorder != null) {
            stopRecording();
        }

        if (onRecordingStatusChangedListener != null) onRecordingStatusChangedListener = null;
    }

    public void startRecording(int duration) {
        Log.d(TAG, "RecordingService - startRecording");
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

        if (onRecordingStatusChangedListener != null) {
            onRecordingStatusChangedListener.onRecordingStarted();
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
                if (onRecordingStatusChangedListener != null) {
                    onRecordingStatusChangedListener.onTimerChanged(mElapsedSeconds);
                }
            }
        };
        mTimer.scheduleAtFixedRate(mIncrementTimerTask, 1000, 1000);
    }

    public void stopRecording() {
        Log.d(TAG, "RecordingService - stopRecording");
        mRecorder.stop();
        long mElapsedMillis = (System.currentTimeMillis() - mStartingTimeMillis);
        mRecorder.release();
        isRecording = false;
        mRecorder = null;

        // Communicate the file path to the connected Activity.
        if (onRecordingStatusChangedListener != null) {
            onRecordingStatusChangedListener.onRecordingStopped(mFilePath);
        }

        // Stop timer.
        if (mIncrementTimerTask != null) {
            mIncrementTimerTask.cancel();
            mIncrementTimerTask = null;
        }

        // Save the recording data in the database.
        try {
            mDatabase.addRecording(mFileName, mFilePath, mElapsedMillis);
        } catch (Exception e) {
            Log.e(TAG, "exception", e);
        }

        stopForeground(true);
    }

    // Specific to scheduled recordings.
    private void stopScheduledRecording() {
        Log.d(TAG, "RecordingService - stopScheduledRecording");
        // Stop recording as usual.
        stopRecording();

        // No Activity connected -> stop the Service.
        if (onRecordingStatusChangedListener == null)
            stopSelf();
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

    /*
        For Marshmallow+ check if we have the necessary permissions. This method is called for
        scheduled recordings because the use might deny the permissions after a scheduled
        recording has already been set.
     */
    private boolean hasPermissions() {
        boolean writePerm = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
        boolean audioPerm = ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED;
        return writePerm && audioPerm;
    }
}
