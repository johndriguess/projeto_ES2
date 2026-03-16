package repo;

import model.User;
import java.io.*;
import java.util.*;

public class UserRepository {
    private static final String STORAGE_DB_NAME = "users.db";
    private static final String LEGACY_STORAGE_NAME = "users.date";

    private final File storageFile;
    private Map<String, User> usersByEmail;

    public UserRepository() {
        File dataDir = new File("data");
        if (!dataDir.exists())
            dataDir.mkdirs();
        this.storageFile = new File(dataDir, STORAGE_DB_NAME);
        load();
    }

    public UserRepository(File storageFile) {
        this.storageFile = storageFile;
        load();
    }

    @SuppressWarnings("unchecked")
    private void load() {
        File source = storageFile;
        if (!source.exists()) {
            File legacy = new File(storageFile.getParentFile(), LEGACY_STORAGE_NAME);
            if (legacy.exists()) {
                source = legacy;
            } else {
                usersByEmail = new HashMap<>();
                return;
            }
        }

        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(source))) {
            Object o = ois.readObject();
            usersByEmail = (Map<String, User>) o;

            // Migra automaticamente do arquivo legado users.date para users.db.
            if (!source.equals(storageFile)) {
                save();
            }
        } catch (Exception e) {
            System.err
                    .println("Não foi possível carregar armazenamento. Inicializando vazio. (" + e.getMessage() + ")");
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