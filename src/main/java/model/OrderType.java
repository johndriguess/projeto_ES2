package model;

public enum OrderType {
    IMEDIATO("Imediato"),
    AGENDADO("Agendado");

    private final String displayName;

    OrderType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
