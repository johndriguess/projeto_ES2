package test;
import org.junit.jupiter.api.*;
import repo.UserRepository;
import service.AuthService;
import model.Passenger;
import model.Driver;
import util.ValidationException;

import java.io.File;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

public class AuthServiceTest {
	private static final String TEST_DB = "test_users.db";
    private UserRepository repo;
    private AuthService auth;

    @BeforeEach
    public void setup() {
        // limpa DB de teste
        File f = new File(TEST_DB);
        if (f.exists()) f.delete();
        repo = new UserRepository(TEST_DB);
        auth = new AuthService(repo);
    }

    @Test
    public void shouldRegisterPassenger() throws Exception {
        Passenger p = auth.registerPassenger("Ascendino", "ascendino@example.com", "82999999999");
        assertNotNull(p.getId());
        assertEquals("ascendino@example.com", p.getEmail());
        assertTrue(repo.existsByEmail("ascendino@example.com"));
    }

    @Test
    public void shouldNotRegisterDuplicateEmail() throws Exception {
        auth.registerPassenger("Sabrina", "sabrina@example.com", "82999998888");
        ValidationException ex = assertThrows(ValidationException.class, () -> {
            auth.registerDriver("Johnatan", "sabrina@example.com", "82000000000", "12345", "ABC-1234", "Gol");
        });
        assertTrue(ex.getMessage().contains("já existe"));
    }

    @Test
    public void shouldValidateEmailFormat() {
        assertThrows(ValidationException.class, () -> {
            auth.registerPassenger("Larrysa", "invalid-email", "111111111");
        });
    }

    @Test
    public void shouldRegisterDriver() throws Exception {
        Driver d = auth.registerDriver("Johnatan", "johnatan@ex.com", "82911112222", "CNH123", "XYZ-9999", "Uno");
        assertNotNull(d.getId());
        assertEquals("johnatan@ex.com", d.getEmail());
    }
}
