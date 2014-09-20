package com.personal.taskmanager2;

import android.app.Dialog;
import android.app.DialogFragment;
import android.app.Fragment;
import android.app.TimePickerDialog;
import android.app.TimePickerDialog.OnTimeSetListener;
import android.os.Bundle;
import android.text.format.DateFormat;

import java.util.Calendar;

public class TimePickerFragment extends DialogFragment {

    private Fragment mFragment;

    public TimePickerFragment(Fragment callback) {

        mFragment = callback;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        //Use current time is default values
        final Calendar c = Calendar.getInstance();
        int hour = c.get(Calendar.HOUR_OF_DAY);
        int minute = c.get(Calendar.MINUTE);
        return new TimePickerDialog(getActivity(),
                                    (OnTimeSetListener) mFragment,
                                    hour,
                                    minute,
                                    DateFormat.is24HourFormat(getActivity()));
    }
}
