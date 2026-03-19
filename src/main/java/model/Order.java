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
    private final String customerEmail; // email do cliente que fez o pedido

    private double subtotal;
    private double deliveryFee;
    private double discount;
    private double total;

    private OrderStatus status; // AGUARDANDO_CONFIRMACAO, CONFIRMADO, REJEITADO

    // RF22 - Atribuição de entregador
    private String assignedDeliveryId;

    // RF23 - Pedidos agendados
    private OrderType orderType;
    private LocalDateTime scheduledTime;

    public Order(String restaurantId, String customerEmail, List<MenuItem> items) {
        this.id = UUID.randomUUID().toString();
        this.restaurantId = restaurantId;
        this.customerEmail = customerEmail;
        this.items = items;
        this.status = OrderStatus.AGUARDANDO_CONFIRMACAO;
        this.orderType = OrderType.IMEDIATO;
    }

    public Order(String restaurantId, String customerEmail, List<MenuItem> items, OrderType orderType,
            LocalDateTime scheduledTime) {
        this.id = UUID.randomUUID().toString();
        this.restaurantId = restaurantId;
        this.customerEmail = customerEmail;
        this.items = items;
        this.status = OrderStatus.AGUARDANDO_CONFIRMACAO;
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

    public boolean isAwaitingConfirmation() {
        return status == OrderStatus.AGUARDANDO_CONFIRMACAO;
    }

    public boolean isConfirmed() {
        return status == OrderStatus.CONFIRMADO
                || status == OrderStatus.AGUARDANDO_ACEITE_ENTREGADOR
                || status == OrderStatus.PREPARACAO
                || status == OrderStatus.PRONTO
                || status == OrderStatus.EM_ENTREGA
                || status == OrderStatus.ENTREGUE;
    }

    public boolean isAwaitingDeliveryAcceptance() {
        return status == OrderStatus.AGUARDANDO_ACEITE_ENTREGADOR;
    }

    public boolean isPreparing() {
        return status == OrderStatus.PREPARACAO;
    }

    public boolean isReady() {
        return status == OrderStatus.PRONTO;
    }

    public boolean isOutForDelivery() {
        return status == OrderStatus.EM_ENTREGA;
    }

    public boolean isAwaitingCustomerConfirmation() {
        return status == OrderStatus.AGUARDANDO_CONFIRMACAO_CLIENTE;
    }

    public boolean isDelivered() {
        return status == OrderStatus.ENTREGUE;
    }

    public boolean isRejected() {
        return status == OrderStatus.REJEITADO;
    }

    public void confirm() {
        this.status = OrderStatus.CONFIRMADO;
    }

    public void reject() {
        this.status = OrderStatus.REJEITADO;
    }

    public void setStatus(OrderStatus status) {
        this.status = status;
    }

    public String getAssignedDeliveryId() {
        return assignedDeliveryId;
    }

    public String getCustomerEmail() {
        return customerEmail;
    }

    public OrderStatus getStatus() {
        return status;
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