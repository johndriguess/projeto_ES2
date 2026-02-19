package repo;

import model.Delivery;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class DeliveryRepository {

    private final List<Delivery> deliveries = new ArrayList<>();

    public void save(Delivery delivery) {
        deliveries.add(delivery);
    }

    public Optional<Delivery> findByEmail(String email) {
        return deliveries.stream()
                .filter(d -> d.getEmail().equalsIgnoreCase(email))
                .findFirst();
    }

    public Optional<Delivery> findByDocument(String document) {
        return deliveries.stream()
                .filter(d -> d.getDocument().equals(document))
                .findFirst();
    }

    public List<Delivery> findAll() {
        return deliveries;
    }
}
