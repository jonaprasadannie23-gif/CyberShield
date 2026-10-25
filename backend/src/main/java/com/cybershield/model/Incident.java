package com.cybershield.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Base class for all cyber incidents.
 *
 * OOP – Inheritance: PhishingIncident, SuspiciousLinkIncident, OTPScamIncident extend this.
 * OOP – Encapsulation: private fields, validated setters.
 * OOP – Polymorphism: getTypeSpecificDetails() overridden in each subclass.
 *
 * Jackson @JsonTypeInfo/@JsonSubTypes allow polymorphic JSON serialisation so that
 * the correct subclass is reconstructed when loading from the JSON data file.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "incidentClass")
@JsonSubTypes({
    @JsonSubTypes.Type(value = PhishingIncident.class,       name = "PHISHING"),
    @JsonSubTypes.Type(value = SuspiciousLinkIncident.class, name = "SUSPICIOUS_LINK"),
    @JsonSubTypes.Type(value = OTPScamIncident.class,        name = "OTP_SCAM"),
    @JsonSubTypes.Type(value = Incident.class,               name = "GENERIC")
})
public class Incident {

    private String incidentId;
    private String userId;
    private String title;
    private IncidentType type;
    private String description;
    private IncidentSource source;
    private LocalDate incidentDate;
    private String suspiciousUrl;
    private RiskLevel riskLevel;
    private IncidentStatus status;
    private LocalDateTime reportedAt;
    private List<String> assessmentReasons = new ArrayList<>();
    private List<String> recommendations   = new ArrayList<>();

    // --- Constructors ---

    public Incident() {}

    public Incident(String incidentId, String userId, String title,
                    IncidentType type, String description, IncidentSource source,
                    LocalDate incidentDate, String suspiciousUrl) {
        this.incidentId   = incidentId;
        this.userId       = userId;
        this.title        = title;
        this.type         = type;
        this.description  = description;
        this.source       = source;
        this.incidentDate = incidentDate;
        this.suspiciousUrl = suspiciousUrl;
        this.status       = IncidentStatus.REPORTED;
        this.reportedAt   = LocalDateTime.now();
    }

    // --- Polymorphic method ---

    /**
     * Returns type-specific guidance. Overridden in subclasses.
     * Runtime polymorphism is demonstrated when iterating a List<Incident>.
     */
    public String getTypeSpecificDetails() {
        return "General cyber incident. Review the description carefully.";
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
    public List<String> getAssessmentReasons() { return new ArrayList<>(assessmentReasons); }
    public List<String> getRecommendations()   { return new ArrayList<>(recommendations); }

    // --- Setters ---
    public void setIncidentId(String id)         { this.incidentId = id; }
    public void setUserId(String userId)         { this.userId = userId; }
    public void setTitle(String title)           { if (title != null && !title.isBlank()) this.title = title; }
    public void setType(IncidentType type)       { this.type = type; }
    public void setDescription(String d)         { if (d != null && !d.isBlank()) this.description = d; }
    public void setSource(IncidentSource s)      { if (s != null) this.source = s; }
    public void setIncidentDate(LocalDate d)     { if (d != null) this.incidentDate = d; }
    public void setSuspiciousUrl(String url)     { this.suspiciousUrl = url; }
    public void setRiskLevel(RiskLevel r)        { if (r != null) this.riskLevel = r; }
    public void setStatus(IncidentStatus s)      { if (s != null) this.status = s; }
    public void setReportedAt(LocalDateTime t)   { this.reportedAt = t; }
    public void setAssessmentReasons(List<String> r) { if (r != null) this.assessmentReasons = new ArrayList<>(r); }
    public void setRecommendations(List<String> r)   { if (r != null) this.recommendations = new ArrayList<>(r); }
}
