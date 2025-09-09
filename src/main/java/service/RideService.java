package service;

import model.Ride;
import model.Location;
import model.Passenger;
import model.User;
import repo.RideRepository;
import repo.UserRepository;
import util.ValidationException;
import java.io.IOException;

/**
 * Serviço para gerenciar operações de corrida.
 * RF04 - Para criar e validar solicitações de corrida.
 */
public class RideService {
    private final RideRepository rideRepo;
    private final UserRepository userRepo;

    public RideService(RideRepository rideRepo, UserRepository userRepo) {
        this.rideRepo = rideRepo;
        this.userRepo = userRepo;
    }

    /**
     * Cria uma nova solicitação de corrida.
     * RF04 - Passageiro informa origem e destino.
     */
    public Ride createRideRequest(String passengerEmail, String originAddress, String destinationAddress) 
            throws ValidationException, IOException {
        
        // Validar se o passageiro existe e está logado
        User user = userRepo.findByEmail(passengerEmail);
        if (user == null) {
            throw new ValidationException("Usuário não encontrado.");
        }
        
        if (!(user instanceof Passenger)) {
            throw new ValidationException("Apenas passageiros podem solicitar corridas.");
        }
        
        // Validar dados da corrida
        validateRideRequest(originAddress, destinationAddress);
        
        // Criar localizações
        Location origin = new Location(originAddress);
        Location destination = new Location(destinationAddress);
        
        // Criar corrida
        Ride ride = new Ride(user.getId(), passengerEmail, origin, destination);
        
        // Salvar no repositório
        rideRepo.add(ride);
        
        return ride;
    }

    /**
     * Valida os dados de uma solicitação de corrida.
     */
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
        
        // Validação básica de formato de endereço
        if (originAddress.trim().length() < 5) {
            throw new ValidationException("Endereço de origem deve ter pelo menos 5 caracteres.");
        }
        
        if (destinationAddress.trim().length() < 5) {
            throw new ValidationException("Endereço de destino deve ter pelo menos 5 caracteres.");
        }
    }

    /**
     * Busca uma corrida por ID.
     */
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

    /**
     * Lista todas as corridas de um passageiro.
     */
    public java.util.Collection<Ride> getRidesByPassenger(String passengerEmail) throws ValidationException {
        if (passengerEmail == null || passengerEmail.trim().isEmpty()) {
            throw new ValidationException("Email do passageiro é obrigatório.");
        }
        
        return rideRepo.findByPassengerEmail(passengerEmail);
    }

    /**
     * Lista todas as corridas com um status específico.
     */
    public java.util.Collection<Ride> getRidesByStatus(Ride.RideStatus status) throws ValidationException {
        if (status == null) {
            throw new ValidationException("Status da corrida é obrigatório.");
        }
        
        return rideRepo.findByStatus(status);
    }

    /**
     * Atualiza o status de uma corrida.
     */
    public void updateRideStatus(String rideId, Ride.RideStatus newStatus) throws ValidationException, IOException {
        Ride ride = getRideById(rideId);
        ride.setStatus(newStatus);
        rideRepo.update(ride);
    }

    /**
     * Define a categoria do veículo para uma corrida.
     */
    public void setVehicleCategory(String rideId, String vehicleCategory) throws ValidationException, IOException {
        if (vehicleCategory == null || vehicleCategory.trim().isEmpty()) {
            throw new ValidationException("Categoria do veículo é obrigatória.");
        }
        
        Ride ride = getRideById(rideId);
        ride.setVehicleCategory(vehicleCategory);
        rideRepo.update(ride);
    }
}
