package repository;

import model.User;
import util.FileStorageUtil;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Handles persistence for User objects.
 * Loads data at startup and writes it back on every mutation.
 */
public class UserRepository {

    private static final String FILE_NAME = "users.dat";

    private List<User> users;

    public UserRepository() {
        this.users = FileStorageUtil.loadAll(FILE_NAME);
    }

    // --- Query ---

    public List<User> findAll() {
        return new ArrayList<>(users);
    }

    public Optional<User> findById(String userId) {
        return users.stream().filter(u -> u.getUserId().equals(userId)).findFirst();
    }

    public Optional<User> findByUsername(String username) {
        return users.stream()
                    .filter(u -> u.getUsername().equalsIgnoreCase(username))
                    .findFirst();
    }

    public Optional<User> findByEmail(String email) {
        return users.stream()
                    .filter(u -> u.getEmail().equalsIgnoreCase(email))
                    .findFirst();
    }

    public boolean existsByUsername(String username) {
        return findByUsername(username).isPresent();
    }

    public boolean existsByEmail(String email) {
        return findByEmail(email).isPresent();
    }

    // --- Mutation ---

    public void save(User user) {
        // Replace if exists, otherwise add
        users.removeIf(u -> u.getUserId().equals(user.getUserId()));
        users.add(user);
        persist();
    }

    public void delete(String userId) {
        users.removeIf(u -> u.getUserId().equals(userId));
        persist();
    }

    // --- Persistence ---

    private void persist() {
        try {
            FileStorageUtil.saveAll(FILE_NAME, users);
        } catch (IOException e) {
            System.err.println("[UserRepository] Failed to save users: " + e.getMessage());
        }
    }
}
