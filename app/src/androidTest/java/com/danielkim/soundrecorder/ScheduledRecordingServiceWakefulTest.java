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
 * Checks that the ScheduledRecordingService called as wakeful is treated as such.
 * Created by iClaude on 03/07/2017.
 */

@LargeTest
@RunWith(AndroidJUnit4.class)
public class ScheduledRecordingServiceWakefulTest implements ServiceConnection {

    private ScheduledRecordingService service;

    @Rule
    public final ServiceTestRule mServiceRule = new ServiceTestRule();

    @Test
    public void testWakeful() throws TimeoutException, InterruptedException {
        // Launch a non-wakeful Service.
        Intent intent = ScheduledRecordingService.makeIntent(InstrumentationRegistry.getTargetContext(), false);
        mServiceRule.startService(intent);
        assertEquals("Service should be not wakeful", false, ScheduledRecordingService.wakeful);
    }

    @Override
    public void onServiceConnected(ComponentName name, IBinder iBinder) {
        service = ((ScheduledRecordingService.LocalBinder) iBinder).getService();
    }

    @Override
    public void onServiceDisconnected(ComponentName name) {
        service = null;
    }
}

