package com.cybershield.model;

import java.time.LocalDate;

/**
 * OOP – Inheritance: extends Incident.
 * OOP – Polymorphism: overrides getTypeSpecificDetails().
 */
public class OTPScamIncident extends Incident {

    private boolean otpWasShared;
    private boolean financialInfoShared;

    public OTPScamIncident() {
        super();
    }

    public OTPScamIncident(String incidentId, String userId, String title,
                           String description, IncidentSource source,
                           LocalDate incidentDate, String suspiciousUrl,
                           boolean otpWasShared, boolean financialInfoShared) {
        super(incidentId, userId, title, IncidentType.OTP_SCAM,
                description, source, incidentDate, suspiciousUrl);
        this.otpWasShared = otpWasShared;
        this.financialInfoShared = financialInfoShared;
    }

    @Override
    public String getTypeSpecificDetails() {
        StringBuilder sb = new StringBuilder();
        sb.append("OTP/PIN scam via ").append(getSource().getDisplayName()).append(". ");
        if (otpWasShared) {
            sb.append("CRITICAL: OTP was already shared with the attacker. ");
        }
        if (financialInfoShared) {
            sb.append("CRITICAL: Financial information was disclosed. ");
            sb.append("Contact your bank IMMEDIATELY to block accounts and report fraud. ");
        }
        if (!otpWasShared && !financialInfoShared) {
            sb.append("No information was shared. Do NOT respond and block the sender. ");
        }
        sb.append("Legitimate organisations never request OTPs via phone, SMS, or chat.");
        return sb.toString();
    }

    public boolean isOtpWasShared() { return otpWasShared; }
    public boolean isFinancialInfoShared() { return financialInfoShared; }
    public void setOtpWasShared(boolean v) { this.otpWasShared = v; }
    public void setFinancialInfoShared(boolean v) { this.financialInfoShared = v; }
}
