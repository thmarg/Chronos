/*
 * WatchTimer
 *
 * Copyright (c) 2015 Thierry Margenstern under MIT license
 * http://opensource.org/licenses/MIT
 */

package tm.android.chronos.uicomponent;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.RectF;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.preference.Preference;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.widget.Toast;
import tm.android.chronos.audio.AudioVolumeVariator;
import tm.android.chronos.core.*;
import tm.android.chronos.preference.PreferenceCst;
import tm.android.chronos.util.geom.Point;
import tm.android.chronos.util.geom.Segment;

import java.io.IOException;

import static tm.android.chronos.preference.PreferenceCst.PREF_KEYS.*;
import static tm.android.chronos.util.geom.Point.SYMMETRY_TYPE.X_AXIS;

/**
 * A graphical Analogue watch for a Timer from 1 to  60 minutes, for setting the time and see visually the  count down.
 * Preferences allow to choose the sound at the end of the timer.
 */
public class WatchTimer extends Watch implements SurfaceViewRenderer {


	private Segment fixedLine;//
	private double rotMinute;
	private double rotSecond;
	private Segment baseLineSeconds;
	private Segment baselineZeroSegment;
	private long[] currentTime;
	private MediaPlayer player; // to play music file or ringtone
	private AudioManager audioManager; // to manage volume.
	private Stopwatch stopwatch; // a stopwatch to show the time since the timer is stopped.
	private float[] pointStartTimeout;// starting position to write timeout message.
	private RectF rectTimeout;
	private float[] pointStartTimeoutText;
	private String textTimeout;
	private String prefix = PreferenceCst.PREFIX_TIMER;
	private DelayedActionRunner<AudioVolumeVariator> delayedActionRunner;

	private ClockWorker<Clock> clockWorker;
	private final static double EPSILON = 0.000001;

	public WatchTimer(Context context, AttributeSet attrs) {
		super(context, attrs);
		clockWorker = new ClockWorker<>(this);
		clockWorker.setInnerSleepTime(100);
		currentTime = new long[5];
		player = MediaPlayer.create(getContext(), Uri.parse(""));
		player.reset();
		player.setOnCompletionListener(new MediaPlayListener());
		audioManager = (AudioManager) getContext().getSystemService(Context.AUDIO_SERVICE);
	}

	@Override
	protected void init(int i1, int i2) {
		super.init(i1, i2);
		fixedLine = new Segment(localCenter, new Point(0.82 * radius, Math.PI / 2, Point.TYPE.POLAR_RADIANT));
		baselineZeroSegment = baseLineMinute.getTranslatedParallel(0);
		baseLineSeconds = baseLineMinute.getTranslatedParallel(0);
		baseLineSeconds.rotate(Point.ZERO, -EPSILON);
		baseLineMinute.rotate(Point.ZERO, -EPSILON);
		//
		textTimeout = Units.getLocalizedText("timeout");
		BaseUI.getPaintWhiteRigthDigitSize().getTextBounds("00:00", 0, 5, rect);
		Point point = new Point(rect.right / 2, -(3 * radius / 2), Point.TYPE.XY);
		pointStartTimeout = point.getChangedCoordinates(screenCoordInLocalCoord).getSymmetrical(X_AXIS, Point.ZERO).asCanvasPoint();
		rectTimeout = new RectF();
		rectTimeout.right = pointStartTimeout[0] + BaseUI.SPACING;
		rectTimeout.left = rectTimeout.right - rect.right + rect.left;
		rectTimeout.bottom = pointStartTimeout[1] - BaseUI.getDigitTextBaseLineVerticalOffset();
		rectTimeout.top = rectTimeout.bottom + BaseUI.getDigitTextBlocHeight();

		BaseUI.getPaintNormalTextWhiteRigth().getTextBounds(textTimeout, 0, textTimeout.length(), rect);
		point = new Point(rect.right / 2, -(3 * radius / 2), Point.TYPE.XY);
		pointStartTimeoutText = point.getChangedCoordinates(screenCoordInLocalCoord).getSymmetrical(X_AXIS, Point.ZERO).asCanvasPoint();
		pointStartTimeoutText[1] += BaseUI.getNormalTextBaseLineVerticalOffset() + BaseUI.SPACING;
	}


	@Override
	protected void drawAll(Canvas canvas) {
		super.drawAll(canvas);
		if (canvas == null)
			return;
		BaseUI.getPaintWhiteRigthDigitSize().setColor(Color.WHITE);
		paintNeedles.setColor(Color.BLUE);
		drawNeedles(fixedLine, canvas, 0);

		paintNeedles.setColor(Color.RED);
		drawNeedles(baseLineMinute, canvas, rotMinute);
		if (clockWorker.get(0).isRunning())
			drawNeedles(baseLineSeconds, canvas, rotSecond);

		drawDigits(canvas, currentTime[2], currentTime[3]);
		drawCenterPoint(canvas);
	}

