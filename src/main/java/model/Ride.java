package model;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

public class Ride implements Serializable {
    private static final long serialVersionUID = 1L;
    private String driverId;
    private Location driverCurrentLocation;
    
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
    private int estimatedTimeMinutes;
    private List<String> optimizedRoute;
    private PaymentMethod paymentMethod;
    
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
    
    public String getDriverId() {
        return driverId;
    }
    
    public void setDriverId(String driverId) {
        this.driverId = driverId;
    }
    
    public Location getDriverCurrentLocation() {
        return driverCurrentLocation;
    }

    public void setDriverCurrentLocation(Location driverCurrentLocation) {
        this.driverCurrentLocation = driverCurrentLocation;
    }
    
    public int getEstimatedTimeMinutes() {
        return estimatedTimeMinutes;
    }

    public void setEstimatedTimeMinutes(int estimatedTimeMinutes) {
        this.estimatedTimeMinutes = estimatedTimeMinutes;
    }
    
    public List<String> getOptimizedRoute() {
        return optimizedRoute;
    }

    public void setOptimizedRoute(List<String> optimizedRoute) {
        this.optimizedRoute = optimizedRoute;
    }

    public PaymentMethod getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(PaymentMethod paymentMethod) {
        this.paymentMethod = paymentMethod;
    }
    
    @Override
    public String toString() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        return String.format("Ride[id=%s, passenger=%s, origin=%s, destination=%s, status=%s, time=%s, category=%s, payment=%s]",
                id, passengerEmail, origin.getAddress(), destination.getAddress(), 
                status.getDisplayName(), requestTime.format(formatter), 
                vehicleCategory != null ? vehicleCategory : "Nao definida",
                paymentMethod != null ? paymentMethod.getDisplayName() : "Nao definido");
    }
}
