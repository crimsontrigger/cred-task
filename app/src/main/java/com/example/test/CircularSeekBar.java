package com.example.test;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.*;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

import static android.view.MotionEvent.*;

public class CircularSeekBar extends View {
    private static final int DEFAULT_EDGE_LENGTH = 260;

    private static final float CIRCLE_ANGLE = 360;
    private static final int DEFAULT_ARC_WIDTH = 20;
    private static final float DEFAULT_OPEN_ANGLE = 120;
    private static final float DEFAULT_ROTATE_ANGLE = 90;
    private static final int DEFAULT_BORDER_WIDTH = 0;
    private static final int DEFAULT_BORDER_COLOR = 0xffffffff;

    private static final int DEFAULT_THUMB_COLOR = 0xffffffff;
    private static final int DEFAULT_THUMB_WIDTH = 2;
    private static final int DEFAULT_THUMB_RADIUS = 15;
    private static final int DEFAULT_THUMB_SHADOW_RADIUS = 0;
    private static final int DEFAULT_THUMB_SHADOW_COLOR = 0xFF000000;

    private static final int DEFAULT_SHADOW_RADIUS = 0;

    private static final int THUMB_MODE_STROKE = 0;
    private static final int THUMB_MODE_FILL = 1;
    private static final int THUMB_MODE_FILL_STROKE = 2;

    private static final int DEFAULT_MAX_VALUE = 100;
    private static final int DEFAULT_MIN_VALUE = 0;

    private static final String KEY_PROGRESS_PRESENT = "PRESENT";


    private int[] mArcColors;
    private float mArcWidth;
    private float mOpenAngle;
    private float mRotateAngle;
    private int mBorderWidth;
    private int mBorderColor;

    private int mThumbColor;
    private float mThumbWidth;
    private float mThumbRadius;
    private float mThumbShadowRadius;
    private int mThumbShadowColor;
    private int mThumbMode;

    private int mShadowRadius;

    private int mMaxValue;
    private int mMinValue;

    private float mCenterX;
    private float mCenterY;

    private float mThumbX;
    private float mThumbY;

    private Path mSeekPath;
    private Path mBorderPath;
    private Paint mArcPaint;
    private Paint mThumbPaint;
    private Paint mBorderPaint;
    private Paint mShadowPaint;

    private float[] mTempPos;
    private float[] mTempTan;
    private PathMeasure mSeekPathMeasure;

    private float mProgressPresent = 0;
    private boolean mCanDrag = false;
    private boolean mAllowTouchSkip = false;
    private GestureDetector mDetector;
    private Matrix mInvertMatrix;
    private Region mArcRegion;


    public CircularSeekBar(Context context) {
        this(context, null);
    }

    public CircularSeekBar(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CircularSeekBar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setSaveEnabled(true);
        setLayerType(LAYER_TYPE_SOFTWARE, null);
        initAttrs(context, attrs);
        initData();
        initPaint();
    }


    private void initAttrs(Context context, AttributeSet attrs) {
        TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.CircularSeekBar);
        mArcColors = getArcColors(context, ta);
        mArcWidth = ta.getDimensionPixelSize(R.styleable.CircularSeekBar_arc_width, dp2px(DEFAULT_ARC_WIDTH));
        mOpenAngle = ta.getFloat(R.styleable.CircularSeekBar_arc_open_angle, DEFAULT_OPEN_ANGLE);
        mRotateAngle = ta.getFloat(R.styleable.CircularSeekBar_arc_rotate_angle, DEFAULT_ROTATE_ANGLE);
        mMaxValue = ta.getInt(R.styleable.CircularSeekBar_arc_max, DEFAULT_MAX_VALUE);
        mMinValue = ta.getInt(R.styleable.CircularSeekBar_arc_min, DEFAULT_MIN_VALUE);

        if (mMaxValue <= mMinValue) {
            mMaxValue = DEFAULT_MAX_VALUE;
            mMinValue = DEFAULT_MIN_VALUE;
        }
        int progress = ta.getInt(R.styleable.CircularSeekBar_arc_progress, mMinValue);
        setProgress(progress);
        mBorderWidth = ta.getDimensionPixelSize(R.styleable.CircularSeekBar_arc_border_width, dp2px(DEFAULT_BORDER_WIDTH));
        mBorderColor = ta.getColor(R.styleable.CircularSeekBar_arc_border_color, DEFAULT_BORDER_COLOR);

