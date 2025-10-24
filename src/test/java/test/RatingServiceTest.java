package test;

import model.Driver;
import model.Passenger;
import model.Ride;
import model.Location;
import model.Vehicle;
import repo.RideRepository;
import repo.UserRepository;
import service.AuthService;
import service.RatingService;
import util.ValidationException;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.io.File;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

public class RatingServiceTest {

    private UserRepository userRepo;
    private RideRepository rideRepo;
    private RatingService ratingService;

    private Passenger passenger;
    private Driver driver;
    private Ride finalizedRide;
    private Ride pendingRide;

    private File userDb = new File("test_users_rating.date");
    private File rideDb = new File("test_rides_rating.db");

    @BeforeEach
    public void setUp() throws IOException, ValidationException {
        userDb.delete();
        rideDb.delete();

        userRepo = new UserRepository(userDb);
        rideRepo = new RideRepository(rideDb.getPath());
        ratingService = new RatingService(userRepo, rideRepo);

        passenger = new Passenger("Passageiro", "p@p.com", "111", "pass");
        userRepo.add(passenger);
        
        Vehicle v = new Vehicle("ABC-123", "Corolla", 2020, "Preto");
        v.setCategory("UberX");
        driver = new Driver("Motorista", "d@d.com", "222", "pass", "123cnh", v);
        userRepo.add(driver);

        finalizedRide = new Ride(passenger.getId(), passenger.getEmail(), new Location("Origem"), new Location("Destino"));
        finalizedRide.setDriverId(driver.getId());
        finalizedRide.setStatus(Ride.RideStatus.FINALIZADA);
        rideRepo.add(finalizedRide);

        pendingRide = new Ride(passenger.getId(), passenger.getEmail(), new Location("Origem"), new Location("Destino"));
        pendingRide.setDriverId(driver.getId());
        pendingRide.setStatus(Ride.RideStatus.ACEITA);
        rideRepo.add(pendingRide);
    }

    @AfterEach
    public void tearDown() {
        userDb.delete();
        rideDb.delete();
    }

    @Test
    public void testPassengerRatesDriverSuccess() throws ValidationException, IOException {
        ratingService.rateDriver(finalizedRide, 5);

        Driver reloadedDriver = (Driver) userRepo.findById(driver.getId());
        Ride reloadedRide = rideRepo.findById(finalizedRide.getId());

        assertEquals(5.0, reloadedDriver.getAverageRating());
        assertEquals(1, reloadedDriver.getTotalRatings());
        assertTrue(reloadedRide.hasPassengerRated());
        assertFalse(reloadedRide.hasDriverRated());
    }

    @Test
    public void testDriverRatesPassengerSuccess() throws ValidationException, IOException {
        ratingService.ratePassenger(finalizedRide, 3);

        Passenger reloadedPassenger = (Passenger) userRepo.findById(passenger.getId());
        Ride reloadedRide = rideRepo.findById(finalizedRide.getId());

        assertEquals(3.0, reloadedPassenger.getAverageRating());
        assertEquals(1, reloadedPassenger.getTotalRatings());
        assertTrue(reloadedRide.hasDriverRated());
        assertFalse(reloadedRide.hasPassengerRated());
    }

    @Test
    public void testCannotRateRideNotFinalizada() {
        ValidationException ex = assertThrows(ValidationException.class, () -> {
            ratingService.rateDriver(pendingRide, 5);
        });
        assertEquals("A corrida deve estar 'Finalizada' para ser avaliada.", ex.getMessage());
    }

    @Test
    public void testPassengerCannotRateTwice() throws ValidationException, IOException {
        ratingService.rateDriver(finalizedRide, 5);

        ValidationException ex = assertThrows(ValidationException.class, () -> {
            ratingService.rateDriver(finalizedRide, 1);
        });
        assertEquals("Você já avaliou o motorista para esta corrida.", ex.getMessage());
    }

    @Test
    public void testDriverCannotRateTwice() throws ValidationException, IOException {
        ratingService.ratePassenger(finalizedRide, 4);

        ValidationException ex = assertThrows(ValidationException.class, () -> {
            ratingService.ratePassenger(finalizedRide, 2);
        });
        assertEquals("Você já avaliou o passageiro para esta corrida.", ex.getMessage());
    }
}