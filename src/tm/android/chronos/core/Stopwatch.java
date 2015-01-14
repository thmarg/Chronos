/*
 * Stopwatch
 *
 * Copyright (c) 2014 Thierry Margenstern under MIT license
 * http://opensource.org/licenses/MIT
 */

package tm.android.chronos.core;


import tm.android.chronos.uicomponent.StopwatchUIImpl;

/**
 * Some other methods for a stopwatch.
 */
public interface Stopwatch extends Clock{
    /**
     *  The container of stopwatch definition and its data.
     * @return StopwatchData
     */
    StopwatchData getStopwatchData();

    StopwatchUIImpl getStopwatchUi();


    /**
     * Call this method to register a lap time
     * @param now time of the action
     */
    void lapTime(long now);

}
