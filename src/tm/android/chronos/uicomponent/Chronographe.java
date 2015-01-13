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
import android.graphics.*;
import android.graphics.drawable.ColorDrawable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.View;
import android.widget.Toast;
import tm.android.chronos.core.*;
import tm.android.chronos.dialogs.ChronographeDialog;
import tm.android.chronos.dialogs.OnDialogClickListener;


import java.util.Vector;

public class Chronographe<T extends Stopwatch> extends BaseChronographe implements SurfaceHolder.Callback, SurfaceViewRenderer<T> {

	private Canvas cachedCanvas = null;
	private Rect rect;
	private T touchedStopwatch;
	private Bitmap bufferBitmap;
	private ClockWorker<T> clockWorker;
	private  Rect viewPort;
	private Rect scrollView;
	private int scrollOffset = 0;
	private int lastY; // last vertical position of the line of the last stopwatch;
	private boolean viewPortHeighSet = false;

	public Chronographe(Context context) {
		super(context);
		init();

	}

	public Chronographe(Context context, AttributeSet attributeSet) {
		super(context, attributeSet);
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
		//setOnClickListener(new OnClick());
		bufferBitmap = Bitmap.createBitmap(getScreenWidth(), Resources.getSystem().getDisplayMetrics().heightPixels, Bitmap.Config.ARGB_8888);
		cachedCanvas = new Canvas(bufferBitmap);
		viewPort = new Rect(0, 0, getScreenWidth(), 0);
		scrollView = new Rect(0, 0, getScreenWidth(), 0);

	}

