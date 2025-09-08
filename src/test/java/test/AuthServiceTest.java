package test;

import model.Driver;
import model.Passenger;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import repo.UserRepository;
import repo.VehicleRepository;
import service.AuthService;
import util.ValidationException;

import java.io.File;
import java.io.IOException;
import static org.junit.jupiter.api.Assertions.*;

public class AuthServiceTest {
    private static final String TEST_USER_DB = "test_users.db";
    private static final String TEST_VEHICLE_DB = "test_vehicles.db";
    private UserRepository userRepo;
    private VehicleRepository vehicleRepo;
    private AuthService auth;

    @BeforeEach
    public void setup() throws IOException {
        // Limpa os arquivos de teste antes de cada execução
        File userFile = new File(TEST_USER_DB);
        File vehicleFile = new File(TEST_VEHICLE_DB);
        if (userFile.exists()) userFile.delete();
        if (vehicleFile.exists()) vehicleFile.delete();

        // Cria as dependências
        userRepo = new UserRepository(TEST_USER_DB);
        vehicleRepo = new VehicleRepository(TEST_VEHICLE_DB);
        
        // Injeta as dependências no AuthService
        auth = new AuthService(userRepo, vehicleRepo);
    }

    @Test
    public void shouldRegisterPassenger() throws Exception {
        Passenger p = auth.registerPassenger("Ascendino", "ascendino@example.com", "82999999999");
        assertNotNull(p.getId());
        assertEquals("ascendino@example.com", p.getEmail());
        assertTrue(userRepo.existsByEmail("ascendino@example.com"));
    }

    @Test
    public void shouldNotRegisterDuplicateEmail() throws Exception {
        auth.registerPassenger("Sabrina", "sabrina@example.com", "82999998888");
        ValidationException ex = assertThrows(ValidationException.class, () -> {
            auth.registerDriver("Johnatan", "sabrina@example.com", "82000000000", "12345", "ABC-1234", "Gol", 2018, "Prata");
        });
        assertTrue(ex.getMessage().contains("já existe"));
    }

    @Test
    public void shouldValidateEmailFormat() {
        assertThrows(ValidationException.class, () -> {
            auth.registerPassenger("Laryssa", "invalid-email", "111111111");
        });
    }

    @Test
    public void shouldRegisterDriverWithInitialVehicle() throws Exception {
        Driver d = auth.registerDriver("Johnatan", "johnatan@ex.com", "82911112222", "CNH123", "XYZ-9999", "Uno", 2018, "Branco");
        assertNotNull(d.getId());
        assertEquals("johnatan@ex.com", d.getEmail());
        assertFalse(d.getVehicles().isEmpty()); // Garante que a lista não está vazia
        assertEquals(1, d.getVehicles().size()); // Garante que há apenas um veículo
        assertEquals("XYZ-9999", d.getVehicles().get(0).getPlate());
        assertEquals("UberComfort", d.getVehicles().get(0).getCategory());
        assertTrue(userRepo.existsByEmail("johnatan@ex.com"));
        assertTrue(vehicleRepo.existsByPlate("XYZ-9999"));
    }

    @Test
    public void shouldAddVehicleToExistingDriver() throws Exception {
        // 1. Cadastra um motorista com um veículo
        auth.registerDriver("Existing Driver", "existing@ex.com", "111", "123", "ABC-123", "Gol", 2018, "Prata");

        // 2. Adiciona um novo veículo para o mesmo motorista
        Driver d = auth.addVehicleToDriver("existing@ex.com", "DEF-456", "Uno", 2015, "Azul");
        
        // 3. Verifica se o motorista agora tem dois veículos
        assertNotNull(d);
        assertEquals(2, d.getVehicles().size());
        assertEquals("DEF-456", d.getVehicles().get(1).getPlate());
        assertEquals("UberComfort", d.getVehicles().get(1).getCategory());
        assertTrue(userRepo.existsByEmail("existing@ex.com"));
        assertTrue(vehicleRepo.existsByPlate("DEF-456"));
    }
    
    @Test
    public void shouldNotAddDuplicateVehiclePlate() throws Exception {
        auth.registerDriver("First Driver", "first@ex.com", "111", "123", "ABC-123", "Gol", 2018, "Prata");
        
        ValidationException ex = assertThrows(ValidationException.class, () -> {
            auth.addVehicleToDriver("first@ex.com", "ABC-123", "Palio", 2015, "Azul");
        });
        
        assertTrue(ex.getMessage().contains("já está cadastrado"));
    }

    @Test
    public void shouldNotRegisterDriverWithInvalidVehicle() {
        ValidationException ex = assertThrows(ValidationException.class, () -> {
            auth.registerDriver("Invalid", "invalid@ex.com", "12345", "CNH123", "OLD-CAR", "Fusca", 1970, "Azul");
        });
        assertTrue(ex.getMessage().contains("não atende aos requisitos"));
    }

    @Test
    public void shouldAssignUberBlackCategory() throws Exception {
        Driver d = auth.registerDriver("Luxury", "luxury@ex.com", "111", "CNH456", "LUX-0001", "Audi A6", 2022, "preto");
        assertNotNull(d);
        assertEquals("UberBlack", d.getVehicles().get(0).getCategory());
        assertTrue(userRepo.existsByEmail("luxury@ex.com"));
        assertTrue(vehicleRepo.existsByPlate("LUX-0001"));
    }
}