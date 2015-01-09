/*
 * Units
 *
 *   Copyright (c) 2014 Thierry Margenstern under MIT license
 *   http://opensource.org/licenses/MIT
 */

package tm.android.chronos.core;


import android.content.res.Resources;

import java.util.Arrays;
import java.util.List;

import static tm.android.chronos.core.Units.LENGTH_UNIT.*;
import static tm.android.chronos.core.Units.TIME_UNIT.HOUR;
import static tm.android.chronos.core.Units.TIME_UNIT.SECOND;

/**
 * Created by thmarg on 06/01/15.
 * The goal of this class is to provide static enums and methods to convert length and speed, and to show units names and their abbreviations on the UI.
 */
public class Units {

    private static Resources _resources;

    /**
     *
     * @param resources necessary for internationalization
     */
    public static void setResources(Resources resources){
        _resources = resources;
    }

    public static enum LENGTH_UNIT {
        CENTIMETER(0.01d, "cm"), METER(1.0d, "m"), KILOMETER(1000.0d, "km"), FOOT(0.3048d, "ft"), MILES(1609.344d, "mi"), MILES_NAUTIC(1852.0d, "n.m");
        private double value;
        private String key;

        LENGTH_UNIT(double value, String key) {
            this.value = value;
            this.key = key;
        }

        double value() {
            return value;
        }


        public String getShortName() {
            return getName("_short");
        }

        public String getLongName(){
           return getName("_long");
        }

        private String getName(String type){
            if (_resources ==null)
                return key;
                int id = _resources.getIdentifier(key + type, "string", "tm.android.chronos");
                if (id>0)
                    return _resources.getString(id);
            return key;

        }

        public String getKey(){
            return key;
        }// used by test

        @Override
        public String toString() {
            return getLongName();
        }
    }

    public static enum TIME_UNIT {
        MILLISECONDES(1.0d,"ms"), SECOND(1000.0d, "s"), HOUR(3600000.0d, "h");
        private double value;
        private String shortName;

        TIME_UNIT(double value, String shortName) {
            this.value = value;
            this.shortName = shortName;
        }

         double value() {
            return value;
        }

        String getShortName() {
            return shortName;
        }

        public double getValue() {
            return value;
        }
    }


    public static enum SPEED_UNIT {
        CENTIMETER_PER_SECOND(CENTIMETER, SECOND, "cms"), CENTIMETER_PER_HOUR(CENTIMETER, HOUR, "cmh"),
        METER_PER_SECOND(METER, SECOND, "ms"), METER_PER_HOUR(METER, HOUR, "mh"),
        KILOMETER_PER_SECOND(KILOMETER, SECOND, "kms"), KILOMETER_PER_HOUR(KILOMETER, HOUR, "kmh"),
        FOOT_PER_SECOND(FOOT, SECOND, "fts"), FOOT_PER_HOUR(FOOT, HOUR, "fth"),
        MILES_PER_SECOND(MILES, SECOND, "mps"), MILES_PER_HOUR(MILES, HOUR, "mph"),
        NAUTIC_MILES_PER_SECOND(MILES_NAUTIC, SECOND, "nms"), NAUTIC_MILES_PER_HOUR(MILES_NAUTIC, HOUR, "kt");

        private LENGTH_UNIT lengthUnit;
        private TIME_UNIT timeUnit;
        private String key;

        SPEED_UNIT(LENGTH_UNIT lengthUnit, TIME_UNIT timeUnit, String key) {
            this.lengthUnit = lengthUnit;
            this.timeUnit = timeUnit;

            this.key = key;
        }


        public String getKey(){
            return key;
        }
        @Override
        public String toString() {
            return getLocalizedText(key, null);
        }

        public LENGTH_UNIT getLengthUnit() {
            return lengthUnit;
        }

        public TIME_UNIT getTimeUnit() {
            return timeUnit;
        }
    }


    public static enum CHRONO_TYPE {LAPS,INSIDE_LAP,PREDEFINED_TIMES}

    /**
     * Length conversion.
     * @param length to convert
     * @param currentLengthUnit the unit of the length to convert.
     * @param requestedLengthUnit  the return length is expressed in this unit.
     * @return length in currentLengthUnit converted to requestedLengthUnit.
     */
    public static double getLength(double length, LENGTH_UNIT currentLengthUnit, LENGTH_UNIT requestedLengthUnit) {
        return (length * currentLengthUnit.value()) / requestedLengthUnit.value();
    }


    /**
     * Return a speed expressed in requestedSpeedUnit, from distance length in lengthUnit done in "time" time in milli seconds
     * @param length distance
     * @param lengthUnit length unit
     * @param time expressed in milli seconds
     * @param requestedSpeedUnit units of the returned speed
     * @return double
     */
    public static double getSpeed(double length, LENGTH_UNIT lengthUnit,long time, SPEED_UNIT requestedSpeedUnit){
        return (getLength(length,lengthUnit,requestedSpeedUnit.lengthUnit)/time) * requestedSpeedUnit.timeUnit.value();
    }


    /**
     *
     * @return the length units list
     */
    public static List<LENGTH_UNIT> getUnitLenghtList() {
        return Arrays.asList(LENGTH_UNIT.values());
    }

    /**
     *
     * @return the speed units list
     */
    public static List<SPEED_UNIT> getSpeedUnitList() {
        return Arrays.asList(SPEED_UNIT.values());
    }

    /**
     *
     * @return the mode's list, (mode of a stopwatch)
     */
    public static List<CHRONO_TYPE> getModeList(){
        return Arrays.asList(CHRONO_TYPE.values());
    }


    public static synchronized String getLocalizedText(String key, String type){
        if (_resources ==null)
            return key;
        int id = _resources.getIdentifier(key + (type==null?"":type), "string", "tm.android.chronos");
        if (id>0)
            return _resources.getString(id);
        return key;

    }

}
