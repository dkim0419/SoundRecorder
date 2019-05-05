package com.danielkim.soundrecorder.fragments;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.SystemClock;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.Transformation;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.TextView;
import android.widget.Toast;

import com.danielkim.soundrecorder.R;
import com.danielkim.soundrecorder.RecordingService;
import com.melnykov.fab.FloatingActionButton;

import java.io.File;

public class RecordFragment extends Fragment implements View.OnClickListener {
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_POSITION = "position";
    private static final String LOG_TAG = RecordFragment.class.getSimpleName();
    private static final int PERMISSIONS_REQUEST_RECORD_AUDIO_AND_WRITE_EXTERNAL = 11069;
    private static final int PERMISSIONS_REQUEST_RECORD_AUDIO = 11070;
    private static final int PERMISSIONS_REQUEST_WRITE_EXTERNAL = 11071;

    private int position;

    //Recording controls
    private View mRecordButton = null;
    private AppCompatImageView mPauseButton = null;
    private View mStopButton = null;

    private TextView mRecordingPrompt;
    private int mRecordPromptCount = 0;

    private boolean isPaused = false;
    private Intent intent;

    private Chronometer mChronometer = null;
    long timeWhenPaused = 0; //stores time when user clicks pause button


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

        mChronometer = recordView.findViewById(R.id.chronometer);
        //update recording prompt text
        mRecordingPrompt = recordView.findViewById(R.id.recording_status_text);
        mRecordButton = recordView.findViewById(R.id.btnRecord);
        mPauseButton = recordView.findViewById(R.id.btnPause);
        mStopButton = recordView.findViewById(R.id.btnStop);

        mRecordButton.setOnClickListener(this);
        mPauseButton.setOnClickListener(this);
        mStopButton.setOnClickListener(this);

        mStopButton.setVisibility(View.GONE);
        mPauseButton.setVisibility(View.GONE);

        return recordView;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.btnRecord:
                startRecording();
                break;

            case R.id.btnPause:
                if (isPaused){
                    resumeRecording();

                }else{
                    pauseRecording();

                }
                break;

            case R.id.btnStop:
                stopRecording();
                break;
        }
    }


    private void startRecording(){

        if ((ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.RECORD_AUDIO)!= PackageManager.PERMISSION_GRANTED)&&
                (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE)!= PackageManager.PERMISSION_GRANTED)) {
            requestPermissions(new String[]{Manifest.permission.RECORD_AUDIO,Manifest.permission.WRITE_EXTERNAL_STORAGE}, PERMISSIONS_REQUEST_RECORD_AUDIO_AND_WRITE_EXTERNAL);
        }else if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.RECORD_AUDIO)!= PackageManager.PERMISSION_GRANTED){
            requestPermissions(new String[]{Manifest.permission.RECORD_AUDIO}, PERMISSIONS_REQUEST_RECORD_AUDIO);
        }else if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE)!= PackageManager.PERMISSION_GRANTED){
            requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, PERMISSIONS_REQUEST_WRITE_EXTERNAL);
        }else{

            if(intent == null){
                intent = new Intent(getContext(),RecordingService.class);
            }

//            Toast.makeText(getActivity(),R.string.toast_recording_start,Toast.LENGTH_SHORT).show();
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
            isPaused = false;
            expand(mPauseButton);
            expand(mStopButton);
            collapse(mRecordButton);

        }


    }
    private void stopRecording(){
        //stop recording
        mChronometer.stop();
        mChronometer.setBase(SystemClock.elapsedRealtime());
        timeWhenPaused = 0;
        mRecordingPrompt.setText(getString(R.string.record_prompt));

        getActivity().stopService(intent);
        //allow the screen to turn off again once recording is finished
        getActivity().getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        isPaused = false;
        expand(mRecordButton);
        collapse(mPauseButton);
        collapse(mStopButton);

    }

    private void pauseRecording(){
        //pause recording
        mRecordingPrompt.setText(getString(R.string.resume_recording_button).toUpperCase());
        timeWhenPaused = mChronometer.getBase() - SystemClock.elapsedRealtime();
        mChronometer.stop();
        isPaused = true;
        mPauseButton.setImageResource(R.drawable.ic_play);
        mPauseButton.setColorFilter(ContextCompat.getColor(getContext(), R.color.primary), android.graphics.PorterDuff.Mode.SRC_IN);
    }

    private void resumeRecording(){
        //resume recording
        mRecordingPrompt.setText(getString(R.string.pause_recording_button).toUpperCase());
        mChronometer.setBase(SystemClock.elapsedRealtime() + timeWhenPaused);
        mChronometer.start();
        isPaused = false;
        mPauseButton.setImageResource(R.drawable.ic_pause);
        mPauseButton.setColorFilter(ContextCompat.getColor(getContext(), R.color.primary), android.graphics.PorterDuff.Mode.SRC_IN);
    }



    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode){

            case PERMISSIONS_REQUEST_RECORD_AUDIO_AND_WRITE_EXTERNAL:

                if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED){
                    startRecording();
                }
                break;
            case PERMISSIONS_REQUEST_RECORD_AUDIO:

                if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    startRecording();
                }
                break;
            case PERMISSIONS_REQUEST_WRITE_EXTERNAL:

                if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    startRecording();
                }
                break;
        }
    }


    public void expand(final View v) {
        v.measure(getResources().getDimensionPixelOffset(R.dimen.button_size), getResources().getDimensionPixelOffset(R.dimen.button_size));
        final int targetwidth = v.getMeasuredWidth();
        // Older versions of android (pre API 21) cancel animations for views with a height of 0.
        v.getLayoutParams().width = 1;
        v.setVisibility(View.VISIBLE);
        Animation a = new Animation() {
            @Override
            protected void applyTransformation(float interpolatedTime, Transformation t) {
                v.getLayoutParams().width = interpolatedTime == 1
                        ? getResources().getDimensionPixelOffset(R.dimen.button_size)
                        : (int) (targetwidth * interpolatedTime);
                v.requestLayout();
            }

            @Override
            public boolean willChangeBounds() {
                return true;
            }
        };

        // 1dp/ms
        a.setDuration(500);
        v.startAnimation(a);
    }

    public static void collapse(final View v) {
        final int initalWidth = v.getMeasuredWidth();

        Animation a = new Animation() {
            @Override
            protected void applyTransformation(float interpolatedTime, Transformation t) {
                if (interpolatedTime == 1) {
                    v.setVisibility(View.GONE);
                } else {
                    v.getLayoutParams().width = initalWidth - (int) (initalWidth * interpolatedTime);
                    v.requestLayout();
                }
            }
            @Override
            public boolean willChangeBounds() {
                return true;
            }
        };
        a.setDuration(500);
        v.startAnimation(a);
    }
}