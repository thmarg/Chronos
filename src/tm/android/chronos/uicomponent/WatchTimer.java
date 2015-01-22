/*
 * WatchTimer
 *
 * Copyright (c) 2015 Thierry Margenstern under MIT license
 * http://opensource.org/licenses/MIT
 */

package tm.android.chronos.uicomponent;

//import android.app.Activity;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Environment;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import tm.android.chronos.core.*;
import tm.android.chronos.util.geom.Point;
import tm.android.chronos.util.geom.Segment;

import java.io.File;

import static tm.android.chronos.util.geom.Point.SYMMETRY_TYPE.X_AXIS;


/**
 * A graphical Analogue watch for a Timer from 1 to  60 minutes, for setting the time and see visually the  count down.
 */
public class WatchTimer extends Watch implements SurfaceViewRenderer {


	private Segment fixedLine;//
	private double rotMinute;
	private double rotSecond;
	private Segment baseLineSeconds;
	private Segment baselineZeroSegment;
	private long[] currentTime;
	//private Activity activity;

	private ClockWorker<ClockTimer> clockWorker;
	private final static double EPSILON = 0.000001;

	public WatchTimer(Context context, AttributeSet attrs) {
		super(context, attrs);
		clockWorker = new ClockWorker<ClockTimer>(this);
		clockWorker.setInnerSleepTime(100);
		currentTime = new long[5];
	}

	@Override
	protected void init(int i1, int i2) {
		super.init(i1, i2);
		fixedLine = new Segment(localCenter, new Point(0.82 * radius, Math.PI / 2, Point.TYPE.POLAR_RADIANT));
		baselineZeroSegment = baseLineMinute.getTranslatedParallel(0);
		baseLineSeconds = baseLineMinute.getTranslatedParallel(0);
		baseLineSeconds.rotate(Point.ZERO,- EPSILON);
		baseLineMinute.rotate(Point.ZERO,- EPSILON);
	}


	@Override
	protected void drawAll(Canvas canvas) {
		super.drawAll(canvas);
		if (canvas== null)
			return ;
		paintNeedles.setColor(Color.BLUE);
		drawNeedles(fixedLine, canvas, 0);

		paintNeedles.setColor(Color.RED);
		drawNeedles(baseLineMinute, canvas, rotMinute);
		if (clockWorker.get(0).isRunning())
			drawNeedles(baseLineSeconds, canvas, rotSecond);

		drawDigits(canvas, currentTime[2],currentTime[3]);
		drawCenterPoint(canvas);
	}

	@Override
	public void surfaceCreated(SurfaceHolder surfaceHolder) {
		super.surfaceCreated(surfaceHolder);
		if (clockWorker.getState() == Thread.State.NEW) {
			clockWorker.start();
		clockWorker.register(new ClockTimer());
		} else if (clockWorker.isDisplayStopped()){
			clockWorker.setStopDisplay(false);
		}
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
		clockWorker.setStopDisplay(true);
	}

	@Override
	public void renderOnCache() {
		// unused.
	}

	@Override
	public void renderOnScreen(Canvas canvas) {
		Clock runTimeClock = clockWorker.get(0);
		if (runTimeClock.getTime().getInternal() != 0) {
			currentTime = runTimeClock.getTime().toArray();
			updateNeedlesPosition(currentTime[2],currentTime[3]);
			drawAll(canvas);
		}
		else {
			if (runTimeClock.isRunning()){
				stop();
				runTimeClock.reset();
				baseLineMinute = baselineZeroSegment.getTranslatedParallel(0).getRotated(localCenter, -EPSILON);
				playSound();
			}
		}
	}

	@Override
	protected void drawNeedles(Segment segment, Canvas canvas, double rotationAngle) {
		if (clockWorker.get(0).isRunning())
			segment = segment.getRotated(localCenter, rotationAngle);
		else
			segment.rotate(localCenter, rotationAngle);

		for (int j = 0; j < 3; j++) {
			canvas.drawLines(segment.getTranslatedParallel(j).getChangedCoordinate(screenCoordInLocalCoord).getSymmetrical(X_AXIS, Point.ZERO).asCanvasLine(), paintNeedles);
			canvas.drawLines(segment.getTranslatedParallel(-j).getChangedCoordinate(screenCoordInLocalCoord).getSymmetrical(X_AXIS, Point.ZERO).asCanvasLine(), paintNeedles);
		}
	}

