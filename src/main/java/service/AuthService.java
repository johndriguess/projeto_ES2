package service;

import model.Driver;
import model.Passenger;
import model.User;
import model.Vehicle;
import repo.UserRepository;
import repo.VehicleRepository;
import util.ValidationException;

import java.io.IOException;
import java.util.regex.Pattern;

public class AuthService {
    private final UserRepository userRepo;
    private final VehicleRepository vehicleRepo;
    private final DocumentValidator documentValidator;
    private final Pattern emailPattern = Pattern.compile("^[\\w.%+\\-]+@[\\w.\\-]+\\.[A-Za-z]{2,}$");

    public AuthService(UserRepository userRepo, VehicleRepository vehicleRepo) {
        this.userRepo = userRepo;
        this.vehicleRepo = vehicleRepo;
        this.documentValidator = new DocumentValidator();
    }

    private void validateCommon(String name, String email, String phone) throws ValidationException {
        if (name == null || name.trim().isEmpty()) throw new ValidationException("Nome obrigatório.");
        if (email == null || email.trim().isEmpty()) throw new ValidationException("Email obrigatório.");
        if (!emailPattern.matcher(email.trim()).matches()) throw new ValidationException("Email inválido.");
        if (phone == null || phone.trim().isEmpty()) throw new ValidationException("Telefone obrigatório.");
        if (userRepo.existsByEmail(email.trim().toLowerCase())) throw new ValidationException("Usuário com este email já existe.");
    }

    public Passenger registerPassenger(String name, String email, String phone) throws ValidationException, IOException {
        validateCommon(name, email, phone);
        Passenger p = new Passenger(name, email, phone);
        userRepo.add(p);
        return p;
    }

    public Driver registerDriver(String name, String email, String phone,
                                 String documentNumber, String vehiclePlate,
                                 String vehicleModel, int vehicleYear, String vehicleColor)
            throws ValidationException, IOException {
        validateCommon(name, email, phone);
        if (documentNumber == null || documentNumber.trim().isEmpty()) throw new ValidationException("Documento do motorista obrigatório.");
        
        if (vehicleRepo.existsByPlate(vehiclePlate)) {
            throw new ValidationException("Veículo com esta placa já está cadastrado.");
        }
        
        Vehicle vehicle = new Vehicle(vehiclePlate, vehicleModel, vehicleYear, vehicleColor);
        documentValidator.validateVehicleCategory(vehicle);
        
        Driver d = new Driver(name, email, phone, documentNumber, vehicle);
        
        userRepo.add(d);
        vehicleRepo.add(vehicle);

        return d;
    }

    public Driver addVehicleToDriver(String driverEmail, String vehiclePlate,
                                     String vehicleModel, int vehicleYear, String vehicleColor)
            throws ValidationException, IOException {

        User user = userRepo.findByEmail(driverEmail);
        if (user == null || !(user instanceof Driver)) {
            throw new ValidationException("Motorista não encontrado.");
        }
        Driver driver = (Driver) user;
        
        if (vehicleRepo.existsByPlate(vehiclePlate)) {
            throw new ValidationException("Veículo com esta placa já está cadastrado.");
        }
        
        Vehicle newVehicle = new Vehicle(vehiclePlate, vehicleModel, vehicleYear, vehicleColor);
        documentValidator.validateVehicleCategory(newVehicle);
        
        driver.addVehicle(newVehicle);
        
        userRepo.add(driver);
        vehicleRepo.add(newVehicle);
        
        return driver;
    }
}