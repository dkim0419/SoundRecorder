package by.naxa.soundrecorder.fragments;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.IBinder;
import android.os.SystemClock;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.TextView;
import android.widget.Toast;

import com.melnykov.fab.FloatingActionButton;

import java.io.File;

import by.naxa.soundrecorder.Paths;
import by.naxa.soundrecorder.PermissionsHelper;
import by.naxa.soundrecorder.R;
import by.naxa.soundrecorder.RecordingService;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * to handle interaction events.
 * Use the {@link RecordFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class RecordFragment extends Fragment {
    private static final String LOG_TAG = RecordFragment.class.getSimpleName();
    private static final int MY_PERMISSIONS_REQUEST_RECORD_AUDIO = 1;
    private static final int MY_PERMISSIONS_REQUEST_RECORD_AUDIO_RESUME = 2;

    //Recording controls
    private FloatingActionButton mRecordButton = null;
    private Button mPauseButton = null;

    private TextView mRecordingPrompt;
    private int mRecordPromptCount = 0;

    private boolean mStartRecording = true;
    private boolean mPauseRecording = true;

    private Chronometer mChronometer = null;
    long timeWhenPaused = 0; //stores time when user clicks pause button

    RecordingService mRecordingService;

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment Record_Fragment.
     */
    public static RecordFragment newInstance() {
        RecordFragment f = new RecordFragment();
        Bundle b = new Bundle();
        f.setArguments(b);

        return f;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View recordView = inflater.inflate(R.layout.fragment_record, container, false);

        mChronometer = recordView.findViewById(R.id.chronometer);
        //update recording prompt text
        mRecordingPrompt = recordView.findViewById(R.id.recording_status_text);

        mRecordButton = recordView.findViewById(R.id.btnRecord);
        mRecordButton.setColorNormal(getResources().getColor(R.color.primary));
        mRecordButton.setColorPressed(getResources().getColor(R.color.primary_dark));
        mRecordButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mStartRecording = onRecord(mStartRecording);
            }
        });

        mPauseButton = recordView.findViewById(R.id.btnPause);
        mPauseButton.setVisibility(View.GONE); //hide pause button before recording starts
        mPauseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mPauseRecording = onPauseRecord(mPauseRecording);
            }
        });

        return recordView;
    }

    /**
     * Defines callbacks for service binding, passed to bindService()
     */
    private ServiceConnection mConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className,
                                       IBinder service) {
            // We've bound to LocalService, cast the IBinder and get LocalService instance
            RecordingService.LocalBinder binder = (RecordingService.LocalBinder) service;
            mRecordingService = binder.getService();

        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {

        }
    };

    // Recording Start/Stop
    private boolean onRecord(boolean start) {
        final Intent intent = new Intent(getActivity(), RecordingService.class);

        if (start) {
            if (!PermissionsHelper.checkAndRequestPermissions(this,
                    MY_PERMISSIONS_REQUEST_RECORD_AUDIO)) {
                return start;
            }
            startRecording(intent);
        } else {
            //stop recording
            mRecordButton.setImageResource(R.drawable.ic_mic_white_36dp);
            mPauseButton.setVisibility(View.GONE);
            mChronometer.stop();
            mChronometer.setBase(SystemClock.elapsedRealtime());
            timeWhenPaused = 0;
            mRecordingPrompt.setText(getString(R.string.record_prompt));

            //handle case : user press stop after pause
            if (!mPauseRecording) mPauseRecording = true;
            getActivity().stopService(intent);
            getActivity().unbindService(mConnection);

            //allow the screen to turn off again once recording is finished
            getActivity().getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        }

        return !start;
    }

    private void startRecording(Intent intent) {
        // start recording
        mRecordButton.setImageResource(R.drawable.ic_media_stop);
        mPauseButton.setVisibility(View.VISIBLE);
        Toast.makeText(getActivity(), R.string.toast_recording_start, Toast.LENGTH_SHORT).show();
        File folder = new File(Environment.getExternalStorageDirectory(),
                Paths.SOUND_RECORDER_FOLDER);
        if (!folder.exists()) {
            //folder /SoundRecorder doesn't exist, create the folder
            folder.mkdir();
        }

        //start Chronometer
        mChronometer.setBase(SystemClock.elapsedRealtime());
        mChronometer.start();
        mChronometer.setOnChronometerTickListener(new Chronometer.OnChronometerTickListener() {
            @Override
            public void onChronometerTick(Chronometer chronometer) {
                if (mRecordPromptCount == 0) {
                    mRecordingPrompt.setText(getString(R.string.record_in_progress) + ".");
                } else if (mRecordPromptCount == 1) {
                    mRecordingPrompt.setText(getString(R.string.record_in_progress) + "..");
                } else if (mRecordPromptCount == 2) {
                    mRecordingPrompt.setText(getString(R.string.record_in_progress) + "...");
                    mRecordPromptCount = -1;
                }

                mRecordPromptCount++;
            }
        });

        //start RecordingService
        getActivity().startService(intent);
        getActivity().bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
        //keep screen on while recording
        getActivity().getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        mRecordingPrompt.setText(getString(R.string.record_in_progress) + ".");
        mRecordPromptCount++;

        mStartRecording = false;
    }

    private boolean onPauseRecord(boolean pause) {
        if (pause) {
            //pause recording
            mRecordingService.pauseRecording();
            mPauseButton.setCompoundDrawablesWithIntrinsicBounds
                    (R.drawable.ic_media_play, 0, 0, 0);
            mPauseButton.setText(getString(R.string.resume_recording_button).toUpperCase());
            mRecordingPrompt.setText(getString(R.string.record_paused));
            timeWhenPaused = mChronometer.getBase() - SystemClock.elapsedRealtime();
            mChronometer.stop();
        } else {
            if (!PermissionsHelper.checkAndRequestPermissions(this,
                    MY_PERMISSIONS_REQUEST_RECORD_AUDIO)) {
                return pause;
            }
            resumeRecording();
        }

        return !pause;
    }

    private void resumeRecording() {
        mRecordingService.resumeRecording();
        mPauseButton.setCompoundDrawablesWithIntrinsicBounds
                (R.drawable.ic_media_pause, 0, 0, 0);
        mPauseButton.setText(getString(R.string.pause_recording_button).toUpperCase());
        mRecordingPrompt.setText(getString(R.string.record_in_progress));
        mChronometer.setBase(SystemClock.elapsedRealtime() + timeWhenPaused);
        mChronometer.start();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_RECORD_AUDIO: {
                // If request is cancelled, the result arrays are empty.
                if (allPermissionsGranted(grantResults)) {
                    // permissions were granted, yay!
                    final Intent intent = new Intent(getActivity(), RecordingService.class);
                    startRecording(intent);
                    mStartRecording = false;
                } else {
                    Snackbar.make(getActivity().findViewById(android.R.id.content),
                            R.string.error_no_permission_granted_record, Snackbar.LENGTH_SHORT)
                            .show();
                }
                break;
            }

            case MY_PERMISSIONS_REQUEST_RECORD_AUDIO_RESUME: {
                // If request is cancelled, the result arrays are empty.
                if (allPermissionsGranted(grantResults)) {
                    // permission was granted, yay!
                    resumeRecording();
                    mPauseRecording = true;
                } else {
                    Snackbar.make(getActivity().findViewById(android.R.id.content),
                            R.string.error_no_permission_granted_record, Snackbar.LENGTH_LONG)
                            .show();
                }
                break;
            }
        }
    }

    private boolean allPermissionsGranted(int[] grantResults) {
        if (grantResults == null || grantResults.length == 0)
            return false;

        boolean ok = true;
        for (int grantResult : grantResults) {
            ok &= (grantResult == PackageManager.PERMISSION_GRANTED);
        }
        return ok;
    }

}