package test;

import model.Location;
import model.Ride;
import service.RouteService;
import util.ValidationException;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

public class RouteServiceTest {
    @Test
    public void testGenerateRouteWithoutDriverLocation() {
        Ride ride = new Ride("pass1", "pass@example.com", new Location("Origem A"), new Location("Destino B"));
        // driver location is not set
        RouteService routeService = new RouteService();
        routeService.generateRoute(ride);

        List<String> steps = ride.getOptimizedRoute();
        assertNotNull(steps, "Route steps should not be null");
        assertEquals(1, steps.size(), "Should have only origin->destination when driver location missing");
        assertTrue(steps.get(0).contains("Passageiro -> Destino"));
        assertTrue(ride.getEstimatedTimeMinutes() > 0, "ETA should be positive");
    }

    @Test
    public void testGenerateRouteWithDriverLocation() {
        Ride ride = new Ride("pass2", "pass2@example.com", new Location("Origem X"), new Location("Destino Y"));
        ride.setDriverCurrentLocation(new Location("Rua Motorista"));
        RouteService routeService = new RouteService();
        routeService.generateRoute(ride);

        List<String> steps = ride.getOptimizedRoute();
        assertNotNull(steps, "Route steps should not be null");
        assertEquals(2, steps.size(), "Should have two segments when driver location available");
        assertTrue(steps.get(0).contains("Motorista -> Passageiro"));
        assertTrue(steps.get(1).contains("Passageiro -> Destino"));
        assertTrue(ride.getEstimatedTimeMinutes() > 0, "ETA should be positive");
    }
}
