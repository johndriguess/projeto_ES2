package test;

import model.Vehicle;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import repo.VehicleRepository;
import java.io.File;
import java.io.IOException;
import static org.junit.jupiter.api.Assertions.*;

public class VehicleRepositoryTest {
    private static final String TEST_DB = "test_vehicles.db";
    private VehicleRepository repo;

    @BeforeEach
    public void setup() {
        File file = new File(TEST_DB);
        if (file.exists()) {
            file.delete();
        }
        repo = new VehicleRepository(TEST_DB);
    }

    @Test
    public void shouldAddVehicleAndFindItByPlate() throws IOException {
        // Adicionar um veículo e encontrá-lo pela placa
        Vehicle v = new Vehicle("AAA-1111", "Gol", 2018, "Branco");
        repo.add(v);
        Vehicle foundVehicle = repo.findByPlate("AAA-1111");
        
        assertNotNull(foundVehicle);
        assertEquals(v.getPlate(), foundVehicle.getPlate());
    }

    @Test
    public void shouldCheckIfVehicleExists() throws IOException {
        // Verificar se um veículo existe após ser adicionado
        Vehicle v = new Vehicle("BBB-2222", "Uno", 2015, "Cinza");
        repo.add(v);
        
        assertTrue(repo.existsByPlate("BBB-2222"));
        assertFalse(repo.existsByPlate("CCC-3333"));
    }

    @Test
    public void shouldPersistVehicleAfterClosingAndReopening() throws IOException {
        // Salvar um veículo e verificar se ele ainda existe após fechar o repositório
        Vehicle v = new Vehicle("CCC-3333", "Fusion", 2020, "Preto");
        repo.add(v);
        
        VehicleRepository newRepo = new VehicleRepository(TEST_DB);
        Vehicle foundVehicle = newRepo.findByPlate("CCC-3333");
        
        assertNotNull(foundVehicle);
        assertEquals(v.getPlate(), foundVehicle.getPlate());
    }
}