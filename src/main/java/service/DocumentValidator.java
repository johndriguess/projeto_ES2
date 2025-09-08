package service;

import model.Vehicle;
import util.ValidationException;

public class DocumentValidator {

    public void validateVehicleCategory(Vehicle vehicle) throws ValidationException {
        if (isUberBlack(vehicle)) {
            vehicle.setCategory("UberBlack");
        } else if (isUberComfort(vehicle)) {
            vehicle.setCategory("UberComfort");
        } else if (isUberX(vehicle)) {
            vehicle.setCategory("UberX");
        } else {
            throw new ValidationException("Veículo não atende aos requisitos para nenhuma categoria.");
        }
    }

    private boolean isUberX(Vehicle vehicle) {
        // ano a partir de 2010.
        return vehicle.getYear() >= 2010;
    }

    private boolean isUberComfort(Vehicle vehicle) {
        // Veículos com ano a partir de 2015 
        return vehicle.getYear() >= 2015;
    }

    private boolean isUberBlack(Vehicle vehicle) {
        // Veículos sedãs de luxo, com ano a partir de 2020 e cor preta.
        return vehicle.getYear() >= 2020 && vehicle.getColor().equalsIgnoreCase("preto");
    }
}