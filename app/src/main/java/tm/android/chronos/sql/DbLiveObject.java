/*
 * DbLiveObject
 *
 * Copyright (c) 2015 Thierry Margenstern under MIT license
 * http://opensource.org/licenses/MIT
 */

package tm.android.chronos.sql;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.util.Log;
import tm.android.chronos.activity.Chronos;
import tm.android.chronos.core.Alarm;
import tm.android.chronos.core.WithId;

import java.io.*;
import java.util.List;
import java.util.Vector;


/**
 * Dedicated class to store living clock by serialization.<br>
 * This allow to return to the app main screen (use back button), or even power off the device(after properly leaving the app), while living clocks are saved.<br>
 * When reopening the app the the living clocks will be there.<br>
 * Usable for stopwatches, timer and alarms.
 */
@SuppressWarnings("unchecked")
public class DbLiveObject<T> extends DbBase {
    private final static String logname = Chronos.name+"DbLiveObject";
    private final static String BVALUE = "bvalue";

    public DbLiveObject(Context context) {
        super(context);
    }


    public void storeLiveObject(T liveObject, String tableName) {
        if (!(liveObject instanceof Serializable))
            throw new RuntimeException(new ClassCastException(liveObject.getClass().getCanonicalName()).getLocalizedMessage());
        ContentValues values = new ContentValues();
        try {
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(byteArrayOutputStream);
            objectOutputStream.writeObject(liveObject);
            values.put(BVALUE, byteArrayOutputStream.toByteArray());
            long ret = dbHelper.getWritableDatabase().insert(tableName, null, values);
            if (ret == -1)
                Log.i(logname, "storeLiveObject Error inserting live stopwatch: " + liveObject.toString());
        } catch (IOException e) {
            setOnError(e, "ZOB");
        }
    }

    public void storeLiveObjects(List<T> list, String tableName) {
        for (T liveObject : list) {
            storeLiveObject(liveObject, tableName);
            if (error) {
                Log.i(logname, "storeLiveObjects Error inserting live stopwatch: " + errorMessage.localiszedMessage);
                break;
            }
        }

        dbHelper.getWritableDatabase().close();
    }

    public List<T> getRunningLiveObjects(String tableName) {
        Vector<T> slist = new Vector<>(10);
        try {
            Cursor cursor = dbHelper.getReadableDatabase().query(tableName, STAR, null, null, null, null, null);
            if (cursor.moveToFirst()) {
                do {
                    byte[] data = cursor.getBlob(0);
                    ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(data));
                    @SuppressWarnings("Unchecked")
                    T liveObject = (T) ois.readObject();
                    slist.add(liveObject);
                } while (cursor.moveToNext());

            }
            cursor.close();
        } catch (Exception e) {
            setOnError(e, "Deserialization failed !");
            e.printStackTrace();
        }
        return slist;
    }

    private  <T extends WithId> void storeLiveObjectWithId(T liveObject, String tableName) {
        if (!(liveObject instanceof Serializable))
            throw new RuntimeException(new ClassCastException(liveObject.getClass().getCanonicalName()).getLocalizedMessage());
        ContentValues values = new ContentValues();
        try {
            values.put("id",liveObject.getId());
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(byteArrayOutputStream);
            objectOutputStream.writeObject(liveObject);
            values.put(BVALUE, byteArrayOutputStream.toByteArray());
            long ret = dbHelper.getWritableDatabase().insert(tableName, null, values);
            if (ret == -1)
                Log.i(logname, "storeLiveObjectWithId: Error inserting live stopwatch: " + liveObject.toString());
        } catch (IOException e) {
            setOnError(e, "ZOB");
        }
    }


    public <T extends WithId> T getRunningLiveObjectById(String tableName, long param) {
        T liveObject = null;
        String selection = "id="+param;
        try {
            Cursor cursor = dbHelper.getReadableDatabase().query(tableName, STAR, selection,null,null, null, null);
            if (cursor.moveToFirst()) {
                long id = cursor.getLong(0);
                byte[] data = cursor.getBlob(1);
                ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(data));
                liveObject = (T) ois.readObject();
                liveObject.setId(id);
            }
            cursor.close();
        } catch (Exception e) {
            setOnError(e, "Deserialization failed !");
            e.printStackTrace();
        }
        return liveObject;
    }

    public <T extends WithId> List<T> getRunningLiveObjectsWithId(String tableName) {
        Vector<T> slist = new Vector<>(10);
        try {
            Cursor cursor = dbHelper.getReadableDatabase().query(tableName, STAR, null, null, null, null, null);
            if (cursor.moveToFirst()) {
                do {
                    long id = cursor.getLong(0);
                    byte[] data = cursor.getBlob(1);
                    ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(data));
                    @SuppressWarnings("Unchecked")
                    T liveObject = (T) ois.readObject();
                    liveObject.setId(id);
                    slist.add(liveObject);
                } while (cursor.moveToNext());

            }
            cursor.close();
        } catch (Exception e) {
            setOnError(e, "Deserialization failed !");
            e.printStackTrace();
        }
        return slist;
    }

    public void deleteFromTableById(String tableName, long id) {
        try {
            String selection = "id="+id;
            dbHelper.getWritableDatabase().delete(tableName, selection, null);
        } catch (Exception e) {
            setOnError(e, "Removing live object with id "+id+" failed @" + tableName);
        }
        dbHelper.getWritableDatabase().close();
    }

    public void clearTable(String tableName) {
        try {
            dbHelper.getWritableDatabase().delete(tableName, null, null);
        } catch (Exception e) {
            setOnError(e, "Removing live object failed @" + tableName);
        }
        dbHelper.getWritableDatabase().close();
    }

    public void clearTableAndClose(String tableName) {
        clearTable(tableName);
        dbHelper.getWritableDatabase().close();
        dbHelper.close();
    }

    public void close() {
        dbHelper.close();
    }


    public static DbLiveObject storeAlarm(Context context, Alarm alarm) {
        DbLiveObject<Alarm> dbLiveObject = new DbLiveObject<>(context);
        dbLiveObject.deleteFromTableById(DbConstant.RUNNING_ALARMS_TABLE_NAME, alarm.getId());
        dbLiveObject.storeLiveObjectWithId(alarm, DbConstant.RUNNING_ALARMS_TABLE_NAME);
        dbLiveObject.close();
        return dbLiveObject;

    }
}
