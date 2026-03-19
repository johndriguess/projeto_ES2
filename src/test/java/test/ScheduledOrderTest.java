package test;

import model.*;
import model.OrderStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import repo.OrderRepository;
import repo.RestaurantRepository;
import service.OrderService;
import service.RestaurantService;
import util.ValidationException;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ScheduledOrderTest {

        private OrderService orderService;
        private Restaurant restaurant;

        @BeforeEach
        void setup() {
                String basePath = "target/test-data/scheduled-order-" + System.nanoTime();
                RestaurantRepository restaurantRepository = new RestaurantRepository(basePath + "-restaurants.db");
                RestaurantService restaurantService = new RestaurantService(restaurantRepository);
                OrderRepository orderRepository = new OrderRepository(basePath + "-orders.db");

                orderService = new OrderService(orderRepository, restaurantRepository, restaurantService);

                restaurant = restaurantService.register(
                                "Pizza Top",
                                "pizza@email.com",
                                "senha123",
                                "12345678901234",
                                new Location("Rua A", "", 0, 0));

                restaurant.addMenuItem(new MenuItem("Pizza Calabresa", "Tradicional", 40));
                restaurant.addMenuItem(new MenuItem("Refrigerante", "Lata", 10));
        }

        @Test
        void shouldCreateScheduledOrder() {
                LocalDateTime scheduledTime = LocalDateTime.now().plusHours(2);

                Order order = orderService.createScheduledOrder(
                                restaurant.getId(),
                                "cliente@teste.com",
                                restaurant.getMenu(),
                                5,
                                0,
                                scheduledTime);
                assertEquals(OrderStatus.AGUARDANDO_CONFIRMACAO, order.getStatus());
                orderService.confirmOrder(order.getId());
                assertTrue(order.isPreparing());

                assertNotNull(order);
                assertNotNull(order.getId());
                assertEquals(OrderType.AGENDADO, order.getOrderType());
                assertEquals(scheduledTime, order.getScheduledTime());
                assertTrue(order.isScheduled());
                assertFalse(order.isImmediate());
        }

        @Test
        void shouldCreateImmediateOrder() {
                Order order = orderService.createImmediateOrder(
                                restaurant.getId(),
                                "cliente@teste.com",
                                restaurant.getMenu(),
                                5,
                                0);

                assertNotNull(order);
                assertEquals(OrderType.IMEDIATO, order.getOrderType());
                assertTrue(order.isImmediate());
                assertFalse(order.isScheduled());
                assertNull(order.getScheduledTime());
        }

        @Test
        void shouldNotAllowScheduledOrderInThePast() {
                LocalDateTime pastTime = LocalDateTime.now().minusHours(1);

                assertThrows(ValidationException.class, () -> orderService.createScheduledOrder(
                                restaurant.getId(),
                                "cliente@teste.com",
                                restaurant.getMenu(),
                                5,
                                0,
                                pastTime));
        }

        @Test
        void shouldRequireScheduledTimeForScheduledOrders() {
                assertThrows(ValidationException.class, () -> orderService.createScheduledOrder(
                                restaurant.getId(),
                                "cliente@teste.com",
                                restaurant.getMenu(),
                                5,
                                0,
                                null));
        }

        @Test
        void shouldGetScheduledOrders() {
                // Criar pedido imediato
                orderService.createImmediateOrder(
                                restaurant.getId(),
                                "cliente@teste.com",
                                restaurant.getMenu(),
                                5,
                                0);

                // Criar pedido agendado
                orderService.createScheduledOrder(
                                restaurant.getId(),
                                "cliente@teste.com",
                                restaurant.getMenu(),
                                5,
                                0,
                                LocalDateTime.now().plusHours(3));

                List<Order> scheduledOrders = orderService.getScheduledOrders();

                assertEquals(1, scheduledOrders.size());
                assertTrue(scheduledOrders.get(0).isScheduled());
        }

        @Test
        void shouldGetImmediateOrders() {
                // Criar pedido imediato
                orderService.createImmediateOrder(
                                restaurant.getId(),
                                "cliente@teste.com",
                                restaurant.getMenu(),
                                5,
                                0);

                // Criar pedido agendado
                orderService.createScheduledOrder(
                                restaurant.getId(),
                                "cliente@teste.com",
                                restaurant.getMenu(),
                                5,
                                0,
                                LocalDateTime.now().plusHours(3));

                List<Order> immediateOrders = orderService.getImmediateOrders();

                assertEquals(1, immediateOrders.size());
                assertTrue(immediateOrders.get(0).isImmediate());
        }

        @Test
        void shouldScheduleOrderForSpecificTime() {
                LocalDateTime tomorrow2PM = LocalDateTime.now()
                                .plusDays(1)
                                .withHour(14)
                                .withMinute(0)
                                .withSecond(0);

                Order order = orderService.createScheduledOrder(
                                restaurant.getId(),
                                "cliente@outro.com",
                                List.of(new MenuItem("Pizza", "Margherita", 35)),
                                8,
                                5,
                                tomorrow2PM);
                assertEquals(OrderStatus.AGUARDANDO_CONFIRMACAO, order.getStatus());

                assertNotNull(order.getScheduledTime());
                assertEquals(14, order.getScheduledTime().getHour());
                assertEquals(OrderType.AGENDADO, order.getOrderType());
        }

        @Test
        void shouldCalculatePriceCorrectlyForScheduledOrders() {
                Order order = orderService.createScheduledOrder(
                                restaurant.getId(),
                                "cliente@ok.com",
                                restaurant.getMenu(),
                                5,
                                10,
                                LocalDateTime.now().plusHours(5));

                double expectedSubtotal = 50; // 40 + 10
                double expectedDeliveryFee = 5;
                double expectedDiscount = 10;
                double expectedTotal = 45; // 50 + 5 - 10

                assertEquals(expectedSubtotal, order.getSubtotal());
                assertEquals(expectedDeliveryFee, order.getDeliveryFee());
                assertEquals(expectedDiscount, order.getDiscount());
                assertEquals(expectedTotal, order.getTotal());
        }
}
