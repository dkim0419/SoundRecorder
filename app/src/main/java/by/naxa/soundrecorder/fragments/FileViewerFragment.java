package by.naxa.soundrecorder.fragments;

import android.os.Bundle;
import android.os.Environment;
import android.os.FileObserver;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import by.naxa.soundrecorder.R;
import by.naxa.soundrecorder.adapters.FileViewerAdapter;
import by.naxa.soundrecorder.util.Paths;

/**
 * Created by Daniel on 12/23/2014.
 */
public class FileViewerFragment extends Fragment {
    private static final String LOG_TAG = "FileViewerFragment";

    private FileViewerAdapter mFileViewerAdapter;
    private RecyclerView mRecyclerView;
    private TextView mTextView;
    private RecyclerView.AdapterDataObserver adapterDataObserver;

    public static FileViewerFragment newInstance() {
        FileViewerFragment f = new FileViewerFragment();
        Bundle b = new Bundle();
        f.setArguments(b);

        return f;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        observer.startWatching();
    }

    @Override
    public void onDestroy() {
        observer.stopWatching();
        super.onDestroy();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_file_viewer, container, false);

        mRecyclerView = v.findViewById(R.id.recyclerView);
        mTextView = v.findViewById(R.id.noRecordView);

        mRecyclerView.setHasFixedSize(true);
        //newest to oldest order (database stores from oldest to newest)
        final LinearLayoutManager llm = new LinearLayoutManager(
                getActivity(), RecyclerView.VERTICAL, true);
        llm.setStackFromEnd(true);

        mRecyclerView.setLayoutManager(llm);
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());

        mFileViewerAdapter = new FileViewerAdapter(getActivity(), llm);
        mRecyclerView.setAdapter(mFileViewerAdapter);
        changeVisibilityRecycleView();
        adapterDataObserver = new RecyclerView.AdapterDataObserver() {

            @Override
            public void onItemRangeInserted(int positionStart, int itemCount) {
                changeVisibilityRecycleView();
                super.onItemRangeInserted(positionStart, itemCount);
            }

            @Override
            public void onItemRangeRemoved(int positionStart, int itemCount) {
                changeVisibilityRecycleView();
                super.onItemRangeRemoved(positionStart, itemCount);
            }
        };

        mFileViewerAdapter.registerAdapterDataObserver(adapterDataObserver);
        return v;
    }

    /**
     *   Change visibility of RecycleView if no item is present(from {@link FileViewerAdapter})
     *   and show TextView with information about no available item. If any item is present,
     *   visibility for RecycleView is set to visible and for TextView to gone.
     */
    private void changeVisibilityRecycleView() {
        if (mFileViewerAdapter.getItemCount() == 0) {
            mRecyclerView.setVisibility(View.GONE);
            mTextView.setVisibility(View.VISIBLE);
        } else if(mRecyclerView.getVisibility() != View.VISIBLE) {
            mRecyclerView.setVisibility(View.VISIBLE);
            mTextView.setVisibility(View.GONE);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mFileViewerAdapter.unregisterAdapterDataObserver(adapterDataObserver);
        mFileViewerAdapter = null;
    }

    private final FileObserver observer =
            new FileObserver(Paths.combine(
                    Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC),
                    Paths.SOUND_RECORDER_FOLDER)) {
                // set up a file observer to watch this directory on sd card
                @Override
                public void onEvent(int event, String file) {
                    if (event == FileObserver.DELETE) {
                        // user deletes a recording file out of the app
                        final String filePath = Paths.combine(
                                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC),
                                Paths.SOUND_RECORDER_FOLDER, file);
                        Log.d(LOG_TAG, "File deleted [" + filePath + "]");

                        // remove file from database and recyclerview
                        mFileViewerAdapter.removeOutOfApp(filePath);
                    }
                }
            };
}




