package pt.ipbeja.aula5.data.entity;

public class Coordinates {


    private double latitude;
    private double longitude;

    public Coordinates(double latitude, double longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public boolean isValid() {
        return (latitude >= -90 && latitude <= 90) && (longitude >= -180 && longitude <= 180);
    }
}
