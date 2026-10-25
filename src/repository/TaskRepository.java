package repository;

import model.SecurityTask;
import util.FileStorageUtil;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Handles persistence for SecurityTask objects.
 */
public class TaskRepository {

    private static final String FILE_NAME = "tasks.dat";

    private List<SecurityTask> tasks;

    public TaskRepository() {
        this.tasks = FileStorageUtil.loadAll(FILE_NAME);
    }

    // --- Query ---

    public List<SecurityTask> findAll() {
        return new ArrayList<>(tasks);
    }

    public List<SecurityTask> findByUserId(String userId) {
        return tasks.stream()
                    .filter(t -> t.getUserId().equals(userId))
                    .collect(Collectors.toList());
    }

    public Optional<SecurityTask> findById(String taskId) {
        return tasks.stream()
                    .filter(t -> t.getTaskId().equals(taskId))
                    .findFirst();
    }

    public List<SecurityTask> findCompletedByUserId(String userId) {
        return findByUserId(userId).stream()
                                   .filter(SecurityTask::isCompleted)
                                   .collect(Collectors.toList());
    }

    public List<SecurityTask> findPendingByUserId(String userId) {
        return findByUserId(userId).stream()
                                   .filter(t -> !t.isCompleted())
                                   .collect(Collectors.toList());
    }

    // --- Mutation ---

    public void save(SecurityTask task) {
        tasks.removeIf(t -> t.getTaskId().equals(task.getTaskId()));
        tasks.add(task);
        persist();
    }

    public void delete(String taskId) {
        tasks.removeIf(t -> t.getTaskId().equals(taskId));
        persist();
    }

    // --- Persistence ---

    private void persist() {
        try {
            FileStorageUtil.saveAll(FILE_NAME, tasks);
        } catch (IOException e) {
            System.err.println("[TaskRepository] Failed to save tasks: " + e.getMessage());
        }
    }
}
