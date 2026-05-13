package com.gradproject.dto;

public class FeedbackDTO {
    private String comment;
    private Integer grade;
    private String status;

    public String getComment() { return comment; }
    public void setComment(String comment) { this.comment = comment; }

    public Integer getGrade() { return grade; }
    public void setGrade(Integer grade) { this.grade = grade; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
