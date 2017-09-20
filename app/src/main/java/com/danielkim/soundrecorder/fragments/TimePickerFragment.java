package com.danielkim.soundrecorder.fragments;

import android.app.Dialog;
import android.app.DialogFragment;
import android.app.TimePickerDialog;
import android.content.Context;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.widget.TimePicker;

/**
 * Shows a dialog to pick a time (hour and minute).
 * Communicates the time selected through an interface.
 * This class stores and communicates the id of the view that needs the time, so it can be used
 * for different views within the same Activity/Fragment.
 */

public class TimePickerFragment extends DialogFragment implements TimePickerDialog.OnTimeSetListener {
    private static final String ARG_VIEW_ID = "ARG_VIEW_ID";
    private static final String ARG_HOUR = "ARG_HOUR";
    private static final String ARG_MINUTE = "ARG_MINUTE";


    private MyOnTimeSetListener listener;

    public static TimePickerFragment newInstance(long viewId, int hour, int minute) {
        TimePickerFragment f = new TimePickerFragment();
        Bundle bundle = new Bundle();
        bundle.putLong(ARG_VIEW_ID, viewId);
        bundle.putInt(ARG_HOUR, hour);
        bundle.putInt(ARG_MINUTE, minute);
        f.setArguments(bundle);

        return f;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        int hour = getArguments().getInt(ARG_HOUR);
        int minute = getArguments().getInt(ARG_MINUTE);

        return new TimePickerDialog(getActivity(), this, hour, minute, DateFormat.is24HourFormat(getActivity()));
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        try {
            listener = (TimePickerFragment.MyOnTimeSetListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() + "must implement TimePickerFragment.MyOnTimeSetListener");
        }
    }

    @Override
    public void onTimeSet(TimePicker timePicker, int hourOfDay, int minute) {
        if (listener != null) {
            listener.onTimeSet(getArguments().getLong(ARG_VIEW_ID, 0), hourOfDay, minute);
        }
    }

    // Interface form communication with the Activity.
    public interface MyOnTimeSetListener {
        void onTimeSet(long viewId, int hour, int minute);
    }

}
