package com.danielkim.soundrecorder;

import android.support.test.InstrumentationRegistry;
import android.support.test.filters.LargeTest;
import android.support.test.runner.AndroidJUnit4;

import com.danielkim.soundrecorder.database.DBHelper;
import com.danielkim.soundrecorder.listeners.ScheduledRecordingItem;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;

/**
 * Tests for "scheduled_recordings" table in the database.
 * Tests methods of DBHelper.java class.
 */
@RunWith(AndroidJUnit4.class)
@LargeTest
public class DBScheduledRecordingsTest {

    private DBHelper dbHelper;

    @Before
    public void setUp() {
        dbHelper = new DBHelper(InstrumentationRegistry.getTargetContext());
    }

    @After
    public void finish() {
        dbHelper.close();
    }

    @Test
    public void testDBHelperNotNull() throws Exception {
        assertNotNull("DBHelper class is null", dbHelper);
    }

    @Test
    public void testAdd() throws Exception {
        dbHelper.restoreDatabase();
        assertEquals("Table is not empty", 0, dbHelper.getScheduledRecordingsCount());

        dbHelper.addScheduledRecording(0, 100);
        assertEquals("Records not incremented to 1 after 1st insertion", 1, dbHelper.getScheduledRecordingsCount());
        dbHelper.addScheduledRecording(100, 500);
        assertEquals("Records not incremented to 2 after 2nd insertion", 2, dbHelper.getScheduledRecordingsCount());
        dbHelper.addScheduledRecording(200, 600);
        assertEquals("Records not incremented to 3 after 3rd insertion", 3, dbHelper.getScheduledRecordingsCount());
    }

    @Test
    public void testGet() throws Exception {
        // First add 3 records.
        dbHelper.restoreDatabase();
        long rec1 = dbHelper.addScheduledRecording(0, 100);
        long rec2 = dbHelper.addScheduledRecording(100, 500);
        long rec3 = dbHelper.addScheduledRecording(200, 600);

        ScheduledRecordingItem item = dbHelper.getScheduledRecording(rec1);
        assertNotNull("1st item is null", item);
        assertEquals("Start of 1st item is not 0", 0, item.getStart());
        assertEquals("Length of 1st item is not 100", 100, item.getLength());

        item = dbHelper.getScheduledRecording(rec2);
        assertNotNull("2nd item is null", item);
        assertEquals("Start of 2nd item is not 100", 100, item.getStart());
        assertEquals("Length of 2nd item is not 500", 500, item.getLength());

        item = dbHelper.getScheduledRecording(rec3);
        assertNotNull("Item is null", item);
        assertEquals("Start of 3rd item is not 200", 200, item.getStart());
        assertEquals("Length of 3rd item is not 600", 600, item.getLength());
    }

    @Test
    public void testUpdate() throws Exception {
        // First add 3 records.
        dbHelper.restoreDatabase();
        long rec1 = dbHelper.addScheduledRecording(0, 100);
        long rec2 = dbHelper.addScheduledRecording(100, 500);
        long rec3 = dbHelper.addScheduledRecording(200, 600);

        dbHelper.updateScheduledRecording(rec2, 455, 315);
        ScheduledRecordingItem item = dbHelper.getScheduledRecording(rec2);
        assertNotNull("Updated item is null", item);
        assertEquals("Start of updated item is not 455", 455, item.getStart());
        assertEquals("Length of updated item is not 315", 315, item.getLength());
    }

}
