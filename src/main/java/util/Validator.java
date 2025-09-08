package util;

import model.User;
import repo.UserRepository;
import java.util.regex.Pattern;

public class Validator {

    private final UserRepository userRepo;
    private final Pattern emailPattern = Pattern.compile("^[\\w.%+\\-]+@[\\w.\\-]+\\.[A-Za-z]{2,}$");

    public Validator(UserRepository userRepo) {
        this.userRepo = userRepo;
    }

    public void validateCommon(String name, String email, String phone) throws ValidationException {
        if (name == null || name.trim().isEmpty()) {
            throw new ValidationException("Nome obrigatório.");
        }
        if (email == null || email.trim().isEmpty()) {
            throw new ValidationException("Email obrigatório.");
        }
        if (!emailPattern.matcher(email.trim()).matches()) {
            throw new ValidationException("Email inválido.");
        }
        if (phone == null || phone.trim().isEmpty()) {
            throw new ValidationException("Telefone obrigatório.");
        }
        if (userRepo.existsByEmail(email.trim().toLowerCase())) {
            throw new ValidationException("Usuário com este email já existe.");
        }
    }
    
    public void validatePassword(String password) throws ValidationException {
        if (password == null || password.trim().isEmpty()) {
            throw new ValidationException("Senha obrigatória.");
        }
        if (password.trim().length() < 6) {
            throw new ValidationException("A senha deve ter no mínimo 6 caracteres.");
        }
    }
}