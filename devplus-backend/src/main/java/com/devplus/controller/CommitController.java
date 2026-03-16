package com.devplus.controller;

import com.devplus.dto.MemberStatsResponse;
import com.devplus.model.CommitLog;
import com.devplus.model.User;
import com.devplus.service.AuthService;
import com.devplus.service.CommitService;
import com.devplus.service.ProjectService;
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
public class CommitController {
    
    private final CommitService commitService;
    private final ProjectService projectService;
    private final AuthService authService;
    
    @GetMapping("/projects/{projectId}/commits")
    public ResponseEntity<?> getProjectCommits(@PathVariable Long projectId,
                                               @AuthenticationPrincipal UserDetails userDetails) {
        User user = authService.getCurrentUser(userDetails.getUsername());
        
        if (!projectService.isUserInProject(projectId, user.getId())) {
            return ResponseEntity.status(403).body(Map.of("message", "Access denied"));
        }
        
        List<CommitLog> commits = commitService.getCommitsByProject(projectId);
        return ResponseEntity.ok(commits.stream()
                .map(this::toCommitResponse)
                .collect(Collectors.toList()));
    }
    
    @GetMapping("/projects/{projectId}/commits/stats")
    public ResponseEntity<?> getProjectCommitStats(@PathVariable Long projectId,
                                                   @AuthenticationPrincipal UserDetails userDetails) {
        User user = authService.getCurrentUser(userDetails.getUsername());
        
        if (!projectService.isUserInProject(projectId, user.getId())) {
            return ResponseEntity.status(403).body(Map.of("message", "Access denied"));
        }
        
        List<MemberStatsResponse> stats = commitService.getMemberStats(projectId);
        return ResponseEntity.ok(stats);
    }
    
    @GetMapping("/projects/{projectId}/commits/member/{userId}")
    public ResponseEntity<?> getMemberCommits(@PathVariable Long projectId,
                                              @PathVariable Long userId,
                                              @AuthenticationPrincipal UserDetails userDetails) {
        User currentUser = authService.getCurrentUser(userDetails.getUsername());
        
        if (!projectService.isUserInProject(projectId, currentUser.getId())) {
            return ResponseEntity.status(403).body(Map.of("message", "Access denied"));
        }
        
        List<CommitLog> commits = commitService.getCommitsByUser(projectId, userId);
        return ResponseEntity.ok(commits.stream()
                .map(this::toCommitResponse)
                .collect(Collectors.toList()));
    }
    
    @GetMapping("/my-commits")
    public ResponseEntity<?> getMyCommits(@RequestParam Long projectId,
                                          @AuthenticationPrincipal UserDetails userDetails) {
        User user = authService.getCurrentUser(userDetails.getUsername());
        
        List<CommitLog> commits = commitService.getCommitsByUser(projectId, user.getId());
        return ResponseEntity.ok(commits.stream()
                .map(this::toCommitResponse)
                .collect(Collectors.toList()));
    }
    
    private CommitResponse toCommitResponse(CommitLog commit) {
        return CommitResponse.builder()
                .id(commit.getId())
                .projectId(commit.getProject().getId())
                .userId(commit.getUser() != null ? commit.getUser().getId() : null)
                .userName(commit.getUser() != null ? commit.getUser().getName() : null)
                .githubUsername(commit.getGithubUsername())
                .commitSha(commit.getCommitSha())
                .commitMessage(commit.getCommitMessage())
                .branchName(commit.getBranchName())
                .committedAt(commit.getCommittedAt())
                .receivedAt(commit.getReceivedAt())
                .build();
    }
    
    @lombok.Data
    @lombok.Builder
    public static class CommitResponse {
        private Long id;
        private Long projectId;
        private Long userId;
        private String userName;
        private String githubUsername;
        private String commitSha;
        private String commitMessage;
        private String branchName;
        private java.time.LocalDateTime committedAt;
        private java.time.LocalDateTime receivedAt;
    }
}