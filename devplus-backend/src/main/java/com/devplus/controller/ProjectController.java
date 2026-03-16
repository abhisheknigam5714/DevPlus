package com.devplus.controller;

import com.devplus.dto.AddMemberRequest;
import com.devplus.dto.ProjectRequest;
import com.devplus.model.Project;
import com.devplus.model.ProjectMember;
import com.devplus.model.Role;
import com.devplus.model.User;
import com.devplus.security.JwtUtil;
import com.devplus.service.AuthService;
import com.devplus.service.ProjectService;
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
@RequestMapping("/api/projects")
@RequiredArgsConstructor
public class ProjectController {
    
    private final ProjectService projectService;
    private final AuthService authService;
    private final JwtUtil jwtUtil;
    
    @GetMapping
    public ResponseEntity<?> getProjects(@AuthenticationPrincipal UserDetails userDetails) {
        User user = authService.getCurrentUser(userDetails.getUsername());
        
        List<Project> projects;
        if (user.getRole() == Role.MANAGER) {
            projects = projectService.getProjectsByCreator(user);
        } else {
            projects = projectService.getProjectsForMember(user);
        }
        
        return ResponseEntity.ok(projects.stream()
                .map(this::toProjectResponse)
                .collect(Collectors.toList()));
    }
    
    @PostMapping
    public ResponseEntity<?> createProject(@Valid @RequestBody ProjectRequest request,
                                           @AuthenticationPrincipal UserDetails userDetails) {
        User user = authService.getCurrentUser(userDetails.getUsername());
        
        if (user.getRole() != Role.MANAGER) {
            return ResponseEntity.status(403).body(Map.of("message", "Only managers can create projects"));
        }
        
        Project project = projectService.createProject(request, user);
        return ResponseEntity.ok(toProjectResponse(project));
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<?> getProject(@PathVariable Long id,
                                        @AuthenticationPrincipal UserDetails userDetails) {
        User user = authService.getCurrentUser(userDetails.getUsername());
        
        Project project = projectService.getProjectById(id);
        
        // Check if user has access
        if (!projectService.isUserInProject(id, user.getId())) {
            return ResponseEntity.status(403).body(Map.of("message", "Access denied"));
        }
        
        return ResponseEntity.ok(toProjectResponse(project));
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteProject(@PathVariable Long id,
                                           @AuthenticationPrincipal UserDetails userDetails) {
        User user = authService.getCurrentUser(userDetails.getUsername());
        
        try {
            projectService.deleteProject(id, user);
            return ResponseEntity.ok(Map.of("message", "Project deleted successfully"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }
    
    @PostMapping("/{id}/members")
    public ResponseEntity<?> addMember(@PathVariable Long id,
                                       @Valid @RequestBody AddMemberRequest request,
                                       @AuthenticationPrincipal UserDetails userDetails) {
        User user = authService.getCurrentUser(userDetails.getUsername());
        
        // Check if user is manager of the project
        ProjectMember.RoleInProject role = projectService.getUserRoleInProject(id, user.getId());
        if (role != ProjectMember.RoleInProject.MANAGER) {
            return ResponseEntity.status(403).body(Map.of("message", "Only project managers can add members"));
        }
        
        try {
            ProjectMember member = projectService.addMember(id, request);
            return ResponseEntity.ok(toMemberResponse(member));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }
    
    @GetMapping("/{id}/members")
    public ResponseEntity<?> getMembers(@PathVariable Long id,
                                        @AuthenticationPrincipal UserDetails userDetails) {
        User user = authService.getCurrentUser(userDetails.getUsername());
        
        if (!projectService.isUserInProject(id, user.getId())) {
            return ResponseEntity.status(403).body(Map.of("message", "Access denied"));
        }
        
        List<ProjectMember> members = projectService.getProjectMembers(id);
        return ResponseEntity.ok(members.stream()
                .map(this::toMemberResponse)
                .collect(Collectors.toList()));
    }
    
    @DeleteMapping("/{projectId}/members/{userId}")
    public ResponseEntity<?> removeMember(@PathVariable Long projectId,
                                          @PathVariable Long userId,
                                          @AuthenticationPrincipal UserDetails userDetails) {
        User user = authService.getCurrentUser(userDetails.getUsername());
        
        ProjectMember.RoleInProject role = projectService.getUserRoleInProject(projectId, user.getId());
        if (role != ProjectMember.RoleInProject.MANAGER) {
            return ResponseEntity.status(403).body(Map.of("message", "Only project managers can remove members"));
        }
        
        try {
            projectService.removeMember(projectId, userId);
            return ResponseEntity.ok(Map.of("message", "Member removed successfully"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }
    
    private ProjectResponse toProjectResponse(Project project) {
        return ProjectResponse.builder()
                .id(project.getId())
                .name(project.getName())
                .description(project.getDescription())
                .githubRepoUrl(project.getGithubRepoUrl())
                .githubOwner(project.getGithubOwner())
                .githubRepoName(project.getGithubRepoName())
                .createdById(project.getCreatedBy().getId())
                .createdByName(project.getCreatedBy().getName())
                .createdAt(project.getCreatedAt())
                .build();
    }
    
    private MemberResponse toMemberResponse(ProjectMember member) {
        return MemberResponse.builder()
                .id(member.getId())
                .userId(member.getUser().getId())
                .userName(member.getUser().getName())
                .userEmail(member.getUser().getEmail())
                .userGithubUsername(member.getUser().getGithubUsername())
                .roleInProject(member.getRoleInProject())
                .joinedAt(member.getJoinedAt())
                .build();
    }
    
    @lombok.Data
    @lombok.Builder
    public static class ProjectResponse {
        private Long id;
        private String name;
        private String description;
        private String githubRepoUrl;
        private String githubOwner;
        private String githubRepoName;
        private Long createdById;
        private String createdByName;
        private java.time.LocalDateTime createdAt;
    }
    
    @lombok.Data
    @lombok.Builder
    public static class MemberResponse {
        private Long id;
        private Long userId;
        private String userName;
        private String userEmail;
        private String userGithubUsername;
        private ProjectMember.RoleInProject roleInProject;
        private java.time.LocalDateTime joinedAt;
    }
}