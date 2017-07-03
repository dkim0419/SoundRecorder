package com.danielkim.soundrecorder;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
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

    // Just for testing.
    public static int onCreateCalls, onDestroyCalls, onStartCommandCalls;
    private LocalBinder localBinder = new LocalBinder();

    /*
        Static factory method used to create an Intent to start this Service.
    */
    public static Intent makeIntent(Context context) {
        Intent intent = new Intent(context, ScheduledRecordingService.class);
        return intent;
    }

    public ScheduledRecordingService() {

    }

    @Override
    public void onCreate() {
        super.onCreate();
        onCreateCalls++; // just for testing

        alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);

        // Start background thread with a Looper.
        HandlerThread handlerThread = new HandlerThread("BackgroundThread");
        handlerThread.start();
        mHandler = new Handler(handlerThread.getLooper(), this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        onDestroyCalls++; // just for testing

        // Stop background thread.
        mHandler.getLooper().quit();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        onStartCommandCalls++; // just for testing

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

    /*
        Implementation of local binder pattern for testing purposes.
    */
    public class LocalBinder extends Binder {
        public ScheduledRecordingService getService() {
            return ScheduledRecordingService.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return localBinder;
    }
}
