package test;

import model.*;
import repo.*;
import service.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ReceiptTestAdvanced {

    public static void main(String[] args) {
        try {
            // Criar repositórios
            UserRepository userRepo = new UserRepository();
            RideRepository rideRepo = new RideRepository("rides.dat");

            // Criar serviço
            RideService rideService = new RideService(rideRepo, userRepo);

            // Criar passageiro
            Passenger passenger = new Passenger("João da Silva", "pass@example.com", "11999999999", "123456");
            userRepo.add(passenger);

            // Criar motoristas com veículos
            Vehicle vehicle1 = new Vehicle("ABC-1234", "Toyota Corolla", 2020, "Prata");
            vehicle1.setCategory("Sedan");
            Driver driver1 = new Driver("Maria Motorista", "driver@example.com", "11988888888", "123456", "12345678901", vehicle1);
            userRepo.add(driver1);

            Vehicle vehicle2 = new Vehicle("XYZ-5678", "Honda Civic", 2021, "Preto");
            vehicle2.setCategory("Sedan");
            Driver driver2 = new Driver("Carlos Motorista", "driver2@example.com", "11977777777", "123456", "98765432100", vehicle2);
            userRepo.add(driver2);

            // Lista de corridas para teste
            List<Ride> rides = new ArrayList<>();

            // 1️⃣ Corrida solicitada
            Ride ride1 = new Ride(passenger.getId(), passenger.getEmail(),
                    new Location("Rua A, 100"), new Location("Rua B, 200"));
            ride1.setVehicleCategory("Sedan");
            ride1.setStatus(Ride.RideStatus.SOLICITADA);
            rideRepo.add(ride1);
            rides.add(ride1);

            // 2️⃣ Corrida aceita
            Ride ride2 = new Ride(passenger.getId(), passenger.getEmail(),
                    new Location("Rua C, 300"), new Location("Rua D, 400"));
            ride2.setDriverId(driver1.getId());
            ride2.setVehicleCategory("Sedan");
            ride2.setStatus(Ride.RideStatus.ACEITA);
            ride2.setEstimatedTimeMinutes(12);
            rideRepo.add(ride2);
            rides.add(ride2);

            // 3️⃣ Corrida finalizada
            Ride ride3 = new Ride(passenger.getId(), passenger.getEmail(),
                    new Location("Rua E, 500"), new Location("Rua F, 600"));
            ride3.setDriverId(driver2.getId());
            ride3.setVehicleCategory("Sedan");
            ride3.setStatus(Ride.RideStatus.FINALIZADA);
            ride3.setEstimatedTimeMinutes(20);
            rideRepo.add(ride3);
            rides.add(ride3);

            // Emitir recibos para todas as corridas
            for (Ride r : rides) {
                System.out.println("\n===== EMITINDO RECIBO =====");
                rideService.emitReceiptForRide(r.getId(), "Cartão de Crédito");
            }

        } catch (IOException e) {
            System.err.println("Erro durante o teste avançado: " + e.getMessage());
        }
    }
}
