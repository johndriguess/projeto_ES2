package test;

import model.Delivery;
import model.DeliveryStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import repo.DeliveryRepository;
import service.DeliveryService;
import util.ValidationException;

import static org.junit.jupiter.api.Assertions.*;

class DeliveryServiceTest {

    private DeliveryService service;

    @BeforeEach
    void setup() {
        service = new DeliveryService(new DeliveryRepository());
    }

    // ✅ teste antigo (continua válido)
    @Test
    void shouldRegisterDelivery() {
        Delivery delivery = service.register(
                "João",
                "joao@email.com",
                "12345678901",
                "999999999"
        );

        assertNotNull(delivery.getId());
    }

    // ✅ teste antigo (continua válido)
    @Test
    void shouldNotAllowDuplicateEmail() {
        service.register(
                "João",
                "joao@email.com",
                "12345678901",
                "999999999"
        );

        assertThrows(ValidationException.class, () -> {
            service.register(
                    "Pedro",
                    "joao@email.com",
                    "99999999999",
                    "88888888"
            );
        });
    }

    // 🆕 RF02 — entregador aprovado
    @Test
    void shouldApproveDeliveryWithValidDocuments() {
        Delivery delivery = service.registerWithDocuments(
                "Maria",
                "maria@email.com",
                "12345678901",
                "11999999999",
                "12345678901",
                "CRLV123"
        );

        assertEquals(DeliveryStatus.APROVADO, delivery.getValidationStatus());
    }

    // 🆕 RF02 — entregador rejeitado
    @Test
    void shouldRejectDeliveryWithInvalidCPF() {
        Delivery delivery = service.registerWithDocuments(
                "Maria",
                "maria2@email.com",
                "123",
                "11999999999",
                "12345678901",
                "CRLV123"
        );

        assertEquals(DeliveryStatus.REJEITADO, delivery.getValidationStatus());
    }

    // 🆕 validação de CNH obrigatória
    @Test
    void shouldThrowExceptionWhenCNHMissing() {
        assertThrows(ValidationException.class, () ->
                service.registerWithDocuments(
                        "Carlos",
                        "carlos@email.com",
                        "12345678901",
                        "11999999999",
                        "",
                        "CRLV123"
                )
        );
    }
}