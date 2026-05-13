package com.gradproject.service;

import com.gradproject.entity.*;
import com.gradproject.repository.MilestoneRepository;
import com.gradproject.repository.ProjectGroupRepository;
import com.gradproject.repository.SubmissionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class SubmissionService {

    private static final Logger logger = LoggerFactory.getLogger(SubmissionService.class);

    private final SubmissionRepository submissionRepository;
    private final MilestoneRepository milestoneRepository;
    private final ProjectGroupRepository groupRepository;
    private final EmailService emailService;

    public SubmissionService(SubmissionRepository submissionRepository, MilestoneRepository milestoneRepository,
                             ProjectGroupRepository groupRepository, EmailService emailService) {
        this.submissionRepository = submissionRepository;
        this.milestoneRepository = milestoneRepository;
        this.groupRepository = groupRepository;
        this.emailService = emailService;
    }

    public Submission upload(Long groupId, Long milestoneId, MultipartFile file) throws IOException {
        ProjectGroup group = groupRepository.findById(groupId)
                .orElseThrow(() -> new RuntimeException("Group not found"));
        Milestone milestone = milestoneRepository.findById(milestoneId)
                .orElseThrow(() -> new RuntimeException("Milestone not found"));

        if (LocalDateTime.now().isAfter(milestone.getDeadline())) {
            throw new RuntimeException("Deadline has passed. Upload is not allowed.");
        }

        Optional<Submission> existing = submissionRepository.findByGroupIdAndMilestoneId(groupId, milestoneId);
        if (existing.isPresent()) {

            Submission sub = existing.get();
            sub.setFileName(file.getOriginalFilename());
            sub.setFileData(file.getBytes());
            sub.setFileType(file.getContentType());
            sub.setStatus(SubmissionStatus.PENDING);
            sub.setUploadedAt(LocalDateTime.now());
            Submission saved = submissionRepository.save(sub);
            logger.info("AUDIT - Submission replaced: id={} groupId={} milestoneId={} fileName={}",
                    saved.getId(), groupId, milestoneId, file.getOriginalFilename());
            notifySupervisor(group, milestone, true);
            return saved;
        }

        Submission submission = new Submission();
        submission.setFileName(file.getOriginalFilename());
        submission.setFileData(file.getBytes());
        submission.setFileType(file.getContentType());
        submission.setGroup(group);
        submission.setMilestone(milestone);
        submission.setStatus(SubmissionStatus.PENDING);
        submission.setUploadedAt(LocalDateTime.now());

        Submission saved = submissionRepository.save(submission);
        logger.info("AUDIT - Submission uploaded: id={} groupId={} milestoneId={} fileName={}",
                saved.getId(), groupId, milestoneId, file.getOriginalFilename());
        notifySupervisor(group, milestone, false);
        return saved;
    }

    private void notifySupervisor(ProjectGroup group, Milestone milestone, boolean isReplacement) {
        if (group.getSupervisor() == null) return;
        String action = isReplacement ? "re-submitted" : "submitted";
        String subject = "Group '" + group.getName() + "' " + action + " " + milestone.getTitle();
        String body = "The group '" + group.getName() + "' has " + action +
                " their work for milestone '" + milestone.getTitle() + "'.\n\n" +
                "Please log in to the portal to review the submission.";
        emailService.sendEmail(group.getSupervisor().getEmail(), subject, body);
    }

    public void delete(Long submissionId) {
        Submission submission = submissionRepository.findById(submissionId)
                .orElseThrow(() -> new RuntimeException("Submission not found"));

        if (LocalDateTime.now().isAfter(submission.getMilestone().getDeadline())) {
            throw new RuntimeException("Deadline has passed. Deletion is not allowed.");
        }

        ProjectGroup group = submission.getGroup();
        Milestone milestone = submission.getMilestone();
        SubmissionStatus previousStatus = submission.getStatus();

        logger.info("AUDIT - Submission deleted: id={} groupId={} milestoneId={}",
                submissionId, group.getId(), milestone.getId());
        submissionRepository.delete(submission);

        notifySupervisorOnDelete(group, milestone, previousStatus);
    }

    private void notifySupervisorOnDelete(ProjectGroup group, Milestone milestone, SubmissionStatus previousStatus) {
        if (group.getSupervisor() == null) return;
        String subject = "Group '" + group.getName() + "' deleted submission for " + milestone.getTitle();
        String body = "The group '" + group.getName() + "' has deleted their submission for milestone '" +
                milestone.getTitle() + "'.\n\n" +
                "Status was: " + previousStatus + "\n\n" +
                "Please log in to the portal for details.";
        emailService.sendEmail(group.getSupervisor().getEmail(), subject, body);
    }

    public Submission findById(Long id) {
        return submissionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Submission not found"));
    }

    public List<Submission> findByGroupId(Long groupId) {
        return submissionRepository.findByGroupId(groupId);
    }

    public int calculateProgress(Long groupId) {

        int totalMilestoneTypes = MilestoneType.values().length;
        long accepted = submissionRepository.countByGroupIdAndStatus(groupId, SubmissionStatus.ACCEPTED);
        return (int) ((accepted * 100) / totalMilestoneTypes);
    }
}
