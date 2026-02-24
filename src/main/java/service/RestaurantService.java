package service;

import model.Location;
import model.Restaurant;
import repo.RestaurantRepository;
import util.ValidationException;

import java.util.List;

public class RestaurantService {

    private final RestaurantRepository repository;

    public RestaurantService(RestaurantRepository repository) {
        this.repository = repository;
    }

    // =========================
    // Cadastro de restaurante
    // =========================
    public Restaurant register(String name, String email, String cnpj, Location location) {

        validate(name, email, cnpj, location);

        if (repository.findByEmail(email).isPresent()) {
            throw new ValidationException("Email já cadastrado.");
        }

        if (repository.findByCnpj(cnpj).isPresent()) {
            throw new ValidationException("CNPJ já cadastrado.");
        }

        Restaurant restaurant = new Restaurant(name, email, cnpj, location);

        repository.save(restaurant);

        return restaurant;
    }

    private void validate(String name, String email, String cnpj, Location location) {

        if (name == null || name.isBlank()) {
            throw new ValidationException("Nome é obrigatório.");
        }

        if (email == null || !email.contains("@")) {
            throw new ValidationException("Email inválido.");
        }

        if (cnpj == null || cnpj.length() != 14) {
            throw new ValidationException("CNPJ inválido.");
        }

        if (location == null) {
            throw new ValidationException("Localização é obrigatória.");
        }
    }

    // =========================
    // RF20
    // =========================

    public double calculateDeliveryFee(double distance) {
        if (distance <= 5) return 5.0;
        if (distance <= 10) return 8.0;
        return 12.0;
    }

    public int calculateEstimatedTime(double distance) {
        return (int) (20 + distance * 2);
    }

    public RestaurantDetails getRestaurantDetails(String restaurantId, double distance) {

        Restaurant restaurant = repository.findById(restaurantId)
                .orElseThrow(() -> new ValidationException("Restaurante não encontrado."));

        double fee = calculateDeliveryFee(distance);
        int time = calculateEstimatedTime(distance);

        return new RestaurantDetails(restaurant, restaurant.getMenu(), fee, time);
    }

    // =========================
    // RF19 - Buscar restaurantes disponíveis por localização
    // =========================

    public List<Restaurant> findAvailableRestaurants(Location clientLocation,
                                                     double radius) {

        if (clientLocation == null) {
            throw new ValidationException("Localização do cliente é obrigatória.");
        }

        return repository.findAll()
                .stream()
                .filter(Restaurant::isActive) // só ativos
                .filter(Restaurant::isOpen)   // só abertos
                .filter(r -> r.getLocation()
                        .distanceTo(clientLocation) <= radius) // dentro do raio
                .collect(java.util.stream.Collectors.toList());
    }
}