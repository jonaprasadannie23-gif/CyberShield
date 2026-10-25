package service;

import model.*;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Calculates all statistics displayed on the Reports screen.
 * All numbers are derived from live data – nothing is hardcoded.
 */
public class ReportService {

    private final IncidentService incidentService;
    private final TaskService taskService;

    public ReportService(IncidentService incidentService, TaskService taskService) {
        this.incidentService = incidentService;
        this.taskService     = taskService;
    }

    // -----------------------------------------------------------------------
    // Incident statistics
    // -----------------------------------------------------------------------

    public int getTotalIncidents(String userId) {
        return incidentService.getIncidentsByUser(userId).size();
    }

    public Map<RiskLevel, Long> getCountByRiskLevel(String userId) {
        Map<RiskLevel, Long> map = new LinkedHashMap<>();
        for (RiskLevel level : RiskLevel.values()) map.put(level, 0L);
        incidentService.getIncidentsByUser(userId)
                       .forEach(i -> map.merge(i.getRiskLevel(), 1L, Long::sum));
        return map;
    }

    public Map<IncidentType, Long> getCountByType(String userId) {
        Map<IncidentType, Long> map = new LinkedHashMap<>();
        for (IncidentType t : IncidentType.values()) map.put(t, 0L);
        incidentService.getIncidentsByUser(userId)
                       .forEach(i -> map.merge(i.getType(), 1L, Long::sum));
        return map;
    }

    public Map<IncidentStatus, Long> getCountByStatus(String userId) {
        Map<IncidentStatus, Long> map = new LinkedHashMap<>();
        for (IncidentStatus s : IncidentStatus.values()) map.put(s, 0L);
        incidentService.getIncidentsByUser(userId)
                       .forEach(i -> map.merge(i.getStatus(), 1L, Long::sum));
        return map;
    }

    public long getResolvedCount(String userId) {
        return incidentService.getIncidentsByUser(userId).stream()
               .filter(i -> i.getStatus() == IncidentStatus.RESOLVED).count();
    }

    public long getUnresolvedCount(String userId) {
        return getTotalIncidents(userId) - getResolvedCount(userId);
    }

    // -----------------------------------------------------------------------
    // Task statistics
    // -----------------------------------------------------------------------

    public int getTotalTasks(String userId) {
        return taskService.getTasksByUser(userId).size();
    }

    public int getCompletedTasks(String userId) {
        return taskService.countCompleted(userId);
    }

    public int getPendingTasks(String userId) {
        return taskService.countPending(userId);
    }

    public double getTaskCompletionPercent(String userId) {
        return taskService.completionPercentage(userId);
    }

    // -----------------------------------------------------------------------
    // Recent incidents (for dashboard)
    // -----------------------------------------------------------------------

    public List<Incident> getRecentIncidents(String userId, int limit) {
        List<Incident> all = incidentService.getIncidentsByUser(userId);
        all.sort(Comparator.comparing(Incident::getReportedAt).reversed());
        return all.stream().limit(limit).collect(Collectors.toList());
    }
}
