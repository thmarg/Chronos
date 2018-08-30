package tm.android.chronos.core;

import tm.android.chronos.localisation.Point;

import java.io.Serializable;

public class TrackPart implements Serializable {
    public enum TYPE {START, END, CURRENT}

    private Point location;
    private float distanceToNextLocation = 0;
    private TYPE type;

    public TrackPart(String name, TYPE type) {
        location = new Point(name);
        this.type = type;
    }

    public TrackPart(TYPE type) {
        this.type = type;
    }
    public float getDistanceToNextLocation() {
        return distanceToNextLocation;
    }

    public String getName() {
        return location.getName();
    }

    public Point getLocation() {
        return location;
    }

    public void setLocation(Point location) {
        this.location = location;
    }

    public void setName(String name) {
        location.setName(name);
    }

    public void setLatitude(Double latitude) {
        location.setLatitude(latitude);
    }

    public Double getLatitude() {
        return location.getLatitude();
    }

    public void setLongitude(Double longitude) {
        location.setLongitude(longitude);
    }

    public Double getLongitude() {
        return location.getLongitude();

    }

    public void setDistanceToNextLocation(float distanceToNextLocation) {
        this.distanceToNextLocation = distanceToNextLocation;
    }


    public TYPE getType() {
        return type;
    }

    public boolean isCurrent() {
        return type == TYPE.CURRENT;
    }

    public boolean isEnd() {
        return type == TYPE.END;
    }

    public boolean isStart(){return  type == TYPE.START;}
}
