/*
 * Stopwatch
 *
 * Copyright (c) 2014 Thierry Margenstern under MIT license
 * http://opensource.org/licenses/MIT
 */

package tm.android.chronos.core;

/**
 * Some other methods for a stopwatch.
 */
public interface Stopwatch extends Clock{
    StopwatchData getStopwatchData();

    /**
     * Call this method to register a lap time
     * @param now time of the action
     */
    void lapTime(long now);

    /**
     * *
     * @return true if some info other than time have changed
     */
    boolean mustUpdateUI();

    /**
     * *
     * @param must update ui or not.
     */
    void setMustUpdateUI(boolean must);

    /**
     * Call this method when this stopwatch must be deleted (in the business logic and on UI)
     */
    void setMustDelete();

    /**
     * Say if this stopwatch must be deleted or not.
     * @return boolean
     */
    boolean mustDelete();

}
