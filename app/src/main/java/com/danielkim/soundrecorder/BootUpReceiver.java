/*
 * Year: 2017. This class was added by iClaude.
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
        startWakefulService(context, ScheduledRecordingService.makeIntent(context, true));
    }
}
