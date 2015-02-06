/*
 * ClockTimer
 *
 * Copyright (c) 2015 Thierry Margenstern under MIT license
 * http://opensource.org/licenses/MIT
 */

package tm.android.chronos.core;



import static tm.android.chronos.core.Units.STATUS.RUNNING;
import static tm.android.chronos.core.Units.STATUS.STOPPED;
import static tm.android.chronos.core.Units.STATUS.WAIT_TO_START;


/**
 *
 */
public class ClockTimer implements Clock {
	private Units.STATUS status;
	private Digit currentTime;
	private long startTime;
	private long duration;
	private boolean mustUpdateUI=false;

	public ClockTimer(){
		status = WAIT_TO_START;
	}

	public void setDuration(long duration) {
		this.duration = duration;
	}

	@Override
	public Digit getTime() {
		long now = System.currentTimeMillis();
		if (now<startTime) {
			currentTime = Digit.split(startTime - now);
			return currentTime;
		} else {
			return Digit.split(0);
		}
	}

	@Override
	public void start(long startTime) {
		if (status==WAIT_TO_START){
			currentTime = Digit.split(startTime+duration);
			this.startTime = startTime+duration;
			status = RUNNING;
		}
	}

	@Override
	public void stopTime(long stopTime) {
		if (isRunning())
			status = STOPPED;
	}

	@Override
	public void reset() {
		status = WAIT_TO_START;
	}

	@Override
	public boolean isRunning() {
		return status==RUNNING;
	}

	@Override
	public boolean isStopped() {
		return status==STOPPED;
	}

	@Override
	public boolean isWaitingStart() {
		return status == WAIT_TO_START;
	}

	@Override
	public void setName(String name) {
		//unused
	}
	@Override
	public String getName() {
		return "";// not used here
	}

	@Override
	public boolean mustUpdateUI() {
		return mustUpdateUI;
	}

	public void setMustUpdateUI(boolean update)  {
		mustUpdateUI=update;
	}

	@Override
	public long getStartTime() {
		return startTime;
	}
}
