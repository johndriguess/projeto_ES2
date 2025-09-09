package model;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

public class Ride implements Serializable {
    private static final long serialVersionUID = 1L;
    
    public enum RideStatus {
        SOLICITADA("Solicitada"),
        ACEITA("Aceita"),
        EM_ANDAMENTO("Em Andamento"),
        FINALIZADA("Finalizada"),
        CANCELADA("Cancelada");
        
        private final String displayName;
        
        RideStatus(String displayName) {
            this.displayName = displayName;
        }
        
        public String getDisplayName() {
            return displayName;
        }
    }
    
    private String id;
    private String passengerId;
    private String passengerEmail;
    private Location origin;
    private Location destination;
    private RideStatus status;
    private LocalDateTime requestTime;
    private String vehicleCategory;
    
    public Ride(String passengerId, String passengerEmail, Location origin, Location destination) {
        this.id = UUID.randomUUID().toString();
        this.passengerId = passengerId;
        this.passengerEmail = passengerEmail;
        this.origin = origin;
        this.destination = destination;
        this.status = RideStatus.SOLICITADA;
        this.requestTime = LocalDateTime.now();
        this.vehicleCategory = null;
    }
    
    public String getId() {
        return id;
    }
    
    public String getPassengerId() {
        return passengerId;
    }
    
    public String getPassengerEmail() {
        return passengerEmail;
    }
    
    public Location getOrigin() {
        return origin;
    }
    
    public Location getDestination() {
        return destination;
    }
    
    public RideStatus getStatus() {
        return status;
    }
    
    public LocalDateTime getRequestTime() {
        return requestTime;
    }
    
    public String getVehicleCategory() {
        return vehicleCategory;
    }
    
    public void setStatus(RideStatus status) {
        this.status = status;
    }
    
    public void setVehicleCategory(String vehicleCategory) {
        this.vehicleCategory = vehicleCategory;
    }
    
    @Override
    public String toString() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        return String.format("Ride[id=%s, passenger=%s, origin=%s, destination=%s, status=%s, time=%s, category=%s]",
                id, passengerEmail, origin.getAddress(), destination.getAddress(), 
                status.getDisplayName(), requestTime.format(formatter), 
                vehicleCategory != null ? vehicleCategory : "Nao definida");
    }
}
