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
import java.util.List;

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
        // Arrange: Adiciona um motorista para que a corrida possa ser atribuída
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
        // Arrange: Cria dois motoristas da mesma categoria, mas com "localizações" diferentes
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

        // Calcula as distâncias para determinar qual motorista é o mais próximo
        double distDriverA = pricingService.calculateDistance("Rua Origem", "ABC-1234");
        double distDriverB = pricingService.calculateDistance("Rua Origem", "DEF-5678");

        // Act: Cria a requisição da corrida. A atribuição será automática.
        Ride ride = rideService.createRideRequest(testPassenger.getEmail(), "Rua Origem", "Rua Destino", "UberX");

        // Assert: Verifiqua se a corrida foi atribuída corretamente
        assertNotNull(ride.getDriverId());
        assertEquals(Ride.RideStatus.ACEITA, ride.getStatus());

        if (distDriverA < distDriverB) {
            assertEquals(driverA.getId(), ride.getDriverId());
        } else {
            assertEquals(driverB.getId(), ride.getDriverId());
        }
    }

    @Test
    void testUpdateEtaOnLocationUpdate_Success() throws ValidationException, IOException {
        // Arrange: Cria um motorista, um passageiro e uma corrida
        Driver driver = new Driver("Motorista ETA", "drivereta@test.com", "1111", "senha", "doc1");
        Vehicle vehicle = new Vehicle("ETA-0001", "Carro ETA", 2020, "Branco");
        vehicle.setCategory("UberX");
        driver.setVehicle(vehicle);
        userRepo.add(driver);

        String passengerEmail = "joao@test.com";
        String origin = "Rua Origem Teste";
        String destination = "Rua Destino Teste";
        String vehicleCategory = "UberX";
        Ride ride = rideService.createRideRequest(passengerEmail, origin, destination, vehicleCategory);
        String rideId = ride.getId();

        // Simula que o motorista já está a caminho, então a corrida está EM_ANDAMENTO
        rideService.updateRideStatus(rideId, Ride.RideStatus.EM_ANDAMENTO);

        // Act & Assert 1: Simula a primeira atualização da localização do motorista
        String location1 = "Rua Ponto 1";
        rideService.updateDriverLocation(rideId, location1);
        Ride updatedRide1 = rideRepo.findById(rideId); // Busque a corrida atualizada do repositório
        int eta1 = updatedRide1.getEstimatedTimeMinutes();
        assertTrue(eta1 > 0);

        // Act & Assert 2: Simula uma segunda atualização, mais perto do destino
        String location2 = "Rua Ponto 2, mais perto";
        rideService.updateDriverLocation(rideId, location2);
        Ride updatedRide2 = rideRepo.findById(rideId);
        int eta2 = updatedRide2.getEstimatedTimeMinutes();
        assertTrue(eta2 < eta1); // Verifique se a ETA diminuiu

        // Act & Assert 3: Simula a chegada ao destino
        String location3 = destination;
        rideService.updateDriverLocation(rideId, location3);
        Ride updatedRide3 = rideRepo.findById(rideId);
        int eta3 = updatedRide3.getEstimatedTimeMinutes();
        assertEquals(12, eta3);
    }
    
    @Test
    void testGenerateOptimizedRoute_Success() throws ValidationException, IOException {
        // Arrange: Cria um motorista, um passageiro e uma corrida
        Driver driver = new Driver("Motorista Rota", "driverrota@test.com", "1111", "senha", "doc1");
        Vehicle vehicle = new Vehicle("ROTA-0001", "Carro Rota", 2020, "Preto");
        vehicle.setCategory("UberX");
        driver.setVehicle(vehicle);
        userRepo.add(driver);

        String passengerEmail = "joao@test.com";
        String origin = "Rua Origem Rota";
        String destination = "Rua Destino Rota";
        String vehicleCategory = "UberX";
        Ride ride = rideService.createRideRequest(passengerEmail, origin, destination, vehicleCategory);
        String rideId = ride.getId();
        
        // Simula que o motorista já está a caminho, então a corrida está EM_ANDAMENTO
        rideService.updateRideStatus(rideId, Ride.RideStatus.EM_ANDAMENTO);

        // Act 1: Simula a primeira atualização da localização do motorista
        String location1 = "Rua Ponto 1 da Rota";
        rideService.updateDriverLocation(rideId, location1);
        Ride updatedRide1 = rideService.getRideById(rideId);

        // Assert 1: Verifica se a rota foi gerada
        List<String> route1 = updatedRide1.getOptimizedRoute();
        assertNotNull(route1);
        assertFalse(route1.isEmpty());
        assertTrue(route1.get(0).contains("Saia de " + location1));

        // Act 2: Simula uma segunda atualização da localização, mais perto do destino
        String location2 = "Rua Ponto 2 da Rota, mais perto";
        rideService.updateDriverLocation(rideId, location2);
        Ride updatedRide2 = rideService.getRideById(rideId);

        // Assert 2: Verifica se a rota foi recalculada
        List<String> route2 = updatedRide2.getOptimizedRoute();
        assertNotNull(route2);
        assertFalse(route2.isEmpty());
        assertTrue(route2.get(0).contains("Saia de " + location2));
    }
}