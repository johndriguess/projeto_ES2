package repo;

import model.Restaurant;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class RestaurantRepository {

    private final List<Restaurant> restaurants = new ArrayList<>();

    public void save(Restaurant restaurant) {
        restaurants.add(restaurant);
    }

    public Optional<Restaurant> findById(String id) {
        return restaurants.stream()
                .filter(r -> r.getId().equals(id))
                .findFirst();
    }

    public Optional<Restaurant> findByEmail(String email) {
        return restaurants.stream()
                .filter(r -> r.getEmail().equalsIgnoreCase(email))
                .findFirst();
    }

    public Optional<Restaurant> findByCnpj(String cnpj) {
        return restaurants.stream()
                .filter(r -> r.getCnpj().equals(cnpj))
                .findFirst();
    }

    public List<Restaurant> findAll() {
        return restaurants;
    }
}