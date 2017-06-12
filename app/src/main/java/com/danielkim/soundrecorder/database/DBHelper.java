package com.danielkim.soundrecorder.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.danielkim.soundrecorder.RecordingItem;
import com.danielkim.soundrecorder.ScheduledRecordingItem;
import com.danielkim.soundrecorder.listeners.OnDatabaseChangedListener;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import static com.danielkim.soundrecorder.database.RecordingsContract.TableSavedRecording;
import static com.danielkim.soundrecorder.database.RecordingsContract.TableScheduledRecording;
import static com.danielkim.soundrecorder.database.SQLStrings.CREATE_TABLE_SAVED_RECORDINGS;
import static com.danielkim.soundrecorder.database.SQLStrings.CREATE_TABLE_SCHEDULED_RECORDINGS;
import static com.danielkim.soundrecorder.database.SQLStrings.DELETE_TABLE_SAVED_RECORDINGS;
import static com.danielkim.soundrecorder.database.SQLStrings.DELETE_TABLE_SCHEDULED_RECORDINGS;

/**
 * Created by Daniel on 12/29/2014.
 * Updated by iClaude on 6/12/2017.
 */
public class DBHelper extends SQLiteOpenHelper {
    private final Context mContext;
    private static OnDatabaseChangedListener mOnDatabaseChangedListener;

    private static final String LOG_TAG = "DBHelper";
    private static final String DATABASE_NAME = "saved_recordings.db";
    private static final int DATABASE_VERSION = 2;


    public DBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        mContext = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE_SAVED_RECORDINGS);
        db.execSQL(CREATE_TABLE_SCHEDULED_RECORDINGS);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion == 1) { // table scheduled_recordings was added with version 2
            db.execSQL(CREATE_TABLE_SCHEDULED_RECORDINGS);
        }
    }

    /*
        Delete all tables and create an empty database.
     */
    public void restoreDatabase() {
        SQLiteDatabase db = getWritableDatabase();
        db.execSQL(DELETE_TABLE_SAVED_RECORDINGS);
        db.execSQL(DELETE_TABLE_SCHEDULED_RECORDINGS);
        db.execSQL(CREATE_TABLE_SAVED_RECORDINGS);
        db.execSQL(CREATE_TABLE_SCHEDULED_RECORDINGS);
    }

    public static void setOnDatabaseChangedListener(OnDatabaseChangedListener listener) {
        mOnDatabaseChangedListener = listener;
    }

    // Table "saved_recordings".
    public long addRecording(String recordingName, String filePath, long length) {

        SQLiteDatabase db = getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(TableSavedRecording.COLUMN_NAME_RECORDING_NAME, recordingName);
        cv.put(TableSavedRecording.COLUMN_NAME_RECORDING_FILE_PATH, filePath);
        cv.put(TableSavedRecording.COLUMN_NAME_RECORDING_LENGTH, length);
        cv.put(TableSavedRecording.COLUMN_NAME_TIME_ADDED, System.currentTimeMillis());
        long rowId = db.insert(TableSavedRecording.TABLE_NAME, null, cv);

        if (mOnDatabaseChangedListener != null) {
            mOnDatabaseChangedListener.onNewDatabaseEntryAdded();
        }

        return rowId;
    }

    public void removeRecording(int id) {
        SQLiteDatabase db = getWritableDatabase();
        String[] whereArgs = {String.valueOf(id)};
        db.delete(TableSavedRecording.TABLE_NAME, "_ID=?", whereArgs);
    }

    public RecordingItem getRecording(int position) {
        SQLiteDatabase db = getReadableDatabase();
        String[] projection = {
                TableSavedRecording._ID,
                TableSavedRecording.COLUMN_NAME_RECORDING_NAME,
                TableSavedRecording.COLUMN_NAME_RECORDING_FILE_PATH,
                TableSavedRecording.COLUMN_NAME_RECORDING_LENGTH,
                TableSavedRecording.COLUMN_NAME_TIME_ADDED
        };
        Cursor c = db.query(TableSavedRecording.TABLE_NAME, projection, null, null, null, null, null);
        if (c.moveToPosition(position)) {
            RecordingItem item = new RecordingItem();
            item.setId(c.getInt(c.getColumnIndex(TableSavedRecording._ID)));
            item.setName(c.getString(c.getColumnIndex(TableSavedRecording.COLUMN_NAME_RECORDING_NAME)));
            item.setFilePath(c.getString(c.getColumnIndex(TableSavedRecording.COLUMN_NAME_RECORDING_FILE_PATH)));
            item.setLength(c.getInt(c.getColumnIndex(TableSavedRecording.COLUMN_NAME_RECORDING_LENGTH)));
            item.setTime(c.getLong(c.getColumnIndex(TableSavedRecording.COLUMN_NAME_TIME_ADDED)));
            c.close();
            return item;
        }
        return null;
    }

    public void updateRecording(RecordingItem item, String recordingName, String filePath) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(TableSavedRecording.COLUMN_NAME_RECORDING_NAME, recordingName);
        cv.put(TableSavedRecording.COLUMN_NAME_RECORDING_FILE_PATH, filePath);
        db.update(TableSavedRecording.TABLE_NAME, cv,
                TableSavedRecording._ID + "=" + item.getId(), null);

        if (mOnDatabaseChangedListener != null) {
            mOnDatabaseChangedListener.onDatabaseEntryRenamed();
        }
    }

    public int getSavedRecordingsCount() {
        SQLiteDatabase db = getReadableDatabase();
        String[] projection = {TableSavedRecording._ID};
        Cursor c = db.query(TableSavedRecording.TABLE_NAME, projection, null, null, null, null, null);
        int count = c.getCount();
        c.close();
        return count;
    }

    public Context getContext() {
        return mContext;
    }

    public class RecordingComparator implements Comparator<RecordingItem> {
        public int compare(RecordingItem item1, RecordingItem item2) {
            Long o1 = item1.getTime();
            Long o2 = item2.getTime();
            return o2.compareTo(o1);
        }
    }

    public long restoreRecording(RecordingItem item) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(TableSavedRecording.COLUMN_NAME_RECORDING_NAME, item.getName());
        cv.put(TableSavedRecording.COLUMN_NAME_RECORDING_FILE_PATH, item.getFilePath());
        cv.put(TableSavedRecording.COLUMN_NAME_RECORDING_LENGTH, item.getLength());
        cv.put(TableSavedRecording.COLUMN_NAME_TIME_ADDED, item.getTime());
        cv.put(TableSavedRecording._ID, item.getId());
        long rowId = db.insert(TableSavedRecording.TABLE_NAME, null, cv);
