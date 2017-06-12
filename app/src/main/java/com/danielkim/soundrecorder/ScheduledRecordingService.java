package com.danielkim.soundrecorder;

import android.app.AlarmManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Message;

public class ScheduledRecordingService extends Service implements Handler.Callback {

    private final int SCHEDULE_RECORDINGS = 1;

    private AlarmManager alarmManager;
    private Handler mHandler;

    public ScheduledRecordingService() {

    }

    @Override
    public void onCreate() {
        super.onCreate();

        alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);

        // Start background thread with a Looper.
        HandlerThread handlerThread = new HandlerThread("BackgroundThread");
        handlerThread.start();
        mHandler = new Handler(handlerThread.getLooper(), this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        mHandler.getLooper().quit();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Message message = mHandler.obtainMessage(SCHEDULE_RECORDINGS);
        mHandler.sendMessage(message);

        return START_STICKY;
    }

    @Override
    public boolean handleMessage(Message message) {
        if (message.what == SCHEDULE_RECORDINGS) {
            scheduleRecordings();
        }

        return true;
    }

    // Get scheduled recordings from database and set the AlarmManager.
    private void scheduleRecordings() {

    }
}
