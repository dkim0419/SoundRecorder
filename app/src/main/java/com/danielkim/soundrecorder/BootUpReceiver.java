/*
 * Copyright (c) 2017 Claudio "iClaude" Agostini <agostini.claudio1@gmail.com>
 * Licensed under the Apache License, Version 2.0
 */

package com.danielkim.soundrecorder;

import android.content.Context;
import android.content.Intent;
import android.support.v4.content.WakefulBroadcastReceiver;

/**
 * When the device is rebooted alarms set with the AlarmManager are cancelled.
 * So we need to use a BroadcastReceiver that gets triggered at bootup in order to start
 * the ScheduledRecordingService and set the next alarm.
 */
public class BootUpReceiver extends WakefulBroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction() != null && intent.getAction().equals("android.intent.action.BOOT_COMPLETED"))
            startWakefulService(context, ScheduledRecordingService.makeIntent(context, true));
    }
}