	@Override
	public void surfaceCreated(SurfaceHolder surfaceHolder) {
		super.surfaceCreated(surfaceHolder);
		if (clockWorker.getState() == Thread.State.NEW) {
			clockWorker.start();
			clockWorker.register(new ClockTimer());
		}
		else if (clockWorker.isDisplayStopped()) {
			clockWorker.setStopDisplay(false);
		}
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
		clockWorker.setStopDisplay(true);
	}

	@Override
	public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2) {
		super.surfaceChanged(surfaceHolder, i, i1, i2);
		if (clockWorker.noneChronoIsRunningNorNeedToUpdateUI()) {
			Canvas canvas = surfaceHolder.lockCanvas();
			drawAll(canvas);
			surfaceHolder.unlockCanvasAndPost(canvas);
		}
	}

	@Override
	public void renderOnCache() {
		// unused.
	}

	@Override
	public void renderOnScreen(Canvas canvas) {
		Clock runTimeClock = clockWorker.get(0);
		if (runTimeClock.getTime().getInternal() != 0 && runTimeClock.isRunning()) {
			currentTime = runTimeClock.getTime().toArray();
			updateNeedlesPosition(currentTime[2], currentTime[3]);
			drawAll(canvas);
		}

		if ((runTimeClock.getTime().getInternal() == 0 && runTimeClock.isRunning()) ||
				(runTimeClock.getTime().getInternal()!=0 && runTimeClock.isWaitingStart())) {
				stop();
				runTimeClock.reset();
				baseLineMinute = baselineZeroSegment.getTranslatedParallel(0).getRotated(localCenter, -EPSILON);
			currentTime = runTimeClock.getTime().toArray();
			updateNeedlesPosition(currentTime[2], currentTime[3]);
			drawAll(canvas);
				playSound();
		}

		//
		if (stopwatch != null && (stopwatch.getStopwatchUi().mustUpdateUI() || stopwatch.isRunning())) {
			paint.setColor(Color.BLACK);
			BaseUI.getPaintWhiteRigthDigitSize().setColor(Color.RED);
			drawElapsedTimeSinceCountDownReached(canvas, stopwatch.getTime());
			stopwatch.getStopwatchUi().clearUpdateType();
		}
	}

	private void drawElapsedTimeSinceCountDownReached(Canvas canvas, Digit digit) {
		if (canvas == null)
			return;

		textTimeout = Units.getLocalizedText("timeout");
		canvas.drawRect(rectTimeout, paint);
		canvas.drawText(digit.toString().trim(), pointStartTimeout[0], pointStartTimeout[1], BaseUI.getPaintWhiteRigthDigitSize());
		// now text below in normal size
		canvas.drawText(textTimeout, pointStartTimeoutText[0], pointStartTimeoutText[1], BaseUI.getPaintNormalTextWhiteRigth());
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

	private void drawDigits(Canvas canvas, long minutes, long seconds) {
		String t = minutes + ":" + (seconds < 10?"0":"") + seconds;
		BaseUI.getPaintWhiteRigthDigitSize().getTextBounds(t, 0, t.length(), rect);
		Point position = new Point(rect.right / 2, -centerX / 3, Point.TYPE.XY);
		float[] pos = position.getChangedCoordinates(screenCoordInLocalCoord).getSymmetrical(X_AXIS, Point.ZERO).asCanvasPoint();
		canvas.drawText(t, pos[0], pos[1], BaseUI.getPaintWhiteRigthDigitSize());
	}


	private Digit getTimeFromScreen() {
		return Digit.split(60000 * currentTime[2]);
	}

	private void updateDigits() {
		currentTime[3] = 0;
		currentTime[2] =  Math.round((((5 * Math.PI / 2 - baseLineMinute.getB().getAngle()) % (2 * Math.PI) + EPSILON) * 30 / Math.PI));
	}


	public void start(long now) {
		// check preferences
		SharedPreferences preferences = getContext().getSharedPreferences(PreferenceCst.PREF_STORE_NAME, Preference.DEFAULT_ORDER);
		if (preferences.getString(prefix + RINGTONE_URI.toString(), "").equals("") && preferences.getString(prefix + MUSIC_PATH.toString(), "").equals("")) {
			Toast.makeText(getContext(),Units.getLocalizedText("timer_pref_not_set"),Toast.LENGTH_LONG).show();
			return;
		}
		//
		ClockTimer timer = (ClockTimer) clockWorker.get(0);
		if (timer.isWaitingStart() && getTimeFromScreen().getInternal() != 0) {
			timer.setDuration(getTimeFromScreen().getInternal());
			baseLineSeconds = baselineZeroSegment.getTranslatedParallel(0);
			baseLineMinute = baselineZeroSegment.getTranslatedParallel(0);
			timer.start(now);

			if (stopwatch != null)
				if (stopwatch.isRunning()) {
					stopwatch.stopTime(0);
					stopwatch.reset();
					stopwatch.getStopwatchUi().clearUpdateType();
				}
				else if (stopwatch.isStopped()) {
					stopwatch.reset();
					stopwatch.getStopwatchUi().clearUpdateType();
				}
		}
	}

	public void stop() {
		if (!clockWorker.get(0).isRunning() && (stopwatch == null || !stopwatch.isRunning()))
			return;

		if (clockWorker.get(0).isRunning())
			clockWorker.get(0).stopTime(0);

		if (stopwatch == null) {
			stopwatch = StopwatchFactory.create();
			stopwatch.getStopwatchData().setChronoType(Units.CHRONO_TYPE.SIMPLE);
			clockWorker.register(stopwatch);
			BaseUI.getPaintNormalTextWhiteRigth().setColor(Color.RED);
		}
		if (stopwatch.isRunning()) {
			stopwatch.stopTime(System.currentTimeMillis());
		}
		else if (clockWorker.get(0).getTime().getInternal() == 0 && stopwatch.isWaitingStart())
			stopwatch.start(System.currentTimeMillis());


		if (player != null && player.isPlaying()) {
			player.stop();
			player.reset();
		}
		if (delayedActionRunner != null)
			delayedActionRunner.stopAll();
	}

	public void reset() {
		if (clockWorker.get(0).isStopped()) {
			clockWorker.get(0).reset();
			baseLineMinute = baselineZeroSegment.getTranslatedParallel(0).getRotated(localCenter, -EPSILON);
			rotMinute = 0;
			currentTime[2] = 0;
			currentTime[3] = 0;
			Canvas canvas = getHolder().lockCanvas();
			drawAll(canvas);
			getHolder().unlockCanvasAndPost(canvas);
		}
		if (!clockWorker.get(0).isRunning() && stopwatch != null && stopwatch.isStopped()) {
			Canvas canvas = getHolder().lockCanvas();
			drawAll(canvas);
			getHolder().unlockCanvasAndPost(canvas);
		}
	}


	private void playSound() {

		SharedPreferences preferences = getContext().getSharedPreferences(PreferenceCst.PREF_STORE_NAME, Preference.DEFAULT_ORDER);

		String path;


		boolean ringtoneSelected = preferences.getBoolean(prefix + RINGTONE_CKB.toString(), false);
		if (ringtoneSelected)
			path = preferences.getString(prefix + RINGTONE_URI.toString(), "");
		else
			path = preferences.getString(prefix + MUSIC_PATH.toString(), "");

		try {
			player.setDataSource(getContext(), Uri.parse(path));
			player.prepare();
		} catch (IOException e) {
			//
		}
		if (preferences.getBoolean(prefix+VOL_FIXE_CKB,true)) {
			float ratio = preferences.getFloat(prefix + MAX_VOLUME, 1);
			int max = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
			audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, (int) (ratio * max), AudioManager.FLAG_ALLOW_RINGER_MODES);
		}
		player.start();
		if (preferences.getBoolean(prefix+VOL_VARIABLE_CKB,false)) {
			audioManager.setStreamVolume(AudioManager.STREAM_MUSIC,0,AudioManager.FLAG_ALLOW_RINGER_MODES);
			double maxVolume = preferences.getFloat(prefix + MAX_VOLUME, 1.0f);
			double minVolume = preferences.getFloat(prefix + MIN_VOLUME, 0.0f);
			delayedActionRunner = new DelayedActionRunner<>(DelayedActionRunner.TYPE.RUNNER, new AudioVolumeVariator(), audioManager, minVolume, maxVolume, (maxVolume - minVolume) / 30);
			delayedActionRunner.setDelay(1000);
			delayedActionRunner.setSleepStepDelay(500);
			delayedActionRunner.setSleepStepRunner(1000);
			delayedActionRunner.setRunnerDuration(30000);
			delayedActionRunner.start();
		}
	}


	private void updateNeedlesPosition(long minute, long secondes) {
		rotMinute = (2 * Math.PI - minute * Math.PI / 30) % (2 * Math.PI) - secondes * Math.PI / 1800;
		rotSecond = (2 * Math.PI - secondes * Math.PI / 30) % (2 * Math.PI);
	}


	@Override
	public boolean onTouchEvent(MotionEvent event) {
		if (clockWorker.get(0).isRunning())
			return true;
		if (stopwatch != null && stopwatch.isRunning()) {
			stopwatch.stopTime(0);
			stopwatch.reset();
			BaseUI.getPaintWhiteRigthDigitSize().setColor(Color.WHITE);
		}

		switch (event.getAction()) {
			case MotionEvent.ACTION_DOWN:
				previousPoint = new Point(event.getX(), event.getY(), Point.TYPE.XY).getSymmetrical(X_AXIS, Point.ZERO).getChangedCoordinates(localCoordInScreenCoord);
				return true;
			case MotionEvent.ACTION_UP:
				previousPoint = null;
				return true;
			case MotionEvent.ACTION_MOVE:
				Point currentPoint = new Point(event.getX(), event.getY(), Point.TYPE.XY).getSymmetrical(X_AXIS, Point.ZERO).getChangedCoordinates(localCoordInScreenCoord);
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

	private class MediaPlayListener implements MediaPlayer.OnCompletionListener {

		@Override
		public void onCompletion(MediaPlayer mediaPlayer) {
			mediaPlayer.stop();
			mediaPlayer.reset();
		}
	}

	public void releaseResources() {
		player.release();
		clockWorker.getClockList().removeAllElements();
	}
}
