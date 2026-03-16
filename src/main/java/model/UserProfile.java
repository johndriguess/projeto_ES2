package model;

import java.util.ArrayList;
import java.util.List;

/**
 * Agrega múltiplos papéis que uma pessoa pode ter no sistema
 */
public class UserProfile {
    private final String email;
    private User user; // Pode ser Passenger ou Driver
    private Delivery delivery;
    private Restaurant restaurant;

    public UserProfile(String email) {
        this.email = email;
    }

    public String getEmail() {
        return email;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Delivery getDelivery() {
        return delivery;
    }

    public void setDelivery(Delivery delivery) {
        this.delivery = delivery;
    }

    public Restaurant getRestaurant() {
        return restaurant;
    }

    public void setRestaurant(Restaurant restaurant) {
        this.restaurant = restaurant;
    }

    public boolean hasMultipleRoles() {
        int roleCount = 0;
        if (user != null)
            roleCount++;
        if (delivery != null)
            roleCount++;
        if (restaurant != null)
            roleCount++;
        return roleCount > 1;
    }

    public List<String> getRoles() {
        List<String> roles = new ArrayList<>();
        if (user instanceof Passenger)
            roles.add("Passageiro");
        if (user instanceof Driver)
            roles.add("Motorista");
        if (delivery != null)
            roles.add("Entregador");
        if (restaurant != null)
            roles.add("Restaurante");
        return roles;
    }

    public boolean isPassenger() {
        return user instanceof Passenger;
    }

    public boolean isDriver() {
        return user instanceof Driver;
    }

    public boolean isDelivery() {
        return delivery != null;
    }

    public boolean isRestaurant() {
        return restaurant != null;
    }

    public Passenger getAsPassenger() {
        return user instanceof Passenger ? (Passenger) user : null;
    }

    public Driver getAsDriver() {
        return user instanceof Driver ? (Driver) user : null;
    }

    public String getPrimaryName() {
        if (user != null)
            return user.getName();
        if (delivery != null)
            return delivery.getName();
        if (restaurant != null)
            return restaurant.getName();
        return "Usuário";
    }
}
