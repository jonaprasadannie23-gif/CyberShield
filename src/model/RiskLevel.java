package model;

/**
 * Enum representing the risk classification levels for a cyber incident.
 * Demonstrates use of Enums for type-safe constants.
 */
public enum RiskLevel {
    LOW("Low", "#27ae60"),
    MEDIUM("Medium", "#f39c12"),
    HIGH("High", "#e74c3c");

    private final String displayName;
    private final String hexColor;

    RiskLevel(String displayName, String hexColor) {
        this.displayName = displayName;
        this.hexColor = hexColor;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getHexColor() {
        return hexColor;
    }

    @Override
    public String toString() {
        return displayName;
    }
}
