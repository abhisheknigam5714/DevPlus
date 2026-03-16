package com.devplus.service;

import com.devplus.dto.MemberStatsResponse;
import com.devplus.model.*;
import com.devplus.repository.CommitLogRepository;
import com.devplus.repository.TaskRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CommitService {
    
    private final CommitLogRepository commitLogRepository;
    private final ProjectService projectService;
    private final TaskRepository taskRepository;
    
    public List<CommitLog> getCommitsByProject(Long projectId) {
        Project project = projectService.getProjectById(projectId);
        return commitLogRepository.findByProjectOrderByCommittedAtDesc(project);
    }
    
    public List<CommitLog> getCommitsByUser(Long projectId, Long userId) {
        Project project = projectService.getProjectById(projectId);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return commitLogRepository.findByProjectAndUserOrderByCommittedAtDesc(project, user);
    }
    
    public List<MemberStatsResponse> getMemberStats(Long projectId) {
        Project project = projectService.getProjectById(projectId);
        List<ProjectMember> members = projectService.getProjectMembers(projectId);
        
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime weekStart = now.minusDays(7);
        
        return members.stream()
                .map(member -> buildMemberStats(member, project, weekStart, now))
                .collect(Collectors.toList());
    }
    
    private MemberStatsResponse buildMemberStats(ProjectMember member, Project project, LocalDateTime weekStart, LocalDateTime now) {
        User user = member.getUser();
        
        // Get commit counts
        Long totalCommits = commitLogRepository.countByUserAndProject(user, project);
        Long weeklyCommits = commitLogRepository.countByUserAndProjectAndCommittedAtBetween(user, project, weekStart, now);
        
        // Get task counts
        Long totalTasks = taskRepository.countByAssignedToAndProject(user, project);
        Long completedTasks = taskRepository.countByAssignedToAndProjectAndStatus(user, project, Status.DONE);
        Long weeklyCompletedTasks = taskRepository.countByAssignedToAndStatusAndUpdatedAtBetween(user, Status.DONE, weekStart, now);
        
        // Get last commit info
        CommitLog lastCommit = commitLogRepository.findLatestByProjectAndUser(project, user);
        
        int daysSinceLastCommit = 0;
        boolean isActive = true;
        
        if (lastCommit != null) {
            daysSinceLastCommit = (int) ChronoUnit.DAYS.between(lastCommit.getCommittedAt(), now);
            isActive = daysSinceLastCommit <= 7;
        } else {
            daysSinceLastCommit = -1; // No commits
            isActive = false;
        }
        
        return MemberStatsResponse.builder()
                .userId(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .githubUsername(user.getGithubUsername())
                .roleInProject(member.getRoleInProject())
                .totalCommits(totalCommits)
                .weeklyCommits(weeklyCommits)
                .totalTasks(totalTasks)
                .completedTasks(completedTasks)
                .weeklyCompletedTasks(weeklyCompletedTasks)
                .lastCommitMessage(lastCommit != null ? lastCommit.getCommitMessage() : null)
                .lastCommitDate(lastCommit != null ? lastCommit.getCommittedAt() : null)
                .daysSinceLastCommit(daysSinceLastCommit)
                .isActive(isActive)
                .build();
    }
    
    public Long getTotalCommitsThisWeek(Project project, LocalDateTime start, LocalDateTime end) {
        return commitLogRepository.countByProjectAndCommittedAtBetween(project, start, end);
    }
    
    public List<CommitLog> getCommitsThisWeek(Project project, LocalDateTime start, LocalDateTime end) {
        return commitLogRepository.findByProjectAndCommittedAtBetween(project, start, end);
    }
    
    // Need to inject UserRepository
    private final com.devplus.repository.UserRepository userRepository;
}