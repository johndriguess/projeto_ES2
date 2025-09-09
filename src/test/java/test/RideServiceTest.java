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

/**
 * Testes unitários para RideService - RF04
 */
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
        
        // Criar passageiro de teste
        testPassenger = new Passenger("João Silva", "joao@test.com", "83999999999", "senha123");
        userRepo.add(testPassenger);
    }
    
    @Test
    void testCreateRideRequest_Success() throws ValidationException, IOException {
        // Arrange
        String origin = "Rua A, 123, Centro";
        String destination = "Rua B, 456, Bairro Novo";
        
        // Act
        Ride ride = rideService.createRideRequest("joao@test.com", origin, destination);
        
        // Assert
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
        // Act & Assert
        ValidationException exception = assertThrows(ValidationException.class, () -> {
            rideService.createRideRequest("inexistente@test.com", "Rua A", "Rua B");
        });
        
        assertEquals("Usuário não encontrado.", exception.getMessage());
    }
    
    @Test
    void testCreateRideRequest_NotPassenger() throws IOException {
        // Arrange - Criar um motorista
        model.Driver driver = new model.Driver("Motorista", "motorista@test.com", "83988888888", "senha123", "123456789");
        userRepo.add(driver);
        
        // Act & Assert
        ValidationException exception = assertThrows(ValidationException.class, () -> {
            rideService.createRideRequest("motorista@test.com", "Rua A", "Rua B");
        });
        
        assertEquals("Apenas passageiros podem solicitar corridas.", exception.getMessage());
    }
    
    @Test
    void testCreateRideRequest_EmptyOrigin() {
        // Act & Assert
        ValidationException exception = assertThrows(ValidationException.class, () -> {
            rideService.createRideRequest("joao@test.com", "", "Rua B");
        });
        
        assertEquals("Endereço de origem é obrigatório.", exception.getMessage());
    }
    
    @Test
    void testCreateRideRequest_EmptyDestination() {
        // Act & Assert
        ValidationException exception = assertThrows(ValidationException.class, () -> {
            rideService.createRideRequest("joao@test.com", "Rua A", "");
        });
        
        assertEquals("Endereço de destino é obrigatório.", exception.getMessage());
    }
    
    @Test
    void testCreateRideRequest_SameOriginAndDestination() {
        // Act & Assert
        ValidationException exception = assertThrows(ValidationException.class, () -> {
            rideService.createRideRequest("joao@test.com", "Rua A", "Rua A");
        });
        
        assertEquals("Origem e destino não podem ser iguais.", exception.getMessage());
    }
    
    @Test
    void testCreateRideRequest_ShortOrigin() {
        // Act & Assert
        ValidationException exception = assertThrows(ValidationException.class, () -> {
            rideService.createRideRequest("joao@test.com", "Rua", "Rua B, 456");
        });
        
        assertEquals("Endereço de origem deve ter pelo menos 5 caracteres.", exception.getMessage());
    }
    
    @Test
    void testCreateRideRequest_ShortDestination() {
        // Act & Assert
        ValidationException exception = assertThrows(ValidationException.class, () -> {
            rideService.createRideRequest("joao@test.com", "Rua A, 123", "Rua");
        });
        
        assertEquals("Endereço de destino deve ter pelo menos 5 caracteres.", exception.getMessage());
    }
    
    @Test
    void testGetRideById_Success() throws ValidationException, IOException {
        // Arrange
        Ride ride = rideService.createRideRequest("joao@test.com", "Rua A", "Rua B");
        String rideId = ride.getId();
        
        // Act
        Ride foundRide = rideService.getRideById(rideId);
        
        // Assert
        assertNotNull(foundRide);
        assertEquals(rideId, foundRide.getId());
        assertEquals("joao@test.com", foundRide.getPassengerEmail());
    }
    
    @Test
    void testGetRideById_NotFound() {
        // Act & Assert
        ValidationException exception = assertThrows(ValidationException.class, () -> {
            rideService.getRideById("inexistente");
        });
        
        assertEquals("Corrida não encontrada.", exception.getMessage());
    }
    
    @Test
    void testGetRidesByPassenger_Success() throws ValidationException, IOException {
        // Arrange
        rideService.createRideRequest("joao@test.com", "Rua A", "Rua B");
        rideService.createRideRequest("joao@test.com", "Rua C", "Rua D");
        
        // Act
        java.util.Collection<Ride> rides = rideService.getRidesByPassenger("joao@test.com");
        
        // Assert
        assertEquals(2, rides.size());
    }
    
    @Test
    void testGetRidesByPassenger_Empty() throws ValidationException {
        // Act
        java.util.Collection<Ride> rides = rideService.getRidesByPassenger("joao@test.com");
        
        // Assert
        assertTrue(rides.isEmpty());
    }
    
    @Test
    void testUpdateRideStatus_Success() throws ValidationException, IOException {
        // Arrange
        Ride ride = rideService.createRideRequest("joao@test.com", "Rua A", "Rua B");
        String rideId = ride.getId();
        
        // Act
        rideService.updateRideStatus(rideId, Ride.RideStatus.ACEITA);
        
        // Assert
        Ride updatedRide = rideService.getRideById(rideId);
        assertEquals(Ride.RideStatus.ACEITA, updatedRide.getStatus());
    }
    
    @Test
    void testSetVehicleCategory_Success() throws ValidationException, IOException {
        // Arrange
        Ride ride = rideService.createRideRequest("joao@test.com", "Rua A", "Rua B");
        String rideId = ride.getId();
        
        // Act
        rideService.setVehicleCategory(rideId, "UberX");
        
        // Assert
        Ride updatedRide = rideService.getRideById(rideId);
        assertEquals("UberX", updatedRide.getVehicleCategory());
    }
    
    @Test
    void testSetVehicleCategory_EmptyCategory() throws ValidationException, IOException {
        // Arrange
        Ride ride = rideService.createRideRequest("joao@test.com", "Rua A", "Rua B");
        String rideId = ride.getId();
        
        // Act & Assert
        ValidationException exception = assertThrows(ValidationException.class, () -> {
            rideService.setVehicleCategory(rideId, "");
        });
        
        assertEquals("Categoria do veículo é obrigatória.", exception.getMessage());
    }
}
