package com.personal.taskmanager2.utilities;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.widget.EditText;

public class MyEditText extends EditText {

    public MyEditText(Context context, AttributeSet attrs) {

        super(context, attrs);
    }

    @Override
    public void setError(CharSequence error, Drawable icon) {

        setCompoundDrawables(null, null, icon, null);
    }
}
