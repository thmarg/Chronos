package tm.android.chronos.sql;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.widget.Toast;
import tm.android.chronos.core.Track;
import tm.android.chronos.core.TrackPart;
import tm.android.chronos.localisation.Point;

import java.util.ArrayList;
import java.util.List;

import static tm.android.chronos.sql.DbConstant.ColumnNames;


public class TrackFactory extends DbBase {
    public TrackFactory(Context context) {
        super(context);
    }

    public void storeTrack(Track track) {
        ContentValues values = new ContentValues();
        values.put(ColumnNames.ID, track.getId());
        values.put(ColumnNames.NAME, track.getName());
        values.put(ColumnNames.DESCRIPTION, track.getDescription());
        dbHelper.getWritableDatabase().beginTransaction();
        long ret = dbHelper.getWritableDatabase().insert(DbConstant.TRACKS, null, values);
        if (ret != -1) {
            int i = 0;
            for (TrackPart trackPart : track.getTrackParts()) {
                values.clear();
                values.put(ColumnNames.ID, track.getId());
                values.put(ColumnNames.NUM, i);
                values.put(ColumnNames.POINTID, trackPart.getLocation().getId());
                values.put(ColumnNames.DISTANCE, trackPart.getDistanceToNextLocation());
                values.put(ColumnNames.TYPE, trackPart.getType().name());
                ret = dbHelper.getWritableDatabase().insert(DbConstant.TRACK_PARTS, null, values);
                if (ret == -1) {
                    // roll back
                    String selection = ColumnNames.ID + "=" + track.getId();
                    for (int j = 0; j < i; j++) {
                        dbHelper.getWritableDatabase().delete(DbConstant.TRACK_PARTS, selection, null);
                    }
                    dbHelper.getWritableDatabase().delete(DbConstant.TRACKS, selection, null);
                }
                i++;
            }
            dbHelper.getWritableDatabase().setTransactionSuccessful();
            dbHelper.getWritableDatabase().endTransaction();
        }
        dbHelper.close();
    }


    public List<Track> getTracks() {
        Cursor cursor = dbHelper.getReadableDatabase().query(DbConstant.TRACKS, STAR, null, null, null, null, null);
        List<Track> trackList = new ArrayList<>(5);
        if (cursor.moveToFirst()) {
            int id_index = cursor.getColumnIndex(ColumnNames.ID);
            int name_index = cursor.getColumnIndex(ColumnNames.NAME);
            int desc_index = cursor.getColumnIndex(ColumnNames.DESCRIPTION);
            do {
                Track track = new Track(cursor.getLong(id_index), cursor.getString(name_index));
                track.setDescription(cursor.getString(desc_index));
                String selection = ColumnNames.ID + "=" + track.getId();
                Cursor cursor_track_parts = dbHelper.getReadableDatabase().query(DbConstant.TRACK_PARTS, STAR, selection, null, null, null, ColumnNames.NUM);
                List<TrackPart> trackPartList = new ArrayList<>(5);
                if (cursor_track_parts.moveToFirst()) {
                    int type_index = cursor_track_parts.getColumnIndex(ColumnNames.TYPE);
                    int pointid_index = cursor_track_parts.getColumnIndex(ColumnNames.POINTID);
                    int dist_index = cursor_track_parts.getColumnIndex(ColumnNames.DISTANCE);
                    do {
                        TrackPart trackPart = new TrackPart(TrackPart.TYPE.valueOf(cursor_track_parts.getString(type_index)));
                        trackPart.setDistanceToNextLocation(cursor_track_parts.getFloat(dist_index));
                        trackPart.setLocation(getPoint(cursor_track_parts.getLong(pointid_index)));
                        trackPartList.add(trackPart);
                    } while (cursor_track_parts.moveToNext());
                    if (!trackPartList.isEmpty())
                        track.setTrackParts(trackPartList);
                }
                trackList.add(track);
            } while (cursor.moveToNext());
        }
        dbHelper.close();
        return trackList;
    }

    public Track getTrack(Long trackId) {
        Track track = null;
        String selection = ColumnNames.ID + "=" + trackId;
        Cursor cursor = dbHelper.getReadableDatabase().query(DbConstant.TRACKS, STAR, selection, null, null, null, null);
        if (cursor.moveToFirst()) {
            int id_index = cursor.getColumnIndex(ColumnNames.ID);
            int name_index = cursor.getColumnIndex(ColumnNames.NAME);
            int desc_index = cursor.getColumnIndex(ColumnNames.DESCRIPTION);

            track = new Track(cursor.getLong(id_index), cursor.getString(name_index));
            track.setDescription(cursor.getString(desc_index));
            Cursor cursor_track_parts = dbHelper.getReadableDatabase().query(DbConstant.TRACK_PARTS, STAR, selection, null, null, null, ColumnNames.NUM);
            List<TrackPart> trackPartList = new ArrayList<>(5);
            if (cursor_track_parts.moveToFirst()) {
                int type_index = cursor_track_parts.getColumnIndex(ColumnNames.TYPE);
                int pointid_index = cursor_track_parts.getColumnIndex(ColumnNames.POINTID);
                int dist_index = cursor_track_parts.getColumnIndex(ColumnNames.DISTANCE);
                do {
                    TrackPart trackPart = new TrackPart(TrackPart.TYPE.valueOf(cursor_track_parts.getString(type_index)));
                    trackPart.setDistanceToNextLocation(cursor_track_parts.getFloat(dist_index));
                    trackPart.setLocation(getPoint(cursor_track_parts.getLong(pointid_index)));
                    trackPartList.add(trackPart);
                } while (cursor_track_parts.moveToNext());
                if (!trackPartList.isEmpty())
                    track.setTrackParts(trackPartList);
            }
        }
        dbHelper.close();
        return track;
    }

