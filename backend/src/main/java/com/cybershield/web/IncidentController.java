package com.cybershield.web;

import com.cybershield.model.Incident;
import com.cybershield.model.IncidentStatus;
import com.cybershield.model.IncidentType;
import com.cybershield.model.RiskLevel;
import com.cybershield.service.IncidentService;
import com.cybershield.web.dto.CreateIncidentRequest;
import com.cybershield.web.dto.StatusUpdateRequest;
import com.cybershield.web.dto.UpdateIncidentRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/incidents")
public class IncidentController {

    private final IncidentService incidentService;

    public IncidentController(IncidentService incidentService) {
        this.incidentService = incidentService;
    }

    @PostMapping
    public ResponseEntity<Incident> create(@RequestBody CreateIncidentRequest request, HttpSession session) {
        Incident created = incidentService.createIncident(SessionUser.requireUserId(session), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping
    public List<Incident> list(@RequestParam(required = false) String keyword,
                               @RequestParam(required = false) IncidentType type,
                               @RequestParam(required = false) RiskLevel risk,
                               @RequestParam(required = false) IncidentStatus status,
                               HttpSession session) {
        return incidentService.search(SessionUser.requireUserId(session), keyword, type, risk, status);
    }

    @GetMapping("/{id}")
    public Incident get(@PathVariable String id, HttpSession session) {
        return incidentService.getOwnedIncident(id, SessionUser.requireUserId(session));
    }

    @PutMapping("/{id}")
    public Incident update(@PathVariable String id,
                           @RequestBody UpdateIncidentRequest request,
                           HttpSession session) {
        String userId = SessionUser.requireUserId(session);
        Incident updated = incidentService.updateIncident(id, userId, request);
        if (request.getStatus() != null) {
            updated = incidentService.updateStatus(id, userId, request.getStatus());
        }
        return updated;
    }

    @PatchMapping("/{id}/status")
    public Incident updateStatus(@PathVariable String id,
                                 @RequestBody StatusUpdateRequest request,
                                 HttpSession session) {
        return incidentService.updateStatus(id, SessionUser.requireUserId(session), request.getStatus());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable String id, HttpSession session) {
        incidentService.deleteIncident(id, SessionUser.requireUserId(session));
        return ResponseEntity.noContent().build();
    }
}
