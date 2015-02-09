/*
 * DbLiveObject
 *
 * Copyright (c) 2015 Thierry Margenstern under MIT license
 * http://opensource.org/licenses/MIT
 */

package tm.android.chronos.sql;

import android.content.ContentValues;
import android.database.Cursor;
import java.io.*;
import java.util.List;
import java.util.Vector;


/**
 * Dedicated class to store living clock by serialization.<br>
 * This allow to return to the app main screen (use back button), or even power off the device(after properly leaving the app), while living clocks are saved.<br>
 * When reopening the app the the living clocks will be there.<br>
 * Usable for stopwatches, timer and alarms.
 */
public class DbLiveObject<T> extends DbBase {

	private final static String BVALUE = "bvalue";

	public DbLiveObject() {
		super();
	}


	private void storeLiveObject(T liveObject, String tableName) {
		if (!(liveObject instanceof Serializable)) throw new RuntimeException(new ClassCastException(liveObject.getClass().getCanonicalName()).getLocalizedMessage());
		ContentValues values = new ContentValues();
		try {
			ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
			ObjectOutputStream objectOutputStream = new ObjectOutputStream(byteArrayOutputStream);
			objectOutputStream.writeObject(liveObject);
			values.put(BVALUE, byteArrayOutputStream.toByteArray());
			dbHelper.getWritableDatabase().insert(tableName, null, values);
		} catch (IOException e) {
			setOnError(e, "ZOB");
		}
	}

	public void storeLiveObjects(List<T> list, String tableName) {
		for (T liveObject : list) {
			storeLiveObject(liveObject, tableName);
			if (error)
				break;
		}
	}

	public List<T> getRunningLiveObjects(String tableName) {
		try {
			Cursor cursor = dbHelper.getReadableDatabase().query(tableName, ETOILE, null, null, null, null, null);
			if (cursor.moveToFirst()) {
				Vector<T> slist = new Vector<>(5);

				do {

					byte[] data = cursor.getBlob(0);
					ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(data));
					@SuppressWarnings("Unchecked")
					T liveObject = (T) ois.readObject();
					slist.add(liveObject);
				} while (cursor.moveToNext());
				return slist;
			}
		} catch (Exception e) {
			setOnError(e, "ZOB deserialization failed !");
			e.printStackTrace();
		}
		return null;
	}

	public void RemoveRunningStopwatches(String tableName) {
		try {
			dbHelper.getWritableDatabase().delete(tableName, null, null);
		} catch (Exception e) {
			setOnError(e, "ZOB live object failed @" + tableName);
		}
	}
}
