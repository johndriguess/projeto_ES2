package model;

import java.io.Serializable;

public class Tariff implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private String category;
    private double baseFare;
    private double pricePerKm;
    private double pricePerMinute;
    private double speedKmH; 
    
    public Tariff(String category, double baseFare, double pricePerKm, double pricePerMinute, double speedKmH) {
        this.category = category;
        this.baseFare = baseFare;
        this.pricePerKm = pricePerKm;
        this.pricePerMinute = pricePerMinute;
        this.speedKmH = speedKmH;
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

    public double getSpeedKmH() {
        return speedKmH;
    }
    
    @Override
    public String toString() {
        return String.format("Tariff[category=%s, baseFare=%.2f, pricePerKm=%.2f, pricePerMinute=%.2f, speedKmH=%.2f]",
                category, baseFare, pricePerKm, pricePerMinute, speedKmH);
    }
}