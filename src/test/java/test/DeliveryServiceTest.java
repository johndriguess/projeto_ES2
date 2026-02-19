package test;

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

    @Test
    void shouldRegisterDelivery() {
        var delivery = service.register(
                "João",
                "joao@email.com",
                "12345678901",
                "999999999"
        );

        assertNotNull(delivery.getId());
    }

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
}

