package service;

import model.SecurityTask;
import repository.TaskRepository;
import util.IDGenerator;
import util.InputValidator;

import java.util.List;
import java.util.Optional;

/**
 * Business logic for the Safety Task Manager.
 */
public class TaskService {

    private final TaskRepository taskRepository;

    public TaskService(TaskRepository taskRepository) {
        this.taskRepository = taskRepository;
    }

    // -----------------------------------------------------------------------
    // Create
    // -----------------------------------------------------------------------

    public SecurityTask createTask(String userId, String title, String description)
            throws IllegalArgumentException {
        if (!InputValidator.isNotBlank(title))
            throw new IllegalArgumentException("Task title is required.");

        SecurityTask task = new SecurityTask(IDGenerator.generate(), userId,
                                             title.trim(),
                                             description == null ? "" : description.trim());
        taskRepository.save(task);
        return task;
    }

    public SecurityTask createTask(String userId, String title, String description,
                                   String relatedIncidentId)
            throws IllegalArgumentException {
        SecurityTask task = createTask(userId, title, description);
        task.setRelatedIncidentId(relatedIncidentId);
        taskRepository.save(task);
        return task;
    }

    // -----------------------------------------------------------------------
    // Read
    // -----------------------------------------------------------------------

    public List<SecurityTask> getTasksByUser(String userId) {
        return taskRepository.findByUserId(userId);
    }

    public List<SecurityTask> getPendingTasksByUser(String userId) {
        return taskRepository.findPendingByUserId(userId);
    }

    public List<SecurityTask> getCompletedTasksByUser(String userId) {
        return taskRepository.findCompletedByUserId(userId);
    }

    // -----------------------------------------------------------------------
    // Update
    // -----------------------------------------------------------------------

    public void markCompleted(String taskId, String userId) throws IllegalArgumentException {
        SecurityTask task = getOwnedTask(taskId, userId);
        task.markCompleted();
        taskRepository.save(task);
    }

    public void markPending(String taskId, String userId) throws IllegalArgumentException {
        SecurityTask task = getOwnedTask(taskId, userId);
        task.markPending();
        taskRepository.save(task);
    }

    public void updateTask(String taskId, String userId, String title, String description)
            throws IllegalArgumentException {
        if (!InputValidator.isNotBlank(title))
            throw new IllegalArgumentException("Task title is required.");
        SecurityTask task = getOwnedTask(taskId, userId);
        task.setTitle(title.trim());
        task.setDescription(description == null ? "" : description.trim());
        taskRepository.save(task);
    }

    // -----------------------------------------------------------------------
    // Delete
    // -----------------------------------------------------------------------

    public void deleteTask(String taskId, String userId) throws IllegalArgumentException {
        getOwnedTask(taskId, userId);   // ownership check
        taskRepository.delete(taskId);
    }

    // -----------------------------------------------------------------------
    // Statistics
    // -----------------------------------------------------------------------

    public int countPending(String userId) {
        return getPendingTasksByUser(userId).size();
    }

    public int countCompleted(String userId) {
        return getCompletedTasksByUser(userId).size();
    }

    public double completionPercentage(String userId) {
        List<SecurityTask> all = getTasksByUser(userId);
        if (all.isEmpty()) return 0.0;
        long done = all.stream().filter(SecurityTask::isCompleted).count();
        return (done * 100.0) / all.size();
    }

    // -----------------------------------------------------------------------
    // Helper
    // -----------------------------------------------------------------------

    private SecurityTask getOwnedTask(String taskId, String userId) {
        Optional<SecurityTask> opt = taskRepository.findById(taskId);
        if (opt.isEmpty())
            throw new IllegalArgumentException("Task not found.");
        SecurityTask t = opt.get();
        if (!t.getUserId().equals(userId))
            throw new IllegalArgumentException("Access denied: you do not own this task.");
        return t;
    }
}
