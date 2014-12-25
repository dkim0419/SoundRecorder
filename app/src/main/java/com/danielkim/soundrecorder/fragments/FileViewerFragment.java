package com.danielkim.soundrecorder.fragments;

import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.danielkim.soundrecorder.R;

/**
 * Created by Daniel on 12/23/2014.
 */
public class FileViewerFragment extends Fragment{
    private static final String ARG_POSITION = "position";

    private int position;

    public static FileViewerFragment newInstance(int position) {
        FileViewerFragment f = new FileViewerFragment();
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
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View playView = inflater.inflate(R.layout.fragment_file_viewer, container, false);
        return playView;
    }
}
