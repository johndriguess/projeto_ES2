package service;

import model.Driver;
import model.Passenger;
import model.User;
import repo.UserRepository;
import util.ValidationException;

import java.io.IOException;
import java.util.regex.Pattern;

public class AuthService {
    private final UserRepository repo;
    private final Pattern emailPattern = Pattern.compile("^[\\w.%+\\-]+@[\\w.\\-]+\\.[A-Za-z]{2,}$");

    public AuthService(UserRepository repo) {
        this.repo = repo;
    }

    private void validateCommon(String name, String email, String phone) throws ValidationException {
        if (name == null || name.trim().isEmpty()) throw new ValidationException("Nome obrigatório.");
        if (email == null || email.trim().isEmpty()) throw new ValidationException("Email obrigatório.");
        if (!emailPattern.matcher(email.trim()).matches()) throw new ValidationException("Email inválido.");
        if (phone == null || phone.trim().isEmpty()) throw new ValidationException("Telefone obrigatório.");
        if (repo.existsByEmail(email.trim().toLowerCase())) throw new ValidationException("Usuário com este email já existe.");
    }

    public Passenger registerPassenger(String name, String email, String phone) throws ValidationException, IOException {
        validateCommon(name, email, phone);
        Passenger p = new Passenger(name, email, phone);
        repo.add(p);
        return p;
    }

    public Driver registerDriver(String name, String email, String phone,
                                 String documentNumber, String vehiclePlate, String vehicleModel)
            throws ValidationException, IOException {
        validateCommon(name, email, phone);
        if (documentNumber == null || documentNumber.trim().isEmpty()) throw new ValidationException("Documento do motorista obrigatório.");
        if (vehiclePlate == null || vehiclePlate.trim().isEmpty()) throw new ValidationException("Placa do veículo obrigatória.");
        if (vehicleModel == null || vehicleModel.trim().isEmpty()) throw new ValidationException("Modelo do veículo obrigatório.");
        Driver d = new Driver(name, email, phone, documentNumber, vehiclePlate, vehicleModel);
        repo.add(d);
        return d;
    }
}
