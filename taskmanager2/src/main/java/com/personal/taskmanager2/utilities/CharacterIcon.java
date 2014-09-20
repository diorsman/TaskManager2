package com.personal.taskmanager2.utilities;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;


public class CharacterIcon extends ColorDrawable {

    private char    mCharacter;
    private Paint   mTextPaint;


    public CharacterIcon(char character, int color, Typeface typeface) {

        super(color);

        mCharacter = character;
        mTextPaint = new Paint();

        //text paint settings
        mTextPaint.setColor(Color.WHITE);
        mTextPaint.setAntiAlias(true);
        mTextPaint.setStyle(Paint.Style.FILL);
        mTextPaint.setTextAlign(Paint.Align.CENTER);
        mTextPaint.setTypeface(typeface);
    }

    @Override
    public void draw(Canvas canvas) {
        super.draw(canvas);

        // draw text
        int width = canvas.getWidth();
        int height = canvas.getHeight();
        mTextPaint.setTextSize((height/2) * 1.2f);
        canvas.drawText(String.valueOf(mCharacter),
                        width / 2,
                        height / 2 -
                        ((mTextPaint.descent() + mTextPaint.ascent()) / 2),
                        mTextPaint);

    }
}
