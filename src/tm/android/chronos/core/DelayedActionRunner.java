/*
 * DelayedActionRunner
 *
 * Copyright (c) 2015 Thierry Margenstern under MIT license
 * http://opensource.org/licenses/MIT
 */

package tm.android.chronos.core;

/**
 * A thread to do one action after a delay.
 * After the delay either the method is called once, or a new loop around the method launched.
 * any arbitrary object can be passed to the executed method.
 */
public class DelayedActionRunner<T extends DelayedActionListener> extends  Thread{
	public static enum TYPE {ONCE, RUNNER}
	private long startTime;
	private T data;
	private Object[] obj;
	private int delay;
	private int sleepStepDelay;
	private int sleepStepRunner;
	private long runnerDuration=-1;
	private boolean runDelay = true;
	private boolean runRunner =false;
	private boolean actionDone =false;
	private TYPE type;

	public DelayedActionRunner(TYPE type, T data, Object... obj){
		this.data = data;
		this.obj = obj;
		this.type=type;

	}



	@Override
	public void run() {
		while (runDelay && ((System.currentTimeMillis()- startTime)<delay)){
			try {
				sleep(sleepStepDelay);
			}catch (InterruptedException e){
				//
			}
		}

		if (runDelay) {
			runDelay =false;
			if (type==TYPE.ONCE) {
				data.onDelayedAction(obj);
			} else {
				runRunner =true;
				while (runRunner){
					data.onDelayedAction(obj);
					try {
						sleep(sleepStepRunner);
					} catch ( InterruptedException e){
						//
					}
					if (runnerDuration!=-1) {
						runnerDuration -= sleepStepRunner;
						if (runnerDuration<=0)
							runRunner=false;
					}
				}
			}
			actionDone = true;

		}
	}

	public void stopAll() {
		runDelay = false;
		runRunner=false;
	}


	public boolean isActionDone() {
		return actionDone;
	}

	public void setDelay(int delay) {
		this.delay = delay;
	}

	public void setSleepStepDelay(int sleepStepDelay) {
		this.sleepStepDelay = sleepStepDelay;
	}

	public void setSleepStepRunner(int sleepStepRunner) {
		this.sleepStepRunner = sleepStepRunner;
	}

	public void setStartTime(long startTime) {
		this.startTime = startTime;
	}

	public void setRunnerDuration(long runnerDuration) {
		this.runnerDuration = runnerDuration;
	}
}
