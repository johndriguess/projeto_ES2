package model;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

public abstract class User implements Serializable {
    private static final long serialVersionUID = 1L;
    protected String id;
    protected String name;
    protected String email;
    protected String phone;
    protected String password;

    public User(String name, String email, String phone, String password) {
        this.id = UUID.randomUUID().toString();
        this.name = name.trim();
        this.email = email.trim().toLowerCase();
        this.phone = phone.trim();
        this.password = password;
    }

    public User(String id, String name, String email, String phone, String password) {
        this.id = id;
        this.name = name.trim();
        this.email = email.trim().toLowerCase();
        this.phone = phone.trim();
        this.password = password;
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public String getEmail() { return email; }
    public String getPhone() { return phone; }
    public String getPassword() { return password; }

    public abstract String getRole();

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof User)) return false;
        User u = (User) o;
        return Objects.equals(email, u.email);
    }

    @Override
    public int hashCode() {
        return Objects.hash(email);
    }

    @Override
    public String toString() {
        return String.format("%s[id=%s,name=%s,email=%s,phone=%s]",
                this.getClass().getSimpleName(), id, name, email, phone);
    }
}