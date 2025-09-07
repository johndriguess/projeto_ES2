package model;

import java.io.Serializable;
import java.util.UUID;

public class Driver extends User implements Serializable {
    private static final long serialVersionUID = 1L;

    private String documentNumber;
    private String vehiclePlate;
    private String vehicleModel;

    public Driver(String name, String email, String phone, String documentNumber,
                  String vehiclePlate, String vehicleModel) {
        super(UUID.randomUUID().toString(), name, email, phone);
        this.documentNumber = documentNumber.trim();
        this.vehiclePlate = vehiclePlate.trim().toUpperCase();
        this.vehicleModel = vehicleModel.trim();
    }

    public String getDocumentNumber() { return documentNumber; }
    public String getVehiclePlate() { return vehiclePlate; }
    public String getVehicleModel() { return vehicleModel; }

    @Override public String toString() {
        return String.format("Driver[id=%s,name=%s,email=%s,phone=%s,doc=%s,plate=%s,model=%s]",
                id, name, email, phone, documentNumber, vehiclePlate, vehicleModel);
    }
}
