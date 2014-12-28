package com.danielkim.soundrecorder;

import android.app.Service;
import android.content.Intent;
import android.media.MediaRecorder;
import android.os.Environment;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by Daniel on 12/28/2014.
 */
public class RecordingService extends Service {

    private static final String LOG_TAG = "SoundRecorder_RecordFragment";

    SimpleDateFormat formatter;
    Date now;
    private String mFileName = null;

    private MediaRecorder mRecorder = null;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        startRecording();
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        if (mRecorder != null) {
            stopRecording();
        }

        super.onDestroy();
    }

    public void startRecording() {
        formatter = new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss");
        now = new Date();
        mFileName = Environment.getExternalStorageDirectory().getAbsolutePath();
        mFileName += "/SoundRecorder/" + formatter.format(now) + "_soundrecorder.mp4";

        mRecorder = new MediaRecorder();
        mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
        mRecorder.setOutputFile(mFileName);
        mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);

        try {
            mRecorder.prepare();
            mRecorder.start();

        } catch (IOException e) {
            Log.e(LOG_TAG, "prepare() failed");
        }
    }

    public void stopRecording() {
        mRecorder.stop();
        mRecorder.release();
        Toast.makeText(this, "Recording saved to " + mFileName, Toast.LENGTH_LONG).show();
        mRecorder = null;
    }
}
