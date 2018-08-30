package tm.android.chronos.localisation;

import java.io.Serializable;

public class Point implements Serializable {
    public final static int NO_VALUE=500;
    private long id=0;
    private String name="";
    private double latitude = NO_VALUE; // default non sense to say not set !
    private double longitude = NO_VALUE;
    private String description="";
//    private float altitude_gps;
//    private float altitude_amsl;
//    private float speed;
//    private float bearing;

    public Point(){
        id = System.currentTimeMillis();
    }
    public Point(double latitude, double longitude) {
        this();
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public Point(String name, double latitude, double longitude) {
        this(name);
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public Point(String name){
        this();
        this.name=name;
    }

    public Point(long id, String name, double latitude, double longitude) {
        this.id = id;
        this.name = name;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public long getId(){
        return id;
    }
    @Override
    public String toString() {
        return name + " => [latitude: "+latitude + ", longitude: " + longitude +"]";
    }
}
