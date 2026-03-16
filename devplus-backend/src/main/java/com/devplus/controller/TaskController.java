package com.devplus.controller;

import com.devplus.dto.TaskRequest;
import com.devplus.model.*;
import com.devplus.service.AuthService;
import com.devplus.service.ProjectService;
import com.devplus.service.TaskService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class TaskController {
    
    private final TaskService taskService;
    private final ProjectService projectService;
    private final AuthService authService;
    
    @GetMapping("/projects/{projectId}/tasks")
    public ResponseEntity<?> getProjectTasks(@PathVariable Long projectId,
                                             @RequestParam(required = false) Long assigneeId,
                                             @AuthenticationPrincipal UserDetails userDetails) {
        User user = authService.getCurrentUser(userDetails.getUsername());
        
        if (!projectService.isUserInProject(projectId, user.getId())) {
            return ResponseEntity.status(403).body(Map.of("message", "Access denied"));
        }
        
        List<Task> tasks;
        if (assigneeId != null) {
            tasks = taskService.getTasksByAssignee(projectId, assigneeId);
        } else {
            tasks = taskService.getTasksByProject(projectId);
        }
        
        return ResponseEntity.ok(tasks.stream()
                .map(this::toTaskResponse)
                .collect(Collectors.toList()));
    }
    
    @PostMapping("/projects/{projectId}/tasks")
    public ResponseEntity<?> createTask(@PathVariable Long projectId,
                                        @Valid @RequestBody TaskRequest request,
                                        @AuthenticationPrincipal UserDetails userDetails) {
        User user = authService.getCurrentUser(userDetails.getUsername());
        
        ProjectMember.RoleInProject role = projectService.getUserRoleInProject(projectId, user.getId());
        if (role != ProjectMember.RoleInProject.MANAGER && role != ProjectMember.RoleInProject.TEAM_LEAD) {
            return ResponseEntity.status(403).body(Map.of("message", "Only managers and team leads can create tasks"));
        }
        
        try {
            Task task = taskService.createTask(projectId, request, user);
            return ResponseEntity.ok(toTaskResponse(task));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }
    
    @PutMapping("/tasks/{id}")
    public ResponseEntity<?> updateTask(@PathVariable Long id,
                                        @RequestBody TaskRequest request,
                                        @AuthenticationPrincipal UserDetails userDetails) {
        User user = authService.getCurrentUser(userDetails.getUsername());
        
        try {
            Task task = taskService.updateTask(id, request, user);
            return ResponseEntity.ok(toTaskResponse(task));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }
    
    @PatchMapping("/tasks/{id}/status")
    public ResponseEntity<?> updateTaskStatus(@PathVariable Long id,
                                              @RequestBody Map<String, String> request,
                                              @AuthenticationPrincipal UserDetails userDetails) {
        User user = authService.getCurrentUser(userDetails.getUsername());
        
        try {
            Status status = Status.valueOf(request.get("status"));
            Task task = taskService.updateTaskStatus(id, status, user);
            return ResponseEntity.ok(toTaskResponse(task));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }
    
    @DeleteMapping("/tasks/{id}")
    public ResponseEntity<?> deleteTask(@PathVariable Long id,
                                        @AuthenticationPrincipal UserDetails userDetails) {
        User user = authService.getCurrentUser(userDetails.getUsername());
        
        Task task = taskService.getTaskById(id);
        ProjectMember.RoleInProject role = projectService.getUserRoleInProject(task.getProject().getId(), user.getId());
        
        if (role != ProjectMember.RoleInProject.MANAGER && role != ProjectMember.RoleInProject.TEAM_LEAD) {
            return ResponseEntity.status(403).body(Map.of("message", "Only managers and team leads can delete tasks"));
        }
        
        try {
            taskService.deleteTask(id);
            return ResponseEntity.ok(Map.of("message", "Task deleted successfully"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }
    
    @GetMapping("/my-tasks")
    public ResponseEntity<?> getMyTasks(@AuthenticationPrincipal UserDetails userDetails) {
        User user = authService.getCurrentUser(userDetails.getUsername());
        List<Task> tasks = taskService.getTasksByUser(user);
        return ResponseEntity.ok(tasks.stream()
                .map(this::toTaskResponse)
                .collect(Collectors.toList()));
    }
    
    private TaskResponse toTaskResponse(Task task) {
        return TaskResponse.builder()
                .id(task.getId())
                .title(task.getTitle())
                .description(task.getDescription())
                .projectId(task.getProject().getId())
                .projectName(task.getProject().getName())
                .assignedToId(task.getAssignedTo() != null ? task.getAssignedTo().getId() : null)
                .assignedToName(task.getAssignedTo() != null ? task.getAssignedTo().getName() : null)
                .createdById(task.getCreatedBy().getId())
                .createdByName(task.getCreatedBy().getName())
                .priority(task.getPriority())
                .status(task.getStatus())
                .dueDate(task.getDueDate())
                .createdAt(task.getCreatedAt())
                .updatedAt(task.getUpdatedAt())
                .build();
    }
    
    @lombok.Data
    @lombok.Builder
    public static class TaskResponse {
        private Long id;
        private String title;
        private String description;
        private Long projectId;
        private String projectName;
        private Long assignedToId;
        private String assignedToName;
        private Long createdById;
        private String createdByName;
        private Priority priority;
        private Status status;
        private java.time.LocalDate dueDate;
        private java.time.LocalDateTime createdAt;
        private java.time.LocalDateTime updatedAt;
    }
}