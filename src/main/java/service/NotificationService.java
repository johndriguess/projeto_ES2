package service;

import model.Notification;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class NotificationService {

    private final List<Notification> notifications = new ArrayList<>();

    // RF22 - Notificar restaurante sobre novo pedido
    public Notification notifyRestaurant(String restaurantId, String orderId, String orderDetails) {
        String message = String.format(
                "Novo pedido recebido! ID: %s - %s",
                orderId,
                orderDetails);

        Notification notification = new Notification(restaurantId, "RESTAURANT", message);
        notifications.add(notification);
        return notification;
    }

    // RF22 - Notificar entregador sobre atribuição de pedido
    public Notification notifyDelivery(String deliveryId, String orderId, String restaurantName,
            String deliveryAddress) {
        String message = String.format(
                "Novo pedido atribuído! ID: %s - Restaurante: %s - Destino: %s",
                orderId,
                restaurantName,
                deliveryAddress);

        Notification notification = new Notification(deliveryId, "DELIVERY", message);
        notifications.add(notification);
        return notification;
    }

    // RF?? - Notificar cliente sobre status do pedido
    public Notification notifyCustomer(String customerEmail, String orderId, String message) {
        String fullMessage = String.format(
                "Pedido %s: %s",
                orderId,
                message);

        Notification notification = new Notification(customerEmail, "CUSTOMER", fullMessage);
        notifications.add(notification);
        return notification;
    }

    // Buscar notificações por destinatário
    public List<Notification> getNotificationsByRecipient(String recipientId) {
        return notifications.stream()
                .filter(n -> n.getRecipientId().equals(recipientId))
                .collect(Collectors.toList());
    }

    // Buscar notificações não lidas
    public List<Notification> getUnreadNotificationsByRecipient(String recipientId) {
        return notifications.stream()
                .filter(n -> n.getRecipientId().equals(recipientId))
                .filter(n -> !n.isRead())
                .collect(Collectors.toList());
    }

    // Marcar notificação como lida
    public void markAsRead(String notificationId) {
        notifications.stream()
                .filter(n -> n.getId().equals(notificationId))
                .findFirst()
                .ifPresent(Notification::markAsRead);
    }

    // Marcar todas as notificações de um destinatário como lidas
    public void markAllAsRead(String recipientId) {
        notifications.stream()
                .filter(n -> n.getRecipientId().equals(recipientId))
                .forEach(Notification::markAsRead);
    }

    // Buscar todas as notificações
    public List<Notification> getAllNotifications() {
        return new ArrayList<>(notifications);
    }

    // Limpar notificações (útil para testes)
    public void clearNotifications() {
        notifications.clear();
    }
}
