package com.cybershield.model;

/**
 * Enum representing the risk classification levels for a cyber incident.
 * OOP – Enum with behaviour (displayName, hexColor).
 */
public enum RiskLevel {
    LOW("Low", "#27ae60"),
    MEDIUM("Medium", "#f39c12"),
    HIGH("High", "#e74c3c");

    private final String displayName;
    private final String hexColor;

    RiskLevel(String displayName, String hexColor) {
        this.displayName = displayName;
        this.hexColor    = hexColor;
    }

    public String getDisplayName() { return displayName; }
    public String getHexColor()    { return hexColor; }

    @Override
    public String toString() { return displayName; }
}
