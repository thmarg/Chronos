/*
 * DbConstant
 *
 * Copyright (c) 2018 Thierry Margenstern under MIT license
 * http://opensource.org/licenses/MIT
 */

package tm.android.chronos.sql;

import java.util.Hashtable;

/**
 * DB access for stopwatches. db version :1 (17/06/2018) build 0.5.4
 * DB access for stopwatches. db version :2 (04/07/2018) build > 0.5.4
 * new table RUNNING_STOPWATCHE_WITH_ID
 */
public class DbConstant {
    public final static String RUNNING_STOPWATCHES_TABLE_NAME = "stopwatches_running";
    public final static String RUNNING_TIMER_TABLE_NAME = "timer_running";
    public final static String RUNNING_ALARMS_TABLE_NAME = "alarms_running";
    final static String TRACKS = "tracks";
    final static String TRACK_PARTS = "track_parts";
    final static String POINTS = "points";


    public static class ColumnNames {
        final static String ID = "id";
        public final static String NAME = "name";
        final static String NUM = "num";
        final static String LATITUDE = "latitude";
        final static String LONGITUDE = "longitude";
        final static String DISTANCE = "distance";
        public final static String TYPE = "type";
        final static String POINTID = "pointid";
        final static String BLOB_VALUE = "bvalue";
        final static String DESCRIPTION = "description";

    }

    private static class ColumnType {
        final static String INTEGER = "INTEGER";
        final static String TEXT = "TEXT";
        final static String REAL = "REAL";
        final static String BLOB = "BLOB";
    }


    private static class ColumnParser {
        private final static Hashtable<String, String> parser = new Hashtable<>(10);

        static {
            parser.put(ColumnNames.ID, ColumnType.INTEGER);
            parser.put(ColumnNames.NUM, ColumnType.INTEGER);
            parser.put(ColumnNames.POINTID, ColumnType.INTEGER);
            parser.put(ColumnNames.NAME, ColumnType.TEXT);
            parser.put(ColumnNames.DESCRIPTION, ColumnType.TEXT);
            parser.put(ColumnNames.TYPE, ColumnType.TEXT);
            parser.put(ColumnNames.LATITUDE, ColumnType.REAL);
            parser.put(ColumnNames.LONGITUDE, ColumnType.REAL);
            parser.put(ColumnNames.DISTANCE, ColumnType.REAL);
            parser.put(ColumnNames.BLOB_VALUE, ColumnType.BLOB);

        }

        static String getDeclaration(String column_name) {
            if (parser.keySet().contains(column_name)) {
                return " " + column_name + " " + parser.get(column_name) + " ";
            } else return "UNKNOWN";
        }
    }

    private DbConstant() {
    }


    static String create_table_stopwatches = "CREATE TABLE " + RUNNING_STOPWATCHES_TABLE_NAME + " (" +
            ColumnParser.getDeclaration(ColumnNames.BLOB_VALUE) + ");";

    static String create_table_timers = "CREATE TABLE " + RUNNING_TIMER_TABLE_NAME + " (" +
            ColumnParser.getDeclaration(ColumnNames.BLOB_VALUE) + ");";

    static String create_table_alarms = "CREATE TABLE " + RUNNING_ALARMS_TABLE_NAME + " (" +
            ColumnParser.getDeclaration(ColumnNames.ID) + "PRIMARY KEY NOT NULL," +
            ColumnParser.getDeclaration(ColumnNames.BLOB_VALUE) + ");";

    static String create_table_tracks = "CREATE TABLE " + TRACKS + " (" +
            ColumnParser.getDeclaration(ColumnNames.ID) + "PRIMARY KEY NOT NULL," +
            ColumnParser.getDeclaration(ColumnNames.NAME) + "," +
            ColumnParser.getDeclaration(ColumnNames.DESCRIPTION) + ");";

    static String create_table_track_parts = "CREATE TABLE " + TRACK_PARTS + " (" +
            ColumnParser.getDeclaration(ColumnNames.ID) + "," +
            ColumnParser.getDeclaration(ColumnNames.NUM) + "," +
            ColumnParser.getDeclaration(ColumnNames.POINTID) + "," +
            ColumnParser.getDeclaration(ColumnNames.DISTANCE) + "," +
            ColumnParser.getDeclaration(ColumnNames.TYPE) + "," +
            "PRIMARY KEY (id, num ASC));";

    static String create_table_points = "CREATE TABLE " + POINTS + " (" +
            ColumnParser.getDeclaration(ColumnNames.ID) + "," +
            ColumnParser.getDeclaration(ColumnNames.NAME) + "UNIQUE," +
            ColumnParser.getDeclaration(ColumnNames.LATITUDE) + "," +
            ColumnParser.getDeclaration(ColumnNames.LONGITUDE) + "," +
            ColumnParser.getDeclaration(ColumnNames.DESCRIPTION) + ");";

    static String[] getCreationScripts() {
        return new String[]{create_table_stopwatches, create_table_timers, create_table_alarms, create_table_tracks, create_table_track_parts, create_table_points};
    }
}
