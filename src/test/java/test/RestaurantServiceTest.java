package test;

import model.Location;
import model.Restaurant;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import repo.RestaurantRepository;
import service.RestaurantDetails;
import service.RestaurantService;
import util.ValidationException;

import static org.junit.jupiter.api.Assertions.*;

class RestaurantServiceTest {

    private RestaurantService service;
    private RestaurantRepository repository;

    @BeforeEach
    void setup() {
        String dbPath = "target/test-data/restaurant-service-" + System.nanoTime() + ".db";
        repository = new RestaurantRepository(dbPath);
        service = new RestaurantService(repository);
    }

    @Test
    void shouldRegisterRestaurant() {

        Restaurant restaurant = service.register(
                "Pizza Top",
                "pizza@email.com",
                "senha123",
                "12345678901234",
                new Location("Rua A"));

        assertNotNull(restaurant.getId());
        assertEquals("Pizza Top", restaurant.getName());
    }

    @Test
    void shouldNotAllowDuplicateEmail() {

        service.register(
                "Pizza Top",
                "pizza@email.com",
                "senha123",
                "12345678901234",
                new Location("Rua A"));

        assertThrows(ValidationException.class, () -> service.register(
                "Outra",
                "pizza@email.com",
                "senha123",
                "99999999999999",
                new Location("Rua B")));
    }

    @Test
    void shouldCalculateCorrectDeliveryFee() {

        assertEquals(5.0, service.calculateDeliveryFee(3));
        assertEquals(8.0, service.calculateDeliveryFee(7));
        assertEquals(12.0, service.calculateDeliveryFee(15));
    }

    @Test
    void shouldCalculateEstimatedTime() {

        assertEquals(26, service.calculateEstimatedTime(3));
    }

    @Test
    void shouldReturnRestaurantDetails() {

        Restaurant restaurant = service.register(
                "Pizza Top",
                "pizza@email.com",
                "senha123",
                "12345678901234",
                new Location("Rua A"));

        RestaurantDetails details = service.getRestaurantDetails(restaurant.getId(), 5);

        assertNotNull(details);
        assertNotNull(details.getMenu());
        assertTrue(details.getDeliveryFee() > 0);
        assertTrue(details.getDeliveryTime() > 0);
    }
}