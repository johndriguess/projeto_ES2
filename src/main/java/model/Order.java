package model;

import java.util.List;
import java.util.UUID;

public class Order {

    private final String id;
    private final String restaurantId;
    private final List<MenuItem> items;

    private double subtotal;
    private double deliveryFee;
    private double discount;
    private double total;
    private boolean confirmed;

    public Order(String restaurantId, List<MenuItem> items) {
        this.id = UUID.randomUUID().toString();
        this.restaurantId = restaurantId;
        this.items = items;
        this.confirmed = false;
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
}