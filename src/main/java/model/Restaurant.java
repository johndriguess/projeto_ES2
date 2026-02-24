package model;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Restaurant {

    private String id;
    private String name;
    private String email;
    private String cnpj;
    private Location location;
    private boolean active;
    private boolean open;
    private final List<MenuItem> menu = new ArrayList<>();

    public Restaurant(String name, String email, String cnpj, Location location) {
        this.id = UUID.randomUUID().toString();
        this.name = name;
        this.email = email;
        this.cnpj = cnpj;
        this.location = location;
        this.active = true;
        this.open = true;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public String getCnpj() {
        return cnpj;
    }

    public Location getLocation() {
        return location;
    }

    public boolean isActive() {
        return active;
    }

    public void deactivate() {
        this.active = false;
    }

    public boolean isOpen() {
        return open;
    }

    public void open() {
        this.open = true;
    }

    public void close() {
        this.open = false;
    }

    public void addMenuItem(MenuItem item) {
        menu.add(item);
    }

    public List<MenuItem> getMenu() {
        return menu;
    }
}