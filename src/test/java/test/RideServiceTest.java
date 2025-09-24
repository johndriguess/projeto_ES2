package test;

import model.Passenger;
import model.Ride;
import model.Location;
import repo.RideRepository;
import repo.UserRepository;
import service.RideService;
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
    private Passenger testPassenger;
    
    @BeforeEach
    void setUp() throws IOException {
        rideRepo = new RideRepository(tempDir.resolve("test_rides.db").toString());
        userRepo = new UserRepository(tempDir.resolve("test_users.db").toString());
        rideService = new RideService(rideRepo, userRepo);
        
        testPassenger = new Passenger("João Silva", "joao@test.com", "83999999999", "senha123");
        userRepo.add(testPassenger);
    }
    
    @Test
    void testCreateRideRequest_Success() throws ValidationException, IOException {
        String origin = "Rua A, 123, Centro";
        String destination = "Rua B, 456, Bairro Novo";
        
        Ride ride = rideService.createRideRequest("joao@test.com", origin, destination);
        assertNotNull(ride);
        assertEquals("joao@test.com", ride.getPassengerEmail());
        assertEquals(origin, ride.getOrigin().getAddress());
        assertEquals(destination, ride.getDestination().getAddress());
        assertEquals(Ride.RideStatus.SOLICITADA, ride.getStatus());
        assertNotNull(ride.getId());
        assertNotNull(ride.getRequestTime());
    }
    
    @Test
    void testCreateRideRequest_UserNotFound() {
        ValidationException exception = assertThrows(ValidationException.class, () -> {
            rideService.createRideRequest("inexistente@test.com", "Rua A", "Rua B");
        });
        
        assertEquals("Usuário não encontrado.", exception.getMessage());
    }
    
    @Test
    void testCreateRideRequest_NotPassenger() throws IOException {
        model.Driver driver = new model.Driver("Motorista", "motorista@test.com", "83988888888", "senha123", "123456789");
        userRepo.add(driver);
        
        ValidationException exception = assertThrows(ValidationException.class, () -> {
            rideService.createRideRequest("motorista@test.com", "Rua A", "Rua B");
        });
        
        assertEquals("Apenas passageiros podem solicitar corridas.", exception.getMessage());
    }
    
    @Test
    void testCreateRideRequest_EmptyOrigin() {
        ValidationException exception = assertThrows(ValidationException.class, () -> {
            rideService.createRideRequest("joao@test.com", "", "Rua B");
        });
        
        assertEquals("Endereço de origem é obrigatório.", exception.getMessage());
    }
    
    @Test
    void testCreateRideRequest_EmptyDestination() {
        ValidationException exception = assertThrows(ValidationException.class, () -> {
            rideService.createRideRequest("joao@test.com", "Rua A", "");
        });
        
        assertEquals("Endereço de destino é obrigatório.", exception.getMessage());
    }
    
    @Test
    void testCreateRideRequest_SameOriginAndDestination() {
        ValidationException exception = assertThrows(ValidationException.class, () -> {
            rideService.createRideRequest("joao@test.com", "Rua A", "Rua A");
        });
        
        assertEquals("Origem e destino não podem ser iguais.", exception.getMessage());
    }
    
    @Test
    void testCreateRideRequest_ShortOrigin() {
        ValidationException exception = assertThrows(ValidationException.class, () -> {
            rideService.createRideRequest("joao@test.com", "Rua", "Rua B, 456");
        });
        
        assertEquals("Endereço de origem deve ter pelo menos 5 caracteres.", exception.getMessage());
    }
    
    @Test
    void testCreateRideRequest_ShortDestination() {
        ValidationException exception = assertThrows(ValidationException.class, () -> {
            rideService.createRideRequest("joao@test.com", "Rua A, 123", "Rua");
        });
        
        assertEquals("Endereço de destino deve ter pelo menos 5 caracteres.", exception.getMessage());
    }
    
    @Test
    void testGetRideById_Success() throws ValidationException, IOException {
        Ride ride = rideService.createRideRequest("joao@test.com", "Rua A", "Rua B");
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
        rideService.createRideRequest("joao@test.com", "Rua A", "Rua B");
        rideService.createRideRequest("joao@test.com", "Rua C", "Rua D");
        
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
        Ride ride = rideService.createRideRequest("joao@test.com", "Rua A", "Rua B");
        String rideId = ride.getId();
        
        rideService.updateRideStatus(rideId, Ride.RideStatus.ACEITA);
        
        Ride updatedRide = rideService.getRideById(rideId);
        assertEquals(Ride.RideStatus.ACEITA, updatedRide.getStatus());
    }
    
    @Test
    void testSetVehicleCategory_Success() throws ValidationException, IOException {
        Ride ride = rideService.createRideRequest("joao@test.com", "Rua A", "Rua B");
        String rideId = ride.getId();
        
        rideService.setVehicleCategory(rideId, "UberX");
        
        Ride updatedRide = rideService.getRideById(rideId);
        assertEquals("UberX", updatedRide.getVehicleCategory());
    }
    
    @Test
    void testSetVehicleCategory_EmptyCategory() throws ValidationException, IOException {
        Ride ride = rideService.createRideRequest("joao@test.com", "Rua A", "Rua B");
        String rideId = ride.getId();
        
        ValidationException exception = assertThrows(ValidationException.class, () -> {
            rideService.setVehicleCategory(rideId, "");
        });
        
        assertEquals("Categoria do veículo é obrigatória.", exception.getMessage());
    }

    @Test
    void testCreateRideRequest_ShouldNotifyCorrectDrivers() throws IOException, ValidationException {
        // Arrange: Create drivers with different vehicle categories
        model.Driver driverUberX = new model.Driver("Motorista UberX", "driverx@test.com", "83911111111", "senha123", "111");
        driverUberX.setVehicle(new model.Vehicle("P-111", "Onix", 2020, "Branco", model.VehicleCategory.UBER_X));
        userRepo.add(driverUberX);

        model.Driver driverComfort = new model.Driver("Motorista Comfort", "drivercomfort@test.com", "83922222222", "senha123", "222");
        driverComfort.setVehicle(new model.Vehicle("P-222", "Corolla", 2022, "Preto", model.VehicleCategory.UBER_COMFORT));
        userRepo.add(driverComfort);

        model.Driver driverNoVehicle = new model.Driver("Motorista Sem Carro", "drivernovehicle@test.com", "83933333333", "senha123", "333");
        userRepo.add(driverNoVehicle);

        // Capture System.out
        java.io.ByteArrayOutputStream outContent = new java.io.ByteArrayOutputStream();
        System.setOut(new java.io.PrintStream(outContent));

        // Act: Request a ride for a specific category
        rideService.createRideRequest("joao@test.com", "Rua Origem", "Rua Destino", "UberX");

        // Assert: Check the console output for notifications
        String output = outContent.toString();
        assertTrue(output.contains("Notificando motorista Motorista UberX"));
        assertFalse(output.contains("Notificando motorista Motorista Comfort"));
        assertFalse(output.contains("Notificando motorista Motorista Sem Carro"));

        // Clean up
        System.setOut(new java.io.PrintStream(new java.io.FileOutputStream(java.io.FileDescriptor.out)));
    }
}
