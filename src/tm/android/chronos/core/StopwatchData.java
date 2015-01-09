/*
 *  StopwatchData
 *
 *    Copyright (c) 2014 Thierry Margenstern under MIT license
 *    http://opensource.org/licenses/MIT
 *
 */

package tm.android.chronos.core;

import tm.android.chronos.core.Units.CHRONO_TYPE;
import tm.android.chronos.core.Units.LENGTH_UNIT;
import tm.android.chronos.core.Units.SPEED_UNIT;

import java.util.Vector;


/**
 *
 */
public class StopwatchData {

    /**
     * LAPS is default mode :  intermediates times, plus speed calculation if a length is provided<br>
     * INSIDE_LAP : predefine partial lengths, measure time for each part.<br>
     * PREDEFINED_TIMES : predefine times, be notified each time a predefined time is reach.<br>
     */

    private CHRONO_TYPE chronoType;
    private String name;
    private long creationDate;
    private LENGTH_UNIT lengthUnit;
    private SPEED_UNIT speedUnit;
    private long chronoTime;

    private double gLength;
    private Vector<StopwatchDataRow> timeList;


    public StopwatchData(String name) {
        chronoType = CHRONO_TYPE.LAPS;
        lengthUnit = LENGTH_UNIT.KILOMETER;
        speedUnit = SPEED_UNIT.KILOMETER_PER_HOUR;
        creationDate = System.currentTimeMillis();
        timeList = new Vector<StopwatchDataRow>(5);
        this.name = name;

    }

    public StopwatchData(CHRONO_TYPE chronoType, String name, long date) {
        this.chronoType = chronoType;
        lengthUnit = LENGTH_UNIT.KILOMETER;
        speedUnit = SPEED_UNIT.KILOMETER_PER_HOUR;
        timeList = new Vector<StopwatchDataRow>(5);
        this.name = name;
        this.creationDate = date;
    }


    /**
     * Call this method when an intermediate time action is done.
     *
     * @param time
     */
    public void add(long time) {
        StopwatchDataRow stopwatchDataRow = new StopwatchDataRow(time);
        stopwatchDataRow.setLength(gLength);
        timeList.add(stopwatchDataRow);
        int id = timeList.indexOf(stopwatchDataRow);
        if (id > 0)
            stopwatchDataRow.setDiffTime(time - timeList.get(id - 1).getClickTime());
        else
            stopwatchDataRow.setDiffTime(time);
    }


    public void setLengthUnit(LENGTH_UNIT lengthUnit) {
        this.lengthUnit = lengthUnit;
    }

    public void setName(String name) {
        this.name = name;
    }

    public long getCreationDate() {
        return creationDate;
    }

    public LENGTH_UNIT getLengthUnit() {
        return lengthUnit;
    }

    public String getName() {
        return name;
    }

    public Vector<StopwatchDataRow> getTimeList() {
        return timeList;
    }

    public int getCount() {
        return timeList.size();
    }

    public StopwatchDataRow getStopwatchDataRow(int i) {
        return timeList.get(i);
    }

    public CHRONO_TYPE getChronoType() {
        return chronoType;
    }

    public SPEED_UNIT getSpeedUnit() {
        return speedUnit;
    }

    public void setChronoType(CHRONO_TYPE chronoType) {
        this.chronoType = chronoType;
    }

    public void setSpeedUnit(SPEED_UNIT speedUnit) {
        this.speedUnit = speedUnit;
    }

    public long getChronoTime() {
        return chronoTime;
    }

    /**
     * Call this method when a stopwatch is stopped.
     * Update the final time and eventually the distance, deponds on chronoType.
     *
     * @param chronoTime final time.
     */
    public void setChronoTime(long chronoTime) {
        this.chronoTime = chronoTime;
        if (chronoType == CHRONO_TYPE.LAPS && timeList.size() > 1) {
            gLength = gLength * (timeList.size() + 1);
        }

    }

    public double getgLength() {
        return gLength;
    }

    public void setgLength(double gLength) {
        this.gLength = gLength;
    }

    public void reset() {
        timeList.removeAllElements();
        chronoTime = 0;
    }





    public String getInfo() {
        StringBuilder builder = new StringBuilder("Type: ");
        if (chronoType == CHRONO_TYPE.LAPS)
            builder.append("laps ");
        if (gLength > 0)
            builder.append("D : ").append(gLength).append(" ").append(lengthUnit.getShortName());
        if (gLength > 0 && chronoTime > 0) {
            builder.append("V : ");
            builder.append(String.format("%1$.2f", Units.getSpeed(gLength, lengthUnit, chronoTime, speedUnit)));
            builder.append(" ").append(speedUnit.toString());
        }
        return builder.toString();
    }

//    public String getInfo(LENGTH_UNIT requestedLengthUnit, SPEED_UNIT speedUnit) {
//        StringBuilder builder = new StringBuilder("Type: ");
//        if (chronoType == CHRONO_TYPE.LAPS)
//            builder.append("laps ");
//        if (gLength > 0)
//            builder.append("D: " + gLength + " " + lengthUnit.getShortName());
//        if (chronoTime > 0)
//            builder.append("V : " + String.format("%1$.2f",Units.getSpeed(gLength, lengthUnit, chronoTime, speedUnit)) + " " + speedUnit.toString());
//        return builder.toString();
//    }


}
