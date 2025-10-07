package model;

import java.io.Serializable;
import java.time.Instant;

public class Receipt implements Serializable {
    private static final long serialVersionUID = 1L;

    private final String id; // geralmente rideId
    private final String rideId;
    private final String passengerName;
    private final String passengerEmail;
    private final String driverName;
    private final String driverEmail;
    private final String paymentMethod;
    private final double totalAmount;
    private final String details; // texto com detalhes da corrida
    private final Instant generatedAt;
    private final String content; // texto completo do recibo

    public Receipt(String id, String rideId, String passengerName, String passengerEmail,
                   String driverName, String driverEmail, String paymentMethod,
                   double totalAmount, String details, String content, Instant generatedAt) {
        this.id = id;
        this.rideId = rideId;
        this.passengerName = passengerName;
        this.passengerEmail = passengerEmail;
        this.driverName = driverName;
        this.driverEmail = driverEmail;
        this.paymentMethod = paymentMethod;
        this.totalAmount = totalAmount;
        this.details = details;
        this.content = content;
        this.generatedAt = generatedAt;
    }

    public String getId() { return id; }
    public String getRideId() { return rideId; }
    public String getPassengerName() { return passengerName; }
    public String getPassengerEmail() { return passengerEmail; }
    public String getDriverName() { return driverName; }
    public String getDriverEmail() { return driverEmail; }
    public String getPaymentMethod() { return paymentMethod; }
    public double getTotalAmount() { return totalAmount; }
    public String getDetails() { return details; }
    public Instant getGeneratedAt() { return generatedAt; }
    public String getContent() { return content; }

    @Override
    public String toString() {
        return "Receipt{id='" + id + "', rideId='" + rideId + "', passenger='" + passengerName +
                "', driver='" + driverName + "', total=" + totalAmount + ", generatedAt=" + generatedAt + "}";
    }
}

