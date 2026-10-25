package com.cybershield.service;

import com.cybershield.exception.ConflictException;
import com.cybershield.exception.ForbiddenException;
import com.cybershield.exception.NotFoundException;
import com.cybershield.model.SecurityTask;
import com.cybershield.repository.TaskRepository;
import com.cybershield.util.IDGenerator;
import com.cybershield.util.InputValidator;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TaskService {

    private final TaskRepository taskRepository;

    public TaskService(TaskRepository taskRepository) {
        this.taskRepository = taskRepository;
    }

    public SecurityTask createTask(String userId, String title, String description, String relatedIncidentId) {
        if (!InputValidator.isNotBlank(title))
            throw new IllegalArgumentException("Task title is required.");

        String cleanTitle = title.trim();
        String cleanDesc = description == null ? "" : description.trim();

        boolean duplicate = taskRepository.findByUserId(userId).stream()
                .anyMatch(t -> t.getTitle().equalsIgnoreCase(cleanTitle)
                        || (!cleanDesc.isEmpty() && t.getDescription().equalsIgnoreCase(cleanDesc)));
        if (duplicate) {
            throw new ConflictException("A safety task for this recommendation is already tracked.");
        }

        SecurityTask task = new SecurityTask(IDGenerator.generate(), userId,
                cleanTitle, cleanDesc);
        if (InputValidator.isNotBlank(relatedIncidentId)) {
            task.setRelatedIncidentId(relatedIncidentId.trim());
        }
        taskRepository.save(task);
        return task;
    }

    public List<SecurityTask> getTasksByUser(String userId) {
        return taskRepository.findByUserId(userId);
    }

    public List<SecurityTask> getPendingTasksByUser(String userId) {
        return taskRepository.findPendingByUserId(userId);
    }

    public List<SecurityTask> getCompletedTasksByUser(String userId) {
        return taskRepository.findCompletedByUserId(userId);
    }

    public SecurityTask markCompleted(String taskId, String userId) {
        SecurityTask task = getOwnedTask(taskId, userId);
        task.markCompleted();
        taskRepository.save(task);
        return task;
    }

    public SecurityTask markPending(String taskId, String userId) {
        SecurityTask task = getOwnedTask(taskId, userId);
        task.markPending();
        taskRepository.save(task);
        return task;
    }

    public SecurityTask updateTask(String taskId, String userId, String title, String description) {
        if (!InputValidator.isNotBlank(title))
            throw new IllegalArgumentException("Task title is required.");
        SecurityTask task = getOwnedTask(taskId, userId);
        task.setTitle(title.trim());
        task.setDescription(description == null ? "" : description.trim());
        taskRepository.save(task);
        return task;
    }

    public void deleteTask(String taskId, String userId) {
        getOwnedTask(taskId, userId);
        taskRepository.delete(taskId);
    }

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

    private SecurityTask getOwnedTask(String taskId, String userId) {
        SecurityTask task = taskRepository.findById(taskId)
                .orElseThrow(() -> new NotFoundException("Task not found."));
        if (!task.getUserId().equals(userId)) {
            throw new ForbiddenException("Access denied: you do not own this task.");
        }
        return task;
    }
}
