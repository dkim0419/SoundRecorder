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
        assertEquals("Table is not empty", 0, dbHelper.getScheduledRecordingsCount());

        rec1 = dbHelper.addScheduledRecording(0, 100);
        assertEquals("Records not incremented to 1 after 1st insertion", 1, dbHelper.getScheduledRecordingsCount());
        rec2 = dbHelper.addScheduledRecording(100, 500);
        assertEquals("Records not incremented to 2 after 2nd insertion", 2, dbHelper.getScheduledRecordingsCount());
        rec3 = dbHelper.addScheduledRecording(200, 600);
        assertEquals("Records not incremented to 3 after 3rd insertion", 3, dbHelper.getScheduledRecordingsCount());
    }

    @Test
    public void testGet() throws Exception {
        ScheduledRecordingItem item = dbHelper.getScheduledRecording(rec1);
        assertNotNull("1st item is null", item);
        assertEquals("Start of 1st item is not 0", 0, item.getmStart());
        assertEquals("Length of 1st item is not 100", 100, item.getmLength());

        item = dbHelper.getScheduledRecording(rec2);
        assertNotNull("2nd item is null", item);
        assertEquals("Start of 2nd item is not 100", 100, item.getmStart());
        assertEquals("Length of 2nd item is not 500", 500, item.getmLength());

        item = dbHelper.getScheduledRecording(rec3);
        assertNotNull("Item is null", item);
        assertEquals("Start of 2nd item is not 100", 100, item.getmStart());
        assertEquals("Length of 2nd item is not 500", 500, item.getmLength());
    }

}
