package com.cybershield.model;

import java.time.LocalDate;

/**
 * OOP – Inheritance: extends Incident.
 * OOP – Polymorphism: overrides getTypeSpecificDetails().
 */
public class PhishingIncident extends Incident {

    private boolean claimedToBeLegitOrg;
    private boolean containedAttachment;

    public PhishingIncident() {
        super();
    }

    public PhishingIncident(String incidentId, String userId, String title,
                            String description, IncidentSource source,
                            LocalDate incidentDate, String suspiciousUrl,
                            boolean claimedToBeLegitOrg, boolean containedAttachment) {
        super(incidentId, userId, title, IncidentType.PHISHING,
                description, source, incidentDate, suspiciousUrl);
        this.claimedToBeLegitOrg = claimedToBeLegitOrg;
        this.containedAttachment = containedAttachment;
    }

    @Override
    public String getTypeSpecificDetails() {
        StringBuilder sb = new StringBuilder();
        sb.append("Phishing attempt detected via ").append(getSource().getDisplayName()).append(". ");
        if (claimedToBeLegitOrg) {
            sb.append("Sender impersonated a legitimate organisation. ");
        }
        if (containedAttachment) {
            sb.append("Message contained a suspicious attachment – do NOT open it. ");
        }
        sb.append("Do not click any links and report to the impersonated organisation.");
        return sb.toString();
    }

    public boolean isClaimedToBeLegitOrg() { return claimedToBeLegitOrg; }
    public boolean isContainedAttachment() { return containedAttachment; }
    public void setClaimedToBeLegitOrg(boolean v) { this.claimedToBeLegitOrg = v; }
    public void setContainedAttachment(boolean v) { this.containedAttachment = v; }
}
