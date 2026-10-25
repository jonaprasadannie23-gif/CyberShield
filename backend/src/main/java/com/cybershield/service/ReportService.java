package com.cybershield.service;

import com.cybershield.model.Incident;
import com.cybershield.model.IncidentStatus;
import com.cybershield.model.IncidentType;
import com.cybershield.model.RiskLevel;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class ReportService {

    private final IncidentService incidentService;
    private final TaskService taskService;

    public ReportService(IncidentService incidentService, TaskService taskService) {
        this.incidentService = incidentService;
        this.taskService = taskService;
    }

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

    public List<Incident> getRecentIncidents(String userId, int limit) {
        List<Incident> all = incidentService.getIncidentsByUser(userId);
        all.sort(Comparator.comparing(Incident::getReportedAt, Comparator.nullsLast(Comparator.naturalOrder())).reversed());
        return all.stream().limit(limit).collect(Collectors.toList());
    }

    public List<String> getUniqueRecommendations(String userId) {
        List<Incident> incidents = new ArrayList<>(incidentService.getIncidentsByUser(userId));
        incidents.sort(Comparator
                .comparing(Incident::getRiskLevel, Comparator.nullsLast(Comparator.reverseOrder()))
                .thenComparing(i -> i.getStatus() != IncidentStatus.RESOLVED ? 0 : 1)
                .thenComparing(Incident::getReportedAt, Comparator.nullsLast(Comparator.reverseOrder())));

        Set<String> unique = new LinkedHashSet<>();
        for (Incident incident : incidents) {
            List<String> recs = incident.getRecommendations();
            if (recs != null) {
                for (String r : recs) {
                    if (r != null && !r.isBlank()) {
                        unique.add(r.trim());
                    }
                }
            }
        }
        return deduplicateAdvice(new ArrayList<>(unique));
    }

    private List<String> deduplicateAdvice(List<String> list) {
        List<String> result = new ArrayList<>();
        boolean hasDetailedOtp = list.stream().anyMatch(r -> r.contains("Legitimate banks and services will never ask"));
        for (String item : list) {
            if (hasDetailedOtp && item.equals("Never share your OTP with anyone under any circumstances.")) {
                continue;
            }
            if (!result.contains(item)) {
                result.add(item);
            }
        }
        return List.copyOf(result);
    }

    public long getHighRiskCount(String userId) {
        return incidentService.getIncidentsByUser(userId).stream()
                .filter(i -> i.getRiskLevel() == RiskLevel.HIGH)
                .count();
    }
}
