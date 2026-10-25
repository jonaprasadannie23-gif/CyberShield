package com.cybershield.repository;

import com.cybershield.model.SecurityAssessment;
import com.cybershield.storage.JsonFileStorage;
import org.springframework.stereotype.Repository;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository
public class AssessmentRepository {

    private static final String FILE_NAME = "assessments.json";

    private final JsonFileStorage storage;
    private final List<SecurityAssessment> assessments;

    public AssessmentRepository(JsonFileStorage storage) {
        this.storage = storage;
        this.assessments = storage.loadAll(FILE_NAME, SecurityAssessment.class);
    }

    public synchronized List<SecurityAssessment> findByUserId(String userId) {
        return assessments.stream()
                .filter(a -> a.getUserId() != null && a.getUserId().equals(userId))
                .sorted(Comparator.comparing(SecurityAssessment::getCreatedAt, Comparator.nullsLast(Comparator.reverseOrder())))
                .collect(Collectors.toList());
    }

    public synchronized Optional<SecurityAssessment> findById(String assessmentId) {
        return assessments.stream()
                .filter(a -> a.getAssessmentId() != null && a.getAssessmentId().equals(assessmentId))
                .findFirst();
    }

    public synchronized Optional<SecurityAssessment> findLatestByUserId(String userId) {
        return findByUserId(userId).stream().findFirst();
    }

    public synchronized void save(SecurityAssessment assessment) {
        assessments.removeIf(a -> a.getAssessmentId().equals(assessment.getAssessmentId()));
        assessments.add(assessment);
        persist();
    }

    public synchronized void delete(String assessmentId) {
        assessments.removeIf(a -> a.getAssessmentId().equals(assessmentId));
        persist();
    }

    private void persist() {
        storage.saveAll(FILE_NAME, assessments, SecurityAssessment.class);
    }
}
