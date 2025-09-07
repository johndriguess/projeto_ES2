package model;

import java.util.UUID;

public class Passenger extends User {
    private static final long serialVersionUID = 1L;

    public Passenger(String name, String email, String phone) {
        super(UUID.randomUUID().toString(), name, email, phone);
    }
}
