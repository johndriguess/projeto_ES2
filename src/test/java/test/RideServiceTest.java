package test;

import model.Passenger;
import model.Ride;
import model.Location;
import model.Driver;
import model.Vehicle;
import model.VehicleCategory;
import repo.RideRepository;
import repo.UserRepository;
import service.RideService;
import service.PricingService;
import util.ValidationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

public class RideServiceTest {

    @TempDir
    Path tempDir;

    private RideRepository rideRepo;
    private UserRepository userRepo;
    private RideService rideService;
    private PricingService pricingService;
    private Passenger testPassenger;

    @BeforeEach
    void setUp() throws IOException {
        rideRepo = new RideRepository(tempDir.resolve("test_rides.db").toString());
        userRepo = new UserRepository(tempDir.resolve("test_users.db").toString());
        rideService = new RideService(rideRepo, userRepo);
        pricingService = new PricingService();

        testPassenger = new Passenger("João Silva", "joao@test.com", "83999999999", "senha123");
        userRepo.add(testPassenger);
    }

    @Test
    void testCreateRideRequest_Success() throws ValidationException, IOException {
        Driver testDriver = new Driver("Motorista Teste", "motorista@test.com", "83988888888", "senha123", "123456789");
        Vehicle vehicle = new Vehicle("AAA-1111", "Modelo Teste", 2018, "Branco");
        vehicle.setCategory("UberX");
        testDriver.setVehicle(vehicle);
        userRepo.add(testDriver);

        String origin = "Rua A, 123, Centro";
        String destination = "Rua B, 456, Bairro Novo";
        String vehicleCategory = "UberX";

        Ride ride = rideService.createRideRequest("joao@test.com", origin, destination, vehicleCategory);
        assertNotNull(ride);
        assertEquals("joao@test.com", ride.getPassengerEmail());
        assertEquals(origin, ride.getOrigin().getAddress());
        assertEquals(destination, ride.getDestination().getAddress());
        assertEquals(Ride.RideStatus.ACEITA, ride.getStatus()); 
        assertNotNull(ride.getId());
        assertNotNull(ride.getRequestTime());
    }
    @Test
    void testCreateRideRequest_UserNotFound() {
        ValidationException exception = assertThrows(ValidationException.class, () -> {
            rideService.createRideRequest("inexistente@test.com", "Rua A", "Rua B", "UberX");
        });

        assertEquals("Usuário não encontrado.", exception.getMessage());
    }

    @Test
    void testCreateRideRequest_NotPassenger() throws IOException {
        model.Driver driver = new model.Driver("Motorista", "motorista@test.com", "83988888888", "senha123", "123456789");
        userRepo.add(driver);

        ValidationException exception = assertThrows(ValidationException.class, () -> {
            rideService.createRideRequest("motorista@test.com", "Rua A", "Rua B", "UberX");
        });

        assertEquals("Apenas passageiros podem solicitar corridas.", exception.getMessage());
    }

    @Test
    void testCreateRideRequest_EmptyOrigin() {
        ValidationException exception = assertThrows(ValidationException.class, () -> {
            rideService.createRideRequest("joao@test.com", "", "Rua B", "UberX");
        });

        assertEquals("Endereço de origem é obrigatório.", exception.getMessage());
    }

    @Test
    void testCreateRideRequest_EmptyDestination() {
        ValidationException exception = assertThrows(ValidationException.class, () -> {
            rideService.createRideRequest("joao@test.com", "Rua A", "", "UberX");
        });

        assertEquals("Endereço de destino é obrigatório.", exception.getMessage());
    }

    @Test
    void testCreateRideRequest_SameOriginAndDestination() {
        ValidationException exception = assertThrows(ValidationException.class, () -> {
            rideService.createRideRequest("joao@test.com", "Rua A", "Rua A", "UberX");
        });

        assertEquals("Origem e destino não podem ser iguais.", exception.getMessage());
    }

    @Test
    void testCreateRideRequest_ShortOrigin() {
        ValidationException exception = assertThrows(ValidationException.class, () -> {
            rideService.createRideRequest("joao@test.com", "Rua", "Rua B, 456", "UberX");
        });

        assertEquals("Endereço de origem deve ter pelo menos 5 caracteres.", exception.getMessage());
    }

