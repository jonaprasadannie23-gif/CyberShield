package service;

import model.*;
import repository.IncidentRepository;
import util.IDGenerator;
import util.InputValidator;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Business logic for creating, updating, and deleting incidents.
 * Connects the UI to the repository and runs risk assessment on every new incident.
 */
public class IncidentService {

    private final IncidentRepository incidentRepository;
    private final RiskAssessor assessor;

    public IncidentService(IncidentRepository incidentRepository, RiskAssessor assessor) {
        this.incidentRepository = incidentRepository;
        this.assessor           = assessor;
    }

    // -----------------------------------------------------------------------
    // Create
    // -----------------------------------------------------------------------

    /**
     * Creates and persists a new incident after validation.
     * Risk assessment is run automatically.
     */
    public Incident createIncident(String userId, String title, IncidentType type,
                                   String description, IncidentSource source,
                                   LocalDate incidentDate, String suspiciousUrl)
            throws IllegalArgumentException {

        validateRequiredFields(title, description, type, source, incidentDate);

        String id = IDGenerator.generate();
        String safeUrl = InputValidator.sanitiseUrl(suspiciousUrl);

        Incident incident = buildIncident(id, userId, title.trim(), type,
                                          description.trim(), source,
                                          incidentDate, safeUrl);

        // Run risk assessment
        AssessmentResult result = assessor.assess(incident);
        incident.setRiskLevel(result.getRiskLevel());
        incident.setAssessmentReasons(result.getReasons());
        incident.setRecommendations(result.getRecommendations());

        incidentRepository.save(incident);
        return incident;
    }

    /**
     * Factory method: creates the appropriate subclass based on IncidentType.
     * Generic fields are handled via base constructor; subclass-specific fields
     * are left at defaults (false) for general form submissions.
     */
    private Incident buildIncident(String id, String userId, String title,
                                   IncidentType type, String description,
                                   IncidentSource source, LocalDate date,
                                   String url) {
        switch (type) {
            case PHISHING:        return new PhishingIncident(id, userId, title, description, source, date, url, false, false);
            case SUSPICIOUS_LINK: return new SuspiciousLinkIncident(id, userId, title, description, source, date, url, false, false);
            case OTP_SCAM:        return new OTPScamIncident(id, userId, title, description, source, date, url, false, false);
            default:              return new Incident(id, userId, title, type, description, source, date, url);
        }
    }

    // -----------------------------------------------------------------------
    // Read
    // -----------------------------------------------------------------------

    public List<Incident> getIncidentsByUser(String userId) {
        return incidentRepository.findByUserId(userId);
    }

    public Optional<Incident> getIncidentById(String incidentId) {
        return incidentRepository.findById(incidentId);
    }

    public List<Incident> searchIncidents(String userId, String keyword) {
        if (keyword == null || keyword.isBlank()) return getIncidentsByUser(userId);
        return incidentRepository.searchByUserIdAndKeyword(userId, keyword);
    }

    public List<Incident> filterByType(String userId, IncidentType type) {
        return incidentRepository.findByUserIdAndType(userId, type);
    }

    public List<Incident> filterByRiskLevel(String userId, RiskLevel level) {
        return incidentRepository.findByUserIdAndRiskLevel(userId, level);
    }

    public List<Incident> filterByStatus(String userId, IncidentStatus status) {
        return incidentRepository.findByUserIdAndStatus(userId, status);
    }

    // -----------------------------------------------------------------------
    // Update
    // -----------------------------------------------------------------------

    public void updateStatus(String incidentId, String userId, IncidentStatus newStatus)
            throws IllegalArgumentException {
        Incident incident = getOwnedIncident(incidentId, userId);
        incident.setStatus(newStatus);
        // Re-run assessment after update
        AssessmentResult result = assessor.assess(incident);
        incident.setRiskLevel(result.getRiskLevel());
        incident.setAssessmentReasons(result.getReasons());
        incident.setRecommendations(result.getRecommendations());
        incidentRepository.save(incident);
    }

    public void updateIncident(String incidentId, String userId,
                               String title, String description,
                               IncidentSource source, LocalDate date,
                               String suspiciousUrl)
            throws IllegalArgumentException {
        if (!InputValidator.isNotBlank(title))
            throw new IllegalArgumentException("Title is required.");
        if (!InputValidator.isNotBlank(description))
            throw new IllegalArgumentException("Description is required.");

        Incident incident = getOwnedIncident(incidentId, userId);
        incident.setTitle(title.trim());
        incident.setDescription(description.trim());
        incident.setSource(source);
        incident.setIncidentDate(date);
        incident.setSuspiciousUrl(InputValidator.sanitiseUrl(suspiciousUrl));

        // Re-run assessment
        AssessmentResult result = assessor.assess(incident);
        incident.setRiskLevel(result.getRiskLevel());
        incident.setAssessmentReasons(result.getReasons());
        incident.setRecommendations(result.getRecommendations());

        incidentRepository.save(incident);
    }

    // -----------------------------------------------------------------------
    // Delete
    // -----------------------------------------------------------------------

    public void deleteIncident(String incidentId, String userId)
            throws IllegalArgumentException {
        getOwnedIncident(incidentId, userId);   // ownership check
        incidentRepository.delete(incidentId);
    }

    // -----------------------------------------------------------------------
    // Statistics
    // -----------------------------------------------------------------------

    public long countByUser(String userId) {
        return getIncidentsByUser(userId).size();
    }

    public long countHighRiskByUser(String userId) {
        return getIncidentsByUser(userId).stream()
                .filter(i -> i.getRiskLevel() == RiskLevel.HIGH).count();
    }

    // -----------------------------------------------------------------------
    // Helpers
    // -----------------------------------------------------------------------

    private void validateRequiredFields(String title, String description,
                                        IncidentType type, IncidentSource source,
                                        LocalDate date) {
        if (!InputValidator.isNotBlank(title))
            throw new IllegalArgumentException("Incident title is required.");
        if (!InputValidator.isNotBlank(description))
            throw new IllegalArgumentException("Description is required.");
        if (type == null)
            throw new IllegalArgumentException("Incident type must be selected.");
        if (source == null)
            throw new IllegalArgumentException("Incident source must be selected.");
        if (date == null)
            throw new IllegalArgumentException("Incident date is required.");
        if (date.isAfter(LocalDate.now()))
            throw new IllegalArgumentException("Incident date cannot be in the future.");
    }

    private Incident getOwnedIncident(String incidentId, String userId) {
        Optional<Incident> opt = incidentRepository.findById(incidentId);
        if (opt.isEmpty())
            throw new IllegalArgumentException("Incident not found.");
        Incident i = opt.get();
        if (!i.getUserId().equals(userId))
            throw new IllegalArgumentException("Access denied: you do not own this incident.");
        return i;
    }
}
