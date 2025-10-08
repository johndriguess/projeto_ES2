package service;

import model.Ride;
import model.Location;
import model.Passenger;
import model.User;
import model.PricingInfo;
import model.Driver;
import repo.RideRepository;
import repo.UserRepository;
import util.ValidationException;
import util.DistanceCalculator;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;
import java.util.Comparator;
import java.util.Collection;
import java.util.Set;
import java.util.Arrays;
import java.time.format.DateTimeFormatter;
import java.time.LocalDateTime;

public class RideService {
    private final RideRepository rideRepo;
    private final UserRepository userRepo;
    private final PricingService pricingService;
    private final PaymentService paymentService; // RF13

    public RideService(RideRepository rideRepo, UserRepository userRepo) {
        this.rideRepo = rideRepo;
        this.userRepo = userRepo;
        this.pricingService = new PricingService();
        this.paymentService = new PaymentService(); // RF13
    }

    // Construtor para receber a instância de PricingService
    public RideService(RideRepository rideRepo, UserRepository userRepo, PricingService pricingService) {
        this.rideRepo = rideRepo;
        this.userRepo = userRepo;
        this.pricingService = pricingService;
        this.paymentService = new PaymentService(); // RF13
    }


    public Ride createRideRequest(String passengerEmail, String originAddress, String destinationAddress, String vehicleCategory, model.PaymentMethod paymentMethod)
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
        ride.setVehicleCategory(vehicleCategory);
        ride.setPaymentMethod(paymentMethod); // RF13

        rideRepo.add(ride);

        assignClosestDriver(ride);

