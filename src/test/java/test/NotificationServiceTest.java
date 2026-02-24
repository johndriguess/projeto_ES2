package test;

import model.Notification;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import service.NotificationService;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class NotificationServiceTest {

    private NotificationService service;

    @BeforeEach
    void setup() {
        service = new NotificationService();
    }

    @Test
    void shouldNotifyRestaurant() {
        Notification notification = service.notifyRestaurant(
                "restaurant-123",
                "order-456",
                "2 itens - Total: R$ 45,00");

        assertNotNull(notification);
        assertNotNull(notification.getId());
        assertEquals("restaurant-123", notification.getRecipientId());
        assertEquals("RESTAURANT", notification.getRecipientType());
        assertTrue(notification.getMessage().contains("Novo pedido recebido"));
        assertFalse(notification.isRead());
    }

    @Test
    void shouldNotifyDelivery() {
        Notification notification = service.notifyDelivery(
                "delivery-789",
                "order-456",
                "Pizza Top",
                "Rua das Flores, 123");

        assertNotNull(notification);
        assertEquals("delivery-789", notification.getRecipientId());
        assertEquals("DELIVERY", notification.getRecipientType());
        assertTrue(notification.getMessage().contains("Novo pedido atribuído"));
    }

    @Test
    void shouldGetNotificationsByRecipient() {
        service.notifyRestaurant("restaurant-1", "order-1", "Pedido 1");
        service.notifyRestaurant("restaurant-1", "order-2", "Pedido 2");
        service.notifyRestaurant("restaurant-2", "order-3", "Pedido 3");

        List<Notification> notifications = service.getNotificationsByRecipient("restaurant-1");

        assertEquals(2, notifications.size());
    }

    @Test
    void shouldGetUnreadNotifications() {
        service.notifyRestaurant("restaurant-1", "order-1", "Pedido 1");
        Notification n2 = service.notifyRestaurant("restaurant-1", "order-2", "Pedido 2");
        n2.markAsRead();

        List<Notification> unread = service.getUnreadNotificationsByRecipient("restaurant-1");

        assertEquals(1, unread.size());
    }

    @Test
    void shouldMarkNotificationAsRead() {
        Notification notification = service.notifyRestaurant(
                "restaurant-1",
                "order-1",
                "Pedido 1");

        assertFalse(notification.isRead());

        service.markAsRead(notification.getId());

        assertTrue(notification.isRead());
    }

    @Test
    void shouldMarkAllAsRead() {
        service.notifyRestaurant("restaurant-1", "order-1", "Pedido 1");
        service.notifyRestaurant("restaurant-1", "order-2", "Pedido 2");

        service.markAllAsRead("restaurant-1");

        List<Notification> unread = service.getUnreadNotificationsByRecipient("restaurant-1");
        assertEquals(0, unread.size());
    }

    @Test
    void shouldClearNotifications() {
        service.notifyRestaurant("restaurant-1", "order-1", "Pedido 1");
        service.notifyDelivery("delivery-1", "order-1", "Pizza", "Rua A");

        assertEquals(2, service.getAllNotifications().size());

        service.clearNotifications();

        assertEquals(0, service.getAllNotifications().size());
    }
}
