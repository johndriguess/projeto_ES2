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
        RestaurantRepository restaurantRepository = new RestaurantRepository();
        RestaurantService restaurantService = new RestaurantService(restaurantRepository);
        OrderRepository orderRepository = new OrderRepository();

        notificationService = new NotificationService();
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
    void shouldCreateAndConfirmOrder() {
        Order order = orderService.createOrder(
                restaurant.getId(),
                "cliente@teste.com",
                restaurant.getMenu(),
                5.0, 
                0.0
        );

        assertNotNull(order.getId());
        assertEquals(OrderStatus.AGUARDANDO_CONFIRMACAO, order.getStatus());

        orderService.confirmOrder(order.getId());
        
        Order updatedOrder = orderService.findById(order.getId());
        assertEquals(OrderStatus.PREPARACAO, updatedOrder.getStatus());
    }

    @Test
    void shouldGetOrderStatus() {
        Order o = orderService.createOrder(
                restaurant.getId(),
                "status@teste.com",
                restaurant.getMenu(),
                5.0, 
                0.0
        );
        assertEquals(OrderStatus.AGUARDANDO_CONFIRMACAO, orderService.findById(o.getId()).getStatus());
        orderService.confirmOrder(o.getId());
        assertEquals(OrderStatus.PREPARACAO, orderService.findById(o.getId()).getStatus());
    }

    @Test
    void shouldListPendingOrdersForRestaurant() {
        Order o1 = orderService.createOrder(
                restaurant.getId(),
                "a@b.com",
                restaurant.getMenu(),
                5.0, 
                0.0
        );

        Order o2 = orderService.createOrder(
                restaurant.getId(),
                "a@b.com",
                restaurant.getMenu(),
                5.0, 
                0.0
        );
        
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
                5.0, 
                0.0
        );
        orderService.confirmOrder(o.getId());

        List<Order> pending = orderService.getPendingOrdersForRestaurant(restaurant.getId());
        assertTrue(pending.isEmpty());
    }

    @Test
    void shouldThrowWhenRestaurantNotFoundForPending() {
        assertThrows(ValidationException.class, () -> 
            orderService.getPendingOrdersForRestaurant("non-existent")
        );
    }

    @Test
    void shouldRejectPendingOrder() {
        Order o = orderService.createOrder(
                restaurant.getId(),
                "cust@rej.com",
                restaurant.getMenu(),
                5.0, 
                0.0
        );
        
        assertEquals(OrderStatus.AGUARDANDO_CONFIRMACAO, o.getStatus());

        orderService.rejectOrder(o.getId());
        
        Order updated = orderService.findById(o.getId());
        assertEquals(OrderStatus.REJEITADO, updated.getStatus());
    }

    @Test
    void shouldProgressThroughStatuses() {
        Order o = orderService.createOrder(
                restaurant.getId(),
                "cust@flow.com",
                restaurant.getMenu(),
                5.0, 
                0.0
        );

        orderService.confirmOrder(o.getId());
        orderService.markReady(o.getId());
        orderService.makeOrderAvailableForDelivery(o.getId());
        orderService.acceptOrderByDelivery(o.getId(), "del-1");

        orderService.dispatchOrder(o.getId());
        assertEquals(OrderStatus.EM_ENTREGA, orderService.findById(o.getId()).getStatus());

        orderService.deliverOrder(o.getId());
        assertEquals(OrderStatus.ENTREGUE, orderService.findById(o.getId()).getStatus());
    }

    @Test
    void shouldThrowExceptionWhenNoItems() {
        assertThrows(ValidationException.class, () ->
                orderService.createOrder(
                        restaurant.getId(),
                        "email@none.com",
                        List.of(),
                        5.0, 
                        0.0
                )
        );
    }

    @Test
    void shouldMakeOrderAvailableAndAcceptByDelivery() {
        Order o = orderService.createOrder(
                restaurant.getId(), "cust@test.com", restaurant.getMenu(), 5.0, 0.0);
        
        orderService.confirmOrder(o.getId()); 
        orderService.markReady(o.getId()); 

        orderService.makeOrderAvailableForDelivery(o.getId());
        assertEquals(OrderStatus.DISPONIVEL, orderService.findById(o.getId()).getStatus());

        List<Order> available = orderService.getAvailableOrdersForDelivery("del-1", "Origem", 10.0);
        assertEquals(1, available.size());

        orderService.acceptOrderByDelivery(o.getId(), "del-1");
        Order accepted = orderService.findById(o.getId());
        assertEquals(OrderStatus.ACEITO, accepted.getStatus());
        assertEquals("del-1", accepted.getAssignedDeliveryId());
    }

    @Test
    void shouldRefuseOrderAndExcludeFromAvailableList() {
        Order o = orderService.createOrder(
                restaurant.getId(), "cust@test.com", restaurant.getMenu(), 5.0, 0.0);
        
        orderService.confirmOrder(o.getId()); 
        orderService.markReady(o.getId()); 
        orderService.makeOrderAvailableForDelivery(o.getId());

        orderService.refuseOrderByDelivery(o.getId(), "del-1");
        Order refused = orderService.findById(o.getId());
        assertTrue(refused.getRefusedDeliveryIds().contains("del-1"));

        List<Order> available = orderService.getAvailableOrdersForDelivery("del-1", "Origem", 10.0);
        assertTrue(available.isEmpty()); 
    }
}