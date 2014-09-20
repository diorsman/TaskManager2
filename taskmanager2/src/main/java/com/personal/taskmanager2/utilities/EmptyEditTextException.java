package com.personal.taskmanager2.utilities;


import java.text.ParseException;

public class EmptyEditTextException extends ParseException {

    private EditTextNoErrorMsg mEditText;

    public EmptyEditTextException(EditTextNoErrorMsg editText) {

        super("EditText is empty", 0);
        mEditText = editText;
    }

    public EditTextNoErrorMsg getEditText() {

        return mEditText;
    }
}
