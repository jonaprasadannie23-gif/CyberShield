package com.cybershield.model;

public enum IncidentSource {
    SMS("SMS"),
    EMAIL("Email"),
    WEBSITE("Website"),
    SOCIAL_MEDIA("Social Media"),
    PHONE_CALL("Phone Call"),
    OTHER("Other");

    private final String displayName;

    IncidentSource(String displayName) { this.displayName = displayName; }

    public String getDisplayName() { return displayName; }

    @Override
    public String toString() { return displayName; }
}
