package model;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.ArrayList;

/**
 * Base class for all cyber incidents.
 *
 * OOP – Inheritance: PhishingIncident, SuspiciousLinkIncident, OTPScamIncident
 *        all extend this class.
 * OOP – Encapsulation: fields are private; subclasses use protected setters.
 * OOP – Abstraction: getTypeSpecificDetails() is declared here and overridden
 *        in every subclass to return meaningful, type-specific information.
 */
public class Incident implements Serializable {

    private static final long serialVersionUID = 1L;

    // --- Core fields ---
    private String incidentId;
    private String userId;           // owner
    private String title;
    private IncidentType type;
    private String description;
    private IncidentSource source;
    private LocalDate incidentDate;
    private String suspiciousUrl;    // stored as text only; never opened
    private RiskLevel riskLevel;
    private IncidentStatus status;
    private LocalDateTime reportedAt;
    private List<String> assessmentReasons;
    private List<String> recommendations;

    // --- Constructors ---

    public Incident(String incidentId, String userId, String title,
                    IncidentType type, String description, IncidentSource source,
                    LocalDate incidentDate, String suspiciousUrl) {
        this.incidentId      = incidentId;
        this.userId          = userId;
        this.title           = title;
        this.type            = type;
        this.description     = description;
        this.source          = source;
        this.incidentDate    = incidentDate;
        this.suspiciousUrl   = suspiciousUrl;
        this.status          = IncidentStatus.REPORTED;
        this.reportedAt      = LocalDateTime.now();
        this.assessmentReasons  = new ArrayList<>();
        this.recommendations    = new ArrayList<>();
    }

    public Incident() {}

    // --- Overridable method (Polymorphism) ---

    /**
     * Returns a brief summary of type-specific characteristics.
     * Subclasses override this to provide meaningful, concrete behaviour.
     * This demonstrates runtime polymorphism when iterating over a List<Incident>.
     */
    public String getTypeSpecificDetails() {
        return "General cyber incident. Review description carefully.";
    }

    // --- Getters ---

    public String getIncidentId()     { return incidentId; }
    public String getUserId()         { return userId; }
    public String getTitle()          { return title; }
    public IncidentType getType()     { return type; }
    public String getDescription()    { return description; }
    public IncidentSource getSource() { return source; }
    public LocalDate getIncidentDate(){ return incidentDate; }
    public String getSuspiciousUrl()  { return suspiciousUrl; }
    public RiskLevel getRiskLevel()   { return riskLevel; }
    public IncidentStatus getStatus() { return status; }
    public LocalDateTime getReportedAt() { return reportedAt; }
    public List<String> getAssessmentReasons()  { return new ArrayList<>(assessmentReasons); }
    public List<String> getRecommendations()    { return new ArrayList<>(recommendations); }

    // --- Setters with validation ---

    public void setTitle(String title) {
        if (title != null && !title.isBlank()) this.title = title;
    }

    public void setDescription(String description) {
        if (description != null && !description.isBlank()) this.description = description;
    }

    public void setSource(IncidentSource source) {
        if (source != null) this.source = source;
    }

    public void setIncidentDate(LocalDate date) {
        if (date != null) this.incidentDate = date;
    }

    public void setSuspiciousUrl(String url) {
        this.suspiciousUrl = url;
    }

    public void setRiskLevel(RiskLevel riskLevel) {
        if (riskLevel != null) this.riskLevel = riskLevel;
    }

    public void setStatus(IncidentStatus status) {
        if (status != null) this.status = status;
    }

    public void setAssessmentReasons(List<String> reasons) {
        if (reasons != null) this.assessmentReasons = new ArrayList<>(reasons);
    }

    public void setRecommendations(List<String> recommendations) {
        if (recommendations != null) this.recommendations = new ArrayList<>(recommendations);
    }

    @Override
    public String toString() {
        return "Incident{id='" + incidentId + "', title='" + title +
               "', type=" + type + ", risk=" + riskLevel + "}";
    }
}
