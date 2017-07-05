/*
 * Copyright (c) 2017. This code was written by iClaude. All rights reserved.
 */

package com.danielkim.soundrecorder;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * When the device is rebooted alarms set with the AlarmManager are cancelled.
 * So we need to use a BroadcastReceiver that gets triggered at bootup in order to start
 * the ScheduledRecordingService and reset all the scheduled recordings present in the database.
 */
public class BootUpReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        context.startService(ScheduledRecordingService.makeIntent(context));
    }
}
