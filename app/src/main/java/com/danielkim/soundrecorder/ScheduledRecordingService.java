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
import android.support.annotation.VisibleForTesting;

import com.danielkim.soundrecorder.database.DBHelper;

import java.util.List;

/**
 * This Service gets triggered at boot time and sets the next scheduled recording using an
 * AlarmManager. Scheduled recordings are retrieved from the database and loaded in a separate
 * thread.
 * This class (started Service) also implements the Local Binder pattern just for testing purposes.
 */
public class ScheduledRecordingService extends Service implements Handler.Callback {

    private final int SCHEDULE_RECORDINGS = 1;
    protected static final String EXTRA_WAKEFUL = "com.danielkim.soundrecorder.WAKEFUL";

    protected AlarmManager alarmManager;
    protected Context context;
    private Handler mHandler;
    protected Intent startIntent;

    // Just for testing.
    @VisibleForTesting(otherwise = VisibleForTesting.NONE)
    public static int onCreateCalls, onDestroyCalls, onStartCommandCalls;
    @VisibleForTesting(otherwise = VisibleForTesting.NONE)
    public static boolean wakeful;
    @VisibleForTesting(otherwise = VisibleForTesting.NONE)
    private LocalBinder localBinder = new LocalBinder();

    /*
        Static factory method used to create an Intent to start this Service.
    */
    public static Intent makeIntent(Context context, boolean wakeful) {
        Intent intent = new Intent(context, ScheduledRecordingService.class);
        intent.putExtra(EXTRA_WAKEFUL, wakeful);
        return intent;
    }

    public ScheduledRecordingService() {
    }

    @Override
    public void onCreate() {
        super.onCreate();
        onCreateCalls++; // just for testing

        if (alarmManager == null)
            alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);

        if (context == null) context = this;

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

        // Is this a wakeful Service? In this case we have to release the wake-lock at the end.
        wakeful = intent.getBooleanExtra(EXTRA_WAKEFUL, false);
        startIntent = intent;

        Message message = mHandler.obtainMessage(SCHEDULE_RECORDINGS);
        mHandler.sendMessage(message);

        return START_REDELIVER_INTENT;
    }

    @Override
    public boolean handleMessage(Message message) {
        if (message.what == SCHEDULE_RECORDINGS) {
            resetAlarmManager(); // cancel all pending alarms
            scheduleNextRecording();
            if (wakeful) {
                BootUpReceiver.completeWakefulIntent(startIntent);
            }
        }

        return true;
    }

    // Cancels all pending alarms already set in the AlarmManager.
    protected void resetAlarmManager() {
        Intent intent = RecordingService.makeIntent(context, null);
        PendingIntent pendingIntent = PendingIntent.getService(context, 0, intent, 0);
        alarmManager.cancel(pendingIntent);
    }

    // Get scheduled recordings from database and set the AlarmManager.
    protected void scheduleNextRecording() {
        DBHelper database = new DBHelper(context);
        List<ScheduledRecordingItem> list = database.getAllScheduledRecordings();
        int i = 0;
        for (ScheduledRecordingItem item : list) {
            Intent intent = RecordingService.makeIntent(context, item);
            PendingIntent pendingIntent = PendingIntent.getService(context, i++, intent, 0);
            if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.JELLY_BEAN_MR2) { // up to API 18
                alarmManager.set(AlarmManager.RTC_WAKEUP, item.getStart(), pendingIntent);
            } else if (Build.VERSION.SDK_INT > Build.VERSION_CODES.JELLY_BEAN_MR2 && Build.VERSION.SDK_INT <= Build.VERSION_CODES.LOLLIPOP_MR1) { // API 19-22
                alarmManager.setExact(AlarmManager.RTC_WAKEUP, item.getStart(), pendingIntent);
            } else if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP_MR1) { // API 23+
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, item.getStart(), pendingIntent);
            }
        }
    }

    /*
        Implementation of local binder pattern for testing purposes.
    */
    @VisibleForTesting(otherwise = VisibleForTesting.NONE)
    public class LocalBinder extends Binder {
        public ScheduledRecordingService getService() {
            return ScheduledRecordingService.this;
        }
    }

    @VisibleForTesting(otherwise = VisibleForTesting.NONE)
    @Override
    public IBinder onBind(Intent intent) {
        return localBinder;
    }

}
