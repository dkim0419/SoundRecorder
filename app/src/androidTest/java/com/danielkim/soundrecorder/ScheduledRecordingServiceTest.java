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

    @Rule
    public final ServiceTestRule mServiceRule = new ServiceTestRule();

    @Test
    public void testLifecycleMethods() throws TimeoutException {
        Intent intent = ScheduledRecordingService.makeIntent(InstrumentationRegistry.getTargetContext());
        // Call startService 3 times.
        mServiceRule.startService(intent);
        mServiceRule.startService(intent);
        mServiceRule.startService(intent);

        assertEquals("onCreate called multiple times", 1, ScheduledRecordingService.onCreateCalls);
        assertEquals("onStartCommand not called 3 times as expected", 3, ScheduledRecordingService.onStartCommandCalls);
    }


    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {

    }

    @Override
    public void onServiceDisconnected(ComponentName name) {

    }
}
