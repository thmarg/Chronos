/*
 *  StopwatchData
 *
 *  Copyright (c) 2014 Thierry Margenstern under MIT license
 *  http://opensource.org/licenses/MIT
 *
 */

package tm.android.chronos.core;

import tm.android.chronos.core.Units.CHRONO_TYPE;
import tm.android.chronos.core.Units.LENGTH_UNIT;
import tm.android.chronos.core.Units.SPEED_UNIT;
import tm.android.chronos.util.Pwrapper;

import java.util.Vector;


/**
 * This class represent the data of a stopwatch.<br>
 * Type
 * <ul>
 * <li>SIMPLE = default : start, intermediate time, stop, reset, that all</li>
 * <li>LAPS = by lap :  a distance is eventually given for one lap.
 * when intermediate time action is done, if a distance is defined, it is considered that a lap has been done.
 * Then  for this lap, time and speed are calculated; a lap count is updated and so the global distance.
 * Finally when stopped, the global average speed on the global distance is calculated.</li>
 * <li>SEGMENTS =segments : distances are defined, each one represent a segment of a global distance.
 * when intermediate time action is done, it is considered that the current segment has been traveled.
 * Then  for this segment, time and speed are calculated;
 * Finally when stopped, the global average speed on the global distance is calculated.
 * </li>
 * <li>PREDEFINED_TIMES : durations are defined and translate into times from a start at zero.
 * Once started, each time a time is reached by the stopwatch, a notification is raised.</li>
 * </ul>
 * Name<br>
 * Creation date<br>
 * Length unit<br>
 * Speed units<br>
 * Distance<br>
 * Global Distance<br>
 * Lap count<br>
 * Final time<br>
 */
public class StopwatchData {

    private CHRONO_TYPE chronoType;
    private String name;
    private long creationDate;

    private LENGTH_UNIT lengthUnit;
    private double globalDistance;
    private double lapDistance;
    private int lapCount;

    private long globalTime;
    private SPEED_UNIT speedUnit;

    private Vector<StopwatchDataRow> timeList;

    public StopwatchData(String name) {
        chronoType = CHRONO_TYPE.SIMPLE;
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
     * Call this method when an lap time action is done.
     *
     * @param time time when the action has been done.
     */
    public void add(long time) {
        StopwatchDataRow stopwatchDataRow = new StopwatchDataRow(this,time);
        stopwatchDataRow.setLength(lapDistance);
        timeList.add(stopwatchDataRow);
        int id = timeList.indexOf(stopwatchDataRow);
        if (id > 0)
            stopwatchDataRow.setDiffTime(time - timeList.get(id - 1).getClickTime());
         else
            stopwatchDataRow.setDiffTime(time);
        globalDistance+=lapDistance;
        lapCount++;
    }


    public void setName(String name) {
        this.name = name;
    }
    public String getName() {
        return name;
    }

    public long getCreationDate() {
        return creationDate;
    }

    public LENGTH_UNIT getLengthUnit() {
        return lengthUnit;
    }
    public void setLengthUnit(LENGTH_UNIT lengthUnit) {
        this.lengthUnit = lengthUnit;
    }

    public double getGlobalDistance() {
        return globalDistance;
    }

    public CHRONO_TYPE getChronoType() {
        return chronoType;
    }
    public void setChronoType(CHRONO_TYPE chronoType) {
        this.chronoType = chronoType;
    }

    public SPEED_UNIT getSpeedUnit() {
        return speedUnit;
    }
    public void setSpeedUnit(SPEED_UNIT speedUnit) {
        this.speedUnit = speedUnit;
    }

    public void setLapDistance(double lapDistance) {
        this.lapDistance = lapDistance;
    }

    public double getLapDistance() {
        return lapDistance;
    }

    public boolean hasDataRow(){return timeList.size()>0;}

    public Vector<StopwatchDataRow> getTimeList() {
        return timeList;
    }

    /**
     * Call this method when a stopwatch is stopped.
     * Update the final time and eventually the distance, depends on chronoType.
     *
     * @param globalTime final time.
     */
    public void setGlobalTime(long globalTime) {
        this.globalTime = globalTime;
    }

    public long getGlobalTime() {
        return globalTime;
    }



    public void reset() {
        timeList.removeAllElements();
        globalTime = 0;
        lapCount=0;
        globalDistance=0;
    }


    public String getInfoL2() {
        StringBuilder builder = new StringBuilder();

            if (globalDistance > 0)
                builder.append("D : ").append(new Pwrapper<Double>(globalDistance).format(3,true)).append(" ").append(lengthUnit.getShortName());
            if (globalDistance > 0 && globalTime > 0) {
                builder.append(" --> ");
                builder.append(String.format("%1$.2f", Units.getSpeed(globalDistance, lengthUnit, globalTime, speedUnit)));
                builder.append(" ").append(speedUnit.toString());
            }

        return builder.toString();
    }

    public String getInfoL3(){
        StringBuilder builder = new StringBuilder();
        if (lapDistance>0)
        builder.append("d : ").append(new Pwrapper<Double>(lapDistance).format(3,true)).append(" ").append(lengthUnit.getShortName());
        if (lapCount>0)
            builder.append("  ").append(lapCount).append(" ").append(lapCount == 1 ? Units.getLocalizedText("klap", null) : Units.getLocalizedText("klaps",null));
        return builder.toString();



    }
}
