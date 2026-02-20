package service;

import model.Delivery;
import model.DeliveryStatus;
import model.Vehicle;
import util.ValidationException;

public class DocumentValidator {

    // 🔹 já existente (veículos motorista)
    public void validateVehicleCategory(Vehicle vehicle) throws ValidationException {
        if (isUberBlack(vehicle)) {
            vehicle.setCategory("Uber Black");
        } else if (isUberComfort(vehicle)) {
            vehicle.setCategory("Uber Comfort");
        } else if (isUberX(vehicle)) {
            vehicle.setCategory("UberX");
        } else {
            throw new ValidationException("Veículo não atende aos requisitos para nenhuma categoria.");
        }
    }

    private boolean isUberX(Vehicle vehicle) {
        return vehicle.getYear() >= 2010;
    }

    private boolean isUberComfort(Vehicle vehicle) {
        return vehicle.getYear() >= 2015;
    }

    private boolean isUberBlack(Vehicle vehicle) {
        return vehicle.getYear() >= 2020 && vehicle.getColor().equalsIgnoreCase("preto");
    }

    // 🔹 NOVAS validações RF02 (entregador)

    public boolean isValidCPF(String cpf) {
        return cpf != null && cpf.matches("\\d{11}");
    }

    public boolean isValidCNH(String cnh) {
        return cnh != null && cnh.matches("\\d{11}");
    }

    public boolean isValidVehicleDocument(String doc) {
        return doc != null && !doc.isBlank();
    }

    public void validateDeliveryDocuments(Delivery delivery) {
        if (isValidCPF(delivery.getDocument())
                && isValidCNH(delivery.getCnh())
                && isValidVehicleDocument(delivery.getVehicleDocument())) {

            delivery.setValidationStatus(DeliveryStatus.APROVADO);

        } else {
            delivery.setValidationStatus(DeliveryStatus.REJEITADO);
        }
    }
}