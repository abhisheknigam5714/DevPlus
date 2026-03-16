package com.devplus.service;

import com.devplus.dto.AddMemberRequest;
import com.devplus.dto.ProjectRequest;
import com.devplus.model.Project;
import com.devplus.model.ProjectMember;
import com.devplus.model.User;
import com.devplus.repository.ProjectMemberRepository;
import com.devplus.repository.ProjectRepository;
import com.devplus.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProjectService {
    
    private final ProjectRepository projectRepository;
    private final ProjectMemberRepository projectMemberRepository;
    private final UserRepository userRepository;
    
    @Transactional
    public Project createProject(ProjectRequest request, User creator) {
        Project project = Project.builder()
                .name(request.getName())
                .description(request.getDescription())
                .githubRepoUrl(request.getGithubRepoUrl())
                .githubOwner(request.getGithubOwner())
                .githubRepoName(request.getGithubRepoName())
                .createdBy(creator)
                .build();
        
        project = projectRepository.save(project);
        
        // Add creator as manager in project
        ProjectMember projectMember = ProjectMember.builder()
                .project(project)
                .user(creator)
                .roleInProject(ProjectMember.RoleInProject.MANAGER)
                .build();
        projectMemberRepository.save(projectMember);
        
        return project;
    }
    
    public List<Project> getProjectsByCreator(User creator) {
        return projectRepository.findByCreatedBy(creator);
    }
    
    public List<Project> getProjectsForMember(User member) {
        return projectMemberRepository.findProjectsByUser(member);
    }
    
    public Project getProjectById(Long id) {
        return projectRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Project not found with id: " + id));
    }
    
    @Transactional
    public void deleteProject(Long id, User user) {
        Project project = getProjectById(id);
        if (!project.getCreatedBy().getId().equals(user.getId())) {
            throw new RuntimeException("Only the project creator can delete the project");
        }
        projectRepository.delete(project);
    }
    
    @Transactional
    public ProjectMember addMember(Long projectId, AddMemberRequest request) {
        Project project = getProjectById(projectId);
        
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found with email: " + request.getEmail()));
        
        if (projectMemberRepository.existsByProjectAndUser(project, user)) {
            throw new RuntimeException("User is already a member of this project");
        }
        
        ProjectMember member = ProjectMember.builder()
                .project(project)
                .user(user)
                .roleInProject(request.getRoleInProject())
                .build();
        
        return projectMemberRepository.save(member);
    }
    
    public List<ProjectMember> getProjectMembers(Long projectId) {
        Project project = getProjectById(projectId);
        return projectMemberRepository.findByProject(project);
    }
    
    @Transactional
    public void removeMember(Long projectId, Long userId) {
        Project project = getProjectById(projectId);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));
        
        projectMemberRepository.deleteByProjectAndUser(project, user);
    }
    
    public boolean isUserInProject(Long projectId, Long userId) {
        Project project = getProjectById(projectId);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return projectMemberRepository.existsByProjectAndUser(project, user);
    }
    
    public ProjectMember.RoleInProject getUserRoleInProject(Long projectId, Long userId) {
        Project project = getProjectById(projectId);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return projectMemberRepository.findByProjectAndUser(project, user)
                .map(ProjectMember::getRoleInProject)
                .orElse(null);
    }
}