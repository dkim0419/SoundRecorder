package com.danielkim.soundrecorder;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.MediaRecorder;
import android.os.Binder;
import android.os.Bundle;
import android.os.Environment;
import android.os.IBinder;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

import com.coremedia.iso.boxes.Container;
import com.danielkim.soundrecorder.activities.MainActivity;
import com.googlecode.mp4parser.authoring.Movie;
import com.googlecode.mp4parser.authoring.Track;
import com.googlecode.mp4parser.authoring.builder.DefaultMp4Builder;
import com.googlecode.mp4parser.authoring.container.mp4.MovieCreator;
import com.googlecode.mp4parser.authoring.tracks.AppendTrack;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Handler;

/**
 * Created by Daniel on 12/28/2014.
 */
public class RecordingService extends Service {

    private static final String LOG_TAG = "RecordingService";

    private String mFileName = null;
    private String mFilePath = null;

    private MediaRecorder mRecorder = null;

    private DBHelper mDatabase;

    private long mStartingTimeMillis = 0;
    private long mElapsedMillis = 0;
    private int mElapsedSeconds = 0;
    private OnTimerChangedListener onTimerChangedListener = null;
    private static final SimpleDateFormat mTimerFormat = new SimpleDateFormat("mm:ss", Locale.getDefault());

    private Timer mTimer = null;
    private TimerTask mIncrementTimerTask = null;


    /**
     * Binding this service with the RecordingService fragment for calling the methods of this service from the fragment.
     */

    IBinder mBinder = new LocalBinder();

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    /**
     * This class returns an instance of this service class.
     */
    public class LocalBinder extends Binder {
        public RecordingService getServerInstance() {
            return RecordingService.this;
        }
    }


    public interface OnTimerChangedListener {
        void onTimerChanged(int seconds);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mDatabase = new DBHelper(getApplicationContext());
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }



    /**
     * This methods takes as input all the files which are created when the user used the paused button
     * and append those files into one file. This method even works when the paused button is not used and only one audio file
     * is created in table2.
     *
     * Refer to this library for better understanding the below method - https://github.com/sannies/mp4parser
     */
    public void startAppendingAudio()
    {
        try {


            String mediaKey = "soun" ;
            List<Movie> listMovies = new ArrayList<>();

            // Getting the absolute paths of all the files from table two and saving them in this list;
            ArrayList<String> myList = mDatabase.getAllAppendingFiles();

            for(int i=0;i<myList.size();i++)
            {
                Log.d("check i ",String.valueOf(i) + String.valueOf(myList.get(i)));
                listMovies.add(MovieCreator.build(String.valueOf(myList.get(i))));
            }

            List<Track> listTracks = new LinkedList<>();
            for (Movie movie : listMovies) {
                for (Track track : movie.getTracks()) {
                    if (track.getHandler().equals(mediaKey)) {
                        listTracks.add(track);
                    }
                }
            }
            Movie outputMovie = new Movie();
            if (!listTracks.isEmpty()) {
                outputMovie.addTrack(new AppendTrack(listTracks.toArray(new Track[listTracks.size()])));
            }
            Container container = new DefaultMp4Builder().build(outputMovie);

            long elapsedTime = mDatabase.getTotalTime();

            String fname = getString(R.string.default_file_name)
                    + "_" + (mDatabase.getCount() + 1) + ".mp4";


            String fPath = Environment.getExternalStorageDirectory().getAbsolutePath();
            fPath += "/SoundRecorder/" + fname;

           File f = new File(fPath);

            FileChannel fileChannel = new RandomAccessFile(f, "rw").getChannel();
            container.writeContainer(fileChannel);
            fileChannel.close();

            stopRecording(fname,fPath,elapsedTime);


            //return true;
        }
        catch (IOException e) {
            Log.e(LOG_TAG, "Error merging media files. exception: "+e.getMessage());
            //return false;
        }


    }



    public void startRecording() {

        setFileNameAndPath();

        mRecorder = new MediaRecorder();
        mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
        mRecorder.setOutputFile(mFilePath);
        mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
        mRecorder.setAudioChannels(1);
        if (MySharedPreferences.getPrefHighQuality(this)) {
            mRecorder.setAudioSamplingRate(44100);
            mRecorder.setAudioEncodingBitRate(192000);
        }

        try {
            mRecorder.prepare();
            mRecorder.start();
            mStartingTimeMillis = System.currentTimeMillis();

            //startTimer();
            //startForeground(1, createNotification());

        } catch (IOException e) {
            Log.e(LOG_TAG, "prepare() failed");
        }
    }

    public void setFileNameAndPath(){
        int count = 0;
        File f;

        do{
            count++;

            mFileName = "paused_recording"
                    + "_" + (mDatabase.getCount2() + count) + ".mp4";
            mFilePath = Environment.getExternalStorageDirectory().getAbsolutePath();
            mFilePath += "/SoundRecorder/" + mFileName;

            f = new File(mFilePath);
        }while (f.exists() && !f.isDirectory());
    }


    /**
     * After appending audio files from table2 this method stores the newly created audio file in table1.
     * @param fileName
     * @param filePath
     * @param elapsedMilis
     */

    public void stopRecording(String fileName,String filePath, long elapsedMilis) {

        if(mRecorder!=null)
        {
            mRecorder.stop();
            mRecorder.release();
            mRecorder = null;
        }


        Toast.makeText(this, getString(R.string.toast_recording_finish) + " " + fileName, Toast.LENGTH_LONG).show();

        //remove notification
        if (mIncrementTimerTask != null) {
            mIncrementTimerTask.cancel();
            mIncrementTimerTask = null;
        }


        try {
            mDatabase.addRecording(fileName, filePath, elapsedMilis);

        } catch (Exception e){
            Log.e(LOG_TAG, "exception", e);
        }
    }


    /**
     * This method stores the files to table2 when the recording stops (or paused) for the purpose of appending them later.
     */
    public void stopRecordingForPause()
    {
        if(mRecorder!=null)
        {
            mRecorder.stop();
            mRecorder.release();
            mRecorder = null;
        }

        mElapsedMillis = (System.currentTimeMillis() - mStartingTimeMillis);

        //Toast.makeText(this, getString(R.string.toast_recording_finish) + " " + mFilePath, Toast.LENGTH_LONG).show();

        //remove notification
        if (mIncrementTimerTask != null) {
            mIncrementTimerTask.cancel();
            mIncrementTimerTask = null;
        }



        try {
            mDatabase.addRecording2(mFileName, mFilePath, mElapsedMillis);

        } catch (Exception e){
            Log.e(LOG_TAG, "exception", e);
        }
    }

    private void startTimer() {
        mTimer = new Timer();
        mIncrementTimerTask = new TimerTask() {
            @Override
            public void run() {
                mElapsedSeconds++;
                if (onTimerChangedListener != null)
                    onTimerChangedListener.onTimerChanged(mElapsedSeconds);
                NotificationManager mgr = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                mgr.notify(1, createNotification());
            }
        };
        mTimer.scheduleAtFixedRate(mIncrementTimerTask, 1000, 1000);
    }

    //TODO:
    private Notification createNotification() {
        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(getApplicationContext())
                        .setSmallIcon(R.drawable.ic_mic_white_36dp)
                        .setContentTitle(getString(R.string.notification_recording))
                        .setContentText(mTimerFormat.format(mElapsedSeconds * 1000))
                        .setOngoing(true);

        mBuilder.setContentIntent(PendingIntent.getActivities(getApplicationContext(), 0,
                new Intent[]{new Intent(getApplicationContext(), MainActivity.class)}, 0));

        return mBuilder.build();
    }
}
