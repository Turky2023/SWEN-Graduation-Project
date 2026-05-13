package com.gradproject.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "submissions")
public class Submission {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String fileName;

    @Column(nullable = false, columnDefinition = "BYTEA")
    private byte[] fileData;

    @Column(nullable = false)
    private String fileType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SubmissionStatus status = SubmissionStatus.PENDING;

    @Column(nullable = false)
    private LocalDateTime uploadedAt = LocalDateTime.now();

    @ManyToOne
    @JoinColumn(name = "group_id", nullable = false)
    private ProjectGroup group;

    @ManyToOne
    @JoinColumn(name = "milestone_id", nullable = false)
    private Milestone milestone;

    @OneToOne(mappedBy = "submission", cascade = CascadeType.ALL, orphanRemoval = true)
    private Feedback feedback;

    public Submission() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getFileName() { return fileName; }
    public void setFileName(String fileName) { this.fileName = fileName; }

    public byte[] getFileData() { return fileData; }
    public void setFileData(byte[] fileData) { this.fileData = fileData; }

    public String getFileType() { return fileType; }
    public void setFileType(String fileType) { this.fileType = fileType; }

    public SubmissionStatus getStatus() { return status; }
    public void setStatus(SubmissionStatus status) { this.status = status; }

    public LocalDateTime getUploadedAt() { return uploadedAt; }
    public void setUploadedAt(LocalDateTime uploadedAt) { this.uploadedAt = uploadedAt; }

    public ProjectGroup getGroup() { return group; }
    public void setGroup(ProjectGroup group) { this.group = group; }

    public Milestone getMilestone() { return milestone; }
    public void setMilestone(Milestone milestone) { this.milestone = milestone; }

    public Feedback getFeedback() { return feedback; }
    public void setFeedback(Feedback feedback) { this.feedback = feedback; }
}
