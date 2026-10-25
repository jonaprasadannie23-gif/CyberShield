package com.cybershield.repository;

import com.cybershield.model.SecurityTask;
import com.cybershield.storage.JsonFileStorage;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository
public class TaskRepository {

    private static final String FILE_NAME = "tasks.json";

    private final JsonFileStorage storage;
    private final List<SecurityTask> tasks;

    public TaskRepository(JsonFileStorage storage) {
        this.storage = storage;
        this.tasks = storage.loadAll(FILE_NAME, SecurityTask.class);
    }

    public synchronized List<SecurityTask> findByUserId(String userId) {
        return tasks.stream()
                .filter(t -> t.getUserId().equals(userId))
                .collect(Collectors.toList());
    }

    public synchronized Optional<SecurityTask> findById(String taskId) {
        return tasks.stream()
                .filter(t -> t.getTaskId().equals(taskId))
                .findFirst();
    }

    public synchronized List<SecurityTask> findCompletedByUserId(String userId) {
        return findByUserId(userId).stream()
                .filter(SecurityTask::isCompleted)
                .collect(Collectors.toList());
    }

    public synchronized List<SecurityTask> findPendingByUserId(String userId) {
        return findByUserId(userId).stream()
                .filter(t -> !t.isCompleted())
                .collect(Collectors.toList());
    }

    public synchronized void save(SecurityTask task) {
        tasks.removeIf(t -> t.getTaskId().equals(task.getTaskId()));
        tasks.add(task);
        persist();
    }

    public synchronized void delete(String taskId) {
        tasks.removeIf(t -> t.getTaskId().equals(taskId));
        persist();
    }

    private void persist() {
        storage.saveAll(FILE_NAME, tasks);
    }
}
