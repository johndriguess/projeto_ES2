package service;

import model.MenuItem;
import model.Order;
import model.Restaurant;
import repo.OrderRepository;
import repo.RestaurantRepository;
import util.ValidationException;

import java.util.List;

public class OrderService {

    private final OrderRepository orderRepository;
    private final RestaurantRepository restaurantRepository;
    private final RestaurantService restaurantService;

    public OrderService(OrderRepository orderRepository,
                        RestaurantRepository restaurantRepository,
                        RestaurantService restaurantService) {
        this.orderRepository = orderRepository;
        this.restaurantRepository = restaurantRepository;
        this.restaurantService = restaurantService;
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