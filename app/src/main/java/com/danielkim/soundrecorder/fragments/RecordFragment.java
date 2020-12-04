package com.danielkim.soundrecorder.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.os.SystemClock;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.TextView;
import android.widget.Toast;

import com.danielkim.soundrecorder.R;
import com.danielkim.soundrecorder.RecordingService;
import com.melnykov.fab.FloatingActionButton;

import java.io.File;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * to handle interaction events.
 * Use the {@link RecordFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class RecordFragment extends Fragment {
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_POSITION = "position";
    // private static final String LOG_TAG = RecordFragment.class.getSimpleName();

    private int position;

    //Recording controls
    private FloatingActionButton mRecordButton = null;
    private Button mPauseButton = null;

    private TextView mRecordingPrompt;
    private int mRecordPromptCount = 0;

    private boolean mStartRecording = true;
    private boolean mPauseRecording = true;

    private Chronometer mChronometer = null;
    long timeWhenPaused = 0; //stores time when user clicks pause button
    long pauseStart, pauseStop, totalPause = 0; //stores start, stop, total pause time
    private boolean inPause = false; //handles when user stops while in pause

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment Record_Fragment.
     */
    public static RecordFragment newInstance(int position) {
        RecordFragment f = new RecordFragment();
        Bundle b = new Bundle();
        b.putInt(ARG_POSITION, position);
        f.setArguments(b);

        return f;
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        position = getArguments().getInt(ARG_POSITION);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View recordView = inflater.inflate(R.layout.fragment_record, container, false);

        mChronometer = (Chronometer) recordView.findViewById(R.id.chronometer);
        //update recording prompt text
        mRecordingPrompt = (TextView) recordView.findViewById(R.id.recording_status_text);

        mRecordButton = (FloatingActionButton) recordView.findViewById(R.id.btnRecord);
        mRecordButton.setColorNormal(getResources().getColor(R.color.primary));
        mRecordButton.setColorPressed(getResources().getColor(R.color.primary_dark));
        mRecordButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onRecord(mStartRecording);
                mStartRecording = !mStartRecording;
            }
        });

        mPauseButton = (Button) recordView.findViewById(R.id.btnPause);
        mPauseButton.setVisibility(View.GONE); //hide pause button before recording starts
        mPauseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onPauseRecord(mPauseRecording);
                mPauseRecording = !mPauseRecording;
            }
        });

        return recordView;
    }

    // Recording Start/Stop
    private void onRecord(boolean start){
        Intent intent = new Intent(getActivity(), RecordingService.class);
        if (start) {
            Log.d("Flow", "onRecording (true)");

            // start recording
            mRecordButton.setImageResource(R.drawable.ic_media_stop);
            mPauseButton.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_media_pause,0,0,0);

            mPauseButton.setVisibility(View.VISIBLE);
            Toast.makeText(getActivity(),R.string.toast_recording_start,Toast.LENGTH_SHORT).show();
            File folder = new File(Environment.getExternalStorageDirectory() + "/SoundRecorder");
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
            //keep screen on while recording
            getActivity().getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
            mRecordingPrompt.setText(getString(R.string.record_in_progress) + ".");
            mRecordPromptCount++;

        } else {
            //stop recording
            Log.d("Flow", "onRecording (false)");
            mRecordButton.setImageResource(R.drawable.ic_mic_white_36dp);
            mPauseButton.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_media_pause,0,0,0);
            mPauseButton.setVisibility(View.GONE);
            mPauseRecording = true;

            mChronometer.stop();
            mChronometer.setBase(SystemClock.elapsedRealtime());

            if(inPause){
                pauseStop = System.currentTimeMillis();
                totalPause += pauseStart - pauseStop;
            }
            Log.d("Flow - paused", "total paused: " + totalPause);
            //Intent updateIntent = new Intent(getActivity(), RecordingService.class);
            //intent.putExtra("timeWhenPaused", totalPause);
            totalPause = 0;

            //getActivity().startService(intent);
            mRecordingPrompt.setText(getString(R.string.record_prompt));
            getActivity().stopService(intent);
            //allow the screen to turn off again once recording is finished
            getActivity().getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        }
    }

    private void onPauseRecord(boolean pause) {
        if (pause) {
            Log.d("Flow", "onPauseRecord (true)");
            mPauseButton.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_media_play, 0,0,0);
            mRecordingPrompt.setText((String)getString(R.string.resume_recording_button).toUpperCase());
            timeWhenPaused = SystemClock.elapsedRealtime() - mChronometer.getBase();
            mChronometer.stop();

            pauseStart = System.currentTimeMillis();
            inPause = true;
        }else {
            pauseStop = System.currentTimeMillis();
            totalPause += pauseStart - pauseStop;
            inPause = false;

            Log.d("Flow", "onPauseRecord (false)");
            Log.d("Flow - paused", "total paused: " + totalPause);

            mPauseButton.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_media_pause,0,0,0);
            mRecordingPrompt.setText((String)getString(R.string.pause_recording_button).toUpperCase());
            mChronometer.setBase(SystemClock.elapsedRealtime() - timeWhenPaused);
            mChronometer.start();
        }

        Intent intent = new Intent(getActivity(), RecordingService.class);
        intent.putExtra("inPause", inPause);
        getActivity().startService(intent);
    }
}








