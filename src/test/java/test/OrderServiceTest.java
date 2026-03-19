package test;

import model.Location;
import model.MenuItem;
import model.Order;
import model.OrderStatus;
import model.Restaurant;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import repo.OrderRepository;
import repo.RestaurantRepository;
import service.OrderService;
import service.RestaurantService;
import model.Notification;
import service.NotificationService;
import util.ValidationException;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class OrderServiceTest {

    private OrderService orderService;
    private Restaurant restaurant;
    private NotificationService notificationService;

    @BeforeEach
    void setup() {
        String basePath = "target/test-data/order-service-" + System.nanoTime();

        RestaurantRepository restaurantRepository = new RestaurantRepository(basePath + "-restaurants.db");
        RestaurantService restaurantService = new RestaurantService(restaurantRepository);
        OrderRepository orderRepository = new OrderRepository(basePath + "-orders.db");

        notificationService = new NotificationService();
        orderService = new OrderService(orderRepository, restaurantRepository, restaurantService);
        orderService.setNotificationService(notificationService);

        restaurant = restaurantService.register(
                "Pizza Top",
                "pizza@email.com",
                "senha123",
                "12345678901234",
                new Location("Rua A"));

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
                "cliente@teste.com",
                restaurant.getMenu(),
                5,
                0);

        assertNotNull(order.getId());
        assertTrue(order.isAwaitingConfirmation());
        assertTrue(order.getTotal() > 0);

        orderService.confirmOrder(order.getId());

        assertTrue(order.isConfirmed());
    }

    @Test
    void shouldGetOrderStatus() {
        Order o = orderService.createOrder(
                restaurant.getId(),
                "status@teste.com",
                restaurant.getMenu(),
                5,
                0);
        assertEquals(OrderStatus.AGUARDANDO_CONFIRMACAO, orderService.getOrderStatus(o.getId()));
        orderService.confirmOrder(o.getId());
        assertEquals(OrderStatus.PREPARACAO, orderService.getOrderStatus(o.getId()));
    }

    @Test
    void shouldListPendingOrdersForRestaurant() {
        // criar dois pedidos
        Order o1 = orderService.createOrder(
                restaurant.getId(),
                "a@b.com",
                restaurant.getMenu(),
                5,
                0);

        Order o2 = orderService.createOrder(
                restaurant.getId(),
                "a@b.com",
                restaurant.getMenu(),
                5,
                0);
        // confirmar o segundo
        orderService.confirmOrder(o2.getId());

        List<Order> pending = orderService.getPendingOrdersForRestaurant(restaurant.getId());
        assertEquals(1, pending.size());
        assertEquals(o1.getId(), pending.get(0).getId());
    }

    @Test
    void shouldReturnEmptyWhenNoPendingOrders() {
        Order o = orderService.createOrder(
                restaurant.getId(),
                "email@c.com",
                restaurant.getMenu(),
                5,
                0);
        orderService.confirmOrder(o.getId());

        List<Order> pending = orderService.getPendingOrdersForRestaurant(restaurant.getId());
        assertTrue(pending.isEmpty());
    }

    @Test
    void shouldThrowWhenRestaurantNotFoundForPending() {
        assertThrows(ValidationException.class, () -> orderService.getPendingOrdersForRestaurant("non-existent"));
    }

    @Test
    void shouldRejectPendingOrder() {
        Order o = orderService.createOrder(
                restaurant.getId(),
                "cust@rej.com",
                restaurant.getMenu(),
                5,
                0);
        assertTrue(o.isAwaitingConfirmation());

        orderService.rejectOrder(o.getId());
        assertTrue(o.isRejected());

        List<Notification> notes = notificationService.getNotificationsByRecipient("cust@rej.com");
        assertEquals(1, notes.size());
        assertTrue(notes.get(0).getMessage().contains("REJEITADO"));
    }

    @Test
    void shouldProgressThroughStatuses() {
        Order o = orderService.createOrder(
                restaurant.getId(),
                "cust@flow.com",
                restaurant.getMenu(),
                5,
                0);

        // confirmOrder automatically moves to PREPARACAO
        orderService.confirmOrder(o.getId());
        assertTrue(o.isPreparing());
        assertEquals(OrderStatus.PREPARACAO, o.getStatus());

        // mark ready
        orderService.markReady(o.getId());
        assertTrue(o.isReady());
        assertEquals(OrderStatus.PRONTO, o.getStatus());

        // dispatch
        orderService.dispatchOrder(o.getId());
        assertTrue(o.isOutForDelivery());
        assertEquals(OrderStatus.EM_ENTREGA, o.getStatus());

        // deliver
        orderService.deliverOrder(o.getId());
        assertTrue(o.isAwaitingCustomerConfirmation());
        assertEquals(OrderStatus.AGUARDANDO_CONFIRMACAO_CLIENTE, o.getStatus());

        // customer confirms receipt
        orderService.confirmDeliveryByCustomer(o.getId(), "cust@flow.com");
        assertTrue(o.isDelivered());
        assertEquals(OrderStatus.ENTREGUE, o.getStatus());

        List<Notification> notes = notificationService.getNotificationsByRecipient("cust@flow.com");
        // should have multiple notifications for each step
        assertTrue(notes.size() >= 4);
    }

    @Test
    void shouldNotConfirmDeliveryForDifferentCustomer() {
        Order o = orderService.createOrder(
                restaurant.getId(),
                "cliente@ok.com",
                restaurant.getMenu(),
                5,
                0);

        orderService.confirmOrder(o.getId());
        orderService.markReady(o.getId());
        orderService.dispatchOrder(o.getId());
        orderService.deliverOrder(o.getId());

        ValidationException ex = assertThrows(ValidationException.class,
                () -> orderService.confirmDeliveryByCustomer(o.getId(), "outro@teste.com"));

        assertEquals("Este pedido não pertence ao cliente informado.", ex.getMessage());
    }

    @Test
    void confirmOrderShouldNotifyCustomer() {
        Order o = orderService.createOrder(
                restaurant.getId(),
                "cust@ok.com",
                restaurant.getMenu(),
                5,
                0);

        orderService.confirmOrder(o.getId());

        List<Notification> notes = notificationService.getNotificationsByRecipient("cust@ok.com");
        assertEquals(2, notes.size());
        assertTrue(notes.stream().anyMatch(n -> n.getMessage().contains("CONFIRMADO")));
    }

    @Test
    void shouldThrowExceptionWhenNoItems() {
        assertThrows(ValidationException.class, () -> orderService.createOrder(
                restaurant.getId(),
                "email@none.com",
                List.of(),
                5,
                0));
    }
}