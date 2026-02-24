package model;

import java.util.List;

public class Menu {

    private String restaurantId;
    private List<MenuItem> items;

    public Menu(String restaurantId, List<MenuItem> items) {
        this.restaurantId = restaurantId;
        this.items = items;
    }

    public String getRestaurantId() {
        return restaurantId;
    }

    public List<MenuItem> getItems() {
        return items;
    }
}