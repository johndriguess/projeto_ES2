package model;

public enum VehicleCategory {
    UBER_X("UberX", "Corrida mais econômica.", false),
    UBER_COMFORT("Uber Comfort", "Carros mais novos e espaçosos.", true),
    UBER_BLACK("Uber Black", "Veículos premium e motoristas de alta avaliação.", true),
    UBER_BAG("Uber Bag", "Veículos com porta-malas maior.", false),
    UBER_XL("Uber XL", "Capacidade para mais passageiros.", false);

    private final String displayName;
    private final String description;
    private final boolean isPremium;

    VehicleCategory(String displayName, String description, boolean isPremium) {
        this.displayName = displayName;
        this.description = description;
        this.isPremium = isPremium;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getDescription() {
        return description;
    }
    
    public boolean isPremium() {
        return isPremium;
    }

    @Override
    public String toString() {
        return displayName + " - " + description;
    }
}