package com.cybershield.repository;

import com.cybershield.model.Incident;
import com.cybershield.model.IncidentStatus;
import com.cybershield.model.IncidentType;
import com.cybershield.model.RiskLevel;
import com.cybershield.storage.JsonFileStorage;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository
public class IncidentRepository {

    private static final String FILE_NAME = "incidents.json";

    private final JsonFileStorage storage;
    private final List<Incident> incidents;

    public IncidentRepository(JsonFileStorage storage) {
        this.storage = storage;
        this.incidents = storage.loadAll(FILE_NAME, Incident.class);
    }

    public synchronized List<Incident> findByUserId(String userId) {
        return incidents.stream()
                .filter(i -> i.getUserId().equals(userId))
                .collect(Collectors.toList());
    }

    public synchronized Optional<Incident> findById(String incidentId) {
        return incidents.stream()
                .filter(i -> i.getIncidentId().equals(incidentId))
                .findFirst();
    }

    public synchronized List<Incident> findByUserIdAndType(String userId, IncidentType type) {
        return findByUserId(userId).stream()
                .filter(i -> i.getType() == type)
                .collect(Collectors.toList());
    }

    public synchronized List<Incident> findByUserIdAndRiskLevel(String userId, RiskLevel level) {
        return findByUserId(userId).stream()
                .filter(i -> i.getRiskLevel() == level)
                .collect(Collectors.toList());
    }

    public synchronized List<Incident> findByUserIdAndStatus(String userId, IncidentStatus status) {
        return findByUserId(userId).stream()
                .filter(i -> i.getStatus() == status)
                .collect(Collectors.toList());
    }

    public synchronized List<Incident> searchByUserIdAndKeyword(String userId, String keyword) {
        String lc = keyword.toLowerCase();
        return findByUserId(userId).stream()
                .filter(i -> (i.getTitle() != null && i.getTitle().toLowerCase().contains(lc))
                        || (i.getDescription() != null && i.getDescription().toLowerCase().contains(lc)))
                .collect(Collectors.toList());
    }

    public synchronized void save(Incident incident) {
        incidents.removeIf(i -> i.getIncidentId().equals(incident.getIncidentId()));
        incidents.add(incident);
        persist();
    }

    public synchronized void delete(String incidentId) {
        incidents.removeIf(i -> i.getIncidentId().equals(incidentId));
        persist();
    }

    private void persist() {
        storage.saveAll(FILE_NAME, incidents, Incident.class);
    }
}
