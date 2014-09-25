package com.personal.taskmanager2.utilities;


import android.widget.EditText;

import java.text.ParseException;

public class EmptyEditTextException extends ParseException {

    private EditText mEditText;

    public EmptyEditTextException(EditText editText) {

        super("EditText is empty", 0);
        mEditText = editText;
    }

    public EditText getEditText() {

        return mEditText;
    }
}
