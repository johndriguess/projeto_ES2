package model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Driver extends User implements Serializable {
    private static final long serialVersionUID = 1L;

    private String documentNumber;
    private List<Vehicle> vehicles;

    public Driver(String name, String email, String phone, String documentNumber) {
        super(UUID.randomUUID().toString(), name, email, phone);
        this.documentNumber = documentNumber.trim();
        this.vehicles = new ArrayList<>(); 
    }
    
    public Driver(String name, String email, String phone, String documentNumber, Vehicle vehicle) {
        this(name, email, phone, documentNumber);
        if (vehicle != null) {
            this.vehicles.add(vehicle);
        }
    }

    public String getDocumentNumber() { return documentNumber; }
    
    public List<Vehicle> getVehicles() { 
        if (vehicles == null) {
            vehicles = new ArrayList<>();
        }
        return vehicles; 
    }

    public void addVehicle(Vehicle vehicle) {
        if (vehicles == null) {
            vehicles = new ArrayList<>();
        }
        if (vehicle != null) {
            this.vehicles.add(vehicle);
        }
    }

    @Override
    public String toString() {
        return String.format("Driver[id=%s, name=%s, email=%s, phone=%s, doc=%s, vehicles=%s]",
                id, name, email, phone, documentNumber, vehicles.toString());
    }
}