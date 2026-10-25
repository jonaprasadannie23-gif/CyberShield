package com.cybershield.web;

import com.cybershield.model.SecurityTask;
import com.cybershield.service.TaskService;
import com.cybershield.web.dto.TaskRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/tasks")
public class TaskController {

    private final TaskService taskService;

    public TaskController(TaskService taskService) {
        this.taskService = taskService;
    }

    @GetMapping
    public List<SecurityTask> list(HttpSession session) {
        return taskService.getTasksByUser(SessionUser.requireUserId(session));
    }

    @PostMapping
    public ResponseEntity<SecurityTask> create(@RequestBody TaskRequest request, HttpSession session) {
        SecurityTask task = taskService.createTask(
                SessionUser.requireUserId(session),
                request.getTitle(),
                request.getDescription(),
                request.getRelatedIncidentId());
        return ResponseEntity.status(HttpStatus.CREATED).body(task);
    }

    @PutMapping("/{id}")
    public SecurityTask update(@PathVariable String id, @RequestBody TaskRequest request, HttpSession session) {
        String userId = SessionUser.requireUserId(session);
        SecurityTask task = taskService.updateTask(id, userId, request.getTitle(), request.getDescription());
        if (Boolean.TRUE.equals(request.getCompleted())) {
            task = taskService.markCompleted(id, userId);
        } else if (Boolean.FALSE.equals(request.getCompleted())) {
            task = taskService.markPending(id, userId);
        }
        return task;
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable String id, HttpSession session) {
        taskService.deleteTask(id, SessionUser.requireUserId(session));
        return ResponseEntity.noContent().build();
    }
}
