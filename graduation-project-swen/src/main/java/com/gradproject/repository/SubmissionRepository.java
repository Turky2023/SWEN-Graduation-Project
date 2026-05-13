package com.gradproject.repository;

import com.gradproject.entity.Submission;
import com.gradproject.entity.SubmissionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface SubmissionRepository extends JpaRepository<Submission, Long> {
    List<Submission> findByGroupId(Long groupId);
    Optional<Submission> findByGroupIdAndMilestoneId(Long groupId, Long milestoneId);
    long countByGroupIdAndStatus(Long groupId, SubmissionStatus status);
}
