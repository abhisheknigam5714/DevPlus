package com.devplus.service;

import com.devplus.dto.TaskRequest;
import com.devplus.model.*;
import com.devplus.repository.TaskRepository;
import com.devplus.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TaskService {
    
    private final TaskRepository taskRepository;
    private final ProjectService projectService;
    private final UserRepository userRepository;
    
    @Transactional
    public Task createTask(Long projectId, TaskRequest request, User creator) {
        Project project = projectService.getProjectById(projectId);
        
        Task.TaskBuilder builder = Task.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .project(project)
                .createdBy(creator)
                .priority(request.getPriority() != null ? request.getPriority() : Priority.MEDIUM)
                .status(request.getStatus() != null ? request.getStatus() : Status.TODO)
                .dueDate(request.getDueDate());
        
        if (request.getAssignedToId() != null) {
            User assignee = userRepository.findById(request.getAssignedToId())
                    .orElseThrow(() -> new RuntimeException("Assignee not found with id: " + request.getAssignedToId()));
            builder.assignedTo(assignee);
        }
        
        return taskRepository.save(builder.build());
    }
    
    public List<Task> getTasksByProject(Long projectId) {
        Project project = projectService.getProjectById(projectId);
        return taskRepository.findByProject(project);
    }
    
    public List<Task> getTasksByAssignee(Long projectId, Long assigneeId) {
        Project project = projectService.getProjectById(projectId);
        User assignee = userRepository.findById(assigneeId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return taskRepository.findByProjectAndAssignedTo(project, assignee);
    }
    
    public Task getTaskById(Long id) {
        return taskRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Task not found with id: " + id));
    }
    
    @Transactional
    public Task updateTask(Long id, TaskRequest request, User currentUser) {
        Task task = getTaskById(id);
        
        if (request.getTitle() != null) {
            task.setTitle(request.getTitle());
        }
        if (request.getDescription() != null) {
            task.setDescription(request.getDescription());
        }
        if (request.getPriority() != null) {
            task.setPriority(request.getPriority());
        }
        if (request.getDueDate() != null) {
            task.setDueDate(request.getDueDate());
        }
        if (request.getStatus() != null) {
            // Only allow assignee to update status, or manager/team lead to update any field
            if (task.getAssignedTo() != null && task.getAssignedTo().getId().equals(currentUser.getId())) {
                task.setStatus(request.getStatus());
            } else if (projectService.getUserRoleInProject(task.getProject().getId(), currentUser.getId()) != null) {
                task.setStatus(request.getStatus());
            }
        }
        if (request.getAssignedToId() != null) {
            User assignee = userRepository.findById(request.getAssignedToId())
                    .orElseThrow(() -> new RuntimeException("Assignee not found"));
            task.setAssignedTo(assignee);
        }
        
        return taskRepository.save(task);
    }
    
    @Transactional
    public Task updateTaskStatus(Long id, Status status, User currentUser) {
        Task task = getTaskById(id);
        
        // Verify the user is the assignee
        if (task.getAssignedTo() == null || !task.getAssignedTo().getId().equals(currentUser.getId())) {
            // Check if user is manager or team lead
            ProjectMember.RoleInProject role = projectService.getUserRoleInProject(
                    task.getProject().getId(), currentUser.getId());
            if (role != ProjectMember.RoleInProject.MANAGER && role != ProjectMember.RoleInProject.TEAM_LEAD) {
                throw new RuntimeException("You can only update your own tasks");
            }
        }
        
        task.setStatus(status);
        return taskRepository.save(task);
    }
    
    @Transactional
    public void deleteTask(Long id) {
        Task task = getTaskById(id);
        taskRepository.delete(task);
    }
    
    public List<Task> getTasksByUser(User user) {
        return taskRepository.findByAssignedTo(user);
    }
    
    public List<Task> getCompletedTasksThisWeek(Project project, LocalDateTime start, LocalDateTime end) {
        return taskRepository.findByProjectAndUpdatedAtBetweenAndStatus(project, start, end);
    }
}