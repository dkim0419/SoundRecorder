package by.naxa.soundrecorder.util;

import android.app.Activity;
import android.view.WindowManager;

public class ScreenLock {

    /**
     * Keep the screen on while playing or recording audio
     * @param activity
     */
    public static void keepScreenOn(Activity activity) {
        if (activity != null) {
            activity.getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        }
    }

    /**
     * Allow the screen to turn off again once audio is finished playing or recording is stopped
     * @param activity
     */
    public static void allowScreenTurnOff(Activity activity) {
        if (activity != null) {
            activity.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        }
    }

}
