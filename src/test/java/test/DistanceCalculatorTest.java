package test;

import org.junit.jupiter.api.Test;
import util.DistanceCalculator;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class DistanceCalculatorTest {

    @Test
    public void testCalculateDistanceConsistency() {
        double dist1 = DistanceCalculator.calculateDistance("Origem A", "Destino B");
        double dist2 = DistanceCalculator.calculateDistance("Origem A", "Destino B");
        assertEquals(dist1, dist2);
    }

    @Test
    public void testCalculateDistanceDifferent() {
        double dist1 = DistanceCalculator.calculateDistance("Av. Paulista, 1000", "Rua Augusta, 500");
        double dist2 = DistanceCalculator.calculateDistance("Av. Brigadeiro Faria Lima, 3500", "Av. Rebouças, 1000");
        assertTrue(dist1 != dist2);
    }
    
    @Test
    public void testCalculateDistanceNullOrEmpty() {
        assertEquals(0.0, DistanceCalculator.calculateDistance(null, "Destino"));
        assertEquals(0.0, DistanceCalculator.calculateDistance("Origem", null));
        assertEquals(0.0, DistanceCalculator.calculateDistance(null, null));
    }

    @Test
    public void testCalculateEstimatedTime() {
        int time = DistanceCalculator.calculateEstimatedTime("Origem Longa", "Destino Bem Longe");
        assertTrue(time >= 10); 
    }
    
    @Test
    public void testCalculateEstimatedTimeFromDistance() {
        int time = DistanceCalculator.calculateEstimatedTime(10.0, 30.0);
        assertEquals(27, time); 
    }
    
    @Test
    public void testCalculateEstimatedTimeZero() {
        int time = DistanceCalculator.calculateEstimatedTime(0.0, 30.0);
        assertEquals(0, time);
    }
    
    @Test
    public void testWaitingTime() {
        assertEquals(7, DistanceCalculator.getWaitingTime());
    }
}