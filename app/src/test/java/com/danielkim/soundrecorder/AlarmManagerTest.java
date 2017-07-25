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
    private ShadowAlarmManager shadowAlarmManager;

    @Before
    public void setUp() throws Exception {
        context = RuntimeEnvironment.application.getApplicationContext();
        AlarmManager alarmManager = (AlarmManager) RuntimeEnvironment.application.getSystemService(Context.ALARM_SERVICE);
        shadowAlarmManager = shadowOf(alarmManager);
    }

    // Test with empty database: the AlarmManager should be empty.
    @Test
    public void testEmptyDatabase() throws Exception {
        DBHelper dbHelper = new DBHelper(context);
        dbHelper.restoreDatabase();
        Intent srsIntent = ScheduledRecordingService.makeIntent(context, false);
        context.startService(srsIntent);

        assertNull(shadowAlarmManager.getNextScheduledAlarm());
    }

    @Test
    public void test3Alarms() throws Exception {
        // Insert 3 records in the database.
        DBHelper dbHelper = new DBHelper(context);
        dbHelper.restoreDatabase();
        dbHelper.addScheduledRecording(0, 100);
        dbHelper.addScheduledRecording(100, 500);
        dbHelper.addScheduledRecording(200, 600);

        // Start the service to schedule the alarms.
        Intent srsIntent = ScheduledRecordingService.makeIntent(context, false);
        context.startService(srsIntent);

        // Checks.
        assertEquals(3, shadowAlarmManager.getScheduledAlarms().size());
    }
}
