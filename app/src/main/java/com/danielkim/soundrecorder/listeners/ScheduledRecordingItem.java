package com.danielkim.soundrecorder.listeners;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * POJO representin a scheduled recording, mapping scheduled recordings in the table
 * "scheduled_recordings" of the database.
 */

public class ScheduledRecordingItem implements Parcelable {

    private long mId;
    private long mStart;
    private long mLength;

    public ScheduledRecordingItem() {
    }

    public ScheduledRecordingItem(long mId, long mStart, long mLength) {
        this.mId = mId;
        this.mStart = mStart;
        this.mLength = mLength;
    }

    public long getmId() {
        return mId;
    }

    public long getmStart() {
        return mStart;
    }

    public long getmLength() {
        return mLength;
    }

    public void setmId(long mId) {
        this.mId = mId;
    }

    public void setmStart(long mStart) {
        this.mStart = mStart;
    }

    public void setmLength(long mLength) {
        this.mLength = mLength;
    }

    // Implementation of Parcelable interface.
    public static final Parcelable.Creator<ScheduledRecordingItem> CREATOR = new Parcelable.Creator<ScheduledRecordingItem>() {
        public ScheduledRecordingItem createFromParcel(Parcel in) {
            return new ScheduledRecordingItem(in);
        }

        public ScheduledRecordingItem[] newArray(int size) {
            return new ScheduledRecordingItem[size];
        }
    };

    public ScheduledRecordingItem(Parcel in) {
        mId = in.readLong();
        mStart = in.readLong();
        mLength = in.readLong();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeLong(mId);
        parcel.writeLong(mStart);
        parcel.writeLong(mLength);
    }
}
