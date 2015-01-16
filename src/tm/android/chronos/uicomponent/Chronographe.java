/*
 * Chronographe
 *
 * Copyright (c) 2014 Thierry Margenstern under MIT license
 * http://opensource.org/licenses/MIT
 */

package tm.android.chronos.uicomponent;

import android.app.Activity;
import android.app.DialogFragment;
import android.app.FragmentManager;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.ColorDrawable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.View;
import android.widget.Toast;
import tm.android.chronos.R;
import tm.android.chronos.core.*;
import tm.android.chronos.core.Units.UPDATE_TYPE;
import tm.android.chronos.dialogs.ChronographeDialog;
import tm.android.chronos.dialogs.OnDialogClickListener;

import java.util.Vector;

import static tm.android.chronos.core.Units.CHRONO_TYPE.*;
import static tm.android.chronos.core.Units.UPDATE_TYPE.*;


public class Chronographe<T extends Stopwatch> extends BaseChronographe implements SurfaceHolder.Callback, SurfaceViewRenderer<T> {

	private Canvas cachedCanvas = null;
	private Rect rect;
	private T touchedStopwatch;
	private Bitmap bufferBitmap;
	private ClockWorker<T> clockWorker;
	private Rect viewPort;
	private Rect scrollView;
	private int scrollOffset = 0;
	private int lastY; // last vertical position of the line of the last stopwatch;
	private boolean viewPortHeighSet = false;

	public Chronographe(Context context) {
		super(context);
		init();
	}

	private void init() {
		rect = new Rect();

		//stopwatch = new Stopwatch(this);
		getHolder().addCallback(this);
		clockWorker = new ClockWorker<T>(this);
		setClickable(true);
		setKeepScreenOn(true);
		setBackground(new ColorDrawable(Color.TRANSPARENT));
		setDrawingCacheEnabled(true);
		setOnTouchListener(new OnTouch());

		bufferBitmap = Bitmap.createBitmap(screenWidth, Resources.getSystem().getDisplayMetrics().heightPixels, Bitmap.Config.ARGB_8888);
		cachedCanvas = new Canvas(bufferBitmap);
		viewPort = new Rect(0, 0, screenWidth, 0);
		scrollView = new Rect(0, 0, screenWidth, 0);
	}

	public Chronographe(Context context, AttributeSet attributeSet) {
		super(context, attributeSet);
		init();
	}

	@Override
	public void surfaceCreated(SurfaceHolder surfaceHolder) {
		if (clockWorker.getState() == Thread.State.NEW) {
			clockWorker.start();
			// add a first stopwatch
			addNewStopwatch();
		}
		else if (clockWorker.isDisplayStopped()) {
			clockWorker.setStopDisplay(false);
			// force rendering if all are waiting to start
			if (clockWorker.noneChronoIsRunning()) updateUI();
		}
	}

