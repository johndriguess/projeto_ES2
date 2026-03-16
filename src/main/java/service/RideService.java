package service;

import repo.RideRepository;
import repo.UserRepository;
import repo.RideHistoryRepository;
import model.Ride;
import model.Driver;
import model.Passenger;
import model.Location;
import model.PricingInfo;
import model.PaymentMethod;
import model.Receipt;
import model.VehicleCategory;
import util.ValidationException;
import util.DistanceCalculator;
import service.PaymentService;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;
import java.util.Comparator;
import java.util.Collections;

public class RideService {
    private final RideRepository rideRepo;
    private final UserRepository userRepo;
    private final PricingService pricingService;
    private final DigitalReceiptService digitalReceiptService;
    private final PaymentService paymentService;
    private RideHistoryRepository historyRepo;
    private final RouteService routeService;

    public RideService(RideRepository rideRepo, UserRepository userRepo, PricingService pricingService) {
        this.rideRepo = rideRepo;
        this.userRepo = userRepo;
        this.pricingService = pricingService;
        this.digitalReceiptService = new DigitalReceiptService();
        this.paymentService = new PaymentService();
        this.routeService = new RouteService();
    }

    public void setHistoryRepository(RideHistoryRepository historyRepo) {
        this.historyRepo = historyRepo;
    }

    public Ride createRideRequest(String passengerEmail, String originAddr, String destAddr, String categoryName,
            PaymentMethod paymentMethod) throws ValidationException, IOException {
        Passenger p = (Passenger) userRepo.findByEmail(passengerEmail);
        if (p == null) {
            throw new ValidationException("Passageiro não encontrado.");
        }
        Location originLoc = new Location(originAddr);
        Location destLoc = new Location(destAddr);

        Ride ride = new Ride(p.getId(), passengerEmail, originLoc, destLoc);
        ride.setPaymentMethod(paymentMethod);
        VehicleCategory categoryEnum = resolveCategory(categoryName);
        if (categoryEnum == null) {
            throw new ValidationException("Categoria de veículo inválida: " + categoryName);
        }
        ride.setVehicleCategory(categoryEnum.name());

        Driver assignedDriver = findAndAssignBestDriver(ride, Collections.emptyList());

        if (assignedDriver != null) {
            ride.setDriverId(assignedDriver.getId());
            ride.setStatus(Ride.RideStatus.AGUARDANDO_ACEITE_MOTORISTA);
            ride.setDriverCurrentLocation(assignedDriver.getCurrentLocation());
            System.out.println("Motorista encontrado e atribuído: " + assignedDriver.getName());
        } else {
            System.out.println("Nenhum motorista disponível. A corrida ficará solicitada.");
        }

        // generate initial route / eta (may be partial if driver not yet defined)
        routeService.generateRoute(ride);

        rideRepo.add(ride);
        return ride;
    }

