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

import java.io.Serializable;
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
 * Once started, for each time when it is reached by the stopwatch, a notification is raised.</li>
 * </ul>
 * Name<br>
 * Creation ftime<br>
 * Length unit<br>
 * Speed units<br>
 * Distance<br>
 * Global Distance<br>
 * Lap count<br>
 * Final time<br>
 */
public class StopwatchData implements Serializable {

    private CHRONO_TYPE chronoType;
    private String name;

    private LENGTH_UNIT lengthUnit;
    private double globalDistance;
    private double lapDistance;
    private int lapCount;

    private long globalTime;
    private SPEED_UNIT speedUnit;

    private Vector<StopwatchDataRow> timeList;

    private int trackPartIndex = 0;
    private Track track; // the track associated with this when type is "parcours"
    private boolean useGps ;
    private int displacementType;
    private boolean randomMusic;
    private String randomMusicPath;

    StopwatchData(String name) {
        chronoType = CHRONO_TYPE.SIMPLE;
        lengthUnit = LENGTH_UNIT.KILOMETER;
        speedUnit = SPEED_UNIT.KILOMETER_PER_HOUR;
        timeList = new Vector<>(5);
        this.name = name;
        useGps = false;
        randomMusic = false;
    }



    /**
     * Call this method when a lap time action is done.
     *
     * @param time time when the action has been done.
     */
    public void add(long time) {
        StopwatchDataRow stopwatchDataRow = new StopwatchDataRow(this, time);
        if (chronoType == CHRONO_TYPE.LAPS)
            stopwatchDataRow.setLength(lapDistance);
        else if (chronoType == CHRONO_TYPE.SEGMENTS) {
            //Couple<String, Float> couple = segments.get(segmentId);
            TrackPart trackPart = track.getTrackParts().get(trackPartIndex);
            stopwatchDataRow.setLength(trackPart.getDistanceToNextLocation());
            stopwatchDataRow.setSegmentName(String.valueOf(trackPartIndex+1)); // name should be the "name start => name desination". Too long for the ui
            trackPartIndex++;
            //segmentId++;
        }

        timeList.add(stopwatchDataRow);
        int id = timeList.indexOf(stopwatchDataRow);
        if (id > 0)
            stopwatchDataRow.setLapTime(time - timeList.get(id - 1).getClickTime());
        else
            stopwatchDataRow.setLapTime(time);
        if (chronoType == CHRONO_TYPE.LAPS) {
            globalDistance += lapDistance;
            lapCount++;
        }
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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

    public double getLapDistance() {
        return lapDistance;
    }

    public void setLapDistance(double lapDistance) {
        this.lapDistance = lapDistance;
        if (chronoType == CHRONO_TYPE.SEGMENTS)
            globalDistance = lapDistance;
        else globalDistance = 0;
    }

    public boolean isRandomMusic() {
        return randomMusic;
    }

    public void setRandomMusic(boolean randomMusic) {
        this.randomMusic = randomMusic;
    }

    public String getRandomMusicPath() {
        return randomMusicPath;
    }

    public void setRandomMusicPath(String randomMusicPath) {
        this.randomMusicPath = randomMusicPath;
    }

    public boolean hasDataRow() {
        return timeList.size() > 0;
    }

    public Vector<StopwatchDataRow> getTimeList() {
        return timeList;
    }

    public long getGlobalTime() {
        return globalTime;
    }

    /**
     * Call this method when a stopwatch is stopped.
     * Update the final time and eventually the distance, depends on chronoType.
     *
     * @param globalTime final time.
     */
    void setGlobalTime(long globalTime) {
        this.globalTime = globalTime;
    }

    public boolean isLastSegment() {
        return (chronoType == CHRONO_TYPE.SEGMENTS) &&
                (track.getTrackParts().get(trackPartIndex+1) != null &&
                        track.getTrackParts().get(trackPartIndex+1).isEnd());//(segmentId >= segments.size() - 1);
    }


    public void reset() {
        timeList.removeAllElements();
        lapCount = 0;
        globalTime = 0;
        if (chronoType != CHRONO_TYPE.SEGMENTS)
            globalDistance = 0;
        else trackPartIndex = 0;
    }


    public String getInfoL2() {
        StringBuilder builder = new StringBuilder();

        if (globalDistance > 0)
            builder.append("D : ").append(new Pwrapper<>(globalDistance).format(3, true)).append(" ").append(lengthUnit.getShortName());
        if (globalDistance > 0 && globalTime > 0) {
            builder.append(" --> ");
            builder.append(String.format("%1$.2f", Units.getSpeed(globalDistance, lengthUnit, globalTime, speedUnit)));
            builder.append(" ").append(speedUnit.toString());
        }

        return builder.toString();
    }

    public String getInfoL3() {
        StringBuilder builder = new StringBuilder();
        if (lapDistance > 0)
            builder.append("d : ").append(new Pwrapper<>(lapDistance).format(3, true)).append(" ").append(lengthUnit.getShortName());
        if (lapCount > 0)
            builder.append("  ").append(lapCount).append(" ").append(lapCount == 1 ? Units.getLocalizedText("klap", null) : Units.getLocalizedText("klaps", null));
        return builder.toString();


    }

    public void setTrack(Track track) {
        this.track = track;
    }

    public void setUseGps(boolean useGps) {
        this.useGps = useGps;
    }

    public boolean isUseGps() {
        return useGps;
    }

    public Track getTrack() {
        return track;
    }

    void resetTrackPartIndex(){
        trackPartIndex = 0;
    }

    public int getDisplacementType() {
        return displacementType;
    }

    public void setDisplacementType(int displacementType) {
        this.displacementType = displacementType;
    }
}
