package test;

import model.*;
import model.OrderStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import repo.DeliveryRepository;
import service.DeliveryAssignmentService;
import service.DeliveryService;
import service.NotificationService;
import model.Notification;
import util.ValidationException;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class DeliveryAssignmentServiceTest {

        private DeliveryAssignmentService assignmentService;
        private DeliveryRepository deliveryRepository;
        private NotificationService notificationService;
        private DeliveryService deliveryService;

        @BeforeEach
        void setup() {
                String dbPath = "target/test-data/delivery-assignment-" + System.nanoTime() + ".db";
                deliveryRepository = new DeliveryRepository(dbPath);
                notificationService = new NotificationService();
                assignmentService = new DeliveryAssignmentService(deliveryRepository, notificationService);
                deliveryService = new DeliveryService(deliveryRepository);
        }

        @Test
        void shouldFindNearestAvailableDelivery() {
                // Criar entregadores em localizações diferentes
                Delivery delivery1 = deliveryService.register(
                                "João", "joao@email.com", "12345678901",
                                "11999999999", "12345678901", "CRLV123");
                delivery1.setCurrentLocation(new Location("Local 1", "", 0, 0));

                Delivery delivery2 = deliveryService.register(
                                "Maria", "maria@email.com", "12345678902",
                                "11999999998", "12345678902", "CRLV456");
                delivery2.setCurrentLocation(new Location("Local 2", "", 10, 10));

                Location restaurantLocation = new Location("Restaurante", "", 1, 1);

                // Encontrar o mais próximo
                Delivery nearest = assignmentService.findNearestAvailableDelivery(restaurantLocation);

                assertNotNull(nearest);
                assertEquals("João", nearest.getName());
        }

        @Test
        void shouldThrowExceptionWhenNoDeliveryAvailable() {
                Location restaurantLocation = new Location("Restaurante", "", 0, 0);

                assertThrows(ValidationException.class,
                                () -> assignmentService.findNearestAvailableDelivery(restaurantLocation));
        }

        @Test
        void shouldNotSelectInactiveDelivery() {
                Delivery delivery = deliveryService.register(
                                "Pedro", "pedro@email.com", "12345678903",
                                "11999999997", "12345678903", "CRLV789");
                delivery.setCurrentLocation(new Location("Local", "", 0, 0));
                delivery.deactivate();

                Location restaurantLocation = new Location("Restaurante", "", 0, 0);

                assertThrows(ValidationException.class,
                                () -> assignmentService.findNearestAvailableDelivery(restaurantLocation));
        }

        @Test
        void shouldAssignDeliveryToOrder() {
                // Criar entregador disponível
                Delivery delivery = deliveryService.register(
                                "Carlos", "carlos@email.com", "12345678904",
                                "11999999996", "12345678904", "CRLV101");
                delivery.setCurrentLocation(new Location("Local", "", 0, 0));

                // Criar pedido (incluindo email do cliente)
                Order order = new Order("restaurant-123", "cust@delivery.com", List.of(
                                new MenuItem("Pizza", "Calabresa", 40.0)));
                order.confirm();

                Location restaurantLocation = new Location("Restaurante", "", 1, 1);

                // Atribuir entregador
                assignmentService.assignDeliveryToOrder(
                                order,
                                restaurantLocation,
                                "Pizza Top",
                                "Rua das Flores, 123");

                assertNotNull(order.getAssignedDeliveryId());
                assertEquals(delivery.getId(), order.getAssignedDeliveryId());
                assertTrue(order.isAwaitingDeliveryAcceptance());
                assertEquals(OrderStatus.AGUARDANDO_ACEITE_ENTREGADOR, order.getStatus());

                // Verificar se notificação foi enviada
                List<Notification> notifications = notificationService.getNotificationsByRecipient(delivery.getId());
                assertEquals(1, notifications.size());
                assertTrue(notifications.get(0).getMessage().contains("Novo pedido atribuído"));
        }

        @Test
        void shouldFindDeliveriesInRadius() {
                Delivery delivery1 = deliveryService.register(
                                "Ana", "ana@email.com", "12345678905",
                                "11999999995", "12345678905", "CRLV202");
                delivery1.setCurrentLocation(new Location("Perto", "", 1, 1));

                Delivery delivery2 = deliveryService.register(
                                "Bruno", "bruno@email.com", "12345678906",
                                "11999999994", "12345678906", "CRLV303");
                delivery2.setCurrentLocation(new Location("Longe", "", 50, 50));

                Location center = new Location("Centro", "", 0, 0);

                List<Delivery> nearby = assignmentService.findAvailableDeliveriesInRadius(center, 5);

                assertEquals(1, nearby.size());
                assertEquals("Ana", nearby.get(0).getName());
        }

        @Test
        void shouldCalculateDistanceCorrectly() {
                Delivery delivery = deliveryService.register(
                                "Lucas", "lucas@email.com", "12345678907",
                                "11999999993", "12345678907", "CRLV404");
                delivery.setCurrentLocation(new Location("Origem", "", 0, 0));

                Location target = new Location("Destino", "", 3, 4);

                double distance = assignmentService.calculateDistance(delivery.getId(), target);

                assertEquals(5.0, distance, 0.01);
        }
}
