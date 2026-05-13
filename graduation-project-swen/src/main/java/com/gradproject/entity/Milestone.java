package com.gradproject.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "milestones")
public class Milestone {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private LocalDateTime deadline;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MilestoneType type;

    @ManyToOne
    @JoinColumn(name = "group_id", nullable = false)
    private ProjectGroup group;

    private String templateFileName;

    @Column(columnDefinition = "BYTEA")
    private byte[] templateFileData;

    private String templateFileType;

    public Milestone() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public LocalDateTime getDeadline() { return deadline; }
    public void setDeadline(LocalDateTime deadline) { this.deadline = deadline; }

    public MilestoneType getType() { return type; }
    public void setType(MilestoneType type) { this.type = type; }

    public ProjectGroup getGroup() { return group; }
    public void setGroup(ProjectGroup group) { this.group = group; }

    public String getTemplateFileName() { return templateFileName; }
    public void setTemplateFileName(String templateFileName) { this.templateFileName = templateFileName; }

    public byte[] getTemplateFileData() { return templateFileData; }
    public void setTemplateFileData(byte[] templateFileData) { this.templateFileData = templateFileData; }

    public String getTemplateFileType() { return templateFileType; }
    public void setTemplateFileType(String templateFileType) { this.templateFileType = templateFileType; }
}
