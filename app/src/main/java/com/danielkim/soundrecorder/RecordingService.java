package com.danielkim.soundrecorder;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Environment;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

import com.danielkim.soundrecorder.activities.MainActivity;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by Daniel on 12/28/2014.
 */
public class RecordingService extends Service {

    private static final String LOG_TAG = "RecordingService";
    private static final String AUDIO_RECORDER_TEMP_FILE = "record_temp.raw";

    private String mFileName = null;
    private String mFilePath = null;
    private String tempFilePath = null;

    private DBHelper mDatabase;

    private static final int RECORDER_BPP = 16;
    private static final int RECORDER_HIGH_QUALITY_SAMPLERATE = 48000;
    private static final int RECORDER_CHANNELS = AudioFormat.CHANNEL_IN_MONO;
    private static final int RECORDER_AUDIO_ENCODING = AudioFormat.ENCODING_PCM_16BIT;

    private int bufferSize = 0;

    private AudioRecord audioRecord;

    private Thread recordingThread;

    private boolean isRecording;
    private boolean isRecordingInPause;

    private long pauseTimeStart;
    private long pauseTimeEnd;
    private long timeWhenPaused;
    private long mStartingTimeMillis;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        this.isRecording = false;
        this.isRecordingInPause = false;
        this.pauseTimeStart = 0;
        this.pauseTimeEnd = 0;
        this.timeWhenPaused = 0;
        this.mStartingTimeMillis = 0;
        this.mDatabase = DBHelper.getInstance(getApplicationContext());
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if(intent.hasExtra("inPause")){
            this.isRecordingInPause = (boolean) intent.getExtras().get("inPause");

            if (this.isRecordingInPause) this.pauseTimeStart = System.currentTimeMillis();
            else {
                this.pauseTimeEnd = System.currentTimeMillis();
                this.timeWhenPaused += (this.pauseTimeEnd - this.pauseTimeStart);
            }
        }
        else {
            startRecording();
        }

        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        if (audioRecord != null) {
            stopRecording();
        }

