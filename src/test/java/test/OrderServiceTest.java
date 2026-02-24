package test;

import model.Location;
import model.MenuItem;
import model.Order;
import model.Restaurant;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import repo.OrderRepository;
import repo.RestaurantRepository;
import service.OrderService;
import service.RestaurantService;
import util.ValidationException;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class OrderServiceTest {

    private OrderService orderService;
    private Restaurant restaurant;

    @BeforeEach
    void setup() {

        RestaurantRepository restaurantRepository = new RestaurantRepository();
        RestaurantService restaurantService = new RestaurantService(restaurantRepository);
        OrderRepository orderRepository = new OrderRepository();

        orderService = new OrderService(orderRepository, restaurantRepository, restaurantService);

        restaurant = restaurantService.register(
                "Pizza Top",
                "pizza@email.com",
                "12345678901234",
                new Location("Rua A")
        );

        restaurant.addMenuItem(new MenuItem("Pizza Calabresa", "Tradicional", 40));
        restaurant.addMenuItem(new MenuItem("Refrigerante", "Lata", 10));
    }

    @Test
    void shouldCalculateSubtotalCorrectly() {
        double subtotal = orderService.calculateSubtotal(restaurant.getMenu());
        assertEquals(50, subtotal);
    }

    @Test
    void shouldCalculateTotalWithDiscount() {
        double total = orderService.calculateTotal(50, 5, 10);
        assertEquals(45, total);
    }

    @Test
    void shouldCreateAndConfirmOrder() {

        Order order = orderService.createOrder(
                restaurant.getId(),
                restaurant.getMenu(),
                5,
                0
        );

        assertNotNull(order.getId());
        assertFalse(order.isConfirmed());
        assertTrue(order.getTotal() > 0);

        orderService.confirmOrder(order.getId());

        assertTrue(order.isConfirmed());
    }

    @Test
    void shouldThrowExceptionWhenNoItems() {
        assertThrows(ValidationException.class, () ->
                orderService.createOrder(
                        restaurant.getId(),
                        List.of(),
                        5,
                        0
                )
        );
    }
}