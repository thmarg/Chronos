package tm.android.chronos.core;

import java.io.Serializable;
import java.util.List;

public class Track implements Serializable {
    private long id;
    private String name;
    private List<TrackPart> trackParts;
    private String description;

    public Track(String name) {
        this.name = name;
        id = System.currentTimeMillis();
    }
    public Track(long id, String name) {
        this.name = name;
        this.id = id;
    }

    private float getTrackLength(){
        float length =0.0f;
        for (TrackPart part : trackParts)
            length += part.getDistanceToNextLocation();
        return length;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public long getId() {
        return id;
    }

    public void setTrackParts(List<TrackPart> trackParts) {
        this.trackParts = trackParts;
    }

    public List<TrackPart> getTrackParts() {
        return trackParts;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public String toString() {
        return name + ", Distance: " + getTrackLength() + "km, track parts count: " + (trackParts.size()-1);
    }


}
