package test;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import repo.RestaurantRepository;
import service.RestaurantService;
import util.ValidationException;

import static org.junit.jupiter.api.Assertions.*;

class RestaurantServiceTest {

    private RestaurantService service;

    @BeforeEach
    void setup() {
        service = new RestaurantService(new RestaurantRepository());
    }

    @Test
    void shouldRegisterRestaurant() {
        var restaurant = service.register(
                "Pizza Top",
                "pizza@email.com",
                "12345678901234",
                "Rua A"
        );

        assertNotNull(restaurant.getId());
    }

    @Test
    void shouldNotAllowDuplicateEmail() {
        service.register(
                "Pizza Top",
                "pizza@email.com",
                "12345678901234",
                "Rua A"
        );

        assertThrows(ValidationException.class, () -> {
            service.register(
                    "Outra",
                    "pizza@email.com",
                    "99999999999999",
                    "Rua B"
            );
        });
    }
}
