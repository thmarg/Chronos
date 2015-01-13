/*
 * Clock
 *
 * Copyright (c) 2014 Thierry Margenstern under MIT license
 * http://opensource.org/licenses/MIT
 */

package tm.android.chronos.core;



/**
 * Interface Clock<br>
 * <p>Represent basic time duration actions such as start stop reset.
 * A clock can measure a duration from a start, or down to an end, and has a name.</p>
 *
 */
public interface Clock {
    /**
     * Start
     * @param startTime the time a user make the start action on the user interface.
     */
    void start(long startTime);

    /**
     * Stop the time but if the implementation use a thread don't stop the thread so that the clock can be reset and the started again.
     * Does nothing if <code>isRunning()</code> is not true.
     * @param stopTime the time a user make the stopTime action on the user interface.
     */
    void stopTime(long stopTime);

    /**
     * When stopped this action reset to wait state this clock.
     */
    void reset();

//    /**
//     * Pause the clock, (this will stop ui update to render the running digits).<br>
//     * Change state to paused only if current state is running.<br>
//     * Useful state (if clock time is stored) to maintain a running clock across device shutdown.
//     */
//    void pause();
//
//    /**
//     * Use this method to restart, state changed to running only if previous state is paused
//     */
//    void restart();

    /**
     *
     * @return <code>boolean</code> true if action start has been done and no stopTime since.
     */
    boolean isRunning();

    /**
     *
     * @return <code>boolean</code> true if actions stopTime has been done and no reset since.
     */
    boolean isStopped();

    /**
     *
     * @return <code>boolean</code> true after instantiation and if actions reset has been done and no start since.
     */
    boolean isWaitingStart();

//    /**
//     *
//     * @return <code>boolean</code> true if pause action has been done
//     */
//    boolean isPaused();


    /**
     * *
     * @return current time from the clock.
     */
    Digit getTime();


    String getName();
    void setName(String name);


}
