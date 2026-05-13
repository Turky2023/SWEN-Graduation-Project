package com.gradproject.service;

import com.gradproject.dto.GroupDTO;
import com.gradproject.entity.*;
import com.gradproject.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class GroupService {

    private static final Logger logger = LoggerFactory.getLogger(GroupService.class);

    private final ProjectGroupRepository groupRepository;
    private final GroupMemberRepository memberRepository;
    private final UserRepository userRepository;

    public GroupService(ProjectGroupRepository groupRepository, GroupMemberRepository memberRepository,
                        UserRepository userRepository) {
        this.groupRepository = groupRepository;
        this.memberRepository = memberRepository;
        this.userRepository = userRepository;
    }

    public ProjectGroup createGroup(GroupDTO dto, User creator) {
        ProjectGroup group = new ProjectGroup();
        group.setName(dto.getName());
        group.setSupervisor(creator);

        ProjectGroup saved = groupRepository.save(group);
        logger.info("AUDIT - Group created: id={} name={} by supervisorId={}", saved.getId(), saved.getName(), creator.getId());
        return saved;
    }

    public void addMember(Long groupId, Long studentId) {
        ProjectGroup group = groupRepository.findById(groupId)
                .orElseThrow(() -> new RuntimeException("Group not found"));
        User student = userRepository.findById(studentId)
                .orElseThrow(() -> new RuntimeException("Student not found"));

        if (student.getRole() != Role.STUDENT) {
            throw new RuntimeException("Only students can be added as members");
        }
        if (memberRepository.existsByGroupAndStudent(group, student)) {
            throw new RuntimeException("Student is already a member of this group");
        }

        GroupMember member = new GroupMember();
        member.setGroup(group);
        member.setStudent(student);
        memberRepository.save(member);

        logger.info("AUDIT - Member added: groupId={} studentId={}", groupId, studentId);
    }

    public void removeMember(Long groupId, Long memberId) {
        GroupMember member = memberRepository.findById(memberId)
                .orElseThrow(() -> new RuntimeException("Member not found"));
        if (!member.getGroup().getId().equals(groupId)) {
            throw new RuntimeException("Member does not belong to this group");
        }
        memberRepository.delete(member);
        logger.info("AUDIT - Member removed: groupId={} memberId={}", groupId, memberId);
    }

    public ProjectGroup findById(Long id) {
        return groupRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Group not found"));
    }

    public List<ProjectGroup> findBySupervisor(Long supervisorId) {
        return groupRepository.findBySupervisorId(supervisorId);
    }

    public List<ProjectGroup> findByStudent(Long studentId) {
        List<GroupMember> memberships = memberRepository.findByStudentId(studentId);
        return memberships.stream().map(GroupMember::getGroup).toList();
    }

    public List<GroupMember> getMembers(Long groupId) {
        ProjectGroup group = findById(groupId);
        return memberRepository.findByGroup(group);
    }
}
