package repo;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import model.VehicleCategory;

import java.io.*;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

/**
 * Repositório para persistir e carregar categorias de veículos.
 * Cria um arquivo JSON local em data/categories.json.
 */
public class CategoryRepository {
    private final Path filePath;
    private final Gson gson = new Gson();

    public CategoryRepository(Path filePath) {
        this.filePath = filePath;
        ensureFileExists();
    }

    public CategoryRepository(String filePathStr) {
        this(Path.of(filePathStr));
    }

    private void ensureFileExists() {
        try {
            if (Files.notExists(filePath.getParent())) {
                Files.createDirectories(filePath.getParent());
            }
            if (Files.notExists(filePath)) {
                List<VehicleCategory> defaults = Arrays.asList(VehicleCategory.values());
                try (Writer w = new FileWriter(filePath.toFile())) {
                    gson.toJson(defaults, w);
                }
            }
        } catch (IOException e) {
            throw new RuntimeException("Erro ao criar arquivo de categorias: " + filePath, e);
        }
    }

    public List<VehicleCategory> loadAll() {
        try (Reader reader = new FileReader(filePath.toFile())) {
            Type listType = new TypeToken<List<VehicleCategory>>(){}.getType();
            List<VehicleCategory> list = gson.fromJson(reader, listType);
            if (list == null || list.isEmpty()) {
                return Arrays.asList(VehicleCategory.values());
            }
            return list;
        } catch (IOException e) {
            throw new RuntimeException("Erro ao ler categorias de: " + filePath, e);
        }
    }

    public void saveAll(List<VehicleCategory> categories) {
        try (Writer w = new FileWriter(filePath.toFile())) {
            gson.toJson(categories, w);
        } catch (IOException e) {
            throw new RuntimeException("Erro ao salvar categorias em: " + filePath, e);
        }
    }
}
