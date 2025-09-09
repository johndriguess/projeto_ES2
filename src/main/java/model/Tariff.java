package model;

import java.io.Serializable;

public class Tariff implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private String category;
    private double baseFare;
    private double pricePerKm;
    private double pricePerMinute;
    
    public Tariff(String category, double baseFare, double pricePerKm, double pricePerMinute) {
        this.category = category;
        this.baseFare = baseFare;
        this.pricePerKm = pricePerKm;
        this.pricePerMinute = pricePerMinute;
    }
    
    public String getCategory() {
        return category;
    }
    
    public double getBaseFare() {
        return baseFare;
    }
    
    public double getPricePerKm() {
        return pricePerKm;
    }
    
    public double getPricePerMinute() {
        return pricePerMinute;
    }
    
    @Override
    public String toString() {
        return String.format("Tariff[category=%s, baseFare=%.2f, pricePerKm=%.2f, pricePerMinute=%.2f]",
                category, baseFare, pricePerKm, pricePerMinute);
    }
}
