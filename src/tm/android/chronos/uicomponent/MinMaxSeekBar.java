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
 *
 */
public class MinMaxSeekBar extends View {

	public static enum SEEKBAR_MODE {MAX, MIN_MAX}

	private Rect rect;
	private Paint paintDeft;
	private double lvariationForMaxValue; // length variation from finger on screen for max value.
	private double lvariationForMinValue;
	private int barMaxLenght; // max lenght of the variable line.
	private int lineEnd; // actual lenght of the variable line.
	private int lineStart = 0;//
	private SEEKBAR_MODE mode;
	private boolean firstPass = true;
	private List<OnSeekBarChangeListener> onSeekBarChangeListenerList;
	private SeekBarEvent minEvent;
	private SeekBarEvent maxEvent;
	private double initialMaxRatio = -1.0;
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
		barMaxLenght = w;
		if (initialMaxRatio == -1.0)
			lineEnd = barMaxLenght;
		else
			lineEnd = (int) (initialMaxRatio * barMaxLenght);

		lineStart = (int) (initialMinRatio * barMaxLenght);
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

		if (lvariationForMaxValue!=-1) {
			lineEnd += lvariationForMaxValue;
			maxEvent.setValue((double) lineEnd / barMaxLenght);
			fireEvent(maxEvent);
		}
		if (lvariationForMinValue!=-1) {
			lineStart += lvariationForMinValue;
			minEvent.setValue((double)lineStart/barMaxLenght);
			fireEvent(minEvent);
		}

		rect.left=lineStart;
		rect.right = lineEnd;
		canvas.drawRect(rect, paintDeft);
	}

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		drawBase(canvas);
	}


	class TouchedListener implements OnTouchListener {

		private final static int DOWN_DONE = 0;
		private final static int WAIT = -1;
		private final static int MOVE = 1;
		private int status = -WAIT;
		private Point initialFinger = new Point();
		private Point currentFinger = new Point();


		@Override
		public boolean onTouch(View view, MotionEvent motionEvent) {
			if (view.getId() != R.id.seek_bar_custom)
				return false;

			switch (motionEvent.getAction()) {
				case MotionEvent.ACTION_DOWN:
					status = DOWN_DONE;
					initialFinger.setX(motionEvent.getX());
					initialFinger.setY(motionEvent.getY());
					break;
				case MotionEvent.ACTION_MOVE:
					if (status == DOWN_DONE) {
						status = MOVE;
					}
					else if (status == MOVE) {
						currentFinger.setX(motionEvent.getX());
						currentFinger.setY(motionEvent.getY());
						double lvariation ;
						if (mode == MIN_MAX) { // Volume Varying
							if (initialFinger.getX() < barMaxLenght / 2 && currentFinger.getX() < barMaxLenght / 2) {
								lvariationForMaxValue=-1;
								lvariation = getVariationForMin(currentFinger.getX(),initialFinger.getX());
								if (lvariation==-1)
									break;
								lvariationForMinValue = lvariation;

							} else if (initialFinger.getX() > barMaxLenght / 2 && currentFinger.getX() > barMaxLenght / 2) {
								lvariationForMinValue=-1;
								lvariation = getVariationForMax(currentFinger.getX(),initialFinger.getX());
								if (lvariation==-1)
									break;
								lvariationForMaxValue = lvariation;
							}
						}
						else {// Volume Fixed
							lvariation = getVariationForMax(currentFinger.getX(),initialFinger.getX());
							if (lvariation==-1)
								break;
							lvariationForMaxValue = lvariation;
						}
						requestLayout();
						initialFinger.setX(currentFinger.getX());
						initialFinger.setY(currentFinger.getY());
					}
					else {
						status = WAIT;
					}

					break;
				case MotionEvent.ACTION_UP:
					status = WAIT;

					break;
			}

			return true;
		}
	}

	private double getVariationForMax(double currentX, double initialX) {
		double lvariation = currentX - initialX;
		if (Math.abs(lvariation) < 1.1)
			return -1;
		if (lineEnd + lvariation < 20 || lineEnd + lvariation > barMaxLenght)
			return -1;
		if ((lineEnd+lvariation)<(lineStart+20))
			return -1;
		return lvariation;

	}
	private double getVariationForMin(double currentX, double initialX) {
		double lvariation = currentX - initialX;
		if (Math.abs(lvariation) < 1.1)
			return -1;
		if (lineStart + lvariation < 20 || lineStart + lvariation > barMaxLenght)
			return -1;
		if ((lineStart+lvariation)>=(lineEnd-20))
			return -1;
		return lvariation;

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
}
