/*
 * DelayedActionRunner
 *
 * Copyright (c) 2015 Thierry Margenstern under MIT license
 * http://opensource.org/licenses/MIT
 */

package tm.android.chronos.core;

import android.util.Log;
import tm.android.chronos.activity.Chronos;

/**
 * A thread to do, delayed actions ...
 * After an initial (optional) delay either the method is called once, or a new loop around the method is launched.
 * A list of arbitrary object can be passed to the executed method.
 * Mode RUNNER :
 * sleepStepRunner set the time between each call of the method.
 * if duration is set to -1, it doJob forever otherwise until the duration is consumed.
 * method can be executed before or after the waitStep
 * Mode ONCE : execute only once the method.
 * For both modes :
 *  sleepStepDelay is the initial delay.
 */
public class DelayedActionRunner<T extends DelayedActionListener> extends Thread {
    private long startTime;
    private final T data;
    private final Object[] obj;
    private int delay;
    private int sleepStepDelay;
    private long sleepStepRunner;
    private long runnerDuration = -1;
    private boolean runDelay = false;
    private boolean runRunner;
    private boolean actionDone = false;
    private boolean stopedFoced = false;
    private final TYPE type;
    private METHOD_POSITION methodPosition = METHOD_POSITION.BEFORE_SLEEP_STEP;



    public DelayedActionRunner(TYPE type, T data, Object... obj) {
        this.data = data;
        this.obj = obj;
        this.type = type;
        runRunner = type == TYPE.RUNNER;


    }


        @Override
        public void run() {
            while (runDelay && ((System.currentTimeMillis() - startTime) < delay)) {
                try {
                    sleep(sleepStepDelay);
                } catch (InterruptedException e) {
                    stopAll();
                }
            }
            if (type == TYPE.ONCE) {
                if (runDelay)
                    data.onDelayedAction(obj);
            } else {
                data.onDelayedActionBefore(obj);
                runRunner = true;
                while (runRunner) {
                    if (methodPosition == METHOD_POSITION.BEFORE_SLEEP_STEP)
                        data.onDelayedAction(obj);
                    try {
                        sleep(sleepStepRunner);
                    } catch (InterruptedException e) {
                        stopAll();
                    }
                    if (runRunner) {
                        if (methodPosition == METHOD_POSITION.AFTER_SLEEP_STEP)
                            data.onDelayedAction(obj);

                        if (runnerDuration != -1) {
                            runnerDuration -= sleepStepRunner;
                            if (runnerDuration <= 0)
                                runRunner = false;
                            Log.i(Chronos.name+"-DelayedRunner", "runnerDuration : " + runnerDuration);
                        }
                    }
                }
                if (!stopedFoced)
                    data.onDelayedActionAfter(obj);
            }
            actionDone = true;


        }


    private void stopAll() {
        runDelay = false;
        runRunner = false;
        stopedFoced = true;
    }

    public void safeStop(){
        interrupt();
    }

    public boolean isActionDone() {
        return actionDone;
    }

    public void setDelay(int delay) {
        runDelay = true;
        this.delay = delay;
    }

    public void setSleepStepDelay(int sleepStepDelay) {
        this.sleepStepDelay = sleepStepDelay;
    }

    public void setSleepStepRunner(long sleepStepRunner) {
        this.sleepStepRunner = sleepStepRunner;
    }

    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    public void setRunnerDuration(long runnerDuration) {
        this.runnerDuration = runnerDuration;
    }

    // if this method is called while running, return the remaining duration
    public long getRunnerDuration() {
        return runnerDuration;
    }

    public void setMethodPosition(METHOD_POSITION methodPosition) {
        this.methodPosition = methodPosition;
    }

    public enum TYPE {ONCE, RUNNER}
    public enum METHOD_POSITION  {BEFORE_SLEEP_STEP,AFTER_SLEEP_STEP}
}
