package by.naxa.soundrecorder.util;

import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

public class EventBroadcaster {
    public static final String SHOW_SNACKBAR = "SHOW_SNACKBAR";
    public static final String MESSAGE = "MESSAGE";

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
}
