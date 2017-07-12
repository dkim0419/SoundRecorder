package com.danielkim.soundrecorder;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.support.test.InstrumentationRegistry;
import android.support.test.filters.LargeTest;
import android.support.test.rule.ServiceTestRule;
import android.support.test.runner.AndroidJUnit4;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.concurrent.TimeoutException;

import static junit.framework.Assert.assertEquals;

/**
 * Created by iClaude on 03/07/2017.
 */

@LargeTest
@RunWith(AndroidJUnit4.class)
public class ScheduledRecordingServiceTest implements ServiceConnection {

    private ScheduledRecordingService service;

    @Rule
    public final ServiceTestRule mServiceRule = new ServiceTestRule();

    @Test
    public void testLifecycleMethods() throws TimeoutException {
        Intent intent = ScheduledRecordingService.makeIntent(InstrumentationRegistry.getTargetContext(), false);
        // Call startService 3 times.
        mServiceRule.startService(intent);
        mServiceRule.startService(intent);
        mServiceRule.startService(intent);

        assertEquals("onCreate called multiple times", 1, ScheduledRecordingService.onCreateCalls);
        assertEquals("onStartCommand not called 3 times as expected", 3, ScheduledRecordingService.onStartCommandCalls);
    }

    @Test
    public void testWakeful() throws TimeoutException, InterruptedException {
        // Launch a non-wakeful Service.
        Intent intent = ScheduledRecordingService.makeIntent(InstrumentationRegistry.getTargetContext(), false);
        mServiceRule.startService(intent);
        assertEquals("Service should be not wakeful", false, ScheduledRecordingService.wakeful);

        // Launch a wakeful Service.
        intent = ScheduledRecordingService.makeIntent(InstrumentationRegistry.getTargetContext(), true);
        mServiceRule.startService(intent);
        assertEquals("Service should be wakeful", true, ScheduledRecordingService.wakeful);
    }

/*    @Test
    public void testAlarmManager() throws TimeoutException, InterruptedException {
        Context context = InstrumentationRegistry.getTargetContext();
        resetAlarmManager(context);

        // Test with empty database: the AlarmManager should be empty.
        DBHelper dbHelper = new DBHelper(context);
        dbHelper.restoreDatabase();
        Intent srsIntent = ScheduledRecordingService.makeIntent(context, false);
        mServiceRule.startService(srsIntent);

        // Check results.
        boolean alarmUp = PendingIntent.getService(context, 0,
                RecordingService.makeIntent(context, null),
                PendingIntent.FLAG_NO_CREATE) != null;
        assertEquals("AlarmManager should be empty", false, alarmUp);


        // Add some scheduled recordings to the database and test the AlarmManager.
        dbHelper.restoreDatabase();
        dbHelper.addScheduledRecording(0, 100);
        dbHelper.addScheduledRecording(100, 500);
        dbHelper.addScheduledRecording(200, 600);

        // Start the Service and set the AlarmManager with the scheduled recordings.
        mServiceRule.startService(srsIntent);

        // Check results.
        alarmUp = PendingIntent.getService(context, 0,
                RecordingService.makeIntent(context, null),
                PendingIntent.FLAG_NO_CREATE) != null;
        assertEquals("AlarmManager is empty", true, alarmUp);
    }

    // Cancels all pending alarms already set in the AlarmManager.
    private void resetAlarmManager(Context context) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = RecordingService.makeIntent(context, null);
        PendingIntent pendingIntent = PendingIntent.getService(context, 0, intent, 0);
        alarmManager.cancel(pendingIntent);
    }*/

    @Override
    public void onServiceConnected(ComponentName name, IBinder iBinder) {
        service = ((ScheduledRecordingService.LocalBinder) iBinder).getService();
    }

    @Override
    public void onServiceDisconnected(ComponentName name) {
        service = null;
    }
}
