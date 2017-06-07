package com.danielkim.soundrecorder.listeners;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * POJO representing a scheduled recording, mapping scheduled recordings in the table
 * "scheduled_recordings" of the database.
 */

public class ScheduledRecordingItem implements Parcelable {

    private long id;
    private long start;
    private long length;

    public ScheduledRecordingItem() {
    }

    public ScheduledRecordingItem(long id, long start, long length) {
        this.id = id;
        this.start = start;
        this.length = length;
    }

    public long getId() {
        return id;
    }

    public long getStart() {
        return start;
    }

    public long getLength() {
        return length;
    }

    public void setId(long id) {
        this.id = id;
    }

    public void setStart(long start) {
        this.start = start;
    }

    public void setLength(long length) {
        this.length = length;
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
        id = in.readLong();
        start = in.readLong();
        length = in.readLong();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeLong(id);
        parcel.writeLong(start);
        parcel.writeLong(length);
    }
}