    private Driver findAndAssignBestDriver(Ride ride, List<String> excludedDriverIds) {
        final VehicleCategory categoryEnum;
        try {
            categoryEnum = VehicleCategory.valueOf(ride.getVehicleCategory());
        } catch (IllegalArgumentException e) {
            System.err.println("Categoria não encontrada no Enum: " + ride.getVehicleCategory());
            return null;
        }

        final boolean isPremium = categoryEnum.isPremium();
        final Location rideOrigin = ride.getOrigin();

        List<Driver> availableDrivers = userRepo.findAll().stream()
                .filter(u -> u instanceof Driver)
                .map(u -> (Driver) u)
                .filter(Driver::isAvailable)
                .filter(d -> excludedDriverIds == null || !excludedDriverIds.contains(d.getId()))
                .filter(d -> d.getVehicle() != null && d.getVehicle().getCategory() != null
                        && (d.getVehicle().getCategory().equals(categoryEnum.name()) ||
                                d.getVehicle().getCategory().equalsIgnoreCase(categoryEnum.getDisplayName())))
                .collect(Collectors.toList());

        if (availableDrivers.isEmpty()) {
            return null;
        }

        Comparator<Driver> comparator;

        if (isPremium) {
            comparator = Comparator
                    .comparing(Driver::getAverageRating, Comparator.reverseOrder())
                    .thenComparing(d -> DistanceCalculator.calculateDistance(d.getCurrentLocation().getAddress(),
                            rideOrigin.getAddress()));
        } else {
            comparator = Comparator
                    .comparingDouble(d -> DistanceCalculator.calculateDistance(d.getCurrentLocation().getAddress(),
                            rideOrigin.getAddress()));
        }

        availableDrivers.sort(comparator);

        Driver bestDriver = availableDrivers.get(0);
        bestDriver.setAvailable(false);
        try {
            userRepo.update(bestDriver);
        } catch (IOException e) {
            System.err.println("Erro ao atualizar status do motorista: " + e.getMessage());
        }

        return bestDriver;
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

        return rideRepo.findAll().stream()
                .filter(ride -> ride.getStatus() == Ride.RideStatus.AGUARDANDO_ACEITE_MOTORISTA)
                .filter(ride -> driver.getId().equals(ride.getDriverId()))
                .collect(Collectors.toList());
    }

    public List<Ride> getAvailableRidesForDriver(String driverEmail) throws ValidationException {
        Driver driver = (Driver) userRepo.findByEmail(driverEmail);
        if (driver == null) {
            throw new ValidationException("Motorista não encontrado.");
        }

        return rideRepo.findAll().stream()
                .filter(ride -> ride.getStatus() == Ride.RideStatus.AGUARDANDO_ACEITE_MOTORISTA)
                .filter(ride -> driver.getId().equals(ride.getDriverId()))
                .collect(Collectors.toList());
    }

    public void acceptRide(String rideId, String driverEmail) throws ValidationException, IOException {
        Ride ride = getRideById(rideId);
        Driver driver = (Driver) userRepo.findByEmail(driverEmail);

        if (ride.getStatus() != Ride.RideStatus.AGUARDANDO_ACEITE_MOTORISTA) {
            throw new ValidationException("Esta corrida não está mais disponível.");
        }
        if (driver == null) {
            throw new ValidationException("Motorista não encontrado.");
        }

        if (ride.getDriverId() == null || !ride.getDriverId().equals(driver.getId())) {
            throw new ValidationException("Esta corrida não está atribuída a você.");
        }

        ride.setStatus(Ride.RideStatus.ACEITA);
        ride.setDriverCurrentLocation(driver.getCurrentLocation());

        // recalc route now that driver location is known
        routeService.generateRoute(ride);

        rideRepo.update(ride);
        System.out.println("Corrida " + rideId + " aceita por " + driver.getName());
    }

    public void refuseRide(String rideId, String driverEmail) throws ValidationException {
        Ride ride = getRideById(rideId);
        Driver driver = (Driver) userRepo.findByEmail(driverEmail);

        if (driver == null) {
            throw new ValidationException("Motorista não encontrado.");
        }
        if (ride.getStatus() != Ride.RideStatus.AGUARDANDO_ACEITE_MOTORISTA) {
            throw new ValidationException("Esta corrida não está aguardando aceite do motorista.");
        }
        if (ride.getDriverId() == null || !ride.getDriverId().equals(driver.getId())) {
            throw new ValidationException("Esta corrida não está atribuída a você.");
        }

        ride.addRefusedDriverId(driver.getId());
        driver.setAvailable(true);
        try {
            userRepo.update(driver);
        } catch (IOException e) {
            throw new ValidationException("Não foi possível atualizar o status do motorista.");
        }

        Driver reassignedDriver = findAndAssignBestDriver(ride, ride.getRefusedDriverIds());
        if (reassignedDriver != null) {
            ride.setDriverId(reassignedDriver.getId());
            ride.setStatus(Ride.RideStatus.AGUARDANDO_ACEITE_MOTORISTA);
            ride.setDriverCurrentLocation(reassignedDriver.getCurrentLocation());
            try {
                routeService.generateRoute(ride);
                rideRepo.update(ride);
            } catch (IOException e) {
                throw new ValidationException("Erro ao atualizar a corrida após recusa.");
            }
            System.out.println("Corrida " + rideId + " recusada por " + driver.getName()
                    + ". Novo motorista atribuído: " + reassignedDriver.getName());
            return;
        }

        ride.setDriverId(null);
        ride.setDriverCurrentLocation(null);
        ride.setStatus(Ride.RideStatus.CANCELADA);
        try {
            routeService.generateRoute(ride);
            rideRepo.update(ride);
        } catch (IOException e) {
            throw new ValidationException("Erro ao cancelar a corrida após recusa.");
        }
        System.out.println("Motorista " + driverEmail + " recusou a corrida " + rideId
                + ". Nenhum outro motorista disponível; corrida cancelada.");
    }

