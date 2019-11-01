package by.naxa.soundrecorder.util;

import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import by.naxa.soundrecorder.RecorderState;

public class EventBroadcaster {
    public static final String SHOW_SNACKBAR = "SHOW_SNACKBAR";
    public static final String MESSAGE = "MESSAGE";
    public static final String CHANGE_STATE = "CHANGE_STATE";
    public static final String NEW_STATE = "NEW_STATE";
    public static final String CHRONOMETER_TIME = "CHRONOMETER_TIME";
    public static final String LAST_AUDIO_LOCATION = "LAST_AUDIO_LOCATION";

    public static void send(@NonNull Context context, String message) {
        final Intent it = new Intent(EventBroadcaster.SHOW_SNACKBAR);
        if (!TextUtils.isEmpty(message))
            it.putExtra(EventBroadcaster.MESSAGE, message);
        LocalBroadcastManager.getInstance(context).sendBroadcast(it);
    }

    public static void send(@Nullable Context context, int stringId) {
        if (context == null)
            return;
        send(context, context.getString(stringId));
    }

    public static void stopRecording(@Nullable Context context) {
        if (context == null)
            return;
        final Intent it = new Intent(EventBroadcaster.CHANGE_STATE);
        it.putExtra(EventBroadcaster.NEW_STATE, RecorderState.STOPPED);
        LocalBroadcastManager.getInstance(context).sendBroadcast(it);
    }

    public static void stopRecording(@Nullable Context context,String filePath) {
        if (context == null)
            return;
        final Intent it = new Intent(EventBroadcaster.CHANGE_STATE);
        it.putExtra(EventBroadcaster.NEW_STATE, RecorderState.STOPPED);
        it.putExtra(EventBroadcaster.LAST_AUDIO_LOCATION, filePath);
        LocalBroadcastManager.getInstance(context).sendBroadcast(it);
    }

    public static void startRecording(@Nullable Context context, long chronometerTime) {
        if (context == null)
            return;
        final Intent it = new Intent(EventBroadcaster.CHANGE_STATE);
        it.putExtra(EventBroadcaster.NEW_STATE, RecorderState.RECORDING);
        it.putExtra(EventBroadcaster.CHRONOMETER_TIME, chronometerTime);
        LocalBroadcastManager.getInstance(context).sendBroadcast(it);
    }
}
