package by.naxa.soundrecorder.listeners;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.crashlytics.android.Crashlytics;

import by.naxa.soundrecorder.fragments.PlaybackFragment;
import io.fabric.sdk.android.Fabric;

import static android.content.Intent.ACTION_HEADSET_PLUG;
import static android.media.AudioManager.ACTION_AUDIO_BECOMING_NOISY;

/**
 * Receives headset connect and disconnect intents so that playback
 * may be paused when headset (earphone, headphone, or wireless speaker)
 * is disconnected or when audio becomes too noisy.
 */
public class HeadsetListener extends BroadcastReceiver {

    private static final String LOG_TAG = HeadsetListener.class.getName();
    private PlaybackFragment mInstance;

    boolean shouldResumeOnHeadphonesConnect = false;

    public HeadsetListener(PlaybackFragment instance) {
        mInstance = instance;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (isInitialStickyBroadcast()) return;

        Log.i(LOG_TAG, "onReceive: " + intent.toString());

        final boolean plugged, unplugged;

        if (ACTION_HEADSET_PLUG.equals(intent.getAction())) {
            plugged = intent.getIntExtra("state", -1) == 1;
            unplugged = intent.getIntExtra("state", -1) == 0;
        } else {
            plugged = unplugged = false;
        }

        final boolean becomingNoisy = ACTION_AUDIO_BECOMING_NOISY.equals(intent.getAction());

        if (Fabric.isInitialized()) {
            Crashlytics.setBool("plugged", plugged);
            Crashlytics.setBool("unplugged", unplugged);
            Crashlytics.setBool("becoming_noisy", becomingNoisy);
        }

        if (unplugged || becomingNoisy) {
            mInstance.tapStopButton();
        } else if (plugged && shouldResumeOnHeadphonesConnect) {
            mInstance.tapStartButton();
        }
    }
}
