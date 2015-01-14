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
import static tm.android.chronos.core.Units.CHRONO_TYPE.*;

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
        stopwatchData.setChronoTime(currentTime.getInternal());
        lapTime(stopTime);
        defineCommonUpdates();


    }


    private void defineCommonUpdates(){
        if (stopwatchData.getChronoType()== LAPS || stopwatchData.getChronoType()== INSIDE_LAP) {
            stopwatchUI.addUpdateType(UPDATE_HEAD_LINE2);
        }
        stopwatchUI.addUpdateType(UPDATE_HEAD_DIGIT);

    }

    @Override
    public void reset() {
        startTime = 0;
        lastTime = 0;
        currentTime.reset();
        status = WAIT_TO_START;
        stopwatchData.reset();
       defineCommonUpdates();
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
    }


    @Override
    public String getName() {
        return stopwatchData.getName();
    }

    @Override
    public void setName(String name) {
        stopwatchData.setName(name);
    }



}
