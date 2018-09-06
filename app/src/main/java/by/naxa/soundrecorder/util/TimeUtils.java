package by.naxa.soundrecorder.util;

import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class TimeUtils {

    public static String formatDuration(long millis) {
        final String sign;
        if (millis < 0) {
            sign = "-";
            millis = -millis;
        } else {
            sign = "";
        }

        long hours = TimeUnit.MILLISECONDS.toHours(millis);
        long minutes = TimeUnit.MILLISECONDS.toMinutes(millis) - TimeUnit.HOURS.toMinutes(hours);
        long seconds = TimeUnit.MILLISECONDS.toSeconds(millis) - TimeUnit.HOURS.toSeconds(hours)
                - TimeUnit.MINUTES.toSeconds(minutes);
        if (hours > 0) {
            return String.format(Locale.ENGLISH, sign + "%d:%02d:%02d", hours, minutes, seconds);
        } else {
            return String.format(Locale.ENGLISH, sign + "%02d:%02d", minutes, seconds);
        }
    }

}
