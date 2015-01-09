/*
 *   StopwatchRowData
 *   Copyright (c) 2014 Thierry Margenstern under MIT license
 *   http://opensource.org/licenses/MIT
 */

package tm.android.chronos.core;
import tm.android.chronos.core.Units.*;

/**
 * This class is a data holder for a stopwatch. It has two goal<br>
 *     <ul>
 *         <li>retain intermediate clickTime lap, has backend for the UI</li>
 *         <li>provide data structure to store the final state of a Stopwatch onto db or file system.</li>
 *     </ul>
 *<p>Static methods are provided fom data conversion, either length or speed.
 * The data are typed by the unit of length.
 * In case of storage this type must be store somewhere.
 * This type once set must never been change.
 * The default constructor define this type as METER.
 * The clickTime is always stored in milliseconds.
 * </p>
 */
public class StopwatchDataRow {

    private LENGTH_UNIT storedLengthUnit;
    private long clickTime; // the time when the action intermediate time has been done from the stopwatch start.
    private long diffTime; // the "real" intermediate time -> clickTime - previous dataRow clickTime if any.
    private double length;
    private CHRONO_TYPE chronoType;// must be known to show or hide some ui elements.


    public StopwatchDataRow() {
        storedLengthUnit = LENGTH_UNIT.METER;

    }

    public StopwatchDataRow(LENGTH_UNIT length_unit) {
        storedLengthUnit = length_unit;

    }

    public StopwatchDataRow(LENGTH_UNIT length_unit, long clickTime, double length) {
        storedLengthUnit = length_unit;
        this.clickTime = clickTime;
        this.length = length;
    }

    public StopwatchDataRow(LENGTH_UNIT length_unit, long clickTime) {
        storedLengthUnit = length_unit;
        this.clickTime = clickTime;

    }

    public StopwatchDataRow(long clickTime, double length) {
        this.clickTime = clickTime;
        this.length = length;
    }

    public StopwatchDataRow(long clickTime) {
        this.clickTime = clickTime;

    }


    public void setLength(double length) {
        this.length = length;
    }

    public long getClickTime() {
        return clickTime;
    }

    public long getDiffTime() {
        return diffTime;
    }

    public void setDiffTime(long diffTime) {
        this.diffTime = diffTime;
    }

    public double getLength() {
        return length;
    }


}
