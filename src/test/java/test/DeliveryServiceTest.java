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

    // ✅ RF02 - Teste de cadastro completo com documentos
    @Test
    void shouldRegisterDeliveryWithAllDocuments() {
        Delivery delivery = service.register(
                "João",
                "joao@email.com",
                "12345678901",
                "999999999",
                "12345678901",
                "CRLV123");

        assertNotNull(delivery.getId());
        assertEquals("João", delivery.getName());
    }

    // ✅ RF02 - Validação de email duplicado
    @Test
    void shouldNotAllowDuplicateEmail() {
        service.register(
                "João",
                "joao@email.com",
                "12345678901",
                "999999999",
                "12345678901",
                "CRLV123");

        assertThrows(ValidationException.class, () -> {
            service.register(
                    "Pedro",
                    "joao@email.com",
                    "99999999999",
                    "88888888",
                    "99999999999",
                    "CRLV456");
        });
    }

    // 🆕 RF02 — entregador aprovado com documentos válidos
    @Test
    void shouldApproveDeliveryWithValidDocuments() {
        Delivery delivery = service.register(
                "Maria",
                "maria@email.com",
                "12345678901",
                "11999999999",
                "12345678901",
                "CRLV123");

        assertEquals(DeliveryStatus.APROVADO, delivery.getValidationStatus());
    }

    // 🆕 RF02 — entregador rejeitado por CPF inválido
    @Test
    void shouldRejectDeliveryWithInvalidCPF() {
        Delivery delivery = service.register(
                "Maria",
                "maria2@email.com",
                "123",
                "11999999999",
                "12345678901",
                "CRLV123");

        assertEquals(DeliveryStatus.REJEITADO, delivery.getValidationStatus());
    }

    // 🆕 RF02 - Validação de CNH obrigatória
    @Test
    void shouldThrowExceptionWhenCNHMissing() {
        assertThrows(ValidationException.class, () -> service.register(
                "Carlos",
                "carlos@email.com",
                "12345678901",
                "11999999999",
                "",
                "CRLV123"));
    }
}