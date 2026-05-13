package com.gradproject.repository;

import com.gradproject.entity.GroupMember;
import com.gradproject.entity.ProjectGroup;
import com.gradproject.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface GroupMemberRepository extends JpaRepository<GroupMember, Long> {
    List<GroupMember> findByStudentId(Long studentId);
    List<GroupMember> findByGroup(ProjectGroup group);
    boolean existsByGroupAndStudent(ProjectGroup group, User student);
}
