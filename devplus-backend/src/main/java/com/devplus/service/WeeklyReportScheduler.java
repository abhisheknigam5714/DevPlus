package com.devplus.service;

import com.devplus.dto.MemberStatsResponse;
import com.devplus.model.*;
import com.devplus.repository.CommitLogRepository;
import com.devplus.repository.ProjectRepository;
import com.devplus.repository.TaskRepository;
import com.devplus.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@Slf4j
public class WeeklyReportScheduler {
    
    private final UserRepository userRepository;
    private final ProjectRepository projectRepository;
    private final ProjectService projectService;
    private final TaskRepository taskRepository;
    private final CommitLogRepository commitLogRepository;
    private final EmailService emailService;
    
    // Every Monday at 9:00 AM
    @Scheduled(cron = "0 0 9 * * MON")
    public void sendWeeklyReports() {
        log.info("Starting weekly report generation...");
        
        List<User> managers = userRepository.findByRole(Role.MANAGER);
        
        for (User manager : managers) {
            try {
                sendReportToManager(manager);
            } catch (Exception e) {
                log.error("Failed to send weekly report to manager {}: {}", manager.getEmail(), e.getMessage());
            }
        }
        
        log.info("Weekly report generation completed.");
    }
    
    private void sendReportToManager(User manager) {
        List<Project> projects = projectRepository.findByCreatedBy(manager);
        
        if (projects.isEmpty()) {
            log.info("Manager {} has no projects, skipping report.", manager.getEmail());
            return;
        }
        
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime weekStart = now.minusDays(7);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM dd, yyyy");
        String reportPeriod = weekStart.format(formatter) + " - " + now.format(formatter);
        
        List<EmailService.ProjectReport> projectReports = new ArrayList<>();
        
        for (Project project : projects) {
            EmailService.ProjectReport report = buildProjectReport(project, weekStart, now, reportPeriod);
            projectReports.add(report);
        }
        
        String subject = "📊 DevPlus Weekly Report — " + now.format(DateTimeFormatter.ofPattern("MMMM dd, yyyy"));
        String htmlContent = emailService.buildWeeklyReportHtml(manager.getName(), projectReports);
        
        emailService.sendWeeklyReport(manager.getEmail(), subject, htmlContent);
    }
    
    private EmailService.ProjectReport buildProjectReport(Project project, LocalDateTime weekStart, LocalDateTime now, String reportPeriod) {
        List<ProjectMember> members = projectService.getProjectMembers(project.getId());
        
        // Filter to only get TEAM_LEAD and MEMBER roles for stats
        List<ProjectMember> teamMembers = members.stream()
                .filter(m -> m.getRoleInProject() == ProjectMember.RoleInProject.TEAM_LEAD || 
                             m.getRoleInProject() == ProjectMember.RoleInProject.MEMBER)
                .collect(Collectors.toList());
        
        // Build member stats
        List<EmailService.MemberStat> memberStats = new ArrayList<>();
        Map<Long, Long> commitCounts = new java.util.HashMap<>();
        Map<Long, Integer> taskCounts = new java.util.HashMap<>();
        Map<Long, Long> weeklyCommitCounts = new java.util.HashMap<>();
        
        for (ProjectMember member : teamMembers) {
            User user = member.getUser();
            
            Long totalCommits = commitLogRepository.countByUserAndProject(user, project);
            Long weeklyCommits = commitLogRepository.countByUserAndProjectAndCommittedAtBetween(user, project, weekStart, now);
            Long totalTasks = taskRepository.countByAssignedToAndProject(user, project);
            Long completedTasks = taskRepository.countByAssignedToAndProjectAndStatus(user, project, Status.DONE);
            
            commitCounts.put(user.getId(), totalCommits);
            weeklyCommitCounts.put(user.getId(), weeklyCommits);
            taskCounts.put(user.getId(), completedTasks.intValue());
            
            memberStats.add(new EmailService.MemberStat(
                    user.getName(),
                    weeklyCommits,
                    taskRepository.countByAssignedToAndStatusAndUpdatedAtBetween(user, Status.DONE, weekStart, now).intValue(),
                    totalTasks.intValue()
            ));
        }
        
        // Find top contributor
        String topContributor = null;
        Integer topContributorCommits = 0;
        for (EmailService.MemberStat stat : memberStats) {
            if (stat.getWeeklyCommits() > topContributorCommits) {
                topContributorCommits = stat.getWeeklyCommits().intValue();
                topContributor = stat.getName();
            }
        }
        
        // Find most tasks completed
        String mostTasksCompleted = null;
        Integer mostTasksCompletedCount = 0;
        for (EmailService.MemberStat stat : memberStats) {
            if (stat.getWeeklyTasksDone() > mostTasksCompletedCount) {
                mostTasksCompletedCount = stat.getWeeklyTasksDone();
                mostTasksCompleted = stat.getName();
            }
        }
        
        // Find inactive members
        List<String> inactiveMembers = new ArrayList<>();
        for (ProjectMember member : teamMembers) {
            User user = member.getUser();
            Long weeklyCommits = weeklyCommitCounts.getOrDefault(user.getId(), 0L);
            
            if (weeklyCommits == 0) {
                CommitLog lastCommit = commitLogRepository.findLatestByProjectAndUser(project, user);
                String inactiveInfo = user.getName();
                if (lastCommit != null) {
                    long daysSince = java.time.temporal.ChronoUnit.DAYS.between(lastCommit.getCommittedAt(), now);
                    inactiveInfo += " (last commit: " + daysSince + " days ago)";
                } else {
                    inactiveInfo += " (no commits yet)";
                }
                inactiveMembers.add(inactiveInfo);
            }
        }
        
        // Calculate totals
        Long totalCommitsWeek = commitLogRepository.countByProjectAndCommittedAtBetween(project, weekStart, now);
        List<Task> completedTasksWeek = taskRepository.findByProjectAndUpdatedAtBetweenAndStatus(project, weekStart, now);
        
        return new EmailService.ProjectReport(
                project.getName(),
                project.getGithubRepoUrl() != null ? project.getGithubRepoUrl() : "No repo linked",
                reportPeriod,
                memberStats,
                topContributor,
                topContributorCommits,
                mostTasksCompleted,
                mostTasksCompletedCount,
                inactiveMembers,
                totalCommitsWeek,
                (long) completedTasksWeek.size()
        );
    }
}