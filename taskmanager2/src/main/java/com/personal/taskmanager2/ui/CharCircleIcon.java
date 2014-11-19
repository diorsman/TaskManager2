package com.personal.taskmanager2.ui;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.OvalShape;
import android.graphics.drawable.shapes.Shape;

/**
 * Created by Omid Ghomeshi on 11/14/14.
 */
public class CharCircleIcon extends ShapeDrawable {

    private char  mCharacter;
    private Paint mTextPaint;

    public CharCircleIcon(char character, int color, Typeface typeface) {
        super(new OvalShape());
        this.getPaint().setColor(color);

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
    protected void onDraw(Shape shape, Canvas canvas, Paint paint) {
        super.onDraw(shape, canvas, paint);

        // draw text
        int width = canvas.getWidth();
        int height = canvas.getHeight();
        mTextPaint.setTextSize((height / 2) * 1.2f);
        canvas.drawText(String.valueOf(mCharacter),
                        width / 2,
                        height / 2 - ((mTextPaint.descent() + mTextPaint.ascent()) / 2),
                        mTextPaint);
    }
}
