/*
 *   StopwatchRowData
 *   Copyright (c) 2014 Thierry Margenstern under MIT license
 *   http://opensource.org/licenses/MIT
 */

package tm.android.chronos.core;

import tm.android.chronos.core.Units.CHRONO_TYPE;

import java.io.Serializable;


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
public class StopwatchDataRow implements Serializable {

    //private LENGTH_UNIT storedLengthUnit;
    private long clickTime; // the time when the action intermediate time has been done from the stopwatch start.
    private long lapTime; // the "real" intermediate time -> clickTime - previous dataRow clickTime if any.
    private double length;
    private transient StopwatchData head; // the container of the list of stopwatchDataRow. Needed by common property own by head.



    public StopwatchDataRow(StopwatchData head,long clickTime) {
        this.head=head;
        this.clickTime = clickTime;

    }


    public void setLength(double length) {
        this.length = length;
    }

    public long getClickTime() {
        return clickTime;
    }

    public long getLapTime() {
        return lapTime;
    }

    public void setLapTime(long lapTime) {
        this.lapTime = lapTime;
    }

//    public double getLength() {
//        return length;
//    }



    public String getLine() {
        StringBuilder stringBuilder = new StringBuilder();
        if (length > 0 && (head.getChronoType() == CHRONO_TYPE.LAPS || head.getChronoType() == CHRONO_TYPE.SEGMENTS)) {
                stringBuilder.append(String.format("%1$.2f", Units.getSpeed(length, head.getLengthUnit(), lapTime, head.getSpeedUnit())));
                stringBuilder.append(" ").append(head.getSpeedUnit().toString()).append("    ");
        }

        stringBuilder.append(Digit.split(lapTime).toString().trim()).append("    ");
        stringBuilder.append(Digit.split(clickTime).toString().trim());

        return stringBuilder.toString();

    }
}
