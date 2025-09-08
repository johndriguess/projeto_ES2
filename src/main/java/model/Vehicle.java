package model;

import java.io.Serializable;
import java.util.UUID;

public class Vehicle implements Serializable {
    private static final long serialVersionUID = 1L;

    private String id;
    private String plate;
    private String model;
    private int year;
    private String color;
    private String category; // UberX, Comfort, Black

    public Vehicle(String plate, String model, int year, String color) {
        this.id = UUID.randomUUID().toString();
        this.plate = plate.trim().toUpperCase();
        this.model = model.trim();
        this.year = year;
        this.color = color.trim();
        this.category = "UNASSIGNED"; // Categoria inicial, será definida pelo validador
    }

    // Getters
    public String getId() { return id; }
    public String getPlate() { return plate; }
    public String getModel() { return model; }
    public int getYear() { return year; }
    public String getColor() { return color; }
    public String getCategory() { return category; }

    // a categoria será definida apos a avaliação
    public void setCategory(String category) {
        this.category = category;
    }

    @Override
    public String toString() {
        return String.format("Vehicle[id=%s, plate=%s, model=%s, year=%d, color=%s, category=%s]",
                id, plate, model, year, color, category);
    }
}