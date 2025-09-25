package repo;

import model.Ride;
import java.io.*;
import java.util.*;
import java.util.stream.Collectors;

public class RideRepository {
    private final File storageFile;
    private Map<String, Ride> ridesById;

    public RideRepository(String pathToFile) {
        this.storageFile = new File(pathToFile);
        load();
    }

    @SuppressWarnings("unchecked")
    private void load() {
        if (!storageFile.exists()) {
            ridesById = new HashMap<>();
            return;
        }
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(storageFile))) {
            Object o = ois.readObject();
            ridesById = (Map<String, Ride>) o;
        } catch (Exception e) {
            System.err.println("Não foi possível carregar armazenamento de corridas. Inicializando vazio... (" + e.getMessage() + ")");
            ridesById = new HashMap<>();
        }
    }

    private void save() throws IOException {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(storageFile))) {
            oos.writeObject(ridesById);
        }
    }

    public synchronized void add(Ride ride) throws IOException {
        ridesById.put(ride.getId(), ride);
        save();
    }

    public Ride findById(String id) {
        return ridesById.get(id);
    }

    public Collection<Ride> findAll() {
        return Collections.unmodifiableCollection(ridesById.values());
    }

    public Collection<Ride> findByPassengerEmail(String passengerEmail) {
        return ridesById.values().stream()
                .filter(ride -> ride.getPassengerEmail().equalsIgnoreCase(passengerEmail))
                .collect(Collectors.toList());
    }

    public Collection<Ride> findByStatus(model.Ride.RideStatus status) {
        return ridesById.values().stream()
                .filter(ride -> ride.getStatus() == status)
                .collect(Collectors.toList());
    }

    public synchronized void update(Ride ride) throws IOException {
        if (ridesById.containsKey(ride.getId())) {
            ridesById.put(ride.getId(), ride);
            save();
        }
    }

    public boolean existsById(String id) {
        return ridesById.containsKey(id);
    }

    public int count() {
        return ridesById.size();
    }
}
