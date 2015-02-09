/*
 * DbStopwatches
 *
 * Copyright (c) 2015 Thierry Margenstern under MIT license
 * http://opensource.org/licenses/MIT
 */

package tm.android.chronos.sql;

import android.content.ContentValues;
import android.database.Cursor;
import android.provider.BaseColumns;
import tm.android.chronos.core.Stopwatch;
import tm.android.chronos.core.StopwatchDataRow;
import tm.android.chronos.core.StopwatchFactory;
import tm.android.chronos.core.Units;
import java.util.List;
import java.util.Vector;

/**
 * DB access for stopwatches.
 */
public class DbStopwatches extends DbBase {
	private final static String STOPWATCHES_TABLE_NAME= "stopwatches";
	public final static String RUNNING_STOPWATCHES_TABLE_NAME= "stopwatches_running";
	private final static String STOPWATCHES_LAPTIMES_TABLE_NAME="stopwatches_lp";



	private DbStopwatches(){
		super();
	}



	public static abstract class Columns implements BaseColumns {
		public Columns(){}
		public final static String ID="id";
		public final static String TYPE="type";
		public final static String NAME = "name";
		public final static String DATE = "date";
		public final static String RUNNING="running";
		public final static String LAP_DISTANCE= "lap_distance";
		public final static String LAP_COUNT="lap_count";
		public final static String START_TIME = "start_time";
		public final static String CLICK_TIME="click_time";
		public final static String LAP_TIME="lap_time";
		public final static String BVALUE="bvalue";

	}


	public static  String[] getCreationScripts(){
		StringBuilder builder = new StringBuilder(100);
		builder.append("CREATE TABLE ").append(STOPWATCHES_TABLE_NAME).append(" (");
		builder.append(Columns._ID).append(" INTEGER PRIMARY KEY, ");
		builder.append(Columns.NAME).append(" TEXT, ");
		builder.append(Columns.TYPE).append(" TEXT, ");
		builder.append(Columns.DATE).append(" INTEGER, ");// same as long in System.currentTimeMillis()
		builder.append(Columns.RUNNING).append(" INTEGER, "); // boolean 0 or 1
		builder.append(Columns.LAP_DISTANCE).append(" REAL, ");
		builder.append(Columns.LAP_COUNT).append(" INTEGER, ");
		builder.append(Columns.START_TIME).append(" INTEGER").append(");");

		StringBuilder builder2 = new StringBuilder(50);
		builder2.append("CREATE TABLE ").append(STOPWATCHES_LAPTIMES_TABLE_NAME).append(" (");
		builder2.append(Columns.ID).append(" INTEGER, ");
		builder2.append(Columns.LAP_TIME).append(" INTEGER, ");
		builder2.append(Columns.CLICK_TIME).append(" INTEGER").append(");");

		StringBuilder builder3 = new StringBuilder(30);
		builder3.append("CREATE TABLE ").append(RUNNING_STOPWATCHES_TABLE_NAME).append(" (");
		builder3.append(Columns.BVALUE).append(" BLOB);");
		return new String[]{builder.toString(),builder2.toString(),builder3.toString()};

	}




	private  void storeStopwatch(Stopwatch stopwatch, DbHelper dbHelper){
		ContentValues values = new ContentValues();

		values.put(Columns.NAME,stopwatch.getName());
		values.put(Columns.TYPE,stopwatch.getStopwatchData().getChronoType().name());
		values.put(Columns.DATE,stopwatch.getStopwatchData().getCreationDate());
		values.put(Columns.RUNNING,stopwatch.isRunning()?1:0 );
		values.put(Columns.LAP_DISTANCE,stopwatch.getStopwatchData().getLapDistance());
		values.put(Columns.LAP_COUNT,stopwatch.getStopwatchData().getLapCount());
		values.put(Columns.START_TIME,stopwatch.getStartTime());

		long id = dbHelper.getWritableDatabase().insert(STOPWATCHES_TABLE_NAME,null,values);
		if (stopwatch.getStopwatchData().hasDataRow()){
			values.clear();
			for (StopwatchDataRow dataRow : stopwatch.getStopwatchData().getTimeList()){
				values.put(Columns.ID,id);
				values.put(Columns.LAP_TIME,dataRow.getLapTime());
				values.put(Columns.CLICK_TIME,dataRow.getClickTime());
				dbHelper.getWritableDatabase().insert(STOPWATCHES_LAPTIMES_TABLE_NAME,null,values);
				values.clear();
			}
		}
	}
	public   void  stroreStopwatches(List<Stopwatch> list){
		for (Stopwatch stopwatch : list)
			storeStopwatch(stopwatch, dbHelper);
	}



	private boolean hasDetails(long id){
		Cursor cursor = dbHelper.getReadableDatabase().query(STOPWATCHES_LAPTIMES_TABLE_NAME,COUNT_ETOILE,Columns.ID+"=?",new String[]{id+""},null,null,null);
		return cursor.moveToFirst() && cursor.getInt(0)>0 ;
	}



	public List<Stopwatch> getStopwatches(){
		Cursor cursor = dbHelper.getReadableDatabase().query(STOPWATCHES_TABLE_NAME,ETOILE,null,null,null,null,null);
		Vector<Stopwatch> ret = new Vector<>(cursor.getCount());
		cursor.moveToFirst();
		do {
			Stopwatch stopwatch = StopwatchFactory.create();
			stopwatch.setId(cursor.getLong(cursor.getColumnIndex(Columns._ID)));
			stopwatch.setName(cursor.getString(cursor.getColumnIndex(Columns.NAME)));
			stopwatch.getStopwatchData().setCreationDate(cursor.getLong(cursor.getColumnIndex(Columns.DATE)));
			stopwatch.setStartTime(cursor.getLong(cursor.getColumnIndex(Columns.START_TIME)));
			stopwatch.getStopwatchData().setChronoType(Units.CHRONO_TYPE.valueOf(cursor.getString(cursor.getColumnIndex(Columns.TYPE))));
			stopwatch.getStopwatchData().setLapDistance(cursor.getDouble(cursor.getColumnIndex(Columns.LAP_DISTANCE)));
			stopwatch.getStopwatchData().setLapCount(cursor.getInt(cursor.getColumnIndex(Columns.LAP_COUNT)));
			stopwatch.setRunning();
			if (hasDetails(stopwatch.getId())){
				cursor.close();
				cursor = dbHelper.getReadableDatabase().query(STOPWATCHES_LAPTIMES_TABLE_NAME,ETOILE,Columns.RUNNING+"=?",new String[]{stopwatch.getId()+""},null,null,null);
				cursor.moveToFirst();
				do {
					StopwatchDataRow dataRow = new StopwatchDataRow(stopwatch.getStopwatchData(),cursor.getLong(cursor.getColumnIndex(Columns.CLICK_TIME)));
					dataRow.setLapTime(cursor.getLong(cursor.getColumnIndex(Columns.LAP_TIME)));
					dataRow.setLength(stopwatch.getStopwatchData().getLapDistance());
					stopwatch.getStopwatchData().getTimeList().add(dataRow);
				} while (cursor.moveToNext());
			}
			ret.add(stopwatch);
		} while (cursor.moveToNext());
return ret;
	}


}
