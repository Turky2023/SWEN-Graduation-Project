package com.gradproject.service;

import com.gradproject.dto.FeedbackDTO;
import com.gradproject.entity.*;
import com.gradproject.repository.FeedbackRepository;
import com.gradproject.repository.SubmissionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class FeedbackService {

    private static final Logger logger = LoggerFactory.getLogger(FeedbackService.class);

    private final FeedbackRepository feedbackRepository;
    private final SubmissionRepository submissionRepository;
    private final EmailService emailService;
    private final GroupService groupService;

    public FeedbackService(FeedbackRepository feedbackRepository, SubmissionRepository submissionRepository,
                           EmailService emailService, GroupService groupService) {
        this.feedbackRepository = feedbackRepository;
        this.submissionRepository = submissionRepository;
        this.emailService = emailService;
        this.groupService = groupService;
    }

    @Transactional
    public Feedback leaveFeedback(Long submissionId, FeedbackDTO dto) {
        Submission submission = submissionRepository.findById(submissionId)
                .orElseThrow(() -> new RuntimeException("Submission not found"));

        SubmissionStatus newStatus = SubmissionStatus.valueOf(dto.getStatus());
        SubmissionStatus oldStatus = submission.getStatus();
        submission.setStatus(newStatus);
        submissionRepository.save(submission);

        Feedback feedback = feedbackRepository.findBySubmissionId(submissionId).orElse(new Feedback());
        feedback.setComment(dto.getComment());
        feedback.setGrade(dto.getGrade());
        feedback.setSubmission(submission);
        feedback.setCreatedAt(java.time.LocalDateTime.now());

        Feedback saved = feedbackRepository.save(feedback);
        logger.info("AUDIT - Feedback left: submissionId={} status={} grade={}", submissionId, newStatus, dto.getGrade());

        ProjectGroup group = submission.getGroup();
        String milestoneName = submission.getMilestone().getTitle();
        boolean statusChanged = oldStatus != newStatus;

        String subject = "Review Update: " + milestoneName;

        StringBuilder body = new StringBuilder();
        body.append("Your supervisor has reviewed your submission for '").append(milestoneName).append("'.\n\n");
        if (statusChanged) {
            body.append("Status: ").append(oldStatus).append(" \u2192 ").append(newStatus).append("\n");
        } else {
            body.append("Status: ").append(newStatus).append(" (unchanged)\n");
        }
        body.append("Grade: ").append(dto.getGrade() != null ? dto.getGrade() : "N/A").append("\n");
        body.append("Comment: ").append(dto.getComment() != null && !dto.getComment().isBlank() ? dto.getComment() : "(no comment)").append("\n");

        groupService.getMembers(group.getId()).forEach(member ->
                emailService.sendEmail(member.getStudent().getEmail(), subject, body.toString()));

        return saved;
    }
}
