package com.cybershield.service;

import com.cybershield.exception.ForbiddenException;
import com.cybershield.exception.NotFoundException;
import com.cybershield.model.AssessmentResult;
import com.cybershield.model.Incident;
import com.cybershield.model.IncidentSource;
import com.cybershield.model.IncidentStatus;
import com.cybershield.model.IncidentType;
import com.cybershield.model.OTPScamIncident;
import com.cybershield.model.PhishingIncident;
import com.cybershield.model.RiskLevel;
import com.cybershield.model.SuspiciousLinkIncident;
import com.cybershield.repository.IncidentRepository;
import com.cybershield.util.IDGenerator;
import com.cybershield.util.InputValidator;
import com.cybershield.web.dto.CreateIncidentRequest;
import com.cybershield.web.dto.UpdateIncidentRequest;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class IncidentService {

    private final IncidentRepository incidentRepository;
    private final RiskAssessor assessor;

    public IncidentService(IncidentRepository incidentRepository, RiskAssessor assessor) {
        this.incidentRepository = incidentRepository;
        this.assessor = assessor;
    }

    public Incident createIncident(String userId, CreateIncidentRequest request) {
        validateRequiredFields(request.getTitle(), request.getDescription(),
                request.getType(), request.getSource(), request.getIncidentDate());

        String id = IDGenerator.generate();
        String safeUrl = InputValidator.sanitiseUrl(request.getSuspiciousUrl());
        Incident incident = buildIncident(id, userId, request.getTitle().trim(), request.getType(),
                request.getDescription().trim(), request.getSource(),
                request.getIncidentDate(), safeUrl, request);

        applyAssessment(incident);
        incidentRepository.save(incident);
        return incident;
    }

    public List<Incident> getIncidentsByUser(String userId) {
        return incidentRepository.findByUserId(userId);
    }

    public Incident getOwnedIncident(String incidentId, String userId) {
        Incident incident = incidentRepository.findById(incidentId)
                .orElseThrow(() -> new NotFoundException("Incident not found."));
        if (!incident.getUserId().equals(userId)) {
            throw new ForbiddenException("Access denied: you do not own this incident.");
        }
        return incident;
    }

    public List<Incident> search(String userId, String keyword, IncidentType type,
                                 RiskLevel risk, IncidentStatus status) {
        List<Incident> result;
        if (keyword != null && !keyword.isBlank()) {
            result = incidentRepository.searchByUserIdAndKeyword(userId, keyword);
        } else {
            result = incidentRepository.findByUserId(userId);
        }
        if (type != null) {
            result = result.stream().filter(i -> i.getType() == type).toList();
        }
        if (risk != null) {
            result = result.stream().filter(i -> i.getRiskLevel() == risk).toList();
        }
        if (status != null) {
            result = result.stream().filter(i -> i.getStatus() == status).toList();
        }
        return result;
    }

    public Incident updateStatus(String incidentId, String userId, IncidentStatus newStatus) {
        if (newStatus == null) throw new IllegalArgumentException("Status is required.");
        Incident incident = getOwnedIncident(incidentId, userId);
        incident.setStatus(newStatus);
        applyAssessment(incident);
        incidentRepository.save(incident);
        return incident;
    }

    public Incident updateIncident(String incidentId, String userId, UpdateIncidentRequest request) {
        if (!InputValidator.isNotBlank(request.getTitle()))
            throw new IllegalArgumentException("Title is required.");
        if (!InputValidator.isNotBlank(request.getDescription()))
            throw new IllegalArgumentException("Description is required.");
        if (request.getSource() == null)
            throw new IllegalArgumentException("Incident source must be selected.");
        if (request.getIncidentDate() == null)
            throw new IllegalArgumentException("Incident date is required.");
        if (request.getIncidentDate().isAfter(LocalDate.now()))
            throw new IllegalArgumentException("Incident date cannot be in the future.");

        Incident incident = getOwnedIncident(incidentId, userId);
        incident.setTitle(request.getTitle().trim());
        incident.setDescription(request.getDescription().trim());
        incident.setSource(request.getSource());
        incident.setIncidentDate(request.getIncidentDate());
        incident.setSuspiciousUrl(InputValidator.sanitiseUrl(request.getSuspiciousUrl()));
        applyTypeFlags(incident, request);
        applyAssessment(incident);
        incidentRepository.save(incident);
        return incident;
    }

    public void deleteIncident(String incidentId, String userId) {
        getOwnedIncident(incidentId, userId);
        incidentRepository.delete(incidentId);
    }

    private void applyAssessment(Incident incident) {
        AssessmentResult result = assessor.assess(incident);
        incident.setRiskLevel(result.getRiskLevel());
        incident.setAssessmentReasons(result.getReasons());
        incident.setRecommendations(result.getRecommendations());
    }

    private Incident buildIncident(String id, String userId, String title,
                                   IncidentType type, String description,
                                   IncidentSource source, LocalDate date,
                                   String url, CreateIncidentRequest request) {
        return switch (type) {
            case PHISHING -> new PhishingIncident(id, userId, title, description, source, date, url,
                    request.isClaimedToBeLegitOrg(), request.isContainedAttachment());
            case SUSPICIOUS_LINK -> new SuspiciousLinkIncident(id, userId, title, description, source, date, url,
                    request.isLinkWasClicked(), request.isRequestedCredentials());
            case OTP_SCAM -> new OTPScamIncident(id, userId, title, description, source, date, url,
                    request.isOtpWasShared(), request.isFinancialInfoShared());
            default -> new Incident(id, userId, title, type, description, source, date, url);
        };
    }

    private void applyTypeFlags(Incident incident, UpdateIncidentRequest request) {
        if (incident instanceof PhishingIncident pi) {
            pi.setClaimedToBeLegitOrg(request.isClaimedToBeLegitOrg());
            pi.setContainedAttachment(request.isContainedAttachment());
        } else if (incident instanceof SuspiciousLinkIncident sli) {
            sli.setLinkWasClicked(request.isLinkWasClicked());
            sli.setRequestedCredentials(request.isRequestedCredentials());
        } else if (incident instanceof OTPScamIncident otp) {
            otp.setOtpWasShared(request.isOtpWasShared());
            otp.setFinancialInfoShared(request.isFinancialInfoShared());
        }
    }

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
}
