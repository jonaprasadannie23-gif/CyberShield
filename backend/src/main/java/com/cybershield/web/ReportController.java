package com.cybershield.web;

import com.cybershield.service.ReportService;
import com.cybershield.web.dto.ReportResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class ReportController {

    private final ReportService reportService;

    public ReportController(ReportService reportService) {
        this.reportService = reportService;
    }

    @GetMapping("/reports")
    public ReportResponse reports(HttpSession session) {
        String userId = SessionUser.requireUserId(session);
        return new ReportResponse(
                reportService.getTotalIncidents(userId),
                reportService.getResolvedCount(userId),
                reportService.getUnresolvedCount(userId),
                reportService.getCountByRiskLevel(userId),
                reportService.getCountByType(userId),
                reportService.getCountByStatus(userId),
                reportService.getTotalTasks(userId),
                reportService.getCompletedTasks(userId),
                reportService.getPendingTasks(userId),
                reportService.getTaskCompletionPercent(userId)
        );
    }

    @GetMapping("/recommendations")
    public Map<String, Object> recommendations(HttpSession session) {
        String userId = SessionUser.requireUserId(session);
        return Map.of(
                "recommendations", reportService.getUniqueRecommendations(userId),
                "disclaimer", List.of(
                        "Recommendations are generated from your recorded incidents using a rule-based engine.",
                        "CyberShield does not detect every real-world scam and does not provide real-time protection."
                ),
                "totalIncidents", reportService.getTotalIncidents(userId),
                "totalTasks", reportService.getTotalTasks(userId),
                "completedTasks", reportService.getCompletedTasks(userId)
        );
    }
}
