package repo;

import model.Vehicle;
import java.io.*;
import java.util.*;

public class VehicleRepository {
    private final File storageFile;
    private Map<String, Vehicle> vehiclesByPlate; // placa -> Vehicle

    public VehicleRepository(String pathToFile) {
        this.storageFile = new File(pathToFile);
        load();
    }

    @SuppressWarnings("unchecked")
    private void load() {
        if (!storageFile.exists()) {
            vehiclesByPlate = new HashMap<>();
            return;
        }
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(storageFile))) {
            Object o = ois.readObject();
            vehiclesByPlate = (Map<String, Vehicle>) o;
        } catch (Exception e) {
            System.err.println("Não foi possível carregar o armazenamento de veículos. Inicializando vazio. (" + e.getMessage() + ")");
            vehiclesByPlate = new HashMap<>();
        }
    }

    private void save() throws IOException {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(storageFile))) {
            oos.writeObject(vehiclesByPlate);
        }
    }

    public synchronized void add(Vehicle v) throws IOException {
        vehiclesByPlate.put(v.getPlate(), v);
        save();
    }

    public boolean existsByPlate(String plate) {
        return vehiclesByPlate.containsKey(plate.toUpperCase());
    }

    public Vehicle findByPlate(String plate) {
        return vehiclesByPlate.get(plate.toUpperCase());
    }

    public Collection<Vehicle> findAll() {
        return Collections.unmodifiableCollection(vehiclesByPlate.values());
    }
}