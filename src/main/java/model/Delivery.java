package model;

import java.util.UUID;

public class Delivery {

    private String id;
    private String name;
    private String email;
    private String document;
    private String phone;
    private boolean active;

    public Delivery(String name, String email, String document, String phone) {
        this.id = UUID.randomUUID().toString();
        this.name = name;
        this.email = email;
        this.document = document;
        this.phone = phone;
        this.active = true;
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

    public String getDocument() {
        return document;
    }

    public String getPhone() {
        return phone;
    }

    public boolean isActive() {
        return active;
    }

    public void deactivate() {
        this.active = false;
    }
}
