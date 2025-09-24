package service;

import model.Ride;
import model.Location;
import model.Passenger;
import model.User;
import model.PricingInfo;
import repo.RideRepository;
import repo.UserRepository;
import util.ValidationException;
import java.io.IOException;
import java.util.List;

public class RideService {
    private final RideRepository rideRepo;
    private final UserRepository userRepo;
    private final PricingService pricingService;

    public RideService(RideRepository rideRepo, UserRepository userRepo) {
        this.rideRepo = rideRepo;
        this.userRepo = userRepo;
        this.pricingService = new PricingService();
    }

    public Ride createRideRequest(String passengerEmail, String originAddress, String destinationAddress, String vehicleCategory)
            throws ValidationException, IOException {

        User user = userRepo.findByEmail(passengerEmail);
        if (user == null) {
            throw new ValidationException("Usuário não encontrado.");
        }

        if (!(user instanceof Passenger)) {
            throw new ValidationException("Apenas passageiros podem solicitar corridas.");
        }

        validateRideRequest(originAddress, destinationAddress);

        Location origin = new Location(originAddress);
        Location destination = new Location(destinationAddress);

        Ride ride = new Ride(user.getId(), passengerEmail, origin, destination);
        ride.setVehicleCategory(vehicleCategory); // Set the category

        rideRepo.add(ride);

        // Notify drivers
        notifyDriversByCategory(ride);

        return ride;
    }

    public void notifyDriversByCategory(Ride ride) {
        System.out.println("Notificando motoristas da categoria " + ride.getVehicleCategory() + "...");
        userRepo.findAll().stream()
                .filter(u -> u instanceof model.Driver)
                .map(u -> (model.Driver) u)
                .filter(d -> d.getVehicle() != null && d.getVehicle().getCategory().getName().equalsIgnoreCase(ride.getVehicleCategory()))
                .forEach(d -> System.out.println("Notificando motorista " + d.getName() + " para a corrida de " + ride.getOrigin().getAddress()));
    }

    private void validateRideRequest(String originAddress, String destinationAddress) throws ValidationException {
        if (originAddress == null || originAddress.trim().isEmpty()) {
            throw new ValidationException("Endereço de origem é obrigatório.");
        }
        
        if (destinationAddress == null || destinationAddress.trim().isEmpty()) {
            throw new ValidationException("Endereço de destino é obrigatório.");
        }
        
        if (originAddress.trim().equalsIgnoreCase(destinationAddress.trim())) {
            throw new ValidationException("Origem e destino não podem ser iguais.");
        }
        
        if (originAddress.trim().length() < 5) {
            throw new ValidationException("Endereço de origem deve ter pelo menos 5 caracteres.");
        }
        
        if (destinationAddress.trim().length() < 5) {
            throw new ValidationException("Endereço de destino deve ter pelo menos 5 caracteres.");
        }
    }

    public Ride getRideById(String rideId) throws ValidationException {
        if (rideId == null || rideId.trim().isEmpty()) {
            throw new ValidationException("ID da corrida é obrigatório.");
        }
        
        Ride ride = rideRepo.findById(rideId);
        if (ride == null) {
            throw new ValidationException("Corrida não encontrada.");
        }
        
        return ride;
    }

    public java.util.Collection<Ride> getRidesByPassenger(String passengerEmail) throws ValidationException {
        if (passengerEmail == null || passengerEmail.trim().isEmpty()) {
            throw new ValidationException("Email do passageiro é obrigatório.");
        }
        
        return rideRepo.findByPassengerEmail(passengerEmail);
    }

    public java.util.Collection<Ride> getRidesByStatus(Ride.RideStatus status) throws ValidationException {
        if (status == null) {
            throw new ValidationException("Status da corrida é obrigatório.");
        }
        
        return rideRepo.findByStatus(status);
    }

    public void updateRideStatus(String rideId, Ride.RideStatus newStatus) throws ValidationException, IOException {
        Ride ride = getRideById(rideId);
        ride.setStatus(newStatus);
        rideRepo.update(ride);
    }

    public void setVehicleCategory(String rideId, String vehicleCategory) throws ValidationException, IOException {
        if (vehicleCategory == null || vehicleCategory.trim().isEmpty()) {
            throw new ValidationException("Categoria do veículo é obrigatória.");
        }
        
        Ride ride = getRideById(rideId);
        ride.setVehicleCategory(vehicleCategory);
        rideRepo.update(ride);
    }

    public PricingInfo calculatePricing(String origin, String destination, String category) 
            throws ValidationException {
        return pricingService.calculatePricing(origin, destination, category);
    }

    public List<PricingInfo> calculateAllPricing(String origin, String destination) 
            throws ValidationException {
        return pricingService.calculateAllPricing(origin, destination);
    }

    public double calculateDistance(String origin, String destination) {
        return pricingService.calculateDistance(origin, destination);
    }

    public int calculateEstimatedTime(String origin, String destination) {
        return pricingService.calculateEstimatedTime(origin, destination);
    }

    public java.util.Set<String> getAvailableCategories() {
        return pricingService.getAvailableCategories();
    }
}
