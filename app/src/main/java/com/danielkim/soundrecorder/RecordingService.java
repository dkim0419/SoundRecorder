package com.danielkim.soundrecorder;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.MediaRecorder;
import android.os.Environment;
import android.os.FileObserver;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.widget.Toast;

import com.danielkim.soundrecorder.activities.MainActivity;
import com.danielkim.soundrecorder.adapters.FileViewerAdapter;
import com.danielkim.soundrecorder.fragments.FileViewerFragment;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by Daniel on 12/28/2014.
 */
public class RecordingService extends Service {

    private static final String LOG_TAG = "RecordingService";

    SimpleDateFormat formatter;
    Date now;
    private String mFileName = null;
    private String mFilePath = null;

    private MediaRecorder mRecorder = null;

    private DBHelper mDatabase;

    private int mElapsedSeconds = 0;
    private static final SimpleDateFormat mTimerFormat = new SimpleDateFormat("mm:ss", Locale.getDefault());

    private Timer mTimer = null;
    private TimerTask mIncrementTimerTask = null;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mDatabase = new DBHelper(getApplicationContext());
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        startRecording();
        startTimer();
        return START_STICKY;
    }

    private void startTimer() {
        mTimer = new Timer();
        mIncrementTimerTask = new TimerTask() {
            @Override
            public void run() {
                mElapsedSeconds++;
                // NotificationManager mgr = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                // mgr.notify(1, createNotification());
            }
        };

        mTimer.scheduleAtFixedRate(mIncrementTimerTask, 1000, 1000);
    }

    @Override
    public void onDestroy() {
        if (mRecorder != null) {
            stopRecording();
        }

        super.onDestroy();
    }

    public void startRecording() {
        formatter = new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss");
        now = new Date();

        mFileName = formatter.format(now) + ".mp4";
        mFilePath = Environment.getExternalStorageDirectory().getAbsolutePath();
        mFilePath += "/SoundRecorder/" + mFileName;

        mRecorder = new MediaRecorder();
        mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
        mRecorder.setOutputFile(mFilePath);
        mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);

        try {
            mRecorder.prepare();
            mRecorder.start();

        } catch (IOException e) {
            Log.e(LOG_TAG, "prepare() failed");
        }
    }

    public void stopRecording() {
        mRecorder.stop();
        mRecorder.release();
        Toast.makeText(this, R.string.toast_recording_finish + " " + mFilePath, Toast.LENGTH_LONG).show();
        mRecorder = null;

        try {
            mDatabase.addRecording(mFileName, mFilePath, mElapsedSeconds);
            FileViewerAdapter adapter = new FileViewerAdapter(getApplicationContext());
            RecyclerView view = new RecyclerView(getApplicationContext());
            view.swapAdapter(adapter, true);

            //add the new file to the top of the list (position 0)
            adapter.notifyItemInserted(0);

        } catch (Exception e){
            Log.e(LOG_TAG, "exception", e);
        }
    }

    private Notification createNotification() {
        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(getApplicationContext())
                        .setSmallIcon(R.drawable.ic_mic_white_36dp)
                        .setContentTitle(getString(R.string.notification_recording))
                        .setContentText(mTimerFormat.format(1000 * mElapsedSeconds))
                        .setOngoing(true);

        return mBuilder.build();
    }
}
