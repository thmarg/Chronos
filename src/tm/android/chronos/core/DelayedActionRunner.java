/*
 * DelayedActionRunner
 *
 * Copyright (c) 2015 Thierry Margenstern under MIT license
 * http://opensource.org/licenses/MIT
 */

package tm.android.chronos.core;

/**
 * A thread to do one action after a delay.
 * any arbitrary object can be passed to the executed method.
 */
public class DelayedActionRunner<T extends DelayedActionListener> extends  Thread{
	private long startTime;
	private T data;
	private Object[] obj;
	private int delay;
	private int sleepStep;
	private boolean run = true;
	private boolean done=false;

	public DelayedActionRunner(long startTime, int delay, int sleepStep, T data, Object... obj){
		this.startTime = startTime;
		this.data = data;
		this.obj = obj;
		this.delay = delay;
		this.sleepStep = sleepStep;
	}


	@Override
	public void run() {
		while (run && ((System.currentTimeMillis()- startTime)<delay)){
			try {
				sleep(sleepStep);
			}catch (InterruptedException e){
				//
			}
		}
		if (run) {
			data.onDelayedAction(obj);
			done = true;
			run =false;
		}
	}

	public void setRunFalse() {
		this.run = false;
	}

	public boolean isDone() {
		return done;
	}

}