    public void deleteTrack(Track track) {
        // destroy and then store again !
        String selection = ColumnNames.ID + "=" + track.getId();
        dbHelper.getWritableDatabase().beginTransaction();
        dbHelper.getWritableDatabase().delete(DbConstant.TRACKS, selection, null);
        dbHelper.getWritableDatabase().delete(DbConstant.TRACK_PARTS, selection, null);
        dbHelper.getWritableDatabase().setTransactionSuccessful();
        dbHelper.getWritableDatabase().endTransaction();
        dbHelper.close();
    }

    public void updateTrack(Track track) {
        // destroy and then store again !
        String selection = ColumnNames.ID + "=" + track.getId();
        dbHelper.getWritableDatabase().beginTransaction();
        dbHelper.getWritableDatabase().delete(DbConstant.TRACKS, selection, null);
        dbHelper.getWritableDatabase().delete(DbConstant.TRACK_PARTS, selection, null);
        dbHelper.getWritableDatabase().setTransactionSuccessful();
        dbHelper.getWritableDatabase().endTransaction();
        storeTrack(track);
    }


    public void storePoint(Point point) {
        ContentValues values = new ContentValues();
        values.put(ColumnNames.ID, point.getId());
        values.put(ColumnNames.NAME, point.getName());
        values.put(ColumnNames.LATITUDE, point.getLatitude());
        values.put(ColumnNames.LONGITUDE, point.getLongitude());
        values.put(ColumnNames.DESCRIPTION, point.getDescription());
        dbHelper.getWritableDatabase().beginTransaction();
        long ret = dbHelper.getWritableDatabase().insert(DbConstant.POINTS, null, values);
        if (ret != -1) {
            dbHelper.getWritableDatabase().setTransactionSuccessful();
            dbHelper.getWritableDatabase().endTransaction();

        } else {
            Toast.makeText(dbHelper.getContext(), "Error while database insert for Point: " + point.toString(), Toast.LENGTH_LONG).show();
        }
        dbHelper.close();
    }


    public List<Point> getPoints() {
        Cursor cursor = dbHelper.getReadableDatabase().query(DbConstant.POINTS, STAR, null, null, null, null, null);
        List<Point> pointList = new ArrayList<>(5);
        if (cursor.moveToFirst()) {
            int id_index = cursor.getColumnIndex(ColumnNames.ID);
            int name_index = cursor.getColumnIndex(ColumnNames.NAME);
            int latitude_index = cursor.getColumnIndex(ColumnNames.LATITUDE);
            int longitude_index = cursor.getColumnIndex(ColumnNames.LONGITUDE);
            int desc_index = cursor.getColumnIndex(ColumnNames.DESCRIPTION);
            do {
                Point point = new Point(cursor.getLong(id_index), cursor.getString(name_index), cursor.getDouble(latitude_index), cursor.getDouble(longitude_index));
                point.setDescription(cursor.getString(desc_index));
                pointList.add(point);
            } while (cursor.moveToNext());
        }
        dbHelper.close();
        return pointList;
    }

    // this method does not close the connection.
    private Point getPoint(long pointId) {
        String selectPoint = ColumnNames.ID + "=" + pointId;
        Point point = null;
        Cursor cursor = dbHelper.getReadableDatabase().query(DbConstant.POINTS, STAR, selectPoint, null, null, null, null);
        if (cursor.moveToFirst()) {
            int name_index = cursor.getColumnIndex(ColumnNames.NAME);
            int latitude_index = cursor.getColumnIndex(ColumnNames.LATITUDE);
            int longitude_index = cursor.getColumnIndex(ColumnNames.LONGITUDE);
            int desc_index = cursor.getColumnIndex(ColumnNames.DESCRIPTION);
            point = new Point(pointId, cursor.getString(name_index), cursor.getDouble(latitude_index), cursor.getDouble(longitude_index));
            point.setDescription(cursor.getString(desc_index));
        }
        return point;
    }

    public void updatePoint(Point point) {
        // destroy and then store again !
        String selection = ColumnNames.ID + "=" + point.getId();
        dbHelper.getWritableDatabase().beginTransaction();
        dbHelper.getWritableDatabase().delete(DbConstant.POINTS, selection, null);
        dbHelper.getWritableDatabase().setTransactionSuccessful();
        dbHelper.getWritableDatabase().endTransaction();
        storePoint(point);
    }

    public void deletePoint(Point point) {
        // destroy and then store again !
        String selection = ColumnNames.ID + "=" + point.getId();
        dbHelper.getWritableDatabase().beginTransaction();
        dbHelper.getWritableDatabase().delete(DbConstant.POINTS, selection, null);
        dbHelper.getWritableDatabase().setTransactionSuccessful();
        dbHelper.getWritableDatabase().endTransaction();
        dbHelper.close();
    }

    public boolean isPointInUse(long id) {
        String selection = ColumnNames.POINTID + "=" + id;
        Cursor cursor = dbHelper.getReadableDatabase().query(DbConstant.TRACK_PARTS, new String[]{ColumnNames.POINTID}, selection, null, null, null, null);
        boolean retValue = cursor.getCount() > 0;
        dbHelper.close();
        return retValue;
    }

}
