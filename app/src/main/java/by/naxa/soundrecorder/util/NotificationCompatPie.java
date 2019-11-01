package by.naxa.soundrecorder.util;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Build;

import java.util.Random;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;
import by.naxa.soundrecorder.R;
import by.naxa.soundrecorder.activities.MainActivity;
import by.naxa.soundrecorder.services.RecordingService;

public class NotificationCompatPie {
    private static final Random r = new Random();

    public static final String CHANNEL_ID = "13";
    private static final int ONGOING_NOTIFICATION_ID = r.nextInt(1000);

    public static void createNotification(Service context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            final String channelId = createChannel(context);
            final Notification notification = buildNotification(context, channelId);
            context.startForeground(ONGOING_NOTIFICATION_ID, notification);
        } else {
            createNotificationPreN(context);
        }
    }

    private static void createNotificationPreN(Service context) {
        // Create Pending Intents.
        PendingIntent piLaunchMainActivity = getLaunchActivityPI(context);
        PendingIntent piStopService = getStopServicePI(context);
        PendingIntent piPauseService = getPauseServicePI(context);
        PendingIntent piResumeService = getResumeServicePI(context);

        // Action to pause the service.
        NotificationCompat.Action pauseAction =
                new NotificationCompat.Action.Builder(
                        R.drawable.ic_media_pause,
                        context.getString(R.string.pause_recording_button),
                        piPauseService)
                        .build();

        // Action to resume the service.
        NotificationCompat.Action resumeAction =
                new NotificationCompat.Action.Builder(
                        R.drawable.ic_media_play,
                        context.getString(R.string.resume_recording_button),
                        piResumeService)
                        .build();

        // Action to stop the service.
        NotificationCompat.Action stopAction =
                new NotificationCompat.Action.Builder(
                        R.drawable.ic_media_stop,
                        context.getString(R.string.action_stop),
                        piStopService)
                        .build();

        // Create a notification.
        final NotificationCompat.Builder builder = new NotificationCompat.Builder(context)
                .setContentTitle(context.getString(R.string.notification_title))
                .setSmallIcon(R.drawable.ic_mic_white_36dp)
                .setContentIntent(piLaunchMainActivity)
                .addAction(stopAction)
                .setStyle(new NotificationCompat.BigTextStyle());
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            builder.setColor(context.getColor(R.color.primary));
        }

        context.startForeground(ONGOING_NOTIFICATION_ID, builder.build());
    }

    private static PendingIntent getServicePI(Service context, @Command int cmd) {
        final Intent iService = MyIntentBuilder.getInstance(context, RecordingService.class)
                .setCommand(cmd).build();
        return PendingIntent.getService(context, r.nextInt(100), iService, 0);
    }

    private static PendingIntent getStopServicePI(Service context) {
        return getServicePI(context, Command.STOP);
    }

    private static PendingIntent getPauseServicePI(Service context) {
        return getServicePI(context, Command.PAUSE);
    }

    private static PendingIntent getResumeServicePI(Service context) {
        return getServicePI(context, Command.START);
    }

    private static PendingIntent getLaunchActivityPI(Service context) {
        final Intent iLaunchActivity = new Intent(context, MainActivity.class);
        return PendingIntent.getActivity(context, r.nextInt(100), iLaunchActivity,
                PendingIntent.FLAG_UPDATE_CURRENT);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private static Notification buildNotification(Service context, String channelId) {
        // Create Pending Intents.
        PendingIntent piLaunchMainActivity = getLaunchActivityPI(context);
        PendingIntent piStopService = getStopServicePI(context);
        PendingIntent piPauseService = getPauseServicePI(context);
        PendingIntent piResumeService = getResumeServicePI(context);

        // Action to pause the service.
        Notification.Action pauseAction =
                new Notification.Action.Builder(
                        R.drawable.ic_media_pause,
                        context.getString(R.string.pause_recording_button),
                        piPauseService)
                        .build();

        // Action to resume the service.
        Notification.Action resumeAction =
                new Notification.Action.Builder(
                        R.drawable.ic_media_play,
                        context.getString(R.string.resume_recording_button),
                        piResumeService)
                        .build();

        // Action to stop the service.
        Notification.Action stopAction =
                new Notification.Action.Builder(
                        R.drawable.ic_media_stop,
                        context.getString(R.string.action_stop),
                        piStopService)
                        .build();

        // Create a notification.
        return new Notification.Builder(context, channelId)
                .setContentTitle(context.getString(R.string.notification_title))
                .setSmallIcon(R.drawable.ic_mic_white_36dp)
                .setColor(context.getColor(R.color.primary))
                .setContentIntent(piLaunchMainActivity)
                .setActions(stopAction)
                .setStyle(new Notification.BigTextStyle())
                .build();
    }

    /**
     * Create a notification channel.
     * But only on API 26+ because the {@link NotificationChannel} class is new
     * and not in the support library.
     */
    @NonNull
    @RequiresApi(api = Build.VERSION_CODES.O)
    private static String createChannel(Service ctx) {
        final String channelName = ctx.getString(R.string.notification_channel_recorder);
        final NotificationChannel notificationChannel =
                new NotificationChannel(CHANNEL_ID, channelName, NotificationManager.IMPORTANCE_LOW);
        notificationChannel.setDescription(ctx.getString(R.string.notification_channel_description));

        // Register the channel with the system;
        // you can't change the importance or other notification behaviours after this
        final NotificationManager notificationManager = ctx.getSystemService(NotificationManager.class);
        notificationManager.createNotificationChannel(notificationChannel);
        return CHANNEL_ID;
    }
}
