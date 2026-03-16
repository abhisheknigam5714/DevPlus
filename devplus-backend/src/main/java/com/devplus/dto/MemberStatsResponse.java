package com.devplus.dto;

import com.devplus.model.ProjectMember;
import com.devplus.model.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MemberStatsResponse {
    
    private Long userId;
    private String name;
    private String email;
    private String githubUsername;
    private ProjectMember.RoleInProject roleInProject;
    
    private Long totalCommits;
    private Long weeklyCommits;
    private Long totalTasks;
    private Long completedTasks;
    private Long weeklyCompletedTasks;
    
    private String lastCommitMessage;
    private LocalDateTime lastCommitDate;
    
    private Integer daysSinceLastCommit;
    private boolean isActive;
}