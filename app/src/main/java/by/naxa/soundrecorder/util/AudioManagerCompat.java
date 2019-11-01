package by.naxa.soundrecorder.util;

import android.content.Context;
import android.media.AudioAttributes;
import android.media.AudioFocusRequest;
import android.media.AudioManager;
import android.media.AudioManager.OnAudioFocusChangeListener;
import android.os.Build;

import static android.content.Context.AUDIO_SERVICE;

public class AudioManagerCompat {

    private AudioManager mAudioManager;

    public static AudioManagerCompat getInstance(Context context) {
        return new AudioManagerCompat((AudioManager) context.getSystemService(AUDIO_SERVICE));
    }

    private AudioManagerCompat(AudioManager audioManager) {
        mAudioManager = audioManager;
    }

    public boolean requestAudioFocus(OnAudioFocusChangeListener focusChangeListener,
                                     int streamType, int audioFocusGain) {
        int r;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            r = mAudioManager.requestAudioFocus(
                    new AudioFocusRequest.Builder(audioFocusGain)
                            .setAudioAttributes(
                                    new AudioAttributes.Builder()
                                            .setLegacyStreamType(streamType)
                                            .build())
                            .setOnAudioFocusChangeListener(focusChangeListener)
                            .build());
        } else {
            r = mAudioManager.requestAudioFocus(focusChangeListener, streamType, audioFocusGain);
        }

        return r == AudioManager.AUDIOFOCUS_REQUEST_GRANTED;
    }

    public int abandonAudioFocus(OnAudioFocusChangeListener focusChangeListener) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            return mAudioManager.abandonAudioFocusRequest(
                    new AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN)
                            .setOnAudioFocusChangeListener(focusChangeListener)
                            .build());
        } else {
            return mAudioManager.abandonAudioFocus(focusChangeListener);
        }
    }

}
