package com.gradproject.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "feedbacks")
public class Feedback {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(columnDefinition = "TEXT")
    private String comment;

    private Integer grade;

    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @OneToOne
    @JoinColumn(name = "submission_id", nullable = false)
    private Submission submission;

    public Feedback() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getComment() { return comment; }
    public void setComment(String comment) { this.comment = comment; }

    public Integer getGrade() { return grade; }
    public void setGrade(Integer grade) { this.grade = grade; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public Submission getSubmission() { return submission; }
    public void setSubmission(Submission submission) { this.submission = submission; }
}
