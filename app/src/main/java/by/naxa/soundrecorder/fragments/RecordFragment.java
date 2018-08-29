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
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Chronometer;
import android.widget.TextView;

import com.budiyev.android.circularprogressbar.CircularProgressBar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.io.File;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import by.naxa.soundrecorder.R;
import by.naxa.soundrecorder.RecorderState;
import by.naxa.soundrecorder.services.RecordingService;
import by.naxa.soundrecorder.util.EventBroadcaster;
import by.naxa.soundrecorder.util.Paths;
import by.naxa.soundrecorder.util.PermissionsHelper;
import by.naxa.soundrecorder.util.ScreenLock;

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
    private MaterialButton mPauseButton = null;
    private boolean isRecordButtonInState1 = true;  // true = record, false = stop
    private boolean isPauseButtonInState1 = true;   // true = pause, false = resume

    // ProgressBar around Chronometer
    private CircularProgressBar mProgressBar;

    private TextView mRecordingPrompt;
    private int mRecordPromptCount = 0;

    private Chronometer mChronometer = null;
    long timeWhenPaused = 0; //stores time when user clicks pause button

    private RecordingService mRecordingService;

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
        tryBindService();
    }

    private void tryBindService() {
        if (mRecordingService == null) {
            final Context context = getContext();
            if (context == null)
                return;
            final Intent intent = new Intent(context, RecordingService.class);
            context.bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View recordView = inflater.inflate(R.layout.fragment_record, container, false);

        mChronometer = recordView.findViewById(R.id.chronometer);
        //update recording prompt text
        mRecordingPrompt = recordView.findViewById(R.id.recording_status_text);

        mRecordButton = recordView.findViewById(R.id.btnRecord);
        mRecordButton.setOnClickListener(createRecordButtonClickListener());

        mProgressBar = recordView.findViewById(R.id.recordProgressBar);
        mPauseButton = recordView.findViewById(R.id.btnPause);
        mPauseButton.setVisibility(View.GONE); //hide pause button before recording starts
        mPauseButton.setOnClickListener(createPauseButtonClickListener());

        return recordView;
    }

    private final Chronometer.OnChronometerTickListener listener = new Chronometer.OnChronometerTickListener() {
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

            ++mRecordPromptCount;
        }
    };

    private View.OnClickListener createPauseButtonClickListener() {
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isPauseButtonInState1) {
                    // pause recording
                    mRecordingService.pauseRecording();
                    long chronometerTime = SystemClock.elapsedRealtime() - mRecordingService.getTotalDurationMillis();
                    updateUI(RecorderState.PAUSED, chronometerTime);
                } else {
                    if (PermissionsHelper.checkAndRequestPermissions(RecordFragment.this,
                            MY_PERMISSIONS_REQUEST_RECORD_AUDIO)) {
                        resumeRecording();
                    }
                }

            }
        };
    }

    private View.OnClickListener createRecordButtonClickListener() {
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Intent intent = new Intent(getActivity(), RecordingService.class);
                if (isRecordButtonInState1) {
                    // start recording
                    if (PermissionsHelper.checkAndRequestPermissions(
                            RecordFragment.this, MY_PERMISSIONS_REQUEST_RECORD_AUDIO)) {
                        startRecording(intent);
                    }
                } else {
                    // stop recording
                    final FragmentActivity activity = getActivity();
                    if (activity == null) {
                        Log.wtf(LOG_TAG, "RecordFragment failed to stop recording, getActivity() returns null.");
                        return;
                    }
                    activity.stopService(intent);
                    activity.unbindService(mConnection);

                    updateUI(RecorderState.STOPPED, SystemClock.elapsedRealtime());

                    ScreenLock.allowScreenTurnOff(activity);
                }
            }
        };
    }

    private void updateUI(RecorderState state, long chronometerBaseTime) {
        if (getActivity() == null || !isAdded()) {
            Log.i(LOG_TAG, "RecordFragment#updateUI: RecordFragment is not attached to an Activity");
            return;
        }
        Log.i(LOG_TAG, "RecordFragment#updateUI: new state is " + state + ", time is " + chronometerBaseTime + " ms");

        switch (state) {
            case STOPPED:
                mRecordButton.setImageResource(R.drawable.ic_mic_white_36dp);
                mPauseButton.setVisibility(View.GONE);
                timeWhenPaused = 0;
                mRecordingPrompt.setText(getString(R.string.record_prompt));

                isPauseButtonInState1 = true;
                isRecordButtonInState1 = true;

                mChronometer.setOnChronometerTickListener(null);
                mChronometer.setBase(SystemClock.elapsedRealtime());
                mChronometer.stop();

                mProgressBar.setIndeterminate(false);
                break;

            case RECORDING:
                mRecordButton.setImageResource(R.drawable.ic_media_stop);
                mPauseButton.setCompoundDrawablesWithIntrinsicBounds
                        (R.drawable.ic_media_pause, 0, 0, 0);
                mPauseButton.setText(getString(R.string.pause_recording_button).toUpperCase());
                mPauseButton.setVisibility(View.VISIBLE);
                mRecordingPrompt.setText(getString(R.string.record_in_progress));

                isPauseButtonInState1 = true;
                isRecordButtonInState1 = false;

                mChronometer.setBase(chronometerBaseTime);
                mChronometer.setOnChronometerTickListener(listener);
                mChronometer.start();

                mProgressBar.setIndeterminate(true);
                break;

            case PAUSED:
                mRecordButton.setImageResource(R.drawable.ic_media_stop);
                mPauseButton.setCompoundDrawablesWithIntrinsicBounds
                        (R.drawable.ic_media_play, 0, 0, 0);
                mPauseButton.setText(getString(R.string.resume_recording_button).toUpperCase());
                mPauseButton.setVisibility(View.VISIBLE);
                mRecordingPrompt.setText(getString(R.string.record_paused));

                isPauseButtonInState1 = false;
                isRecordButtonInState1 = false;

                mChronometer.setOnChronometerTickListener(null);
                mChronometer.setBase(chronometerBaseTime);
                mChronometer.stop();

                mProgressBar.setIndeterminate(false);
                break;
        }
    }

    /**
     * Defines callbacks for service binding, passed to bindService()
     */
    private ServiceConnection mConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {
            // We've bound to LocalService, cast the IBinder and get LocalService instance
            RecordingService.LocalBinder binder = (RecordingService.LocalBinder) service;
            mRecordingService = binder.getService();
            Log.i(LOG_TAG, "RecordFragment ServiceConnection#onServiceConnected");

            long chronometerTime = SystemClock.elapsedRealtime() - mRecordingService.getTotalDurationMillis();
            updateUI(mRecordingService.getState(), chronometerTime);
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            Log.i(LOG_TAG, "RecordFragment ServiceConnection#onServiceDisconnected");
            mRecordingService = null;
        }
    };

    /**
     * Start recording
     */
    private boolean startRecording(Intent intent) {
        final File folder = new File(
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC),
                Paths.SOUND_RECORDER_FOLDER);
        if (!folder.exists()) {
            // a folder for sound recordings doesn't exist -> create the folder
            boolean ok = Paths.isExternalStorageWritable() && folder.mkdir();
            if (!ok) {
                EventBroadcaster.send(getContext(), R.string.error_mkdir);
                return false;
            }
        }

        updateUI(RecorderState.RECORDING, SystemClock.elapsedRealtime());

        final FragmentActivity activity = getActivity();
        if (activity == null) {
            Log.wtf(LOG_TAG, "RecordFragment#startRecording(): failed to start recording, getActivity() returns null.");
            return false;
        }

        //start RecordingService
        activity.startService(intent);
        activity.bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
        ScreenLock.keepScreenOn(activity);

        return true;
    }

    private void resumeRecording() {
        long chronometerTime = SystemClock.elapsedRealtime() - mRecordingService.getTotalDurationMillis();
        mRecordingService.startRecording();
        updateUI(RecorderState.RECORDING, chronometerTime);
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
                } else {
                    EventBroadcaster.send(getContext(), R.string.error_no_permission_granted_record);
                }
                break;
            }

            case MY_PERMISSIONS_REQUEST_RECORD_AUDIO_RESUME: {
                // If request is cancelled, the result arrays are empty.
                if (allPermissionsGranted(grantResults)) {
                    // permission was granted, yay!
                    resumeRecording();
                } else {
                    EventBroadcaster.send(getContext(), R.string.error_no_permission_granted_record);
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