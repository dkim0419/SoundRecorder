package by.naxa.soundrecorder.util;

import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class TimeUtils {

    public static String formatDuration(long millis) {
        String format;
        if (millis < 0) {
            format = "-";
            millis = -millis;
        } else {
            format = "";
        }

        long hours = TimeUnit.MILLISECONDS.toHours(millis);
        long minutes = TimeUnit.MILLISECONDS.toMinutes(millis) - TimeUnit.HOURS.toMinutes(hours);
        long seconds = TimeUnit.MILLISECONDS.toSeconds(millis) - TimeUnit.HOURS.toSeconds(hours)
                - TimeUnit.MINUTES.toSeconds(minutes);
        if (hours > 0) {
            format += "%d:%02d:%02d";
        } else {
            format += "%02d:%02d";
        }

        return String.format(Locale.ENGLISH, format, hours, minutes, seconds);
    }

}
