package com.cybershield.web.dto;

import com.cybershield.model.IncidentStatus;
import com.cybershield.model.IncidentType;
import com.cybershield.model.RiskLevel;

import java.util.Map;

public record ReportResponse(
        int totalIncidents,
        long resolvedCount,
        long unresolvedCount,
        Map<RiskLevel, Long> byRisk,
        Map<IncidentType, Long> byType,
        Map<IncidentStatus, Long> byStatus,
        int totalTasks,
        int completedTasks,
        int pendingTasks,
        double taskCompletionPercent
) {}