        return ride;
    }
    
    // RF13
    public void completeRide(String rideId) throws ValidationException, IOException {
        Ride ride = getRideById(rideId);

        if (ride.getStatus() != Ride.RideStatus.ACEITA && ride.getStatus() != Ride.RideStatus.EM_ANDAMENTO) {
            throw new ValidationException("A corrida não pode ser finalizada neste status.");
        }

        // Calcula o preço final
        PricingInfo pricing = calculatePricing(ride.getOrigin().getAddress(), ride.getDestination().getAddress(), ride.getVehicleCategory());
        double finalPrice = pricing.getTimePrice();

        // Processa o pagamento
        boolean paymentSuccess = paymentService.processPayment(finalPrice, ride.getPaymentMethod());

        if (paymentSuccess) {
            ride.setStatus(Ride.RideStatus.FINALIZADA);
            rideRepo.update(ride);
            System.out.println("Corrida finalizada com sucesso! Pagamento processado.");
            // Emite o recibo automaticamente
            emitReceiptForRide(rideId, ride.getPaymentMethod().getDisplayName());
        } else {
            System.out.println("Falha no pagamento. A corrida não foi finalizada. Tente novamente.");
        }
    }

    public void notifyDriversByCategory(Ride ride) {
        System.out.println("Notificando motoristas da categoria " + ride.getVehicleCategory() + "...");
        userRepo.findAll().stream()
                .filter(u -> u instanceof model.Driver)
                .map(u -> (model.Driver) u)
                .filter(d -> !d.getVehicles().isEmpty() && d.getVehicles().get(0).getCategory().equalsIgnoreCase(ride.getVehicleCategory()))
                .forEach(d -> System.out.println("Notificando motorista " + d.getName() + " para a corrida de " + ride.getOrigin().getAddress()));
    }

    public void assignClosestDriver(Ride ride) throws ValidationException, IOException {
        List<Driver> eligibleDrivers = userRepo.findAll().stream()
                .filter(u -> u instanceof Driver)
                .map(u -> (Driver) u)
                .filter(d -> !d.getVehicles().isEmpty() && d.getVehicles().get(0).getCategory().equalsIgnoreCase(ride.getVehicleCategory()))
                .collect(Collectors.toList());

        if (eligibleDrivers.isEmpty()) {
            System.out.println("Nenhum motorista disponível na categoria " + ride.getVehicleCategory());
            return;
        }

        Driver closestDriver = eligibleDrivers.stream()
                .min(Comparator.comparingDouble(d -> DistanceCalculator.calculateDistance(ride.getOrigin().getAddress(), d.getVehicles().get(0).getPlate())))
                .orElse(null);

        if (closestDriver != null) {
            ride.setDriverId(closestDriver.getId());
            ride.setStatus(Ride.RideStatus.ACEITA);
            rideRepo.update(ride);
            System.out.println("Corrida atribuída ao motorista: " + closestDriver.getName());
        } else {
            System.out.println("Não foi possível encontrar um motorista elegível.");
        }
    }

    public void updateDriverLocation(String rideId, String newAddress) throws ValidationException, IOException {
        if (newAddress == null || newAddress.trim().isEmpty()) {
            throw new ValidationException("O novo endereço de localização do motorista é obrigatório.");
        }

        Ride ride = getRideById(rideId);

        if (ride.getStatus() == Ride.RideStatus.FINALIZADA || ride.getStatus() == Ride.RideStatus.CANCELADA) {
            throw new ValidationException("Não é possível atualizar a localização. A corrida já foi finalizada ou cancelada.");
        }

        Location newLocation = new Location(newAddress);
        ride.setDriverCurrentLocation(newLocation);

        calculateAndUpdateETA(ride);
        generateOptimizedRoute(ride);

        rideRepo.update(ride);

        System.out.println("Localização do motorista atualizada para: " + newAddress);
    }

    private void calculateAndUpdateETA(Ride ride) throws ValidationException {
        if (ride.getDriverCurrentLocation() == null || ride.getDestination() == null) {
            throw new ValidationException("Não foi possível calcular a ETA. Localização do motorista ou destino não definidos.");
        }

        if (ride.getVehicleCategory() == null || ride.getVehicleCategory().isEmpty()) {
            throw new ValidationException("Não foi possível calcular a ETA. Categoria do veículo não definida.");
        }

        double speedKmH = pricingService.getSpeedForCategory(ride.getVehicleCategory());

        double distance = DistanceCalculator.calculateDistance(
                ride.getDriverCurrentLocation().getAddress(),
                ride.getDestination().getAddress()
        );

        int time = DistanceCalculator.calculateEstimatedTime(distance, speedKmH);

        ride.setEstimatedTimeMinutes(time);

        System.out.println("Estimativa de chegada atualizada para: " + time + " minutos.");
    }

    private void generateOptimizedRoute(Ride ride) throws ValidationException {
        if (ride.getDriverCurrentLocation() == null || ride.getDestination() == null) {
            throw new ValidationException("Não foi possível gerar a rota. Localização do motorista ou destino não definidos.");
        }

        String startAddress = ride.getDriverCurrentLocation().getAddress();
        String endAddress = ride.getDestination().getAddress();

        List<String> routeSteps = Arrays.asList(
                "Saia de " + startAddress,
                "Siga em frente por 2km.",
                "Aguarde o semáforo na Rua Central.",
                "Vire à direita na Rua Principal.",
                "Você chegou ao seu destino: " + endAddress
        );

        ride.setOptimizedRoute(routeSteps);
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

    public Collection<Ride> getRidesByPassenger(String passengerEmail) throws ValidationException {
        if (passengerEmail == null || passengerEmail.trim().isEmpty()) {
            throw new ValidationException("Email do passageiro é obrigatório.");
        }

        return rideRepo.findByPassengerEmail(passengerEmail);
    }

    public Collection<Ride> getRidesByStatus(Ride.RideStatus status) throws ValidationException {
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

    public Set<String> getAvailableCategories() {
        return pricingService.getAvailableCategories();
    }

    public List<Ride> getPendingRidesForDriver(String driverEmail) throws ValidationException {
        User user = userRepo.findByEmail(driverEmail);
        if (!(user instanceof model.Driver)) {
            throw new ValidationException("Apenas motoristas podem ver as corridas disponíveis.");
        }
        model.Driver driver = (model.Driver) user;
        if (driver.getVehicles().isEmpty()) {
            throw new ValidationException("Você precisa ter um veículo para aceitar corridas.");
        }
        String category = driver.getVehicles().get(0).getCategory();
        return rideRepo.findByStatus(Ride.RideStatus.SOLICITADA)
                .stream()
                .filter(r -> r.getVehicleCategory().equalsIgnoreCase(category))
                .collect(Collectors.toList());
    }

    public void acceptRide(String rideId, String driverEmail) throws ValidationException, IOException {
        User user = userRepo.findByEmail(driverEmail);
        if (!(user instanceof model.Driver)) {
            throw new ValidationException("Apenas motoristas podem aceitar corridas.");
        }
        Ride ride = getRideById(rideId);
        if (ride.getStatus() != Ride.RideStatus.SOLICITADA) {
            throw new ValidationException("Esta corrida não está mais disponível.");
        }
        model.Driver driver = (model.Driver) user;
        if (driver.getVehicles().isEmpty()) {
            throw new ValidationException("Você precisa ter um veículo para aceitar corridas.");
        }
        if (!ride.getVehicleCategory().equalsIgnoreCase(driver.getVehicles().get(0).getCategory())) {
            throw new ValidationException("Você não pode aceitar esta corrida com seu veículo atual.");
        }
        ride.setStatus(Ride.RideStatus.ACEITA);
        rideRepo.update(ride);
        System.out.println("Corrida aceita! Passageiro " + ride.getPassengerEmail() + " foi notificado.");
    }

    public void refuseRide(String rideId, String driverEmail) throws ValidationException, IOException {
        User user = userRepo.findByEmail(driverEmail);
        if (!(user instanceof model.Driver)) {
            throw new ValidationException("Apenas motoristas podem recusar corridas.");
        }
        Ride ride = getRideById(rideId);
        if (ride.getStatus() != Ride.RideStatus.SOLICITADA) {
            throw new ValidationException("Esta corrida não está mais disponível.");
        }
        System.out.println("Você recusou a corrida. Ela permanecerá disponível para outros motoristas.");
    }

    public void emitReceiptForRide(String rideId, String paymentMethod) {
        try {
            Ride ride = getRideById(rideId);

            // Formatação de data/hora
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

            StringBuilder receipt = new StringBuilder();
            receipt.append("\n========================================\n");
            receipt.append("            RECIBO DE CORRIDA           \n");
            receipt.append("========================================\n");
            receipt.append("Passageiro: ").append(ride.getPassengerEmail()).append("\n");
            if (ride.getDriverId() != null) {
                User driver = userRepo.findByEmail(ride.getDriverId());
                receipt.append("Motorista: ").append(driver.getName()).append("\n");
            } else {
                receipt.append("Motorista: Ainda não atribuído\n");
            }
            receipt.append("----------------------------------------\n");
            receipt.append("Origem: ").append(ride.getOrigin().getAddress()).append("\n");
            receipt.append("Destino: ").append(ride.getDestination().getAddress()).append("\n");
            receipt.append("Categoria do veículo: ").append(ride.getVehicleCategory() != null ? ride.getVehicleCategory() : "Não definida").append("\n");
            receipt.append("Status da corrida: ").append(ride.getStatus().name()).append("\n");

            // Valor da corrida
            try {
                PricingInfo pricing = calculatePricing(ride.getOrigin().getAddress(), ride.getDestination().getAddress(), ride.getVehicleCategory());
                receipt.append("Valor: R$ ").append(String.format("%.2f", pricing.getTimePrice())).append("\n");
            } catch (ValidationException e) {
                receipt.append("Valor: Não calculado\n");
            }

            // Horário
            receipt.append("Data/Hora da solicitação: ").append(ride.getRequestTime() != null ? ride.getRequestTime().format(formatter) : "Não registrado").append("\n");
            int estimated = ride.getEstimatedTimeMinutes();
            receipt.append("Horário estimado de chegada: ")
                    .append(estimated > 0 ? estimated + " min" : "Não calculado")
                    .append("\n");
            receipt.append("Forma de pagamento: ").append(paymentMethod != null ? paymentMethod : "Não informada").append("\n");

            receipt.append("----------------------------------------\n");
            receipt.append("         OBRIGADO POR USAR NOSSO APP!   \n");
            receipt.append("========================================\n");

            System.out.println(receipt.toString());

        } catch (ValidationException e) {
            System.out.println("Erro ao gerar recibo: " + e.getMessage());
        }
    }
}
