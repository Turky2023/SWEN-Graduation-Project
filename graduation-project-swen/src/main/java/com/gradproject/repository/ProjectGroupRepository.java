package com.gradproject.repository;

import com.gradproject.entity.ProjectGroup;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ProjectGroupRepository extends JpaRepository<ProjectGroup, Long> {
    List<ProjectGroup> findBySupervisorId(Long supervisorId);
}
