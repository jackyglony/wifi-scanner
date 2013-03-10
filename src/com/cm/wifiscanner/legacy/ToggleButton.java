package com.cm.wifiscanner.legacy;

import com.cm.wifiscanner.R;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;

public class ToggleButton extends View implements OnClickListener, OnTouchListener {

    public interface OnChangedListener {
        public void onChanged(boolean checked);
    }

    private Bitmap mBackgroundOn;
    private Bitmap mBackgroundOff;
    private Bitmap mBackgroundButton;

    private Rect mRectOn;
    private Rect mRectOff;

    private Paint mPaint;
    private Matrix mMatrix;

    private float mPositionX;

    private boolean mSliding;
    private boolean mChecked;
    private boolean mLastSelection;

    private OnChangedListener mListener;

    public ToggleButton(Context context) {
        super(context);
        init();
    }

    public ToggleButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        mBackgroundOn = BitmapFactory.decodeResource(getResources(), R.drawable.toggle_on);
        mBackgroundOff = BitmapFactory.decodeResource(getResources(), R.drawable.toggle_off);
        mBackgroundButton = BitmapFactory.decodeResource(getResources(), R.drawable.toggle_button);

        final int width = mBackgroundOn.getWidth();
        final int w = mBackgroundButton.getWidth();
        final int h = mBackgroundButton.getHeight();

        mRectOff = new Rect(0, 0, w, h);
        mRectOn = new Rect(width - w, 0, w, h);

        mPaint = new Paint();
        mMatrix = new Matrix();

        setOnTouchListener(this);

        this.setBackgroundColor(Color.BLUE);

        mPositionX = mRectOn.left;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (!isEnabled())
            return;

        float x = mPositionX;
        if (mSliding) {
            if (x < mRectOff.left) {
                x = 0;
            } else if (x > mRectOn.left) {
                x = mRectOn.left;
            }

            if (x < mRectOff.right) {
                mChecked = false;
            } else if ((x + mBackgroundButton.getWidth()) > mRectOn.left) {
                mChecked = true;
            }
        } else {
            if (mChecked) {
                x = mRectOn.left;
            } else {
                x = mRectOff.left;
            }
        }

        if (mChecked) {
            canvas.drawBitmap(mBackgroundOn, mMatrix, mPaint);
        } else {
            canvas.drawBitmap(mBackgroundOff, mMatrix, mPaint);
        }

        canvas.drawBitmap(mBackgroundButton, x, 0, mPaint);

//        mPositionX = x;
    }

    @Override
    public boolean onTouch(View view, MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mSliding = true;
                break;

            case MotionEvent.ACTION_MOVE:
                mPositionX = event.getX();
                break;

            case MotionEvent.ACTION_UP:
                mSliding = false;

                if (mChecked != mLastSelection) {
                    mLastSelection = mChecked;

                    if (mListener != null) {
                        mListener.onChanged(mChecked);
                    }
                }
                break;

            default:
                break;
        }

        invalidate();
        return true;
    }

//    @Override
//    public boolean onTouchEvent(MotionEvent event) {
//        switch (event.getAction()) {
//            case MotionEvent.ACTION_DOWN:
//                mSliding = true;
//                break;
//
//            case MotionEvent.ACTION_MOVE:
//                mPositionX = event.getX();
//                break;
//
//            case MotionEvent.ACTION_UP:
//                mSliding = false;
//
//                if (mChecked != mLastSelection) {
//                    mLastSelection = mChecked;
//
//                    if (mListener != null) {
//                        mListener.onChanged(mChecked);
//                    }
//                }
//                break;
//
//            default:
//                break;
//        }
//
//        Toast.makeText(getContext(), "onTouchEvent", Toast.LENGTH_SHORT).show();
//
//        invalidate();
//        return true;
//    }

    @Override
    public void onClick(View v) {
        mChecked = !mChecked;
    }

    public void setOnChangedListener(OnChangedListener listener) {
        mListener = listener;
    }

    public void setChecked(boolean checked) {
        mChecked = checked;
    }
}
