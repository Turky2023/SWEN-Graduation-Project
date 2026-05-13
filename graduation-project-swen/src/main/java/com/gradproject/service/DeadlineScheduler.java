package com.gradproject.service;

import com.gradproject.entity.Milestone;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class DeadlineScheduler {

    private static final Logger logger = LoggerFactory.getLogger(DeadlineScheduler.class);

    private final MilestoneService milestoneService;
    private final GroupService groupService;
    private final EmailService emailService;

    public DeadlineScheduler(MilestoneService milestoneService, GroupService groupService,
                             EmailService emailService) {
        this.milestoneService = milestoneService;
        this.groupService = groupService;
        this.emailService = emailService;
    }

    @Scheduled(cron = "0 0 8 * * *")
    public void checkApproachingDeadlines() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime in48Hours = now.plusHours(48);

        List<Milestone> approaching = milestoneService.findApproachingDeadlines(now, in48Hours);

        for (Milestone milestone : approaching) {
            logger.info("AUDIT - Deadline approaching: milestoneId={} title={} deadline={}",
                    milestone.getId(), milestone.getTitle(), milestone.getDeadline());

            groupService.getMembers(milestone.getGroup().getId()).forEach(member ->
                    emailService.sendEmail(
                            member.getStudent().getEmail(),
                            "Deadline Approaching: " + milestone.getTitle(),
                            "The deadline for '" + milestone.getTitle() + "' is " +
                            milestone.getDeadline() + ". Please submit your work before the deadline."
                    )
            );
        }
    }
}
