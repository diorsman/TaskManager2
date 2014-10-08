package com.personal.taskmanager2;

import android.app.DatePickerDialog;
import android.app.DatePickerDialog.OnDateSetListener;
import android.app.Dialog;
import android.app.DialogFragment;
import android.os.Bundle;

import java.util.Calendar;


public class DatePickerFragment extends DialogFragment {

    //private OnDateSetListener mFragment;

    /*public DatePickerFragment(Fragment callback) {

        mFragment = (OnDateSetListener) callback;
    }*/

    @Override
    public Dialog onCreateDialog(Bundle savedInstaceState) {

        final Calendar c = Calendar.getInstance();
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int day = c.get(Calendar.DAY_OF_MONTH);

        return new DatePickerDialog(getActivity(),
                                    (OnDateSetListener) getTargetFragment(),
                                    year,
                                    month,
                                    day);
    }
}
