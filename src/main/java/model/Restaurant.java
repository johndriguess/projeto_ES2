package model;

import java.util.UUID;

public class Restaurant {

    private String id;
    private String name;
    private String email;
    private String cnpj;
    private String address;
    private boolean active;

    public Restaurant(String name, String email, String cnpj, String address) {
        this.id = UUID.randomUUID().toString();
        this.name = name;
        this.email = email;
        this.cnpj = cnpj;
        this.address = address;
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

    public String getCnpj() {
        return cnpj;
    }

    public String getAddress() {
        return address;
    }

    public boolean isActive() {
        return active;
    }

    public void deactivate() {
        this.active = false;
    }
}
