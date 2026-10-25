package model;

/**
 * Enum representing the category/type of a cyber incident.
 */
public enum IncidentType {
    PHISHING("Phishing"),
    SUSPICIOUS_LINK("Suspicious Link"),
    OTP_SCAM("OTP Scam"),
    IMPERSONATION("Impersonation"),
    SUSPICIOUS_EMAIL("Suspicious Email"),
    OTHER("Other");

    private final String displayName;

    IncidentType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    @Override
    public String toString() {
        return displayName;
    }
}
