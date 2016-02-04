package com.danielkim.soundrecorder;

import android.media.MediaMetadataRetriever;
import android.util.Log;

import java.io.File;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by Daniel on 12/30/2014.
 */
public class RecordingItem {
    private static final String LOG_TAG = "RecordingItem";
    private String mName; // file name
    private String mFilePath; //file path
    private int mLength; // length of recording in seconds
    private long mTime; // date/time of the recording

    public RecordingItem()
    {
    }

    public RecordingItem(String filePath) {
        mFilePath = filePath;
        MediaMetadataRetriever m = new MediaMetadataRetriever();
        m.setDataSource(mFilePath);
        mName = new File(mFilePath).getName();
        mLength = Integer.parseInt(m.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION));
        String date_str = m.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DATE);
        try {
            Date date = DateFormat.getDateTimeInstance().parse(date_str);
            mTime = date.getTime();
        } catch (ParseException e) {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd'T'HHmmss.SSS'Z'");
            try {
                sdf.parse(date_str);
                mTime = sdf.getCalendar().getTimeInMillis();
            } catch (ParseException _e) {
                Log.e(LOG_TAG, "can't parse date: " + date_str);
                mTime = 0;
            }
        }
    }

    public String getFilePath() {
        return mFilePath;
    }

    public void setFilePath(String filePath) {
        mFilePath = filePath;
        mName = new File(mFilePath).getName();
    }

    public int getLength() {
        return mLength;
    }

    public String getName() {
        return mName;
    }

    public long getTime() {
        return mTime;
    }
}