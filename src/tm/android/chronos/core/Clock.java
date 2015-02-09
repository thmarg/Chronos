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
public interface Clock extends UpdatableUI {
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


    /**
     * *
     * @return current time from the clock.
     */
    Digit getTime();


    String getName();
    void setName(String name);

    /**
     * *
     * @return the time this clock has been started.
     */
    long getStartTime();

    /**
     * Set the start time, used to restore a clock from database.
     * @param startTime to set
     */
    void setStartTime(long startTime);

    /**
     * Used to restore from dataBase
     */
    void setRunning();

    /**
     * id from data base
     */
    void setId(long id);

    /**
     *
     * @return id
     */
    long getId();

}
