package com.danielkim.soundrecorder;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.MediaRecorder;
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
 * Created by Daniel on 12/28/2014.
 * Edited by iClaude.
 */
public class RecordingService extends Service {

    private static final String LOG_TAG = "RecordingService";
    private static final String EXTRA_ITEM = "com.danielkim.soundrecorder.ITEM";

    private String mFileName = null;
    private String mFilePath = null;

    private MediaRecorder mRecorder = null;

    private DBHelper mDatabase;

    private long mStartingTimeMillis = 0;
    private long mElapsedMillis = 0;
    private int mElapsedSeconds = 0;
    private OnTimerChangedListener onTimerChangedListener = null;
    private static final SimpleDateFormat mTimerFormat = new SimpleDateFormat("mm:ss", Locale.getDefault());

    private Timer mTimer = null;
    private TimerTask mIncrementTimerTask = null;

    private ScheduledRecordingItem item = null;


    /*
        Static factory method used to create an Intent to start this Service for
        a scheduled recording.
     */
    public static Intent makeIntent(Context context, ScheduledRecordingItem item) {
        Intent intent = new Intent(context, RecordingService.class);
        intent.putExtra(EXTRA_ITEM, item);
        return intent;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    public interface OnTimerChangedListener {
        void onTimerChanged(int seconds);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mDatabase = new DBHelper(getApplicationContext());
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        item = intent.getParcelableExtra(EXTRA_ITEM);
        int duration = item == null ? 0 : (int) (item.getEnd() - item.getStart()); // is this a scheduled recording?
        startRecording(duration);

        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        if (mRecorder != null) {
            stopRecording();
        }

        super.onDestroy();
    }

    public void startRecording(int duration) {

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

            //startTimer();
            startForeground(1, createNotification());

        } catch (IOException e) {
            Log.e(LOG_TAG, "prepare() failed");
        }
    }

    public void setFileNameAndPath(){
        int count = 0;
        File f;

        do{
            count++;

            mFileName = getString(R.string.default_file_name)
                    + " #" + (mDatabase.getSavedRecordingsCount() + count) + ".mp4";
            mFilePath = Environment.getExternalStorageDirectory().getAbsolutePath();
            mFilePath += "/SoundRecorder/" + mFileName;

            f = new File(mFilePath);
        }while (f.exists() && !f.isDirectory());
    }

    public void stopRecording() {
        mRecorder.stop();
        mElapsedMillis = (System.currentTimeMillis() - mStartingTimeMillis);
        mRecorder.release();
        Toast.makeText(this, getString(R.string.toast_recording_finish) + " " + mFilePath, Toast.LENGTH_LONG).show();

        //remove notification
        if (mIncrementTimerTask != null) {
            mIncrementTimerTask.cancel();
            mIncrementTimerTask = null;
        }

        mRecorder = null;

        try {
            mDatabase.addRecording(mFileName, mFilePath, mElapsedMillis);

        } catch (Exception e){
            Log.e(LOG_TAG, "exception", e);
        }

        stopForeground(true);
        stopSelf();
    }

    // Specific to scheduled recordings.
    private void stopScheduledRecording() {
        // Remove scheduled recording from database.
        DBHelper dbHelper = new DBHelper(this);
        dbHelper.removeScheduledRecording(item.getId());

        // Schedule next recording.
        startService(ScheduledRecordingService.makeIntent(this, false));

        // Save recording file to database.
        stopRecording();
    }

    private void startTimer() {
        mTimer = new Timer();
        mIncrementTimerTask = new TimerTask() {
            @Override
            public void run() {
                mElapsedSeconds++;
                if (onTimerChangedListener != null)
                    onTimerChangedListener.onTimerChanged(mElapsedSeconds);
                NotificationManager mgr = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                mgr.notify(1, createNotification());
            }
        };
        mTimer.scheduleAtFixedRate(mIncrementTimerTask, 1000, 1000);
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
}
