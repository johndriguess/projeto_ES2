package service;

import repo.RideRepository;
import repo.UserRepository;
import model.Ride;
import model.User;
import model.Driver;
import model.Passenger;
import model.Location;
import model.PricingInfo;
import model.PaymentMethod;
import model.Receipt; 
import util.ValidationException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

public class RideService {
    private final RideRepository rideRepo;
    private final UserRepository userRepo;
    private final PricingService pricingService;
    private final DigitalReceiptService digitalReceiptService; 

    public RideService(RideRepository rideRepo, UserRepository userRepo, PricingService pricingService) {
        this.rideRepo = rideRepo;
        this.userRepo = userRepo;
        this.pricingService = pricingService;
        this.digitalReceiptService = new DigitalReceiptService(); 
    }

    public Ride createRideRequest(String passengerEmail, String originAddr, String destAddr, String categoryName, PaymentMethod paymentMethod) throws ValidationException, IOException {
        Passenger p = (Passenger) userRepo.findByEmail(passengerEmail);
        if (p == null) {
            throw new ValidationException("Passageiro não encontrado.");
        }
        Location originLoc = new Location(originAddr);
        Location destLoc = new Location(destAddr);

        Ride ride = new Ride(p.getId(), passengerEmail, originLoc, destLoc);
        ride.setPaymentMethod(paymentMethod);
        ride.setVehicleCategory(categoryName); 

        rideRepo.add(ride);
        return ride;
    }

    public List<Ride> getRidesByPassenger(String passengerEmail) throws ValidationException {
        if (!userRepo.existsByEmail(passengerEmail)) {
            throw new ValidationException("Passageiro não encontrado.");
        }
        return (List<Ride>) rideRepo.findByPassengerEmail(passengerEmail);
    }

    public Ride getRideById(String rideId) throws ValidationException {
        Ride ride = rideRepo.findById(rideId);
        if (ride == null) {
            throw new ValidationException("Corrida não encontrada.");
        }
        return ride;
    }

    public List<PricingInfo> calculateAllPricing(String originAddr, String destAddr) throws ValidationException {
        return pricingService.calculateAllPricing(originAddr, destAddr);
    }

    public List<Ride> getPendingRidesForDriver(String driverEmail) throws ValidationException {
        Driver driver = (Driver) userRepo.findByEmail(driverEmail);
        if (driver == null) {
            throw new ValidationException("Motorista não encontrado.");
        }
        if (driver.getVehicle() == null) {
            throw new ValidationException("Você não tem veículo cadastrado.");
        }
        
        String driverCategory = driver.getVehicle().getCategory();
        
        if (driverCategory == null || driverCategory.equalsIgnoreCase("UNASSIGNED")) {
            throw new ValidationException("Seu veículo não possui uma categoria válida ('" + driverCategory + "').");
        }
        
        return rideRepo.findAll().stream()
                .filter(ride -> ride.getStatus() == Ride.RideStatus.SOLICITADA)
                .filter(ride -> ride.getVehicleCategory() != null && ride.getVehicleCategory().equalsIgnoreCase(driverCategory))
                .collect(Collectors.toList());
    }

    public void acceptRide(String rideId, String driverEmail) throws ValidationException, IOException {
        Ride ride = getRideById(rideId);
        Driver driver = (Driver) userRepo.findByEmail(driverEmail);

        if (ride.getStatus() != Ride.RideStatus.SOLICITADA) {
            throw new ValidationException("Esta corrida não está mais disponível.");
        }
        if (driver == null) {
            throw new ValidationException("Motorista não encontrado.");
        }

        ride.setDriverId(driver.getId());
        ride.setStatus(Ride.RideStatus.ACEITA);
        ride.setDriverCurrentLocation(driver.getCurrentLocation()); 

        rideRepo.update(ride);
        System.out.println("Corrida " + rideId + " aceita por " + driver.getName());
    }

    public void refuseRide(String rideId, String driverEmail) {
        System.out.println("Motorista " + driverEmail + " recusou a corrida " + rideId);
    }

    public void emitReceiptForRide(String rideId, String paymentMethod) throws IOException, ValidationException {
        Ride ride = getRideById(rideId);
        if (ride == null) {
            throw new ValidationException("Corrida não encontrada.");
        }
        
        ride.setStatus(Ride.RideStatus.FINALIZADA);
        rideRepo.update(ride);

        Passenger passenger = (Passenger) userRepo.findById(ride.getPassengerId());
        Driver driver = (Driver) userRepo.findById(ride.getDriverId());
        
        List<PricingInfo> pricingList = pricingService.calculateAllPricing(ride.getOrigin().getAddress(), ride.getDestination().getAddress());
        PricingInfo ridePricing = null;
        for (PricingInfo p : pricingList) {
            if (p.getCategory().equalsIgnoreCase(ride.getVehicleCategory())) {
                ridePricing = p;
                break;
            }
        }
        
        if (ridePricing == null && !pricingList.isEmpty()) {
            ridePricing = pricingList.get(0); 
        } else if (ridePricing == null) {
            throw new ValidationException("Não foi possível calcular o preço para o recibo.");
        }

        Receipt receipt = digitalReceiptService.generateReceipt(ride, passenger, driver, ridePricing, paymentMethod);
        
        digitalReceiptService.sendReceiptToPassenger(receipt);
    }
}