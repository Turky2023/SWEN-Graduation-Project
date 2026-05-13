package com.gradproject.service;

import com.gradproject.entity.*;
import com.gradproject.repository.GroupMemberRepository;
import com.gradproject.repository.MessageRepository;
import com.gradproject.repository.ProjectGroupRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MessageService {

    private static final Logger logger = LoggerFactory.getLogger(MessageService.class);
    private static final int MAX_MESSAGE_LENGTH = 2000;

    private final MessageRepository messageRepository;
    private final ProjectGroupRepository groupRepository;
    private final GroupMemberRepository memberRepository;

    public MessageService(MessageRepository messageRepository, ProjectGroupRepository groupRepository,
                          GroupMemberRepository memberRepository) {
        this.messageRepository = messageRepository;
        this.groupRepository = groupRepository;
        this.memberRepository = memberRepository;
    }

    public Message send(Long groupId, User sender, String content) {
        if (content == null || content.trim().isEmpty()) {
            throw new RuntimeException("Message cannot be empty");
        }
        if (content.length() > MAX_MESSAGE_LENGTH) {
            throw new RuntimeException("Message too long (max " + MAX_MESSAGE_LENGTH + " characters)");
        }

        ProjectGroup group = groupRepository.findById(groupId)
                .orElseThrow(() -> new RuntimeException("Group not found"));

        if (!hasAccess(group, sender)) {
            throw new RuntimeException("You do not have access to this group's chat");
        }

        Message msg = new Message();
        msg.setGroup(group);
        msg.setSender(sender);
        msg.setContent(content.trim());

        Message saved = messageRepository.save(msg);
        logger.info("AUDIT - Message sent: id={} groupId={} senderId={}", saved.getId(), groupId, sender.getId());
        return saved;
    }

    public List<Message> getMessages(Long groupId, User user, Long afterId) {
        ProjectGroup group = groupRepository.findById(groupId)
                .orElseThrow(() -> new RuntimeException("Group not found"));

        if (!hasAccess(group, user)) {
            throw new RuntimeException("You do not have access to this group's chat");
        }

        if (afterId != null && afterId > 0) {
            return messageRepository.findByGroupIdAndIdGreaterThanOrderBySentAtAsc(groupId, afterId);
        }
        return messageRepository.findByGroupIdOrderBySentAtAsc(groupId);
    }

    private boolean hasAccess(ProjectGroup group, User user) {

        if (group.getSupervisor() != null && group.getSupervisor().getId().equals(user.getId())) {
            return true;
        }

        return memberRepository.existsByGroupAndStudent(group, user);
    }
}
