package model;

import java.io.Serializable;

/**
 * Representa um endereço/localização no sistema.
 * RF04 - Para capturar origem e destino das corridas.
 */
public class Location implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private String address;
    private String description;
    
    public Location(String address) {
        this.address = address.trim();
        this.description = "";
    }
    
    public Location(String address, String description) {
        this.address = address.trim();
        this.description = description != null ? description.trim() : "";
    }
    
    public String getAddress() {
        return address;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description != null ? description.trim() : "";
    }
    
    @Override
    public String toString() {
        if (description.isEmpty()) {
            return address;
        }
        return address + " - " + description;
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Location location = (Location) obj;
        return address.equals(location.address);
    }
    
    @Override
    public int hashCode() {
        return address.hashCode();
    }
}
