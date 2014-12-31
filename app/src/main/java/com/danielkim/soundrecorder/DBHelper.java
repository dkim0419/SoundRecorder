package com.danielkim.soundrecorder;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;

import java.util.Comparator;

/**
 * Created by Daniel on 12/29/2014.
 */
public class DBHelper extends SQLiteOpenHelper {
    private Context mContext;

    public static final String DATABASE_NAME = "saved_recordings.db";
    private static final int DATABASE_VERSION = 1;

    public static abstract class RecordingDatabaseItem implements BaseColumns {
        public static final String TABLE_NAME = "saved_recordings";

        public static final String COLUMN_NAME_RECORDING_NAME = "recording_name";
        public static final String COLUMN_NAME_RECORDING_FILE_PATH = "file_path";
        public static final String COLUMN_NAME_RECORDING_LENGTH = "length";
        public static final String COLUMN_NAME_TIME_ADDED = "time_added";
    }

    public interface OnDatabaseChangedListener {
        void onDatabaseEntryUpdated();
    }

    private OnDatabaseChangedListener mOnDatabaseChangedListener;

    private static final String TEXT_TYPE = " TEXT";
    private static final String COMMA_SEP = ",";
    private static final String SQL_CREATE_ENTRIES =
            "CREATE TABLE " + RecordingDatabaseItem.TABLE_NAME + " (" +
                    RecordingDatabaseItem._ID + " INTEGER PRIMARY KEY" + COMMA_SEP +
                    RecordingDatabaseItem.COLUMN_NAME_RECORDING_NAME + TEXT_TYPE + COMMA_SEP +
                    RecordingDatabaseItem.COLUMN_NAME_RECORDING_FILE_PATH + TEXT_TYPE + COMMA_SEP +
                    RecordingDatabaseItem.COLUMN_NAME_RECORDING_LENGTH + " INTEGER " + COMMA_SEP +
                    RecordingDatabaseItem.COLUMN_NAME_TIME_ADDED + " INTEGER " + ")";

    @SuppressWarnings("unused")
    private static final String SQL_DELETE_ENTRIES = "DROP TABLE IF EXISTS " + RecordingDatabaseItem.TABLE_NAME;

    public long addRecording(String recordingName, String filePath, long length) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(RecordingDatabaseItem.COLUMN_NAME_RECORDING_NAME, recordingName);
        cv.put(RecordingDatabaseItem.COLUMN_NAME_RECORDING_FILE_PATH, filePath);
        cv.put(RecordingDatabaseItem.COLUMN_NAME_RECORDING_LENGTH, length);
        cv.put(RecordingDatabaseItem.COLUMN_NAME_TIME_ADDED, System.currentTimeMillis());
        long rowId = db.insert(RecordingDatabaseItem.TABLE_NAME, null, cv);
        if (mOnDatabaseChangedListener != null)
            mOnDatabaseChangedListener.onDatabaseEntryUpdated();
        return rowId;
    }

    public RecordingItem getItemAt(int position) {
        SQLiteDatabase db = getReadableDatabase();
        String[] projection = {
                RecordingDatabaseItem._ID,
                RecordingDatabaseItem.COLUMN_NAME_RECORDING_NAME,
                RecordingDatabaseItem.COLUMN_NAME_RECORDING_FILE_PATH,
                RecordingDatabaseItem.COLUMN_NAME_RECORDING_LENGTH,
                RecordingDatabaseItem.COLUMN_NAME_TIME_ADDED
        };
        Cursor c = db.query(RecordingDatabaseItem.TABLE_NAME, projection, null, null, null, null, null);
        if (c.moveToPosition(position)) {
            RecordingItem item = new RecordingItem();
            item.setId(c.getInt(c.getColumnIndex(RecordingDatabaseItem._ID)));
            item.setLength(c.getInt(c.getColumnIndex(RecordingDatabaseItem.COLUMN_NAME_RECORDING_LENGTH)));
            item.setFilePath(c.getString(c.getColumnIndex(RecordingDatabaseItem.COLUMN_NAME_RECORDING_FILE_PATH)));
            item.setName(c.getString(c.getColumnIndex(RecordingDatabaseItem.COLUMN_NAME_RECORDING_NAME)));
            item.setTime(c.getLong(c.getColumnIndex(RecordingDatabaseItem.COLUMN_NAME_TIME_ADDED)));
            c.close();
            return item;
        }
        return null;
    }

    public void removeItemWithId(int id) {
        SQLiteDatabase db = getWritableDatabase();
        String[] whereArgs = { String.valueOf(id) };
        db.delete(RecordingDatabaseItem.TABLE_NAME, "_id=?", whereArgs);
        if (mOnDatabaseChangedListener != null)
            mOnDatabaseChangedListener.onDatabaseEntryUpdated();
    }

    public int getCount() {
        SQLiteDatabase db = getReadableDatabase();
        String[] projection = { RecordingDatabaseItem._ID };
        Cursor c = db.query(RecordingDatabaseItem.TABLE_NAME, projection, null, null, null, null, null);
        int count = c.getCount();
        c.close();
        return count;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_ENTRIES);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    public DBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        mContext = context;
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

    public void renameItem(RecordingItem item, String recordingName) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(RecordingDatabaseItem.COLUMN_NAME_RECORDING_NAME, recordingName);
        db.update(RecordingDatabaseItem.TABLE_NAME, cv,
                RecordingDatabaseItem._ID + "=" + item.getId(), null);
        if (mOnDatabaseChangedListener != null)
            mOnDatabaseChangedListener.onDatabaseEntryUpdated();
    }

    public void setOnDatabaseChangedListener(OnDatabaseChangedListener listener) {
        mOnDatabaseChangedListener = listener;
    }

    public long restoreRecording(RecordingItem item) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(RecordingDatabaseItem.COLUMN_NAME_RECORDING_NAME, item.getName());
        cv.put(RecordingDatabaseItem.COLUMN_NAME_RECORDING_FILE_PATH, item.getFilePath());
        cv.put(RecordingDatabaseItem.COLUMN_NAME_RECORDING_LENGTH, item.getLength());
        cv.put(RecordingDatabaseItem.COLUMN_NAME_TIME_ADDED, item.getTime());
        cv.put(RecordingDatabaseItem._ID, item.getId());
        long rowId = db.insert(RecordingDatabaseItem.TABLE_NAME, null, cv);
        if (mOnDatabaseChangedListener != null)
            mOnDatabaseChangedListener.onDatabaseEntryUpdated();
        return rowId;
    }
}
