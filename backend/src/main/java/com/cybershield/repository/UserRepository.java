package com.cybershield.repository;

import com.cybershield.model.User;
import com.cybershield.storage.JsonFileStorage;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Repository
public class UserRepository {

    private static final String FILE_NAME = "users.json";

    private final JsonFileStorage storage;
    private final List<User> users;

    public UserRepository(JsonFileStorage storage) {
        this.storage = storage;
        this.users = storage.loadAll(FILE_NAME, User.class);
    }

    public synchronized List<User> findAll() {
        return new ArrayList<>(users);
    }

    public synchronized Optional<User> findById(String userId) {
        return users.stream().filter(u -> u.getUserId().equals(userId)).findFirst();
    }

    public synchronized Optional<User> findByUsername(String username) {
        return users.stream()
                .filter(u -> u.getUsername().equalsIgnoreCase(username))
                .findFirst();
    }

    public synchronized boolean existsByUsername(String username) {
        return findByUsername(username).isPresent();
    }

    public synchronized boolean existsByEmail(String email) {
        return users.stream().anyMatch(u -> u.getEmail().equalsIgnoreCase(email));
    }

    public synchronized void save(User user) {
        users.removeIf(u -> u.getUserId().equals(user.getUserId()));
        users.add(user);
        persist();
    }

    private void persist() {
        storage.saveAll(FILE_NAME, users);
    }
}
