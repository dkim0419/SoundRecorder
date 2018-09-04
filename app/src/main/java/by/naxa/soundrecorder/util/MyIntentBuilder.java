package by.naxa.soundrecorder.util;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;

public class MyIntentBuilder {

    private static final String KEY_MESSAGE = "msg";
    private static final String KEY_COMMAND = "cmd";
    private final Context mContext;
    private final Class<? extends Service> serviceClass;
    private String mMessage;
    private @Command
    int mCommandId = Command.INVALID;

    public static MyIntentBuilder getInstance(@NonNull Context context,
                                              @NonNull Class<? extends Service> service) {
        return new MyIntentBuilder(context, service);
    }

    private MyIntentBuilder(@NonNull Context context,
                            @NonNull Class<? extends Service> serviceClass) {
        this.mContext = context;
        this.serviceClass = serviceClass;
    }

    public MyIntentBuilder setMessage(String message) {
        this.mMessage = message;
        return this;
    }

    /**
     * @param command Don't use {@link Command#INVALID} as a param. If you do then this method does
     *                nothing.
     */
    public MyIntentBuilder setCommand(@Command int command) {
        this.mCommandId = command;
        return this;
    }

    public Intent build() {
        Intent intent = new Intent(mContext, serviceClass);
        if (mCommandId != Command.INVALID) {
            intent.putExtra(KEY_COMMAND, mCommandId);
        }
        if (mMessage != null) {
            intent.putExtra(KEY_MESSAGE, mMessage);
        }
        return intent;
    }

    public static boolean containsCommand(Intent intent) {
        final Bundle extras = intent.getExtras();
        return extras != null && extras.containsKey(KEY_COMMAND);
    }

    public static boolean containsMessage(Intent intent) {
        final Bundle extras = intent.getExtras();
        return extras != null && extras.containsKey(KEY_MESSAGE);
    }

    @Command
    public static int getCommand(Intent intent) {
        final @Command int commandId = intent.getExtras().getInt(KEY_COMMAND);
        return commandId;
    }

    public static String getMessage(Intent intent) {
        return intent.getExtras().getString(KEY_MESSAGE);
    }

}