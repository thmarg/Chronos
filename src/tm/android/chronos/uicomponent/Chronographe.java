
/*
 *  Chronographe
 *
 *  Copyright (c) 2014 Thierry Margenstern under MIT license
 *  http://opensource.org/licenses/MIT
 */

package tm.android.chronos.uicomponent;

import android.app.*;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.View;
import tm.android.chronos.core.*;
import tm.android.chronos.dialogs.ChronographeDialog;
import tm.android.chronos.dialogs.OnDialogClickListener;

public class Chronographe extends BaseChronographe implements SurfaceHolder.Callback, StopwatchRenderDelegate<Digit> {

	private Canvas canvas=null;
	private Rect rect;


	private IntermediateTimeListener intermediateTimeListener;
	private Stopwatch stopwatch;

	public Chronographe(Context context){
		super(context);
		init();

	}

	public Chronographe(Context context,AttributeSet attributeSet) {
		super(context,attributeSet);
		init();
	}

	private void init(){
		rect = new Rect();

		stopwatch = new Stopwatch(this);
		getHolder().addCallback(this);
		setClickable(true);

		setOnTouchListener(new OnTouch());
		//setOnClickListener(new OnClick());

	}

	@Override
	public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2) {

	}

	@Override
	public void surfaceCreated(SurfaceHolder surfaceHolder) {
		if (stopwatch.getState() == Thread.State.NEW)
			stopwatch.startLoop();
		else{
			stopwatch = stopwatch.RebuildAndStart();
		}
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
				
		stopwatch.fullStop();
	}

	@Override
	public void doDrawRun(Digit digit) {
		synchronized (getHolder()){

			if ((canvas = getHolder().lockCanvas())==null) return;

			rect=canvas.getClipBounds();
			canvas.drawRect(rect,paintWileWaitToStart);
			canvas.drawText(stopwatch.getName(), SPACING, topLineVerticalPosition, paintOtherTextWhiteLeft);
			canvas.drawText(stopwatch.getStopwatchData().getInfo(), SPACING, middleLineVerticalPosition, paintOtherTextWhiteLeft);
			canvas.drawText(digit.toString(),chronoHorizontalEnd, bottomLineVerticalPosition, paintWhiteRight);
		}
	}

	@Override
	public void doDrawNoRun(Digit digit, boolean update) {
		synchronized (getHolder()){
			if (update) {

			if ((canvas = getHolder().lockCanvas())==null) return;

				rect = canvas.getClipBounds();
				canvas.drawRect(rect, paintWileWaitToStart);
				canvas.drawText(stopwatch.getName(), SPACING, topLineVerticalPosition, paintOtherTextWhiteLeft);
				canvas.drawText(stopwatch.getStopwatchData().getInfo(), SPACING, middleLineVerticalPosition, paintOtherTextWhiteLeft);
				canvas.drawText(digit.toString(), chronoHorizontalEnd, bottomLineVerticalPosition, paintWhiteRight);
			} else {
				canvas = null;
			}
		}
	}


	@Override
	public void doFinal() {
		if (canvas!=null)
			getHolder().unlockCanvasAndPost(canvas);
	}

	public Stopwatch getStopwatch() {
		return stopwatch;
	}



	private class OnTouch implements OnTouchListener{
		@Override
		public boolean onTouch(View view, MotionEvent event) {
			long now = System.currentTimeMillis();
			if (event.getAction()!= MotionEvent.ACTION_DOWN)
				return false;

			if (event.getY()< middleLineVerticalPosition && event.getX()<chronoHorizontalEnd){
				ChronographeDialog dialog = new ChronographeDialog();
				FragmentManager fm = ((Activity)getContext()).getFragmentManager();
				dialog.setOnDialogClickListener(new OnDialogClick());
				dialog.setChronoName(stopwatch.getName());
				dialog.setChronoType(stopwatch.getStopwatchData().getChronoType());
				dialog.setLengthUnit(stopwatch.getStopwatchData().getLengthUnit());
				dialog.setSpeedUnit(stopwatch.getStopwatchData().getSpeedUnit());
				dialog.setDistance(stopwatch.getStopwatchData().getgLength());
				//dialog.setChronoTime(stopwatch.getStopwatchData().getChronoTime());
				dialog.show(fm,"Dialog-Chronographe");
			} else {
				if (event.getX()>chronoHorizontalEnd && stopwatch.isRunning()) {
					getStopwatch().intermediateTime(now);
					intermediateTimeListener.onIntermediateTimeUpdate(getId());
				}
				else if (getStopwatch().isWaitingStart())
					getStopwatch().start(now);
				else if (getStopwatch().isRunning())
					getStopwatch().stopTime(now);
				else if (getStopwatch().isStopped()) {
					getStopwatch().reset();
					intermediateTimeListener.onIntermediateTimeUpdate(getId());
				}
			}

			return true;
		}
	}

	public void setIntermediateTimeListener(IntermediateTimeListener intermediateTimeListener) {
		this.intermediateTimeListener = intermediateTimeListener;
	}

	private class OnDialogClick implements OnDialogClickListener {
		@Override
		public void onDialogNegativeClick(DialogFragment dialog) {

		}

		@Override
		public void onDialogPositiveClick(DialogFragment dialog) {
			String name = ((ChronographeDialog)dialog).getChronoName();
			if (name!=null && !name.equals(""))
			getStopwatch().setName(name);

			Units.CHRONO_TYPE type = ((ChronographeDialog) dialog).getChronoType();
			if (type != null)
				getStopwatch().getStopwatchData().setChronoType(type);

			if (type!= Units.CHRONO_TYPE.PREDEFINED_TIMES) {
				Units.LENGTH_UNIT lengthUnit = ((ChronographeDialog) dialog).getLengthUnit();
				if (lengthUnit!=null)
					getStopwatch().getStopwatchData().setLengthUnit(lengthUnit);

				Units.SPEED_UNIT speedUnit = ((ChronographeDialog) dialog).getSpeedUnit();
				if (speedUnit!=null)
					getStopwatch().getStopwatchData().setSpeedUnit(speedUnit);


				double distance = ((ChronographeDialog) dialog).getDistance();
				if (distance>0)
					getStopwatch().getStopwatchData().setgLength(distance);

			}
			stopwatch.setNoRunRenderUpdate(true);



		}
	}



}
