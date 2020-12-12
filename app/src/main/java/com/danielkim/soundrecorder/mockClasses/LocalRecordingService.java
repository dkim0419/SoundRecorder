package com.danielkim.soundrecorder.mockClasses;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;


public class LocalRecordingService extends Service {
    private Thread recordingThread;
    private boolean isRecording;
    private boolean isRecordingInPause;

    private long recordingDuration;
    private long pauseTimeStart;
    private long pauseTimeEnd;
    private long totalBreakTime; // cumulative pause intervals
    private long mStartingTimeMillis;

    LocalRecordingService(){}

    @Override
    public void onCreate() {
        super.onCreate();

        this.isRecording = false;
        this.isRecordingInPause = false;
        this.pauseTimeStart = 0;
        this.pauseTimeEnd = 0;
        this.totalBreakTime = 0;
        this.mStartingTimeMillis = 0;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if(intent.hasExtra("inPause")){
            this.isRecordingInPause = intent.getExtras().getBoolean("inPause");

            if (this.isRecordingInPause) this.pauseTimeStart = System.currentTimeMillis();
            else {
                this.pauseTimeEnd = System.currentTimeMillis();
                this.totalBreakTime += (this.pauseTimeEnd - this.pauseTimeStart);
            }
        }
        else {
            startRecording();
        }

        return START_STICKY;
    }


    private void startRecording(){
        this.recordingThread = new Thread(new Runnable() {
            @Override
            public void run() {
                writeAudioDataToFile();
            }
        });

        this.mStartingTimeMillis = System.currentTimeMillis();
        this.isRecording = true;
        this.recordingThread.start();
    }

    private void writeAudioDataToFile(){
        System.out.println("Recording started.");
        while(this.isRecording) {
            if(this.isRecordingInPause) {
                System.out.println("Recording in pause...");
            }
            else{
                System.out.println("On recording: writing on temp file...");
            }

            try {
                Thread.sleep(2 * 1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onDestroy() {
        stopRecording();
        super.onDestroy();
    }

    private void stopRecording(){
        this.isRecording = false;
        this.recordingDuration = System.currentTimeMillis() - this.mStartingTimeMillis - this.totalBreakTime;

        if (this.isRecordingInPause) {
            this.recordingDuration -= (System.currentTimeMillis() - this.pauseTimeStart);
        }

        this.isRecordingInPause = false;
        this.recordingThread = null;


        copyWaveFile();
        System.out.println("Recording stopped. Audio file created. Duration: " + Math.round(this.recordingDuration/100.0)/10.0 + " s.\n");
    }

    private void copyWaveFile(){
        System.out.println("Audio converted in wav, temp file deleted...");
    }


    public boolean isRecording() {
        return isRecording;
    }

    public boolean isRecordingInPause() {
        return isRecordingInPause;
    }

    public long getPauseTimeStart() {
        return pauseTimeStart;
    }

    public long getPauseTimeEnd() {
        return pauseTimeEnd;
    }

    public long getTotalBreakTime() {
        return totalBreakTime;
    }

    public long getmStartingTimeMillis() {
        return mStartingTimeMillis;
    }

    public long getRecordingDuration(){
        return recordingDuration;
    }


    /*** States description*/
    public boolean isStateS0(){
        if(!isRecording && !isRecordingInPause && mStartingTimeMillis == 0 && pauseTimeStart == 0 &&
                pauseTimeEnd == 0 && totalBreakTime == 0) return true;

        else return false;
    }


    public boolean isStateS1(){
        if (isRecording && !isRecordingInPause && mStartingTimeMillis > 0 &&
                pauseTimeStart == 0 && pauseTimeEnd == 0 && totalBreakTime == 0) return true;

        else return false;
    }

    public boolean isStateS2(){
        if(!isRecording && !isRecordingInPause && mStartingTimeMillis > 0 && pauseTimeStart >= 0){
            if (pauseTimeEnd == 0) return true;
            else if (pauseTimeStart > pauseTimeEnd && totalBreakTime >= 0){ return true; }
            else if (pauseTimeEnd > pauseTimeStart && totalBreakTime > 0){ return true; }
            else return false;
        }
        else return false;
    }

    public boolean isStateS3(){
        if (isRecording && isRecordingInPause && mStartingTimeMillis > 0 && pauseTimeStart >0 &&
                pauseTimeEnd >= 0 && pauseTimeStart > pauseTimeEnd && totalBreakTime >= 0) return true;

        else return false;
    }

    public boolean isStateS4(){
        if (isRecording && !isRecordingInPause && mStartingTimeMillis > 0 && pauseTimeStart > 0 &&
                pauseTimeEnd > 0 && pauseTimeEnd > pauseTimeStart && totalBreakTime > 0) return true;

        else return false;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
