package service;

import model.Restaurant;
import repo.RestaurantRepository;
import util.ValidationException;

public class RestaurantService {

    private final RestaurantRepository repository;

    public RestaurantService(RestaurantRepository repository) {
        this.repository = repository;
    }

    public Restaurant register(String name, String email, String cnpj, String address) {

        validate(name, email, cnpj, address);

        if (repository.findByEmail(email).isPresent()) {
            throw new ValidationException("Email já cadastrado.");
        }

        if (repository.findByCnpj(cnpj).isPresent()) {
            throw new ValidationException("CNPJ já cadastrado.");
        }

        Restaurant restaurant = new Restaurant(name, email, cnpj, address);

        repository.save(restaurant);

        return restaurant;
    }

    private void validate(String name, String email, String cnpj, String address) {

        if (name == null || name.isBlank()) {
            throw new ValidationException("Nome é obrigatório.");
        }

        if (email == null || !email.contains("@")) {
            throw new ValidationException("Email inválido.");
        }

        if (cnpj == null || cnpj.length() != 14) {
            throw new ValidationException("CNPJ inválido.");
        }

        if (address == null || address.isBlank()) {
            throw new ValidationException("Endereço é obrigatório.");
        }
    }
}
