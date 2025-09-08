package model;

/**
 * RF06 - categorias de veículos disponíveis no sistema.
 */
public enum VehicleCategory {
    UBER_X("UberX", "Corrida mais econômica."),
    UBER_COMFORT("Uber Comfort", "Carros mais novos e espaçosos."),
    UBER_BLACK("Uber Black", "Veículos premium e motoristas de alta avaliação."),
    UBER_BAG("Uber Bag", "Veículos com porta-malas maior."),
    UBER_XL("Uber XL", "Capacidade para mais passageiros.");

    private final String displayName;
    private final String description;

    VehicleCategory(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getDescription() {
        return description;
    }

    @Override
    public String toString() {
        return displayName + " - " + description;
    }
}
