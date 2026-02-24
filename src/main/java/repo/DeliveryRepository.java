package repo;

import model.Delivery;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class DeliveryRepository {

    private final File storageFile;
    private Map<String, Delivery> deliveriesByEmail;

    public DeliveryRepository() {
        File dataDir = new File("data");
        if (!dataDir.exists())
            dataDir.mkdirs();
        this.storageFile = new File(dataDir, "deliveries.db");
        load();
    }

    public DeliveryRepository(String filePath) {
        this.storageFile = new File(filePath);
        load();
    }

    @SuppressWarnings("unchecked")
    private void load() {
        if (!storageFile.exists()) {
            deliveriesByEmail = new HashMap<>();
            return;
        }
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(storageFile))) {
            Object o = ois.readObject();
            deliveriesByEmail = (Map<String, Delivery>) o;
        } catch (Exception e) {
            System.err.println("Não foi possível carregar entregadores. Inicializando vazio. (" + e.getMessage() + ")");
            deliveriesByEmail = new HashMap<>();
        }
    }

    private void persist() {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(storageFile))) {
            oos.writeObject(deliveriesByEmail);
        } catch (IOException e) {
            System.err.println("Erro ao salvar entregadores: " + e.getMessage());
        }
    }

    public void save(Delivery delivery) {
        deliveriesByEmail.put(delivery.getEmail().toLowerCase(), delivery);
        persist();
    }

    public Optional<Delivery> findByEmail(String email) {
        return Optional.ofNullable(deliveriesByEmail.get(email.toLowerCase()));
    }

    public Optional<Delivery> findByDocument(String document) {
        return deliveriesByEmail.values().stream()
                .filter(d -> d.getDocument().equals(document))
                .findFirst();
    }

    public Optional<Delivery> findById(String id) {
        return deliveriesByEmail.values().stream()
                .filter(d -> d.getId().equals(id))
                .findFirst();
    }

    public List<Delivery> findAll() {
        return new ArrayList<>(deliveriesByEmail.values());
    }

    public void update(Delivery delivery) {
        deliveriesByEmail.put(delivery.getEmail().toLowerCase(), delivery);
        persist();
    }
}
