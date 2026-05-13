package com.gradproject.service;

import com.gradproject.dto.MilestoneDTO;
import com.gradproject.entity.Milestone;
import com.gradproject.entity.MilestoneType;
import com.gradproject.entity.ProjectGroup;
import com.gradproject.repository.MilestoneRepository;
import com.gradproject.repository.ProjectGroupRepository;
import com.gradproject.repository.SubmissionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class MilestoneService {

    private static final Logger logger = LoggerFactory.getLogger(MilestoneService.class);

    private final MilestoneRepository milestoneRepository;
    private final ProjectGroupRepository groupRepository;
    private final SubmissionRepository submissionRepository;
    private final EmailService emailService;
    private final GroupService groupService;

    public MilestoneService(MilestoneRepository milestoneRepository, ProjectGroupRepository groupRepository,
                            SubmissionRepository submissionRepository,
                            EmailService emailService, GroupService groupService) {
        this.milestoneRepository = milestoneRepository;
        this.groupRepository = groupRepository;
        this.submissionRepository = submissionRepository;
        this.emailService = emailService;
        this.groupService = groupService;
    }

    public Milestone createMilestone(MilestoneDTO dto) {
        ProjectGroup group = groupRepository.findById(dto.getGroupId())
                .orElseThrow(() -> new RuntimeException("Group not found"));

        Milestone milestone = new Milestone();
        milestone.setTitle(dto.getTitle());
        milestone.setDeadline(LocalDateTime.parse(dto.getDeadline()));
        milestone.setType(MilestoneType.valueOf(dto.getType()));
        milestone.setGroup(group);

        Milestone saved = milestoneRepository.save(milestone);
        logger.info("AUDIT - Milestone created: id={} type={} groupId={} deadline={}",
                saved.getId(), saved.getType(), group.getId(), saved.getDeadline());

        notifyGroupMembers(group, "New Milestone: " + saved.getTitle(),
                "A new milestone '" + saved.getTitle() + "' has been created with deadline: " + saved.getDeadline());

        return saved;
    }

    public Milestone updateMilestone(Long id, MilestoneDTO dto) {
        Milestone milestone = milestoneRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Milestone not found"));

        milestone.setTitle(dto.getTitle());
        milestone.setDeadline(LocalDateTime.parse(dto.getDeadline()));
        milestone.setType(MilestoneType.valueOf(dto.getType()));

        Milestone saved = milestoneRepository.save(milestone);
        logger.info("AUDIT - Milestone updated: id={} type={} deadline={}", saved.getId(), saved.getType(), saved.getDeadline());
        return saved;
    }

    public void uploadTemplate(Long milestoneId, MultipartFile file) throws IOException {
        Milestone milestone = milestoneRepository.findById(milestoneId)
                .orElseThrow(() -> new RuntimeException("Milestone not found"));

        milestone.setTemplateFileName(file.getOriginalFilename());
        milestone.setTemplateFileData(file.getBytes());
        milestone.setTemplateFileType(file.getContentType());

        milestoneRepository.save(milestone);
        logger.info("AUDIT - Template uploaded: milestoneId={} fileName={}", milestoneId, file.getOriginalFilename());

        notifyGroupMembers(milestone.getGroup(), "Template Uploaded: " + milestone.getTitle(),
                "A template file '" + file.getOriginalFilename() + "' has been uploaded for milestone: " + milestone.getTitle());
    }

    public Milestone findById(Long id) {
        return milestoneRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Milestone not found"));
    }

    @Transactional
    public void deleteMilestone(Long id) {
        Milestone milestone = milestoneRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Milestone not found"));
        ProjectGroup group = milestone.getGroup();
        String title = milestone.getTitle();

        submissionRepository.findByGroupId(group.getId()).stream()
                .filter(s -> s.getMilestone().getId().equals(id))
                .forEach(submissionRepository::delete);

        milestoneRepository.delete(milestone);
        logger.info("AUDIT - Milestone deleted: id={} title={} groupId={}", id, title, group.getId());

        notifyGroupMembers(group, "Milestone Deleted: " + title,
                "The milestone '" + title + "' has been deleted by your supervisor.");
    }

    public List<Milestone> findByGroupId(Long groupId) {
        return milestoneRepository.findByGroupId(groupId);
    }

    public List<Milestone> findApproachingDeadlines(LocalDateTime start, LocalDateTime end) {
        return milestoneRepository.findByDeadlineBetween(start, end);
    }

    private void notifyGroupMembers(ProjectGroup group, String subject, String body) {
        groupService.getMembers(group.getId()).forEach(member ->
                emailService.sendEmail(member.getStudent().getEmail(), subject, body));
    }
}
