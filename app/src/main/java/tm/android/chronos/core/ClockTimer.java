/*
 * ClockTimer
 *
 * Copyright (c) 2015 Thierry Margenstern under MIT license
 * http://opensource.org/licenses/MIT
 */

package tm.android.chronos.core;


import java.io.Serializable;

import static tm.android.chronos.core.Units.STATUS.*;


/**
 *
 */
public class ClockTimer implements Clock, Serializable {
    private Units.STATUS status;
    private Digit currentTime;
    private long startTime;
    private long duration;
    private long endTime;
    private String name;
    int id;

    public ClockTimer() {
        status = WAIT_TO_START;

    }

    public long getDuration() {
        return duration;
    }

    public void setDuration(long duration) {
        if (isWaitingStart())
            this.duration = duration;
    }

    @Override
    public Digit getTime() {
        if (status == RUNNING) {
            long now = System.currentTimeMillis();
            long delta = endTime - now;
            if (delta > 0) {
                currentTime = Digit.split(delta);
                return currentTime;
            } else {
                status = STOPPED;
                return Digit.ZERRO;
            }
        } else return Digit.ZERRO;
    }

    @Override
    public void start(long startTime) {
        if (status == WAIT_TO_START) {
            currentTime = Digit.split(startTime + duration);
            this.startTime = startTime;
            endTime = this.startTime + duration;
            status = RUNNING;
        }
    }

    /**
     * Force to stop : reset
     *
     * @param stopTime the time a user make the stopTime action on the user interface.
     */
    @Override
    public void stopTime(long stopTime) {
        reset();
    }

    @Override
    public void reset() {
        startTime = 0;
        endTime = 0;
        duration = 0;
        status = WAIT_TO_START;
    }

    @Override
    public boolean isRunning() {
        getTime();
        return status == RUNNING;
    }

    @Override
    public boolean isStopped() {
        getTime();
        return status == STOPPED;
    }

    @Override
    public boolean isWaitingStart() {
        getTime();
        return status == WAIT_TO_START;
    }

    @Override
    public String getName() {
        return name;    }

    @Override
    public void setName(String name) {
        this.name = name;
    }

    @Override
    public long getStartTime() {
        return startTime;
    }

    @Override
    public String toString() {
        return "Status " + status + " remaining time " + getTime().toString();
    }

}
