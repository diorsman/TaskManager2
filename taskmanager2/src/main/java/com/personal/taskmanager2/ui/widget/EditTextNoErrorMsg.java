package com.personal.taskmanager2.ui.widget;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.widget.EditText;

public class EditTextNoErrorMsg extends EditText {

    public EditTextNoErrorMsg(Context context, AttributeSet attrs) {

        super(context, attrs);
    }

    @Override
    public void setError(CharSequence error, Drawable icon) {

        setCompoundDrawables(null, null, icon, null);
    }
}
