package model;

import java.io.Serializable;

public class Location implements Serializable {

    private static final long serialVersionUID = 1L;

    private String address;
    private String description;

    private double latitude;
    private double longitude;

    public Location(String address) {
        this(address, "", 0, 0);
    }

    public Location(String address, String description) {
        this(address, description, 0, 0);
    }

    public Location(String address, String description,
                    double latitude, double longitude) {

        this.address = address.trim();
        this.description = description != null ? description.trim() : "";
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public String getAddress() {
        return address;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description != null ? description.trim() : "";
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public double distanceTo(Location other) {

        double dx = this.latitude - other.latitude;
        double dy = this.longitude - other.longitude;

        return Math.sqrt(dx * dx + dy * dy);
    }

    @Override
    public String toString() {
        if (description.isEmpty()) {
            return address;
        }
        return address + " - " + description;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Location location = (Location) obj;
        return address.equals(location.address);
    }

    @Override
    public int hashCode() {
        return address.hashCode();
    }
}