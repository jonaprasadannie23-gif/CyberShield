package com.cybershield.web.dto;

import com.cybershield.model.IncidentStatus;

public class StatusUpdateRequest {
    private IncidentStatus status;

    public IncidentStatus getStatus() { return status; }
    public void setStatus(IncidentStatus status) { this.status = status; }
}
