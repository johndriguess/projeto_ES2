package service;

import model.Driver;
import model.Passenger;
import model.User;
import model.Vehicle;
import repo.UserRepository;
import repo.VehicleRepository;
import service.DocumentValidator;
import util.ValidationException;
import util.Validator; 
import java.io.IOException;

public class AuthService {
    private final UserRepository userRepo;
    private final VehicleRepository vehicleRepo;
    private final DocumentValidator documentValidator;
    private final Validator validator;

    public AuthService(UserRepository userRepo, VehicleRepository vehicleRepo) {
        this.userRepo = userRepo;
        this.vehicleRepo = vehicleRepo;
        this.documentValidator = new DocumentValidator();
        this.validator = new Validator(userRepo); 
    }

    public Passenger registerPassenger(String name, String email, String phone, String password) throws ValidationException, IOException {
        validator.validateCommon(name, email, phone);
        validator.validatePassword(password);
        Passenger p = new Passenger(name, email, phone, password);
        userRepo.add(p);
        return p;
    }

    public Driver registerDriver(String name, String email, String phone, String password,
                                   String documentNumber, String vehiclePlate,
                                   String vehicleModel, int vehicleYear, String vehicleColor)
            throws ValidationException, IOException {
        validator.validateCommon(name, email, phone);
        validator.validatePassword(password);
        if (documentNumber == null || documentNumber.trim().isEmpty()) throw new ValidationException("Documento do motorista obrigatório.");
        
        if (vehicleRepo.existsByPlate(vehiclePlate)) {
            throw new ValidationException("Veículo com esta placa já está cadastrado.");
        }
        
        Vehicle vehicle = new Vehicle(vehiclePlate, vehicleModel, vehicleYear, vehicleColor);
        documentValidator.validateVehicleCategory(vehicle);
        
        Driver d = new Driver(name, email, phone, password, documentNumber, vehicle);
        
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
        
        userRepo.update(driver);
        vehicleRepo.add(newVehicle);
        
        return driver;
    }
    
    public User login(String email, String password) throws ValidationException {
        if (email == null || email.trim().isEmpty()) {
            throw new ValidationException("Email não pode ser vazio.");
        }
        if (password == null || password.trim().isEmpty()) {
            throw new ValidationException("Senha não pode ser vazia.");
        }
        
        User user = userRepo.findByEmail(email);
        
        if (user == null) {
            throw new ValidationException("Usuário não encontrado.");
        }
        
        if (!user.getPassword().equals(password)) {
            throw new ValidationException("Senha incorreta.");
        }
        
        return user;
    }
}