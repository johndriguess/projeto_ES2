package service;

import model.RideHistory;
import model.Ride;
import model.User;
import model.Driver;
import repo.RideHistoryRepository;
import repo.UserRepository;
import util.ValidationException;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

public class RideHistoryService {
    private final RideHistoryRepository historyRepo;
    private final UserRepository userRepo;

    public RideHistoryService(RideHistoryRepository historyRepo, UserRepository userRepo) {
        this.historyRepo = historyRepo;
        this.userRepo = userRepo;
    }

    public void addRideToHistory(Ride ride, double price, String paymentMethod) throws ValidationException, IOException {
        if (ride == null) {
            throw new ValidationException("Corrida não pode ser nula.");
        }

        String driverName = "Não informado";
        if (ride.getDriverId() != null) {
            User driver = userRepo.findById(ride.getDriverId());
            if (driver != null) {
                driverName = driver.getName();
            }
        }

        RideHistory history = new RideHistory(ride, driverName, price, paymentMethod);
        historyRepo.add(history);
    }

    public List<RideHistory> getHistoryByPassenger(String passengerEmail) throws ValidationException {
        if (passengerEmail == null || passengerEmail.trim().isEmpty()) {
            throw new ValidationException("Email do passageiro é obrigatório.");
        }

        if (!userRepo.existsByEmail(passengerEmail)) {
            throw new ValidationException("Passageiro não encontrado.");
        }

        return historyRepo.findByPassengerEmail(passengerEmail);
    }

    public List<RideHistory> getHistoryByDriver(String driverEmail) throws ValidationException {
        if (driverEmail == null || driverEmail.trim().isEmpty()) {
            throw new ValidationException("Email do motorista é obrigatório.");
        }

        User driver = userRepo.findByEmail(driverEmail);
        if (driver == null || !(driver instanceof Driver)) {
            throw new ValidationException("Motorista não encontrado.");
        }

        return historyRepo.findByDriverId(driver.getId());
    }

    public List<RideHistory> getHistoryByCategory(String category) throws ValidationException {
        if (category == null || category.trim().isEmpty()) {
            throw new ValidationException("Categoria é obrigatória.");
        }

        return historyRepo.findByVehicleCategory(category);
    }

    public List<RideHistory> getHistoryByPassengerAndCategory(String passengerEmail, String category) throws ValidationException {
        if (passengerEmail == null || passengerEmail.trim().isEmpty()) {
            throw new ValidationException("Email do passageiro é obrigatório.");
        }

        if (category == null || category.trim().isEmpty()) {
            throw new ValidationException("Categoria é obrigatória.");
        }

        if (!userRepo.existsByEmail(passengerEmail)) {
            throw new ValidationException("Passageiro não encontrado.");
        }

        return historyRepo.findByPassengerAndCategory(passengerEmail, category);
    }

    public List<RideHistory> getHistoryByDateRange(LocalDateTime startDate, LocalDateTime endDate) throws ValidationException {
        if (startDate == null || endDate == null) {
            throw new ValidationException("Datas de início e fim são obrigatórias.");
        }

        if (startDate.isAfter(endDate)) {
            throw new ValidationException("Data de início deve ser anterior à data de fim.");
        }

        return historyRepo.findByDateRange(startDate, endDate);
    }

    public List<RideHistory> getHistoryByPassengerAndDateRange(String passengerEmail, LocalDateTime startDate, LocalDateTime endDate) throws ValidationException {
        if (passengerEmail == null || passengerEmail.trim().isEmpty()) {
            throw new ValidationException("Email do passageiro é obrigatório.");
        }

        if (startDate == null || endDate == null) {
            throw new ValidationException("Datas de início e fim são obrigatórias.");
        }

        if (startDate.isAfter(endDate)) {
            throw new ValidationException("Data de início deve ser anterior à data de fim.");
        }

        if (!userRepo.existsByEmail(passengerEmail)) {
            throw new ValidationException("Passageiro não encontrado.");
        }

        return historyRepo.findByPassengerAndDateRange(passengerEmail, startDate, endDate);
    }

    public List<String> getAvailableCategories() {
        return historyRepo.getAvailableCategories();
    }

    public Map<String, Long> getCategoryStatistics() {
        return historyRepo.getCategoryStatistics();
    }

    public RideHistory getHistoryById(String historyId) throws ValidationException {
        if (historyId == null || historyId.trim().isEmpty()) {
            throw new ValidationException("ID do histórico é obrigatório.");
        }

        RideHistory history = historyRepo.findById(historyId);
        if (history == null) {
            throw new ValidationException("Histórico não encontrado.");
        }

        return history;
    }

    public void updateHistoryRating(String historyId, int rating, boolean isPassengerRating) throws ValidationException, IOException {
        if (rating < 1 || rating > 5) {
            throw new ValidationException("Avaliação deve estar entre 1 e 5.");
        }

        RideHistory history = getHistoryById(historyId);
        
        if (isPassengerRating) {
            history.setPassengerRating(rating);
        } else {
            history.setDriverRating(rating);
        }

        historyRepo.update(history);
    }

    public void addNotesToHistory(String historyId, String notes) throws ValidationException, IOException {
        RideHistory history = getHistoryById(historyId);
        history.setNotes(notes);
        historyRepo.update(history);
    }

    public String formatHistoryList(List<RideHistory> historyList) {
        if (historyList.isEmpty()) {
            return "Nenhum histórico encontrado.";
        }

        StringBuilder sb = new StringBuilder();
        sb.append("=== HISTÓRICO DE CORRIDAS ===\n");
        sb.append("Total de registros: ").append(historyList.size()).append("\n\n");

        for (int i = 0; i < historyList.size(); i++) {
            RideHistory history = historyList.get(i);
            sb.append(String.format("%d. %s\n", i + 1, history.toString()));
        }

        sb.append("=============================");
        return sb.toString();
    }

    public String formatCategoryStatistics() {
        Map<String, Long> stats = getCategoryStatistics();
        if (stats.isEmpty()) {
            return "Nenhuma estatística disponível.";
        }

        StringBuilder sb = new StringBuilder();
        sb.append("=== ESTATÍSTICAS POR CATEGORIA ===\n");
        
        long total = stats.values().stream().mapToLong(Long::longValue).sum();
        
        for (Map.Entry<String, Long> entry : stats.entrySet()) {
            String category = entry.getKey();
            Long count = entry.getValue();
            double percentage = (count.doubleValue() / total) * 100;
            sb.append(String.format("%s: %d corridas (%.1f%%)\n", category, count, percentage));
        }
        
        sb.append(String.format("Total: %d corridas\n", total));
        sb.append("================================");
        return sb.toString();
    }
}