    /**
     * Recalculates the route information for a ride and persists the changes.
     * This can be used when the ride was created earlier without a driver or when
     * the driver's location has changed.
     */
    public void generateRouteForRide(String rideId) throws ValidationException, IOException {
        Ride ride = getRideById(rideId);
        routeService.generateRoute(ride);
        rideRepo.update(ride);
    }

    public boolean processRidePayment(String rideId) throws ValidationException {
        Ride ride = getRideById(rideId);
        if (ride == null) {
            throw new ValidationException("Corrida não encontrada.");
        }

        if (ride.getStatus() != Ride.RideStatus.ACEITA) {
            throw new ValidationException("Corrida deve estar aceita para processar pagamento.");
        }

        // Calcular preço da corrida
        List<PricingInfo> pricingList = pricingService.calculateAllPricing(ride.getOrigin().getAddress(),
                ride.getDestination().getAddress());
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
            throw new ValidationException("Não foi possível calcular o preço da corrida.");
        }

        // Processar pagamento
        boolean paymentSuccess = paymentService.processPayment(ridePricing.getTotalPrice(), ride.getPaymentMethod());

        if (paymentSuccess) {
            System.out.println("Pagamento processado com sucesso!");
            return true;
        } else {
            System.out.println("Falha no processamento do pagamento.");
            return false;
        }
    }

    public void emitReceiptForRide(String rideId, String paymentMethod) throws IOException, ValidationException {
        Ride ride = getRideById(rideId);
        if (ride == null) {
            throw new ValidationException("Corrida não encontrada.");
        }

        Driver driver = (Driver) userRepo.findById(ride.getDriverId());
        if (driver != null) {
            driver.setAvailable(true);
            userRepo.update(driver);
        }

        ride.setStatus(Ride.RideStatus.FINALIZADA);
        rideRepo.update(ride);

        Passenger passenger = (Passenger) userRepo.findById(ride.getPassengerId());

        List<PricingInfo> pricingList = pricingService.calculateAllPricing(ride.getOrigin().getAddress(),
                ride.getDestination().getAddress());
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

        // Adicionar ao histórico de corridas
        if (historyRepo != null) {
            try {
                model.RideHistory history = new model.RideHistory(ride,
                        driver != null ? driver.getName() : "Não informado",
                        ridePricing.getTotalPrice(), paymentMethod);
                historyRepo.add(history);
            } catch (IOException e) {
                System.err.println("Erro ao adicionar corrida ao histórico: " + e.getMessage());
            }
        }
    }

    private VehicleCategory resolveCategory(String input) {
        if (input == null)
            return null;
        for (VehicleCategory vc : VehicleCategory.values()) {
            if (vc.name().equalsIgnoreCase(input))
                return vc;
            if (vc.getDisplayName().equalsIgnoreCase(input))
                return vc;
        }
        // try normalized fallback
        String norm = input.replace(" ", "_").replace("-", "_").replace("Uber", "UBER").toUpperCase();
        try {
            return VehicleCategory.valueOf(norm);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}