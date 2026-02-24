package test;

import model.Location;
import model.Restaurant;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import repo.RestaurantRepository;
import service.RestaurantService;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class RestaurantAvailabilityTest {

    private RestaurantService service;

    @BeforeEach
    void setup() {
        RestaurantRepository repository = new RestaurantRepository();
        service = new RestaurantService(repository);
    }

    @Test
    void shouldReturnRestaurantWithinRadius() {

        service.register(
                "Pizza",
                "pizza@email.com",
                "12345678901234",
                new Location("Centro", "", 0, 0)
        );

        service.register(
                "Sushi",
                "sushi@email.com",
                "12345678901235",
                new Location("Bairro", "", 20, 20)
        );

        List<Restaurant> result =
                service.findAvailableRestaurants(
                        new Location("Cliente", "", 1, 1),
                        5
                );

        assertEquals(1, result.size());
        assertEquals("Pizza", result.get(0).getName());
    }

    @Test
    void shouldNotReturnClosedRestaurant() {

        Restaurant r = service.register(
                "Burger",
                "burger@email.com",
                "12345678901236",
                new Location("Centro", "", 0, 0)
        );

        r.close();

        List<Restaurant> result =
                service.findAvailableRestaurants(
                        new Location("Cliente", "", 1, 1),
                        5
                );

        assertTrue(result.isEmpty());
    }

    @Test
    void shouldNotReturnInactiveRestaurant() {

        Restaurant r = service.register(
                "Lanche",
                "lanche@email.com",
                "12345678901237",
                new Location("Centro", "", 0, 0)
        );

        r.deactivate();

        List<Restaurant> result =
                service.findAvailableRestaurants(
                        new Location("Cliente", "", 1, 1),
                        5
                );

        assertTrue(result.isEmpty());
    }

    @Test
    void shouldNotReturnRestaurantOutsideRadius() {

        service.register(
                "Churrasco",
                "churrasco@email.com",
                "12345678901238",
                new Location("Distante", "", 50, 50)
        );

        List<Restaurant> result =
                service.findAvailableRestaurants(
                        new Location("Cliente", "", 0, 0),
                        5
                );

        assertTrue(result.isEmpty());
    }
}