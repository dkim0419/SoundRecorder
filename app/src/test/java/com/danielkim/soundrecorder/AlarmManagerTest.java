package com.danielkim.soundrecorder;

import android.app.AlarmManager;
import android.content.Context;
import android.content.Intent;

import com.danielkim.soundrecorder.database.DBHelper;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.ShadowAlarmManager;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNull;
import static org.robolectric.Shadows.shadowOf;

/**
 * Created by iClaude on 24/07/2017.
 */

@RunWith(RobolectricTestRunner.class)
@Config(constants = BuildConfig.class)
public class AlarmManagerTest {

    private Context context;
    private AlarmManager alarmManager;
    private ShadowAlarmManager shadowAlarmManager;

    @Before
    public void setUp() throws Exception {
        context = RuntimeEnvironment.application.getApplicationContext();
        alarmManager = (AlarmManager) RuntimeEnvironment.application.getSystemService(Context.ALARM_SERVICE);
        shadowAlarmManager = shadowOf(alarmManager);
    }

    // Test with empty database: the AlarmManager should be empty.
    @Test
    public void testEmptyDatabase() throws Exception {
        // Clear the database.
        DBHelper dbHelper = new DBHelper(context);
        dbHelper.restoreDatabase();

        // Start the service.
        MockScheduledRecordingService mockService = new MockScheduledRecordingService(context, alarmManager);
        mockService.onCreate();
        Intent intent = ScheduledRecordingService.makeIntent(context, false);
        mockService.onStartCommand(intent, 0, 0);
        mockService.onDestroy();

        assertNull(shadowAlarmManager.getNextScheduledAlarm());
    }

    @Test
    public void test3Alarms() throws Exception {
        // Insert 3 records in the database.
        DBHelper dbHelper = new DBHelper(context);
        dbHelper.restoreDatabase();
        dbHelper.addScheduledRecording(0, 100);
        dbHelper.addScheduledRecording(200, 300);
        dbHelper.addScheduledRecording(500, 600);

        // Test the service to schedule the alarms.
        MockScheduledRecordingService mockService = new MockScheduledRecordingService(context, alarmManager);
        mockService.onCreate();
        Intent intent = ScheduledRecordingService.makeIntent(context, false);
        mockService.onStartCommand(intent, 0, 0);
        mockService.onStartCommand(intent, 0, 0);
        mockService.onStartCommand(intent, 0, 0);
        mockService.onDestroy();

        // Checks.
        assertEquals(1, shadowAlarmManager.getScheduledAlarms().size());

        // Test the type and times of the alarms.
        ShadowAlarmManager.ScheduledAlarm scheduledAlarm = shadowAlarmManager.getNextScheduledAlarm();
        assertEquals(AlarmManager.RTC_WAKEUP, scheduledAlarm.type);
        assertEquals(0, scheduledAlarm.triggerAtTime);

        // Test with other settings.
        dbHelper.restoreDatabase();
        dbHelper.addScheduledRecording(500, 600);
        intent = ScheduledRecordingService.makeIntent(context, false);
        mockService.onStartCommand(intent, 0, 0);
        mockService.onStartCommand(intent, 0, 0);
        mockService.onStartCommand(intent, 0, 0);
        mockService.onDestroy();

        // Checks.
        assertEquals(1, shadowAlarmManager.getScheduledAlarms().size());

        // Test the type and times of the alarms.
        scheduledAlarm = shadowAlarmManager.getNextScheduledAlarm();
        assertEquals(AlarmManager.RTC_WAKEUP, scheduledAlarm.type);
        assertEquals(500, scheduledAlarm.triggerAtTime);
    }
}