	@Override
	public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2) {
		if (!viewPortHeighSet) {// set view port height once.
			viewPort.bottom = i2;
			scrollView.bottom = i2;
			viewPortHeighSet = true;
			updateUI();
		}
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
		clockWorker.setStopDisplay(true);
	}


	public void updateUI() {
		renderOnCache(clockWorker.getClockList());
		copyCacheToScreen();
	}

	@Override
	public void renderOnCache(Vector<T> clockList) {
		/*
		  synchronize the access on clockList because it is used from the thread (ClockWorker) and some other time
          directly from here especially when adding and removing stopwatches while there is at least
          one running stopwatches. This is the only place where synchronize on clockList is working fine.

          Any other place (synchronized getClockList and use it every where (also change renderOnCache(clockList) to renderOnCache()) to access the list, or
          inside run() of ClockWorker synchronize(clockList){renderOnCache(clockList)} ) doesn't work.
        */
		int i = 0;
		synchronized (clockList) {
			for (T stopwatch : clockList) {
				if (stopwatch.getStopwatchUi().mustUpdateUI()) {
					renderOnCacheAll(stopwatch, i);
				}
				else if (stopwatch.isRunning()) renderOnCacheRunning(stopwatch, i);
				i++;
			}
		}
	}

	@Override
	public void renderOnScreen(Canvas canvas) {
		if (canvas == null) return;
		canvas.drawRect(viewPort, paintBackgroundBlack);
		canvas.drawBitmap(bufferBitmap, scrollView, viewPort, null);
	}

	private void copyCacheToScreen() {
		Canvas canvas1 = getHolder().lockCanvas();
		renderOnScreen(canvas1);
		getHolder().unlockCanvasAndPost(canvas1);
	}

	private void renderOnCacheAll(T stopwatch, int i) {
		int offSet = getOffset(i);
		for (UPDATE_TYPE updateType : stopwatch.getStopwatchUi().getUpdateTypes())
			switch (updateType) {
				case DELETE:
					renderOnCacheDelete(stopwatch, offSet);
					break;
				case ADD_NEW:
					renderOnCacheAddNew(stopwatch, offSet, NONE);
					break;
				case UPDATE_HEAD_DIGIT:
					if (stopwatch.getStopwatchData().getLapDistance()>0 && (stopwatch.getStopwatchData().getChronoType()==LAPS || stopwatch.getStopwatchData().getChronoType()==SEGMENTS))
						renderOnCacheHeadLineDigit(stopwatch, new Pwrapper<Integer>(offSet +SPACING+3*normalTextBlocHeight));
					else
						renderOnCacheHeadLineDigit(stopwatch, new Pwrapper<Integer>(offSet +SPACING+normalTextBlocHeight));
					break;
				case UPDATE_HEAD_LINE1:
					renderOnCacheHeadLine1(stopwatch, new Pwrapper<Integer>(offSet + SPACING), true);
					break;
				case UPDATE_HEAD_LINE2:
					renderOnCacheHeadLine2(stopwatch, new Pwrapper<Integer>(offSet + SPACING + normalTextBlocHeight), true);
					break;
				case UPDATE_HEAD_LINE3:
					renderOnCacheHeadLine3(stopwatch, new Pwrapper<Integer>(offSet + SPACING + 2 * normalTextBlocHeight), true);
					break;
				case HEAD_CHANGE_SIZE:
					renderOnCacheRebuildFromPosition(offSet, i, NONE);
					break;
				case REMOVE_DETAILS:
					renderOnCacheRebuildFromPosition(offSet, i, REMOVE_DETAILS);
				case COLLAPSE_DETAILS:
					renderOnCacheRebuildFromPosition(offSet, i, COLLAPSE_DETAILS);
					break;
				case EXPAND_DETAILS:
					renderOnCacheRebuildFromPosition(offSet, i, EXPAND_DETAILS);
					break;
				default:
					break;
			}
		stopwatch.getStopwatchUi().clearUpdateType();
	}

	private void renderOnCacheRunning(T stopwatch, int i) {
		int offSet = getOffset(i);
		int blBottomVPos = offSet + SPACING+normalTextBlocHeight;
		if (stopwatch.getStopwatchData().getLapDistance()>0 && (stopwatch.getStopwatchData().getChronoType()==LAPS || stopwatch.getStopwatchData().getChronoType()==SEGMENTS))
			blBottomVPos+=2*normalTextBlocHeight;


		rect.set(screenWidth - SPACING - digitMaxLength, blBottomVPos, screenWidth - SPACING, blBottomVPos+digitTextBlocHeight);
		cachedCanvas.drawRect(rect, paintBackgroundBlack);
		cachedCanvas.drawText(stopwatch.getTime().toString(), screenWidth - SPACING, blBottomVPos + digitTextBaseLineVerticalOffset, paintDigitsWhileRunning);
	}

	private int getOffset(int i) {
		int offset = 0;
		for (int j = 0; j < i; j++)
			offset += clockWorker.get(j).getStopwatchUi().getHeadHeight() + clockWorker.get(j).getStopwatchUi().getDetailsHeight();
		return offset;
	}

	private void renderOnCacheDelete(T stopwatch, int offSet) {
		// for now only the last stopwatch can be removed, excepted the first one.
		rect.set(0, offSet, screenWidth, offSet + stopwatch.getStopwatchUi().getHeadHeight() + stopwatch.getStopwatchUi().getDetailsHeight());
		cachedCanvas.drawRect(rect, paintBackgroundBlack);
		clockWorker.remove(stopwatch);
		lastY -= stopwatch.getStopwatchUi().getHeadHeight() + stopwatch.getStopwatchUi().getDetailsHeight();
	}

	private void renderOnCacheAddNew(T stopwatch, int offSet, UPDATE_TYPE updateType) {
		Pwrapper<Integer> blBottomVPos = new Pwrapper<Integer>(offSet + SPACING);
		renderOnCacheHeadLine1(stopwatch, blBottomVPos, false);

		if (stopwatch.getStopwatchData().getChronoType() != Units.CHRONO_TYPE.SIMPLE) {
			renderOnCacheHeadLine2(stopwatch, blBottomVPos, false);
		}

		if (stopwatch.getStopwatchData().getChronoType() == Units.CHRONO_TYPE.LAPS || stopwatch.getStopwatchData().getChronoType() == Units.CHRONO_TYPE.SEGMENTS) {
			renderOnCacheHeadLine3(stopwatch, blBottomVPos, false);
		}

		renderOnCacheHeadLineDigit(stopwatch, blBottomVPos);

		renderOnCacheHorizontalLine(blBottomVPos, groupBottomLineHeight, colorBottomGroup);

		// update head height
		stopwatch.getStopwatchUi().setHeadHeight(blBottomVPos.value() - offSet);

		// render the children
		if (stopwatch.getStopwatchData().hasDataRow() || updateType == REMOVE_DETAILS) {
			renderOnCacheAddDetail(stopwatch, blBottomVPos, updateType);
		}


		// update lastY
		lastY += stopwatch.getStopwatchUi().getHeadHeight() + stopwatch.getStopwatchUi().getDetailsHeight();
	}

	private void renderOnCacheHeadLineDigit(T stopwatch, Pwrapper<Integer> blBottomVPos) {
		// third (or second line)
		if (stopwatch.isStopped()) {
			paintDigit.setColor(colorStopped);
		}
		else if (stopwatch.isRunning()) {
			paintDigit.setColor(colorRunning);
		}
		else {
			paintDigit.setColor(colorReseted);
		}

		rect.set(0, blBottomVPos.value(), screenWidth, blBottomVPos.value() + digitTextBlocHeight);
		cachedCanvas.drawRect(rect, paintBackgroundBlack);
		cachedCanvas.drawText(stopwatch.getTime().toString(), screenWidth - SPACING, blBottomVPos.value() + digitTextBaseLineVerticalOffset, paintDigit);
		blBottomVPos = blBottomVPos.add(digitTextBlocHeight);
	}

	private void renderOnCacheHeadLine1(T stopwatch, Pwrapper<Integer> blBottomVPos, boolean replace) {
		// entering each bloc that whrite on cachedCanvas
		// blBottomVPos is the bottom position off the previous bloc (ie the top of this one)
		// first line name+type (always)
		if (replace) {
			rect.set(0, blBottomVPos.value(), screenWidth, blBottomVPos.value() + normalTextBlocHeight);
			cachedCanvas.drawRect(rect, paintBackgroundBlack);
		}
		cachedCanvas.drawText(stopwatch.getName(), SPACING, blBottomVPos.value() + normalTextBaseLineVerticalOffset, paintNormalTextWhiteLeft);
		cachedCanvas.drawText(stopwatch.getStopwatchData().getChronoType().toString(), screenWidth - SPACING, blBottomVPos.value() + normalTextBaseLineVerticalOffset, paintNormalTextWhiteRigth);
		blBottomVPos = blBottomVPos.add(normalTextBlocHeight);
	}

	private void renderOnCacheHeadLine2(T stopwatch, Pwrapper<Integer> blBottomVPos, boolean replace) {
		if (stopwatch.getStopwatchData().getLapDistance() > 0) {
			if (replace) {
				rect.set(0, blBottomVPos.value(), screenWidth, blBottomVPos.value() + normalTextBlocHeight);
				cachedCanvas.drawRect(rect, paintBackgroundBlack);
			}
			cachedCanvas.drawText(stopwatch.getStopwatchData().getInfoL2(), SPACING, blBottomVPos.value() + normalTextBaseLineVerticalOffset, paintNormalTextWhiteLeft);
			blBottomVPos = blBottomVPos.add(normalTextBlocHeight);
		}
	}

	private void renderOnCacheHeadLine3(T stopwatch, Pwrapper<Integer> blBottomVPos, boolean replace) {
		if (stopwatch.getStopwatchData().getLapDistance() > 0) {
			if (replace) {
				rect.set(0, blBottomVPos.value(), screenWidth, blBottomVPos.value() + normalTextBlocHeight);
				cachedCanvas.drawRect(rect, paintBackgroundBlack);
			}
			cachedCanvas.drawText(stopwatch.getStopwatchData().getInfoL3(), SPACING, blBottomVPos.value() + normalTextBaseLineVerticalOffset, paintNormalTextWhiteLeft);
			blBottomVPos = blBottomVPos.add(normalTextBlocHeight);
		}
	}

	// index = index of the stopwatch to rebuild from
	// offset the start position in the cachedCanvas
	private void renderOnCacheRebuildFromPosition(int startPosition, int index, UPDATE_TYPE updateType) {
		renderOnCacheDeleteToTheEnd(startPosition);

		for (int i = index; i < clockWorker.getClockList().size(); i++) {
			renderOnCacheAddNew(clockWorker.get(i), startPosition, (i == index ? updateType : NONE));
			startPosition += clockWorker.get(i).getStopwatchUi().getHeadHeight() + clockWorker.get(i).getStopwatchUi().getDetailsHeight();
		}
	}

	private void renderOnCacheHorizontalLine(Pwrapper<Integer> blBottomVPos, int height, int color) {
		// sep line
		paintLine.setColor(color);
		rect.set(0, blBottomVPos.value(), screenWidth, blBottomVPos.value() + height);
		cachedCanvas.drawRect(rect, paintLine);
		blBottomVPos = blBottomVPos.add(height);
	}

	private void renderOnCacheAddDetail(T stopwatch, Pwrapper<Integer> pos, UPDATE_TYPE updateType) {
		// a sep line

		if (updateType == COLLAPSE_DETAILS || updateType == REMOVE_DETAILS || (updateType == NONE && !stopwatch.getStopwatchUi().isExpanded())) {
			//display a sign + (inside the digit bloc but to the right
			cachedCanvas.drawText("+", 8 * SPACING, pos.value() - digitTextBlocHeight + digitTextBaseLineVerticalOffset-groupBottomLineHeight, paintWhiteRigthDigitSize);
			stopwatch.getStopwatchUi().setExpanded(false);
			stopwatch.getStopwatchUi().setDetailsHeight(0);
		}
		else if (updateType == EXPAND_DETAILS || (updateType == NONE && stopwatch.getStopwatchUi().isExpanded())) {
			//display a sign - (inside the digit bloc but to the right
			cachedCanvas.drawText("-", 8 * SPACING, pos.value() - digitTextBlocHeight + digitTextBaseLineVerticalOffset-groupBottomLineHeight, paintWhiteRigthDigitSize);
			// at the bottom of the bottom line.
			// remove it
			rect.set(0,pos.value()-groupBottomLineHeight,screenWidth,pos.value());
			cachedCanvas.drawRect(rect,paintBackgroundBlack);
			pos.sub(groupBottomLineHeight);// it is possible to do this because all calculation are made from top to down,
			int n = stopwatch.getStopwatchUi().getHeadHeight(); // but we must update the height of the Head.
			n-=groupBottomLineHeight;
			stopwatch.getStopwatchUi().setHeadHeight(n);
			// retain position to calculate at this end the height of this detail bloc.
			int localStart = pos.value();
			renderOnCacheHorizontalLine(pos, groupBottomLineHeight/2, colorSepHeadDetail);
			//
			for (StopwatchDataRow stopwatchDataRow : stopwatch.getStopwatchData().getTimeList()) {
				cachedCanvas.drawText(stopwatchDataRow.getLine(), screenWidth - SPACING, pos.value() + smallTextBaseLineVerticalOffset, paintSmallTextWhiteRigth);
				pos.add(smallTextBlocHeight);
			}
			pos.add(SPACING);
			renderOnCacheHorizontalLine(pos, groupBottomLineHeight, colorBottomGroup);
			stopwatch.getStopwatchUi().setDetailsHeight(pos.sub(localStart).value());
			stopwatch.getStopwatchUi().setExpanded(true);
		}
	}

	private void renderOnCacheDeleteToTheEnd(int startPosition) {
		if (startPosition >= lastY) return;
		rect.set(0, startPosition, screenWidth, lastY);
		cachedCanvas.drawRect(rect, paintBackgroundBlack);
		lastY = startPosition;
	}

	public void addNewStopwatch() {
		// does the height of the cache fit to add a new stopwatch ?
		if (lastY + fullHeight > bufferBitmap.getHeight()) {
			// no, we must expand
			if (!changeBufferBitmapHeight(bufferBitmap.getHeight() + 3 * fullHeight)) {
				if (!changeBufferBitmapHeight(bufferBitmap.getHeight() + 2 * fullHeight)) // retry !
				{
					if (!changeBufferBitmapHeight(bufferBitmap.getHeight() + fullHeight)) return; // return if failed
				}
			}
		}


		T stopwatch = StopwatchFactory.create();
		stopwatch.setName("Chrono-" + clockWorker.getClocksCount());
		stopwatch.getStopwatchUi().addUpdateType(ADD_NEW);
		clockWorker.register(stopwatch);
		if (clockWorker.noneChronoIsRunning()) updateUI();
		//updateLastY();

	}

	private boolean changeBufferBitmapHeight(int newHeight) {
		// if newHeight > bufferBitmap Height ----> expand to newHeight
		// if newHeight < bufferBitmap Height ----> collapse to newHeight
		try {

			Bitmap b = Bitmap.createBitmap(bufferBitmap.getWidth(), newHeight, Bitmap.Config.ARGB_8888);
			Canvas dest = new Canvas(b);
			Rect view = new Rect();
			if (newHeight > bufferBitmap.getHeight()) {
				view.set(0, 0, bufferBitmap.getWidth(), bufferBitmap.getHeight());
			}
			else {
				view.set(0, 0, bufferBitmap.getWidth(), newHeight);
			}

			dest.drawBitmap(bufferBitmap, view, view, null);
			bufferBitmap = null;
			bufferBitmap = b;
			cachedCanvas = new Canvas(bufferBitmap);
			updateUI();
			return true;
		} catch (RuntimeException e) {
			Toast.makeText(getContext(), (clockWorker.getClocksCount() + "" + getContext().getString(R.string.msg_max_stopwatch_count)), Toast.LENGTH_LONG).show();
			return false;
		}
	}

	public void removeLastStopwatch() {

		int size = clockWorker.getClocksCount();
		if (size <= 1) {
			Toast.makeText(getContext(), getContext().getString(R.string.msg_first_stopwatch_no_delete), Toast.LENGTH_LONG).show();
			return;
		}
		Stopwatch stopwatch = clockWorker.get(size - 1);
		if (stopwatch.isRunning()) {
			Toast.makeText(getContext(), getContext().getString(R.string.msg_only_delete_last), Toast.LENGTH_LONG).show();
			return;
		}
		stopwatch.getStopwatchUi().addUpdateType(DELETE);
		if (clockWorker.noneChronoIsRunning()) updateUI();


		//the initial cachedCanvas height is about 1000  and can contain about 4 stopwatches.
		//When remove stopwatch ,  we must decrease some times the height to regain memory. and never go down to an height of say 1000.
			if (bufferBitmap.getHeight() - lastY > 1000 && lastY > 1000) {
				// parm must be new total size
				changeBufferBitmapHeight(lastY + 100);
		}
	}

	/* return the stopwatch found at this y position on screen
	 */
	private T getStopwatch(float y) {
		// y is on screen, we must translate it into the cachedCanvas coordinate
		float yCached = y;
		yCached += scrollOffset;
		if (yCached > lastY) return null;
		int computedY = 0;
		for (T stopwatch : clockWorker.getClockList()) { // sum of height from the top, at each iteration computedY is at the end of a bloc (head+detail)
			computedY += stopwatch.getStopwatchUi().getHeadHeight() + stopwatch.getStopwatchUi().getDetailsHeight();
			if (yCached < computedY) return stopwatch; // ycached was not in the previous bloc, if in this on, good, return
		}
		return null;
	}

	/*
		Instantiated in this class but needed outside.
	 */
	public ClockWorker<T> getClockWorker() {
		return clockWorker;
	}
	/*
	   All event on the main screen (not the dialogs windows)
	 */
	private class OnTouch implements OnTouchListener {

		private final int WAIT = -1;
		private final int DOWN_DONE = 0;
		private final int MOVE = 1;
		private int status = WAIT;
		private long now;
		private float lastFingerY = 0;

		@Override
		public boolean onTouch(View view, MotionEvent event) {


			long now = System.currentTimeMillis();


			System.out.println("[EVENT] History size " + event.getHistorySize());
			System.out.println("[EVENT] pointer size " + event.getPointerCount());
			if (status == WAIT && event.getAction() == MotionEvent.ACTION_DOWN) {
				this.now = now;
				lastFingerY = event.getRawY();
				System.out.println("[EVENT] wait+down");
				status = DOWN_DONE;
				return true;
			}
			if (status == DOWN_DONE && event.getAction() == MotionEvent.ACTION_MOVE) {
				status = MOVE;
				System.out.println("[EVENT] down_done+move");
				return true;
			}
			if (status == MOVE && event.getAction() == MotionEvent.ACTION_MOVE) {
				System.out.println("[EVENT] move+move");


				if (event.getY() > lastFingerY) {
					if (scrollView.top >= 25) {
						scrollView.top -= 25;
						scrollView.bottom -= 25;
						scrollOffset -= 25;
					}
				}
				else {
					if (lastY - scrollOffset > viewPort.bottom) {
						scrollView.top += 25;
						scrollView.bottom += 25;
						scrollOffset += 25;
					}
				}
				if (clockWorker.noneChronoIsRunning()) updateUI();
				lastFingerY = event.getY();
				System.out.println("scrollView" + scrollView.toShortString());
				System.out.println("scrollOffset " + scrollOffset);
				return true;//
			}
			if (status == MOVE && event.getAction() == MotionEvent.ACTION_UP) {
				//
				System.out.println("[EVENT] move+up");
				status = WAIT;
				return true;
			}
			if (status == DOWN_DONE && event.getAction() == MotionEvent.ACTION_UP) {
				// like a click
				System.out.println("[EVENT] down_done+up");
				status = WAIT;

				touchedStopwatch = getStopwatch(event.getY());
				if (touchedStopwatch == null) return true;

				switch (getUserAction(event.getX())) {
					case START_STOP_RESET:
						if (touchedStopwatch.isWaitingStart()) {
							touchedStopwatch.start(this.now);
						}
						else if (touchedStopwatch.isRunning()) {
							touchedStopwatch.stopTime(this.now);
							updateUI();
						}
						else if (touchedStopwatch.isStopped()) {
							touchedStopwatch.reset();
							updateUI();
						}

						break;
					case LAP_TIME:
						if (touchedStopwatch.isRunning()) touchedStopwatch.lapTime(this.now);
						break;
					case PARAM:
						if (touchedStopwatch.isRunning())
							break;
						ChronographeDialog<T> dialog = new ChronographeDialog<T>();
						FragmentManager fm = ((Activity) getContext()).getFragmentManager();
						dialog.setStopwatch(touchedStopwatch);
						dialog.setDialogClickListener(new DialogListener());
						dialog.show(fm, "Dialog-Chronographe");
						break;
					case SHOW_HIDE:
						if (touchedStopwatch.getStopwatchData().hasDataRow()) {
							if (touchedStopwatch.getStopwatchUi().isExpanded()) {
								touchedStopwatch.getStopwatchUi().addUpdateType(COLLAPSE_DETAILS);
							}
							else {
								touchedStopwatch.getStopwatchUi().addUpdateType(EXPAND_DETAILS);
							}
							if (clockWorker.noneChronoIsRunning()) updateUI();
						}
						break;
				}
			}


			return true;
		}
	}


	/*
		Controller for the dialog to manage a stopwatch's parameters.
	 */
	private class DialogListener implements OnDialogClickListener {

		@Override
		public void onDialogPositiveClick(DialogFragment dialog) {
			// Name
			ChronographeDialog cdialog = (ChronographeDialog) dialog;
			if (cdialog.getName() != null && !cdialog.getName().equals("")) {
				if (!touchedStopwatch.getName().equals(cdialog.getName())) {
					touchedStopwatch.getStopwatchUi().addUpdateType(UPDATE_HEAD_LINE1);
				}
				touchedStopwatch.setName(cdialog.getName());
			}

			// chrono type
			if (cdialog.getType() != null) {
				Units.CHRONO_TYPE ctype = touchedStopwatch.getStopwatchData().getChronoType();
				if (ctype != cdialog.getType()) {
					if (((ctype == LAPS || ctype == SEGMENTS) && (cdialog.getType() == SIMPLE || cdialog.getType() == PREDEFINED_TIMES)) || ((ctype == SIMPLE || ctype == PREDEFINED_TIMES) && (cdialog.getType() == LAPS || cdialog.getType() == SEGMENTS))) {
						touchedStopwatch.getStopwatchUi().addUpdateType(HEAD_CHANGE_SIZE);
					}
					else {
						touchedStopwatch.getStopwatchUi().addUpdateType(UPDATE_HEAD_LINE1);
					}
				}
				touchedStopwatch.getStopwatchData().setChronoType(cdialog.getType());
			}

			if (cdialog.getType() == Units.CHRONO_TYPE.LAPS || cdialog.getType() == Units.CHRONO_TYPE.SEGMENTS) {

				if (cdialog.getDistance() >= 0) {
					if (touchedStopwatch.getStopwatchData().getLapDistance() != cdialog.getDistance()) {
						touchedStopwatch.getStopwatchUi().addUpdateType(UPDATE_HEAD_LINE3);
					}
					touchedStopwatch.getStopwatchData().setLapDistance(cdialog.getDistance());
				}

				if (cdialog.getLengthUnit() != null) {
					if (touchedStopwatch.getStopwatchData().getLengthUnit() != cdialog.getLengthUnit()) {
						touchedStopwatch.getStopwatchUi().addUpdateType(UPDATE_HEAD_LINE3);
					}
					touchedStopwatch.getStopwatchData().setLengthUnit(cdialog.getLengthUnit());
				}


				if (cdialog.getSpeedUnit() != null) {
					if (touchedStopwatch.getStopwatchData().getGlobalDistance() > 0 && touchedStopwatch.getStopwatchData().getGlobalTime() > 0 && touchedStopwatch.getStopwatchData().getSpeedUnit() != cdialog.getSpeedUnit()) {
						touchedStopwatch.getStopwatchUi().addUpdateType(UPDATE_HEAD_LINE2);
					}
					touchedStopwatch.getStopwatchData().setSpeedUnit(cdialog.getSpeedUnit());
				}
			}
			if (clockWorker.noneChronoIsRunning()) updateUI();
		}

		@Override
		public void onDialogNegativeClick(DialogFragment dialog) {

		}
	}
}
