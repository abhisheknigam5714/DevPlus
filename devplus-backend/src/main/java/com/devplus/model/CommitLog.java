package com.devplus.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "commit_logs")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CommitLog {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;
    
    @Column(name = "github_username", nullable = false)
    private String githubUsername;
    
    @Column(name = "commit_sha", nullable = false, length = 40)
    private String commitSha;
    
    @Column(name = "commit_message", nullable = false, length = 500)
    private String commitMessage;
    
    @Column(name = "branch_name", nullable = false)
    private String branchName;
    
    @Column(name = "committed_at", nullable = false)
    private LocalDateTime committedAt;
    
    @Column(name = "received_at", updatable = false)
    private LocalDateTime receivedAt;
    
    @PrePersist
    protected void onCreate() {
        receivedAt = LocalDateTime.now();
    }
}