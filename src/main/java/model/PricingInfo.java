package model;

import java.io.Serializable;

public class PricingInfo implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private String category;
    private double estimatedDistance;
    private int estimatedTimeMinutes;
    private double baseFare;
    private double distancePrice;
    private double timePrice;
    private double totalPrice;
    
    public PricingInfo(String category, double estimatedDistance, int estimatedTimeMinutes,
                      double baseFare, double distancePrice, double timePrice, double totalPrice) {
        this.category = category;
        this.estimatedDistance = estimatedDistance;
        this.estimatedTimeMinutes = estimatedTimeMinutes;
        this.baseFare = baseFare;
        this.distancePrice = distancePrice;
        this.timePrice = timePrice;
        this.totalPrice = totalPrice;
    }
    
    public String getCategory() {
        return category;
    }
    
    public double getEstimatedDistance() {
        return estimatedDistance;
    }
    
    public int getEstimatedTimeMinutes() {
        return estimatedTimeMinutes;
    }
    
    public double getBaseFare() {
        return baseFare;
    }
    
    public double getDistancePrice() {
        return distancePrice;
    }
    
    public double getTimePrice() {
        return timePrice;
    }
    
    public double getTotalPrice() {
        return totalPrice;
    }
    
    public String getFormattedTime() {
        return estimatedTimeMinutes + " min";
    }
    
    public String getFormattedPrice() {
        return String.format("R$ %.2f", totalPrice);
    }
    
    public String getFormattedDistance() {
        return String.format("%.1f km", estimatedDistance);
    }
    
    @Override
    public String toString() {
        return String.format("PricingInfo[category=%s, distance=%.1fkm, time=%dmin, total=R$%.2f]",
                category, estimatedDistance, estimatedTimeMinutes, totalPrice);
    }
}
