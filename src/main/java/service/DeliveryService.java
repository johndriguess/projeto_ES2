package service;

import model.Delivery;
import repo.DeliveryRepository;
import util.ValidationException;

public class DeliveryService {

    private final DeliveryRepository repository;

    public DeliveryService(DeliveryRepository repository) {
        this.repository = repository;
    }

    public Delivery register(String name, String email, String document, String phone) {

        validate(name, email, document, phone);

        if (repository.findByEmail(email).isPresent()) {
            throw new ValidationException("Email já cadastrado.");
        }

        if (repository.findByDocument(document).isPresent()) {
            throw new ValidationException("Documento já cadastrado.");
        }

        Delivery delivery = new Delivery(name, email, document, phone);

        repository.save(delivery);

        return delivery;
    }

    private void validate(String name, String email, String document, String phone) {

        if (name == null || name.isBlank()) {
            throw new ValidationException("Nome é obrigatório.");
        }

        if (email == null || !email.contains("@")) {
            throw new ValidationException("Email inválido.");
        }

        if (document == null || document.length() < 11) {
            throw new ValidationException("Documento inválido.");
        }

        if (phone == null || phone.isBlank()) {
            throw new ValidationException("Telefone é obrigatório.");
        }
    }
}
