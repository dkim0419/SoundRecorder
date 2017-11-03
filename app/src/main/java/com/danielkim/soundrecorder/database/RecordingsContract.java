/*
 * Year: 2017. This class was added by iClaude.
 */

package com.danielkim.soundrecorder.database;

import android.provider.BaseColumns;

/**
 * Contract class for all operations related to database.
 */

public class RecordingsContract {

    public static class TableSavedRecording implements BaseColumns {
        public static final String TABLE_NAME = "saved_recordings";

        public static final String COLUMN_NAME_RECORDING_NAME = "recording_name";
        public static final String COLUMN_NAME_RECORDING_FILE_PATH = "file_path";
        public static final String COLUMN_NAME_RECORDING_LENGTH = "length";
        public static final String COLUMN_NAME_TIME_ADDED = "time_added";
    }

    // Table "scheduled_recordings".
    public static class TableScheduledRecording implements BaseColumns {
        public static final String TABLE_NAME = "scheduled_recordings";

        public static final String COLUMN_NAME_START = "start"; // start of the recording in ms from epoch
        public static final String COLUMN_NAME_END = "end"; // length of the recording in ms
    }

    // Requirements.
    public static final int MIN_DURATION = 1000 * 60 * 5; // 5 minutes
    public static final int MAX_DURATION = 1000 * 60 * 60 * 3; // 3 hours

    private RecordingsContract() {
    }
}
