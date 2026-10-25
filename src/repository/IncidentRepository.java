package repository;

import model.Incident;
import model.IncidentStatus;
import model.IncidentType;
import model.RiskLevel;
import util.FileStorageUtil;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Handles persistence for Incident objects (all subtypes via polymorphism).
 */
public class IncidentRepository {

    private static final String FILE_NAME = "incidents.dat";

    private List<Incident> incidents;

    public IncidentRepository() {
        this.incidents = FileStorageUtil.loadAll(FILE_NAME);
    }

    // --- Query ---

    public List<Incident> findAll() {
        return new ArrayList<>(incidents);
    }

    public List<Incident> findByUserId(String userId) {
        return incidents.stream()
                        .filter(i -> i.getUserId().equals(userId))
                        .collect(Collectors.toList());
    }

    public Optional<Incident> findById(String incidentId) {
        return incidents.stream()
                        .filter(i -> i.getIncidentId().equals(incidentId))
                        .findFirst();
    }

    public List<Incident> findByUserIdAndType(String userId, IncidentType type) {
        return findByUserId(userId).stream()
                                   .filter(i -> i.getType() == type)
                                   .collect(Collectors.toList());
    }

    public List<Incident> findByUserIdAndRiskLevel(String userId, RiskLevel level) {
        return findByUserId(userId).stream()
                                   .filter(i -> i.getRiskLevel() == level)
                                   .collect(Collectors.toList());
    }

    public List<Incident> findByUserIdAndStatus(String userId, IncidentStatus status) {
        return findByUserId(userId).stream()
                                   .filter(i -> i.getStatus() == status)
                                   .collect(Collectors.toList());
    }

    public List<Incident> searchByUserIdAndKeyword(String userId, String keyword) {
        String lc = keyword.toLowerCase();
        return findByUserId(userId).stream()
                                   .filter(i -> i.getTitle().toLowerCase().contains(lc)
                                             || i.getDescription().toLowerCase().contains(lc))
                                   .collect(Collectors.toList());
    }

    // --- Mutation ---

    public void save(Incident incident) {
        incidents.removeIf(i -> i.getIncidentId().equals(incident.getIncidentId()));
        incidents.add(incident);
        persist();
    }

    public void delete(String incidentId) {
        incidents.removeIf(i -> i.getIncidentId().equals(incidentId));
        persist();
    }

    // --- Persistence ---

    private void persist() {
        try {
            FileStorageUtil.saveAll(FILE_NAME, incidents);
        } catch (IOException e) {
            System.err.println("[IncidentRepository] Failed to save incidents: " + e.getMessage());
        }
    }
}
