package service;

import model.Delivery;
import repo.DeliveryRepository;
import util.ValidationException;

public class DeliveryService {

    private final DeliveryRepository repository;
    private final DocumentValidator documentValidator;

    public DeliveryService(DeliveryRepository repository) {
        this.repository = repository;
        this.documentValidator = new DocumentValidator();
    }

    public Delivery register(String name,
                             String email,
                             String document,
                             String phone,
                             String cnh,
                             String vehicleDocument) {

        validate(name, email, document, phone, cnh, vehicleDocument);

        if (repository.findByEmail(email).isPresent()) {
            throw new ValidationException("Email já cadastrado.");
        }

        if (repository.findByDocument(document).isPresent()) {
            throw new ValidationException("Documento já cadastrado.");
        }

        Delivery delivery = new Delivery(
                name,
                email,
                document,
                phone,
                cnh,
                vehicleDocument
        );

        // 🔹 valida documentos RF02
        documentValidator.validateDeliveryDocuments(delivery);

        repository.save(delivery);
        return delivery;
    }

    private void validate(String name,
                          String email,
                          String document,
                          String phone,
                          String cnh,
                          String vehicleDocument) {

        if (name == null || name.isBlank()) {
            throw new ValidationException("Nome é obrigatório.");
        }

        if (email == null || !email.contains("@")) {
            throw new ValidationException("Email inválido.");
        }

        if (document == null || document.length() < 11) {
            throw new ValidationException("CPF inválido.");
        }

        if (phone == null || phone.isBlank()) {
            throw new ValidationException("Telefone é obrigatório.");
        }

        if (cnh == null || cnh.length() < 11) {
            throw new ValidationException("CNH inválida.");
        }

        if (vehicleDocument == null || vehicleDocument.isBlank()) {
            throw new ValidationException("Documento do veículo é obrigatório.");
        }
    }
}