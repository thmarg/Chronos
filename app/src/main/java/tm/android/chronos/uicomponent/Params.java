/*
 * Params
 *
 *   Copyright (c) 2018 Thierry Margenstern under MIT license
 *   http://opensource.org/licenses/MIT
 */
package tm.android.chronos.uicomponent;

import java.io.Serializable;

/**
 * Convenient class to pass parameters by one parameter rather than by three explicit ones
 */
public class Params implements Serializable {
    private long timerDuration;
    private long timerStartTime;
    private long timerEndTime;

    public Params(long timerDuration, long timerStatTime) {
        this.timerDuration = timerDuration;
        this.timerStartTime = timerStatTime;
        this.timerEndTime = timerStatTime + timerDuration;

    }

    public long getTimerDuration() {
        return timerDuration;
    }

    public long getTimerStartTime() {
        return timerStartTime;
    }

    public long getTimerEndTime() {
        return timerEndTime;
    }

    @Override
    public String toString() {
        return "Duration: " + timerDuration + ", timerStartTime: " + timerStartTime + ", timerEndTime: " + timerEndTime;
    }
}
