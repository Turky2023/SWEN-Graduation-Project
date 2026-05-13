package com.gradproject.repository;

import com.gradproject.entity.Milestone;
import com.gradproject.entity.ProjectGroup;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDateTime;
import java.util.List;

public interface MilestoneRepository extends JpaRepository<Milestone, Long> {
    List<Milestone> findByGroup(ProjectGroup group);
    List<Milestone> findByGroupId(Long groupId);

    @Query("SELECT m FROM Milestone m WHERE m.deadline BETWEEN :start AND :end")
    List<Milestone> findByDeadlineBetween(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);
}
