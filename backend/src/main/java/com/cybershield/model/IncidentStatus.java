package com.cybershield.model;

public enum IncidentStatus {
    REPORTED("Reported"),
    UNDER_REVIEW("Under Review"),
    RESOLVED("Resolved");

    private final String displayName;

    IncidentStatus(String displayName) { this.displayName = displayName; }

    public String getDisplayName() { return displayName; }

    @Override
    public String toString() { return displayName; }
}
