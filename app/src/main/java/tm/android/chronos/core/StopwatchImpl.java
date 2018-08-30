/*
     StopwatchImpl
 *
 *   Copyright (c) 2014 Thierry Margenstern under MIT license
 *   http://opensource.org/licenses/MIT
 */

package tm.android.chronos.core;

import tm.android.chronos.core.Units.STATUS;

import java.io.Serializable;

import static tm.android.chronos.core.Units.STATUS.*;

/**
 * Implementation of interface Stopwatch
 */
public class StopwatchImpl implements Stopwatch, Serializable {

    private STATUS status;

    private long startTime;// start time

    private Digit currentTime;
    private long lastTime;
    private StopwatchData stopwatchData;

    StopwatchImpl() {
        status = WAIT_TO_START;
        currentTime = Digit.split(0);
        stopwatchData = new StopwatchData("Chrono");
    }


    @Override
    public void start(long startTime) {
        this.startTime = startTime;
        lastTime = startTime;
        status = RUNNING;
        if (stopwatchData.getChronoType() == Units.CHRONO_TYPE.SEGMENTS)
            stopwatchData.resetTrackPartIndex();
    }

    @Override
    public void stopTime(long stopTime) {
        status = STOPPED;
        currentTime.reset();// reset to rebuild the real time between stat and stop
        currentTime.addMillisSeconds(stopTime - startTime);
        stopwatchData.setGlobalTime(currentTime.getInternal());
        if (getStopwatchData().hasDataRow()) {
            lapTime(stopTime);// no need to define an update type it is done by lapTime method.
        }
    }

    @Override
    public void reset() {
        startTime = 0;
        lastTime = 0;
        currentTime.reset();
        status = WAIT_TO_START;
        stopwatchData.reset();
    }

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

    @Override
    public Digit getTime() {
        if (isRunning()) {
            long l = System.currentTimeMillis() - lastTime;
            lastTime += l;
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
        stopwatchData.add(now - startTime);
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
    public long getStartTime() {
        return startTime;
    }


//    @Override
//    public void setId(long id) {
//        this.id = id;
//    }

}
