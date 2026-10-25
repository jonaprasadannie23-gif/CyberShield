package com.cybershield.web;

import com.cybershield.service.ReportService;
import com.cybershield.service.TaskService;
import com.cybershield.web.dto.DashboardResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    private final ReportService reportService;
    private final TaskService taskService;

    public DashboardController(ReportService reportService, TaskService taskService) {
        this.reportService = reportService;
        this.taskService = taskService;
    }

    @GetMapping
    public DashboardResponse dashboard(HttpSession session) {
        String userId = SessionUser.requireUserId(session);
        return new DashboardResponse(
                reportService.getTotalIncidents(userId),
                reportService.getHighRiskCount(userId),
                reportService.getUnresolvedCount(userId),
                reportService.getPendingTasks(userId),
                reportService.getTaskCompletionPercent(userId),
                reportService.getRecentIncidents(userId, 5),
                taskService.getPendingTasksByUser(userId),
                reportService.getCountByRiskLevel(userId),
                reportService.getCountByType(userId),
                reportService.getCountByStatus(userId)
        );
    }
}
