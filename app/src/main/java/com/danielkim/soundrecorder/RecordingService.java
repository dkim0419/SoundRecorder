package com.danielkim.soundrecorder;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.MediaRecorder;
import android.os.Binder;
import android.os.Environment;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

import com.coremedia.iso.boxes.Container;
import com.danielkim.soundrecorder.activities.MainActivity;
import com.googlecode.mp4parser.FileDataSourceImpl;
import com.googlecode.mp4parser.authoring.Movie;
import com.googlecode.mp4parser.authoring.Track;
import com.googlecode.mp4parser.authoring.builder.DefaultMp4Builder;
import com.googlecode.mp4parser.authoring.container.mp4.MovieCreator;
import com.googlecode.mp4parser.authoring.tracks.AppendTrack;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

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

    private boolean isFilePathTemp = true;
    private boolean isPaused;
    private int tempFileCount = 0;

    private ArrayList<String> filesPaused = new ArrayList<>();
    private ArrayList<Long> pauseDurations= new ArrayList<>();

    // Binder given to clients
    private final IBinder mBinder = new LocalBinder();


    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
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

  public void setFileNameAndPath() {
    if (isFilePathTemp) {
      mFileName = getString(R.string.default_file_name) + (++tempFileCount )+ "_" + ".tmp";
      mFilePath = Environment.getExternalStorageDirectory().getAbsolutePath();
      mFilePath += "/SoundRecorder/" + mFileName;
    } else {
      int count = 0;
      File f;

      do {
        count++;

        mFileName =
            getString(R.string.default_file_name) + "_" + (mDatabase.getCount() + count) + ".mp4";

        mFilePath = Environment.getExternalStorageDirectory().getAbsolutePath();
        mFilePath += "/SoundRecorder/" + mFileName;

        f = new File(mFilePath);
      } while (f.exists() && !f.isDirectory());
    }
  }

    public void startRecording() {
        isPaused = false;
        isFilePathTemp=true;
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

    public void pauseRecording(){
        isPaused = true;
        mRecorder.stop();
        mElapsedMillis = (System.currentTimeMillis() - mStartingTimeMillis);
        pauseDurations.add(mElapsedMillis);
        Toast.makeText(this, getString(R.string.toast_recording_paused), Toast.LENGTH_LONG).show();

        //remove notification
        if (mIncrementTimerTask != null) {
            mIncrementTimerTask.cancel();
            mIncrementTimerTask = null;
        }
        filesPaused.add(mFilePath);

    }

    public void stopRecording() {
        if(!isPaused)
            filesPaused.add(mFilePath);

        isFilePathTemp =false;
        setFileNameAndPath();

        if (!isPaused) {
            mRecorder.stop();
            mElapsedMillis = (System.currentTimeMillis() - mStartingTimeMillis);
        }
        mRecorder.release();
        Toast.makeText(this, getString(R.string.toast_recording_finish) + " " + mFilePath, Toast.LENGTH_LONG).show();

        isPaused = false;
        //remove notification
        if (mIncrementTimerTask != null) {
            mIncrementTimerTask.cancel();
            mIncrementTimerTask = null;
        }

        mRecorder = null;
        if (filesPaused != null && !filesPaused.isEmpty()) {
            if (makeSingleFile(filesPaused)) {
                for (long duration : pauseDurations)
                    mElapsedMillis += duration;
            }
        }

        try {
            mDatabase.addRecording(mFileName, mFilePath, mElapsedMillis);

        } catch (Exception e){
            Log.e(LOG_TAG, "exception", e);
        }
    }

    /**
     * collect temp generated files because of pause to one target file
     * @param filesPaused contains all temp files due to pause
     */
    private boolean makeSingleFile(ArrayList<String> filesPaused) {
        ArrayList<Track> tracks =new ArrayList<>();
        Movie finalMovie =new Movie();
        for (String filePath : filesPaused) {
            try {
                 Movie movie = MovieCreator.build(new FileDataSourceImpl(filePath));
                 List<Track> movieTracks = movie.getTracks();
                tracks.addAll(movieTracks);
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            }
        }

        if (tracks.size() > 0) {
            try {
                finalMovie.addTrack(new AppendTrack(tracks.toArray(new Track[tracks.size()])));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
//        try {
//            finalMovie.addTrack(new AppendTrack((Track) tracks));
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
        Container mp4file = new DefaultMp4Builder().build(finalMovie);
        FileChannel fc = null;
        try {
            fc = new FileOutputStream(new File(mFilePath)).getChannel();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return false;
        }
        try {
            mp4file.writeContainer(fc);
            fc.close();
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }

    }

    public void resumeRecording(){
        isPaused=false;
        startRecording();
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



    /**
     * Class used for the client Binder.  Because we know this service always
     * runs in the same process as its clients, we don't need to deal with IPC.
     */
    public class LocalBinder extends Binder {
        public RecordingService getService() {
            // Return this instance of LocalService so clients can call public methods
            return RecordingService.this;
        }
    }
}
