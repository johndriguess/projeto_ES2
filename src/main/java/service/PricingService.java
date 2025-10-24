package service;

import model.PricingInfo;
import model.Tariff;
import model.VehicleCategory;
import util.DistanceCalculator;
import util.ValidationException;
import java.util.*;

public class PricingService {
    
    private final Map<String, Tariff> tariffs;
    private double dynamicFareFactor = 1.0; 
    
    public PricingService() {
        this.tariffs = initializeTariffs();
    }
    
    private Map<String, Tariff> initializeTariffs() {
        Map<String, Tariff> tariffMap = new HashMap<>();
        //  mapeia uma tarifa por categoria
        // tarifa base + preco/km + preco/min + velocidade media p calcular o tempo
        tariffMap.put("UberX", new Tariff("UberX", 2.50, 1.20, 0.30, 30.0));
        tariffMap.put("Uber Comfort", new Tariff("Uber Comfort", 3.00, 1.50, 0.35, 35.0));
        tariffMap.put("Uber Black", new Tariff("Uber Black", 4.00, 2.00, 0.50, 40.0));
        tariffMap.put("Uber Bag", new Tariff("Uber Bag", 2.80, 1.30, 0.32, 28.0));
        tariffMap.put("Uber XL", new Tariff("Uber XL", 3.50, 1.80, 0.40, 32.0));
        
        return tariffMap;
    }

    public void setDynamicFareFactor(double factor) {
        if (factor > 0) {
            this.dynamicFareFactor = factor;
        }
    }

    public double getSpeedForCategory(String category) throws ValidationException {
        Tariff tariff = getTariffForCategory(category);
        return tariff.getSpeedKmH();
    }
    // esse método é reponsável pelo calculo total da corrida
    //tarifa base + distancia (valor /km) + tempo ( valor/min) + fator de ajuste
    public PricingInfo calculatePricing(String origin, String destination, String category) 
            throws ValidationException {
        
        if (origin == null || origin.trim().isEmpty()) {
            throw new ValidationException("Endereço de origem é obrigatório.");
        }
        
        if (destination == null || destination.trim().isEmpty()) {
            throw new ValidationException("Endereço de destino é obrigatório.");
        }
        
        if (category == null || category.trim().isEmpty()) {
            throw new ValidationException("Categoria do veículo é obrigatória.");
        }
        
        Tariff tariff = tariffs.get(category);
        if (tariff == null) {
            throw new ValidationException("Categoria de veículo não encontrada: " + category);
        }
        
        double distance = DistanceCalculator.calculateDistance(origin, destination);
        int timeMinutes = DistanceCalculator.calculateEstimatedTime(distance, tariff.getSpeedKmH());
        
        double distancePrice = distance * tariff.getPricePerKm();
        double timePrice = timeMinutes * tariff.getPricePerMinute();
        double totalPrice = tariff.getBaseFare() + distancePrice + timePrice;
        
        // Aplica a tarifa dinâmica ao preço total
        totalPrice = totalPrice * dynamicFareFactor;
        
        totalPrice = Math.round(totalPrice * 100.0) / 100.0;
        distancePrice = Math.round(distancePrice * 100.0) / 100.0;
        timePrice = Math.round(timePrice * 100.0) / 100.0;
        
        return new PricingInfo(category, distance, timeMinutes, 
                             tariff.getBaseFare(), distancePrice, timePrice, totalPrice);
    }
    
    public List<PricingInfo> calculateAllPricing(String origin, String destination) 
            throws ValidationException {
        
        List<PricingInfo> pricingList = new ArrayList<>();
        
        for (String category : tariffs.keySet()) {
            PricingInfo pricing = calculatePricing(origin, destination, category);
            pricingList.add(pricing);
        }
        
        pricingList.sort(Comparator.comparing(PricingInfo::getTotalPrice));
        
        return pricingList;
    }
    
    public Tariff getTariffForCategory(String category) throws ValidationException {
        if (category == null || category.trim().isEmpty()) {
            throw new ValidationException("Categoria do veículo é obrigatória.");
        }
        
        Tariff tariff = tariffs.get(category);
        if (tariff == null) {
            throw new ValidationException("Categoria de veículo não encontrada: " + category);
        }
        
        return tariff;
    }
    
    public Map<String, Tariff> getAllTariffs() {
        return new HashMap<>(tariffs);
    }
    
    public Set<String> getAvailableCategories() {
        return new HashSet<>(tariffs.keySet());
    }
    
    public double calculateDistance(String origin, String destination) {
        return DistanceCalculator.calculateDistance(origin, destination);
    }
    
    public int calculateEstimatedTime(String origin, String destination) {
        double distance = DistanceCalculator.calculateDistance(origin, destination);
        return DistanceCalculator.calculateEstimatedTime(distance, 30.0);
    }
}