package test;

import model.VehicleCategory;
import service.CategoryService;
import org.junit.jupiter.api.*;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class CategoryServiceTest {
    private static Path tempFile;

    @BeforeAll
    static void setup() throws Exception {
        Path tempDir = Files.createTempDirectory("uberpb-test");
        tempFile = tempDir.resolve("categories.json");
    }

    @Test
    void testGetAvailableCategoriesReturnsDefaultsWhenFileMissing() {
        CategoryService service = new CategoryService(tempFile);
        List<VehicleCategory> cats = service.getAvailableCategories();
        assertNotNull(cats);
        assertTrue(cats.size() >= 5);
        assertTrue(cats.stream().anyMatch(c -> c.name().equals("UBER_X")));
    }
}