        mThumbColor = ta.getColor(R.styleable.CircularSeekBar_arc_thumb_color, DEFAULT_THUMB_COLOR);
        mThumbRadius = ta.getDimensionPixelSize(R.styleable.CircularSeekBar_arc_thumb_radius, dp2px(DEFAULT_THUMB_RADIUS));
        mThumbShadowRadius = ta.getDimensionPixelSize(R.styleable.CircularSeekBar_arc_thumb_shadow_radius, dp2px(DEFAULT_THUMB_SHADOW_RADIUS));
        mThumbShadowColor = ta.getColor(R.styleable.CircularSeekBar_arc_thumb_shadow_color, DEFAULT_THUMB_SHADOW_COLOR);
        mThumbWidth = ta.getDimensionPixelSize(R.styleable.CircularSeekBar_arc_thumb_width, dp2px(DEFAULT_THUMB_WIDTH));
        mThumbMode = ta.getInt(R.styleable.CircularSeekBar_arc_thumb_mode, THUMB_MODE_STROKE);

        mShadowRadius = ta.getDimensionPixelSize(R.styleable.CircularSeekBar_arc_shadow_radius, dp2px(DEFAULT_SHADOW_RADIUS));
        ta.recycle();
    }


    private int[] getArcColors(Context context, TypedArray ta) {
        int[] ret;
        int resId = ta.getResourceId(R.styleable.CircularSeekBar_arc_colors, 0);
        if (0 == resId) {
            resId = R.array.arc_colors_default;
        }
        ret = getColorsByArrayResId(context, resId);
        return ret;
    }


    private int[] getColorsByArrayResId(Context context, int resId) {
        int[] ret;
        TypedArray colorArray = context.getResources().obtainTypedArray(resId);
        ret = new int[colorArray.length()];
        for (int i = 0; i < colorArray.length(); i++) {
            ret[i] = colorArray.getColor(i, 0);
        }
        return ret;
    }


    private void initData() {
        mSeekPath = new Path();
        mBorderPath = new Path();
        mSeekPathMeasure = new PathMeasure();
        mTempPos = new float[2];
        mTempTan = new float[2];

        mDetector = new GestureDetector(getContext(), new OnClickListener());
        mInvertMatrix = new Matrix();
        mArcRegion = new Region();
    }


    private void initPaint() {
        initArcPaint();
        initThumbPaint();
        initBorderPaint();
        initShadowPaint();
    }


    private void initArcPaint() {
        mArcPaint = new Paint();
        mArcPaint.setAntiAlias(true);
        mArcPaint.setStrokeWidth(mArcWidth);
        mArcPaint.setStyle(Paint.Style.STROKE);
        mArcPaint.setStrokeCap(Paint.Cap.ROUND);
    }


    private void initThumbPaint() {
        mThumbPaint = new Paint();
        mThumbPaint.setAntiAlias(true);
        mThumbPaint.setColor(mThumbColor);
        mThumbPaint.setStrokeWidth(mThumbWidth);
        mThumbPaint.setStrokeCap(Paint.Cap.ROUND);
        if (mThumbMode == THUMB_MODE_FILL) {
            mThumbPaint.setStyle(Paint.Style.FILL_AND_STROKE);
        } else if (mThumbMode == THUMB_MODE_FILL_STROKE) {
            mThumbPaint.setStyle(Paint.Style.FILL_AND_STROKE);
        } else {
            mThumbPaint.setStyle(Paint.Style.STROKE);
        }
        mThumbPaint.setTextSize(56);
    }


    private void initBorderPaint() {
        mBorderPaint = new Paint();
        mBorderPaint.setAntiAlias(true);
        mBorderPaint.setColor(mBorderColor);
        mBorderPaint.setStrokeWidth(mBorderWidth);
        mBorderPaint.setStyle(Paint.Style.STROKE);
    }


    private void initShadowPaint() {
        mShadowPaint = new Paint();
        mShadowPaint.setAntiAlias(true);
        mShadowPaint.setStrokeWidth(mBorderWidth);
        mShadowPaint.setStyle(Paint.Style.FILL_AND_STROKE);
    }

    @Override
    protected Parcelable onSaveInstanceState() {
        Bundle bundle = new Bundle();
        bundle.putParcelable("superState", super.onSaveInstanceState());
        bundle.putFloat(KEY_PROGRESS_PRESENT, mProgressPresent);
        return bundle;
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        if (state instanceof Bundle) {
            Bundle bundle = (Bundle) state;
            this.mProgressPresent = bundle.getFloat(KEY_PROGRESS_PRESENT);
            state = bundle.getParcelable("superState");
        }
        if (null != mOnProgressChangeListener) {
            mOnProgressChangeListener.onProgressChanged(this, getProgress(), false);
        }
        super.onRestoreInstanceState(state);
    }


    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int ws = MeasureSpec.getSize(widthMeasureSpec);
        int wm = MeasureSpec.getMode(widthMeasureSpec);
        int hs = MeasureSpec.getSize(heightMeasureSpec);
        int hm = MeasureSpec.getMode(heightMeasureSpec);

        if (wm == MeasureSpec.UNSPECIFIED) {
            wm = MeasureSpec.EXACTLY;
            ws = dp2px(DEFAULT_EDGE_LENGTH);
        } else if (wm == MeasureSpec.AT_MOST) {
            wm = MeasureSpec.EXACTLY;
            ws = Math.min(dp2px(DEFAULT_EDGE_LENGTH), ws);
        }
        if (hm == MeasureSpec.UNSPECIFIED) {
            hm = MeasureSpec.EXACTLY;
            hs = dp2px(DEFAULT_EDGE_LENGTH);
        } else if (hm == MeasureSpec.AT_MOST) {
            hm = MeasureSpec.EXACTLY;
            hs = Math.min(dp2px(DEFAULT_EDGE_LENGTH), hs);
        }
        setMeasuredDimension(MeasureSpec.makeMeasureSpec(ws, wm), MeasureSpec.makeMeasureSpec(hs, hm));
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        int safeW = w - getPaddingLeft() - getPaddingRight();
        int safeH = h - getPaddingTop() - getPaddingBottom();
        float edgeLength, startX, startY;
        float fix = mArcWidth / 2 + mBorderWidth + mShadowRadius * 2;
        if (safeW < safeH) {

            edgeLength = safeW - fix;
            startX = getPaddingLeft();
            startY = (safeH - safeW) / 2.0f + getPaddingTop();
        } else {

            edgeLength = safeH - fix;
            startX = (safeW - safeH) / 2.0f + getPaddingLeft();
            startY = getPaddingTop();
        }


        RectF content = new RectF(startX + fix, startY + fix, startX + edgeLength, startY + edgeLength);
        mCenterX = content.centerX();
        mCenterY = content.centerY();


        mSeekPath.reset();
        mSeekPath.addArc(content, mOpenAngle / 2, CIRCLE_ANGLE - mOpenAngle);
        mSeekPathMeasure.setPath(mSeekPath, false);
        computeThumbPos(mProgressPresent);

        resetShaderColor();

        mInvertMatrix.reset();
        mInvertMatrix.preRotate(-mRotateAngle, mCenterX, mCenterY);

        mArcPaint.getFillPath(mSeekPath, mBorderPath);
        mBorderPath.close();
        mArcRegion.setPath(mBorderPath, new Region(0, 0, w, h));
    }


    private void resetShaderColor() {

        float startPos = (mOpenAngle / 2) / CIRCLE_ANGLE;
        float stopPos = (CIRCLE_ANGLE - (mOpenAngle / 2)) / CIRCLE_ANGLE;
        int len = mArcColors.length - 1;
        float distance = (stopPos - startPos) / len;
        float pos[] = new float[mArcColors.length];
        for (int i = 0; i < mArcColors.length; i++) {
            pos[i] = startPos + (distance * i);
        }
        SweepGradient gradient = new SweepGradient(mCenterX, mCenterY, mArcColors, pos);
        mArcPaint.setShader(gradient);
    }


    @Override
    protected void onDraw(Canvas canvas) {
        canvas.save();
        canvas.rotate(mRotateAngle, mCenterX, mCenterY);
        mShadowPaint.setShadowLayer(mShadowRadius * 2, 0, 0, getColor());
        canvas.drawPath(mBorderPath, mShadowPaint);
        canvas.drawPath(mSeekPath, mArcPaint);
        if (mBorderWidth > 0) {
            canvas.drawPath(mBorderPath, mBorderPaint);
        }
        if (mThumbShadowRadius > 0) {
            mThumbPaint.setShadowLayer(mThumbShadowRadius, 0, 0, mThumbShadowColor);
            canvas.drawCircle(mThumbX, mThumbY, mThumbRadius, mThumbPaint);
            mThumbPaint.clearShadowLayer();
        }
        canvas.drawCircle(mThumbX, mThumbY, mThumbRadius, mThumbPaint);
        canvas.restore();
    }

    private boolean moved = false;
    private int lastProgress = -1;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        super.onTouchEvent(event);
        int action = event.getActionMasked();
        switch (action) {
            case ACTION_DOWN:
                moved = false;
                judgeCanDrag(event);
                if (null != mOnProgressChangeListener) {
                    mOnProgressChangeListener.onStartTrackingTouch(this);
                }
                break;
            case ACTION_MOVE:
                if (!mCanDrag) {
                    break;
                }
                float tempProgressPresent = getCurrentProgress(event.getX(), event.getY());
                if (!mAllowTouchSkip) {

                    if (Math.abs(tempProgressPresent - mProgressPresent) > 0.5f) {
                        break;
                    }
                }

                mProgressPresent = tempProgressPresent;
                computeThumbPos(mProgressPresent);

                if (null != mOnProgressChangeListener && getProgress() != lastProgress) {
                    mOnProgressChangeListener.onProgressChanged(this, getProgress(), true);
                    lastProgress = getProgress();
                }
                moved = true;
                break;
            case ACTION_UP:
            case ACTION_CANCEL:
                if (null != mOnProgressChangeListener && moved) {
                    mOnProgressChangeListener.onStopTrackingTouch(this);
                }
                break;
        }
        mDetector.onTouchEvent(event);
        invalidate();
        return true;
    }


    private void judgeCanDrag(MotionEvent event) {
        float[] pos = {event.getX(), event.getY()};
        mInvertMatrix.mapPoints(pos);
        if (getDistance(pos[0], pos[1]) <= mThumbRadius * 1.5) {
            mCanDrag = true;
        } else {
            mCanDrag = false;
        }
    }

    private class OnClickListener extends GestureDetector.SimpleOnGestureListener {
        @Override
        public boolean onSingleTapUp(MotionEvent e) {

            if (!isInArcProgress(e.getX(), e.getY())) return false;

            mProgressPresent = getCurrentProgress(e.getX(), e.getY());
            computeThumbPos(mProgressPresent);

            if (null != mOnProgressChangeListener) {
                mOnProgressChangeListener.onProgressChanged(CircularSeekBar.this, getProgress(), true);
                mOnProgressChangeListener.onStopTrackingTouch(CircularSeekBar.this);
            }
            return true;
        }
    }


    private boolean isInArcProgress(float px, float py) {
        float[] pos = {px, py};
        mInvertMatrix.mapPoints(pos);
        return mArcRegion.contains((int) pos[0], (int) pos[1]);
    }


    private float getCurrentProgress(float px, float py) {
        float diffAngle = getDiffAngle(px, py);
        float progress = diffAngle / (CIRCLE_ANGLE - mOpenAngle);
        if (progress < 0) progress = 0;
        if (progress > 1) progress = 1;
        return progress;
    }


    private float getDiffAngle(float px, float py) {
        float angle = getAngle(px, py);
        float diffAngle;
        diffAngle = angle - mRotateAngle;
        if (diffAngle < 0) {
            diffAngle = (diffAngle + CIRCLE_ANGLE) % CIRCLE_ANGLE;
        }
        diffAngle = diffAngle - mOpenAngle / 2;
        return diffAngle;
    }


    private float getAngle(float px, float py) {
        float angle = (float) ((Math.atan2(py - mCenterY, px - mCenterX)) * 180 / 3.14f);
        if (angle < 0) {
            angle += 360;
        }
        return angle;
    }


    private float getDistance(float px, float py) {
        return (float) Math.sqrt((px - mThumbX) * (px - mThumbX) + (py - mThumbY) * (py - mThumbY));
    }

    private int dp2px(int dp) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, getContext().getResources().getDisplayMetrics());
    }

    private void computeThumbPos(float present) {
        if (present < 0) present = 0;
        if (present > 1) present = 1;
        if (null == mSeekPathMeasure) return;
        float distance = mSeekPathMeasure.getLength() * present;
        mSeekPathMeasure.getPosTan(distance, mTempPos, mTempTan);
        mThumbX = mTempPos[0];
        mThumbY = mTempPos[1];
    }

    public int getColor() {
        return getColor(mProgressPresent);
    }


    private int getColor(float radio) {
        float diatance = 1.0f / (mArcColors.length - 1);
        int startColor;
        int endColor;
        if (radio >= 1) {
            return mArcColors[mArcColors.length - 1];
        }
        for (int i = 0; i < mArcColors.length; i++) {
            if (radio <= i * diatance) {
                if (i == 0) {
                    return mArcColors[0];
                }
                startColor = mArcColors[i - 1];
                endColor = mArcColors[i];
                float areaRadio = getAreaRadio(radio, diatance * (i - 1), diatance * i);
                return getColorFrom(startColor, endColor, areaRadio);
            }
        }
        return -1;
    }


    private float getAreaRadio(float radio, float startPosition, float endPosition) {
        return (radio - startPosition) / (endPosition - startPosition);
    }


    private int getColorFrom(int startColor, int endColor, float radio) {
        int redStart = Color.red(startColor);
        int blueStart = Color.blue(startColor);
        int greenStart = Color.green(startColor);
        int redEnd = Color.red(endColor);
        int blueEnd = Color.blue(endColor);
        int greenEnd = Color.green(endColor);

        int red = (int) (redStart + ((redEnd - redStart) * radio + 0.5));
        int greed = (int) (greenStart + ((greenEnd - greenStart) * radio + 0.5));
        int blue = (int) (blueStart + ((blueEnd - blueStart) * radio + 0.5));
        return Color.argb(255, red, greed, blue);
    }



    public void setProgress(int progress) {
        System.out.println("setProgress = " + progress);
        if (progress > mMaxValue) progress = mMaxValue;
        if (progress < mMinValue) progress = mMinValue;
        mProgressPresent = (progress - mMinValue) * 1.0f / (mMaxValue - mMinValue);
        System.out.println("setProgress present = " + mProgressPresent);
        if (null != mOnProgressChangeListener) {
            mOnProgressChangeListener.onProgressChanged(this, progress, false);
        }
        computeThumbPos(mProgressPresent);
        postInvalidate();
    }

    public int getProgress() {
        return (int) (mProgressPresent * (mMaxValue - mMinValue)) + mMinValue;
    }

    public void setArcColors(int[] colors) {
        mArcColors = colors;
        resetShaderColor();
        postInvalidate();
    }


    public void setMaxValue(int max) {
        mMaxValue = max;
    }

    public void setMinValue(int min) {
        mMinValue = min;
    }


    public void setArcColors(int colorArrayRes) {
        setArcColors(getColorsByArrayResId(getContext(), colorArrayRes));
    }


    private OnProgressChangeListener mOnProgressChangeListener;

    public void setOnProgressChangeListener(OnProgressChangeListener onProgressChangeListener) {
        mOnProgressChangeListener = onProgressChangeListener;
    }

    public interface OnProgressChangeListener {

        void onProgressChanged(CircularSeekBar seekBar, int progress, boolean isUser);


        void onStartTrackingTouch(CircularSeekBar seekBar);

        void onStopTrackingTouch(CircularSeekBar seekBar);
    }
    // endregion -----------------------------------------------------------------------------------
}
