package repo;

import model.Driver;
import model.User;
import java.io.*;
import java.util.*;

public class UserRepository {
    private final File storageFile;
    private Map<String, User> usersByEmail; 

    public UserRepository() {
        File dataDir = new File("data");
        if (!dataDir.exists()) dataDir.mkdirs();
        this.storageFile = new File(dataDir, "users.date");
        load();
    }

    public UserRepository(File storageFile) {
        this.storageFile = storageFile;
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

    public void save() throws IOException {
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

    public User findById(String id) {
        if (id == null || id.trim().isEmpty()) {
            return null;
        }
        for (User user : usersByEmail.values()) {
            if (id.equals(user.getId())) {
                return user;
            }
        }
        return null; 
    }
    
    public synchronized void update(User user) throws IOException {
        if (user == null || !existsByEmail(user.getEmail())) {
            return; 
        }
        usersByEmail.put(user.getEmail(), user);
        save(); 
    }

    public Collection<User> findAll() {
        return Collections.unmodifiableCollection(usersByEmail.values());
    }
}