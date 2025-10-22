package test;

import model.RideHistory;
import model.Ride;
import model.Passenger;
import model.Driver;
import model.Location;
import model.PaymentMethod;
import repo.RideHistoryRepository;
import repo.UserRepository;
import service.RideHistoryService;
import util.ValidationException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.AfterEach;
import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public class RideHistoryServiceTest {
    private RideHistoryRepository historyRepo;
    private UserRepository userRepo;
    private RideHistoryService historyService;
    private final String TEST_HISTORY_DB = "test_ride_history.db";
    private final String TEST_USER_DB = "test_users_history.db";

    @BeforeEach
    void setUp() throws IOException {
        historyRepo = new RideHistoryRepository(TEST_HISTORY_DB);
        userRepo = new UserRepository();
        userRepo.setStorageFile(TEST_USER_DB);
        historyService = new RideHistoryService(historyRepo, userRepo);
    }

    @AfterEach
    void tearDown() {
        // Limpar arquivos de teste
        java.io.File historyFile = new java.io.File(TEST_HISTORY_DB);
        if (historyFile.exists()) {
            historyFile.delete();
        }
        
        java.io.File userFile = new java.io.File(TEST_USER_DB);
        if (userFile.exists()) {
            userFile.delete();
        }
    }

    @Test
    void testAddRideToHistory() throws ValidationException, IOException {
        // Criar um passageiro de teste
        Passenger passenger = new Passenger("João Silva", "joao@test.com", "123456789", "senha123");
        userRepo.add(passenger);

        // Criar uma corrida de teste
        Location origin = new Location("Rua A, 123");
        Location destination = new Location("Rua B, 456");
        Ride ride = new Ride(passenger.getId(), passenger.getEmail(), origin, destination);
        ride.setVehicleCategory("ECONOMICO");
        ride.setStatus(Ride.RideStatus.FINALIZADA);
        ride.setDriverId("driver123");

        // Adicionar ao histórico
        historyService.addRideToHistory(ride, 15.50, "Cartão de Crédito");

        // Verificar se foi adicionado
        List<RideHistory> history = historyService.getHistoryByPassenger(passenger.getEmail());
        assertEquals(1, history.size());
        assertEquals(ride.getId(), history.get(0).getRideId());
        assertEquals(passenger.getEmail(), history.get(0).getPassengerEmail());
        assertEquals(15.50, history.get(0).getPrice(), 0.01);
        assertEquals("Cartão de Crédito", history.get(0).getPaymentMethod());
    }

    @Test
    void testGetHistoryByPassenger() throws ValidationException, IOException {
        // Criar passageiro
        Passenger passenger = new Passenger("Maria Santos", "maria@test.com", "987654321", "senha456");
        userRepo.add(passenger);

        // Criar e adicionar múltiplas corridas ao histórico
        for (int i = 0; i < 3; i++) {
            Location origin = new Location("Origem " + i);
            Location destination = new Location("Destino " + i);
            Ride ride = new Ride(passenger.getId(), passenger.getEmail(), origin, destination);
            ride.setVehicleCategory("ECONOMICO");
            ride.setStatus(Ride.RideStatus.FINALIZADA);
            ride.setDriverId("driver" + i);
            
            historyService.addRideToHistory(ride, 10.0 + i, "Dinheiro");
        }

        List<RideHistory> history = historyService.getHistoryByPassenger(passenger.getEmail());
        assertEquals(3, history.size());
        
        // Verificar se está ordenado por data (mais recente primeiro)
        assertTrue(history.get(0).getRequestTime().isAfter(history.get(1).getRequestTime()) ||
                   history.get(0).getRequestTime().isEqual(history.get(1).getRequestTime()));
    }

    @Test
    void testGetHistoryByCategory() throws ValidationException, IOException {
        // Criar passageiro
        Passenger passenger = new Passenger("Carlos Lima", "carlos@test.com", "111222333", "senha789");
        userRepo.add(passenger);

        // Criar corridas com diferentes categorias
        String[] categories = {"ECONOMICO", "PREMIUM", "ECONOMICO"};
        for (int i = 0; i < 3; i++) {
            Location origin = new Location("Origem " + i);
            Location destination = new Location("Destino " + i);
            Ride ride = new Ride(passenger.getId(), passenger.getEmail(), origin, destination);
            ride.setVehicleCategory(categories[i]);
            ride.setStatus(Ride.RideStatus.FINALIZADA);
            ride.setDriverId("driver" + i);
            
            historyService.addRideToHistory(ride, 20.0, "PIX");
        }

        // Buscar apenas corridas ECONÔMICAS
        List<RideHistory> economicHistory = historyService.getHistoryByCategory("ECONOMICO");
        assertEquals(2, economicHistory.size());
        
        for (RideHistory h : economicHistory) {
            assertEquals("ECONOMICO", h.getVehicleCategory());
        }
    }

    @Test
    void testGetHistoryByPassengerAndCategory() throws ValidationException, IOException {
        // Criar passageiro
        Passenger passenger = new Passenger("Ana Costa", "ana@test.com", "444555666", "senha101");
        userRepo.add(passenger);

        // Criar corridas com diferentes categorias
        String[] categories = {"ECONOMICO", "PREMIUM", "ECONOMICO", "LUXO"};
        for (int i = 0; i < 4; i++) {
            Location origin = new Location("Origem " + i);
            Location destination = new Location("Destino " + i);
            Ride ride = new Ride(passenger.getId(), passenger.getEmail(), origin, destination);
            ride.setVehicleCategory(categories[i]);
            ride.setStatus(Ride.RideStatus.FINALIZADA);
            ride.setDriverId("driver" + i);
            
            historyService.addRideToHistory(ride, 25.0, "Cartão de Débito");
        }

        // Buscar corridas ECONÔMICAS do passageiro
        List<RideHistory> economicHistory = historyService.getHistoryByPassengerAndCategory(passenger.getEmail(), "ECONOMICO");
        assertEquals(2, economicHistory.size());
        
        for (RideHistory h : economicHistory) {
            assertEquals(passenger.getEmail(), h.getPassengerEmail());
            assertEquals("ECONOMICO", h.getVehicleCategory());
        }
    }

    @Test
    void testGetHistoryByDateRange() throws ValidationException, IOException {
        // Criar passageiro
        Passenger passenger = new Passenger("Pedro Oliveira", "pedro@test.com", "777888999", "senha202");
        userRepo.add(passenger);

        // Criar corridas com datas diferentes
        LocalDateTime baseDate = LocalDateTime.now().minusDays(5);
        for (int i = 0; i < 5; i++) {
            Location origin = new Location("Origem " + i);
            Location destination = new Location("Destino " + i);
            Ride ride = new Ride(passenger.getId(), passenger.getEmail(), origin, destination);
            ride.setVehicleCategory("ECONOMICO");
            ride.setStatus(Ride.RideStatus.FINALIZADA);
            ride.setDriverId("driver" + i);
            
            // Simular datas diferentes
            ride.setRequestTime(baseDate.plusDays(i));
            
            historyService.addRideToHistory(ride, 30.0, "Cartão de Crédito");
        }

        // Buscar corridas dos últimos 3 dias
        LocalDateTime startDate = LocalDateTime.now().minusDays(3);
        LocalDateTime endDate = LocalDateTime.now();
        
        List<RideHistory> recentHistory = historyService.getHistoryByDateRange(startDate, endDate);
        assertTrue(recentHistory.size() <= 3);
    }

    @Test
    void testGetAvailableCategories() throws ValidationException, IOException {
        // Criar passageiro
        Passenger passenger = new Passenger("Lucia Ferreira", "lucia@test.com", "000111222", "senha303");
        userRepo.add(passenger);

        // Criar corridas com diferentes categorias
        String[] categories = {"ECONOMICO", "PREMIUM", "ECONOMICO", "LUXO", "PREMIUM"};
        for (int i = 0; i < 5; i++) {
            Location origin = new Location("Origem " + i);
            Location destination = new Location("Destino " + i);
            Ride ride = new Ride(passenger.getId(), passenger.getEmail(), origin, destination);
            ride.setVehicleCategory(categories[i]);
            ride.setStatus(Ride.RideStatus.FINALIZADA);
            ride.setDriverId("driver" + i);
            
            historyService.addRideToHistory(ride, 35.0, "Dinheiro");
        }

        List<String> availableCategories = historyService.getAvailableCategories();
        assertEquals(3, availableCategories.size());
        assertTrue(availableCategories.contains("ECONOMICO"));
        assertTrue(availableCategories.contains("PREMIUM"));
        assertTrue(availableCategories.contains("LUXO"));
    }

    @Test
    void testGetCategoryStatistics() throws ValidationException, IOException {
        // Criar passageiro
        Passenger passenger = new Passenger("Roberto Silva", "roberto@test.com", "333444555", "senha404");
        userRepo.add(passenger);

        // Criar corridas com diferentes categorias
        String[] categories = {"ECONOMICO", "PREMIUM", "ECONOMICO", "LUXO", "ECONOMICO", "PREMIUM"};
        for (int i = 0; i < 6; i++) {
            Location origin = new Location("Origem " + i);
            Location destination = new Location("Destino " + i);
            Ride ride = new Ride(passenger.getId(), passenger.getEmail(), origin, destination);
            ride.setVehicleCategory(categories[i]);
            ride.setStatus(Ride.RideStatus.FINALIZADA);
            ride.setDriverId("driver" + i);
            
            historyService.addRideToHistory(ride, 40.0, "PIX");
        }

        Map<String, Long> stats = historyService.getCategoryStatistics();
        assertEquals(3, stats.size());
        assertEquals(3L, stats.get("ECONOMICO"));
        assertEquals(2L, stats.get("PREMIUM"));
        assertEquals(1L, stats.get("LUXO"));
    }

    @Test
    void testUpdateHistoryRating() throws ValidationException, IOException {
        // Criar passageiro
        Passenger passenger = new Passenger("Fernanda Lima", "fernanda@test.com", "666777888", "senha505");
        userRepo.add(passenger);

        // Criar corrida
        Location origin = new Location("Rua X, 100");
        Location destination = new Location("Rua Y, 200");
        Ride ride = new Ride(passenger.getId(), passenger.getEmail(), origin, destination);
        ride.setVehicleCategory("ECONOMICO");
        ride.setStatus(Ride.RideStatus.FINALIZADA);
        ride.setDriverId("driver123");
        
        historyService.addRideToHistory(ride, 50.0, "Cartão de Crédito");

        // Buscar o histórico criado
        List<RideHistory> history = historyService.getHistoryByPassenger(passenger.getEmail());
        String historyId = history.get(0).getId();

        // Atualizar avaliação do passageiro
        historyService.updateHistoryRating(historyId, 5, true);
        
        RideHistory updatedHistory = historyService.getHistoryById(historyId);
        assertEquals(5, updatedHistory.getPassengerRating());
    }

    @Test
    void testAddNotesToHistory() throws ValidationException, IOException {
        // Criar passageiro
        Passenger passenger = new Passenger("Gabriel Santos", "gabriel@test.com", "999000111", "senha606");
        userRepo.add(passenger);

        // Criar corrida
        Location origin = new Location("Rua Z, 300");
        Location destination = new Location("Rua W, 400");
        Ride ride = new Ride(passenger.getId(), passenger.getEmail(), origin, destination);
        ride.setVehicleCategory("PREMIUM");
        ride.setStatus(Ride.RideStatus.FINALIZADA);
        ride.setDriverId("driver456");
        
        historyService.addRideToHistory(ride, 75.0, "PIX");

        // Buscar o histórico criado
        List<RideHistory> history = historyService.getHistoryByPassenger(passenger.getEmail());
        String historyId = history.get(0).getId();

        // Adicionar observações
        String notes = "Corrida excelente, motorista muito atencioso!";
        historyService.addNotesToHistory(historyId, notes);
        
        RideHistory updatedHistory = historyService.getHistoryById(historyId);
        assertEquals(notes, updatedHistory.getNotes());
    }

    @Test
    void testValidationExceptions() {
        // Testar exceções de validação
        assertThrows(ValidationException.class, () -> {
            historyService.getHistoryByPassenger("");
        });

        assertThrows(ValidationException.class, () -> {
            historyService.getHistoryByPassenger(null);
        });

        assertThrows(ValidationException.class, () -> {
            historyService.getHistoryByCategory("");
        });

        assertThrows(ValidationException.class, () -> {
            historyService.getHistoryByDateRange(LocalDateTime.now(), LocalDateTime.now().minusDays(1));
        });
    }

    @Test
    void testFormatHistoryList() throws ValidationException, IOException {
        // Criar passageiro
        Passenger passenger = new Passenger("Isabela Costa", "isabela@test.com", "222333444", "senha707");
        userRepo.add(passenger);

        // Criar corrida
        Location origin = new Location("Rua A, 1");
        Location destination = new Location("Rua B, 2");
        Ride ride = new Ride(passenger.getId(), passenger.getEmail(), origin, destination);
        ride.setVehicleCategory("ECONOMICO");
        ride.setStatus(Ride.RideStatus.FINALIZADA);
        ride.setDriverId("driver789");
        
        historyService.addRideToHistory(ride, 25.0, "Dinheiro");

        List<RideHistory> history = historyService.getHistoryByPassenger(passenger.getEmail());
        String formatted = historyService.formatHistoryList(history);
        
        assertTrue(formatted.contains("HISTÓRICO DE CORRIDAS"));
        assertTrue(formatted.contains("Total de registros: 1"));
        assertTrue(formatted.contains(passenger.getEmail()));
    }

    @Test
    void testFormatCategoryStatistics() throws ValidationException, IOException {
        // Criar passageiro
        Passenger passenger = new Passenger("Marcos Oliveira", "marcos@test.com", "555666777", "senha808");
        userRepo.add(passenger);

        // Criar corridas com diferentes categorias
        String[] categories = {"ECONOMICO", "ECONOMICO", "PREMIUM"};
        for (int i = 0; i < 3; i++) {
            Location origin = new Location("Origem " + i);
            Location destination = new Location("Destino " + i);
            Ride ride = new Ride(passenger.getId(), passenger.getEmail(), origin, destination);
            ride.setVehicleCategory(categories[i]);
            ride.setStatus(Ride.RideStatus.FINALIZADA);
            ride.setDriverId("driver" + i);
            
            historyService.addRideToHistory(ride, 30.0, "Cartão de Crédito");
        }

        String stats = historyService.formatCategoryStatistics();
        assertTrue(stats.contains("ESTATÍSTICAS POR CATEGORIA"));
        assertTrue(stats.contains("ECONOMICO"));
        assertTrue(stats.contains("PREMIUM"));
        assertTrue(stats.contains("Total: 3 corridas"));
    }
}
