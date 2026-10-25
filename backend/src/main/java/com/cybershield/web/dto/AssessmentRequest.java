package com.cybershield.web.dto;

/**
 * Data transfer object for submitting answers to the 8 security practice questions.
 * The client does not supply a score; scoring is computed exclusively on the backend.
 */
public class AssessmentRequest {

    private String twoFactorAuth;
    private String passwordReuse;
    private String strongPasswords;
    private String systemUpdates;
    private String linkVerification;
    private String recoveryMethods;
    private String publicWifiPrecautions;
    private String phishingRecognition;

    public AssessmentRequest() {}

    public AssessmentRequest(String twoFactorAuth,
                             String passwordReuse,
                             String strongPasswords,
                             String systemUpdates,
                             String linkVerification,
                             String recoveryMethods,
                             String publicWifiPrecautions,
                             String phishingRecognition) {
        this.twoFactorAuth = twoFactorAuth;
        this.passwordReuse = passwordReuse;
        this.strongPasswords = strongPasswords;
        this.systemUpdates = systemUpdates;
        this.linkVerification = linkVerification;
        this.recoveryMethods = recoveryMethods;
        this.publicWifiPrecautions = publicWifiPrecautions;
        this.phishingRecognition = phishingRecognition;
    }

    public String getTwoFactorAuth() {
        return twoFactorAuth;
    }

    public void setTwoFactorAuth(String twoFactorAuth) {
        this.twoFactorAuth = twoFactorAuth;
    }

    public String getPasswordReuse() {
        return passwordReuse;
    }

    public void setPasswordReuse(String passwordReuse) {
        this.passwordReuse = passwordReuse;
    }

    public String getStrongPasswords() {
        return strongPasswords;
    }

    public void setStrongPasswords(String strongPasswords) {
        this.strongPasswords = strongPasswords;
    }

    public String getSystemUpdates() {
        return systemUpdates;
    }

    public void setSystemUpdates(String systemUpdates) {
        this.systemUpdates = systemUpdates;
    }

    public String getLinkVerification() {
        return linkVerification;
    }

    public void setLinkVerification(String linkVerification) {
        this.linkVerification = linkVerification;
    }

    public String getRecoveryMethods() {
        return recoveryMethods;
    }

    public void setRecoveryMethods(String recoveryMethods) {
        this.recoveryMethods = recoveryMethods;
    }

    public String getPublicWifiPrecautions() {
        return publicWifiPrecautions;
    }

    public void setPublicWifiPrecautions(String publicWifiPrecautions) {
        this.publicWifiPrecautions = publicWifiPrecautions;
    }

    public String getPhishingRecognition() {
        return phishingRecognition;
    }

    public void setPhishingRecognition(String phishingRecognition) {
        this.phishingRecognition = phishingRecognition;
    }
}
