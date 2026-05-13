package com.gradproject.entity;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "project_groups")
public class ProjectGroup {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @ManyToOne
    @JoinColumn(name = "supervisor_id")
    private User supervisor;

    @OneToMany(mappedBy = "group", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<GroupMember> members = new ArrayList<>();

    @OneToMany(mappedBy = "group", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Milestone> milestones = new ArrayList<>();

    @OneToMany(mappedBy = "group", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Submission> submissions = new ArrayList<>();

    public ProjectGroup() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public User getSupervisor() { return supervisor; }
    public void setSupervisor(User supervisor) { this.supervisor = supervisor; }

    public List<GroupMember> getMembers() { return members; }
    public void setMembers(List<GroupMember> members) { this.members = members; }

    public List<Milestone> getMilestones() { return milestones; }
    public void setMilestones(List<Milestone> milestones) { this.milestones = milestones; }

    public List<Submission> getSubmissions() { return submissions; }
    public void setSubmissions(List<Submission> submissions) { this.submissions = submissions; }
}
