package com.danielkim.soundrecorder;

import android.support.test.InstrumentationRegistry;
import android.support.test.filters.LargeTest;
import android.support.test.runner.AndroidJUnit4;

import com.danielkim.soundrecorder.database.DBHelper;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.List;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertNull;

/**
 * Tests for "scheduled_recordings" table in the database.
 * Tests methods of DBHelper.java class.
 */
@RunWith(AndroidJUnit4.class)
@LargeTest
public class DBScheduledRecordingsTest {

    private DBHelper dbHelper;
    private long rec1, rec2, rec3;

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
        addRecords();

        // Get existing records.
        ScheduledRecordingItem item = dbHelper.getScheduledRecording(rec1);
        assertNotNull("1st item is null", item);
        assertEquals("Start of 1st item is not 0", 0, item.getStart());
        assertEquals("End of 1st item is not 100", 100, item.getEnd());

        item = dbHelper.getScheduledRecording(rec2);
        assertNotNull("2nd item is null", item);
        assertEquals("Start of 2nd item is not 100", 100, item.getStart());
        assertEquals("End of 2nd item is not 500", 500, item.getEnd());

        item = dbHelper.getScheduledRecording(rec3);
        assertNotNull("Item is null", item);
        assertEquals("Start of 3rd item is not 200", 200, item.getStart());
        assertEquals("End of 3rd item is not 600", 600, item.getEnd());

        // Get non-existent record.
        item = dbHelper.getScheduledRecording(-7);
        assertNull("Non-existent record returned", item);
    }

    @Test
    public void testUpdate() throws Exception {
        // First add 3 records.
        addRecords();

        // Update 1 record.
        int updated = 0;
        updated = dbHelper.updateScheduledRecording(rec2, 455, 315);
        assertEquals("0 records updated", 1, updated);
        ScheduledRecordingItem item = dbHelper.getScheduledRecording(rec2);
        assertNotNull("Updated item is null", item);
        assertEquals("Start of updated item is not 455", 455, item.getStart());
        assertEquals("End of updated item is not 315", 315, item.getEnd());

        // Update non-existent record.
        updated = dbHelper.updateScheduledRecording(-7, 455, 315);
        assertEquals("Non-existent record updated", 0, updated);
    }

    @Test
    public void testDelete() throws Exception {
        addRecords();

        int deleted = 0;
        // Delete record 1.
        deleted = dbHelper.removeScheduledRecording(rec1);
        assertEquals("0 records deleted", 1, deleted);
        ScheduledRecordingItem item = dbHelper.getScheduledRecording(rec1);
        assertNull("Record 1 not deleted", item);
        int count = dbHelper.getScheduledRecordingsCount();
        assertEquals("Records are not 2 after deleting 1 item", 2, count);

        // Delete record 2.
        deleted = dbHelper.removeScheduledRecording(rec2);
        assertEquals("0 records deleted", 1, deleted);
        item = dbHelper.getScheduledRecording(rec2);
        assertNull("Record 2 not deleted", item);
        count = dbHelper.getScheduledRecordingsCount();
        assertEquals("Records are not 1 after deleting 2 items", 1, count);

        // Delete record 3.
        deleted = dbHelper.removeScheduledRecording(rec3);
        assertEquals("0 records deleted", 1, deleted);
        item = dbHelper.getScheduledRecording(rec3);
        assertNull("Record 3 not deleted", item);
        count = dbHelper.getScheduledRecordingsCount();
        assertEquals("Records are not 0 after deleting 3 items", 0, count);

        // Delete non-existent record.
        deleted = dbHelper.removeScheduledRecording(-7);
        assertEquals("Non-existent record deleted", 0, deleted);
    }

    @Test
    public void testAlreadyScheduled() throws Exception {
        addRecords();

        // Recordings already scheduled.
        boolean scheduled = dbHelper.alreadyScheduled(50);
        assertEquals("A recording is already scheduled for time 50", true, scheduled);
        scheduled = dbHelper.alreadyScheduled(100);
        assertEquals("A recording is already scheduled for time 100", true, scheduled);
        scheduled = dbHelper.alreadyScheduled(550);
        assertEquals("A recording is already scheduled for time 550", true, scheduled);

        // No recordings scheduled.
        scheduled = dbHelper.alreadyScheduled(700);
        assertEquals("No recording is scheduled for time 700", false, scheduled);
    }

    @Test
    public void testGetScheduledRecordingsBetween() throws Exception {
        addRecords();

        List<ScheduledRecordingItem> list;
        // Should return empty list.
        list = dbHelper.getScheduledRecordingsBetween(700, 900);
        assertEquals("List should be empty", true, list.isEmpty());

        // Should return 2 items.
        list = dbHelper.getScheduledRecordingsBetween(0, 150);
        assertEquals("List should contain 2 items", 2, list.size());
        // Get the 2 items and check their properties.
        ScheduledRecordingItem item = list.get(0);
        assertEquals("1st item's start should be 0", 0, item.getStart());
        assertEquals("1st item's end should be 0", 100, item.getEnd());
        item = list.get(1);
        assertEquals("2nd item's start should be 100", 100, item.getStart());
        assertEquals("2nd item's end should be 500", 500, item.getEnd());
    }

    // Add 3 records to the database.
    private void addRecords() {
        dbHelper.restoreDatabase();
        rec1 = dbHelper.addScheduledRecording(0, 100);
        rec2 = dbHelper.addScheduledRecording(100, 500);
        rec3 = dbHelper.addScheduledRecording(200, 600);
    }
}
