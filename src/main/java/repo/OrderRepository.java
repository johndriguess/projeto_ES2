package repo;

import model.Order;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class OrderRepository {

    private final File storageFile;
    private Map<String, Order> ordersById;

    public OrderRepository() {
        File dataDir = new File("data");
        if (!dataDir.exists())
            dataDir.mkdirs();
        this.storageFile = new File(dataDir, "orders.db");
        load();
    }

    public OrderRepository(String filePath) {
        this.storageFile = new File(filePath);
        load();
    }

    @SuppressWarnings("unchecked")
    private void load() {
        if (!storageFile.exists()) {
            ordersById = new HashMap<>();
            return;
        }
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(storageFile))) {
            Object o = ois.readObject();
            ordersById = (Map<String, Order>) o;
        } catch (Exception e) {
            System.err.println("Não foi possível carregar pedidos. Inicializando vazio. (" + e.getMessage() + ")");
            ordersById = new HashMap<>();
        }
    }

    private void persist() {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(storageFile))) {
            oos.writeObject(ordersById);
        } catch (IOException e) {
            System.err.println("Erro ao salvar pedidos: " + e.getMessage());
        }
    }

    public void save(Order order) {
        ordersById.put(order.getId(), order);
        persist();
    }

    public Optional<Order> findById(String id) {
        return Optional.ofNullable(ordersById.get(id));
    }

    public List<Order> findAll() {
        return new ArrayList<>(ordersById.values());
    }

    public void update(Order order) {
        ordersById.put(order.getId(), order);
        persist();
    }
}