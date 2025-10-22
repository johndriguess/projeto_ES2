package repo;

import model.RideHistory;
import java.io.*;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

public class RideHistoryRepository {
    private final File storageFile;
    private Map<String, RideHistory> historyById;
    private Map<String, List<RideHistory>> historyByPassenger;
    private Map<String, List<RideHistory>> historyByDriver;

    public RideHistoryRepository(String pathToFile) {
        this.storageFile = new File(pathToFile);
        this.historyByPassenger = new HashMap<>();
        this.historyByDriver = new HashMap<>();
        load();
    }

    @SuppressWarnings("unchecked")
    private void load() {
        if (!storageFile.exists()) {
            historyById = new HashMap<>();
            return;
        }
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(storageFile))) {
            Object o = ois.readObject();
            historyById = (Map<String, RideHistory>) o;
            
            // Reconstruir índices
            historyByPassenger.clear();
            historyByDriver.clear();
            for (RideHistory history : historyById.values()) {
                addToPassengerIndex(history);
                addToDriverIndex(history);
            }
        } catch (Exception e) {
            System.err.println("Não foi possível carregar histórico de corridas. Inicializando vazio... (" + e.getMessage() + ")");
            historyById = new HashMap<>();
        }
    }

    private void save() throws IOException {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(storageFile))) {
            oos.writeObject(historyById);
        }
    }

    private void addToPassengerIndex(RideHistory history) {
        String passengerEmail = history.getPassengerEmail();
        historyByPassenger.computeIfAbsent(passengerEmail, k -> new ArrayList<>()).add(history);
    }

    private void addToDriverIndex(RideHistory history) {
        String driverId = history.getDriverId();
        if (driverId != null) {
            historyByDriver.computeIfAbsent(driverId, k -> new ArrayList<>()).add(history);
        }
    }

    public synchronized void add(RideHistory history) throws IOException {
        historyById.put(history.getId(), history);
        addToPassengerIndex(history);
        addToDriverIndex(history);
        save();
    }

    public RideHistory findById(String id) {
        return historyById.get(id);
    }

    public Collection<RideHistory> findAll() {
        return Collections.unmodifiableCollection(historyById.values());
    }

    public List<RideHistory> findByPassengerEmail(String passengerEmail) {
        return historyByPassenger.getOrDefault(passengerEmail, new ArrayList<>())
                .stream()
                .sorted(Comparator.comparing(RideHistory::getRequestTime).reversed())
                .collect(Collectors.toList());
    }

    public List<RideHistory> findByDriverId(String driverId) {
        return historyByDriver.getOrDefault(driverId, new ArrayList<>())
                .stream()
                .sorted(Comparator.comparing(RideHistory::getRequestTime).reversed())
                .collect(Collectors.toList());
    }

    public List<RideHistory> findByVehicleCategory(String category) {
        return historyById.values().stream()
                .filter(history -> history.getVehicleCategory() != null && 
                        history.getVehicleCategory().equalsIgnoreCase(category))
                .sorted(Comparator.comparing(RideHistory::getRequestTime).reversed())
                .collect(Collectors.toList());
    }

    public List<RideHistory> findByPassengerAndCategory(String passengerEmail, String category) {
        return historyByPassenger.getOrDefault(passengerEmail, new ArrayList<>())
                .stream()
                .filter(history -> history.getVehicleCategory() != null && 
                        history.getVehicleCategory().equalsIgnoreCase(category))
                .sorted(Comparator.comparing(RideHistory::getRequestTime).reversed())
                .collect(Collectors.toList());
    }

    public List<RideHistory> findByDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        return historyById.values().stream()
                .filter(history -> history.getRequestTime().isAfter(startDate) && 
                        history.getRequestTime().isBefore(endDate))
                .sorted(Comparator.comparing(RideHistory::getRequestTime).reversed())
                .collect(Collectors.toList());
    }

    public List<RideHistory> findByPassengerAndDateRange(String passengerEmail, LocalDateTime startDate, LocalDateTime endDate) {
        return historyByPassenger.getOrDefault(passengerEmail, new ArrayList<>())
                .stream()
                .filter(history -> history.getRequestTime().isAfter(startDate) && 
                        history.getRequestTime().isBefore(endDate))
                .sorted(Comparator.comparing(RideHistory::getRequestTime).reversed())
                .collect(Collectors.toList());
    }

    public synchronized void update(RideHistory history) throws IOException {
        if (historyById.containsKey(history.getId())) {
            historyById.put(history.getId(), history);
            save();
        }
    }

    public boolean existsById(String id) {
        return historyById.containsKey(id);
    }

    public int count() {
        return historyById.size();
    }

    public List<String> getAvailableCategories() {
        return historyById.values().stream()
                .map(RideHistory::getVehicleCategory)
                .filter(Objects::nonNull)
                .distinct()
                .sorted()
                .collect(Collectors.toList());
    }

    public Map<String, Long> getCategoryStatistics() {
        return historyById.values().stream()
                .filter(history -> history.getVehicleCategory() != null)
                .collect(Collectors.groupingBy(
                        RideHistory::getVehicleCategory,
                        Collectors.counting()
                ));
    }
}
