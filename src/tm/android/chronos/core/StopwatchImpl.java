/*
     StopwatchImpl
 *
 *   Copyright (c) 2014 Thierry Margenstern under MIT license
 *   http://opensource.org/licenses/MIT
 */

package tm.android.chronos.core;

import tm.android.chronos.uicomponent.StopwatchUIImpl;
import static tm.android.chronos.core.Units.STATUS.*;
import tm.android.chronos.core.Units.STATUS;
import static tm.android.chronos.core.Units.UPDATE_TYPE.*;

/**
 * Implementation of interface Stopwatch
 */
public class StopwatchImpl implements Stopwatch {

    private STATUS status;

    private long startTime;// start time

    private Digit currentTime;
    private long lastTime;

    private StopwatchData stopwatchData;
    private StopwatchUIImpl stopwatchUI;


    public StopwatchImpl() {
        status = WAIT_TO_START;
        currentTime = Digit.split(0);
        stopwatchData = new StopwatchData("Chrono");
        stopwatchUI = new StopwatchUIImpl();
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
        currentTime.reset();// reset to rebuild the real time between stat and stop
        currentTime.addMillisSeconds(stopTime - startTime);
        stopwatchData.setGlobalTime(currentTime.getInternal());
        if (getStopwatchData().hasDataRow()) {
            lapTime(stopTime);// no need to define an update type it is done by lapTime method.
        } else {
            stopwatchUI.addUpdateType(UPDATE_HEAD_DIGIT);
        }

    }


    @Override
    public void reset() {
        startTime = 0;
        lastTime = 0;
        currentTime.reset();
        status = WAIT_TO_START;
        stopwatchData.reset();
       // REMOVE_DETAILS, all will be updated
        stopwatchUI.addUpdateType(REMOVE_DETAILS);

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
    public StopwatchUIImpl getStopwatchUi() {
        return stopwatchUI;
    }

    @Override
    public void lapTime(long now) {
        stopwatchData.add(now - startTime);
        stopwatchUI.addUpdateType(EXPAND_DETAILS);
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
       return  stopwatchUI.mustUpdateUI();

    }

    @Override
    public long getStartTime() {
        return startTime;
    }
}
