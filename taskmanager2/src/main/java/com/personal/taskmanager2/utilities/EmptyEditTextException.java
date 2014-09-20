package com.personal.taskmanager2.utilities;


public class EmptyEditTextException extends Exception {

    private MyEditText mEditText;

    public EmptyEditTextException(MyEditText editText) {
        super();
        mEditText = editText;
    }

    public MyEditText getEditText() {
        return mEditText;
    }
}
