package com.cybershield.web.dto;

import com.cybershield.model.Incident;
import com.cybershield.model.IncidentStatus;
import com.cybershield.model.IncidentType;
import com.cybershield.model.RiskLevel;
import com.cybershield.model.SecurityTask;

import java.util.List;
import java.util.Map;

public record DashboardResponse(
        int totalIncidents,
        long highRiskCount,
        long unresolvedCount,
        int pendingTasks,
        double taskCompletionPercent,
        List<Incident> recentIncidents,
        List<SecurityTask> pendingTaskList,
        Map<RiskLevel, Long> byRisk,
        Map<IncidentType, Long> byType,
        Map<IncidentStatus, Long> byStatus
) {}
