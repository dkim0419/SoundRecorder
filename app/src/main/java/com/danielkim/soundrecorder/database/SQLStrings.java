/*
 * Copyright (c) 2017 Claudio "iClaude" Agostini <agostini.claudio1@gmail.com>
 * Licensed under the Apache License, Version 2.0
 */

package com.danielkim.soundrecorder.database;

/**
 * Class containing strings with SQL commands for the database.
 */

public interface SQLStrings {
    // Utility.
    String TEXT_TYPE = " TEXT";
    String INTEGER_TYPE = " INTEGER";
    String COMMA_SEP = ", ";

    // Table "saved_recordings": create and delete.
    String CREATE_TABLE_SAVED_RECORDINGS =
            "CREATE TABLE " + RecordingsContract.TableSavedRecording.TABLE_NAME + " (" +
                    RecordingsContract.TableSavedRecording._ID + " INTEGER PRIMARY KEY" + COMMA_SEP +
                    RecordingsContract.TableSavedRecording.COLUMN_NAME_RECORDING_NAME + TEXT_TYPE + COMMA_SEP +
                    RecordingsContract.TableSavedRecording.COLUMN_NAME_RECORDING_FILE_PATH + TEXT_TYPE + COMMA_SEP +
                    RecordingsContract.TableSavedRecording.COLUMN_NAME_RECORDING_LENGTH + INTEGER_TYPE + COMMA_SEP +
                    RecordingsContract.TableSavedRecording.COLUMN_NAME_TIME_ADDED + INTEGER_TYPE + ")";

    @SuppressWarnings("unused")
    String DELETE_TABLE_SAVED_RECORDINGS = "DROP TABLE IF EXISTS " + RecordingsContract.TableSavedRecording.TABLE_NAME;

    // Table "scheduled_recordings": create and delete.
    String CREATE_TABLE_SCHEDULED_RECORDINGS =
            "CREATE TABLE " + RecordingsContract.TableScheduledRecording.TABLE_NAME + " (" +
                    RecordingsContract.TableScheduledRecording._ID + " INTEGER PRIMARY KEY AUTOINCREMENT" + COMMA_SEP +
                    RecordingsContract.TableScheduledRecording.COLUMN_NAME_START + INTEGER_TYPE + COMMA_SEP +
                    RecordingsContract.TableScheduledRecording.COLUMN_NAME_END + INTEGER_TYPE + ")";

    @SuppressWarnings("unused")
    String DELETE_TABLE_SCHEDULED_RECORDINGS = "DROP TABLE IF EXISTS " + RecordingsContract.TableScheduledRecording.TABLE_NAME;


}
