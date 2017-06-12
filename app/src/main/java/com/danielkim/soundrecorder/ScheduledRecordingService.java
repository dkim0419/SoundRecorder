package com.danielkim.soundrecorder;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Message;

import com.danielkim.soundrecorder.database.DBHelper;

import java.util.List;

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

        // Stop background thread.
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

        return START_REDELIVER_INTENT;
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
        DBHelper database = new DBHelper(this);
        List<ScheduledRecordingItem> list = database.getAllScheduledRecordings();
        for (ScheduledRecordingItem item : list) {
            Intent intent = RecordingService.makeIntent(this, item);
            PendingIntent pendingIntent = PendingIntent.getService(this, 0, intent, 0);
            if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.JELLY_BEAN_MR2) { // up to API 18

            } else if (Build.VERSION.SDK_INT > Build.VERSION_CODES.JELLY_BEAN_MR2 && Build.VERSION.SDK_INT <= Build.VERSION_CODES.LOLLIPOP_MR1) { // API 19-22

            } else { // API 23+

            }
        }

    }
}
