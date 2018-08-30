/*
 * MinMaxSeekBar
 *
 * Copyright (c) 2015 Thierry Margenstern under MIT license
 * http://opensource.org/licenses/MIT
 */

package tm.android.chronos.uicomponent;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import tm.android.chronos.R;
import tm.android.chronos.uicomponent.event.OnSeekBarChangeListener;
import tm.android.chronos.uicomponent.event.SeekBarEvent;
import tm.android.chronos.util.geom.Point;

import java.util.ArrayList;
import java.util.List;

import static tm.android.chronos.uicomponent.MinMaxSeekBar.SEEKBAR_MODE.MAX;
import static tm.android.chronos.uicomponent.MinMaxSeekBar.SEEKBAR_MODE.MIN_MAX;


/**
 * This widget is a seek bar.
 * It as two modes.
 * Classic mode (MAX mode) with max value that vary, min value always 0.
 * Second mode (MIN_MAX mode) allow to change the min value.
 * Min and max are returned as percentage between 0 and  1.
 */
public class MinMaxSeekBar extends View {

    private final static double NOT_SET = -1.0;
    private final Rect rect;
    private final Paint paintDeft;
    private double lvariationForMaxValue; // length variation from finger on screen for max value.
    private double lvariationForMinValue;
    private int barMaxLength; // max lenght of the variable line.
    private int lineEnd; // actual lenght of the variable line.
    private int lineStart = 0;//
    private int lastMin = 0;

    private SEEKBAR_MODE mode;
    private boolean firstPass = true;
    private final List<OnSeekBarChangeListener> onSeekBarChangeListenerList;
    private final SeekBarEvent minEvent;
    private final SeekBarEvent maxEvent;
    private double initialMaxRatio = NOT_SET;
    private double initialMinRatio = 0;

    public MinMaxSeekBar(Context context, AttributeSet attrs) {
        super(context, attrs);
        rect = new Rect();
        paintDeft = new Paint(Paint.ANTI_ALIAS_FLAG);
        paintDeft.setColor(Color.BLUE);
        setBackground(getRootView().getBackground());
        setOnTouchListener(new TouchedListener());
        mode = MAX;
        onSeekBarChangeListenerList = new ArrayList<>();
        minEvent = new SeekBarEvent(SeekBarEvent.TYPE.MIN, 0);
        maxEvent = new SeekBarEvent(SeekBarEvent.TYPE.MAX, 1);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        barMaxLength = w;
        if (initialMaxRatio == NOT_SET)
            lineEnd = barMaxLength;
        else
            lineEnd = (int) (initialMaxRatio * barMaxLength);

        lineStart = (int) (initialMinRatio * barMaxLength);
    }

    private void drawBase(Canvas canvas) {
        if (firstPass) {
            Rect tmpRect = canvas.getClipBounds();
            int n = tmpRect.bottom - tmpRect.top;
            rect.top = n / 2 - 3;
            rect.bottom = n / 2 + 3;
            rect.right = tmpRect.right;
            rect.left = tmpRect.left;
            firstPass = false;
        }

        if (lvariationForMaxValue != NOT_SET) {
            lineEnd += lvariationForMaxValue;
            maxEvent.setValue((double) lineEnd / (double) barMaxLength);
            fireEvent(maxEvent);
        }
        if (lvariationForMinValue != NOT_SET) {
            lineStart += lvariationForMinValue;
            lastMin = lineStart;
            minEvent.setValue((double) lineStart / (double) barMaxLength);
            fireEvent(minEvent);
        }

        rect.left = lineStart;
        rect.right = lineEnd;
        canvas.drawRect(rect, paintDeft);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        drawBase(canvas);
    }

    private void fireEvent(SeekBarEvent event) {
        for (OnSeekBarChangeListener onSeekBarChangeListener : onSeekBarChangeListenerList)
            onSeekBarChangeListener.OnSeekBarChange(event);
    }

    public void addOnSeekBarChangeListener(OnSeekBarChangeListener onSeekBarChangeListener) {
        onSeekBarChangeListenerList.add(onSeekBarChangeListener);
    }

    public void setInitialMaxRatio(double initialMaxRatio) {
        this.initialMaxRatio = initialMaxRatio;
    }

    public void setInitialMinRatio(double initialMinRatio) {
        this.initialMinRatio = initialMinRatio;
    }

    public void setMode(SEEKBAR_MODE mode) {
        if (this.mode == MIN_MAX && mode == MAX) {
            lineStart = 0;
            lvariationForMinValue = NOT_SET;
        } else {
            lineStart = lastMin;
            lvariationForMinValue = 0;
        }
        this.mode = mode;
        requestLayout();
    }

    public enum SEEKBAR_MODE {MAX, MIN_MAX}

    class TouchedListener implements OnTouchListener {
        private final Point initialFinger = new Point();
        private final Point currentFinger = new Point();

        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            if (view.getId() != R.id.seek_bar_custom)
                return false;

            switch (motionEvent.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    initialFinger.setX(motionEvent.getX());
                    initialFinger.setY(motionEvent.getY());
                    break;
                case MotionEvent.ACTION_MOVE:

                    currentFinger.setX(motionEvent.getX());
                    currentFinger.setY(motionEvent.getY());
                    double lvariation = currentFinger.getX() - initialFinger.getX();
                    boolean left = initialFinger.getX() < barMaxLength / 2; //&& currentFinger.getX() < barMaxLength / 2;
                    if (Math.abs(lvariation)>0) {
                        if (!left && mode == MIN_MAX) {
                            lvariationForMinValue = 1.5 * lvariation;
                            lvariationForMaxValue = NOT_SET;
                        } else {
                            lvariationForMaxValue = 1.5 * lvariation;
                            lvariationForMinValue = NOT_SET;
                        }
                        requestLayout();
                    }
                    initialFinger.setX(currentFinger.getX());
                    initialFinger.setY(currentFinger.getY());
                    break;
                case MotionEvent.ACTION_UP:
                    break;
            }
            performClick();
            return true;
        }
    }
}
