package service;

import model.Driver;
import model.Passenger;
import model.Ride;
import model.Ride.RideStatus;
import repo.RideRepository;
import repo.UserRepository;
import util.ValidationException;
import java.io.IOException;

public class RatingService {

    private final UserRepository userRepository;
    private final RideRepository rideRepository;

    public RatingService(UserRepository userRepository, RideRepository rideRepository) {
        this.userRepository = userRepository;
        this.rideRepository = rideRepository;
    }

    public void rateDriver(Ride ride, int rating) throws ValidationException, IOException {
        if (ride.getStatus() != RideStatus.FINALIZADA) {
            throw new ValidationException("A corrida deve estar 'Finalizada' para ser avaliada.");
        }
        
        if (ride.hasPassengerRated()) {
            throw new ValidationException("Você já avaliou o motorista para esta corrida.");
        }

        Driver driver = (Driver) userRepository.findById(ride.getDriverId());
        if (driver == null) {
            throw new ValidationException("Motorista (ID: " + ride.getDriverId() + ") não encontrado.");
        }

        driver.addRating(rating);
        
        ride.setPassengerHasRated(true);

        userRepository.update(driver); 
        rideRepository.update(ride);  
    }

    public void ratePassenger(Ride ride, int rating) throws ValidationException, IOException {
        if (ride.getStatus() != RideStatus.FINALIZADA) {
            throw new ValidationException("A corrida deve estar 'Finalizada' para ser avaliada.");
        }

        if (ride.hasDriverRated()) {
            throw new ValidationException("Você já avaliou o passageiro para esta corrida.");
        }

        Passenger passenger = (Passenger) userRepository.findById(ride.getPassengerId());
        if (passenger == null) {
            throw new ValidationException("Passageiro (ID: " + ride.getPassengerId() + ") não encontrado.");
        }

        passenger.addRating(rating);
        
        ride.setDriverHasRated(true);

        userRepository.update(passenger); 
        rideRepository.update(ride);    
    }
}