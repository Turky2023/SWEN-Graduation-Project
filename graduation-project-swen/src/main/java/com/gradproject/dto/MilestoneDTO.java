package com.gradproject.dto;

public class MilestoneDTO {
    private String title;
    private String deadline;
    private String type;
    private Long groupId;

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDeadline() { return deadline; }
    public void setDeadline(String deadline) { this.deadline = deadline; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public Long getGroupId() { return groupId; }
    public void setGroupId(Long groupId) { this.groupId = groupId; }
}
