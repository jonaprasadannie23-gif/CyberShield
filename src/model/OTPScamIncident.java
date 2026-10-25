package model;

import java.time.LocalDate;

/**
 * Represents an OTP / PIN / banking-credentials scam incident.
 *
 * OOP – Inheritance: extends Incident.
 * OOP – Polymorphism: overrides getTypeSpecificDetails() with OTP-specific guidance.
 */
public class OTPScamIncident extends Incident {

    private static final long serialVersionUID = 1L;

    private boolean otpWasShared;         // user already shared the OTP
    private boolean financialInfoShared;  // banking/card details were exposed

    // --- Constructors ---

    public OTPScamIncident(String incidentId, String userId, String title,
                           String description, IncidentSource source,
                           LocalDate incidentDate, String suspiciousUrl,
                           boolean otpWasShared, boolean financialInfoShared) {
        super(incidentId, userId, title, IncidentType.OTP_SCAM,
              description, source, incidentDate, suspiciousUrl);
        this.otpWasShared        = otpWasShared;
        this.financialInfoShared = financialInfoShared;
    }

    public OTPScamIncident() { super(); }

    // --- Polymorphism: meaningful override ---

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

    // --- Getters / Setters ---

    public boolean isOtpWasShared()        { return otpWasShared; }
    public boolean isFinancialInfoShared() { return financialInfoShared; }

    public void setOtpWasShared(boolean v)        { this.otpWasShared = v; }
    public void setFinancialInfoShared(boolean v) { this.financialInfoShared = v; }
}
