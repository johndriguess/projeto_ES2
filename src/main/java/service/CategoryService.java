package service;

import model.VehicleCategory;
import repo.CategoryRepository;

import java.nio.file.Path;
import java.util.List;

/**
 * Serviço que disponibiliza as categorias de veículos (RF06).
 */
public class CategoryService {
    private final CategoryRepository repository;

    public CategoryService(Path storageFile) {
        this.repository = new CategoryRepository(storageFile);
    }

    public CategoryService(String storageFile) {
        this(Path.of(storageFile));
    }

    public List<VehicleCategory> getAvailableCategories() {
        return repository.loadAll();
    }
}