        super.onDestroy();
    }

    public void startRecording(){
        this.recordingThread = new Thread(new Runnable() {
            @Override
            public void run() {
                writeAudioDataToFile();
            }
        });

        setFileNameAndPath();

        this.tempFilePath = Environment.getExternalStorageDirectory().getPath() + "/SoundRecorder/" + AUDIO_RECORDER_TEMP_FILE;
        this.bufferSize = AudioRecord.getMinBufferSize(RECORDER_HIGH_QUALITY_SAMPLERATE, RECORDER_CHANNELS, RECORDER_AUDIO_ENCODING) * 3;
        this.audioRecord = new AudioRecord(MediaRecorder.AudioSource.MIC, RECORDER_HIGH_QUALITY_SAMPLERATE, RECORDER_CHANNELS, RECORDER_AUDIO_ENCODING, bufferSize);

        if(this.audioRecord.getState() == AudioRecord.STATE_INITIALIZED){
            this.mStartingTimeMillis = System.currentTimeMillis();
            this.isRecording = true;
            this.recordingThread.start();
        }
    }

    private void writeAudioDataToFile() {
        byte data[] = new byte[this.bufferSize];
        FileOutputStream os = null;

        try {
            os = new FileOutputStream(this.tempFilePath);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        int read = 0;
        if (null != os) {
            while (this.isRecording) {
                if(isRecordingInPause){
                    if (this.audioRecord.getState() != AudioRecord.RECORDSTATE_STOPPED) this.audioRecord.stop();
                }
                else {
                    if (this.audioRecord.getState() != AudioRecord.RECORDSTATE_RECORDING) this.audioRecord.startRecording();

                    read = audioRecord.read(data, 0, bufferSize);

                    if (read != AudioRecord.ERROR_INVALID_OPERATION) {
                        try {
                            os.write(data);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }

            try {
                os.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void deleteTempFile() {
        File file = new File(this.tempFilePath);
        file.delete();
    }

    public void setFileNameAndPath(){
        int count = 0;
        File f;

        do{
            count++;

            this.mFileName = getString(R.string.default_file_name) + "_" + (this.mDatabase.getCount() + count) + ".wav";

            this.mFilePath = Environment.getExternalStorageDirectory().getAbsolutePath();
            this.mFilePath += "/SoundRecorder/" + this.mFileName;

            f = new File(mFilePath);
        }while (f.exists() && !f.isDirectory());
    }

    public void stopRecording(){
        if (this.audioRecord != null) {
            this.isRecording = false;

            if (this.audioRecord.getState() == AudioRecord.STATE_INITIALIZED) this.audioRecord.stop();

            this.audioRecord.release();

            long recordingDuration = System.currentTimeMillis() - this.mStartingTimeMillis - this.timeWhenPaused;

            if (this.isRecordingInPause) {
                recordingDuration -= (System.currentTimeMillis() - this.pauseTimeStart);
            }

            this.isRecordingInPause = false;

            copyWaveFile(this.tempFilePath, this.mFilePath);
            deleteTempFile();

            this.audioRecord = null;
            this.recordingThread = null;

            Toast.makeText(this, getString(R.string.toast_recording_finish) + " " + this.mFilePath, Toast.LENGTH_LONG).show();

            try {
                this.mDatabase.addRecording(this.mFileName, this.mFilePath, recordingDuration);
            } catch (Exception e){
                Log.e(LOG_TAG, "exception", e);
            }
        }
    }

    private void copyWaveFile(String inFilename, String outFilename) {
        FileInputStream in = null;
        FileOutputStream out = null;
        long totalAudioLen = 0;
        long totalDataLen = totalAudioLen + 36;
        long longSampleRate = RECORDER_HIGH_QUALITY_SAMPLERATE;
        int channels = ((RECORDER_CHANNELS == AudioFormat.CHANNEL_IN_MONO) ? 1 : 2);
        long byteRate = RECORDER_BPP * RECORDER_HIGH_QUALITY_SAMPLERATE * channels / 8;

        byte[] data = new byte[bufferSize];

        try {
            in = new FileInputStream(inFilename);
            out = new FileOutputStream(outFilename);
            totalAudioLen = in.getChannel().size();
            totalDataLen = totalAudioLen + 36;

            WriteWaveFileHeader(out, totalAudioLen, totalDataLen, longSampleRate, channels, byteRate);

            while (in.read(data) != -1) {
                out.write(data);
            }

            in.close();
            out.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void WriteWaveFileHeader(FileOutputStream out, long totalAudioLen, long totalDataLen, long longSampleRate, int channels, long byteRate) throws IOException {
        byte[] header = new byte[44];

        header[0] = 'R'; // RIFF/WAVE header
        header[1] = 'I';
        header[2] = 'F';
        header[3] = 'F';
        header[4] = (byte) (totalDataLen & 0xff);
        header[5] = (byte) ((totalDataLen >> 8) & 0xff);
        header[6] = (byte) ((totalDataLen >> 16) & 0xff);
        header[7] = (byte) ((totalDataLen >> 24) & 0xff);
        header[8] = 'W';
        header[9] = 'A';
        header[10] = 'V';
        header[11] = 'E';
        header[12] = 'f'; // 'fmt ' chunk
        header[13] = 'm';
        header[14] = 't';
        header[15] = ' ';
        header[16] = 16; // 4 bytes: size of 'fmt ' chunk
        header[17] = 0;
        header[18] = 0;
        header[19] = 0;
        header[20] = 1; // format = 1
        header[21] = 0;
        header[22] = (byte) channels;
        header[23] = 0;
        header[24] = (byte) (longSampleRate & 0xff);
        header[25] = (byte) ((longSampleRate >> 8) & 0xff);
        header[26] = (byte) ((longSampleRate >> 16) & 0xff);
        header[27] = (byte) ((longSampleRate >> 24) & 0xff);
        header[28] = (byte) (byteRate & 0xff);
        header[29] = (byte) ((byteRate >> 8) & 0xff);
        header[30] = (byte) ((byteRate >> 16) & 0xff);
        header[31] = (byte) ((byteRate >> 24) & 0xff);
        header[32] = (byte) (((RECORDER_CHANNELS == AudioFormat.CHANNEL_IN_MONO) ? 1 : 2) * 16 / 8); // block align
        header[33] = 0;
        header[34] = RECORDER_BPP; // bits per sample
        header[35] = 0;
        header[36] = 'd';
        header[37] = 'a';
        header[38] = 't';
        header[39] = 'a';
        header[40] = (byte) (totalAudioLen & 0xff);
        header[41] = (byte) ((totalAudioLen >> 8) & 0xff);
        header[42] = (byte) ((totalAudioLen >> 16) & 0xff);
        header[43] = (byte) ((totalAudioLen >> 24) & 0xff);

        out.write(header, 0, 44);
    }
}
