/*
 * Copyright (c) 2017 Claudio "iClaude" Agostini <agostini.claudio1@gmail.com>
 * Licensed under the Apache License, Version 2.0
 */

package com.danielkim.soundrecorder;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;

/**
 * POJO representing a scheduled recording, mapping scheduled recordings in the table
 * "scheduled_recordings" of the database.
 */

public class ScheduledRecordingItem implements Parcelable, Comparable<ScheduledRecordingItem> {

    private long id;
    private long start;
    private long end;

    public ScheduledRecordingItem() {
    }

    public ScheduledRecordingItem(long id, long start, long end) {
        this.id = id;
        this.start = start;
        this.end = end;
    }

    public long getId() {
        return id;
    }

    public long getStart() {
        return start;
    }

    public long getEnd() {
        return end;
    }

    public void setId(long id) {
        this.id = id;
    }

    public void setStart(long start) {
        this.start = start;
    }

    public void setEnd(long end) {
        this.end = end;
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
        end = in.readLong();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeLong(id);
        parcel.writeLong(start);
        parcel.writeLong(end);
    }

    // Implementation of Comparable interface.

    @Override
    public int compareTo(@NonNull ScheduledRecordingItem scheduledRecordingItem) {
        return (int) (getStart() - scheduledRecordingItem.getStart());
    }
}
