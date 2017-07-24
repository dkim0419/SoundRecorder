package com.danielkim.soundrecorder;

import android.content.BroadcastReceiver;
import android.content.Intent;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.ShadowApplication;

import java.util.List;


/**
 * Created by iClaude on 24/07/2017.
 */

@RunWith(RobolectricTestRunner.class)
@Config(constants = BuildConfig.class)
public class BootUpReceiverTest {

    /**
     * Let's first test if the BroadcastReceiver, which was defined in the manifest, is correctly
     * load in our tests
     */
    @Test
    public void testBroadcastReceiverRegistered() {
        List<ShadowApplication.Wrapper> registeredReceivers = ShadowApplication.getInstance().getRegisteredReceivers();
        Assert.assertFalse(registeredReceivers.isEmpty());

        boolean receiverFound = false;
        for (ShadowApplication.Wrapper wrapper : registeredReceivers) {
            if (!receiverFound)
                receiverFound = BootUpReceiver.class.getSimpleName().equals(
                        wrapper.broadcastReceiver.getClass().getSimpleName());
        }
        Assert.assertTrue(receiverFound); //will be false if not found
    }

    /**
     * We defined the Broadcast receiver with a certain action, so we should check if we have
     * receivers listening to the defined action
     */
    @Test
    public void testBroadcastReceiverAction() {
        Intent intent = new Intent("android.intent.action.BOOT_COMPLETED");
        ShadowApplication shadowApplication = ShadowApplication.getInstance();
        Assert.assertTrue(shadowApplication.hasReceiverForIntent(intent));
    }

    /**
     * Test onReceive method of the BroadcastReceiver: test that our receiver starts the
     * ScheduledRecordingService service.
     */
    @Test
    public void testBroadcastReceiverStartService() {
        ShadowApplication shadowApplication = ShadowApplication.getInstance();

        // First find the BootUpReceiver.
        Intent intent = new Intent("android.intent.action.BOOT_COMPLETED");
        List<BroadcastReceiver> registeredReceivers = shadowApplication.getReceiversForIntent(intent);
        BootUpReceiver bootUpReceiver = null;
        for (BroadcastReceiver receiver : registeredReceivers) {
            if (BootUpReceiver.class.getSimpleName().equals(
                    receiver.getClass().getSimpleName())) {
                bootUpReceiver = (BootUpReceiver) receiver;
            }
        }
        Assert.assertFalse(bootUpReceiver == null);

        // Test onReceive method of BootUpReceiver.
        bootUpReceiver.onReceive(shadowApplication.getApplicationContext(), intent);
        Intent serviceIntent = shadowApplication.peekNextStartedService();
        Assert.assertEquals("Expected the ScheduledRecordingService service to be invoked",
                ScheduledRecordingService.class.getCanonicalName(),
                serviceIntent.getComponent().getClassName());
    }

}
