package model;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

public class RideHistory implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private String id;
    private String rideId;
    private String passengerId;
    private String passengerEmail;
    private String driverId;
    private String driverName;
    private String origin;
    private String destination;
    private String vehicleCategory;
    private String status;
    private LocalDateTime requestTime;
    private LocalDateTime completionTime;
    private double price;
    private String paymentMethod;
    private int passengerRating;
    private int driverRating;
    private String notes;
    
    public RideHistory() {
        this.id = UUID.randomUUID().toString();
    }
    
    public RideHistory(Ride ride, String driverName, double price, String paymentMethod) {
        this();
        this.rideId = ride.getId();
        this.passengerId = ride.getPassengerId();
        this.passengerEmail = ride.getPassengerEmail();
        this.driverId = ride.getDriverId();
        this.driverName = driverName;
        this.origin = ride.getOrigin().getAddress();
        this.destination = ride.getDestination().getAddress();
        this.vehicleCategory = ride.getVehicleCategory();
        this.status = ride.getStatus().getDisplayName();
        this.requestTime = ride.getRequestTime();
        this.completionTime = LocalDateTime.now();
        this.price = price;
        this.paymentMethod = paymentMethod;
        this.passengerRating = 0;
        this.driverRating = 0;
    }
    
    // Getters
    public String getId() { return id; }
    public String getRideId() { return rideId; }
    public String getPassengerId() { return passengerId; }
    public String getPassengerEmail() { return passengerEmail; }
    public String getDriverId() { return driverId; }
    public String getDriverName() { return driverName; }
    public String getOrigin() { return origin; }
    public String getDestination() { return destination; }
    public String getVehicleCategory() { return vehicleCategory; }
    public String getStatus() { return status; }
    public LocalDateTime getRequestTime() { return requestTime; }
    public LocalDateTime getCompletionTime() { return completionTime; }
    public double getPrice() { return price; }
    public String getPaymentMethod() { return paymentMethod; }
    public int getPassengerRating() { return passengerRating; }
    public int getDriverRating() { return driverRating; }
    public String getNotes() { return notes; }
    
    // Setters
    public void setPassengerRating(int passengerRating) { this.passengerRating = passengerRating; }
    public void setDriverRating(int driverRating) { this.driverRating = driverRating; }
    public void setNotes(String notes) { this.notes = notes; }
    public void setPrice(double price) { this.price = price; }
    
    @Override
    public String toString() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        return String.format(
            "Histórico[ID=%s, Passageiro=%s, Motorista=%s, Origem=%s, Destino=%s, " +
            "Categoria=%s, Status=%s, Preço=R$%.2f, Data=%s]",
            id, passengerEmail, driverName, origin, destination, 
            vehicleCategory, status, price, requestTime.format(formatter)
        );
    }
    
    public String getDetailedInfo() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        StringBuilder sb = new StringBuilder();
        sb.append("=== DETALHES DO HISTÓRICO ===\n");
        sb.append("ID do Histórico: ").append(id).append("\n");
        sb.append("ID da Corrida: ").append(rideId).append("\n");
        sb.append("Passageiro: ").append(passengerEmail).append("\n");
        sb.append("Motorista: ").append(driverName).append("\n");
        sb.append("Origem: ").append(origin).append("\n");
        sb.append("Destino: ").append(destination).append("\n");
        sb.append("Categoria: ").append(vehicleCategory).append("\n");
        sb.append("Status: ").append(status).append("\n");
        sb.append("Data da Solicitação: ").append(requestTime.format(formatter)).append("\n");
        sb.append("Data da Finalização: ").append(completionTime.format(formatter)).append("\n");
        sb.append("Preço: R$").append(String.format("%.2f", price)).append("\n");
        sb.append("Forma de Pagamento: ").append(paymentMethod).append("\n");
        sb.append("Avaliação do Passageiro: ").append(passengerRating > 0 ? passengerRating + " estrelas" : "Não avaliado").append("\n");
        sb.append("Avaliação do Motorista: ").append(driverRating > 0 ? driverRating + " estrelas" : "Não avaliado").append("\n");
        if (notes != null && !notes.isEmpty()) {
            sb.append("Observações: ").append(notes).append("\n");
        }
        sb.append("=============================");
        return sb.toString();
    }
}
