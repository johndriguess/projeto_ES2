package test;

import model.Driver;
import model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import repo.UserRepository;
import java.io.File;
import java.io.IOException;
import static org.junit.jupiter.api.Assertions.*;

public class UserRepositoryTest {
    private static final String TEST_DB = "test_users.db";
    private UserRepository repo;

    @BeforeEach
    public void setup() {
        File file = new File(TEST_DB);
        if (file.exists()) {
            file.delete();
        }
        repo = new UserRepository(TEST_DB);
    }

    @Test
    public void shouldAddUserAndFindItByEmail() throws IOException {
        // Adicionar um motorista e encontrá-lo pelo e-mail
        Driver d = new Driver("Ana", "ana@test.com", "111", "senha123", "CNH456");
        repo.add(d);
        User foundUser = repo.findByEmail("ana@test.com");
        
        assertNotNull(foundUser);
        assertEquals(d.getEmail(), foundUser.getEmail());
    }

    @Test
    public void shouldCheckIfUserExists() throws IOException {
        //  Verificar se um usuário existe após ser adicionado
        Driver d = new Driver("Joao", "joao@test.com", "222", "senha123", "CNH123");
        repo.add(d);
        
        assertTrue(repo.existsByEmail("joao@test.com"));
        assertFalse(repo.existsByEmail("naoexiste@test.com"));
    }

    @Test
    public void shouldPersistUserAfterClosingAndReopening() throws IOException {
        //Salvar um usuário e verificar se ele ainda existe após fechar o repositório
        Driver d = new Driver("Persistido", "persistido@test.com", "333", "senha123", "CNH789");
        repo.add(d);
        
        UserRepository newRepo = new UserRepository(TEST_DB);
        User foundUser = newRepo.findByEmail("persistido@test.com");
        
        assertNotNull(foundUser);
        assertEquals(d.getEmail(), foundUser.getEmail());
    }
}