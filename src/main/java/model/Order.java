package model;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public class Order implements Serializable {

    private static final long serialVersionUID = 1L;

    private final String id;
    private final String restaurantId;
    private final List<MenuItem> items;

    private double subtotal;
    private double deliveryFee;
    private double discount;
    private double total;
    private boolean confirmed;

    // RF22 - Atribuição de entregador
    private String assignedDeliveryId;

    // RF23 - Pedidos agendados
    private OrderType orderType;
    private LocalDateTime scheduledTime;

    public Order(String restaurantId, List<MenuItem> items) {
        this.id = UUID.randomUUID().toString();
        this.restaurantId = restaurantId;
        this.items = items;
        this.confirmed = false;
        this.orderType = OrderType.IMEDIATO;
    }

    public Order(String restaurantId, List<MenuItem> items, OrderType orderType, LocalDateTime scheduledTime) {
        this.id = UUID.randomUUID().toString();
        this.restaurantId = restaurantId;
        this.items = items;
        this.confirmed = false;
        this.orderType = orderType;
        this.scheduledTime = scheduledTime;
    }

    public String getId() {
        return id;
    }

    public String getRestaurantId() {
        return restaurantId;
    }

    public List<MenuItem> getItems() {
        return items;
    }

    public double getSubtotal() {
        return subtotal;
    }

    public void setSubtotal(double subtotal) {
        this.subtotal = subtotal;
    }

    public double getDeliveryFee() {
        return deliveryFee;
    }

    public void setDeliveryFee(double deliveryFee) {
        this.deliveryFee = deliveryFee;
    }

    public double getDiscount() {
        return discount;
    }

    public void setDiscount(double discount) {
        this.discount = discount;
    }

    public double getTotal() {
        return total;
    }

    public void setTotal(double total) {
        this.total = total;
    }

    public boolean isConfirmed() {
        return confirmed;
    }

    public void confirm() {
        this.confirmed = true;
    }

    public String getAssignedDeliveryId() {
        return assignedDeliveryId;
    }

    public void setAssignedDeliveryId(String assignedDeliveryId) {
        this.assignedDeliveryId = assignedDeliveryId;
    }

    public OrderType getOrderType() {
        return orderType;
    }

    public void setOrderType(OrderType orderType) {
        this.orderType = orderType;
    }

    public LocalDateTime getScheduledTime() {
        return scheduledTime;
    }

    public void setScheduledTime(LocalDateTime scheduledTime) {
        this.scheduledTime = scheduledTime;
    }

    public boolean isScheduled() {
        return orderType == OrderType.AGENDADO;
    }

    public boolean isImmediate() {
        return orderType == OrderType.IMEDIATO;
    }
}