	private void drawDigits(Canvas canvas, long minutes, long seconds){
		String t = minutes+":"+(seconds<10?"0":"")+seconds;
		BaseUI.getPaintWhiteRigthDigitSize().getTextBounds(t,0,t.length(),rect);
		Point position = new Point(rect.right/2,-centerX/3, Point.TYPE.XY);
		float[] pos = position.getChangedCoordinates(screenCoordInLocalCoord).getSymmetrical(X_AXIS,Point.ZERO).asCanvasPoint();
		canvas.drawText(t,pos[0],pos[1],BaseUI.getPaintWhiteRigthDigitSize());


	}


	private Digit getTimeFromScreen() {
		return Digit.split(60000*currentTime[2]);
	}

	private void updateDigits(){
		currentTime[3] = 0;
		currentTime[2] = (long) (((5 * Math.PI / 2 - baseLineMinute.getB().getAngle()) % (2 * Math.PI) + EPSILON) * 30 / Math.PI);

	}


	public void start(long now) {
		//Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
		//Intent intent = new Intent(RingtoneManager.ACTION_RINGTONE_PICKER);
		//intent.setType("audio/*");
		//Intent chooser = Intent.createChooser(intent,"Select a song");
		//activity.startActivityForResult(intent,1);


		ClockTimer timer =  clockWorker.get(0);
		if (timer.isWaitingStart() && getTimeFromScreen().getInternal() !=0) {
			timer.setDuration(getTimeFromScreen().getInternal());
			baseLineSeconds = baselineZeroSegment.getTranslatedParallel(0);
			baseLineMinute = baselineZeroSegment.getTranslatedParallel(0);
			timer.start(now);
		}
	}

	public void  stop() {
		if (clockWorker.get(0).isRunning())
			clockWorker.get(0).stopTime(0);
	}

	public void reset(){
		if (clockWorker.get(0).isStopped()) {
			clockWorker.get(0).reset();
			baseLineMinute = baselineZeroSegment.getTranslatedParallel(0).getRotated(localCenter, -EPSILON);
			rotMinute=0;
			currentTime[2]=0;
			currentTime[3]=0;
			Canvas canvas = getHolder().lockCanvas();
			drawAll(canvas);
			getHolder().unlockCanvasAndPost(canvas);
		}

	}


	private void playSound(){


		File f = 	new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC),"track01.flac");
		MediaPlayer player = MediaPlayer.create(getContext(), Uri.fromFile(f));
		player.start();


	}

	private void updateNeedlesPosition(long minute, long secondes) {
		rotMinute = (2 * Math.PI  - minute * Math.PI / 30 ) % (2 * Math.PI)  -secondes*Math.PI/1800;
		rotSecond = (2 * Math.PI  - secondes * Math.PI / 30) % (2 * Math.PI);
	}


//	public void setActivity(Activity activity) {
//		this.activity = activity;
//	}

	@Override
	public boolean onTouchEvent(@SuppressWarnings("NonNull") MotionEvent event) {
		if (clockWorker.get(0).isRunning())
			return true;

		switch (event.getAction()) {
			case MotionEvent.ACTION_DOWN:
				previousPoint = new Point(event.getX(), event.getY(), Point.TYPE.XY).getSymmetrical(X_AXIS,Point.ZERO).getChangedCoordinates(localCoordInScreenCoord);
				return true;
			case MotionEvent.ACTION_UP:
				previousPoint = null;
				return true;
			case MotionEvent.ACTION_MOVE:
				Point currentPoint = new Point(event.getX(), event.getY(), Point.TYPE.XY).getSymmetrical(X_AXIS,Point.ZERO).getChangedCoordinates(localCoordInScreenCoord);
				if (Math.abs(currentPoint.getAngle() - previousPoint.getAngle()) >= 1) {
					previousPoint = currentPoint;
					return true;
				}
				if (Math.abs(currentPoint.getAngle() - previousPoint.getAngle()) < 0.001)
					return true;

				rotMinute = currentPoint.getAngle() - previousPoint.getAngle();
				if ((rotMinute < 0 && baseLineMinute.getB().getAngle() > Math.PI / 2 && baseLineMinute.getB().getAngle() + rotMinute < Math.PI / 2) || (rotMinute > 0 && baseLineMinute.getB().getAngle() < Math.PI / 2 && baseLineMinute.getB().getAngle() + rotMinute > Math.PI / 2)) {
					previousPoint = currentPoint;
					return true;
				}
				updateDigits();
				Canvas canvas = getHolder().lockCanvas();
				drawAll(canvas);
				getHolder().unlockCanvasAndPost(canvas);

				previousPoint = currentPoint;
				return true;
		}
		return super.onTouchEvent(event);
	}
}
