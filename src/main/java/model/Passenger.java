package model;

import java.io.Serializable;

public class Passenger extends User implements Serializable {
    private static final long serialVersionUID = 1L;

    public Passenger(String name, String email, String phone, String password) {
        super(name, email, phone, password);
    }
    
    // Construtor auxiliar para desserialização
    public Passenger(String id, String name, String email, String phone, String password) {
        super(id, name, email, phone, password);
    }

    @Override
    public String getRole() {
        return "PASSENGER";
    }

    @Override
    public String toString() {
        return String.format("Passenger[id=%s, name=%s, email=%s, phone=%s]",
                id, name, email, phone);
    }
}