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

    // --- AVALIAÇÃO (RF16) ---
    private double totalRatingSum = 0.0;
    private int totalRatings = 0;
    private double averageRating = 0.0; 
    
    
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

    
    public void addRating(int rating) {
        if (rating < 1) {
            rating = 1;
        } else if (rating > 5) {
            rating = 5;
        }

        this.totalRatingSum += rating;
        this.totalRatings++;
        
        if (this.totalRatings > 0) {
            this.averageRating = this.totalRatingSum / this.totalRatings;
        }
    }

    public double getAverageRating() {
        return this.averageRating;
    }

 
    public int getTotalRatings() {
        return this.totalRatings;
    }
    // ------------------------------------------

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
        return String.format("%s[id=%s,name=%s,email=%s,phone=%s,rating=%.1f]",
                this.getClass().getSimpleName(), id, name, email, phone, averageRating); 
    }
}