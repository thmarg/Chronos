/*
 * Units
 *
 * Copyright (c) 2014 Thierry Margenstern under MIT license
 * http://opensource.org/licenses/MIT
 */

package tm.android.chronos.core;


import android.content.res.Resources;

import java.util.Arrays;
import java.util.List;

import static tm.android.chronos.core.Units.LENGTH_UNIT.*;


/**
 * Created by thmarg on 06/01/15.
 * The goal of this class is to provide static enums and methods to convert length and speed, and to show units names and their abbreviations on the UI.<br>
 * Some other constant such as CHRONO_TYPE and ZONE_ACTION are here.<br>
 * The static method <code>setResources</code> must be call to have localized name.
 */
public class Units {

    private static Resources _resources;

    /**
     * @param resources necessary for internationalization
     */
    public static void setResources(Resources resources) {
        _resources = resources;
    }

    public static boolean hasNoResources() {
        return _resources == null;
    }

    /**
     * Length conversion.
     *
     * @param length              to convert
     * @param currentLengthUnit   the unit of the length to convert.
     * @param requestedLengthUnit the return length is expressed in this unit.
     * @return length in currentLengthUnit converted to requestedLengthUnit.
     */
    private static double getLength(double length, LENGTH_UNIT currentLengthUnit, LENGTH_UNIT requestedLengthUnit) {
        return (length * currentLengthUnit.value()) / requestedLengthUnit.value();
    }

    /**
     * Return a speed expressed in requestedSpeedUnit, from distance length in lengthUnit done in "time" time in milli seconds
     *
     * @param length             distance
     * @param lengthUnit         length unit
     * @param time               expressed in milli seconds
     * @param requestedSpeedUnit units of the returned speed
     * @return double
     */
    static double getSpeed(double length, LENGTH_UNIT lengthUnit, long time, SPEED_UNIT requestedSpeedUnit) {
        return (getLength(length, lengthUnit, requestedSpeedUnit.lengthUnit) / time) * requestedSpeedUnit.timeUnit.value();
    }

    /**
     * @return the length units list
     */
    public static List<LENGTH_UNIT> getUnitLenghtList() {
        return Arrays.asList(LENGTH_UNIT.values());
    }

    /**
     * @return the speed units list
     */
    public static List<SPEED_UNIT> getSpeedUnitList() {
        return Arrays.asList(SPEED_UNIT.values());
    }

    /**
     * @return the mode's list, (mode of a stopwatch)
     */
    public static List<CHRONO_TYPE> getModeList() {
        return Arrays.asList(CHRONO_TYPE.values());
    }

    public static synchronized String getLocalizedText(String key, String type) {
        if (_resources == null)
            return key;
        int id = _resources.getIdentifier(key + (type == null ? "" : type), "string", "tm.android.chronos");
        if (id > 0)
            return _resources.getString(id);
        return key;
    }

    public static synchronized String getLocalizedText(int resourceId) {
        if (_resources == null)
            return String.valueOf(resourceId);
        try {
            return _resources.getString(resourceId);
        } catch (Resources.NotFoundException e) {
            return String.valueOf(resourceId);
        }
    }

    public static synchronized String getLocalizedText(String key) {
        return getLocalizedText(key, null);
    }

    public static synchronized String getLocalizedTexts(String... keys) {
        StringBuilder builder = new StringBuilder();
        for (String key : keys)
            builder.append(getLocalizedText(key)).append(" ");
        return builder.toString();
    }

    public static synchronized String getLocalizedTextWithParams(String key, String... params) {
        String txt = getLocalizedText(key);
        int i = 1;
        for (String param : params)
            txt = txt.replace("$" + (i++), param);
        return txt;
    }

    public enum LENGTH_UNIT {
        CENTIMETER(0.01d, "cm"), METER(1.0d, "m"), KILOMETER(1000.0d, "km"), FOOT(0.3048d, "ft"), MILES(1609.344d, "mi"), MILES_NAUTIC(1852.0d, "n.m");
        private final double value;
        private final String key;

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

        public String getLongName() {
            return getName("_long");
        }

        private String getName(String type) {
            if (_resources == null)
                return key;
            int id = _resources.getIdentifier(key + type, "string", "tm.android.chronos");
            if (id > 0)
                return _resources.getString(id);
            return key;
        }


