package model;

import java.io.Serializable;
import java.util.UUID;

public class Delivery implements Serializable {

    private static final long serialVersionUID = 1L;

    private String id = UUID.randomUUID().toString();
    private String name;
    private String email;
    private String document; // CPF
    private String phone;

    // 🔹 novos campos RF02
    private String cnh;
    private String vehicleDocument; // documento da moto
    private DeliveryStatus validationStatus;

    private boolean active;

    // RF22 - Localização para seleção de entregador mais próximo
    private Location currentLocation;

    public Delivery(String name, String email, String document, String phone,
            String cnh, String vehicleDocument) {
        this.name = name;
        this.email = email;
        this.document = document;
        this.phone = phone;
        this.cnh = cnh;
        this.vehicleDocument = vehicleDocument;
        this.validationStatus = DeliveryStatus.PENDENTE;
        this.active = true;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public String getDocument() {
        return document;
    }

    public String getPhone() {
        return phone;
    }

    public String getCnh() {
        return cnh;
    }

    public String getVehicleDocument() {
        return vehicleDocument;
    }

    public DeliveryStatus getValidationStatus() {
        return validationStatus;
    }

    public void setValidationStatus(DeliveryStatus validationStatus) {
        this.validationStatus = validationStatus;
    }

    public boolean isActive() {
        return active;
    }

    public void deactivate() {
        this.active = false;
    }

    public void activate() {
        this.active = true;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public Location getCurrentLocation() {
        return currentLocation;
    }

    public void setCurrentLocation(Location currentLocation) {
        this.currentLocation = currentLocation;
    }
}