package com.gradproject.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "group_members")
public class GroupMember {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "group_id", nullable = false)
    private ProjectGroup group;

    @ManyToOne
    @JoinColumn(name = "student_id", nullable = false)
    private User student;

    public GroupMember() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public ProjectGroup getGroup() { return group; }
    public void setGroup(ProjectGroup group) { this.group = group; }

    public User getStudent() { return student; }
    public void setStudent(User student) { this.student = student; }
}
