package com.cybershield.model;

import java.time.LocalDate;

/**
 * OOP – Inheritance: extends Incident.
 * OOP – Polymorphism: overrides getTypeSpecificDetails().
 */
public class SuspiciousLinkIncident extends Incident {

    private boolean linkWasClicked;
    private boolean requestedCredentials;

    public SuspiciousLinkIncident() {
        super();
    }

    public SuspiciousLinkIncident(String incidentId, String userId, String title,
                                  String description, IncidentSource source,
                                  LocalDate incidentDate, String suspiciousUrl,
                                  boolean linkWasClicked, boolean requestedCredentials) {
        super(incidentId, userId, title, IncidentType.SUSPICIOUS_LINK,
                description, source, incidentDate, suspiciousUrl);
        this.linkWasClicked = linkWasClicked;
        this.requestedCredentials = requestedCredentials;
    }

    @Override
    public String getTypeSpecificDetails() {
        StringBuilder sb = new StringBuilder();
        sb.append("Suspicious link reported via ").append(getSource().getDisplayName()).append(". ");
        if (linkWasClicked) {
            sb.append("WARNING: Link was already clicked. ");
            if (requestedCredentials) {
                sb.append("The site requested credentials – change your passwords immediately. ");
            }
            sb.append("Scan your device for malware and monitor accounts for unusual activity.");
        } else {
            sb.append("Do NOT click the link. Record it for evidence and report to your provider.");
        }
        return sb.toString();
    }

    public boolean isLinkWasClicked() { return linkWasClicked; }
    public boolean isRequestedCredentials() { return requestedCredentials; }
    public void setLinkWasClicked(boolean v) { this.linkWasClicked = v; }
    public void setRequestedCredentials(boolean v) { this.requestedCredentials = v; }
}
