package com.cybershield.web.dto;

import com.cybershield.model.IncidentSource;
import com.cybershield.model.IncidentType;

import java.time.LocalDate;

public class CreateIncidentRequest {
    private String title;
    private IncidentType type;
    private String description;
    private IncidentSource source;
    private LocalDate incidentDate;
    private String suspiciousUrl;
    private boolean claimedToBeLegitOrg;
    private boolean containedAttachment;
    private boolean linkWasClicked;
    private boolean requestedCredentials;
    private boolean otpWasShared;
    private boolean financialInfoShared;

    public String getTitle() { return title; }
    public IncidentType getType() { return type; }
    public String getDescription() { return description; }
    public IncidentSource getSource() { return source; }
    public LocalDate getIncidentDate() { return incidentDate; }
    public String getSuspiciousUrl() { return suspiciousUrl; }
    public boolean isClaimedToBeLegitOrg() { return claimedToBeLegitOrg; }
    public boolean isContainedAttachment() { return containedAttachment; }
    public boolean isLinkWasClicked() { return linkWasClicked; }
    public boolean isRequestedCredentials() { return requestedCredentials; }
    public boolean isOtpWasShared() { return otpWasShared; }
    public boolean isFinancialInfoShared() { return financialInfoShared; }

    public void setTitle(String title) { this.title = title; }
    public void setType(IncidentType type) { this.type = type; }
    public void setDescription(String description) { this.description = description; }
    public void setSource(IncidentSource source) { this.source = source; }
    public void setIncidentDate(LocalDate incidentDate) { this.incidentDate = incidentDate; }
    public void setSuspiciousUrl(String suspiciousUrl) { this.suspiciousUrl = suspiciousUrl; }
    public void setClaimedToBeLegitOrg(boolean claimedToBeLegitOrg) { this.claimedToBeLegitOrg = claimedToBeLegitOrg; }
    public void setContainedAttachment(boolean containedAttachment) { this.containedAttachment = containedAttachment; }
    public void setLinkWasClicked(boolean linkWasClicked) { this.linkWasClicked = linkWasClicked; }
    public void setRequestedCredentials(boolean requestedCredentials) { this.requestedCredentials = requestedCredentials; }
    public void setOtpWasShared(boolean otpWasShared) { this.otpWasShared = otpWasShared; }
    public void setFinancialInfoShared(boolean financialInfoShared) { this.financialInfoShared = financialInfoShared; }
}
