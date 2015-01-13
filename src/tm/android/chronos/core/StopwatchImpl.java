/*
     StopwatchImpl
 *
 *   Copyright (c) 2014 Thierry Margenstern under MIT license
 *   http://opensource.org/licenses/MIT
 */

package tm.android.chronos.core;

import static tm.android.chronos.core.StopwatchImpl.STATUS.*;

/**
 * Implementation of interface Stopwatch
 */
public class StopwatchImpl implements Stopwatch {
    static enum STATUS {RUNNING, STOPPED, WAIT_TO_START, KILLED, PAUSED}


    private STATUS status;


    private long startTime;// start time


    private Digit currentTime;
    private long lastTime;

    private StopwatchData stopwatchData;

    private boolean mustUpdateUI;
    private boolean mustDelete=false;

    public StopwatchImpl() {
        status = WAIT_TO_START;
        currentTime = Digit.split(0);
        stopwatchData = new StopwatchData("Chrono");
        mustUpdateUI=true;
    }


    @Override
    public void start(long startTime) {
        this.startTime = startTime;
        lastTime = startTime;
        status = RUNNING;

    }

    @Override
    public void stopTime(long stopTime) {
        status = STOPPED;
        currentTime.reset();
        currentTime.addMillisSeconds(stopTime - startTime);
        stopwatchData.setChronoTime(currentTime.getInternal());
        lapTime(stopTime);
        mustUpdateUI=true;
    }

    @Override
    public void reset() {
        startTime = 0;
        lastTime = 0;
        currentTime.reset();
        status = WAIT_TO_START;
        stopwatchData.reset();
        mustUpdateUI=true;
    }

//    @Override
//    public void pause() {
//
//    }
//
//    @Override
//    public void restart() {
//
//    }

    @Override
    public boolean isRunning() {
        return status == RUNNING;
    }

    @Override
    public boolean isStopped() {
        return status == STOPPED;
    }

    @Override
    public boolean isWaitingStart() {
        return status == WAIT_TO_START;
    }

//    @Override
//    public boolean isPaused() {
//        return false;
//    }


    @Override
    public Digit getTime() {
        if (isRunning()) {
            long l = System.currentTimeMillis() - lastTime;
            lastTime+=l;
            return currentTime.addMillisSeconds(l);
        } else {
            return currentTime;
        }
    }

    @Override
    public StopwatchData getStopwatchData() {
        return stopwatchData;
    }

    @Override
    public void lapTime(long now) {
        stopwatchData.add(now);
    }


    @Override
    public String getName() {
        return stopwatchData.getName();
    }

    @Override
    public void setName(String name) {
        stopwatchData.setName(name);
    }


    @Override
    public boolean mustUpdateUI() {
        return mustUpdateUI;
    }

    @Override
    public void setMustUpdateUI(boolean must){
        mustUpdateUI = must;

    }

    @Override
    public boolean mustDelete() {
        return mustDelete;
    }

    @Override
    public void setMustDelete() {
        mustDelete = true;
        mustUpdateUI=true;
        status=STOPPED;
    }
}