/*        if (mOnDatabaseChangedListener != null) {
            //mOnDatabaseChangedListener.onNewDatabaseEntryAdded();
        }*/
        return rowId;
    }

    // Table "scheduled_recordings".
    public long addScheduledRecording(long start, long end) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(TableScheduledRecording.COLUMN_NAME_START, start);
        cv.put(TableScheduledRecording.COLUMN_NAME_END, end);
        long rowId = db.insert(TableScheduledRecording.TABLE_NAME, null, cv);

        if (mOnDatabaseChangedListener != null) {
            mOnDatabaseChangedListener.onNewDatabaseEntryAdded();
        }

        return rowId;
    }

    public int removeScheduledRecording(long id) {
        SQLiteDatabase db = getWritableDatabase();
        String[] whereArgs = {String.valueOf(id)};
        return db.delete(TableScheduledRecording.TABLE_NAME, "_ID=?", whereArgs);
    }

    public int updateScheduledRecording(long id, long start, long end) {
        int updated;

        SQLiteDatabase db = getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(TableScheduledRecording.COLUMN_NAME_START, start);
        cv.put(TableScheduledRecording.COLUMN_NAME_END, end);
        String[] whereArgs = {String.valueOf(id)};
        updated = db.update(TableScheduledRecording.TABLE_NAME, cv,
                TableScheduledRecording._ID + "= ?", whereArgs);

        if (mOnDatabaseChangedListener != null) {
            mOnDatabaseChangedListener.onDatabaseEntryRenamed();
        }

        return updated;
    }

    public ScheduledRecordingItem getScheduledRecording(long id) {
        SQLiteDatabase db = getReadableDatabase();
        String[] projection = {
                TableScheduledRecording._ID,
                TableScheduledRecording.COLUMN_NAME_START,
                TableScheduledRecording.COLUMN_NAME_END
        };
        String[] whereArgs = {String.valueOf(id)};

        Cursor c = db.query(TableScheduledRecording.TABLE_NAME, projection, TableScheduledRecording._ID + "= ?", whereArgs, null, null, null);
        if (c.moveToFirst()) {
            ScheduledRecordingItem item = new ScheduledRecordingItem();
            item.setId(c.getLong(c.getColumnIndex(TableScheduledRecording._ID)));
            item.setStart(c.getLong(c.getColumnIndex(TableScheduledRecording.COLUMN_NAME_START)));
            item.setEnd(c.getLong(c.getColumnIndex(TableScheduledRecording.COLUMN_NAME_END)));
            c.close();
            return item;
        }
        return null;
    }

    // Returns all scheduled recordings whose field start is between start and end.
    public List<ScheduledRecordingItem> getScheduledRecordingsBetween(long start, long end) {
        SQLiteDatabase db = getReadableDatabase();
        String[] projection = {
                TableScheduledRecording._ID,
                TableScheduledRecording.COLUMN_NAME_START,
                TableScheduledRecording.COLUMN_NAME_END
        };
        String where = TableScheduledRecording.COLUMN_NAME_START + " >= ? AND " + TableScheduledRecording.COLUMN_NAME_START + " <= ?";
        String[] whereArgs = {String.valueOf(start), String.valueOf(end)};

        Cursor c = db.query(TableScheduledRecording.TABLE_NAME, projection, where, whereArgs, null, null, null);
        List<ScheduledRecordingItem> list = new ArrayList<>();
        while (c.moveToNext()) {
            ScheduledRecordingItem item = new ScheduledRecordingItem();
            item.setId(c.getLong(c.getColumnIndex(TableScheduledRecording._ID)));
            item.setStart(c.getLong(c.getColumnIndex(TableScheduledRecording.COLUMN_NAME_START)));
            item.setEnd(c.getLong(c.getColumnIndex(TableScheduledRecording.COLUMN_NAME_END)));
            list.add(item);
        }
        c.close();

        return list;
    }

    public int getScheduledRecordingsCount() {
        SQLiteDatabase db = getReadableDatabase();
        String[] projection = {TableScheduledRecording._ID};
        Cursor c = db.query(TableScheduledRecording.TABLE_NAME, projection, null, null, null, null, null);
        int count = c.getCount();
        c.close();
        return count;
    }

    // Given a time, returns true if other recordings have already been scheduled for that time.
    public boolean alreadyScheduled(long time) {
        SQLiteDatabase db = getReadableDatabase();
        String[] projection = {TableScheduledRecording._ID};
        String where = "? >= " + TableScheduledRecording.COLUMN_NAME_START + " AND ? <= " + TableScheduledRecording.COLUMN_NAME_END;
        String[] whereArgs = {String.valueOf(time), String.valueOf(time)};
        Cursor c = db.query(TableScheduledRecording.TABLE_NAME, projection, where, whereArgs, null, null, null);
        int count = c.getCount();
        c.close();

        return count > 0;
    }
}
