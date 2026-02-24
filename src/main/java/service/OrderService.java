package service;

import model.MenuItem;
import model.Order;
import model.OrderType;
import model.Restaurant;
import repo.OrderRepository;
import repo.RestaurantRepository;
import util.ValidationException;

import java.time.LocalDateTime;
import java.util.List;

public class OrderService {

    private final OrderRepository orderRepository;
    private final RestaurantRepository restaurantRepository;
    private final RestaurantService restaurantService;
    private NotificationService notificationService;

    public OrderService(OrderRepository orderRepository,
            RestaurantRepository restaurantRepository,
            RestaurantService restaurantService) {
        this.orderRepository = orderRepository;
        this.restaurantRepository = restaurantRepository;
        this.restaurantService = restaurantService;
    }

    public void setNotificationService(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    public Order createOrder(String restaurantId,
            List<MenuItem> items,
            double distance,
            double discount) {

        if (items == null || items.isEmpty()) {
            throw new ValidationException("Pedido deve conter pelo menos um item.");
        }

        Restaurant restaurant = restaurantRepository.findById(restaurantId)
                .orElseThrow(() -> new ValidationException("Restaurante não encontrado."));

        double subtotal = calculateSubtotal(items);
        double deliveryFee = restaurantService.calculateDeliveryFee(distance);
        double total = calculateTotal(subtotal, deliveryFee, discount);

        Order order = new Order(restaurant.getId(), items);
        order.setSubtotal(subtotal);
        order.setDeliveryFee(deliveryFee);
        order.setDiscount(discount);
        order.setTotal(total);

        orderRepository.save(order);

        return order;
    }

    public void confirmOrder(String orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ValidationException("Pedido não encontrado."));

        order.confirm();

        // RF22 - Notificar restaurante quando pedido for confirmado
        if (notificationService != null) {
            Restaurant restaurant = restaurantRepository.findById(order.getRestaurantId())
                    .orElse(null);

            if (restaurant != null) {
                String orderDetails = String.format("%d itens - Total: R$ %.2f",
                        order.getItems().size(), order.getTotal());
                notificationService.notifyRestaurant(
                        restaurant.getId(),
                        order.getId(),
                        orderDetails);
            }
        }
    }

    // RF23 - Criar pedido agendado
    public Order createScheduledOrder(String restaurantId,
            List<MenuItem> items,
            double distance,
            double discount,
            LocalDateTime scheduledTime) {

        if (scheduledTime == null) {
            throw new ValidationException("Horário agendado é obrigatório para pedidos agendados.");
        }

        if (scheduledTime.isBefore(LocalDateTime.now())) {
            throw new ValidationException("Horário agendado não pode ser no passado.");
        }

        if (items == null || items.isEmpty()) {
            throw new ValidationException("Pedido deve conter pelo menos um item.");
        }

        Restaurant restaurant = restaurantRepository.findById(restaurantId)
                .orElseThrow(() -> new ValidationException("Restaurante não encontrado."));

        double subtotal = calculateSubtotal(items);
        double deliveryFee = restaurantService.calculateDeliveryFee(distance);
        double total = calculateTotal(subtotal, deliveryFee, discount);

        Order order = new Order(restaurant.getId(), items, OrderType.AGENDADO, scheduledTime);
        order.setSubtotal(subtotal);
        order.setDeliveryFee(deliveryFee);
        order.setDiscount(discount);
        order.setTotal(total);

        orderRepository.save(order);

        return order;
    }

    // RF23 - Criar pedido imediato (método original mantido para compatibilidade)
    public Order createImmediateOrder(String restaurantId,
            List<MenuItem> items,
            double distance,
            double discount) {

        Order order = createOrder(restaurantId, items, distance, discount);
        order.setOrderType(OrderType.IMEDIATO);
        return order;
    }

    // Buscar pedidos agendados
    public List<Order> getScheduledOrders() {
        return orderRepository.findAll().stream()
                .filter(Order::isScheduled)
                .collect(java.util.stream.Collectors.toList());
    }

    // Buscar pedidos imediatos
    public List<Order> getImmediateOrders() {
        return orderRepository.findAll().stream()
                .filter(Order::isImmediate)
                .collect(java.util.stream.Collectors.toList());
    }

    // Buscar pedido por ID
    public Order findById(String orderId) {
        return orderRepository.findById(orderId)
                .orElseThrow(() -> new ValidationException("Pedido não encontrado."));
    }

    public double calculateSubtotal(List<MenuItem> items) {
        return items.stream()
                .mapToDouble(MenuItem::getPrice)
                .sum();
    }

    public double calculateTotal(double subtotal, double deliveryFee, double discount) {
        return subtotal + deliveryFee - discount;
    }
}