	@Override
	public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2) {
		if (!viewPortHeighSet) {// set view port height once.
			viewPort.bottom= i2;
			scrollView.bottom= i2;
			viewPortHeighSet=true;
			updateUI();
		}
	}

	@Override
	public void surfaceCreated(SurfaceHolder surfaceHolder) {
		if (clockWorker.getState() == Thread.State.NEW) {
			clockWorker.start();
			// add a first stopwatch
			T stopwatch1 = StopwatchFactory.create();
			stopwatch1.setName("Chrono-1");
			clockWorker.register(stopwatch1);
			updateLastY();
			//initial rendering
			updateUI();
		} else if (clockWorker.isDisplayStopped()) {
			clockWorker.setStopDisplay(false);
			// force rendering if all are waiting to start
			if (clockWorker.noneChronoIsRunning())
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

	private void copyCacheToScreen(){
		Canvas canvas1 = getHolder().lockCanvas();
		renderOnScreen(canvas1);
		getHolder().unlockCanvasAndPost(canvas1);
	}

	private class OnTouch implements OnTouchListener {
		private final int WAIT = -1;
		private final int DOWN_DONE = 0;
		private final int MOVE = 1;
		private int status = WAIT;
		private long now;
		private float lastY = 0;

		@Override
		public boolean onTouch(View view, MotionEvent event) {


			long now = System.currentTimeMillis();


			System.out.println("[EVENT] History size " + event.getHistorySize());
			System.out.println("[EVENT] pointer size " + event.getPointerCount());
			if (status == WAIT && event.getAction() == MotionEvent.ACTION_DOWN) {
				this.now = now;
				lastY = event.getRawY();
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
				if (event.getY() > lastY) {
					if (scrollView.top >= 5) {
						scrollView.top -= 5;
						scrollView.bottom -= 5;
						scrollOffset -= 5;
					}
				} else {

					scrollView.top += 5;
					scrollView.bottom += 5;
					scrollOffset += 5;

				}
				if (clockWorker.noneChronoIsRunning())
					updateUI();
				lastY = event.getY();
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
				if (touchedStopwatch == null)
					return true;

				switch (getUserAction(event.getX())) {
					case START_STOP_RESET:
						if (touchedStopwatch.isWaitingStart())
							touchedStopwatch.start(this.now);
						else if (touchedStopwatch.isRunning()) {
							touchedStopwatch.stopTime(this.now);
							updateUI();
						} else if (touchedStopwatch.isStopped()) {
							touchedStopwatch.reset();
							updateUI();
						}

						break;
					case LAP_TIME:
						touchedStopwatch.lapTime(this.now);
						break;
					case PARAM:
						ChronographeDialog<T> dialog = new ChronographeDialog<T>();
						FragmentManager fm = ((Activity) getContext()).getFragmentManager();
						dialog.setStopwatch(touchedStopwatch);
						dialog.setDialogClickListener(new DialogListener());
						dialog.show(fm, "Dialog-Chronographe");
						break;
					case SHOW_HIDE:
						break;
				}

			}


			return true;
		}
	}


	public void addNewStopwatch(){
		// does the height of the cache fit to add a new stopwatch ?
		if (lastY+fullHeight>bufferBitmap.getHeight()){
			// no, we must expand
			if (!changeBufferBitmapHeight(bufferBitmap.getHeight() + 3 * fullHeight))
				return ; // return if failed
		}


		T stopwatch = StopwatchFactory.create();
		clockWorker.register(stopwatch);
		stopwatch.setName("Chrono-" + clockWorker.getClocksCount());
		if (clockWorker.noneChronoIsRunning())
			updateUI();
		updateLastY();

	}

	private boolean changeBufferBitmapHeight(int newHeight) {
		// if newHeight > bufferBitmap Height ----> expand to newHeight
		// if newHeight < bufferBitmap Height ----> collapse to newHeight
		try {

			Bitmap b = Bitmap.createBitmap(bufferBitmap.getWidth(), newHeight, Bitmap.Config.ARGB_8888);
			Canvas dest = new Canvas(b);
			Rect view = new Rect();
			if (newHeight>bufferBitmap.getHeight())
				view.set(0, 0, bufferBitmap.getWidth(), bufferBitmap.getHeight());
			else
				view.set(0,0,bufferBitmap.getWidth(),newHeight);

			dest.drawBitmap(bufferBitmap, view, view, null);
			bufferBitmap = null;
			bufferBitmap = b;
			cachedCanvas = new Canvas(bufferBitmap);
			updateUI();
			return true;
		} catch (RuntimeException e){
			Toast.makeText(getContext(),clockWorker.getClocksCount()+" chronomètres !!!!! On ne peut plus ajouter de chronomètre",Toast.LENGTH_LONG).show();
			return false;
		}

	}

	public void removeLastStopwatch(){
		// No need to update lastY here, it is done in renderOnCacheAll with the flag mustDelete.
		// the initial cachedCanvas height is about 1000  and can contain about 4 stopwatches.
		// When remove stopwatch ,  we must decrease some times the height to regain memory. and never go down to an height of say 1000.
		if (bufferBitmap.getHeight()-lastY>600 && lastY>1000){
			if (!changeBufferBitmapHeight(lastY-600))
				return;
		}

		int size = clockWorker.getClocksCount();
		if (size <= 1) {
			Toast.makeText(getContext(),"Vous ne pouvez pas supprimer le premier chronomètre.",Toast.LENGTH_LONG).show();
			return;
		}
		Stopwatch stopwatch = clockWorker.get(size - 1);
		if (stopwatch.isRunning()){
			Toast.makeText(getContext(),"Vous ne pouvez supprimer que le dernier chronomètre or il tourne !",Toast.LENGTH_LONG).show();
			return;
		}
		stopwatch.setMustDelete();
		if (clockWorker.noneChronoIsRunning())
			updateUI();

	}




	public void updateLastY() {
		lastY += fullHeight;
	}

	private T getStopwatch(float y) {
		// y is on screen, we must translate it into the cachedCanvas coordinate
		float yCached = y;
		yCached += scrollOffset;
		if (yCached > lastY)
			return null;
		int id = (int) (yCached / fullHeight);

		return clockWorker.getClockList().get(id);


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
				if (stopwatch.mustUpdateUI())
					renderOnCacheAll(stopwatch, i);
				else if (stopwatch.isRunning())
					renderOnCacheRunning(stopwatch, i);
				i++;
			}
		}
	}




	private void renderOnCacheAll(T stopwatch, int i) {
		int offSet = fullHeight  * i;
		//rect = cachedCanvas.getClipBounds();
		rect.set(0, offSet, getScreenWidth(), offSet + fullHeight);
		cachedCanvas.drawRect(rect, paintWileWaitToStart);
		if (stopwatch.mustDelete()){
			clockWorker.remove(stopwatch);
			lastY-=fullHeight;
			return;
		}
		cachedCanvas.drawText(stopwatch.getName(), SPACING, offSet + topLineVerticalPosition, paintOtherTextWhiteLeft);
		cachedCanvas.drawText(stopwatch.getStopwatchData().getInfo(), SPACING, offSet + middleLineVerticalPosition, paintOtherTextWhiteLeft);
		cachedCanvas.drawText(stopwatch.getTime().toString(), getScreenWidth() - SPACING, offSet + bottomLineVerticalPosition, paintWhiteRight);
		rect.set(0, offSet + fullHeight - SPACING - SPACING / 2, getScreenWidth(), offSet + fullHeight - SPACING);
		cachedCanvas.drawRect(rect, paintLine);
		stopwatch.setMustUpdateUI(false);
	}


	private void renderOnCacheRunning(T stopwatch, int i) {
		int offSet = fullHeight * i;
		rect.set(getScreenWidth() - SPACING - allDigitWidth, offSet + middleLineVerticalPosition, getScreenWidth() - SPACING, offSet + fullHeight);
		//cachedCanvas.clipRect(rect);
		cachedCanvas.drawRect(rect, paintWhileRunning);
		cachedCanvas.drawText(stopwatch.getTime().toString(), getScreenWidth() - SPACING, offSet + bottomLineVerticalPosition, paintWhiteRight);

	}


	@Override
	public void renderOnScreen(Canvas canvas) {
		if (canvas == null)
			return;
		canvas.drawRect(viewPort, paintWileWaitToStart);
		canvas.drawBitmap(bufferBitmap, scrollView, viewPort, null);


	}

	public ClockWorker<T> getClockWorker() {
		return clockWorker;
	}


	private class DialogListener implements OnDialogClickListener {
		@Override
		public void onDialogNegativeClick(DialogFragment dialog) {

		}

		@Override
		public void onDialogPositiveClick(DialogFragment dialog) {
			ChronographeDialog cdialog = (ChronographeDialog) dialog;
			if (cdialog.getName() != null && !cdialog.getName().equals(""))
				touchedStopwatch.setName(cdialog.getName());


			if (cdialog.getType() != null)
				touchedStopwatch.getStopwatchData().setChronoType(cdialog.getType());

			if (cdialog.getType() != Units.CHRONO_TYPE.PREDEFINED_TIMES) {

				if (cdialog.getLengthUnit() != null)
					touchedStopwatch.getStopwatchData().setLengthUnit(cdialog.getLengthUnit());


				if (cdialog.getSpeedUnit() != null)
					touchedStopwatch.getStopwatchData().setSpeedUnit(cdialog.getSpeedUnit());


				if (cdialog.getDistance() > 0)
					touchedStopwatch.getStopwatchData().setgLength(cdialog.getDistance());

			}

			touchedStopwatch.setMustUpdateUI(true);
			if (clockWorker.noneChronoIsRunning())
				updateUI();
		}
	}
}
