package test;

import model.Vehicle;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import service.DocumentValidator;
import util.ValidationException;

import static org.junit.jupiter.api.Assertions.*;

public class DocumentValidatorTest {

    private DocumentValidator validator;

    @BeforeEach
    public void setup() {
        validator = new DocumentValidator();
    }

    @Test
    public void uberBlackCategory() throws ValidationException {
        //  Veículo de luxo e preto deve ser UberBlack
        Vehicle vehicle = new Vehicle("LUX-0001", "Audi A6", 2022, "preto");
        validator.validateVehicleCategory(vehicle);
        assertEquals("UberBlack", vehicle.getCategory());
    }

    @Test
    public void uberComfortCategory() throws ValidationException {
        //  Veículo com ano a partir de 2015 deve ser UberComfort
        Vehicle vehicle = new Vehicle("CONF-9999", "Honda Civic", 2018, "Prata");
        validator.validateVehicleCategory(vehicle);
        assertEquals("UberComfort", vehicle.getCategory());
    }

    @Test
    public void uberXCategory() throws ValidationException {
        //Veículo a partir de 2010 (que não se encaixa nas categorias superiores) deve ser UberX
        Vehicle vehicle = new Vehicle("UBER-1234", "Fiat Uno", 2014, "Branco");
        validator.validateVehicleCategory(vehicle);
        assertEquals("UberX", vehicle.getCategory());
    }

    @Test
    public void shouldThrowExceptionForInvalidVehicle() {
        // Veículo com ano anterior a 2010 deve falhar
        Vehicle vehicle = new Vehicle("OLD-CAR", "Fusca", 2009, "Azul");
        ValidationException ex = assertThrows(ValidationException.class, () -> {
            validator.validateVehicleCategory(vehicle);
        });
        assertTrue(ex.getMessage().contains("não atende aos requisitos"));
    }
}