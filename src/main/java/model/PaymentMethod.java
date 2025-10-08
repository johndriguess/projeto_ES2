package model;

public enum PaymentMethod {
    CREDIT_CARD("Cartão de Crédito"),
    PIX("PIX"),
    PAYPAL("PayPal"),
    CASH("Dinheiro");

    private final String displayName;

    PaymentMethod(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
