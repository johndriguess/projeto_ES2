package repo;

import model.User;
import java.io.*;
import java.util.*;

public class UserRepository {
    private final File storageFile;
    private Map<String, User> usersByEmail; // email -> User

    public UserRepository(String pathToFile) {
        this.storageFile = new File(pathToFile);
        load();
    }

    @SuppressWarnings("unchecked")
    private void load() {
        if (!storageFile.exists()) {
            usersByEmail = new HashMap<>();
            return;
        }
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(storageFile))) {
            Object o = ois.readObject();
            usersByEmail = (Map<String, User>) o;
        } catch (Exception e) {
            System.err.println("Não foi possível carregar armazenamento. Inicializando vazio. (" + e.getMessage() + ")");
            usersByEmail = new HashMap<>();
        }
    }

    private void save() throws IOException {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(storageFile))) {
            oos.writeObject(usersByEmail);
        }
    }

    public synchronized void add(User u) throws IOException {
        usersByEmail.put(u.getEmail(), u);
        save();
    }

    public boolean existsByEmail(String email) {
        return usersByEmail.containsKey(email.toLowerCase());
    }

    public User findByEmail(String email) {
        return usersByEmail.get(email.toLowerCase());
    }

    public Collection<User> findAll() {
        return Collections.unmodifiableCollection(usersByEmail.values());
    }
}
