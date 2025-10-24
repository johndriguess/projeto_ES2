package test;

import model.PricingInfo;
import model.Tariff;
import service.PricingService;
import util.ValidationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

public class PricingServiceTest {
    
    private PricingService pricingService;
    
    @BeforeEach
    void setUp() {
        pricingService = new PricingService();
    }
    
    @Test
    void testCalculatePricing_UberX_Success() throws ValidationException {
        String origin = "Rua A, 123";
        String destination = "Rua B, 456";
        String category = "UberX";
        
        PricingInfo pricing = pricingService.calculatePricing(origin, destination, category);
        assertNotNull(pricing);
        assertEquals(category, pricing.getCategory());
        assertTrue(pricing.getEstimatedDistance() > 0);
        assertTrue(pricing.getEstimatedTimeMinutes() > 0);
        assertEquals(2.50, pricing.getBaseFare(), 0.01);
        assertTrue(pricing.getTotalPrice() > pricing.getBaseFare());
    }
    
    @Test
    void testCalculatePricing_UberBlack_Success() throws ValidationException {
        String origin = "Rua A, 123";
        String destination = "Rua B, 456";
        String category = "Uber Black";
        
        PricingInfo pricing = pricingService.calculatePricing(origin, destination, category);
        assertNotNull(pricing);
        assertEquals(category, pricing.getCategory());
        assertEquals(4.00, pricing.getBaseFare(), 0.01);
        assertTrue(pricing.getTotalPrice() > pricing.getBaseFare());
    }
    
    @Test
    void testCalculatePricing_EmptyOrigin() {
        ValidationException exception = assertThrows(ValidationException.class, () -> {
            pricingService.calculatePricing("", "Rua B", "UberX");
        });
        
        assertEquals("Endereço de origem é obrigatório.", exception.getMessage());
    }
    
    @Test
    void testCalculatePricing_EmptyDestination() {
        ValidationException exception = assertThrows(ValidationException.class, () -> {
            pricingService.calculatePricing("Rua A", "", "UberX");
        });
        
        assertEquals("Endereço de destino é obrigatório.", exception.getMessage());
    }
    
    @Test
    void testCalculatePricing_EmptyCategory() {
        ValidationException exception = assertThrows(ValidationException.class, () -> {
            pricingService.calculatePricing("Rua A", "Rua B", "");
        });
        
        assertEquals("Categoria do veículo é obrigatória.", exception.getMessage());
    }
    
    @Test
    void testCalculatePricing_InvalidCategory() {
        ValidationException exception = assertThrows(ValidationException.class, () -> {
            pricingService.calculatePricing("Rua A", "Rua B", "CategoriaInexistente");
        });
        
        assertEquals("Categoria de veículo não encontrada: CategoriaInexistente", exception.getMessage());
    }
    
    @Test
    void testCalculateAllPricing_Success() throws ValidationException {
        String origin = "Rua A, 123";
        String destination = "Rua B, 456";
        
        List<PricingInfo> pricingList = pricingService.calculateAllPricing(origin, destination);
        assertNotNull(pricingList);
        assertEquals(5, pricingList.size());
        
        for (int i = 1; i < pricingList.size(); i++) {
            assertTrue(pricingList.get(i-1).getTotalPrice() <= pricingList.get(i).getTotalPrice());
        }
        
        Set<String> categories = Set.of("UberX", "Uber Comfort", "Uber Black", "Uber Bag", "Uber XL");
        for (PricingInfo pricing : pricingList) {
            assertTrue(categories.contains(pricing.getCategory()));
        }
    }
    
    @Test
    void testGetTariffForCategory_UberX() throws ValidationException {
        Tariff tariff = pricingService.getTariffForCategory("UberX");
        assertNotNull(tariff);
        assertEquals("UberX", tariff.getCategory());
        assertEquals(2.50, tariff.getBaseFare(), 0.01);
        assertEquals(1.20, tariff.getPricePerKm(), 0.01);
        assertEquals(0.30, tariff.getPricePerMinute(), 0.01);
    }
    
    @Test
    void testGetTariffForCategory_UberBlack() throws ValidationException {
        Tariff tariff = pricingService.getTariffForCategory("Uber Black");
        assertNotNull(tariff);
        assertEquals("Uber Black", tariff.getCategory());
        assertEquals(4.00, tariff.getBaseFare(), 0.01);
        assertEquals(2.00, tariff.getPricePerKm(), 0.01);
        assertEquals(0.50, tariff.getPricePerMinute(), 0.01);
    }
    
    @Test
    void testGetTariffForCategory_InvalidCategory() {
        ValidationException exception = assertThrows(ValidationException.class, () -> {
            pricingService.getTariffForCategory("CategoriaInexistente");
        });
        
        assertEquals("Categoria de veículo não encontrada: CategoriaInexistente", exception.getMessage());
    }
    
    @Test
    void testGetAllTariffs() {
        Map<String, Tariff> tariffs = pricingService.getAllTariffs();
        assertNotNull(tariffs);
        assertEquals(5, tariffs.size());
        assertTrue(tariffs.containsKey("UberX"));
        assertTrue(tariffs.containsKey("Uber Comfort"));
        assertTrue(tariffs.containsKey("Uber Black"));
        assertTrue(tariffs.containsKey("Uber Bag"));
        assertTrue(tariffs.containsKey("Uber XL"));
    }
    
    @Test
    void testGetAvailableCategories() {
        Set<String> categories = pricingService.getAvailableCategories();
        assertNotNull(categories);
        assertEquals(5, categories.size());
        assertTrue(categories.contains("UberX"));
        assertTrue(categories.contains("Uber Comfort"));
        assertTrue(categories.contains("Uber Black"));
        assertTrue(categories.contains("Uber Bag"));
        assertTrue(categories.contains("Uber XL"));
    }
    
    @Test
    void testCalculateDistance() {
        double distance1 = pricingService.calculateDistance("Av. Paulista, 1000", "Rua Augusta, 500");
        double distance2 = pricingService.calculateDistance("Av. Paulista, 1000", "Rua Augusta, 500");
        double distance3 = pricingService.calculateDistance("Av. Brigadeiro Faria Lima, 3500", "Av. Rebouças, 1000");
        assertTrue(distance1 > 0);
        assertEquals(distance1, distance2, 0.01);
        assertNotEquals(distance1, distance3, 0.01);
    }
    
    @Test
    void testCalculateEstimatedTime() {
        int time1 = pricingService.calculateEstimatedTime("Av. Paulista, 1000", "Rua Augusta, 500");
        int time2 = pricingService.calculateEstimatedTime("Av. Paulista, 1000", "Rua Augusta, 500");
        int time3 = pricingService.calculateEstimatedTime("Av. Brigadeiro Faria Lima, 3500", "Av. Rebouças, 1000");
        assertTrue(time1 >= 10);
        assertTrue(time1 <= 120);
        assertEquals(time1, time2);
        assertNotEquals(time1, time3);
    }
    
    @Test
    void testPricingInfo_FormattedMethods() throws ValidationException {
        PricingInfo pricing = pricingService.calculatePricing("Rua A", "Rua B", "UberX");
        assertNotNull(pricing.getFormattedTime());
        assertTrue(pricing.getFormattedTime().contains("min"));
        
        assertNotNull(pricing.getFormattedPrice());
        assertTrue(pricing.getFormattedPrice().startsWith("R$"));
        
        assertNotNull(pricing.getFormattedDistance());
        assertTrue(pricing.getFormattedDistance().contains("km"));
    }
}
