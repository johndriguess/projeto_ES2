package service;

import model.MenuItem;
import model.Restaurant;

import java.util.List;

public class RestaurantDetails {

    private final Restaurant restaurant;
    private final List<MenuItem> menu;
    private final double deliveryFee;
    private final int deliveryTime;

    public RestaurantDetails(Restaurant restaurant,
                             List<MenuItem> menu,
                             double deliveryFee,
                             int deliveryTime) {
        this.restaurant = restaurant;
        this.menu = menu;
        this.deliveryFee = deliveryFee;
        this.deliveryTime = deliveryTime;
    }

    public Restaurant getRestaurant() {
        return restaurant;
    }

    public List<MenuItem> getMenu() {
        return menu;
    }

    public double getDeliveryFee() {
        return deliveryFee;
    }

    public int getDeliveryTime() {
        return deliveryTime;
    }
}