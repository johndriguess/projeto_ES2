package test;

import model.*;
import repo.RideRepository;
import repo.UserRepository;
import repo.VehicleRepository;
import service.AuthService;
import service.PricingService;
import service.RideService;
import util.ValidationException;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.io.File;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

public class RideServiceTest {

    private UserRepository userRepo;
    private VehicleRepository vehicleRepo;
    private RideRepository rideRepo;
    private PricingService pricingService;
    private AuthService authService;
    private RideService rideService;

    private Passenger passenger;
    private Driver driverNear;
    private Driver driverFar;
    private Driver driverPremiumNear;
    private Driver driverPremiumFar;

    private File userDb = new File("test_users_ride.date");
    private File vehicleDb = new File("test_vehicles_ride.db");
    private File rideDb = new File("test_rides_ride.db");

    @BeforeEach
    public void setUp() throws IOException, ValidationException {
        userDb.delete();
        vehicleDb.delete();
        rideDb.delete();

        userRepo = new UserRepository(userDb);
        vehicleRepo = new VehicleRepository(vehicleDb.getPath());
        rideRepo = new RideRepository(rideDb.getPath());
        pricingService = new PricingService();
        authService = new AuthService(userRepo, vehicleRepo);
        rideService = new RideService(rideRepo, userRepo, pricingService);

        passenger = authService.registerPassenger("Passageiro", "p@p.com", "111", "password123");

        driverNear = authService.registerDriver("Driver Perto", "near@d.com", "222", "password123", "cnh1", "PLT-001", "Kwid", 2019, "Branco"); 
        driverNear.setCurrentLocation(new Location("Rua Perto")); 
        driverNear.addRating(3);
        driverNear.getVehicle().setCategory("UBER_X");
        userRepo.update(driverNear);

        driverFar = authService.registerDriver("Driver Longe", "far@d.com", "333", "password123", "cnh2", "PLT-002", "Mobi", 2020, "Azul");
        driverFar.setCurrentLocation(new Location("Avenida Distante")); 
        driverFar.addRating(5);
        driverFar.getVehicle().setCategory("UBER_X");
        userRepo.update(driverFar);

        driverPremiumNear = authService.registerDriver("Driver Premium Perto", "premnear@d.com", "444", "password123", "cnh3", "PLT-003", "Civic", 2022, "Preto"); 
        driverPremiumNear.setCurrentLocation(new Location("Rua Perto")); 
        driverPremiumNear.addRating(3);
        driverPremiumNear.getVehicle().setCategory("UBER_COMFORT");
        userRepo.update(driverPremiumNear);
        
        driverPremiumFar = authService.registerDriver("Driver Premium Longe", "premfar@d.com", "555", "password123", "cnh4", "PLT-004", "Corolla", 2023, "Prata"); 
        driverPremiumFar.setCurrentLocation(new Location("Avenida Distante")); 
        driverPremiumFar.addRating(5);
        driverPremiumFar.getVehicle().setCategory("UBER_COMFORT");
        userRepo.update(driverPremiumFar);
    }

    @AfterEach
    public void tearDown() {
        userDb.delete();
        vehicleDb.delete();
        rideDb.delete();
    }

    @Test
    public void testAssignsClosestDriverForNonPremium() throws ValidationException, IOException {
    String category = "UBER_X";
        Ride ride = rideService.createRideRequest(passenger.getEmail(), "Rua Perto", "Destino", category, PaymentMethod.PIX);

        assertNotNull(ride.getDriverId());
        assertEquals(driverNear.getId(), ride.getDriverId());
        assertEquals(Ride.RideStatus.ACEITA, ride.getStatus());
        Driver driver = (Driver) userRepo.findById(driverNear.getId());
        assertFalse(driver.isAvailable());
    }

    @Test
    public void testAssignsHighestRatedDriverForPremium() throws ValidationException, IOException {
    String category = "UBER_COMFORT";
        Ride ride = rideService.createRideRequest(passenger.getEmail(), "Rua Perto", "Destino", category, PaymentMethod.PIX);
        
        assertNotNull(ride.getDriverId());
        assertEquals(driverPremiumFar.getId(), ride.getDriverId());
        assertEquals(Ride.RideStatus.ACEITA, ride.getStatus());
        Driver driver = (Driver) userRepo.findById(driverPremiumFar.getId());
        assertFalse(driver.isAvailable());
    }

    @Test
    public void testPremiumTieBreakerUsesDistance() throws ValidationException, IOException {
        // Ajusta o rating dos dois motoristas premium para o mesmo valor
        driverPremiumNear.getVehicle().setCategory("UBER_COMFORT");
        driverPremiumFar.getVehicle().setCategory("UBER_COMFORT");
            driverPremiumNear.setCurrentLocation(new Location("A"));
            driverPremiumFar.setCurrentLocation(new Location("Z"));

        while (driverPremiumNear.getAverageRating() < 5.0) {
            driverPremiumNear.addRating(5);
        }
        while (driverPremiumFar.getAverageRating() < 5.0) {
            driverPremiumFar.addRating(5);
        }

        userRepo.update(driverPremiumNear);
        userRepo.update(driverPremiumFar);

        String category = "UBER_COMFORT";
        Ride ride = rideService.createRideRequest(passenger.getEmail(), "Rua Perto", "Destino", category, PaymentMethod.PIX);
        
        assertNotNull(ride.getDriverId());
        assertEquals(driverPremiumNear.getId(), ride.getDriverId());
    }

    @Test
    public void testNoDriverAvailable() throws ValidationException, IOException {
        driverNear.setAvailable(false);
        userRepo.update(driverNear);
        driverFar.setAvailable(false);
        userRepo.update(driverFar);

        String category = "UBER_X";
        Ride ride = rideService.createRideRequest(passenger.getEmail(), "Rua Perto", "Destino", category, PaymentMethod.PIX);

        assertNull(ride.getDriverId());
        assertEquals(Ride.RideStatus.SOLICITADA, ride.getStatus());
    }

    @Test
    public void testDriverIsReleasedAfterReceipt() throws ValidationException, IOException {
    String category = "UBER_X";
        Ride ride = rideService.createRideRequest(passenger.getEmail(), "Rua Perto", "Destino", category, PaymentMethod.PIX);
        String driverId = ride.getDriverId();
        Driver driver = (Driver) userRepo.findById(driverId);
        assertFalse(driver.isAvailable());
        
        rideService.emitReceiptForRide(ride.getId(), "PIX");
        
        driver = (Driver) userRepo.findById(driverId);
        assertTrue(driver.isAvailable());
        assertEquals(Ride.RideStatus.FINALIZADA, rideRepo.findById(ride.getId()).getStatus());
    }
}