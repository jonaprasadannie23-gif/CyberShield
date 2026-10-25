package ui;

import java.awt.*;

/**
 * Centralised colour palette and font constants for the CyberShield UI.
 * Apply these values consistently across all panels to maintain a cohesive
 * dark cybersecurity-themed look.
 */
public final class Theme {

    // --- Background colours ---
    public static final Color BG_DARK       = new Color(13, 17, 23);      // deep navy
    public static final Color BG_PANEL      = new Color(22, 27, 34);      // panel background
    public static final Color BG_CARD       = new Color(30, 37, 46);      // card / table row
    public static final Color BG_SIDEBAR    = new Color(15, 20, 28);      // sidebar background
    public static final Color BG_INPUT      = new Color(36, 43, 53);      // input field bg

    // --- Accent colours ---
    public static final Color ACCENT_TEAL   = new Color(32, 201, 151);    // primary accent
    public static final Color ACCENT_BLUE   = new Color(56, 139, 253);    // secondary accent
    public static final Color ACCENT_HOVER  = new Color(48, 54, 61);      // hover state

    // --- Risk level colours ---
    public static final Color RISK_HIGH     = new Color(248, 81,  73);    // red
    public static final Color RISK_MEDIUM   = new Color(210, 153,  34);   // amber
    public static final Color RISK_LOW      = new Color(63, 185, 80);     // green

    // --- Text colours ---
    public static final Color TEXT_PRIMARY   = new Color(230, 237, 243);  // main text
    public static final Color TEXT_SECONDARY = new Color(139, 148, 158);  // muted text
    public static final Color TEXT_ACCENT    = ACCENT_TEAL;

    // --- Border ---
    public static final Color BORDER        = new Color(48, 54, 61);

    // --- Fonts ---
    public static final Font FONT_TITLE     = new Font("Segoe UI", Font.BOLD, 20);
    public static final Font FONT_SUBTITLE  = new Font("Segoe UI", Font.BOLD, 14);
    public static final Font FONT_BODY      = new Font("Segoe UI", Font.PLAIN, 13);
    public static final Font FONT_SMALL     = new Font("Segoe UI", Font.PLAIN, 11);
    public static final Font FONT_MONO      = new Font("Consolas", Font.PLAIN, 12);
    public static final Font FONT_LABEL     = new Font("Segoe UI", Font.BOLD, 12);

    // --- Status colours ---
    public static final Color STATUS_REPORTED     = new Color(56, 139, 253);
    public static final Color STATUS_UNDER_REVIEW = new Color(210, 153, 34);
    public static final Color STATUS_RESOLVED     = new Color(63, 185, 80);

    private Theme() {}

    /** Convenience: returns the appropriate risk colour for a given label. */
    public static Color riskColor(String risk) {
        if (risk == null) return TEXT_SECONDARY;
        switch (risk.toUpperCase()) {
            case "HIGH":   return RISK_HIGH;
            case "MEDIUM": return RISK_MEDIUM;
            case "LOW":    return RISK_LOW;
            default:       return TEXT_SECONDARY;
        }
    }

    public static Color statusColor(String status) {
        if (status == null) return TEXT_SECONDARY;
        switch (status.toUpperCase().replace(" ", "_")) {
            case "REPORTED":     return STATUS_REPORTED;
            case "UNDER_REVIEW": return STATUS_UNDER_REVIEW;
            case "RESOLVED":     return STATUS_RESOLVED;
            default:             return TEXT_SECONDARY;
        }
    }
}
