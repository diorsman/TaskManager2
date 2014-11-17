package com.personal.taskmanager2.ui.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import com.personal.taskmanager2.R;

/**
 * Created by Omid on 11/14/14.
 */
public class FloatingActionButton extends View {

    private static final String TAG = "FloatingActionButton";

    private Paint           mButtonPaint;
    private Paint           mDrawablePaint;
    private int             mColor;
    private int             mColorPressed;
    private float           mRadius;
    private float           mDx;
    private float           mDy;
    private int             mShadowColor;
    private Bitmap          mBitmap;

    public FloatingActionButton(Context context) {
        this(context, null);
    }

    public FloatingActionButton(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public FloatingActionButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        TypedArray a = context.getTheme()
                              .obtainStyledAttributes(attrs,
                                                      R.styleable.FloatingActionButton,
                                                      0,
                                                      0);

        try {
            Drawable d = a.getDrawable(R.styleable.FloatingActionButton_floatingIcon);
            if (d != null) {
                mBitmap = ((BitmapDrawable) d).getBitmap();
            }
            mColor = a.getColor(R.styleable.FloatingActionButton_floatingColor, Color.WHITE);
            mColorPressed =
                    a.getColor(R.styleable.FloatingActionButton_floatingColorPressed, Color.GRAY);
            mRadius = a.getFloat(R.styleable.FloatingActionButton_floatingShadowRadius, 10.0f);
            mDx = a.getFloat(R.styleable.FloatingActionButton_floatingShadowDx, 0.0f);
            mDy = a.getFloat(R.styleable.FloatingActionButton_floatingShadowDy, 3.5f);
            mShadowColor = a.getInteger(R.styleable.FloatingActionButton_floatingShadowColor,
                                        Color.argb(100, 0, 0, 0));
        }
        finally {
            a.recycle();
        }

        init();
    }

    public void setIcon(Drawable drawable) {
        mBitmap = ((BitmapDrawable) drawable).getBitmap();
        invalidate();
    }

    public Bitmap getIcon() {
        return mBitmap;
    }

    public void setColor(int color) {
        mColor = color;
        mButtonPaint.setColor(color);
        invalidate();
    }

    public int getColor() {
        return mColor;
    }

    public void setColorPressed(int color) {
        mColorPressed = color;
        invalidate();
    }

    public int getColorPressed() {
        return mColorPressed;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        canvas.drawCircle(getWidth() / 2,
                          getHeight() / 2,
                          (float) (getWidth() / 2.6),
                          mButtonPaint);
        if (mBitmap != null) {
            canvas.drawBitmap(mBitmap, (getWidth() - mBitmap.getWidth()) / 2,
                              (getHeight() - mBitmap.getHeight()) / 2, mDrawablePaint);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int color;
        if (event.getAction() == MotionEvent.ACTION_UP) {
            color = mColor;
        }
        else {
            color = mColorPressed;
        }
        mButtonPaint.setColor(color);
        invalidate();
        super.onTouchEvent(event);
        return true;
    }

    private void init() {
        setLayerToSW(this);
        mButtonPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mDrawablePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mButtonPaint.setStyle(Paint.Style.FILL);
        mButtonPaint.setShadowLayer(mRadius, mDx, mDy, mShadowColor);
        mButtonPaint.setColor(mColor);
    }

    private void setLayerToSW(View v) {
        if (!v.isInEditMode() && Build.VERSION.SDK_INT >= 11) {
            setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        }
    }
}