    @Test
    void testCreateRideRequest_ShortDestination() {
        ValidationException exception = assertThrows(ValidationException.class, () -> {
            rideService.createRideRequest("joao@test.com", "Rua A, 123", "Rua", "UberX");
        });

        assertEquals("Endereço de destino deve ter pelo menos 5 caracteres.", exception.getMessage());
    }

    @Test
    void testGetRideById_Success() throws ValidationException, IOException {
        Ride ride = rideService.createRideRequest("joao@test.com", "Rua A", "Rua B", "UberX");
        String rideId = ride.getId();

        Ride foundRide = rideService.getRideById(rideId);
        assertNotNull(foundRide);
        assertEquals(rideId, foundRide.getId());
        assertEquals("joao@test.com", foundRide.getPassengerEmail());
    }

    @Test
    void testGetRideById_NotFound() {
        ValidationException exception = assertThrows(ValidationException.class, () -> {
            rideService.getRideById("inexistente");
        });

        assertEquals("Corrida não encontrada.", exception.getMessage());
    }

    @Test
    void testGetRidesByPassenger_Success() throws ValidationException, IOException {
        rideService.createRideRequest("joao@test.com", "Rua A", "Rua B", "UberX");
        rideService.createRideRequest("joao@test.com", "Rua C", "Rua D", "UberX");

        java.util.Collection<Ride> rides = rideService.getRidesByPassenger("joao@test.com");
        assertEquals(2, rides.size());
    }

    @Test
    void testGetRidesByPassenger_Empty() throws ValidationException {
        java.util.Collection<Ride> rides = rideService.getRidesByPassenger("joao@test.com");
        assertTrue(rides.isEmpty());
    }

    @Test
    void testUpdateRideStatus_Success() throws ValidationException, IOException {
        Ride ride = rideService.createRideRequest("joao@test.com", "Rua A", "Rua B", "UberX");
        String rideId = ride.getId();

        rideService.updateRideStatus(rideId, Ride.RideStatus.ACEITA);

        Ride updatedRide = rideService.getRideById(rideId);
        assertEquals(Ride.RideStatus.ACEITA, updatedRide.getStatus());
    }

    @Test
    void testSetVehicleCategory_Success() throws ValidationException, IOException {
        Ride ride = rideService.createRideRequest("joao@test.com", "Rua A", "Rua B", "UberX");
        String rideId = ride.getId();

        rideService.setVehicleCategory(rideId, "UberX");

        Ride updatedRide = rideService.getRideById(rideId);
        assertEquals("UberX", updatedRide.getVehicleCategory());
    }

    @Test
    void testSetVehicleCategory_EmptyCategory() throws ValidationException, IOException {
        Ride ride = rideService.createRideRequest("joao@test.com", "Rua A", "Rua B", "UberX");
        String rideId = ride.getId();

        ValidationException exception = assertThrows(ValidationException.class, () -> {
            rideService.setVehicleCategory(rideId, "");
        });

        assertEquals("Categoria do veículo é obrigatória.", exception.getMessage());
    }

    @Test
    void testAssignClosestDriver_ShouldAssignCorrectly() throws ValidationException, IOException {
        Driver driverA = new Driver("Motorista A", "motoristaA@test.com", "1111", "senha", "doc1");
        Vehicle vehicleA = new Vehicle("ABC-1234", "Carro A", 2018, "Branco");
        vehicleA.setCategory("UberX");
        driverA.setVehicle(vehicleA);
        userRepo.add(driverA);

        Driver driverB = new Driver("Motorista B", "motoristaB@test.com", "2222", "senha", "doc2");
        Vehicle vehicleB = new Vehicle("DEF-5678", "Carro B", 2019, "Preto");
        vehicleB.setCategory("UberX");
        driverB.setVehicle(vehicleB);
        userRepo.add(driverB);

        double distDriverA = pricingService.calculateDistance("Rua Origem", "ABC-1234");
        double distDriverB = pricingService.calculateDistance("Rua Origem", "DEF-5678");

        Ride ride = rideService.createRideRequest(testPassenger.getEmail(), "Rua Origem", "Rua Destino", "UberX");

        assertNotNull(ride.getDriverId());
        assertEquals(Ride.RideStatus.ACEITA, ride.getStatus());

        if (distDriverA < distDriverB) {
            assertEquals(driverA.getId(), ride.getDriverId());
        } else {
            assertEquals(driverB.getId(), ride.getDriverId());
        }
    }
}