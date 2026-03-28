package com.taskmanager.service;

import com.taskmanager.model.Task;
import com.taskmanager.model.TaskStatus;
import com.taskmanager.repository.TaskRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TaskService {

    private final TaskRepository taskRepository;

    public TaskService(TaskRepository taskRepository) {
        this.taskRepository = taskRepository;
    }

    public List<Task> getTasksForUser(Long userId, String statusFilter) {
        if (statusFilter != null && !statusFilter.isBlank() && !statusFilter.equalsIgnoreCase("ALL")) {
            TaskStatus status = TaskStatus.valueOf(statusFilter.toUpperCase());
            return taskRepository.findByUserIdAndStatus(userId, status);
        }
        return taskRepository.findByUserId(userId);
    }

    public Task createTask(String title, String description, Long userId) {
        if (title == null || title.isBlank()) {
            throw new IllegalArgumentException("Title is required");
        }
        Task task = new Task(title, description, userId);
        return taskRepository.save(task);
    }

    public Task updateTask(Long taskId, String title, String description, TaskStatus status, Long userId) {
        Task task = taskRepository.findById(taskId)
            .orElseThrow(() -> new IllegalArgumentException("Task not found"));

        // Users can only update their own tasks
        if (!task.getUserId().equals(userId)) {
            throw new IllegalArgumentException("Access denied");
        }
        if (title != null && !title.isBlank()) {
            task.setTitle(title);
        }
        if (description != null) {
            task.setDescription(description);
        }
        if (status != null) {
            task.setStatus(status);
        }
        return taskRepository.save(task);
    }

    public void deleteTask(Long taskId, Long userId) {
        Task task = taskRepository.findById(taskId)
            .orElseThrow(() -> new IllegalArgumentException("Task not found"));

        // Users can only delete their own tasks
        if (!task.getUserId().equals(userId)) {
            throw new IllegalArgumentException("Access denied");
        }
        taskRepository.delete(task);
    }
}
