package model;

import java.io.Serializable;

public class Driver extends User implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private String licenseDoc;
    private Vehicle vehicle; 
    private Location currentLocation; 
    private boolean isAvailable;

    public Driver(String name, String email, String phone, String password, String licenseDoc, Vehicle vehicle) {
        super(name, email, phone, password);
        this.licenseDoc = licenseDoc;
        this.vehicle = vehicle;
        this.isAvailable = true; 
        this.currentLocation = new Location("Garagem"); 
    }

    @Override
    public String getRole() {
        return "Motorista";
    }
    
    public String getLicenseDoc() {
        return licenseDoc;
    }

    public void addVehicle(Vehicle vehicle) {
        this.vehicle = vehicle;
    }

    public boolean isAvailable() {
        return isAvailable;
    }

    public void setAvailable(boolean available) {
        isAvailable = available;
    }

    public void setCurrentLocation(Location currentLocation) {
        this.currentLocation = currentLocation;
    }
    
    public Vehicle getVehicle() {
        return vehicle;
    }

    public Location getCurrentLocation() {
        return currentLocation;
    }
}