        @Override
        public String toString() {
            return getLongName();
        }
    }


    public enum TIME_UNIT {
        SECOND(1000.0d, "s"), HOUR(3600000.0d, "h");    // MILLISECONDES(1.0d, "ms")
        private final double value;
        private final String key;

        TIME_UNIT(double value, String key) {
            this.value = value;
            this.key = key;
        }

        double value() {
            return value;
        }

        public double getValue() {
            return value;
        }


        @Override
        public String toString() {
            return getLocalizedText(key, null);
        }
    }


    public enum SPEED_UNIT {
        CENTIMETER_PER_SECOND(CENTIMETER, TIME_UNIT.SECOND, "cms"), CENTIMETER_PER_HOUR(CENTIMETER, TIME_UNIT.HOUR, "cmh"),
        METER_PER_SECOND(METER, TIME_UNIT.SECOND, "ms"), METER_PER_HOUR(METER, TIME_UNIT.HOUR, "mh"),
        KILOMETER_PER_SECOND(KILOMETER, TIME_UNIT.SECOND, "kms"), KILOMETER_PER_HOUR(KILOMETER, TIME_UNIT.HOUR, "kmh"),
        FOOT_PER_SECOND(FOOT, TIME_UNIT.SECOND, "fts"), FOOT_PER_HOUR(FOOT, TIME_UNIT.HOUR, "fth"),
        MILES_PER_SECOND(MILES, TIME_UNIT.SECOND, "mps"), MILES_PER_HOUR(MILES, TIME_UNIT.HOUR, "mph"),
        NAUTIC_MILES_PER_SECOND(MILES_NAUTIC, TIME_UNIT.SECOND, "nms"), NAUTIC_MILES_PER_HOUR(MILES_NAUTIC, TIME_UNIT.HOUR, "kt");

        private final LENGTH_UNIT lengthUnit;
        private final TIME_UNIT timeUnit;
        private final String key;

        SPEED_UNIT(LENGTH_UNIT lengthUnit, TIME_UNIT timeUnit, String key) {
            this.lengthUnit = lengthUnit;
            this.timeUnit = timeUnit;

            this.key = key;
        }

        @Override
        public String toString() {
            return getLocalizedText(key, null);
        }

//        public LENGTH_UNIT getLengthUnit() {
//            return lengthUnit;
//        }
//
//        public TIME_UNIT getTimeUnit() {
//            return timeUnit;
//        }
    }

    public enum CHRONO_TYPE {
        SIMPLE("basic"), LAPS("laps"), SEGMENTS("segments"); //PREDEFINED_TIMES("times")
        private final String key;

        CHRONO_TYPE(String key) {
            this.key = key;
        }

        @Override
        public String toString() {
            return getLocalizedText(key, null);
        }
    }

    public enum ZONE_ACTION {LAP_TIME, START_STOP_RESET, PARAM, SHOW_HIDE}


    public enum DIGIT_FORMAT {
        EXTRA_SHORT("             000", "ext_short"), VERY_SHORT("          00:000", "very_short"),
        NO_MS_SHORT("       00:00    ", "no_ms");  //EXTENDED("    00:00:00:000", "extend"), FULL("000:00:00:00:000", "full"), SHORT("       00:00:000", "short")
        private final String format;
        private final String key;

        DIGIT_FORMAT(String format, String key) {
            this.key = key;
            this.format = format;
        }

        public String getFormat() {
            return format;
        }

        public String getKey() {
            return key;

        }

        @Override
        public String toString() {
            return getLocalizedText(key, null);
        }
    }

    public enum UPDATE_TYPE {
        ADD_NEW, UPDATE_HEAD_LINE1, UPDATE_HEAD_LINE2, UPDATE_HEAD_LINE3, UPDATE_HEAD_DIGIT, UPDATE_DETAILS, UPDATE_CHRONO_TYPE,
        COLLAPSE_DETAILS, EXPAND_DETAILS,  SELECT, DESELECT, PAINT_ALL, UPDATE_ALARM_TYPE
    }

    public enum STATUS {RUNNING, STOPPED, WAIT_TO_START}

}
