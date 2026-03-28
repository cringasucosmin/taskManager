package com.taskmanager.dto;

import com.taskmanager.model.TaskStatus;

public class TaskRequest {
    private String title;
    private String description;
    private TaskStatus status;

    public TaskRequest() {}

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public TaskStatus getStatus() { return status; }
    public void setStatus(TaskStatus status) { this.status = status; }
}
