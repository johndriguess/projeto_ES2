package test;

import model.Driver;
import model.Passenger;
import model.User;
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
        userRepo = new UserRepository(new File(TEST_USER_DB));
        vehicleRepo = new VehicleRepository(TEST_VEHICLE_DB);
        
        // Injeta as dependências no AuthService
        auth = new AuthService(userRepo, vehicleRepo);
    }

    @Test
    public void shouldRegisterPassenger() throws Exception {
        Passenger p = auth.registerPassenger("Ascendino", "ascendino@example.com", "82999999999", "senha123");
        assertNotNull(p.getId());
        assertEquals("ascendino@example.com", p.getEmail());
        assertTrue(userRepo.existsByEmail("ascendino@example.com"));
    }

    @Test
    public void shouldNotRegisterDuplicateEmail() throws Exception {
        auth.registerPassenger("Sabrina", "sabrina@example.com", "82999998888", "123456");
        ValidationException ex = assertThrows(ValidationException.class, () -> {
            auth.registerDriver("Johnatan", "sabrina@example.com", "82000000000", "senha123", "12345", "ABC-1234", "Gol", 2018, "Prata");
        });
        assertTrue(ex.getMessage().contains("já existe"));
    }

    @Test
    public void shouldValidateEmailFormat() {
        assertThrows(ValidationException.class, () -> {
            auth.registerPassenger("Laryssa", "invalid-email", "111111111", "senha123");
        });
    }

    @Test
    public void shouldRegisterDriverWithInitialVehicle() throws Exception {
        Driver d = auth.registerDriver("Johnatan", "johnatan@ex.com", "82911112222", "senhadriver", "CNH123", "XYZ-9999", "Uno", 2018, "Branco");
        assertNotNull(d.getId());
        assertEquals("johnatan@ex.com", d.getEmail());
        assertNotNull(d.getVehicle());
        assertEquals("XYZ-9999", d.getVehicle().getPlate());
        assertEquals("Uber Comfort", d.getVehicle().getCategory());
        assertTrue(userRepo.existsByEmail("johnatan@ex.com"));
        assertTrue(vehicleRepo.existsByPlate("XYZ-9999"));
    }

    @Test
    public void shouldAddVehicleToExistingDriver() throws Exception {
        auth.registerDriver("Existing Driver", "existing@ex.com", "111", "senha111", "123", "ABC-123", "Gol", 2018, "Prata");

        Driver d = auth.addVehicleToDriver("existing@ex.com", "DEF-456", "Uno", 2015, "Azul");
        
        assertNotNull(d);
        assertNotNull(d.getVehicle());
        assertEquals("DEF-456", d.getVehicle().getPlate());
        assertEquals("Uber Comfort", d.getVehicle().getCategory());
        assertTrue(userRepo.existsByEmail("existing@ex.com"));
        assertTrue(vehicleRepo.existsByPlate("DEF-456"));
    }
    
    @Test
    public void shouldNotAddDuplicateVehiclePlate() throws Exception {
        auth.registerDriver("First Driver", "first@ex.com", "111", "senha123", "123", "ABC-123", "Gol", 2018, "Prata");
        
        ValidationException ex = assertThrows(ValidationException.class, () -> {
            auth.addVehicleToDriver("first@ex.com", "ABC-123", "Palio", 2015, "Azul");
        });
        
        assertTrue(ex.getMessage().contains("já está cadastrado"));
    }

    @Test
    public void shouldNotRegisterDriverWithInvalidVehicle() {
        ValidationException ex = assertThrows(ValidationException.class, () -> {
            auth.registerDriver("Invalid", "invalid@ex.com", "12345", "senha_curta", "CNH123", "OLD-CAR", "Fusca", 1970, "Azul");
        });
        assertTrue(ex.getMessage().contains("não atende aos requisitos"));
    }

    @Test
    public void shouldAssignUberBlackCategory() throws Exception {
        Driver d = auth.registerDriver("Luxury", "luxury@ex.com", "111", "senhaluxo", "CNH456", "LUX-0001", "Audi A6", 2022, "preto");
        assertNotNull(d);
        assertEquals("Uber Black", d.getVehicle().getCategory());
        assertTrue(userRepo.existsByEmail("luxury@ex.com"));
        assertTrue(vehicleRepo.existsByPlate("LUX-0001"));
    }
    
    @Test
    public void shouldLoginSuccessfully() throws Exception {
        auth.registerPassenger("Login User", "login@ex.com", "123456789", "minhasenha");
        User loggedInUser = auth.login("login@ex.com", "minhasenha");
        assertNotNull(loggedInUser);
        assertEquals("Login User", loggedInUser.getName());
        assertEquals("login@ex.com", loggedInUser.getEmail());
    }

    @Test
    public void shouldFailLoginWithIncorrectPassword() throws Exception {
        auth.registerPassenger("Wrong Pass", "wrongpass@ex.com", "111", "correct_password");
        ValidationException ex = assertThrows(ValidationException.class, () -> {
            auth.login("wrongpass@ex.com", "incorrect_password");
        });
        assertTrue(ex.getMessage().contains("Senha incorreta"));
    }

    @Test
    public void shouldFailLoginWithNonExistentEmail() {
        ValidationException ex = assertThrows(ValidationException.class, () -> {
            auth.login("nonexistent@ex.com", "qualquersenha");
        });
        assertTrue(ex.getMessage().contains("Usuário não encontrado"));
    }
}