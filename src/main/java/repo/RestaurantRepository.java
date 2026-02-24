package repo;

import model.Restaurant;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class RestaurantRepository {

    private final File storageFile;
    private Map<String, Restaurant> restaurantsByEmail;

    public RestaurantRepository() {
        File dataDir = new File("data");
        if (!dataDir.exists())
            dataDir.mkdirs();
        this.storageFile = new File(dataDir, "restaurants.db");
        load();
    }

    public RestaurantRepository(String filePath) {
        this.storageFile = new File(filePath);
        load();
    }

    @SuppressWarnings("unchecked")
    private void load() {
        if (!storageFile.exists()) {
            restaurantsByEmail = new HashMap<>();
            return;
        }
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(storageFile))) {
            Object o = ois.readObject();
            restaurantsByEmail = (Map<String, Restaurant>) o;
        } catch (Exception e) {
            System.err.println("Não foi possível carregar restaurantes. Inicializando vazio. (" + e.getMessage() + ")");
            restaurantsByEmail = new HashMap<>();
        }
    }

    private void persist() {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(storageFile))) {
            oos.writeObject(restaurantsByEmail);
        } catch (IOException e) {
            System.err.println("Erro ao salvar restaurantes: " + e.getMessage());
        }
    }

    public void save(Restaurant restaurant) {
        restaurantsByEmail.put(restaurant.getEmail().toLowerCase(), restaurant);
        persist();
    }

    public Optional<Restaurant> findById(String id) {
        return restaurantsByEmail.values().stream()
                .filter(r -> r.getId().equals(id))
                .findFirst();
    }

    public Optional<Restaurant> findByEmail(String email) {
        return Optional.ofNullable(restaurantsByEmail.get(email.toLowerCase()));
    }

    public Optional<Restaurant> findByCnpj(String cnpj) {
        return restaurantsByEmail.values().stream()
                .filter(r -> r.getCnpj().equals(cnpj))
                .findFirst();
    }

    public List<Restaurant> findAll() {
        return new ArrayList<>(restaurantsByEmail.values());
    }

    public void update(Restaurant restaurant) {
        restaurantsByEmail.put(restaurant.getEmail().toLowerCase(), restaurant);
        persist();
    }
}