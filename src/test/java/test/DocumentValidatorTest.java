package test;

import model.Delivery;
import model.DeliveryStatus;
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

    // ======================
    // TESTES EXISTENTES VEÍCULO
    // ======================

    @Test
    public void uberBlackCategory() throws ValidationException {
        Vehicle vehicle = new Vehicle("LUX-0001", "Audi A6", 2022, "preto");
        validator.validateVehicleCategory(vehicle);
        assertEquals("Uber Black", vehicle.getCategory());
    }

    @Test
    public void uberComfortCategory() throws ValidationException {
        Vehicle vehicle = new Vehicle("CONF-9999", "Honda Civic", 2018, "Prata");
        validator.validateVehicleCategory(vehicle);
        assertEquals("Uber Comfort", vehicle.getCategory());
    }

    @Test
    public void uberXCategory() throws ValidationException {
        Vehicle vehicle = new Vehicle("UBER-1234", "Fiat Uno", 2014, "Branco");
        validator.validateVehicleCategory(vehicle);
        assertEquals("UberX", vehicle.getCategory());
    }

    @Test
    public void shouldThrowExceptionForInvalidVehicle() {
        Vehicle vehicle = new Vehicle("OLD-CAR", "Fusca", 2009, "Azul");

        ValidationException ex = assertThrows(ValidationException.class, () -> {
            validator.validateVehicleCategory(vehicle);
        });

        assertTrue(ex.getMessage().contains("não atende aos requisitos"));
    }

    // ======================
    // 🆕 TESTES RF02 ENTREGADOR
    // ======================

    @Test
    public void shouldValidateCPF() {
        assertTrue(validator.isValidCPF("12345678901"));
    }

    @Test
    public void shouldInvalidateCPF() {
        assertFalse(validator.isValidCPF("123"));
    }

    @Test
    public void shouldApproveDeliveryDocuments() {
        Delivery delivery = new Delivery(
                "Ana",
                "ana@email.com",
                "12345678901",
                "11999999999",
                "12345678901",
                "CRLV123"
        );

        validator.validateDeliveryDocuments(delivery);

        assertEquals(DeliveryStatus.APROVADO, delivery.getValidationStatus());
    }

    @Test
    public void shouldRejectDeliveryDocuments() {
        Delivery delivery = new Delivery(
                "Ana",
                "ana@email.com",
                "123",
                "11999999999",
                "12345678901",
                "CRLV123"
        );

        validator.validateDeliveryDocuments(delivery);

        assertEquals(DeliveryStatus.REJEITADO, delivery.getValidationStatus());
    }
}