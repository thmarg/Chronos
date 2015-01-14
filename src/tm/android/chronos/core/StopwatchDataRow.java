/*
 *   StopwatchRowData
 *   Copyright (c) 2014 Thierry Margenstern under MIT license
 *   http://opensource.org/licenses/MIT
 */

package tm.android.chronos.core;

import tm.android.chronos.core.Units.CHRONO_TYPE;
import tm.android.chronos.core.Units.LENGTH_UNIT;

/**
 * This class is a data holder for a stopwatch. It has two goal<br>
 * <ul>
 * <li>retain intermediate clickTime lap, has backend for the UI</li>
 * <li>provide data structure to store the final state of a Stopwatch onto db or file system.</li>
 * </ul>
 * <p>Static methods are provided fom data conversion, either length or speed.
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
    private StopwatchData head; // the container of the list of stopwatchDataRow. Needed by common property own by head.


    public StopwatchDataRow(StopwatchData head) {
        this.head = head;

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

    public String getLine() {
        StringBuilder stringBuilder = new StringBuilder();
        if (length > 0 && (chronoType == CHRONO_TYPE.LAPS || chronoType == CHRONO_TYPE.PREDEFINED_TIMES)) {
            if (chronoType == CHRONO_TYPE.LAPS && length > 0) {
                stringBuilder.append("V : ").append(Units.getSpeed(length, head.getLengthUnit(), diffTime, head.getSpeedUnit()));
                stringBuilder.append(" ").append(head.getSpeedUnit().toString());
            }

            stringBuilder.append(" D : ").append(length).append(" ").append(head.getLengthUnit().getShortName());
            stringBuilder.append(Digit.split(diffTime)).append("  ");
        }
        // TODO : display something to distinguish between traveled times, and others.
        stringBuilder.append(Digit.split(clickTime));

        return stringBuilder.toString();

    }
}
